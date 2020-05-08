 package org.dawb.workbench.plotting.tools;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.LinearROIData;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class LineProfileTool extends ProfileTool {
 
 	@Override
 	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
 		plotter.getSelectedXAxis().setTitle("Pixel");
 		plotter.getSelectedYAxis().setTitle("Intensity");
 	}
 
 	@Override
 	protected void createProfile(	IImageTrace  image, 
 						            IRegion      region, 
 						            ROIBase      rbs, 
 						            boolean      tryUpdate,
 				                    boolean      isDrag,
 						            IProgressMonitor monitor) {
         
 		if (monitor.isCanceled()) return;
 		if (image==null) return;
 		
 		if (!isRegionTypeSupported(region.getRegionType())) return;
 
 		final LinearROI bounds = (LinearROI) (rbs==null ? region.getROI() : rbs);
 		if (bounds==null) return;
 		if (!region.isVisible()) return;
 
 		if (monitor.isCanceled()) return;
		LinearROIData ld = new LinearROIData(bounds, image.getData(), 1d);
 
 		if (monitor.isCanceled()) return;
 		
		final AbstractDataset intensity = ld.getProfileData(0);
 		intensity.setName(region.getName());
		final AbstractDataset indices = ld.getXAxes()[0].toDataset();
 		indices.setName("Pixel");
 		
 		final ILineTrace trace = (ILineTrace)profilePlottingSystem.getTrace(region.getName());
 		if (tryUpdate && trace!=null) {
 			if (trace!=null && !monitor.isCanceled()) getControl().getDisplay().syncExec(new Runnable() {
 				public void run() {
 					trace.setData(indices, intensity);
 				}
 			});
 			
 		} else {
 			if (monitor.isCanceled()) return;
 			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new AbstractDataset[]{intensity}), monitor);
 			registerTraces(region, plotted);
 			
 		}
 		
 	}
 
 	@Override
 	protected boolean isRegionTypeSupported(RegionType type) {
 		return type==RegionType.LINE;
 	}
 
 	@Override
 	protected RegionType getCreateRegionType() {
 		return RegionType.LINE;
 	}
 
 }
