 package no.nxc.bank.atm.client;
 
//import javax.xml.bind.annotation.XmlElement;
 import java.util.HashMap;
 
 
 public class Cash {
 
    private int rating;
    private int quantity;

    public Cash(int rating, int quantity){
        this.rating   = rating;
        this.quantity = quantity;
    }
 //
 //    public int getQuantity() {
 //        return quantity;
 //    }
 //    @XmlElement
 //    public void setQuantity(int quantity) {
 //        this.quantity = quantity;
 //    }
 //
 //    public int getRating() {
 //        return rating;
 //    }
 //    @XmlElement
 //    public void setRating(int rating) {
 //        this.rating = rating;
 //    }
     private HashMap<Integer, Integer> cash;
 
    public HashMap<Integer, Integer> getCash(){
         cash = new HashMap<Integer, Integer>();
         return cash;
     }
 
 
 
 
 }
