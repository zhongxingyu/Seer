 package expensable.client.view.batch;
 
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.SafeHtmlCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.SelectionModel;
 
 import expensable.client.activity.batch.ShowBatchesActivity;
 import expensable.shared.models.Batch;
 
 public class BatchesViewImpl extends Composite implements BatchesView {
 
   private static Binder binder = GWT.create(Binder.class);
 
   interface Binder extends UiBinder<Widget, BatchesViewImpl>{}
 
   private static interface GetValue<C> {
 	    C getValue(Batch contact);
 	  }
 
   
   private BatchesPresenter presenter;
   
   @UiField(provided = true) CellTable<Batch> reports;
   /*@UiField(provided = true)*/ SimplePager pager;
 
   public BatchesViewImpl() {
 	  reports = new CellTable<Batch>(ShowBatchesActivity.KEY_PROVIDER);
 
 	    // Create a Pager to control the table.
 	    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
 	    pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
 	    pager.setDisplay(reports);
     initWidget(binder.createAndBindUi(this));
   }
 
   @Override
   public void setPresenter(BatchesPresenter presenter) {
     this.presenter = presenter;
     final MultiSelectionModel<Batch> selectionModel
     = new MultiSelectionModel<Batch>(presenter.getKeyProvider());
 reports.setSelectionModel(selectionModel);
 initTableColumns(selectionModel);
 presenter.addReportsDisplay(reports);
   }
   
 
   private volatile int numCols = 0; // TODO(dpurpura): get this from table somehow
   
   /**
    * Add the columns to the table.
    */
   private void initTableColumns(final SelectionModel<? super Batch> selectionModel) {
     while (numCols-- > 0) {
       reports.removeColumn(0);
     }
 
     // Checkbox column. This table will uses a checkbox column for selection.
     // Alternatively, you can call reports.setSelectionEnabled(true) to enable
     // mouse selection.
     Column<Batch, Boolean> checkColumn
         = new Column<Batch, Boolean>(new CheckboxCell(true)) {
       @Override
       public Boolean getValue(Batch object) {
         return selectionModel.isSelected(object);
       }
     };
     checkColumn.setFieldUpdater(new FieldUpdater<Batch, Boolean>() {
       @Override
       public void update(int index, Batch object, Boolean value) {
         // Called when the user clicks on a checkbox.
         selectionModel.setSelected(object, value);
       }
     });
     reports.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br>"));
 
    
  // Name
     Column<Batch, SafeHtml> idColumn
         = new Column<Batch, SafeHtml>(new SafeHtmlCell()) {
       @Override
       public SafeHtml getValue(Batch report) {
        return new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml("<a href=\"#batches:id=12345\">"+report.getId()+"</a>");
       }
     };
     /*idColumn.setFieldUpdater(new FieldUpdater<Batch, String>() {
 
       @Override
       public void update(int index, Batch report, String value) {
         report.setName(value);
         presenter.refreshDisplays();
       }
 
     });*/
     reports.addColumn(idColumn, "Batch Id");
 
 
     // Name
     Column<Batch, String> nameColumn
         = new Column<Batch, String>(new EditTextCell()) {
       @Override
       public String getValue(Batch report) {
         return report.getName();
       }
     };
     nameColumn.setFieldUpdater(new FieldUpdater<Batch, String>() {
 
       @Override
       public void update(int index, Batch report, String value) {
         report.setName(value);
         presenter.refreshDisplays();
       }
 
     });
     reports.addColumn(nameColumn, "Name");
     
  // Name
     Column<Batch, String> statusColumn
         = new Column<Batch, String>(new EditTextCell()) {
       @Override
       public String getValue(Batch report) {
         return report.getStatus();
       }
     };
     statusColumn.setFieldUpdater(new FieldUpdater<Batch, String>() {
 
       @Override
       public void update(int index, Batch report, String value) {
         report.setStatus(value);
         presenter.refreshDisplays();
       }
 
     });
     reports.addColumn(statusColumn, "Status");
     
     numCols = 4;
   }
 
 @Override
 public CellTable<Batch> getReportsTable() {
 	// TODO Auto-generated method stub
 	return reports;
 }
 
 }
