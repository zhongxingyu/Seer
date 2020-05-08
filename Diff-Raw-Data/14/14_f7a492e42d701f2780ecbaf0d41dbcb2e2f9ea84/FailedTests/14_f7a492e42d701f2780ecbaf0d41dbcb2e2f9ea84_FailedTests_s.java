 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.instructionexecutor.FunRbTreeTest;
 import org.instructionexecutor.InstructionTranslatorTest;
 import org.junit.Test;
 import org.suite.Journal;
 import org.suite.Suite;
 import org.suite.doer.Binder;
 import org.suite.doer.Formatter;
 import org.suite.doer.Generalizer;
 import org.suite.doer.ProverConfig;
 import org.suite.kb.RuleSet;
 import org.suite.node.Node;
 import org.suite.search.CompiledProverBuilder.CompiledProverBuilderLevel2;
 import org.suite.search.ProverBuilder.Builder;
 
 public class FailedTests {
 
 	// Need to increase InstructionExecutor.stackSize, or implement tail
 	// recursion
 	@Test
 	public void test0() {
 		RuleSet rs = Suite.nodeToRuleSet(Suite.parse("" //
 				+ "member (.e, _) .e #" //
 				+ "member (_, .tail) .e :- member .tail .e #" //
 				+ "sum .a .b .c :- bound .a, bound .b, let .c (.a - .b) #" //
 				+ "sum .a .b .c :- bound .a, bound .c, let .b (.a - .c) #" //
 				+ "sum .a .b .c :- bound .b, bound .c, let .a (.b + .c) #" //
 		));
 
 		Node goal = Suite.parse("(), sink ()");
 		Builder builder = new CompiledProverBuilderLevel2(new ProverConfig(), false);
 		Suite.evaluateLogic(builder, rs, goal);
 	}
 
 	// Type check take 11 seconds
 	@Test
 	public void test1() throws IOException {
 		new FunRbTreeTest().test();
 	}
 
 	// Strange error message "Unknown expression if b"
 	@Test
 	public void test2() throws IOException {
 		Suite.evaluateFun("if a then b", false);
 	}
 
 	// Code too large
 	@Test
 	public void test3() throws IOException {
 		new InstructionTranslatorTest().testStandardLibrary();
 	}
 
 	// Resolved dangling types
 	@Test
 	public void test4() throws IOException {
 		String f = "" //
 				+ "define merge-sort = (merge => list => \n" //
				+ "    if true then \n" //
				+ "        define list0 = (list | _ltail | merge-sort {merge}) >> \n" //
				+ "        merge {list0} {list0} \n" //
				+ "    else list \n" //
 				+ ") >> \n" //
				+ "merge-sort \n";
 
		checkType(f, "(list-of T => _) => _", "(list-of T => list-of T => list-of T) => list-of T => list-of T");
 	}
 
 	private void checkType(String f, String bindTo, String ts) {
 		Node type;
 		type = getType(f);
 		Binder.bind(type, new Generalizer().generalize(Suite.parse(bindTo)), new Journal());
 		assertEquals(ts, Formatter.dump(type));
 	}
 
 	private static Node getType(String f) {
 		return Suite.evaluateFunType(f);
 	}
 
 }
