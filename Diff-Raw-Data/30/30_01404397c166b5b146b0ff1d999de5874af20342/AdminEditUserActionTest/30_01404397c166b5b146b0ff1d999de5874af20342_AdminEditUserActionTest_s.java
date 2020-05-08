 /*
  * AdminEditUserActionTest.java
  * JUnit based test
  *
  * Created on November 22, 2005, 2:18 PM
  * 
  * $Id: AdminEditUserActionTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2009/07/07 17:46:54  pandyas
  * modified according to the recommended directory layout for BDA
  *
  * Revision 1.3  2006/04/17 19:37:33  pandyas
  * Added $Id: AdminEditUserActionTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $ and $log:$
  *
  */
 
 
 
package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.service.impl.PersonManagerSingleton;
 import gov.nih.nci.camod.webapp.form.EditUserForm;
 import gov.nih.nci.common.persistence.Search;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionMapping;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 /**
  *
  * @author piparom
  */
 public class AdminEditUserActionTest extends BaseActionTest
 {
 
     private static final String USERNAME = "testuser";
     private static final String FIRSTNAME = "Test";
     private static final String LASTNAME = "User";
 
     // Needed because a reset is called which causes validation to fail
     private class NoResetEditUserForm extends EditUserForm
     {
         private static final long serialVersionUID = 3257098753799404851L;
 
         public void reset(ActionMapping mapping,
                           HttpServletRequest request)
         {}
     }
 
     public AdminEditUserActionTest(String testName)
     {
         super(testName);
     }
 
     protected void setUp() throws Exception
     {
         super.setUp();
         removeUser(USERNAME, null, null);
         removeUser(null, FIRSTNAME, LASTNAME);
     }
 
     protected void tearDown() throws Exception
     {
         super.tearDown();
         removeUser(USERNAME, null, null);
         removeUser(null, FIRSTNAME, LASTNAME);
     }
 
     public static Test suite()
     {
         TestSuite suite = new TestSuite(AdminEditUserActionTest.class);
         return suite;
     }
 
     public void testAddUser() throws Exception
     {
 
         setRequestPathInfo("/AdminEditUserAction.do");
 
         // Set the user as being logged in with all privs
         setLoggedIn();
         setAllRoles();
 
         // Create a fake user
         NoResetEditUserForm theForm = new NoResetEditUserForm();
         theForm.setFirstName(FIRSTNAME);
         theForm.setLastName(LASTNAME);
         theForm.setUsername(USERNAME);
         theForm.setPrincipalInvestigator(false);
 
         setActionForm(theForm);
 
         // execute the action
         actionPerform();
 
         // verify if the expected forward is set
         verifyForward("next");
 
         // verify no action errors occured
         verifyNoActionErrors();
     }
 
     public void testAddUserNoUsername() throws Exception
     {
 
         setRequestPathInfo("/AdminEditUserAction.do");
 
         // Set the user as being logged in with all privs
         setLoggedIn();
         setAllRoles();
 
         // Create a fake user
         NoResetEditUserForm theForm = new NoResetEditUserForm();
         theForm.setFirstName(FIRSTNAME);
         theForm.setLastName(LASTNAME);
         theForm.setPrincipalInvestigator(false);
 
         setActionForm(theForm);
 
         // execute the action
         actionPerform();
 
         // verify if the expected forward is set
         verifyForward("next");
 
         // verify no action errors occured
         verifyNoActionErrors();
     }
 
     public void testAddUserWithSameUsername() throws Exception
     {
 
         setRequestPathInfo("/AdminEditUserAction.do");
 
         // Set the user as being logged in with all privs
         setLoggedIn();
         setAllRoles();
 
         // Create the test user in the DB
         Person thePerson = new Person();
         thePerson.setFirstName(FIRSTNAME);
         thePerson.setLastName(LASTNAME);
         thePerson.setUsername(USERNAME);
         thePerson.setIsPrincipalInvestigator(new Boolean(false));
         PersonManagerSingleton.instance().save(thePerson);
 
         // Attempt to create a user w/ a duplicate username
         NoResetEditUserForm theForm = new NoResetEditUserForm();
         theForm.setFirstName(FIRSTNAME);
         theForm.setLastName(LASTNAME);
         theForm.setUsername(USERNAME);
         theForm.setPrincipalInvestigator(false);
 
         setActionForm(theForm);
 
         // execute the action; should fail since a user w/ that username already exists
         actionPerform();
 
         // verify if the expected forward is set; should go 
         verifyForward("baddata");
         
         // verify the correct errors ocurred
         String[] theErrors = {"admin.user.usernameexists"};
         verifyActionErrors(theErrors);
     }
 
     public void testAddUserMissingFirstName() throws Exception
     {
 
         setRequestPathInfo("/AdminEditUserAction.do");
 
         // Set the user as being logged in with all privs
         setLoggedIn();
         setAllRoles();
 
         // Create a fake user
         NoResetEditUserForm theForm = new NoResetEditUserForm();
         theForm.setLastName(LASTNAME);
         theForm.setPrincipalInvestigator(false);
 
         setActionForm(theForm);
 
         // execute the action
         actionPerform();
 
         // verify that we go back to the input on validation errors
         verifyForward("input");
         
         // verify the correct errors ocurred
         String[] theErrors = {"errors.required"};
         verifyActionErrors(theErrors);
     }
 
     public void testAddUserMissingLastName() throws Exception
     {
 
         setRequestPathInfo("/AdminEditUserAction.do");
 
         // Set the user as being logged in with all privs
         setLoggedIn();
         setAllRoles();
 
         // Create a fake user
         NoResetEditUserForm theForm = new NoResetEditUserForm();
         theForm.setFirstName(FIRSTNAME);
         theForm.setPrincipalInvestigator(false);
 
         setActionForm(theForm);
 
         // execute the action
         actionPerform();
 
         // verify that we go back to the input on validation errors
         verifyForward("input");
         
         // verify the correct errors ocurred
         String[] theErrors = {"errors.required"};
         verifyActionErrors(theErrors);
     }
 
     private void removeUser(String inUsername,
                             String inFirstName,
                             String inLastName) throws Exception
     {
         Person theQBEPerson = new Person();
 
         if (inUsername != null)
         {
             theQBEPerson.setUsername(inUsername);
         }
         else if (inFirstName != null && inLastName != null)
         {
             theQBEPerson.setFirstName(inFirstName);
             theQBEPerson.setLastName(inLastName);
         }
         else
         {
             throw new IllegalArgumentException("Need more info to delete a user");
         }
 
         List theModelList = Search.query(theQBEPerson);
 
         if (theModelList.size() > 1)
         {
             throw new IllegalArgumentException("More than one user matched the name: " + inUsername);
         }
         else if (theModelList.size() > 0)
         {
             Person thePerson = (Person) theModelList.get(0);
             PersonManagerSingleton.instance().remove(thePerson.getId().toString());
         }
     }
 
 }
