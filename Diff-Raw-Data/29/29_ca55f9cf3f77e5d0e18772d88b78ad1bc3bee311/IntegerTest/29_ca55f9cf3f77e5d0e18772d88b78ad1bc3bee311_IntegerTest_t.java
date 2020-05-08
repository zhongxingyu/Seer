 package alice.tuprolog;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class IntegerTest {
 
     Prolog engine;
 
     @Before
     public void setUp() {
         engine = new Prolog();
     }
     
     @Test public void test0() throws PrologException {
         SolveInfo solution = engine.solve("integer(3).");
         assertTrue(solution.isSuccess());
     }
 
     @Test public void test1() throws PrologException {
         SolveInfo solution = engine.solve("integer(-3).");
         assertTrue(solution.isSuccess());
     }
 
     @Test public void test2() throws PrologException {
         SolveInfo solution = engine.solve("integer(3.3).");
         assertFalse(solution.isSuccess());
     }
 
     @Test public void test3() throws PrologException {
         SolveInfo solution = engine.solve("integer(X).");
         assertFalse(solution.isSuccess());
     }
 
     @Test public void test4() throws PrologException {
         SolveInfo solution = engine.solve("integer(atom).");
         assertFalse(solution.isSuccess());
     }
 
 }
