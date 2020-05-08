 package uk.co.mtford.jalp;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.IInferableInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IUnifiableAtomInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.PredicateInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.InIntegerListConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.NegativeConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.CharConstantInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.IntegerConstantInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.IntegerConstantListInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 02/06/2012
  * Time: 06:36
  * To change this template use File | Settings | File Templates.
  */
 public class ConstraintTest {
 
     JALPSystem system;
 
     public ConstraintTest() {
 
     }
 
     @Before
     public void noSetup() {
 
     }
 
     @After
     public void noTearDown() {
 
     }
 
     /*
     q(Y) :- Y in [john,bob,mary].
     Q  = q(X)
 
     We expect three results i.e. an assignment to X of one of 3 in the list.
      */
     @Test
     public void constantListTest1() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/constant-list.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance("X");
         PredicateInstance q = new PredicateInstance("q",X);
         query.add(q);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/constant-list-1");
         assertTrue(result.size()==3);
         Result resultOne = result.get(0);
         Result resultTwo = result.get(1);
         Result resultThree = result.get(2);
         JALP.reduceResult(resultOne);
         JALP.reduceResult(resultTwo);
         JALP.reduceResult(resultThree);
         assertTrue(resultOne.getAssignments().get(X).equals(new CharConstantInstance("mary")));
         assertTrue(resultTwo.getAssignments().get(X).equals(new CharConstantInstance("bob")));
         assertTrue(resultThree.getAssignments().get(X).equals(new CharConstantInstance("john")));
     }
 
     /*
    q(Y) :- Y in [john,bob,mary].
    Q  = q(mike)
 
    We expect to fail i.e. 'no'
     */
     @Test
     public void constantListTest2() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/constant-list.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         CharConstantInstance mike = new CharConstantInstance("mike");
         PredicateInstance q = new PredicateInstance("q",mike);
         query.add(q);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/constant-list-2");
         assertTrue(result.size()==0);
     }
 
     /*
    q(Y) :- Y in [john,bob,mary].
    Q  = q(bob)
 
    We expect one result i.e. 'yes'
     */
     @Test
     public void constantListTest3() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/constant-list.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         CharConstantInstance bob = new CharConstantInstance("bob");
         PredicateInstance q = new PredicateInstance("q",bob);
         query.add(q);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/constant-list-3");
         assertTrue(result.size()==1);
     }
 
     /*
     q(X) :- X in [1,2,3].
     Q = q(3)
 
     We expect 'yes'
      */
     @Test
     public void integerListTest1() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/integer-list.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         IntegerConstantInstance three = new IntegerConstantInstance(3);
         PredicateInstance q = new PredicateInstance("q",three);
         query.add(q);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/integer-list-1");
         assertTrue(result.size()==1);
     }
 
 
     /*
    q(X) :- X in [1,2,3].
    Q = q(5)
 
    We expect 'no'
     */
     @Test
     public void integerListTest2() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/integer-list.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         IntegerConstantInstance three = new IntegerConstantInstance(5);
         PredicateInstance q = new PredicateInstance("q",three);
         query.add(q);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/integer-list-2");
         assertTrue(result.size()==0);
     }
 
 
     /*
    q(X) :- X in [1,2,3].
    Q = q(X)
 
    We expect 3 results.
     */
     @Test
     public void integerListTest3() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/integer-list.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance("X");
         PredicateInstance q = new PredicateInstance("q",X);
         query.add(q);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/integer-list-3");
         assertTrue(result.size()==3);
     }
 
     /*
     p(X) :- X in [1,2,3], Y in [1,2], X<Y.
     Q = p(X)
 
     We expect one result: X/1
      */
     @Test
     public void lessThanTest1() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/less-than.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance("X");
         PredicateInstance p = new PredicateInstance("p",X);
         query.add(p);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/less-than-test-1");
         assertTrue(result.size()==1);
         Result resultOne = result.remove(0);
         JALP.reduceResult(resultOne);
         assertTrue(resultOne.getAssignments().get(X).equals(new IntegerConstantInstance(1)));
     }
 
     /*
    p(X) :- X in [1,2,3], Y in [1,2], X<Y.
    Q = p(1)
 
    We expect one result: yes
     */
     @Test
     public void lessThanTest2() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/less-than.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         PredicateInstance p = new PredicateInstance("p",new IntegerConstantInstance(1));
         query.add(p);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/less-than-test-2");
         assertTrue(result.size()==1);
     }
 
     /*
    p(X) :- X in [1,2,3], Y in [1,2], X<Y.
    Q = p(2)
 
    We expect one result no
     */
     @Test
     public void lessThanTest3() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/less-than.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         PredicateInstance p = new PredicateInstance("p",new IntegerConstantInstance(2));
         query.add(p);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/less-than-test-3");
         assertTrue(result.size()==0);
     }
 
     /*
    p(X) :- X in [1,2,3], Y in [1,2], X<Y.
    Q = p(3)
 
    We expect result no
     */
     @Test
     public void lessThanTest4() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/less-than.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         PredicateInstance p = new PredicateInstance("p",new IntegerConstantInstance(3));
         query.add(p);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/less-than-test-4");
         assertTrue(result.size()==0);
     }
 
     /*
    p(X) :- X in [1,2,3], Y in [1,2], X<Y.
    Q = p(4)
 
    We expect result no
     */
     @Test
     public void lessThanTest5() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/constraint/less-than.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         PredicateInstance p = new PredicateInstance("p",new IntegerConstantInstance(4));
         query.add(p);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/less-than-test-5");
         assertTrue(result.size()==0);
     }
 
     /*
     p(X) :- a(X), not q(X).
     q(X) :- X in [1].
 
     abducible(a(X)).
 
     We expect to collect a FD not X in [1].
     */
     @Test
     public void ungroundAbducible() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
        system = new JALPSystem("examples/abducible/unground-abducible.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance("X");
         PredicateInstance a = new PredicateInstance("p",X);
         query.add(a);
         List<Result> result = system.generateDebugFiles(query, "debug/constraint/unground-abducible");
         assertTrue(result.size()==1);
         Result resultOne = result.get(0);
         JALP.reduceResult(resultOne);
         assertTrue(resultOne.getStore().abducibles.size()==1);
         IUnifiableAtomInstance XAssignment = resultOne.getAssignments().get(X);
         assertTrue(resultOne.getStore().abducibles.get(0).equals(new PredicateInstance("a",XAssignment)));
         IntegerConstantListInstance list = new IntegerConstantListInstance();
         list.getList().add(new IntegerConstantInstance(1));
         assertTrue(resultOne.getStore().constraints.contains(new NegativeConstraintInstance(new InIntegerListConstraintInstance(X, list))));
     }
 
 }
