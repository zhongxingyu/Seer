 package com.nityankhanna.androidutils.system.datetime;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * Created by Nityan Khanna on Jan 03 2014.
  */
 public class DateUtils {
 
	private static final String ERROR_MESSAGE = "An error has occurred when parsing the date time. " +
			" Reference http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html for date time formatting help.";
 	private DateUtils() {
 	}
 
 	public static Date convertToLocalTimeAsDate(Date date, String dateTimeFormat) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
 		long when = 0;
 
 		try {
 			when = dateFormat.parse(dateToString(date, dateTimeFormat)).getTime();
 
 			return new Date(when + TimeZone.getDefault().getRawOffset() + (TimeZone.getDefault().inDaylightTime(new Date())
 					? TimeZone.getDefault().getDSTSavings() : 0));
 		} catch (ParseException e) {
 			e.printStackTrace();
			throw new IllegalArgumentException(ERROR_MESSAGE);
 		}
 	}
 
 	public static String convertToLocalTimeAsString(Date date, String dateTimeFormat) {
 		Date localDate = convertToLocalTimeAsDate(date, dateTimeFormat);
 		return dateToString(localDate, dateTimeFormat);
 	}
 
 	public static Date convertToLocalTimeAsDate(String date, String dateTimeFormat) {
 		return convertToLocalTimeAsDate(stringToDate(date, dateTimeFormat), dateTimeFormat);
 	}
 
 	public static String convertToLocalTimeAsString(String date, String dateTimeFormat) {
 		Date localDate = convertToLocalTimeAsDate(date, dateTimeFormat);
 		return dateToString(localDate, dateTimeFormat);
 	}
 
 	public static Date convertToNewFormat(String dateToFormat, String currentFormat, String targetFormat) {
 		SimpleDateFormat originalFormat = new SimpleDateFormat(currentFormat);
 		SimpleDateFormat newFormat = new SimpleDateFormat(targetFormat);
 
 		try {
 			Date date = originalFormat.parse(dateToFormat);
 			String formattedDateAsString = newFormat.format(date);
 			return newFormat.parse(formattedDateAsString);
 		} catch (ParseException e) {
 			e.printStackTrace();
			throw new IllegalArgumentException(ERROR_MESSAGE);
 		}
 	}
 
 	public static String dateToString(Date date, String dateTimeFormat) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
 		return dateFormat.format(date);
 	}
 
 	public static Date stringToDate(String date, String dateTimeFormat) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
 
 		try {
 			return dateFormat.parse(date);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("The date time format " + dateTimeFormat + " does not match the date time format of" + date +
 					". Reference http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html for date time formatting help.");
 		}
 	}
 }
