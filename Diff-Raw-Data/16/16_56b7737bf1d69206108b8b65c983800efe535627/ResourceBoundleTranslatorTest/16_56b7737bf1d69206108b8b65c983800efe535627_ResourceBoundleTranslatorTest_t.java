 package com.tracktopell.util;
 
 import java.io.IOException;
 import java.util.Arrays;
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import org.junit.Test;
 
 /**
  * Unit test for simple App.
  */
 
 
 public class ResourceBoundleTranslatorTest {
 
 	@Test
 	public void shuldTransletaOnePhrase() {
 		try {
 			String r = ResourceBoundleTranslator.translateUsingMicrosoftTranslator("Hola", "es", "en");			
 			assertEquals("Hello",r);						
 		} catch (IOException ex) {
 			fail(ex.toString());
 		}
 		
 	}
 	
 	@Test
 	public void shuldTransletaManyPhrases() {
 		try {
			String r = ResourceBoundleTranslator.translateUsingMicrosoftTranslator(
					"Hola # Mesa # Mensaje con parametro {0} en medio {1} de los 2", "es", "en");			
			final String[] ra = r.split("#");
			
			// Hello ,  table ,  message with parameter {0} in half 2 {1}
			System.err.print("r="+Arrays.asList(ra));
			assertTrue("1st is not equal",ra[0].toLowerCase().contains("hello"));
			assertTrue("2nd is not equal",ra[1].toLowerCase().contains("table"));
			assertTrue("3rd is not equal",ra[2].toLowerCase().contains("message with parameter"));
			//assertTrue("Hello # Table # Message with parameter {0} in {1} half of the 2".toUpperCase(),r.toUpperCase());						
			
 		} catch (IOException ex) {
 			fail(ex.toString());
 		}
 
 	}
 }
