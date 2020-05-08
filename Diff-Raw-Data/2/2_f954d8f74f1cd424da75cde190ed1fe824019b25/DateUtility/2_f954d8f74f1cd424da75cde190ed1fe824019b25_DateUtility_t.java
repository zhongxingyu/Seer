 package com.reckonlabs.reckoner.domain.utility;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 /**
  * This class provides static helper methods for date manipulation and
  * evaluation using {@link GregorianCalendar} and {@link Date}.
  */
 public class DateUtility {
 
 	/**
 	 * This method determines whether or not the provided date is before the
 	 * current time.
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @return boolean
 	 */
 	public static boolean isBeforeNow(Date date) {
 		return date.before(now());
 	}
 
 	/**
 	 * This method determines whether or not the provided date is after the
 	 * current time.
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @return boolean
 	 */
 	public static boolean isAfterNow(Date date) {
 		return date.after(now());
 	}
 
 	/**
 	 * This method determines whether or not the provided date is before the
 	 * current date (no time stamp).
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @return boolean
 	 */
 	public static boolean isBeforeToday(Date date) {
 		return date.before(today());
 	}
 
 	/**
 	 * This method determines whether or not the provided date is after the
 	 * current date (no time stamp).
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @return boolean
 	 */
 	public static boolean isAfterToday(Date date) {
 		return date.after(today());
 	}
 
 	/**
 	 * This method gets the {@link Date} of today, without time stamp.
 	 * 
 	 * @return {@link Date}
 	 */
 	public static Date today() {
 		return getInstanceWithoutTimestamp().getTime();
 	}
 
 	/**
 	 * This method gets the {@link Date} of today, with time stamp.
 	 * 
 	 * @return {@link Date}
 	 */
 	public static Date now() {
 		
 		return getInstance().getTime();
 	}
 
 	/**
 	 * This method adds the provided number of days to the input {@link Date}.
 	 * Pass negative int to subtract.
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @param days
 	 *            int
 	 * @return {@link Date}
 	 */
 	public static Date addDays(Date date, int days) {
 		GregorianCalendar calendar = fromDate(date);
 		calendar.add(GregorianCalendar.DATE, days);
 		date = calendar.getTime();
 		return date;
 	}
 
 	/**
 	 * This method adds the provided number of months to the input {@link Date}.
 	 * Pass negative int to subtract.
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @param months
 	 *            int
 	 * @return {@link Date}
 	 */
 	public static Date addMonths(Date date, int months) {
 		GregorianCalendar calendar = fromDate(date);
 		calendar.add(GregorianCalendar.MONTH, months);
 		date = calendar.getTime();
 		return date;
 	}
 
 	/**
 	 * This method adds the provided number of years to the input {@link Date}.
 	 * Pass negative int to subtract.
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @param years
 	 *            int
 	 * @return {@link Date}
 	 */
 	public static Date addYears(Date date, int years) {
 		GregorianCalendar calendar = fromDate(date);
 		calendar.add(GregorianCalendar.YEAR, years);
 		date = calendar.getTime();
 		return date;
 	}
 
 	/**
 	 * This method adds the provided number of days to today's date. Pass
 	 * negative int to subtract.
 	 * 
 	 * @param days
 	 *            int
 	 * @return {@link Date}
 	 */
 	public static Date todayPlusDays(int days) {
 		GregorianCalendar calendar = getInstanceWithoutTimestamp();
 		calendar.add(GregorianCalendar.DATE, days);
 		return calendar.getTime();
 	}
 
 	/**
 	 * This method adds the provided number of months to today's date. Pass
 	 * negative int to subtract.
 	 * 
 	 * @param months
 	 *            int
 	 * @return {@link Date}
 	 */
 	public static Date todayPlusMonths(int months) {
 		GregorianCalendar calendar = getInstanceWithoutTimestamp();
 		calendar.add(GregorianCalendar.MONTH, months);
 		return calendar.getTime();
 	}
 
 	/**
 	 * This method adds the provided number of years to today's date. Pass
 	 * negative int to subtract.
 	 * 
 	 * @param years
 	 *            int
 	 * @return {@link Date}
 	 */
 	public static Date todayPlusYears(int years) {
 		GregorianCalendar calendar = getInstanceWithoutTimestamp();
 		calendar.add(GregorianCalendar.YEAR, years);
 		return calendar.getTime();
 	}
 
 	/**
 	 * This method gets the {@link GregorianCalendar} of today, with time stamp.
 	 * 
 	 * @return {@link GregorianCalendar}
 	 */
 	public static GregorianCalendar getInstance() {
 		TimeZone.setDefault(TimeZone
				.getTimeZone("SystemV/EST5EDT"));
 		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
 		return calendar;
 	}
 
 	/**
 	 * This method gets the {@link GregorianCalendar} of today, without time
 	 * stamp.
 	 * 
 	 * @return {@link GregorianCalendar}
 	 */
 	public static GregorianCalendar getInstanceWithoutTimestamp() {
 		
 		Date todaysDate = formatToDate(now(), "MM/dd/yyyy");
 		GregorianCalendar calendar = getInstance();
 		calendar.setTime(todaysDate);
 
 		return calendar;
 	}
 
 	/**
 	 * This method builds a {@link GregorianCalendar} from the provided
 	 * {@link Date}.
 	 * 
 	 * @param date
 	 *            {@link Date}
 	 * @return {@link GregorianCalendar}
 	 */
 	public static GregorianCalendar fromDate(Date date) {
 		GregorianCalendar calendar = getInstance();
 		calendar.setTime(date);
 		return calendar;
 	}
 
 	public static XMLGregorianCalendar toXML(Date date) {
 		XMLGregorianCalendar calendar;
 		try {
 			calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(
 					fromDate(date));
 		} catch (DatatypeConfigurationException e) {
 			calendar = null;
 		}
 		return calendar;
 	}
 
 	public static Date asDate(XMLGregorianCalendar xgc) {
 		if (xgc == null) {
 			return null;
 		} else {
 			return xgc.toGregorianCalendar().getTime();
 		}
 	}
 
 	/**
 	 * This method can build a date with given year, month, and day. Month
 	 * should be a value between 1 and 12.
 	 * 
 	 * @param day
 	 *            {@link Integer}
 	 * @param month
 	 *            {@link Integer}, 1 through 12
 	 * @param year
 	 *            {@link Integer}
 	 * @return date {@link Date}
 	 */
 	public static Date createDate(int day, int month, int year) {
 		GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day);
 		return calendar.getTime();
 	}
 
 	/**
 	 * @param date
 	 *            {@link Date}
 	 * @param pattern
 	 *            {@link String}
 	 * @return {@link String}
 	 */
 	public static String formatDate(Date date, String pattern) {
 		DateFormat format = new SimpleDateFormat(pattern);
 		return format.format(date);
 	}
 
 	/**
 	 * @param date
 	 *            {@link Date}
 	 * @param pattern
 	 *            {@link String}
 	 * @return {@link String}
 	 */
 	public static Date formatToDate(Date date, String pattern) {
 		DateFormat format = new SimpleDateFormat(pattern);
 		try {
 			return format.parse(format.format(date));
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static String dateStringW3C(Date date) {
 
 		String pattern = "yyyy-MM-dd'T'HH:mm:ssz";
 		DateFormat format = new SimpleDateFormat(pattern);
 		return format.format(date);
 
 	}
 
 }
