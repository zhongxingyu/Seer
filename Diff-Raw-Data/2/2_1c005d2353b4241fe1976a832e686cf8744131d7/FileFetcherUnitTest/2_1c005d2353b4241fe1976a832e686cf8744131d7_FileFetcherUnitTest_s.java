 package gov.usgs.cida.ncetl.sis;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class FileFetcherUnitTest {
 
 	private FileFetcher victim;
 	
 	private static final long HOUR = 1000*60*60;
 	private static final long DAY = 24 * HOUR;
 	
 	@Before
 	public void setUp() throws Exception {
 		victim = new FileFetcher();
 	}
 
 	@Test
 	public void testMakeOutputFileName() {
 		String fn = victim.makeOutputFileName(1999, 11, 42);
 		System.out.printf("out file name %s\n",  fn);
 		assertTrue("has year", fn.contains("1999"));
 		assertTrue("has month", fn.contains("11"));
 	}
 
 	@Test
 	public void testOneMonthAgo() {
 		Date oma = victim.oneMonthAgo();
 		
 		System.out.printf("one month ago %s\n", oma);
 		
 		long diff = System.currentTimeMillis() - oma.getTime();
 		assertTrue("long enuf", diff > 20 * DAY);
 		assertTrue("small enuf", diff < 40 * DAY);
 	}
 
 	@Test
 	public void testDaysInMonth() {
 		assertEquals(28, victim.daysInMonth(1999, 2));
 		assertEquals(29, victim.daysInMonth(2004, 2));
 	}
 
 }
