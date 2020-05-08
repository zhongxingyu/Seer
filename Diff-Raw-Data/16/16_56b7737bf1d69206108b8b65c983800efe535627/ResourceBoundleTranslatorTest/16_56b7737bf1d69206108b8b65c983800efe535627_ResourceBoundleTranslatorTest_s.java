 package com.tracktopell.util;
 
 import java.io.IOException;
 import java.util.Arrays;
 import static org.junit.Assert.assertEquals;
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
			String r = ResourceBoundleTranslator.translateUsingMicrosoftTranslator("Hola # Mesa # Mensaje con parametro {0} en medio {1} de los 2", "es", "en");			
			//System.err.print("->"+Arrays.asList(r.split("#")));
			assertEquals("Hello # Table # Message with parameter {0} in {1} half of the 2".toUpperCase(),r.toUpperCase());						
 		} catch (IOException ex) {
 			fail(ex.toString());
 		}
 
 	}
 }
