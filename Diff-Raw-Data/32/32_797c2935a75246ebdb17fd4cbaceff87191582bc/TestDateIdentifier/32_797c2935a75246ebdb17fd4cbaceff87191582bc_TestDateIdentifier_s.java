 package eu.trentorise.nlprise;
 
 import static org.junit.Assert.*; 
 import java.util.Date;
 import org.junit.Test;
 
 import eu.trentorise.opendata.nlprise.identifiers.date.DateIdentifier;
 
 /**
  * 
 * @author a.zanella
  *Last modified by azanella On 08/lug/2013
  */
 
 public class TestDateIdentifier {
 
 	@Test
 	public void testItalianDateIdentification()
 	{
 		String test = "15/11/1999";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "15/02/2003";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "15/2/2003";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "15 gennaio 1999";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "15 marzo '02";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "18 gen 2006";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "30 set '07";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "11-gen-04";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "13-febbraio-95";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "13-febbraio-1995";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "15-12-07";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "15-12-2007";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Giovedì, 18 aprile 2012";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Giovedì 18 aprile";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Giovedì 18";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Giovedì, 18";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "gen-15";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "gennaio-15";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "ottobre-1999";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "nov-1999";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		
 		test = "15/13/1999";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "15/13/6500";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "15,/13/6500";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "15/13-1999";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "15-13/1999";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "gennaio 15 1999";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "giove 10 ag 03";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "lunedì";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "sab-130";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		test = "mr11";
 		assertFalse(test+" must be rejected",DateIdentifier.isADate(test).getResult());
 		
 	}
 	@Test
 	public void testEnglishDateIdentification()
 	{
 		String test = "1999/12/13";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "1999-12-13";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "99-10-11";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "99/06/13";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "99/3/1";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Mon, Feb 24";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Mon, Feb 24 2013";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Jul 24 2013";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Jul 24th 2013";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "Jul 24th 2013";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 	}
 	@Test
 	public void testFormalDateIdentification()
 	{
 		String test = (new Date()).toString();
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "2013-03-04T00:00:00";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 		test = "2013-03-04T00:00:00.1234";
 		assertTrue(test+" must be valid date",DateIdentifier.isADate(test).getResult());
 	}
 	
 
 }
