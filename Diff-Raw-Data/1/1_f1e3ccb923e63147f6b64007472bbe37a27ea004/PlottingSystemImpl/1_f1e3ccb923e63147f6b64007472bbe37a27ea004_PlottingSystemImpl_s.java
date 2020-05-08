 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.plotting.system;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.dawb.common.services.IPaletteService;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlottingActionBarManager;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.annotation.IAnnotation;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.axis.IPositionListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.trace.IImageStackTrace;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ILineStackTrace;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ISurfaceTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.draw2d.swtxy.LineTrace;
 import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
 import org.dawnsci.plotting.jreality.JRealityPlotViewer;
 import org.dawnsci.plotting.preference.PlottingConstants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.Cursors;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.PageBook;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 
 
 /**
  * An implementation of IPlottingSystem, not designed to be public.
  * 
  * THIS CLASS SHOULD NOT BE USED OUTSIDE THIS PLUGIN!
  * 
  * THIS CLASS IS plugin private, do not export org.dawb.workbench.plotting.system from this plugin.
  * 
  * @author gerring
  *
  */
 public class PlottingSystemImpl extends AbstractPlottingSystem {
 
 	private Logger logger = LoggerFactory.getLogger(PlottingSystemImpl.class);
 	
 	private Composite      parent;
 	
 	private PlotActionsManagerImpl       actionBarManager;
 	private LightWeightPlotViewer        lightWeightViewer;
 	private JRealityPlotViewer           jrealityViewer;
 
 	private final String SHOW_LEGEND_ACTION_ID = "org.csstudio.swt.xygraph.toolbar.configureShow Legend";
 	
 	public PlottingSystemImpl() {
 		super();
 		this.actionBarManager     = (PlotActionsManagerImpl)super.actionBarManager;
 		this.lightWeightViewer    = new LightWeightPlotViewer();
 		this.jrealityViewer       = new JRealityPlotViewer();
 	}
 	
 	private boolean containerOverride = false;
 	
 	public void createPlotPart(final Composite      container,
 							   final String         plotName,
 							   final IActionBars    bars,
 							   final PlotType       hint,
 							   final IWorkbenchPart part) {
 
 		super.createPlotPart(container, plotName, bars, hint, part);
 		
 		if (container.getLayout() instanceof GridLayout) {
 			GridUtils.removeMargins(container);
 		}
 		
 		this.plottingMode = hint;
 		if (container.getLayout() instanceof PageBook.PageBookLayout) {
 			if (hint.is3D()) throw new RuntimeException("Cannot deal with "+PageBook.PageBookLayout.class.getName()+" and 3D at the moment!");
 		    this.parent       = container;
 		    logger.debug("Cannot deal with "+PageBook.PageBookLayout.class.getName()+" and 3D at the moment!");
 		} else {
 		    this.containerOverride = true;
 			this.parent       = new Composite(container, SWT.NONE);
 			final StackLayout layout = new StackLayout();
 			this.parent.setLayout(layout);
 			parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		}
 		
 		// We ignore hint, we create a light weight plot as default because
 		// it looks nice. We swap this for a 3D one if required.
 		createLightWeightUI();
 		
 		if (parent.getLayout() instanceof StackLayout) {
 			final StackLayout layout = (StackLayout)parent.getLayout();
 			layout.topControl = lightWeightViewer.getControl();
 			container.layout();
 		}
 	}
 	
 	@Override
 	protected PlottingActionBarManager createActionBarManager() {
 		return new PlotActionsManagerImpl(this);
 	}
 
 	
 	@Override
 	public Composite getPlotComposite() {
 		if (containerOverride) return parent;
 		if (plottingMode!=null && plottingMode.is3D()) return (Composite)jrealityViewer.getControl();
 		if (lightWeightViewer.getControl()!=null)      return (Composite)lightWeightViewer.getControl();
 		return null;
 	}
 	
 	private void createLightWeightUI() {
 
 		if (lightWeightViewer.getControl()!=null) return;
 		lightWeightViewer.init(this);
 		lightWeightViewer.createControl(parent);
 		parent.layout();
 	}
 
 	private void createJRealityUI() {
 
 		if (jrealityViewer.getControl()!=null) return;
 		jrealityViewer.init(this);
 		jrealityViewer.createControl(parent);
 		parent.layout();
 	}
 
 
 	public void setFocus() {
 		lightWeightViewer.setFocus();
 	}
 
 		
 	public void addTraceListener(final ITraceListener l) {
 		super.addTraceListener(l);
 		lightWeightViewer.addImageTraceListener(l);
 	}
 	public void removeTraceListener(final ITraceListener l) {
 		super.removeTraceListener(l);
 		lightWeightViewer.removeImageTraceListener(l);
 	}
 	
 	
 	public List<ITrace> updatePlot1D(IDataset             x, 
 						             final List<? extends IDataset> ys,
 						             final IProgressMonitor      monitor) {
 
 		final List<ITrace> updatedAndCreated = new ArrayList<ITrace>(3);		
 		final List<IDataset> unfoundYs = new ArrayList<IDataset>(ys.size());
 		
 		for (final IDataset y : ys) {
 			
 			final ITrace trace = getTrace(y.getName());
 			if (trace!=null && trace instanceof ILineTrace) {
 				
 				if (x==null) x = IntegerDataset.arange(y.getSize(), IntegerDataset.INT32);
 				final IDataset finalX = x;
 				final ILineTrace lineTrace = (ILineTrace)trace;
 				updatedAndCreated.add(lineTrace);
 				
 				if (getDisplay().getThread()==Thread.currentThread()) {
 					lineTrace.setData(finalX, y);
 				} else {
 					getDisplay().syncExec(new Runnable() {
 						public void run() {
 							lineTrace.setData(finalX, y);
 						}
 					});
 				}
 				continue;
 			}
 			unfoundYs.add(y);
 		}
 		
 		if (!unfoundYs.isEmpty()) {
 			if (x==null) x = IntegerDataset.arange(unfoundYs.get(0).getSize(), IntegerDataset.INT32);
 			final Collection<ITrace> news = createPlot1D(x, unfoundYs, monitor);
 			updatedAndCreated.addAll(news);
 		}
 		
 		return updatedAndCreated;
 	}
 	
 	@Override
 	public List<ITrace> createPlot1D(final IDataset       xIn, 
 					                 final List<? extends IDataset> ysIn,
 					                 final IProgressMonitor      monitor) {
         return this.createPlot1D(xIn, ysIn, null, monitor);
 	}
 
 	/**
 	 * Does not have to be called in UI thread.
 	 */
 	@Override
 	public List<ITrace> createPlot1D(final IDataset       xIn, 
 					                 final List<? extends IDataset> ysIn,
 					                 final String                title,
 					                 final IProgressMonitor      monitor) {
 		
 		if (monitor!=null) monitor.worked(1);
 
 		// create index datasets if necessary
 		final List<ITrace> traces = new ArrayList<ITrace>(7);
 		final IDataset x;
 		if (ysIn == null || ysIn.isEmpty()) {
 			return traces;
 		}
 
 		if (xIn == null) {
 			final int max = getMaxSize(ysIn);
 			x = AbstractDataset.arange(0, max, 1, AbstractDataset.INT32);
 			if (ysIn.size() == 1)
 				x.setName("Index of " + ysIn.get(0).getName());
 			else
 				x.setName("Indices");
 		} else {
 			x = xIn;
 		}
 
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			List<ITrace> ts = createPlot1DInternal(x, ysIn, title, monitor);
 			if (ts!=null) traces.addAll(ts);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					List<ITrace> ts = createPlot1DInternal(x, ysIn, title, monitor);
 					if (ts!=null) traces.addAll(ts);
 				}
 			});
 		}
 
 		if (monitor!=null) monitor.worked(1);
 		return traces;
 		
 	}
 	
 	private Display getDisplay() {
 		return Display.getDefault();
 	}
 
 	@Override
 	public void append( final String           name, 
 			            final Number           xValue,
 					    final Number           yValue,
 					    final IProgressMonitor monitor) throws Exception  {       
 		
 		if (!this.plottingMode.is1D()) throw new Exception("Can only add in 1D mode!");
 		if (name==null || "".equals(name)) throw new IllegalArgumentException("The dataset name must not be null or empty string!");
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			appendInternal(name, xValue, yValue, monitor);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					appendInternal(name, xValue, yValue, monitor);
 				}
 			});
 		}
 	}
 
 	/**
      * Do not call before createPlotPart(...)
      */
 	public void setPlotType(final PlotType mode) {
 		super.setPlotType(mode);
 		if (Thread.currentThread()==Display.getDefault().getThread()) {
 		    switchPlottingType(mode);
 		} else {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 				    switchPlottingType(mode);
 				}
 			});
 		}
 	}
 	
 	public ITrace updatePlot2D(final IDataset       data, 
 							   final List<? extends IDataset> axes,
 							   final IProgressMonitor      monitor) {
 		
 		if (plottingMode.is1D()) {
 			if (getDisplay().getThread()==Thread.currentThread()) {
 				switchPlottingType(PlotType.IMAGE);
 			} else {
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						switchPlottingType(PlotType.IMAGE);
 					}
 				});
 			}
 		}
 		
 		final Collection<ITrace> traces = plottingMode.is3D() 
 				                        ? getTraces(ISurfaceTrace.class)
 				                        : getTraces(IImageTrace.class);
 				                        
 		if (monitor!=null&&monitor.isCanceled()) return null;
 		if (traces!=null && traces.size()>0) {
 			
 			ITrace image = traces.iterator().next();
 			final int[]       shape = image.getData()!=null ? image.getData().getShape() : null;
 			if (shape!=null && Arrays.equals(shape, data.getShape())) {
 				if (getDisplay().getThread()==Thread.currentThread()) {
 					image = updatePlot2DInternal(image, data, axes, monitor);
 				} else {
 					final List<ITrace> images = Arrays.asList(image);
 					Display.getDefault().syncExec(new Runnable() {
 						public void run() {
 							// This will keep the previous zoom level if there was one
 							// and will be faster than createPlot2D(...) which autoscales.
 			                ITrace im = updatePlot2DInternal(images.get(0), data, axes, monitor);
 			                images.set(0, im);
 						}
 					});
 					image = images.get(0);
 				}
 				return image;
 			} else {
 				return createPlot2D(data, axes, monitor);
 			}
 		} else {
 		    return createPlot2D(data, axes, monitor);
 		}
 	}
 
 	private ITrace updatePlot2DInternal(final ITrace image,
 			                          final IDataset       data, 
 								      final List<? extends IDataset> axes,
 								      final IProgressMonitor      monitor) {
 		
 		if (data.getName()!=null) lightWeightViewer.setTitle(data.getName());
 		
 		if (monitor!=null&&monitor.isCanceled()) return null;
 		try {
 			if (image instanceof IImageTrace) {
 			    ((IImageTrace)image).setData(data, axes, false);
 			} else if (image instanceof ISurfaceTrace) {
 			    ((ISurfaceTrace)image).setData(data, axes);
 			}
 			return image;
 		} catch (Throwable ne) { // We create a new one then
 			clear();
 			return createPlot2D(data, axes, monitor);
 		}
 	}
 
 
 	/**
 	 * Must be called in UI thread. Creates and updates image.
 	 * NOTE removes previous traces if any plotted.
 	 * 
 	 * @param data
 	 * @param axes, x first.
 	 * @param monitor
 	 */
 	@Override
 	public ITrace createPlot2D(final IDataset       data, 
 							   final List<? extends IDataset> axes,
 							   final IProgressMonitor      monitor) {
   
 		final List<ITrace> traces = new ArrayList<ITrace>(7);
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			ITrace ts = createPlot2DInternal(data, axes, monitor);
 			if (ts!=null) traces.add(ts);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					ITrace ts = createPlot2DInternal(data, axes, monitor);
 					if (ts!=null) traces.add(ts);
 				}
 			});
 		}
 		
 		return traces.size()>0 ? traces.get(0) : null;
 	}
 
 	public ITrace createPlot2DInternal(final IDataset       data, 
 										List<? extends IDataset>       axes,
 										final IProgressMonitor      monitor) {
 		try {
 			if (plottingMode.is1D()) {
 				switchPlottingType(PlotType.IMAGE);
 			}
 
 			clearTraces(); // Only one image at a time!
             			
 			if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
 			traceMap.clear();
 			
 			String traceName = data.getName();
 			if (part!=null&&(traceName==null||"".equals(traceName))) {
 				traceName = part.getTitle();
 			}
 			if (monitor!=null&&monitor.isCanceled()) return null;
 			
 			ITrace trace=null;
 			if (plottingMode.is3D()) {
 				trace = createSurfaceTrace(traceName);
 				((ISurfaceTrace)trace).setData(data, axes);
 				addTrace(trace);
 			} else {
 				trace = lightWeightViewer.createLightWeightImage(traceName, data, axes, monitor);
 				traceMap.put(trace.getName(), trace);
 				fireTraceAdded(new TraceEvent(trace));
 			}
 
 			return trace;
             
 		} catch (Throwable e) {
 			logger.error("Cannot load file "+data.getName(), e);
 			return null;
 		}
 	}
 
 
 
 	@Override
 	public IImageTrace createImageTrace(String traceName) {
 		IImageTrace trace = lightWeightViewer.createImageTrace(traceName);
 		fireTraceCreated(new TraceEvent(trace));
 		return trace;
 	}
 	
 	@Override
 	public IImageStackTrace createImageStackTrace(String traceName) {
 		IImageStackTrace trace = lightWeightViewer.createImageStackTrace(traceName);
 		fireTraceCreated(new TraceEvent(trace));
 		return trace;
 	}
 
 	/**
 	 * An IdentityHashMap used to map AbstractDataset to color used to plot it.
 	 * records keys for both strings and sets so that different models for the
 	 * file being plotted work. Sometimes dataset name is unique but the set is
 	 * not, sometimes the dataset is unique but its name is not.
 	 */
 	private Map<Object, Color> colorMap; // Warning can be mem leak
 	
 	
 	/**
 	 * A map for recording traces to be used in the update method.
 	 * 
 	 * Uses a map of abstract data set name to Trace to retrieve Trace on the
 	 * update.
 	 */
 	private Map<String, ITrace> traceMap; // Warning can be mem leak
 
 
 	private List<ITrace> createPlot1DInternal(final IDataset       xIn, 
 										      final List<? extends IDataset> ysIn,
 										      final String                title,
 										      final IProgressMonitor      monitor) {
 		
 		if (plottingMode.is1Dor2D()) {
 		    this.plottingMode = PlotType.XY;
 		} else {
 			this.plottingMode = PlotType.XY_STACKED_3D;
 		}
 		switchPlottingType(plottingMode);
 
 		if (colorMap == null && getColorOption()!=ColorOption.NONE) {
 			if (getColorOption()==ColorOption.BY_NAME) {
 				colorMap = new HashMap<Object,Color>(ysIn.size());
 			} else {
 				colorMap = new IdentityHashMap<Object,Color>(ysIn.size());
 			}
 		}
 		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
 	
 		List<ITrace> traces=null;
 		if (plottingMode.is1D()) {
 			if (lightWeightViewer.getControl()==null) return null;	
 			traces = lightWeightViewer.createLineTraces(title, xIn, ysIn, traceMap, colorMap, monitor);
 			
 		} else {
 			traceMap.clear();
 			ILineStackTrace trace = jrealityViewer.createStackTrace(title);
 			final IDataset x = xIn;
 			final AbstractDataset y = AbstractDataset.arange(getMaxSize(ysIn), AbstractDataset.INT32);
 			final AbstractDataset z = AbstractDataset.arange(ysIn.size(), AbstractDataset.INT32);
 			trace.setData(Arrays.asList(x,y,z), ysIn.toArray(new AbstractDataset[ysIn.size()]));
 			jrealityViewer.addTrace(trace);
 			traceMap.put(trace.getName(), trace);
 			traces = Arrays.asList((ITrace)trace);
 		}
 		  	
 		fireTracesPlotted(new TraceEvent(traces));
         return traces;
 	}
 	
 	@SuppressWarnings("unused")
 	private boolean isAllInts(List<AbstractDataset> ysIn) {
 		for (AbstractDataset a : ysIn) {
 			if (a.getDtype()!=AbstractDataset.INT16 &&
 				a.getDtype()!=AbstractDataset.INT32 &&
 				a.getDtype()!=AbstractDataset.INT64) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	public ILineTrace createLineTrace(String traceName) {
 
 		final Axis xAxis = (Axis)getSelectedXAxis();
 		final Axis yAxis = (Axis)getSelectedYAxis();
 
 		LightWeightDataProvider traceDataProvider = new LightWeightDataProvider();
 		final LineTrace   trace    = new LineTrace(traceName, xAxis, yAxis, traceDataProvider);
 		final LineTraceImpl wrapper = new LineTraceImpl(this, trace);
 		fireTraceCreated(new TraceEvent(wrapper));
 		return wrapper;
 	}
 	
 
 	@Override
 	public ISurfaceTrace createSurfaceTrace(String traceName) {
 		
         ISurfaceTrace trace = jrealityViewer.createSurfaceTrace(traceName);
 		
         PaletteData palette = null;
 		if (trace.getPaletteData()==null) {
 			final Collection<ITrace> col = getTraces(IImageTrace.class);
 			if (col!=null && col.size()>0) {
 				palette = ((IImageTrace)col.iterator().next()).getPaletteData();
 			} else {
 				try {
 					final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
 					final String schemeName = Activator.getDefault().getPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);	
 					palette = pservice.getPaletteData(schemeName);
 				} catch (Exception e) {
 					palette = null;
 				}				
 			}
 			trace.setPaletteData(palette);
 		}
 
 		
 		return trace;
 	}
 	
 	@Override
 	public ILineStackTrace createLineStackTrace(String traceName) {	
 		return jrealityViewer.createStackTrace(traceName);
 	}
 	
 	protected void switchPlottingType( PlotType type ) {
 		
 		this.plottingMode=type;
 		this.actionBarManager.switchActions(plottingMode);
 		
 		Control top = null;
 		if (type.is3D()) { 
 			createJRealityUI();
 			top = jrealityViewer.getControl();
 			jrealityViewer.updatePlottingRole(type);
 		} else {
 			createLightWeightUI();
 			top = lightWeightViewer.getControl();
 			lightWeightViewer.updatePlottingRole(type);
 		}
 		if (parent.getLayout() instanceof StackLayout) {
 			final StackLayout layout = (StackLayout)parent.getLayout();
 			layout.topControl = top;
 			parent.layout();
 		}
 	}
 	
 	/**
 	 * Adds trace, makes visible
 	 * @param traceName
 	 * @return
 	 */
 	public void addTrace(ITrace trace) {
 		
 		if (traceMap==null) this.traceMap = new HashMap<String, ITrace>(7);
 		traceMap.put(trace.getName(), trace);
 		
 		if (trace.is3DTrace()) {
 			jrealityViewer.addTrace(trace);
 			fireTraceAdded(new TraceEvent(trace));
 			
 		} else { // 1D, an image or LineTrace
 			lightWeightViewer.addTrace(trace);
 			fireTraceAdded(new TraceEvent(trace));
 		}
 	}
 	/**
 	 * Removes a trace.
 	 * @param traceName
 	 * @return
 	 */
 	public void removeTrace(ITrace trace) {
 		if (traceMap!=null) traceMap.remove(trace.getName());
 		
 		if (trace instanceof ISurfaceTrace) { // TODO FIXME Others?
 			jrealityViewer.removeSurfaceTrace((ISurfaceTrace)trace);
 		} else {
 			lightWeightViewer.removeTrace(trace);
 		}
 
 		fireTraceRemoved(new TraceEvent(trace));
 	}
 
 	@Override
 	public void renameTrace(final ITrace trace, String name) {
 		if (traceMap!=null) traceMap.remove(trace.getName());
 		trace.setName(name);
 		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(3);
 		traceMap.put(name, trace);
 	}
 	
 	public Collection<ITrace> getTraces() {
 		if (traceMap==null) return Collections.emptyList();
 		return traceMap.values();
 	}
 
 	@Override
 	public ITrace getTrace(String name) {
 		if (traceMap==null) return null;
 		return traceMap.get(name);
 	}
 
 	private void appendInternal(final String           name, 
 					                  Number           xValue,
 							    final Number           yValue,
 							    final IProgressMonitor monitor) {
 
 
 		final ITrace wrapper = traceMap.get(name);
 		if (wrapper==null) return;
 		
 		final Trace trace = ((LineTraceImpl)wrapper).getTrace();
 		
 		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
 		if (prov==null) return;
 
 		prov.append(xValue, yValue);
 	}
 	
 	
     /**
      * Thread safe method
      */
 	@Override
 	public AbstractDataset getData(String name) {
 		
 		final ITrace wrapper = traceMap.get(name);
 		if (wrapper==null) return null;
 		
 		final Trace trace = ((LineTraceImpl)wrapper).getTrace();
 		if (trace==null) return null;
 		
 		return getData(name, trace, true);
 	}
 	
 	/**
 	 * Thread safe method
 	 * @param name
 	 * @param trace
 	 * @param isY
 	 * @return
 	 */
 	protected AbstractDataset getData(String name, Trace trace, boolean isY) {
 
 		if (trace==null) return null;
 		
 		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
 		if (prov==null) return null;
 		
 		return isY ? prov.getY() : prov.getX();
 	}
 
 	/**
 	 * Override this method to provide an implementation of title setting.
 	 * @param title
 	 */
 	public void setTitle(final String title) {
 		super.setTitle(title);
 		if (plottingMode.is3D()) {
 			jrealityViewer.setTitle(title);
 		} else {
 			lightWeightViewer.setTitle(title);
 		}
 	}
 	public String getTitle() {
 		if (plottingMode.is3D()) {
 			return jrealityViewer.getTitle();
 		} else {
 			return lightWeightViewer.getTitle();
 		}
 	}	
 	/**
 	 * Override this method to provide an implementation of show legend setting.
 	 * @param b
 	 */
 	@Override
 	public void setShowLegend(boolean b) {
 		IAction action = actionBarManager.findAction(SHOW_LEGEND_ACTION_ID);
 		action.setChecked(b);
 		if (lightWeightViewer!=null) {
 			lightWeightViewer.setShowLegend(b);
 		}
 	}
 
 	public Color get1DPlotColor(Object object) {
 		if (getColorOption()==ColorOption.NONE) return null;
 		if (colorMap==null) return null;
 		if (object==null) return null;
 		if (colorOption==ColorOption.BY_DATA) {
 			return colorMap.get(object);
 		} else if (colorOption==ColorOption.BY_NAME) {
 			return colorMap.get((String)object);
 		}
 		return null;
 	}
 
 	private int getMaxSize(List<? extends IDataset> sets) {
 		int max = 1; // Cannot be less than one
 		for (IDataset set : sets) {
 			if (set != null)
 			    max = Math.max(max, set.getSize());
 		}
 		
 		return max;
 	}
 
 	@Override
 	public void reset() {
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			resetInternal();
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					resetInternal();
 				}
 			});
 		}
 
 	}
 	private void resetInternal() {
 		
 		if (traceMap!=null) traceMap.clear();
 		if (colorMap!=null) colorMap.clear();
 		lightWeightViewer.reset();
 		jrealityViewer.reset();
 		fireTracesCleared(new TraceEvent(this));
 	}
 	
 	@Override
 	public void clear() {
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			clearInternal();
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					clearInternal();
 				}
 			});
 		}
 
 	}
 	private void clearInternal() {		
 		if (lightWeightViewer.getControl()!=null) {
 			try {
 				clearTraces();
 				if (colorMap!=null) colorMap.clear();	
 	
 			} catch (Throwable e) {
 				logger.error("Cannot remove traces!", e);
 			}
 		}	
 	}
 
 
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (colorMap!=null) {
 			colorMap.clear();
 			colorMap = null;
 		}
 		clearTraces();
 		lightWeightViewer.dispose();
 		jrealityViewer.dispose();
 	}
 
 	private void clearTraces() {
 		
 		if (lightWeightViewer!=null)  lightWeightViewer.clearTraces();
 		if (traceMap!=null) traceMap.clear();
 		fireTracesCleared(new TraceEvent(this));
 	}
 
 	public void repaint() {
 		repaint(isRescale());
 	}
 	
 	public void repaint(final boolean autoScale) {
 		lightWeightViewer.repaint(autoScale);
 	}
 	
 	/**
 	 * Creates an image of the same size as the Rectangle passed in.
 	 * @param size
 	 * @return
 	 */
 	@Override
 	public Image getImage(Rectangle size) {
 		return lightWeightViewer.getImage(size);
 	}
 	
 
 	/**
 	 * Use this method to create axes other than the default y and x axes.
 	 * 
 	 * @param title
 	 * @param isYAxis, normally it is.
 	 * @param side - either SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM
 	 * @return
 	 */
 	@Override
 	public IAxis createAxis(final String title, final boolean isYAxis, int side) {
 		
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 					
 		return lightWeightViewer.createAxis(title, isYAxis, side);
 	}
 	
 	@Override
 	public IAxis removeAxis(final IAxis axis) {
 		
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 					
 		return lightWeightViewer.removeAxis(axis);
 	}
 	
 	@Override
 	public List<IAxis> getAxes() {
 		
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 					
 		return lightWeightViewer.getAxes();
 	}
 
 	
 	@Override
 	public IAxis getSelectedXAxis() {
 		return lightWeightViewer.getSelectedXAxis();
 	}
 
 	@Override
 	public void setSelectedXAxis(IAxis selectedXAxis) {
 		lightWeightViewer.setSelectedXAxis(selectedXAxis);
 	}
 
 	@Override
 	public IAxis getSelectedYAxis() {
 		return lightWeightViewer.getSelectedYAxis();
 	}
 
 	@Override
 	public void setSelectedYAxis(IAxis selectedYAxis) {
 		lightWeightViewer.setSelectedYAxis(selectedYAxis);
 	}
 	
 	public boolean addRegionListener(final IRegionListener l) {
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 		return lightWeightViewer.addRegionListener(l);
 	}
 	
 	public boolean removeRegionListener(final IRegionListener l) {
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 		return lightWeightViewer.removeRegionListener(l);
 	}
 	
 	/**
 	 * Throws exception if region exists already.
 	 * @throws Exception 
 	 */
 	public IRegion createRegion(final String name, final RegionType regionType) throws Exception  {
 
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 		return lightWeightViewer.createRegion(name, regionType);
 	}
 
 	public void clearRegions() {
 		lightWeightViewer.clearRegions();
 	}
 	
 	protected void clearRegionTool() {
 		if (lightWeightViewer.getControl() == null) return;
 		
 		lightWeightViewer.clearRegionTool();
 	}
 
 	/**
 	 * Add a selection region to the graph.
 	 * @param region
 	 */
 	public void addRegion(final IRegion region) {
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 		lightWeightViewer.addRegion(region);
 	}
 
 	/**
 	 * Remove a selection region to the graph.
 	 * @param region
 	 */
 	public void removeRegion(final IRegion region) {
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
 		lightWeightViewer.removeRegion(region);
 	}
 
 	@Override
 	public void renameRegion(final IRegion region, String name) {
 		if (lightWeightViewer.getControl() == null) return;
 		lightWeightViewer.renameRegion(region, name);
 	}
 
 	/**
 	 * Get a region by name.
 	 * @param name
 	 * @return
 	 */
 	public IRegion getRegion(final String name) {
 		if (lightWeightViewer.getControl() == null) return null;
 		return lightWeightViewer.getRegion(name);
 	}
 
 	/**
 	 * Get regions
 	 * @param name
 	 * @return
 	 */
 	public Collection<IRegion> getRegions() {
 		if (lightWeightViewer.getControl() == null) return null;
 		return lightWeightViewer.getRegions();
 	}
 	
 	@Override
 	public Collection<IRegion> getRegions(final RegionType type) {
 		if (lightWeightViewer.getControl() == null) return null;
         return lightWeightViewer.getRegions(type);
 	}
 
 	@Override
 	public IAnnotation createAnnotation(final String name) throws Exception {
 		if (lightWeightViewer.getControl() == null) createLightWeightUI();
         return lightWeightViewer.createAnnotation(name);
 	}
 
 	@Override
 	public void addAnnotation(final IAnnotation annotation) {
 		lightWeightViewer.addAnnotation(annotation);
 	}
 
 	@Override
 	public void removeAnnotation(final IAnnotation annotation) {
 		lightWeightViewer.removeAnnotation(annotation);
 	}
 
 	@Override
 	public void renameAnnotation(final IAnnotation annotation, String name) {
 		lightWeightViewer.renameAnnotation(annotation, name);
 	}
 	
 	@Override
 	public void clearAnnotations(){
 		lightWeightViewer.clearAnnotations();
 	}
 
 
 	@Override
 	public IAnnotation getAnnotation(final String name) {
 		return lightWeightViewer.getAnnotation(name);
 	}
 
 	@Override
 	public void autoscaleAxes() {
 		lightWeightViewer.autoscaleAxes();
 	}
 
 	@Override
 	public void printPlotting(){
 		lightWeightViewer.printPlotting();
 	}
 
 	/**
 	 * Print scaled plotting to printer
 	 */
 	public void printScaledPlotting(){
 		lightWeightViewer.printScaledPlotting();
 	}
 
 	@Override
 	public void copyPlotting(){
 		lightWeightViewer.copyPlotting();
 	}
 
 	@Override
 	public String savePlotting(String filename) throws Exception{
 		return lightWeightViewer.savePlotting(filename);
 	}
 
 	@Override
 	public void savePlotting(String filename, String filetype) throws Exception{
 		lightWeightViewer.savePlotting(filename, filetype);
 	}
 	
 	public void setXfirst(boolean xfirst) {
 		super.setXfirst(xfirst);
 		lightWeightViewer.setXFirst(xfirst);
 	}
 	
 	public void setRescale(boolean rescale) {
 		super.setRescale(rescale);
 		lightWeightViewer.setRescale(rescale);
 	}
 	
 	
 	/**
 	 * NOTE This listener is *not* notified once for each configuration setting made on 
 	 * the configuration but once whenever the form is applied by the user (and many things
 	 * are changed) 
 	 * 
 	 * You then have to read the property you require from the object (for instance the axis
 	 * format) in case it has changed. This is not ideal, later there may be more events fired and
 	 * it will be possible to check property name, for now it is always set to "Graph Configuration".
 	 * 
 	 * @param listener
 	 */
 	@Override
 	public void addPropertyChangeListener(IPropertyChangeListener listener) {
 		super.addPropertyChangeListener(listener);
 		lightWeightViewer.addPropertyChangeListener(listener);
 	}
 	
 	@Override
 	public void removePropertyChangeListener(IPropertyChangeListener listener) {
 		super.removePropertyChangeListener(listener);
 		lightWeightViewer.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * Internal use only do not use this method externally at any point!
 	 * @return
 	 */
 	public XYRegionGraph getLightWeightGraph() {
 		return lightWeightViewer.getXYRegionGraph();
 	}
 
 
 	public void setActionBars(IActionBars bars) {
 		this.bars = bars;
 	}
 
 	@Override
 	public void setDefaultCursor(int cursorType) {
 		Cursor cursor = Cursors.ARROW;
 		if (cursorType == CROSS_CURSOR) cursor = Cursors.CROSS;
 		lightWeightViewer.setDefaultPlotCursor(cursor);
 	}
 	
 	/**
 	 * Set the cursor using a custom icon on the plot.
 	 * This may get cancelled if other tools are used!
 	 */
 	@Override
 	public void setSelectedCursor(Cursor cursor) {
 		
 		if (isDisposed() || lightWeightViewer==null || lightWeightViewer.getXYRegionGraph()==null) return;
 		lightWeightViewer.setSelectedCursor(cursor);
 	}
 	
 	@Override
 	public Cursor getSelectedCursor() {
 		
 		if (isDisposed() || lightWeightViewer==null || lightWeightViewer.getXYRegionGraph()==null) return null;
 		return lightWeightViewer.getXYRegionGraph().getRegionArea().getSelectedCursor();
 	}
 
 	
 	@Override
 	public void addPositionListener(IPositionListener l) {
 		if (lightWeightViewer==null) return;
 		lightWeightViewer.addPositionListener(l);
 	}
 
 	@Override
 	public void removePositionListener(IPositionListener l) {
 		if (lightWeightViewer==null) return;
 		lightWeightViewer.removePositionListener(l);
 	}
 
 	@Override
 	public void addMouseMotionListener(MouseMotionListener mml) {
 		if (isDisposed() || lightWeightViewer==null || lightWeightViewer.getXYRegionGraph()==null) return;
 		lightWeightViewer.getXYRegionGraph().getRegionArea().addAuxilliaryMotionListener(mml);
 	}
 	
 	@Override
 	public void addMouseClickListener(MouseListener mcl) {
 		if (isDisposed() || lightWeightViewer==null || lightWeightViewer.getXYRegionGraph()==null) return;
 		lightWeightViewer.getXYRegionGraph().getRegionArea().addAuxilliaryClickListener(mcl);
 	}
 	
 	@Override
 	public void removeMouseMotionListener(MouseMotionListener mml) {
 		if (isDisposed() || lightWeightViewer==null || lightWeightViewer.getXYRegionGraph()==null) return;
 		lightWeightViewer.getXYRegionGraph().getRegionArea().removeAuxilliaryMotionListener(mml);
 	}
 	
 	/**
 	 * Please override for draw2d listeners.
 	 * @deprecated draw2d Specific
 	 */
 	@Override
 	public void removeMouseClickListener(MouseListener mcl) {
 		if (isDisposed() || lightWeightViewer==null || lightWeightViewer.getXYRegionGraph()==null) return;
 		lightWeightViewer.getXYRegionGraph().getRegionArea().removeAuxilliaryClickListener(mcl);
 	}
 	public void setKeepAspect(boolean checked){
 		lightWeightViewer.setKeepAspect(checked);
 	}
 }
