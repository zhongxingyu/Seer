 package nl.sense_os.commonsense.client.viz.panels.timeline;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
 import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
 import nl.sense_os.commonsense.client.viz.panels.VizPanel;
 
 import com.chap.links.client.Graph;
 import com.chap.links.client.Timeline;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.layout.FillData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.events.RangeChangeHandler;
 
 public class TimeLinePanel extends VizPanel {
 
     private static final Logger LOG = Logger.getLogger(TimeLinePanel.class.getName());
 
     private Graph graph;
     private final Graph.Options graphOpts = Graph.Options.create();
 
     private Timeline timeline;
     private final Timeline.Options tlineOpts = Timeline.Options.create();
     private boolean showTimeLine = true;
 
     public TimeLinePanel(List<SensorModel> sensors, long start, long end, boolean subsample,
             String title) {
         super();
 
         LOG.setLevel(Level.WARNING);
 
         // set up layout
         setHeading("Time line: " + title);
         setBodyBorder(false);
         setLayout(new FillLayout());
 
         // Graph options
         graphOpts.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
         graphOpts.setLineRadius(2);
         graphOpts.setWidth("100%");
         graphOpts.setHeight("100%");
         graphOpts.setLegendCheckboxes(true);
         graphOpts.setLegendWidth(125);
 
         // time line options
         tlineOpts.setWidth("100%");
         tlineOpts.setHeight("100%");
         tlineOpts.setAnimate(false);
         tlineOpts.setSelectable(false);
         tlineOpts.setEditable(false);
         tlineOpts.setStackEvents(false);
         tlineOpts.setGroupsOnRight(true);
         tlineOpts.setGroupsWidth(135);
 
         visualize(sensors, start, end, subsample);
     }
 
     /**
      * @param stringData
      *            JsArray with time series data to put in the table.
      * @return A DataTable for Timeline visualization. Each time series will be shows in its own
      *         group.
      */
     private DataTable createDataTable(JsArray<Timeseries> stringData) {
         LOG.fine("Create data table...");
 
         DataTable dataTable = DataTable.create();
         dataTable.addColumn(DataTable.ColumnType.DATETIME, "startdate");
         dataTable.addColumn(DataTable.ColumnType.DATETIME, "enddate");
         dataTable.addColumn(DataTable.ColumnType.STRING, "content");
         dataTable.addColumn(DataTable.ColumnType.STRING, "group");
 
         // put the time series values to the data table
         Timeseries ts;
         JsArray<DataPoint> values;
         DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
         for (int i = 0; i < stringData.length(); i++) {
             ts = stringData.get(i);
             values = ts.getData();
             for (int j = 0, index = dataTable.getNumberOfRows(); j < values.length(); j++) {
                 lastPoint = dataPoint;
                 if (j == 0) {
                     dataPoint = values.get(j);
                 } else {
                     dataPoint = nextPoint;
                 }
                 if (j < values.length() - 1) {
                     nextPoint = values.get(j + 1);
                 } else {
                     nextPoint = null;
                 }
                 if (j > 0) {
                     if (false == (lastPoint != null && lastPoint.getRawValue().equals(
                             dataPoint.getRawValue()))) {
                         // value changed! new row...
                         dataTable.addRow();
                         index++;
                         dataTable.setValue(index, 0, dataPoint.getTimestamp());
                         dataTable.setValue(index, 2, dataPoint.getRawValue());
                         dataTable.setValue(index, 3, ts.getLabel());
                     } else {
                         // only the end time has to be changed
                     }
                 } else {
                     // insert first data point
                     dataTable.addRow();
                     dataTable.setValue(index, 0, dataPoint.getTimestamp());
                     dataTable.setValue(index, 2, dataPoint.getRawValue());
                     dataTable.setValue(index, 3, ts.getLabel());
                 }
 
                 // set end time
                 if (nextPoint != null) {
                     long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                             .getTimestamp().getTime() - 1000);
                     dataTable.setValue(index, 1, new Date(endDate));
                 } else {
                     dataTable.setValue(index, 1, new Date());
                 }
             }
         }
 
         return dataTable;
     }
 
     /**
      * Creates a new graph visualization using the supplied data, and adds it to the layout.
      * 
      * @param numberData
      *            JsArray with timeseries to show in the graph.
      */
     private void createGraph(JsArray<Timeseries> numberData) {
         LOG.fine("Create graph...");
 
         graph = new Graph(numberData, graphOpts);
 
         graph.addRangeChangeHandler(new RangeChangeHandler() {
 
             @Override
             public void onRangeChange(RangeChangeEvent event) {
                 if (null != timeline) {
                     timeline.setVisibleChartRange(event.getStart(), event.getEnd());
                     // timeline.redraw(); // not required
                 }
             }
         });
 
         // this LayoutContainer ensures that the graph is sized and resized correctly
         LayoutContainer graphWrapper = new LayoutContainer() {
 
             @Override
             protected void onResize(int width, int height) {
                 redrawGraph();
                 super.onResize(width, height);
             }
         };
         graphWrapper.add(graph);
 
         add(graphWrapper, new FillData(0));
         layout();
     }
 
     /**
      * Creates a new time line visualization using the supplied data table, and adds it to the
      * layout.
      * 
      * @param table
      *            DataTable with the data to show in the time line.
      */
     private void createTimeline(DataTable table) {
         LOG.fine("Create time line...");
 
         timeline = new Timeline(table, tlineOpts);
 
         timeline.addRangeChangeHandler(new RangeChangeHandler() {
 
             @Override
             public void onRangeChange(RangeChangeEvent event) {
                 if (null != graph) {
                     graph.setVisibleChartRange(event.getStart(), event.getEnd());
                     graph.redraw();
                 }
             }
         });
 
         // this LayoutContainer ensures that the graph is sized and resized correctly
         LayoutContainer wrapper = new LayoutContainer(new FitLayout()) {
 
             @Override
             protected void onAfterLayout() {
                 redrawTimeline();
                 super.onAfterLayout();
             }
 
             @Override
             protected void onResize(int width, int height) {
                 super.onResize(width, height);
                 this.layout(true);
             }
         };
         wrapper.add(timeline, new FitData());
 
         insert(wrapper, 0, new FillData(new Margins(5, 10, 5, 70)));
         layout();
     }
 
     @Override
     protected void onNewData(final JsArray<Timeseries> data) {
 
         LOG.fine("New data...");
         LOG.fine("Total " + data.length() + " timeseries");
 
         if (null == timeline && null == graph && data.length() == 0) {
             onNoData();
             return;
         }
 
         JsArray<Timeseries> numberData = JavaScriptObject.createArray().cast();
         JsArray<Timeseries> stringData = JavaScriptObject.createArray().cast();
         for (int i = 0; i < data.length(); i++) {
             Timeseries ts = data.get(i);
             if (ts.getType().equalsIgnoreCase("number")) {
                 LOG.finest(ts.getLabel() + ": " + ts.getData().length()
                         + " data points (number data)");
                 numberData.push(ts);
             } else {
                 LOG.finest(ts.getLabel() + ": " + ts.getData().length() + " data points ("
                         + ts.getType() + " data)");
                 stringData.push(ts);
             }
         }
 
         // show the string data in a time line
         if (showTimeLine && stringData.length() > 0) {
             showStringData(stringData);
         }
 
         // show the numerical data in a line graph
         if (numberData.length() > 0) {
             showNumberData(numberData);
         }
 
         // make sure both show the same time range
         if (graph != null && timeline != null) {
             Graph.DateRange graphRange = graph.getVisibleChartRange();
             Timeline.DateRange tlineRange = timeline.getVisibleChartRange();
             Date rangeStart = graphRange.getStart().before(tlineRange.getStart()) ? graphRange
                     .getStart() : tlineRange.getStart();
             Date rangeEnd = graphRange.getEnd().after(tlineRange.getEnd())
                     ? graphRange.getEnd()
                     : tlineRange.getEnd();
             graph.setVisibleChartRange(rangeStart, rangeEnd);
             graph.redraw();
             timeline.setVisibleChartRange(rangeStart, rangeEnd);
             timeline.redraw();
         } else if (graph != null) {
             graph.redraw();
         } else if (timeline != null) {
             timeline.redraw();
         }
     }
 
     /**
      * Shows a dialog to inform the user that there was no data to show.
      */
     private void onNoData() {
         LOG.fine("No data to visualize!");
 
         String msg = "No data to visualize! "
                 + "Please make sure that you selected a time range that contains sensor readings.";
         MessageBox.info(null, msg, new Listener<MessageBoxEvent>() {
 
             @Override
             public void handleEvent(MessageBoxEvent be) {
                 if (null == graph && null == timeline) {
                     TimeLinePanel.this.hide();
                 }
             }
         });
     }
 
     /**
      * Redraws the graph, if this is possible (i.e. if it is drawn already).
      */
     private void redrawGraph() {
         if (null != graph && graph.isAttached()) {
             LOG.finest("Redraw graph...");
             graph.redraw();
         }
     }
 
     /**
      * Redraws the time line, if this is possible (i.e. if it is drawn already).
      */
     private void redrawTimeline() {
         if (null != timeline && timeline.isAttached()) {
             LOG.finest("Redraw time line...");
             timeline.redraw();
         }
     }
 
     private void showNumberData(JsArray<Timeseries> data) {
         LOG.fine("Show number data...");
 
         if (null == graph) {
             createGraph(data);
         } else {
             LOG.fine("Draw in existing graph");
             graph.draw(data, graphOpts);
         }
     }
 
     private void showStringData(JsArray<Timeseries> data) {
         LOG.fine("Show string data...");
 
         // create a new data table
         DataTable dataTable = createDataTable(data);
 
         if (dataTable.getNumberOfRows() > 0) {
             if (null == timeline) {
                 createTimeline(dataTable);
             } else {
                 LOG.fine("Draw on existing time line");
                timeline.setData(dataTable);
             }
         } else {
             LOG.warning("No data for time line visualization!");
         }
     }
 }
