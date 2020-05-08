 package org.dawb.workbench.plotting.system;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.csstudio.swt.xygraph.undo.AddAnnotationCommand;
 import org.csstudio.swt.xygraph.undo.RemoveAnnotationCommand;
 import org.csstudio.swt.xygraph.undo.ZoomType;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.IPrintablePlotting;
 import org.dawb.common.ui.plot.ITraceActionProvider;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.annotation.AnnotationUtils;
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.annotation.IAnnotationSystem;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.axis.IAxisSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionContainer;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.IRegionSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace.PointStyle;
 import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceContainer;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceWillPlotEvent;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.dawb.workbench.plotting.printing.PlotExportPrintUtil;
 import org.dawb.workbench.plotting.printing.PlotPrintPreviewDialog;
 import org.dawb.workbench.plotting.printing.PrintSettings;
 import org.dawb.workbench.plotting.system.dialog.XYRegionConfigDialog;
 import org.dawb.workbench.plotting.util.ColorUtility;
 import org.dawb.workbench.plotting.util.TraceUtils;
 import org.dawnsci.plotting.draw2d.swtxy.AspectAxis;
 import org.dawnsci.plotting.draw2d.swtxy.ImageTrace;
 import org.dawnsci.plotting.draw2d.swtxy.LineTrace;
 import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
 import org.dawnsci.plotting.draw2d.swtxy.RegionCreationLayer;
 import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.dawnsci.plotting.draw2d.swtxy.selection.SelectionRegionFactory;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.BorderLayout;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.Layer;
 import org.eclipse.draw2d.LayeredPane;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.draw2d.LineBorder;
 import org.eclipse.draw2d.PrintFigureOperation;
 import org.eclipse.draw2d.SchemeBorder;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.printing.PrintDialog;
 import org.eclipse.swt.printing.Printer;
 import org.eclipse.swt.printing.PrinterData;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * Package private. This class deals with plotting actions specific to the
  * LightWeight (non-3D) part of the plotting.
  * 
  * @author fcp94556
  *
  */
 class LightWeightPlotViewer implements IAnnotationSystem, IRegionSystem, IAxisSystem, IPrintablePlotting, ITraceActionProvider {
 
 	// Controls
 	private Canvas                 xyCanvas;
 	private XYRegionGraph          xyGraph;
 	private LightweightSystem      lws;
 	
 	// Plotting stuff
 	private PlottingSystemImpl     system;
 	private LightWeightPlotActions plotActionsCreator;
 	private ColorMapRamp           intensity;
 
 
 	public void init(PlottingSystemImpl system) {
 		this.system = system;
 	}
 
 	/**
 	 * Call to create plotting
 	 * @param parent
 	 * @param initialMode may be null
 	 */
 	public void createControl(final Composite parent) {
 
 		if (xyCanvas!=null) return;		
 		
 		this.xyCanvas = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED|SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
 		lws = new LightweightSystem(xyCanvas);
 		
 		// Stops a mouse wheel move corrupting the plotting area, but it wobbles a bit.
 		xyCanvas.addMouseWheelListener(getMouseWheelListener());
 		xyCanvas.addKeyListener(getKeyListener());
 		
 		lws.setControl(xyCanvas);
 		xyCanvas.setBackground(xyCanvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 	
 		this.xyGraph = new XYRegionGraph();
 		xyGraph.setSelectionProvider(system.getSelectionProvider());
 		
 		IActionBars bars = system.getActionBars();
  		PlotActionsManagerImpl actionBarManager = (PlotActionsManagerImpl)system.getPlotActionSystem();
 		// We contain the action bars in an internal object
 		// if the API user said they were null. This allows the API
 		// user to say null for action bars and then use:
 		// getPlotActionSystem().fillXXX() to add their own actions.
  		if (bars==null) {
  			bars = actionBarManager.createEmptyActionBars();
  			system.setActionBars(bars);
  		}
  				
 // 		bars.getMenuManager().removeAll();
 // 		bars.getToolBarManager().removeAll();
 
  		actionBarManager.init(this);
  		this.plotActionsCreator = new LightWeightPlotActions();
  		plotActionsCreator.init(this, xyGraph, actionBarManager);
  		plotActionsCreator.createLightWeightActions();
 		 
  		// Create the layers (currently everything apart from the temporary 
  		// region draw layer is on 0)
  		final LayeredPane layers      = new LayeredPane();
         new RegionCreationLayer(layers, xyGraph.getRegionArea());  
         final Layer graphLayer = new Layer();
         graphLayer.setLayoutManager(new BorderLayout());
         graphLayer.add(xyGraph, BorderLayout.CENTER);
 		this.intensity = new ColorMapRamp();
         graphLayer.add(intensity, BorderLayout.RIGHT);
 		intensity.setVisible(false);
 		intensity.setBorder(new LineBorder(ColorConstants.white, 5));
 		
   		layers.add(graphLayer,     0);
		lws.setContents(layers);
 		
 		// Create status contribution for position
 		IWorkbenchPart part = system.getPart();
 		if (part!=null) {
 			IStatusLineManager statusLine = null;
 		    if (part instanceof IViewPart) {
 		    	bars = ((IViewPart)part).getViewSite().getActionBars();
 		    	statusLine = bars.getStatusLineManager();
 			} else if (part instanceof IEditorPart) {
 				bars = ((IEditorPart)part).getEditorSite().getActionBars();
 		    	statusLine = bars.getStatusLineManager();
 			}
 		    if (statusLine!=null) {
 		    	xyGraph.getRegionArea().setStatusLineManager(statusLine);
 		    }
 		}
 		
 		// Configure axes
 		xyGraph.primaryXAxis.setShowMajorGrid(true);
 		xyGraph.primaryXAxis.setShowMinorGrid(true);		
 		xyGraph.primaryYAxis.setShowMajorGrid(true);
 		xyGraph.primaryYAxis.setShowMinorGrid(true);
 		xyGraph.primaryYAxis.setTitle("");
 		
 		if (system.getPlotType()!=null) {
 			if (!Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.SHOW_AXES) && !system.getPlotType().is1D()) {
 				xyGraph.primaryXAxis.setVisible(false);
 				xyGraph.primaryYAxis.setVisible(false);
 			}
 		}
 		if (bars!=null) bars.updateActionBars();
 		if (bars!=null) bars.getToolBarManager().update(true);
            
  		final MenuManager popupMenu = new MenuManager();
 		popupMenu.setRemoveAllWhenShown(true); // Remake menu each time
         xyCanvas.setMenu(popupMenu.createContextMenu(xyCanvas));
         popupMenu.addMenuListener(getIMenuListener());
         
         if (system.getPlotType()!=null) {
         	actionBarManager.switchActions(system.getPlotType());
         }
 
         parent.layout();
 		
 		
 	}
 	
 	public void addImageTraceListener(final ITraceListener l) {
 		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).addImageTraceListener(l);
 	}
 	
 	public void removeImageTraceListener(final ITraceListener l) {
 		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).removeImageTraceListener(l);
 	}
 
 
 	private MouseWheelListener mouseWheelListener;
 	private MouseWheelListener getMouseWheelListener() {
 		if (mouseWheelListener == null) mouseWheelListener = new MouseWheelListener() {
 			@Override
 			public void mouseScrolled(MouseEvent e) {
 				
 				int direction = e.count > 0 ? 1 : -1;
 
 
 				IFigure fig = getFigureAtCurrentMousePosition();
 				if (fig!=null && fig.getParent() instanceof Axis) {
 					Axis axis = (Axis)fig.getParent();
 					final double center = axis.getPositionValue(e.x, false);
 					axis.zoomInOut(center, direction*0.01);
 					xyGraph.repaint();
 					return;
 				}
 			
 				if (xyGraph==null) return;
 				if (e.count==0)    return;
 				String level  = System.getProperty("org.dawb.workbench.plotting.system.zoomLevel");
 				double factor = level!=null ? Double.parseDouble(level) :  0.1d;
 				xyGraph.setZoomLevel(e, direction*factor);
 				xyGraph.repaint();
 			}	
 		};
 		return mouseWheelListener;
 	}
 
 	private KeyListener keyListener;
 	private KeyListener getKeyListener() {
 		
 		final IActionBars bars = system.getActionBars();
 		if (keyListener==null) keyListener = new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.keyCode==27) { // Esc
 					xyGraph.clearRegionTool();
 					
 				} if (e.keyCode==16777230 || e.character=='h') {
 					final IContributionItem action = bars.getToolBarManager().find("org.dawb.workbench.plotting.histo");
 				    if (action!=null && action.isVisible() && action instanceof ActionContributionItem) {
 				    	ActionContributionItem iaction = (ActionContributionItem)action;
 				    	iaction.getAction().setChecked(!iaction.getAction().isChecked());
 				    	iaction.getAction().run();
 				    }
 				} else if (e.keyCode==16777217) {//Up
  					Point point = Display.getDefault().getCursorLocation();
  					point.y-=1;
  					Display.getDefault().setCursorLocation(point);
 					
  				} else if (e.keyCode==16777218) {//Down
  					Point point = Display.getDefault().getCursorLocation();
  					point.y+=1;
  					Display.getDefault().setCursorLocation(point);
  					
  				} else if (e.keyCode==16777219) {//Left
  					Point point = Display.getDefault().getCursorLocation();
  					point.x-=1;
  					Display.getDefault().setCursorLocation(point);
 					
  				} else if (e.keyCode==16777220) {//Right
  					Point point = Display.getDefault().getCursorLocation();
  					point.x+=1;
  					Display.getDefault().setCursorLocation(point);
  				} else if (e.keyCode==127) {//Delete
 					IFigure fig = getFigureAtCurrentMousePosition();
  					if (fig!=null && fig instanceof IRegionContainer) {
  						xyGraph.removeRegion((AbstractSelectionRegion)((IRegionContainer)fig).getRegion());
  					}
  				}
 			}
 		};
 		return keyListener;
 	}
 
 	
 	private IMenuListener popupListener;
 	private IMenuListener getIMenuListener() {
 		if (popupListener == null) {
 			popupListener = new IMenuListener() {			
 				@Override
 				public void menuAboutToShow(IMenuManager manager) {
 					IFigure fig = getFigureAtCurrentMousePosition();
 					if (fig!=null) {
 					    if (fig instanceof IRegionContainer) {
 							final IRegion region = ((IRegionContainer)fig).getRegion();
 							SelectionRegionFactory.fillActions(manager, region, xyGraph, getSystem());
 							
 							final Action configure = new Action("Configure '"+region.getName()+"'", Activator.getImageDescriptor("icons/RegionProperties.png")) {
 								public void run() {
 									final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph, system.isRescale());
 									dialog.setSelectedRegion(region);
 									dialog.open();
 								}
 							};
 							manager.add(configure);
 							
 							manager.add(new Separator("org.dawb.workbench.plotting.system.region.end"));
 
 					    }
 					    if (fig instanceof ITraceContainer) {
 							final ITrace trace = ((ITraceContainer)fig).getTrace();
 							fillTraceActions(manager, trace, system);
 					    }
 					    
 					    if (fig instanceof Label && fig.getParent() instanceof Annotation) {
 					    	
 					    	fillAnnotationConfigure(manager, (Annotation)fig.getParent(), system);
 					    }
 					}
 					system.getPlotActionSystem().fillZoomActions(manager);
 					manager.update();
 				}
 			};
 		}
 		return popupListener;
 	}
 	
 	protected IFigure getFigureAtCurrentMousePosition() {
 		Point   pnt       = Display.getDefault().getCursorLocation();
 		Point   par       = xyCanvas.toDisplay(new Point(0,0));
 		final int xOffset = par.x+xyGraph.getLocation().x;
 		final int yOffset = par.y+xyGraph.getLocation().y;
 		
 		return xyGraph.findFigureAt(pnt.x-xOffset, pnt.y-yOffset);
 
 	}
 
 	protected void fillAnnotationConfigure(IMenuManager manager,
 													final Annotation annotation,
 													final IPlottingSystem system) {
 
 		final Action configure = new Action("Configure '"+annotation.getName()+"'", Activator.getImageDescriptor("icons/Configure.png")) {
 			public void run() {
 				final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getDefault().getActiveShell(), xyGraph, getSystem().isRescale());
 				dialog.setPlottingSystem(system);
 				dialog.setSelectedAnnotation(annotation);
 				dialog.open();
 			}
 		};
 		manager.add(configure);	
 
 		manager.add(new Separator("org.dawb.workbench.plotting.system.configure.group"));
 	}
 	
     /**
      * 
      * Problems:
      * 1. Line trace bounds extend over other line traces so the last line trace added, will
      * always be the figure that the right click detects.
      * 
      * Useful things, visible, annotation, quick set to line or points, open configure page.
      * 
      * @param manager
      * @param trace
      * @param xyGraph
      */
 	@Override
 	public void fillTraceActions(final IContributionManager manager, final ITrace trace, final IPlottingSystem sys) {
 
 		manager.add(new Separator("org.dawb.workbench.plotting.system.trace.start"));
 
 		final String name = trace!=null&&trace.getName()!=null?trace.getName():"";
 
 		if (trace instanceof ILineTrace) { // Does actually work for images but may confuse people.
 			final Action visible = new Action("Hide '"+name+"'", Activator.getImageDescriptor("icons/TraceVisible.png")) {
 				public void run() {
 					trace.setVisible(false);
 				}
 			};
 			manager.add(visible);
 			
 			if (trace instanceof LineTraceImpl) {
 	 			final Action export = new Action("Export '"+name+"' to ascii (dat file)", Activator.getImageDescriptor("icons/export_wiz.gif")) {
 					public void run() {
 						try {
 							TraceUtils.doExport((LineTraceImpl)trace);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				};
 				manager.add(export);
 			}
 		}
 		
 		if (xyGraph!=null) {
 			
 			if (SelectionRegionFactory.getStaticBuffer()!=null) {
 				final Action pasteRegion = new Action("Paste '"+SelectionRegionFactory.getStaticBuffer().getName()+"'", Activator.getImageDescriptor("icons/RegionPaste.png")) {
 					public void run() {
 						AbstractSelectionRegion region = null;
 						try {
 							region = (AbstractSelectionRegion)sys.createRegion(SelectionRegionFactory.getStaticBuffer().getName(), SelectionRegionFactory.getStaticBuffer().getRegionType());
 						} catch (Exception ne) {
 							MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot paste '"+SelectionRegionFactory.getStaticBuffer().getName()+"'",
 									                   "A region with the name '"+SelectionRegionFactory.getStaticBuffer().getName()+"' already exists.");
 							return;
 						}
 						
 						region.sync(SelectionRegionFactory.getStaticBuffer().getBean());
 						region.setROI(SelectionRegionFactory.getStaticBuffer().getROI());
 						sys.addRegion(region);
 					}
 				};
 				manager.add(pasteRegion);
 			}
 			
 			final Action addAnnotation = new Action("Add annotation to '"+name+"'", Activator.getImageDescriptor("icons/TraceAnnotation.png")) {
 				public void run() {
 					final String annotName = AnnotationUtils.getUniqueAnnotation(name+" annotation ", sys);
 					if (trace instanceof LineTraceImpl) {
 						final LineTraceImpl lt = (LineTraceImpl)trace;
 						xyGraph.addAnnotation(new Annotation(annotName, lt.getTrace()));
 					} else {
 						xyGraph.addAnnotation(new Annotation(annotName, xyGraph.primaryXAxis, xyGraph.primaryYAxis));
 					}
 				}
 			};
 			manager.add(addAnnotation);
 		}
 		
 		if (trace instanceof ILineTrace) {
 			final ILineTrace lt = (ILineTrace)trace;
 			if (lt.getTraceType()!=TraceType.POINT) { // Give them a quick change to points
 				final Action changeToPoints = new Action("Plot '"+name+"' as scatter", Activator.getImageDescriptor("icons/TraceScatter.png")) {
 					public void run() {
 						lt.setTraceType(TraceType.POINT);
 						lt.setPointSize(8);
 						lt.setPointStyle(PointStyle.XCROSS);
 					}
 				};
 				manager.add(changeToPoints);
 			} else if (lt.getTraceType()!=TraceType.SOLID_LINE) {
 				final Action changeToLine = new Action("Plot '"+name+"' as line", Activator.getImageDescriptor("icons/TraceLine.png")) {
 					public void run() {
 						lt.setTraceType(TraceType.SOLID_LINE);
 						lt.setLineWidth(1);
 						lt.setPointSize(1);
 						lt.setPointStyle(PointStyle.NONE);
 					}
 				};
 				manager.add(changeToLine);
 			}
 		}
 
 		if (xyGraph!=null) {
 			final Action configure = new Action("Configure '"+name+"'", Activator.getImageDescriptor("icons/TraceProperties.png")) {
 				public void run() {
 					final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getDefault().getActiveShell(), xyGraph, getSystem().isRescale());
 					dialog.setPlottingSystem(sys);
 					dialog.setSelectedTrace(trace);
 					dialog.open();
 				}
 			};
 			manager.add(configure);
 		}
 		manager.add(new Separator("org.dawb.workbench.plotting.system.trace.end"));
 	}
 
 
 	public Control getControl() {
 		return xyCanvas;
 	}
 
 	public void setFocus() {
 		if (xyCanvas!=null) xyCanvas.setFocus();
 	}
 
 	public void setTitle(String name) {
         if(xyGraph!=null) {
         	xyGraph.setTitle(name);
         	xyGraph.repaint();
         }
 	}
 
 	public void clearTraces() {
 		if (xyGraph!=null) xyGraph.clearTraces();
 	}
 
 	protected ITrace createLightWeightImage(String traceName, AbstractDataset data, List<AbstractDataset> axes, IProgressMonitor monitor) {
 		
 		final Axis xAxis = ((AspectAxis)system.getSelectedXAxis());
 		final Axis yAxis = ((AspectAxis)system.getSelectedYAxis());
 		xAxis.setLogScale(false);
 		yAxis.setLogScale(false);
 
 		if (data.getName()!=null) xyGraph.setTitle(data.getName());
 		xyGraph.clearTraces();
 
 		final ImageTrace trace = xyGraph.createImageTrace(traceName, xAxis, yAxis, intensity);
 		trace.setPlottingSystem(system);
 		if (!trace.setData(data, axes, true)) return trace; // But not plotted
 		
 		addTrace(trace);
 		return trace;
 	}
 
 	protected IImageTrace createImageTrace(String traceName) {
 		final Axis xAxis = (Axis)getSelectedXAxis();
 		final Axis yAxis = (Axis)getSelectedYAxis();
 		
 		final ImageTrace trace = xyGraph.createImageTrace(traceName, xAxis, yAxis, intensity);
 		trace.setPlottingSystem(system);
 		return trace;
 	}
 
 	/**
 	 * Internal usage only
 	 * 
 	 * @param title
 	 * @param x
 	 * @param ys
 	 * @param traceMap, may be null
 	 * @return
 	 */
 	protected List<ITrace> createLineTraces(final String                title, 
 			                                    final AbstractDataset       x, 
 			                                    final List<AbstractDataset> ys,
 			                                    final Map<String,ITrace>    traceMap,
 			                                    final Map<Object, Color>    colorMap,
 			                                    final IProgressMonitor      monitor) {
 		
 		final String rootName = system.getRootName();
 		
 		final AspectAxis xAxis = (AspectAxis)getSelectedXAxis();
 		xAxis.setLabelDataAndTitle(null);
 		final AspectAxis yAxis = (AspectAxis)getSelectedYAxis();
 		yAxis.setLabelDataAndTitle(null);
 
 		xAxis.setVisible(true);
 		yAxis.setVisible(true);
 
 		if (title==null) {
 			// TODO Fix titles for multiple calls to create1DPlot(...)
 			setTitle(DatasetTitleUtils.getTitle(x, ys, true, rootName));
 		} else {
 			setTitle(title);
 		}
 		xAxis.setTitle(DatasetTitleUtils.getName(x,rootName));
 
 		//create a trace data provider, which will provide the data to the trace.
 		int iplot = 0;
 		
 		final List<ITrace> traces = new ArrayList<ITrace>(ys.size());
 		for (AbstractDataset y : ys) {
 
 			if (y==null) continue;
 			LightWeightDataProvider traceDataProvider = new LightWeightDataProvider(x, y);
 			
 			//create the trace
 			final LineTrace trace = new LineTrace(DatasetTitleUtils.getName(y,rootName), 
 					                      xAxis, 
 					                      yAxis,
 									      traceDataProvider);	
 			
 			LineTraceImpl wrapper = new LineTraceImpl(system, trace);
 			traces.add(wrapper);
 			
 			if (y.getName()!=null && !"".equals(y.getName())) {
 				if (traceMap!=null) traceMap.put(y.getName(), wrapper);
 				trace.setInternalName(y.getName());
 			}
 			
 			//set trace property
 			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.NONE);
 			int index = system.getTraces().size()+iplot-1;
 			if (index<0) index=0;
 			final Color plotColor = ColorUtility.getSwtColour(colorMap!=null?colorMap.values():null, index);
 			if (colorMap!=null) {
 				if (system.getColorOption()==ColorOption.BY_NAME) {
 					colorMap.put(y.getName(),plotColor);
 				} else {
 					colorMap.put(y,          plotColor);
 				}
 			}
 			trace.setTraceColor(plotColor);
 
 			//add the trace to xyGraph
 			xyGraph.addTrace(trace, xAxis, yAxis, false);
 			
 			
 			if (monitor!=null) monitor.worked(1);
 			iplot++;
 		}
 		
 		xyCanvas.redraw();
 		
 		if (system.isRescale()) {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					system.autoscaleAxes();
 					system.autoscaleAxes();
 				}
 			});
 		}
 		return traces;
 	}
 
 	public void addTrace(ITrace trace) {
 		if (trace instanceof IImageTrace) {
 			system.setPlotType(PlotType.IMAGE); // Only one image allowed at a time
 			final TraceWillPlotEvent evt = new TraceWillPlotEvent(trace, true);
 			system.fireWillPlot(evt);
 			if (!evt.doit) return;
 			xyGraph.addImageTrace((ImageTrace)trace);
 			removeAdditionalAxes(); // Do not have others with images.
 			intensity.setVisible(true);
 
 		} else {
 			system.setPlotType(PlotType.XY);
 			final TraceWillPlotEvent evt = new TraceWillPlotEvent(trace, true);
 			system.fireWillPlot(evt);
 			if (!evt.doit) return;
 			final AspectAxis xAxis = (AspectAxis)getSelectedXAxis();
 			final AspectAxis yAxis = (AspectAxis)getSelectedYAxis();
 			xyGraph.addTrace(((LineTraceImpl)trace).getTrace(), xAxis, yAxis, true);
 			intensity.setVisible(false);
 		}
 		
 		xyCanvas.redraw();
 	
 	}
 
 	public void removeTrace(ITrace trace) {
 		
 		if (trace instanceof LineTraceImpl) {
 			xyGraph.removeTrace(((LineTraceImpl)trace).getTrace());
 		} else if (trace instanceof ImageTrace) {
 			xyGraph.removeImageTrace((ImageTrace)trace);
 		}
 		xyCanvas.redraw();		
 	}
 
 	public void setShowLegend(boolean b) {
 		if (xyGraph!=null) {
 			xyGraph.setShowLegend(b);
 			xyCanvas.redraw();
 		}
 	}
 
 	public void reset() {
 		if (xyGraph!=null) {
 			try {
 				clearAnnotations();
 				clearRegions();
 				clearAxes();
 				clearTraces();
 	
 			} catch (Throwable e) {
 				throw new RuntimeException(e); // We cannot deal with it here.
 			}
 		}
 	}
 		
 	private void clearAxes() {
 		for (Axis axis : xyGraph.getAxisList()) {
 			axis.setRange(0,100);
 			axis.setTitle("");
 			if (axis!=getSelectedXAxis() && axis!=getSelectedYAxis()) {
 				axis.setVisible(false);
 			}
 		}
 	}
 	
 	protected void removeAdditionalAxes() {
 		for (Axis axis : xyGraph.getAxisList()) {
 			if (axis!=getSelectedXAxis() && axis!=getSelectedYAxis()) {
 				axis.setVisible(false);
 			}
 		}
 	}
 
 
 	@Override
 	public IAnnotation createAnnotation(final String name) throws Exception {
 
 		final List<Annotation> anns = xyGraph.getPlotArea().getAnnotationList();
 		for (Annotation annotation : anns) {
 			if (annotation.getName() != null && annotation.getName().equals(name)) {
 				throw new Exception("The annotation name '" + name + "' is already taken.");
 			}
 		}
 
 		final Axis xAxis = (Axis) getSelectedXAxis();
 		final Axis yAxis = (Axis) getSelectedYAxis();
 
 		return new AnnotationWrapper(name, xAxis, yAxis);
 	}
 
 	@Override
 	public void addAnnotation(final IAnnotation annotation) {
 		final AnnotationWrapper wrapper = (AnnotationWrapper) annotation;
 		xyGraph.addAnnotation(wrapper.getAnnotation());
 		xyGraph.getOperationsManager().addCommand(new AddAnnotationCommand(xyGraph, wrapper.getAnnotation()));
 	}
 
 	@Override
 	public void removeAnnotation(final IAnnotation annotation) {
 		final AnnotationWrapper wrapper = (AnnotationWrapper) annotation;
 		xyGraph.removeAnnotation(wrapper.getAnnotation());
 		xyGraph.getOperationsManager().addCommand(new RemoveAnnotationCommand(xyGraph, wrapper.getAnnotation()));
 	}
 
 	@Override
 	public void renameAnnotation(final IAnnotation annotation, String name) {
 		final AnnotationWrapper wrapper = (AnnotationWrapper) annotation;
 		wrapper.getAnnotation().setName(name);
 	}
 
 	public void clearAnnotations(){
 		final List<Annotation>anns = new ArrayList<Annotation>(xyGraph.getPlotArea().getAnnotationList());
 		for (Annotation annotation : anns) {
 			if (annotation==null) continue;
 			xyGraph.getPlotArea().removeAnnotation(annotation);
 		}
 	}
 	
 	@Override
 	public IAnnotation getAnnotation(final String name) {
 		final List<Annotation> anns = xyGraph.getPlotArea().getAnnotationList();
 		for (Annotation annotation : anns) {
 			if (annotation.getName() != null && annotation.getName().equals(name)) {
 				return new AnnotationWrapper(annotation);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void autoscaleAxes() {
 		if (xyGraph==null) return;
 		xyGraph.performAutoScale();
 	}
 	
 	public boolean addRegionListener(final IRegionListener l) {
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
 
 		final IAxis xAxis = getSelectedXAxis();
 		final IAxis yAxis = getSelectedYAxis();
 
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
 		final AbstractSelectionRegion r = (AbstractSelectionRegion) region;
 		xyGraph.addRegion(r);
 	}
 
 	/**
 	 * Remove a selection region to the graph.
 	 * @param region
 	 */
 	public void removeRegion(final IRegion region) {
 		final AbstractSelectionRegion r = (AbstractSelectionRegion) region;
 		xyGraph.removeRegion(r);
 	}
 
 	@Override
 	public void renameRegion(final IRegion region, String name) {
 		if (xyGraph == null) return;
 		xyGraph.renameRegion((AbstractSelectionRegion) region, name);
 	}
 
 	/**
 	 * Get a region by name.
 	 * @param name
 	 * @return
 	 */
 	public IRegion getRegion(final String name) {
 		if (xyGraph == null)
 			return null;
 		return xyGraph.getRegion(name);
 	}
 	
 	@Override
 	public Collection<IRegion> getRegions(final RegionType type) {
 		
 		final Collection<IRegion> regions = getRegions();
 		if (regions==null) return null;
 		
 		final Collection<IRegion> ret= new ArrayList<IRegion>();
 		for (IRegion region : regions) {
 			if (region.getRegionType()==type) {
 				ret.add(region);
 			}
 		}
 		
 		return ret; // may be empty
 	}
 
 	/**
 	 * Get regions
 	 * @param name
 	 * @return
 	 */
 	public Collection<IRegion> getRegions() {
 		if (xyGraph == null) return null;
 		List<AbstractSelectionRegion> regions = xyGraph.getRegions();
 		return new ArrayList<IRegion>(regions);
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
 					
 		AspectAxis axis = new AspectAxis(title, isYAxis);
 		if (side==SWT.LEFT||side==SWT.BOTTOM) {
 		    axis.setTickLabelSide(LabelSide.Primary);
 		} else {
 			axis.setTickLabelSide(LabelSide.Secondary);
 		}
 		axis.setAutoScaleThreshold(0.1);
 		axis.setShowMajorGrid(true);
 		axis.setShowMinorGrid(true);		
 	
 		xyGraph.addAxis(axis);
 		
 		return axis;
 	}	
 	
 	@Override
 	public IAxis removeAxis(final IAxis axis) {
 		if (axis.isPrimaryAxis()) return null;
 		if (!(axis instanceof AspectAxis)) return null;
 		xyGraph.removeAxis((AspectAxis)axis);
 		return axis;
 	}	
 	
 	@Override
 	public List<IAxis> getAxes() {
 		
 		List<Axis> axes = xyGraph.getAxisList();
 		List<IAxis> ret = new ArrayList<IAxis>(axes.size());
 		for (Axis axis : axes) {
 			if (!(axis instanceof IAxis)) continue;
 			ret.add((IAxis)axis);
 		}
 		return ret;
 	}
 
 
 	
 	private IAxis selectedXAxis;
 	private IAxis selectedYAxis;
 
 	@Override
 	public IAxis getSelectedXAxis() {
 		if (selectedXAxis==null) {
 			return (AspectAxis)xyGraph.primaryXAxis;
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
 			return (AspectAxis)xyGraph.primaryYAxis;
 		}
 		return selectedYAxis;
 	}
 
 	@Override
 	public void setSelectedYAxis(IAxis selectedYAxis) {
 		this.selectedYAxis = selectedYAxis;
 	}
 
 	public Image getImage(Rectangle size) {
 		return xyGraph.getImage(size);
 	}
 
 	public IRegion createRegion(String name, RegionType regionType, boolean b) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void dispose() {
 		if (plotActionsCreator!=null) {
 			plotActionsCreator.dispose();
 		}
 		if (xyGraph!=null) {
 			xyGraph.dispose();
 			xyGraph = null;
 		}
 		if (xyCanvas!=null && !xyCanvas.isDisposed()) {
 			xyCanvas.removeMouseWheelListener(getMouseWheelListener());
 			xyCanvas.removeKeyListener(getKeyListener());
 			xyCanvas.dispose();
 		}
 	}
 
 	public void repaint(final boolean autoScale) {
 		if (Display.getDefault().getThread()==Thread.currentThread()) {
 			if (xyCanvas!=null) {
 				if (autoScale) xyGraph.performAutoScale();
 				xyCanvas.layout(xyCanvas.getChildren());
 				xyGraph.revalidate();
 				xyGraph.repaint();
 			}
 		} else {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					if (xyCanvas!=null) {
 						if (autoScale)xyGraph.performAutoScale();
 						xyCanvas.layout(xyCanvas.getChildren());
 						xyGraph.revalidate();
 						xyGraph.repaint();
 					}
 				}
 			});
 		}		
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
 	public void addPropertyChangeListener(IPropertyChangeListener listener) {
 		xyGraph.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(IPropertyChangeListener listener) {
 		xyGraph.removePropertyChangeListener(listener);
 	}
 	
 
 	// Print / Export methods
 	private PrintSettings settings;
 	
 	@Override
 	public void printPlotting(){
 		if (settings==null) settings = new PrintSettings();
 		PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(xyGraph, Display.getDefault(), settings);
 		settings=dialog.open();
 	}
 
 	/**
 	 * Print scaled plotting to printer
 	 */
 	public void printScaledPlotting(){
 		
 		PrintDialog dialog      = new PrintDialog(Display.getDefault().getActiveShell(), SWT.NULL);
 		PrinterData printerData = dialog.open();
 		// TODO There are options on PrintFigureOperation
 		if (printerData != null) {
             final PrintFigureOperation op = new PrintFigureOperation(new Printer(printerData), xyGraph);
             op.run("Print "+xyGraph.getTitle());
 		}
 	}
 
 	@Override
 	public void copyPlotting(){
 		PlotExportPrintUtil.copyGraph(xyGraph.getImage());
 	}
 
 	@Override
 	public String savePlotting(String filename) throws Exception {
 		FileDialog dialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.SAVE);
 		String [] filterExtensions = new String [] {"*.png;*.PNG;*.jpg;*.JPG;*.jpeg;*.JPEG", "*.ps;*.eps"};
 		// TODO ,"*.svg;*.SVG"};
 		if (filename!=null) {
 			dialog.setFilterPath((new File(filename)).getParent());
 		} else {
 			String filterPath = "/";
 			String platform = SWT.getPlatform();
 			if (platform.equals("win32") || platform.equals("wpf")) {
 				filterPath = "c:\\";
 			}
 			dialog.setFilterPath (filterPath);
 		}
 		dialog.setFilterNames (PlotExportPrintUtil.FILE_TYPES);
 		dialog.setFilterExtensions (filterExtensions);
 		filename = dialog.open();
 		if (filename == null)
 			return null;
 		try {
 			final File file = new File(filename);
 			if (file.exists()) {
 				boolean yes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Confirm Overwrite", "The file '"+file.getName()+"' exists.\n\nWould you like to overwrite it?");
 			    if (!yes) return filename;
 			}
 			PlotExportPrintUtil.saveGraph(filename, PlotExportPrintUtil.FILE_TYPES[dialog.getFilterIndex()], xyGraph.getImage());
 			//logger.debug("Plot saved");
 		} catch (Exception e) {
 			throw e;
 		}
 		return filename;
 	}
 
 	@Override
 	public void savePlotting(String filename, String filetype)  throws Exception{
 		if (filename == null)
 			return;
 		try {
 			PlotExportPrintUtil.saveGraph(filename, filetype, xyGraph.getImage());
 			//logger.debug("Plotting saved");
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	protected XYRegionGraph getXYRegionGraph() {
 		return xyGraph;
 	}
 
 	AbstractPlottingSystem getSystem() {
 		return system;
 	}
 
 	public void setXFirst(boolean xfirst) {
 		this.plotActionsCreator.setXfirstButtons(xfirst);
 	}
 
 	public void setRescale(boolean rescale) {
 		this.plotActionsCreator.setRescaleButton(rescale);
 	}
 
 	public String getTitle() {
 		return xyGraph.getTitle();
 	}
 
 	public void setDefaultPlotCursor(Cursor cursor) {
 		xyGraph.getRegionArea().setCursor(cursor);
 		ZoomType.NONE.setCursor(cursor);
 	}
 
 }
