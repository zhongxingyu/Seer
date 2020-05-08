 package models;
  
 import java.util.*;
 import javax.persistence.*;
  
 import play.db.jpa.*;
  
 @Entity
public class Task extends Model {
     public String value;
     public Date createdAt;
     
     @ManyToOne
     public User user;
     
     public Task(User user, String value) {
         this.user = user;
         this.value = value;
         this.createdAt = new Date();
     }
 }
