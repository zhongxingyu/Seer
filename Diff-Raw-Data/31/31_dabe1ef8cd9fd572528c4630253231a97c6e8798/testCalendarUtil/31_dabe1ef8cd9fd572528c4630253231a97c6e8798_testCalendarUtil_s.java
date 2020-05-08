 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import hirondelle.date4j.DateTime;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.TimeZone;
 
 import models.Calendar;
 import models.CalendarUtil;
 import models.Event;
 import models.User;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class testCalendarUtil extends UnitTest{
 
 	CalendarUtil util = CalendarUtil.getInstanceToday();
 	
 	@Before
 	public void setToAfixedDay(){
 		util.setAtDate(2011, 10, 11);
 	}
 	
 	@Test
 	public void testMonthAndYearUnmanipulated(){
 		assertEquals(10, util.getMonth());
 		assertEquals(2011, util.getYear());
 	}
 	
 	@Test
 	public void shouldGiveFirstDayOfMonth(){
 		assertEquals(6, util.getFirstDayInMonth()); //samstag == 6.
 	}
 	
 	@Test
 	public void shouldGoForthAndBackInMonths(){
 		util.previousMonth();
 		assertEquals(9,util.getMonth());
 		util.nextMonth();
 		assertEquals(10, util.getMonth());
 	}
 	
 	@Test
 	public void shouldGiveListOfCurrentMonth(){ //Oktober im 2011
 		assertEquals(31, util.getThisMonthDates().size());
 		assertEquals(1, (int)util.getThisMonthDates().get(0));
 		
 		assertEquals(6, util.getNextMonthDates().size());
//		assertEquals(5, util.getLastMonthDates().size());		
 	}
 	
 	@Test
 	public void shouldGiveListOfNextMonth(){ //November im 2011
 		util.nextMonth();
 		assertEquals(11, util.getMonth());
 		assertEquals(30, util.getThisMonthDates().size());
 		assertEquals(1, (int)util.getThisMonthDates().get(0));
 		
 		assertEquals(4, util.getNextMonthDates().size());
//		assertEquals(5, util.getLastMonthDates().size());		
 	}	
 
 	@Test
 	public void shouldGiveListOfLastMonth(){ //September im 2011
 		util.previousMonth();
 		assertEquals(9, util.getMonth());
 		assertEquals(9, util.getMonth());
 		assertEquals(30, util.getThisMonthDates().size());
 		assertEquals(1, (int)util.getThisMonthDates().get(0));
 		
 		assertEquals(2, util.getNextMonthDates().size());
//		assertEquals(5, util.getLastMonthDates().size());		
 	}
 }
