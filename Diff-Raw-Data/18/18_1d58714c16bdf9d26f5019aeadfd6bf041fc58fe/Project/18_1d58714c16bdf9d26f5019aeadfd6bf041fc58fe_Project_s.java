 package org.xezz.timeregistration.model;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.xezz.timeregistration.dao.ProjectDAO;
import org.xezz.timeregistration.services.CustomerService;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 26.04.13
  * Time: 17:50
  */
 @Entity
 public class Project implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long projectId;
     private String name;
     private String description;
     @ManyToOne(optional = false)
     private Customer customer;
     @Temporal(TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdatedDate;
 
    @Transient
    @Autowired
    CustomerService customerService;

     /**
      * Default Constructor to support DI and JPA
      */
     public Project() {
     }
 
     public Project(ProjectDAO p) {
         if (p.getProjectId() != null) {
             this.projectId = p.getProjectId();
         }
         this.name = p.getName();
         this.description = p.getDescription();
         this.creationDate = p.getCreationDate();
         this.lastUpdatedDate = p.getLastUpdatedDate();
         this.customer = p.receiveCustomer();
     }
 
     @PrePersist
     private void setDateBeforePersisting() {
         lastUpdatedDate = creationDate = new Date();
     }
 
     @PreUpdate
     private void updateLastEditedDate() {
         lastUpdatedDate = new Date();
     }
 
     public Long getProjectId() {
         return projectId;
     }
 
     public void setProjectId(Long projectId) {
         this.projectId = projectId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Customer getCustomer() {
         return customer;
     }
 
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getLastUpdatedDate() {
         return lastUpdatedDate;
     }
 
     public void setLastUpdatedDate(Date lastUpdatedDate) {
         this.lastUpdatedDate = lastUpdatedDate;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Project)) return false;
 
         Project project = (Project) o;
 
         if (creationDate != null ? !creationDate.equals(project.creationDate) : project.creationDate != null)
             return false;
         if (customer != null ? !customer.equals(project.customer) : project.customer != null) return false;
         if (description != null ? !description.equals(project.description) : project.description != null) return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(project.lastUpdatedDate) : project.lastUpdatedDate != null)
             return false;
         if (name != null ? !name.equals(project.name) : project.name != null) return false;
         if (projectId != null ? !projectId.equals(project.projectId) : project.projectId != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = projectId != null ? projectId.hashCode() : 0;
         final int PRIME = 31;
         result = PRIME * result + (name != null ? name.hashCode() : 0);
         result = PRIME * result + (description != null ? description.hashCode() : 0);
         result = PRIME * result + (customer != null ? customer.hashCode() : 0);
         result = PRIME * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = PRIME * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
 
         return result;
     }
 }
