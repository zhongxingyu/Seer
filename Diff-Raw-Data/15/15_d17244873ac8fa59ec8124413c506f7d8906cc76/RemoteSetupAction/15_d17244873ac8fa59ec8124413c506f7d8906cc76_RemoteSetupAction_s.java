 package gov.nih.nci.cagwas.web.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  * The RemoteSetupAction class is used to get the remote content for any remote
  * plone page using a parameter flag to determine which to get.
  * <P>
  * @author mholck
  * @see org.apache.struts.action.Action
  */
 public class RemoteSetupAction extends Action
 {
 	/**
 	 * execute is called when this action is posted to
 	 * <P>
 	 * @param mapping The ActionMapping for this action as configured in struts
 	 * @param form The ActionForm that posted to this action if any
 	 * @param request The HttpServletRequest for the current post
 	 * @param response The HttpServletResponse for the current post
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception
 	{
 		ActionForward forward = null;
 		String content = (String)request.getParameter("content");
 		String remoteContent = null;
 		
 		RemoteContentHelper rcHelp = new RemoteContentHelper();
 		
 		// Get the remote static content
 		if ((content != null) && (content.equalsIgnoreCase("study1")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.study1");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("study1Content", remoteContent);
 				forward = mapping.findForward("study1");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("study2")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.study2");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("study2Content", remoteContent);
 				forward = mapping.findForward("study2");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("glossary")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.glossary");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("glossaryContent", remoteContent);
 				forward = mapping.findForward("glossary");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("contacts")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.contacts");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("contactContent", remoteContent);
 				forward = mapping.findForward("contacts");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("policies")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.policies");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("policiesContent", remoteContent);
 				forward = mapping.findForward("policies");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("cite")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.cite");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("citeContent", remoteContent);
 				forward = mapping.findForward("cite");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("citeExample")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.citeExample");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("citeExampleContent", remoteContent);
 				forward = mapping.findForward("citeExample");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("access")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.access");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("accessContent", remoteContent);
 				forward = mapping.findForward("access");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("openAccess")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.openAccess");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("openContent", remoteContent);
 				forward = mapping.findForward("openAccess");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
 		else if ((content != null) && (content.equalsIgnoreCase("controlledAccess")))
 		{
 			remoteContent = rcHelp.getRemoteContent("remote.controlledAccess");
 			
 			if (remoteContent != null)
 			{
 				request.setAttribute("controlledContent", remoteContent);
 				forward = mapping.findForward("controlledAccess");
 			}
 			else
 				forward = mapping.findForward("ploneError");
 		}
		
 		return forward;
 	}
 
 }
