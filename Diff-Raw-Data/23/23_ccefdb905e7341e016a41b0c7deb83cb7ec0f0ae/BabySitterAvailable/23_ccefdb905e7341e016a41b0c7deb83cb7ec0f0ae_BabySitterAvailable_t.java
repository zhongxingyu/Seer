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
     public static void setRequested(long id) {
         BabySitterAvailable availability = BabySitterAvailable.find.byId(id);
         BabySitterAvailable bsa = new BabySitterAvailable();
         bsa.id = availability.id;
         bsa.requested = true;
         bsa.endTime = availability.endTime;
         bsa.startTime = availability.startTime;
         bsa.babySitter = availability.babySitter;
         bsa.save();
         System.out.println("requested= " + find.byId(id).requested);
     }
     
     public void setRequested() {
     	this.requested = true;
     	this.save();
     }

    public void setAgreed() {
    	this.agreed = true;
    	this.save();
    }
     
     // I hate this.
     public static BabySitterAvailable findById(Long id) {
     	BabySitterAvailable bsa = BabySitterAvailable.find.byId(id);
     	bsa.babySitter = BabySitter.find.byId(bsa.babySitter.id);
     	return bsa;
     }
     
     /**
      * Marks a babysitter as agreed for that time period
      * @param id Unique id of the available period
      */
     public static void setAgreed(String id) {
         BabySitterAvailable availability = find.byId(Long.parseLong(id));
         BabySitterAvailable.find.byId(Long.parseLong(id)).delete();
         BabySitterAvailable bsa = new BabySitterAvailable();
         bsa.id = availability.id;
         bsa.agreed = true;
         bsa.endTime = availability.endTime;
         bsa.startTime = availability.startTime;
         bsa.babySitter = availability.babySitter;
         bsa.save();
         System.out.println("agreed= " + find.byId(Long.parseLong(id)).agreed);
     }
     
 }
 
