// $Id: TimeUtil.java,v 1.5 2000/06/05 12:13:08 locher Exp $
 
 package jobmatch.business.util;
 
 import java.sql.*;
 import java.util.Calendar;
 
 /**
  *  Time Utilities
  *
  *  @since May 23 2000
  *  @author $Author: locher $
 *  @version $Revision: 1.5 $
  **/
 public class TimeUtil {
 
     /**
      * constructs a date from the given fields 
      * @param year year of the date
      * @param month month (1-12) of the date
      * @param day day (1-31) of the date
      **/
     public static Date getDate(int year, int month, int day)  {
 	Calendar cal = Calendar.getInstance();
 	cal.set(year, month - 1, day);
 	return new Date(cal.getTime().getTime()); 
     }
 
     /** @return Timestamp with the current system time **/
     public static Timestamp getTimeNow() {
 	return new Timestamp(System.currentTimeMillis());
     }
     
     /** extracts the year from the date **/
     public static int getYear(Date date) {
 	Calendar cal = Calendar.getInstance();
 	cal.setTime(date);
 	return cal.get(Calendar.YEAR);
     }
 
     /** 
      * extracts the month of the date
      * range of month (1-12)
      **/
     public static int getMonth(Date date) {
 	Calendar cal = Calendar.getInstance();
 	cal.setTime(date);
	return cal.get(Calendar.MONTH) + 1;
     }
 
     /** 
      * extracts the day ot the month of the date
      * range of month (1-31)
      **/  
     public static int getDay(Date date) {
 	Calendar cal = Calendar.getInstance();
 	cal.setTime(date);
 	return cal.get(Calendar.DAY_OF_MONTH);
     }
 
 } //class
 
 // Document history
 /*
  * $Log: TimeUtil.java,v $
 * Revision 1.5  2000/06/05 12:13:08  locher
 * fix month 1 based
 *
  * Revision 1.4  2000/06/05 12:00:14  locher
  * implemented TimeUtil
  *
  * Revision 1.3  2000/06/05 11:38:45  loeffel
  * Set some temporary methods to return date
  *
  * Revision 1.2  2000/06/02 15:24:13  loeffel
  * irgend
  *
  * Revision 1.1  2000/05/23 09:53:05  locher
  * time utilities
  *
  */
