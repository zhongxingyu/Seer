 package de.thischwa.pmcms.tool;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.Normalizer;
 
 import org.junit.Test;
 
 public class TestUniCode {
 
 	@Test
 	public void test() {
 		String aeStr16 = "\u00E4";
		String aeStr = "ä";
 		
 		assertEquals(normalize(aeStr16), normalize(aeStr));
 	}
 	
 
 	private static String normalize(String str) {
 		return Normalizer.normalize(str, Normalizer.Form.NFC);
 	}
 
 }
