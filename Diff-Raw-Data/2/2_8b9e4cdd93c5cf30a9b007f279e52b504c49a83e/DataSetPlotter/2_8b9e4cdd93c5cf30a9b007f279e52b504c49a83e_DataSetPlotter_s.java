 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting;
 
 import gda.observable.IObserver;
 
 import java.awt.Component;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JApplet;
 import javax.swing.JPanel;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
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
 import org.eclipse.swt.printing.Printer;
 import org.eclipse.swt.printing.PrinterData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.ColourImageData;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.ColourLookupTable;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramChartPlot1D;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.mapfunctions.AbstractMapFunction;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.compositing.CompositeEntry;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.compositing.CompositingControl;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.CompositeOp;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.ScaleType;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.SurfPlotStyles;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.TickFormatting;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.legend.LegendChangeEvent;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.legend.LegendChangeEventListener;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.legend.LegendComponent;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.legend.LegendTable;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DConsumer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay2DConsumer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayConsumer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.SurfacePlotROI;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.CameraRotationTool;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.ClickWheelZoomTool;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.ClickWheelZoomToolWithScrollBar;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PanActionListener;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PanningTool;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PlotActionEvent;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PlotActionEventListener;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.SceneDragTool;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.PlotExportUtil;
 import uk.ac.diamond.scisoft.system.info.JOGLChecker;
 import de.jreality.math.MatrixBuilder;
 import de.jreality.scene.Camera;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.scene.Viewer;
 import de.jreality.scene.tool.Tool;
 import de.jreality.tools.ClickWheelCameraZoomTool;
 import de.jreality.ui.viewerapp.AbstractViewerApp;
 import de.jreality.ui.viewerapp.ViewerApp;
 import de.jreality.ui.viewerapp.ViewerAppSwt;
 import de.jreality.util.CameraUtility;
 import de.jreality.util.SceneGraphUtility;
 import de.jreality.util.Secure;
 import de.jreality.util.SystemProperties;
 
 /**
  * Central Plotting class responsible for all kind of plots and interaction with them
  */
 
 public class DataSetPlotter extends JPanel implements ComponentListener, IObserver, Listener, PaintListener,
 		LegendChangeEventListener, PlotActionEventListener, IMainPlot, SelectionListener, PanActionListener {
 	protected IDataSet3DCorePlot plotter = null;
 
 	private SceneGraphComponent root = null;
 	private SceneGraphComponent graph = null;
 	private SceneGraphComponent coordAxes = null;
 	private SceneGraphComponent bbox = null;
 	private SceneGraphComponent coordTicks = null;
 	private SceneGraphComponent coordXLabels = null;
 	private SceneGraphComponent coordYLabels = null;
 	private SceneGraphComponent coordZLabels = null;
 	private SceneGraphComponent coordGrid = null;
 	private SceneGraphComponent toolNode = null;
 	private SceneGraphComponent cameraNode = null;
 	private int maxDataSingleDim = 256;
 	private int maxDataTotalDim = maxDataSingleDim * maxDataSingleDim;
 	private boolean hasJOGL = false;
 	private boolean hasJOGLshaders = false;
 	private AbstractViewerApp viewerApp = null;
 	private PanningTool panTool = null;
 	private SceneDragTool dragTool = null;
 	private ClickWheelCameraZoomTool cameraZoomTool = null;
 	private ClickWheelZoomTool zoomTool = null;
 	private CameraRotationTool cameraRotateTool = null;
 	private PlottingMode currentMode = PlottingMode.SURF2D;
 	private List<IDataset> currentDataSets = Collections.synchronizedList(new LinkedList<IDataset>());
 	private Plot1DGraphTable graphColourTable;
 	private Cursor defaultCursor = null;
 	private LegendComponent legendTable = null;
 	private InfoBoxComponent infoBox = null;
 	private CompositingControl cmpControl = null;
 	private SashForm container = null;
 	private Composite plotArea = null;
 	private ScrollBar vBar = null;
 	private ScrollBar hBar = null;
 
 	IDataset currentDataSet = null;
 	private boolean useWindow = false;
 	private boolean hasData = false;
 	private boolean xGridActive = true;
 	private boolean yGridActive = true;
 	private boolean zGridActive = true;
 	private double perspFOV = 56.5;
 	private double orthoFOV = 140.0;
 	private int historyCounter = 0;
 	private String xAxisLabel = "";
 	private String yAxisLabel = "";
 	private String zAxisLabel = "";
 	private String x2AxisLabel = "";
 	private boolean useLegend = true;
 	private boolean isInExporting = false;
 	private boolean donotProcessEvent = false;
 	private QSpace qSpace = null;
 	private static final String RENDER_SOFTWARE_PROPERTY_STRING = "uk.ac.diamond.analysis.rcp.plotting.useSoftware";
 	private static final String RENDER_HYBRID_PROPERTY_STRING = "uk.ac.diamond.analysis.rcp.plotting.useGL13";
 	private static final String ERROR_MESG = "DataSet contains either NaNs or Infs can not plot";
 	private static final String ERROR_MESG_NO_SHADERS = "System does not support OpenGL shaders falling back to compatibily mode. Some advanced features might not work";
 	private static final Logger logger = LoggerFactory.getLogger(DataSetPlotter.class);
 
 	private AbstractMapFunction cacheRedFunc;
 	private AbstractMapFunction cacheGreenFunc;
 	private AbstractMapFunction cacheBlueFunc;
 	private AbstractMapFunction cacheAlphaFunc;
 	private boolean cacheInverseRed;
 	private boolean cacheInverseGreen;
 	private boolean cacheInverseBlue;
 	private boolean cacheInverseAlpha;
 	private boolean showScrollBars = true;
 	private double cacheMinValue;
 	private double cacheMaxValue;
 
 	private PrinterData defaultPrinterData;
 	private String printOrientation;
 	private double printScale;
 	private int printResolution;
 	
 	/**
 	 * Define the handness of the coordinate system
 	 */
 
 	public static final double HANDNESS = 1.0; // -1.0 right hand system 1.0
 
 	// left hand system
 
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
 			panTool.addPanActionListener(this);
 			cameraRotateTool = new CameraRotationTool();
 			dragTool = new SceneDragTool();
 			toolNode.addTool(panTool);
 		}
 		zoomTool = new ClickWheelZoomToolWithScrollBar(root, 
 													   toolNode, 
 													   (showScrollBars ? hBar : null), 
 													   (showScrollBars ? vBar : null));
 
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
 		hasJOGL = true;
 		String propString = System.getProperty(RENDER_SOFTWARE_PROPERTY_STRING);
 		if (propString != null && propString.toLowerCase().equals("true")) {
 			logger.warn("Force software render");
 			hasJOGL = false;
 		} else {
 			String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL);
 			hasJOGL = JOGLChecker.canUseJOGL_OpenGL(viewer, parent);
 		}
 		hasData = false;
 		
 		defaultPrinterData = Printer.getDefaultPrinterData();
 		printOrientation = PlotPrintPreviewDialog.portraitText;
 		printScale= 0.5;
 		printResolution = 2;
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
 		legendTable.addIObserver(this);
 		legendTable.addLegendChangeEventListener(this);
 	}
 
 	private void buildInfoBox() {
 		if (infoBox == null || infoBox.isDisposed()) {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.heightHint = 55;
 			infoBox = new InfoBoxComponent(container, SWT.DOUBLE_BUFFERED);
 			infoBox.setLayoutData(gridData);
 			container.setWeights(new int[] {90, 10});		
 		}
 	}
 
 	private void buildCompositingControl() {
 		if (cmpControl == null || cmpControl.isDisposed()) {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.heightHint = 95;
 			cmpControl = new CompositingControl(container, SWT.DOUBLE_BUFFERED);
 			cmpControl.setLayoutData(gridData);		
 			cmpControl.addSelectionListener(this);
 			container.setWeights(new int[] {90, 10});					
 		}
 	}
 	
 	/**
 	 * Get the SWT composite of the DataSetPlotter
 	 * 
 	 * @return the SWT composite of the DataSetPlotter
 	 */
 	public Composite getComposite() {
 		return container;
 	}
 
 	/**
 	 * Create the GUI for the SWT environment
 	 * 
 	 * @param parent
 	 *            Composite the GUI should be contained in
 	 * @return the created Composite
 	 */
 
 	private Composite createSWTGUI(Composite parent) {
 		
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
 			hasJOGLshaders = ((ViewerAppSwt) viewerApp).supportsShaders();
 			String propString = System.getProperty(RENDER_HYBRID_PROPERTY_STRING);
 			if (propString != null && propString.toLowerCase().equals("true")) {
 				hasJOGLshaders = false;
 			}
 			if (!hasJOGLshaders)
 				logger.warn(ERROR_MESG_NO_SHADERS);
 		} else {
 			viewerApp = new ViewerApp(root, true);
 			BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
 			this.setLayout(layout);
 			java.awt.Component comp = ((ViewerApp) viewerApp).getContent();
 			this.add(comp);
 			comp.addComponentListener(this);
 			java.awt.Frame frame = SWT_AWT.new_Frame(plotArea);
 			JApplet applet = new JApplet();
 			frame.add(applet);
 			applet.add(this);
 		}
 		viewerApp.setBackgroundColor(java.awt.Color.white);
 		if (useLegend)
 			buildLegendTable();
 		removeInitialTools();
 		return container;
 	}
 
 	/**
 	 * Constructor of a DataSetPlotter setting a plot mode at the same time
 	 * 
 	 * @param mode
 	 *            PlottingMode to be used
 	 * @param parent
 	 *            parent SWT composite container
 	 */
 	public DataSetPlotter(PlottingMode mode, Composite parent) {
 		this(mode, parent, true);
 	}
 
 	/**
 	 * Constructor of a DataSetPlotter setting a plot mode at the same time
 	 * 
 	 * @param mode
 	 *            PlottingMode to be used
 	 * @param parent
 	 *            parent SWT composite container
 	 * @param useLegend
 	 *            should the legend be shown or not?
 	 */
 	public DataSetPlotter(PlottingMode mode, Composite parent, boolean useLegend) {
 		this.useLegend = useLegend;
 		currentMode = mode;
 		init(parent);
 		createSWTGUI(parent);
 		setInitPlotMode();
 	}
 
 	/**
 	 * @param mode
 	 * @param parent
 	 * @param legend
 	 */
 	public DataSetPlotter(PlottingMode mode, Composite parent, LegendComponent legend) {
 		this.useLegend = true;
 		this.legendTable = legend;
 		this.legendTable.addLegendChangeEventListener(this);
 		currentMode = mode;
 		init(parent);
 		createSWTGUI(parent);
 		setInitPlotMode();
 	}
 
 	private void setInitPlotMode() {
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
 			plotter = new DataSet3DPlot3D(viewerApp, hasJOGL, useWindow);
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
 			plotter = new DataSetScatterPlot3D(viewerApp, hasJOGL, useWindow);
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
 
 	/**
 	 * Get the maximum total data size in a single dimension that can be displayed at once (important for 3D of 2D
 	 * plots)
 	 * 
 	 * @return the maximum single dimension size
 	 */
 	public int getMaximumSingleDimension() {
 		return maxDataSingleDim;
 	}
 
 	/**
 	 * Get the maximum total data size that can be displayed at once (important for 3D of 2D and 1D plots)
 	 * 
 	 * @return the maximum total size
 	 */
 
 	public int getMaximumTotalDimension() {
 		return maxDataTotalDim;
 	}
 
 	private void removeOldSceneNodes() {
 		if (bbox != null) {
 			root.removeChild(bbox);
 			bbox = null;
 		}
 		if (coordTicks != null) {
 			root.removeChild(coordTicks);
 			coordTicks = null;
 		}
 		if (coordGrid != null) {
 			root.removeChild(coordGrid);
 			coordGrid = null;
 		}
 		if (currentMode == PlottingMode.TWOD) {
 			if (root != null && root.getTransformation() != null)
 				root.getTransformation().removeTransformationListener((DataSet3DPlot2D) plotter);
 		}
 		if (graph != null) {
 			graph.setGeometry(null);
 		}
 		// remove all hanged on children on the graph node
 		if (plotter != null)
 			plotter.cleanUpGraphNode();
 
 		// since we removed all the previous
 		// scene nodes now might be a good time
 		// to call garbage collector to make sure
 
 		// System.gc();
 	}
 
 	/**
 	 * This function applies the colour cast by using a HistogramUpdate object
 	 * @param update
 	 * 			Histogram update object
 	 * 
 	 */
 	
 	public void applyColourCast(HistogramUpdate update) {
 		applyColourCast(update.getRedMapFunction(), update.getGreenMapFunction(), 
 						update.getBlueMapFunction(), update.getAlphaMapFunction(), 
 						update.inverseRed(), update.inverseGreen(),	update.inverseBlue(), 
 						update.inverseAlpha(), update.getMinValue(), update.getMaxValue());
 	}
 	
 	/**
 	 * This function applies the colour cast 
 	 * 
 	 * @param redFunc
 	 *            red channel mapping function
 	 * @param greenFunc
 	 *            green channel mapping function
 	 * @param blueFunc
 	 *            blue channel mapping function
 	 * @param alphaFunc
 	 *            alpha channel mapping function
 	 * @param inverseRed
 	 *            inverse red channel
 	 * @param inverseGreen
 	 *            inverse green channel
 	 * @param inverseBlue
 	 *            inverse blue channel
 	 * @param inverseAlpha
 	 *            inverse alpha channel
 	 * @param minValue
 	 *            minimum value
 	 * @param maxValue
 	 *            maximum value
 	 */
 	public void applyColourCast(AbstractMapFunction redFunc, AbstractMapFunction greenFunc,
 			AbstractMapFunction blueFunc, AbstractMapFunction alphaFunc, boolean inverseRed, boolean inverseGreen,
 			boolean inverseBlue, boolean inverseAlpha, double minValue, double maxValue) {
 
 		if (currentDataSets.size() > 0 && graph != null) {
 			ColourImageData imageData = null;
 			if (hasJOGLshaders || currentMode == PlottingMode.SCATTER3D) {
 				imageData = ColourLookupTable.generateColourLookupTable(redFunc, greenFunc, blueFunc, alphaFunc,
 						inverseRed, inverseGreen, inverseBlue, inverseAlpha);
 			} else {
 				cacheRedFunc = redFunc;
 				cacheGreenFunc = greenFunc;
 				cacheBlueFunc = blueFunc;
 				cacheAlphaFunc = alphaFunc;
 				cacheInverseAlpha = inverseAlpha;
 				cacheInverseRed = inverseRed;
 				cacheInverseGreen = inverseGreen;
 				cacheInverseBlue = inverseBlue;
 				cacheMinValue = minValue;
 				cacheMaxValue = maxValue;
 				if (currentMode != PlottingMode.BARCHART)
 					imageData = ColourLookupTable.generateColourTable(currentDataSets.get(0), redFunc, greenFunc,
 							blueFunc, alphaFunc, inverseRed, inverseGreen, inverseBlue, inverseAlpha, minValue,
 							maxValue, plotter.getScaling() != ScaleType.LINEAR);
 				else
 					imageData = ColourLookupTable.generateColourLookupTable(redFunc, greenFunc, blueFunc, alphaFunc,
 							inverseRed, inverseGreen, inverseBlue, inverseAlpha);
 			}
 			plotter.handleColourCast(imageData, graph, minValue, maxValue);
 		}
 		// System.gc();
 	}
 
 	/**
 	 * @param newData
 	 *            DataSet that should be plotted
 	 */
 
 	public void setPlot(IDataset newData) {
 		if (currentDataSet != null) {
 			currentDataSets.remove(0);
 			currentDataSets.add(0, newData);
 			plotter.updateGraph(newData);
 		} else {
 			currentDataSets.add(newData);
 			graph = plotter.buildGraph(currentDataSets, graph);
 			coordAxes = plotter.buildCoordAxis(coordAxes);
 			hasData = true;
 			plotter.setXAxisLabel(xAxisLabel);
 			plotter.setYAxisLabel(yAxisLabel);
 		}
 	}
 
 	/**
 	 * Add more 1D plots to the graph
 	 * 
 	 * @param datasets
 	 *            n number of 1D datasets that should be plotted
 	 */
 	@Deprecated
 	public void addPlot(IDataset... datasets) {
 		if (currentMode == PlottingMode.SURF2D) {
 			logger.info("Plot3D is currently in 2D mode but 1D plot has been added switching to Multi 1D");
 			currentDataSets.clear();
 
 			for (int i = 0; i < datasets.length; i++)
 				currentDataSets.add(datasets[i]);
 			if (datasets.length > 1)
 				setMode(PlottingMode.ONED_THREED);
 			else {
 				setMode(PlottingMode.ONED);
 				currentDataSet = datasets[0];
 			}
 
 		} else {
 			for (int i = 0; i < datasets.length; i++)
 				currentDataSets.add(datasets[i]);
 			checkAndAddLegend(currentDataSets);
 			if (currentDataSet == null) {
 				graph = plotter.buildGraph(currentDataSets, graph);
 				coordAxes = plotter.buildCoordAxis(coordAxes);
 				hasData = true;
 				plotter.setXAxisLabel(xAxisLabel);
 				plotter.setYAxisLabel(yAxisLabel);
 				currentDataSet = datasets[0];
 			} else
 				plotter.updateGraph(currentDataSets);
 		}
 	}
 
 	/**
 	 * Add some double values to the current DataSet that is plotted by appending the additional data
 	 * 
 	 * @param values
 	 *            array of double values to be added
 	 * @throws PlotException
 	 *             if something goes wrong by adding these value points
 	 */
 	public void addToCurrentPlot(double[] values) throws PlotException {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			try {
 				((DataSet3DPlot1D) plotter).addDataPoints(values);
 			} catch (PlotException ex) {
 				throw ex;
 			}
 		}
 	}
 
 	/**
 	 * Add a list of double values to the current DataSets that are plotted
 	 * 
 	 * @param valueList
 	 *            List of array of double values to be added
 	 * @throws PlotException
 	 *             if something goes wrong by adding these value points
 	 */
 	public void addToCurrentPlots(List<double[]> valueList) throws PlotException {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			try {
 				((DataSet3DPlot1D) plotter).addDataPoints(valueList);
 			} catch (PlotException ex) {
 				throw ex;
 			}
 		}
 	}
 
 	/**
 	 * Add some double values to the current DataSet that is plotted with some additional axis values as well
 	 * 
 	 * @param values
 	 *            array of double values to be added as data
 	 * @param axisValues
 	 *            array of double values to be added to the axis values
 	 * @throws PlotException
 	 *             if something goes wrong by adding these value points
 	 */
 
 	public void addToCurrentPlot(double[] values, double[] axisValues) throws PlotException {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			if (values.length != axisValues.length)
 				throw new PlotException("Length of values doesn't match length of axisValues");
 			try {
 				((DataSet3DPlot1D) plotter).addDataPoints(values, axisValues);
 			} catch (PlotException ex) {
 				throw ex;
 			}
 		}
 	}
 
 	/**
 	 * Add some new data to a scatter plot
 	 * 
 	 * @param newValues
 	 *            Dataset containing new values
 	 * @param newXaxis
 	 *            new X-Axis values
 	 * @param newYaxis
 	 *            new Y-Axis values
 	 * @throws PlotException
 	 *             if something goes wrong by adding these value points
 	 */
 
 	public void addToCurrentPlot(IDataset newValues, AxisValues newXaxis, AxisValues newYaxis) throws PlotException {
 		if (currentMode == PlottingMode.SCATTER2D) {
 			if (currentDataSets.size() == 0)
 				throw new PlotException("There is no data in the scatter plot so far");
 			currentDataSets.add(newValues);
 			checkAndAddLegend(currentDataSets);
 			if (newValues.getSize() != newXaxis.size())
 				throw new PlotException("Length of values doesn't match length of axisValues");
 			((DataSetScatterPlot2D) plotter).addAxises(newXaxis, newYaxis);
 			plotter.updateGraph(currentDataSets);
 		}
 	}
 
 	/**
 	 * Add a list of double values to the current DataSets and a list of doubles to the axis values that are plotted
 	 * 
 	 * @param valueList
 	 *            list of double values to be added to the plotted DataSets
 	 * @param axisValues
 	 *            list of double values to be added to the axis values
 	 * @throws PlotException
 	 *             if something goes wrong by adding these value points
 	 */
 	public void addToCurrentPlots(List<double[]> valueList, List<double[]> axisValues) throws PlotException {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			if (valueList.size() != axisValues.size())
 				throw new PlotException("Lenght of value list doesn't match length of axis value list");
 			try {
 				((DataSet3DPlot1D) plotter).addDataPoints(valueList, axisValues);
 			} catch (PlotException ex) {
 				throw ex;
 			}
 		}
 	}
 
 	/**
 	 * Set the x axis label
 	 * 
 	 * @param label
 	 */
 	public void setXAxisLabel(String label) {
 		xAxisLabel = label;
 		if (hasData)
 			plotter.setXAxisLabel(xAxisLabel);
 	}
 
 	public String getXAxisLabel() {
 		return xAxisLabel;
 	}
 
 	public String getYAxisLabel() {
 		return yAxisLabel;
 	}
 
 	public String getZAxisLabel() {
 		return zAxisLabel;
 	}
 
 	/**
 	 * Set the y axis label
 	 * 
 	 * @param label
 	 */
 	public void setYAxisLabel(String label) {
 		yAxisLabel = label;
 		if (hasData)
 			plotter.setYAxisLabel(yAxisLabel);
 	}
 
 	/**
 	 * Set the z axis label
 	 * 
 	 * @param label
 	 */
 
 	public void setZAxisLabel(String label) {
 		zAxisLabel = label;
 		if (hasData)
 			plotter.setZAxisLabel(zAxisLabel);
 	}
 
 	/**
 	 * Replace a series of datasets (or one) with a pair of AxisValues this makes only sense when the AxisMode has been
 	 * set to custom
 	 * 
 	 * @param dataSets
 	 *            List of datasets
 	 * @param xAxisValues
 	 *            List of xAxis values
 	 * @throws PlotException
 	 *             if the number of AxisValues doesn't match number of DataSets
 	 */
 	public void replaceAllPlots(Collection<? extends IDataset> dataSets, List<AxisValues> xAxisValues) throws PlotException {
 		// sanityCheckDataSets(dataSets); // TODO Still necessary?
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			if (dataSets.size() > xAxisValues.size())
 				throw new PlotException("Number of DataSets is larger than Axis values");
 			((DataSet3DPlot1D) plotter).setXAxisValues(xAxisValues, historyCounter);
 			replaceAllPlots(dataSets);
 		}
 	}
 	
 	public void replaceAllPlots(Collection<? extends IDataset> dataSets,
 								List<AxisValues> xAxisValues,
 								List<AxisValues> yAxisValues) throws PlotException
 	{
 		if (currentMode == PlottingMode.SCATTER2D) {
 			if (dataSets.size() != xAxisValues.size() ||
 				dataSets.size() != yAxisValues.size()) 
 				throw new PlotException("Number of DataSets different to Axis values");
 			((DataSetScatterPlot2D)plotter).replaceAxises(xAxisValues, yAxisValues);
 			replaceAllPlots(dataSets);
 		}
 	}
 
 	private void checkAndAddLegend(Collection<? extends IDataset> dataSets) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.SCATTER2D) {
 			if (dataSets != null && dataSets.size() > graphColourTable.getLegendSize()) {
 				logger.info("# graphs > # of entries in the legend will auto add entries");
 				for (int i = graphColourTable.getLegendSize(); i < dataSets.size(); i++) {
 					graphColourTable.addEntryOnLegend(new Plot1DAppearance(PlotColorUtility.getDefaultColour(i),
 							PlotColorUtility.getDefaultStyle(i), ""));
 				}
 			}
 		}
 	}
 
 	private boolean checkForNan(IDataset data) {
 		if (data instanceof AbstractDataset)
 			return ((AbstractDataset) data).containsNans();
 
 		for (int i = 0; i < data.getShape()[0]; i++)
 			if (Double.isNaN(data.getDouble(i)))
 				return true;
 		return false;
 	}
 
 	private boolean checkForInf(IDataset data) {
 		if (data instanceof AbstractDataset)
 			return ((AbstractDataset) data).containsInfs();
 
 		for (int i = 0; i < data.getShape()[0]; i++)
 			if (Double.isInfinite(data.getDouble(i)))
 				return true;
 		return false;
 	}
 
 	private void sanityCheckDataSets(Collection<? extends IDataset> datasets) throws PlotException {
 		Iterator<? extends IDataset> iter = datasets.iterator();
 		while (iter.hasNext()) {
 			IDataset dataset = iter.next();
 
 			if (checkForNan(dataset) || checkForInf(dataset)) {
 				throw new PlotException(ERROR_MESG);
 			}
 		}
 	}
 
 	/**
 	 * Replace all the current datasets that are have been plotted with a whole set of new ones
 	 * 
 	 * @param datasets
 	 *            list of new datasets
 	 * @throws PlotException
 	 *             throws a PlotException when something goes wrong
 	 */
 
 	public void replaceAllPlots(Collection<? extends IDataset> datasets) throws PlotException {
 		checkAndAddLegend(datasets);
 		sanityCheckDataSets(datasets); // TODO still necessary?
 		if (currentDataSets.size() > 0) {
 			int actualDataSets = currentDataSets.size() - historyCounter;
 
 			if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.SCATTER2D) {
 				if (actualDataSets > datasets.size())
 					for (int i = 0; i < actualDataSets - datasets.size(); i++)
 						((DataSet3DPlot1D) plotter).removeLastGraphNode();
 				if (actualDataSets < datasets.size())
 					for (int i = 0; i < datasets.size() - actualDataSets; i++)
 						((DataSet3DPlot1D) plotter).addGraphNode();
 			}
 
 			for (int i = currentDataSets.size() - (historyCounter + 1); i >= 0; i--)
 				currentDataSets.remove(i);
 			currentDataSets.addAll(0, datasets);
 			plotter.updateGraph(currentDataSets);
 		} else {
 			currentDataSets.addAll(datasets);
 			coordAxes = plotter.buildCoordAxis(coordAxes);
 			graph = plotter.buildGraph(currentDataSets, graph);
 			hasData = true;
 			plotter.setXAxisLabel(xAxisLabel);
 			if (currentMode == PlottingMode.ONED)
 				((DataSet3DPlot1D)plotter).setSecondaryXAxisLabel(x2AxisLabel);
 			
 			plotter.setYAxisLabel(yAxisLabel);
 			plotter.setZAxisLabel(zAxisLabel);
 		}
 		if (currentMode == PlottingMode.SURF2D || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.SCATTER3D) {
 			root.removeChild(bbox);
 			bbox = plotter.buildBoundingBox();
 			root.addChild(bbox);
 		}
 		if (currentMode == PlottingMode.MULTI2D) {
 			List<CompositeEntry> table = new ArrayList<CompositeEntry>();
 			for (int i = 0; i < currentDataSets.size(); i++) {
 				String name = currentDataSets.get(i).getName();
 				float weight = 1.0f / currentDataSets.size();
 				if (name == null)
 					name = "";
 				CompositeEntry entry = 
 					new CompositeEntry(name, weight, CompositeOp.ADD,(byte)7);
 				table.add(entry);
 			}			
 			if (cmpControl != null)
 				cmpControl.updateTable(table);
 			((DataSet3DPlot2DMulti)plotter).updateCompositingSettings(table);			
 		}
 		if (currentDataSets.size() > 0) {
 			currentDataSet = currentDataSets.get(0);
 			checkForDiffractionImage(currentDataSet);
 		}
 	}
 
 	/**
 	 * Replace the current dataset that is plotted with a new one
 	 * 
 	 * @param dataset
 	 *            that replaces the current one
 	 * @throws PlotException
 	 *             a plot exception when something goes wrong
 	 */
 
 	public void replaceCurrentPlot(IDataset dataset) throws PlotException {
 		if (checkForNan(dataset) || checkForInf(dataset))
 			throw new PlotException(ERROR_MESG);
 
 		if (currentDataSet != null) {
 			currentDataSets.remove(0);
 			currentDataSets.add(0, dataset);
 			checkAndAddLegend(currentDataSets);
 			plotter.updateGraph(dataset);
 		} else {
 			currentDataSets.add(dataset);
 			checkAndAddLegend(currentDataSets);
 			graph = plotter.buildGraph(currentDataSets, graph);
 			coordAxes = plotter.buildCoordAxis(coordAxes);
 			hasData = true;
 			plotter.setXAxisLabel(xAxisLabel);
 			plotter.setYAxisLabel(yAxisLabel);
 			plotter.setZAxisLabel(zAxisLabel);
 		}
 		if (currentMode == PlottingMode.SURF2D || currentMode == PlottingMode.SCATTER3D) {
 			root.removeChild(bbox);
 			bbox = plotter.buildBoundingBox();
 			root.addChild(bbox);
 		}
 		currentDataSet = dataset;
 		checkForDiffractionImage(dataset);
 	}
 
 	/**
 	 * Replace a plot in a series of plots
 	 * 
 	 * @param dataset
 	 *            new dataset of that plot
 	 * @param plotNumber
 	 *            the number of the plot in the graph list
 	 * @throws PlotException
 	 *             if something doesn't match up
 	 */
 	public void replaceAPlot(IDataset dataset, int plotNumber) throws PlotException {
 		if (checkForNan(dataset) || checkForInf(dataset))
 			throw new PlotException(ERROR_MESG);
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			if (plotNumber < currentDataSets.size()) {
 				((DataSet3DPlot1D) plotter).updateAGraph(dataset, plotNumber);
 			} else
 				throw new PlotException("This plotNumber doesn't exist");
 		}
 	}
 
 	/**
 	 * Replace a plot in a series of plots
 	 * 
 	 * @param dataset
 	 *            new dataset of that plot
 	 * @param newAxis
 	 *            new x-axis values for that plot
 	 * @param plotNumber
 	 *            the number of the plot in the graph list
 	 * @throws PlotException
 	 *             if something doesn't match up
 	 */
 	public void replaceAPlot(IDataset dataset, AxisValues newAxis, int plotNumber) throws PlotException {
 		if (checkForNan(dataset) || checkForInf(dataset))
 			throw new PlotException(ERROR_MESG);
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			if (plotNumber < currentDataSets.size()) {
 				((DataSet3DPlot1D) plotter).replaceXAxisValue(newAxis, plotNumber);
 				((DataSet3DPlot1D) plotter).updateAGraph(dataset, plotNumber);
 			} else
 				throw new PlotException("This plotNumber doesn't exist");
 		}
 	}
 
 	private void checkForDiffractionImage(IDataset currentDataset) {
 		if (currentMode == PlottingMode.TWOD || currentMode == PlottingMode.SURF2D) {
 			qSpace = null;
 			boolean isDiffImage = false;
 			if (currentDataset instanceof AbstractDataset) {
 				AbstractDataset image = (AbstractDataset) currentDataset;
 				IMetaData metadata = image.getMetadata();
 				if (metadata instanceof IDiffractionMetadata) {
 					IDiffractionMetadata diffnMetadata = (IDiffractionMetadata) metadata;
 					try {
 						qSpace = new QSpace(diffnMetadata.getDetector2DProperties(),
 								diffnMetadata.getDiffractionCrystalEnvironment());
 						isDiffImage = true;
					} catch (IllegalArgumentException e) {
 						logger.debug("Could not create a detector properties object from metadata");
 					}
 				}
 			}
 			final boolean l_bfDiffImage = isDiffImage;
 			if (infoBox != null) {
 				infoBox.getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						infoBox.isDiffractionImage(l_bfDiffImage);
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Set q-space for diffraction image so info box can display q-space information
 	 * 
 	 * @param qspace
 	 */
 	public void setQSpace(QSpace qspace) {
 		qSpace = qspace;
 	}
 
 	/**
 	 * Empty the plot
 	 * 
 	 */	
 	
 	public void emptyPlot() {
 		setMode(PlottingMode.EMPTY);
 		coordXLabels.setVisible(false);
 		coordYLabels.setVisible(false);
 		coordZLabels.setVisible(false);
 		coordAxes.setVisible(false);		
 	}
 	
 	private void clearPlot() {
 		historyCounter = 0;
 		currentDataSets.clear();
 		currentDataSet = null;		
 		removeOldSceneNodes();
 		if (legendTable != null) {
 			legendTable.removeAllLegendChangeEventListener();
 			legendTable.dispose();
 			legendTable = null;
 		}
 		if (infoBox != null) {
 			infoBox.dispose();
 			infoBox = null;
 		}
 		if (cmpControl != null) {
 			cmpControl.removeSelectionListener(this);
 			cmpControl.dispose();
 			cmpControl = null;
 		}
 		
 	}
 	
 	/**
 	 * Set the plotter to a new plotting mode
 	 * 
 	 * @param newPlotMode
 	 *            the new plotting mode
 	 */
 
 	public void setMode(PlottingMode newPlotMode) {
 		clearPlot();
 		if (newPlotMode != currentMode)
 			hasData = false;
 		currentMode = newPlotMode;
 		if (hasJOGL)
 			plotArea.setFocus();
 
 		// this might be a bit strange but to make sure
 		// the tool doesn't get added twice first remove
 		// it if it isn't attached it will simply do nothing
 
 		toolNode.removeTool(panTool);
 		toolNode.removeTool(dragTool);
 		cameraNode.removeTool(cameraRotateTool);
 		viewerApp.getSceneRoot().removeTool(zoomTool);
 		viewerApp.getSceneRoot().removeTool(cameraZoomTool);
 
 		switch (currentMode) {
 		case ONED:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSet3DPlot1D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			buildLegendTable();
 			container.layout();
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);			
 			break;
 		case ONED_THREED:
 			// this might be a bit strange but to make sure
 			// the tool doesn't get added twice first remove
 			// it if it isn't attached it will simply do nothing
 			plotter = new DataSet3DPlot1DStack(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			buildLegendTable();
 			container.layout();
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);
 			hBar.setVisible(false);
 			vBar.setVisible(false);			
 			break;
 		case SCATTER2D:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSetScatterPlot2D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			buildLegendTable();
 			container.layout();
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case TWOD:
 		{	
 			root.removeChild(coordTicks);
 			plotter = new DataSet3DPlot2D(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			toolNode.addTool(panTool);
 			viewerApp.getSceneRoot().addTool(zoomTool);
 			if (useLegend)
 				buildInfoBox();
 			container.layout();
 			root.getTransformation().addTransformationListener((DataSet3DPlot2D) plotter);
 			setPerspectiveCamera(true,false);			
 			break;
 		}
 		case MULTI2D:
 			root.removeChild(coordTicks);
 			plotter = new DataSet3DPlot2DMulti(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			toolNode.addTool(panTool);
 			viewerApp.getSceneRoot().addTool(zoomTool);
 			buildCompositingControl();
 			container.layout();
 			root.getTransformation().addTransformationListener((DataSet3DPlot2D) plotter);
 			setPerspectiveCamera(true,false);			
 			break;
 		case SURF2D:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSet3DPlot3D(viewerApp, hasJOGL, useWindow);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildCoordAxis(coordAxes);
 			container.layout();
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case SCATTER3D:
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			plotter = new DataSetScatterPlot3D(viewerApp, hasJOGL, useWindow);
 			plotter.buildXCoordLabeling(coordXLabels);
 			plotter.buildYCoordLabeling(coordYLabels);
 			plotter.buildZCoordLabeling(coordZLabels);
 			coordTicks = plotter.buildCoordAxesTicks();
 			root.addChild(coordTicks);
 			plotter.buildCoordAxis(coordAxes);
 			container.layout();
 			toolNode.addTool(dragTool);
 			cameraNode.addTool(cameraRotateTool);
 			viewerApp.getSceneRoot().addTool(cameraZoomTool);
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case BARCHART:
 			plotter = new HistogramChartPlot1D(viewerApp, graphColourTable, hasJOGL);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
 			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
 			setPerspectiveCamera(true,false);
 			hBar.setVisible(false);
 			vBar.setVisible(false);						
 			break;
 		case EMPTY:
 			Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
 			sceneCamera.setPerspective(true);			
 			break;
 		}
 		coordXLabels.setVisible(true);
 		coordYLabels.setVisible(true);	
 		coordZLabels.setVisible(true);
 		coordAxes.setVisible(true);
 	}
 
 	/**
 	 * Get the current plotting mode
 	 * 
 	 * @return the current plotting mode
 	 */
 	public PlottingMode getMode() {
 		return currentMode;
 	}
 
 	private void cleanUpViewers() {
 		Viewer[] viewers = viewerApp.getViewerSwitch().getViewers();
 		for (int i = 0; i < viewers.length; i++)
 			if (viewers[i] instanceof de.jreality.softviewer.SoftViewer) {
 				((de.jreality.softviewer.SoftViewer) viewers[i]).dispose();
 			}
 	}
 
 	/**
 	 * CleanUp the data when it is inactive, that hopefully will reduce the memory footprint
 	 */
 	public void cleanUp() {
 		if (plotter != null)
 			plotter.cleanUpGraphNode();
 		try {
 			removeOldSceneNodes();
 			if (graph != null)
 				 graph.setOwner(null);
 			if (bbox != null)
 				root.removeChild(bbox);
 			if (coordGrid != null)
 				root.removeChild(coordGrid);
 			if (coordXLabels != null)
 				root.removeChild(coordXLabels);
 			if (coordYLabels != null)
 				root.removeChild(coordYLabels);
 			if (coordZLabels != null)
 				root.removeChild(coordZLabels);
 			if (coordTicks != null)
 				root.removeChild(coordTicks);
 
 			if (panTool != null)
 				panTool.removeAllPanActionListener();
 
 			bbox = null;
 			coordTicks = null;
 			coordXLabels = null;
 			coordYLabels = null;
 			coordZLabels = null;
 			coordGrid = null;
 			root = null;
 			currentDataSets.clear();
 			currentDataSet = null;
 			plotter = null;
 			if (graphColourTable != null)
 				graphColourTable.clearLegend();
 			graphColourTable = null;
 			cleanUpViewers();
 			try {
 				viewerApp.dispose();
 			} catch (Exception e) {
 				logger.debug("oh no", e);
 			}
 			if (plotArea != null)
 				plotArea.dispose();
 			if (container != null)
 				container.dispose();
 			if (legendTable != null) {
 				legendTable.removeAllLegendChangeEventListener();
 				legendTable.dispose();
 			}
 			if (infoBox != null && !infoBox.isDisposed())
 				infoBox.dispose();
 
 		} catch (RuntimeException ne) {
 			if (isDisposed()) {
 				// Well we are disposed, it might not be possible to clean up all things
 				return;
 			}
 			throw ne; // This exception gets sent back otherwise
 		}
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent evt) {
 		// Nothing to do
 
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent evt) {
 		// Nothing to do
 
 	}
 
 	@Override
 	public void componentResized(ComponentEvent arg0) {
 		if (plotter != null) {
 			Component comp = ((ViewerApp) viewerApp).getViewingComponent();
 			plotter.notifyComponentResize(comp.getWidth(), comp.getHeight());
 		}
 	}
 
 	@Override
 	public void componentShown(ComponentEvent evt) {
 		// Nothing to do
 	}
 
 	/**
 	 * Set the colour of the current graph only works with Plot1D and MultiPlot1D
 	 * 
 	 * @param graphNr
 	 *            number of the graph that should be updated
 	 */
 
 	public void setCurrentGraphColour(int graphNr) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			((DataSet3DPlot1D) plotter).updateGraphAppearance(graphNr);
 			if (legendTable != null)
 				legendTable.updateTable(graphColourTable);
 		}
 	}
 
 	/**
 	 * Update the appearance of all graphs
 	 */
 	public void updateAllAppearance() {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.BARCHART || currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).updateAllGraphAppearances();
 			if (legendTable != null)
 				legendTable.updateTable(graphColourTable);
 		}
 	}
 
 	/**
 	 * Undo a zoom step
 	 */
 
 	public void undoZoom() {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED ||
 			currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).undoZoom();
 		} else if (currentMode == PlottingMode.BARCHART) {
 			((HistogramChartPlot1D) plotter).undoZoom();
 		}
 	}
 
 	/**
 	 * Reset / flat the zoom history to zero
 	 */
 
 	public void resetZoom() {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED ||
 			currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).resetZoom();
 		}
 	}
 
 	
 	/**
 	 * Push current graph in 1D mode onto history
 	 */
 
 	public void pushGraphOntoHistory() {
 		if (currentMode == PlottingMode.ONED) {
 			IDataset history = DatasetUtils.convertToAbstractDataset(currentDataSets.get(0)).clone();
 			currentDataSets.add(history);
 			((DataSet3DPlot1D) plotter).addGraphNode();
 			historyCounter++;
 		}
 	}
 
 	/**
 	 * Pop the last graph from the history in 1D mode
 	 */
 
 	public void popGraphFromHistory() {
 		if (currentMode == PlottingMode.ONED) {
 			if (currentDataSets.size() > 1) {
 				((DataSet3DPlot1D) plotter).removeLastGraphNode();
 				currentDataSets.remove(currentDataSets.size() - 1);
 				historyCounter--;
 				if (useLegend)
 					legendTable.updateTable(graphColourTable);
 			}
 		}
 	}
 
 	/**
 	 * Get the number of history entries
 	 * 
 	 * @return number of history entries
 	 */
 
 	public int getNumHistory() {
 		return historyCounter;
 	}
 
 	/**
 	 * Set the different axis modes on all the axis
 	 * 
 	 * @param xAxis
 	 *            mode for the x-axis
 	 * @param yAxis
 	 *            mode for the y-axis
 	 * @param zAxis
 	 *            mode for the z-axis
 	 */
 	public void setAxisModes(AxisMode xAxis, AxisMode yAxis, AxisMode zAxis) {
 		plotter.setAxisModes(xAxis, yAxis, zAxis);
 	}
 
 	/**
 	 * Set the different offsets on the axis if the Axis mode is linear with offset
 	 * 
 	 * @param xOffset
 	 *            offset on the x-axis
 	 * @param yOffset
 	 *            offset on the y-axis
 	 * @param zOffset
 	 *            offset on the z-axis
 	 */
 	public void setAxisOffset(double xOffset, double yOffset, double zOffset) {
 		if (plotter != null) {
 			plotter.setXAxisOffset(xOffset);
 			plotter.setYAxisOffset(yOffset);
 			plotter.setZAxisOffset(zOffset);
 		}
 	}
 
 	/**
 	 * Set x Axis values that map from each entry of the data set as an x value
 	 * 
 	 * @param xAxis
 	 *            x-axis values container
 	 * @param numOfDataSets
 	 *            the number of datasets this axis is referencing to
 	 */
 	public void setXAxisValues(AxisValues xAxis, int numOfDataSets) {
 		if (plotter != null) {
 			plotter.setXAxisValues(xAxis, numOfDataSets);
 		}
 	}
 
 	public void setSecondaryXAxisValues(AxisValues xAxis2, String axisName) {
 		if (plotter != null && currentMode == PlottingMode.ONED) {
 			((DataSet3DPlot1D)plotter).set2ndXAxisValues(xAxis2);
 			x2AxisLabel = axisName;
 			if (hasData)
 				((DataSet3DPlot1D)plotter).setSecondaryXAxisLabel(axisName);			
 		}
 	}
 	
 	/**
 	 * Set y Axis values that map from each entry of the data set as an y value
 	 * 
 	 * @param yAxis
 	 *            y-axis values container
 	 */
 	public void setYAxisValues(AxisValues yAxis) {
 		if (plotter != null) {
 			plotter.setYAxisValues(yAxis);
 		}
 	}
 
 	/**
 	 * Set z Axis values that map from each entry of the data set as an y value
 	 * 
 	 * @param zAxis
 	 *            z-axis values container
 	 */
 	public void setZAxisValues(AxisValues zAxis) {
 		if (plotter != null) {
 			plotter.setZAxisValues(zAxis);
 		}
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
 
 	/**
 	 * Get the graph colour table
 	 * 
 	 * @return the graph colour table
 	 */
 
 	public Plot1DGraphTable getColourTable() {
 		return graphColourTable;
 	}
 
 	/**
 	 * Register an UI to the plotter
 	 * 
 	 * @param ui
 	 *            new UI controls
 	 */
 
 	public void registerUI(IPlotUI ui) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).addPlotActionEventListener(ui);
 		} else if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).addPlotActionEventListener(this);
 		} else if (currentMode == PlottingMode.BARCHART) {
 			((HistogramChartPlot1D) plotter).addAreaSelectEventListener(ui);
 		}
 	}
 
 	/**
 	 * Unregister an UI to the plotter
 	 * 
 	 * @param ui
 	 *            the UI controls that should be unregistered
 	 */
 
 	public void unregisterUI(IPlotUI ui) {
 		if (ui!= null) 
 			ui.disposeOverlays();
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			((DataSet3DPlot1D) plotter).removePlotActionEventListener(ui);
 		} else if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).removePlotActionEventListener(this);
 		} else if (currentMode == PlottingMode.BARCHART) {
 			((HistogramChartPlot1D) plotter).removeAreaSelectEventListener(ui);
 		}
 	}
 
 	/**
 	 * Register a new overlay consumer to the Plotter
 	 * 
 	 * @param consumer
 	 */
 	public void registerOverlay(OverlayConsumer consumer) {
 		switch (currentMode) {
 		case ONED:
 			((DataSet3DPlot1D) plotter).registerOverlay((Overlay1DConsumer) consumer);
 			break;
 		case TWOD:
 			((DataSet3DPlot2D) plotter).registerOverlay((Overlay2DConsumer) consumer);
 			break;
 		case ONED_THREED:
 			break;
 		case SCATTER2D:
 			break;
 		case SURF2D:
 			break;
 		case SCATTER3D:
 			break;
 		case BARCHART:
 			((DataSet3DPlot1D) plotter).registerOverlay((Overlay1DConsumer) consumer);
 			break;
 		case MULTI2D:
 			break;
 		case EMPTY:
 			break;
 		}
 	}
 
 	/**
 	 * Unregister a previous overlay consumer to the Plotter
 	 * 
 	 * @param consumer
 	 */
 	public void unRegisterOverlay(OverlayConsumer consumer) {
 		switch (currentMode) {
 		case ONED:
 			((DataSet3DPlot1D) plotter).unRegisterOverlay((Overlay1DConsumer) consumer);
 			break;
 		case TWOD:
 			((DataSet3DPlot2D) plotter).unRegisterOverlay((Overlay2DConsumer) consumer);
 			break;
 		case ONED_THREED:
 			break;
 		case SCATTER2D:
 			break;
 		case SURF2D:
 			break;
 		case SCATTER3D:
 			break;
 		case BARCHART:
 			break;
 		case MULTI2D:
 			break;
 		case EMPTY:
 			break;
 		}
 	}
 
 	/**
 	 * Required to tell the user of the API if the graph is zooming, then they can decided not to use
 	 */
 	private boolean isZoomEnabled = false;
 
 	/**
 	 * Set the zoom for the plot as enabled or not
 	 * 
 	 * @param enable
 	 *            true for enable otherwise false
 	 */
 	public void setZoomEnabled(boolean enable) {
 		isZoomEnabled = enable;
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.BARCHART || currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).enableZoomTool(enable);
 		}
 	}
 
 	public boolean isZoomEnabled() {
 		return isZoomEnabled;
 	}
 
 	/**
 	 * Set the zoom mode for the zoom, region zoom = false, area zoom = true
 	 * 
 	 * @param areaMode
 	 *            is it area mode zoom (true) otherwise region zoom (false)
 	 */
 	public void setZoomMode(boolean areaMode) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.BARCHART
 				|| currentMode == PlottingMode.ONED_THREED || currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).setZoomMode(areaMode);
 		}
 	}
 
 	/**
 	 * Set the hover over action on the plot as enabled or not
 	 * 
 	 * @param enable
 	 *            true for enable otherwise false
 	 */
 	public void setPlotActionEnabled(boolean enable) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).enablePlotActionTool(enable);
 		} else if (currentMode == PlottingMode.TWOD)
 			((DataSet3DPlot2D) plotter).enablePlotActionTool(enable);
 	}
 
 	/**
 	 * Set the right click action on the plot as enabled or not
 	 * 
 	 * @param enable
 	 *            true for enable otherwise false
 	 */
 	public void setPlotRightClickActionEnabled(boolean enable) {
 		if (currentMode == PlottingMode.ONED) {
 			((DataSet3DPlot1D) plotter).enableRightClickActionTool(enable);
 		}
 	}
 
 	/**
 	 * Set the x axis tick label format
 	 * 
 	 * @param newFormat
 	 */
 
 	public void setXTickLabelFormat(TickFormatting newFormat) {
 		plotter.setXAxisLabelMode(newFormat);
 	}
 
 	/**
 	 * Set the y axis tick label format
 	 * 
 	 * @param newFormat
 	 */
 
 	public void setYTickLabelFormat(TickFormatting newFormat) {
 		plotter.setYAxisLabelMode(newFormat);
 	}
 
 	/**
 	 * Set the z axis tick label format
 	 * 
 	 * @param newFormat
 	 */
 
 	public void setZTickLabelFormat(TickFormatting newFormat) {
 		plotter.setZAxisLabelMode(newFormat);
 	}
 
 	/**
 	 * Set the title for the graph
 	 * 
 	 * @param titleStr
 	 *            new Title String
 	 */
 	public void setTitle(final String titleStr) {
 		if(plotter != null) {
 			plotter.setTitle(titleStr);
 			if (infoBox != null) {
 				infoBox.getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						infoBox.setName(titleStr);
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Set the grid lines on/off for the individual coordinates
 	 * 
 	 * @param xcoord
 	 *            should grid lines for the x axis be shown (true/false)
 	 * @param ycoord
 	 *            should grid lines for the y axis be shown (true/false)
 	 * @param zcoord
 	 *            should grid lines for the z axis be shown (true/false)
 	 */
 
 	public void setTickGridLines(boolean xcoord, boolean ycoord, boolean zcoord) {
 		xGridActive = xcoord;
 		yGridActive = ycoord;
 		zGridActive = zcoord;
 		plotter.setTickGridLinesActive(xcoord, ycoord, zcoord);
 	}
 
 	/**
 	 * Determine if the X grid lines should be drawn
 	 * 
 	 * @return If the X grid lines are drawn or not
 	 */
 
 	public boolean getXGridActive() {
 		return xGridActive;
 	}
 
 	/**
 	 * Determine if the Y grid lines should be drawn
 	 * 
 	 * @return If the Y grid lines are drawn or not
 	 */
 
 	public boolean getYGridActive() {
 		return yGridActive;
 	}
 
 	/**
 	 * Determine if the Z grid lines should be drawn
 	 * 
 	 * @return If the Z grid lines are drawn or not
 	 */
 
 	public boolean getZGridActive() {
 		return zGridActive;
 	}
 
 	/**
 	 * Should a data window be used if the data is too large only valid in PLOT2D_3D and VOLUME mode
 	 * 
 	 * @param useWindow
 	 *            should a window be used (true) otherwise it will subsample the data (false)
 	 */
 	public void useWindow(boolean useWindow) {
 		this.useWindow = useWindow;
 	}
 
 	@Override
 	public IDataset getCurrentDataSet() {
 		return currentDataSet;
 	}
 
 	/**
 	 * @return a List of current datasets
 	 */
 	@Override
 	public List<IDataset> getCurrentDataSets() {
 		if (currentDataSets == null)
 			return null;
 		// NOTE We do not give them the actual collection, they might break it!
 		return new ArrayList<IDataset>(currentDataSets);
 	}
 
 	/**
 	 * Enable/disable the bounding box in 3D visualisation
 	 * 
 	 * @param enabled
 	 *            should the bounding box be drawn (true) otherwise (false)
 	 */
 	public void enableBoundingBox(boolean enabled) {
 		if (currentMode == PlottingMode.SURF2D || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.SCATTER3D) {
 			if (enabled) {
 				bbox = plotter.buildBoundingBox();
 				root.addChild(bbox);
 			} else {
 				root.removeChild(bbox);
 			}
 		}
 	}
 
 	/**
 	 * Set the plotting style in 2D surface plotting
 	 * 
 	 * @param newStyle
 	 *            the new selected style
 	 */
 	public void setPlot2DSurfStyle(SurfPlotStyles newStyle) {
 		if (currentMode == PlottingMode.SURF2D) {
 			((DataSet3DPlot3D) plotter).setStyle(newStyle);
 		}
 	}
 
 	/**
 	 * Resets the view to the initial stage, this should undo all rotation, panning, zooming
 	 */
 	public void resetView() {
 		MatrixBuilder.euclidean().translate(0.0f, 0.0f, 0.0f).assignTo(toolNode);
 		MatrixBuilder.euclidean().translate(0.0f, 0.0f, 0.0f).assignTo(root);
 		if (currentMode == PlottingMode.ONED_THREED || currentMode == PlottingMode.SURF2D) {
 			Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
 
 			if (sceneCamera.isPerspective()) {
 				sceneCamera.setFieldOfView(56.5);
 			} else {
 				sceneCamera.setFieldOfView(140.0);
 			}
 		}
 		if (vBar != null) {
 			vBar.setVisible(false);
 			vBar.setMaximum(0);
 			vBar.setMinimum(0);
 			vBar.setIncrement(0);
 		}
 		if (hBar != null) {
 			hBar.setVisible(false);
 			hBar.setMaximum(0);
 			hBar.setMinimum(0);
 			hBar.setIncrement(0);
 		}
 		plotArea.redraw();
 		plotter.resetView();
 	}
 
 	/**
 	 * Save the graph with the given filename. If the file name ends with a known extension, this is used as the file
 	 * type otherwise it is the string passed in which is read from the save as dialog form normally.
 	 * 
 	 * @param filename
 	 *            the name under which the graph should be saved
 	 * @param fileType
 	 *            type of the file
 	 */
 
 	public synchronized void saveGraph(String filename, String fileType) {
 		isInExporting = true;
 		
 		// Can't reach file permissions error message in JReality. Checking explicitly here. 
 		File p = new File(filename).getParentFile();
 		if (!p.canWrite()) {
 			String msg = "Saving failed: no permission to write in directory: " + p.getAbsolutePath();
 			logger.error(msg);
 		 	Status status = new Status(IStatus.ERROR, AnalysisRCPActivator.PLUGIN_ID, msg); 
 		 	ErrorDialog.openError(getComposite().getShell(), "Image export error", "Error saving image file", status);
 			isInExporting = false;
 			return;
 		}
 		
 		try {
 			PlotExportUtil.saveGraph(filename, fileType, viewerApp);
 		} catch (Exception e) {
 			logger.error(e.getCause().getMessage(), e);
 		 	Status status = new Status(IStatus.ERROR, AnalysisRCPActivator.PLUGIN_ID, e.getMessage(), e); 
 		 	ErrorDialog.openError(getComposite().getShell(), "Image export error", "Error saving image file", status);
 		} finally {
 			isInExporting = false;
 		}
 	}
 
 	/**
 	 * Print the graph to a printer. This will temporary create an image
 	 * 
 	 * @param printerData
 	 *            SWT specific printer object
 	 */
 
 	public synchronized void printGraph(PrinterData printerData, float scaling) {
 		if (printerData != null) {
 			isInExporting = true;
 			if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 					|| currentMode == PlottingMode.SCATTER2D)
 				PlotExportUtil.printGraph(printerData, viewerApp, container.getDisplay(), graphColourTable, scaling);
 			else
 				PlotExportUtil.printGraph(printerData, viewerApp, container.getDisplay(), null, scaling);
 			isInExporting = false;
 		}
 	}
 	
 	public void printGraph() {
 		isInExporting = true;
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.SCATTER2D) {
 			if(defaultPrinterData==null)
 				defaultPrinterData=Printer.getDefaultPrinterData();
 			PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(viewerApp, container.getDisplay(),graphColourTable, 
 					defaultPrinterData, printOrientation, printScale, printResolution);
 			defaultPrinterData=dialog.open();
 			printOrientation = dialog.getOrientation();
 			printScale = dialog.getScale();
 			printResolution = dialog.getResolution();
 		} else{
 			if(defaultPrinterData==null)
 				defaultPrinterData=Printer.getDefaultPrinterData();
 			PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(viewerApp, container.getDisplay(),null,
 					defaultPrinterData, printOrientation, printScale, printResolution);
 			defaultPrinterData=dialog.open();
 			printOrientation = dialog.getOrientation();
 			printScale = dialog.getScale();
 			printResolution = dialog.getResolution();
 		}
 		isInExporting = false;
 
 	}
 	
 	/**
 	 * Copy the graph to the Clipboard.
 	 * 
 	 */
 	public synchronized void copyGraph() {
 		try {
 			PlotExportUtil.copyGraph(viewerApp);
 		} catch (Exception e) {
 			logger.error(e.getCause().getMessage(), e);
 			Status status = new Status(IStatus.ERROR, AnalysisRCPActivator.PLUGIN_ID, e.getMessage(), e); 
 			ErrorDialog.openError(getComposite().getShell(), "Image copy error", "Error copying image to clipboard", status);
 		}
 	}
 
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		if (theObserved.equals(legendTable)) {
 			if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 					|| currentMode == PlottingMode.SCATTER2D) {
 				((DataSet3DPlot1D) plotter).updateAllGraphAppearances();
 				viewerApp.getCurrentViewer().render();
 			}
 		}
 	}
 
 	/**
 	 * Switch between perspective and orthographic camera
 	 * 
 	 * @param persp
 	 *            should this be a perspective camera (true) otherwise false
 	 */
 	public void setPerspectiveCamera(boolean persp, boolean needToRender) {
 		Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
 		if (sceneCamera.isPerspective())
 			perspFOV = sceneCamera.getFieldOfView();
 		else
 			orthoFOV = sceneCamera.getFieldOfView();
 
 		sceneCamera.setPerspective(persp);
 		if (persp)
 			sceneCamera.setFieldOfView(perspFOV);
 		else
 			sceneCamera.setFieldOfView(orthoFOV);
 		if (needToRender) viewerApp.getCurrentViewer().render();
 	}
 
 	/**
 	 * Clear the zoom history
 	 */
 	public void clearZoomHistory() {
 		if (currentMode == PlottingMode.BARCHART && plotter != null)
 			((HistogramChartPlot1D) plotter).clearZoom();
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		if (plotter != null) {
 			Rectangle bounds = plotArea.getBounds();
 			plotter.notifyComponentResize(bounds.width, bounds.height);
 		}
 	}
 
 	@Override
 	public void paintControl(PaintEvent e) {
 		viewerApp.getCurrentViewer().render();
 	}
 
 	/**
 	 * Set the data window position in 2D surface plotting if it is in window mode
 	 * 
 	 * @param roi
 	 *            the SurfacePlot region of interest object that contains all relevant information
 	 */
 
 	public void setDataWindowPosition(SurfacePlotROI roi) {
 		if (currentMode == PlottingMode.SURF2D) {
 			((DataSet3DPlot3D) plotter).setDataWindow(roi);
 		}
 	}
 
 	@Override
 	public void legendDeleted(LegendChangeEvent evt) {
 		if (currentMode == PlottingMode.ONED) {
 			int index = evt.getEntryNr();
 			if (index < currentDataSets.size()) {
 				((DataSet3DPlot1D) plotter).removeGraphNode(index);
 				currentDataSets.remove(index);
 				graphColourTable.deleteLegendEntry(index);
 				historyCounter--;
 				if (useLegend)
 					legendTable.updateTable(graphColourTable);
 				refresh(false);
 			}
 		}
 	}
 
 	public void setPlotUpdateOperation(boolean isUpdate) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED) {
 			((DataSet3DPlot1D) plotter).setUpdateOperation(isUpdate);
 		}
 	}
 
 	public void setYAxisScaling(ScaleType newScaling) {
 		plotter.setScaling(newScaling);
 	}
 
 	public void setZAxisScaling(ScaleType newScaling) {
 		if (!hasJOGL && currentMode == PlottingMode.TWOD && newScaling != plotter.getScaling()) {
 			plotter.setScaling(newScaling);
 			applyColourCast(cacheRedFunc, cacheGreenFunc, cacheBlueFunc, cacheAlphaFunc, cacheInverseRed,
 					cacheInverseGreen, cacheInverseBlue, cacheInverseAlpha, cacheMinValue, cacheMaxValue);
 		} else
 			plotter.setScaling(newScaling);
 	}
 
 	/**
 	 * For stack plots only get the current z Axis length factor
 	 * 
 	 * @return the z Axis length factor which is the factor between the current z Axis length and the maximum length
 	 */
 
 	public double stackPlotGetZAxisLengthFactor() {
 		// if (currentMode == PlottingMode.ONED_THREED) {
 		// return ((DataSet3DPlot1DStack)plotter).getZAxisLengthFactor();
 		// }
 		return 0.0;
 	}
 
 	public void stackPlotSetZAxisLengthFactor(@SuppressWarnings("unused") double newFactor) {
 		// if (currentMode == PlottingMode.ONED_THREED) {
 		// ((DataSet3DPlot1DStack)plotter).setZAxisLengthFactor(newFactor);
 		// }
 	}
 
 	/**
 	 * For image plots only tell the plotter to use the canvas aspect ratio instead of the data aspect ratio
 	 * 
 	 * @param useCanvasAspectRatio
 	 *            true if canvas aspect ratio should be used , false if data aspect ratio should be used
 	 */
 
 	public void imagePlotSetCanvasAspectRatio(boolean useCanvasAspectRatio) {
 		if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).setCanvasAspectRation(useCanvasAspectRatio);
 		}
 	}
 
 	@Override
 	public void plotActionPerformed(final PlotActionEvent event) {
 		if (currentMode == PlottingMode.TWOD) {
 			if (infoBox != null) {
 				container.getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						int xPos = event.getDataPosition()[0];
 						int yPos = event.getDataPosition()[1];
 						xPos = Math.max(xPos, 0);
 						xPos = Math.min(xPos, currentDataSet.getShape()[1] - 1);
 						yPos = Math.max(yPos, 0);
 						yPos = Math.min(yPos, currentDataSet.getShape()[0] - 1);
 						infoBox.setPositionInfo(event.getPosition()[0], event.getPosition()[1], currentDataSet
 								.getDouble(yPos, xPos));
 						if (qSpace != null) {
 							infoBox.setQSpaceInfo(xPos, yPos, qSpace);
 						}
 					}
 				});
 			}
 		} else {
 			logger.warn("This shouldn't be happening there should be no notifications");
 		}
 	}
 
 	/**
 	 * Restore the plotArea to use the default cursor
 	 */
 
 	public void restoreDefaultPlotAreaCursor() {
 		plotArea.getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				plotArea.setCursor(defaultCursor);
 			}
 		});
 
 	}
 
 	/**
 	 * Set a specific SWT System cursor
 	 */
 
 	public void setPlotAreaCursor(final int cursor) {
 		plotArea.getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				Cursor tempCursor = plotArea.getDisplay().getSystemCursor(cursor);
 				if (tempCursor != null)
 					plotArea.setCursor(tempCursor);
 			}
 		});
 	}
 
 	/**
 	 * Check if the underlying SWT Composite is disposed
 	 * 
 	 * @return true if the SWT Composite is disposed.
 	 */
 	@Override
 	public boolean isDisposed() {
 		return getComposite().isDisposed();
 	}
 
 	/**
 	 * Get the XAxis Values associated to the Plotter
 	 * 
 	 * @return a list of AxisValues associated to the X-Axis
 	 */
 
 	@Override
 	public List<AxisValues> getXAxisValues() {
 		if (plotter == null)
 			return null;
 		return plotter.getAxisValues();
 	}
 
 	/**
 	 * Set if transparency should be used on the 3D scatter plot
 	 * 
 	 * @param newTransp
 	 *            true (use transparency) false (do not use transparency)
 	 */
 
 	public void useTransparency(boolean newTransp) {
 		if (currentMode == PlottingMode.SCATTER3D) {
 			((DataSetScatterPlot3D) plotter).setTransparency(newTransp);
 		}
 	}
 
 	/**
 	 * Set if transparency should be used on the 3D scatter plot
 	 * 
 	 * @param newDraw
 	 *            true (use draw outlines only) false (do not use draw outlines only)
 	 */
 	public void useDrawOutlinesOnly(boolean newDraw) {
 		if (currentMode == PlottingMode.SCATTER3D) {
 			((DataSetScatterPlot3D) plotter).setDrawOutlinesOnly(newDraw);
 		}
 	}
 
 	/**
 	 * Set if point sizes should be of uniform size in 3D scatter plot
 	 * 
 	 * @param uniform
 	 *            true (use uniform size) false (do not use uniform size)
 	 */
 	public void useUniformSize(boolean uniform) {
 		if (currentMode == PlottingMode.SCATTER3D) {
 			((DataSetScatterPlot3D) plotter).setUniformSize(uniform);
 		}
 	}
 	
 	/**
 	 * Enable/Disable Diffraction mode, this will display error pixels and overloaded pixels that seems to be specific
 	 * to MX requirements
 	 * 
 	 * @param checked
 	 *            true (enable Diffraction mode) false (disable Diffraction mode)
 	 */
 
 	public void setDiffractionMode(boolean checked) {
 		if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).setDiffractionImageMode(checked);
 			refresh(false);
 		}
 	}
 
 	/**
 	 * Enable/Disable specific feature detection this can help to make spots or other characteristic features in the
 	 * image stand out more
 	 * 
 	 * @param checked
 	 *            true (enable feature detection) false (disable feature detection)
 	 */
 
 	public void setGradientImageMode(boolean checked) {
 		if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).setGradientImageMode(checked);
 			refresh(false);
 		}
 	}
 
 	/**
 	 * Set the threshold value when a pixel should be displayed as overloaded only has an affect when image is in
 	 * diffraction mode
 	 */
 
 	public void setOverloadThreshold(double threshold) {
 		if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).changeThreshold(threshold);
 			refresh(false);
 		}
 	}
 
 	/**
 	 * Increase the feature detection sensitivity (decrease threshold)
 	 */
 	public void increaseFeatureSensitivty() {
 		if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).changeThreshold(0.9);
 			refresh(false);
 		}
 	}
 
 	/**
 	 * Decrease the feature detection sensitivity (increase threshold)
 	 */
 
 	public void decreaseFeatureSensitivty() {
 		if (currentMode == PlottingMode.TWOD) {
 			((DataSet3DPlot2D) plotter).changeThreshold(1.1);
 			refresh(false);
 		}
 	}
 
 	/**
 	 * Allows to set the zoom area of the plot manually programatically
 	 * 
 	 * @param startX
 	 *            start x coordinate in the data domain
 	 * @param startY
 	 *            start y coordinate in the data domain
 	 * @param endX
 	 *            end x coordinate in the data domain (endX must be >= startX)
 	 * @param endY
 	 *            end y coordinate in the data domain (endY must be >= startY)
 	 */
 
 	public void setPlotZoomArea(double startX, double startY, double endX, double endY) {
 		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 				|| currentMode == PlottingMode.SCATTER2D) {
 			((DataSet3DPlot1D) plotter).setPlotZoomArea(startX, startY, endX, endY);
 			refresh(false);
 		}
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
 
 	public Composite getPlotArea() {
 		return plotArea;
 	}
 	
 	public void enableImageScrollBars(boolean enable) {
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
 		
 
 	public void hideAllPlots() {
 		for (int i = 0; i < graphColourTable.getLegendSize(); i++)
 			graphColourTable.getLegendEntry(i).setVisible(false);
 		refresh(true);
 	}
 	
 	public void showAllPlots() {
 		for (int i = 0; i < graphColourTable.getLegendSize(); i++)
 			graphColourTable.getLegendEntry(i).setVisible(true);
 		refresh(true);
 	}
 	
 	
 	public void toggleErrorBars(boolean xcoord, boolean ycoord, boolean zcoord) {		
 		plotter.toggleErrorBars(xcoord, ycoord, zcoord);
 	}
 	
 }
