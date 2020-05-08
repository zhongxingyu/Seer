 package gov.nih.nci.cagwas.web.action;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.security.sasl.AuthenticationException;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.action.ActionMessage;
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.cagwas.web.form.LoginForm;
 import gov.nih.nci.caintegrator.security.SecurityManager;
 import gov.nih.nci.caintegrator.security.UserCredentials;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 
 /**
  * The LoginAction class is called when the login form posts. It is
  * responsible for authenticating the user and getting its roles.
  * <P>
  * @author mholck
  * @see org.apache.struts.action.Action
  */
 public class LoginAction extends Action
 {
 	private static Logger logger = Logger.getLogger(LoginAction.class);
 	private final String APPLICATION_CONTEXT = "cagwas";
 	
 	/**
 	 * execute is called when this action is posted to
 	 * <P>
 	 * @param mapping The ActionMapping for this action as configured in struts
 	 * @param form The ActionForm that posted to this action if any
 	 * @param request The HttpServletRequest for the current post
 	 * @param response The HttpServletResponse for the current post
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 		throws Exception
 	{
 		ActionMessages errors = new ActionMessages();
 		ActionForward forward = null;
 		
 		// Figure out the state of the form
 		LoginForm lForm = (LoginForm)form;
 		String username = lForm.getUsername();
 		String password = lForm.getPassword();
 		logger.debug("Username is " + username);
 		
 		SecurityManager securityManager = SecurityManager.getInstance(APPLICATION_CONTEXT);
 		UserCredentials credentials = null;
 		
 		if (securityManager == null)
 		{
 			logger.error("Unable to get security manager for authentication");
 			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.authentication"));
 		}
 		else
 		{
 			try
 			{
 				if(securityManager.authenticate(username, password))
 				{
 					credentials = securityManager.authorization(username);
 				}
 			}
 			catch (AuthenticationException e)
 			{
 				logger.debug(e);
 			}
 	
 			if (credentials != null && credentials.authenticated())
 			{
 				// Now put the logged attribute in the session to signify they are logged in
 				request.getSession().setAttribute("logged", "yes");
 				request.getSession().setAttribute("name", username);
 				request.getSession().setAttribute("email", credentials.getEmailAddress());
 				Collection<ProtectionElement> protectionElements = credentials.getprotectionElements();
 				if(protectionElements != null  && !protectionElements.isEmpty()){
 					List<String> studyIDList = new ArrayList<String>();
 					for(ProtectionElement pe:protectionElements){
						if(pe.getProtectionElementId()!= null){
 							studyIDList.add(pe.getObjectId());
 						}
 					}
 					request.getSession().setAttribute("studyIDList", studyIDList);		
 				}
 			}
 			else
 			{
 				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.authentication"));
 				//request.getSession().invalidate();
 			}
 		}
 		
 		// If there were errors then return to the input page else go on
 	    if (!errors.isEmpty())
 	    {
 	      addErrors(request, errors);
 	      forward = new ActionForward(mapping.getInput());
 	    }
 	    else
 	    {
 	    	if(request.getSession().getAttribute("ref") != null)
 	    	{
 	    		String go = (String)request.getSession().getAttribute("ref");
 	    		request.getSession().removeAttribute("ref");
 	    		List studyIDs = (List)request.getSession().getAttribute("studyIDList");
 	    		Long studyId = (Long)request.getSession().getAttribute("studyId");
 	    		// must do this preprocessing before forwarding to make sure beans are in place
 	    		String logged = (String)request.getSession().getAttribute("logged");
 	    		if (logged != null  && (logged.equals("yes")  &&
 	    			studyId != null &&
 	    			studyIDs != null && studyIDs.contains(studyId.toString())))
 	    		{
 		    		BrowseAction brac = new BrowseAction();
 		    		
 		    		// Setting up genotypes will get everything for subjects too
 		    		brac.setupGenotype(request);
 		    		// make a new forward with the orig referer as the new path
 			    	forward = new ActionForward(go, false);
 	    		}
 	    		else{
 					forward = mapping.findForward("accessWarning");
 	    		}
 
 
 	    	}
 	    	else
 	    	{
 	    		forward = mapping.findForward("success");
 	    	}
 	    }
 		
 		return forward;
 	}
 
 }
