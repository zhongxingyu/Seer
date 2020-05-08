 package com.personnelmanager.domain.db;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.format.annotation.DateTimeFormat;
 
 import javax.persistence.*;
 import javax.validation.constraints.Min;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: MikeChen
  * Date: Jul 13, 2010
  * Time: 4:24:50 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name = "person")
 public class Person implements Auditable {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "personId")
     private Long personId;
 
     @Version
     @Column(name = "version")
     private Integer version;
 
     @Column(name = "username", unique = true, nullable = false)
     @NotEmpty
     private String login;
 
     @Column(name = "password", nullable = false)
     @NotEmpty
     private String password;
 
     @Column(name = "salutation")
     private String salutation;
 
     @Column(name = "firstName", nullable = false)
     @NotEmpty
     private String firstName;
 
     @Column(name = "middleName")
     private String middleName;
 
     @Column(name = "lastName", nullable = false)
     @NotEmpty
     private String lastName;
 
     @Temporal(TemporalType.DATE)
     @Column(name = "dob", nullable = false)
     @DateTimeFormat(pattern="dd/MM/yyyy")
     private Date dob;
 
     @ManyToOne
     @JoinColumn(name = "roleId")
     private Role role;
 
     public Role getRole() {
         return role;
     }
 
     public void setRole(Role role) {
         this.role = role;
     }
 
     public Long getPersonId() {
         return personId;
     }
 
     public void setPersonId(Long personId) {
         this.personId = personId;
     }
 
     public Integer getVersion() {
         return version;
     }
 
     public void setVersion(Integer version) {
         this.version = version;
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getSalutation() {
         return salutation;
     }
 
     public void setSalutation(String salutation) {
         this.salutation = salutation;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getMiddleName() {
         return middleName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public Date getDob() {
         return dob;
     }
 
     public void setDob(Date dob) {
         this.dob = dob;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Person person = (Person) o;
 
         if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
         if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
         if (login != null ? !login.equals(person.login) : person.login != null) return false;
         if (middleName != null ? !middleName.equals(person.middleName) : person.middleName != null) return false;
         if (password != null ? !password.equals(person.password) : person.password != null) return false;
         if (personId != null ? !personId.equals(person.personId) : person.personId != null) return false;
         if (salutation != null ? !salutation.equals(person.salutation) : person.salutation != null) return false;
         if (version != null ? !version.equals(person.version) : person.version != null) return false;
         if (!dobSame(dob, person.dob)) return false;
 
         return true;
     }
 
     private boolean dobSame(Date dob, Date dob1) {
         if (dob != null) {
             if (dob1 != null) {
                 Calendar calendar1 = Calendar.getInstance();
                 Calendar calendar2 = Calendar.getInstance();
                 calendar1.setTime(dob);
                 calendar2.setTime(dob1);
                 if ((calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) &&
                     (calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)) &&
                     (calendar1.get(Calendar.DATE) == calendar2.get(Calendar.DATE))) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         } else {
             if (dob1 != null) {
                 return false;
             } else {
                 return true;
             }
         }
     }
 
     @Override
     public int hashCode() {
         int result = personId != null ? personId.hashCode() : 0;
         result = 31 * result + (version != null ? version.hashCode() : 0);
         result = 31 * result + (login != null ? login.hashCode() : 0);
         result = 31 * result + (password != null ? password.hashCode() : 0);
         result = 31 * result + (salutation != null ? salutation.hashCode() : 0);
         result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
         result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
         result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
         result = 31 * result + (dob != null ? dob.hashCode() : 0);
         return result;
     }
 }
