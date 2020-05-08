 package banjo.parser.ast;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class TestLetParser {
 	@Test public void oneLine()         { hello("hello = \"world\" ; hello", 0); }
 	@Test public void twoLine()         { hello("hello = \"world\"\nhello", 0); }
 	@Test public void twoLineIndented() { hello("   hello = \"world\"\n   hello", 0); }
 	@Test public void badBackdent()     { hello("   hello = \"world\"\nhello", 2); } // Backdent here should be reported as an error
 	@Test public void badIndent()       { hello(" hello = \"world\"\n   hello", 1); } // Indent here should be reported as an error
 	
 
 	private void hello(String source, int expectedErrorCount) {
 		Steps node = ParseTestUtils.testParse(source, expectedErrorCount, Steps.class, "hello = \"world\"; hello");
 		assertEquals(2, node.getSteps().size());
 		Let let = (Let) node.getSteps().get(0);
 		
 		assertEquals("hello", let.getName());
 		assertEquals(StringLiteral.class, let.getValue().getClass());
 		assertEquals("world", ((StringLiteral)let.getValue()).getString());
 		Expr body = node.getSteps().get(1);
 		assertEquals(IdRef.class, body.getClass());
 		assertEquals("hello", ((IdRef)body).getId());
 	}
 	
 
 	@Test public void f1() { func("f(x) = x", 0, "f = (x) -> x"); }
	@Test public void f2() { func("f(x,y) = x", 0, "f = (x,y) -> x"); }
 	@Test public void f3() { func("f() = x", 0, "f = () -> x"); }
 	//@Test public void f4() { func("f() = x", 0, "f = () -> x"); }
 	
 	public void func(String source, int expectedErrorCount, String expectedSource) {
 		Let let = ParseTestUtils.testParse(source, expectedErrorCount, Let.class, expectedSource);
 		assertEquals("f", let.getName());
 		assertEquals(FunctionLiteral.class, let.getValue().getClass());
 	}
 	
 }
