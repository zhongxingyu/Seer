 package nl.sense_os.commonsense.client.viz.panels.timeline;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.common.json.overlays.DataPoint;
 import nl.sense_os.commonsense.client.common.json.overlays.Timeseries;
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.common.models.UserModel;
 import nl.sense_os.commonsense.client.viz.panels.VizPanel;
 
 import com.chap.links.client.Graph;
 import com.chap.links.client.Timeline;
 import com.chap.links.client.Timeline.Options;
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.layout.FillData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.events.RangeChangeHandler;
 
 public class TimeLinePanel extends VizPanel {
 
     private static final Logger logger = Logger.getLogger("TimeLinePanel");
     protected Graph graph;
     protected final Graph.Options graphOpts;
     protected Timeline timeline;
     protected final Timeline.Options tlineOpts;
     protected final DataTable dataTable;
 
     private boolean isPimCheckComplete = false;
 
     private boolean showTimeLine = true;
     public TimeLinePanel(List<SensorModel> sensors, long start, long end, String title) {
         super();
 
         // set up layout
         setHeading("Time line: " + title);
         setBodyBorder(false);
         setLayout(new FillLayout());
 
         // Graph options
         this.graphOpts = Graph.Options.create();
         this.graphOpts.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
         this.graphOpts.setLineRadius(2);
         this.graphOpts.setWidth("100%");
         this.graphOpts.setHeight("100%");
         this.graphOpts.setLegendCheckboxes(true);
         this.graphOpts.setLegendWidth(125);
 
         // time line options
         this.tlineOpts = Options.create();
         this.tlineOpts.setWidth("100%");
         this.tlineOpts.setHeight("100%");
         this.tlineOpts.setAnimate(false);
         this.tlineOpts.setSelectable(false);
         this.tlineOpts.setEditable(false);
         this.tlineOpts.setStackEvents(false);
         this.tlineOpts.setGroupsOnRight(true);
         this.tlineOpts.setGroupsWidth(135);
 
         this.dataTable = createDataTable();
 
         visualize(sensors, start, end);
     }
 
     @Override
     public void addData(final JsArray<Timeseries> data) {
         // logger.fine( "addData...");
 
         // special pim message
         if (!isPimCheckComplete
                 && Registry.<UserModel> get(Constants.REG_USER).getId().equals("1547")) {
             MessageBox.confirm(null, "Hoi Pim! Wil je eventueel de tijdslijnvisualisatie zien?",
                     new Listener<MessageBoxEvent>() {
 
                         @Override
                         public void handleEvent(MessageBoxEvent be) {
                             if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                                 showTimeLine = true;
                             } else {
                                 showTimeLine = false;
                             }
                             isPimCheckComplete = true;
                             addData(data);
                         }
                     });
             return;
         }
 
         if (data.length() == 0) {
             onNoData();
             return;
         }
 
         JsArray<Timeseries> numberData = JsArray.createArray().cast();
         JsArray<Timeseries> stringData = JsArray.createArray().cast();
         for (int i = 0; i < data.length(); i++) {
             Timeseries ts = data.get(i);
             if (ts.getType().equalsIgnoreCase("number")) {
                // logger.fine( ts.getLabel() + " (number data)");
                 numberData.push(ts);
             } else {
                 // logger.fine( ts.getLabel() + " (" + ts.getType() + " data)");
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
     }

     /**
      * @return An empty DataTable with the correct columns for Timeline visualization.
      */
     private DataTable createDataTable() {
 
         DataTable data = DataTable.create();
         data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
         data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
         data.addColumn(DataTable.ColumnType.STRING, "content");
         data.addColumn(DataTable.ColumnType.STRING, "group");
 
         return data;
     }
 
     private void createGraph(JsArray<Timeseries> data) {
 
         this.graph = new Graph(data, this.graphOpts);
 
         this.graph.addRangeChangeHandler(new RangeChangeHandler() {
 
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
                 super.onResize(width, height);
                 redrawGraph();
             }
         };
         graphWrapper.add(this.graph);
 
         this.add(graphWrapper, new FillData(0));
         this.layout();
     }
 
     private void createTimeline() {
 
         this.timeline = new Timeline(this.dataTable, this.tlineOpts);
 
         this.timeline.addRangeChangeHandler(new RangeChangeHandler() {
 
             @Override
             public void onRangeChange(RangeChangeEvent event) {
                 if (null != graph) {
                     graph.setVisibleChartRange(event.getStart(), event.getEnd());
                     graph.redraw();
                 }
             }
         });
 
         // this LayoutContainer ensures that the graph is sized and resized correctly
         LayoutContainer wrapper = new LayoutContainer() {
             @Override
             protected void onAfterLayout() {
                 super.onAfterLayout();
                 redrawTimeline();
             }
 
             @Override
             protected void onResize(int width, int height) {
                 super.onResize(width, height);
                 // redrawTimeline();
                 layout(true);
             }
         };
         wrapper.add(this.timeline);
 
         this.insert(wrapper, 0, new FillData(new Margins(5, 10, 5, 45)));
         this.layout();
     }
 
     private void onNoData() {
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
 
     private void redrawGraph() {
         // only redraw if the graph is already drawn
         if (null != this.graph && this.graph.isAttached()) {
             this.graph.redraw();
         }
     }
 
     private void redrawTimeline() {
         // only redraw if the time line is already drawn
         if (null != this.timeline && this.timeline.isAttached()) {
             this.timeline.redraw();
         }
     }
 
     private void showNumberData(JsArray<Timeseries> data) {
         if (null == this.graph) {
             createGraph(data);
         } else {
             redrawGraph();
         }
     }
 
     private void showStringData(JsArray<Timeseries> data) {
 
         // clear the data table
         this.dataTable.removeRows(0, this.dataTable.getNumberOfRows());
 
         // put the time series values to the data table
         Timeseries ts;
         JsArray<DataPoint> values;
         DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
         for (int i = 0; i < data.length(); i++) {
             ts = data.get(i);
             values = ts.getData();
             for (int j = 0, index = this.dataTable.getNumberOfRows(); j < values.length(); j++) {
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
                         this.dataTable.addRow();
                         index++;
                         this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                         this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                         this.dataTable.setValue(index, 3, ts.getLabel());
                     } else {
                         // only the end time has to be changed
                     }
                 } else {
                     // insert first data point
                     this.dataTable.addRow();
                     this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                     this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                     this.dataTable.setValue(index, 3, ts.getLabel());
                 }
 
                 // set end time
                 if (nextPoint != null) {
                     long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                             .getTimestamp().getTime() - 1000);
                     this.dataTable.setValue(index, 1, new Date(endDate));
                 } else {
                     this.dataTable.setValue(index, 1, new Date());
                 }
             }
         }
 
         if (dataTable.getNumberOfRows() > 0) {
             if (null == this.timeline) {
                 createTimeline();
             } else {
                 redrawTimeline();
             }
         } else {
             logger.warning("No data for time line visualization!");
         }
     }
 }
