 package models;
 
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 
 /**
  * @author <a href="mailto:alexander.hanschke@gmail.com">Alexander Hanschke</a>
  */
 @Entity
 public class Authentication extends Model {
 
     @ManyToOne
     public User user;
 
    @Enumerated(EnumType.STRING)
     public AuthProvider provider;
     public String token;
 
 }
