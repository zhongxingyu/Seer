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
		assertEquals("0", Shortener.encode(0));
		assertEquals("1", Shortener.encode(1));
		assertEquals("zik0zj", Shortener.encode(2147483647));
 	}
 	
 	public void testDecode() {
		assertEquals(0, Shortener.decode("0"));
		assertEquals(1, Shortener.decode("1"));
		assertEquals(2147483647, Shortener.decode("zik0zj"));
 	}
 	
 }
