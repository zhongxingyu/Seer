 package edu.chl.dat076.foodfeed.model.entity;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 @Entity
 public class Grocery implements IEntity<String>, Serializable {
         
     @Id
     @NotNull
     @Size(min=1, message="Needs to be at least one character long.")
     private String id;
         
     private String description;
 
     public Grocery() {
     
     }
 
    public Grocery(String id, String description) {
        this.id = id;
         this.description = description;
     }
 
     @Override
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Override
     public String toString() {
         return this.id;
     }
 }
