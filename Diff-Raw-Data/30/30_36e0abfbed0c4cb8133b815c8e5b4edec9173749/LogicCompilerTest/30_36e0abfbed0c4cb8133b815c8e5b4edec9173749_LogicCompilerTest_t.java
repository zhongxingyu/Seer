 package org.instructionexecutor;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.suite.SuiteUtil;
 
 public class LogicCompilerTest {
 
 	@Test
 	public void test() throws IOException {
 		assertTrue(eval("()"));
 		assertTrue(eval("3 = 3"));
 		assertFalse(eval("fail"));
 		assertFalse(eval("1 = 2"));
 	}
 
 	@Test
 	public void testCut() throws IOException {
		assertTrue(eval("(.a = 1; .a = 2), !, .a = 1"));
		assertFalse(eval("(.a = 1; .a = 2), !, .a = 2"));
 		assertFalse(eval(".a = 1, !, .b = 2, fail; .b = 3"));
 	}
 
 	@Test
 	public void testFibonacci() throws IOException {
 		assertTrue(eval("" //
 				+ "( \n" //
 				+ "    fib 0 1 # \n" //
 				+ "    fib 1 1 # \n" //
 				+ "    fib .n .f \n" //
 				+ "        :- let .n1 (.n - 1) \n" //
 				+ "        , let .n2 (.n1 - 1) \n" //
 				+ "        , fib .n1 .f1 \n" //
 				+ "        , fib .n2 .f2 \n" //
 				+ "        , let .f (.f1 + .f2) \n" //
 				+ "    # \n" //
 				+ ") >> fib 10 89"));
 	}
 
 	@Test
 	public void testLogic() throws IOException {
 		assertTrue(eval("1 = 2; 3 = 3"));
 		assertFalse(eval("3 = 3, 1 = 2"));
 	}
 
 	@Test
	public void testNot() throws IOException {
		assertTrue(eval("not (1 = 2)"));
		assertTrue(eval("not (1 = 1)"));
		assertTrue(eval("not (.v = 1), .v = 2"));
		assertFalse(eval("not (.v = 1), 1 = 2"));
	}

	@Test
 	public void testOnce() throws IOException {
 		assertTrue(eval("once (.v = 1; .v = 2), .v = 1"));
 		assertFalse(eval("once (.v = 1; .v = 2), .v = 2"));
 	}
 
 	@Test
 	public void testVariables() throws IOException {
 		assertTrue(eval(".a = 1, 1 = .a"));
 		assertFalse(eval(".a = 1, .a = 2"));
 	}
 
 	@Test
 	public void testWith() throws IOException {
 		assertTrue(eval("(p 2 # p 3 #) >> p .v, .v = 3"));
 		assertFalse(eval("(p 2 :- ! # p 3 #) >> p .v, .v = 3"));
 		assertTrue(eval("(p .v :- q .v # q 3 #) >> p 3"));
 	}
 
 	private boolean eval(String program) throws IOException {
 		return SuiteUtil.evaluateLogical(program);
 	}
 
 }
