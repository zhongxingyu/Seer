 package org.dawb.common.ui.plot;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ISurfaceTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * Will be a thread safe version of all the plotting system methods.
  * 
  * @author fcp94556
  *
  */
 public class ThreadSafePlottingSystem implements IPlottingSystem {
 
 	private static final Logger logger = LoggerFactory.getLogger(ThreadSafePlottingSystem.class);
 	
 	private IPlottingSystem deligate;
 
 	public ThreadSafePlottingSystem(IPlottingSystem deligate) {
 		this.deligate = deligate;
 	}
 
 	@Override
 	public IImageTrace createImageTrace(String traceName) {
 		return (IImageTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
 	}
 
 	@Override
 	public ILineTrace createLineTrace(String traceName) {
 		return (ILineTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
 	}
 
 	@Override
 	public ISurfaceTrace createSurfaceTrace(String traceName) {
 		return (ISurfaceTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
 	}
 
 	@Override
 	public void addTrace(ITrace trace) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), trace);
 	}
 
 	@Override
 	public void removeTrace(ITrace trace) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), trace);
 	}
 
 	@Override
 	public ITrace getTrace(String name) {
 		return deligate.getTrace(name);
 	}
 
 	@Override
 	public Collection<ITrace> getTraces() {
 		return deligate.getTraces();
 	}
 
 	@Override
 	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
 		return deligate.getTraces(clazz);
 	}
 
 	@Override
 	public void addTraceListener(ITraceListener l) {
 		deligate.addTraceListener(l);
 	}
 
 	@Override
 	public void removeTraceListener(ITraceListener l) {
 		deligate.removeTraceListener(l);
 	}
 
 	@Override
 	public void renameTrace(ITrace trace, String name) throws Exception {
 		call(getMethodName(Thread.currentThread().getStackTrace()), trace, name);
 	}
 
 	@Override
 	public IRegion createRegion(String name, RegionType regionType) throws Exception {
 		return (IRegion)call(getMethodName(Thread.currentThread().getStackTrace()), name, regionType);
 	}
 
 	@Override
 	public void addRegion(IRegion region) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), region);
 	}
 
 	@Override
 	public void removeRegion(IRegion region) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), region);
 	}
 
 	@Override
 	public IRegion getRegion(String name) {
 		return deligate.getRegion(name);
 	}
 
 	@Override
 	public Collection<IRegion> getRegions(RegionType type) {
         return deligate.getRegions(type);
 	}
 
 	@Override
 	public boolean addRegionListener(IRegionListener l) {
 		return deligate.addRegionListener(l);
 	}
 
 	@Override
 	public boolean removeRegionListener(IRegionListener l) {
 		return deligate.removeRegionListener(l);
 	}
 
 	@Override
 	public void clearRegions() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public Collection<IRegion> getRegions() {
 		return deligate.getRegions();
 	}
 
 	@Override
 	public void renameRegion(IRegion region, String name) throws Exception {
 		call(getMethodName(Thread.currentThread().getStackTrace()), region, name);
 	}
 
 	@Override
 	public IAxis createAxis(String title, boolean isYAxis, int side) {
		return 	(IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), 
				           new Class[]{String.class, boolean.class, int.class},
				           title, isYAxis, side);
 	}
 
 	@Override
 	public IAxis getSelectedYAxis() {
 		return deligate.getSelectedYAxis();
 	}
 
 	@Override
 	public void setSelectedYAxis(IAxis yAxis) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), yAxis);
 	}
 
 	@Override
 	public IAxis getSelectedXAxis() {
 		return deligate.getSelectedXAxis();
 	}
 
 	@Override
 	public void setSelectedXAxis(IAxis xAxis) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), xAxis);
 	}
 
 	@Override
 	public void autoscaleAxes() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public IAnnotation createAnnotation(String name) throws Exception {
 		return (IAnnotation)call(getMethodName(Thread.currentThread().getStackTrace()), name);
 	}
 
 	@Override
 	public void addAnnotation(IAnnotation annot) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), annot);
 	}
 
 	@Override
 	public void removeAnnotation(IAnnotation annot) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), annot);
 	}
 
 	@Override
 	public IAnnotation getAnnotation(String name) {
 		return deligate.getAnnotation(name);
 	}
 
 	@Override
 	public void clearAnnotations() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public void renameAnnotation(IAnnotation annotation, String name)
 			throws Exception {
 		call(getMethodName(Thread.currentThread().getStackTrace()), annotation, name);
 	}
 
 	@Override
 	public void printPlotting() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public void copyPlotting() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public String savePlotting(String filename) throws Exception {
 		return (String)call(getMethodName(Thread.currentThread().getStackTrace()), filename);
 	}
 
 	@Override
 	public void savePlotting(String filename, String filetype) throws Exception {
 		call(getMethodName(Thread.currentThread().getStackTrace()), filename, filetype);
 	}
 
 	@Override
 	public void setTitle(String title) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), title);
 	}
 
 	@Override
 	public void createPlotPart(Composite parent, 
 			                   String plotName,
 			                   IActionBars bars, 
 			                   PlotType hint, 
 			                   IWorkbenchPart part) {
 		
 		throw new RuntimeException("Cannot call createPlotPart, only allowed to use this from python!");
 	}
 
 	@Override
 	public String getPlotName() {
 		return deligate.getPlotName();
 	}
 
 	@Override
 	public List<ITrace> createPlot1D(AbstractDataset x, List<AbstractDataset> ys, IProgressMonitor monitor) {
 		return deligate.createPlot1D(x, ys, monitor);
 	}
 
 	@Override
 	public List<ITrace> createPlot1D(AbstractDataset x,
 			List<AbstractDataset> ys, String title, IProgressMonitor monitor) {
 		return deligate.createPlot1D(x, ys, title, monitor);
 	}
 
 	@Override
 	public List<ITrace> updatePlot1D(AbstractDataset x,
 			List<AbstractDataset> ys, IProgressMonitor monitor) {
 		return deligate.updatePlot1D(x, ys, monitor);
 	}
 
 	@Override
 	public ITrace createPlot2D(AbstractDataset image,
 			List<AbstractDataset> axes, IProgressMonitor monitor) {
 		return deligate.createPlot2D(image, axes, monitor);
 	}
 
 	@Override
 	public ITrace updatePlot2D(AbstractDataset image,
 			List<AbstractDataset> axes, IProgressMonitor monitor) {
 		return deligate.updatePlot2D(image, axes, monitor);
 	}
 
 	@Override
 	public void setPlotType(PlotType plotType) {
 		call(getMethodName(Thread.currentThread().getStackTrace()), plotType);
 	}
 
 	@Override
 	public void append(String dataSetName, Number xValue, Number yValue, IProgressMonitor monitor) throws Exception {
 		deligate.append(dataSetName, xValue, yValue, monitor);
 	}
 
 	@Override
 	public void reset() {
 		deligate.reset();
 	}
 
 	@Override
 	public void clear() {
 		deligate.clear();
 	}
 
 	@Override
 	public void dispose() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public void repaint() {
 		call(getMethodName(Thread.currentThread().getStackTrace()));
 	}
 
 	@Override
 	public Composite getPlotComposite() {
 		return deligate.getPlotComposite();
 	}
 
 	@Override
 	public ISelectionProvider getSelectionProvider() {
 		return deligate.getSelectionProvider();
 	}
 
 	@Override
 	public AbstractDataset getData(String dataSetName) {
 		return deligate.getData(dataSetName);
 	}
 
 	@Override
 	public PlotType getPlotType() {
 		return deligate.getPlotType();
 	}
 
 	@Override
 	public boolean is2D() {
 		return deligate.is2D();
 	}
 
 	@Override
 	public IActionBars getActionBars() {
 		return deligate.getActionBars();
 	}
 
 	@Override
 	public IPlotActionSystem getPlotActionSystem() {
 		return deligate.getPlotActionSystem();
 	}
 
 	@Override
 	public void setDefaultCursor(int cursorType) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{int.class}, cursorType);
 	}
 	
 	/**
 	 * Calls method in a SWT thread safe way.
 	 * @param methodName
 	 * @param args
 	 */
 	private Object call(final String methodName, final Object... args) {
 		
 		final Class[] classes = args!=null ? new Class[args.length] : null;
 		if (classes!=null) {
 			for (int i = 0; i < args.length; i++) classes[i]=args[i].getClass();
 		}
		return call(methodName, classes, args);
	}
	
	/**
	 * Calls method in a SWT thread safe way.
	 * @param methodName
	 * @param args
	 */
	private Object call(final String methodName, final Class[] classes, final Object... args) {
		
 		final List<Object> ret = new ArrayList<Object>(1);
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				try {
 				    Method method = deligate.getClass().getMethod(methodName, classes);
 				    Object val    = method.invoke(deligate, args);
 				    ret.add(val);
 				} catch (Exception ne) {
 					logger.error("Cannot execute "+methodName+" with "+args, ne);
 				}
 			}
 		});
 		return ret.get(0);
 	}
 
 	public static String getMethodName ( StackTraceElement ste[] ) {  
 		   
 	    String methodName = "";  
 	    boolean flag = false;  
 	   
 	    for ( StackTraceElement s : ste ) {  
 	   
 	        if ( flag ) {  
 	   
 	            methodName = s.getMethodName();  
 	            break;  
 	        }  
 	        flag = s.getMethodName().equals( "getStackTrace" );  
 	    }  
 	    return methodName;  
 	}  
 }
