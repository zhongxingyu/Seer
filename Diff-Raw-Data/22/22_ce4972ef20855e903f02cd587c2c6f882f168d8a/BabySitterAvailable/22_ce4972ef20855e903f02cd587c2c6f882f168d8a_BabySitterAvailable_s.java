 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import play.data.format.Formats;
 import play.db.ebean.Model;
 
 /**
  * Computer entity managed by Ebean
  */
 @SuppressWarnings("serial")
 @Entity 
 public class BabySitterAvailable extends Model {
 
 	@Id
 	public Long id;
 
 	@ManyToOne
 	public BabySitter babySitter;
 
     @Formats.DateTime(pattern="yyyy-MM-dd HH-mm-ss")
 	public Date startTime;
     
     @Formats.DateTime(pattern="yyyy-MM-dd HH-mm-ss")
     public Date endTime;
     
     public boolean requested;
     
     public boolean agreed;
     
     /**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,BabySitterAvailable> find = new Finder<Long,BabySitterAvailable>(Long.class, BabySitterAvailable.class);
     
     
     /**
      * Marks a babysitter as requested for that time period
      * @param id Unique id of the available period
      */
     public static void setRequested(String id) {
         BabySitterAvailable availability = find.byId(Long.parseLong(id));
        availability.requested = true;
        availability.save();
        /*BabySitterAvailable.find.all().get(Integer.parseInt(id)).delete();
         BabySitterAvailable bsa = new BabySitterAvailable();
         bsa.id = availability.id;
         bsa.requested = true;
         bsa.endTime = availability.endTime;
         bsa.startTime = availability.startTime;
         bsa.babySitter = availability.babySitter;
        bsa.save();*/
        //find.byId(Long.parseLong(id)).requested = true;
        //find.byId(Long.parseLong(id)).update();
         System.out.println("requested= " + find.byId(Long.parseLong(id)).requested);
     }
     
     /**
      * Marks a babysitter as agreed for that time period
      * @param id Unique id of the available period
      */
     public static void setAgreed(String id) {
         BabySitterAvailable availability = find.byId(Long.parseLong(id));
        availability.agreed = true;
        availability.save();
         System.out.println("agreed= " + find.byId(Long.parseLong(id)).agreed);
     }
     
 }
 
