 package org.bloodtorrent.dto;
 
 import org.hibernate.validator.constraints.NotBlank;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.validation.constraints.Size;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.NotNull;
 import java.util.Date;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 14
  * Time: 오후 2:20
  * To change this template use File | Settings | File Templates.
  */
 @Entity(name = "user")
 public class User {
     @Id
    @Pattern(regexp = "^([0-9a-zA-Z_-]([0-9a-zA-Z_-]|\\.)+[0-9a-zA-Z_-])@([0-9a-zA-Z_-]+)(\\.[0-9a-zA-Z_-]+){1,2}$", message="Please check email address")
     @Size(min = 5, max = 100, message="Please check email address")
     @NotBlank(message="Please fill out all the mandatory fields(email)")
     private String id;
     @Size(min = 8, max = 25, message="Please check password")
     @Pattern(regexp ="\\D*\\d+\\D*", message="Please check password")
     @NotBlank(message="Please fill out all the mandatory fields(password)")
     private String password;
     private String role;
     @javax.persistence.Column(name = "first_name")
     @NotBlank(message="Please fill out all the mandatory fields(first name)")
     @Size(min = 1, max = 35, message= "Please check first name")
     private String firstName;
     @javax.persistence.Column(name = "last_name")
     @NotBlank(message="Please fill out all the mandatory fields(last name)")
     @Size(min = 1, max = 35, message= "Please check last name")
     private String lastName;
     @NotNull
     @Size(min = 10, max = 10, message= "Please check phone number")
     @Pattern(regexp = "\\d*", message= "Please check phone number")
     @javax.persistence.Column(name = "cell_phone")
     private String cellPhone;
     private String gender;
     @javax.persistence.Column(name = "blood_group")
     @NotBlank(message="Please fill out all the mandatory fields(blood group)")
     @Pattern(regexp = "^(A\\+)|(A-)|(B\\+)|(B-)|(AB\\+)|(AB-)|(O\\+)|(O-)|(Unknown)$", message="Please check blood group")
     private String bloodGroup;
     private boolean anonymous;
     @NotBlank(message="Please fill out all the mandatory fields(city)")
     @Size(min = 1, max = 255, message="Please check city size(1-255)")
     private String city;
     @NotBlank(message="Please fill out all the mandatory fields(state)")
     @Size(min = 1, max = 255, message="Please check state size(1-255)")
     @Pattern(regexp = "^(Andhra Pradesh)|(Arunachal Pradesh)|(Asom \\(Assam\\))|(Bihar)|(Karnataka)|(Kerala)|(Chhattisgarh)|(Goa)|(Gujarat)|(Haryana)|(Himachal Pradesh)|(Jammu And Kashmir)|(Jharkhand)|(West Bengal)|(Madhya Pradesh)|(Maharashtra)|(Manipur)|(Meghalaya)|(Mizoram)|(Nagaland)|(Orissa)|(Punjab)|(Rajasthan)|(Sikkim)|(Tamilnadu)|(Tripura)|(Uttarakhand \\(Uttaranchal\\))|(Uttar Pradesh)$")
     private String state;
     @NotBlank(message="Please fill out all the mandatory fields(address)")
     @Size(min = 1, max = 1000 , message="Please check address size(1-1000)")
     private String address;
     @NotBlank(message="Please fill out all the mandatory fields(distance)")
    @Pattern(regexp = "^5|10|20|50$" , message="Please check distance")
     private String distance;
     @javax.persistence.Column(name = "birth_day")
     @Pattern(regexp ="^((0[1-9]|[1-2][0-9]|3[0-1])\\-(0[0-9]|1[0-2])\\-(19[0-9][0-9]|20\\d{2}))*$", message="Please check date of birth")
     private String birthDay;
     @javax.persistence.Column(name = "last_donate_date")
     private Date lastDonateDate;
 
     public Date getLastDonateDate() {
         return lastDonateDate;
     }
 
     public void setLastDonateDate(Date lastDonateDate) {
         this.lastDonateDate = lastDonateDate;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getState() {
         return state;
     }
 
     public void setState(String state) {
         this.state = state;
     }
 
     public String getBirthDay() {
         return birthDay;
     }
 
     public void setBirthDay(String birthDay) {
         this.birthDay = birthDay;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
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
 
     public String getCellPhone() {
         return cellPhone;
     }
 
     public void setCellPhone(String cellPhone) {
         this.cellPhone = cellPhone;
     }
 
     public String getGender() {
         return gender;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public String getBloodGroup() {
         return bloodGroup;
     }
 
     public void setBloodGroup(String bloodGroup) {
         this.bloodGroup = bloodGroup;
     }
 
     public boolean getAnonymous() {
         return anonymous;
     }
 
     public void setAnonymous(boolean anonymous) {
         this.anonymous = anonymous;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getDistance() {
         return distance;
     }
 
     public void setDistance(String distance) {
         this.distance = distance;
     }
 }
