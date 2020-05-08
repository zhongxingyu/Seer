 package org.openmrs.module.hr;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 
 public class HrTraining  implements java.io.Serializable {
 
 
     // Fields    
 
      private int trainingId;
      private Integer nationalId;
      private String category;
      private String name;
      private String description;
      private int creator;
      private Date dateCreated;
      private Integer changedBy;
      private Date dateChanged;
      private short retired;
      private Integer retiredBy;
      private Date dateRetired;
      private String retireReason;
      private String uuid;
      private Double sortWeight;
      private Set hrTrainingClasses = new HashSet(0);
 
 
 
     // Constructors
 
     /** default constructor */
     public HrTraining() {
     }
 
 	/** minimal constructor */
     public HrTraining(int trainingId, String category, String name, String description, int creator, Date dateCreated, short retired, String uuid) {
         this.trainingId = trainingId;
         this.category = category;
         this.name = name;
         this.description = description;
         this.creator = creator;
         this.dateCreated = dateCreated;
         this.retired = retired;
         this.uuid = uuid;
     }
     
     /** full constructor */
    public HrTraining(int trainingId, Integer nationalId, String category, String name, String description, int creator, Date dateCreated, Integer changedBy, Date dateChanged, short retired, Integer retiredBy, Date dateRetired, String retireReason, String uuid, Double sortWeight, Set hrTrainingClasses) {
         this.trainingId = trainingId;
         this.nationalId = nationalId;
         this.category = category;
         this.name = name;
         this.description = description;
         this.creator = creator;
         this.dateCreated = dateCreated;
         this.changedBy = changedBy;
         this.dateChanged = dateChanged;
         this.retired = retired;
         this.retiredBy = retiredBy;
         this.dateRetired = dateRetired;
         this.retireReason = retireReason;
         this.uuid = uuid;
         this.sortWeight = sortWeight;
         this.hrTrainingClasses = hrTrainingClasses;
     }
     
 
    
     // Property accessors
 
     public int getTrainingId() {
         return this.trainingId;
     }
     
     public void setTrainingId(int trainingId) {
         this.trainingId = trainingId;
     }
 
     public Integer getNationalId() {
         return this.nationalId;
     }
     
     public void setNationalId(Integer nationalId) {
         this.nationalId = nationalId;
     }
 
     public String getCategory() {
         return this.category;
     }
     
     public void setCategory(String category) {
         this.category = category;
     }
 
     public String getName() {
         return this.name;
     }
     
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return this.description;
     }
     
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int getCreator() {
         return this.creator;
     }
     
     public void setCreator(int creator) {
         this.creator = creator;
     }
 
     public Date getDateCreated() {
         return this.dateCreated;
     }
     
     public void setDateCreated(Date dateCreated) {
         this.dateCreated = dateCreated;
     }
 
     public Integer getChangedBy() {
         return this.changedBy;
     }
     
     public void setChangedBy(Integer changedBy) {
         this.changedBy = changedBy;
     }
 
     public Date getDateChanged() {
         return this.dateChanged;
     }
     
     public void setDateChanged(Date dateChanged) {
         this.dateChanged = dateChanged;
     }
 
     public short getRetired() {
         return this.retired;
     }
     
     public void setRetired(short retired) {
         this.retired = retired;
     }
 
     public Integer getRetiredBy() {
         return this.retiredBy;
     }
     
     public void setRetiredBy(Integer retiredBy) {
         this.retiredBy = retiredBy;
     }
 
     public Date getDateRetired() {
         return this.dateRetired;
     }
     
     public void setDateRetired(Date dateRetired) {
         this.dateRetired = dateRetired;
     }
 
     public String getRetireReason() {
         return this.retireReason;
     }
     
     public void setRetireReason(String retireReason) {
         this.retireReason = retireReason;
     }
 
     public String getUuid() {
         return this.uuid;
     }
     
     public void setUuid(String uuid) {
         this.uuid = uuid;
     }
 
     public Double getSortWeight() {
         return this.sortWeight;
     }
     
     public void setSortWeight(Double sortWeight) {
         this.sortWeight = sortWeight;
     }
 
     public Set getHrTrainingClasses() {
         return this.hrTrainingClasses;
     }
     
     public void setHrTrainingClasses(Set hrTrainingClasses) {
         this.hrTrainingClasses = hrTrainingClasses;
     }
 
 }
