 package banjo.parser.ast;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class TestObjectLiteralParser {
 
 
 	@Test public void newlineSeparated()  { abc("{a:1\n b:2\n c:3}", 0); }
 	@Test public void commaSeparator()    { abc("{a:1,b:2,c:3}", 0); }
 	@Test public void noCurliesOrCommas() { abc(" a: 1\n b : 2\n c :3", 0); }
 	@Test public void mixCommasNewlines() { abc("{a:1\n b:2,\n c:3}", 0); }
	@Test public void backdentError()     { abc("{a:1,b:2,\nc:3}", 1); }
 
 	@Test public void objValue() { parse("a::a:1\n   b:2\n   c:3\nb::d:1\n   e:2\n   f:3\n", "{a: {a: 1, b: 2, c: 3}, b: {d: 1, e: 2, f: 3}}"); }
 	@Test public void trailingComma()     { abc("{a:1,b:2,c:3,}", 0); }
 	@Test public void stringKey() { parse("{\"a\":1,\"b\":2,\"c\":3}", "{\"a\": 1, \"b\": 2, \"c\": 3}"); }
 
 	@Test public void keys() { parse("{:x,:y}", "{x: \"x\", y: \"y\"}"); }
 	@Test public void enum_() { parse(" : x\n : y", "{x: \"x\", y: \"y\"}"); }
 	@Test public void method() { parse("{f(x): x}", "{f: (x) -> x}"); }
 	@Test public void specialCharsKeys() { parse("{\"a b\":1,\"b.c\":2,\"-f\":3}", "{\"a b\": 1, \"b.c\": 2, \"-f\": 3}"); }
 
 	@Test public void table1() { parse("#::a,b,c\nabc::1,2,3", "{abc: {a: 1, b: 2, c: 3}}"); }
 	@Test public void table2() { parse("#::a,b\n\"12\"::1,2\n\"34\"::3,4\n\"56\"::5,6", "{\"12\": {a: 1, b: 2}, \"34\": {a: 3, b: 4}, \"56\": {a: 5, b: 6}}"); }
 	
 	private void abc(String source, int expectedErrorCount) {
 		ObjectLiteral node = parse(source, "{a: 1, b: 2, c: 3}");
 		final Field[] eltsArray = node.getFields().values().toArray(new Field[3]);
 		assertEquals(3, eltsArray.length);
 		assertEquals(NumberLiteral.class, eltsArray[0].getValue().getClass());
 		assertEquals(1L, ((NumberLiteral)eltsArray[0].getValue()).getNumber().longValue());
 		assertEquals("a", eltsArray[0].getKey().toSource());
 		assertEquals(NumberLiteral.class, eltsArray[1].getValue().getClass());
 		assertEquals(2L, ((NumberLiteral)eltsArray[1].getValue()).getNumber().longValue());
 		assertEquals("b", eltsArray[1].getKey().toSource());
 		assertEquals(NumberLiteral.class, eltsArray[2].getValue().getClass());
 		assertEquals(3L, ((NumberLiteral)eltsArray[2].getValue()).getNumber().longValue());
 		assertEquals("c", eltsArray[2].getKey().toSource());
 	}
 
 	public ObjectLiteral parse(String source, String expectedSource) {
 		return ParseTestUtils.testParse(source, 0, null, ObjectLiteral.class, expectedSource);
 	}
 	
 	@Test
	public void testEmpty() {
 		ObjectLiteral node = parse("{}", null);
 		assertTrue(node.getFields().isEmpty());
 	}
 }
