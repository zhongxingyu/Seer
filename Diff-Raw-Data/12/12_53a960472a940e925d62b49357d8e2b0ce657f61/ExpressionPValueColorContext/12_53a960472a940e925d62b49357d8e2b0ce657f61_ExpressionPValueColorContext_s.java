 package org.geworkbench.bison.util.colorcontext;
 
 import java.awt.Color;
 import java.io.Serializable;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
 import org.geworkbench.bison.util.Range;
 
 public class ExpressionPValueColorContext implements ColorContext, Serializable {
 
 	private static final long serialVersionUID = 7733639267835219074L;
 
 	public ExpressionPValueColorContext() {
     }
 	
 	private transient Object lock = new Object();
 
     /**
      * @param mv        The <code>MarkerValue</code> that needs to be drawn.
      * @param intensity color intensity to be used
      * @return The <code>Color</code> to use for drawing.
      */
     public Color getMarkerValueColor(DSMarkerValue mv, DSGeneMarker mInfo, float intensity) {
 
 //        intensity *= 2;
         intensity = 2 / intensity; 
         double value = mv.getValue();
         synchronized (lock) {
         	org.geworkbench.bison.util.Range range = ((DSRangeMarker) mInfo).getRange();
 	        double mean = range.norm.getMean(); //(range.max + range.min) / 2.0;
 	        double foldChange = (value - mean) / (range.norm.getSigma() + 0.00001); //Math.log(change) / Math.log(2.0);
 	        if (foldChange < -intensity) {
 	            foldChange = -intensity;
 	        }
 	        if (foldChange > intensity) {
 	            foldChange = intensity;
 	        }
 	
 	        double colVal = foldChange / intensity;
 	        if (foldChange > 0) {
 	            return new Color(1.0F, (float) (1 - colVal), (float) (1 - colVal));
 	        } else {
 	            return new Color((float) (1 + colVal), (float) (1 + colVal), 1.0F);
 	        }
         }
        
     }
 
     // TODO: this needs to be reviewed: 
     // considering the range is each DSRangeMarker's property, why does it need to be updated here? 
 	public void updateContext(
 			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
 		DSMicroarraySet microarraySet = view.getMicroarraySet();
 
 		if (!microarraySet.getMarkers().isEmpty()) {
 
 			if (microarraySet.getMarkers().get(0) instanceof DSRangeMarker) {
 				synchronized (lock) {
 					for (DSGeneMarker marker : microarraySet.getMarkers()) {
 						((DSRangeMarker) marker).reset(marker.getSerial());
 					}
 					if (view.items().size() == 1) {
 						DSMicroarray ma = view.items().get(0);
 						Range range = new org.geworkbench.bison.util.Range();
 						for (DSGeneMarker marker : microarraySet.getMarkers()) {
 							DSMutableMarkerValue mValue = (DSMutableMarkerValue) ma
 									.getMarkerValue(marker.getSerial());
 							double value = mValue.getValue();
 							range.min = Math.min(range.min, value);
 							range.max = Math.max(range.max, value);
 							range.norm.add(value);
 						}
 						for (DSGeneMarker marker : microarraySet.getMarkers()) {
 							Range markerRange = ((DSRangeMarker) marker)
 									.getRange();
 							markerRange.min = range.min;
 							markerRange.max = range.max;
 							markerRange.norm = range.norm;
 						}
 					} else {
 						for (DSGeneMarker marker : microarraySet.getMarkers()) {
 							for (DSMicroarray ma : view.items()) {
 								DSMutableMarkerValue mValue = (DSMutableMarkerValue) ma
 										.getMarkerValue(marker.getSerial());
 								((DSRangeMarker) marker).updateRange(mValue);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
     public Color getMaxColorValue(float intensity) {
         return Color.red;
     }
 
     public Color getMinColorValue(float intensity) {
         return Color.blue;
     }
 
     public Color getMiddleColorValue(float intensity) {
         return Color.white;
     }
 }
