 package models.statistics;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 public class Report extends Model {
 
     @Id
     public Long id;
     
     @Constraints.Required
     public String name;
 
     public String description;
 
     @ManyToMany()
    public Set<Statistic> statistics;
 
     @ManyToMany()
     public Set<Category> categories;
 
     //@ManyToMany(mappedBy="usergroup")
     //public UserGroup usergroup;
 
     public static Finder<Long,Report> find = new Finder<Long, Report>(
       Long.class, Report.class
     );
 
     @Override
     public String toString(){
         return this.name;
     }
 }
