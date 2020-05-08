 package it.unibs.ing.fp.dates;
 
 import static org.junit.Assert.*;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.junit.Test;
 
 public class DateFormatTest {
 	private static final Date ORIGIN = DateUtils.createDate(1970, Calendar.JANUARY, 1, 0, 0, 0, 0);
 	@Test
 	public void formatDateUsingItalianFormat() throws Exception {
 		final String format = "dd/MM/yyyy";
 		DateFormat formatter = new SimpleDateFormat(format);
 		assertEquals("01/01/1970", formatter.format(ORIGIN));
 	}
 	
 	@Test
 	public void formatDateAndTime() throws Exception {
 		final String format = "dd-MM-yy HH:mm:ss";
 		DateFormat formatter = new SimpleDateFormat(format);
 		assertEquals("01-01-70 00:00:00", formatter.format(ORIGIN));
 	}
 	
 	@Test
 	public void formatUsingMonthSmallDescriptionAndItalianLocale() throws Exception {
 		final String format = "dd MMM yyyy";
 		DateFormat formatter = new SimpleDateFormat(format, Locale.ITALY);
 		assertEquals("01 gen 1970", formatter.format(ORIGIN));
 	}
 	
 	@Test
 	public void formatUsingMonthSmallDescriptionAndEnglishLocale() throws Exception {
 		final String format = "dd MMM yyyy";
 		DateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
 		assertEquals("01 Jan 1970", formatter.format(ORIGIN));
 	}
 	
 	@Test
 	public void formatUsingMonthLongDescriptionAndItalianLocale() throws Exception {
 		final String format = "dd MMMM yyyy";
 		DateFormat formatter = new SimpleDateFormat(format, Locale.ITALY);
 		assertEquals("01 gennaio 1970", formatter.format(ORIGIN));
 	}
 	
 	@Test
 	public void formatUsingMonthLongDescriptionAndEnglishlocale() throws Exception {
 		final String format = "dd MMMM yyyy";
 		DateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
 		assertEquals("01 January 1970", formatter.format(ORIGIN));
 	}
 	
 	@Test
 	public void parseDateUsingItalianFormat() throws Exception {
 		final DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 		final String dateAsString = "11/03/2013";
 		Date parsed = formatter.parse(dateAsString);
 		Date expected = DateUtils.createDate(2013, Calendar.MARCH, 11);
 		assertEquals(expected, parsed);
 	}
 }
