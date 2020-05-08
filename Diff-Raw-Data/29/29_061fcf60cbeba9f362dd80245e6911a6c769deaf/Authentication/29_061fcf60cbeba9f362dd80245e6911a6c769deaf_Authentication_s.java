 package models;
 
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 /**
  * @author <a href="mailto:alexander.hanschke@gmail.com">Alexander Hanschke</a>
  */
 @Entity
 public class Authentication extends Model {
 
     @ManyToOne
     public User user;
 
     public AuthProvider provider;
     public String token;
 
 }
