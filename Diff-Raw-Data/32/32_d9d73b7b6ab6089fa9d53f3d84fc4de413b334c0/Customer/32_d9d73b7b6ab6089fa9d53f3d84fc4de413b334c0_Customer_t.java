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
         // TODO: Unsure of default values check for validity.
         credits = 0;
         name = "";
         address = "";
         id = 0; // Not sure how long each Id should be. Correct later to correct length
         purchaseHistory = null;
         db = null;

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
        db.writeCustomerPurchase( this, purchase );
         purchaseHistory.add( purchase );

         // recalculate ranking
        for ( Media m : db.media ) {
         }
     }
 
     public Media Search( String query ) {
         Media media = null;
        for ( Media m : db.media ) {
            if ( m.title.equals( query ) ) {
                 media = m;
             }
         }
         return media;
     }

    public String getName() {
         return name;
     }

    public int getID() {
         return id;
     }

    public void setDB( Database db ) {
         this.db = db;
     }
 }
