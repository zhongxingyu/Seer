 package com.rakas.mvc.domain;
 
 import org.hibernate.annotations.Type;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.joda.time.DateTime;
 import org.springframework.format.annotation.DateTimeFormat;
 
 import javax.persistence.*;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 
 /**
  * @author <a href="mailto:riuvshin@codenvy.com">Roman Iuvshin</a>
  * @version $Id: 1:02 AM 8/20/13 $
  */
 @Entity
 @Table(name = "contact")
 public class Contact implements Serializable {
 
     private Long     id;
     private int      version;
     private String   firstName;
     private String   lastName;
     private DateTime birthDate;
     private String   description;
     private byte[] photo;
 
     public Contact() {
     }
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "ID")
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Version
     @Column(name = "VERSION")
     public int getVersion() {
         return this.version;
     }
 
     public void setVersion(int version) {
         this.version = version;
     }
 
     @NotEmpty(message="{validation.firstname.NotEmpty.message}")
     @Size(min=3, max=60, message ="{validation.firstname.Size.message}")
     @Column(name = "FIRST_NAME")
     public String getFirstName() {
         return this.firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     @NotEmpty(message="{validation.lastname.NotEmpty.message}")
     @Size(min=1, max=40, message ="{validation.lastname.Size.message}")
     @Column(name = "LAST_NAME")
     public String getLastName() {
         return this.lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     @Column(name = "DESCRIPTION")
     public String getDescription() {
         return this.description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
 
     @Column(name = "BIRTH_DATE")
     @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
     public DateTime getBirthDate() {
         return this.birthDate;
     }
 
     public void setBirthDate(DateTime birthDate) {
         this.birthDate = birthDate;
     }
 
     @Basic(fetch = FetchType.LAZY)
     @Lob @Column(name = "PHOTO")
     public byte[] getPhoto(){
         return photo;
     }
 
     public void setPhoto(byte[] photo){
         this.photo = photo;
     }
 
     @Transient
     public String getBirthDateString() {
         String birthDateString = "";
         if(birthDate != null){
             birthDateString = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd").print(birthDate);
         }
         return birthDateString;
     }
 
     public String toString() {
         return "Contact id: " + id + ", First name: " + firstName + ", Last name: " + lastName + ", birth day: " + birthDate + ", description: " +
                description;
     }
 }
