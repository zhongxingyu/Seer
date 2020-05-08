 package banjo.parser.ast;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import banjo.parser.errors.BanjoParseException;
 import banjo.parser.errors.UnsupportedUnaryOperator;
 
 public class TestMathParser {
 
 	@Test public void t1() { test("1 + 2 * 3", "(1).plus((2).times(3))"); }
 	@Test public void t2() { test("3 * 2 + 1", "(3).times(2).plus(1)"); }
	@Test public void t3() { test("2 + 2 > 1 + 1", "(2).plus(2).cmp((1).plus(1)).greater()"); }
 	
 	@Test public void multiline1() { test("2 + 2 >\n 1 +\n 1", "(2).plus(2).cmp((1).plus(1)).greater()"); }
 	@Test public void multiline2() { test("2 + 2\n > 1 + 1", "(2).plus(2).cmp((1).plus(1)).greater()"); }
 	@Test public void multiline3() { test("2\n + 2\n  > 1\n   + 1", "(2).plus(2).cmp((1).plus(1)).greater()"); }
 	@Test public void multiline4() { test("  2\n+ 2", "(2).plus(2)"); }
 	
 	// Not how indentation is overriding the operator precedence here; feature or bug ?
 	@Test public void multiline5() { test("  2\n+ 2\n* 4", "(2).plus(2).times(4)"); }
 
 	@Test public void gt() { test("3 > 2", "(3).cmp(2).greater()"); }
 	@Test public void lt() { test("3 < 2", "(3).cmp(2).less()"); }
 	@Test public void ge() { test("3 >= 2", "(3).cmp(2).less().false()"); }
 	@Test public void le() { test("3 <= 2", "(3).cmp(2).greater().false()"); }
 	
 	// TODO Support right-aligned operands
 	@Ignore @Test public void testRightAlign() { test("   1\n+ 11\n+111", "(1).plus(11).plus(111)"); }
 	
 	@Test public void badDedent1() { test("3\n> 2", UnsupportedUnaryOperator.class); }
 	
 	@Test public void unaries1() throws Exception { unaries("+ - ~1"); }
 	@Test public void unaries2() throws Exception { unaries("+\n -\n  ~\n   1"); }
 	@Test public void unaries3() throws Exception { unaries("  +  -  ~ 1"); }
 
 	public void unaries(String src) {
 		test(src, "(1).complement().negate().plus()");
 	}
 	
 	public static void test(String source, String normalizedSource) {
 		ParseTestUtils.testParse(source, 0, null, null, normalizedSource);
 	}
 	private void test(String source, Class<? extends BanjoParseException> expectedError) {
 		ParseTestUtils.testParse(source, 2, expectedError, null, null);
 	}
 
 	
 }
