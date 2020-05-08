 /**
  * @author dgeorge
  * 
  * $Id: AdminCommentsAssignmentPopulateAction.java,v 1.3 2006-05-15 13:40:50 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.2  2005/10/24 13:28:17  georgeda
  * Cleanup changes
  *
  * Revision 1.1  2005/10/17 13:28:45  georgeda
  * Initial revision
  *
  * Revision 1.2  2005/10/10 14:10:48  georgeda
  * Changes for comment curation
  *
  * Revision 1.1  2005/09/19 19:53:51  georgeda
  * New model assignment functionality
  *
  * 
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Comments;
 import gov.nih.nci.camod.domain.CommentsSearchResult;
 import gov.nih.nci.camod.webapp.form.CurationAssignmentForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 import gov.nih.nci.common.persistence.Search;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * 
  * Used to populate the list of animal models needing action for the various
  * roles
  * 
  */
 public class AdminCommentsAssignmentPopulateAction extends BaseAction
 {
 
     /**
      * Action used to populate the various admin lists for the curation process
      */
     public ActionForward execute(ActionMapping inMapping,
                                  ActionForm inForm,
                                  HttpServletRequest inRequest,
                                  HttpServletResponse inResponse) throws Exception
     {
         log.trace("Entering execute");
         
 		String sID = inRequest.getHeader("Referer");
		String link = inRequest.getParameter("link");
     	
     	// prevents Referer Header injection
     	if ( sID != null && sID != "" && !sID.contains("camod")) {
     		return (inMapping.findForward("failure"));
     	}
 
         CurationAssignmentForm theForm = (CurationAssignmentForm) inForm;
 
         try
         {
             NewDropdownUtil.populateDropdown(inRequest, Constants.Dropdowns.CURATIONSTATESDROP, Constants.Admin.COMMENT_CURATION_WORKFLOW);
             
            // the user clicked the View Comment Assignment link
            if( link != null && link.equals("true")) {
        		theForm.setCurrentState(null);
        		inRequest.getSession().removeAttribute(Constants.ADMIN_COMMENTS_SEARCH_RESULTS);
        	}
            
            if (theForm.getCurrentState() == null)
         	{
         		saveToken(inRequest);
         	}
 
             if (theForm.getCurrentState() != null)
             {
             	if (!isTokenValid(inRequest)) {
         			return inMapping.findForward("failure");
         		}
             	
                 Comments theQBEComments = new Comments();
                 theQBEComments.setState(theForm.getCurrentState());
 
                 // Get the models
                 List theCommentsList = Search.query(theQBEComments);
 
                 // Create a display list
                 List<CommentsSearchResult> theDisplayList = new ArrayList<CommentsSearchResult>();
 
                 // Add Comment DTO's so that the paganation works quickly
                 for (int i = 0, j = theCommentsList.size(); i < j; i++)
                 {
                     Comments theComments = (Comments) theCommentsList.get(i);
                     theDisplayList.add(new CommentsSearchResult(theComments));
                 }
 
                 inRequest.getSession().setAttribute(Constants.ADMIN_COMMENTS_SEARCH_RESULTS, theDisplayList);
             }
         }
         catch (Exception e)
         {
             log.error("Unable to get comments for state: " + theForm.getCurrentState());
 
             // Encountered an error
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(inRequest, theMsg);
         }
 
         log.trace("Exiting execute");
 
         if (theForm.getCurrentState() != null)
         {
         	resetToken(inRequest);
         	saveToken(inRequest);
         }
 
         
         return inMapping.findForward("next");
     }
 }
