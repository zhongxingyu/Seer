 package nl.sense_os.commonsense.client.states.feedback;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.components.CenteredWindow;
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
 import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
 import nl.sense_os.commonsense.client.viz.panels.VizPanel;
 
 import com.chap.links.client.AddHandler;
 import com.chap.links.client.EditHandler;
 import com.chap.links.client.Graph;
 import com.chap.links.client.Timeline;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
 import com.extjs.gxt.ui.client.widget.layout.FillData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.Selection;
 import com.google.gwt.visualization.client.events.RangeChangeHandler;
 
 public class FeedbackPanel extends VizPanel {
 
     private static final Logger LOG = Logger.getLogger(FeedbackPanel.class.getName());
 
     private final SensorModel stateSensor;
     private final LayoutContainer stateContainer;
     private final LayoutContainer vizContainer;
     private List<String> labels;
 
     private Graph graph;
     private final Graph.Options graphOpts = Graph.Options.create();
     private Timeline sensorTline;
     private final Timeline.Options tlineOpts = Timeline.Options.create();
     private Timeline stateTline;
     private DataTable initialStates;
 
     private Button submitButton;
     private Button cancelButton;
 
     private boolean isProcessingInBackground;
 
     public FeedbackPanel(SensorModel stateSensor, List<SensorModel> sensors, long start, long end,
             boolean subsample, String title, List<String> labels) {
         super();
 
         // LOG.setLevel(Level.ALL);
 
         this.stateSensor = stateSensor;
         this.labels = labels;
 
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
 
         // set up layout
         setHeading("Feedback: " + stateSensor.getDisplayName());
         setBodyBorder(false);
         setLayout(new RowLayout(Orientation.VERTICAL));
 
         // separate containers for the state timeline and for the other sensor visualizations
         stateContainer = new LayoutContainer(new FitLayout());
         add(stateContainer, new RowData(1, 150));
         vizContainer = new LayoutContainer(new FillLayout(Orientation.VERTICAL));
         add(vizContainer, new RowData(1, 1));
         createButtons();
 
         // request sensor data
         sensors.add(stateSensor);
         visualize(sensors, start, end, subsample);
 
         DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
         LOG.finest("Start time: " + dtf.format(new Date(start)) + ", end time: "
                 + dtf.format(new Date(end)));
     }
 
     private void addFeedbackHandlers() {
 
         stateTline.addAddHandler(new AddHandler() {
 
             @Override
             public void onAdd(AddEvent event) {
                 showLabelChoice(true);
             }
         });
 
         stateTline.addEditHandler(new EditHandler() {
 
             @Override
             public void onEdit(EditEvent event) {
                 showLabelChoice(false);
             }
         });
     }
 
     private void createButtons() {
         SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 Button source = ce.getButton();
                 if (source.equals(submitButton)) {
                     submitForm();
                 } else if (source.equals(cancelButton)) {
                     FeedbackPanel.this.hide();
                 } else {
                     LOG.warning("Unexpected button pressed");
                 }
             }
         };
 
        submitButton = new Button("Submit Feedback", l);
         submitButton.setIconStyle("sense-btn-icon-go");
         submitButton.setMinWidth(75);
         cancelButton = new Button("Cancel", l);
         cancelButton.setMinWidth(75);
 
         ButtonBar bar = new ButtonBar();
         bar.setAlignment(HorizontalAlignment.CENTER);
         bar.add(submitButton);
         bar.add(cancelButton);
         setBottomComponent(bar);
     }
 
     private void createGraph(JsArray<Timeseries> numberData) {
         LOG.fine("Create line graph...");
 
         // Graph options
         graphOpts.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
         graphOpts.setLineRadius(2);
         graphOpts.setWidth("100%");
         graphOpts.setHeight("100%");
         graphOpts.setLegendCheckboxes(true);
         graphOpts.setLegendWidth(125);
 
         // create graph instance
         graph = new Graph(numberData, graphOpts);
 
         graph.addRangeChangeHandler(new RangeChangeHandler() {
 
             @Override
             public void onRangeChange(RangeChangeEvent event) {
                 if (null != sensorTline) {
                     sensorTline.setVisibleChartRange(event.getStart(), event.getEnd());
                     // sensorTline.redraw(); // not required
                 }
 
                 if (null != stateTline) {
                     stateTline.setVisibleChartRange(event.getStart(), event.getEnd());
                     // stateTline.redraw(); // not required
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
         graphWrapper.add(graph);
 
         vizContainer.add(graphWrapper, new FillData(0));
         layout();
     }
 
     /**
      * @return An DataTable with the correct columns for Timeline visualization.
      */
     private DataTable createSensorsTable(JsArray<Timeseries> data) {
         LOG.fine("Create sensors data table...");
 
         DataTable dataTable = DataTable.create();
         dataTable.addColumn(DataTable.ColumnType.DATETIME, "startdate");
         dataTable.addColumn(DataTable.ColumnType.DATETIME, "enddate");
         dataTable.addColumn(DataTable.ColumnType.STRING, "content");
         dataTable.addColumn(DataTable.ColumnType.STRING, "group");
 
         // put the time series values to the data table
         Timeseries ts;
         JsArray<DataPoint> values;
         DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
         for (int i = 0; i < data.length(); i++) {
             ts = data.get(i);
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
 
     private void createSensorsTimeline(DataTable table) {
         LOG.fine("Create sensors timeline...");
 
         // time line options
         Timeline.Options options = Timeline.Options.create();
         options.setWidth("100%");
         options.setHeight("100%");
         options.setAnimate(false);
         options.setSelectable(false);
         options.setEditable(false);
         options.setStackEvents(false);
         options.setGroupsOnRight(true);
         options.setGroupsWidth(135);
 
         sensorTline = new Timeline(table, options);
 
         // keep the range equal to the other charts
         sensorTline.addRangeChangeHandler(new RangeChangeHandler() {
 
             @Override
             public void onRangeChange(RangeChangeEvent event) {
                 if (null != graph) {
                     graph.setVisibleChartRange(event.getStart(), event.getEnd());
                     graph.redraw();
                 }
 
                 if (null != stateTline) {
                     stateTline.setVisibleChartRange(event.getStart(), event.getEnd());
                     // stateTline.redraw(); // not required
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
         wrapper.add(sensorTline, new FitData(0));
 
         vizContainer.insert(wrapper, 0, new FillData(new Margins(5, 10, 5, 70)));
         layout();
     }
 
     /**
      * @return A DataTable with the correct columns for Timeline visualization.
      */
     private DataTable createStateTable(JsArray<Timeseries> stateData) {
         LOG.fine("Create state data table...");
 
         DataTable table = DataTable.create();
         table.addColumn(DataTable.ColumnType.DATETIME, "startdate");
         table.addColumn(DataTable.ColumnType.DATETIME, "enddate");
         table.addColumn(DataTable.ColumnType.STRING, "content");
 
         initialStates = DataTable.create();
         initialStates.addColumn(DataTable.ColumnType.DATETIME, "startdate");
         initialStates.addColumn(DataTable.ColumnType.DATETIME, "enddate");
         initialStates.addColumn(DataTable.ColumnType.STRING, "content");
 
         // put the time series values to the data table
         Timeseries ts;
         JsArray<DataPoint> values;
         DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
         for (int i = 0; i < stateData.length(); i++) {
             ts = stateData.get(i);
             values = ts.getData();
             for (int j = 0, index = table.getNumberOfRows(); j < values.length(); j++) {
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
                         index++;
                         table.addRow();
                         table.setValue(index, 0, dataPoint.getTimestamp());
                         table.setValue(index, 2, dataPoint.getRawValue());
                         initialStates.addRow();
                         initialStates.setValue(index, 0, dataPoint.getTimestamp());
                         initialStates.setValue(index, 2, dataPoint.getRawValue());
                     } else {
                         // only the end time has to be changed
                     }
                 } else {
                     // insert first data point
                     table.addRow();
                     table.setValue(index, 0, dataPoint.getTimestamp());
                     table.setValue(index, 2, dataPoint.getRawValue());
                     initialStates.addRow();
                     initialStates.setValue(index, 0, dataPoint.getTimestamp());
                     initialStates.setValue(index, 2, dataPoint.getRawValue());
                 }
 
                 // set end time
                 if (nextPoint != null) {
                     long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                             .getTimestamp().getTime() - 1000);
                     table.setValue(index, 1, new Date(endDate));
                     initialStates.setValue(index, 1, new Date(endDate));
                 } else {
                     table.setValue(index, 1, new Date());
                     initialStates.setValue(index, 1, new Date());
                 }
             }
         }
 
         return table;
     }
 
     private void createStateTimeline(DataTable table) {
         LOG.fine("Create state timeline...");
 
         // time line options
         Timeline.Options options = Timeline.Options.create();
         options.setWidth("100%");
         options.setHeight("100%");
         options.setEditable(true); // important
         options.setStackEvents(true);
         options.setGroupsOnRight(true);
         options.setGroupsWidth(135);
 
         stateTline = new Timeline(table, options);
         if (table.getNumberOfRows() == 0) {
             stateTline.setVisibleChartRange(new Date(start), new Date(end));
         }
 
         stateTline.addRangeChangeHandler(new RangeChangeHandler() {
 
             @Override
             public void onRangeChange(RangeChangeEvent event) {
                 if (null != sensorTline) {
                     sensorTline.setVisibleChartRange(event.getStart(), event.getEnd());
                     // sensorTline.redraw(); // not required
                 }
 
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
                 redrawFeedback();
                 super.onAfterLayout();
             }
 
             @Override
             protected void onResize(int width, int height) {
                 super.onResize(width, height);
                 this.layout();
             }
         };
         wrapper.add(stateTline, new FitData(0));
 
         stateContainer.add(wrapper, new FitData(new Margins(5, 145, 5, 70)));
         layout();
 
         addFeedbackHandlers();
     }
 
     private List<FeedbackData> findChanges() {
         List<FeedbackData> changes = new ArrayList<FeedbackData>();
 
         DataTable endStates = stateTline.getData();
 
         Date initialStart, finalStart, initialEnd, finalEnd;
         String initialLabel, finalLabel;
         boolean isInitialState, isFinalState;
 
         // check for deleted stateTline
         for (int i = 0; i < initialStates.getNumberOfRows(); i++) {
             initialStart = initialStates.getValueDate(i, 0);
             initialEnd = initialStates.getValueDate(i, 1);
             isFinalState = false;
             for (int j = 0; j < endStates.getNumberOfRows(); j++) {
                 finalStart = endStates.getValueDate(j, 0);
                 finalEnd = endStates.getValueDate(j, 1);
                 if (initialStart.compareTo(finalStart) == 0 && initialEnd.compareTo(finalEnd) == 0) {
                     isFinalState = true;
                     break;
                 }
             }
 
             if (false == isFinalState) {
                 changes.add(new FeedbackData(initialStart.getTime(), initialEnd.getTime(),
                         FeedbackData.TYPE_REMOVE, null));
             }
         }
 
         // check for changed and added stateTline
         for (int i = 0; i < endStates.getNumberOfRows(); i++) {
             finalStart = endStates.getValueDate(i, 0);
             finalEnd = endStates.getValueDate(i, 1);
             finalLabel = endStates.getValueString(i, 2);
             isInitialState = false;
             for (int j = 0; j < initialStates.getNumberOfRows(); j++) {
                 initialStart = initialStates.getValueDate(j, 0);
                 initialEnd = initialStates.getValueDate(j, 1);
                 if (initialStart.compareTo(finalStart) == 0 && initialEnd.compareTo(finalEnd) == 0) {
                     isInitialState = true;
                     initialLabel = initialStates.getValueString(j, 2);
                     if (!initialLabel.equals(finalLabel)) {
                         changes.add(new FeedbackData(initialStart.getTime(), initialEnd.getTime(),
                                 FeedbackData.TYPE_REMOVE, null));
                         changes.add(new FeedbackData(finalStart.getTime(), finalEnd.getTime(),
                                 FeedbackData.TYPE_ADD, finalLabel));
                     }
                     break;
                 }
             }
 
             if (false == isInitialState) {
                 changes.add(new FeedbackData(finalStart.getTime(), finalEnd.getTime(),
                         FeedbackData.TYPE_ADD, finalLabel));
             }
         }
 
         return changes;
     }
 
     public void onFeedbackComplete() {
         setBusy(false);
         if (!isProcessingInBackground) {
             MessageBox.info(null, "Feedback succesfully processed.",
                     new Listener<MessageBoxEvent>() {
 
                         @Override
                         public void handleEvent(MessageBoxEvent be) {
                             // do nothing
                         }
                     });
         }
     }
 
     public void onFeedbackFailed() {
         setBusy(false);
         if (!isProcessingInBackground) {
             MessageBox.alert(null, "Failed to process feedback!", null);
         }
     }
 
     public void onFeedbackSlow() {
         setBusy(false);
         if (!isProcessingInBackground) {
             isProcessingInBackground = true;
             String msg = "Oops! Processing of feedback is taking a longer than usual."
                     + " This usually happens when you submit feedback for a very large time period."
                     + "<br><br>"
                     + "Processing will continue in the background, you can close the feedback panel.";
             MessageBox.info(null, msg, null);
         }
     }
 
     @Override
     protected void onNewData(JsArray<Timeseries> data) {
         LOG.fine("New data...");
 
         JsArray<Timeseries> numberData = JavaScriptObject.createArray().cast();
         JsArray<Timeseries> stringData = JavaScriptObject.createArray().cast();
         JsArray<Timeseries> stateData = JavaScriptObject.createArray().cast();
         for (int i = 0; i < data.length(); i++) {
             Timeseries ts = data.get(i);
             if (ts.getId() == stateSensor.getId()) {
                 LOG.fine(ts.getLabel() + ": " + ts.getData().length() + " data points (state data)");
                 stateData.push(ts);
             } else if (ts.getType().equalsIgnoreCase("number")) {
                 LOG.fine(ts.getLabel() + ": " + ts.getData().length()
                         + " data points (number data)");
                 numberData.push(ts);
             } else {
                 LOG.fine(ts.getLabel() + ": " + ts.getData().length() + " data points ("
                         + ts.getType() + " data)");
                 stringData.push(ts);
             }
         }
 
         // show the stateSensor data in a stateSensor time line
         showStateData(stateData);
 
         // show the string data in a time line
         if (stringData.length() > 0) {
             showStringData(stringData);
         }
 
         // show the numerical data in a line graph
         if (numberData.length() > 0) {
             showNumberData(numberData);
         }
 
         // find out the time range that spans all three charts
         Date rangeStart = new Date(start);
         Date rangeEnd = new Date(start);
         if (stateTline != null && stateData.length() > 0) {
             Timeline.DateRange stateRange = stateTline.getVisibleChartRange();
             rangeStart = stateRange.getStart();
             rangeEnd = stateRange.getEnd();
         }
         if (graph != null) {
             Graph.DateRange range = graph.getVisibleChartRange();
             rangeStart = range.getStart().before(rangeStart) ? range.getStart() : rangeStart;
             rangeEnd = range.getEnd().after(rangeEnd) ? range.getEnd() : rangeEnd;
         }
         if (sensorTline != null) {
             Timeline.DateRange range = sensorTline.getVisibleChartRange();
             rangeStart = range.getStart().before(rangeStart) ? range.getStart() : rangeStart;
             rangeEnd = range.getEnd().after(rangeEnd) ? range.getEnd() : rangeEnd;
         }
 
         // set same the visible time range for each chart
         stateTline.setVisibleChartRange(rangeStart, rangeEnd);
         stateTline.redraw();
         if (null != graph) {
             graph.setVisibleChartRange(rangeStart, rangeEnd);
             graph.redraw();
         }
         if (null != sensorTline) {
             sensorTline.setVisibleChartRange(rangeStart, rangeEnd);
             sensorTline.redraw();
         }
 
         LOG.finest("onNewData finished...");
     }
 
     private void redrawFeedback() {
         // only redraw if the time line is already drawn
         if (null != stateTline && stateTline.isAttached()) {
             stateTline.redraw();
         }
     }
 
     private void redrawGraph() {
         // only redraw if the graph is already drawn
         if (null != graph && graph.isAttached()) {
             graph.redraw();
         }
     }
 
     private void redrawTimeline() {
         // only redraw if the time line is already drawn
         if (null != sensorTline && sensorTline.isAttached()) {
             sensorTline.redraw();
         }
     }
 
     private void removeActiveEvent() {
         JsArray<Selection> sel = stateTline.getSelections();
         if (sel != null && sel.length() > 0) {
             final int row = sel.get(0).getRow();
             stateTline.getData().removeRow(row);
             stateTline.redraw();
         }
     }
 
     private void setBusy(boolean busy) {
         if (busy) {
             submitButton.setIconStyle("sense-btn-icon-loading");
         } else {
             submitButton.setIconStyle("sense-btn-icon-go");
         }
     }
 
     private void setLabelChoice(String label) {
 
         // check if label is already listed
         if (!labels.contains(label)) {
             labels.add(label);
         }
 
         // retrieve the row number of the changed event
         JsArray<Selection> sel = stateTline.getSelections();
         if (sel != null && sel.length() > 0) {
             final int row = sel.get(0).getRow();
 
             if (label != null) {
                 // apply the new title
                 stateTline.getData().setValue(row, 2, label);
                 stateTline.redraw();
             }
         }
     }
 
     private void showLabelChoice(final boolean newEntry) {
 
         // get current label
         JsArray<Selection> sel = stateTline.getSelections();
         String currentLabel = null;
         if (sel.length() > 0) {
             final int row = sel.get(0).getRow();
             currentLabel = stateTline.getData().getValueString(row, 2);
         } else if (labels.size() > 0) {
             currentLabel = labels.get(0);
         }
 
         final CenteredWindow choiceWindow = new CenteredWindow();
         choiceWindow.setSize(300, 100);
         choiceWindow.setLayout(new FitLayout());
         choiceWindow.setHeading("State label selection");
         choiceWindow.addListener(Events.Close, new Listener<ComponentEvent>() {
 
             @Override
             public void handleEvent(ComponentEvent be) {
                 LOG.finest("Label choice closed");
                 if (newEntry) {
                     removeActiveEvent();
                 }
             }
         });
 
         FormPanel choiceForm = new FormPanel();
         choiceForm.setHeaderVisible(false);
         choiceForm.setBodyBorder(false);
 
         final SimpleComboBox<String> labelCombo = new SimpleComboBox<String>();
         labelCombo.setFieldLabel("Select state");
         labelCombo.setEmptyText("Select one, or enter a new state.");
         labelCombo.setAllowBlank(false);
         labelCombo.setTypeAhead(true);
         labelCombo.setTriggerAction(TriggerAction.ALL);
         labelCombo.add(labels);
         SimpleComboValue<String> v = labelCombo.findModel(currentLabel);
         if (v != null) {
             labelCombo.setValue(v);
         }
 
         choiceForm.add(labelCombo, new FormData("-10"));
 
         Button submitChoice = new Button("Ok", new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 choiceWindow.hide();
                 setLabelChoice(labelCombo.getSimpleValue());
             }
         });
         submitChoice.setMinWidth(75);
 
         FormButtonBinding binding = new FormButtonBinding(choiceForm);
         binding.addButton(submitChoice);
 
         Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 if (newEntry) {
                     removeActiveEvent();
                 }
                 choiceWindow.hide();
             }
         });
         cancel.setMinWidth(75);
 
         ButtonBar bar = new ButtonBar();
         bar.setAlignment(HorizontalAlignment.CENTER);
         bar.add(submitChoice);
         bar.add(cancel);
         choiceForm.setBottomComponent(bar);
 
         choiceWindow.add(choiceForm);
         choiceWindow.show();
     }
 
     private void showNumberData(JsArray<Timeseries> data) {
         LOG.fine("Show numeric data...");
 
         if (null == graph) {
             createGraph(data);
         } else {
             LOG.fine("Draw on existing line graph");
             graph.draw(data, graphOpts);
         }
     }
 
     private void showStateData(JsArray<Timeseries> data) {
         LOG.fine("Show state data...");
 
         if (null == stateTline) {
             // create a new data table
             DataTable table = createStateTable(data);
             createStateTimeline(table);
         } else {
             // do not update the state data table! this erases all feedback that was already added!
             stateTline.redraw();
 
             // LOG.fine("Draw on existing state timeline");
             // stateTline.draw(table);
         }
     }
 
     private void showStringData(JsArray<Timeseries> data) {
         LOG.fine("Show string data...");
 
         // create a new data table
         DataTable table = createSensorsTable(data);
 
         if (table.getNumberOfRows() > 0) {
             if (null == sensorTline) {
                 createSensorsTimeline(table);
             } else {
                 LOG.fine("Draw on existing sensors timeline");
                 sensorTline.draw(table);
             }
         } else {
             LOG.warning("No data for time line visualization!");
         }
     }
 
     private void submitForm() {
         setBusy(true);
 
         List<FeedbackData> changes = findChanges();
         AppEvent submitEvent = new AppEvent(FeedbackEvents.FeedbackSubmit);
         submitEvent.setData("state", stateSensor);
         submitEvent.setData("changes", changes);
         submitEvent.setData("panel", this);
         Dispatcher.forwardEvent(submitEvent);
     }
 }
