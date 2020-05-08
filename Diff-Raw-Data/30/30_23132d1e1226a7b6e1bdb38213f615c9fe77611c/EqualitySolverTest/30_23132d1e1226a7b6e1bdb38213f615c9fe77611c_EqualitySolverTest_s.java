 package uk.co.mtford.jalp.abduction.logic.instance;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.Store;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.InEqualityInstance;
 import uk.co.mtford.jalp.abduction.rules.LeafRuleNode;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 23/05/2012
  * Time: 10:21
  * To change this template use File | Settings | File Templates.
  */
 public class EqualitySolverTest {
     VariableInstance X, B, Y;
     ConstantInstance john, bob;
     EqualityInstance E1, E2, E3;
     InEqualityInstance IE1;
 
     public EqualitySolverTest() {
 
     }
 
     @Before
     public void noSetup() {
         HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         X = new VariableInstance("X");
         Y = new VariableInstance("Y");
         B = new VariableInstance("B");
         john = new ConstantInstance("john");
         bob = new ConstantInstance("bob");
         E1 = new EqualityInstance(john, X);
         E2 = new EqualityInstance(B,Y);
         E3 = new EqualityInstance(X,B);
 
     }
 
     @After
     public void noTearDown() {
 
     }
 
     @Test
     public void simpleEqualityTest1() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,bob);
         Store store = new Store();
         store.equalities.add(E2);
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments!=null);
         assertTrue(newAssignments.size()==2);
         assertTrue(newAssignments.get(Y)==bob);
         assertTrue(newAssignments.get(B)==bob);
     }
 
     @Test
     public void simpleEqualityTest2() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,bob);
         Store store = new Store();
         store.equalities.add(new EqualityInstance(Y,bob));
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments!=null);
         assertTrue(newAssignments.size()==1);
         assertTrue(newAssignments.get(Y)==bob);
     }
 
     // theta = {Y/bob}, equalities = {john=X, B=Y}
     @Test
     public void simpleEqualityTest3() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,bob);
         Store store = new Store();
         store.equalities.add(E1);
         store.equalities.add(E2);
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments!=null);
         assertTrue(newAssignments.get(Y)==bob);
         assertTrue(newAssignments.get(X)==john);
         assertTrue(newAssignments.get(B)==bob);
     }
 
     // theta = {Y/bob}, equalities = {john=X, B=Y, X=B}
     @Test
     public void simpleEqualityTest4() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,bob);
         Store store = new Store();
         store.equalities.add(E1);
         store.equalities.add(E2);
         store.equalities.add(E3);
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments==null);
     }
 
     @Test
     public void simpleInEqualityTest1() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,bob);
         Store store = new Store();
         store.equalities.add(new InEqualityInstance(new EqualityInstance(Y, bob)));
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments==null);
     }
 
     @Test
     public void simpleInEqualityTest2() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,bob);
         Store store = new Store();
         store.equalities.add(new EqualityInstance(B, Y));
         store.equalities.add(new InEqualityInstance(new EqualityInstance(B, bob)));
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments==null);
     }
 
     @Test
     public void simpleInEqualityTest3() {
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(Y,john);
         Store store = new Store();
         store.equalities.add(new EqualityInstance(B,Y));
         store.equalities.add(new InEqualityInstance(new EqualityInstance(B, bob)));
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
         assertTrue(newAssignments!=null);
         assertTrue(newAssignments.size()==2);
         assertTrue(newAssignments.get(Y)==john);
         assertTrue(newAssignments.get(B)==john);
     }
 
     @Test
     public void complexTest1() {
         ConstantInstance sam = new ConstantInstance("sam");
         ConstantInstance tweety = new ConstantInstance("tweety");
         VariableInstance X6 = new VariableInstance("X");
         VariableInstance X7 = new VariableInstance("X");
         VariableInstance X9 = new VariableInstance("X");
         VariableInstance X10 = new VariableInstance("X");
         EqualityInstance E1 = new EqualityInstance(X6,X7);
         EqualityInstance E2 = new EqualityInstance(X7,X9);
         EqualityInstance E3 = new EqualityInstance(X9,sam);
         InEqualityInstance IE = new InEqualityInstance(new EqualityInstance(X7,tweety));
 
         HashMap<VariableInstance,IUnifiableAtomInstance> assignments;
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         assignments.put(X10,X7);
 
         Store store = new Store();
         store.equalities.add(E1);
         store.equalities.add(E2);
         store.equalities.add(E3);
         store.equalities.add(IE);
 
         LeafRuleNode node = new LeafRuleNode(new AbductiveFramework(),store,assignments);
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = node.equalitySolve();
 
         assertTrue(newAssignments!=null);
         assertTrue(newAssignments.size()==4);
         assertTrue(newAssignments.get(X10)==X7);
         assertTrue(newAssignments.get(X6)==X7);
         assertTrue(newAssignments.get(X7)==X9);
         assertTrue(newAssignments.get(X9)==sam);
     }
 
 
 
 }
