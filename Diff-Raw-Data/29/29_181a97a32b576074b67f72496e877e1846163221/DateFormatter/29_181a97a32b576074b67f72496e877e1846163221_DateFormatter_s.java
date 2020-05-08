 package uk.co.eelpieconsulting.common.dates;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class DateFormatter {
    
 	private static final int ONE_MINUTE = 60 * 1000;
 	private static final int ONE_HOUR = ONE_MINUTE * 60;
 	private static final int ONE_DAY = ONE_HOUR * 24;
 	private static final int ONE_WEEK = ONE_DAY * 7;
 	
 	private static final String MMM = "MMM";
 	private static final String YYYY = "yyyy";
     private static final String D_MMM_YYYY = "d MMM yyyy";
     private static final String MMMMM_YYYY = "MMMMM yyyy";
     private static final String D_MMM_YYYY_HHMM = "d MMM yyyy HH:mm";
    private static final String W3C_DATETIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ssZZ";
 
 	public String timeSince(Date then) {
 		if (then == null) {
 			return null;
 		}
 		
         final Date now = Calendar.getInstance().getTime();
         final long deltaInMills = now.getTime() - then.getTime();
         
         if (deltaInMills > ONE_WEEK) {
         	final int weeks = Math.round((deltaInMills / (1000 * 60 * 60 * 24 * 7)));        	
         	return weeks + (weeks == 1 ? " week ago" : " weeks ago"); 
         }        
         if (deltaInMills > ONE_DAY) {
         	final int days = Math.round((deltaInMills / (1000 * 60 * 60 * 24)));
         	return days + (days == 1 ? " day ago" : " days ago");
         }        
         if (deltaInMills > ONE_HOUR) {
         	final int hours = Math.round((deltaInMills / (1000 * 60 * 60)));
         	return hours + (hours == 1 ? " hour ago" : " hours ago");
         }
         if (deltaInMills > ONE_MINUTE) {        	
         	final int minutes = Math.round(deltaInMills / (1000 * 60));
         	return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
         }        
         return "just now";
     }
 
 	public String dayMonthYear(Date date) {
 		return date != null ? new SimpleDateFormat(D_MMM_YYYY).format(date) : null;
 	}
 
 	public String dayMonthYearTime(Date date) {
 		return date != null ? new SimpleDateFormat(D_MMM_YYYY_HHMM).format(date) : null;
 	}
 	
 	public String fullMonthYear(Date date) {
 		return date != null ? new SimpleDateFormat(MMMMM_YYYY).format(date) : null;
 	}
 	
 	public String month(Date date) {
 		return date != null ? new SimpleDateFormat(MMM).format(date) : null;
 	}
 
 	public String year(Date date) {
 		return date != null ? new SimpleDateFormat(YYYY).format(date) : null;
 	}
 	
 	public String w3cDateTime(Date date) {
 		return date != null ? new SimpleDateFormat(W3C_DATETIME_FORMAT).format(date) : null;
 	}
 
 	public String yearMonthDayUrlStub(Date date) {
 		return date != null ? new SimpleDateFormat("yyyy/MMM/d").format(date).toLowerCase() : null;
 	}
 
 	public String yearMonthUrlStub(Date date) {
 		return date != null ? new SimpleDateFormat("yyyy/MMM").format(date).toLowerCase() : null;
 	}
 	
 }
