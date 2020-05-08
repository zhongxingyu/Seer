 package org.catamarancode.faq.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 /**
  * TODO: Move into a scandilabs-core util at some point 
  * @author mkvalsvik
  *
  */
 public class StringLineBreakToParagraphConverter {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
		String test1 = "<style>\ntd {\ncolor:black;\n}\n</style>\nThis is a test string\nthis is line2\nthis is line3";
 		StringLineBreakToParagraphConverter c = new StringLineBreakToParagraphConverter();
 		String result1 = c.process(test1);
 		System.out.println(result1);		
 
 	}
 
 	public String process(String stringWithLineBreaks) {
 		StringBuilder result = new StringBuilder();
 
 		// Fail with empty string return
 		if (stringWithLineBreaks == null || stringWithLineBreaks.length() <= 0) {
 			return "";
 		}
 		
		boolean suppressPTag = false;
 		BufferedReader in = new BufferedReader(new StringReader(
 				stringWithLineBreaks));
 		try {
			String line = in.readLine();			
 			while (line != null) {
				
				// Does the line start with a style tag?  
				if (line.startsWith("<style")) {
					suppressPTag = true;
				} 
				
				if (!suppressPTag) {
					result.append("<p>");	
				}				
				result.append(line);
				if (!suppressPTag) {
					result.append("</p>");
				}
				
				// Turn off p tag suppress?
				if (line.startsWith("</style")) {
					if (suppressPTag) {
						suppressPTag = false;
					}
				}
 				line = in.readLine();
 			}			
 		} catch (IOException e) {
 			throw new RuntimeException(
 					"Exception while converting string with line breaks into html paragraph tags",
 					e);
 		} finally {
 			try {
 				in.close();
 			} catch (IOException e) {
 				// eat the exception silently
 			}
 		}
 		return result.toString();
 	}
 
 }
