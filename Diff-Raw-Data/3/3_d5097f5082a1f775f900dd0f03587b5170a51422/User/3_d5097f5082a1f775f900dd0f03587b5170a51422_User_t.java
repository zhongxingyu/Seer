 package models.cms;
 
 import java.util.List;
import java.util.ArrayList;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import play.db.jpa.Model;
 
 /**
  * @author benoit
  */
 @Entity
 @Table(name="cms_user")
 public class User extends Model {
 
     @Column(nullable=false,length=64)
     public String firstName;
 
     @Column(nullable=false,length=64)
     public String lastName;
 
     @Column(length=128, nullable=false,unique=true)
     public String mail;
 
     @Column(length=16, nullable=false)
     public String password;
 
     @JoinTable(name="cms_user_role")
     @ManyToMany
     public List<Role> roles;
 
     public boolean hasRole(String... roleNames) {
 
         if (roleNames == null){return false;}
 
         int          nbRoles = roleNames.length;
         List<String> names   = new ArrayList<String>(nbRoles);
 
         for (int i = 0; i < nbRoles; i++) {
             
             names.add(roleNames[i]);
         }
 
         return hasRole(names);
    }
 
    public boolean hasRole(List<String> roleNames) {
 
        for (Role role : this.roles) {
 
            if (roleNames.contains(role.name)) {
 
                return true;
            }
        }
 
        return false;
    }
 }
