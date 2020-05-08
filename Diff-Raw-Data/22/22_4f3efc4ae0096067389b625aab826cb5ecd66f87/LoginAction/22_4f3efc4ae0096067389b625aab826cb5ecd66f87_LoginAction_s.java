 /**
  * 
 * $Id: LoginAction.java,v 1.23 2008-06-30 15:31:03 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.22  2008/06/23 18:07:06  pandyas
  * Modified to prevent Security issues
  * Invalidate relevant session identifiers when a user signs out
  * Re: Apps Scan run 06/17/2008
  *
  * Revision 1.21  2008/06/13 17:35:00  pandyas
  * Modified to prevent Security issues
  * Invalidate relevant session identifiers when a user signs out
  * Re: Apps Scan run 06/12/2008
  *
  * Revision 1.20  2008/06/03 00:29:38  pandyas
  * Modified to prevent SQL injection
  * Trying to secure JSESSIONID to prevent modification
  * Re: Apps Scan run 05/29/2008
  *
  * Revision 1.19  2008/05/27 14:37:02  pandyas
  * Modified to prevent SQL injection
  * Cleaned method name before proceeding
  * Re: Apps Scan run 05/23/2008
  *
  * Revision 1.18  2008/05/23 16:00:28  pandyas
  * Modified JSESSIONID in App Scan testing
  * This is an attempt to create a new JSESSIONID if App Scan removes it
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.17  2008/01/15 19:31:55  pandyas
  * Modified debug statements to build to dev tier
  *
  * Revision 1.16  2007/12/17 18:03:22  pandyas
  * Removed * in searchFilter used for getting e-mail from LDAP
  * Apps Support ticket was submitted (31169 - incorrect e-mail associated with my caMOD account) stating:
  *
  * Cheryl Marks submitted a ticket to NCICB Application Support in which she requested that the e-mail address associated with her account in the "User Settings" screen in caMOD be corrected. She has attempted to correct it herself, but because the program queries the LDAP Server for the e-mail address, her corrections were not retained.
  *
  * Revision 1.15  2007/09/12 19:36:40  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.14  2007/03/20 14:08:48  pandyas
  * Added logging to debug QA tier
  *
  * Revision 1.13  2006/10/17 16:11:00  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.12  2006/05/17 14:16:52  schroedn
  * Added columnOrder to retain the column order on search results
  *
  * Revision 1.11  2006/05/10 14:15:39  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.10  2006/04/28 19:26:17  schroedn
  * Defect # 238
  * On login sets the user's search result column settings
  *
  * Revision 1.9  2005/12/06 19:51:39  georgeda
  * Defect #255 - add SSL
  *
  * Revision 1.8  2005/12/06 14:50:45  georgeda
  * Defect #253, change the lowecase to the login action so that roles match
  *
  * Revision 1.7  2005/11/28 18:33:35  georgeda
  * Defect #104.  Clicking on a model and then using the backarrow works after login
  *
  * Revision 1.6  2005/09/22 15:18:13  georgeda
  * More changes
  *
  * Revision 1.5  2005/09/16 15:52:55  georgeda
  * Changes due to manager re-write
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.ResultSettings;
 import gov.nih.nci.camod.domain.ResultSettingsColumns;
 import gov.nih.nci.camod.service.SavedQueryManager;
 import gov.nih.nci.camod.service.ResultSettingsManager;
 import gov.nih.nci.camod.service.impl.UserManagerSingleton;
 import gov.nih.nci.camod.webapp.form.LoginForm;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public final class LoginAction extends BaseAction {
 
     /**
      * Authenticates the user using the CSM AuthenticationManager and stores in
      * the session.
      * 
      * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
      *      org.apache.struts.action.ActionForm,
      *      javax.servlet.http.HttpServletRequest,
      *      javax.servlet.http.HttpServletResponse)
      */
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws IOException, ServletException {
     	log.debug("Enter LoginAction.execute");
         LoginForm loginForm = (LoginForm) form;
 
         String theUsername = loginForm.getUsername().toLowerCase(); 
         
         Cookie validUserCookie = new Cookie("validUserKey", "123456789");
         response.addCookie(validUserCookie);        
         
         
         //  Example to secure a cookie, i.e. instruct the browser to
         //  Send the cookie using a secure protocol
         Cookie[] cookieArray = request.getCookies(); 
         for(int i = 0; i < cookieArray.length; i++){
         	log.debug("Cookie name: " + cookieArray[i].getName());
         	log.debug("Cookie value: " + cookieArray[i].getValue()); 
         	log.debug("Cookie MaxAge: " + cookieArray[i].getMaxAge());         	
         	if(cookieArray[i].getName().startsWith("JSESSIONID=")) {
         	cookieArray[i].setSecure(true); 
         	log.debug("Secured JSESSIONID");
         	}       	
         } 
 
         // check login credentials using Authentication Mangager
         boolean loginOK = UserManagerSingleton.instance().login(theUsername, loginForm.getPassword(), request);
 
         String forward = "failure";
 
         if (loginOK) {
             log.info("Successful login");            
             
             forward = "success";
             request.getSession().setAttribute(Constants.CURRENTUSER, theUsername);
             
 		    //Used for sidebar, number of saved queries
             SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");   
             
             //Used to pre-load settings for search result columns
             ResultSettingsManager resultSettingsManager = (ResultSettingsManager) getBean("resultSettingsManager");
             
             try {
                 List savedQueriesList = savedQueryManager.getSavedQueriesByUsername( (String) request.getSession().getAttribute(Constants.CURRENTUSER) );
                 request.getSession().setAttribute(Constants.NUMBEROFSAVEDQUERIES, String.valueOf(savedQueriesList.size()) );
                 
                 //Pre-load columns for search results
                 String[] columns = Constants.SEARCHRESULTCOLUMNSDEFAULT;
                 String itemsPerPage = "" + Constants.ITEMSPERPAGEDEFAULT;
                 
                 ResultSettings inResultSettings = resultSettingsManager.getByUsername( (String) request.getSession().getAttribute(Constants.CURRENTUSER) );
         
                 if ( inResultSettings != null ) 
                 {
                     log.info("queryString: " + request.getQueryString());                	
                     Set<ResultSettingsColumns> resultSettingsColumnsList = inResultSettings.getResultSettingsColumns();
                     Iterator <ResultSettingsColumns> setIter = resultSettingsColumnsList.iterator();                  
                     itemsPerPage = "" + inResultSettings.getItemsPerPage();
                     String[] theColumns = new String[resultSettingsColumnsList.size()];
                     
                     while ( setIter.hasNext() )
                     {                       
                         ResultSettingsColumns theResultSettingsColumns = (ResultSettingsColumns) setIter.next();
                         theColumns[theResultSettingsColumns.getColumnOrder()] = theResultSettingsColumns.getColumnName();
                     }  
                     
                     request.getSession().setAttribute( Constants.ITEMSPERPAGE, itemsPerPage );        
                     request.getSession().setAttribute( Constants.SEARCHRESULTCOLUMNS, theColumns );
 
                 } else {                
                     request.getSession().setAttribute( Constants.ITEMSPERPAGE, itemsPerPage );        
                     request.getSession().setAttribute( Constants.SEARCHRESULTCOLUMNS, columns );
                 }
                 
             } catch (Exception e) {
                 log.debug( "User search result settings load failed" );
             }
             
         } else {
             log.debug("Login failed");
             request.getSession().setAttribute(Constants.LOGINFAILED, "true");
         }
 
         // Forward control to the specified success URI
         return mapping.findForward(forward);
     }

    // Simple code for the retrieval of a cookie value given a cookie name by looping through the 
    // array of available Cookie objects, returning the value of any Cookie whose name matches the input. 
    // If there is no match, the designated default value is returned.
    public static String getCookieValue(Cookie[] cookies,
        String cookieName,
        String defaultValue) {
			for(int i=0; i<cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookieName.equals(cookie.getName()))
				return(cookie.getValue());
			}
		return(defaultValue);
    }    
 }
