 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.plotting.system;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
 import org.csstudio.swt.xygraph.dataprovider.ISample;
 import org.csstudio.swt.xygraph.dataprovider.Sample;
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.csstudio.swt.xygraph.linearscale.LinearScale.Orientation;
 import org.csstudio.swt.xygraph.undo.AddAnnotationCommand;
 import org.csstudio.swt.xygraph.undo.RemoveAnnotationCommand;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IAxis;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingActionBarManager;
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.plotting.system.swtxy.AspectAxis;
 import org.dawb.workbench.plotting.system.swtxy.ImageTrace;
 import org.dawb.workbench.plotting.system.swtxy.Region;
 import org.dawb.workbench.plotting.system.swtxy.RegionArea;
 import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
 import org.dawb.workbench.plotting.system.swtxy.XYRegionToolbar;
 import org.dawb.workbench.plotting.util.ColorUtility;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 
 
 /**
  * Link between EDNA 1D plotting and csstudio plotter and fable plotter.
  * 
  * 
  * @author gerring
  *
  */
 public class LightWeightPlottingSystem extends AbstractPlottingSystem {
 
 	private Logger logger = LoggerFactory.getLogger(LightWeightPlottingSystem.class);
 	
 	private Composite      parent;
 	private IActionBars    bars;
 
 	// Controls
 	private Canvas         xyCanvas;
 	private XYRegionGraph  xyGraph;
 		
 	// The plotting mode, used for updates to data
 	private PlotType plottingMode;
 
 	private LightWeightActionBarsManager lightWeightActionBarMan;
 	
 	public LightWeightPlottingSystem() {
 		super();
 		this.lightWeightActionBarMan = (LightWeightActionBarsManager)this.actionBarManager;
 	}
 	
 	
 	public void createPlotPart(final Composite      parent,
 							   final String         plotName,
 							   final IActionBars    bars,
 							   final PlotType       hint,
 							   final IWorkbenchPart part) {
 
 		super.createPlotPart(parent, plotName, bars, hint, part);
 		
 		this.parent  = parent;
 		this.bars    = bars;
 		
 		createUI();
 
 	}
 	
 	@Override
 	protected PlottingActionBarManager createActionBarManager() {
 		return new LightWeightActionBarsManager(this);
 	}
 
 	
 	@Override
 	public Composite getPlotComposite() {
 		if (xyCanvas!=null)       return xyCanvas;
 		return null;
 	}
 		
 	private void createUI() {
 		
 		if (xyCanvas!=null) return;		
 		
 		this.xyCanvas = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED|SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
 		final LightweightSystem lws = new LightweightSystem(xyCanvas);
 		
 		// Stops a mouse wheel move corrupting the plotting area, but it wobbles a bit.
 		xyCanvas.addMouseWheelListener(new MouseWheelListener() {
 			@Override
 			public void mouseScrolled(MouseEvent e) {
 				if (xyGraph!=null) xyGraph.repaint();
 			}	
 		});
 		lws.setControl(xyCanvas);
 		xyCanvas.setBackground(xyCanvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 	
 		this.xyGraph = new XYRegionGraph();
 		xyGraph.setSelectionProvider(getSelectionProvider());
 		
         if (bars!=null) if (bars.getMenuManager()!=null)    bars.getMenuManager().removeAll();
         if (bars!=null) if (bars.getToolBarManager()!=null) bars.getToolBarManager().removeAll();
 
         final MenuManager rightClick = new MenuManager();
         
 		if (bars!=null) {
     		final XYRegionToolbar toolbar = new XYRegionToolbar(xyGraph);
         	toolbar.createGraphActions(bars.getToolBarManager(), rightClick);
 		}
 		
 		lightWeightActionBarMan.createAspectAction();
 		lightWeightActionBarMan.createToolDimensionalActions(ToolPageRole.ROLE_1D, "org.dawb.workbench.plotting.views.toolPageView.1D");
 		lightWeightActionBarMan.createToolDimensionalActions(ToolPageRole.ROLE_2D, "org.dawb.workbench.plotting.views.toolPageView.2D");
 		lightWeightActionBarMan.createToolDimensionalActions(ToolPageRole.ROLE_1D_AND_2D, "org.dawb.workbench.plotting.views.toolPageView.1D_and_2D");
 		lightWeightActionBarMan.createPalleteActions();
 		lightWeightActionBarMan.createOriginActions();
 		lightWeightActionBarMan.createAdditionalActions(rightClick);
 		
 		lws.setContents(xyGraph);
 		xyGraph.primaryXAxis.setShowMajorGrid(true);
 		xyGraph.primaryXAxis.setShowMinorGrid(true);		
 		xyGraph.primaryYAxis.setShowMajorGrid(true);
 		xyGraph.primaryYAxis.setShowMinorGrid(true);
 		xyGraph.primaryYAxis.setTitle("");
 		
 		if (bars!=null) bars.updateActionBars();
 		if (bars!=null) bars.getToolBarManager().update(true);
                  
         final Menu rightClickMenu = rightClick.createContextMenu(xyCanvas);
         xyCanvas.setMenu(rightClickMenu);
  
         if (defaultPlotType!=null) {
 		    this.lightWeightActionBarMan.switchActions(defaultPlotType.is1D(), !defaultPlotType.is1D());
         }
 
         parent.layout();
 
 	}
 		
 	public void addTraceListener(final ITraceListener l) {
 		super.addTraceListener(l);
 		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).addImageTraceListener(l);
 	}
 	public void removeTraceListener(final ITraceListener l) {
 		super.removeTraceListener(l);
 		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).removeImageTraceListener(l);
 	}
 
 	/**
 	 * Does not have to be called in UI thread.
 	 */
 	@Override
 	protected List<ITrace> createPlot(final AbstractDataset       data, 
 					                  final List<AbstractDataset> axes,
 					                  final PlotType              mode, 
 					                  final IProgressMonitor      monitor) {
 		
 		if (monitor!=null) monitor.worked(1);
 		
 		final Object[] oa = getIndexedDatasets(data, axes);
 		final AbstractDataset       x   = (AbstractDataset)oa[0];
 		final List<AbstractDataset> ys  = (List<AbstractDataset>)oa[1];
 		final boolean createdIndices    = (Boolean)oa[2];
 		
 
 		final List<ITrace> traces = new ArrayList<ITrace>(7);
 		
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			List<ITrace> ts = createPlotInternal(mode, x, ys, createdIndices, monitor);
 			if (ts!=null) traces.addAll(ts);
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					List<ITrace> ts = createPlotInternal(mode, x, ys, createdIndices, monitor);
 					if (ts!=null) traces.addAll(ts);
 				}
 			});
 		}
 
 		
 		if (monitor!=null) monitor.worked(1);
 		return traces;
 		
 	}
 	
 	private Display getDisplay() {
 		if (part!=null) return part.getSite().getShell().getDisplay();
 		if (xyCanvas!=null) return xyCanvas.getDisplay();
 		if (parent!=null)  parent.getDisplay();
 		return Display.getDefault();
 	}
 
 	private Object[] getIndexedDatasets(AbstractDataset data,
 			                            List<AbstractDataset> axes) {
 		
 		final AbstractDataset x;
 		final List<AbstractDataset> ys;
 		final boolean createdIndices;
 		if (axes==null || axes.isEmpty()) {
 			ys = new ArrayList<AbstractDataset>(1);
 			ys.add(data);
 			x = DoubleDataset.arange(ys.get(0).getSize());
 			x.setName("Index of "+data.getName());
 			createdIndices = true;
 		} else {
 			x  = data;
 			ys = axes;
 			createdIndices = false;
 		}
 		return new Object[]{x,ys,createdIndices};
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
 	 * Must be called in UI thread.
 	 * 
 	 * @param mode
 	 * @param x
 	 * @param ys
 	 * @param createdIndices
 	 * @param monitor
 	 */
 	private List<ITrace> createPlotInternal(final PlotType              mode, 
 					                        final AbstractDataset       x, 
 					                        final List<AbstractDataset> ys, 
 					                        final boolean               createdIndices, 
 					                        final IProgressMonitor      monitor) {
 		
 		this.plottingMode = mode;
 		createUI();
 		this.lightWeightActionBarMan.switchActions(mode.is1D(), !mode.is1D());
 		if (mode.is1D()) {
 			return create1DPlot(x,ys,createdIndices,monitor);
 		} else {
             ITrace trace = createImagePlot(x,ys,monitor);
             return Arrays.asList(new ITrace[]{trace});
 		}
 	}
 
 	/**
      * Do not call before createPlotPart(...)
      */
 	public void setDefaultPlotType(PlotType mode) {
 		this.defaultPlotType = mode;
 		createUI();
 	}
 
 	/**
 	 * Must be called in UI thread. Creates and updates image.
 	 * NOTE removes previous traces if any plotted.
 	 * 
 	 * @param data
 	 * @param axes
 	 * @param monitor
 	 */
 	private ITrace createImagePlot(final AbstractDataset       data, 
 								   final List<AbstractDataset> axes,
 								   final IProgressMonitor      monitor) {
   
 		try {
 			
 			final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 			final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 			yAxis.setTitle("");
 			xAxis.setTitle("");
 
 			xyGraph.clearTraces();
 			
 			if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
 			traceMap.clear();
 			
 			final ImageTrace trace = xyGraph.createImageTrace(data.getName(), xAxis, yAxis);
 			trace.setData(data, axes);
 			
 			traceMap.put(trace.getName(), trace);
 
 			xyGraph.addImageTrace(trace);
 			
 			
 			return trace;
             
 		} catch (Throwable e) {
 			logger.error("Cannot load file "+data.getName(), e);
 			return null;
 		}
 	}
 
 
 	@Override
 	public IImageTrace createImageTrace(String traceName) {
 		final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 		
 		final ImageTrace trace = xyGraph.createImageTrace(traceName, xAxis, yAxis);
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
 
 
 	private List<ITrace> create1DPlot(  final AbstractDataset       xIn, 
 										final List<AbstractDataset> ysIn,
 										final boolean               createdIndices,
 										final IProgressMonitor      monitor) {
 		
 		Object[] oa = getOrderedDatasets(xIn, ysIn, createdIndices);
 		final AbstractDataset       x  = (AbstractDataset)oa[0];
 		final List<AbstractDataset> ys = (List<AbstractDataset>)oa[1];
 		
 		if (x.containsInvalidNumbers()) throw new RuntimeException("The value of "+x.getName()+" is invalid. Cannot plot datasets with infinite values in it!");
 		for (AbstractDataset y : ys) {
 			if (y.containsInvalidNumbers()) throw new RuntimeException("The value of "+y.getName()+" is invalid. Cannot plot datasets with infinite values in it!");
 		}
 		
 		if (colorMap == null && getColorOption()!=ColorOption.NONE) {
 			if (getColorOption()==ColorOption.BY_NAME) {
 				colorMap = new HashMap<Object,Color>(ys.size());
 			} else {
 				colorMap = new IdentityHashMap<Object,Color>(ys.size());
 			}
 		}
 		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
 	
 		if (xyGraph==null) return null;
 		
 		Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		xAxis.setVisible(true);
 		yAxis.setVisible(true);
 
 		// TODO Fix titles for multiple calls to create1DPlot(...)
 		xyGraph.setTitle(DatasetTitleUtils.getTitle(x, ys, true, rootName));
 		xAxis.setTitle(DatasetTitleUtils.getName(x,rootName));
 		
 
 		//create a trace data provider, which will provide the data to the trace.
 		int iplot = 0;
 		final double[] xArray = (double[])DatasetUtils.cast(x, AbstractDataset.FLOAT64).getBuffer();
 		
 		final List<ITrace> traces = new ArrayList<ITrace>(ys.size());
 		for (AbstractDataset y : ys) {
 
 			CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
 			if (y.getBuffer()!=null && y.getShape()!=null) {
 				traceDataProvider.setBufferSize(y.getSize());	
 				traceDataProvider.setCurrentXDataArray(xArray);
 				final double[] yArray = (double[])DatasetUtils.cast(y, AbstractDataset.FLOAT64).getBuffer();
 				traceDataProvider.setCurrentYDataArray(yArray);	
 			}
 			
 			//create the trace
 			final Trace trace = new Trace(DatasetTitleUtils.getName(y,rootName), 
 					                      xAxis, 
 					                      yAxis,
 									      traceDataProvider);	
 			
 			TraceWrapper wrapper = new TraceWrapper(this, trace);
 			traces.add(wrapper);
 			
 			if (y.getName()!=null && !"".equals(y.getName())) {
 				traceMap.put(y.getName(), wrapper);
 				trace.setInternalName(y.getName());
 			}
 			
 			//set trace property
 			trace.setPointStyle(PointStyle.NONE);
			int index = getTraces().size()+iplot-1;
			if (index<0) index=0;
			final Color plotColor = ColorUtility.getSwtColour(colorMap!=null?colorMap.values():null, index);
 			if (colorMap!=null) {
 				if (getColorOption()==ColorOption.BY_NAME) {
 					colorMap.put(y.getName(),plotColor);
 				} else {
 					colorMap.put(y,          plotColor);
 				}
 			}
 			trace.setTraceColor(plotColor);
 
 			//add the trace to xyGraph
 			xyGraph.addTrace(trace);
 			
 			
 			if (monitor!=null) monitor.worked(1);
 			iplot++;
 		}
 		
 		xyCanvas.redraw();
 		getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				autoscaleAxes();
 				autoscaleAxes();
 			}
 		});
 		fireTracesPlotted(new TraceEvent(traces));
         return traces;
 	}
 	
 	public ILineTrace createLineTrace(String traceName) {
 
 		Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
 		final Trace trace = new Trace(traceName, xAxis, yAxis, traceDataProvider);
 		final TraceWrapper wrapper = new TraceWrapper(this, trace);
 		fireTraceCreated(new TraceEvent(wrapper));
 		return wrapper;
 	}
 	
 	/**
 	 * Adds trace, makes visible
 	 * @param traceName
 	 * @return
 	 */
 	public void addTrace(ITrace trace) {
 		
 		if (traceMap==null) this.traceMap = new HashMap<String, ITrace>(7);
 		traceMap.put(trace.getName(), trace);
 		
 		if (trace instanceof ImageTrace) {
 			xyGraph.addImageTrace((ImageTrace)trace);
 		} else {
 			xyGraph.addTrace(((TraceWrapper)trace).getTrace());
 			xyCanvas.redraw();
 			fireTraceAdded(new TraceEvent(trace));
 		}
 	}
 	/**
 	 * Removes a trace.
 	 * @param traceName
 	 * @return
 	 */
 	public void removeTrace(ITrace trace) {
 		xyGraph.removeTrace(((TraceWrapper)trace).getTrace());
 		xyCanvas.redraw();
 		fireTraceRemoved(new TraceEvent(trace));
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
 		
 		final Trace trace = ((TraceWrapper)wrapper).getTrace();
 		
 		CircularBufferDataProvider prov = (CircularBufferDataProvider)trace.getDataProvider();
 		if (prov==null) return;
 
 		if (rescale) {
 			try {
 				Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 				
 				if (xValue==null) xValue = xAxis.getRange().getUpper()+1d;					
 				double min = Math.min(xAxis.getRange().getLower(), xValue.doubleValue());
 				double max = Math.max(xAxis.getRange().getUpper(), xValue.doubleValue());
 				xAxis.setRange(min,max);
 
 				Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 				min = Math.min(yAxis.getRange().getLower(), yValue.doubleValue());
 				max = Math.max(yAxis.getRange().getUpper(), yValue.doubleValue());
 				yAxis.setRange(min,max);
 				
 			} catch (Throwable e) {
 				logger.error("Cannot rescale data, internal error.\nNormally this would be thrown back to the calling API but it happening in an update thread.", e);
 				return;
 			}
 		}		
 
 	    
 	    // View all the data - not we can 
 	    // Also do tickers if the buffer size is not
         // changed.
 	    prov.setBufferSize(prov.getSize()+1);  
 	    
 	    // Change data
 	    prov.addSample(new Sample(xValue.doubleValue(), yValue.doubleValue()));
 	    
 	    
 	}
 	
 	
     /**
      * Thread safe method
      */
 	@Override
 	public AbstractDataset getData(String name) {
 		
 		final ITrace wrapper = traceMap.get(name);
 		if (wrapper==null) return null;
 		
 		final Trace trace = ((TraceWrapper)wrapper).getTrace();
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
 		
 		CircularBufferDataProvider prov = (CircularBufferDataProvider)trace.getDataProvider();
 		if (prov==null) return null;
 		
 		final double[]          da = new double[prov.getSize()];
 		final Iterator<ISample> it = prov.iterator();
 		int i = 0;
 		while(it.hasNext()) {
 			da[i] = isY ? it.next().getYValue() : it.next().getXValue();
 			++i;
 		}
 		
 		final DoubleDataset set = new DoubleDataset(da, da.length);
 		if (isY) set.setName(name); else set.setName(getSelectedXAxis().getTitle());
 		
 		return set;
 	}
 
 	private Object[] getOrderedDatasets(final AbstractDataset       xIn,
 										final List<AbstractDataset> ysIn,
 										final boolean               createdIndices) {
 
 		final AbstractDataset       x;
 		final List<AbstractDataset> ys;
 		if (xfirst || createdIndices) {
 			x = xIn;
 			ys= ysIn;
 		} else {
 			ys = new ArrayList<AbstractDataset>(ysIn.size()+1);
 			ys.add(xIn);
 			ys.addAll(ysIn);
 
 			final int max = getMaxSize(ys);
 			x = AbstractDataset.arange(0, max, 1, AbstractDataset.INT32);
 			x.setName("Indices");
 
 		}
 
 		return new Object[]{x,ys};
 	}
 
 	/**
 	 * Override this method to provide an implementation of title setting.
 	 * @param title
 	 */
 	public void setTitle(final String title) {
 		
 		if (xyGraph!=null) {
 			xyGraph.setTitle(title);
 		} else {
 			throw new RuntimeException("Cannot set the plot title when the plotting system is not created or plotting something!");
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
 	
 	private int getMaxSize(List<AbstractDataset> sets) {
 		int max = 1; // Cannot be less than one
 		for (AbstractDataset set : sets) {
 			try {
 			    max = Math.max(max, set.getSize());
 			} catch (NullPointerException npe) {
 				continue;
 			}
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
 		
 		if (colorMap!=null) colorMap.clear();
 		if (xyGraph!=null) {
 			try {
 				clearTraces();
 				for (Axis axis : xyGraph.getAxisList()) axis.setRange(0,100);
 	
 			} catch (Throwable e) {
 				logger.error("Cannot remove plots!", e);
 			}
 		}
 				
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
 		if (xyGraph!=null) {
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
 		if (xyGraph!=null) {
 			xyGraph.removeAll();
 			xyGraph = null;
 		}
 	}
 
 	private void clearTraces() {
 		if (xyGraph!=null)  xyGraph.clearTraces();
 		if (traceMap!=null) traceMap.clear();
 		fireTracesCleared(new TraceEvent(this));
 	}
 
 	public void repaint() {
 		if (getDisplay().getThread()==Thread.currentThread()) {
 			if (xyCanvas!=null) {
 				LightWeightPlottingSystem.this.xyGraph.performAutoScale();
 				LightWeightPlottingSystem.this.xyCanvas.redraw();
 			}
 		} else {
 			getDisplay().syncExec(new Runnable() {
 				public void run() {
 					if (xyCanvas!=null) {
 						LightWeightPlottingSystem.this.xyGraph.performAutoScale();
 						LightWeightPlottingSystem.this.xyCanvas.redraw();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * Creates an image of the same size as the Rectangle passed in.
 	 * @param size
 	 * @return
 	 */
 	@Override
 	public Image getImage(Rectangle size) {
 		return xyGraph.getImage(size);
 	}
 	
 	/**
 	 * Access to the XYGraph, may return null. Access discouraged, just for emergencies!
 	 * To use cast your IPlottingSystem to LightWeightPlottingSystem
 	 * 
 	 * @return
 	 */
 	public XYRegionGraph getGraph() {
 		return xyGraph;
 	}
 
 	/**
 	 * Use this method to create axes other than the default y and x axes.
 	 * 
 	 * @param title
 	 * @param isYAxis, normally it is.
 	 * @param side - either SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM
 	 * @return
 	 */
 	public IAxis createAxis(final String title, final boolean isYAxis, int side) {
 		
 		if (xyGraph==null) createUI();
 			
 		Axis axis = new AspectAxis(title, isYAxis);
 		if (isYAxis) {
 			axis.setOrientation(Orientation.VERTICAL);
 		} else {
 			axis.setOrientation(Orientation.HORIZONTAL);
 		}
 		if (side==SWT.LEFT||side==SWT.BOTTOM) {
 		    axis.setTickLableSide(LabelSide.Primary);
 		} else {
 			axis.setTickLableSide(LabelSide.Secondary);
 		}
 		axis.setAutoScaleThreshold(0.1);
 		axis.setShowMajorGrid(true);
 		axis.setShowMinorGrid(true);		
 	
 		xyGraph.addAxis(axis);
 		
 		return new AxisWrapper(axis);
 	}
 	
 	private IAxis selectedXAxis;
 	private IAxis selectedYAxis;
 
 	@Override
 	public IAxis getSelectedXAxis() {
 		if (selectedXAxis==null) {
 			if (xyGraph==null) createUI();
 			return new AxisWrapper(xyGraph.primaryXAxis);
 		}
 		return selectedXAxis;
 	}
 
 	@Override
 	public void setSelectedXAxis(IAxis selectedXAxis) {
 		this.selectedXAxis = selectedXAxis;
 	}
 
 	@Override
 	public IAxis getSelectedYAxis() {
 		if (selectedYAxis==null) {
 			if (xyGraph==null) createUI();
 			return new AxisWrapper(xyGraph.primaryYAxis);
 		}
 		return selectedYAxis;
 	}
 
 	@Override
 	public void setSelectedYAxis(IAxis selectedYAxis) {
 		this.selectedYAxis = selectedYAxis;
 	}
 	
 	public boolean addRegionListener(final IRegionListener l) {
 		if (xyGraph==null) createUI();
 		return xyGraph.addRegionListener(l);
 	}
 	
 	public boolean removeRegionListener(final IRegionListener l) {
 		if (xyGraph==null) return false;
 		return xyGraph.removeRegionListener(l);
 	}
 	
 	/**
 	 * Throws exception if region exists already.
 	 * @throws Exception 
 	 */
 	public IRegion createRegion(final String name, final RegionType regionType) throws Exception  {
 
 		if (xyGraph==null) createUI();
 		final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 
 		return xyGraph.createRegion(name, xAxis, yAxis, regionType, true);
 	}
 	
 	/**
 	 * Thread safe
 	 */
 	public void clearRegions() {
 		if (xyGraph==null) return;
 		
 		xyGraph.clearRegions();
 	}
 	
 	protected void clearRegionTool() {
 		if (xyGraph==null) return;
 		
 		xyGraph.clearRegionTool();
 	}
 
 	/**
 	 * Add a selection region to the graph.
 	 * @param region
 	 */
 	public void addRegion(final IRegion region) {		
 		if (xyGraph==null) createUI();
 		final Region r = (Region)region;
 		xyGraph.addRegion(r);		
  	}
 	
 	/**
 	 * Remove a selection region to the graph.
 	 * @param region
 	 */
 	public void removeRegion(final IRegion region) {		
 		if (xyGraph==null) createUI();
 		final Region r = (Region)region;
 		xyGraph.removeRegion(r);
 	}
 
 	/**
 	 * Get a region by name.
 	 * @param name
 	 * @return
 	 */
 	public IRegion getRegion(final String name) {
 		if (xyGraph==null)  return null;
 		return xyGraph.getRegion(name);
 	}
 	/**
 	 * Get regions
 	 * @param name
 	 * @return
 	 */
 	public Collection<IRegion> getRegions() {
 		if (xyGraph==null)  return null;
 		List<Region> regions = xyGraph.getRegions();
 		return new ArrayList<IRegion>(regions);
 	}
 	
 	public IAnnotation createAnnotation(final String name) throws Exception {
 		if (xyGraph==null) createUI();
 		
 		final List<Annotation>anns = xyGraph.getPlotArea().getAnnotationList();
 		for (Annotation annotation : anns) {
 			if (annotation.getName()!=null&&annotation.getName().equals(name)) {
 				throw new Exception("The annotation name '"+name+"' is already taken.");
 			}
 		}
 		
 		final Axis xAxis = ((AxisWrapper)getSelectedXAxis()).getWrappedAxis();
 		final Axis yAxis = ((AxisWrapper)getSelectedYAxis()).getWrappedAxis();
 		
 		return new AnnotationWrapper(name, xAxis, yAxis);
 	}
 	
 	/**
 	 * Add an annotation to the graph.
 	 * @param region
 	 */
 	public void addAnnotation(final IAnnotation annotation) {
 		
         final AnnotationWrapper wrapper = (AnnotationWrapper)annotation;
         xyGraph.addAnnotation(wrapper.getAnnotation());
         xyGraph.getOperationsManager().addCommand(new AddAnnotationCommand(xyGraph, wrapper.getAnnotation()));
 	}
 	
 	
 	/**
 	 * Remove an annotation to the graph.
 	 * @param region
 	 */
 	public void removeAnnotation(final IAnnotation annotation) {
         final AnnotationWrapper wrapper = (AnnotationWrapper)annotation;
         xyGraph.removeAnnotation(wrapper.getAnnotation());
         xyGraph.getOperationsManager().addCommand(new RemoveAnnotationCommand(xyGraph, wrapper.getAnnotation()));
 	}
 	
 	/**
 	 * Get an annotation by name.
 	 * @param name
 	 * @return
 	 */
 	public IAnnotation getAnnotation(final String name) {
 		final List<Annotation>anns = xyGraph.getPlotArea().getAnnotationList();
 		for (Annotation annotation : anns) {
 			if (annotation.getName()!=null&&annotation.getName().equals(name)) {
 				return new AnnotationWrapper(annotation);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Remove all annotations
 	 */
 	public void clearAnnotations(){
 		final List<Annotation>anns = new ArrayList<Annotation>(xyGraph.getPlotArea().getAnnotationList());
 		for (Annotation annotation : anns) {
 			xyGraph.getPlotArea().removeAnnotation(annotation);
 		}
 	}
 
 	protected IActionBars getActionBars() {
 	    return bars;
 	}
 
 	@Override
 	public void autoscaleAxes() {
 		if (xyGraph==null) return;
 		xyGraph.performAutoScale();
 	}
 }
