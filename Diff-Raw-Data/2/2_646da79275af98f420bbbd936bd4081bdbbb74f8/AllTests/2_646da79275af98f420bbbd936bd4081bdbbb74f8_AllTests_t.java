 package jumble.fast;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 public class AllTests extends TestSuite {
   public static Test suite() {
     TestSuite suite = new TestSuite();
     suite.addTest(FastJumblerTest.suite());
     suite.addTest(FastRunnerTest.suite());
     suite.addTest(FlatTestSuiteTest.suite());
     suite.addTest(JumbleTestSuiteTest.suite());
     suite.addTest(SeanResultPrinterTest.suite());
    suite.addTest(TestOrderTest.suite());
     suite.addTest(TimingTestSuiteTest.suite());
     suite.addTest(FailedTestMapTest.suite());
     return suite;
   }
   
   public static void main(String[] args) {
     TestRunner.run(suite());
   }
 }
