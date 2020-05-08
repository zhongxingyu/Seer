 package org.instructionexecutor;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.suite.SuiteUtil;
 import org.suite.node.Node;
 
 public class FunctionCompilerTypeTest {
 
 	@Test
 	public void testBasic() {
 		assertEquals(SuiteUtil.parse("boolean") //
 				, getType("4 = 8"));
 	}
 
 	@Test
 	public void testDefineType() {
 		getType("define type t = number >> \n" //
 				+ "define v as t = 1 >> \n" //
 				+ "v = 99");
		getType("repeat {23}");
 	}
 
 	@Test
 	public void testFun() {
 		assertEquals(SuiteUtil.parse("number {number}") //
 				, getType("a => a + 1"));
 		assertEquals(SuiteUtil.parse("number") //
 				, getType("define f = (a => a + 1) >> f {3}"));
 		assertEquals(SuiteUtil.parse("boolean {boolean} {boolean}") //
 				, getType("and"));
 	}
 
 	@Test
 	public void testList() {
 		assertEquals(SuiteUtil.parse("list-of number") //
 				, getType("1,"));
 		assertEquals(SuiteUtil.parse("list-of (list-of number)") //
 				, getType("\"a\", \"b\", \"c\", \"d\","));
 	}
 
 	@Test
 	public void testOneOf() {
 		getType("" //
 				+ "define type t = one-of (NIL, BTREE t t,) >> \n" //
 				+ "define u as t = NIL >> \n" //
 				+ "define v as t = NIL >> \n" //
 				+ "v = BTREE (BTREE NIL NIL) NIL");
 	}
 
 	@Test
 	public void testTuple() {
 		getType("BTREE 2 3 = BTREE 4 6");
 		getTypeMustFail("T1 2 3 = T2 2 3");
 		getTypeMustFail("BTREE 2 3 = BTREE \"a\" 6");
 	}
 
 	@Test
 	public void testFail() {
 		String cases[] = { "1 + \"abc\"" //
 				, "(f => f {0}) | 1" //
 				, "define fib = (i2 => dummy => 1, fib {i2}) >> ()" //
 				, "define type t = one-of (BTREE t t,) >> \n" //
 						+ "define v as t = BTREE 2 3 >> 1" //
 		};
 
 		// There is a problem in deriving type of 1:(fib {i2})...
 		// Rule specified that right hand side of CONS should be a list,
 		// however fib {i2} is a closure.
 		for (String c : cases)
 			getTypeMustFail(c);
 	}
 
 	private static void getTypeMustFail(String c) {
 		try {
 			getType(c);
 		} catch (RuntimeException ex) {
 			return;
 		}
 		throw new RuntimeException("Cannot catch type error of: " + c);
 	}
 
 	private static Node getType(String a) {
 		return SuiteUtil.evaluateFunctionalType(a);
 	}
 
 }
