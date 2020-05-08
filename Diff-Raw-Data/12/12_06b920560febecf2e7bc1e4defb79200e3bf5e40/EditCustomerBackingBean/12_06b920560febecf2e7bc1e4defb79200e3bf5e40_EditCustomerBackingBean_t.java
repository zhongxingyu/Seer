 package backingbeans;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author thituson
  */
 import core.Address;
 import java.io.Serializable;
 import javax.enterprise.context.*;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 @Named
@SessionScoped
 public class EditCustomerBackingBean implements Serializable{
     
     @Inject // Handled by system, don't need to create class.
     private Conversation conv;
 
     @NotNull
     private Long id;
     @NotNull
     @Pattern(regexp = "^[a-zA-Z]*")
     @Size(min = 1, message = "Must enter a name")
     private  String fname;
     @Pattern(regexp = "^[a-zA-Z]*")
     @Size(min = 1, message = "Must enter a name")
     private  String lname;
     private  String email;
     private  Address address;
     private String userName;
     private String password;
     
     public EditCustomerBackingBean(){}
         
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getFname() {
         return fname;
     }
 
     public void setFname(String fname) {
         this.fname = fname;
     }
 
     public String getLname() {
         return lname;
     }
 
     public void setLname(String lname) {
         this.lname = lname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public Address getAddress() {
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }  
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
     
 }
