 package osmo.tester.generator.testsuite;
 
 import osmo.tester.model.FSMTransition;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Describes the test suite being generated.
  *
  * @author Teemu Kanstren
  */
 public class TestSuite {
   /** The current test being generated. */
   private TestCase current = null;
   /** The test cases generated so far, excluding the current test case. */
   private List<TestCase> history = new ArrayList<TestCase>();
 
   /**
    * Start a new test case.
    */
   public void startTest() {
     current = new TestCase();
   }
 
   /**
    * End the current test case and moves it to the suite "history".
    */
   public void endTest() {
     //TODO: check that current is non-null, add tests
     history.add(current);
     current = null;
   }
 
   //TODO: add checks that current is != null and test cases for adding new ones before starting them (after ending previous one)
 
   /**
    * Adds the given transition as a step into the current test case.
    *
    * @param transition The transition to add.
    */
   public void add(FSMTransition transition) {
     current.addTransition(transition);
   }
 
   /**
    * Marks the given requirement as covered by the current test case.
    *
    * @param requirement The requirement identifier.
    */
   public void covered(String requirement) {
     current.covered(requirement);
   }
 
   /**
    * Counts the total number of tests in the test suite and the test case currently being generated.
    *
    * @return The total number of test steps in test suite.
    */
   public int totalSteps() {
     int count = 0;
     for (TestCase test : history) {
       count += test.getSteps().size();
     }
     //current is null when suite is initialized but no tests are started
     if (current != null) {
       count += current.getSteps().size();
     }
     return count;
   }
 
   public TestCase getCurrent() {
     return current;
   }
 
   public List<TestCase> getHistory() {
     return history;
   }
 
   /**
    * Gives the number of test steps in the current test case.
    *
    * @return The number of test steps in the current test case.
    */
   public int currentSteps() {
    //current is null before starting the first test case
    if (current == null) {
      return 0;
    }
     return current.getSteps().size();
   }
 }
