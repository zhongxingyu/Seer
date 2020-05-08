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
@Table(name = "user")
 public class User implements Serializable{
     
     @Id
     @GeneratedValue
     private long id;
 
     public User() {
     }
 
     
     
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
     
     
     
 }
