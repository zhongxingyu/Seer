 package models;
 
 import play.*;
 import play.data.validation.*;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 import java.util.*;
 
 
 /**
  * The Class Woman.
  */
 @Entity
 public class Woman extends Model implements Logicable {
     
     /**
      * The Enum Outcome.
      */
     public static enum Outcome {
         
         /** The None. */
         NONE,
         /** The Alive. */
         ALIVE,
         /** The Dead. */
         DEAD,
         /** The Still Birth. */
         STILL_BIRTH,
         /** The Live Birth. */
         LIVE_BIRTH
     };
     
     /**
      * The Enum Trigger.
      */
     public static enum Trigger {
         
         /** The NONE. */
         NONE,
         /** The REGISTRATION. */
         REGISTRATION,
         /** The DELIVERY. */
         DELIVERY,
         /** The OUTCOME. */
         OUTCOME
     };
     
     /**
      * The Enum Event.
      */
     public static enum Event {
         
         /** The NONE. */
         NONE,
         /** The REGISTRATION. */
         REGISTRATION,
         /** The DELIVERY. */
         DELIVERY,
         /** The OUTCOME. */
         OUTCOME,
         /** The LMP. */
         LMP
         
     };
     
     /**
      * The Enum StatusCode.
      */
     public static enum Status {
         
        /** The Any. */
         ANY,
        /** The Complete. */
        COMPLETE,
        /** The Incomplete. */
        INCOMPLETE
     };
     
     /** The woman name. */
     @Required
     public String name;
     
     /** The woman's unique ID. */
     @Required
     public Long   UID;
     
     /** The woman's husband name. */
     @Required
     public String husbandName;
     
     /** The sector ID. */
     @Required
     public Long   sectorId;
     
     /** The house hold ID. */
     @Required
     public Long   hhId;
     
     /** The mauza name. */
     public String mauzaName;
     
     /** The ti pin cd. */
     public Long   tiPinCd;
     
     /** The ac area. */
     public Long   acArea;
     
     /** The urine positive week. */
     public Long   urinePositiveWeek;
     
     /** The last menstrual period. */
     public Date   lmp;
     
     /** The registration Date. */
     public Date   registered;
     
     /** The delivery Date. */
     public Date   deliveryDate;
     
     /** The status. */
     public Short  status;
     
     /**
      * The Constructor.
      * 
      * @param UID
      *            the woman's unique ID
      * @param name
      *            the name
      * @param husbandName
      *            the husband name
      * @param sectorId
      *            the sector ID
      * @param hhId
      *            the house hold ID
      */
     public Woman(Long UID, String name, String husbandName, Long sectorId, Long hhId) {
     
         this.UID = UID;
         this.name = name;
         this.husbandName = husbandName;
         this.sectorId = sectorId;
         this.hhId = hhId;
         
     }
     
     /* (non-Javadoc)
      * @see play.db.jpa.JPABase#_save()
      */
     @Override
     public void _save() {
     
         this.registered = new Date();
         super._save();
         Form.invoke(Trigger.REGISTRATION, this);
     }
     
     /* (non-Javadoc)
      * @see models.Logicable#getEventDate(models.Woman.Event)
      */
     @Override
     public Date getEventDate(Event event) {
     
         switch (event) {
             case REGISTRATION:
                 return this.registered;
             case DELIVERY:
                 return this.deliveryDate;
             case LMP:
                 return this.lmp;
         }
         
         return new Date();
     }
 }
