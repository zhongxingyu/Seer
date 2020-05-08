 package com.pellcorp.xml.parser;
 
 import java.io.StringReader;
 
 import org.junit.Test;
 
 public class HxmlTokeniserTest {
 	@Test
 	public void testReader() throws Exception {
		String template = "<*Date type=\"xxxx\"><year-to-date>*</year-to-date><month>*</month><day>*</day></*Date>";
 
 		HxmlTokeniser parse = new HxmlTokeniser(new StringReader(template));
 		
 		while (parse.nextToken()) {
			
 		}
 	}
 }
