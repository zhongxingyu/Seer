 package com.karhatsu.suosikkipysakit.datasource.parsers.test;
 
 import junit.framework.TestCase;
 
 import com.karhatsu.suosikkipysakit.datasource.parsers.LineParser;
 
 public class LineParserTest extends TestCase {
 
 	private LineParser lineParser = new LineParser();
 
 	public void testInternal() {
 		assertEquals("78", lineParser.format("1078  2"));
 		assertEquals("20", lineParser.format("1020  1"));
 		assertEquals("11", lineParser.format("2011  1"));
 		assertEquals("2", lineParser.format("2002  1"));
 		assertEquals("51", lineParser.format("4051  1"));
 	}
 
 	public void testInternalWithLetter() {
 		assertEquals("94A", lineParser.format("1094A 1"));
 		assertEquals("94B", lineParser.format("1094B 1"));
 		assertEquals("12B", lineParser.format("2012B 1"));
 		assertEquals("14B", lineParser.format("2014B 1"));
 		assertEquals("51K", lineParser.format("4051K 1"));
 	}
 
 	public void testJoukoLine() {
 		assertEquals("93J", lineParser.format("1293J 2"));
 		assertEquals("94J", lineParser.format("1294J 1"));
 	}
 
 	public void testRegional() {
 		assertEquals("506", lineParser.format("2506  1"));
 		assertEquals("519", lineParser.format("4519  2"));
 		assertEquals("540", lineParser.format("7540  1"));
 	}
 
 	public void testRegionalWithLetter() {
 		assertEquals("623Z", lineParser.format("4623Z 2"));
 		assertEquals("519A", lineParser.format("4519A 2"));
 		assertEquals("633N", lineParser.format("9633N 2"));
 	}
 
 	public void testLocalTrain() {
 		assertEquals("K", lineParser.format("3001K 1"));
 		assertEquals("T", lineParser.format("3001T 1"));
 	}
 
	public void testMetro() {
		assertEquals("M", lineParser.format("1300M 1"));
		assertEquals("M", lineParser.format("1300M 2"));
		assertEquals("M", lineParser.format("1300M42"));
		assertEquals("M", lineParser.format("1300V 1"));
		assertEquals("M", lineParser.format("1300V 2"));
	}

 }
