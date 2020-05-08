 package edu.cmu.square.client.ui.agreeOnDefinitions;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.model.AsquareCase;
 import edu.cmu.square.client.model.GwtAsquareCase;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.client.navigation.HistoryManager;
 import edu.cmu.square.client.navigation.Pilot;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.ui.core.TeachStepPane;
 import edu.cmu.square.client.ui.core.content.BreadCrumbMessages;
 
 /**
  *
  */
 public class AgreeOnDefinitionsPilot extends Pilot
 {
 
 	public static class PageId
 	{
 		public static final String home = "teach";
 		public static final String start = "summary";
 	}
 
 	
 	public AgreeOnDefinitionsPilot()
 	{
 		this.isStep=true;
 		this.STEP_DESCRIPTION="Step 1: Agree On Definitions";
 	}
 	
 
 	public Widget navigateTo(String pageId, State currentStateInformation)
 	{
 		
 		String step = HistoryManager.ViewId.agreeOnDefinitions;
 
 		//If user settings indicates to skip teach step, the target page will be the start page not the teach step 
 		if(currentStateInformation.getSkipTeachSetp() && pageId.equals(PageId.home))
 		 {
 			 pageId=PageId.start;
 		 }
 	
 		if (pageId.equals(PageId.home))
 		{
 			return new TeachStepPane(step, generateNavigationId(PageId.start));
 			
 		}
 		else if (pageId.equals(PageId.start))
 		{
 			return new AgreeOnDefinitionsPane(currentStateInformation);
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
 //Delete this!! SQUARE user
 			/*
 			else if (currentState.getUserProjectRole() == ProjectRole.Lead_Requirements_Engineer) 
 			{
 				currentState.setMode(GwtModesType.ReadOnly);
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
 		return messages.agreeOnDefinitions();
 	}
 
 	public static String generateNavigationId(String pageId)
 	{
 		return HistoryManager.ViewId.agreeOnDefinitions + "/" + pageId;
 	}
 
 
 }
