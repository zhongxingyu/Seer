 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.aidger.model.validators;
 
 import java.util.Date;
 
 import de.aidger.model.AbstractModel;
 
 /**
  *
  * @author rmbl
  */
 public class DateRangeValidator extends Validator {
 
     /**
      * Initializes the DataRangeValidator class.
      * 
      * @param model
      *            The model to validate
      * @param from
      *            The from date
      * @param to
      *            The to date
      */
     public DateRangeValidator(AbstractModel model, String from, String to) {
         this.model = model;
         this.members = new String[] { from, to };
     }
 
     /**
      * Validate the Date Range
      *
      * @return True if the date range validates
      */
     @Override
     public boolean validate() {
         Date from = (Date)getValueOf(members[0]);
         Date to = (Date)getValueOf(members[1]);
 
        if (from.equals(to) || from.before(to)) {
             return false;
         }
         return true;
     }
 
     /**
      * Empty overridden function
      */
     @Override
     public boolean validateVar(Object o) {
         return false;
     }
 
 }
