 package web.allwebtests;
 
 import web.submission.*;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 /**
  * @author pandyas
  */
 public class AllSubmissionTests extends TestCase {
 
 	public AllSubmissionTests(String arg0) {
 		super(arg0);
 	}
 	
     public static Test suite()
     {
         TestSuite suite = new TestSuite();
 
         // Submission tests
         suite.addTest(EditModelCharacteristicsTest.suite());
       // suite.addTest(SubmitEditDeleteCITest.suite());
         suite.addTest(SubmitEditDeleteCellLinesTest.suite());
         suite.addTest(SubmitEditDeleteHistopathologyTest.suite());
         suite.addTest(SubmitEditDeleteImageTest.suite());
         suite.addTest(SubmitEditDeleteModelAvailabilityTest.suite());
         suite.addTest(SubmitEditDeletePublicationTest.suite());
         suite.addTest(SubmitEditDeleteTherapyTest.suite());
         suite.addTest(SubmitEditDeleteTransIntTest.suite());
        suite.addTest(SubmitEditDeleteTransplantTest.suite());        
        //suite.addTest(SubmitEditDeleteGDTest.suite());     
         suite.addTest(SubmitPageTest.suite());
 
         return suite;
     }
 
     public static void main(String args[])
     {
         TestRunner.run(suite());
     }	
 
 }
