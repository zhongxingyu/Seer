 package eu.comexis.napoleon.client.core.paymentTenant;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import eu.comexis.napoleon.client.core.AbstractListView;
 import eu.comexis.napoleon.client.utils.SimpleTextComparator;
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.PaymentTenant;
 
 public class PaymentTenantListView extends AbstractListView<PaymentTenant> implements
   PaymentTenantListPresenter.MyView {
 
   // key provider object implementation for SimpleLease object
   private static final ProvidesKey<PaymentTenant> KEY_PROVIDER = new ProvidesKey<PaymentTenant>() {
     public Object getKey(PaymentTenant item) {
       // Always do a null check.
       return (item == null) ? null : item.getId();
     }
   };
 
   public PaymentTenantListView() {
   }
 
   @Override
   protected ProvidesKey<PaymentTenant> getKeyProvider() {
     return KEY_PROVIDER;
   }
 
   @Override
   protected void initTableColumns(SingleSelectionModel<PaymentTenant> selectionModel,
       ListHandler<PaymentTenant> sortHandler) {
 
     // period du.
     Column<PaymentTenant, String> fromDateColumn = new Column<PaymentTenant, String>(new TextCell()) {
       @Override
       public String getValue(PaymentTenant object) {
         return UiHelper.displayDate(object.getPeriodStartDate());
       }
     };
 
     fromDateColumn.setSortable(true);
     sortHandler.setComparator(fromDateColumn, new SimpleTextComparator<PaymentTenant>() {
       public int compare(PaymentTenant o1, PaymentTenant o2) {
        return compare(UiHelper.formatDateForCompare(o1.getPeriodStartDate()), UiHelper.formatDateForCompare(o2.getPeriodStartDate()));
       }
     });
 
     table.addColumn(fromDateColumn, "Du");
 
     // period au.
     Column<PaymentTenant, String> toDateColumn = new Column<PaymentTenant, String>(new TextCell()) {
       @Override
       public String getValue(PaymentTenant object) {
         return UiHelper.displayDate(object.getPeriodEndDate());
       }
     };
 
     toDateColumn.setSortable(true);
     sortHandler.setComparator(toDateColumn, new SimpleTextComparator<PaymentTenant>() {
       public int compare(PaymentTenant o1, PaymentTenant o2) {
        return compare(UiHelper.formatDateForCompare(o1.getPeriodEndDate()), UiHelper.formatDateForCompare(o2.getPeriodEndDate()));
       }
     });
 
     table.addColumn(toDateColumn, "Au");
     
     // Montant.
     Column<PaymentTenant, String> amountColumn = new Column<PaymentTenant, String>(new TextCell()) {
       @Override
       public String getValue(PaymentTenant object) {
         return UiHelper.FloatToString(object.getAmount());
       }
     };
 
     amountColumn.setSortable(true);
     sortHandler.setComparator(amountColumn, new SimpleTextComparator<PaymentTenant>() {
       public int compare(PaymentTenant o1, PaymentTenant o2) {
         return compare(UiHelper.FloatToString(o1.getAmount()),UiHelper.FloatToString(o2.getAmount()));
       }
     });
 
     table.addColumn(amountColumn, "Montant");
     
     // Date.
     Column<PaymentTenant, String> dateColumn = new Column<PaymentTenant, String>(new TextCell()) {
       @Override
       public String getValue(PaymentTenant object) {
         return UiHelper.displayDate(object.getPaymentDate());
       }
     };
 
     dateColumn.setSortable(true);
     sortHandler.setComparator(dateColumn, new SimpleTextComparator<PaymentTenant>() {
       public int compare(PaymentTenant o1, PaymentTenant o2) {
        return compare(UiHelper.formatDateForCompare(o1.getPaymentDate()), UiHelper.formatDateForCompare(o2.getPaymentDate()));
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
     return "Retour vers la location";
   }
 }
