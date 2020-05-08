 package jumble.fast;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import experiments.TimedTests;
 
 /**
  * Tests the corresponding class
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class TimingTestSuiteTest extends TestCase {
   private TimingTestSuite mSuite;
 
   private TestResult mResult;
 
   protected void setUp() throws Exception {
    mSuite = new TimingTestSuite(new Class[] { TimedTests.class });
     mResult = new TestResult();
     PrintStream oldOut = System.out;
     System.setOut(new PrintStream(new ByteArrayOutputStream()));
     mSuite.run(mResult);
     System.setOut(oldOut);
   }
 
   protected void tearDown() {
     mSuite = null;
     mResult = null;
   }
 
   public final void testGetOrder() {
     TestOrder order = mSuite.getOrder();
     assertEquals(3, order.getTestCount());
     assertEquals(2, order.getTestIndex(0));
     assertEquals(0, order.getTestIndex(1));
     assertEquals(1, order.getTestIndex(2));
    // A biTt dangerous
    assertTrue(11000 <= order.getTotalRuntime()
        && order.getTotalRuntime() < 12000);
   }
 
   public final void testResult() {
     assertEquals(3, mResult.runCount());
     assertEquals(0, mResult.errorCount());
     assertEquals(0, mResult.failureCount());
   }

   public static Test suite() {
     TestSuite suite = new TestSuite(TimingTestSuiteTest.class);
     return suite;
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 }
