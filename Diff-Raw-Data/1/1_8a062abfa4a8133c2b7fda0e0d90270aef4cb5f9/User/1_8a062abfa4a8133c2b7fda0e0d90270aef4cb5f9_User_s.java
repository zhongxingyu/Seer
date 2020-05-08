 package org.bloodtorrent.dto;
 
import org.h2.table.Column;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
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
     private String id;
     private String password;
     private String role;
     @javax.persistence.Column(name = "first_name")
     private String firstName;
     @javax.persistence.Column(name = "last_name")
     private String lastName;
     @javax.persistence.Column(name = "phone_number")
     private String phoneNumber;
     private String gender;
     private int age;
 
     @javax.persistence.Column(name = "blood_type")
     private String bloodType;
     private String anonymous;
     private String address;
     private double distance;
 
 
     public void setId(String id) {
         this.id = id;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public void setAge(int age) {
         this.age = age;
     }
 
     public void setBloodType(String bloodType) {
         this.bloodType = bloodType;
     }
 
     public void setAnonymous(String anonymous) {
         this.anonymous = anonymous;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public void setDistance(double distance) {
         this.distance = distance;
     }
 
     public String getId() {
         return id;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getRole() {
         return role;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     public String getGender() {
         return gender;
     }
 
     public int getAge() {
         return age;
     }
 
     public String getBloodType() {
         return bloodType;
     }
 
     public String getAnonymous() {
         return anonymous;
     }
 
     public String getAddress() {
         return address;
     }
 
     public double getDistance() {
         return distance;
     }
 }
