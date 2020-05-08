 package common;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Test;
 
 import common.DateUtils;
 
 public class DateUtilsTest {
 	
 	@Test
 	public void testToMillis() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.clear();
		calendar.set(Calendar.MONTH, 7);
 		calendar.set(Calendar.DATE, 10);
 		calendar.set(Calendar.YEAR, 2013);
 		
 		long actual = DateUtils.toMillis(calendar.getTime());
 		
		assertEquals(1376107200000l, actual);
 	}
 
 	@Test
 	public void testToMillisWithNull() {
 		long actual = DateUtils.toMillis(null);
 		assertEquals(-1l, actual);
 	}
 	
 	@Test
 	public void testToDate() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.clear();
		calendar.set(Calendar.MONTH, 7);
 		calendar.set(Calendar.DATE, 10);
 		calendar.set(Calendar.YEAR, 2013);
 		
		Date actual = DateUtils.toDate(1376107200000l);
 		
 		assertTrue(calendar.getTime().equals(actual));
 	}
 	
 	@Test
 	public void testToDateWithNull() {
 		Date actual = DateUtils.toDate(-1l);
 		assertTrue(actual == null);
 	}
 }
