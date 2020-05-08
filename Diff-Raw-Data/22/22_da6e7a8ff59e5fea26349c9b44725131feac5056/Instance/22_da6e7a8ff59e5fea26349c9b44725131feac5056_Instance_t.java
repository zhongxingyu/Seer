 package models.project.test;
 
 import java.util.Date;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import models.project.ProjectModel;
 import models.project.approach.TestCycle;
 import models.tm.User;
 import play.templates.JavaExtensions;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "project_id"})})
 public class Instance extends ProjectModel {
 
     public String name;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public Script script;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public TestCycle testCycle;
 
     public String status;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = true)
     public User responsible;
 
     public Date plannedExecution;
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public List<Tag> tags;
 
     public String getTagNames() {
         return JavaExtensions.join(tags, ", ");
     }
 
     public static List<Instance> find(Script script, TestCycle cycle) {
         return Instance.find("from Instance i where i.script = ? and i.testCycle = ?", script, cycle).fetch();
     }
 
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
