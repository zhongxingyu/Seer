 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.user;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.gridlab.gridsphere.portlet.GuestUser;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portletcontainer.GridSphereServletTest;
 
 public class SetupRootUserTest extends GridSphereServletTest {
 
     protected User rootUser = null;
 
     public SetupRootUserTest(String name) {
         super(name);
     }
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static Test suite() {
         return new TestSuite(SetupRootUserTest.class);
     }
 
     protected void setUp() {
        super.testInitGridSphere();
     }
 
     public void testLoginRootUser() throws PortletServiceException {
        LoginService loginService = (LoginService) factory.createUserPortletService(LoginService.class, GuestUser.getInstance(), config, true);
         rootUser = loginService.login("root", "");
         assertNotNull(rootUser);
 
     }
 
     protected void tearDown() {
 
     }
 
 }
