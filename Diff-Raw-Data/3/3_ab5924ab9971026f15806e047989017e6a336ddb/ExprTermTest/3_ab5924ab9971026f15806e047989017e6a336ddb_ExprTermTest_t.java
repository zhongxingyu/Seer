 package test;
 
 import jason.JasonException;
 import jason.architecture.AgArch;
 import jason.asSemantics.Agent;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.ArithExpr;
 import jason.asSyntax.ArithFunctionTerm;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Literal;
 import jason.asSyntax.NumberTerm;
 import jason.asSyntax.NumberTermImpl;
 import jason.asSyntax.RelExpr;
 import jason.asSyntax.VarTerm;
 import jason.infra.centralised.CentralisedAgArch;
 
 import java.util.Collections;
 
 import junit.framework.TestCase;
 
 /** JUnit test case for syntax package */
 public class ExprTermTest extends TestCase {
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     public void testSolve() throws Exception {
         NumberTerm nb;
         nb = ArithExpr.parseExpr("3");
         assertEquals(nb.solve(),3.0);
 
         nb = ArithExpr.parseExpr("3+2");
         assertEquals(nb.solve(),new NumberTermImpl(5).solve());
 
         nb = ArithExpr.parseExpr("3+2*5");
         assertTrue(nb.solve() == 13);
 
         nb = ArithExpr.parseExpr("(3+2)*5");
         assertTrue(nb.solve() == 25);
 
         nb = ArithExpr.parseExpr("3 - 5");
         assertTrue(nb.solve() == -2);
                 
         nb = ArithExpr.parseExpr("-(3+5*(4----1))*-1-15");
         // System.out.println(nb+"="+nb.solve());
         assertTrue(nb.solve() == 13d);
 
         nb = ArithExpr.parseExpr("3+5.1*2");
         // System.out.println(nb+"="+nb.solve());
         assertTrue(nb.solve() == 13.2);
     }
 
     public void testApply() {
         NumberTerm nb = ArithExpr.parseExpr("(30-X)/(2*X)");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new NumberTermImpl(5));
         nb.apply(u);
         //System.out.println(nb+"="+nb.solve());
         assertEquals(nb, new NumberTermImpl(2.5));
         assertEquals(new NumberTermImpl(2.5), nb);
         assertEquals(new NumberTermImpl(2.5).hashCode(), nb.hashCode());
     }
 
     public void testUnify() {
         Literal t1 = (Literal) Literal.parseLiteral("p(X*2)").clone();
         Literal t2 = Literal.parseLiteral("p(Y)");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("H"), new NumberTermImpl(5));
         u.unifies(new VarTerm("X"), new VarTerm("H"));
         assertTrue(u.unifies(t1, t2));
         t1.apply(u);
         t1 = (Literal)t1.clone();
         assertEquals(t1.toString(), "p(10)");
         assertTrue(t1.getTerm(0).isNumeric());
         VarTerm yvl = new VarTerm("Y");
         yvl.apply(u);
         assertEquals(yvl, new NumberTermImpl(10));
         t2.apply(u);
         assertEquals(t2.toString(), "p(10)");
     }
 
     public void testAddAddAdd() {
         Literal t1 = Literal.parseLiteral("p(X+1)");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new NumberTermImpl(0));
         t1.apply(u);
         assertEquals(t1.toString(),"p(1)");
 
         u = new Unifier();
         u.unifies(Literal.parseLiteral("p(CurVl)"), t1);
         u.unifies(new VarTerm("CurVl"), new VarTerm("X"));
         t1 = Literal.parseLiteral("p(X+1)");
         t1.apply(u);
 
         u = new Unifier();
         u.unifies(Literal.parseLiteral("p(CurVl)"), t1);
         u.unifies(new VarTerm("CurVl"), new VarTerm("X"));
         t1 = Literal.parseLiteral("p(X+1)");
         t1.apply(u);
 
         assertEquals(t1.toString(), "p(3)");
     }
 
     public void testLiteralBuilder() throws JasonException {
         Literal l = Literal.parseLiteral("~p(t1,t2)[a1,a2]");
         assertEquals(l.getAsListOfTerms().size(), 3);
 
         ListTerm lt1 = ListTermImpl.parseList("[~p,[t1,t2],[a1,a2]]");
         assertTrue(l.equals(Literal.newFromListOfTerms(lt1)));
         ListTerm lt2 = ListTermImpl.parseList("[p,[t1,t2],[a1,a2]]");
         assertFalse(l.equals(Literal.newFromListOfTerms(lt2)));
         ListTerm lt3 = ListTermImpl.parseList("[~p,[t1,t2],[a1,a2,a3]]");
         assertFalse(l.equals(Literal.newFromListOfTerms(lt3)));
 
         Unifier u = new Unifier();
         assertFalse(u.unifies(lt1, lt2));
 
         assertTrue(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, lt1).logicalConsequence(null, u).hasNext());
         assertFalse(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, lt2).logicalConsequence(null, u).hasNext());
         assertFalse(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, lt3).logicalConsequence(null, u).hasNext());
 
         VarTerm v = new VarTerm("X");
         u.clear();
         assertTrue(new RelExpr(v, RelExpr.RelationalOp.literalBuilder, lt1).logicalConsequence(null, u).hasNext());
         assertEquals(u.get("X").toString(), l.toString());
         assertEquals(u.get("X"), l);
         assertEquals(l, u.get("X"));
 
         u.clear();
         assertTrue(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, v).logicalConsequence(null, u).hasNext());
         assertEquals(u.get("X").toString(), lt1.toString());
         assertEquals(u.get("X"), lt1);
         assertEquals(lt1, u.get("X"));
 
         l = Literal.parseLiteral("p(t1,t2)");
         assertEquals(l.getAsListOfTerms().size(), 3);    
         assertEquals(((ListTerm)l.getAsListOfTerms().get(1)).size(), 2);    
         assertEquals(((ListTerm)l.getAsListOfTerms().get(2)).size(), 0);    
 
     }
 
     public void testFuncAbs() {
         NumberTerm nb = ArithExpr.parseExpr("(30-math.abs(X))/(2*math.abs(X))");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new NumberTermImpl(-5));
         nb.apply(u);
         //System.out.println(nb+"="+nb.solve());
         assertEquals(nb, new NumberTermImpl(2.5));
         assertEquals(nb.clone(), new NumberTermImpl(2.5));
     }
     
     public void testFuncMax() {
         NumberTerm nb = ArithExpr.parseExpr("math.max(30-math.abs(X),2*math.abs(X))");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new NumberTermImpl(-5));
         nb.apply(u);
         assertEquals(nb, new NumberTermImpl(25));
         assertEquals(nb.clone(), new NumberTermImpl(25));
 
         nb = ArithExpr.parseExpr("math.max([a,3,b(100),400,1])");
         nb.apply(u);
         assertEquals(nb, new NumberTermImpl(400));
     }
     
     public void testSort() {
         ListTerm l = ListTermImpl.parseList("[a(3),10,3,-1,math.abs(55),X+10,math.abs(X),math.max(X-1,X*30)]");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new NumberTermImpl(-5));
         l.apply(u);
         Collections.sort(l);
         assertEquals(l.toString(), "[-6,-1,3,5,5,10,55,a(3)]");    	
     }
     
     public void testLength() {
         NumberTerm nb = ArithExpr.parseExpr(".length(\"aaa\")");
         Unifier u = new Unifier();
         nb.apply(u);
         assertEquals(nb, new NumberTermImpl(3));
 
         nb = ArithExpr.parseExpr(".length([a,3,b(100),400,1])");
         nb.apply(u);
         assertEquals(nb, new NumberTermImpl(5));    	
     }
     
     public void testCount() throws Exception {
         Agent ag = new Agent();
         ag.initDefaultFunctions();
         AgArch arch = new AgArch();
         arch.setArchInfraTier(new CentralisedAgArch());
         ag.setTS(new TransitionSystem(ag, null, null, arch));
 
        assertTrue(ag.getFunction(".count",1) != null);
         ag.getBB().add(Literal.parseLiteral("b(10)"));
         ag.getBB().add(Literal.parseLiteral("a(x)"));
         ag.getBB().add(Literal.parseLiteral("b(20)"));
         assertEquals(3, ag.getBB().size());
         
         ArithFunctionTerm nb = (ArithFunctionTerm)ArithExpr.parseExpr(ag,".count(b(_))");
         nb.apply(new Unifier());
         assertEquals(2.0,nb.solve());
     }
 }
