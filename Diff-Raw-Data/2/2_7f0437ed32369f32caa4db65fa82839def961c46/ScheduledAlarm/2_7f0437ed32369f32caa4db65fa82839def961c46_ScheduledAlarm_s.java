 package net.clonecomputers.lab.todo;
 
 import static java.util.Calendar.*;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 
 import org.junit.Test;
 
 public class ScheduledAlarm {
 
 	public static final int NO_VALUE = Integer.MIN_VALUE;
 	public static final int WILDCARD = 60;
 	
 	private Calendar NOW = new GregorianCalendar();
 	
 	private Date soonestDate = null;
 	
 	private final int ye;
 	private final int mo;
 	private final int da;
 	private final int ho;
 	private final int mi;
 	
 	private Boolean isValid = null;
 	
 	/**
 	 * Initializes a blank scheduled alarm (all the fields are set to NO_VALUE)
 	 */
 	public ScheduledAlarm() {
 		this(NO_VALUE,NO_VALUE,NO_VALUE,NO_VALUE,NO_VALUE);
 	}
 	
 	public ScheduledAlarm(int year, int month, int day, int hour, int minute) {
 		ye = year;
 		mo = month;
 		da = day;
 		ho = hour;
 		mi = minute;
 	}
 	
 	public Date getSoonestDate() {
 		NOW.setTime(new Date());
 		if(isValid != null && !isValid) return null;
 		if(soonestDate == null || NOW.getTime().after(soonestDate)) {
 			soonestDate = getSoonestDateAfter(NOW);
 			isValid = soonestDate != null;
 		}
 		return soonestDate;
 	}
 	
 	private Date getSoonestDateAfter(Calendar curTime) {
 		if(ye == NO_VALUE || mo == NO_VALUE || da == NO_VALUE || ho == NO_VALUE || mi == NO_VALUE) return null;
 		Calendar alarmTime = new GregorianCalendar(1,1,1,0,0,0);
 		alarmTime.set(ye,mo,da,ho,mi);
 		if(ye == WILDCARD) alarmTime.set(YEAR, curTime.get(YEAR));
 		if(mo == WILDCARD) alarmTime.set(MONTH, curTime.get(MONTH));
 		if(da == WILDCARD) alarmTime.set(DATE, curTime.get(MONTH));
 		if(ho == WILDCARD) alarmTime.set(HOUR_OF_DAY, curTime.get(HOUR_OF_DAY));
 		if(mi == WILDCARD) {
 			alarmTime.set(MINUTE, curTime.get(MINUTE));
 			alarmTime.add(MINUTE, 1);
 		}
 		if(ho == WILDCARD) {
 			while(!curTime.getTime().before(alarmTime.getTime())) {
 				alarmTime.add(HOUR_OF_DAY, 1);
 			}
 		}
 		if(da == WILDCARD) {
 			while(!curTime.getTime().before(alarmTime.getTime())) {
 				alarmTime.add(DATE, 1);
 			}
 		}
 		if(mo == WILDCARD) {
			while(!curTime.getTime().before(alarmTime.getTime()) || da > alarmTime.getActualMaximum(DATE)) {
 				alarmTime.add(MONTH, 1);
 			}
 			alarmTime.set(DATE, da);
 		}
 		if(ye != WILDCARD) {
 			if(!curTime.getTime().before(alarmTime.getTime())) return null;
 		} else {
 			while(!curTime.getTime().before(alarmTime.getTime()) || da > alarmTime.getActualMaximum(DATE)) {
 				alarmTime.add(YEAR, 1);
 			}
 			alarmTime.set(DATE, da);
 		}
 		return alarmTime.getTime();
 	}
 	
 	@SuppressWarnings("unused")
 	private Date[] getSoonestDates(int number) {
 		NOW.setTime(new Date());
 		return getSoonestDatesAfter(NOW, number);
 	}
 	
 	private Date[] getSoonestDatesAfter(Calendar curTime, int number) {
 		if(number < 1) throw new IllegalArgumentException("for getSoonestDates(), number must at least be one which it is not");
 		Date[] dates = new Date[number];
 		for(int i = 0; i < number; i++) {
 			dates[i] = getSoonestDateAfter(curTime);
 			curTime.setTime(dates[i]);
 		}
 		return dates;
 	}
 
 	public int getYear() {
 		return ye;
 	}
 
 	public int getMonth() {
 		return mo;
 	}
 
 	public int getDay() {
 		return da;
 	}
 
 	public int getHour() {
 		return ho;
 	}
 
 	public int getMinute() {
 		return mi;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof ScheduledAlarm)) return false;
 		ScheduledAlarm a = (ScheduledAlarm) o;
 		return ye == a.ye && mo == a.mo && da == a.da && ho == a.ho && mi == a.mi;
 	}
 	
 	public boolean isValid() {
 		if(isValid == null) {
 			isValid = (getSoonestDate() != null);
 		}
 		return isValid;
 	}
 
 	@Override
 	/**
 	 * This formats the ScheduledAlarm into the form MM/dd/yyyy hh:mm
 	 * Wildcards are displayed as "**" (or "****" for year)
 	 * @return A string version of the SheduledAlarm
 	 */
 	public String toString() {
 		return new String(mo + "/" + da + "/" + ye + " " + (ho < 10 ? "0" : "") + ho + ":" + (mi < 10 ? "0" : "") + mi).replaceAll("([^0-9])60([^0-9])", "$1**$2").replaceAll("/\\*\\* ", "/**** ");
 	}
 	
 	public class AlarmTest {
 		
 		private final Date testDate = new GregorianCalendar(2012, 11, 4, 12, 14).getTime();
 		private final Date testLeapYearDate = new GregorianCalendar(2016, 1, 29, 12, 14).getTime();
 		private final Scanner in = new Scanner(System.in);
 
 		/*@Test
 		public void test() {
 			assertTrue(false);
 		}*/
 		
 		@Test
 		public void testDateEquals() {
 			Date alarmDate = new ScheduledAlarm(2012, 11, 4, 12, 14).getSoonestDate();
 			printTestInfo("testDateEquals", alarmDate);
 			assertTrue(testDate.equals(alarmDate));
 		}
 		
 		@Test
 		public void testDateNotEqual() {
 			Date alarmDate = new ScheduledAlarm(2013, 1, 1, 0, 0).getSoonestDate();
 			printTestInfo("testDateNotEqual", alarmDate);
 			assertFalse(testDate.equals(alarmDate));
 		}
 		
 		@Test
 		public void testWildcardYearEqual() {
 			Date alarmDate = new ScheduledAlarm(ScheduledAlarm.WILDCARD, 11, 4, 12, 14).getSoonestDate();
 			printTestInfo("testWildcardYearEqual", alarmDate);
 			assertTrue(testDate.equals(alarmDate));
 		}
 		
 		@Test
 		public void testWildcardYearEqualWithLeapYear() {
 			Date alarmDate = new ScheduledAlarm(ScheduledAlarm.WILDCARD, 1, 29, 12, 14).getSoonestDate();
 			printTestInfo("testWildcardYearEqualWithLeapYear", alarmDate, testLeapYearDate);
 			assertTrue(testLeapYearDate.equals(alarmDate));
 		}
 		
 		@Test
 		public void testWildcardMonthEqual() {
 			Date alarmDate = new ScheduledAlarm(2012, ScheduledAlarm.WILDCARD, 4, 12, 14).getSoonestDate();
 			printTestInfo("testWildcardMonthEqual", alarmDate);
 			assertTrue(testDate.equals(alarmDate));
 		}
 		
 		@Test
 		public void testFromUserInput() {
 			System.out.println("Hi World!");
 			String response = "";
 			int aYear = 0;
 			int aMonth = 0;
 			int aDay = 0;
 			int aHour = 0;
 			int aMinute = 0;
 			int eYear = 0;
 			int eMonth = 0;
 			int eDay = 0;
 			int eHour = 0;
 			int eMinute = 0;
 	        while(!response.equalsIgnoreCase("y")) {
 	        	System.out.println("AlarmTime:");
 	        	System.out.println("Input Year:");
 	        	aYear = in.nextInt();
 	        	System.out.println("Input Month (0-11):");
 	        	aMonth = in.nextInt();
 	        	System.out.println("Input Day (1-?):");
 	        	aDay = in.nextInt();
 	        	System.out.println("Input Hour (0-23):");
 	        	aHour = in.nextInt();
 	        	System.out.println("Input Minute (0-59):");
 	        	aMinute = in.nextInt();
 	        	System.out.println();
 	        	
 	        	System.out.println("ExpectedNextTime:");
 	        	System.out.println("Input Year:");
 	        	eYear = in.nextInt();
 	        	System.out.println("Input Month (0-11):");
 	        	eMonth = in.nextInt();
 	        	System.out.println("Input Day (1-?):");
 	        	eDay = in.nextInt();
 	        	System.out.println("Input Hour (0-23):");
 	        	eHour = in.nextInt();
 	        	System.out.println("Input Minute (0-59):");
 	        	eMinute = in.nextInt();
 	        	
 	        	ScheduledAlarm alarm = new ScheduledAlarm(aYear, aMonth, aDay, aHour, aMinute);
 	        	long startTime = System.currentTimeMillis();
 	        	Date alarmTime = alarm.getSoonestDate();
 	        	long elapsedTime = System.currentTimeMillis() - startTime;
 	        	Date expectedNextTime = new GregorianCalendar(eYear, eMonth, eDay, eHour, eMinute, 0).getTime();
 	        	
 	        	printTestInfo("testFromUserInput, time: " + elapsedTime, alarmTime, expectedNextTime);
 	        	
 	        	assertTrue(expectedNextTime.equals(alarmTime));
 	        	
 	    		System.out.println("Quit? (y)");
 	    		response = in.nextLine();
 	        }
 		}
 		
 		private void printTestInfo(String testName, Date alarmDate) {
 			System.out.println("-------------------------------------------");
 			System.out.println("Test: " + testName);
 			System.out.println("    Test Date:  " + testDate);
 			System.out.println("    Alarm Date: " + alarmDate);
 			System.out.println("    Equal?:     " + testDate.equals(alarmDate));
 			System.out.println("-------------------------------------------");
 		}
 		
 		private void printTestInfo(String testName, Date alarmDate, Date theTestDate) {
 			System.out.println("-------------------------------------------");
 			System.out.println("Test: " + testName);
 			System.out.println("    Test Date:  " + theTestDate);
 			System.out.println("    Alarm Date: " + alarmDate);
 			System.out.println("    Equal?:     " + theTestDate.equals(alarmDate));
 			System.out.println("-------------------------------------------");
 		}
 
 	}
 
 }
