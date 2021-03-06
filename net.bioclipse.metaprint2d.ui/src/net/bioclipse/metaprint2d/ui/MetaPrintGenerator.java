/* *****************************************************************************
 * Copyright (c) 2009-2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.metaprint2d.ui;

 import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sf.metaprint2d.MetaPrintResult;
import net.bioclipse.metaprint2d.ui.prefs.MetaprintPrefs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;

/**
 * A Genrator responsible for drawing MetaPrint2D circles in JCP
 * @author ola
 *
 */
public class MetaPrintGenerator implements IGenerator<IAtomContainer> {

	/**
	 * Adds the ability to turn the generator on/off via a Handler
	 */
    public static class Visibility extends
    AbstractGeneratorParameter<Boolean> {
        public Boolean getDefault() {
            return true;
        }
    }
    private static IGeneratorParameter<Boolean> visible = new Visibility();

	public static void setVisible(Boolean visible1) {
		visible.setValue(visible1);
	}

	public MetaPrintGenerator() {

    }
    
    /**
     * Set up the colored M2D circles based on calculated properties
     */
    public IRenderingElement generate( IAtomContainer ac,
                                       RendererModel model ) {
    	

//        System.out.println("M2D Generator found AC: " + ac);

        ElementGroup group = new ElementGroup();
    	if (visible.getValue()==false) return group;

    	Object o = ac.getProperty( Metaprint2DConstants.METAPRINT_RESULT_PROPERTY );
        if (o==null) return group;
        
        //Read prefs for rendering params and compute real values
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        boolean renderSolid=store.getBoolean(MetaprintPrefs.RENDER_SOLID_CIRCLES );
        int circleRadiusPref = store.getInt( MetaprintPrefs.CIRCLE_RADIUS );
        double circleRadius=(double)circleRadiusPref / 10;
        if (circleRadius<=0 || circleRadius >1)
            circleRadius=0.4;

        //OLD
//        Map<Integer, Double> mrlist = MetaPrint2DHelper
//            .getNormResultsFromProperty( (String)o );
        
        Map<Integer, MetaPrintResult> mresmap = MetaPrint2DHelper
            .getMetaprintResultsFromProperty( (String)o );


        for(int i = 0;i<ac.getAtomCount();i++) {  //Loop over all atoms
            for (Integer ii : mresmap.keySet()){   //Loop over list of atom indices with M2D results
                if (ii.intValue()==i){
                    IAtom atom = ac.getAtom( i );
                    
                    MetaPrintResult mres=mresmap.get( ii );

                    Color drawColor=MetaPrint2DHelper
                    .getColorByNormValue( mres );

                    if(drawColor != null){
                        if (model.get( CompactAtom.class ) || renderSolid){
                            group.add( new OvalElement( atom.getPoint2d().x,
                                                        atom.getPoint2d().y,
                                                        circleRadius,true, drawColor ));
//                            group.add( new OvalElement( atom.getPoint2d().x,
//                                                        atom.getPoint2d().y,
//                                                        .4,true, drawColor ));
                        }
                        else{
                            
                            group.add( new OvalElement( atom.getPoint2d().x,
                                                        atom.getPoint2d().y,
                                                        circleRadius,false, drawColor ));
                            group.add( new OvalElement( atom.getPoint2d().x,
                                                        atom.getPoint2d().y,
                                                        circleRadius+0.002,false, drawColor ));
//                            group.add( new OvalElement( atom.getPoint2d().x,
//                                                        atom.getPoint2d().y,
//                                                        .4,false, drawColor ));
//                            group.add( new OvalElement( atom.getPoint2d().x,
//                                                        atom.getPoint2d().y,
//                                                        .402,false, drawColor ));
                        }
                    }
                }
            }
        }

        
        return group;
    }

    public List<IGeneratorParameter<?>> getParameters() {
        return Arrays.asList(
                new IGeneratorParameter<?>[] {
                	visible
                }
            );
    }
}
