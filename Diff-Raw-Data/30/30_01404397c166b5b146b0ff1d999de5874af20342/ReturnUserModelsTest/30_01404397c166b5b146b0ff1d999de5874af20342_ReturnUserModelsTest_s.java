 /*
  * LoginActionTest.java
  * JUnit based test
  *
  * $Id: ReturnUserModelsTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2009/07/07 17:46:54  pandyas
  * modified according to the recommended directory layout for BDA
  *
  * Revision 1.1  2005/12/28 16:41:57  georgeda
  * Changes for testing
  *
  * 
  */
 
package gov.nih.nci.camod.webapp.action;
 
 import java.util.ResourceBundle;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class ReturnUserModelsTest extends BaseActionTest {
 
     protected ResourceBundle myBundle = ResourceBundle.getBundle("test");
     
     public ReturnUserModelsTest(String testName) {
         super(testName);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public static Test suite() {
         TestSuite suite = new TestSuite(ReturnUserModelsTest.class);
 
         return suite;
     }
 
     public void testReturnUserModels() {
         
         // set the request path
         setRequestPathInfo("/ReturnUserModels.do");
 
         setLoggedIn();
         setProtectedMethod("returnUserModels");
         
         // execute the action
         actionPerform();
 
         // verify if the expected forward is set
         verifyForward("submitModels");
 
         // verify no action errors occured
         verifyNoActionErrors();
     }
 }
