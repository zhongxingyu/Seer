 package models.tm;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *  nikola
  * Date: 5/19/11
  * Time: 5:34 PM
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name="id", columnNames = {"project_id", "naturalId"})})
 public class Filter extends ProjectModel{
 
     public String name;
     
     @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     public List<FilterConstraint> filterConstraints;
 
 
     @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false)
     public TMUser owner;
     public String entity;
 
     public Filter(String name, String entity){
         this.name = name;
         this.entity = entity;
         this.filterConstraints = new ArrayList<FilterConstraint>();
     }
 
     public Filter(Project project) {
         super(project);
     }
 
     public String getName() {
         return name;
     }
 
     public List<FilterConstraint> getFilterConstraints() {
         return filterConstraints;
     }

    public TMUser getOwner() {
        return owner;
    }

    public String getEntity() {
        return entity;
    }
 }
