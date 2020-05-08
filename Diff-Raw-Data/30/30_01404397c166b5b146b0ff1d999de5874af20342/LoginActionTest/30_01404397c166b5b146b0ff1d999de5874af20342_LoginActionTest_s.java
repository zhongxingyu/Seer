 /*
  * LoginActionTest.java
  * JUnit based test
  *
  * $Id: LoginActionTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $
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
 
 import gov.nih.nci.camod.webapp.form.LoginForm;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class LoginActionTest extends BaseActionTest {
 
     public LoginActionTest(String testName) {
         super(testName);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public static Test suite() {
         TestSuite suite = new TestSuite(LoginActionTest.class);
         return suite;
     }
 
     public void testGoodLogin() {
 
         // set the request path
         setRequestPathInfo("/LoginAction.do");
 
         String theUsername = myBundle.getString("username");
         String thePassword = myBundle.getString("password");
 
         LoginForm theForm = new LoginForm();
         theForm.setUsername(theUsername);
         theForm.setPassword(thePassword);
 
         setActionForm(theForm);
 
         setUnprotectedMethod("");
 
         // execute the action
         actionPerform();
 
         // verify if the expected forward is set
         verifyForward("success");
 
         // verify no action errors occured
         verifyNoActionErrors();
     }
 
     public void testBadLogin() {
 
         // set the request path
         setRequestPathInfo("/LoginAction.do");
 
         String theUsername = myBundle.getString("username");
 
         LoginForm theForm = new LoginForm();
         theForm.setUsername(theUsername);
         theForm.setPassword("badpassword");
 
         setActionForm(theForm);
 
         setUnprotectedMethod("");
 
         // execute the action
         actionPerform();
 
         // verify if the expected forward is set
         verifyForward("failure");
     }
 }
