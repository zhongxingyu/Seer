 package models;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.util.List;
 
 @Entity
 public class Poll extends Model {
 
     @Id
    public String id;
 
     @Constraints.Required
     public String name;
 
     //public String description;
 
     //public List<Option> options;
 
     /**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,Poll> find = new Finder<Long,Poll>(Long.class, Poll.class);
 }
