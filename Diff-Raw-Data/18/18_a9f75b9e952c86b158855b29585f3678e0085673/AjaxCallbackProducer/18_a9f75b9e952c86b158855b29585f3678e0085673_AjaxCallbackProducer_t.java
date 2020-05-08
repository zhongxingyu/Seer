 package org.sakaiproject.assignment2.tool.producers.fragments;
 
 import java.util.Set;
 import java.util.HashSet;
 
 import org.sakaiproject.assignment2.tool.params.AjaxCallbackViewParams;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
 import uk.org.ponder.rsf.content.ContentTypeReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 public class AjaxCallbackProducer implements ViewComponentProducer, ViewParamsReporter, ContentTypeReporter {
 	
 	public static final String VIEW_ID = "ajax-callback";
     public String getViewID() {
         return VIEW_ID;
     }
     
     private String[] assignmentIdParam;
    
     private AssignmentLogic assignmentLogic;
     
     private SessionManager sessionManager;
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
     
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
     	AjaxCallbackViewParams params = (AjaxCallbackViewParams) viewparams;
     	
     	//First check if we have parameters passed
     	if (params.sortable != null){
     		//get String parameters
     		assignmentIdParam = (String[]) params.sortable;
     		// create an array of longs to hold the ids
     		Long[] assignmentIds = new Long[assignmentIdParam.length]; 
     	    for (int i=0; i < assignmentIdParam.length; i++){
     	    	//now remember, the param ids are actually xhtml ids, and begin
     	    	// with the string "li_", therefore we have to first get the
     	    	// string after the li_, then convert it to a long
     	    	assignmentIds[i] = Long.valueOf(assignmentIdParam[i].substring(3));
     	    }
 	    	assignmentLogic.setAssignmentSortIndexes(assignmentIds);
 	    } else if (params.removeAttachment && params.refId != null && !params.refId.equals("")) {
 	    	Set<String> set = new HashSet();
 	    	ToolSession session = sessionManager.getCurrentToolSession();
 	    	if (session.getAttribute("removedAttachmentRefs") != null) {
 	    		set.addAll((Set)session.getAttribute("removedAttachmentRefs"));
 	    	}
 	  
 	    	//params.refId = org.sakaiproject.util.Web.unEscapeHtml(params.refId);
 	    	set.add(params.refId);
 	    	session.setAttribute("removedAttachmentRefs", set);
 	    }
     }
     
     public ViewParameters getViewParameters(){
     	return new AjaxCallbackViewParams();
     }
     
     public String getContentType() {
         return ContentTypeInfoRegistry.AJAX;
     }
     
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
     	this.assignmentLogic = assignmentLogic;
     }
 
 }
