 package com.huhuo.integration.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class TimeUtils {
 	
	enum HPattern {
 		SHORT("yyyy-MM-dd"),	// yyyy-MM-dd
 		LONG("yyyy-MM-dd HH:mm:ss"),	// yyyy-MM-dd HH:mm:ss
 		;
 		
 		private String value;
 		HPattern(String value) {
 			this.value = value;
 		}
 		/**
 		 * get value of this enumeration
 		 */
 		public String getValue() {
 			return value;
 		}
 	}
 	/**
 	 * 获取距离baseTime偏离offset个月的日期
 	 * @param offset
 	 * @param baseTime
 	 * @return
 	 */
 	public static Date offsetMonth(int offset, Date baseTime) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(baseTime);
 		c.add(Calendar.MONTH, offset);
 		return c.getTime();
 	}
 	/**
 	 * @param offset 偏离量（负数则为baseTime之前offset天的时间）
 	 * @param baseTime 基准时间
 	 * @return 返回baseTime为基准，偏离offset天后的时间
 	 */
 	public static Date offsetDate(int offset, long baseTime) {
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(baseTime);
 		c.add(Calendar.DATE, offset);
 		return c.getTime();
 	}
 	/**
 	 * @see #offsetDate(int, long)
 	 */
 	public static Date offsetDate(int offset, Date baseTime) {
 		return offsetDate(offset, baseTime.getTime());
 	}
 	/**
 	 * Adds or subtracts the specified amount of time to the given calendar field,
      * based on the calendar's rules. For example, to subtract 5 days from
      * the current time of the calendar, you can achieve it by calling:
      * <p><code>add(Calendar.DAY_OF_MONTH, -5)</code>.
 	 * @param offset 偏离量（负数则为baseTime之前offset个小时的时间）
 	 * @param baseTime 基准时间
 	 * @return 返回baseTime为基准，偏离offset小时后的时间
 	 */
 	public static Date offsetHour(int offset, long baseTime) {
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(baseTime);
 		c.add(Calendar.HOUR_OF_DAY, offset);
 		return c.getTime();
 	}
 	/**
 	 * @see #offsetDate(int, long)
 	 */
 	public static Date offsetHour(int offset, Date baseTime) {
 		return offsetHour(offset, baseTime.getTime());
 	}
 	/**
 	 * 
 	 * @param offset 偏离量（负数则为baseTime之前offset分钟的时间）
 	 * @param baseTime 基准时间
 	 * @return 返回baseTime为基准，偏离offset分钟后的时间
 	 */
 	public static long offsetMinute(int offset, long baseTime) {
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(baseTime);
 		c.add(Calendar.MINUTE, offset);
 		return c.getTimeInMillis();
 	}
 	/**
 	 * @see #offsetMinute(int, long)
 	 */
 	public static long offsetMinute(int offset, Date baseTime) {
 		return offsetMinute(offset, baseTime.getTime());
 	}
 	/**计算给定Calendar的日期是当年第几天,如果是1月1日则返回去年的天数。
 	 * @param offset （负数则为Calendar之前offset天）
 	 * @param c
 	 * @return
 	 */
 	public static int getDaysOfYear(int offset,Calendar c){
 		int days = c.get(Calendar.DAY_OF_YEAR)+offset;
 		if(days==0){
 			c.set(Calendar.YEAR, c.get(Calendar.YEAR)-1);
 			c.set(Calendar.MONTH, 11);
 			c.set(Calendar.DAY_OF_MONTH,31);
 			days=c.get(Calendar.DAY_OF_YEAR);
 		}
 		return days;
 	}
 	/**计算时间差
 	 * @param startTime
 	 * @param endTime
 	 * @return 两个时间相差的毫秒数
 	 */
 	public static long dateDiff(Date startTime,Date endTime){
 		Calendar sc = GregorianCalendar.getInstance();
 		Calendar ec = GregorianCalendar.getInstance();
 		sc.setTime(startTime);
 		ec.setTime(endTime);
 		long dateDiff = ec.getTimeInMillis()-sc.getTimeInMillis();
 		return dateDiff;
 	}
 	/**
 	 * 将字符串date按照格式pattern转换成日期
 	 * @param date
 	 * @param pattern
 	 * @return
 	 */
 	public static Date parse(String date, String pattern) {
 		try {
			return new SimpleDateFormat(pattern).parse(date);
 		} catch (ParseException e) {
			e.printStackTrace();
 		}
		return null;
 	}
 	/**
 	 * @see #parse(String, String)
 	 */
 	public static Date parse(String date, HPattern pattern) {
 		return parse(date, pattern.getValue());
 	}
 	/**
 	 * 将日期date按照格式pattern转换成日期
 	 * @param date
 	 * @param pattern
 	 * @return
 	 */
 	public static String parse(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
 	}
 	/**
 	 * @see #parse(Date, String)
 	 */
 	public static String parse(Date date, HPattern pattern) {
 		return parse(date, pattern.getValue());
 	}
 	/**
 	 * 获取日期date的开始时间，如2012-06-25 00:00:00
 	 * @param date
 	 * @return
 	 */
 	public static Date getDateBeginFor(Date date) {
 		if(date == null)
 			return null;
 		String dateStr = parse(date, HPattern.SHORT);
 		return parse(dateStr, HPattern.SHORT);
 	}
 	/**
 	 * 获取日期date的结束时间，如2012-06-25 23:59:59
 	 * @param date
 	 * @return
 	 */
 	public static Date getDateEndFor(Date date) {
 		if(date == null)
 			return null;
 		Date tomorrow = offsetDate(1, getDateBeginFor(date));
 		return new Date(tomorrow.getTime() - 1000);
 	}
 	/**
 	 * 获取date所在的天次（如2012-06-18，则为18）
 	 * @param date
 	 * @return
 	 */
 	public static Integer getDate(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		return c.get(Calendar.DAY_OF_MONTH);
 	}
 	/**
 	 * 获取date所在的周次
 	 * @param date
 	 * @return
 	 */
 	public static Integer getWeek(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		c.setFirstDayOfWeek(Calendar.MONDAY);
 		return c.get(Calendar.WEEK_OF_YEAR);
 	}
 	/**
 	 * 获取date所在的月份
 	 * @param date
 	 * @return
 	 */
 	public static Integer getMonth(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		return c.get(Calendar.MONTH) + 1;
 	}
 	/**
 	 * 获取date所在的年份
 	 * @param date
 	 * @return
 	 */
 	public static Integer getYear(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		c.setFirstDayOfWeek(Calendar.MONDAY);
 		return c.get(Calendar.YEAR);
 	}
 	/**
 	 * 获取某年某周的开始日期
 	 * @param year 如果为null，则默认为今天所在的年份
 	 * @param week
 	 * @return
 	 */
 	public static Date getWeekBegin(Integer year, Integer week) {
 		Calendar c = Calendar.getInstance();
 		c.setFirstDayOfWeek(Calendar.MONDAY);
 		if(year == null) {
 			c.setTime(new Date());
 			year = c.get(Calendar.YEAR);
 		}
 		c.set(Calendar.YEAR, year);
 		c.set(Calendar.WEEK_OF_YEAR, week);
 		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		return getDateBeginFor(c.getTime());
 	}
 	/**
 	 * 获取date所在周次的开始日期
 	 * @param date
 	 * @return
 	 * @see #getWeekBegin(Integer, Integer)
 	 */
 	public static Date getWeekBegin(Date date) {
 		Integer week = getWeek(date);
 		Integer year = getYear(date);
 		return getWeekBegin(year, week);
 	}
 	/**
 	 * 获取某年某周的结束日期
 	 * @param year 如果为null，则默认为今天所在的年份
 	 * @param week
 	 * @return
 	 */
 	public static Date getWeekEnd(Integer year, Integer week) {
 		Calendar c = Calendar.getInstance();
 		c.setFirstDayOfWeek(Calendar.MONDAY);
 		if(year == null) {
 			c.setTime(new Date());
 			year = c.get(Calendar.YEAR);
 		}
 		c.set(Calendar.YEAR, year);
 		c.set(Calendar.WEEK_OF_YEAR, week);
 		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
 		return getDateEndFor(c.getTime());
 	}
 	/**
 	 * 获取date所在周次的结束日期
 	 * @param date
 	 * @return
 	 * @see #getWeekEnd(Integer, Integer)
 	 */
 	public static Date getWeekEnd(Date date) {
 		Integer week = getWeek(date);
 		Integer year = getYear(date);
 		return getWeekEnd(year, week);
 	}
 	/**
 	 * 获取date所在月的第一天（如，2012-06-01 00:00:00）
 	 * @param date
 	 * @return
 	 */
 	public static Date getMonthBegin(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		c.set(Calendar.DAY_OF_MONTH, 01);
 		return getDateBeginFor(c.getTime());
 	}
 	/**
 	 * 获取date所在月的第最后一天（如，2012-06-30 23:59:59）
 	 * @param date
 	 * @return
 	 */
 	public static Date getMonthEnd(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(getMonthBegin(date));
 		c.add(Calendar.MONTH, 1);
 		c.add(Calendar.DAY_OF_MONTH, -1);
 		return getDateEndFor(c.getTime());
 	}
 	/**
 	 * 格式化date
 	 * @param date
 	 * @param longPattern 如果fullPattern==true，则返回yyyy-MM-dd HH:mm:ss长格式日期，否则返回yyyy-MM-dd短格式日期
 	 * @return
 	 */
 	public static String format(Date date, boolean longPattern) {
 		String dateStr = null;
 		if(longPattern) {
 			dateStr = parse(date, HPattern.LONG);
 		} else {
 			dateStr = parse(date, HPattern.SHORT);
 		}
 		return dateStr;
 	}
 	/**
 	 * @see #format(Date, boolean)
 	 * @param date
 	 * @return
 	 */
 	public static String format(Date date) {
 		return format(date, true);
 	}
 	/**
 	 * @see #format(Date)
 	 * @param date
 	 * @return
 	 */
 	public static String format(Long date) {
 		return format(new Date(date));
 	}
 	
 	/**
 	 * 获取用户年龄
 	 * @param birthDay
 	 * @return
 	 * @throws Exception
 	 */
 	public static int getAge(Date birthDay)  {
         Calendar cal = Calendar.getInstance();
 
         if (cal.before(birthDay)) {
             throw new IllegalArgumentException(
                 "The birthDay is before Now.It's unbelievable!");
         }
 
         int yearNow = cal.get(Calendar.YEAR);
         int monthNow = cal.get(Calendar.MONTH);
         int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
         cal.setTime(birthDay);
 
         int yearBirth = cal.get(Calendar.YEAR);
         int monthBirth = cal.get(Calendar.MONTH);
         int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
 
         int age = yearNow - yearBirth;
 
         if (monthNow <= monthBirth) {
             if (monthNow == monthBirth) {
                 //monthNow==monthBirth
                 if (dayOfMonthNow < dayOfMonthBirth) {
                     age--;
                 } 
             } else {
                 //monthNow>monthBirth
                 age--;
             }
         } 
 
         return age;
     }
 	
 	public static void main(String[] args) throws Exception{
 		
 		int age = getAge(new SimpleDateFormat("yyyy-MM-dd").parse("2012-04-07"));
 		System.out.println(age);
 		
 		String pattern = "yyyy-MM-dd HH:mm:ss";
 		Date today = new Date();
 		System.out.println("今天所在周次：" + getWeek(today));
 		System.out.println("dfsd  " + parse("2011-12-30", "yyyy-MM-dd"));
 		System.out.println("2011-12-25所在周次：" + getWeek(parse("2011-12-25", "yyyy-MM-dd")));
 		System.out.println("2012-01-01所在周次：" + getWeek(parse("2012-01-01", "yyyy-MM-dd")));
 		System.out.println("2012-01-02所在周次：" + getWeek(parse("2012-01-02", "yyyy-MM-dd")));
 		System.out.println("2012-01-08所在周次：" + getWeek(parse("2012-01-08", "yyyy-MM-dd")));
 		
 		System.out.println("今天所在周的周一：" + parse(getWeekBegin(2012, 25), pattern));
 		System.out.println("今天所在周的周日：" + parse(getWeekEnd(2012, 25), pattern));
 		
 		System.out.println("今天所在周的周日：" + parse(getWeekEnd(2012, 27), pattern));
 		
 		System.out.println("今天所在周的周一：" + parse(getWeekBegin(today), pattern));
 		System.out.println("今天所在周的周日：" + parse(getWeekEnd(today), pattern));
 		
 		System.out.println(getWeek(parse("2012-01-03", "yyyy-MM-dd")));
 		System.out.println(getWeek(parse("2012-01-02", "yyyy-MM-dd")));
 		System.out.println(getWeek(parse("2012-01-01", "yyyy-MM-dd")));
 		System.out.println(getWeek(parse("2011-12-31", "yyyy-MM-dd")));
 		
 		System.out.println(getWeekBegin(parse("2012-06-24", "yyyy-MM-dd")).getTime());
 		System.out.println(getWeekEnd(new Date()).getTime());
 		
 	}
 }
