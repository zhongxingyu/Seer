 package polly.core.roles;
 
import java.io.Serializable;

 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 @Entity
 @NamedQueries({
     @NamedQuery(name = "ALL_PERMISSIONS", query = "SELECT p FROM Permission p"),
     @NamedQuery(name = "PERMISSION_BY_NAME", query = "SELECT p FROM Permission p WHERE p.name = ?1")
 })
public class Permission implements Serializable {
     
    private static final long serialVersionUID = 1L;

     public final static String ALL_PERMISSIONS = "ALL_PERMISSIONS";
     
     public final static String PERMISSION_BY_NAME = "PERMISSION_BY_NAME";
 
     @Id@GeneratedValue(strategy = GenerationType.TABLE)
     private int id;
     
     private String name;
     
     
     public Permission() {}
     
     public Permission(String name) {
         this.name = name;
     }
     
     
     
     public int getId() {
         return this.id;
     }
     
     
     
     public String getName() {
         return this.name;
     }
 
     
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + this.id;
         result = prime * result
             + ((this.name == null) ? 0 : this.name.hashCode());
         return result;
     }
 
     
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Permission)) {
             return false;
         }
         Permission other = (Permission) obj;
         if (this.id != other.id) {
             return false;
         }
         if (this.name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
     
     
     
     @Override
     public String toString() {
         return "(" + this.id + ") " + this.name;
     }
 }
