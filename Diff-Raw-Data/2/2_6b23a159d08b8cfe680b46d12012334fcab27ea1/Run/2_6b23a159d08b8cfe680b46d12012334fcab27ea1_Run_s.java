 package models.tm.test;
 
 import java.util.Date;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Query;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import models.tm.Defect;
 import models.tm.ProjectModel;
 import models.tm.User;
 import play.db.jpa.JPA;
 import play.db.jpa.JPABase;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "project_id"})})
 public class Run extends ProjectModel implements ParameterHolder {
 
     /** this flag indicates that the Run is being created by the user but was not saved yet, ths it is not visible in the list **/
     public boolean temporary = true;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public Instance instance;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = true)
     public User tester;
 
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public Defect defect;
 
     public Date executionDate;
 
     public Integer status;
 
     public List<RunStep> getSteps() {
         return RunStep.find("from RunStep r where r.run = ?", this).<RunStep>fetch();
     }
 
     public List<RunParam> getParams() {
         return RunParam.find("from RunParam p where p.run = ?", this).<RunParam>fetch();
     }
 
     public RunParam getParam(String name) {
         return RunParam.find("from RunParam p where p.run = ? and p.name = ?", this, name).<RunParam>first();
     }
 
     public void updateStatus() {
         boolean passed = true;
         for(RunStep step : getSteps()) {
             if(step.executionStatus == ExecutionStatus.FAILED) {
                 this.executionStatus = ExecutionStatus.FAILED;
                 passed = false;
                 break;
             }
             if(step.executionStatus == ExecutionStatus.NOT_COMPLETED || step.executionStatus == ExecutionStatus.NOT_RUN) {
                 this.executionStatus = ExecutionStatus.NOT_COMPLETED;
                 passed = false;
                 break;
             }
         }
         if(passed) {
             this.executionStatus = ExecutionStatus.PASSED;
         }
 
         // TODO this feels like a Play bug - we should not need to invoke the PreUpdate callback manually
         doSave();
         save();
     }
 
     @Override
     public <T extends JPABase> T delete() {
         // delete all the attached elements
         Query deleteSteps = JPA.em().createQuery("delete from RunStep s where s.run = :run");
         deleteSteps.setParameter("run", this);
         deleteSteps.executeUpdate();
 
         Query deleteParams = JPA.em().createQuery("delete from RunParam p where p.run = :run");
         deleteParams.setParameter("run", this);
         deleteParams.executeUpdate();
 
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
 
 
 }
