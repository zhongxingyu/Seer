 package models.tm;
 
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import models.tm.test.Tag;
 import models.tm.test.TagHolder;
 import play.data.validation.MaxSize;
 import play.templates.JavaExtensions;
 
 /**
  * @author Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name="id", columnNames = {"project_id", "naturalId"})})
 public class Defect extends ProjectModel implements TagHolder{
 
     public String name;
 
     @MaxSize(8000)
     public String description;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public TMUser assignedTo;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public TMUser submittedBy;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public DefectStatus status;
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE})
     public List<Tag> tags;
 
     public Defect(Project project) {
         super(project);
     }
 
     public String getTagNames() {
         return JavaExtensions.join(tags, ", ");
     }
 
     public List<Tag> getTags() {
         return tags;
     }

    public String toString(){
        return this.name;
    }
 }
