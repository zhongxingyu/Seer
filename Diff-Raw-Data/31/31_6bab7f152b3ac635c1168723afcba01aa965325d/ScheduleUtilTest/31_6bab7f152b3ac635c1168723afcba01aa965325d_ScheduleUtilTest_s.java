 package jre.pathtimes.test;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import jre.pathtimes.ScheduleUtil;
 import jre.pathtimes.Station;
 import jre.pathtimes.TrainLine;
 
 public class ScheduleUtilTest extends PathTimesUnitTest {
 	
 	/**
 	 * Tests converting a date string into a calendar instance.
 	 */
 	public void testConvertDateStringToCalendar() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR, 4);
 		cal.set(Calendar.MINUTE, 4);
 		cal.set(Calendar.AM_PM, Calendar.PM);
 		assertClose(cal, ScheduleUtil.convertDateStringToCalendar("04:04 PM"));
 		assertClose(cal, ScheduleUtil.convertDateStringToCalendar("4:04 PM"));	
 		
 		Calendar cal2 = Calendar.getInstance();
 		cal2.set(Calendar.HOUR, 4);
 		cal2.set(Calendar.MINUTE, 4);
 		cal2.set(Calendar.AM_PM, Calendar.AM);
 		
 		assertClose(cal2, ScheduleUtil.convertDateStringToCalendar("04:04 AM"));
 		
 		Calendar cal3 = Calendar.getInstance();
 		cal3.set(Calendar.HOUR_OF_DAY, 0);
 		cal3.set(Calendar.MINUTE, 4);
 		
 		assertClose(cal3, ScheduleUtil.convertDateStringToCalendar("12:04 AM"));
 		assertEquals(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), ScheduleUtil.convertDateStringToCalendar("12:04 AM").get(Calendar.DAY_OF_WEEK));
 		
 		Calendar cal4 = Calendar.getInstance();
 		cal4.set(Calendar.HOUR_OF_DAY, 0);
 		cal4.set(Calendar.MINUTE, 13);
 		assertClose(cal4, ScheduleUtil.convertDateStringToCalendar("12:13 AM"));
 		
 		Calendar cal5 = Calendar.getInstance();
 		cal5.set(Calendar.HOUR_OF_DAY, 12);
 		cal5.set(Calendar.MINUTE, 26);
 		assertClose(cal5, ScheduleUtil.convertDateStringToCalendar("12:26 PM"));
 		
 	}
 	
 	/**
 	 * Tests the compare methods that compares two calendar instances.
 	 */
 	public void testCompare() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR, 4);
 		cal.set(Calendar.MINUTE, 4);
 		cal.set(Calendar.AM_PM, Calendar.PM);
 		
 		assertEquals(0, ScheduleUtil.compare(ScheduleUtil.convertDateStringToCalendar("04:04 PM"), cal));
 		assertTrue(ScheduleUtil.compare(ScheduleUtil.convertDateStringToCalendar("04:05 AM"), cal) < 0);
 		assertTrue(ScheduleUtil.compare(ScheduleUtil.convertDateStringToCalendar("04:03 PM"), cal) < 0);
 		assertTrue(ScheduleUtil.compare(ScheduleUtil.convertDateStringToCalendar("04:05 PM"), cal) > 0);
 		assertTrue(ScheduleUtil.compare(ScheduleUtil.convertDateStringToCalendar("05:04 PM"), cal) > 0);
 	}
 
 	/**
 	 * Tests finding appropriate train lines.
 	 */
 	public void testFindAppropriateTrainLine() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR, 4);
 		cal.set(Calendar.MINUTE, 4);
 		cal.set(Calendar.AM_PM, Calendar.PM);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		
 		List<TrainLine> correctResult = new ArrayList<TrainLine>();
 		correctResult.add(TrainLine.HOB_TTRD_WEEKDAY);
 		correctResult.add(TrainLine.JSQ_TTRD_VIA_HOB_WEEKDAY);
 		
 		assertEquals(correctResult, ScheduleUtil.findAppropriateTrainLines(
 				Station.Hoboken, Station.ThirtyThird, cal));
 	}
 	
 	/**
 	 * Tests getting the next arrival time.
 	 */
 	public void testGetNextArrivalTimes() {
 		
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR, 4);
 		cal.set(Calendar.MINUTE, 4);
 		cal.set(Calendar.AM_PM, Calendar.PM);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(
 				Station.Hoboken, Station.ThirtyThird, cal, 5);
 		
 		assertNotNull(nextArrivalTimes);
 		
 		assertClose(ScheduleUtil.convertDateStringToCalendar("04:12 PM"), nextArrivalTimes.get(0));
 	}
 	
 	/**
 	 * Tests getting the next arrival times, second case.
 	 */
 	public void testGetNextArrivalTimes_2() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR, 10);
 		cal.set(Calendar.MINUTE, 43);
 		cal.set(Calendar.AM_PM, Calendar.PM);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
 	  
 		
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(
 				Station.Hoboken, Station.ThirtyThird, cal, 5);
 		
 		assertNotNull(nextArrivalTimes);
 		assertEquals(5, nextArrivalTimes.size());
 		
 		assertClose(ScheduleUtil.convertDateStringToCalendar("10:52 PM"),
 				nextArrivalTimes.get(0));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:07 PM"),
 				nextArrivalTimes.get(1));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:26 PM"),
 				nextArrivalTimes.get(2));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:56 PM"),
 				nextArrivalTimes.get(3));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:11 AM"),
 				nextArrivalTimes.get(4));
 	}
 	
 	/**
 	 * Tests getting arrival times, 3rd case.
 	 */
 	public void testGetNextArrivalTimes_3() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR, 10);
 		cal.set(Calendar.MINUTE, 43);
 		cal.set(Calendar.AM_PM, Calendar.PM);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
 
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(
 				Station.ThirtyThird, Station.Hoboken, cal, 5);
 
 		assertNotNull(nextArrivalTimes);
 		assertEquals(5, nextArrivalTimes.size());
 		
 		assertClose(ScheduleUtil.convertDateStringToCalendar("10:57 PM"), nextArrivalTimes.get(0));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:12 PM"), nextArrivalTimes.get(1));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:27 PM"), nextArrivalTimes.get(2));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:42 PM"), nextArrivalTimes.get(3));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:12 AM"), nextArrivalTimes.get(4));
 	}
 	
 	public void testGetNextArrivalTime_4() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY, 23);
 		cal.set(Calendar.MINUTE, 43);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
 
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(
 				Station.Newark, Station.WTC, cal, 5);
 
 		assertNotNull(nextArrivalTimes);
 		assertEquals(5, nextArrivalTimes.size());
 		
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:00 AM"),
 				nextArrivalTimes.get(0));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:30 AM"),
 				nextArrivalTimes.get(1));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("01:00 AM"),
 				nextArrivalTimes.get(2));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("01:30 AM"),
 				nextArrivalTimes.get(3));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("02:00 AM"),
 				nextArrivalTimes.get(4));
 	}
 	
 	public void testGetNextArrivalTime_NON_CONNECTED() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY, 23);
 		cal.set(Calendar.MINUTE, 43);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
 		
 		List<Calendar> results = ScheduleUtil.getNextArrivalTimes(Station.Hoboken, Station.Newark, cal, 5);
 		
 		assertTrue(results == null);
 	}
 	
 	public void testGetNextArrivalTime_6() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY, 11);
 		cal.set(Calendar.MINUTE, 55);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
 
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(
 				Station.Hoboken, Station.ThirtyThird, cal, 5);
 		
 		List<TrainLine> trainLine = ScheduleUtil.findAppropriateTrainLines(
 				Station.Hoboken, Station.ThirtyThird, cal);
 		
 		assertEquals(2, trainLine.size());
 
 		assertNotNull(nextArrivalTimes);
 		assertEquals(5, nextArrivalTimes.size());
 		
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:02 PM"),
 				nextArrivalTimes.get(0));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:12 PM"),
 				nextArrivalTimes.get(1));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:22 PM"),
 				nextArrivalTimes.get(2));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:32 PM"),
 				nextArrivalTimes.get(3));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:42 PM"),
 				nextArrivalTimes.get(4));
 	}
 	
 	public void testGetNextArrivalTime_7() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY, 23);
 		cal.set(Calendar.MINUTE, 30);
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
 
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(
 				Station.Nineth, Station.TwentyThird, cal, 5);
 
 		assertNotNull(nextArrivalTimes);
 		assertEquals(5, nextArrivalTimes.size());
 		
 		assertClose(ScheduleUtil.convertDateStringToCalendar("11:36 PM"),
 				nextArrivalTimes.get(0));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:06 AM"),
 				nextArrivalTimes.get(1));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:21 AM"),
 				nextArrivalTimes.get(2));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("12:36 AM"),
 				nextArrivalTimes.get(3));
 		assertClose(ScheduleUtil.convertDateStringToCalendar("01:06 AM"),
 				nextArrivalTimes.get(4));
 	}
 	
 }
