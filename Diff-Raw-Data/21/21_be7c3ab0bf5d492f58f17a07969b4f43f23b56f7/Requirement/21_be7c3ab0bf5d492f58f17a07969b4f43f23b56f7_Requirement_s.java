 package models.tm;
 
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import models.tm.test.Script;
 import models.tm.test.Tag;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import tree.persistent.Node;
 import tree.persistent.NodeName;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"project_id", "naturalId"})})
 public class Requirement extends ProjectModel implements Node {
 
     @NodeName
     @Required
     public String name;
 
 //    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
 //    public RequirementType type;
 
     @MaxSize(5000)
     public String description;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public TMUser createdBy;
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public List<Tag> tags;
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public List<Script> linkedScripts;
 
     public Requirement(Project project) {
         super(project);
     }
 
     @Override
     public Object clone() throws CloneNotSupportedException {
         // TODO
         return super.clone();
     }
 
 }
