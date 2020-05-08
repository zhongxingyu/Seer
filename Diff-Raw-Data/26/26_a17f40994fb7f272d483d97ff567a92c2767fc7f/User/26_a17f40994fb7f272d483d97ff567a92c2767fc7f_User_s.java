 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.UUID;
 
 
 /**
  *
  * @author Saad Ahmed
  */
 
 @Entity
 @XmlAccessorType(XmlAccessType.PROPERTY)
 @Table(name="users")
 public class User extends Model {
 
     @Id
     public String uuid;
     public String email;
     public String firstName;
     public String lastName;
     public String openId;
 
 

     public User(String uuid, String email, String firstName, String lastName, String openId) {
         this.uuid = uuid;
         this.email = email;
         this.firstName = firstName;
         this.lastName = lastName;
         this.openId = openId;
     }
 
 
     public static Finder<String, User> find() {
         return new Finder<String, User>(String.class, User.class);
     }
 
 }
