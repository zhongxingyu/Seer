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
 
		SuiteUtil.addRule(rs, "add-list () .t/.t");
		SuiteUtil.addRule(rs, "add-list (.head, .tail) .t0/.tx " //
				+ ":- t23-map .head/.head .t0/.t1" //
				+ ", add-list .tail .t1/.tx");
 
 		assertTrue(SuiteUtil.proveThis(rs, "" //
				+ "add-list (1, 2, 3, 4, 5, 6, 7, 8, 9, 10,) ()/.t0" //
				+ ", t23-map 6/6 .t0/.t1" //
				+ ", dump .t1, nl" //
				+ ", t23-search 4/4 .t1"));
 	}
 
 }
