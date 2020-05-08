 package models.tm.test;
 
 import java.util.Date;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import models.tm.ProjectModel;
 import models.tm.approach.TestCycle;
 import models.tm.User;
import play.db.jpa.JPABase;
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
 
     public Integer status;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = true)
     public User responsible;
 
     public Date plannedExecution;
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public List<Tag> tags;
 
     public String getTagNames() {
         return JavaExtensions.join(tags, ", ");
     }
 
     public List<InstanceParam> getParams() {
         return InstanceParam.find("from InstanceParam p where p.instance = ?", this).<InstanceParam>fetch();
     }
 
     public List<Run> getRuns() {
         return Run.find("from Run r where r.instance = ?", this).<Run>fetch();
     }
 
     public void updateStatus() {
         // instance status = status of the last run
         Run run = Run.find("from Run r where r.instance = ? order by r.executionDate desc", this).<Run>first();
         if (run != null) {
             executionStatus = run.executionStatus;
 
            // TODO this is a Play bug - we should not need to invoke the PreUpdate callback manually
             doSave();
             save();
         }
     }
 
    @Override
    public <T extends JPABase> T delete() {
        // remove associated entities
        for(InstanceParam p : getParams()) {
            p.delete();
        }

        return super.delete();
    }

     @Transient
     public ExecutionStatus executionStatus;
 
     @PostLoad
     public void doLoad() {
         if (status != null) {
             this.executionStatus = ExecutionStatus.fromPosition(status);
         }
     }
 
     @PreUpdate
     @PrePersist
     public void doSave() {
         if (executionStatus != null) {
             this.status = executionStatus.getPosition();
         }
     }
 
     public static List<Instance> find(Script script, TestCycle cycle) {
         return Instance.find("from Instance i where i.script = ? and i.testCycle = ?", script, cycle).fetch();
     }
 
 
 }
