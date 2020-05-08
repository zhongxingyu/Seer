 package org.dawb.workbench.plotting.tools;
 
 import java.util.Arrays;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class ZoomTool extends ProfileTool {
 	
 	private static Logger logger = LoggerFactory.getLogger(ZoomTool.class);
 
 	@Override
 	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected String getRegionName() {
 		return "Zoom";
 	}
 
 	@Override
 	protected boolean isRegionTypeSupported(RegionType type) {
 		return type==RegionType.BOX;
 	}
 
 	@Override
 	protected RegionType getCreateRegionType() {
 		return RegionType.BOX;
 	}
 
 	@Override
	protected void createProfile(final IImageTrace  image, 
 			                     IRegion      region,
 			                     ROIBase      rbs, 
 			                     boolean      tryUpdate, 
 			                     boolean      isDrag,
 			                     IProgressMonitor monitor) {
 		
 		if (monitor.isCanceled()) return;
 		if (image==null) return;
 		
 		if (region.getRegionType()!=RegionType.BOX) return;
 
 		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
 		if (bounds==null) return;
 		if (!region.isVisible()) return;
 
 		if (monitor.isCanceled()) return;
 
 		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
 		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
 		
 		try {
 			final AbstractDataset slice = image.getData().getSlice(new int[] { (int) bounds.getPoint()[1], (int) bounds.getPoint()[0] },
 					                                               new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
 					                                               new int[] {yInc, xInc});
 			
 	
 			slice.setName(region.getName());
 			
 			// Calculate axes to have real values not size
 			AbstractDataset yLabels = null;
 			AbstractDataset xLabels = null;
 			if (image.getAxes()!=null) {
 				AbstractDataset xl = image.getAxes().get(0);
 				if (xl!=null) xLabels = getLabelsFromLabels(xl, bounds, 0);
 				AbstractDataset yl = image.getAxes().get(1);
 				if (yl!=null) yLabels = getLabelsFromLabels(yl, bounds, 1);
 			}
 			
 			if (yLabels==null) yLabels = IntegerDataset.arange(bounds.getPoint()[1], bounds.getEndPoint()[1], yInc);
 			if (xLabels==null) xLabels = IntegerDataset.arange(bounds.getPoint()[0], bounds.getEndPoint()[0], xInc);
 			
			final IImageTrace zoom_trace = (IImageTrace)profilePlottingSystem.updatePlot2D(slice, Arrays.asList(new AbstractDataset[]{xLabels, yLabels}), monitor);
 			registerTraces(region, Arrays.asList(new ITrace[]{zoom_trace}));
			Display.getDefault().syncExec(new Runnable()  {
				public void run() {
				     zoom_trace.setPaletteData(image.getPaletteData());
				}
			});
 			
 		} catch (IllegalArgumentException ne) {
 			// Occurs when slice outside
 			logger.trace("Slice outside bounds of image!", ne);
 		} catch (Throwable ne) {
 			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
 		}
 
 	}
 
 	private AbstractDataset getLabelsFromLabels(AbstractDataset xl, RectangularROI bounds, int axisIndex) {
 		try {
 			int fromIndex = (int)bounds.getPoint()[axisIndex];
 			int toIndex   = (int)bounds.getEndPoint()[axisIndex];
 			int step      = toIndex>fromIndex ? 1 : -1;
 			final AbstractDataset slice = xl.getSlice(new int[]{fromIndex}, new int[]{toIndex}, new int[]{step});
 			return slice;
 		} catch (Exception ne) {
 			return null;
 		}
 	}
 }
