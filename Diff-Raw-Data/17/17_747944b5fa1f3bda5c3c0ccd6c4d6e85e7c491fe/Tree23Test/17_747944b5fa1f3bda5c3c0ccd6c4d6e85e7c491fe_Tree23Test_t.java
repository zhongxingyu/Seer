 package org.suite;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.suite.kb.RuleSet;
 
 public class Tree23Test {
 
 	@Test
 	public void test() throws IOException {
 		RuleSet rs = new RuleSet();
 		SuiteUtil.importResource(rs, "auto.sl");
 		SuiteUtil.importResource(rs, "t23.sl");
 
		SuiteUtil.addRule(rs, "add-list .t/.t ()");
		SuiteUtil.addRule(rs, "add-list .t0/.tx (.head, .tail) \n" //
				+ ":- t23-map .t0/.t1 .head/.head \n" //
				+ ", dump .t1, nl \n" //
				+ ", add-list .t1/.tx .tail");
 
 		assertTrue(SuiteUtil.proveThis(rs, "" //
				+ "add-list ()/.t0 (1, 2, 3, 4, 5, 6, 7, 8, 9, 10,) \n" //
				+ ", t23-map .t0/.t1 6/6 \n" //
				+ ", t23-search .t1 4/4"));
 	}
 
 }
