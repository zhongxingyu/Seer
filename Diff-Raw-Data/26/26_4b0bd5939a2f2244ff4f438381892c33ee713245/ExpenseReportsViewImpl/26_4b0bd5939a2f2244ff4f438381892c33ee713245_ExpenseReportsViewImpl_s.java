 package expensable.client.view.expensereport;
 
 import java.util.Date;
 
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.cell.client.SafeHtmlCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TabBar;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.SelectionModel;
 
 import expensable.client.activity.expensereport.ShowExpenseReportsActivity;
 import expensable.client.place.CreateExpenseReportPlace;
 import expensable.client.view.widget.Button;
import expensable.shared.models.Batch;
 import expensable.shared.models.ExpenseReport;
 
 public class ExpenseReportsViewImpl extends Composite implements ExpenseReportsView {
 
   private static Binder uiBinder = GWT.create(Binder.class);
 
   interface Binder extends UiBinder<Widget, ExpenseReportsViewImpl> {
   }
 
   @UiField TabBar reportType;
   @UiField Button quickApproveButton;
   @UiField(provided = true) CellTable<ExpenseReport> reports;
   @UiField(provided = true) SimplePager pager;
 
   private ExpenseReportsPresenter presenter;
 
   public ExpenseReportsViewImpl() {
     reports = new CellTable<ExpenseReport>(ShowExpenseReportsActivity.KEY_PROVIDER);
 
     // Create a Pager to control the table.
     SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
     pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
     pager.setDisplay(reports);
    
     initWidget(uiBinder.createAndBindUi(this));
 
     //Initialize the Tab Bar with all required tabs
     reportType.addTab("All");
     reportType.addTab("Unsubmitted");
     reportType.addTab("Pending Approval");
     reportType.addTab("Approved");
     reportType.addTab("Rejected");
    
   }
 
   @Override
   public void setPresenter(ExpenseReportsPresenter presenter) {
     this.presenter = presenter;
 
     // Add a selection model so we can select cells.
     final MultiSelectionModel<ExpenseReport> selectionModel
         = new MultiSelectionModel<ExpenseReport>(presenter.getKeyProvider());
     reports.setSelectionModel(selectionModel);
     initTableColumns(selectionModel);
     presenter.addReportsDisplay(reports);
   }
 
   @Override
   public CellTable<ExpenseReport> getReportsTable() {
     return reports;
   }
 
   @UiHandler("quickApproveButton")
   void onClick(ClickEvent e) {
 	  presenter.goTo(new CreateExpenseReportPlace(""));
   }
 
   private volatile int numCols = 0; // TODO(dpurpura): get this from table somehow
 
   /**
    * Add the columns to the table.
    */
   private void initTableColumns(final SelectionModel<? super ExpenseReport> selectionModel) {
     while (numCols-- > 0) {
       reports.removeColumn(0);
     }
 
     // Checkbox column. This table will uses a checkbox column for selection.
     // Alternatively, you can call reports.setSelectionEnabled(true) to enable
     // mouse selection.
     Column<ExpenseReport, Boolean> checkColumn
         = new Column<ExpenseReport, Boolean>(new CheckboxCell(true)) {
       @Override
       public Boolean getValue(ExpenseReport object) {
         return selectionModel.isSelected(object);
       }
     };
     checkColumn.setFieldUpdater(new FieldUpdater<ExpenseReport, Boolean>() {
       @Override
       public void update(int index, ExpenseReport object, Boolean value) {
         // Called when the user clicks on a checkbox.
         selectionModel.setSelected(object, value);
       }
     });
     reports.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br>"));
 
     // Created date
     Column<ExpenseReport, Date> createdDateColumn
         = new Column<ExpenseReport, Date>(new DateCell()) {
       @Override
       public Date getValue(ExpenseReport report) {
         return report.getCreatedDate();
       }
     };
     reports.addColumn(createdDateColumn, "Created Date");
 
     // Amount
     Column<ExpenseReport, Number> amountColumn
         = new Column<ExpenseReport, Number>(new NumberCell()) {
       @Override
       public Number getValue(ExpenseReport report) {
         return report.getAmount();
       }
     };
     reports.addColumn(amountColumn, "Amount");
 
     // Employee Name
     Column<ExpenseReport, String> nameColumn
         = new Column<ExpenseReport, String>(new TextCell()) {
       @Override
       public String getValue(ExpenseReport report) {
         return report.getName();
       }
     };
    reports.addColumn(nameColumn, "Employee Name");    
    
     // ID
    
     Column<ExpenseReport, SafeHtml> idColumn
     = new Column<ExpenseReport, SafeHtml>(new SafeHtmlCell()) {
   @Override
   public SafeHtml getValue(ExpenseReport report) {
     return new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(
         "<a href=\"#reports:rid=1\">"+report.getId()+"</a>");
   }
 };
     reports.addColumn(idColumn, "Tracking Id");
    
     // Attachment
     Column<ExpenseReport, String> attachmentColumn
         = new Column<ExpenseReport, String>(new TextCell()) {
       @Override
       public String getValue(ExpenseReport report) {
         return report.getAttachment();
       }
     };
     reports.addColumn(attachmentColumn, "Attachment");
    
     numCols = 6;
   }
 
 }
