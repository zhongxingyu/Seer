 package models.com.nitro.activity.account.model;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.hibernate.annotations.GenericGenerator;
 
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.GenericModel;
 
@javax.persistence.Entity (name = "public.'Account'")
 public class Account extends GenericModel {
     
     private static final long serialVersionUID = -7335615128658466071L;
 
     @Id
     @GeneratedValue(generator = "id")
     @GenericGenerator(name = "id", strategy = "uuid")
     public String id;
     
     @Required
     @MaxSize(value=75, message = "email.maxsize")
     @play.data.validation.Email
     String username;
     
     @Required
     String password;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
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
     
     
 
 }
