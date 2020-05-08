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
    helper = new JUnitHelper(".", "target");
   }
 
   @Test
  public void runSingleTest() throws Exception {
     helper.assertTestPasses("FitNesse.UserGuide.TwoMinuteExample");
     helper.assertSuitePasses("FrontPage.SecondTestSuite");
   }
 
 }
