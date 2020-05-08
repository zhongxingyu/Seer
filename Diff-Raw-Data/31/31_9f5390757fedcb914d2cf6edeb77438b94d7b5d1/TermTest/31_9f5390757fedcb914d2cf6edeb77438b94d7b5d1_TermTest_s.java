 package test;
 
 import static jason.asSyntax.ASSyntax.*;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.ASSyntax;
 import jason.asSyntax.Atom;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Literal;
 import jason.asSyntax.LiteralImpl;
 import jason.asSyntax.NumberTermImpl;
 import jason.asSyntax.Plan;
 import jason.asSyntax.Pred;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.UnnamedVar;
 import jason.asSyntax.VarTerm;
 import jason.asSyntax.Trigger.TEOperator;
 import jason.asSyntax.Trigger.TEType;
 import jason.asSyntax.parser.ParseException;
 import jason.bb.BeliefBase;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 /** JUnit test case for syntax package */
 public class TermTest extends TestCase {
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     public void testEquals() {
         Structure t1, t2, t3;
         t1 = new Structure("pos");
         t2 = new Structure(t1);
         t3 = new Structure("pos");
         assertTrue(t1.equals(t2));
         assertTrue(t1.equals(t3));
         
         t1.addTerm(new Atom("a"));
         assertFalse(t1.equals(t2));
         
         t2.addTerm(new Atom("a"));
         assertEquals(new Atom("a"),new Atom("a"));
         assertTrue(t1.equals(t2));
         assertTrue(t2.equals(t1));
         assertEquals(t1.hashCode(),t2.hashCode());
 
         Structure targ1 = new Structure("b");
         targ1.addTerm(new Atom("1"));
         Structure targ2 = new Structure("b");
         targ2.addTerm(new Atom("2"));
 
         t1.addTerm(targ1);
         assertFalse(t1.equals(t2));
         
         Structure targ1a = new Structure("b");
         targ1a.addTerm(new Structure("1"));
         t3.addTerm(new Structure("a"));
         t3.addTerm(targ1a);
         assertTrue(t1.equals(t3));
         
         // tests with variables
         t1.addTerm(new Structure("c"));
         t3.addTerm(new VarTerm("X"));
         assertFalse(t1.equals(t3));
         
         Literal l3 = new LiteralImpl(true, new Pred("pos"));
         l3.addAnnot(BeliefBase.TPercept);
         Literal l4 = new LiteralImpl(true, new Pred("pos"));
         l4.addAnnot(BeliefBase.TPercept);
         assertEquals(l3, l4);
         
         Term tpos = new Atom("pos");
         assertFalse(l3.equals(tpos));
         assertFalse(tpos.equals(l3));
         //System.out.println(new Term("pos")+"="+l3+" --> "+new Term("pos").equals(l3));
 
         assertFalse(new Pred("pos").equals(l3));
         assertTrue(new Pred("pos").equalsAsStructure(l3));
         Pred panot = new Pred("pos");
         panot.addAnnot(new Structure("bla"));
         assertTrue(l3.equalsAsStructure(panot));
         
         // basic VarTerm test
         assertTrue(new VarTerm("X").equals(new VarTerm("X")));
         assertFalse(new VarTerm("X").equals(new VarTerm("Y")));
         assertFalse(new VarTerm("X").equals(new Structure("X")));
         
         VarTerm x1 = new VarTerm("X1");
         x1.setValue(new Structure("a"));
         assertFalse(x1.equals(new VarTerm("X1")));
         
         VarTerm x2 = new VarTerm("X2");
         x2.setValue(new Structure("a"));
         assertTrue(x1.equals(x2));
         assertTrue(x2.equals(x1));
         assertEquals(x1.hashCode(), x2.hashCode());
         
         Term ta = new Structure("a");
         assertTrue(x1.equals(ta));
         assertTrue(ta.equals(x1));
         assertEquals(x1.hashCode(), ta.hashCode());
     }
 
     public void testUnifies() throws ParseException {
         assertTrue(new Unifier().unifies(new Structure("a"), new Structure("a")));
         assertTrue(new Unifier().unifies(ASSyntax.parseTerm("a"), ASSyntax.parseTerm("a")));
         assertTrue(new Unifier().unifies(new Structure("a"), new VarTerm("X")));
         
         Unifier u = new Unifier();
         VarTerm b = new VarTerm("B");
         VarTerm x = new VarTerm("X");
         assertTrue(u.unifies(b, x));
         assertTrue(u.unifies(new Structure("a"), x));
         //System.out.println("u="+u);
         assertEquals(u.get(b).toString(), "a");
         assertEquals(u.get(x).toString(), "a");
         b.apply(u);
         //System.out.println("x="+x);
         //System.out.println("b="+b);
         assertEquals(b.toString(), "a");
         assertEquals(x.toString(), "X");
         
         u = new Unifier();
         Structure t1, t2, t3;
         
         t1 = new Structure("pos");
         t2 = new Structure(t1);
         t3 = new Structure(t1);
 
         t1.addTerm(new Structure("1"));
         t1.addTerm(new Structure("2"));
 
         t2.addTerm(new VarTerm("X"));
         t2.addTerm(new VarTerm("X"));
         assertFalse(u.unifies(t1,t2));
 
         u = new Unifier();
         t3.addTerm(new VarTerm("X"));
         t3.addTerm(new VarTerm("Y"));
         //System.out.println(t1+"="+t3);
         assertTrue( u.unifies(t1,t3));
         //System.out.println("u="+u);
     
         // Test var unified with var
         u = new Unifier();
         VarTerm z1 = new VarTerm("Z1");
         VarTerm z2 = new VarTerm("Z2");
         VarTerm z3 = new VarTerm("Z3");
         VarTerm z4 = new VarTerm("Z4");
         // Z1 = Z2 = Z3 = Z4
         assertTrue(u.unifies(z1,z2));
         assertTrue(u.unifies(z2,z3));
         assertTrue(u.unifies(z2,z4));
         
         assertTrue(z1.isVar()); // z1 is still a var
         assertTrue(z2.isVar()); // z2 is still a var
         
         assertTrue(u.unifies(z2,new Structure("a")));
         //System.out.println("u="+u);
         assertEquals(u.get("Z1").toString(), "a");
         assertEquals(u.get("Z2").toString(), "a");
         assertEquals(u.get("Z3").toString(), "a");
         assertEquals(u.get("Z4").toString(), "a");
     }
     
 
     public void testAnnotsUnify1() {
         Unifier u = new Unifier();
         Pred p1, p2;
         
         p1 = new Pred("pos");
         p2 = new Pred("pos");
 
         p1.addTerm(new Structure("1"));
         p2.addTerm(new Structure("1"));
         
         p2.addAnnot(new Structure("percept"));
         //System.out.println("p1="+p1+"; p2="+p2);
         assertTrue(u.unifies(p1, p2));
     }
     
     public void testAnnotsUnify2() {
         Unifier u = new Unifier();
         Pred p1, p2;
         
         p1 = new Pred("pos");
         p2 = new Pred("pos");
 
         p1.addTerm(new Structure("1"));
         p2.addTerm(new Structure("1"));
         
         p1.addAnnot(new VarTerm("X"));
         p2.addAnnot(new Structure("ag1"));
         
         // pos(1)[X]=pos(1)[ag1]
         assertTrue(u.unifies(p1, p2));
         
         assertEquals(u.get("X").toString(),"ag1");
         
         p1.addAnnot(new Structure("ag2"));
         p2.addAnnot(new VarTerm("Y"));
         u = new Unifier();
         // pos(1)[X,ag2] = pos(1)[ag1,Y]
         assertTrue(u.unifies(p1, p2));
         //System.out.println("u="+u);
         
         p1.addAnnot(new VarTerm("Z"));
         p2.addAnnot(new Structure("ag3"));
         p2.addAnnot(new Structure("ag4"));
         //System.out.println("p1="+p1+"; p2="+p2);
         u = new Unifier();
         assertTrue(u.unifies(p1, p2));
         //System.out.println("u="+u);
 
         p1.addAnnot(new VarTerm("X1"));
         p1.addAnnot(new VarTerm("X2"));
         p1.addAnnot(new VarTerm("X3"));
         //System.out.println("p1="+p1+"; p2="+p2);
         u = new Unifier();
         assertFalse(u.unifies(p1, p2));
         //System.out.println("u="+u);
 
         p1.clearAnnots();
         p1.addAnnot(new Structure("ag2"));
         p2.clearAnnots();
         p2.addAnnot(new Structure("ag1"));
         p2.addAnnot(new Structure("ag2"));
         p2.addAnnot(new Structure("ag3"));
         //System.out.println("p1="+p1+"; p2="+p2);
         u = new Unifier();
         assertTrue(u.unifies(p1, p2));
         //System.out.println("u="+u);
     }
     
     public void testAnnotsUnify3() {
         Literal l1 = Literal.parseLiteral("s(tuesday)");
         Unifier u = new Unifier();
         u.unifies(l1, Literal.parseLiteral("s(Day)"));
         assertEquals(u.get("Day").toString(),"tuesday");
         
         Literal l2 = Literal.parseLiteral("bel[monday]");
         Literal l3 = Literal.parseLiteral("bel[Day]");
         assertFalse(u.unifies(l3, l2));
         assertEquals(u.get("Day").toString(),"tuesday");
     }
     
     public void testAnnotsUnify4() {
         Literal l1 = Literal.parseLiteral("s[A]");
         Literal l2 = Literal.parseLiteral("s[3]");
         Unifier u = new Unifier();
         assertTrue(u.unifies(l1, l2));
         assertEquals(u.get("A").toString(),"3");
     }
 
     public void testAnnotsUnify5() {
         Literal l1 = Literal.parseLiteral("s[source(self)]");
         Literal l2 = Literal.parseLiteral("s");
         Unifier u = new Unifier();
         assertFalse(u.unifies(l1, l2));
         assertTrue(u.unifies(l2, l1));
     }
 
     public void testAnnotsUnify6() {
         Literal lp = Literal.parseLiteral("s(1)[b]");
         Literal ln = Literal.parseLiteral("~s(1)[b]");
         assertTrue(lp.isLiteral());
         assertTrue(ln.isLiteral());
         assertFalse(lp.negated());
         assertTrue(ln.negated());
         
         Unifier u = new Unifier();
 
         // Literal and literal
         assertFalse(u.unifies(lp, ln));
         
         // Literal and predicate
         Pred p = Pred.parsePred("s(1)[b]");
         assertTrue(p.isLiteral());
         assertTrue(u.unifies(lp, p));
         assertFalse(u.unifies((Term)ln, (Term)p));
         assertTrue(u.unifies(Literal.parseLiteral("s(1)"), p));
         assertFalse(u.unifies(p,Literal.parseLiteral("s(1)")));
         
         // Literal and structure
         Structure s = new Structure("s");
         s.addTerm(new NumberTermImpl(1));
         assertTrue(u.unifies(s,lp));
         assertFalse(u.unifies(lp,s));
         assertFalse(u.unifies(ln, s));
         assertFalse(u.unifies(s,ln));
         
         // Literal and Atom
         Atom a = new Atom("s");
         assertFalse(u.unifies(lp, a));
         assertFalse(u.unifies(ln, a));
         assertTrue(u.unifies(a, Literal.parseLiteral("s")));
         assertTrue(Literal.parseLiteral("s").equals(a));
         assertTrue(u.unifies(Literal.parseLiteral("s"), a));
         assertFalse(u.unifies(Literal.parseLiteral("~s"), a));
      
         // Predicate and structure
         assertTrue(u.unifies(s, p));
         assertFalse(u.unifies(p,s));
         
         // Predicate and atom
         assertFalse(u.unifies(a, p));
         assertFalse(u.unifies(p, a));
         assertTrue(u.unifies(Pred.parsePred("s"), a));
         assertFalse(u.unifies(Pred.parsePred("s[b]"), a));
         assertTrue(u.unifies(a,Pred.parsePred("s[b]")));
     }
 
     public void testAnnotsUnify7() throws ParseException {
         // p[a,b,c,d] = p[a,c|R] - ok and R=[b,d]
         Term t1 = parseTerm("p[a,b,c,d]");
         Term t2 = parseTerm("p[a,c|R]");
         Unifier u = new Unifier();
         assertTrue(u.unifies(t1, t2));
         assertEquals(u.get("R").toString(),"[b,d]");
         
         // p[a,c|R] = p[a,b,c,d] - ok and R=[b,d]
         u = new Unifier();
         assertTrue(u.unifies(t2, t1));
         assertEquals(u.get("R").toString(),"[b,d]");
 
         // p[H|R] = p[a,b,c,d] - ok and R=[b,c,d], H=a
         Term t3 = parseTerm("p[H|R]");
         u = new Unifier();
         assertTrue(u.unifies(t1, t3));
         assertEquals(u.get("H").toString(),"a");
         assertEquals(u.get("R").toString(),"[b,c,d]");
     }  
     
     public void testApplyAnnots() throws ParseException {
         Term t1 = parseTerm("p[a,X,c,d]");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new Atom("z"));
         t1.apply(u);
         assertEquals("p[a,z,c,d]",t1.toString());
         
         t1 = parseTerm("p[X,b,c,d]");
         t1.apply(u);
         assertEquals("p[z,b,c,d]",t1.toString());
 
         t1 = parseTerm("p[a,b,c,X]");
         t1.apply(u);
         assertEquals("p[a,b,c,z]",t1.toString());
     }
     
     public void testTrigger() {
         Pred p1 = new Pred("pos");
 
         p1.addTerm(new VarTerm("X"));
         p1.addTerm(new VarTerm("Y"));
     }
     
     public void testTriggetAnnot() throws ParseException {
         Literal content = Literal.parseLiteral("~alliance");
         content.addSource(new Structure("ag1"));
         Literal received = new LiteralImpl(Literal.LPos, new Pred("received"));
         received.addTerm(new Structure("ag1"));
         received.addTerm(new Structure("tell"));
         received.addTerm(content);
         received.addTerm(new Structure("id1"));
         
         Trigger t1 = new Trigger(TEOperator.add, TEType.belief, received);
 
         Literal received2 = new LiteralImpl(Literal.LPos, new Pred("received"));
         received2.addTerm(new VarTerm("S"));
         received2.addTerm(new Structure("tell"));
         received2.addTerm(new VarTerm("C"));
         received2.addTerm(new VarTerm("M"));
         
         Trigger t2 = new Trigger(TEOperator.add, TEType.belief, received2);
         
         //System.out.println("t1 = "+t1);
         //System.out.println("t2 = "+t2);
         Unifier u = new Unifier();
         assertTrue(u.unifies(t1,t2));
         //System.out.println(u);
         t2.apply(u);
         //System.out.println("t2 with apply = "+t2);
         
         assertEquals(t1.toString(), t2.toString());
         
         Trigger t3 = ASSyntax.parseTrigger("+!bid_normally(1)");
         Trigger t4 = ASSyntax.parseTrigger("+!bid_normally(N)");
         u = new Unifier();
         u.unifies(t3,t4);
         //System.out.println("u="+u);
         assertEquals(u.get("N").toString(), "1");
         
     }
     
     public void testLiteralUnify() {
         Literal content = Literal.parseLiteral("~alliance");
         content.addSource(new Structure("ag1"));
         Literal l1 = new LiteralImpl(Literal.LPos, new Pred("received"));
         l1.addTerm(new Structure("ag1"));
         l1.addTerm(new Structure("tell"));
         l1.addTerm(content);
         l1.addTerm(new Structure("id1"));
 
         
         Literal l2 = Literal.parseLiteral("received(S,tell,C,M)");
         Unifier u = new Unifier();
         assertTrue(u.unifies(l1,l2));
         //System.out.println(u);
         l2.apply(u);
         //System.out.println("l2 with apply = "+l2);
         assertEquals(l1.toString(), l2.toString());
         
         assertFalse(new Unifier().unifies(Literal.parseLiteral("c(x)"), Literal.parseLiteral("c(20)")));
         assertTrue(new Unifier().unifies(Literal.parseLiteral("c(20)"), Literal.parseLiteral("c(20)")));
         assertTrue(new Unifier().unifies(Literal.parseLiteral("c(X)"), Literal.parseLiteral("c(20)")));
         
         assertTrue(new Unifier().unifies(Literal.parseLiteral("c(t)"), Literal.parseLiteral("c(t)")));
         assertTrue(new Unifier().unifies(Literal.parseLiteral("~c(t)"), Literal.parseLiteral("~c(t)")));
         assertFalse(new Unifier().unifies(Literal.parseLiteral("c(t)"), Literal.parseLiteral("~c(t)")));
         assertFalse(new Unifier().unifies(Literal.parseLiteral("~c(t)"), Literal.parseLiteral("c(t)")));
     }
     
     public void testSubsetAnnot() {
         Pred p1 = Pred.parsePred("p1(t1,t2)[a1,a(2,3),a(3)]");
         Pred p2 = Pred.parsePred("p2(t1,t2)[a(2,3),a(3)]");
         assertTrue(p2.hasSubsetAnnot(p1));
         //assertTrue(p2.getSubsetAnnots(p1.getAnnots(), new Unifier(), null));
 
         assertFalse(p1.hasSubsetAnnot(p2));
         //assertFalse(p1.getSubsetAnnots(p2.getAnnots(), new Unifier(), null));
         
         Pred p3 = Pred.parsePred("p2(t1,t2)[a(A,_),a(X)]");
         Unifier u = new Unifier();
         assertTrue(p3.hasSubsetAnnot(p2,u));
         assertEquals(u.get("A").toString(),"2");
         assertEquals(u.get("X").toString(),"3");
         assertTrue(p3.hasSubsetAnnot(p1,u));
         
         Pred p4 = Pred.parsePred("p1(t1,t2)[a1|T]");
 
         //List<Unifier> r = new ArrayList<Unifier>();
         //assertTrue(p1.getSubsetAnnots(p4.getAnnots(),new Unifier(),r));
         //assertEquals(r.get(0).get("T").toString(), "[a(2,3),a(3)]");
         
         u = new Unifier();
         assertTrue(p1.hasSubsetAnnot(p4, u));
         assertEquals(u.get("T").toString(), "[a(2,3),a(3)]");
 
         Pred p5 = Pred.parsePred("p1(t1,t2)[a1|[a(2,3),a(3)]]");
         u = new Unifier();
         assertTrue(p1.hasSubsetAnnot(p5, u));
 
         Pred p6 = Pred.parsePred("p1(t1,t2)[a1|T]");
 
         //r.clear();
         //assertTrue(p6.getSubsetAnnots(p1.getAnnots(),new Unifier(),r));
         //System.out.println("p6="+p6+"; p1="+p1+" r="+r);
         //assertEquals(r.get(0).get("T").toString(), "[a(2,3),a(3)]");
 
         u = new Unifier();
         assertTrue(p6.hasSubsetAnnot(p1, u));
         assertEquals(u.get("T").toString(), "[a(2,3),a(3)]");
         assertTrue(p1.hasSubsetAnnot(p6, u));
     }
     
     public void testAnnotUnifAsList() {
         Pred p1 = Pred.parsePred("p[b(2),x]");
         Pred p2 = Pred.parsePred("p[a,b(2),c]");
         Unifier u = new Unifier();
         
         assertFalse(u.unifies(p1,p2));
         
         p1 = Pred.parsePred("p(t1,t2)[z,a(1),a(2,3),a(3)]");
         p2 = Pred.parsePred("p(t1,B)[a(X)|R]");
 
         assertTrue(u.unifies(p2,p1));
         assertEquals(u.get("R").toString(),"[z,a(2,3),a(3)]");
         
         u = new Unifier();
         assertTrue(u.unifies(p1,p2));
         //System.out.println(u+"-"+p2);
         p2.apply(u);
         assertEquals(p2.toString(),"p(t1,t2)[a(1),z,a(2,3),a(3)]");
     }
     
     public void testCompare() {
         Pred p1 = Pred.parsePred("a");
         Pred p2 = Pred.parsePred("b");
         
         assertEquals(p1.compareTo(p2), -1);
         assertEquals(p2.compareTo(p1), 1);
         assertEquals(p1.compareTo(p1), 0);
         
         p1 = Pred.parsePred("a(3)[3]");
         p2 = Pred.parsePred("a(3)[10]");
         Pred p3 = Pred.parsePred("a(3)[10]");
         assertEquals(1, p2.compareTo(p1));
         assertEquals(-1, p1.compareTo(p2));
         assertEquals(0, p2.compareTo(p3));
 
         Literal l1 = Literal.parseLiteral("~a(3)");
         Literal l2 = Literal.parseLiteral("a(3)");
         Literal l3 = Literal.parseLiteral("a(10)[5]");
         assertTrue(l1.compareTo(l2) == 1);
         assertTrue(l1.compareTo(l3) == 1);
         assertTrue(l2.compareTo(l3) == -1);
 
         assertTrue(l2.compareTo(new Atom("g")) < 0);
         assertTrue(new Atom("g").compareTo(l2) > 0);
         assertTrue(new Atom("g").compareTo(new Atom("g")) == 0);
 
         
         ListTerm l = ListTermImpl.parseList("[~a(3),a(3),a(10)[30],a(10)[5]]");
         Collections.sort(l);
         assertEquals(l.toString(), "[a(3),a(10)[5],a(10)[30],~a(3)]");
         
         ListTerm lt1 = ListTermImpl.parseList("[3,10]");
         ListTerm lt2 = ListTermImpl.parseList("[3,4]");
         ListTerm lt3 = ListTermImpl.parseList("[1,1,1]");
         assertTrue(lt1.compareTo(lt2) > 0);
         assertTrue(lt1.compareTo(lt3) < 0);
 
         assertTrue(lt1.compareTo(p1) > 0);
         
         l = ListTermImpl.parseList("[b,[1,1,1],c,10,g,casa,f(10),5,[3,10],f(4),[3,4]]");
         Collections.sort(l);
         assertEquals("[5,10,b,c,casa,f(4),f(10),g,[3,4],[3,10],[1,1,1]]",l.toString());
     }
     
     public void testUnify4() throws ParseException {
         Term a1 = ASSyntax.parseTerm("a(1)");
         Term a2 = ASSyntax.parseTerm("a(X+1)");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"),new NumberTermImpl(0));
         assertFalse(a1.equals(a2));   
     }
 
     public void testUnify5() {
         Structure s1 = Structure.parse("a(X,Y,10)");
         Structure s2 = Structure.parse("a(1,2,1)");
         Unifier u = new Unifier();
         assertFalse(u.unifies(s1,s2));
         assertEquals(u.size(),0);
     }
 
     public void testMakeVarAnnon1() {
         Literal l1 = Literal.parseLiteral("likes(jane,X,peter)");
         Literal l2 = Literal.parseLiteral("likes(X,Y,Y)");
         Literal l3 = Literal.parseLiteral("likes(X,Y,X)");
         Literal l4 = Literal.parseLiteral("likes(Z,Y,Y)");
         Unifier u = new Unifier();                
         assertFalse(u.unifies(l1, l2));
         u.clear();      
         assertFalse(u.unifies(l1, l3));
         u.clear();      
         assertTrue(u.unifies(l1, l4));
         
         l2.makeVarsAnnon();
         u.clear();      
         assertTrue(u.unifies(l1, l2));      
 
         l3.makeVarsAnnon();
         u.clear();      
         assertFalse(u.unifies(l1, l3));     
 
         l4.makeVarsAnnon();
         u.clear();      
         assertTrue(u.unifies(l1, l4));      
     }
     
     public void testMakeVarAnnon2() {
         Literal l1 = Literal.parseLiteral("calc(AgY,QuadY2,QuadY2)");
         Literal l2 = Literal.parseLiteral("calc(32,33,V)");
         Unifier u = new Unifier();
         assertTrue(u.unifies(l1, l2));
         l2.makeVarsAnnon();
         u.clear();
         assertTrue(u.unifies(l1, l2));
         l2.apply(u);
         assertEquals("calc(32,33,33)", l2.toString());
         l1.apply(u);
         assertEquals("calc(32,33,33)", l1.toString());
     }
 
     public void testMakeVarAnnon3() {
         Literal l1 = Literal.parseLiteral("calc(AgY,X)[vl(X),source(AgY),bla(Y),X]");
         l1.makeVarsAnnon();
         Map<VarTerm, Integer> v = new HashMap<VarTerm, Integer>();
         l1.countVars(v);
         assertEquals(3, v.size());
         assertEquals("vl("+l1.getTerm(1)+")",l1.getAnnots("vl").get(0).toString());
     }
     
     public void testMakeVarAnnon4() {
         Literal l = Literal.parseLiteral("p(X)");
         Unifier u = new Unifier();
         u.unifies(new UnnamedVar(4), new VarTerm("X"));
         u.unifies(new VarTerm("X"), new UnnamedVar(2));
         u.unifies(new UnnamedVar(2), new VarTerm("Y"));
         u.unifies(new UnnamedVar(10), new VarTerm("Y"));
         u.unifies(new VarTerm("X"), new VarTerm("Z"));
         /*
         Iterator<VarTerm> i = u.binds(new VarTerm("X"));
         while (i.hasNext()) {
             System.out.println(i.next());
         }
         */
         l.makeVarsAnnon(u);
         //System.out.println(u+ " "+l);
         assertEquals("p(_2)", l.toString());
     }
 
     public void testMakeVarAnnon5() {
         Literal l = Literal.parseLiteral("p(X,Y)[s(Y)]");
         Unifier u = new Unifier();
         u.unifies(new VarTerm("X"), new VarTerm("Y"));
         l.makeVarsAnnon(u);
         assertEquals(l.getTerm(0), l.getTerm(1));
         assertEquals("[s("+l.getTerm(0)+")]", l.getAnnots().toString());
     }
     
     public void testAddAnnots() {
         Literal p1 = Literal.parseLiteral("p1");
         Literal p2 = Literal.parseLiteral("p2[a1,a2]");
         Literal p3 = Literal.parseLiteral("p3[a2,a3,a4,a5,a6,a7,a8]");
         
         p1.addAnnots(Literal.parseLiteral("p").getAnnots());
         assertFalse(p1.hasAnnot());
         
         p1.addAnnots(p2.getAnnots());
         assertEquals(p1.getAnnots(),p2.getAnnots());
         
         p1.addAnnots(p3.getAnnots());
         assertEquals(8,p1.getAnnots().size());
     }
     
     public void testGetSources() {
         Literal p1 = Literal.parseLiteral("p1");
         assertEquals(0, p1.getSources().size());
         
         assertEquals(1, Literal.parseLiteral("p2[source(a)]").getSources().size());
 
         Literal p2 = Literal.parseLiteral("p2[a1,source(ag1),a2,source(ag2),source(ag3)]");
         assertEquals(3, p2.getSources().size());
         
         assertEquals("[ag1,ag2,ag3]",p2.getSources().toString());   
     }
     
     public void testImportAnnots() {
         Literal p1 = Literal.parseLiteral("p1");
         Literal p2 = Literal.parseLiteral("p2[a1,a2]");
         Literal p3 = Literal.parseLiteral("p3[a2,a3,a4,a5,a6,a7,a8]");
         
         assertTrue(p1.importAnnots(p2));
         assertEquals(2,p1.getAnnots().size());
         assertEquals(2,p2.getAnnots().size());
 
         assertFalse(p1.importAnnots(p2));
         assertEquals(2,p1.getAnnots().size());
         assertEquals(0,p2.getAnnots().size());
         
         assertTrue(p1.importAnnots(p3));
         assertEquals(8,p1.getAnnots().size());
         assertEquals(6,p3.getAnnots().size());
 
         assertFalse(p1.importAnnots(p3));
         assertFalse(p1.importAnnots(p2));
         
         assertTrue(p2.importAnnots(p1));
 
         assertEquals(8,p2.getAnnots().size());
     }
     
     public void testGetTermsArray() {
         Structure s2 = createStructure("a");
         s2.addTerms(createNumber(1), createNumber(2), createNumber(3));
         Term[] a = s2.getTermsArray();
         assertEquals(3,a.length);
         assertEquals("1",a[0].toString());
         assertEquals("3",a[2].toString());
     }
     
     public void testIALiteral() throws ParseException {
         Literal l = parseLiteral(".print(a)");
         assertTrue(l.isInternalAction());
         
         l = Literal.parseLiteral("print(a)");
         assertFalse(l.isInternalAction());
 
         l = Literal.parseLiteral("p.rint(a)");
         assertTrue(l.isInternalAction());
     }
     
     
     public void testCloneStructureFromAtom() {
         Structure s = new Structure(new Atom("b"));
         assertFalse(s.isArithExpr());
         assertEquals(0,s.getArity());
     }
 
     public void testHasVar() throws ParseException {
         Literal l = parseLiteral("a(Test,X,Y,b(g([V1,X,V2,V1]),c))[b,source(Y),B,kk(_),oo(oo(OO))]");
         assertTrue(l.hasVar(new VarTerm("X")));
         assertTrue(l.hasVar(new VarTerm("V2")));
         assertTrue(l.hasVar(new VarTerm("OO")));
         assertFalse(l.hasVar(new VarTerm("O")));
     }
     
     public void testSingletonVars() {
         Literal l = Literal.parseLiteral("a(10)");
         assertEquals(0, l.getSingletonVars().size());
         
         l = Literal.parseLiteral("a(X)");
         assertEquals(1, l.getSingletonVars().size());
 
         l = Literal.parseLiteral("a(X,X)");
         assertEquals(0, l.getSingletonVars().size());
 
         l = Literal.parseLiteral("a(_X,_Y,_X)");
         assertEquals(0, l.getSingletonVars().size());
 
         l = Literal.parseLiteral("a(X,Y,b(g(X),c))");
         assertEquals(1, l.getSingletonVars().size());
         assertEquals("Y", l.getSingletonVars().get(0).toString());
 
         l = Literal.parseLiteral("a(X,Y,b(g(X),c))[b,source(Y)]");
         assertEquals(0, l.getSingletonVars().size());
         
         l = Literal.parseLiteral("a(Test,X,Y,b(g(X),c))[b,source(Y),B,kk(U)]");
         assertEquals(3, l.getSingletonVars().size());
         
         l = Literal.parseLiteral("a(Test,X,Y,b(g([V1,X,V2,V1]),c))[b,source(Y),B,kk(_)]");
         assertEquals(3, l.getSingletonVars().size());
 
         Plan p = Plan.parse("+e(X) : X > 10 <- .print(ok).");
         assertEquals(0, p.getSingletonVars().size());
         
         p = Plan.parse("+e(x) : X > 10 <- .print(ok).");
         assertEquals(1, p.getSingletonVars().size());
         assertEquals("X", p.getSingletonVars().get(0).toString());
 
         p = Plan.parse("+e(x) : a(X) & X > 10 <- .print(ok).");
         assertEquals(0, p.getSingletonVars().size());
         
         p = Plan.parse("+e(x) : a(X) & X > 10 <- .print(W).");
         assertEquals(1, p.getSingletonVars().size());
         
         p = Plan.parse("+e(x) : a(X) & X > 10 <- .print(_).");
         assertEquals(0, p.getSingletonVars().size());
     }   
     
     
     public void testAtomParsing() throws ParseException {
         Literal l = parseLiteral("b");
         assertTrue(l instanceof Literal);
         assertTrue(l.isAtom());
 
         // if is atom, can be cast to Atom
         @SuppressWarnings("unused")
         Atom x = (Atom)l;
         
         l = parseLiteral("b(10,a,c(10,x))[ant1,source(c)]");
         assertTrue(l.getTerm(1) instanceof Atom);
         assertFalse(l.getTerm(2).isAtom());
         assertTrue(l.getAnnots().get(0) instanceof Atom);
         
         l =  parseLiteral("b(a.r)"); // internal actions should not be atoms
         assertFalse(l.getTerm(0).isAtom());
     }
 }
