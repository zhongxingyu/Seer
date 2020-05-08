 package de.b2tla.ltl;
 
 
 import static de.b2tla.util.TestUtil.compareLTL;
 import static de.b2tla.util.TestUtil.compareEqualsConfig;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.b2tla.exceptions.ScopeException;
 import de.b2tla.exceptions.TypeErrorException;
 
 
 public class LtlFormulaTest {
 
 	
 	
 	@Test (expected = ScopeException.class)
 	public void testScopeException() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		compareLTL("", machine, "G{y = 1}");
 	}
 	
 	@Test (expected = TypeErrorException.class)
 	public void testTypeError() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		compareLTL("", machine, "G{x = FALSE}");
 	}
 	
 	@Test (expected = ScopeException.class) 
 	public void testUnkownOperation() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "e(foo)");
 	}
 	
 	@Ignore
 	@Test 
 	public void testGobally() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "VARIABLES x\n"
 				+ "Invariant == x = 1\n"
 				+ "Init == x = 1\n"
 				+ "foo == x' = 1\n\n"
 				+ "Next == \\/ foo\n"
 				+ "Spec == Init /\\ [][Next]_<<x>> /\\ WF_<<x>>(Next)\n"
 				+ "ltl == [](x = 1)\n"
 				+ "====";
 		compareLTL(expected, machine, "G{x = 1}");
 	}
 	
 	@Ignore
 	@Test 
 	public void testFinally() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "VARIABLES x\n"
 				+ "Invariant == x = 1\n"
 				+ "Init == x = 1\n"
 				+ "foo == x' = 1\n\n"
 				+ "Next == \\/ foo\n"
 				+ "Spec == Init /\\ [][Next]_<<x>> /\\ WF_<<x>>(Next)\n"
 				+ "ltl == <>(x = 1)\n"
 				+ "====";
 		compareLTL(expected, machine, "F{x = 1}");
 	}
 	
 	@Test 
 	public void testTrue() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "ltl == TRUE\n"
 				+ "====";
 		compareLTL(expected, machine, "true");
 	}
 	
 	@Test 
 	public void testFalse() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "ltl == FALSE\n"
 				+ "====";
 		compareLTL(expected, machine, "false");
 	}
 	
 	@Test 
 	public void testImplication() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "ltl == FALSE => TRUE\n"
 				+ "====";
 		compareLTL(expected, machine, "false => true");
 	}
 	
 	@Test 
 	public void testAnd() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "ltl == TRUE /\\ FALSE\n"
 				+ "====";
 		compareLTL(expected, machine, "true & false");
 	}
 	
 	@Test 
 	public void testOr() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "ltl == TRUE \\/ FALSE\n"
 				+ "====";
 		compareLTL(expected, machine, "true or false");
 	}
 	
 	@Test 
 	public void testNegation() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "ltl == \\neg(TRUE)\n"
 				+ "====";
 		compareLTL(expected, machine, "not(true)");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testUntil() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "true U false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testWeakUntil() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "true W false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testRelease() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "true R false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testHistory() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "H false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testOnce() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "O false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testYesterday() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "Y false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testSince() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "true S false");
 	}
 	
 	@Test (expected = ScopeException.class)
 	public void testTrigger() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		compareLTL("", machine, "true T false");
 	}
 	
 	@Ignore
 	@Test 
 	public void testEnabled() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "VARIABLES x\n"
 				+ "Invariant == x = 1\n"
 				+ "Init == x = 1\n"
 				+ "foo == x' = 1\n\n"
 				+ "Next == \\/ foo\n"
 				+ "Spec == Init /\\ [][Next]_<<x>> /\\ WF_<<x>>(Next)\n"
 				+ "ltl == ENABLED(foo)\n"
 				+ "====";
 		compareLTL(expected, machine, "e(foo)");
 	}
 	
 	@Ignore
 	@Test 
 	public void testWeakFairness() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "VARIABLES x\n"
 				+ "Invariant == x = 1\n"
 				+ "Init == x = 1\n"
 				+ "foo == x' = 1\n\n"
 				+ "Next == \\/ foo\n"
 				+ "Spec == Init /\\ [][Next]_<<x>> /\\ WF_<<x>>(Next)\n"
 				+ "ltl == ENABLED(foo)\n"
 				+ "====";
 		compareLTL(expected, machine, "WF(foo)");
 	}
 	
 	@Ignore
 	@Test 
 	public void testStrongFairness() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		
 		String expected = "---- MODULE test ----\n"
 				+ "VARIABLES x\n"
 				+ "Invariant == x = 1\n"
 				+ "Init == x = 1\n"
 				+ "foo == x' = 1\n\n"
 				+ "Next == \\/ foo\n"
 				+ "Spec == Init /\\ [][Next]_<<x>> /\\ WF_<<x>>(Next)\n"
 				+ "ltl == ENABLED(foo)\n"
 				+ "====";
 		compareLTL(expected, machine, "SF(foo)");
 	}
 	
 	
 	@Test 
 	public void testExistentialQuantification() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		String expected = "---- MODULE test ----\n"
				+ "ltl == \\E p \\in {1}: p = 1 /\\ TRUE\n"
 				+ "====";
		compareLTL(expected, machine, "#p.({p=1} & true)");
 	}
 	
 	@Test 
 	public void testForallQuantification() throws Exception {
 		String machine = "MACHINE test\n"
 				+ "END";
 		String expected = "---- MODULE test ----\n"
				+ "ltl == \\A p \\in {1}: p = 1 /\\ TRUE\n"
 				+ "====";
		compareLTL(expected, machine, "!p.({p=1} => true)");
 	}
 	
 	@Ignore
 	@Test 
 	public void testLTLFormulaInDefinitions() throws Exception {
 		final String machine = "MACHINE test\n"
 				+ "DEFINITIONS ASSERT_LTL == \"false\" \n"
 				+ "VARIABLES x\n"
 				+ "INVARIANT x = 1\n"
 				+ "INITIALISATION x := 1\n"
 				+ "OPERATIONS foo = x := 1"
 				+ "END";
 		
 		final String expected = "---- MODULE test ----\n"
 				+ "VARIABLES x\n"
 				+ "Invariant == x = 1\n"
 				+ "Init == x = 1\n"
 				+ "foo == x' = 1\n\n"
 				+ "Next == \\/ foo\n"
 				+ "Spec == Init /\\ [][Next]_<<x>> /\\ WF_<<x>>(Next)\n"
 				+ "ASSERT_LTL == FALSE\n"
 				+ "====";
 		final String config = "SPECIFICATION Spec\nINVARIANT Invariant\nPROPERTIES ASSERT_LTL\n";
 		
 		compareEqualsConfig(expected, config, machine);
 	}
 	
 }
