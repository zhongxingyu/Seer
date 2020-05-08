 package banjo.parser.ast;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Test;
 
 import banjo.parser.BanjoParser;
 import banjo.parser.util.ParserReader;
 
 public class TestStringLiteralParser {
 
 	@Test public void testHelloWorld() { testParser("\"Hello, world!\"", "Hello, world!", 0); }	
 	@Test public void testEscapes() { testParser("\"\\\"\\t\\n\\r\"", "\"\t\n\r", 0); }
 	@Test public void testMultiline() { testParser("  \"abc\n   def\n    ghi\n   jkl\n   \"", "abc\ndef\n ghi\njkl\n"); }
 	@Test public void testBacktick() { testParser("`HelloWorld", "HelloWorld", 0); }	
 	@Test public void testBacktickEscapes() { testParser("`Hello\\ World\\!", "Hello World!", 0); }	
	@Test public void testBacktickEmpty1() { testParser("`", "", 0); }
	@Test public void testBacktickEmpty2() { testParser("` ", "", 0); }
	@Test public void testBacktickEmpty3() { testParser(" ` ", "", 0); }
	@Test public void testEmpty1() { testParser("\"\"", "", 0); }
	@Test public void testEmpty2() { testParser(" \"\" ", "", 0); }
 	
 	
 	private void testParser(String source, String expectedString) { testParser(source, expectedString, 0); }
 
 	private void testParser(String sourceString, String expectedParsedString, int expectedErrorCount) {
 		assertEquals(expectedParsedString, ParseTestUtils.testParse(sourceString, expectedErrorCount, null, StringLiteral.class, null).getString());
 	}
 	
 	@Test
 	public void testHexEscapes() throws IOException {
 		testParser("\"\\x01\"", "\u0001", 0);
 		testParser("\"\\x10\"", "\u0010", 0);
 		testParser("\"\\xF0\"", "\u00F0", 0);
 		testParser("\"\\xF\"", "", 1);
 		testParser("\"\\x\"", "", 1);
 		testParser("\"\\x00\"", "\u0000", 0);
 		
 		testParser("\"\\u0001\"", "\u0001", 0);
 		testParser("\"\\u0010\"", "\u0010", 0);
 		testParser("\"\\u00F0\"", "\u00F0", 0);
 		testParser("\"\\u2022\"", "\u2022", 0);
 		testParser("\"\\u00F\"", "", 1);
 		testParser("\"\\u00\"", "", 1);
 		testParser("\"\\u0000\"", "\u0000", 0);
 	}
 	
 }
