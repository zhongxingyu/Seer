 /*
  * BaseActionTest.java
  *
  * $Id: BaseActionTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2009/07/07 17:46:54  pandyas
  * modified according to the recommended directory layout for BDA
  *
  * Revision 1.2  2006/05/08 14:22:59  georgeda
  * Reformat and clean up warnings
  *
  * Revision 1.1  2005/12/28 16:41:57  georgeda
  * Changes for testing
  *
  * 
  */
 
package unit.gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 
 import java.util.*;
 
 import servletunit.struts.CactusStrutsTestCase;
 
 public class BaseActionTest extends CactusStrutsTestCase
 {
     protected ResourceBundle myBundle = ResourceBundle.getBundle("test");
 
     public BaseActionTest(String testName)
     {
         super(testName);
     }
 
     protected void setUp() throws Exception
     {
         super.setUp();
     }
 
     protected void tearDown() throws Exception
     {
         super.tearDown();
     }
 
     protected void setLoggedIn()
     {
         String theUsername = myBundle.getString("username");
         getRequest().getSession().setAttribute(Constants.CURRENTUSER, theUsername);
     }
 
     protected void setAllRoles()
     {
         List<String> theRoles = new ArrayList<String>();
         theRoles.add(Constants.Admin.Roles.COORDINATOR);
         theRoles.add(Constants.Admin.Roles.EDITOR);
         theRoles.add(Constants.Admin.Roles.SCREENER);
         theRoles.add(Constants.Admin.Roles.SUPER_USER);
 
         getRequest().getSession().setAttribute(Constants.CURRENTUSERROLES, theRoles);
     }
 
 
     protected void setUnprotectedMethod(String inMethod)
     {
         // set the request parameters
         addRequestParameter("unprotected_method", inMethod);
     }
 
     protected void setProtectedMethod(String inMethod)
     {
         // set the request parameters
         addRequestParameter("method", inMethod);
     }
 }
