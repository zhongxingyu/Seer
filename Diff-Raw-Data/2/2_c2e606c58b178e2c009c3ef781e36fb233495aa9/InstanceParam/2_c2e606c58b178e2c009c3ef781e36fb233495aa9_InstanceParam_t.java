 package models.tm.test;
 
 import javax.persistence.*;
 
 import models.tm.Project;
 import models.tm.ProjectModel;
 
 /**
 * @author Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "project_id"})})
 public class InstanceParam extends ProjectModel {
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public ScriptParam scriptParam;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public Instance instance;
 
     public String value;
 
     public InstanceParam(Project project) {
         super(project);
     }
 }
