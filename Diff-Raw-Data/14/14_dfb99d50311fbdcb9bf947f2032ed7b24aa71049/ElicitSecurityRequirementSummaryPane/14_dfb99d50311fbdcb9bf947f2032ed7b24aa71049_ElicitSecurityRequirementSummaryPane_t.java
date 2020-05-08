 package edu.cmu.square.client.ui.elicitSecurityRequirements;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
 
 import edu.cmu.square.client.exceptions.ExceptionHelper;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.ElicitRequirementService;
 import edu.cmu.square.client.remoteService.step.interfaces.ElicitRequirementServiceAsync;
 import edu.cmu.square.client.ui.ChooseStep.ChooseStepPilot;
import edu.cmu.square.client.ui.ChooseStepCase3.ChooseStepCase3Pilot;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 
 public class ElicitSecurityRequirementSummaryPane extends BasePane
 {
 	protected static final String FILE_PATH_REGEX = "^([a-zA-Z]\\:|\\\\)\\\\" +
 			"([^\\\\]+\\\\)*[^\\\\/:*?\\\"<>|]+\\.(CSV|csv)$";
 	final ElicitSecurityRequirementPaneMessages messages = (ElicitSecurityRequirementPaneMessages) GWT.create(ElicitSecurityRequirementPaneMessages.class);
 	private ElicitRequirementServiceAsync service = GWT.create(ElicitRequirementService.class);
 	
 	private GwtProject currentProject;
 	
 	private List<GwtRequirement> listOfRequirements = new ArrayList<GwtRequirement>();
 	private FlexTable requirementTable = new FlexTable();
 	
 	private Button addRequirement;
 
 	public ElicitSecurityRequirementSummaryPane(final State stateInfo)
 		{
 			super(stateInfo);
 		
 			currentProject = new GwtProject();
 			currentProject.setId(this.getCurrentState().getProjectID());
 			this.showLoadingStatusBar();
 			loadRequirementFromProject();
 
 		}
 	// ------------------------------------------------------
 	// RPC Asynchronous calls section
 	// ------------------------------------------------------
 
 	public void loadRequirementFromProject()
 	{
 		
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "elicitRequirement.rpc");
 		this.service.getRequirementsFromProject(this.currentProject.getId(), new AsyncCallback<List<GwtRequirement>>()
 			{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.errorGettingRequirements());
 				}
 
 				public void onSuccess(List<GwtRequirement> result)
 				{
 					listOfRequirements = result;
 					
 					PaneInitialization();
 				}
 			});
 	}
 
 	public void removeRequirement(int requirementId, final int flexRowID)
 	{
 		boolean response = Window.confirm(messages.confirmDelete() + "?");
 		if (response)
 		{
 			this.service.deleteRequirement(requirementId, currentState.getProjectID(), new AsyncCallback<Void>()
 				{
 
 					
 					public void onFailure(Throwable caught)
 					{
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.errorRemovingRequirements());
 					}
 
 					public void onSuccess(Void result)
 					{
 
 						requirementTable.removeRow(flexRowID);
 
 					}
 				});
 		}
 	}
 
 	// ------------------------------------------------------
 	// Risk Summary Rendering calls section
 	// ------------------------------------------------------
 
 	public void PaneInitialization()
 	{
 		this.hideStatusBar();
 		this.getContent().clear();
 		HorizontalPanel hPanel = new HorizontalPanel();
 		hPanel.setWidth("75%");
 		
 		hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (currentState.getUserProjectRole()==ProjectRole.Lead_Requirements_Engineer
 				|| currentState.getUserProjectRole()==ProjectRole.Requirements_Engineer)
 		{
 			if(currentState.getMode().equals(GwtModesType.ReadWrite))
 			{
 				createImportPanel(hPanel, getCurrentState().getProjectID());
 			}
 		}
 		this.getContent().add(hPanel);
 		loadRiskTable();
 		this.getContent().add(requirementTable);
 		
 		FlexTable buttonPanel = new FlexTable();
 		Button done = new Button(messages.done());
 		done.addStyleName("square-button");
		
		final int caseid = this.getCurrentState().getCaseID();
		
 		done.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event)
 			{
				if(caseid==1)
					History.newItem(ChooseStepPilot.generateNavigationId(ChooseStepPilot.PageId.home));
				if(caseid==3)
					History.newItem(ChooseStepCase3Pilot.generateNavigationId(ChooseStepCase3Pilot.PageId.home));
 				
 			}});
 		
 		buttonPanel.setWidget(0,0,new Label(" "));
 		buttonPanel.setWidget(1, 0, done);
 		buttonPanel.setWidth("75%");
 		buttonPanel.getCellFormatter().setHorizontalAlignment(1,0,HasHorizontalAlignment.ALIGN_RIGHT);
 		this.getContent().add(buttonPanel);
 	}
 	public void loadRiskTable()
 	{
 		initializeRiskTable();
 		loadRiskItemsToTable();
 
 	}
 	public void initializeRiskTable()
 	{
 		this.requirementTable.clear();
 		this.requirementTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		this.requirementTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		this.requirementTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 
 		this.requirementTable.getCellFormatter().setStyleName(0, 0, "square-TableHeader");
 		this.requirementTable.getCellFormatter().setStyleName(0, 1, "square-TableHeader");
 		this.requirementTable.getCellFormatter().setStyleName(0, 2, "square-TableHeader");
 
 		this.requirementTable.setWidth("75%");
 		this.requirementTable.setCellSpacing(0);
 		this.requirementTable.setStyleName("square-flex");
 		this.requirementTable.getCellFormatter().setWidth(0, 0, "50%");
 		this.requirementTable.getCellFormatter().setWidth(0, 1, "5%");
 		this.requirementTable.getCellFormatter().setWidth(0, 2, "5%");
 
 		this.requirementTable.getCellFormatter().setHeight(0, 0, "30px");
 
 		addRequirement = new Button(messages.buttonAddRequirement());
 		addRequirement.addClickHandler(new ClickHandler()
 			{
 
 				public void onClick(ClickEvent event)
 				{
 					currentState.setCurrentRisk(-1);// No risk in context for
 					// next pane
 					currentState.setRiskCommand(0);
 					History.newItem(ElicitSecurityRequirementsPilot.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementDetail));
 
 				}
 			});
 
 	}
 	public void loadRiskItemsToTable()
 	{
 		if (currentState.getMode() == GwtModesType.ReadWrite)
 		{
 			requirementTable.setWidget(0, 0, addRequirement);
 		
 		}
 		else
 		{
 			requirementTable.setWidget(0, 0, new Label(messages.requirementTitle()));
 		}
 		
 		int rowNumber = 1;
 		for (GwtRequirement r : listOfRequirements)
 		{
 
 			final SummaryElementHyperLinkElement requiremenTitle = new SummaryElementHyperLinkElement(r.getId(), r.getTitle());
 			requirementTable.setWidget(rowNumber, 0, requiremenTitle);
 			requiremenTitle.addClickHandler(new ClickHandler()
 				{
 					public void onClick(ClickEvent event)
 					{
 						currentState.setCurrentRisk(requiremenTitle.getRequirementID());
 						currentState.setRiskCommand(1);
 						History.newItem(ElicitSecurityRequirementsPilot
 								.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementDetail));
 
 					}
 				});
 
 			final SummaryElementHyperLinkElement change = new SummaryElementHyperLinkElement(r.getId(), messages.change());
 			change.addClickHandler(new ClickHandler()
 				{
 
 					public void onClick(ClickEvent event)
 					{
 						currentState.setCurrentRisk(requiremenTitle.getRequirementID());
 						currentState.setRiskCommand(2);
 						History.newItem(ElicitSecurityRequirementsPilot
 								.generateNavigationId(ElicitSecurityRequirementsPilot.PageId.requirementDetail));
 
 					}
 				});
 
 			final SummaryElementHyperLinkElement remove = new SummaryElementHyperLinkElement(r.getId(), messages.remove());
 			remove.addClickHandler(new ClickHandler()
 				{
 
 					public void onClick(ClickEvent event)
 					{
 
 						Integer rowId = requirementTable.getCellForEvent(event).getRowIndex();
 						removeRequirement(remove.requirementID, rowId);
 
 					}
 				});
 
 			
 
 			if (currentState.getMode() == GwtModesType.ReadWrite)
 			{
 
 				//if(! r.getStatus().equals("Approved"))
 				//{
 					requirementTable.setWidget(rowNumber, 1, change);
 					requirementTable.setWidget(rowNumber, 2, remove);
 				//}
 			}
 			else
 			{
 		
 				requirementTable.setWidget(rowNumber, 1, new Label(" "));
 				requirementTable.setWidget(rowNumber, 2, new Label(" "));
 			}
 
 			requirementTable.getCellFormatter().setHorizontalAlignment(rowNumber, 0, HasHorizontalAlignment.ALIGN_LEFT);
 			requirementTable.getCellFormatter().setHorizontalAlignment(rowNumber, 1, HasHorizontalAlignment.ALIGN_CENTER);
 			requirementTable.getCellFormatter().setHorizontalAlignment(rowNumber, 2, HasHorizontalAlignment.ALIGN_CENTER);
 
 			rowNumber++;
 		}
 		if (rowNumber == 1)
 		{
 		
 			DisclosurePanel diclosure = new DisclosurePanel();
 			diclosure.add(new Label(messages.noRequirementsFound()));
 
 			diclosure.setAnimationEnabled(true);
 			diclosure.setOpen(true);
 			requirementTable.setWidget(1, 0, diclosure);
 			requirementTable.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
 			requirementTable.getCellFormatter().setStyleName(1, 0, "inner-table");
 			requirementTable.getFlexCellFormatter().setColSpan(1, 0, 3);
 		}
 
 	}
 	protected void createImportPanel(Panel statusPanel, int projectId)
 	{
 		final DisclosurePanel importDisclosurePanel = new DisclosurePanel();
 		importDisclosurePanel.setAnimationEnabled(true);
 		SquareHyperlink importLink = new SquareHyperlink(messages.importRequirements());
 
 		VerticalPanel importPanel = new VerticalPanel();
 		final ListBox importOptions = new ListBox();
 
 		importOptions.addItem(""); // index 0
 		
 		importOptions.addItem(messages.uploadFileFormat());
 
 		// create a formPanel
 		final FormPanel uploadForm = new FormPanel();
 		final String actionString = GWT.getModuleBaseURL() + "requirementsUploader?projectId=" + projectId + "&sessionId=" + currentState.getSessionID();
 		uploadForm.setAction(actionString);
 		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
 		uploadForm.setMethod(FormPanel.METHOD_POST);
 
 		// gwt file chooser
 		
 		final FileUpload upload = new FileUpload();
 		upload.setName("uploadFormElement");
 		upload.setStyleName("square-fileupload");
 		
 		Button submitButton = new Button(messages.upload());
 		submitButton.addClickHandler(new ClickHandler()
 			{
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					String selectedOption = importOptions.getItemText(importOptions.getSelectedIndex());
 					if (selectedOption.equals(""))
 					{
 						Window.alert(messages.errorImportOption());
 						return;
 					}
 					
 					String filePath = upload.getFilename();
 					if (!filePath.endsWith(".csv")
 						&& !filePath.endsWith(".CSV"))
 					{
 						Window.alert(messages.errorFilePath(filePath));
 						return;
 					}
 					
 						uploadForm.setAction(actionString + "&fileType=" + importOptions.getItemText(importOptions.getSelectedIndex()));
 						showLoadingStatusBar();
 						importDisclosurePanel.setOpen(false);
 						uploadForm.submit();
 					
 
 				}
 			});
 		
 		uploadForm.addSubmitHandler(new FormPanel.SubmitHandler()
 			{
 
 				@Override
 				public void onSubmit(SubmitEvent event)
 				{
 					// before submission, you can check auth
 
 				}
 
 			});
 
 		// uploadForm handler
 		uploadForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler()
 			{
 				public void onSubmitComplete(SubmitCompleteEvent event)
 				{
 				
 					// When the form submission is successfully completed, this
 					// event is fired. Assuming the service returned a response
 					// of type
 					// text/html, we can get the result text here (see the
 					// FormPanel
 					// documentation for further explanation).
 					
 					if (event.getResults().indexOf("400")>-1)
 					{
 						Window.alert(messages.titleDescriptionMissing());
 					} 	
 					if (event.getResults().indexOf("417")>-1)
 					{
 						Window.alert(messages.parsingError());
 					}
 					if (event.getResults().indexOf("404")>-1)
 					{
 						Window.alert(messages.emptyFile());
 					}
 					loadRequirementFromProject();
 					
 					
 					
 					//Window.alert(event.getResults());
 				}
 			});
 
 		importPanel.add(upload);
 		importPanel.add(importOptions);
 		importPanel.add(submitButton);
 
 		uploadForm.setWidget(importPanel);
 		importDisclosurePanel.setContent(uploadForm);
 
 		importLink.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					hideStatusBar();
 				
 					if (!importDisclosurePanel.isOpen())
 					{
 						importDisclosurePanel.setOpen(true);
 					}
 					else
 					{
 						importDisclosurePanel.setOpen(false);
 					}
 				}
 
 			});
 		statusPanel.add(importLink);
 		statusPanel.add(importDisclosurePanel);
 	}
 
 	
 	class SummaryElementHyperLinkElement extends SquareHyperlink
 	{
 
 
 		private int requirementID;
 		public SummaryElementHyperLinkElement(int requirementID, String text)
 			{
 				super(text);
 				this.requirementID = requirementID;
 			}
 
 		public void setRequirementID(int requirementID)
 		{
 			this.requirementID = requirementID;
 		}
 
 		public int getRequirementID()
 		{
 			return requirementID;
 		}
 
 	}
 
 }
