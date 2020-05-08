 package edu.cmu.square.client.ui.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.navigation.HistoryManager;
 import edu.cmu.square.client.ui.FinalProductSelection.FinalProductSelectionMessages;
 import edu.cmu.square.client.ui.SelectSecurityTechnique.SelectSecurityTechniqueMessages;
 import edu.cmu.square.client.ui.agreeOnDefinitions.AgreeOnDefinitionsMessages;
 import edu.cmu.square.client.ui.assetsAndGoals.IdentifyAssetsAndGoalsMessages;
 import edu.cmu.square.client.ui.categorizeRequirements.CategorizeRequirementsMessages;
 import edu.cmu.square.client.ui.collectArtifacts.CollectArtifactsMessages;
 import edu.cmu.square.client.ui.core.content.teachStep.TeachMessages;
 import edu.cmu.square.client.ui.core.content.teachStep.TeachStepMessages;
 import edu.cmu.square.client.ui.elicitSecurityRequirements.ElicitSecurityRequirementsMessages;
 import edu.cmu.square.client.ui.inspectRequirements.InspectRequirementsMessages;
 import edu.cmu.square.client.ui.performTradeoffAnalysis.PerformTradeoffAnalysisMessages;
 import edu.cmu.square.client.ui.prioritizeRequirements.PrioritizeMessages;
 import edu.cmu.square.client.ui.reviewAndFinalizeRequirements.ReviewAndFinalizeRequirementsMessages;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionMessages;
 import edu.cmu.square.client.ui.reviewPackages.ReviewPackagesMessages;
 import edu.cmu.square.client.ui.risksAssessment.SecurityRiskAssessmentMessages;
 
 
 public class TeachStepPane extends Composite
 {
 	private VerticalPanel mainLayout = null;
 	private TeachStepMessages pageMessages = null;
 	
 	/**
 	 * 
 	 * @param stepToTeach The ViewId of the step to teach
 	 * @param nextPage A generated navigation ID for the page to be called once the user is finished with this page.
 	 */
 	public TeachStepPane(String stepToTeach, final String nextPage)
 	{
 		super();
 		
 		pageMessages = (TeachStepMessages)GWT.create(TeachStepMessages.class);
 		
 		mainLayout = new VerticalPanel();
 		mainLayout.setStyleName("teach-step");
 		
 		TeachMessages messages = null;
 		
 		List<Roles> responsibilities = new ArrayList<Roles>();
 		List<String> contents = new ArrayList<String>();
 		
 		if (stepToTeach.equals(HistoryManager.ViewId.assetsAndGoals))
 		{
 			messages = (IdentifyAssetsAndGoalsMessages)GWT.create(IdentifyAssetsAndGoalsMessages.class);
 			
 			
 			responsibilities.add(Roles.Acquisition_Organization_Engineer);
 			contents.add(messages.aoeResponsibilities());
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.selectSecurityElicitationTechnique))
 		{
 			messages = (SelectSecurityTechniqueMessages)GWT.create(SelectSecurityTechniqueMessages.class);
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.riskAssessment))
 		{
 			messages = (SecurityRiskAssessmentMessages)GWT.create(SecurityRiskAssessmentMessages.class);
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.elicitSecurityRequirements))
 		{
 			messages = (ElicitSecurityRequirementsMessages)GWT.create(ElicitSecurityRequirementsMessages.class);
 			responsibilities.add(Roles.Contractor);
 			responsibilities.add(Roles.Security_Specialist);
 			
 			contents.add(messages.contractorResponsibilities());
 			contents.add(messages.securityResponsibilities());
 		}
 		/*else if (stepToTeach.equals(HistoryManager.ViewId.categorizeRequirements))
 		{
 			messages = (CategorizeRequirementsMessages)GWT.create(CategorizeRequirementsMessages.class);
 		}
 		*/
 		else if (stepToTeach.equals(HistoryManager.ViewId.agreeOnDefinitions))
 		{
 			messages = (AgreeOnDefinitionsMessages)GWT.create(AgreeOnDefinitionsMessages.class);
 			
 			
 			responsibilities.add(Roles.Acquisition_Organization_Engineer);
			
 			contents.add(messages.aoeResponsibilities());
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.prioritizeRequirements))
 		{
 			messages = (PrioritizeMessages)GWT.create(PrioritizeMessages.class);
 		}
 		/*
 		else if (stepToTeach.equals(HistoryManager.ViewId.collectArtifacts))
 		{
 			messages = (CollectArtifactsMessages)GWT.create(CollectArtifactsMessages.class);
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.inspectRequirements))
 		{
 			messages = (InspectRequirementsMessages)GWT.create(InspectRequirementsMessages.class);
 		}
 		*/
 		else if (stepToTeach.equals(HistoryManager.ViewId.reviewOfRequirementsByAcquisitionOrganization))
 		{
 			messages = (ReviewOfRequirementsByAcquisitionMessages)GWT.create(ReviewOfRequirementsByAcquisitionMessages.class);
 			
 			responsibilities.add(Roles.Acquisition_Organization_Engineer);
 			contents.add(messages.aoeResponsibilities());
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.reviewPackages))
 		{
 			messages = (ReviewPackagesMessages)GWT.create(ReviewPackagesMessages.class);
 			responsibilities.add(Roles.COTS_Vendor);
 			responsibilities.add(Roles.Security_Specialist);
 		
 			contents.add(messages.cotsResponsibilities());
 			contents.add(messages.securityResponsibilities());
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.reviewAndFinalizeRequirements))
 		{
 			messages = (ReviewAndFinalizeRequirementsMessages)GWT.create(ReviewAndFinalizeRequirementsMessages.class);
 	
 			responsibilities.add(Roles.Security_Specialist);
 	
 			contents.add(messages.securityResponsibilities());
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.performTradeoffAnalysis))
 		{
 			messages = (PerformTradeoffAnalysisMessages)GWT.create(PerformTradeoffAnalysisMessages.class);
 			
 			responsibilities.add(Roles.Security_Specialist);
 			
 			contents.add(messages.securityResponsibilities());
 		}
 		else if (stepToTeach.equals(HistoryManager.ViewId.finalProductSelection))
 		{
 			messages = (FinalProductSelectionMessages)GWT.create(FinalProductSelectionMessages.class);
 			
 			responsibilities.add(Roles.Acquisition_Organization_Engineer);
 			
 			contents.add(messages.aoeResponsibilities());
 		}		
 		else
 		{
 			History.newItem(nextPage); //this should be a 404 page maybe?
 		}
 		
 		this.mainLayout.add(this.createTitle(messages.title()));
 		this.mainLayout.add(this.createPurpose(messages.purpose()));
 		this.mainLayout.add(this.createResponsibilities(responsibilities, contents));
 		this.mainLayout.add(this.createExitCriteria(messages.exitCriteria()));
 		this.mainLayout.add(this.createWebisteInfo(messages.webHint()));
 		
 		Button goButton = new Button(pageMessages.nextStep());
 		goButton.setHeight("50px");
 		goButton.setWidth("200px");
 		goButton.addClickHandler(new ClickHandler()
 		{
 			
 			public void onClick(ClickEvent event)
 			{
 				History.newItem(nextPage);
 			}	
 		});
 		
 		mainLayout.add(goButton);
 		mainLayout.setWidth("100%");
 		
 		initWidget(mainLayout);
 	}
 
 	
 	private Widget createTitle(String title)
 	{
 		Label titleLabel = new Label(title);
 		titleLabel.setStyleName("square-title");
 		return titleLabel;
 	}
 	
 	private Widget createPurpose(String content)
 	{
 		String style = "teach-step-purpose-content";
 		String title = this.pageMessages.purposeLabel();
 		
 		return this.setLayout(title, content, style);
 	}
 //	
 //	private Widget createResponsibilities(String stakeholderContent, String engineerContent)
 //	{
 //		String stakeholderStyle = "teach-step-stakeholder-content";
 //		String engineerStyle = "teach-step-engineer-content";
 //		String stakeholderTitle = this.pageMessages.stakeholderResponsibilitiesLabel();
 //		String engineerTitle = this.pageMessages.engineerResponsibilitiesLabel();
 //		
 //		HorizontalPanel layout = new HorizontalPanel();
 //		layout.setStyleName("teach-step-responsibilities");
 //		layout.add(this.setLayout(stakeholderTitle, stakeholderContent, stakeholderStyle));
 //		layout.add(new HTML("<div class=\"teach-step-spacer\"></div>"));
 //		layout.add(this.setLayout(engineerTitle, engineerContent, engineerStyle));
 //		
 //		return layout;
 //	}
 //	
 	private Widget createResponsibilities(List<Roles> roles, List<String> contents)
 	{
 		List<String> style = new ArrayList<String>();
 		List<String> titles = new ArrayList<String>();
 		HorizontalPanel layout = new HorizontalPanel();
 		layout.setStyleName("teach-step-responsibilities");
 		int i=0;
 		for(Roles role : roles)
 		{
 			style.add("teach-step-stakeholder-content");
 			switch(role)
 			{
 				case Acquisition_Organization_Engineer:
 					titles.add(this.pageMessages.aoeResponsibilitiesLabel());
 					break;
 				case Security_Specialist:
 					titles.add(this.pageMessages.securityResponsibilitiesLabel());
 					break;
 				case Contractor:
 					titles.add(this.pageMessages.contractorResponsibilitiesLabel());
 					break;
 				case COTS_Vendor:
 					titles.add(this.pageMessages.cotsResponsibilitiesLabel());
 					break;
 				default:
 					titles.add("ERROR occurred");				
 					
 			}
 			layout.add(this.setLayout(titles.get(i), contents.get(i), style.get(i)));
 			if(i>=roles.size()-1)
 			layout.add(new HTML("<div class=\"teach-step-spacer\"></div>"));
 			++i;
 		}	
 		return layout;
 	}
 	
 	private Widget createExitCriteria(String content)
 	{
 		String style = "teach-step-exit-content";
 		String title = this.pageMessages.exitCriteriaLabel();
 		
 		return this.setLayout(title, content, style);
 	}
 	
 	private Widget createWebisteInfo(String content)
 	{
 		String style = "teach-step-tool-content";
 		String title = this.pageMessages.webHintLabel();
 				
 		return this.setLayout(title, content, style);
 	}
 	
 	
 	/**
 	 * Reusable method for setting up a simple vertically aligned label/box combo.
 	 * 
 	 * All labels automatically receive the .teach-step-label style.
 	 * @param title The title of the label.
 	 * @param content The content of the box.
 	 * @param style The style that is to be applied to the box.
 	 * @return The complete layout for the label and box.
 	 */
 	private Widget setLayout(String title, String content, String style)
 	{
 		VerticalPanel layout = new VerticalPanel();
 		
 		Label titleLabel = new Label(title);
 		titleLabel.setStyleName("page-title-heading");
 	
 		HTML contentLabel = new HTML(content);
 		contentLabel.setStyleName(style);
 		
 		layout.add(titleLabel);
 		layout.add(contentLabel);
 		layout.setWidth("100%");
 		
 		return layout;
 	}
 	
 
 
 
 	
 
 }
