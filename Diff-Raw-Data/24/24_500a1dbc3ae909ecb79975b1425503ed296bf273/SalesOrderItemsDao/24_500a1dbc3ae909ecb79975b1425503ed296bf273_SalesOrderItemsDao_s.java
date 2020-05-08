 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.DaoPersistence;
 
 import java.sql.SQLException;
 import restaurante.Beans.Saleable;
 import restaurante.Beans.SalesOrder;
 import restaurante.Beans.SalesOrderItems;
 import restaurante.Controller.MenuController;
 import restaurante.Controller.ProductsController;
 import restaurante.Utilities.ContentValues;
 
 /**
  *
  * @author Alan
  */
 public class SalesOrderItemsDao extends AbstractDao<SalesOrderItems> {
     private final static String TABLE = "salesorder_items";
     /**
      * "id_salesorder", "amount", "price", "type", "id_saleable"
      */
     private final static String[] FIELDS = new String[] 
         { "id_salesorder", "amount", "price", "type", "id_saleable" };
     
     public SalesOrderItemsDao() throws Exception { hasFieldId = false; }
     
     void saveItems(SalesOrder salesOrder) throws SQLException {
         for(SalesOrderItems item : salesOrder.getItems()) {
            insert(salesOrder.getId(), item.getSaleable().getId(), item.getPrice(), item.getAmount(), item.getSaleable().getType().ordinal());
         }
     }
 
     @Override
     protected SalesOrderItems toBean(ContentValues values) throws SQLException, ClassNotFoundException, Exception {
         SalesOrderItems item = new SalesOrderItems();
         Saleable.Type type = Saleable.Type.values()[values.getInt(FIELDS[3])];
         if(type == Saleable.Type.MENU) {
             item.setSaleable(new MenuController().findById(values.getInt(FIELDS[4])));
         } else {
             item.setSaleable(new ProductsController().findById(values.getInt(FIELDS[4])));
         }
         item.setPrice(values.getDouble(FIELDS[1]));
         item.setAmount(values.getDouble(FIELDS[2]));
         return item;
     }
 
     public void deleteAllFromSale(int salesOrderId) throws SQLException {
         this.delete(FIELDS[0] + " = " + salesOrderId);
     }
     
     @Override
     protected String getTableName() {
         return TABLE;
     }
 
     @Override
     protected String[] getFields() {
         return FIELDS;
     }
 
     SalesOrderItems[] findProducts(int saleOrderId) throws Exception {
         return selectBeans(FIELDS[0] + " = ?", saleOrderId);
     }
     
 }
