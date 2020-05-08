 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.util.Random;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class ShortenerTest extends TestCase {
 
 	/**
 	 * @return the suite of tests being tested
 	 */
 	public static Test suite()
 	{
 		return new TestSuite( ShortenerTest.class );
 	}
 	
 	public void testEquivalency() {
 		Random rng = new Random();
 		int number = rng.nextInt(Integer.MAX_VALUE);
 		
 		int result = Shortener.decode(Shortener.encode(number));
 		assertEquals(number, result);
 	}
 	
 	public void testEncode() {
		assertEquals("1", Shortener.encode(0));
		assertEquals("2", Shortener.encode(1));
		assertEquals("4gLq58", Shortener.encode(2147483647));
 	}
 	
 	public void testDecode() {
		assertEquals(0L, Shortener.decode("1"));
		assertEquals(1L, Shortener.decode("2"));
		assertEquals(2147483647, Shortener.decode("4gLq58"));
 	}
 	
 }
