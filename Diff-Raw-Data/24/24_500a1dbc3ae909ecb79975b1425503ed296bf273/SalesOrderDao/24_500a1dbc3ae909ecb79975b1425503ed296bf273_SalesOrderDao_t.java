 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.DaoPersistence;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import restaurante.Beans.SalesOrder;
 import restaurante.Utilities.ContentValues;
 import restaurante.Utilities.Functions;
 
 /**
  *
  * @author Alan
  */
 public class SalesOrderDao extends AbstractDao<SalesOrder> {
     public static final String TABLE_NAME = "salesorder";
     /**
      * "id_customer", "id_employees", "id_table", "id_branch", "date",
         "totalvalue", "numberpersons", "valueperperson", "payment", "done"
      */
     public static final String[] FIELDS = new String[] 
         { "id_customer", "id_employees", "id_table", "id_branch", "date",
         "totalvalue", "numberpersons", "valueperperson", "payment", "status" };
     
     public SalesOrderDao() throws ClassNotFoundException, SQLException {
     }
 
     /*
      * Insert the current Bean
      */
     public int insert(SalesOrder salesOrder) throws SQLException, Exception {
         int id = insert(salesOrder.getCustomer().getId(), salesOrder.getEmployee().getId(),
                 salesOrder.getTable().getTableNumber(), salesOrder.getBranch().getId(), 
                 Functions.dateToDateStringSql(salesOrder.getDate()), salesOrder.getTotalValue(), 
                 salesOrder.getNumberOfPersons(), salesOrder.getValueForPerson(),
                 salesOrder.getPaymentMethod().ordinal(), salesOrder.getStatus().ordinal());
         salesOrder.setId(id);
         saveItems(salesOrder);
         return salesOrder.getId();
     }
     
     /*
      * Update the current Bean
      */
     public void update(SalesOrder salesOrder) throws SQLException, ParseException, Exception {
         update(salesOrder.getId(), salesOrder.getCustomer().getId(), salesOrder.getEmployee().getId(),
                 salesOrder.getTable().getTableNumber(), salesOrder.getBranch().getId(), 
                 Functions.dateToDateStringSql(salesOrder.getDate()), salesOrder.getTotalValue(), 
                 salesOrder.getNumberOfPersons(), salesOrder.getValueForPerson(),
                salesOrder.getPaymentMethod().ordinal(), salesOrder.getStatus().ordinal());
         saveItems(salesOrder);
     }
     
     private void saveItems(SalesOrder salesOrder) throws SQLException, Exception {
         //Start the transaction to save batch of products from menu
         this.startTransation();
         SalesOrderItemsDao dao = new SalesOrderItemsDao();
         dao.deleteAllFromSale(salesOrder.getId());
         dao.saveItems(salesOrder);
         this.finishTransaction();
         //Remember to finish the transaction!
     }
     /*
      * Convert the ContentValues (database fields) to bean
      */
     @Override
     protected SalesOrder toBean(ContentValues values) throws SQLException, ClassNotFoundException, Exception {
         SalesOrder salesOrder = new SalesOrder();        
 
         salesOrder.setId(values.getInt(FIELD_ID));    
         salesOrder.setCustomer(new CustomersDao().findById(values.getInt(FIELDS[0])));
         salesOrder.setEmployee(new EmployeesDao().findById(values.getInt(FIELDS[1])));
         salesOrder.setTable(new TablesDao().findById(values.getInt(FIELDS[2])));
         salesOrder.setBranch(new BranchesDao().findById(values.getInt(FIELDS[3])));
         
         salesOrder.setDate(values.getDate(FIELDS[4]));
         salesOrder.setTotalValue(values.getDouble(FIELDS[5]));
         salesOrder.setNumberOfPersons(values.getInt(FIELDS[6]));
         salesOrder.setValueForPerson(values.getDouble(FIELDS[7]));
         salesOrder.setPaymentMethod(SalesOrder.PaymentMethod.values()[values.getInt(FIELDS[8])]);
         salesOrder.setStatus(SalesOrder.Status.values()[values.getInt(FIELDS[9])]);
         
         try {
             salesOrder.setItems(new SalesOrderItemsDao().findProducts(salesOrder.getId()));
         } catch (Exception e) {
         }
         return salesOrder;
     }
 
     /*
      * get the name of the table relative this DAO
      */
     @Override
     protected String getTableName() {
         return TABLE_NAME;
     }
 
     /*
      * get the name of the fields relative this DAO
      */
     @Override
     protected String[] getFields() {
         return FIELDS;
     }
 }
