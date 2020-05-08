 package be.kdg.groeph.bean;
 
 import be.kdg.groeph.model.Address;
 import be.kdg.groeph.model.User;
 import be.kdg.groeph.service.UserService;
 import be.kdg.groeph.util.SHAEncryption;
 import org.apache.log4j.Logger;
 import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.Conversation;
 import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.Column;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import java.io.Serializable;
 import java.text.ParseException;
 import java.util.Date;
 
 /**
  * To change this template use File | Settings | File Templates.
  */
 @Component
 @ViewAccessScoped
 @Named
 public class UserBean implements Serializable {
     static Logger logger = Logger.getLogger(UserBean.class);
 
     private static final String SUCCESS = "SUCCESS";
     private static final String FAILURE = "FAILURE";
 
     @ManagedProperty(value="#{userService}")
     @Autowired
     UserService userService;
 
     @NotEmpty(message = "{firstName} {notempty}")
     @Length(max=50, message = "{firstName} {length}")
     private String firstName;
     @NotEmpty(message = "{lastName} {notempty}")
     @Length(max=50, message = "{lastName} {length}")
     private String lastName;
     @NotNull(message = "{dateOfBirth} {notempty}")
     @Past(message = "{dateOfBirth} {past}")
     private Date dateOfBirth;
     @Length(max=30, message = "{phoneNumber} {length}")
    @NotEmpty(message = "{phoneNumber} {notempty}")
     private String phoneNumber;
     private char gender;
     @NotEmpty(message = "{email} {notempty}")
     @Email(message = "{email} {validEmail}")
     @Length(max=100, message = "{email} {length}")
     private String email;
     @NotEmpty(message = "{password} {notempty}")
     private String password;
     @NotEmpty(message = "{password} {notempty}")
     private String secondPassword;
     @NotEmpty(message = "{firstName} {notempty}")
     private Date dateRegistered;
     @NotEmpty(message = "{firstName} {notempty}")
     private String zipcode;
     @NotEmpty(message = "{firstName} {notempty}")
     private String street;
     @NotEmpty(message = "{firstName} {notempty}")
     private String streetNumber;
     @NotEmpty(message = "{firstName} {notempty}")
     private String city;
     @NotEmpty(message = "{firstName} {notempty}")
     private String role;
 
     private boolean registered;
 
     public UserBean() {
         registered = false;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public Date getDateOfBirth() {
         return dateOfBirth;
     }
 
     public void setDateOfBirth(Date dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public char getGender() {
         return gender;
     }
 
     public void setGender(char gender) {
         this.gender = gender;
     }
 
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
 
     public String getSecondPassword() {
         return secondPassword;
     }
 
     public void setSecondPassword(String secondPassword) {
         this.secondPassword = secondPassword;
     }
 
     public Date getDateRegistered() {
         return dateRegistered;
     }
 
     public void setDateRegistered(Date dateRegistered) {
         this.dateRegistered = dateRegistered;
     }
 
     public String getZipcode() {
         return zipcode;
     }
 
     public void setZipcode(String zipcode) {
         this.zipcode = zipcode;
     }
 
     public String getStreet() {
         return street;
     }
 
     public void setStreet(String street) {
         this.street = street;
     }
 
     public String getStreetNumber() {
         return streetNumber;
     }
 
     public void setStreetNumber(String number) {
         this.streetNumber = number;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
     public boolean confirmPassword() {
         return password.equals(getSecondPassword());
     }
 
     public UserService getUserService() {
         return userService;
     }
 
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 
     public boolean isRegistered() {
         return registered;
     }
 
     public void setRegistered(boolean registered) {
         this.registered = registered;
     }
 
     public String addUser() throws ParseException {
         Address address = new Address(getStreet(), getStreetNumber(),getZipcode(),getCity());
         setRole("User");
         setDateRegistered(new Date());
         //todo dees nog aanpasse met die datum...
         //todo hier hebbek ook een encrypt method zetten voor password.
         //
         boolean isAdmin = false;
         User user = new User(getFirstName(), getLastName(), getDateOfBirth(), getPhoneNumber(), getGender(),getEmail(), SHAEncryption.encrypt(getPassword()),address,getDateRegistered(),getRole(), isAdmin);
         if(confirmPassword()){
             userService.addUser(user);
             registered = true;
             return SUCCESS;
         }
         return FAILURE;
     }
 
 
 }
