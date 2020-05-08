 package org.instructionexecutor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.suite.Binder;
 import org.suite.Journal;
 import org.suite.SuiteUtil;
 import org.suite.doer.Generalizer;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 
 public class FunctionCompilerTypeTest {
 
 	@Test
 	public void testBasic() {
 		assertEquals(SuiteUtil.parse("BOOLEAN") //
 				, getType("4 = 8"));
 	}
 
 	@Test
 	public void testDefineType() {
 		getType("define type t = number >> \n" //
 				+ "define v as t = 1 >> \n" //
 				+ "v = 99");
 	}
 
 	@Test
 	public void testFun() {
 		assertEquals(SuiteUtil.parse("FUN NUMBER NUMBER") //
 				, getType("a => a + 1"));
 		assertEquals(SuiteUtil.parse("NUMBER") //
 				, getType("define f = (a => a + 1) >> f {3}"));
 		assertTrue(Binder.bind(SuiteUtil.parse("FUN _ (CO-LIST-OF NUMBER)") //
 				, getType("define fib = (n => dummy => n/(fib {n + 1})) >> \n" //
 						+ "fib {1}") // Pretends co-recursion
 				, new Journal()));
 	}
 
 	@Test
 	public void testList() {
 		assertEquals(SuiteUtil.parse("LIST-OF NUMBER") //
 				, getType("1,"));
 		assertEquals(SuiteUtil.parse("LIST-OF (LIST-OF NUMBER)") //
 				, getType("\"a\", \"b\", \"c\", \"d\","));
 	}
 
 	@Test
 	public void testOneOf() {
 		getType("" //
 				+ "define type t = one of (NIL, BTREE t t,) >> \n" //
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
				, "1, \"abc\"" //
 				, "define fib = (i2 => dummy => 1, fib {i2}) >> ()" //
 				, "define type t = one of (BTREE t t,) >> \n" //
 						+ "define v as t = BTREE 2 3 >> \n" //
 						+ "1" };
 
 		// There is a problem in deriving type of 1:(fib {i2})...
 		// Rule specified that right hand side of CONS should be a list,
 		// however fib {i2} is a closure.
 		// Should actually use corecursive list type (cons-ed by '^').
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
 
 	private static Node getType(String f) {
 		Node program = SuiteUtil.parse(f);
 
 		Node node = SuiteUtil.parse("fc-parse .program .p" //
 				+ ", infer-type-rule .p ()/()/() .tr .type" //
 				+ ", resolve-types .tr");
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		Node variable = generalizer.getVariable(Atom.create(".program"));
 		Node type = generalizer.getVariable(Atom.create(".type"));
 
 		((Reference) variable).bound(program);
 
 		String[] imports = { "auto.sl", "fc.sl" };
 		if (SuiteUtil.getProver(imports).prove(node))
 			return type.finalNode();
 		else
 			throw new RuntimeException("Type inference error");
 	}
 
 }
