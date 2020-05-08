 package edu.wustl.common.action;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.wustl.common.util.XMLPropertyHandler;
 import edu.wustl.common.util.global.CommonServiceLocator;
 import edu.wustl.common.util.global.Constants;
 import edu.wustl.common.util.global.Validator;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Action to support action.
  * @author kalpana_thakur
  * @deprecated use XSSSupportedAction in case of XSS validation only in this release.
  * In next release it will be removed.
  * For this release added XSSSupportedAction.java for cross scripting validation.
  * So if you want only XSS validations use this Action.
  * In later releases XSSSupportedAction will be removed or rather renamed as BaseAction.java.
  */
 public  abstract class XSSSupportedAction extends Action
 {
 
 	/**
 	 * LOGGER Logger - Generic LOGGER.
 	 */
 	private static final Logger LOGGER = Logger.getCommonLogger(BaseAction.class);
 	/**
 	 * Method ensures that the user is authenticated before calling the
 	 * executeAction of the subclass. If the User is not authenticated then an
 	 * UserNotAuthenticatedException is thrown.
 	 *
 	 * @param mapping	ActionMapping
 	 * @param form	ActionForm
 	 * @param request	HttpServletRequest
 	 * @param response	HttpServletResponse
 	 * @return ActionForward
 	 * @exception Exception Generic exception
 	 */
 	public final ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		LOGGER.info("Inside execute method of XSSSupportedAction ");
 		ActionForward actionForward=null;
 		LOGGER.info("Inside execute method of BaseAction ");
 		//long startTime = System.currentTimeMillis();
 		
 		String referer=request.getHeader("referer");
		LOGGER.info(" Referer Value : "+referer);
 		if(request.getRequestURL()!=null)
 		{
 			CommonServiceLocator.getInstance().setAppURL(request.getRequestURL().toString());
 		}
		LOGGER.info("Request URL Value : "+CommonServiceLocator.getInstance().getAppURL());
		if(referer!=null && !referer.startsWith(CommonServiceLocator.getInstance().getAppURL()))
 		{
 			String loadBalancerURL=XMLPropertyHandler.getValue("load.balancer.url");
			LOGGER.info("Load Balancer Value : "+loadBalancerURL);
			if(referer!=null && (loadBalancerURL!=null && !loadBalancerURL.equals("") && !referer.startsWith(loadBalancerURL)))
 			{
 				response.sendRedirect("/catissuecore/Logout.do?invalidRequest=true");
 				return actionForward;
 			}
 		}
 		 actionForward = checkForXSSViolation(mapping,form,
 				request, response);
 		return actionForward;
 	}
 
 	/**
 	 * Subclasses should implement this method to execute the Action logic.
 	 *
 	 * @param mapping ActionMapping
 	 * @param form ActionForm
 	 * @param request HttpServletRequest
 	 * @param response HttpServletResponse
 	 * @return ActionForward
 	 * @throws Exception Generic exception
 	 */
 	protected abstract ActionForward executeXSS(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception;
 	/**
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @return actionForward
 	 * @throws Exception Exception
 	 * @throws IOException IOException
 	 */
 	protected ActionForward checkForXSSViolation(ActionMapping mapping,
 			ActionForm form, HttpServletRequest request,
 			HttpServletResponse response)
 				throws Exception, IOException
 	{
 
 		ActionForward actionForward = null;
 		LOGGER.info("Checking for XSS validations");
 		boolean isRedirected = false;
 		final String ajaxRequest = request
 	                .getParameter(Constants.IS_AJAX_REQEUST);
 		boolean isAjaxRequest = false;
         boolean isToExecuteAction = true;
         
         isRedirected = getIsRedirected(request);
         isAjaxRequest = getIsAjaxRequest(ajaxRequest);
         final boolean isToRedirect = isToExecuteXSSSafeAction(request);
         if(isToRedirect)
         {
         	 if (isAjaxRequest)
              {
                  isToExecuteAction = processAjaxRequestViolations(request,
                          response);
              }
         	 else
         	 {
         		 if(null!=request.getParameter(Constants.PAGEOF))
              	{
         			 response.sendRedirect(CommonServiceLocator
                              .getInstance().getXSSFailurePath()+"?"+Constants.PAGEOF+"="+(String)request.getParameter(Constants.PAGEOF));
              	}
         		else
         		 {
         		 response.sendRedirect(CommonServiceLocator
                          .getInstance().getXSSFailurePath());
         		 }
         		 return actionForward;
         	 }
         }       
         if (isToRedirect)
         {
             if (isAjaxRequest)
             {
                 isToExecuteAction = processAjaxRequestViolations(request,
                         response);
                 addXSSValidationError(request);
             }
             else
             {
             	if (!isRedirected)
                 {
             		 isToExecuteAction = false;
                      request.setAttribute(Constants.PAGE_REDIRECTED,
                              Constants.BOOLEAN_YES);
                      if (mapping.getValidate())
                      {
                          actionForward = mapping.getInputForward();
                      }
                      else
                      {
                          response.sendRedirect(CommonServiceLocator
                                  .getInstance().getXSSFailurePath());
                      }
                 }
             }
         }
         if (isToExecuteAction)
         {
         	actionForward = executeXSS(mapping, form, request, response);
         }
 		return actionForward;
 	}
 
 	/**
 	 * @param isRedirected isRedirected
 	 * @param errorObjects errorObjects
 	 * @return redirect
 	 */
 	private boolean setIsRedirected(boolean isRedirected,
 			final Object errorObjects)
 	{
 		boolean redirect = isRedirected;
 		if (errorObjects instanceof ActionErrors
 		        && !((ActionErrors) errorObjects).isEmpty())
 		{
 			redirect = true;
 		}
 		return redirect;
 	}
 
 	/**
 	 * Check if it's Ajax Request.
 	 * @param ajaxRequest ajaxRequest.
 	 * @return true if ajax request.
 	 */
 	private boolean getIsAjaxRequest(final String ajaxRequest)
     {
         boolean isAjaxRequest = false;
         if (!Validator.isEmpty(ajaxRequest)
                 && Constants.BOOLEAN_YES.equalsIgnoreCase(ajaxRequest))
         {
             isAjaxRequest = true;
         }
         return isAjaxRequest;
     }
 
 	/**
 	 * @param request request
 	 * @param response response
 	 * @return isToExecuteAction
 	 * @throws IOException IOException
 	 */
 	private boolean processAjaxRequestViolations(
             final HttpServletRequest request, final HttpServletResponse response)
             throws IOException
     {
         boolean isToExecuteAction;
         isToExecuteAction = false;
         final Writer writer = response.getWriter();
 
         final Object propNameObject = request
                 .getAttribute(Constants.VIOLATING_PROPERTY_NAMES);
         if (propNameObject instanceof List)
         {
             writer.append(Constants.XSS_ERROR_FIELDS
                     + Constants.PROPERTY_NAMES_DELIMITER);
             final List<String> propertyNamesList = (List<String>) propNameObject;
             for (final String string : propertyNamesList)
             {
                 writer.append(string);
                 writer.append(Constants.PROPERTY_NAMES_DELIMITER);
             }
         }
         return isToExecuteAction;
     }
 
 	/**
 	 * @param request request
 	 * @return isXSSViolation
 	 * @throws Exception Exception
 	 */
 	private boolean isToExecuteXSSSafeAction(final HttpServletRequest request) throws Exception
     {
         boolean isXSSViolation = false;
 
         if (request.getAttribute(Constants.VIOLATING_PROPERTY_NAMES)
         	instanceof List && ((List<String>)request.getAttribute
         			(Constants.VIOLATING_PROPERTY_NAMES)).size()>0)
         {
             addXSSValidationError(request);
             isXSSViolation = true;
         }
         return isXSSViolation;
     }
 
 	/**
 	 * @param request request
 	 */
 	private void addXSSValidationError(final HttpServletRequest request)
     {
         final Object isToAddError = request.getAttribute("isToAddError");
         if (isToAddError instanceof Boolean
                 && ((Boolean) isToAddError).equals(Boolean.TRUE))
         {
             final ActionErrors errors;
             final Object actionErrosObject = request
                     .getAttribute("org.apache.struts.action.ERROR");
             if (actionErrosObject instanceof ActionErrors)
             {
                 errors = (ActionErrors) actionErrosObject;
             }
             else
             {
                 errors = new ActionErrors();
             }
             errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                     "errors.xssvulnerable"));
 
             saveErrors(request, errors);
             request.setAttribute("isToAddError", false);
         }
     }
 
 	/**
 	 * Check if it's redirected.
 	 * @param request request
 	 * @return true if redirected.
 	 */
 	private boolean getIsRedirected(final HttpServletRequest request)
     {
         boolean isRedirected = false;
         final Object redirectedObject = request
                 .getAttribute(Constants.PAGE_REDIRECTED);
         if (redirectedObject != null
                 && Constants.BOOLEAN_YES.equalsIgnoreCase(redirectedObject
                         .toString()))
         {
             isRedirected = true;
         }
 
         final Object errorObjects = request
                 .getAttribute("org.apache.struts.action.ERROR");
         if (!isRedirected)
         {
 
             isRedirected = setIsRedirected(isRedirected, errorObjects);
         }
         return isRedirected;
     }
 
 
 }
