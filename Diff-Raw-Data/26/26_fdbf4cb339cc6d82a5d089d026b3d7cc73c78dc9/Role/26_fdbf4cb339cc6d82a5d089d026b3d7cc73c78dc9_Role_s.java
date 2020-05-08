 package models;
 
 import javax.persistence.Entity;
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
     
     public Role(final String r) {
         this.role = r;
     }
 
     @Override
     public String toString() {
         return this.role;
     }
     
 }
