 package nfa.test;
 
 import static nfa.NFAUtil.a;
 import static nfa.NFAUtil.aOrB;
 import static nfa.NFAUtil.aPlus;
 import static nfa.NFAUtil.aStar;
 import static nfa.NFAUtil.ab;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import nfa.NFA;
 import nfa.NFAUtil;
 import nfa.NFAUtil.NFASegment;
 import nfa.State;
 import nfa.Transition;
 
 import org.junit.Test;
 
 public class NFAUtilTest {
 
 	@Test
 	public void EClosureTest() {
 		State a = new State("a", false);
 		State b = new State("b", false);
 		State c = new State("c", false);
 		State d = new State("d", false);
 		State e = new State("e", false);
 
 		a.addTransition(new Transition(b));
 		b.addTransition(new Transition(c), new Transition("f", e));
 		b.addTransition(new Transition(d));
 		c.addTransition(new Transition(d));
 		d.addTransition(new Transition("f", e));
 
 		List<State> results = NFAUtil.findClosure(a);
 
 		assertTrue("E-Closure retrieval failed.", results.contains(a) && results.contains(b)
 				&& results.contains(c) && results.contains(d) && !results.contains(e));
 	}
 
 	@Test
 	public void nfaBuilderTest() {
 		// (a|b)*
 		NFASegment a = NFAUtil.a("a");
 		NFASegment b = NFAUtil.a("b");
 		NFASegment aOrB = NFAUtil.aOrB(a, b);
 		NFASegment total = NFAUtil.aStar(aOrB);
 		total.end.addTransition(new Transition(new State("trueEnd", true)));
 		assertTrue("(a|b)*", NFAUtil.isValid(total, "ababba"));
 		assertTrue("(a|b)*", NFAUtil.isValid(total, ""));
 
 		// a*b(a|b)+
 		NFASegment d = ab(ab(aStar(a("a")), a("b")), aPlus(aOrB(a("a"), a("b"))));
 		d.end.addTransition(Transition.spawnGoal());
 		assertTrue("a*b(a|b)+", NFAUtil.isValid(d, "aaaaba"));
 		assertTrue("a*b(a|b)+", !NFAUtil.isValid(d, "aa"));
 		assertTrue("a*b(a|b)+", !NFAUtil.isValid(d, "b"));
 	}
 
 	@Test
 	public void nfaConverterTest() {
 		// a*b(a|b)+
 		NFASegment d = ab(ab(aStar(a("a")), a("b")), aPlus(aOrB(a("a"), a("b"))));
 		d.end.addTransition(Transition.spawnGoal());
 		NFA n = new NFA(d.start);
 		n = NFAUtil.convertToDFA(n);
 		assertTrue("a*b(a|b)+ is not NFA", n.isDFA());
 		assertTrue("a*b(a|b)+", NFAUtil.isValid(d, "aaba"));
 		assertTrue("a*b(a|b)+", !NFAUtil.isValid(d, "aa"));
 		assertTrue("a*b(a|b)+", !NFAUtil.isValid(d, "b"));
 
 		// (a|b)*
 		NFASegment a = NFAUtil.a("a");
 		NFASegment b = NFAUtil.a("b");
 		NFASegment aOrB = NFAUtil.aOrB(a, b);
 		NFASegment total = NFAUtil.aStar(aOrB);
 		total.end.addTransition(new Transition(new State("trueEnd", true)));
 		NFA daNFA = new NFA(total.start);
 		daNFA = NFAUtil.convertToDFA(daNFA);
 		assertTrue("(a|b)*", NFAUtil.isValid(daNFA, "ababba"));
 		assertTrue("(a|b)*", NFAUtil.isValid(daNFA, ""));
 		assertTrue("(a|b)*", !NFAUtil.isValid(daNFA, "gggab"));
 	}
 }
