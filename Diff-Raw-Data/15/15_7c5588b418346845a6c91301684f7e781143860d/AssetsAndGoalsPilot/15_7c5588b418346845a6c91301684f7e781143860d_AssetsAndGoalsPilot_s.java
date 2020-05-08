 package edu.cmu.square.client.ui.assetsAndGoals;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.model.AsquareCase;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.client.navigation.HistoryManager;
 import edu.cmu.square.client.navigation.Pilot;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.ui.agreeOnDefinitions.AgreeOnDefinitionsPilot.PageId;
 import edu.cmu.square.client.ui.core.TeachStepPane;
 import edu.cmu.square.client.ui.core.content.BreadCrumbMessages;
 
 
 /**
  *
  */
 public class AssetsAndGoalsPilot extends Pilot
 {
 	
 	public static class PageId
 	{
 		public static final String home = "teach";
 		public static final String start = "business-goal";
 		public static final String summary = "summary";
 		public static final String addSubGoalsAndAssets = "add-sub-goals-and-assets";
 		public static final String assetGoalAssociation = "asset-goal-association";
 		public static final String assetGoalSummary = "asset-goal-summary";
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see edu.cmu.square.client.entryPoint.Pilot#navigateTo(java.lang.String, edu.cmu.square.client.entryPoint.State)
 	 */
 	public AssetsAndGoalsPilot()
 	{
 		this.isStep=true;
 		this.STEP_DESCRIPTION="Step 2: Identify Assets and Goals";
 	}
 	
 	public Widget navigateTo(String pageId, State currentStateInformation)
 	{	
 		
 		//If user settings indicates to skip teach step, the target page will be the start page not the teach step 
 		if(currentStateInformation.getSkipTeachSetp() && pageId.equals(PageId.home)&&currentStateInformation.getMode().equals(GwtModesType.ReadOnly))
 		 {
 			 pageId=PageId.summary;
 		 }
 		else if (currentStateInformation.getSkipTeachSetp() && pageId.equals(PageId.home)&&currentStateInformation.getMode().equals(GwtModesType.ReadWrite))
 		{
 			 pageId=PageId.start;
 		}
 		
 		
 		if (pageId.equals(PageId.home)) 
 		{
 		
 			String targetId = "";
 			if (currentStateInformation.getMode().equals(GwtModesType.ReadOnly))
 			{
 				targetId = generateNavigationId(PageId.summary);
 			}
 			else if (currentStateInformation.getMode().equals(GwtModesType.ReadWrite))
 			{
 				targetId = generateNavigationId(PageId.start);
 			}
 			
 			return new TeachStepPane(HistoryManager.ViewId.assetsAndGoals, targetId);
 		}
 		
 		else if (pageId.equals(PageId.start)) 
 		{
 			return new IdentifyAssetsAndGoalsPane(currentStateInformation);
 		}
 		else if (pageId.equals(PageId.summary)) 
 		{
 			return new IdentifyAssetsAndGoalsSummaryPane(currentStateInformation);
 		}
 		else if (pageId.equals(PageId.addSubGoalsAndAssets)) 
 		{
 			return new IdentifyAssetsAndGoalsSubGoalsPane(currentStateInformation);
 		}
 		else if (pageId.equals(PageId.assetGoalAssociation)) 
 		{
 			return new AssociateAssetsWithGoalsPane(currentStateInformation);
 		}
 		else if(pageId.equals(PageId.assetGoalSummary))
 		{
 			return new IdentifyAssetsAndGoalsSummaryPane(currentStateInformation);
 		}
 		
 		return null;
 	}
 
 	
 	public void determineAccessRights(String page, State currentState)
 	{
 		//case1
		if(currentState.getCaseName() == AsquareCase.Case1)
 		{
 			if (currentState.getUserProjectRole() == ProjectRole.Acquisition_Organization_Engineer)
 			{
 				currentState.setMode(GwtModesType.ReadWrite);
 			}		
 //Delete this!! SQUARE user
 			/*
 			else if (currentState.getUserProjectRole() == ProjectRole.Lead_Requirements_Engineer) 
 			{
 				currentState.setMode(GwtModesType.ReadWrite);
 			}
 			*/
 			else if (currentState.getUserProjectRole() == ProjectRole.Contractor)
 			{
 				currentState.setMode(GwtModesType.ReadOnly);
 			}
 			else if (currentState.getUserProjectRole() == ProjectRole.Security_Specialist)
 			{
 				currentState.setMode(GwtModesType.NoAccess);
 			}
 			else if (currentState.getUserProjectRole() == ProjectRole.COTS_Vendor)
 			{
 				currentState.setMode(GwtModesType.NoAccess);
 			}
 			else if (currentState.getUserProjectRole() == ProjectRole.None)
 			{
 				currentState.setMode(GwtModesType.NoAccess);
 			}
 			else 
 			{
 				currentState.setMode(GwtModesType.ReadOnly);
 			}
 		}
		else if(currentState.getCaseName() == AsquareCase.Case3)
 		{
 			if (currentState.getUserProjectRole() == ProjectRole.Acquisition_Organization_Engineer)
 			{
 				currentState.setMode(GwtModesType.ReadOnly);
 			}		
 			/*
 //Delete this!! SQUARE user
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
 				currentState.setMode(GwtModesType.NoAccess);
 			}
 			else if (currentState.getUserProjectRole() == ProjectRole.COTS_Vendor)
 			{
 				currentState.setMode(GwtModesType.NoAccess);
 			}
 			else if (currentState.getUserProjectRole() == ProjectRole.None)
 			{
 				currentState.setMode(GwtModesType.NoAccess);
 			}
 			else 
 			{
 				currentState.setMode(GwtModesType.ReadOnly);
 			}
 		}
 		
 	}
 	
 	public String getBreadCrumb()
 	{
 		final BreadCrumbMessages messages = (BreadCrumbMessages) GWT.create(BreadCrumbMessages.class);
 		return messages.assetsAndGoals();
 	}
 	
 	
 	
 	public static String generateNavigationId(String pageId)
 	{
 		return HistoryManager.ViewId.assetsAndGoals + "/" + pageId;
 	}
 
 	
 }
