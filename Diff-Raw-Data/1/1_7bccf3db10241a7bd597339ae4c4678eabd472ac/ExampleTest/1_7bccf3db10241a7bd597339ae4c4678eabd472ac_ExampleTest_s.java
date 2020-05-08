 package uk.co.mtford.jalp.abduction.logic.instance;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import uk.co.mtford.jalp.JALPException;
 import uk.co.mtford.jalp.JALPSystem;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.List;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 27/05/2012
  * Time: 08:12
  * To change this template use File | Settings | File Templates.
  */
 public class ExampleTest {
 
     JALPSystem system;
 
     public ExampleTest() {
 
     }
 
     @Before
     public void noSetup() {
 
     }
 
     @After
     public void noTearDown() {
 
     }
 
     @Test
     public void factTest1() throws FileNotFoundException, ParseException, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         system = new JALPSystem("examples/fact-example-a.alp");
         List<IInferableInstance> query = new LinkedList<IInferableInstance>();
         VariableInstance X = new VariableInstance("X");
         VariableInstance Y = new VariableInstance("Y");
         PredicateInstance likes = new PredicateInstance("likes",X,Y);
         query.add(likes);
         List<Result> result = system.processQuery(query, JALPSystem.Heuristic.NONE);
         assertTrue(result.size()==1);
         Result onlyResult = result.remove(0);
         assertTrue(onlyResult.getAssignments().get(Y).equals(new ConstantInstance("jane")));
         assertTrue(onlyResult.getAssignments().get(X).equals(new ConstantInstance("john")));
     }
 
 
 
 
 
 }
