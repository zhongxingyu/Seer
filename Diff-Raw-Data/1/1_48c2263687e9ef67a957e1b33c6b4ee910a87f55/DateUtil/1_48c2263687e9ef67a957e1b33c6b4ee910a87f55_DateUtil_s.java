 package com.zotyo.diary.util;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class DateUtil {
 	public static Date resetHMS(Date d) {
 		//resetting hours:minutes:seconds to zero		
 		Calendar cal=Calendar.getInstance();
 		cal.setTime(d);
 		cal.set(Calendar.HOUR_OF_DAY, 12);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		return cal.getTime();
 	}
 }
