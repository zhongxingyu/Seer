 package mediastore;
 
 import java.util.LinkedList;
 
 /**
  * Encapsulates a single customer.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  */
 public class Customer {
 
     private int id;
     private String name;
     private String address;
     private double credits;
     private LinkedList<Purchase> purchaseHistory;
     private Database db; // the Database this instance is a member of
 
     public Customer() {
        // TODO: default values
     }
 
     public Customer( int id, String name, String address, double credits, LinkedList purchaseHistory, Database db ) {
         this.id = id;
         this.name = name;
         this.address = address;
         this.credits = credits;
         this.db = db;
         this.purchaseHistory = purchaseHistory;
     }
 
     public void Buy( int id ) {
         Media object = db.getFromID( id );
         double price = object.getPrice();
         if ( credits < price ) {
             // not enough money
             return;
         }
         credits -= price;
 
         Purchase purchase = new Purchase( id, price, System.currentTimeMillis() );
         db.writeCustomerPurchase( id, purchase );
         purchaseHistory.add( purchase );
     }
 
     public void Search() {
     }
 }
