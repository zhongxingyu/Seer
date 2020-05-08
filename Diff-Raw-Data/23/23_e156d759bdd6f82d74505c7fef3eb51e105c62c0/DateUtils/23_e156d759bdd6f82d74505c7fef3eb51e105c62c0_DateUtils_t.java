 package com.xlthotel.foundation.common;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class DateUtils {
 
 	/**
 	 * 
 	 * @author lxl
 	 * 
 	 * */
 	private DateUtils() {
 	}
 
 	/**
 	 * @param dateFormat
 	 * @param dateString
 	 * @return date
 	 */
 	public static Date parse(String dateFormat, String dateString) {
 		try {
 			return (new SimpleDateFormat(dateFormat)).parse(dateString);
 		} catch (ParseException ex) {
			// ignore
 		}
 		return null;
 	}
 	
 	public static Date parseOrderDate(String dateString) {
 		try {
 			return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(dateString + " 12:00:00");
 		} catch (ParseException ex) {
			// ignore
 		}
 		return null;
 	}
 	
 	/**
 	 * @return
 	 */
 	public static String getNowDate() {
 		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
 		String nowDate = dt.format(new Date());
 		return nowDate;
 	}
 	
 	public static String getDateOne() {
 		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(0);
 		String dateOne = dt.format(c.getTime());
 		return dateOne;
 	}
 	
 	public static String formatDate(String dateFormat, Date dateString) {
 		return (new SimpleDateFormat(dateFormat)).format(dateString);
 	}
 	
 	public static List<Date> splitDate(Date from, Date to) {
 		List<Date> result = new ArrayList<Date>();
 		Calendar c = Calendar.getInstance();
 		c.setTime(from);
 		
 		while (c.getTime().compareTo(to) < 1) {
 			result.add(c.getTime());
 			c.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		
 		return result;
 	}
 }
