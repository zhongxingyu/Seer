 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.web.organizer.data;
 
 import java.util.Date;
 import java.io.Serializable;
 import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author Gery
  */
 @Entity
 public class Person implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private long id = 0;
    @Enumerated(EnumType.ORDINAL)
     private String firstName = "";
     private String lastName = "";
     private String email = "";
     private String email2 = "";
 
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date birthdate;
 
     private String username = "";
     private String password = "";
     private String mobile = "";
     private String mobile2 = "";
     private String timeZone = "";
     private String countryCode = "";
 
     public Person() {
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getEmail2() {
         return email2;
     }
 
     public void setEmail2(String email2) {
         this.email2 = email2;
     }
 
     public String getMobile() {
         return mobile;
     }
 
     public void setMobile(String mobile) {
         this.mobile = mobile;
     }
 
     public String getMobile2() {
         return mobile2;
     }
 
     public void setMobile2(String mobile2) {
         this.mobile2 = mobile2;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public Date getBirthdate() {
         return birthdate;
     }
 
     public void setBirthdate(Date birthdate) {
         this.birthdate = birthdate;
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
 
     public String getTimeZone() {
         return timeZone;
     }
 
     public void setTimeZone(String timeZone) {
         this.timeZone = timeZone;
     }
 
     public String getCountryCode() {
         return countryCode;
     }
 
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 }
