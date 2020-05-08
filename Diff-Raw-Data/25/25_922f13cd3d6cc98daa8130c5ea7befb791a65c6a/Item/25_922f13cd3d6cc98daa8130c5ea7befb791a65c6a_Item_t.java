 package models;
 
 import javax.persistence.Entity;
 import play.db.jpa.Model;
 
 @Entity
 public class Item extends Model {
  
     public String name;
     public Float price;
     public Enum type;
    //@ManyToOne
    //public Menu menu;

    public enum Type{
        MEAT, VEGETABLE, DESSERT
    }
     
     public Item(String name, Float price, Enum type){
         this.name = name;
         this.price = price;
         this.type = type;
     }
 }
