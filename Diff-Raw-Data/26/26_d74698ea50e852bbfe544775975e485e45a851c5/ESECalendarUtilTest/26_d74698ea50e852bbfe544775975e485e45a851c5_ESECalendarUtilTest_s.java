 package modelTests;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 import models.*;
 
 public class ESECalendarUtilTest extends UnitTest{
 	
 	private ESECalendarUtil util = ESECalendarUtil.getInstanceToday();
 	private Calendar cal = Calendar.getInstance();
 	
 	@Before
 	public void setCalendarAtAFixedDate(){
 		//making a Date ...
 		Date temp = new Date();
 		cal.set(2011, 10, 01); //1.nov.2011
 		temp = cal.getTime();
 		
 		util.setAtDate(temp);
 		assertEquals(3, cal.get(cal.DAY_OF_WEEK)); //1. ist ein Dienstag
 		assertEquals(10, cal.get(cal.MONTH));
 		assertEquals(1, cal.get(cal.DATE));
 		assertEquals(2011, cal.get(cal.YEAR));
 	}
 	
 	@Test
 	public void shouldGetFirstMondayOfMonth(){
 		int monday = util.firstMondayOfCurrentMonth();
 		assertEquals(7, monday);
 		//try another month
 		cal.set(2011, 11, 13);
 		util.setAtDate(cal.getTime());
 		monday = util.firstMondayOfCurrentMonth();
 		assertEquals(5, util.firstMondayOfCurrentMonth());
 	}
 
 	@Test
 	public void shouldGetFirstWeekdayOfCurrentMonth(){
 		cal.set(2011, 10, 13);
 		util.setAtDate(cal.getTime());
 		
 		int firstWeekDay = util.firstWeekdayOfCurrentMonth();
 		assertEquals(2, firstWeekDay);//2.nov 2011 ist dienstag
 		//try another month
 		cal.set(2012, 5, 10);
 		util.setAtDate(cal.getTime());
 		firstWeekDay = util.firstWeekdayOfCurrentMonth();
 		assertEquals(5, util.firstWeekdayOfCurrentMonth());
 	}
 	
 	@Test
 	public void shouldSetOneMonthForth(){
 		//check setups
		cal.set(2012, 5, 10);
		assertEquals(2012, cal.get(cal.YEAR));
		assertEquals(5, cal.get(cal.MONTH));
		assertEquals(10, cal.get(cal.DAY_OF_MONTH));
 		
 		util.setToNextMonth();
		assertEquals(6, util.getMonth());
 		assertEquals(2012, util.getYear());
 		assertEquals(10, util.getDayOfMonth());
 		
 		//check dec-jan
		cal.set(cal.MONTH, 11);
 		util.setAtDate(cal.getTime());
 		util.setToNextMonth();
		assertEquals(2013, cal.get(cal.YEAR));
		assertEquals(0, cal.get(cal.MONTH));
		assertEquals(10, cal.get(cal.DAY_OF_MONTH));
 	}
 	
 	@Test
 	public void shouldSetOneMonthBack(){
		
 	}
 }
