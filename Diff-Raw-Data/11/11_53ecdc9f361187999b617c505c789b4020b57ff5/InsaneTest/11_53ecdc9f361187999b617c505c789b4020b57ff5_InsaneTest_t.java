 package uk.co.mtford.jalp;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.IInferableInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.PredicateInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.CharConstantInstance;
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
  * Time: 08:03
  * To change this template use File | Settings | File Templates.
  */
 public class InsaneTest {
 
     JALPSystem system;
 
     public InsaneTest() {
 
     }
 
     @Before
     public void noSetup() {
 
     }
 
     @After
     public void noTearDown() {
 
     }
 
     /*
     boy(bob).
     boy(peter).
     boy(max).
 
     girl(jane).
     girl(maria).
 
     insane(maria).
 
     likes(X,Y) :- boy(X), girl(Y), not insane(Y).
     likes(X,Y) :- girl(X), boy(Y).
 
     Q = likes(X,Y)
 
     We expect 9 results. All the girls like all the guys, but the guys only like jane as maria is insane.
      */
     @Test
     public void insaneTest1() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/insane/insane.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance(("X"));
         VariableInstance Y = new VariableInstance(("Y"));
         PredicateInstance likes = new PredicateInstance("likes",X,Y);
         query.add(likes);
         List<Result> result = system.generateDebugFiles(query, "debug/insane/insane-1");
         assertTrue(result.size()==9);
     }
 
     /*
     boy(bob).
     boy(peter).
     boy(max).
 
     girl(jane).
     girl(maria).
 
     insane(maria).
 
     likes(X,Y) :- boy(X), girl(Y), not insane(Y).
     likes(X,Y) :- girl(X), boy(Y).
 
    Q = likes(bob,Y)
 
    We expect one result where Y is assigned to jane, as maria is insane.
     */
     @Test
     public void insaneTest2() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/insane/insane.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
        CharConstantInstance bob = new CharConstantInstance("bob");
         VariableInstance Y = new VariableInstance(("Y"));
        PredicateInstance likes = new PredicateInstance("likes",bob,Y);
         query.add(likes);
         List<Result> result = system.generateDebugFiles(query, "debug/insane/insane-2");
         assertTrue(result.size()==1);
         Result resultOne = result.remove(0);
         JALP.reduceResult(resultOne);
         assertTrue(resultOne.getAssignments().get(Y).equals(new CharConstantInstance("jane")));
     }
 
     /*
    boy(X) :- X in [bob,peter,max].
    girl(X) :- X in [jane,maria].
 
    insane(X) :- X in [maria].
 
    likes(X,Y) :- boy(X), girl(Y), not insane(Y).
    likes(X,Y) :- girl(X), boy(Y).
 
    Q = likes(X,Y)
 
    We expect nine results. The girls like all the guys, but the guys only like jane.
     */
     @Test
     public void insaneConstraintTest1() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/insane/insane-constraint.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance(("X"));
         VariableInstance Y = new VariableInstance(("Y"));
         PredicateInstance likes = new PredicateInstance("likes",X,Y);
         query.add(likes);
         List<Result> result = system.generateDebugFiles(query, "debug/insane/insane-constraint-1");
         assertTrue(result.size()==9);
     }
 
     /*
    boy(X) :- X in [bob,peter,max].
    girl(X) :- X in [jane,maria].
 
    insane(X) :- X in [maria].
 
    likes(X,Y) :- boy(X), girl(Y), not insane(Y).
    likes(X,Y) :- girl(X), boy(Y).
 
    Q = likes(bob,Y)
 
    We expect one result where Y is assigned to jane, as maria is insane.
     */
     @Test
     public void insaneConstraintTest2() throws IOException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/insane/insane-constraint.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
        CharConstantInstance bob = new CharConstantInstance("bob");
         VariableInstance Y = new VariableInstance(("Y"));
        PredicateInstance likes = new PredicateInstance("likes",bob,Y);
         query.add(likes);
         List<Result> result = system.generateDebugFiles(query, "debug/insane/insane-constraint-2");
         assertTrue(result.size()==1);
         Result resultOne = result.remove(0);
         JALP.reduceResult(resultOne);
         assertTrue(resultOne.getAssignments().get(Y).equals(new CharConstantInstance("jane")));
     }
 
 }
