 /**
  * Simple Test runner using the JUnitHelper.  Runs a single test.
  * <p>
  * What you get from this method is the tests run will match the method names.
  * In this case, runSingleTest() will be the one flagged with exceptions or
  * pass/fail metrics, but will <b>not</b> specify what tests in FitNesse run,
  * as the method could have called numerous tests.
  * <p>
  * See ExampleTestRunnerTest for a more integrated solution.
  */
 
 
 
 package klauer.fitnessemvn;
 
 import fitnesse.junit.JUnitHelper;
 
 import java.io.File;
 import org.junit.Before;
 import org.junit.Test;
 
 public class JUnitHelperTrinidadTest {
 
   JUnitHelper helper;
 
   @Before
   public void initHelper() throws Exception {
    helper = new JUnitHelper("src/main/fitnesse", "target");
   }
 
  /**
   * Produces an exception, since one of the two asserts failed.  It doesn't
   * provide any sort of detail on what either assert does, it merely states
   * that it was expecting 0 errors, but found something more than that.
   * @throws Exception
   */
   @Test
  public void runSomeTests() throws Exception {
     helper.assertTestPasses("FitNesse.UserGuide.TwoMinuteExample");
     helper.assertSuitePasses("FrontPage.SecondTestSuite");
   }
 
 }
