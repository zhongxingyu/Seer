 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Java;
 
 import DB.Database;
 import java.util.ArrayList;
 
 /**
  *
  * @author Rino
  */
 public class PendingOrders {
     private Database database = new Database();
     private ArrayList<Order> orders = new ArrayList();
     
     public PendingOrders(){
        String query = "Select * from ASD.ORDERS where STATUS !='"+Order.Status.FINISHED.getCode()+"'";
         orders = database.getPendingOrders(query);
     }
     public ArrayList<Order> getOrders(){
         return orders; 
     }
 }
