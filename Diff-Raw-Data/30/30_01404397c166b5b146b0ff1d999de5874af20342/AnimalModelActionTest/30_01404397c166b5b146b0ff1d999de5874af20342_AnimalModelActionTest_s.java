 /*
  * AnimalModelActionTest.java
  * JUnit based test
  *
  * Created on November 22, 2005, 1:59 PM
  * 
  * $Id: AnimalModelActionTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2009/07/07 17:46:54  pandyas
  * modified according to the recommended directory layout for BDA
  *
  * Revision 1.3  2006/04/17 19:37:33  pandyas
  * Added $Id: AnimalModelActionTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $ and $log:$
  *
  */
 
package gov.nih.nci.camod.webapp.action;
 
 import junit.framework.*;
 import servletunit.struts.*;
 
 /**
  *
  * @author piparom
  */
 public class AnimalModelActionTest extends MockStrutsTestCase {
   
   public AnimalModelActionTest(String testName) {
     super(testName);
   }
 
   protected void setUp() throws Exception {
   }
 
   protected void tearDown() throws Exception {
   }
 
   public static Test suite() {
     TestSuite suite = new TestSuite(AnimalModelActionTest.class);
     
     return suite;
   }
 
   /**
    * Test of save method, of class gov.nih.nci.camod.webapp.action.AnimalModelAction.
    */
   public void testSave() {
     System.out.println("testSave");
         
     // set the request path
     setRequestPathInfo("/AnimalModelAction");
     
     // set the request parameters
     addRequestParameter("method", "save");
     //addRequestParameter("param_name", "value");
     //addRequestParameter("param_name", "value");
     
     // execute the action
     actionPerform();
     
      // verify if the expected forward is set
     verifyForward("AnimalModelTreePopulateAction"); 
     
     // verify any expected values
     //assertEquals(expected_value, actual_value);
     
     // verify no action errors occured
     verifyNoActionErrors();
     //or verify specific action errors occured    
     //verifyActionErrors("error.errorname.etc...");      
   }
 
   /**
    * Test of edit method, of class gov.nih.nci.camod.webapp.action.AnimalModelAction.
    */
   public void testEdit() {
     System.out.println("testEdit");
     
     // set the request path
     setRequestPathInfo("/AnimalModelAction");
     
     // set the request parameters
     addRequestParameter("method", "edit");
     //addRequestParameter("param_name", "value");
     //addRequestParameter("param_name", "value");
     
     // execute the action
     actionPerform();
     
      // verify if the expected forward is set
     verifyForward("AnimalModelTreePopulateAction"); 
     
     // verify any expected values
     //assertEquals(expected_value, actual_value);
     
     // verify no action errors occured
     verifyNoActionErrors();
     //or verify specific action errors occured    
     //verifyActionErrors("error.errorname.etc...");      
   }
 
   /**
    * Test of duplicate method, of class gov.nih.nci.camod.webapp.action.AnimalModelAction.
    */
   public void testDuplicate() {
     System.out.println("testDuplicate");
     
    // set the request path
     setRequestPathInfo("/AnimalModelAction");
     
     // set the request parameters
     addRequestParameter("method", "duplicate");
     //addRequestParameter("param_name", "value");
     //addRequestParameter("param_name", "value");
     
     // execute the action
     actionPerform();
     
      // verify if the expected forward is set
     verifyForward("AnimalModelTreePopulateAction"); 
     
     // verify any expected values
     //assertEquals(expected_value, actual_value);
     
     // verify no action errors occured
     verifyNoActionErrors();
     //or verify specific action errors occured    
     //verifyActionErrors("error.errorname.etc...");      
   }
 
   /**
    * Test of delete method, of class gov.nih.nci.camod.webapp.action.AnimalModelAction.
    */
   public void testDelete() {
     System.out.println("testDelete");
     
     // set the request path
     setRequestPathInfo("/AnimalModelAction");
     
     // set the request parameters
     addRequestParameter("method", "delete");
     //addRequestParameter("param_name", "value");
     //addRequestParameter("param_name", "value");
     
     // execute the action
     actionPerform();
     
      // verify if the expected forward is set
     verifyForward("AnimalModelTreePopulateAction"); 
     
     // verify any expected values
     //assertEquals(expected_value, actual_value);
     
     // verify no action errors occured
     verifyNoActionErrors();
     //or verify specific action errors occured    
     //verifyActionErrors("error.errorname.etc...");      
   }
 
   /**
    * Test of returnUserModels method, of class gov.nih.nci.camod.webapp.action.AnimalModelAction.
    */
   public void testReturnUserModels() {
     System.out.println("testReturnUserModels");
     
      // set the request path
     setRequestPathInfo("/AnimalModelAction");
     
     // set the request parameters
     addRequestParameter("method", "returnUserModels");
     //addRequestParameter("param_name", "value");
     //addRequestParameter("param_name", "value");
     
     // execute the action
     actionPerform();
     
      // verify if the expected forward is set
     verifyForward("AnimalModelTreePopulateAction"); 
     
     // verify any expected values
     //assertEquals(expected_value, actual_value);
     
     // verify no action errors occured
     verifyNoActionErrors();
     //or verify specific action errors occured    
     //verifyActionErrors("error.errorname.etc...");      
   }
   
 }
