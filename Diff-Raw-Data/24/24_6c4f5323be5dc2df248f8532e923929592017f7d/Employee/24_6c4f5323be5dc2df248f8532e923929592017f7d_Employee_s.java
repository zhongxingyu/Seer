 package com.fs.humanResources.model.employee.entities;
 
 import com.fs.humanResources.model.address.entities.Address;
 import com.fs.humanResources.model.common.entities.BaseEntity;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 public class Employee extends BaseEntity {
 
     @Id
     @GeneratedValue
     protected Long id;
 
     private String firstName;
     private String lastName;
     private Date dateOfBirth;
     private Long staffNumber;
 
    @OneToMany(mappedBy="employee",cascade=CascadeType.PERSIST)
     private List<Address> addressList;
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public void setDateOfBirth(Date dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public void setStaffNumber(Long employeeId) {
         this.staffNumber = employeeId;
     }
 
     public void setAddressList(List<Address> address) {
         this.addressList = address;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public Date getDateOfBirth() {
         return dateOfBirth;
     }
 
     public Long getStaffNumber() {
         return staffNumber;
     }
 
     public List<Address> getAddressList() {
         return addressList;
     }
 }
