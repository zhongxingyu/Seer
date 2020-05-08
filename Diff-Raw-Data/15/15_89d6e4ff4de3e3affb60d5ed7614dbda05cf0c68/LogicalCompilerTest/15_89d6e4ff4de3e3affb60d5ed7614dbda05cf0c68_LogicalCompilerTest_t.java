 package org.instructioncode;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.suite.SuiteUtil;
 import org.suite.doer.Generalizer;
 import org.suite.doer.Prover;
 import org.suite.kb.RuleSet;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 
 public class LogicalCompilerTest {
 
 	@Test
 	public void test() throws IOException {
 		assertTrue(run("()"));
		assertTrue(run("3 = 3"));
 		assertFalse(run("fail"));
 		assertFalse(run("1 = 2"));
 	}
 
	@Test
	public void testLogic() throws IOException {
		assertTrue(run("1 = 2; 3 = 3"));
		assertFalse(run("3 = 3, 1 = 2"));
	}

 	private boolean run(String program) throws IOException {
 		RuleSet rs = new RuleSet();
 		SuiteUtil.importResource(rs, "auto.sl");
 		SuiteUtil.importResource(rs, "lc.sl");
 
 		Node node = SuiteUtil.parse("" //
 				+ "compile (\n" + program + "\n) .c \n" //
 				+ ", pp-list .c");
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		assertTrue(new Prover(rs).prove(node));
 
 		Node ics = generalizer.getVariable(Atom.create(".c"));
 		Node result = new InstructionCodeExecutor(ics).execute();
 		return result == Atom.create("true");
 	}
 
 }
