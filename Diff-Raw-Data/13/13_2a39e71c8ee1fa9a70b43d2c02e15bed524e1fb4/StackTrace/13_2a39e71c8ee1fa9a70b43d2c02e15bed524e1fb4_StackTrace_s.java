 package org.dawnsci.plotting.jreality;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawnsci.plotting.api.trace.ILineStackTrace;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 
 public class StackTrace extends PlotterTrace implements ILineStackTrace {
 
 	
 	private AbstractDataset[] stack;
 
 	public StackTrace(JRealityPlotViewer plotter2, String name2) {
 		super(plotter2, name2);
 	}
 
 	@Override
 	public AbstractDataset getData() {
 		throw new RuntimeException("Please use getStack() instead!");
 	}
 	
 	@Override
 	public IDataset[] getStack() {
 		return stack;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setData(List<? extends IDataset> axes, IDataset... s) {
 		if (axes!=null && axes.size()==2) {
 			axes = Arrays.asList(axes.get(0), axes.get(1), null);
 		}
 		
 		this.stack = getStack(s);
 		this.axes  = (List<IDataset>) axes;
 		
 		if (isActive()) {
			plotter.updatePlot(createAxisValues(), null, PlottingMode.ONED_THREED, stack);
 			
 			if (plottingSystem!=null) {
 				plottingSystem.fireTraceUpdated(new TraceEvent(this));
 			}
 		}
 
 	}
 	
 	@Override
 	protected List<AxisValues> createAxisValues() {
 		ArrayList<AxisValues> values = new ArrayList<AxisValues>();
 
 		int a = 0;
 		int nAxes;
 		final IDataset y, z;
 		if (axes == null) {
 			nAxes = 0;
 			values.add(new AxisValues(getLabel(a++), null));
 			y = null;
 			z = null;
 		} else {
 			nAxes = axes.size();
 			String l = getLabel(a++);
 			for (int i = 0; i < (nAxes - 2); i++) {
 				values.add(new AxisValues(l, DatasetUtils.convertToAbstractDataset(axes.get(i))));
 			}
 			y = axes.get(nAxes - 2);
 			z = axes.get(nAxes - 1);
 		}
 		values.add(new AxisValues(getLabel(a++), DatasetUtils.convertToAbstractDataset(y)));
 
 		final AxisValues zAxis;
 		if (z != null) {
 			AbstractDataset tz = DatasetUtils.convertToAbstractDataset(z);
 			if (window instanceof LinearROI && tz.getRank() == 1) {
 				final int x1 = window.getIntPoint()[0];
 				final int x2 = (int) Math.ceil(((LinearROI) window).getEndPoint()[0]);
 				tz = tz.getSliceView(new Slice(x1, x2));
 			}
 			zAxis = new AxisValues(getLabel(a), tz);
 		} else if (window instanceof LinearROI) {
 			final int x1 = window.getIntPoint()[0];
 			final int x2 = (int) Math.ceil(((LinearROI) window).getEndPoint()[0]);
 			final int len = x2 - x1;
 			zAxis = new AxisValues(getLabel(a), AbstractDataset.arange(len, AbstractDataset.INT32));
 		} else {
 			zAxis = new AxisValues(getLabel(a), null);
 		}
 		values.add(zAxis);
 		return values;
 	}
 
 	@Override
 	public boolean is3DTrace() {
 		return true;
 	}
 
 	@Override
 	public void setWindow(IROI window) {
 		setWindow(window, null);
 	}
 
 	@Override
 	protected void setActive(boolean active) {
 		super.setActive(active);
 		if (active && plotter!=null) // hack as the window is set in createStackTrace when trace is not active
 			plotter.setStackWindow(window);
 	}
 
 	@Override
 	public IStatus setWindow(IROI roi, IProgressMonitor monitor) {
 		window=roi;
 		if (plotter!=null && this.isActive()) plotter.setStackWindow(window);
 		return Status.OK_STATUS;
 	}
 
 	public void dispose() {
 		try {
 			plotter.removeStackTrace(this);
 			super.dispose();
 		} catch (Throwable ignored) {
 			// It's disposed anyway
 		}
 	}
 
 	@Override
 	public int getRank() {
 		return 1;
 	}
 }
