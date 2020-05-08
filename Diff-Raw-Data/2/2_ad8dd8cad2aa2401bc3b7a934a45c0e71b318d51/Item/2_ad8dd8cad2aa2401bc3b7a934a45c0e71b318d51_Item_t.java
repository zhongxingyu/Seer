 package functionalTestingProject.domain;
 
 import lombok.Setter;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 @Setter
 @Entity
 @Table(name = "ITEMS")
 public class Item implements Serializable {
     public static String NAME_PROPERTY = "name";
     public static String ID = "id";
 
     @GeneratedValue
     @Id
     private int id;
 
     @Column
     private String name;
 
     @Column
     private double price;
 
     @Column
     private double tax;
 
     @Deprecated
     public Item() {}
 
     public Item(int id, String name, double price, double tax) {
         this.id = id;
         this.name = name;
         this.price = price;
         this.tax = tax;
     }
 
     public String getName() {
         return name;
     }
 
     public String getId() {
         return Integer.toString(id);
     }
 
     public String getPrice() {
         return Integer.toString((int) price);
     }
 
     public String getTax() {
        return Double.toString(tax);
     }
 
     public String asJson() {
         return "{\"price\": \"" + price +"\"," +
                 "\"tax\": \"" + tax + "\"}";
     }
 }
