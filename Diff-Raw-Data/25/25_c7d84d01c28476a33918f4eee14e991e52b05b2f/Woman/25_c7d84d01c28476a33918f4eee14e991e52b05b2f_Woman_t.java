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
 public class Woman extends Model {
     
    public static enum Outcome {
        ALIVE, DEAD, STILL_BIRTH, LIVE_BIRTH, REGISTER;
        
        public String toString() {
        
            String output = name().toString();
            output = output.charAt(0) + output.substring(1).toLowerCase();
            return output;
        }
        
     }
     
     
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
     
     /** The lmp. */
     public Date   lmp;
     
     /** The status. */
     public Short  status;
     
     /** Registration Date. */
     public Date   registered = new Date();
     
     
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
     
     @Override
     public void _save() {
     
         super._save();
         Form.invoke(Outcome.REGISTER);
         
         /*
         Form pef = Form.find("shortName", "PEF").first();
         Form ses = Form.find("shortName", "SES").first();
         
         Calendar newDate = Calendar.getInstance();
         newDate.add(Calendar.DATE, 7);
         
         new FormEntity(newDate.getTime(), pef, this).save();
         new FormEntity(newDate.getTime(), ses, this).save();
         */
     }
     
 }
