 /**
  * @author dgeorge
  * 
  * $Id: RegisterUserPopulateAction.java,v 1.2 2005-10-27 18:32:19 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2005/10/21 20:47:04  georgeda
  * Initial revision
  *
  *
  */
 package gov.nih.nci.camod.webapp.action;
 
 import java.util.Enumeration;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 /**
  * 
  * Used to populate the form used to assign roles to a user.
  * 
  */
 public class RegisterUserPopulateAction extends BaseAction {
 
     /**
      * Action used to populate the various admin lists for the curation process
      */
     public ActionForward execute(ActionMapping inMapping, ActionForm inForm, HttpServletRequest inRequest,
             HttpServletResponse inResponse) throws Exception {
 
         log.trace("Entering execute");
        
        // Clean all headers for security scan (careful about what chars you allow)
    	String headername = "";
    	for(Enumeration e = inRequest.getHeaderNames(); e.hasMoreElements();){
    		headername = (String)e.nextElement();
    		log.debug("RegisterUserPopulateAction headername: " + headername);
    		String cleanHeaders = SafeHTMLUtil.clean(headername);
    		log.debug("RegisterUserPopulateAction cleaned headername: " + headername);
    	}        
 
         try {
             NewDropdownUtil.populateDropdown(inRequest, Constants.Dropdowns.PRINCIPALINVESTIGATORDROP, Constants.Dropdowns.ADD_BLANK_OPTION);
         } catch (Exception e) {
 
             log.error("Unable to get user settings: ", e);
 
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(inRequest, theMsg);
         }
 
         log.trace("Exiting execute");
 
         return inMapping.findForward("next");
     }
 }
