 package uk.co.mtford.jalp.abduction.logic.instance;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 
 import java.util.HashMap;
 
 import static org.junit.Assert.assertTrue;
 
 public class SubstitutionTest {
 
     VariableInstance x, y, z;
     VariableInstance q, r, s;
     ConstantInstance c, d, e;
     FalseInstance f;
     TrueInstance t;
 
     public SubstitutionTest() {
 
     }
 
     @Before
     public void noSetup() {
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         x = new VariableInstance("X");
         y = new VariableInstance("Y");
         z = new VariableInstance("Z");
         q = new VariableInstance("q");
         r = new VariableInstance("r");
         s = new VariableInstance("s");
         c = new ConstantInstance("c");
         d = new ConstantInstance("d");
         e = new ConstantInstance("e");
         f = new FalseInstance();
         t = new TrueInstance();
     }
 
     @After
     public void noTearDown() {
 
     }
 
     @Test
     public void constantTest() {
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         assertTrue(c == c.performSubstitutions(subst));
     }
 
     @Test
     public void variableTest() {
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         assertTrue(y.performSubstitutions(subst) == y);
         assertTrue(x.performSubstitutions(subst) == d);
     }
 
     @Test
     public void falseTest() {
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         assertTrue(f == f.performSubstitutions(subst));
     }
 
     @Test
     public void trueTest() {
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         assertTrue(t == t.performSubstitutions(subst));
     }
 
     @Test
     public void denialTest1() {
         PredicateInstance p = new PredicateInstance("p", x, y);
         PredicateInstance q = new PredicateInstance("q", z);
         DenialInstance denial = new DenialInstance(p, q);
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         assertTrue(denial.performSubstitutions(subst) == denial);
         assertTrue(denial.getBody().size() == 2);
         assertTrue(denial.getBody().get(0) != p);
         assertTrue(denial.getBody().get(1) != q);
        assertTrue(denial.getBody().equals(p));
        assertTrue(denial.getBody().equals(q));
         assertTrue(((PredicateInstance) denial.getBody().get(0)).getParameter(0) == d);  // p(d,Y)
         assertTrue(((PredicateInstance) denial.getBody().get(0)).getParameter(1) == y);  // p(d,Y)
         assertTrue(((PredicateInstance) denial.getBody().get(1)).getParameter(0) == z);  // p(Z)
     }
 
     @Test
     public void negationTest1() {
         PredicateInstance p = new PredicateInstance("p", x, y);
         PredicateInstance q = new PredicateInstance("q", z);
         DenialInstance denial = new DenialInstance(p, q);
         NegationInstance neg = new NegationInstance(denial);
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         assertTrue(neg.performSubstitutions(subst) == neg);
         assertTrue(neg.getSubFormula() == denial);
         assertTrue(neg.getSubFormula().equals(denial));
 
         assertTrue(denial.getBody().size() == 2);
        assertTrue(denial.getBody().get(0) == p);
        assertTrue(denial.getBody().get(1) == q);
         assertTrue(((PredicateInstance) denial.getBody().get(0)).getParameter(0) == d);  // p(d,Y)
         assertTrue(((PredicateInstance) denial.getBody().get(0)).getParameter(1) == y);  // p(d,Y)
         assertTrue(((PredicateInstance) denial.getBody().get(1)).getParameter(0) == z);  // p(Z)
 
     }
 
     @Test
     public void equalityTest1() {
         PredicateInstance p = new PredicateInstance("p", x, y);
         PredicateInstance q = new PredicateInstance("p", z, r);
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         subst.put(x, d);
         EqualityInstance e = new EqualityInstance(p, q);
         assert (e.performSubstitutions(subst) == e);
         assertTrue(((PredicateInstance) e.getLeft()).getParameter(0) == d);  // p(d,Y)
         assertTrue(((PredicateInstance) e.getLeft()).getParameter(1) == y);  // p(d,Y)
         assertTrue(((PredicateInstance) e.getRight()).getParameter(0) == z);  // p(Z)
 
     }
 
 
 }
