 package eu.comexis.napoleon.client.core.paymentOwner;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import eu.comexis.napoleon.client.core.AbstractListView;
 import eu.comexis.napoleon.client.utils.SimpleTextComparator;
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.PaymentOwner;
 
 public class PaymentOwnerListView extends AbstractListView<PaymentOwner> implements
   PaymentOwnerListPresenter.MyView {
 
   // key provider object implementation for SimpleLease object
   private static final ProvidesKey<PaymentOwner> KEY_PROVIDER = new ProvidesKey<PaymentOwner>() {
     public Object getKey(PaymentOwner item) {
       // Always do a null check.
       return (item == null) ? null : item.getId();
     }
   };
 
   public PaymentOwnerListView() {
   }
 
   @Override
   protected ProvidesKey<PaymentOwner> getKeyProvider() {
     return KEY_PROVIDER;
   }
 
   @Override
   protected void initTableColumns(SingleSelectionModel<PaymentOwner> selectionModel,
       ListHandler<PaymentOwner> sortHandler) {
 
     // period du.
     Column<PaymentOwner, String> fromDateColumn = new Column<PaymentOwner, String>(new TextCell()) {
       @Override
       public String getValue(PaymentOwner object) {
         return UiHelper.displayDate(object.getPeriodStartDate());
       }
     };
 
     fromDateColumn.setSortable(true);
     sortHandler.setComparator(fromDateColumn, new SimpleTextComparator<PaymentOwner>() {
       public int compare(PaymentOwner o1, PaymentOwner o2) {
        return compare(UiHelper.displayDate(o1.getPeriodStartDate()), UiHelper.displayDate(o2.getPeriodStartDate()));
       }
     });
 
     table.addColumn(fromDateColumn, "Du");
 
     // period au.
     Column<PaymentOwner, String> toDateColumn = new Column<PaymentOwner, String>(new TextCell()) {
       @Override
       public String getValue(PaymentOwner object) {
         return UiHelper.displayDate(object.getPeriodEndDate());
       }
     };
 
     toDateColumn.setSortable(true);
     sortHandler.setComparator(toDateColumn, new SimpleTextComparator<PaymentOwner>() {
       public int compare(PaymentOwner o1, PaymentOwner o2) {
        return compare(UiHelper.displayDate(o1.getPeriodEndDate()), UiHelper.displayDate(o2.getPeriodEndDate()));
       }
     });
 
     table.addColumn(toDateColumn, "Au");
     
     // Montant.
     Column<PaymentOwner, String> amountColumn = new Column<PaymentOwner, String>(new TextCell()) {
       @Override
       public String getValue(PaymentOwner object) {
         return UiHelper.FloatToString(object.getAmount());
       }
     };
 
     amountColumn.setSortable(true);
     sortHandler.setComparator(amountColumn, new SimpleTextComparator<PaymentOwner>() {
       public int compare(PaymentOwner o1, PaymentOwner o2) {
         return compare(UiHelper.FloatToString(o1.getAmount()),UiHelper.FloatToString(o2.getAmount()));
       }
     });
 
     table.addColumn(amountColumn, "Montant");
     
     // Date.
     Column<PaymentOwner, String> dateColumn = new Column<PaymentOwner, String>(new TextCell()) {
       @Override
       public String getValue(PaymentOwner object) {
         return UiHelper.displayDate(object.getPaymentDate());
       }
     };
 
     dateColumn.setSortable(true);
     sortHandler.setComparator(dateColumn, new SimpleTextComparator<PaymentOwner>() {
       public int compare(PaymentOwner o1, PaymentOwner o2) {
        return compare(UiHelper.displayDate(o1.getPaymentDate()), UiHelper.displayDate(o2.getPaymentDate()));
       }
     });
 
     table.addColumn(dateColumn, "Date paiement");
 
   }
   
   @Override
   protected String getButtonNewLabel() {
     return "Nouveau paiement";
   }
   @Override
   protected String getButtonBackLabel() {
     return "Retour à la location";
   }
 }
