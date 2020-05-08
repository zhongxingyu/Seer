 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.user;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 
 import java.util.List;
 
 public class SetupTestUsersTest extends SetupRootUserTest {
 
     protected UserManagerService rootUserService = null;
     protected AccessControlManagerService rootACLService = null;
 
     protected User jason = null;
     protected User michael = null;
     protected User oliver = null;
 
     public SetupTestUsersTest(String name) {
         super(name);
     }
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static Test suite() {
         return new TestSuite(SetupTestUsersTest.class);
     }
 
     protected void setUp() {
         super.setUp();
         try {
             super.testLoginRootUser();
            rootUserService = (UserManagerService) factory.createUserPortletService(UserManagerService.class, rootUser, config, true);
            rootACLService = (AccessControlManagerService) factory.createUserPortletService(AccessControlManagerService.class, rootUser, config, true);
         } catch (PortletServiceException e) {
             fail("Unable to initialize user services");
         }
 
     }
 
     protected void createUsers() {
 
         AccountRequest jasonRequest = rootUserService.createAccountRequest();
         jasonRequest.setUserID("jason");
         jasonRequest.setGivenName("Jason");
         jasonRequest.setPasswordValue("");
         jasonRequest.setPasswordValidation(false);
 
         AccountRequest michaelRequest = rootUserService.createAccountRequest();
         michaelRequest.setUserID("michael");
         michaelRequest.setGivenName("Michael");
         michaelRequest.setPasswordValue("");
         michaelRequest.setPasswordValidation(false);
 
         AccountRequest oliverRequest = rootUserService.createAccountRequest();
         oliverRequest.setUserID("oliver");
         oliverRequest.setGivenName("Oliver");
         oliverRequest.setPasswordValue("");
         oliverRequest.setPasswordValidation(false);
 
         try {
             rootUserService.submitAccountRequest(jasonRequest);
             rootUserService.submitAccountRequest(michaelRequest);
             rootUserService.submitAccountRequest(oliverRequest);
             jason = rootUserService.approveAccountRequest(jasonRequest);
             michael = rootUserService.approveAccountRequest(michaelRequest);
             oliver = rootUserService.approveAccountRequest(oliverRequest);
         } catch (PortletServiceException e) {
             String msg = "Failed to setup users";
             //log.error(msg, e);
             fail(msg);
         }
     }
 
     public void testCreateTestUsers() {
         // jason, michael should be users
         // oliver will be denied
 
         createUsers();
 
         // test accessor methods
 
         // should be root + 3 new users
         List users = rootUserService.getUsers();
         assertEquals(users.size(), 4);
 
         User jason2 = rootUserService.getUserByUserName(jason.getUserName());
         assertEquals(jason2.getGivenName(), jason.getGivenName());
 
 
         deleteUsers();
         users = rootUserService.getUsers();
         assertEquals(1, users.size());
         // test accessor methods
         //users = rootUserService.getUsers();
         //assertFalse(users.contains(jason));
         //assertFalse(users.contains(michael));
         //assertFalse(users.contains(oliver));
     }
 
     protected void deleteUsers() {
         // test delete users
         rootUserService.deleteAccount(jason);
         User jason2 = rootUserService.getUserByUserName(jason.getUserName());
         assertNull(jason2);
 
         rootUserService.deleteAccount(michael);
         rootUserService.deleteAccount(oliver);
 
     }
 
     protected void tearDown() {
 
     }
 
 }
