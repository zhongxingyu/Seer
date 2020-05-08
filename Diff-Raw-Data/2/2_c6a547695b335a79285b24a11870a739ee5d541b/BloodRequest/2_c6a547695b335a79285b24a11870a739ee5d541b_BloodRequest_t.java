 package org.bloodtorrent.dto;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 19
  * Time: 오전 8:05
  * To change this template use File | Settings | File Templates.
  */
 @Entity(name = "TB_BLOOD_REQ")
 public class BloodRequest {
     @Id
     private String id;
     private Date date;
     private String validated;
     @javax.persistence.Column(name = "first_name")
     private String firstName;
     @javax.persistence.Column(name = "last_name")
     private String lastName;
     private String phone;
     private String email;
     private String gender;
     @javax.persistence.Column(name = "blood_type")
     private String bloodType;
     @javax.persistence.Column(name = "blood_volume")
     private Integer bloodVolume;
     @javax.persistence.Column(name = "requester_type")
     private String requesterType;
     private Date birthday;
     private String city;
     private String state;
     @javax.persistence.Column(name = "hospital_address")
     private String hospitalAddress;
 
 
     public void setFirstName(String firstName) {
         if (firstName == null || firstName.trim().length() == 0) {
             throw new NullPointerException("First Name");
         }else if(firstName.trim().length() > 35) {
             throw new IllegalArgumentException("First Name");
         }
         this.firstName = firstName;
     }
 
     public void setLastName(String lastName) {
         if (lastName == null || lastName.trim().length() ==0){
             throw new NullPointerException("Last Name");
         }else if(lastName.trim().length() > 35) {
             throw new IllegalArgumentException("Last Name");
         }
         this.lastName = lastName;
     }
 
     public void setPhone(String phone) {
         if (phone == null || phone.trim().length() ==0){
             throw new NullPointerException("Cell Phone");
         }else if(!phone.matches("[0-9]{10}")){
             throw new IllegalArgumentException("Cell Phone");
         }
         this.phone = phone;
     }
 
     public void setEmail(String email) {
         if (email == null || email.trim().length() ==0){
             throw new NullPointerException("E-mail");
        }else if(!email.matches("^([.0-9a-zA-Z_-]+)@([0-9a-zA-Z_-]+)(\\.[0-9a-zA-Z_-]+){1,2}$")){
             throw new IllegalArgumentException("E-mail");
         }
         this.email = email;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public void setBloodType(String bloodType) {
         if (bloodType == null || bloodType.trim().length() ==0){
                 throw new NullPointerException("Blood Type");
         }
         this.bloodType = bloodType;
     }
 
     public void setBloodVolume(Integer bloodVolume) {
         if (bloodVolume == null){
             throw new NullPointerException("Blood Volume");
         } else if (bloodVolume > 99 || bloodVolume < 1) {
             throw new IllegalArgumentException("Blood Volume");
         }
         this.bloodVolume = bloodVolume;
     }
     public void setBloodVolume(String bloodVolume) {
         if (bloodVolume == null || bloodVolume.trim().length() == 0) {
             throw new NullPointerException("Blood Volume");
         }
         if (bloodVolume.matches("[0-9]+")) {
             this.bloodVolume = Integer.parseInt(bloodVolume);
             if (this.bloodVolume > 99 || this.bloodVolume < 1) {
                 throw new IllegalArgumentException("Blood Volume");
             }
         } else {
             throw new IllegalArgumentException("Blood Volume");
         }
     }
 
     public void setRequesterType(String requesterType) {
         if (requesterType == null || requesterType.trim().length() ==0){
             throw new NullPointerException("Requester");
         }
         this.requesterType = requesterType;
     }
 
     public void setValidated(String validated) {
         this.validated = validated;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public Date getDate() {
         return date;
     }
 
     public String getValidated() {
         return validated;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public String getPhone() {
         return phone;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getGender() {
         return gender;
     }
 
     public String getBloodType() {
         return bloodType;
     }
 
     public Integer getBloodVolume() {
         return bloodVolume;
     }
 
     public String getRequesterType() {
         return requesterType;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public Date getBirthday() {
         return birthday;
     }
 
     public void setBirthday(Date birthday) {
         this.birthday = birthday;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         if (city == null || city.trim().length() == 0) {
             throw new NullPointerException("City");
         } else if (city.length() > 255) {
             throw new IllegalArgumentException("City");
         }
         this.city = city;
     }
 
     public String getState() {
         return state;
     }
 
     public void setState(String state) {
         if (state == null || state.trim().length() == 0) {
             throw new NullPointerException("State");
         } else if (state.length() > 255) {
             throw new IllegalArgumentException("State");
         }
         this.state = state;
     }
 
     public String getHospitalAddress() {
         return hospitalAddress;
     }
 
     public void setHospitalAddress(String hospitalAddress) {
         if (hospitalAddress == null || hospitalAddress.trim().length() == 0) {
             throw new NullPointerException("Hospital or Blood bank address");
         } else if (hospitalAddress.length() > 1000) {
             throw new IllegalArgumentException("Hospital or Blood bank address");
         }
         this.hospitalAddress = hospitalAddress;
     }
 }
