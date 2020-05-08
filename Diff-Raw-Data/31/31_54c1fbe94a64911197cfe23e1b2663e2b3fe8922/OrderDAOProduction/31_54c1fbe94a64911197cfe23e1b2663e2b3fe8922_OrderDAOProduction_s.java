 package model;
 
 import db.accessor.DBAccessor;
 import db.accessor.DB_Generic;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Use this version to connect to WCTC's production database.
  * @author jlombardo
  */
 public class OrderDAOProduction implements IOrderDAO {
     private DBAccessor db;
     private static final String DRIVER = "com.mysql.jdbc.Driver";
     private static final String URL = 
            "jdbc:mysql://localhost:3306/RestaurantOrderingSystem";
     private static final String USER = "root";
    private static final String PWD = "root";
     
     public OrderDAOProduction() {
         db = new DB_Generic();
     }
     
     @Override
     public List<Item> getCurrentMenuChoices() throws RuntimeException {
         List<Item> items = new ArrayList<Item>();
         
         try {
             // Make sure you always open a connection before trying to
             // send commands to the database.
             db.openConnection(OrderDAOProduction.DRIVER,OrderDAOProduction.URL,
                     OrderDAOProduction.USER, OrderDAOProduction.PWD);
             
            String sql = "select * from menu_item order by item_name";
             
             // Usuallly you want the connection to be closed when the db
             // method finishes (second parameter = true). The reason is that
             // if you leave it open (second parameter = false) then you risk
             // the database connection might time out and close on its own.
             List<Map> rawData = db.findRecords(sql, true);
             for(Map record : rawData) {
                 Item item = new Item();
                int id = Integer.valueOf(record.get("menu_item_id").toString());
                 item.setId(id);
                 String name = String.valueOf(record.get("item_name"));
                 item.setName(name);
                Double price = Double.valueOf(record.get("item_price").toString());
                 item.setPrice(price);
                 items.add(item); 
             }
             
             return items;
             
         } catch (IllegalArgumentException ex) {
             throw new RuntimeException(ex.getMessage(), ex);
         } catch (ClassNotFoundException ex) {
             throw new RuntimeException(ex.getMessage(), ex);
         } catch (SQLException ex) {
             throw new RuntimeException(ex.getMessage(), ex);
         } catch(Exception ex) {
              throw new RuntimeException(ex.getMessage(), ex);
         }
     }
     
     
 }
