 /**
  * 
  */
 package eu.larkc.iris.functional.features;
 
import org.deri.iris.functional.Helper;

 /**
  * Tests basic features of predicate and propositional logic that can be
  * expressed in Datalog.
  * 
  * @author Florian Fischer, fisf, 09-Dec-2010
  */
 public class SpecificLangFeaturesTest extends LangFeaturesTest {
 
 	public SpecificLangFeaturesTest(String string) {
 		super(string);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	/**
 	 * Check that rules containing a grounded term in the rule head are
 	 * correctly evaluated.
 	 * 
 	 * @throws Exception
 	 */
 	public void testGroundedTermInRuleHead() throws Exception {
 		program = "p(?X, 'a') :- r(?X, ?Y)." + "?- p(?X, ?Y).";
 
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 
 	}
 
 	public void testSelfJoin() throws Exception {
 		program =
 
 		"p(?X) :- s(?X, ?Y), s(?Y, ?X)." + "?- p(?X).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 	}
 
 	/**
 	 * Check that rules that involve a cartesian product are translated
 	 * correctly.
 	 * 
 	 * @throws Exception
 	 */
 	public void testCartesianProduct() throws Exception {
 		program = "w(?X,?Y) :- s(?X), p(?Y)." + "?- w(?Y,?X).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 
 	}
 
 	public void testTransitiveClosure() throws Exception {
 		program = "path(?X, ?Y) :- edge(?X, ?Y)."
 				+ "path(?X, ?Y) :- path(?X, ?Z), path(?Z, ?Y)."
 				+ "?- path(?X, ?Y).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 
 	}
 
 	public void testGroundedTermInQuery() throws Exception {
 		program = "in(?X, ?Z) :- in(?X, ?Y), in(?Y, ?Z)."
 				+ "?- in('galway', ?Z).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 
 	}
 
 	/**
 	 * Check that a long chain of rules can be correctly evaluated, i.e. a
 	 * implies b, b implies c, c implies d, etc.
 	 * 
 	 * @throws Exception
 	 */
 	public void testLongChainOfRules() throws Exception {
 		StringBuilder buffer = new StringBuilder();
 
 		final String p = "p";
 
 		final int count = 1000;
 
 		for (int predicate = 1; predicate < count; ++predicate) {
 			buffer.append(p + (predicate + 1)).append("(?X,?Y ) :- ").append(
 					p + predicate).append("(?X,?Y ).");
 		}
 		buffer.append("?- " + p + count + "(?x,?y).");
 
 		program = buffer.toString();
 
 	}
 
 	/**
 	 * Check that three rules for one head predicate evaluate correctly.
 	 * Theoretically that amounts to a union.
 	 */
 	public void testUnion() throws Exception {
 		program = "p(?X) :- r(?X)." + "p(?X) :- s(?X)." + "p(?X) :- t(?X)."
 				+ "?- p(?X).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 	}
 
 
 	public void testConstantsInRuleHead() throws Exception {
 		program =
 
 		"p(?X, 'a') :- r(?X, ?Y)." + "?- p(?X, ?Y).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 
 	}
 
 	public void testRuleWithOnlyConstantsInHead() throws Exception {
 		program = "p('a') :- TRUE." + "?-p(?X).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 	}
 
 	public void testRuleWithNoBodyAndOnlyConstantsInHead() throws Exception {
 		program = "p('a') :- ." + "?-p(?X).";
 		parser.parse(program);
 		rules = createRules();
 
 		compile();
 
 	}
 }
