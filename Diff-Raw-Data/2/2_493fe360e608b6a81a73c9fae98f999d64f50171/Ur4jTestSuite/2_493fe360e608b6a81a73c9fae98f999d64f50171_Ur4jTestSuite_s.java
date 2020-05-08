 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 /*
  * User: michael
  * Date: Mar 31, 2009
  * Time: 10:45:27 AM
  */
 public class Ur4jTestSuite {
 
     public static Test suite() {
 
         TestSuite suite = new TestSuite();
 
         // The RandomGUID tests
         suite.addTestSuite(RandomGUIDTest.class);
 
         // The UsageRecord tests
         suite.addTestSuite(UsageRecordTest.class);
 
         // The UsageRecordException tests
         suite.addTestSuite(UsageRecordExceptionTest.class);
 
         return suite;
     }
 
     /**
      * Runs the test suite using the textual runner.
      * @param args not required
      */
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }


 }
