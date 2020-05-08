 package edu.cmu.square.client.ui.elicitSecurityRequirements;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 import edu.cmu.square.client.exceptions.ExceptionHelper;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtArtifact;
 import edu.cmu.square.client.model.GwtAsset;
 import edu.cmu.square.client.model.GwtBusinessGoal;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.model.GwtRisk;
 import edu.cmu.square.client.model.GwtSubGoal;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.CollectArtifactsService;
 import edu.cmu.square.client.remoteService.step.interfaces.CollectArtifactsServiceAsync;
 import edu.cmu.square.client.remoteService.step.interfaces.ElicitRequirementService;
 import edu.cmu.square.client.remoteService.step.interfaces.ElicitRequirementServiceAsync;
 import edu.cmu.square.client.remoteService.step.interfaces.IdentifyGoalsAssetsService;
 import edu.cmu.square.client.remoteService.step.interfaces.IdentifyGoalsAssetsServiceAsync;
 import edu.cmu.square.client.remoteService.step.interfaces.RiskAssessmentService;
 import edu.cmu.square.client.remoteService.step.interfaces.RiskAssessmentServiceAsync;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 import edu.cmu.square.client.ui.risksAssessment.ArtifactDialogBox;
 
 public class ElicitSecurityRequirementPane extends BasePane implements Command
 {
 	final ElicitSecurityRequirementPaneMessages messages = (ElicitSecurityRequirementPaneMessages) GWT
 			.create(ElicitSecurityRequirementPaneMessages.class);
 	private ElicitRequirementServiceAsync requirementService = GWT.create(ElicitRequirementService.class);
 	private RiskAssessmentServiceAsync riskService = GWT.create(RiskAssessmentService.class);
 
 	private GwtProject currentProject;
 	private FlexTable matrix = new FlexTable();
 
 //	private VerticalPanel risksPane = new VerticalPanel();
 
 
 	private Label requirementTitleLabel = new Label(messages.labelRequirementTitle());
 	private Label requirementDescriptionLabel = new Label(messages.description());
 
 	private Label subGoalLabel = new Label(messages.securityGoal());
 //	private Label riskLabel = new Label(messages.risks());
 //	private Label artifactLabel = new Label(messages.artifacts());
 
 
 	private TextBox requirementTitleTextBox = new TextBox();
 	private TextArea requirementDescriptionTextBox = new TextArea();
 
 	private ListBox subGoalsListBox;
 	private DisclosurePanel disPanel;
 
 	private GwtRequirement currentRequirement;
 	private Button save;
 	private Button cancel;
 	private Label errorMessage = null;
 
 	private SquareHyperlink editRequirement;
 	private SquareHyperlink deleteRequirement;
 	private SquareHyperlink gotToSummary;
 
 	private List<GwtRequirement> lisOfRequirements = new ArrayList<GwtRequirement>();
 	private List<GwtRisk> listOfProjectRisks = new ArrayList<GwtRisk>();
 	private List<GwtRisk> listOfProjectFilteredRisks = new ArrayList<GwtRisk>();
 	private List<GwtRisk> listOfRiksMapppedToRequirement = new ArrayList<GwtRisk>();
 	private List<GwtSubGoal> listOfProjectSubGoals = new ArrayList<GwtSubGoal>();
 	private List<GwtSubGoal> listOfSubGoalsMappedToRequirement = new ArrayList<GwtSubGoal>();
 //	private List<GwtArtifact> listOfProjectArtifacts = new ArrayList<GwtArtifact>();
 //	private List<GwtArtifact> listOfArtifactsMappedToRequirement = new ArrayList<GwtArtifact>();
 
 	private int currentIndex = -1;
 	private int currentRequirementId = -1;
 	private boolean contetHasChanged = false;
 	
 	private Label subGoalEmptyLabel;
 	private Label riskEmptyLabel;
 //	private Label artifactsEmptyLabel;
 
 	private enum CommandTypes
 	{
 		insert, update, read
 	};
 	
 	private CommandTypes currentCommand;
 	private SubGoalDialogBox subGoalDialog;
 //	private ArtifactDialogBox artifactDialog;
 //	private RiskDialogBox riskDialogBox;
 
 	private GwtBusinessGoal businessGoalInfo = new GwtBusinessGoal();
 
 	public ElicitSecurityRequirementPane(final State stateInfo)
 	{
 		super(stateInfo);
 		currentProject = new GwtProject();
 		currentProject.setId(this.getCurrentState().getProjectID());
 		currentRequirementId = currentState.getCurrentRisk();
 		currentCommand = getCurrentCommand();
 		
 		this.showLoadingStatusBar();
 
 		loadSubGoalsFromProject();
 	}
 
 	// ------------------------------------------------------
 	// RPC Asynchronous calls section
 	// ------------------------------------------------------
 
 	public void loadSubGoalsFromProject()
 	{
 		IdentifyGoalsAssetsServiceAsync service = GWT.create(IdentifyGoalsAssetsService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "identifyGoalAssetService.rpc");
 		service.loadBusinessGoalInfo(getCurrentState().getProjectID(), new AsyncCallback<GwtBusinessGoal>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "retrieving goals");
 				}
 				public void onSuccess(GwtBusinessGoal result)
 				{
 					businessGoalInfo = result;
 					listOfProjectSubGoals = businessGoalInfo.getSubGoals();
					loadArtifactsFromProject();
 				}
 			});
 	}
	
 	public void loadArtifactsFromProject()
 	{
 		CollectArtifactsServiceAsync service = GWT.create(CollectArtifactsService.class);
 		int projectId = this.getCurrentState().getProjectID();
 		
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "collectArtifacts.rpc");
 		service.getAllArtifacts(projectId, new AsyncCallback<List<GwtArtifact>>()
 		{
 			public void onFailure(Throwable caught)
 			{
 				ExceptionHelper.SquareRootRPCExceptionHandler(caught, "retrieving artifacts");
 			}
 			
 			public void onSuccess(List<GwtArtifact> result)
 			{
			//	listOfProjectArtifacts = result;
 				loadProjectRequirements();
 			}
 		});
 	}

 	
 	public void loadProjectRequirements()
 	{
 		ServiceDefTarget endpoint = (ServiceDefTarget) requirementService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "elicitRequirement.rpc");
 		
 		this.requirementService.getRequirementsFromProject(this.currentProject.getId(), new AsyncCallback<List<GwtRequirement>>()
 		{
 			public void onFailure(Throwable caught)
 			{
 				ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.errorGettingRequirements());
 			}
 		
 			public void onSuccess(List<GwtRequirement> result)
 			{
 				lisOfRequirements = result;
 				currentRequirement = getRequirementFromListByID(currentRequirementId);
 				if (currentRequirement != null)
 				{
 					listOfSubGoalsMappedToRequirement = currentRequirement.getSubGoals();
 					listOfRiksMapppedToRequirement = currentRequirement.getRisks();
 				//	listOfArtifactsMappedToRequirement = currentRequirement.getArtifacts();
 				}
 				loadProjectRisks();
 			}
 		});
 	}
 
 	public void loadProjectRisks()
 	{
 		int projectId = this.getCurrentState().getProjectID();
 		ServiceDefTarget endpoint = (ServiceDefTarget) riskService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "riskAssessment.rpc");
 		
 		riskService.getRisksFromProject(projectId, new AsyncCallback<List<GwtRisk>>()
 		{
 			public void onFailure(Throwable caught)
 			{
 				ExceptionHelper.SquareRootRPCExceptionHandler(caught, "retrieving risks");
 			}
 
 			public void onSuccess(List<GwtRisk> result)
 			{
 				listOfProjectRisks = result;
 				listOfProjectFilteredRisks=filterRiskBaseOnGoals(listOfSubGoalsMappedToRequirement, listOfProjectRisks);
 				PaneInitialization();
 			}
 		});
 	}
 
 	public void updateRequirement(int requirementId, GwtRequirement gwtRequirement)
 	{
 		gwtRequirement.setId(requirementId);
 		this.requirementService.updateRequirement(gwtRequirement, new AsyncCallback<Void>()
 		{
 			public void onFailure(Throwable caught)
 			{
 				ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.errorUpdatingRequirements());
 			}
 			
 			public void onSuccess(Void result)
 			{
 				currentCommand = CommandTypes.read;
 				loadProjectRequirements();
 			}
 		});
 	}
 
 	public void removeRequirement(int requirementId)
 	{
 		boolean response = Window.confirm(messages.confirmDelete());
 		if (response)
 		{
 			this.requirementService.deleteRequirement(requirementId, currentState.getProjectID(), new AsyncCallback<Void>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.errorRemovingRequirements());
 				}
 				
 				public void onSuccess(Void result)
 				{
 					History.newItem(ElicitSecurityRequirementsPilot.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementsSummary));
 				}
 			});
 		}
 	}
 
 	// ------------------------------------------------------
 	// Risk Manipulation section
 	// ------------------------------------------------------
 
 	public void PaneInitialization()
 	{
 		this.hideStatusBar();
 		this.contetHasChanged = false;
 		this.save = new Button(messages.save());
 		this.cancel = new Button(messages.cancel());
 		this.save.setWidth("80px");
 		this.cancel.setWidth("80px");
 		this.getContent().clear();
 		getContent().add(loadRiskNavigator());
 
 		this.disPanel = new DisclosurePanel();
 		this.disPanel.setAnimationEnabled(true);
 		errorMessage = new Label(messages.labelFieldsRequired());
 		errorMessage.setStyleName("square-RequiredMessage");
 
 		this.disPanel.add(errorMessage);
 		this.getContent().add(disPanel);
 		this.matrix = new FlexTable();
 
 		this.requirementTitleTextBox.addChangeHandler(new contentChanged());
 		this.requirementDescriptionTextBox.addChangeHandler(new contentChanged());
 		
 		this.getContent().add(this.matrix);
 
 		loadRequirementForm();
 
 		this.save.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 
 					saveRequirement(currentRequirementId);
 				}
 			});
 
 		cancel.addClickHandler(new ClickHandler()
 			{
 
 				public void onClick(ClickEvent event)
 				{
 					if (CommandTypes.insert == currentCommand)
 					{
 						History.newItem(ElicitSecurityRequirementsPilot
 								.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementsSummary));
 					}
 					else
 					{
 						currentCommand = CommandTypes.read;
 						PaneInitialization();
 					}
 				}
 			});
 	}
 	
 	public void loadRequirementForm()
 	{
 
 		this.matrix.getCellFormatter().setWidth(1, 0, "100px");
 		this.matrix.getCellFormatter().setWidth(1, 1, "80%");
 
 		
 		this.matrix.setWidth("85%");
 		this.matrix.setCellPadding(10);
 		this.matrix.setStyleName("square-flex");
 
 		initializeRequirementFormWidgets();
 
 		if (!CommandTypes.insert.equals(currentCommand))
 		{
 			setRequirementFormValues(currentRequirement);
 		}
 
 		this.matrix.getCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
 
 		if (currentCommand == CommandTypes.read)
 		{
 			// Configure the table when the command is to view.
 			loadReadOnlyRequirementForm();
 		}
 		else
 		{
 			// Configure the table when the command is to Update or Create.
 			loadEditableRequirementForm();
 		}
 
 		this.matrix.setWidget(1, 0, requirementDescriptionLabel);
 		this.matrix.setWidget(1, 1, getField(requirementDescriptionTextBox));
 
 		this.matrix.setWidget(2, 1, createHTMLSubGoalList());
 //		this.matrix.setWidget(3, 1, createHTMLRisksList());
 //		this.matrix.setWidget(3, 1, createHTMLArtifactsList());
 
 		
 		this.matrix.getCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
 		this.matrix.getCellFormatter().setAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
 		this.matrix.getCellFormatter().setAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
 		this.matrix.getCellFormatter().setAlignment(2, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
 		this.matrix.getCellFormatter().setAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
 		this.matrix.getCellFormatter().setAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
 //		this.matrix.getCellFormatter().setAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
 //		this.matrix.getCellFormatter().setAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
 
 	}
 
 	public void initializeRequirementFormWidgets()
 	{
 		this.editRequirement = new SquareHyperlink(messages.linkEditRequirement());
 		this.deleteRequirement = new SquareHyperlink(messages.linkDeleteRequirement());
 		this.gotToSummary = new SquareHyperlink(messages.linkBackRequirementSummary());
 
 		this.requirementTitleLabel.setStyleName("Risk-Input-Labels");
 		this.requirementDescriptionLabel.setStyleName("Risk-Input-Labels");
 
 		this.subGoalLabel.setStyleName("Risk-Input-Labels");
 //		this.riskLabel.setStyleName("Risk-Input-Labels");
 //		this.artifactLabel.setStyleName("Risk-Input-Labels");
 
 		this.requirementTitleTextBox.setStyleName("square-long-textBox");
 		this.requirementDescriptionTextBox.setStyleName("square-long-textBox");
 		this.requirementDescriptionTextBox.setHeight("100px");
 
 		subGoalsListBox = new ListBox();
 		subGoalsListBox.clear();
 		subGoalsListBox.setWidth("455px");
 		subGoalsListBox.addItem(" ", " ");
 		
 		for (GwtSubGoal subGoals : listOfProjectSubGoals)
 		{
 			subGoalsListBox.addItem(subGoals.getDescription(), subGoals.getId() + "");
 		}
 
 	}
 	public void loadReadOnlyRequirementForm()
 	{
 		this.matrix.setWidget(0, 0, getField(requirementTitleTextBox));
 		this.matrix.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
 		this.matrix.getCellFormatter().setStyleName(0, 0, "step-Title-heading");
 		((FlexCellFormatter) this.matrix.getCellFormatter()).setColSpan(0, 0, 2);
 
 		this.matrix.setWidget(2, 0, subGoalLabel);
 //		this.matrix.setWidget(3, 0, riskLabel);
 //		this.matrix.setWidget(3, 0, artifactLabel);
 
 
 		if (currentState.getMode() == GwtModesType.ReadWrite)
 		{
 			FlexTable bottonControlPanel = new FlexTable();
 			bottonControlPanel.setWidth("85%");
 			bottonControlPanel.setWidget(0, 0, editRequirement);
 			bottonControlPanel.setWidget(1, 0, deleteRequirement);
 			bottonControlPanel.setWidget(2, 0, new Label(" "));
 			bottonControlPanel.setWidget(3, 0, gotToSummary);
 			bottonControlPanel.getCellFormatter().setHorizontalAlignment(0,0,HasHorizontalAlignment.ALIGN_LEFT);
 			bottonControlPanel.getCellFormatter().setHorizontalAlignment(1,0,HasHorizontalAlignment.ALIGN_LEFT);
 			bottonControlPanel.getCellFormatter().setHorizontalAlignment(2,0,HasHorizontalAlignment.ALIGN_LEFT);
 			bottonControlPanel.getCellFormatter().setHorizontalAlignment(3,0,HasHorizontalAlignment.ALIGN_LEFT);
 			this.getContent().add(bottonControlPanel);
 
 		}
 
 		this.editRequirement.addClickHandler(new ClickHandler()
 			{
 
 				public void onClick(ClickEvent event)
 				{
 					currentCommand = CommandTypes.update;
 					PaneInitialization();
 
 				}
 			});
 
 		this.deleteRequirement.addClickHandler(new ClickHandler()
 			{
 
 				public void onClick(ClickEvent event)
 				{
 					removeRequirement(currentRequirementId);
 
 				}
 			});
 
 		this.gotToSummary.addClickHandler(new ClickHandler()
 			{
 
 				public void onClick(ClickEvent event)
 				{
 					History.newItem(ElicitSecurityRequirementsPilot.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementsSummary));
 
 				}
 			});
 	}
 	
 	public void loadEditableRequirementForm()
 	{
 		this.matrix.setWidget(0, 0, requirementTitleLabel);
 		this.matrix.setWidget(0, 1, getField(requirementTitleTextBox));
 
 		this.matrix.setWidget(2, 0, getEditSubGoalsHyperLink(this));
 //		this.matrix.setWidget(3, 0, getEditRisksHyperLink(this));
 //		this.matrix.setWidget(3, 0, getEditArtifactsHyperLink(this));
 
 		subGoalLabel.setStyleName("Risk-Input-Labels");
 //		riskLabel.setStyleName("Risk-Input-Labels");
 
 		matrix.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
 		matrix.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
 
 		if (CommandTypes.insert.equals(currentCommand))
 		{
 			this.save.setText(messages.create());
 		}
 		else if (CommandTypes.update.equals(currentCommand))
 		{
 			this.save.setText(messages.save());
 		}
 
 		FlexTable flexTable = new FlexTable();
 		flexTable.setWidget(0, 0, save);
 		flexTable.setWidget(0, 1, cancel);
 		flexTable.setCellSpacing(10);
 
 		this.getContent().add(flexTable);
 	}
 
 	public Widget getField(Widget w)
 	{
 		boolean readOnly = false;
 		if (CommandTypes.read.equals(currentCommand))
 		{
 			readOnly = true;
 		}
 
 		if (w instanceof TextBox && readOnly)
 		{
 			return new Label(((TextBox) w).getText());
 		}
 		else if (w instanceof TextArea && readOnly)
 		{
 			return new Label(((TextArea) w).getText());
 		}
 		else if (w instanceof ListBox && readOnly)
 		{
 			ListBox list = (ListBox) w;
 			String value = list.getItemText(list.getSelectedIndex());
 			return new Label(value);
 		}
 		return w;
 	}
 
 	// This sets the mode the of form based on the commandType.
 	public CommandTypes getCurrentCommand()
 	{
 		int command = currentState.getRiskCommand();
 
 		if (command == 0)
 		{
 			return CommandTypes.insert;
 		}
 		else if (command == 1)
 		{
 			return CommandTypes.read;
 		}
 		else if (command == 2)
 		{
 			return CommandTypes.update;
 		}
 
 		return CommandTypes.read;
 	}
 	public GwtRequirement getFormRiskValues()
 	{
 		GwtRequirement gwtRequirement = new GwtRequirement();
 		gwtRequirement.setTitle(this.requirementTitleTextBox.getText());
 		gwtRequirement.setDescription(this.requirementDescriptionTextBox.getText());
 		gwtRequirement.setSubGoals(this.listOfSubGoalsMappedToRequirement);
 		gwtRequirement.setRisks(this.listOfRiksMapppedToRequirement);
 		//gwtRequirement.setArtifacts(this.listOfArtifactsMappedToRequirement);
 
 		return gwtRequirement;
 	}
 
 	public void setRequirementFormValues(GwtRequirement gwtRequirement)
 	{
 		this.requirementTitleTextBox.setText(gwtRequirement.getTitle());
 		this.requirementDescriptionTextBox.setText(gwtRequirement.getDescription());
 
 		this.listOfRiksMapppedToRequirement = gwtRequirement.getRisks();
 		this.listOfSubGoalsMappedToRequirement = gwtRequirement.getSubGoals();
 
 	}
 	public GwtRequirement getRequirementFromListByID(int requirementId)
 	{
 
 		int count = 0;
 		for (GwtRequirement r : lisOfRequirements)
 		{
 			if (r.getId() == requirementId)
 			{
 				currentIndex = count;
 				return r;
 			}
 			count++;
 		}
 		return currentRequirement;
 
 	}
 	public GwtRequirement getRequirementFromListByIndex(int index)
 	{
 		int count = 0;
 		for (GwtRequirement r : lisOfRequirements)
 		{
 			if (count == index)
 			{
 
 				return r;
 			}
 			count++;
 		}
 		return currentRequirement;
 
 	}
 
 	public Widget loadRiskNavigator()
 	{
 		
 		SquareHyperlink previousRequirement = new SquareHyperlink(messages.linkPreviousRequirement());
 		SquareHyperlink nextRequirement = new SquareHyperlink(messages.linkNextRequirement());
 		SquareHyperlink gotToSummaryTop = new SquareHyperlink(messages.linkBackRequirementSummary());
 
 		FlexTable requirementNavigatorWidget = new FlexTable();
 			requirementNavigatorWidget.setWidth("85%");
 		gotToSummaryTop.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 
 		requirementNavigatorWidget.setWidget(0, 1, gotToSummaryTop);
 		requirementNavigatorWidget.getCellFormatter().setWidth(0, 1, "33%");
 		requirementNavigatorWidget.getCellFormatter().setWidth(0, 0, "33%");
 		requirementNavigatorWidget.getCellFormatter().setWidth(0, 2, "33%");
 
 		// It should only show the requirement navigation bar when is an
 		// existing requirement
 		if (CommandTypes.insert != currentCommand)
 		{
 			if (currentIndex > 0)
 			{
 				requirementNavigatorWidget.setWidget(0, 0, previousRequirement);
 				previousRequirement.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 			}
 			else
 			{
 				Label label = new Label(" ");
 				requirementNavigatorWidget.setWidget(0, 0, label);
 			}
 
 			if (currentIndex < (lisOfRequirements.size() - 1))
 			{
 				requirementNavigatorWidget.setWidget(0, 2, nextRequirement);
 				nextRequirement.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			}
 			else
 			{
 				Label label = new Label(" ");
 				requirementNavigatorWidget.setWidget(0, 2, label);
 
 			}
 		}
 
 		gotToSummaryTop.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					//saveChangesConfirmation();
 				
 					History.newItem(ElicitSecurityRequirementsPilot.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementsSummary));
 				}
 			});
 
 		previousRequirement.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					//saveChangesConfirmation();
 					currentRequirement = getRequirementFromListByIndex(currentIndex - 1);
 					currentRequirementId = currentRequirement.getId();
 					currentIndex = currentIndex - 1;
 					// Reloads the requirement form
 				
 					PaneInitialization();
 				}
 			});
 
 		nextRequirement.addClickHandler(new ClickHandler()
 			{
 
 				public void onClick(ClickEvent event)
 				{
 					//saveChangesConfirmation();
 					currentRequirement = getRequirementFromListByIndex(currentIndex + 1);
 					currentRequirementId = currentRequirement.getId();
 					currentIndex = currentIndex + 1;
 					// Reloads the requirement form
 				
 					PaneInitialization();
 
 				}
 			});
 
 		return requirementNavigatorWidget;
 	}
 
 	public void saveRequirement(int requirementId)
 	{
 		if (validateRequiredFields())
 		{
 			GwtRequirement gwtRequirement = getFormRiskValues();
 			if (requirementId == -1)// Is an insert action
 			{
 				addRequirementToProject(gwtRequirement);
 			}
 			else
 			{
 
 				updateRequirement(requirementId, gwtRequirement);
 			}
 		}
 	}
 	public void addRequirementToProject(GwtRequirement gwtRequirement)
 	{
 
 		this.requirementService.addRequirementToProject(currentProject.getId(), gwtRequirement, new AsyncCallback<Integer>()
 			{
 
 				
 				public void onFailure(Throwable caught)
 				{
 					SquareException se = (SquareException) caught;
 					switch (se.getType())
 					{
 						case authorization :
 							Window.alert(messages.errorAuthorization());
 							break;
 
 						default :
 							Window.alert(messages.errorAddingRequirements());
 							break;
 					}
 				}
 
 				
 				public void onSuccess(Integer result)
 				{
 
 					currentRequirementId = result;
 					currentCommand = CommandTypes.read;
 					loadProjectRequirements();
 				}
 			});
 
 	}
 
 	// ------------------------------------------------------
 	// Field Validator Methods
 	// ------------------------------------------------------
 
 	public void setValidationStyle(Widget w, String textValue)
 	{
 		if (textValue.length() == 0)
 		{
 			w.setStyleName("square-RequiredField");
 
 			if (w instanceof ListBox)
 			{
 				w.setStyleName("square-RequiredFieldListBox");
 			}
 		}
 		else
 		{
 			w.setStyleName("square-long-textBox");
 
 			if (w instanceof ListBox)
 			{
 				w.setStyleName("Risk-Input-listBox");
 			}
 		}
 
 	}
 	public boolean validateRequiredFields()
 	{
 		disPanel.setOpen(false);
 		errorMessage.setText(messages.labelFieldsRequired());
 		
 		for (int j = 0; j < matrix.getRowCount(); j++)
 		{
 			Widget w = matrix.getWidget(j, 1);
 			String textValue = "Ok";
 			if (w instanceof TextBox)
 			{
 				TextBox field = (TextBox) w;
 				textValue = field.getText().trim();
 				setValidationStyle(field, textValue);
 			}
 			else if (w instanceof TextArea)
 			{
 				TextArea field = (TextArea) w;
 				textValue = field.getText().trim();
 				setValidationStyle(field, textValue);
 
 			}
 			else if (w instanceof ListBox)
 			{
 				ListBox field = (ListBox) w;
 				textValue = field.getItemText(field.getSelectedIndex()).trim();
 				setValidationStyle(field, textValue);
 			}
 //			else if (w instanceof VerticalPanel)
 //			{
 //				if (listOfRiksMapppedToRequirement == null || listOfRiksMapppedToRequirement.isEmpty())
 //				{
 //					textValue = "";
 //				}
 //				setValidationStyle(risksPane, textValue);
 //			}
 
 			if (textValue.trim().length() == 0)
 			{
 				disPanel.setOpen(true);
 				return false;
 			}
 		}
 
 		if (listOfSubGoalsMappedToRequirement.size() == 0)
 		{
 			disPanel.setOpen(true);
 			subGoalEmptyLabel.setStyleName("square-RequiredMessage");
 			return false;
 		}
 //		else if (listOfRiksMapppedToRequirement.size() == 0)
 //		{
 //			disPanel.setOpen(true);
 //			riskEmptyLabel.setStyleName("square-RequiredMessage");
 //			return false;
 //		}
 		/*
 		else if (listOfArtifactsMappedToRequirement.size() == 0)
 		{
 			disPanel.setOpen(true);
 			artifactsEmptyLabel.setStyleName("square-RequiredMessage");
 			return false;
 		}
 		*/
 		
 		//Check to make sure this requirement title is not already in the system
 		for (GwtRequirement requirement : lisOfRequirements)
 		{
 			if (requirement.getTitle().equals(requirementTitleTextBox.getText().trim()))
 			{
 				errorMessage.setText(messages.duplicateTitleError());
 				disPanel.setOpen(true);
 				return false;
 			}
 		}
 		
 		
 		disPanel.setOpen(false);
 		
 		return true;
 	}
 
 	// ------------------------------------------------------
 	// Requirements Mapping to Sub Goals, Assets and Artifacts
 	// ------------------------------------------------------
 	String currentDialog = "";
 	public Widget getEditSubGoalsHyperLink(final ElicitSecurityRequirementPane riksPane)
 	{
 
 		SquareHyperlink associateLink = new SquareHyperlink(messages.editSubGoalsLink());
 		associateLink.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					subGoalDialog = new SubGoalDialogBox(riksPane, listOfProjectSubGoals, listOfSubGoalsMappedToRequirement);
 					subGoalDialog.setModal(true);
 					subGoalDialog.center();
 					currentDialog = "SubGoal";
 					subGoalDialog.show();
 
 				}
 			});
 		return associateLink;
 
 	}
 //	public Widget getEditRisksHyperLink(final ElicitSecurityRequirementPane riksPane)
 //	{
 //
 //		SquareHyperlink associateLink = new SquareHyperlink(messages.editRisksLink());
 //		associateLink.addClickHandler(new ClickHandler()
 //			{
 //				public void onClick(ClickEvent event)
 //				{
 //					riskDialogBox = new RiskDialogBox(riksPane, listOfProjectFilteredRisks, listOfRiksMapppedToRequirement);
 //					riskDialogBox.setModal(true);
 //					riskDialogBox.center();
 //					currentDialog = "Risk";
 //					riskDialogBox.show();
 //
 //				}
 //			});
 //		return associateLink;
 //
 //	}
 /*
 	public Widget getEditArtifactsHyperLink(final ElicitSecurityRequirementPane riksPane)
 	{
 		SquareHyperlink associateLink = new SquareHyperlink(messages.editArtifactLink());
 		associateLink.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					artifactDialog = new ArtifactDialogBox(riksPane, listOfProjectArtifacts, listOfArtifactsMappedToRequirement, messages.associateArtifacts());
 					artifactDialog.setModal(true);
 					artifactDialog.center();
 					artifactDialog.show();
 					currentDialog = "Artifact";
 					// isAssetCurrentDialog = false;
 				}
 			});
 		return associateLink;
 	}
 */
 	public Widget createHTMLSubGoalList()
 	{
 		if (listOfSubGoalsMappedToRequirement.size() > 0)
 		{
 			String listStart = "<UL>";
 			String listEnd = "</UL>";
 			StringBuilder listBuilder = new StringBuilder();
 
 			listBuilder.append(listStart);
 			for (GwtSubGoal a : listOfSubGoalsMappedToRequirement)
 			{
 				listBuilder.append("<LI><font color=\"black\">" + a.getDescription() + "</font></LI>");
 
 			}
 			listBuilder.append(listEnd);
 			HTML list = new HTML(listBuilder.toString());
 			list.setStyleName("square-sssets-listing");
 			return list;
 		}
 		else
 		{
 			subGoalEmptyLabel = new Label(messages.noSelectedSecurityGoal());
 			subGoalEmptyLabel.setStyleName("square-note");
 			return subGoalEmptyLabel;
 		}
 
 	}
 //	public Widget createHTMLRisksList()
 //	{
 //		if (listOfRiksMapppedToRequirement.size() > 0)
 //		{
 //			String listStart = "<UL>";
 //			String listEnd = "</UL>";
 //			StringBuilder listBuilder = new StringBuilder();
 //
 //			listBuilder.append(listStart);
 //			for (GwtRisk a : listOfRiksMapppedToRequirement)
 //			{
 //				listBuilder.append("<LI><font color=\"black\">" + a.getRiskTitle() + "</font></LI>");
 //
 //			}
 //			listBuilder.append(listEnd);
 //			HTML list = new HTML(listBuilder.toString());
 //			list.setStyleName("square-sssets-listing");
 //			return list;
 //		}
 //		else
 //		{
 //			riskEmptyLabel = new Label(messages.noSelectedRisks());
 //			riskEmptyLabel.setStyleName("square-note");
 //			return riskEmptyLabel;
 //		}
 //
 //	}
 /*
 	public Widget createHTMLArtifactsList()
 	{
 		if (listOfArtifactsMappedToRequirement.size() > 0)
 		{
 			String listStart = "<UL>";
 			String listEnd = "</UL>";
 			StringBuilder listBuilder = new StringBuilder();
 			listBuilder.append(listStart);
 			for (GwtArtifact a : listOfArtifactsMappedToRequirement)
 			{
 				listBuilder.append("<LI><font color=\"black\">" + a.getName() + "</font></LI>");
 			}
 			listBuilder.append(listEnd);
 			HTML list = new HTML(listBuilder.toString());
 			list.setStyleName("square-sssets-listing");
 
 			return list;
 		}
 		else
 		{
 			artifactsEmptyLabel = new Label(messages.noSelectedArtifacts());
 			artifactsEmptyLabel.setStyleName("square-note");
 			return artifactsEmptyLabel;
 		}
 
 	}
 */
 	public List<GwtRisk> filterRiskBaseOnGoals(List<GwtSubGoal> subGoals, List<GwtRisk> risks)
 	{
 		
 		List<GwtRisk> filterRisks = new ArrayList<GwtRisk>();
 		for (GwtRisk r : risks)
 		{
 			for (GwtAsset a : r.getAssets())
 			{
 				for (GwtSubGoal g : subGoals)
 				{
 
 					if (g.getAssets().contains(a))
 					{
 						if (!filterRisks.contains(r))
 						{
 							filterRisks.add(r);
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return filterRisks;
 
 	}
 	// This handless the call back when the dialog boxes are closed.
 	public void execute()
 	{
 
 		if (currentDialog.equalsIgnoreCase("SubGoal"))
 		{
 			
 			listSubGoalChanged(listOfSubGoalsMappedToRequirement,subGoalDialog.getNewSelectedSubGoals());
 		
 			listOfSubGoalsMappedToRequirement = subGoalDialog.getNewSelectedSubGoals();
 			listOfRiksMapppedToRequirement.clear();
 //			this.matrix.setWidget(3, 1, createHTMLRisksList());
 			listOfProjectFilteredRisks= filterRiskBaseOnGoals(listOfSubGoalsMappedToRequirement, listOfProjectRisks);
 			this.matrix.setWidget(2, 1, createHTMLSubGoalList());
 			
 		}
 //		else if (currentDialog.equalsIgnoreCase("Risk"))
 //		{
 //			listRiskChanged(listOfRiksMapppedToRequirement,  riskDialogBox.getNewSelectedRisks());
 //			
 //			listOfRiksMapppedToRequirement = riskDialogBox.getNewSelectedRisks();
 //			this.matrix.setWidget(3, 1, createHTMLRisksList());
 //		}
 		/*
 		else if (currentDialog.equalsIgnoreCase("Artifact"))
 		{
 			listArtifactChanged(listOfArtifactsMappedToRequirement,artifactDialog.getNewSelectedArtifacts());
 			
 			listOfArtifactsMappedToRequirement = artifactDialog.getNewSelectedArtifacts();
 			this.matrix.setWidget(3, 1, createHTMLArtifactsList());
 		}
 		*/
 
 	}
 
 
 
 
 	private class contentChanged implements ChangeHandler
 	{
 	
 		public void onChange(ChangeEvent event)
 		{
 			contetHasChanged = true;
 			
 		}
 	}
 
 	
 	public void listArtifactChanged(List<GwtArtifact> oldArtifact, List<GwtArtifact> newArtifact)
 	{
 		for(GwtArtifact old:oldArtifact)
 		{
 			if(!newArtifact.contains(old))
 			{
 				contetHasChanged= true;
 				return;
 			}
 		}
 		
 	}
 	public void listRiskChanged(List<GwtRisk> oldRisks, List<GwtRisk> newRisks)
 	{
 		for(GwtRisk old:oldRisks)
 		{
 			if(!newRisks.contains(old))
 			{
 				contetHasChanged= true;
 				return;
 			}
 		}
 	
 	}
 	public void listSubGoalChanged(List<GwtSubGoal> oldSubGoals, List<GwtSubGoal> newSubGoals)
 	{
 		for(GwtSubGoal old:oldSubGoals)
 		{
 			if(!newSubGoals.contains(old))
 			{
 				contetHasChanged= true;
 				return;
 			}
 		}
 	
 	}
 }
