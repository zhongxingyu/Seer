 package is.us.util;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
 
 import java.text.*;
 import java.util.*;
 
 import org.junit.Test;
 
 /**
  * Tests for USDateUtilities.
  * 
  * @author Logi Helgu
  */
 
 public class TestUSDateUtilities {
 
 	@Test
 	public void numberOfDaysBetweenDatesWithSameDate() {
 		Date today = new Date();
 		assertEquals( new Long( 0 ), USDateUtilities.numberOfDaysBetweenDates( today, today ) );
 	}
 
 	@Test
 	public void numberOfDayesBetweenDatesWithDifferentDates() {
 		Date today = new Date();
 		Date distantFuture = new Date();
 
 		distantFuture.setTime( today.getTime() * 2 );
 
 		long expectedDifference = today.getTime() / USDateUtilities.MILLISECONDS_IN_DAY;
 
 		assertEquals( new Long( expectedDifference ), USDateUtilities.numberOfDaysBetweenDates( distantFuture, today ) );
 		assertEquals( new Long( expectedDifference ), USDateUtilities.numberOfDaysBetweenDates( today, distantFuture ) );
 	}
 
 	@Test
 	public void numberOfDaysBetweenDatesNullCheck() {
 		try {
 			assertEquals( new Long( 0 ), USDateUtilities.numberOfDaysBetweenDates( new Date(), null ) );
 		}
 		catch( Exception e ) {
 			assert (true);
 		}
 	}
 
 	@Test
 	public void normalizeToMidnight() {
 		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd HHmmssS" );
 
 		try {
 			Date actual = USDateUtilities.normalizeToMidnight( df.parse( "20000131 000203100" ) );
 			Date expected = df.parse( "20000131 000000000" );
 			assertEquals( expected, actual );
 
 			actual = USDateUtilities.normalizeToMidnight( df.parse( "20002105 2400000" ) );
 			expected = df.parse( "20002105 000000000" );
 
 			actual = USDateUtilities.normalizeToMidnight( df.parse( "19990101 0000000" ) );
 			expected = df.parse( "19990101 000000000" );
 		}
 		catch( ParseException e ) {
 			assert (false);
 		}
 	}
 
 	@Test
	public void normalizeToEndOfDay() {
		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd HHmmssS" );

		try {
			Date actual = USDateUtilities.normalizeToEndOfDay( df.parse( "20000131 000203100" ) );
			Date expected = df.parse( "20000131 235959999" );
			assertEquals( expected, actual );

			actual = USDateUtilities.normalizeToMidnight( df.parse( "20002105 2400000" ) );
			expected = df.parse( "20002105 235959999" );

			actual = USDateUtilities.normalizeToMidnight( df.parse( "19990101 0000000" ) );
			expected = df.parse( "19990101 235959999" );
		}
		catch( ParseException e ) {
			assert (false);
		}
	}

	@Test
 	public void isInLastSevenDays() {
 		Date now = new Date();
 		assertEquals( true, USDateUtilities.isNotOlderThanSevenDays( now ) );
 
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( now );
 		c.add( GregorianCalendar.DAY_OF_YEAR, -7 );
 		assertEquals( true, USDateUtilities.isNotOlderThanSevenDays( c.getTime() ) );
 
 		c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( now );
 		c.add( GregorianCalendar.DAY_OF_YEAR, 1 );
 		assertEquals( true, USDateUtilities.isNotOlderThanSevenDays( c.getTime() ) );
 	}
 
 	@Test
 	public void isToday() {
 		Date now = new Date();
 		assertEquals( true, USDateUtilities.isToday( now ) );
 
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( now );
 		c.add( GregorianCalendar.DAY_OF_WEEK, 3 );
 		assertEquals( false, USDateUtilities.isToday( c.getTime() ) );
 
 		now = new Date();
 		now = USDateUtilities.normalizeToMidnight( now );
 		assertEquals( true, USDateUtilities.isToday( now ) );
 	}
 
 	@Test
 	public void isWorkday() {
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY );
 		assertEquals( true, USDateUtilities.isWorkday( c.getTime() ) );
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.TUESDAY );
 		assertEquals( true, USDateUtilities.isWorkday( c.getTime() ) );
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.WEDNESDAY );
 		assertEquals( true, USDateUtilities.isWorkday( c.getTime() ) );
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.THURSDAY );
 		assertEquals( true, USDateUtilities.isWorkday( c.getTime() ) );
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.FRIDAY );
 		assertEquals( true, USDateUtilities.isWorkday( c.getTime() ) );
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SATURDAY );
 		assertEquals( false, USDateUtilities.isWorkday( c.getTime() ) );
 		c.set( GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY );
 		assertEquals( false, USDateUtilities.isWorkday( c.getTime() ) );
 	}
 
 	@Test
 	public void ageAtDate() {
 		Date birthdate = USDateUtilities.date( 1979, 11, 9 );
 		Integer expected = null;
 		Integer result = null;
 
 		expected = 19;
 		result = USDateUtilities.ageAtDate( birthdate, USDateUtilities.date( 1999, 11, 8 ) );
 		assertEquals( expected, result );
 
 		expected = 20;
 		result = USDateUtilities.ageAtDate( birthdate, USDateUtilities.date( 1999, 11, 9 ) );
 		assertEquals( expected, result );
 
 		expected = 20;
 		result = USDateUtilities.ageAtDate( birthdate, USDateUtilities.date( 1999, 11, 10 ) );
 		assertEquals( expected, result );
 
 		expected = 21;
 		result = USDateUtilities.ageAtDate( birthdate, USDateUtilities.date( 2000, 11, 10 ) );
 		assertEquals( expected, result );
 	}
 
 	@Test
 	public void date() {
 		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd HHmmssS" );
 
 		try {
 			Date expected = USDateUtilities.normalizeToMidnight( df.parse( "20000131 000000000" ) );
 			Date result = USDateUtilities.date( 2000, 1, 31 );
 			assertEquals( expected, result );
 		}
 		catch( ParseException e ) {
 			fail();
 		}
 
 	}
 }
