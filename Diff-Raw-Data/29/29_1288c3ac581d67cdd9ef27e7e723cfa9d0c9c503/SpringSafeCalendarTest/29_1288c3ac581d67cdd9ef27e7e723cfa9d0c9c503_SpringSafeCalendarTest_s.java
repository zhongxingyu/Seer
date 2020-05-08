 package org.oddjob.arooa.utils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import junit.framework.TestCase;
 
 public class SpringSafeCalendarTest extends TestCase {
 
	SimpleDateFormat resultFormat = 
		new SimpleDateFormat("yyyy-MM-dd HH:mm");
 	
	public void testDifferentSpringTimes() throws ParseException {
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
 		
		Calendar cal = Calendar.getInstance();
 		cal.set(2005, 02, 27);
 		
 		TimeParser timeFormat; 
 		
 		timeFormat = new TimeParser(
 				new SpringSafeCalendar(cal)); 
 		
 		assertEquals("2005-03-27 00:55",
 				resultFormat.format(
 						timeFormat.parse("00:55")));
 		
 		assertEquals("2005-03-27 02:00",
 				resultFormat.format(
 						timeFormat.parse("01:00")));
 		
 		assertEquals("2005-03-27 02:00",
 				resultFormat.format(
 						timeFormat.parse("01:30")));
 		
 		assertEquals("2005-03-27 02:00",
 				resultFormat.format(
 						timeFormat.parse("02:00")));
 		
 		assertEquals("2005-03-27 02:05",
 				resultFormat.format(
 						timeFormat.parse("02:05")));
		
		TimeZone.setDefault(null);
 	}
 	
 	public void testDifferentAutumnTimes() throws ParseException {
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		Calendar cal = Calendar.getInstance();
 		cal.set(2005, 9, 30);
 		
 		TimeParser timeFormat; 
 		
 		timeFormat = new TimeParser(
 				new SpringSafeCalendar(cal)); 
 		
 		assertEquals("2005-10-30 00:55",
 				resultFormat.format(
 						timeFormat.parse("00:55")));
 		
 		assertEquals("2005-10-30 01:00",
 				resultFormat.format(
 						timeFormat.parse("01:00")));
 		
 		assertEquals("2005-10-30 01:30",
 				resultFormat.format(
 						timeFormat.parse("01:30")));
 		
 		assertEquals("2005-10-30 02:00",
 				resultFormat.format(
 						timeFormat.parse("02:00")));
 		
 		assertEquals("2005-10-30 02:05",
 				resultFormat.format(
 						timeFormat.parse("02:05")));
		
		TimeZone.setDefault(null);
 	}
 }
