 package models;
 
 import play.db.ebean.Model;
import play.db.ebean.Model.Finder; // Import Finder as sometimes Play! shows compilation error "not found: type Finder"
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 /**
  * Model class that maps to DB table usergroup.
  *
  * Date: 08/11/13
  * Time: 13:19
  *
  * @author      Sav Balac
  * @version     %I%, %G%
  * @since       1.0
  */
 
 @Entity
 public class UserGroup extends Model {
 
     @Id
     public Long         userId;
     @Id
     public Long         groupId;
 
 
     /**
      * Generic query helper for entity UserGroup.
      */
     public static Finder<Long, UserGroup> find = new Finder<Long, UserGroup>(Long.class, UserGroup.class);
 
 
 }
