 package com.pellcorp.xml.parser;
 
 import java.io.StringReader;
 
 import org.junit.Test;
 
 public class HxmlTokeniserTest {
 	@Test
 	public void testReader() throws Exception {
		String template = "<*Date type=\"xxxx\"><year>*</year><month>*</month><day>*</day></*Date>";
 
 		HxmlTokeniser parse = new HxmlTokeniser(new StringReader(template));
 		
 		while (parse.nextToken()) {
 			System.out.println("Type: " + parse.getTypeAsString());
 			System.out.println("Name: " + parse.getTokenName());
 			System.out.println("Text: " + parse.getText());
 			for (String attr : parse.getAttributes()) {
 				System.out.println("Attr: " + attr + "="
 						+ parse.getAttribute(attr));
 			}
 		}
 	}
 }
