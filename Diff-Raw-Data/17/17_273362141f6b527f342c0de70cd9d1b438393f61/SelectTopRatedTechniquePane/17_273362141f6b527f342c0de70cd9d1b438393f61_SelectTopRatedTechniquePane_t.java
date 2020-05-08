 package edu.cmu.square.client.ui.SelectSecurityTechnique;
 
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
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtTechnique;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.interfaces.ManageProjectService;
 import edu.cmu.square.client.remoteService.interfaces.ManageProjectServiceAsync;
 import edu.cmu.square.client.ui.ChooseStep.ChooseStepPilot;
 import edu.cmu.square.client.ui.core.BasePane;
 
 public class SelectTopRatedTechniquePane extends BasePane
 {
 
 	private ManageProjectServiceAsync service = GWT.create(ManageProjectService.class);
 	private FlexTable ratedTechniquesTable = new FlexTable();
 	private FlexTable rationaleTable = new FlexTable();
 	private FlexTable summaryTable = new FlexTable();
 	
 
 	private String rationale = "";
 	int selectedTechnique;
 	int selectedRow;
 	final TextArea textArea = new TextArea();
 	final SelectSecurityTechniquePaneMessages messages =  (SelectSecurityTechniquePaneMessages)GWT.create(SelectSecurityTechniquePaneMessages.class);
 	
 	Label rationalLabel = new Label(messages.labelRational());
 	Button saveButton; 
 
 	private DisclosurePanel disPanel;
 	final Label stepTitle = new Label(messages.labelstepTitle());
 	
 	public SelectTopRatedTechniquePane(State stateInfo)
 	{
 		super(stateInfo);
 		this.showLoadingStatusBar();
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageProject.rpc");
 		selectedRow = -1;
 		selectedTechnique = -1;
 		
 		stepTitle.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		stepTitle.setStyleName("step-Title-heading");
		final List<GwtTechnique> list= null;//stateInfo.getTopTechniqueList();
//		TODO
 		this.service.getProject(this.getCurrentState().getProjectID(), new AsyncCallback<GwtProject>()
 		{
 			public void onFailure(Throwable caught) 
 			{
 				Window.alert(caught.getMessage());		
 			}
 
 			public void onSuccess(GwtProject result) 
 			{
 			     //Verify is there is an already selected technique in the DB.	
 				if(result.getSecurityTechniqueID()!=-1)
 				{
 					selectedTechnique = result.getSecurityTechniqueID();
 					rationale = result.getSecurityRational();
 				}
 				
 				
 				if (GwtModesType.ReadWrite == currentState.getMode())
 				{
 					getContent().add(stepTitle);
 					loadTechniqueToTable(list);
 					loadRationaleTable();
 					
 					disPanel = new DisclosurePanel();
 					disPanel.setAnimationEnabled(true);
 					Label errorMessage =new Label(messages.labelRationaleRequired());
 					errorMessage.setStyleName("square-RequiredMessage");
 					disPanel.add(errorMessage);
 	
 					getContent().add(disPanel);
 					getContent().add(ratedTechniquesTable);
 					getContent().add(rationaleTable);
 				}
 				else
 				{
 					SwitchToSummaryMode(selectedTechnique,rationale);
 				}
 
 				hideStatusBar();
 			}
 		});	
 	}
 	
 	public void SwitchToSummaryMode(int selectedTechniqueID, String rationale)
 	{   
 		getContent().clear();
 		getContent().add(stepTitle);
 		loadSummaryToTable(GwtTechinqueById(selectedTechniqueID),rationale);
 		getContent().add(summaryTable);
 	}
 	
 	private void setProjectTechnique(int projectID, final int techniqueID, final String rationale)
 	{
 		this.service.updateProjectTechnique(projectID, techniqueID, rationale, new AsyncCallback<Void>()
 		{
 			public void onFailure(Throwable caught)
 			{
 				if (caught instanceof SquareException)
 				{
 					SquareException se = (SquareException) caught;
 					switch (se.getType())
 					{
 						case authorization:
 							Window.alert(messages.errorAuthorizedSelectTechnique());
 							break;
 				
 						default:
 							Window.alert(messages.errorUpdateProjectTechnique());
 							break;
 					}
 				}
 				else
 				{
 					Window.alert(messages.errorUpdateProjectTechnique());
 				}
 			}
 
 			
 			public void onSuccess(Void result)
 			{
 				SwitchToSummaryMode(techniqueID,rationale);	
 			}
 		});
 	}
 	
 	
 	public void hideRationale(boolean hide)
 	{
 		saveButton.setEnabled(!hide);
 		textArea.setVisible(!hide);
 		rationalLabel.setVisible(!hide);
 	}
 	
 	
 	
 	public void loadRationaleTable()
 	{
 		final FlexCellFormatter formatter1 = this.rationaleTable.getFlexCellFormatter();
 		
 		rationalLabel.setStyleName("section-heading");
 		textArea.setWidth("100%");
 		textArea.setHeight("100px");
 		Button reviseMatrix = new Button(messages.buttonGotoMatrix());
 
 		saveButton = new Button (messages.buttonSaveSelection());
 	
 		if (selectedTechnique == -1)
 		{
 			hideRationale(true);
 		}
 		
 		reviseMatrix.addStyleName("square-button");
 		saveButton.addStyleName("square-button");
 
 		rationaleTable.setWidget(0, 0, rationalLabel);
 		rationaleTable.setWidget(1, 0, textArea);
 		rationaleTable.setWidget(2, 0, reviseMatrix);
 		rationaleTable.setWidget(2, 1, saveButton);
 
 		formatter1.setColSpan(1, 0, 2);
 
 		this.rationaleTable.setWidth("75%");
 		this.rationaleTable.setCellSpacing(0);
 		
 		
 		formatter1.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter1.setWidth(0, 0, "50%");
 		formatter1.setWidth(0, 1, "50%");
 		
 		saveButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				if(validateRequiredFields())
 				{
 					setProjectTechnique(getCurrentState().getProjectID(),selectedTechnique,textArea.getText().trim());
 				}
 			}
 		});
 		
 		reviseMatrix.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				History.newItem(SelectSecurityElicitationTechniquePilot.generateNavigationId(SelectSecurityElicitationTechniquePilot.PageId.matrix));
 			}
 		});
 	}
 
 	public void loadTechniqueToTable(List<GwtTechnique> tecniqueList)
 	{
 		final FlexCellFormatter formatter1 = this.ratedTechniquesTable.getFlexCellFormatter();
 		this.ratedTechniquesTable.clear();
 		this.ratedTechniquesTable.setWidget(0, 0, new Label(messages.ratedTechniquesTableTitle()));
 		this.ratedTechniquesTable.setWidget(0, 1, new Label(messages.ratedTechniquesTableDescription()));
 		this.ratedTechniquesTable.setWidget(0, 2, new Label(messages.ratedTechniquesTableSelection()));
 		
 		formatter1.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		formatter1.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		formatter1.setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 		formatter1.setStyleName(0, 0, "square-TableHeader");
 		formatter1.setStyleName(0, 1, "square-TableHeader");
 		formatter1.setStyleName(0, 2, "square-TableHeader");
 		
 		int numberOfRows = 1;
 		for (GwtTechnique tecnique: tecniqueList)
 		{		
 			this.ratedTechniquesTable.setWidget(numberOfRows, 0, new Label(tecnique.getTitle()));
 			this.ratedTechniquesTable.setWidget(numberOfRows, 1, new Label(tecnique.getDescription()));
 			final TechniqueRadioButton radio = new TechniqueRadioButton("top");
 			radio.setTecniqueID(tecnique.getTechniqueId());
 			radio.setRowID(numberOfRows);
 		
 			
 			//Initial selection
 			if (selectedTechnique == tecnique.getTechniqueId())
 			{
 				selectedRow=radio.getRowID();
 				formatter1.setStyleName(numberOfRows, 0, "square-Table-SelectedRow");
 				formatter1.setStyleName(numberOfRows, 1, "square-Table-SelectedRow");
 				formatter1.setStyleName(numberOfRows, 2, "square-Table-SelectedRow");
 				radio.setValue(true);
 				
 				textArea.setText(rationale);
 			}
 			this.ratedTechniquesTable.setWidget(numberOfRows, 2, radio);
 			radio.addClickHandler(new ClickHandler()
 			{		
 				public void onClick(ClickEvent event)
 				{
 					if(selectedRow!=-1)
 					{
 						formatter1.removeStyleName(selectedRow, 0,"square-Table-SelectedRow");
 						formatter1.removeStyleName(selectedRow, 1,"square-Table-SelectedRow");
 						formatter1.removeStyleName(selectedRow, 2,"square-Table-SelectedRow");
 					}
 					hideRationale(false);
 					selectedRow=radio.getRowID();
 					formatter1.setStyleName(selectedRow, 0,"square-Table-SelectedRow");
 					formatter1.setStyleName(selectedRow, 1,"square-Table-SelectedRow");
 					formatter1.setStyleName(selectedRow, 2,"square-Table-SelectedRow");
 					
 					selectedTechnique=radio.getTecniqueID();
 				
 				}});
 			
 
 			
 			
 
 			formatter1.setHorizontalAlignment(numberOfRows, 0, HasHorizontalAlignment.ALIGN_LEFT);
 			formatter1.setHorizontalAlignment(numberOfRows, 1, HasHorizontalAlignment.ALIGN_LEFT);
 			formatter1.setHorizontalAlignment(numberOfRows, 2, HasHorizontalAlignment.ALIGN_CENTER);
 			numberOfRows++;
 		}
 		
 		this.ratedTechniquesTable.setWidth("75%");
 		this.ratedTechniquesTable.setCellSpacing(0);
 		this.ratedTechniquesTable.setStyleName("square-flex");
 		formatter1.setWidth(0, 0, "30%");
 		formatter1.setWidth(0, 1, "40%");
 		formatter1.setWidth(0, 2, "30%");
 	
 
 	}
 	
 	private GwtTechnique GwtTechinqueById(int id)
 	{
//		/TODO
		List<GwtTechnique> list= null;//this.getCurrentState().getTopTechniqueList();
 		for (GwtTechnique t: list)
 		{
 			if(id==t.getTechniqueId())
 			{
 				return t;
 			}
 		}
 		return null;
 	}
 	
 	public void loadSummaryToTable(GwtTechnique technique, String rationale)
 	{
 		Label techniqueTitle = new Label(" ");
 		Label techniqueDescription = new Label(messages.noSelection());
 		Label rationaleLabel = new Label(rationale);
 		Button goToMatrix = new Button(messages.reviseMatrix());
         goToMatrix.addStyleName("square-button");
 		goToMatrix.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				History.newItem(SelectSecurityElicitationTechniquePilot.generateNavigationId(SelectSecurityElicitationTechniquePilot.PageId.matrix));
 				
 			}
 		});
 		
 		Button goHomeButton = new Button(messages.done());
 		goHomeButton.addStyleName("square-button");
 		goHomeButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				History.newItem(ChooseStepPilot.generateNavigationId(ChooseStepPilot.PageId.home));
 			}
 		});
 		
 		if (technique != null)
 		{
 			 techniqueTitle.setText(technique.getTitle());
 			 techniqueDescription.setText(technique.getDescription());
 		}
 		
 		
 		final FlexCellFormatter formatter1 = this.summaryTable.getFlexCellFormatter();
 		this.summaryTable.clear();
 		this.summaryTable.setWidget(0, 0, new Label(messages.ratedTechniquesTableTitle()));
 		this.summaryTable.setWidget(0, 1, new Label(messages.ratedTechniquesTableDescription()));
 		this.summaryTable.setWidget(1, 0, techniqueTitle);
 		this.summaryTable.setWidget(1, 1, techniqueDescription);
 		this.summaryTable.setWidget(0, 1, new Label(messages.ratedTechniquesTableDescription()));
 		this.summaryTable.setWidget(2, 0, new Label(messages.rationale()));
 		this.summaryTable.setWidget(3, 0, rationaleLabel);
 		this.summaryTable.setWidget(4, 0, new Label(" "));
 		this.summaryTable.setWidget(5, 0, goToMatrix);
 		this.summaryTable.setWidget(5, 1, goHomeButton);
 		
 		
 		rationaleLabel.setText(rationale);
 		formatter1.setColSpan(2, 0, 2);
 		formatter1.setColSpan(3, 0, 2);
 		formatter1.setColSpan(4, 0, 2);
 		formatter1.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(5, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(5, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		formatter1.setStyleName(0, 0,"square-TableHeader");
 		formatter1.setStyleName(0, 1,"square-TableHeader");
 		formatter1.setStyleName(2, 0,"square-TableHeader");
 		formatter1.setStyleName(3, 0,"square-Table-SelectedRow");
 		formatter1.setStyleName(4, 0,"square-Table-SelectedRow");
 	
 		this.summaryTable.setWidth("75%");
 		this.summaryTable.setCellSpacing(0);
 		//this.summaryTable.setStyleName("square-flex");
 		formatter1.setWidth(0, 0, "30%");
 		formatter1.setWidth(0, 1, "70%");
 	}
 	
 		
 	public void setValidationStyle(Widget w, boolean valid)
 	{
 		if (!valid)
 		{
 			w.setStyleName("square-RequiredField");	
 			
 			if (w instanceof ListBox)
 			{
 				w.setStyleName("square-RequiredFieldListBox");	
 			}
 		}
 		else
 		{
 			w.setStyleName("square-flex");	
 			
 			if (w instanceof ListBox)
 			{
 				w.setStyleName("Risk-Input-listBox");	
 			}
 		}
 	}
 	
 	public boolean validateRequiredFields()
 	{
 		boolean isValid = true;
 		boolean isValidSelection = true;
 		String textValue = textArea.getText().trim();
 		
 		if (selectedRow == -1)
 		{
 			isValidSelection = false;	
 		}
 		
 		if (textValue.length() == 0) 	
 		{	
 			 isValid = false;
 		}
 		
 		setValidationStyle(this.ratedTechniquesTable, isValidSelection);
 		setValidationStyle(textArea, isValid);
 		
 		boolean valid = isValid && isValidSelection;
 		disPanel.setOpen(!valid);
 		
 		return valid;
 	}
 	
 	
 	
 	class TechniqueRadioButton extends RadioButton
 	{
 		private int tecniqueID;
 		private int rowID;
 
 		public TechniqueRadioButton(String name)
 		{
 			super(name);
 		}
 
 		public void setTecniqueID(int tecniqueID)
 		{
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID()
 		{
 			return tecniqueID;
 		}
 
 		public void setRowID(int rowID)
 		{
 			this.rowID = rowID;
 		}
 
 		public int getRowID()
 		{
 			return rowID;
 		}
 	}
 
 	
 	
 
 }
