 package models;
 
 import java.util.List;
 import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 /**
  *
  * @author Maxim Kolchin
  */
 @Entity
 public class User extends Model {
     
     @Email
     @Required
     public String email;
     
     @Required
     public String password;
     public String fullName;
     
    @ManyToMany
    @JoinTable(
        name="User_Role",
        joinColumns=
            @JoinColumn(name="user_id", referencedColumnName="id"),
        inverseJoinColumns=
            @JoinColumn(name="role_id", referencedColumnName="id")
    )
     public List<Role> roles;
     
     public User(String email, String password, String fullName) {
         this.email = email;
         this.password = password;
         this.fullName = fullName;
     }
     
     public static User connect(String email, String password) {
         return find("byEmailAndPassword", email, password).first();
     }
 
     @Override
     public String toString() {
         return email;
     }
     
 }
