 package edu.cmu.square.client.ui.FinalProductSelection;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.client.navigation.HistoryManager;
 import edu.cmu.square.client.navigation.Pilot;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.ui.core.TeachStepPane;
 import edu.cmu.square.client.ui.core.content.BreadCrumbMessages;
 
 
 public class FinalProductSelectionPilot extends Pilot
 {
 
 	public static class PageId
 	{
 		public static final String home = "teach";
 		public static final String matrix = "matrix-tecniques"; 
 		public static final String performTradeoffAnalysis = "final-product-selection";
 	}
 
 
 	public FinalProductSelectionPilot()
 	{
 		this.isStep=true;
 		this.STEP_DESCRIPTION="Step 7: Final product selection";
 	}
 	
 	
 	public Widget navigateTo(String pageId, State currentStateInformation)
 	{
 		//If user settings indicates to skip teach step, the target page will be the start page not the teach step 
 		if(currentStateInformation.getSkipTeachSetp() && pageId.equals(PageId.home))
 		 {
 			 pageId=PageId.matrix;
 		 }		
 		if (pageId.equals(PageId.home))
 		{
 			return new TeachStepPane(HistoryManager.ViewId.finalProductSelection, generateNavigationId(PageId.matrix));
 		}
 		else if (pageId.equals(PageId.matrix))
 		{
 			return new FinalProductSelectionPane(currentStateInformation); //new Label("Summary Page!");
 		}		
 		return null;
 	}
 
 	
 	
 	public void determineAccessRights(String page, State currentState)
 	{
 		
 		if (currentState.isSiteAdministrator() == true)
 		{
 			currentState.setMode(GwtModesType.ReadWrite);
 		}
 		else if (currentState.getUserProjectRole() == ProjectRole.Acquisition_Organization_Engineer)
 		{
 			currentState.setMode(GwtModesType.ReadWrite);
 		}
 			
 		/*
 //Delete this role, I just give him RW access right.
 		else if (currentState.getUserProjectRole() == ProjectRole.Lead_Requirements_Engineer)
 		{
 			currentState.setMode(GwtModesType.ReadWrite);
 		}
 		*/
 		else if (currentState.getUserProjectRole() == ProjectRole.Contractor)
 		{
 			currentState.setMode(GwtModesType.NoAccess);
 		}
 		else if (currentState.getUserProjectRole() == ProjectRole.Security_Specialist)
 		{
			currentState.setMode(GwtModesType.ReadOnly);
 		}
 		else if (currentState.getUserProjectRole() == ProjectRole.COTS_Vendor)
 		{
 			currentState.setMode(GwtModesType.ReadOnly);
 		}
 		else if (currentState.getUserProjectRole() == ProjectRole.Administrator)
 		{
 			currentState.setMode(GwtModesType.ReadWrite);
 		}
 		else
 		{
 			currentState.setMode(GwtModesType.ReadOnly);
 		}		
 	}
 
 	public String getBreadCrumb()
 	{
 		final BreadCrumbMessages messages = (BreadCrumbMessages) GWT.create(BreadCrumbMessages.class);
 		return messages.finalProductSelection();
 	}
 	
 
 	public static String generateNavigationId(String pageId)
 	{
 		return HistoryManager.ViewId.finalProductSelection + "/" + pageId;
 	}
 	
 }
