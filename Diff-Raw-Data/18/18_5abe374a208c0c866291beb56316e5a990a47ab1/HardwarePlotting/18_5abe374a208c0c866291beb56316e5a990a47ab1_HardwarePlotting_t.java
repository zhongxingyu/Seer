 package org.dawnsci.plotting.jreality;
 
 import java.awt.Component;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JApplet;
 import javax.swing.JPanel;
 
 import org.dawnsci.plotting.jreality.compositing.CompositeEntry;
 import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1D;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1DStack;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot2D;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot2DMulti;
 import org.dawnsci.plotting.jreality.impl.DataSet3DPlot3D;
 import org.dawnsci.plotting.jreality.impl.DataSetScatterPlot2D;
 import org.dawnsci.plotting.jreality.impl.DataSetScatterPlot3D;
 import org.dawnsci.plotting.jreality.impl.HistogramChartPlot1D;
 import org.dawnsci.plotting.jreality.impl.Plot1DGraphTable;
 import org.dawnsci.plotting.jreality.legend.LegendChangeEvent;
 import org.dawnsci.plotting.jreality.legend.LegendChangeEventListener;
 import org.dawnsci.plotting.jreality.legend.LegendComponent;
 import org.dawnsci.plotting.jreality.legend.LegendTable;
 import org.dawnsci.plotting.jreality.tool.CameraRotationTool;
 import org.dawnsci.plotting.jreality.tool.ClickWheelZoomTool;
 import org.dawnsci.plotting.jreality.tool.ClickWheelZoomToolWithScrollBar;
 import org.dawnsci.plotting.jreality.tool.PanActionListener;
 import org.dawnsci.plotting.jreality.tool.PanningTool;
 import org.dawnsci.plotting.jreality.tool.SceneDragTool;
 import org.dawnsci.plotting.jreality.util.JOGLChecker;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import de.jreality.math.MatrixBuilder;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.scene.tool.Tool;
 import de.jreality.tools.ClickWheelCameraZoomTool;
 import de.jreality.ui.viewerapp.AbstractViewerApp;
 import de.jreality.ui.viewerapp.ViewerApp;
 import de.jreality.ui.viewerapp.ViewerAppSwt;
 import de.jreality.util.SceneGraphUtility;
 import de.jreality.util.Secure;
 import de.jreality.util.SystemProperties;
 
 /**
  * A class allowing plotting to be done to a hardware accelerated 
  * component sitting in an SWT Composite.
  * 
 * Code lifted out of DataSetPlotter. Could refactor at some point.
 * 
  * @author fcp94556
  *
  */
 public class HardwarePlotting implements SelectionListener, PaintListener, Listener {
 
 	
 	private static Logger logger = LoggerFactory.getLogger(HardwarePlotting.class);
 	
 	protected IDataSet3DCorePlot plotter = null;
 
 	private Plot1DGraphTable graphColourTable;
 	private SceneGraphComponent root;
 	private SceneGraphComponent graph;
 	private SceneGraphComponent coordAxes;
 	private SceneGraphComponent coordXLabels;
 	private SceneGraphComponent coordYLabels;
 	private SceneGraphComponent coordZLabels;
 	private SceneGraphComponent coordTicks = null;
 	private SceneGraphComponent toolNode = null;
 	private SceneGraphComponent cameraNode = null;
 	private CameraRotationTool cameraRotateTool = null;
 	private SceneDragTool dragTool = null;
 	private ClickWheelZoomTool zoomTool = null;
 	private boolean hasJOGL;
 	private boolean isInExporting = false;
 	private boolean showScrollBars = true;
 	private LegendComponent legendTable = null;
 	private ClickWheelCameraZoomTool cameraZoomTool = null;
 	private SashForm container;
 	private Composite plotArea;
 	private ScrollBar hBar;
 	private boolean donotProcessEvent = false;
 	private ScrollBar vBar;
 	private Cursor defaultCursor;
 	private AbstractViewerApp viewerApp;
 	private boolean hasJOGLshaders;
 	private PanningTool panTool = null;
 	private PlottingMode currentMode;
 	private boolean useLegend = true;
 
 	private List<IDataset> currentDataSets;
 
 	public HardwarePlotting() {
 		this.currentDataSets = Collections.synchronizedList(new LinkedList<IDataset>());
 	}
 	
 	/**
 	 * Call to create plotting
 	 * @param parent
 	 */
	public void createControl(final Composite parent, PlottingMode initialMode) {
 		
 		init(parent);
 		createUI(parent);
		setInitPlotMode(initialMode);
 	}
 	
	/**
	 * 
	 * @param data
	 * @param axes
	 */
//	public void plot(final AbstractDataset data, final List<AxisValues> axes, final ) {
//		
//	}
	
 	private void setInitPlotMode(PlottingMode mode) {
 		switch (currentMode) {
 		case ONED:
 			plotter = new DataSet3DPlot1D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			toolNode.removeTool(panTool);
 			viewerApp.getSceneRoot().removeTool(cameraZoomTool);
 			break;
 		case ONED_THREED:
 			plotter = new DataSet3DPlot1DStack(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			break;
 		case SCATTER2D:
 			plotter = new DataSetScatterPlot2D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			toolNode.removeTool(panTool);
 			viewerApp.getSceneRoot().removeTool(cameraZoomTool);
 			break;
 		case TWOD:
 			plotter = new DataSet3DPlot2D(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			break;
 		case MULTI2D:
 			plotter = new DataSet3DPlot2DMulti(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			break;
 		case SURF2D:
 			toolNode.removeTool(panTool);
 			plotter = new DataSet3DPlot3D(viewerApp, hasJOGL, false);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);		
 			break;
 		case SCATTER3D:
 			plotter = new DataSetScatterPlot3D(viewerApp, hasJOGL, false);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			break;
 		case BARCHART:
 			toolNode.removeTool(panTool);
 			toolNode.removeTool(dragTool);
 			cameraNode.removeTool(cameraRotateTool);
 			viewerApp.getSceneRoot().removeTool(zoomTool);
 			viewerApp.getSceneRoot().removeTool(cameraZoomTool);
 			plotter = new HistogramChartPlot1D(viewerApp, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			break;
 		case EMPTY:
 			break;
 		}
 	}
 
 	private void init(Composite parent) {
 		graphColourTable = new Plot1DGraphTable();
 		Secure.setProperty(SystemProperties.AUTO_RENDER, "false");
 		root = SceneGraphUtility.createFullSceneGraphComponent("world");
 		graph = SceneGraphUtility.createFullSceneGraphComponent("graph");
 		graph.setOwner(root);
 		coordAxes = SceneGraphUtility.createFullSceneGraphComponent("axis");
 		coordXLabels = SceneGraphUtility.createFullSceneGraphComponent("xLabels");
 		coordYLabels = SceneGraphUtility.createFullSceneGraphComponent("yLabels");
 		coordZLabels = SceneGraphUtility.createFullSceneGraphComponent("zLabels");
 		root.addChild(coordAxes);
 		root.addChild(coordXLabels);
 		root.addChild(coordYLabels);
 		root.addChild(coordZLabels);
 		root.addChild(graph);
 		// check if JOGL is available
 		hasJOGL = JOGLChecker.isHardwareEnabled(parent);
 		
 	}
 	
 
 	private static final String ERROR_MESG_NO_SHADERS = "System does not support OpenGL shaders falling back to compatibily mode. Some advanced features might not work";
 
 	private Composite createUI(Composite parent) {
 		
 		container = new SashForm(parent, SWT.NONE|SWT.VERTICAL);
 		container.addPaintListener(this);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		// Margins make the graph look bad when put as the
 		// main item in a view which is the most common usage.
 		gridLayout.marginBottom = 0;
 		gridLayout.marginTop = 0;
 		gridLayout.horizontalSpacing = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 		gridLayout.marginHeight = 0;
 		container.setLayout(gridLayout);
 		if (hasJOGL)
 			plotArea = new Composite(container, SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL);
 		else
 			plotArea = new Composite(container, SWT.EMBEDDED | SWT.V_SCROLL | SWT.H_SCROLL);
 		hBar = plotArea.getHorizontalBar();
 		hBar.addSelectionListener(this);
 		vBar = plotArea.getVerticalBar();
 		vBar.addSelectionListener(this);
 
 		// Linux GTK hack to keep scroll wheel events from changing
 		// the vertical scrollbar position this interferes with
 		// the MouseWheel zooming
 		if (SWT.getPlatform().equals("gtk")) {
 			plotArea.addListener(SWT.MouseWheel, new Listener() {
 
 				@Override
 				public void handleEvent(Event event) {
 					donotProcessEvent = true;
 				}
 			});
 		}
 
 		hBar.setVisible(false);
 		vBar.setVisible(false);
 		defaultCursor = plotArea.getCursor();
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		plotArea.setLayoutData(gridData);
 		if (hasJOGL) {
 			plotArea.setLayout(new FillLayout());
 			plotArea.addListener(SWT.Resize, this);
 			viewerApp = new ViewerAppSwt(root, plotArea);
 			hasJOGLshaders = JOGLChecker.isShadingSupported(viewerApp);
 			if (!hasJOGLshaders)
 				logger.warn(ERROR_MESG_NO_SHADERS);
 		} else {
 			final JPanel viewerPanel = new JPanel();
 			viewerApp = new ViewerApp(root, true);
 			BoxLayout layout = new BoxLayout(viewerPanel, BoxLayout.Y_AXIS);
 			viewerPanel.setLayout(layout);
 			java.awt.Component comp = ((ViewerApp) viewerApp).getContent();
 			viewerPanel.add(comp);
 			comp.addComponentListener(new ComponentAdapter() {
 				@Override
 				public void componentResized(ComponentEvent arg0) {
 					if (plotter != null) {
 						Component comp = ((ViewerApp) viewerApp).getViewingComponent();
 						plotter.notifyComponentResize(comp.getWidth(), comp.getHeight());
 					}
 				}
 			});
 			java.awt.Frame frame = SWT_AWT.new_Frame(plotArea);
 			JApplet applet = new JApplet();
 			frame.add(applet);
 			applet.add(viewerPanel);
 		}
 		viewerApp.setBackgroundColor(java.awt.Color.white);
 		if (useLegend) buildLegendTable();
 		removeInitialTools();
 		return container;
 	}
 	
 	private void buildLegendTable() {
 		if (legendTable == null || legendTable.isDisposed()) {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.heightHint = 75;
 			legendTable = new LegendTable(container, SWT.DOUBLE_BUFFERED);
 			legendTable.setLayoutData(gridData);
 		}
 		container.setWeights(new int[] {90, 10});		
 		legendTable.addLegendChangeEventListener(new LegendChangeEventListener() {
 			@Override
 			public void legendDeleted(LegendChangeEvent evt) {
 				if (currentMode == PlottingMode.ONED) {
 					int index = evt.getEntryNr();
 					if (index < currentDataSets.size()) {
 						((DataSet3DPlot1D) plotter).removeGraphNode(index);
 						currentDataSets.remove(index);
 						graphColourTable.deleteLegendEntry(index);
 						//historyCounter--;
 						if (useLegend)
 							legendTable.updateTable(graphColourTable);
 						refresh(false);
 					}
 				}
 			}
 
 			@Override
 			public void legendUpdated(LegendChangeEvent evt) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 
 	/**
 	 * Force the render to refresh
 	 * 
 	 * @param async
 	 */
 
 	public synchronized void refresh(boolean async) {
 		if (!isInExporting) {
 			if (viewerApp != null) {
 				if (!async)
 					viewerApp.getCurrentViewer().render();
 				else
 					viewerApp.getCurrentViewer().renderAsync();
 			}
 		}
 		if ((currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED || currentMode == PlottingMode.SCATTER2D)
 				&& legendTable != null)
 			legendTable.updateTable(graphColourTable);
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public synchronized void widgetSelected(SelectionEvent e) {
 		if (!donotProcessEvent) {
 			if (e.getSource().equals(hBar)) {
 				if (currentMode == PlottingMode.TWOD) {
 					double[] matrix = toolNode.getTransformation().getMatrix();
 					double tTransX = matrix[3];
 					double tTransY = matrix[7];
 					double tTransZ = matrix[11];
 					double minValue = -hBar.getMaximum() / 20.0;
 					tTransX = -minValue - hBar.getSelection() / 10.0f;
 					MatrixBuilder.euclidean().translate(tTransX, tTransY, tTransZ).assignTo(toolNode);
 					refresh(false);
 				}
 			} else	if (e.getSource().equals(vBar) && (e.detail != 0 || SWT.getPlatform().equals("gtk"))) {
 				if (currentMode == PlottingMode.TWOD) {
 					double[] matrix = toolNode.getTransformation().getMatrix();
 					double tTransX = matrix[3];
 					double tTransY = matrix[7];
 					double tTransZ = matrix[11];
 					double minValue = -vBar.getMaximum() / 20.0;
 					tTransY = (minValue + vBar.getSelection() / 10.0f)*1.15f;
 					MatrixBuilder.euclidean().translate(tTransX, tTransY, tTransZ).assignTo(toolNode);
 					refresh(false);
 				}
 			} else if (e.data != null) {
 				if (e.data instanceof List<?> && currentMode == PlottingMode.MULTI2D) {
 					List<CompositeEntry> list = (List<CompositeEntry>)e.data;
 					((DataSet3DPlot2DMulti)plotter).updateCompositingSettings(list);
 					refresh(false);
 				}
 			}
 		}
 		donotProcessEvent = false;
 	}
 
 	
 	@Override
 	public void paintControl(PaintEvent e) {
 		viewerApp.getCurrentViewer().render();
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		if (plotter != null) {
 			Rectangle bounds = plotArea.getBounds();
 			plotter.notifyComponentResize(bounds.width, bounds.height);
 		}
 	}
 
 	private void removeInitialTools() {
 		List<SceneGraphComponent> children = viewerApp.getSceneRoot().getChildComponents();
 		List<Tool> rootTools = viewerApp.getSceneRoot().getTools();
 		for (Tool t : rootTools) {
 			if (t instanceof de.jreality.tools.ClickWheelCameraZoomTool)
 				cameraZoomTool = (ClickWheelCameraZoomTool) t;
 		}
 		// remove the automated added rotation tool
 
 		Tool rotateTool = null;
 		Tool dragingTool = null;
 		for (SceneGraphComponent child : children) {
 			List<Tool> tools = child.getTools();
 			for (Tool t : tools) {
 				if (t instanceof de.jreality.tools.RotateTool) {
 					cameraNode = child;
 					rotateTool = t;
 				}
 				if (t instanceof de.jreality.tools.DraggingTool) {
 					toolNode = child;
 					dragingTool = t;
 				}
 			}
 		}
 		if (toolNode != null && dragingTool != null) {
 			toolNode.removeTool(dragingTool);
 			cameraNode.removeTool(rotateTool);
 			panTool = new PanningTool(toolNode);
 			panTool.addPanActionListener(new PanActionListener() {
 				
 				@Override
 				public synchronized void panPerformed(final double xTrans, final double yTrans) {
 					hBar.getDisplay().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							double xtrans = xTrans;
 							double ytrans = yTrans;
 							if (hBar.isVisible()) {
 								double minValue = -hBar.getMaximum() / 20.0;
 								xtrans = Math.max(xtrans, minValue - 0.25);
 								xtrans = Math.min(xtrans, -minValue + 0.25);
 								hBar.setSelection(hBar.getMaximum() - (int) ((xtrans - minValue) * 10));
 							}
 							if (vBar.isVisible()) {
 								double minValue = -vBar.getMaximum() / 20.0;
 								ytrans = Math.max(ytrans, minValue - 1.5);
 								ytrans = Math.min(ytrans, -minValue + 1.5);
 								vBar.setSelection((int) ((ytrans - minValue) * 10));
 							}
 							MatrixBuilder.euclidean().translate(xtrans, ytrans, 0.0).assignTo(toolNode);
 							refresh(false);
 						}
 					});
 				}
 			});
 			cameraRotateTool = new CameraRotationTool();
 			dragTool = new SceneDragTool();
 			toolNode.addTool(panTool);
 		}
 		zoomTool = new ClickWheelZoomToolWithScrollBar(root, 
 													   toolNode, 
 													   (showScrollBars ? hBar : null), 
 													   (showScrollBars ? vBar : null));
 
 	}
 
 	
 	public void setUseLegend(final boolean useLeg) {
 		this.useLegend = useLeg;
 		if (useLegend) {
 			if (legendTable == null) buildLegendTable();
 			legendTable.setVisible(true);
 			legendTable.updateTable(graphColourTable);
 		} else {
 			if (legendTable != null) legendTable.setVisible(false);
 		}
 		legendTable.getParent().layout();
 	}
 
 	
 	public void setEnableImageScrollBars(boolean enable) {
 		if (!enable) {
 			if (hBar != null && hBar.isVisible())
 				hBar.setVisible(enable);
 			if (vBar != null && vBar.isVisible())
 				vBar.setVisible(enable);
 		}
 		if (zoomTool != null) {
 			if (zoomTool instanceof ClickWheelZoomToolWithScrollBar)
 				((ClickWheelZoomToolWithScrollBar)zoomTool).setScrollBars((enable?vBar:null), 
 																		  (enable?hBar:null));
 		}
 		showScrollBars = enable;
 	}
 
 }
