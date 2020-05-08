 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.middleware.pojos.workbench;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 @Entity
 @Table(name = "workbench_project")
 public class Project implements Serializable{
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @Basic(optional = false)
     @GeneratedValue
     @Column(name = "project_id")
     private Long projectId;
 
     @Basic(optional = false)
     @Column(name = "project_name")
     private String projectName = "";
 
     @Basic(optional = false)
     @Column(name = "target_due_date")
     private Date targetDueDate;
     
     @Basic(optional = false)
     @Column(name = "user_id")
     private int userId;
 
     @OneToOne
     @JoinColumn(name = "template_id")
     private WorkflowTemplate template;
    
    @Column(name = "crop_type")
    @Enumerated(value=EnumType.STRING)
    private CropType cropType;
 
     @Basic(optional = false)
     @Column(name = "template_modified")
     private boolean templateModified;
 
     @Transient
     private List<ProjectWorkflowStep> steps;
 
     // TODO: remove these fields
     @Transient
     private String action;
     @Transient
     private String status;
     @Transient
     private Contact owner;
 
     public Long getProjectId() {
         return projectId;
     }
 
     public void setProjectId(Long projectId) {
         this.projectId = projectId;
     }
     
     public int getUserId() {
         return userId;
     }
 
     public void setUserId(int userId) {
         this.userId = userId;
     }
 
     public String getProjectName() {
         return projectName;
     }
 
     public void setProjectName(String projectName) {
         this.projectName = projectName;
     }
 
     public Date getTargetDueDate() {
         return targetDueDate;
     }
 
     public void setTargetDueDate(Date targetDueDate) {
         this.targetDueDate = targetDueDate;
     }
 
     public WorkflowTemplate getTemplate() {
         return template;
     }
 
     public void setTemplate(WorkflowTemplate template) {
         this.template = template;
     }
 
    public CropType getCropType() {
        return cropType;
    }

    public void setCropType(CropType cropType) {
        this.cropType = cropType;
    }

     public boolean isTemplateModified() {
         return templateModified;
     }
 
     public void setTemplateModified(boolean templateModified) {
         this.templateModified = templateModified;
     }
 
     public List<ProjectWorkflowStep> getSteps() {
         return steps;
     }
 
     public void setSteps(List<ProjectWorkflowStep> steps) {
         this.steps = steps;
     }
 
     public String getAction() {
         return action;
     }
 
     public void setAction(String action) {
         this.action = action;
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     public Contact getOwner() {
         return owner;
     }
 
     public void setOwner(Contact owner) {
         this.owner = owner;
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(projectId).hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (obj == this) {
             return true;
         }
         if (!Project.class.isInstance(obj)) {
             return false;
         }
 
         Project otherObj = (Project) obj;
 
         return new EqualsBuilder().append(projectId, otherObj.projectId).isEquals();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("Project {");
         sb.append("project id = ").append(projectId);
         sb.append(", project name = ").append(projectName);
         sb.append("}");
 
         return sb.toString();
     }
 }
