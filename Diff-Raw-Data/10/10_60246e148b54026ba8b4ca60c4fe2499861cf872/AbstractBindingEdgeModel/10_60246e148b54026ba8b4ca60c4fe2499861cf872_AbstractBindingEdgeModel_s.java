 package com.cloudbees.cloud_resource.model;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Version;
 import java.util.Date;
 
 /**
  * @author Vivek Pandey
  */
 public class AbstractBindingEdgeModel {
     private Long id;
     private String source;
     private String sink;
     private String label;
     private String settings;
     private Date deletedAt;
     private Date createdAt;
     private Date updatedAt;
 
     public AbstractBindingEdgeModel() {
         this.createdAt = new Date();
     }
 
     @Id
     @GeneratedValue
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name="created_at")
     public Date getCreatedAt() {
         return createdAt;
     }
 
     public void setCreatedAt(Date createdAt) {
         this.createdAt = createdAt;
     }
 
     @Column(name="updated_at")
     @Version
     public Date getUpdatedAt() {
         return updatedAt;
     }
 
     public void setUpdatedAt(Date updatedAt) {
         this.updatedAt = updatedAt;
     }
 
 
 
     public String getSource() {
         return source;
     }
 
     public void setSource(String source) {
         this.source = source;
     }
 
     public String getSink() {
         return sink;
     }
 
     public void setSink(String sink) {
         this.sink = sink;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public String getSettings() {
         return settings;
     }
 
     public void setSettings(String settings) {
         this.settings = settings;
     }
 
 
     @Column(name = "deleted_at")
     public Date getDeletedAt() {
         return deletedAt;
     }
 
     public void setDeletedAt(Date deletedAt) {
         this.deletedAt = deletedAt;
     }
 }
