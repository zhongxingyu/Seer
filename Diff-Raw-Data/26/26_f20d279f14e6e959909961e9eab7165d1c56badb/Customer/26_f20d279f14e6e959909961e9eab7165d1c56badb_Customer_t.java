 package org.xezz.timeregistration.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * User: Xezz
  * Date: 26.04.13
  * Time: 17:44
  */
 @Entity
 public class Customer implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long customerId;
     private String name;
     private String customerInfo;
     @Temporal(TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdatedDate;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
     private Set<Project> projects = new HashSet<Project>();
 
     /**
      * Default constructor for JPA/Spring/Whatever
      */
     public Customer() {
     }
 
     /**
      * Set the date of creation to the current time
      */
    @PrePersist
     private void setCreationDate() {
        this.lastUpdatedDate = this.creationDate = new Date();
     }
 
     /**
      * Update the date of last modification to the current time
      */
    @PreUpdate
     private void setLastUpdatedDate() {
         this.lastUpdatedDate = new Date();
     }
 
     public Long getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(Long customerId) {
         this.customerId = customerId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getCustomerInfo() {
         return customerInfo;
     }
 
     public void setCustomerInfo(String customerInfo) {
         this.customerInfo = customerInfo;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public Date getLastUpdatedDate() {
         return lastUpdatedDate;
     }
 
     public Set<Project> getProjects() {
         return projects;
     }
 
     public void setProjects(Set<Project> projects) {
         this.projects = projects;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Customer)) return false;
 
         Customer customer = (Customer) o;
 
         if (creationDate != null ? !creationDate.equals(customer.creationDate) : customer.creationDate != null)
             return false;
         if (customerId != null ? !customerId.equals(customer.customerId) : customer.customerId != null) return false;
         if (customerInfo != null ? !customerInfo.equals(customer.customerInfo) : customer.customerInfo != null)
             return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(customer.lastUpdatedDate) : customer.lastUpdatedDate != null)
             return false;
         if (name != null ? !name.equals(customer.name) : customer.name != null) return false;
         if (projects != null ? !projects.equals(customer.projects) : customer.projects != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = customerId != null ? customerId.hashCode() : 0;
         final int PRIME = 31;
         result = PRIME * result + (name != null ? name.hashCode() : 0);
         result = PRIME * result + (customerInfo != null ? customerInfo.hashCode() : 0);
         result = PRIME * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = PRIME * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
         result = PRIME * result + (projects != null ? projects.hashCode() : 0);
         return result;
     }
 }
