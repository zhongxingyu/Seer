 package org.instructionexecutor;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.suite.SuiteUtil;
 import org.suite.node.Int;
 import org.suite.node.Node;
 
 public class LazyFunctionCompilerTest {
 
 	@Test
 	public void testClosure() {
 		assertEquals(SuiteUtil.parse("4") //
 				, eval("define v as number = 4 >> (i => j => v) {1} {2}"));
 	}
 
 	@Test
 	public void testFibonacci() {
		assertEquals(Int.create(0), eval("" //
 				+ "define seq = (n => n, seq {n}) >> \n" //
				+ "head {seq {0}}"));
 
 		assertEquals(Int.create(89), eval("" // Real co-recursion!
 				+ "define fib = (i1 => i2 => i2, fib {i2} {i1 + i2}) >> \n" //
 				+ "define h = (f => head {f}) >> \n" //
 				+ "define t = (f => tail {f}) >> \n" //
 				+ "apply {fib {0} {1}} {t, t, t, t, t, t, t, t, t, t, h,}"));
 	}
 
 	@Test
 	public void testSubstitution() {
 		assertEquals(Int.create(8), eval("define v = 4 >> v + v"));
 	}
 
 	@Test
 	public void testSystem() {
 		eval("cons {1} {}");
 		eval("head {1, 2, 3,}");
 		eval("tail {1, 2, 3,}");
 	}
 
 	private static Node eval(String f) {
 		return SuiteUtil.evaluateFunctional(f, true);
 	}
 
 }
