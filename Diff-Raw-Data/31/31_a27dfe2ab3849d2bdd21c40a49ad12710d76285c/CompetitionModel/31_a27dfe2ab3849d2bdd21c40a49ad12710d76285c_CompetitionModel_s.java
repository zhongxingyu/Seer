 package models.dbentities;
 
 import models.competition.CompetitionType;
 import models.management.ManageableModel;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 
 import javax.persistence.*;
 import java.util.Date;
 
 /**
  * Database entity for competition.
  *
  * @author Kevin Stobbelaar.
  *
  */
 @Entity
 @Table(name="contests")
 public class CompetitionModel extends ManageableModel {
 
     private static final long serialVersionUID = 4L;
 
     /**
      * creator
      */
     public String creator;
 
     /**
      * id
      */
     @Id
     public String id;
 
     /**
      * name
      */
     @Constraints.Required
     public String name;
 
     /**
      * type
      */
     @Constraints.Required
     @Enumerated(EnumType.STRING)
     public CompetitionType type;
 
     /**
      * active
      */
     @Constraints.Required
     public boolean active;
 
     /**
      * start time
      */
     @Formats.DateTime(pattern = "dd/MM/yyyy HH:mm:ss")
     @Constraints.Required
     public Date starttime;
 
     /**
      * end time
      */
     @Formats.DateTime(pattern = "dd/MM/yyyy HH:mm:ss")
     @Constraints.Required
     public Date endtime;
 
     /**
      * duration
      */
     @Constraints.Required
     public int duration;
 
     /**
      * Returns those values that have to be represented in a table.
      *
      * @return array with the current values of the fields to be represented in the table
      */
     @Override
     public String[] getFieldValues() {
        String[] fieldValues = {name, type.toString(), Boolean.toString(active), starttime.toString(), endtime.toString(), creator, "" + duration};
         return fieldValues;
     }
 
     /**
      * Returns the id of the object.
      *
      * @return id
      */
     @Override
     public String getID() {
         return id;
     }
 
     /**
      * Returns the type of this competition.
      * @return competition type
      */
     public String getTypeString(){
         return type.toString();
     }
 
 }
