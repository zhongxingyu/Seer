/**
 *
 * @author sandy
  */
 package model;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 
 @Entity
 @Table(name = "project_legal")
 public class ProjectLegal implements Serializable {
 
     @Id
     @GeneratedValue
     @Column(name = "project_legal_id")
     private long id;

     @Column(name = "project_legal_name")
     private String projectLegalName;

     @Column(name = "project_legal_date")
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date projectLegalDate;

     @Column(name = "project_legal_required")
     private String projectLegalRequired;

     @Column(name = "project_legal_status")
     private String projectLegalStatus;

     @Column(name = "update_by")
     private String updateBy;

     @Column(name = "created_by", updatable = false)
     private String createdBy;

     @Column(name = "active")
     private boolean active = true;
    
     @ManyToOne
     @JoinColumn(name = "project_id")
     private Project project;
 
     public boolean isActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public String getCreatedBy() {
         return createdBy;
     }
 
     public void setCreatedBy(String createdBy) {
         this.createdBy = createdBy;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public Project getProject() {
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     public Date getProjectLegalDate() {
         return projectLegalDate;
     }
 
     public void setProjectLegalDate(Date projectLegalDate) {
         this.projectLegalDate = projectLegalDate;
     }
 
     public String getProjectLegalName() {
         return projectLegalName;
     }
 
     public void setProjectLegalName(String projectLegalName) {
         this.projectLegalName = projectLegalName;
     }
 
     public String getProjectLegalRequired() {
         return projectLegalRequired;
     }
 
     public void setProjectLegalRequired(String projectLegalRequired) {
         this.projectLegalRequired = projectLegalRequired;
     }
 
     public String getProjectLegalStatus() {
         return projectLegalStatus;
     }
 
     public void setProjectLegalStatus(String projectLegalStatus) {
         this.projectLegalStatus = projectLegalStatus;
     }
 
     public String getUpdateBy() {
         return updateBy;
     }
 
     public void setUpdateBy(String updateBy) {
         this.updateBy = updateBy;
     }
 
     
    
 
 }
