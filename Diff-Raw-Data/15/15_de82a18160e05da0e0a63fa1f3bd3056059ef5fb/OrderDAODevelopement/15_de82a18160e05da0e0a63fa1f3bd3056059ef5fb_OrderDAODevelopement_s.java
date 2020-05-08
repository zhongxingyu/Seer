 
 package edu.wctc.distjava.ch3.model;
 
 import db.accessor.DBAccessor;
 import db.accessor.DB_Generic;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tmueller2
  * @version 1.0
  * This class is used to access the production DB
  */
 public class OrderDAODevelopement implements IOrderDAO {
     private DBAccessor db;
     private static final String DRIVER = "com.mysql.jdbc.Driver";
     private static final String URL = 
             "jdbc:mysql://localhost:3306/restaurantdb";
     private static final String USER = "root";
     private static final String PWD = "admin";
     private static int orderCount = 0;    
     
     public OrderDAODevelopement(){
         db = new DB_Generic();
     }
 
     /**
      * This class gets the menu choices from the data base
      * @return
      * @throws RuntimeException 
      */
     @Override
     public List<MenuItem> getCurrentMenuChoices() throws RuntimeException {
         List<MenuItem> items = new ArrayList<MenuItem>();
         try {
             //Open a connection to the data base
             db.openConnection(OrderDAODevelopement.DRIVER,OrderDAODevelopement.URL,
                     OrderDAODevelopement.USER, OrderDAODevelopement.PWD);
             String sql = "select * from menuitems order by menuDescription";
             List<Map> rawData = db.findRecords(sql, true);
             for(Map record : rawData) {
                 MenuItem item = new MenuItem();
                 int id = Integer.valueOf(record.get("idmenuItems").toString());
                 item.setId(id);
                 String description = String.valueOf(record.get("menuDescription"));
                 item.setMenuDescription(description);
                 items.add(item); 
             }            
             
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(OrderDAODevelopement.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(OrderDAODevelopement.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(OrderDAODevelopement.class.getName()).log(Level.SEVERE, null, ex);
         }catch (Exception ex) {
             Logger.getLogger(OrderDAODevelopement.class.getName()).log(Level.SEVERE, null, ex);
         } 
        return null;
     }
 
     /**
     @Override
     public void saveOrder(List<MenuItem> orderList) throws RuntimeException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     */
 
     @Override
     public void saveOrder(List<MenuItem> orderList) throws RuntimeException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 
     
 }
