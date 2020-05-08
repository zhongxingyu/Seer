 package org.dawb.workbench.plotting.tools;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IAxis;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.RectangularROIData;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class BoxProfileTool extends ProfileTool {
 
 	private IAxis xPixelAxis;
 	private IAxis yPixelAxis;
 
 	@Override
 	protected void createAxes(AbstractPlottingSystem plotter) {
 		if (xPixelAxis==null) {
 			this.xPixelAxis = plotter.getSelectedXAxis();
 			xPixelAxis.setTitle("X Pixel");
 		}
 		
 		if (yPixelAxis==null) {
 			this.yPixelAxis = plotter.createAxis("Y Pixel", false, SWT.TOP);		
 			plotter.getSelectedYAxis().setTitle("Intensity");
 		}
 	}
 
 	@Override
 	protected void createProfile(IImageTrace  image, 
 			                     IRegion      region, 
 			                     ROIBase      rbs, 
 			                     boolean      tryUpdate,
 			                     IProgressMonitor monitor) {
         
 		if (monitor.isCanceled()) return;
 		if (image==null) return;
 		
 		if (region.getRegionType()!=RegionType.BOX) return;
 
 		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
 		if (bounds==null) return;
 		if (!region.isVisible()) return;
 
 		if (monitor.isCanceled()) return;
 		
 		AbstractDataset[] box = ROIProfile.box(image.getData(), image.getMask(), bounds, true);
 
 		if (monitor.isCanceled()) return;
 				
 		final AbstractDataset x_intensity = box[0];
 		x_intensity.setName("X "+region.getName());
 		AbstractDataset xi = IntegerDataset.arange(x_intensity.getSize());
 		final AbstractDataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
 		x_indices.setName("X Pixel");
 		
 		final AbstractDataset y_intensity = box[1];
 		y_intensity.setName("Y "+region.getName());
 		AbstractDataset yi = IntegerDataset.arange(y_intensity.getSize());
 		final AbstractDataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
 		y_indices.setName("Y Pixel");
 
 		if (monitor.isCanceled()) return;
 		if (tryUpdate) {
 			final ILineTrace x_trace = (ILineTrace)plotter.getTrace("X "+region.getName());
 			final ILineTrace y_trace = (ILineTrace)plotter.getTrace("Y "+region.getName());
 			
 			if (x_trace!=null && y_trace!=null && !monitor.isCanceled()) {
 				getControl().getDisplay().syncExec(new Runnable() {
 					public void run() {
						plotter.setSelectedXAxis(xPixelAxis);
 						x_trace.setData(x_indices, x_intensity);
						plotter.setSelectedXAxis(yPixelAxis);
 						y_trace.setData(y_indices, y_intensity);
 					}
 				});
 			}		
 			
 		} else {
 						
 			if (monitor.isCanceled()) return;
 			plotter.setSelectedXAxis(xPixelAxis);
 			Collection<ITrace> plotted = plotter.createPlot1D(x_indices, Arrays.asList(new AbstractDataset[]{x_intensity}), monitor);
 			registerTraces(region, plotted);
 			
 			if (monitor.isCanceled()) return;
 			plotter.setSelectedXAxis(yPixelAxis);
 			plotted = plotter.createPlot1D(y_indices, Arrays.asList(new AbstractDataset[]{y_intensity}), monitor);
 			registerTraces(region, plotted);
 			
 		}
 			
 	}
 	
 	@Override
 	protected boolean isRegionTypeSupported(RegionType type) {
 		return type==RegionType.BOX;
 	}
 
 	@Override
 	protected RegionType getCreateRegionType() {
 		return RegionType.BOX;
 	}
 
 }
