 package models;
 
import java.util.List;
 import javax.persistence.Entity;
import javax.persistence.ManyToMany;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 /**
  *
  * @author Maxim Kolchin
  */
 @Entity
 public class Role extends Model {
     
     @Required
     public String role;
     
    @ManyToMany(mappedBy="roles")
    public List<User> users;
    
     public Role(final String r) {
         this.role = r;
     }
 
     @Override
     public String toString() {
         return this.role;
     }
     
 }
