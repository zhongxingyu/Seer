 package fr.midix.services.boireverre.model;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author midix
  */
 @Entity
@Table(name = "Place")
 public class Place implements Serializable{
     
     @Id
     @GeneratedValue
     private long id;
 
     public Place() {
     }
 
     
     
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
     
     
 }
