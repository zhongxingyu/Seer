 package org.dawnsci.plotting.jreality;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawnsci.plotting.api.histogram.IImageService;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean;
 import org.dawnsci.plotting.api.trace.ISurfaceTrace;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.roi.SurfacePlotROI;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.PlatformUI;
 //import org.slf4j.Logger;
 //import org.slf4j.LoggerFactory;
 

 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * A class for holding surface trace data.
  * 
  * We may need to abstract some parts to a general 3D trace as more options are supported.
  * 
  * @author fcp94556
  *
  */
 public class SurfaceTrace extends Image3DTrace implements ISurfaceTrace{
 
 	//private static Logger logger = LoggerFactory.getLogger(SurfaceTrace.class);
 	private AbstractDataset        data;
 
 	public SurfaceTrace(JRealityPlotViewer plotter, String name) {
 		super(plotter, name);
 		plotType = PlottingMode.SURF2D;
		this.window = new RectangularROI(100, 100, 400, 400, 0);
 	}
 
 	/**
 	 * Also ignores data windows outside the data size.
 	 * @param window
 	 * @param monitor
 	 * @return status
 	 */
 	@Override
 	public IStatus setWindow(IROI window, IProgressMonitor monitor) {
 		// if a surface roi, we make sure there are no negative values
 		if (window instanceof SurfacePlotROI) {
 			int stXPt = (int) window.getPointX();
 			int stYPt = (int) window.getPointY();
 			stXPt = stXPt < 0 ? 0 : stXPt;
 			stYPt = stYPt < 0 ? 0 : stYPt;
 			((SurfacePlotROI)window).setPoint(stXPt, stYPt);
 		}
 		this.window = window;
 		if (plotter!=null && this.isActive())
 			return plotter.setSurfaceWindow(this.window, monitor);
 		return Status.OK_STATUS;
 	}
 
 	@Override
 	public void setWindow(IROI window) {
 		setWindow(window, null);
 	}
 
 	@Override
 	public IROI getWindow() {
 		return window;
 	}
 //
 //	private int[] normalize(int[] point, int maxX, int maxY) {
 //		if (point[0]<0) point[0]=0;
 //		if (point[0]>=maxX) point[0]=maxX-1;
 //		
 //		if (point[1]<0) point[1]=0;
 //		if (point[1]>=maxY) point[1]=maxY-1;
 //		return point;
 //	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setData(final IDataset data, List<? extends IDataset> axes) {
 
 		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
 		imageServiceBean.setImage(data);
 		
 		if (service==null) service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 		if (rescaleHistogram) {
 			final float[] fa = service.getFastStatistics(imageServiceBean);
 			setMin(fa[0]);
 			setMax(fa[1]);
 		}
 
 		if (axes!=null && axes.size()==2) {
 			axes = Arrays.asList(axes.get(0), axes.get(1), null);
 		}
 		
 		this.data = (AbstractDataset)data;
 		this.axes = (List<IDataset>) axes;
 		if (isActive()) {
 			plotter.updatePlot(createAxisValues(), plotter.getWindow(getWindow()), plotType, this.data);
 			
 			if (plottingSystem!=null) {
 				plottingSystem.fireTraceUpdated(new TraceEvent(this));
 			}
 		}
 	}
 
 	@Override
 	public boolean is3DTrace() {
 		return true;
 	}
 
 	@Override
 	public void dispose() {
 		try {
 			plotter.removeSurfaceTrace(this);
 			super.dispose();
 		} catch (Throwable ignored) {
 			// It's disposed anyway
 		}
 	}
 
 	@Override
 	public IDataset getData() throws RuntimeException {
 		return data;
 	}
 
 }
