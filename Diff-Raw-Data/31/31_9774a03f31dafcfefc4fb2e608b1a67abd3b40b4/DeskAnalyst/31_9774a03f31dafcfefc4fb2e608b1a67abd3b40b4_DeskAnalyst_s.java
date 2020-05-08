 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 /**
  * Model class that maps to DB table deskanalyst.
  *
  * Date: 23/10/13
  * Time: 13:18
  *
  * @author      Sav Balac
  * @version     %I%, %G%
  * @since       1.0
  */
 
 @Entity
 public class DeskAnalyst extends Model {
 
     @Id
     public Long         deskId;
     @Id
     public Long         analystId;
     public byte[]       coordinator;
 
 
     /**
      * Generic query helper for entity DeskAnalyst.
      */
     public static Finder<Long,DeskAnalyst> find = new Finder<Long,DeskAnalyst>(Long.class, DeskAnalyst.class);
 
 
 }
