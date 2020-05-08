 package net.devmask.tuit.models;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonFilter;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author <a href="mailto:jonathan@devmask.net"> Jonathan Garay </a>
  *         07/02/12 Creado
  */
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "email" }),name="users")
 @NamedQueries({
     @NamedQuery(name = "user.find.to_login",
                 query = "SELECT u FROM User as u where u.username = :username AND u.password = :password"),
        @NamedQuery(name = "user.by_",
                 query = "SELECT u FROM User as u where u.username = :username AND u.password = :password")
 })
 public class User implements Serializable{
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
 
 
     @NotNull(message = "Username cant be blank")
     private String username;
 
     @NotNull(message = "Email cant be blank")
     private String email;
 
     @NotNull(message = "Password cant be blank")
     @Size(min = 6, message = "The password must be greeter that 6")
     @JsonIgnore
     private String password;
 
     @OneToMany
     @JsonIgnore
     private List<User> followers;
 
     @OneToMany
     @JsonIgnore
     private List<User> following;
 
     @OneToMany(mappedBy = "user")
     @JsonIgnore
     private List<Tuit> tuits;
     
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public List<User> getFollowers() {
         return followers;
     }
 
     public void setFollowers(List<User> followers) {
         this.followers = followers;
     }
 
     public List<User> getFollowing() {
         return following;
     }
 
     public void setFollowing(List<User> following) {
         this.following = following;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public List<Tuit> getTuits() {
         return tuits;
     }
 
     public void setTuits(List<Tuit> tuits) {
         this.tuits = tuits;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 }
