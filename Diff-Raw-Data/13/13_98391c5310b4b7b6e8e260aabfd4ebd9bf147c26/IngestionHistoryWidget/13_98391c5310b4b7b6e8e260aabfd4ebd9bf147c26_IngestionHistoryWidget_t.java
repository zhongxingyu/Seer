 package eu.europeana.uim.gui.cp.client.monitoring;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gwt.cell.client.ActionCell;
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.RunAsyncCallback;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.DefaultSelectionEventManager;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.SelectionModel;
 
 import eu.europeana.uim.gui.cp.client.IngestionWidget;
 import eu.europeana.uim.gui.cp.client.services.ExecutionServiceAsync;
 import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
 
 /**
  * Table view showing current exectuions.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Apr 27, 2011
  */
 public class IngestionHistoryWidget extends IngestionWidget {
     /**
      * The UiBinder interface used by this example.
      */
     interface Binder extends UiBinder<Widget, IngestionHistoryWidget> {
     }
 
     private final ExecutionServiceAsync executionService;
     private final List<ExecutionDTO>    pastExecutions = new ArrayList<ExecutionDTO>();
 
     /**
      * The main CellTable.
      */
     @UiField(provided = true)
     CellTable<ExecutionDTO>             cellTable;
 
     /**
      * The pager used to change the range of data.
      */
     @UiField(provided = true)
     SimplePager                         pager;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param executionService
      */
     public IngestionHistoryWidget(ExecutionServiceAsync executionService) {
         super("Finished Ingestions", "This view shows all finished ingestion activities!");
         this.executionService = executionService;
     }
 
     /**
      * Initialize this example.
      */
     @Override
     public Widget onInitialize() {
         cellTable = new CellTable<ExecutionDTO>(ExecutionDTO.KEY_PROVIDER);
         cellTable.setWidth("100%", true);
         cellTable.setPageSize(10);
 
         final ListDataProvider<ExecutionDTO> dataProvider = new ListDataProvider<ExecutionDTO>();
         dataProvider.setList(pastExecutions);
         dataProvider.addDataDisplay(cellTable);
 
         ListHandler<ExecutionDTO> sortHandler = new ListHandler<ExecutionDTO>(
                 dataProvider.getList());
         cellTable.addColumnSortHandler(sortHandler);
 
         SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
         pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
         pager.setDisplay(cellTable);
 
         final SelectionModel<ExecutionDTO> selectionModel = new MultiSelectionModel<ExecutionDTO>(
                 ExecutionDTO.KEY_PROVIDER);
         cellTable.setSelectionModel(selectionModel,
                 DefaultSelectionEventManager.<ExecutionDTO> createCheckboxManager());
 
         initTableColumns(selectionModel, sortHandler);
 
         Binder uiBinder = GWT.create(Binder.class);
         Widget widget = uiBinder.createAndBindUi(this);
 
         return widget;
     }
 
     @Override
     protected void onLoad() {
         super.onLoad();
         updatePastExecutions();
     }
 
     /**
      * Retrieve current past executions.
      */
     public void updatePastExecutions() {
         executionService.getPastExecutions(new AsyncCallback<List<ExecutionDTO>>() {
             @Override
             public void onFailure(Throwable caught) {
                 caught.printStackTrace();
             }
 
             @Override
             public void onSuccess(List<ExecutionDTO> result) {
                 pastExecutions.clear();
                 pastExecutions.addAll(result);
                 cellTable.setRowData(0, pastExecutions);
                 cellTable.setRowCount(pastExecutions.size());
             }
         });
     }
 
     @Override
     protected void asyncOnInitialize(final AsyncCallback<Widget> callback) {
         GWT.runAsync(IngestionHistoryWidget.class, new RunAsyncCallback() {
             @Override
             public void onFailure(Throwable caught) {
                 callback.onFailure(caught);
             }
 
             @Override
             public void onSuccess() {
                 callback.onSuccess(onInitialize());
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
         cellTable.setColumnWidth(idColumn, 10, Unit.PCT);
 
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
         cellTable.setColumnWidth(nameColumn, 20, Unit.PCT);
 
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
         cellTable.setColumnWidth(datasetColumn, 18, Unit.PCT);
 
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
         cellTable.setColumnWidth(startTimeColumn, 13, Unit.PCT);
 
         // End Time
         Column<ExecutionDTO, Date> endTimeColumn = new Column<ExecutionDTO, Date>(new DateCell(dtf)) {
             @Override
             public Date getValue(ExecutionDTO object) {
                 return object.getEndTime();
             }
         };
         endTimeColumn.setSortable(true);
         sortHandler.setComparator(endTimeColumn, new Comparator<ExecutionDTO>() {
             @Override
             public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                 if (o1.getEndTime() != null && o2.getEndTime() != null) {
                     return o1.getEndTime().compareTo(o2.getEndTime());
                 } else {
                     if (o1.getEndTime() == null) { return o2.getEndTime() == null ? 0 : -1; }
                     return o2.getEndTime() == null ? 1 : 0;
                 }
             }
         });
         cellTable.addColumn(endTimeColumn, "End Time");
         cellTable.setColumnWidth(endTimeColumn, 13, Unit.PCT);
 
         // Canceled
         Column<ExecutionDTO, Boolean> doneColumn = new Column<ExecutionDTO, Boolean>(
                 new CheckboxCell()) {
             @Override
             public Boolean getValue(ExecutionDTO object) {
                 return object.isCanceled();
             }
         };
         doneColumn.setSortable(true);
         cellTable.addColumn(doneColumn, "C/F");
         cellTable.setColumnWidth(doneColumn, 5, Unit.PCT);
 
         // Scheduled/Failure/Completed
         Column<ExecutionDTO, String> scheduledColumn = new Column<ExecutionDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(ExecutionDTO object) {
                 return object.getScheduled() + "/" + object.getFailure() + "/" +
                        object.getCompleted();
             }
         };
         cellTable.addColumn(scheduledColumn, "Sche./Fail./Comp.");
         cellTable.setColumnWidth(scheduledColumn, 13, Unit.PCT);
 
         // Created/Updated/Deleted
         Column<ExecutionDTO, String> failureColumn = new Column<ExecutionDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(ExecutionDTO object) {
                 String createdValue = object.getValue("Created Records");
                 String updatedValue = object.getValue("Updated Records");
                String deletedValue = object.getValue("Deleted Records");
 
                 StringBuilder builder = new StringBuilder();
                 if (createdValue != null) {
                     builder.append(createdValue);
                 } else {
                     builder.append(" ");
                 }
                 builder.append("/");
                if (updatedValue != null) {
                    builder.append(updatedValue);
                 } else {
                     builder.append(" ");
                 }
                 builder.append("/");
                if (deletedValue != null) {
                    builder.append(deletedValue);
                 } else {
                     builder.append(" ");
                 }
                 return builder.toString();
             }
         };
         cellTable.addColumn(failureColumn, "Cre./Upd./Del.");
         cellTable.setColumnWidth(failureColumn, 13, Unit.PCT);
 
         // Log file
         Column<ExecutionDTO, ExecutionDTO> logfileColumn = new Column<ExecutionDTO, ExecutionDTO>(
                 new ActionCell<ExecutionDTO>("Log", new ActionCell.Delegate<ExecutionDTO>() {
                     @Override
                     public void execute(ExecutionDTO parameter) {
                         com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() +
                                                                "logfile?format=html&execution=" +
                                                                parameter.getId(), "_blank", "");
                     }
                 })) {
             @Override
             public ExecutionDTO getValue(ExecutionDTO object) {
                 return object;
             }
         };
 
         cellTable.addColumn(logfileColumn, "Log");
         cellTable.setColumnWidth(logfileColumn, 5, Unit.PCT);
 
         updatePastExecutions();
     }
 }
