 package net.helloworld.model;
 
 import org.springframework.util.StringUtils;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Date: 17/10/13
  * Time: 11:01 PM
  */
 @Entity
 @Table(name = "STUDENTS")
 public class Student implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     private Integer id;
 
     private String lastName;
     private String firstName;
     private String otherNames;
     private Date dateOfBirth;
 
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getOtherNames() {
         return otherNames;
     }
 
     public void setOtherNames(String otherNames) {
         this.otherNames = otherNames;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     @Override
     public String toString() {
         return StringUtils.capitalize(lastName) + ", " + firstName;
     }
 }
