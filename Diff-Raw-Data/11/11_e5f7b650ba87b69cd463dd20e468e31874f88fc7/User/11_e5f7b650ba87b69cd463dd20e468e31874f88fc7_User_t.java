 package Entity;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 import Crypto.Encryption;
 
 @Entity
 @Table(name = "users")
 @NamedQueries( {
         @NamedQuery(name = User.USER_NAME_QUERY,
                 query = "SELECT u from User u WHERE u.name = :name"),
         @NamedQuery(name = User.ALL_USERS_QUERY,
                 query = "SELECT u from User u")})
 public class User implements Serializable {
 
    public static final String USER_NAME_QUERY = "Entity.User.USER_NAME_QUERY";
    public static final String ALL_USERS_QUERY = "Entity.User.ALL_USERS_QUERY";
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "user_id", unique = true, nullable = false)
     private int userId;
 
     @Column(name = "name", nullable = false, length = 64)
     private String name;
 
     @Column(name = "password", nullable = false, length = 64)
     private String password;
 
     //bi-directional many-to-many association to Group
     @ManyToMany(cascade = {CascadeType.ALL})
     @JoinTable(name = "users_groups",
             joinColumns = {@JoinColumn(name = "user_id", nullable = false)},
             inverseJoinColumns = {@JoinColumn(name = "group_id", nullable = false)})
     private Set<Group> groups;
 
     public int getUserId() {
         return this.userId;
     }
 
     public void setUserId(int userId) {
         this.userId = userId;
     }
 
     public void setClearpassword(String clearpassword) {
         String hashed = Encryption.digest(clearpassword);
         this.setPassword(hashed);
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Set<Group> getGroups() {
         return this.groups;
     }
 
     public void setGroups(Set<Group> groups) {
         this.groups = groups;
     }
 }
