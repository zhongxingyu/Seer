 package com.orangeleap.tangerine.web.common;
 
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Calendar;
 
 /**
  * Extend the normal CustomDateEditor to know how to deal with
  * seasonal dates, which are just specified as Month-Day, without
  * a year. Will correctly parse a standard date format (MM/dd/yyyy)
  * along with dates that are just MMMMM-d format. Will add the current
  * year as the year component, unless that date would fall earlier
  * than current date, which means Date is for next year.
  * @version 1.0
  */
 public class TangerineCustomDateEditor extends CustomDateEditor {
 
 
     private DateFormat alternateDateFormat = new SimpleDateFormat("MMMMM-d");
 
 
     public TangerineCustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
         super(dateFormat, allowEmpty);
     }
 
     public TangerineCustomDateEditor(DateFormat dateFormat, boolean allowEmpty, int exactDateLength) {
         super(dateFormat, allowEmpty, exactDateLength);
     }
 
     @Override
     public void setAsText(String text) throws IllegalArgumentException {
         try {
             super.setAsText(text);
         } catch(IllegalArgumentException ex) {
 
             try {
 				// need to add a year component if
                 Date d = this.alternateDateFormat.parse(text);
                 Calendar now = Calendar.getInstance();
                 int year = now.get(Calendar.YEAR);
 
                 Calendar c = Calendar.getInstance();
                 c.setTime(d);
                 c.set(Calendar.YEAR, year);
 
                 // if the date is before current date, assume it is next year
                if(c.get(Calendar.DAY_OF_YEAR) < now.get(Calendar.DAY_OF_YEAR)) {
                     c.set(Calendar.YEAR, year+1);
                 }
                 
                 setValue(c.getTime());
 			}
 			catch (ParseException parseEx) {
 				IllegalArgumentException iae =
 						new IllegalArgumentException("Could not parse date: " + parseEx.getMessage());
 				iae.initCause(parseEx);
 				throw iae;
 			}
         }
     }
 }
