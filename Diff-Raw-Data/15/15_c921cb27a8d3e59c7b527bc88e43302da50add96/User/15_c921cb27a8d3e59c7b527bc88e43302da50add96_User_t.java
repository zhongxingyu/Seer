 package com.supinfo.rmt.entity;
 
 import com.supinfo.rmt.validation.CheckPassword;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.util.Date;
 
 /**
  * @author Antoine Rouaze <antoine.rouaze@zenika.com>
  */
 @Entity
 @Table(name = "users")
 @DiscriminatorColumn(name = "type")
 @NamedQueries({
         @NamedQuery(name = "findUserByEmailAndPassword",
                 query = "SELECT user FROM User AS user " +
                         "WHERE user.username = :username AND user.password = :password")
 })
 public abstract class User {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
    @NotNull
     @NotEmpty
     @Size(min = 3, max = 10)
     private String username;
     @CheckPassword
     private String password;
    @NotNull
     @NotEmpty
     private String firstname;
    @NotNull
     @NotEmpty
     private String lastname;
     @Email
     private String email;
     @Temporal(TemporalType.DATE)
    @NotNull
     private Date dateOfBirth;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
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
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstName) {
         this.firstname = firstName;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public void setLastname(String lastName) {
         this.lastname = lastName;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public Date getDateOfBirth() {
         return dateOfBirth;
     }
 
     public void setDateOfBirth(Date dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         User user = (User) o;
 
         if (dateOfBirth != null ? !dateOfBirth.equals(user.dateOfBirth) : user.dateOfBirth != null) return false;
         if (email != null ? !email.equals(user.email) : user.email != null) return false;
         if (firstname != null ? !firstname.equals(user.firstname) : user.firstname != null) return false;
         if (id != null ? !id.equals(user.id) : user.id != null) return false;
         if (lastname != null ? !lastname.equals(user.lastname) : user.lastname != null) return false;
         if (password != null ? !password.equals(user.password) : user.password != null) return false;
         if (username != null ? !username.equals(user.username) : user.username != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (username != null ? username.hashCode() : 0);
         result = 31 * result + (password != null ? password.hashCode() : 0);
         result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
         result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
         result = 31 * result + (email != null ? email.hashCode() : 0);
         result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
         return result;
     }
 }
