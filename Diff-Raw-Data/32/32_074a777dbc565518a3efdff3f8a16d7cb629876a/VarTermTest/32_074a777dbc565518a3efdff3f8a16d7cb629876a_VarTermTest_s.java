 package test;
 
 import jason.asSemantics.Unifier;
 import jason.asSyntax.ExprTerm;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Literal;
 import jason.asSyntax.NumberTermImpl;
 import jason.asSyntax.Pred;
 import jason.asSyntax.Term;
 import jason.asSyntax.VarTerm;
 
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 /** JUnit test case for syntax package */
 public class VarTermTest extends TestCase {
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
     	Logger.getRootLogger().setLevel(Level.DEBUG);
 	}
 
 	/** test when a var is ground with a Term or another var*/
 	public void testVarTermAsTerm() {
 		VarTerm k = new VarTerm("K");
 		Unifier u = new Unifier();
 		u.unifies(k, new Term("a1"));
 		assertTrue("K".equals(k.toString()));
 		u.apply(k);
 		assertTrue("a1".equals(k.toString()));
 		k.addTerm(new Term("p1"));
 		k.addTerm(new Term("p2"));
 		assertEquals(k.getTermsSize(), 2);
 		
 		
 		VarTerm x1 = new VarTerm("X1");
 		VarTerm x2 = new VarTerm("X2");
 		VarTerm x3 = new VarTerm("X3");
 		
 		x1.setValue(x2);
 		x2.setValue(x3);
 		
 		x3.setValue(new Term("a"));
 		// x1's value is x3's value (a)
 		assertEquals(x1.getValue().toString(), "a");
 		
 		// test x1 -> x2 -> x3 -> x1!
 		assertFalse(x3.setValue(x1));
 		
 		// unification with lists
 		VarTerm v1 = new VarTerm("L");
 		ListTerm lt = ListTermImpl.parseList("[a,B,a(B)]");
 		u = new Unifier();
 		u.unifies(new VarTerm("B"), new Term("oi"));
 		u.unifies(v1, (Term)lt);
 		u.apply(v1);
 		lt = (ListTerm)v1.getValue();
 		Iterator i = lt.iterator();
 		i.next();i.next();
 		Term third = (Term)i.next();
 		assertTrue(third.equals(Term.parse("a(oi)")));
 	}
 
 	/** test when a var is ground with a Pred */
 	public void testVarTermAsPred() {
 		VarTerm k = new VarTerm("K");
 		Unifier u = new Unifier();
 		u.unifies(k, new Pred("p"));
 		assertFalse(k.isPred());
 		u.apply(k);
 		assertTrue(k.isPred());
 		assertTrue(k.hasNoAnnot());
 		k.addAnnot(new Term("annot1"));
 		assertFalse(k.hasNoAnnot());
 
 		k.addSource("marcos");
 		assertEquals(k.getAnnots().size(), 2);
 		k.delSources();
 		assertEquals(k.getAnnots().size(), 1);
 		
 		// test with var not ground
 		k = new VarTerm("K");
 		u = new Unifier();
 		u.unifies(k, new Pred("p"));
 		k.addAnnot(new Term("annot1"));
 		assertEquals(k.getAnnots(), null);		
 	}
 
 	/** test when a var is ground with a Literal */
 	public void testVarTermAsLiteral() {
 		VarTerm k = new VarTerm("K");
 		Unifier u = new Unifier();
 		assertTrue(k.isVar());
 		Literal l = Literal.parseLiteral("~p(a1,a2)[n1,n2]");
 		assertTrue(l.isLiteral());
 		assertTrue(l.isPred());
 		assertTrue(l.negated());
 		assertTrue(u.unifies(k,l));
 		assertFalse(k.isLiteral());
 		u.apply(k);
 		//System.out.println(k+" u="+u);
 		assertFalse(k.isVar());
 		assertTrue(k.isLiteral());
 		assertTrue(k.negated());
 	}
 
 	/** test when a var is ground with a List */
 	public void testVarTermAsList() {
 		VarTerm k = new VarTerm("K");
 		Unifier u = new Unifier();
 		Term l1 = (Term)ListTermImpl.parseList("[a,b,c]");
 		assertTrue(l1.isList());
 		assertTrue(u.unifies(k,l1));
 		assertFalse(k.isList());
 		//u.apply(k);
 		//assertTrue(k.isList());
 		//assertEquals(k.size(),3);
 
 		ListTerm l2 = ListTermImpl.parseList("[d,e|K]");
 		//System.out.println("l2="+l2);
 		VarTerm nl = new VarTerm("NK");
 		u.unifies(nl, (Term)l2);
 		u.apply(nl);
 		//System.out.println(nl+ " un="+u);
 		assertEquals(nl.size(), 5);
 		
 		u.apply((Term)l2);
 		assertEquals(l2.size(), 5);
 	}
 
 	/** test when a var is ground with a Number */
 	public void testVarTermAsNumber() {
 		VarTerm k = new VarTerm("K");
 		Unifier u = new Unifier();
 		NumberTermImpl n = new NumberTermImpl(10);
 		assertTrue(n.isNumber());
 		assertFalse(n.isVar());
 		assertTrue(u.unifies(k,n));
 		u.apply(k);
 		//System.out.println(k+" u="+u);
 		assertTrue(k.isNumber());
 		assertFalse(k.isLiteral());
 		
 		ExprTerm exp = new ExprTerm(k, ExprTerm.EOplus, new NumberTermImpl(20));
 		assertTrue(exp.solve() == 30d);
 	}
 	
 	public void testUnify() {
 		// var with literal
 		VarTerm k = new VarTerm("K");
 		Literal l1 = Literal.parseLiteral("~p(a1,a2)[n1,n2]");
 		Unifier u = new Unifier();
 		assertTrue(u.unifies(k,l1));
 		assertTrue(k.isVar());
 		assertTrue(u.unifies(l1,k));
 
 		k = new VarTerm("K");
 		Literal l2 = Literal.parseLiteral("p(a1,a2)[n1,n2]");
 		u = new Unifier();
 		assertTrue(u.unifies(k,l1));
 		//System.out.println(k+" - "+u);
 		assertFalse(u.unifies(l2,k));
 		
 		Literal l3 = Literal.parseLiteral("~p(X,Y)[A1]");
 		VarTerm k2 = new VarTerm("K");
 		u = new Unifier();
 		assertTrue(u.unifies(k2,l3));
 		assertTrue(u.unifies(k2,l1));
 	}
 }
