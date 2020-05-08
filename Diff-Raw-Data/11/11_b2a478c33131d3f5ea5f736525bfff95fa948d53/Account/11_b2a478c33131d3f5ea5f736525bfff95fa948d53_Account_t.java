 package au.com.bytecode.domain;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.ComparisonChain;
 import java.util.Date;
 
 /**
  * A basic account object with some common field types to allow
  * us to experiment our guava features.
  * 
  * @author Glen
  */
 public class Account implements Comparable<Account> {
     
     private String username;
     private String password;
     
     private String email;
     private Date dateCreated;
     private Date lastLogin;
     
     public Account() {
         dateCreated = new Date();
     }
 
     public Account(String username, String password, String email) {
         this.username = username;
         this.password = password;
         this.email = email;
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
 
     public Date getDateCreated() {
         return dateCreated;
     }
 
     public void setDateCreated(Date dateCreated) {
         this.dateCreated = dateCreated;
     }
 
     public Date getLastLogin() {
         return lastLogin;
     }
 
     public void setLastLogin(Date lastLogin) {
         this.lastLogin = lastLogin;
     }
 
     @Override
     public String toString() {
         
         return Objects.toStringHelper(this)
                 .add("name", username)
                 .add("email", email)
                 .add("created", dateCreated)
                 .add("lastLogon", lastLogin)
                 .toString();
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(username,email);
     }
 
     @Override
     public boolean equals(Object obj) {
         
        if (this == obj) {
             return true;
        }
         
        if (!(obj instanceof Account)) {
             return false;
        }
    
         Account other = (Account) obj;
 
         return Objects.equal(username, other.getUsername()) &&
                 Objects.equal(email, other.getEmail());
     }
 
     @Override
     public int compareTo(Account o) {
     
         return ComparisonChain.start()
                 .compare(username, o.username)
                 .compare(email, o.email)
                 .result();
         
     }
     
     
     
     
 }
