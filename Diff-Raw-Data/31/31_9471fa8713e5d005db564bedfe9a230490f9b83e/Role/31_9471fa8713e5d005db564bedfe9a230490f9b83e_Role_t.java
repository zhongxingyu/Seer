 package polly.core.roles;
 
import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 
 
 @Entity
 @NamedQueries({
     @NamedQuery(name = "ALL_ROLES", query = "SELECT r FROM Role r"),
     @NamedQuery(name = "ROLE_BY_NAME", query = "SELECT r FROM Role r WHERE r.name = ?1")
 })
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

     public final static String ROLE_BY_NAME = "ROLE_BY_NAME"; 
     
     public final static String ALL_ROLES = "ALL_ROLES";
 
     @Id@GeneratedValue(strategy=GenerationType.TABLE)
     private int id;
     
     private String name;
     
     @OneToMany
     private Set<Permission> permissions;
     
     @Transient
     private Set<String> permissionNames;
     
     @Transient
     private boolean stale;
     
     
     public Role() {
         this.permissions = new HashSet<Permission>();
     }
     
     
     
     public Role(String name) {
         this(name, new HashSet<Permission>());
     }
     
     
     
     public Role(String name, Set<Permission> permissions) {
         this.name = name;
         this.permissions = permissions;
     }
     
     
     
     
     public int getId() {
         return this.id;
     }
     
     
     
     
     public String getName() {
         return this.name;
     }
     
     
     
     public Set<Permission> getPermissions() {
         return this.permissions;
     }
     
     
     
     
     public Set<String> getPermissionNames() {
         if (this.permissionNames == null || this.stale) {
             this.permissionNames = new HashSet<String>(this.permissions.size());
             for (Permission perm : this.permissions) {
                 this.permissionNames.add(perm.getName());
             }
             this.permissionNames = Collections.unmodifiableSet(this.permissionNames);
             this.stale = false;
         }
         return this.permissionNames;
     }
     
     
     
     public void setStale(boolean stale) {
         this.stale = stale;
     }
 
 
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
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
         if (!(obj instanceof Role)) {
             return false;
         }
         Role other = (Role) obj;
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
