 package edu.cmu.square.server.remoteService.step.implementations;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Service;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewOfRequirementsByAcquisitionService;
 import edu.cmu.square.server.business.step.interfaces.ReviewOfRequirementsByAcquisitionBusiness;
 import edu.cmu.square.server.remoteService.implementations.SquareRemoteServiceServlet;
 
 @Service
 public class ReviewOfRequirementsByAcquisitionServiceImpl extends SquareRemoteServiceServlet implements ReviewOfRequirementsByAcquisitionService
 {
 		private static final long serialVersionUID = -4630780713009182467L;
 		@Resource
 		private ReviewOfRequirementsByAcquisitionBusiness reviewOfRequirementsByAcquisitionBusiness;
 
 		public void setValuesForAuthorization()
 		{
 			setValuesForAuthorization(reviewOfRequirementsByAcquisitionBusiness);
 			
 		}
 
 
 		
 
 		public List<GwtRequirement> getRequirements(GwtProject gwtProject) throws SquareException
 		{
 			
 			return	reviewOfRequirementsByAcquisitionBusiness.getRequirements(gwtProject);
 		}
 
 
 		public void updateRequirement(GwtProject gwtProject, GwtRequirement gwtRequirement) throws SquareException
 		{
				reviewOfRequirementsByAcquisitionBusiness.updateRequirement(gwtProject, gwtRequirement);
 		}
 		
 		
 		//Detail Pane for ASQUARE
 		public List<GwtRequirement> getRequirementsFromProject(Integer projectID) throws SquareException
 		{
 			
 			return reviewOfRequirementsByAcquisitionBusiness.getRequirementsFromProject(projectID);
 		}
 
 	
 		public void updateRequirement(GwtRequirement Requirement) throws SquareException
 		{
 			reviewOfRequirementsByAcquisitionBusiness.updateRequirement(Requirement);
 			
 		}
 
 
 
 		@Override
 		public void changeStatusToApproveRequirement(Integer projectId, GwtRequirement gwtRequirement) throws SquareException
 		{
 			reviewOfRequirementsByAcquisitionBusiness.changeStatusToApproveRequirement(projectId, gwtRequirement);
 			
 		}
 
 
 		@Override
 		public void changeStatusToRequestRevisionRequirement(Integer projectId, GwtRequirement gwtRequirement) throws SquareException
 		{
 			reviewOfRequirementsByAcquisitionBusiness.changeStatusToRequestRevisionRequirement(projectId, gwtRequirement);
 			
 		}
 
 		
 		
 		
 
 		public void removeRequirement(GwtRequirement gwtRequirement) throws SquareException
 		{
 				reviewOfRequirementsByAcquisitionBusiness.removeRequirement(gwtRequirement);
 			
 		}
 		
 		public GwtRequirement addRequirement(GwtProject gwtProject, GwtRequirement gwtRequirement) throws SquareException
 		{
 		 return	reviewOfRequirementsByAcquisitionBusiness.addRequirement(gwtProject, gwtRequirement);
 		
 		}
 		/*
 		public void assignRequirementsToCategory(List<GwtRequirement> requirements,int categoryID) throws SquareException
 		{
 			reviewOfRequirementsByAcquisitionBusiness.assignRequirementsToCategory(requirements,categoryID);
 		}
 
 		
 		public void removeRequirementsFromCategory(List<GwtRequirement> requirements, int categoryID) throws SquareException
 		{
 			reviewOfRequirementsByAcquisitionBusiness.removeRequirementsFromCategory(requirements,categoryID);
 			
 		}
 
 */
 		public void deleteRequirement(Integer requirementId, Integer projectId) throws SquareException
 		{
 			reviewOfRequirementsByAcquisitionBusiness.deleteRequirement(requirementId, projectId);
 			
 		}
 
 
 		public Integer addRequirementToProject(Integer projectId, GwtRequirement Requirement) throws SquareException
 		{
 			return reviewOfRequirementsByAcquisitionBusiness.addRequirementToProject(projectId, Requirement);
 			
 		}
 
 		
 }
