 package com.github.lazycure;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Time {
 	public final static long ONE_SECOND = 1000;//milliseconds
     public final static long SECONDS = 60;
 
     public final static long ONE_MINUTE = ONE_SECOND * SECONDS;
     public final static long MINUTES = 60;
 
     public final static long ONE_HOUR = ONE_MINUTE * MINUTES;
     public final static long HOURS = 24;
 
     public final static long ONE_DAY = ONE_HOUR * HOURS;
     
     public static Date getCurrentDate(){
 		Date date = new Date();
 		return date;
 	}
 
     /**
      * converts time (in milliseconds) to human-readable format
      *  "<dd>hh:mm:ss"
      */
     public static String millisToShortDHMS(long duration) {
       String res = "";
       duration /= ONE_SECOND;
       int seconds = (int) (duration % SECONDS);
       duration /= SECONDS;
       int minutes = (int) (duration % MINUTES);
       duration /= MINUTES;
       int hours = (int) (duration % HOURS);
       int days = (int) (duration / HOURS);
       if (days == 0) {
         res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
       } else {
         res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
       }
       return res;
     }
     
     public static String GetYYYYMMDD(Date date){
     	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
     	String dateString = dateFormat.format(date);
 		return dateString;
     }
     
     /**
      * Converts date into ISO 8601 string
      * @param date
      * @return String date in ISO 8601 format http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html
      */
     
     public static String GetYYYYMMddHHmmss(Date date){
     	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	String dateString = dateFormat.format(date);
 		return dateString;
     }
 
    /**
     * Formats the Date object to be displayed as time, as millisToShortDHMS do
     * @param Date object presenting time, usually duration
     * @return formatted short string presenting readable time
     */
	public static String format(Date dateTime) {
		return Time.millisToShortDHMS(dateTime.getTime());
	}

 }
