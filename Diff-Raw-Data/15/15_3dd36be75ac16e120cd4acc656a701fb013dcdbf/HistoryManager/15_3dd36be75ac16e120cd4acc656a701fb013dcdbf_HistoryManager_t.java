 package edu.cmu.square.client.navigation;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.remoteService.interfaces.StepService;
 import edu.cmu.square.client.remoteService.interfaces.StepServiceAsync;
 import edu.cmu.square.client.ui.ChooseStep.ChooseStepPilot;
 import edu.cmu.square.client.ui.ChooseStepCase3.ChooseStepCase3Pilot;
 import edu.cmu.square.client.ui.FinalProductSelection.FinalProductSelectionPilot;
 import edu.cmu.square.client.ui.ManageProject.ManageProjectPilot;
 import edu.cmu.square.client.ui.ManageSite.ManageSitePilot;
 import edu.cmu.square.client.ui.SelectSecurityTechnique.SelectSecurityElicitationTechniquePilot;
 import edu.cmu.square.client.ui.agreeOnDefinitions.AgreeOnDefinitionsPilot;
 import edu.cmu.square.client.ui.assetsAndGoals.AssetsAndGoalsPilot;
 import edu.cmu.square.client.ui.categorizeRequirements.CategorizeRequirementsPilot;
 import edu.cmu.square.client.ui.chooseCase.ChooseCasePilot;
 import edu.cmu.square.client.ui.chooseProject.HomePilot;
 import edu.cmu.square.client.ui.collectArtifacts.CollectArtifactsPilot;
 import edu.cmu.square.client.ui.core.AccessDeniedPane;
 import edu.cmu.square.client.ui.core.NotFoundPane;
 import edu.cmu.square.client.ui.core.content.BreadCrumbMessages;
 import edu.cmu.square.client.ui.elicitSecurityRequirements.ElicitSecurityRequirementsPilot;
 import edu.cmu.square.client.ui.inspectRequirements.InspectRequirementsPilot;
 import edu.cmu.square.client.ui.performTradeoffAnalysis.PerformTradeoffAnalysisPilot;
 import edu.cmu.square.client.ui.prioritizeRequirements.PrioritizeRequirementsPilot;
 import edu.cmu.square.client.ui.reviewAndFinalizeRequirements.ReviewAndFinalizeRequirementsPilot;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPilot;
 import edu.cmu.square.client.ui.reviewPackages.ReviewPackagesPilot;
 import edu.cmu.square.client.ui.risksAssessment.RiskAssessmentPilot;
 
 /**
  * Manages the history and all incoming links in the system. To navigate using a
  * link, simply add a history token to the history of the system. Example:
  * History.newItem(historyToken);
  * 
  * History tokens will use the following convention. ViewID/uniquePageId
  * 
  * The ViewID enumeration determines which navigator is used to navigate to the
  * new page. The uniquePageId is a unique identifier for the page being
  * navigated to. This will usually come from the settings class within the
  * Navigator for that ViewId.
  * 
  */
 public class HistoryManager implements ValueChangeHandler<String>
 {
 
 	private Panel contentPane = null;
 	private Panel breadCrumbPane = null;
 	private State currentState = null;
 
 	private Pilot homePilot = new HomePilot();
 	private Pilot manageSitePilot = new ManageSitePilot();
 	private Pilot manageProjectPilot = new ManageProjectPilot();
 	private Pilot assetsAndGoalsPilot = new AssetsAndGoalsPilot();
 	private Pilot selectSecurityElicitationTechniquePilot = new SelectSecurityElicitationTechniquePilot();
 	private Pilot riskAssessmentPilot = new RiskAssessmentPilot();
 	private Pilot elicitSecurityRequirementsPilot = new ElicitSecurityRequirementsPilot();
 	private Pilot definitionsPilot = new AgreeOnDefinitionsPilot();
 	private Pilot priorityPilot = new PrioritizeRequirementsPilot();
 	private Pilot categoryPilot = new CategorizeRequirementsPilot();
 	private Pilot artifactsPilot = new CollectArtifactsPilot();
 	private Pilot inspectionPilot = new InspectRequirementsPilot();
 	private Pilot chooseStepPilot = new ChooseStepPilot();
 	private Pilot chooseStepCase3Pilot = new ChooseStepCase3Pilot();
 	private Pilot chooseCasePilot = new ChooseCasePilot();
 	private BreadCrumbMessages messages = (BreadCrumbMessages)GWT.create(BreadCrumbMessages.class);
 
 	private Pilot reviewOfRequirementsByAcquisitionOrganizationPilot = new ReviewOfRequirementsByAcquisitionPilot();
 	private Pilot reviewPackagesPilot = new ReviewPackagesPilot();
 	private Pilot performTradeoffAnalysisPilot = new PerformTradeoffAnalysisPilot();
 	private Pilot reviewAndFinalizeRequirementsPilot = new ReviewAndFinalizeRequirementsPilot();
 	private Pilot finalProductSelectionPilot = new FinalProductSelectionPilot();
 	
 	 
 	public static class ViewId
 	{
 		public static final String home = "chooseProject";
 		public static final String chooseCase = "chooseCase";
 		public static final String selectSecurityElicitationTechnique = "select-security-elicitation-technique";
 		public static final String manageProject = "project";
 		public static final String manageSite = "site";
 		public static final String assetsAndGoals = "goals";
 		public static final String riskAssessment = "risk-assessment";
 		public static final String elicitSecurityRequirements = "elicit-requirements";
 		public static final String agreeOnDefinitions = "agree-on-definitions";
 		public static final String prioritizeRequirements = "prioritize";
 		public static final String categorizeRequirements = "categorize";
 		public static final String collectArtifacts = "artifacts";
 		public static final String inspectRequirements = "inspect";
 		public static final String chooseStep = "chooseStep";
 		public static final String reviewOfRequirementsByAcquisitionOrganization = "reviewOfRequirementsByAcquisitionOrganization";
 		public static final String chooseStepCase3 = "chooseStepCase3";
 		public static final String reviewPackages = "reviewPackages";
 		public static final String performTradeoffAnalysis = "performTradeoffAnalysis";
 		public static final String reviewAndFinalizeRequirements = "reviewAndFinalizeRequirements";
 		public static final String finalProductSelection = "finalProductSelection";
 	}
 
 	public HistoryManager(Panel contentPane, Panel breadCrumbPane, State stateInfo)
 	{
 		this.contentPane = contentPane;
 		this.currentState = stateInfo;
 		this.breadCrumbPane = breadCrumbPane;
 	}
 
 	public void onValueChange(ValueChangeEvent<String> event)
 	{
 
 		this.forceNavigation(removeQueryString(event.getValue()));
 	}
 
 	public void forceNavigation(String destination)
 	{
 		//boolean isStepClosed = false;
 
 		// if you aren't authenticated, then force you to go to the login page.
 		if (!this.currentState.isAuthenticated())
 		{
 			destination = ChooseCasePilot.generateNavigationId(ChooseCasePilot.PageId.login);
 		}
 
 		currentState.setCurrentView(destination);
 
 		String view = this.findViewId(destination);
 		String page = this.findPageId(destination);
 
 		// Get the right pilot to get the right content to show next.
 		Pilot currentPilot = this.getPilot(view);
 		if (currentPilot == null)
 		{
 			this.swapContent(this.show404Error());
 			return;
 		}
 		
 		// Set the current mode to determine whether the user has privelages to
 		// access the page they want to go to.
 		currentPilot.determineAccessRights(page, this.currentState);
 
 		applyCloseStepPolicy(currentPilot, view, page);
 
 	}
 
 	public void applyCloseStepPolicy(final Pilot currentPilot,final String view, final String page)
 	{
 		if (currentPilot.isStep)
 		{
 			StepServiceAsync stepService = GWT.create(StepService.class);
 			stepService.isStepClosed(currentPilot.STEP_DESCRIPTION, currentState.getProjectID(), new AsyncCallback<Boolean>()
 			{
 				@Override
 				public void onFailure(Throwable caught)
 				{
 					// TODO Auto-generated method stub
 				}
 				@Override
 				public void onSuccess(Boolean closed)
 				{
 					if (closed)
 					{
 						currentState.setMode(GwtModesType.ReadOnly);
 					}
 					swapContent(currentPilot, view, page);
 				}
 			});
 		}
 		else
 		{
 			swapContent(currentPilot, view, page);
 		}
 	}
 	
 	public void swapContent(Pilot currentPilot,String view, String page)
 	{
 		Widget content = null;
 
 		// If they don't have access, we're not even going to bother showing
 		// them the page.
 		if (this.currentState.getMode() == GwtModesType.NoAccess)
 		{
 			content = this.showDenied();
 		}
 		else
 		{
 			content = currentPilot.navigateTo(page, this.currentState);
 		}
 
 		// If the content is still null by this point, someone made an error and
 		// we show the 404.
 		if (content == null)
 		{
 			content = this.show404Error();
 		}
 
 		this.swapContent(content);
 
 		this.setBreadCrumb(view, currentPilot.getBreadCrumb());
 	}
 	
 	public static String generateNavigationId(String viewId, String pageId)
 	{
 		return viewId + "/" + pageId;
 	}
 
 	public void setBreadCrumb(String historyToken)
 	{
 		String viewId = this.findViewId(historyToken);
 		setBreadCrumb(viewId, this.getPilot(viewId).getBreadCrumb());
 	}
 	
 	public void setBreadCrumb(String viewId, String crumb)
 	{
 		this.breadCrumbPane.clear();
 	
 		Hyperlink chooseCase = new Hyperlink(messages.chooseCase(), ChooseCasePilot.generateNavigationId(ChooseCasePilot.PageId.home));
 		HorizontalPanel crumbBar = new HorizontalPanel();
 		Label crumbText = new Label(crumb);
 		
 		
 		crumbText.setStyleName("square-crumb");
 		chooseCase.setStyleName("square-crumb");
 		
 		
 		if (crumb.trim().length()!=0) //Bread crumb text is not empty.
 		{
 			//System.out.println("asdfasdf....."+this.currentState.getProjectName());
 			Hyperlink chooseStep = new Hyperlink(this.currentState.getProjectName(), ChooseStepPilot.generateNavigationId(ChooseStepPilot.PageId.home));
 			Hyperlink chooseStepCase3 = new Hyperlink(this.currentState.getProjectName(), ChooseStepCase3Pilot.generateNavigationId(ChooseStepCase3Pilot.PageId.home));
 			
 			chooseStep.setStyleName("square-crumb");
			chooseStepCase3.setStyleName("square-crumb");
 			crumbBar.add(chooseCase);
 			System.out.println("chooseCase"+chooseCase);
 			
 			//Hyperlink chooseProject = new Hyperlink(this.currentState.getCaseName(), HomePilot.generateNavigationId(HomePilot.PageId.home));
 			
 			Hyperlink chooseProject = new Hyperlink(this.currentState.getCaseName(), HomePilot.generateNavigationId(HomePilot.PageId.home));
 			chooseProject.setStyleName("square-crumb");
 		
 			
 			if(this.currentState.getProjectName()!="none") //empty value of project
 			 { 
 				crumbBar.add(new Label(" > "));
				System.out.println("aaaa   ........."+this.currentState.getProjectName()+"   id..."+this.currentState.getCaseID());
 				crumbBar.add(chooseProject);
				
 				crumbBar.add(new Label(" > "));
 				if(this.currentState.getCaseID()==1)
 					crumbBar.add(chooseStep);
 				else if(this.currentState.getCaseID()==3)
 					crumbBar.add(chooseStepCase3);
				
				
 				//else crumbBar.add("fuck you");
 			 }
 			
 			crumbBar.add(new Label(" > "));
 			crumbBar.add(crumbText);
 			crumbBar.setSpacing(4);
 
 		}
 		this.breadCrumbPane.add(crumbBar);
 	}
 	
 
 	private String findViewId(String historyToken)
 	{
 		return historyToken.split("/")[0];
 	}
 
 	private String findPageId(String historyToken)
 	{
 		String[] tokens = historyToken.split("/");
 		if (tokens.length == 2)
 		{
 			return tokens[1];
 		}
 
 		return "";
 	}
 
 	// This methods remove the query string parameter
 	// Because the history manager will not find the views
 	private String removeQueryString(String historyToken)
 	{
 		if (historyToken.contains("?"))
 		{
 			int index = historyToken.indexOf('?');
 			historyToken = historyToken.substring(0, index);
 
 		}
 		return historyToken;
 	}
 
 	private void swapContent(Widget content)
 	{
 		this.contentPane.clear();
 		this.contentPane.add(content);
 	}
 
 	private Widget show404Error()
 	{
 		return new NotFoundPane(this.currentState);
 	}
 
 	private Widget showDenied()
 	{
 		return new AccessDeniedPane(this.currentState);
 	}
 
 	private Pilot getPilot(String view)
 	{
 		if (ViewId.home.equals(view))
 		{
 			return this.homePilot;
 		}
 		else if (ViewId.selectSecurityElicitationTechnique.equals(view))
 		{
 			return this.selectSecurityElicitationTechniquePilot;
 		}
 		else if (ViewId.manageProject.endsWith(view))
 		{
 			return this.manageProjectPilot;
 		}
 		else if (ViewId.manageSite.equals(view))
 		{	
 			return this.manageSitePilot;
 		}
 		else if (ViewId.assetsAndGoals.equals(view))
 		{
 			return this.assetsAndGoalsPilot;
 		}
 		else if (ViewId.riskAssessment.equals(view))
 		{
 			return this.riskAssessmentPilot;
 		}
 		else if (ViewId.elicitSecurityRequirements.equals(view))
 		{
 			return this.elicitSecurityRequirementsPilot;
 		}
 		else if (ViewId.agreeOnDefinitions.equals(view))
 		{
 			return this.definitionsPilot;
 		}
 		else if (ViewId.prioritizeRequirements.equals(view))
 		{
 			return this.priorityPilot;
 		}
 		else if (ViewId.categorizeRequirements.equals(view))
 		{
 			return this.categoryPilot;
 		}
 		else if (ViewId.collectArtifacts.equals(view))
 		{
 			return this.artifactsPilot;
 		}
 		else if (ViewId.inspectRequirements.equals(view))
 		{
 			return this.inspectionPilot;
 		}
 		else if (ViewId.chooseStep.equals(view))
 		{
 			return this.chooseStepPilot;
 		}
 		else if (ViewId.chooseStepCase3.equals(view))
 		{
 			return this.chooseStepCase3Pilot;
 		}
 		else if (ViewId.chooseCase.equals(view))
 		{
 			return this.chooseCasePilot;
 		}
 		else if (ViewId.reviewOfRequirementsByAcquisitionOrganization.equals(view)){
 			return this.reviewOfRequirementsByAcquisitionOrganizationPilot;
 		}
 		else if (ViewId.reviewPackages.equals(view)){
 			return this.reviewPackagesPilot;
 		}
 		else if(ViewId.performTradeoffAnalysis.equals(view)){
 			return this.performTradeoffAnalysisPilot;
 		}
 		else if (ViewId.reviewAndFinalizeRequirements.equals(view)){
 			return this.reviewAndFinalizeRequirementsPilot;
 		}
 		else if (ViewId.finalProductSelection.equals(view)){
 			return this.finalProductSelectionPilot ;
 		}
 		
 		else
 		{
 			return null;
 		}
 	}
 	
 
 }
