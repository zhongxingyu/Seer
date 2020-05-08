 /**
  * An example test runner to run these sets of tests as JUnit tests.  This
  * makes running tests out of Maven's plugin architecture directly not necessary.
  *
  * To run this test, this example utilizes he FitnessSuite class to run a SUITE
  * of FitNesse tests.  I have not yet determined if this method will run a single
  * test, but the alternative would be to use JUnitHelper to accomplish single
  * test mode.
  */
 package klauer.fitnessemvn;
 
 import org.junit.runner.RunWith;
 
 import fitnesse.junit.FitNesseSuite;
 import fitnesse.junit.FitNesseSuite.FitnesseDir;
 import fitnesse.junit.FitNesseSuite.Name;
 import fitnesse.junit.FitNesseSuite.OutputDir;
 
 @RunWith(FitNesseSuite.class)
 @Name("FrontPage.TestSuite") // We are running a SUITE of tests.
 @FitnesseDir("src/main/fitnesse")
 @OutputDir("target/fitnesse") //Specify an absolute or relative path
public class ExampleTestRunnerTest {}
