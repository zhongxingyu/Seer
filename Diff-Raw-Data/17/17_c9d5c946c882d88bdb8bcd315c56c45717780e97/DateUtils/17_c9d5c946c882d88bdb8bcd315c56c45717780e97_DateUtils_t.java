 package com.laud.doodo.date;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  * @author: Laud
  * @email: htd0324@gmail.com
  * @date: 2012-9-11 下午3:28:34
  * @copyright: www.dreamoriole.com
  */
 public class DateUtils {
	private final static Logger logger = LoggerFactory
			.getLogger(DateUtils.class);
 	// 默认时间格式
 	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
 
 	/**
 	 * 字符串转为时间
 	 * 
 	 * @param date
 	 * @param pattern
 	 * @return
 	 */
 	public static Date string2Date(String date, String pattern) {
 		SimpleDateFormat format = new SimpleDateFormat(pattern);
 		try {
			return format.parse(date);
 		} catch (ParseException e) {
			logger.error("invalid date value: " + date, e);
 		}
		return null;
 	}
 
 	/**
 	 * 字符串转为时间
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date string2Date(String date) {
 		return string2Date(date, DEFAULT_DATE_PATTERN);
 	}
 
 	/**
 	 * 时间转为字符串
 	 * 
 	 * @param date
 	 * @return yyyy-MM-dd HH:mm:ss格式
 	 */
 	public static String date2String(Date date) {
 		SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
 		formatter.setTimeZone(TimeZone.getDefault());
 		return formatter.format(date);
 	}
 
 	/**
 	 * 时间转为字符串
 	 * 
 	 * @param date
 	 * @param pattern
 	 *            时间样式
 	 * @return
 	 */
 	public static String date2String(Date date, String pattern) {
 		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
 		formatter.setTimeZone(TimeZone.getDefault());
 		return formatter.format(date);
 	}
 
 	/**
 	 * 给指定日期加/减年
 	 * 
 	 * @param date
 	 * @param year
 	 *            正数加，负数减
 	 * @return
 	 */
 	public static Date addYear(Date date, int year) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		calendar.add(Calendar.YEAR, year);
 		return calendar.getTime();
 	}
 
 	/**
 	 * 给指定日期加/减指定月
 	 * 
 	 * @param date
 	 * @param month
 	 *            正数加，负数减
 	 * @return
 	 */
 	public static Date addMonth(Date date, int month) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		calendar.add(Calendar.MONTH, month);
 		return calendar.getTime();
 	}
 
 	/**
 	 * 给指定日期加/减指定天数
 	 * 
 	 * @param date
 	 * @param day
 	 *            正数加，负数减
 	 * @return
 	 */
 	public static Date addDay(Date date, int day) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		calendar.add(Calendar.DAY_OF_MONTH, day);
 		return calendar.getTime();
 	}
 
 	/**
 	 * 计算两个指定日期相差几天
 	 * 
 	 * @param arg0
 	 * @param arg1
 	 * @return 返回大减小的相差天数
 	 */
 	public static long differHowManyDays(Date arg0, Date arg1) {
 		long d1 = arg0.getTime();
 		long d2 = arg1.getTime();
 
 		return Math.abs(d1 - d2) / (60 * 60 * 24 * 1000);
 	}
 
 	/**
 	 * 取得一天的开始时间
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date getStartOfTheDay(Date date) {
 		String value = date2String(date, "yyyy-MM-dd 00:00:00");
 		return string2Date(value);
 	}
 
 	/**
 	 * 取得一天的结束时间
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date getEndOfTheDay(Date date) {
 		String value = date2String(date, "yyyy-MM-dd 23:59:59");
 		return string2Date(value);
 	}
 
 	/**
 	 * 取得指定月份的第一天
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date getFirstDayOfTheMonth(Date date) {
 		String value = date2String(date, "yyyy-MM-01 00:00:00");
 		return string2Date(value);
 	}
 
 	/**
 	 * 取得指定月份的最后一天
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date getLastDayOfTheMonth(Date date) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
 		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
 				lastDay, 0, 0, 0);
 		return calendar.getTime();
 	}
 }
