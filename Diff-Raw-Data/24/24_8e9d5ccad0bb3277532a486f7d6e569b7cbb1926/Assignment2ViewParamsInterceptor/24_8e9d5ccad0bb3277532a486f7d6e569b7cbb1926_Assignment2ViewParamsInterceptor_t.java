 package org.sakaiproject.assignment2.tool.params;
 
 import org.sakaiproject.assignment2.tool.beans.locallogic.LocalPermissionLogic;
 import org.sakaiproject.assignment2.tool.producers.AuthorizationFailedProducer;
 import org.sakaiproject.assignment2.tool.producers.AssignmentDetailProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentAssignmentListProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentSubmitProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentSubmitSummaryProducer;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 
 import uk.org.ponder.rsf.viewstate.AnyViewParameters;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsInterceptor;
 
 public class Assignment2ViewParamsInterceptor implements ViewParamsInterceptor {
 
 	private LocalPermissionLogic localPermissionLogic;
 	public void setLocalPermissionLogic(LocalPermissionLogic localPermissionLogic){
 		this.localPermissionLogic = localPermissionLogic;
 	}
 	
 	
 	public AnyViewParameters adjustViewParameters(ViewParameters incoming) {
 		if (AuthorizationFailedProducer.VIEWID.equals(incoming.viewID)) {
 			//Always return incoming if we are going to the Authorization Failed Page
 			return incoming;
 		}
 		
 		if (AssignmentDetailProducer.VIEW_ID.equals(incoming.viewID)) {
 			//This is a entitybroker "helper" that is always visible
 			//TODO make sure that this is always visible
 			return incoming;
 		}
 		
 		//If current user has permission access to requested view
 		if (localPermissionLogic.checkCurrentUserHasViewPermission(incoming.viewID)){
 			
 			//now do specific interceptions for student submission pages
 			//Student always has same link, redirect here based on it being open or not
 			if (StudentSubmitProducer.VIEW_ID.equals(incoming.viewID) 
 					|| StudentSubmitSummaryProducer.VIEW_ID.equals(incoming.viewID)) {
				
				//Returning newly generated view params causes issues (ASNN-53)
				String trueDestinationViewID = localPermissionLogic.filterViewIdForStudentSubmission((SimpleAssignmentViewParams)incoming);
				if (incoming.viewID.equals(trueDestinationViewID)) {
					return incoming;
				} else {
					return new SimpleAssignmentViewParams(trueDestinationViewID, ((SimpleAssignmentViewParams)incoming).assignmentId);
				}
 			}
 			
 			
 			return incoming;
 		} else if (localPermissionLogic.checkCurrentUserHasViewPermission(StudentAssignmentListProducer.VIEW_ID)) {
 			return new AssignmentListSortViewParams(StudentAssignmentListProducer.VIEW_ID);
 			
 		}
 		
 		return new SimpleViewParameters(AuthorizationFailedProducer.VIEWID);
 	}
 	
 }
