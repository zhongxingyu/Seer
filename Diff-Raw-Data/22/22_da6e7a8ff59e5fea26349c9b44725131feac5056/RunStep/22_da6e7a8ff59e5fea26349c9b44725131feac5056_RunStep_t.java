 package models.project.test;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import models.project.ProjectModel;
 import play.data.validation.MaxSize;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "project_id"})})
 public class RunStep extends ProjectModel {
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public Run run;
 
     public Integer position;
 
     public String name;
 
     @MaxSize(5000)
     public String description;
 
     @MaxSize(5000)
     public String expectedResult;
 
     @MaxSize(5000)
     public String actualResult;
 
     public String status;
 
     @Transient
     public ExecutionStatus executionStatus;
 
     @PostLoad
     public void doLoad() {
         if (status != null) {
             this.executionStatus = ExecutionStatus.valueOf(status);
         }
     }
 
     @PrePersist
     public void doSave() {
        if (executionStatus != null) {
            this.status = executionStatus.getKey();
        }
     }
 
 
 }
