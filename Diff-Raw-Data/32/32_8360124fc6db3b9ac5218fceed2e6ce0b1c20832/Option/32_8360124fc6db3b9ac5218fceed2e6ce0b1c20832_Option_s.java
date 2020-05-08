 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 @Entity
 public class Option extends Model {
 
     @Id
    public String id;
 
     public String name;
 
     public String description;
 
     public int upCount;
 
     //public String link;
 
     //public String imgURL;
 }
