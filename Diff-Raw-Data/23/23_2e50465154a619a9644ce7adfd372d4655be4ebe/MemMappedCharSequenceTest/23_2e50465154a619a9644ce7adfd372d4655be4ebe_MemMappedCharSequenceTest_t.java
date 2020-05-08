 package net.sf.okapi.common.tests;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.URL;
 
 import net.sf.okapi.common.MemMappedCharSequence;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class MemMappedCharSequenceTest {
 	private MemMappedCharSequence charSequence;
 	
 	public MemMappedCharSequenceTest() {	
 	}
 	
 	@Test
 	public void testString() {
 		String testString = "This is a test of the in memory string handling";
 		charSequence = new MemMappedCharSequence(testString);
 		assertEquals(47, charSequence.length());
 		assertEquals("test", charSequence.subSequence(10, 14));
 		assertEquals("test", charSequence.subSequence(10, 14));
 		assertEquals('T', charSequence.charAt(0));
 		assertTrue(charSequence.containsAt(testString, 0));
 		assertEquals(testString, charSequence.toString());
 		assertEquals(25, charSequence.lastIndexOf("memory", testString.length()));
 	}
 
 	@Test
	public void testLarge() throws IOException {	
		URL url = ParametersTest.class.getResource("/test.txt");		
		charSequence = new MemMappedCharSequence(url, "UTF-16LE");
 		assertEquals(7257720, charSequence.length());
 		assertEquals('<', charSequence.charAt(1));
 		assertEquals('U', charSequence.charAt(31));
 		assertEquals(7257718, charSequence.lastIndexOf(">", charSequence.length()-1));			
 		charSequence.close();
 	}
 	
 	@Test
 	public void testLowercase() throws IOException {
		URL url = ParametersTest.class.getResource("/test.txt");	
		charSequence = new MemMappedCharSequence(url, "UTF-16LE", true);		
 		assertEquals('u', charSequence.charAt(31));
 		charSequence.close();
 	}
 }
