 package com.karhatsu.suosikkipysakit.datasource.parsers.test;
 
 import junit.framework.TestCase;
 
 import com.karhatsu.suosikkipysakit.datasource.parsers.TimeParser;
 
 public class TimeParserTest extends TestCase {
 
 	private TimeParser timeParser = new TimeParser();
 
 	public void testFourDigits() {
 		assertEquals("10:12", timeParser.format("1012"));
 		assertEquals("21:34", timeParser.format("2134"));
 	}
 
 	public void testThreeDigits() {
 		assertEquals("09:48", timeParser.format("948"));
 		assertEquals("02:33", timeParser.format("233"));
 	}
 
 }
