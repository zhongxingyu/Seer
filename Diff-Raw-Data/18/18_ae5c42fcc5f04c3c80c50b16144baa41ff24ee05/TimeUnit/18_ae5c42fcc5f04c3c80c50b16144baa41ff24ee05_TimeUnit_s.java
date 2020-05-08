 package org.araqne.logdb;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public enum TimeUnit {
 	Second(Calendar.SECOND, 1000L, "s"), Minute(Calendar.MINUTE, 60 * 1000L, "m"), Hour(Calendar.HOUR_OF_DAY, 60 * 60 * 1000L,
 			"h"), Day(Calendar.DAY_OF_MONTH, 86400 * 1000L, "d"), Week(Calendar.WEEK_OF_YEAR, 7 * 86400 * 1000L, "w"), Month(
 			Calendar.MONTH, 30 * 86400 * 1000L, "mon"), Year(Calendar.YEAR, 365 * 86400 * 1000L, "y");
 
 	private int calendarField;
 	private long millis;
 	private String acronym;
 
 	private static final int[] mon2Spans = new int[] { 1, 3, 5, 7, 9, 11 };
 	private static final int[] mon3Spans = new int[] { 1, 4, 7, 10 };
 	private static final int[] mon4Spans = new int[] { 1, 5, 9 };
 	private static final int[] mon6Spans = new int[] { 1, 7 };
 
 	private TimeUnit(int calendarField, long millis, String acronym) {
 		this.calendarField = calendarField;
 		this.millis = millis;
 		this.acronym = acronym;
 	}
 
 	public long getMillis() {
 		return millis;
 	}
 
 	public int getCalendarField() {
 		return calendarField;
 	}
 
 	@Override
 	public String toString() {
 		return acronym;
 	}
 
 	public static Date getKey(Date date, TimeSpan timeSpan) {
 		TimeUnit spanField = timeSpan.unit;
 
 		if (spanField == TimeUnit.Month) {
 			Calendar c = Calendar.getInstance();
 			c.setTime(date);
 			int mon = c.get(Calendar.MONTH);
 
 			if (timeSpan.amount == 2)
 				c.set(Calendar.MONTH, mon2Spans[mon / 2] - 1);
 			else if (timeSpan.amount == 3)
 				c.set(Calendar.MONTH, mon3Spans[mon / 3] - 1);
 			else if (timeSpan.amount == 4)
 				c.set(Calendar.MONTH, mon4Spans[mon / 4] - 1);
 			else if (timeSpan.amount == 6)
 				c.set(Calendar.MONTH, mon6Spans[mon / 6] - 1);
 
 			c.set(Calendar.DAY_OF_MONTH, 1);
 			c.set(Calendar.HOUR_OF_DAY, 0);
 			c.set(Calendar.MINUTE, 0);
 			c.set(Calendar.SECOND, 0);
 			c.set(Calendar.MILLISECOND, 0);
 			return c.getTime();
 		} else if (spanField == TimeUnit.Year) {
 			Calendar c = Calendar.getInstance();
 			c.setTime(date);
 			c.set(Calendar.MONTH, 0);
 			c.set(Calendar.DAY_OF_MONTH, 1);
 			c.set(Calendar.HOUR_OF_DAY, 0);
 			c.set(Calendar.MINUTE, 0);
 			c.set(Calendar.SECOND, 0);
 			c.set(Calendar.MILLISECOND, 0);
 			return c.getTime();
 		} else {
 			long time = date.getTime();
 
 			int spanAmount = timeSpan.amount;
			time += 291600000L; // base to Monday, 00:00:00
 			time -= time % (spanField.millis * spanAmount);
			time -= 291600000L;
 			return new Date(time);
 		}
 	}
 }
