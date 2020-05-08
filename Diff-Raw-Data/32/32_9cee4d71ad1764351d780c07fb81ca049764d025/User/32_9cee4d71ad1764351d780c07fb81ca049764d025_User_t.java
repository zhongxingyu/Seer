 package wad.tukki.models;
 
 import java.util.List;
import org.hibernate.validator.constraints.NotBlank;
 import org.springframework.data.mongodb.core.mapping.DBRef;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 @Document
 public class User extends MongoObject {
     
    @NotBlank(message = "Username may not be blank.")
     private String username;
    
    @NotBlank(message = "Password may not be blank.")
     private String password;
     
     @DBRef
     private List<UserRole> roles;
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public List<UserRole> getRoles() {
         return roles;
     }
 
     public void setRoles(List<UserRole> roles) {
         this.roles = roles;
     }
     
     public void addRole(UserRole role) {
         
         if (roles.contains(role)) {
             return;
         }
         
         roles.add(role);
     }
 
     @Override
     public boolean equals(Object obj) {
         
         if (obj == null) {
             return false;
         }
         
         if (getClass() != obj.getClass()) {
             return false;
         }
         
         final User other = (User) obj;
         if ((this.getId() == null) ? (other.getId() != null) : !this.getId().equals(other.getId())) {
             return false;
         }
         
         return true;
     }
 
     @Override
     public int hashCode() {
         
         int hash = 7;
         hash = 89 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
         
         return hash;
     }
 }
