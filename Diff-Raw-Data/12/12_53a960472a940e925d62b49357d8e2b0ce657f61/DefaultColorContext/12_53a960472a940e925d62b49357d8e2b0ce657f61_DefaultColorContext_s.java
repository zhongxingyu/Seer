 package org.geworkbench.bison.util.colorcontext;
 
 import java.awt.Color;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  * @author First Genetic Trust
  * @version $Id$
  */
 
 /**
  * Default implementation of a color context. Assigns positive marker values
  * in the red spectrum and negative values to the green spectrum.
  */
 
 public class DefaultColorContext implements ColorContext {
 
 	private static final long serialVersionUID = -8105939139046926331L;
 
 	private final Color MISSING_VALUE_COLOR = Color.GRAY;
 
     private double magnitude;
 
     public DefaultColorContext() {
     }
 
     private transient Object lock = new Object();
 
     public Color getMarkerValueColor(DSMarkerValue mv, DSGeneMarker mInfo, float intensity) {
         if (mv == null || mv.isMissing())
             return MISSING_VALUE_COLOR;
         double value = mv.getValue();
         Color color = null;
         synchronized (lock) {
 	        float v = (float) (value * intensity / magnitude);
 	        if (v > 0) {
 	            v = (float)Math.min(1.0, v);
 	            // color = new Color(1.0f, (1 - v), (1 - v));
 	            color = new Color(v, 0, 0);
 	        } else {
 	            v = -v;
 	            v = (float)Math.min(1.0, v);
 	            // color = new Color((1- v), (1 - v), 1.0f);
 	            color = new Color(0, v, 0);
 	        }
         }
         return color;
     }
 
     public void updateContext(DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
         // Use entire set
         DSMicroarraySet set = view.getMicroarraySet();
         synchronized (lock) {
         	magnitude = 0.0;
 	        for (int i = 0; i < set.size(); i++) {
 	            for (int j = 0; j < set.getMarkers().size(); j++) {
 	                double value = Math.abs(set.getValue(j, i));
 	                if (value > magnitude) {
 	                    magnitude = value;
 	                }
 	            }
 	        }
         }
     }
 
     public Color getMaxColorValue(float intensity) {
         return new Color(1f, 0, 0);
     }
 
     public Color getMinColorValue(float intensity) {
         return new Color(0, 1f, 0);
     }
 
     public Color getMiddleColorValue(float intensity) {
         return Color.black;
     }
 }
