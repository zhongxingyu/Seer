 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import play.db.jpa.Model;
 
 @Entity
 public class Item extends Model {
  
     public String name;
     public Float price;
 
     @ManyToOne
     public Category category;
 
     public Item(String name, Float price, Category category){
         this.name = name;
         this.price = price;
         this.category = category;
     }
 }
