 package mork;
 
 import java.io.StringReader;
 
 import junit.framework.TestCase;
 
 public class LiteralParserTest extends TestCase {
 
 	public void testPlainLiteral() throws Exception {
 		LiteralParser parser = new LiteralParser();
 		assertEquals("Foo", parser.parse(new StringReader("Foo")));
		assertEquals("Brcke", parser.parse(new StringReader("Br$C3$BCcke")));
 		assertEquals("test", parser.parse(new StringReader("test)")));
 		assertEquals("line1line2", parser.parse(new StringReader("line1\\\nline2")));
 		assertEquals("test", parser.parse(new StringReader("te\\st")));
 		assertEquals("tent", parser.parse(new StringReader("te\\nt")));
 		assertEquals("tent", parser.parse(new StringReader("te\\\nnt")));
 		assertEquals("te$t", parser.parse(new StringReader("te\\$t")));
 		assertEquals("", parser.parse(new StringReader(")test")));
 		assertEquals(")test", parser.parse(new StringReader("\\)test")));
 		assertEquals("(FOOBAR)", parser.parse(new StringReader("(FOOBAR\\)")));
 	}
 
 }
