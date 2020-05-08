 package de.ptb.epics.eve.viewer.views.plotview;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.ToolbarArmedXYGraph;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.scandescription.PlotWindow;
 import de.ptb.epics.eve.data.scandescription.YAxis;
 import de.ptb.epics.eve.ecp1.types.DataModifier;
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.preferences.PreferenceConstants;
 
 /**
  * @author Marcus Michalsky
  * @since 1.13
  */
 public class XyPlot extends Figure {
 	private static final Logger LOGGER = Logger.getLogger(XyPlot.class
 			.getName());
 	
 	private XYGraph xyGraph;
 	private ToolbarArmedXYGraph toolbarArmedXYGraph;
 	
 	private PlotWindow currentPlotWindow;
 	
 	private List<TraceDataCollector> collectors;
 	
 	/**
 	 * 
 	 */
 	public XyPlot() {
 		LOGGER.debug("constructor");
 		this.xyGraph = null;
 		this.toolbarArmedXYGraph = null;
 		this.currentPlotWindow = null;
 		this.collectors = new ArrayList<TraceDataCollector>();
 		this.init();
 	}
 	
 	/**
 	 * 
 	 * @param plotWindow
 	 */
 	public void setPlotWindow(PlotWindow plotWindow) {
 		if (mustInit(plotWindow)) {
 			this.clear();
 			this.init();
 			
 			this.currentPlotWindow = plotWindow;
 			this.xyGraph.setTitle(plotWindow.getName());
 			this.initXAxis(plotWindow);
 			this.initYAxes(plotWindow);
 		} else {
 			for (TraceDataCollector coll : this.collectors) {
 				Activator.getDefault().getEcp1Client()
 						.addMeasurementDataListener(coll);
 			}
 			LOGGER.debug("PlotWindow " + plotWindow.getId() 
 					+ ": no init -> reenable listeners");
 		}
 		LOGGER.debug("PlotWindow " + plotWindow.getId() + " set");
 	}
 	
 	/*
 	 * 
 	 */
 	private void initXAxis(PlotWindow plotWindow) {
 		this.xyGraph.primaryXAxis.setTitle(plotWindow.getXAxis().getName());
 		boolean timedataX = (Activator.getDefault().getMeasuringStation()
 				.getMotorAxisById(plotWindow.getXAxis().getID()).getType() 
 				== DataTypes.DATETIME);
 		if (timedataX) {
 			this.xyGraph.primaryXAxis.setTimeUnit(Calendar.MILLISECOND);
 		}
 		this.xyGraph.primaryXAxis.setDateEnabled(timedataX);
 		switch (this.currentPlotWindow.getMode()) {
 		case LINEAR:
 			this.xyGraph.primaryXAxis.setLogScale(false);
 			break;
 		case LOG:
 			this.xyGraph.primaryXAxis.setLogScale(true);
 			break;
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void initYAxes(PlotWindow plotWindow) {
 		for (int i = 0; i < plotWindow.getYAxes().size(); i++) {
 			YAxis yAxis = plotWindow.getYAxes().get(i);
 			Axis axis = null;
 			
 			boolean normalized = yAxis.isNormalized(); 
 			
 			TraceInfo traceInfo = new TraceInfo();
 			traceInfo.setPlotId(plotWindow.getId());
 			traceInfo.setPlotName(plotWindow.getName());
 			traceInfo.setMotorId(plotWindow.getXAxis().getID());
 			traceInfo.setDetectorId(yAxis.getDetectorChannel().getID());
 			traceInfo.setNormalizeId(
 					(normalized 
 							? yAxis.getDetectorChannel().getID()
 							+ "__"
 							+ yAxis.getNormalizeChannel().getID() 
 							: ""));
 			traceInfo.setModifier((normalized ? DataModifier.NORMALIZED
 					: DataModifier.UNMODIFIED));
 			
 			if (i == 0) {
 				axis = this.xyGraph.primaryYAxis;
 				if (!normalized) {
 					axis.setTitle(yAxis.getDetectorChannel().getName());
 				} else {
 					axis.setTitle(yAxis.getDetectorChannel().getName() + " / " + 
 							yAxis.getNormalizeChannel().getName());
 				}
 			} else {
 				if (!normalized) {
 					axis = new Axis(yAxis.getDetectorChannel().getName(), true);
 				} else {
 					axis = new Axis(yAxis.getDetectorChannel().getName() + " / " + 
 							yAxis.getNormalizeChannel().getName(), true);
 				}
 				axis.setTickLableSide(LabelSide.Secondary);
 				this.xyGraph.addAxis(axis);
 			}
 			switch (yAxis.getMode()) {
 			case LINEAR:
 				axis.setLogScale(false);
 				break;
 			case LOG:
 				axis.setLogScale(true);
 				break;
 			}
 			if (yAxis.getDetectorChannel().getRead().getType()
 					.equals(DataTypes.DATETIME)) {
 				axis.setDateEnabled(true);
 			}
 			axis.setAutoScale(true);
 			if (!normalized) {
 				this.addTrace(yAxis.getDetectorChannel().getName(), axis,
 						yAxis, traceInfo);
 			} else {
 				this.addTrace(yAxis.getDetectorChannel().getName() + " / "
 						+ yAxis.getNormalizeChannel().getName(), axis, yAxis,
 						traceInfo);
 			}
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void addTrace(String title, Axis yAxis, YAxis model,
 			TraceInfo traceInfo) {
 		TraceDataCollector collector = new TraceDataCollector(Activator
 				.getDefault().getPreferenceStore()
 				.getInt(PreferenceConstants.P_PLOT_BUFFER_SIZE), traceInfo);
 
 		Activator.getDefault().getEcp1Client()
 				.addMeasurementDataListener(collector);
 		Trace trace = new Trace(title, this.xyGraph.primaryXAxis, yAxis,
 				collector);
 		this.collectors.add(collector);
 		trace.setAntiAliasing(true);
 		trace.setTraceColor(new Color(Display.getDefault(), model.getColor()));
 		trace.setPointStyle(model.getMarkstyle());
 		trace.setTraceType(model.getLinestyle());
 		this.xyGraph.addTrace(trace);
 	}
 	
 	/*
 	 * checks whether initialization is necessary
 	 * 
 	 * initialization is necessary if:
 	 *  - no plot was set before (new plot)
 	 *  - x axes are different
 	 *  - y axes are different
 	 *  - normalize channels are different
 	 */
 	private boolean mustInit(PlotWindow plotWindow) {
 		if (this.currentPlotWindow == null) {
 			return true;
 		}
 		if (plotWindow.isInit()) {
 			LOGGER.debug("isInit declared -> clear");
 			return true;
 		}
 		if (!this.currentPlotWindow
 				.getScanModule()
 				.getChain()
 				.getScanDescription()
 				.equals(plotWindow.getScanModule().getChain()
 						.getScanDescription())) {
 			return true;
 		}
 		if (!this.currentPlotWindow.getXAxis().equals(plotWindow.getXAxis())) {
 			return true;
 		}
 		// are there any "conflicting" axes ?
 		// is there a "new" axis ? (Ticket # 749 case 4)
 		for (YAxis yAxis : plotWindow.getYAxes()) {
 			if (!this.currentPlotWindow.getYAxes().contains(yAxis)) {
 				return true;
 			}
 		}
 		// is any axis gone ? (Ticket # 749 case 1, 2)
 		for (YAxis yAxis : this.currentPlotWindow.getYAxes()) {
 			if (!plotWindow.getYAxes().contains(yAxis)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/*
 	 * 
 	 */
 	private void init() {
 		LOGGER.debug("init");
 		this.xyGraph = new XYGraph();
 		this.xyGraph.setTitle("XY - Plot");
 		this.xyGraph.primaryXAxis.setTitle("x - axis");
 		this.xyGraph.primaryYAxis.setTitle("y - axis");
 		this.xyGraph.primaryXAxis.setRange(new Range(0, 200));
 		this.xyGraph.primaryXAxis.setAutoScale(true);
 		this.xyGraph.primaryYAxis.setAutoScale(true);
 		this.xyGraph.primaryXAxis.setShowMajorGrid(true);
 		this.xyGraph.primaryYAxis.setShowMajorGrid(true);
 		this.xyGraph.primaryXAxis.setAutoScaleThreshold(0);
 
		if (toolbarArmedXYGraph != null) {
				remove(toolbarArmedXYGraph);
		}
 		toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
 		add(toolbarArmedXYGraph);
 	}
 	
 	/**
 	 * 
 	 */
 	public void finish() {
 		for (TraceDataCollector coll : this.collectors) {
 			Activator.getDefault().getEcp1Client()
 					.removeMeasurementDataListener(coll);
 			coll.publish();
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public void clear() {
 		for (TraceDataCollector coll : this.collectors) {
 			Activator.getDefault().getEcp1Client()
 					.removeMeasurementDataListener(coll);
 		}
 		if (this.xyGraph != null) {
 			this.xyGraph.erase();
 		}
 		this.collectors.clear();
 		this.currentPlotWindow = null;
 	}
 	
 	/**
 	 * Returns the graph of the XY Figure.
 	 * <p>
 	 * @return the graph of the XY Figure
 	 * @since 1.16
 	 */
 	public XYGraph getGraph() {
 		return this.xyGraph;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void layout() {
 		toolbarArmedXYGraph.setBounds(bounds.getCopy().shrink(5, 5));
 		super.layout();
 	}
 }
