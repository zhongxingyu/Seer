 package org.dawb.common.ui.plot.trace;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * Class containing utility methods for regions to avoid duplication 
  * @author fcp94556
  *
  */
 public class TraceUtils {
 
 	/**
 	 * Call to get a unique region name 
 	 * @param nameStub
 	 * @param system
 	 * @return
 	 */
 	public static String getUniqueTrace(final String nameStub, final IPlottingSystem system, final String... usedNames) {
 		int i = 1;
 		@SuppressWarnings("unchecked")
 		final List<String> used = (List<String>) (usedNames!=null ? Arrays.asList(usedNames) : Collections.emptyList());
 		while(system.getTrace(nameStub+" "+i)!=null || used.contains(nameStub+" "+i)) {
 			++i;
 			if (i>10000) break; // something went wrong!
 		}
 		return nameStub+" "+i;
 	}
 
 	/**
 	 * Removes a trace of this name if it is already there.
 	 * @param plottingSystem
 	 * @param string
 	 * @return
 	 */
 	public static final ILineTrace replaceCreateLineTrace(IPlottingSystem system, String name) {
 		if (system.getTrace(name)!=null) {
 			system.removeTrace(system.getTrace(name));
 		}
 		return system.createLineTrace(name);
 	}
 	
 	/**
 	 * Determine if IImageTrace has custom axes or not.
 	 */
 	public static boolean isCustomAxes(IImageTrace trace) {
 		
 		List<AbstractDataset> axes = trace.getAxes();
 		AbstractDataset      image = trace.getData();
 		
 		if (axes==null)     return false;
 		if (axes.isEmpty()) return false;
 		
 		if (axes.get(0).getDtype()!=AbstractDataset.INT32 || axes.get(1).getDtype()!=AbstractDataset.INT32) {
 			return true;
 		}
 		
 		if (axes.get(0).getSize() == image.getShape()[1] &&
 		    axes.get(1).getSize() == image.getShape()[0]) {
 			boolean startZero = axes.get(0).getDouble(0)==0d  &&
 				                axes.get(1).getDouble(0)==0d;
 			
 			if (!startZero) return true;
 			
 			double xEnd = axes.get(0).getDouble(axes.get(0).getSize()-1);
 			double yEnd = axes.get(1).getDouble(axes.get(1).getSize()-1);
 			
 			boolean maxSame =	xEnd==image.getShape()[1]-1 &&
 				                yEnd==image.getShape()[0]-1;
 			
 			if (maxSame) return false;
 		}
 		
 		return true;
 	}
 
 }
