 package it.unibs.ing.fp.dates;
 
 import static org.junit.Assert.*;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Test;
 
 public class DateUtilsTest {
 	private static final Date D1 = DateUtils.createDate(1978, Calendar.MARCH, 19);
 	private static final Date D2 = DateUtils.createDate(1978, Calendar.MARCH, 20);
 	@Test
 	public void differenceBetweenEqualsDateIsZero() throws Exception {
 		Date d = new Date();
 		assertEquals(0, DateUtils.differenceBetween(d, d));
 	}
 
 	@Test
 	public void differenceBetweenConsecutiveDatesIsOne() throws Exception {
 		assertEquals(1, DateUtils.differenceBetween(D1, D2));
 	}
 	
 	@Test
 	public void differenceBetweenConsecutiveDatesInInverseOrderIsOne() throws Exception {
 		assertEquals(1, DateUtils.differenceBetween(D2, D1));
 	}
 
 	@Test
	public void givenDatesInDifferentMonthsOfTheSameYearCalculateDifferenceOne() throws Exception {
 		Date d2 = DateUtils.createDate(1978, Calendar.APRIL, 20);
 		assertEquals(32, DateUtils.differenceBetween(D1, d2));
 	}
 
 	@Test
 	public void createDate() throws Exception {
 		assertEquals("19/03/1978", new SimpleDateFormat("dd/MM/yyyy").format(D1));
 	}
 
 	@Test
 	public void createDateTime() throws Exception {
 		Date d = DateUtils.createDate(1978, Calendar.MARCH, 19, 0, 0, 0, 0);
 		assertEquals("19/03/1978 00:00:00 0", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss S").format(d));
 	}
 	
 	@Test
 	public void testMinCorrectOrder() throws Exception {
 		assertEquals(D1, DateUtils.min(D1, D2));
 	}
 	
 	@Test
 	public void testMinInverseOrder() throws Exception {
 		assertEquals(D1, DateUtils.min(D2, D1));
 	}
 	
 	@Test
 	public void testMinWhenEquals() throws Exception {
 		assertEquals(D1, DateUtils.min(D1, D1));
 	}
 	
 	@Test
 	public void testMaxCorrectOrder() throws Exception {
 		assertEquals(D2, DateUtils.max(D1, D2));
 	}
 	
 	@Test
 	public void testMaxInverseOrder() throws Exception {
 		assertEquals(D2, DateUtils.max(D2, D1));
 	}
 	
 	@Test
 	public void testMaxWhenEquals() throws Exception {
 		assertEquals(D1, DateUtils.max(D1, D1));
 	}
 }
