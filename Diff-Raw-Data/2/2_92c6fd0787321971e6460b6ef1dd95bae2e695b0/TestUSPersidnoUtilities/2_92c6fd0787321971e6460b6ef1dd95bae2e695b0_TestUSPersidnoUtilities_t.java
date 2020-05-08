 package is.us.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.*;
 
 import org.junit.Test;
 
 /**
  * Tests for USPersidnoUtilities.
  * 
  * @author Hugi Thordarson
  */
 
 public class TestUSPersidnoUtilities {
 
 	@Test
 	public void cleanupPersidno() {
 		assertTrue( USPersidnoUtilities.cleanupPersidno( "091179 4829" ).equals( "0911794829" ) );
 		assertTrue( USPersidnoUtilities.cleanupPersidno( "091179-4829" ).equals( "0911794829" ) );
 		assertTrue( USPersidnoUtilities.cleanupPersidno( "0911794829" ).equals( "0911794829" ) );
 		assertTrue( USPersidnoUtilities.cleanupPersidno( "09" ).equals( "09" ) );
 		assertFalse( USPersidnoUtilities.cleanupPersidno( "091179.4829" ).equals( "0911794829" ) );
 	}
 
 	@Test
 	public void birthyearFromPersidno() {
 		assertTrue( USPersidnoUtilities.birthyearFromPersidno( "091179 4829" ) == 1979 );
 		assertTrue( USPersidnoUtilities.birthyearFromPersidno( "190876-3659" ) == 1976 );
 		assertTrue( (USPersidnoUtilities.birthyearFromPersidno( "5703003340" ) == null) );
 		assertFalse( USPersidnoUtilities.birthyearFromPersidno( "0911794829" ) == 1999 );
 	}
 
 	@Test
 	public void birthMonthFromPersidno() {
 		assertTrue( USPersidnoUtilities.birthMonthFromPersidno( "091179 4829" ) == 11 );
 		assertTrue( USPersidnoUtilities.birthMonthFromPersidno( "190876-3659" ) == 8 );
 		assertTrue( USPersidnoUtilities.birthMonthFromPersidno( "5703003340" ) == null );
 		assertFalse( USPersidnoUtilities.birthMonthFromPersidno( "0911794829" ) == 10 );
 	}
 
 	@Test
 	public void birthDayFromPersidno() {
 		assertTrue( USPersidnoUtilities.birthDayFromPersidno( "091179 4829" ) == 9 );
 		assertTrue( USPersidnoUtilities.birthDayFromPersidno( "190876-3659" ) == 19 );
 		assertTrue( USPersidnoUtilities.birthDayFromPersidno( "5703003340" ) == null );
 		assertFalse( USPersidnoUtilities.birthDayFromPersidno( "0911794829" ) == 8 );
 	}
 
 	@Test
 	public void formatPersidno() {
 		assertTrue( USPersidnoUtilities.formatPersidno( "091179 4829" ).equals( "091179-4829" ) );
 		assertTrue( USPersidnoUtilities.formatPersidno( "0911794829" ).equals( "091179-4829" ) );
 		assertTrue( USPersidnoUtilities.formatPersidno( "570300-3340" ).equals( "570300-3340" ) );
 	}
 
 	@Test
 	public void formatPersidnoWithDelimiter() {
 		assertTrue( USPersidnoUtilities.formatPersidno( "091179 4829", "-" ).equals( "091179-4829" ) );
 		assertTrue( USPersidnoUtilities.formatPersidno( "0911794829", "-" ).equals( "091179-4829" ) );
 		assertTrue( USPersidnoUtilities.formatPersidno( "570300-3340", "-" ).equals( "570300-3340" ) );
 	}
 
 	@Test
 	public void isIndividualPersidno() {
 		assertTrue( USPersidnoUtilities.isIndividualPersidno( "0911794829" ) );
 		assertFalse( USPersidnoUtilities.isIndividualPersidno( "5703003340" ) );
 		assertFalse( USPersidnoUtilities.isIndividualPersidno( null ) );
 	}
 
 	@Test
 	public void isCompanyPersidno() {
 		assertTrue( USPersidnoUtilities.isCompanyPersidno( "5703003340" ) );
 		assertFalse( USPersidnoUtilities.isCompanyPersidno( "0911794829" ) );
 		assertFalse( USPersidnoUtilities.isCompanyPersidno( null ) );
 	}
 
 	@Test
 	public void validatePersidno() {
 		assertTrue( USPersidnoUtilities.validatePersidno( "1708755839" ) );
 		assertTrue( USPersidnoUtilities.validatePersidno( "0911794829" ) );
 		assertTrue( USPersidnoUtilities.validatePersidno( "2007765699" ) );
 
 		assertFalse( USPersidnoUtilities.validatePersidno( null ) );
 		assertFalse( USPersidnoUtilities.validatePersidno( "AAA" ) );
 		assertFalse( USPersidnoUtilities.validatePersidno( "BBBBBBBBBB" ) );
 
 		// INN-702 this company persidno is failing
 		assertTrue( USPersidnoUtilities.validatePersidno( "4612911399" ) );
 
 		//		Look into these.
 		//		assertFalse( USStringUtilities.validatePersidno( "0000000000" ) );
 		//		assertFalse( USStringUtilities.validatePersidno( "1111111111" ) );
 	}
 
 	@Test
 	public void birthdateFromPersidno() {
 		GregorianCalendar g = (GregorianCalendar)GregorianCalendar.getInstance();
 		g.set( 1979, 10, 9, 0, 0, 0 );
 		g.set( Calendar.MILLISECOND, 0 );
 
 		Date expectedBirthdate = g.getTime();
 
 		Date hugiBirthDate = USPersidnoUtilities.birthdateFromPersidno( "0911794829" );
 
 		assertEquals( hugiBirthDate, expectedBirthdate );
 	}
 
 	/**
 	 * Note: Unless we find a way to keep me 29 forever, this test will eventually fail.
 	 * We need to find another way to provide test data.
 	 */
 	@Test
 	public void ageFromPersidno() {
 		Integer n = USPersidnoUtilities.ageFromPersidno( "0911794829" );
 		assertTrue( n == 30 );
 	}
 
 	/**
 	 * Note: Unless we find a way to keep me 29 forever, this test will eventually fail.
 	 * We need to find another way to provide test data.
 	 */
 	@Test
 	public void nextBirthday() {
 		Date birthday = null;
 
 		birthday = USPersidnoUtilities.nextBirthday( "0911794829" );
 		assertEquals( USDateUtilities.date( 2010, 11, 9 ), birthday );
 
 		birthday = USPersidnoUtilities.nextBirthday( "2105794279" );
		assertEquals( USDateUtilities.date( 2011, 5, 21 ), birthday );
 
 		birthday = USPersidnoUtilities.nextBirthday( "1203833029" );
 		assertEquals( USDateUtilities.date( 2011, 3, 12 ), birthday );
 	}
 }
