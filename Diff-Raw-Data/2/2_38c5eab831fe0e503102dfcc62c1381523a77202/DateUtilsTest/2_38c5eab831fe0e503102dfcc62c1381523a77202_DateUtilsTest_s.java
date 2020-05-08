 package it.unibs.ing.fp.dates;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Test;
 
 public class DateUtilsTest {
 	@Test
 	public void differenceBetweenEqualsDateIsZero() throws Exception {
 		Date d = new Date();
 		assertEquals(0, DateUtils.differenceBetween(d, d));
 	}
 	
 	@Test
	public void differenceBetweenConsecuriveDatesIsOne() throws Exception {
 		Date d1 = DateUtils.createDate(1978, Calendar.MARCH, 19);
 		Date d2 = DateUtils.createDate(1978, Calendar.MARCH, 20);
 		assertEquals(1, DateUtils.differenceBetween(d1, d2));
 	}
 	
 	@Test
 	public void createDate() throws Exception {
 		Date d = DateUtils.createDate(1978, Calendar.MARCH, 19);
 		assertEquals("19/03/1978", new SimpleDateFormat("dd/MM/yyyy").format(d));
 	}
 	
 	@Test
 	public void createDateTime() throws Exception {
 		Date d = DateUtils.createDate(1978, Calendar.MARCH, 19, 0, 0, 0, 0);
 		assertEquals("19/03/1978 00:00:00 0", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss S").format(d));
 	}
 }
