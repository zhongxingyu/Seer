 /**
  * 
 * $Id: LogoutAction.java,v 1.6 2008-06-11 17:44:57 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.5  2006/05/10 14:15:39  schroedn
 * New Features - Changes from code review
 *
  * Revision 1.4  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
import javax.servlet.http.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class LogoutAction extends BaseAction {
 	
 	public ActionForward execute(ActionMapping mapping,
             ActionForm form,
             HttpServletRequest request,
             HttpServletResponse response)
 	throws IOException, ServletException
 	{
 		System.out.println( "<LogoutAction execute> Logging Off" );
 		
 		request.getSession().setAttribute( "camod.loggedon.username", null );
 		request.getSession().invalidate();
        
        // explicitly remove the JSESSIONID to prevent security issue
        Cookie[] cookieArray = request.getCookies(); 
        for(int i = 0; i < cookieArray.length; i++){
            log.info("Cookie name: " + cookieArray[i].getName());
            log.info("Cookie value: " + cookieArray[i].getValue());           
            if(cookieArray[i].getName().equals("JSESSIONID") | cookieArray[i].getValue().equals("JSESSIONID")) {
                cookieArray[i].setValue(null); 
            log.info("removed value for JSESSIONID");
            }           
        }
 		return mapping.findForward( "loggedOut" );
 	}
 
 }
