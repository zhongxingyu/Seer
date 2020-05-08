 package eu.comexis.napoleon.client.core.tenant;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 
 import eu.comexis.napoleon.client.core.AbstractListView;
 import eu.comexis.napoleon.client.utils.SimpleTextComparator;
 import eu.comexis.napoleon.shared.model.simple.SimpleTenant;
 
 public class TenantListView extends AbstractListView<SimpleTenant> implements
     TenantListPresenter.MyView {
 
   // key provider object implementation for SimpleTenant object
   private static final ProvidesKey<SimpleTenant> KEY_PROVIDER = new ProvidesKey<SimpleTenant>() {
     public Object getKey(SimpleTenant item) {
       // Always do a null check.
       return (item == null) ? null : item.getId();
     }
   };
 
   @Inject
   public TenantListView() {
   }
 
   @Override
   protected ProvidesKey<SimpleTenant> getKeyProvider() {
     return KEY_PROVIDER;
   }
 
   protected void initTableColumns(SingleSelectionModel<SimpleTenant> selectionModel,
       ListHandler<SimpleTenant> sortHandler) {
 
     // Name.
     Column<SimpleTenant, String> nameColumn = new Column<SimpleTenant, String>(new TextCell()) {
       @Override
       public String getValue(SimpleTenant object) {
         return object.getName();
       }
     };
 
     nameColumn.setSortable(true);
     sortHandler.setComparator(nameColumn, new SimpleTextComparator<SimpleTenant>() {
       public int compare(SimpleTenant o1, SimpleTenant o2) {
         return compare(o1.getName(), o2.getName());
       }
     });
 
     table.addColumn(nameColumn, "Nom");
 
     // address.
     Column<SimpleTenant, String> addressColumn = new Column<SimpleTenant, String>(new TextCell()) {
       @Override
       public String getValue(SimpleTenant object) {
         return object.getAddress();
       }
     };
 
     addressColumn.setSortable(true);
     sortHandler.setComparator(addressColumn, new SimpleTextComparator<SimpleTenant>() {
       public int compare(SimpleTenant o1, SimpleTenant o2) {
         return compare(o1.getAddress(), o2.getAddress());
       }
     });
 
    table.addColumn(addressColumn, "Adresse");
 
     // Postal Code.
     Column<SimpleTenant, String> cpColumn = new Column<SimpleTenant, String>(new TextCell()) {
       @Override
       public String getValue(SimpleTenant object) {
         return object.getPostalCode();
       }
     };
 
     cpColumn.setSortable(true);
 
     sortHandler.setComparator(cpColumn, new SimpleTextComparator<SimpleTenant>() {
       public int compare(SimpleTenant o1, SimpleTenant o2) {
         return compare(o1.getPostalCode(), o2.getPostalCode());
       }
     });
     table.addColumn(cpColumn, "Code Postal");
     table.setColumnWidth(cpColumn, "20%");
 
     // City
     Column<SimpleTenant, String> cityColumn = new Column<SimpleTenant, String>(new TextCell()) {
       @Override
       public String getValue(SimpleTenant object) {
         return object.getCity();
       }
     };
 
     cityColumn.setSortable(true);
     sortHandler.setComparator(cityColumn, new SimpleTextComparator<SimpleTenant>() {
       public int compare(SimpleTenant o1, SimpleTenant o2) {
         return compare(o1.getCity(), o2.getCity());
       }
     });
     table.addColumn(cityColumn, "Localité");
 
     // tel
     Column<SimpleTenant, String> telColumn = new Column<SimpleTenant, String>(new TextCell()) {
       @Override
       public String getValue(SimpleTenant object) {
         return object.getPhoneNumber();
       }
     };
 
     telColumn.setSortable(true);
     sortHandler.setComparator(telColumn, new SimpleTextComparator<SimpleTenant>() {
       public int compare(SimpleTenant o1, SimpleTenant o2) {
         return compare(o1.getPhoneNumber(), o2.getPhoneNumber());
       }
     });
     table.addColumn(telColumn, "Téléphone");
 
     // Mobile
     Column<SimpleTenant, String> mobileColumn = new Column<SimpleTenant, String>(new TextCell()) {
       @Override
       public String getValue(SimpleTenant object) {
         return object.getMobileNumber();
       }
     };
 
     mobileColumn.setSortable(true);
     sortHandler.setComparator(mobileColumn, new SimpleTextComparator<SimpleTenant>() {
       public int compare(SimpleTenant o1, SimpleTenant o2) {
         return compare(o1.getMobileNumber(), o2.getMobileNumber());
       }
     });
     table.addColumn(mobileColumn, "Mobile");
 
   }
 
   @Override
   protected String getButtonNewLabel() {
     return "Nouveau locataire";
   }
   @Override
   protected String getButtonBackLabel() {
     return "Retour vers la location";
   }
 }
