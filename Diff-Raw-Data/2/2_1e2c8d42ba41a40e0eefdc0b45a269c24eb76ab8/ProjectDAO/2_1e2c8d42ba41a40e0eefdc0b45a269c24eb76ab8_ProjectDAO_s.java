 package org.xezz.timeregistration.dao;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.xezz.timeregistration.model.Customer;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.repositories.CustomerRepository;
 
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 30.05.13
  * Time: 22:50
  */
 @Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
 public class ProjectDAO {
     private Long projectId;
     private Long customerId;
     private String name;
     private String description;
     private Date creationDate;
     private Date lastUpdatedDate;
 
     @Autowired
    private CustomerRepository customerRepository;
     private final static Logger LOGGER = LoggerFactory.getLogger(ProjectDAO.class);
 
     public ProjectDAO() {
     }
 
     public ProjectDAO(Long projectId, Long customerId, String name, String description, Date creationDate, Date lastUpdatedDate) {
         this.projectId = projectId;
         this.customerId = customerId;
         this.name = name;
         this.description = description;
         this.creationDate = creationDate;
         this.lastUpdatedDate = lastUpdatedDate;
     }
 
     public ProjectDAO(Project project) {
         this.projectId = project.getProjectId();
         this.name = project.getName();
         this.description = project.getDescription();
         this.customerId = project.getCustomer().getCustomerId();
         this.creationDate = project.getCreationDate();
         this.lastUpdatedDate = project.getLastUpdatedDate();
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
 
     public Long getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(Long customerId) {
         this.customerId = customerId;
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
         if (!(o instanceof ProjectDAO)) return false;
 
         ProjectDAO project = (ProjectDAO) o;
 
         if (creationDate != null ? !creationDate.equals(project.creationDate) : project.creationDate != null)
             return false;
         if (!customerId.equals(project.customerId)) return false;
         if (!description.equals(project.description)) return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(project.lastUpdatedDate) : project.lastUpdatedDate != null)
             return false;
         if (!name.equals(project.name)) return false;
         if (projectId != null ? !projectId.equals(project.projectId) : project.projectId != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = projectId != null ? projectId.hashCode() : 0;
         result = 31 * result + name.hashCode();
         result = 31 * result + description.hashCode();
         result = 31 * result + customerId.hashCode();
         result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = 31 * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
         return result;
     }
 
     public Customer receiveCustomer() {
         return customerRepository.findOne(customerId);
     }
 }
