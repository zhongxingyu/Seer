 package eu.europeana.uim.gui.cp.client.europeanawidgets;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import eu.europeana.uim.gui.cp.client.services.ReportingServiceAsync;
 
 import com.google.gwt.cell.client.ActionCell;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import eu.europeana.uim.gui.cp.client.IngestionWidget;
 import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
 
 /**
  * Table view showing current executions.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Apr 27, 2011
  */
 public abstract class AbstractReportingWidget extends IngestionWidget {
     private final ReportingServiceAsync reportService;
     private final List<ExecutionDTO>    pastExecutions = new ArrayList<ExecutionDTO>();
 
     String IGNORED_KEY;
     String SUBMITTED_KEY;
     String PROCESSED_KEY;
     
     
     /**
      * The UiBinder interface used by this example.
      */
     interface Binder extends UiBinder<Widget, AbstractReportingWidget> {
     }
 
     /**
      * The main CellTable.
      */
     @UiField(provided = true)
     CellTable<ExecutionDTO> cellTable;
 
     /**
      * The pager used to change the range of data.
      */
     @UiField(provided = true)
     SimplePager             pager;
 
     private String[]        workflows;
 
     private String          reportName;
     private String[]        outputFormats;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param reportService
      * @param widgetName
      * @param workflows
      * @param reportName
      * @param outputFormats
      */
     public AbstractReportingWidget(ReportingServiceAsync reportService, String widgetName,
                                    String[] workflows, String reportName, String[] outputFormats) {
         super(widgetName, widgetName +
                           " - this view generates reports about the current process of the ingestion");
         this.reportService = reportService;
 
         this.setWorkflows(workflows);
         this.setReportName(reportName);
         this.setOutputFormats(outputFormats);
 
         // downloadFrame = new Frame();
     }
 
     /**
      * Initialize this example.
      */
     @Override
     public Widget onInitialize() {
         cellTable = new CellTable<ExecutionDTO>(ExecutionDTO.KEY_PROVIDER);
         cellTable.setWidth("100%", true);
         cellTable.setPageSize(10);
 
         // List Data providers
         final ListDataProvider<ExecutionDTO> dataProvider = new ListDataProvider<ExecutionDTO>();
         dataProvider.setList(pastExecutions);
         dataProvider.addDataDisplay(cellTable);
 
         ListHandler<ExecutionDTO> sortHandler = new ListHandler<ExecutionDTO>(
                 new ListDataProvider<ExecutionDTO>().getList());
         cellTable.addColumnSortHandler(sortHandler);
 
         final SingleSelectionModel<ExecutionDTO> selectionModel = new SingleSelectionModel<ExecutionDTO>(
                 ExecutionDTO.KEY_PROVIDER);
         cellTable.setSelectionModel(selectionModel);
 
         SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
         pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
         pager.setDisplay(cellTable);
 
         initTableColumns(selectionModel, sortHandler);
 
         Binder uiBinder = GWT.create(Binder.class);
         Widget widget = uiBinder.createAndBindUi(this);
 
         return widget;
     }
 
     @Override
     protected void onLoad() {
         super.onLoad();
         updateFinishedExecutions();
     }
 
     /**
      * Retrieve current executions.
      */
     public void updateFinishedExecutions() {
         reportService.getPastExecutions(getWorkflows(), getReportName(), getOutputFormats(),
                 new AsyncCallback<List<ExecutionDTO>>() {
                     @Override
                     public void onFailure(Throwable throwable) {
                         throwable.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(List<ExecutionDTO> executions) {
                         pastExecutions.clear();
                         pastExecutions.addAll(executions);
                         cellTable.setRowData(0, pastExecutions);
                         cellTable.setRowCount(pastExecutions.size());
                     }
                 });
     }
 
     /**
      * Add the columns to the table.
      */
     private void initTableColumns(final SelectionModel<ExecutionDTO> selectionModel,
             ListHandler<ExecutionDTO> sortHandler) {
         // ID
         Column<ExecutionDTO, String> idColumn = new Column<ExecutionDTO, String>(new TextCell()) {
             @Override
             public String getValue(ExecutionDTO object) {
                 return object.getId().toString();
             }
         };
         idColumn.setSortable(true);
         sortHandler.setComparator(idColumn, new Comparator<ExecutionDTO>() {
             @Override
             public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                 return o1.getId().toString().compareTo(o2.getId().toString());
             }
         });
         cellTable.addColumn(idColumn, "ID");
         cellTable.setColumnWidth(idColumn, 20, Unit.PCT);
         
 
         // Name
         Column<ExecutionDTO, String> nameColumn = new Column<ExecutionDTO, String>(new TextCell()) {
             @Override
             public String getValue(ExecutionDTO object) {
                 return object.getName();
             }
         };
         nameColumn.setSortable(true);
         sortHandler.setComparator(nameColumn, new Comparator<ExecutionDTO>() {
             @Override
             public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         });
         cellTable.addColumn(nameColumn, "Name");
         cellTable.setColumnWidth(nameColumn, 15, Unit.PCT);
 
         // Data set
         Column<ExecutionDTO, String> datasetColumn = new Column<ExecutionDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(ExecutionDTO object) {
                 return object.getDataSet();
             }
         };
         datasetColumn.setSortable(true);
         sortHandler.setComparator(datasetColumn, new Comparator<ExecutionDTO>() {
             @Override
             public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                 return o1.getDataSet().compareTo(o2.getDataSet());
             }
         });
         cellTable.addColumn(datasetColumn, "Dataset");
         cellTable.setColumnWidth(datasetColumn, 15, Unit.PCT);
 
 
         DateTimeFormat dtf = DateTimeFormat.getFormat("dd.MM.yyyy 'at' HH:mm:ss");
         // Start Time
         Column<ExecutionDTO, Date> startTimeColumn = new Column<ExecutionDTO, Date>(new DateCell(
                 dtf)) {
             @Override
             public Date getValue(ExecutionDTO object) {
                 return object.getStartTime();
             }
         };
         startTimeColumn.setSortable(true);
         sortHandler.setComparator(startTimeColumn, new Comparator<ExecutionDTO>() {
             @Override
             public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                 return o1.getStartTime().compareTo(o2.getStartTime());
             }
         });
         cellTable.addColumn(startTimeColumn, "Start Time");
         cellTable.setColumnWidth(startTimeColumn, 15, Unit.PCT);
 
         // End Time
 
         // Completed
         Column<ExecutionDTO, String> completedColumn = new Column<ExecutionDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(ExecutionDTO object) {
                 String ignored = object.getValue(IGNORED_KEY);
                 ignored = ignored == null ? "0" : ignored;
                 String submitted = object.getValue(SUBMITTED_KEY);
                 submitted = submitted == null ? "0" : submitted;
                 String processed = object.getValue(PROCESSED_KEY);
                 processed = processed == null ? "0" : processed;
 
                 return "" + object.getCompleted() + "/" + submitted + "/" + ignored + "/" +
                        processed;
             }
         };
         completedColumn.setSortable(true);
         sortHandler.setComparator(completedColumn, new Comparator<ExecutionDTO>() {
             @Override
             public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                 return new Integer(o1.getCompleted()).compareTo(o2.getCompleted());
             }
         });
         cellTable.addColumn(completedColumn, "Rec./Sub./Ign./Open");
         cellTable.setColumnWidth(completedColumn,  30, Unit.PCT);
 
         ActionCell.Delegate<ExecutionDTO> generateDelegate = new ActionCell.Delegate<ExecutionDTO>() {
             @Override
             public void execute(ExecutionDTO execution) {
                 final WaiterDialogBox wDialog = new WaiterDialogBox();
                 RootPanel.get().add(wDialog);
                 wDialog.center();
 
                 reportService.generateReport(execution.getId().toString(), getReportName(),
                         getOutputFormats(), new AsyncCallback<Boolean>() {
 
                             @Override
                             public void onSuccess(Boolean result) {
 
                                 wDialog.removeFromParent();
                                 if (!result) {
                                     Window.alert("Could not generate reports. BIRT server running ?");
                                 }
                                 updateFinishedExecutions();
                             }
 
                             @Override
                             public void onFailure(Throwable caught) {
 
                                 // something is wrong, show stacktrace
                                 TextArea tArea = new TextArea();
                                 String st = caught.getClass().getName() + ": " +
                                             caught.getMessage();
                                 for (StackTraceElement ste : caught.getStackTrace())
                                     st += "\n" + ste.toString();
 
                                 tArea.setText(st);
                                 wDialog.setWidget(tArea);
                                 // wDialog.removeFromParent();
                             }
                         });
             }
         };
 
         ActionCell<ExecutionDTO> actionCell = new ActionCell<ExecutionDTO>("Generate Reports",
                 generateDelegate) {
         };
         Column<ExecutionDTO, ExecutionDTO> generateColumn = new Column<ExecutionDTO, ExecutionDTO>(
                 actionCell) {
             @Override
             public ExecutionDTO getValue(ExecutionDTO object) {
                 return object;
             }
 
         };
         cellTable.addColumn(generateColumn, "Generate");
         cellTable.setColumnWidth(generateColumn, 120, Unit.PX);
 
         for (final String format : getOutputFormats()) {
             ActionCell.Delegate<ExecutionDTO> downloadPDFDelegate = new ActionCell.Delegate<ExecutionDTO>() {
 
                 @Override
                 public void execute(ExecutionDTO object) {
 
                     reportService.getReport(getReportName(), object.getId().toString(), format,
                             new AsyncCallback<String>() {
 
                                 @Override
                                 public void onSuccess(String reportURL) {
                                     Window.open(reportURL, "_blank", "enabled");
                                 }
 
                                 @Override
                                 public void onFailure(Throwable caught) {
                                 }
                             });
 
                 }
             };
 
             String key = getReportName() + "." + format;
             ReportingButtonCell pdfActionCell = new ReportingButtonCell(key, "Download " +
                                                                              format.toUpperCase(),
                     downloadPDFDelegate);
 
             Column<ExecutionDTO, ExecutionDTO> downloadPDFColumn = new Column<ExecutionDTO, ExecutionDTO>(
                     pdfActionCell) {
                 @Override
                 public ExecutionDTO getValue(ExecutionDTO object) {
                     return object;
                 }
 
             };
             cellTable.addColumn(downloadPDFColumn, format.toUpperCase());
             cellTable.setColumnWidth(downloadPDFColumn, 240 / getOutputFormats().length, Unit.PX);
         }
 
         updateFinishedExecutions();
         Timer t = new Timer() {
             @Override
             public void run() {
                 updateFinishedExecutions();
             }
         };
         t.scheduleRepeating(5000);
     }
 
     /**
      * @param reportName
      */
     public void setReportName(String reportName) {
         this.reportName = reportName;
     }
 
     /**
      * @return report name
      */
     public String getReportName() {
         return reportName;
     }
 
     /**
      * @return workflows
      */
     public String[] getWorkflows() {
         return workflows;
     }
 
     /**
      * @param workflows
      */
     public void setWorkflows(String[] workflows) {
         this.workflows = workflows;
     }
 
     /**
      * @return output formats
      */
     public String[] getOutputFormats() {
         return outputFormats;
     }
 
     /**
      * @param outputFormats
      */
     public void setOutputFormats(String[] outputFormats) {
         this.outputFormats = outputFormats;
     }
 
     class WaiterDialogBox extends DialogBox {
         public WaiterDialogBox() {
             setText("Generating reports...");
             Image img = new Image("images/network.gif");
 
             setWidget(img);
         }
     }
 
     private static class ReportingButtonCell extends ActionCell<ExecutionDTO> {
         private final String key;
         private final String text;
 
         /**
          * Creates a new instance of this class.
          * 
          * @param key
          * 
          * @param text
          * @param delegate
          */
         public ReportingButtonCell(String key, String text,
                                    ActionCell.Delegate<ExecutionDTO> delegate) {
             super(text, delegate);
             this.key = key;
             this.text = text;
         }
 
         @Override
         public void render(final Context context, final ExecutionDTO data, final SafeHtmlBuilder sb) {
             if (Boolean.parseBoolean(data.getValue(key))) {
                 sb.appendHtmlConstant("<button tabindex=\"-1\">");
             } else {
                 sb.appendHtmlConstant("<button disabled=\"disabled\"  tabindex=\"-1\">");
             }
             sb.append(SafeHtmlUtils.fromString(text));
             sb.appendHtmlConstant("</button>");
         }
     }
 }
