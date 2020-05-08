 package scrumurai.data.entities;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @Entity
 @Table(name = "tbl_user")
 @XmlRootElement
 public class User implements EntityObject {
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private int id;
     @NotNull
     private String username;
     @NotNull
     private String password;
     @NotNull
     private String email;
     private String firstname;
     private String lastname;
     private String number;
 
     public User() {
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public String getNumber() {
         return number;
     }
 
     public void setNumber(String number) {
         this.number = number;
     }
 
     public void setLastname(String lastname) {
         this.lastname = lastname;
     }
 
     @Override
     public String toString() {
         return "User{" + "id=" + id + ", username=" + username + ", password=" + password + ", email=" + email + ", firstname=" + firstname + ", lastname=" + lastname + ", number=" + number + '}';
     }
 
 }
