 package be.kdg.groeph.bean;
 
 import be.kdg.groeph.model.Address;
 import be.kdg.groeph.model.TripUser;
 import be.kdg.groeph.service.MailService;
 import be.kdg.groeph.service.UserService;
 import be.kdg.groeph.util.SHAEncryption;
 import org.apache.log4j.Logger;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.bean.ManagedProperty;
 import javax.inject.Named;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Date;
 
 @Component
 @RequestScoped
 @Named
 public class RegisterBean implements Serializable {
     static Logger logger = Logger.getLogger(RegisterBean.class);
 
     private static final String SUCCESS = "SUCCESS";
     private static final String FAILURE = "FAILURE";
 
     @ManagedProperty(value = "#{userService}")
     @Autowired
     UserService userService;
 
     @ManagedProperty(value = "#{mailService}")
     @Autowired
     MailService mailService;
 
     @Qualifier("loginBean")
     @ManagedProperty(value = "#{loginBean}")
     @Autowired
     LoginBean loginBean;
 
     @NotEmpty(message = "{firstName} {notempty}")
     @Length(max = 50, message = "{firstName} {length}")
     private String firstName;
     @NotEmpty(message = "{lastName} {notempty}")
     @Length(max = 50, message = "{lastName} {length}")
     private String lastName;
     @NotNull(message = "{dateOfBirth} {notempty}")
     @Past(message = "{dateOfBirth} {past}")
     private Date dateOfBirth;
     @Length(max = 30, message = "{phoneNumber} {length}")
     @NotEmpty(message = "{phoneNumber} {notempty}")
     private String phoneNumber;
     private char gender;
     @NotEmpty(message = "{email} {notempty}")
     @Email(message = "{email} {validEmail}")
     @Length(max = 100, message = "{email} {length}")
     private String email;
     @NotEmpty(message = "{password} {notempty}")
     private String password;
     @NotEmpty(message = "{password} {notempty}")
     private String secondPassword;
     @NotEmpty(message = "{dateRegistered} {notempty}")
     private Date dateRegistered;
     @NotEmpty(message = "{zipcode} {notempty}")
     private String zipcode;
     @NotEmpty(message = "{street} {notempty}")
     private String street;
     @NotEmpty(message = "{streetNumber} {notempty}")
     private String streetNumber;
     @NotEmpty(message = "{city} {notempty}")
     private String city;
     private String role;
 
 
     @NotEmpty(message = "{firstName} {notempty}")
     @Length(max = 50, message = "{firstName} {length}")
     private String newfirstName;
     @NotEmpty(message = "{lastName} {notempty}")
     @Length(max = 50, message = "{lastName} {length}")
     private String newlastName;
     @NotNull(message = "{dateOfBirth} {notempty}")
     @Past(message = "{dateOfBirth} {past}")
     private Date newdateOfBirth;
     @Length(max = 30, message = "{phoneNumber} {length}")
     @NotEmpty(message = "{phoneNumber} {notempty}")
     private String newphoneNumber;
     private char newgender;
     @NotEmpty(message = "{email} {notempty}")
     @Email(message = "{email} {validEmail}")
     @Length(max = 100, message = "{email} {length}")
     private String newemail;
     @NotEmpty(message = "{password} {notempty}")
     private String newpassword;
     @NotEmpty(message = "{password} {notempty}")
     private String newsecondPassword;
     @NotEmpty(message = "{dateRegistered} {notempty}")
     private Date newdateRegistered;
     private String newzipcode;
     private String newstreet;
     private String newstreetNumber;
     private String newcity;
 
     private String mailMessage;
 
     private boolean editableUser;
 
     private boolean registered;
 
     public RegisterBean() {
         registered = false;
         editableUser = false;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getMailMessage() {
         return mailMessage;
     }
 
     public void setMailMessage(String mailMessage) {
         this.mailMessage = mailMessage;
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
 
     public boolean isRegistered() {
         return registered;
     }
 
     public void setRegistered(boolean registered) {
         this.registered = registered;
     }
 
     public String getNewfirstName() {
         return newfirstName;
     }
 
     public void setNewfirstName(String newfirstName) {
         this.newfirstName = newfirstName;
     }
 
     public String getNewlastName() {
         return newlastName;
     }
 
     public void setNewlastName(String newlastName) {
         this.newlastName = newlastName;
     }
 
     public Date getNewdateOfBirth() {
         return newdateOfBirth;
     }
 
     public void setNewdateOfBirth(Date newdateOfBirth) {
         this.newdateOfBirth = newdateOfBirth;
     }
 
     public String getNewphoneNumber() {
         return newphoneNumber;
     }
 
     public void setNewphoneNumber(String newphoneNumber) {
         this.newphoneNumber = newphoneNumber;
     }
 
     public char getNewgender() {
         return newgender;
     }
 
     public void setNewgender(char newgender) {
         this.newgender = newgender;
     }
 
     public String getNewemail() {
         return newemail;
     }
 
     public void setNewemail(String newemail) {
         this.newemail = newemail;
     }
 
     public String getNewpassword() {
         return newpassword;
     }
 
     public void setNewpassword(String newpassword) {
         this.newpassword = newpassword;
     }
 
     public String getNewsecondPassword() {
         return newsecondPassword;
     }
 
     public void setNewsecondPassword(String newsecondPassword) {
         this.newsecondPassword = newsecondPassword;
     }
 
     public Date getNewdateRegistered() {
         return newdateRegistered;
     }
 
     public void setNewdateRegistered(Date newdateRegistered) {
         this.newdateRegistered = newdateRegistered;
     }
 
     public String getNewzipcode() {
         return newzipcode;
     }
 
     public void setNewzipcode(String newzipcode) {
         this.newzipcode = newzipcode;
     }
 
     public String getNewstreet() {
         return newstreet;
     }
 
     public void setNewstreet(String newstreet) {
         this.newstreet = newstreet;
     }
 
     public String getNewstreetNumber() {
         return newstreetNumber;
     }
 
     public void setNewstreetNumber(String newstreetNumber) {
         this.newstreetNumber = newstreetNumber;
     }
 
     public String getNewcity() {
         return newcity;
     }
 
     public void setNewcity(String newcity) {
         this.newcity = newcity;
     }
 
     public boolean isEditableUser() {
         return editableUser;
     }
 
     public void setEditableUser(boolean editableUser) {
         this.editableUser = editableUser;
     }
 
     public String addUser() throws ParseException {
         Address address = new Address(getStreet(), getStreetNumber(), getZipcode(), getCity());
         setRole("ROLE_USER");
         setDateRegistered(new Date());
         TripUser user = new TripUser(getFirstName(), getLastName(), getDateOfBirth(), getPhoneNumber(), getGender(), getEmail(), SHAEncryption.encrypt(getPassword()), address, getDateRegistered(), getRole());
         user.setAccountNonExpired(true);
         user.setAccountNonLocked(true);
         user.setCredentialsNonExpired(true);
         user.setEnabled(true);
         if (confirmPassword()) {
             TripUser tripUser = userService.getUserByEmail(user.getEmail());
             if (tripUser.isNull()) {
                 if (mailService.isSuccessfulRegistration(user.getEmail())) {
                     userService.addUser(user);
                     putNewValues(null, null, null, null, null, ' ', null, null, null, null);
                     makeFacesMessage("Registration succesfull !", "info");
                     registered=true;
                     return SUCCESS;
                 } else {
                     mailMessage = "Registration mail could not be send. Please try to registrate again.";
                     makeFacesMessage("Registration mail could not be send. Please try to registrate again.", "error");
                     return FAILURE;
                 }
             } else {
                 makeFacesMessage("This email already has an account !", "error");
                 return FAILURE;
             }
         }
         return FAILURE;
     }
     private void makeFacesMessage(String message, String type) {

//        FacesMessage facesMsg = null;
//        if (type.equals("info")) {
//            facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, message, message);
//        } else if (type.equals("error")) {
//            facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
//        }
//        if(!FacesContext.getCurrentInstance().equals(null)){
//        FacesContext fc = FacesContext.getCurrentInstance();
//        fc.addMessage(null, facesMsg);
   //     }

     }
 
     public String updateUser() throws SQLException {
         TripUser tripUser = loginBean.getUser();
 
         tripUser.setFirstName(newfirstName);
         tripUser.setLastName(newlastName);
         tripUser.setDateOfBirth(newdateOfBirth);
         tripUser.setPhoneNumber(newphoneNumber);
         tripUser.setGender(newgender);
         tripUser.setEmail(newemail);
         tripUser.setDateRegistered(newdateRegistered);
 
         Address adress = loginBean.getUser().getAddress();
         adress.setZipcode(newzipcode);
         adress.setStreet(newstreet);
         adress.setStreetNumber(newstreetNumber);
         adress.setCity(newcity);
         tripUser.setAddress(adress);
 
         userService.updateUser(tripUser);
         editableUser = false;
 
         return null;
     }
 
     public String editUser() {
         newfirstName = loginBean.getUser().getFirstName();
         newlastName = loginBean.getUser().getLastName();
         newdateOfBirth = loginBean.getUser().getDateOfBirth();
         newphoneNumber = loginBean.getUser().getPhoneNumber();
         newgender = loginBean.getUser().getGender();
         newemail = loginBean.getUser().getEmail();
         newdateRegistered = loginBean.getUser().getDateRegistered();
         newzipcode = loginBean.getUser().getAddress().getZipcode();
         newstreet = loginBean.getUser().getAddress().getStreet();
         newstreetNumber = loginBean.getUser().getAddress().getStreetNumber();
         newcity = loginBean.getUser().getAddress().getCity();
         editableUser = true;
         return null;
     }
 
     public String cancel(){
         editableUser = false;
                 return null;
     }
 
     public void putNewValues(String email, String firstName, String lastName, Date dateOfBirth, String phoneNumber,
                              char gender, String zipcode, String street, String streetNumber, String city) {
         setEmail(email);
         setFirstName(firstName);
         setLastName(lastName);
         setDateOfBirth(dateOfBirth);
         setPhoneNumber(phoneNumber);
         setGender(gender);
         setZipcode(zipcode);
         setStreet(street);
         setStreetNumber(streetNumber);
         setCity(city);
     }
 }
