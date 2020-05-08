 package edu.cmu.square.client.ui.SelectSecurityTechnique;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtEvaluation;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtTechnique;
 import edu.cmu.square.client.model.GwtTechniqueEvaluationValue;
 import edu.cmu.square.client.model.StepStatus;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.ElicitationTechniqueService;
 import edu.cmu.square.client.remoteService.step.interfaces.ElicitationTechniqueServiceAsync;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 
 
 public class SelectSecurityTechniquePane extends BasePane
 {
 	final SelectSecurityTechniquePaneMessages messages =  (SelectSecurityTechniquePaneMessages)GWT.create(SelectSecurityTechniquePaneMessages.class); 
 	private ElicitationTechniqueServiceAsync service = GWT.create(ElicitationTechniqueService.class);
 	private GwtProject currentProject;
 	private FlexTable matrix = new FlexTable();
 	private FlexTable matrixHeader = new FlexTable();
 	SquareHyperlink editRatingsLink = new SquareHyperlink(messages.editRatingsLink());		
 	SquareHyperlink finishEditLink = new SquareHyperlink(messages.finishEditLink());
 	Button cmdLoadTopTechnique = new Button(messages.buttonGotoSelect());
 	boolean isReadOnly=false;
 	
 	//These list are loaded sequentially by RPC calls initially.
 	private List<GwtTechnique> listOfTechniques;
 	private List<GwtEvaluation> listOfEvaluation;
 	private List<GwtTechniqueEvaluationValue> listOfRateValues;
 
 	public SelectSecurityTechniquePane(final State stateInfo)
 	{
 		super(stateInfo);
 		
 		this.showLoadingStatusBar();
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "elicitationTechniques.rpc");
 		
 		currentProject = new GwtProject();
 		currentProject.setId(this.getCurrentState().getProjectID());
 		
 		
 	
 		/**
 		 * load the comparison matrix in the read-only mode
 		 */
 		isReadOnly = true;
 		loadEvaluationsCriteria();
 		
 	}
 	
 	public void PaneInitialization()
 	{
 		this.hideStatusBar();
 		VerticalPanel layout = new VerticalPanel();
 		editRatingsLink.addClickHandler(new ClickHandler(){
 
 		
 			public void onClick(ClickEvent event) {
 				isReadOnly=false;
 				//loadEvaluationsCriteria();
 				drawRateMatrix();
 				getTotalsFromMatrix();
 				PaneInitialization();
 				changeLink();
 				
 			}});
 		finishEditLink.addClickHandler(new ClickHandler(){
 
 		
 			public void onClick(ClickEvent event) {
 				isReadOnly=true;
 				//loadEvaluationsCriteria();
 				drawRateMatrix();
 				getTotalsFromMatrix();
 				PaneInitialization();
 				changeLink();
 				
 			}});
 		
 		drawMatrixPage();
 
 		cmdLoadTopTechnique.addClickHandler(new ClickHandler(){
 
 			
 			public void onClick(ClickEvent event) {
 				List<GwtTechnique> list= getTopValues();
//			TODO	
//				currentState.setTopTechniqueList(list);
 				
 				History.newItem(SelectSecurityElicitationTechniquePilot.generateNavigationId(SelectSecurityElicitationTechniquePilot.PageId.selectTechnique));
 			}});
 
 		cmdLoadTopTechnique.addStyleName("square-button");
 		
 		this.getContent().clear();
 		//layout.add(comparisonMatrixLabel);
 		layout.add(matrixHeader);
 		this.getContent().add(layout);
 		
 		//initWidget(layout);
 	}
 	
 	private void changeLink()
 	{
 		if(isReadOnly)
 		{
 			this.matrixHeader.setWidget(2, 1, editRatingsLink);
 			this.matrixHeader.setWidget(3, 1, matrix);
 			
 		}
 		else
 		{
 			this.matrixHeader.setWidget(2, 1, finishEditLink);
 			this.matrixHeader.setWidget(3, 1, matrix);
 		}		
 	}
 	
 	private void drawMatrixPage()
 	{
 		FlexCellFormatter formatter1 = this.matrixHeader.getFlexCellFormatter();
 		this.matrixHeader.setWidth("100%");
 
 		this.matrixHeader.setWidget(1, 1, new Label(messages.matrixLableX()));
 		this.matrixHeader.setWidget(3, 0, new Label(messages.matrixLableY()));
 		this.matrixHeader.setWidget(4, 1, new Label(messages.rateLegend()));
 		this.matrixHeader.setWidget(5, 1, cmdLoadTopTechnique);
 		
 		formatter1.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		formatter1.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter1.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_MIDDLE);
 		formatter1.setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(5, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		if(isReadOnly)
 		{
 			if(GwtModesType.ReadWrite==this.currentState.getMode())
 			{
 				this.matrixHeader.setWidget(2, 1, editRatingsLink);
 			}
 			
 			this.matrixHeader.setWidget(3, 1, matrix);
 			
 			
 			
 		}
 		else
 		{
 			this.matrixHeader.setWidget(2, 1, finishEditLink);
 			this.matrixHeader.setWidget(3, 1, matrix);
 		}
 		
 	}
 	
 	/**
 	 * Contains RPC Calls to retrieve the evaluation criteria for the project
 	 */
 	private void loadEvaluationsCriteria()
 	{
 		final StepStatus stepStatus =  StepStatus.NotStarted;
 		this.service.getEvaluations(currentProject, stepStatus, new AsyncCallback<List<GwtEvaluation>>()
 		{
 			
 			public void onFailure(Throwable caught)
 			{
 				Window.alert(messages.evaluationRetrievalError());
 			}
 			
 			public void onSuccess(List<GwtEvaluation> result)
 			{
 				listOfEvaluation =result;
 				loadTechniques();
 				
 			}
 		});
 	}
 	/**
 	 * Contains RPC Calls to retrieve the techniques for project
 	 */
 	private void loadTechniques()
 	{
 		final StepStatus stepStatus =  StepStatus.NotStarted;
 		
 		this.service.getTechniques(currentProject, stepStatus, new AsyncCallback<List<GwtTechnique>>()
 		{
 			
 			public void onFailure(Throwable caught)
 			{
 				Window.alert(messages.evaluationRetrievalError());
 			}
 			
 			public void onSuccess(List<GwtTechnique> result)
 			{
 				listOfTechniques=result;
 				loadRateValues();
 			}
 		});
 	}
 	/**
 	 * Contains RPC Calls to retrieve the rates for the project
 	 */
 	private void loadRateValues()
 	{
 		
 		this.service.getRateValues(currentProject.getId(), new AsyncCallback<List<GwtTechniqueEvaluationValue>>()
 		{
 			
 			public void onFailure(Throwable caught)
 			{
 				Window.alert(messages.evaluationRetrievalError());
 			}
 			
 			public void onSuccess(List<GwtTechniqueEvaluationValue> result)
 			{
 				listOfRateValues=result;
 				drawRateMatrix();
 				getTotalsFromMatrix();
 				PaneInitialization();
 				
 			}
 		});
 	}
 	/**
 	 * Contains RPC call to Set the rates
 	 * @param techniqueID
 	 * @param evaluationID
 	 * @param value
 	 */
 	private void setRateValue(final int techniqueID, final int evaluationID, final int value)
 	{
 	
 		
 		this.service.setRateValue(currentProject.getId(), techniqueID, evaluationID, value, new AsyncCallback<Void>(){
 
 			
 			public void onFailure(Throwable caught) {
 				if (caught instanceof SquareException) {
 					SquareException se = (SquareException) caught;
 					switch (se.getType()) {
 					case authorization:
 						Window.alert(messages.rateAuthorization());
 						break;
 				
 					default:
 						Window.alert(messages.error());
 						break;
 					}
 
 				} else {
 					Window.alert(messages.error());
 				}
 				
 			}
 
 			
 			public void onSuccess(Void result) {
 				setValueFromlistOfRateValues(techniqueID, evaluationID, value);
 				
 			}});
 	
 	}
 	
 	public void drawRateMatrix()
 	{
 		matrix.clear();
 		//matrix.setBorderWidth(1);
 		matrix.setWidth("100%");
 		matrix.setStyleName("square-Matrix");
 		matrix.setCellSpacing(0);
 		drawRateMatrixHeaderTechniques();
 		drawRateMatrixEvaluationCriteriaColum();
 		drawRateMatrixValues();
 
 	}
 	public void drawRateMatrixHeaderTechniques()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		matrix.setWidget(0, 0, new Label(" "));
 		formatter.setStyleName(0, 0, "square-Matrix");
 		
 		// Set the header rows  with techniques
 		for(int i=1; i<=listOfTechniques.size();i++)
 		{
 			Label techniqueLabel = new Label(listOfTechniques.get(i-1).getTitle());
 			
 				final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 			   // simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
 			    simplePopup.setWidth("150px");
 			    simplePopup.setWidget(new HTML(listOfTechniques.get(i-1).getDescription()));
 			
 			    techniqueLabel.addMouseOverHandler(new MouseOverHandler(){
 				
 				public void onMouseOver(MouseOverEvent event) 
 				{
 					Widget source = (Widget) event.getSource();
 		            int left = source.getAbsoluteLeft() + 20;
 		            int top = source.getAbsoluteTop() + 20;
 		            simplePopup.setPopupPosition(left, top);
 					simplePopup.show();
 
 				}});
 			    techniqueLabel.addMouseOutHandler(new MouseOutHandler(){
 
 					
 					public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();
 						
 					}});
 			    
 			    
 		
 			matrix.setWidget(0, i, techniqueLabel);
 			formatter.setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);
 			formatter.setStyleName(0, i, "square-Matrix");
 		}
 		
 	}
 	public void drawRateMatrixEvaluationCriteriaColum()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		// Set the left columns with the evaluation criteria 
 		for(int j=0; j<listOfEvaluation.size();j++)
 		{
 			
 			Label evaluationLabel = new Label(listOfEvaluation.get(j).getTitle());
 			
 			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 			    simplePopup.setWidth("150px");
 			    simplePopup.setWidget(new HTML(listOfEvaluation.get(j).getDescription()));
 			
 			    evaluationLabel.addMouseOverHandler(new MouseOverHandler(){
 				
 				public void onMouseOver(MouseOverEvent event) {
 					Widget source = (Widget) event.getSource();
 		            int left = source.getAbsoluteLeft() + 40;
 		            int top = source.getAbsoluteTop() + 20;
 		            simplePopup.setPopupPosition(left, top);
 					simplePopup.show();
 
 				}});
 			    evaluationLabel.addMouseOutHandler(new MouseOutHandler(){
 
 					
 					public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();
 						
 					}});
 			matrix.setWidget(j+1,0 , evaluationLabel);
 			formatter.setHorizontalAlignment(j+1,0 , HasHorizontalAlignment.ALIGN_RIGHT);
 			formatter.setStyleName(j+1,0 ,  "square-Matrix");
 			
 		}
 		matrix.setWidget(listOfEvaluation.size()+1, 0, new Label("Total"));
 		formatter.setHorizontalAlignment(listOfEvaluation.size()+1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter.setStyleName(listOfEvaluation.size()+1, 0,"square-Matrix");
 	}
 	public void drawRateMatrixValues()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		for(int i=0; i<listOfTechniques.size();i++)
 		{
 			
 			for(int j=0; j<listOfEvaluation.size();j++)
 			{
 				
 				int tID=listOfTechniques.get(i).getTechniqueId();
 				int eID=listOfEvaluation.get(j).getEvaluationId();
 				
 				int value = getValueFromlistOfRateValues(tID, eID);
 				
 				if(isReadOnly)
 				{
 					Label valueLabel = new Label(String.valueOf(value));
 					valueLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 					matrix.setWidget(j+1, i+1, valueLabel);
 					formatter.setHorizontalAlignment(j+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 					formatter.setStyleName(j+1, i+1,"square-Matrix");
 				}
 				else
 				{
 					//Allow only the digits 1,2 and 3 to be inputed in the rateValueTextBox
 					final RateValueTextbox rateValueTextbox =new RateValueTextbox(tID,eID, value);
 					rateValueTextbox.setTextAlignment(TextBox.ALIGN_CENTER);
 					rateValueTextbox.addKeyPressHandler(new KeyPressHandler(){	
 						public void onKeyPress(KeyPressEvent event) {
 							char keyCode= event.getCharCode();
 							 if (
 									 ( (keyCode< '0' || keyCode>'3')) && (keyCode != (char) KeyCodes.KEY_TAB)
 							            && (keyCode != (char) KeyCodes.KEY_BACKSPACE)
 							            && (keyCode != (char) KeyCodes.KEY_DELETE) && (keyCode != (char) KeyCodes.KEY_ENTER) 
 							            && (keyCode != (char) KeyCodes.KEY_HOME) && (keyCode != (char) KeyCodes.KEY_END)
 							            && (keyCode != (char) KeyCodes.KEY_LEFT) && (keyCode != (char) KeyCodes.KEY_UP)
 							            && (keyCode != (char) KeyCodes.KEY_RIGHT) && (keyCode != (char) KeyCodes.KEY_DOWN)) {
 								 	
 								 // rateValueTextbox.cancelKey() suppresses the current keyboard event.
 								 	rateValueTextbox.cancelKey();
 							 }
 						}
 						});
 						rateValueTextbox.addFocusHandler(new FocusHandler(){
 
 							
 							public void onFocus(FocusEvent event)
 							{
 								rateValueTextbox.setSelectionRange(0, rateValueTextbox.getText().length());
 								
 							}});
 				
 
 						
 						rateValueTextbox.addChangeHandler(new ChangeHandler(){
 						public void onChange(ChangeEvent event) {
 						//	Window.alert("a changed happened");
 							
 							if(rateValueTextbox.getText().trim().length()==0)
 							 {
 								 rateValueTextbox.setText("0");
 							 }
 							char keyCode;
 							
 							// Validation of the textbox from Copy and Paste
 							if(rateValueTextbox.getText().trim().length()==1)
 							{
 								keyCode=rateValueTextbox.getText().trim().charAt(0);
 							   if(keyCode>= '0' && keyCode<='3')
 							   {
 								   getTotalsFromMatrix();
 								   setRateValue(rateValueTextbox.getTecniqueID(),rateValueTextbox.getEvaluationID(),Integer.parseInt(rateValueTextbox.getText()));
 									rateValueTextbox.setOldValue(rateValueTextbox.getText());
 							   }
 							   else
 							   {
 								   rateValueTextbox.setText(rateValueTextbox.getOldValue());
 							   }
 							}
 							else
 							{
 								rateValueTextbox.setText(rateValueTextbox.getOldValue());
 							}
 
 							
 						}});
 
 					matrix.setWidget(j+1, i+1, rateValueTextbox);
 					formatter.setHorizontalAlignment(j+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 					formatter.setStyleName(j+1, i+1,"square-Matrix");
 				}
 				
 			
 			}
 			RateValueLabel totalLabel= new RateValueLabel(listOfTechniques.get(i).getTechniqueId());
 			totalLabel.setText("0");
 
 			matrix.setWidget(listOfEvaluation.size()+1, i+1,totalLabel);
 			formatter.setHorizontalAlignment(listOfEvaluation.size()+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 			formatter.setStyleName(listOfEvaluation.size()+1, i+1,"square-Matrix");
 						
 		}
 	}
 	/**
 	 * Get the totals traversing the current Matrix FlexTable
 	 */
 	public void getTotalsFromMatrix()
 	{
 		for(int i=1; i<=listOfTechniques.size();i++)
 		{	int sum=0;
 			
 			for(int j=1; j<=listOfEvaluation.size();j++)
 			{
 				
 					Widget w =  matrix.getWidget(j, i);
 					
 					if ( w instanceof RateValueTextbox) 
 					{
 						RateValueTextbox new_name = (RateValueTextbox) w;
 						//Window.alert(new_name.getText());
 						sum=sum+ Integer.parseInt(new_name.getText());
 					}
 					else if( w instanceof Label)
 					{
 						Label new_name = (Label) w;
 						//Window.alert(new_name.getText());
 						sum=sum+ Integer.parseInt(new_name.getText());
 					}
 					
 			}
 			
 				Widget totalLabel =  matrix.getWidget(listOfEvaluation.size()+1, i);
 
 				
 				RateValueLabel new_name = (RateValueLabel) totalLabel;
 		
 				new_name.setText(String.valueOf(sum));
 				//new_name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		
 		}
 		
 		
 	}
 
 
 	/**
 	 * Traverse the total row in the Matrix and get the top 1 rated technique
 	 * including ties
 	 * @return List<GwtTechnique> top 1 rated technique(s) remember it could be ties in 1st place
 	 */
 	private List<GwtTechnique> getTopValues()
 	{
 		List<GwtTechnique> topTechniques = new ArrayList<GwtTechnique>();
 		
 		int max=-1;
 		for(int i=1; i<=listOfTechniques.size();i++)
 		{
 				Widget widget =  matrix.getWidget(listOfEvaluation.size()+1, i);	
 				RateValueLabel totalLabel = (RateValueLabel) widget;
 			
 				if(max<Integer.parseInt(totalLabel.getText()) )
 					{
 						max=Integer.parseInt(totalLabel.getText());
 					}		
 		}
 		for(int i=1; i<=listOfTechniques.size();i++)
 		{
 				Widget widget =  matrix.getWidget(listOfEvaluation.size()+1, i);	
 				RateValueLabel totalLabel = (RateValueLabel) widget;
 			
 				if(max==Integer.parseInt(totalLabel.getText()) )
 					{
 						topTechniques.add(getTechniqueFromListOfTechniques(totalLabel.getTecniqueID()));
 					}		
 		}
 		
 		return topTechniques;
 		
 		
 	}
 	/**
 	 * This method search the techniques in the listloaded by RCP initially
 	 * @param tecniqueID
 	 * @return GwtTechnique for that ID
 	 */
 	private GwtTechnique getTechniqueFromListOfTechniques(int tecniqueID)
 	{
 		for(int i=0; i<listOfTechniques.size();i++)
 		{
 				if(listOfTechniques.get(i).getTechniqueId()==tecniqueID)
 				{
 					return 	listOfTechniques.get(i);
 				}
 		}
 		return null;
 	}
 	/**
 	 * This method search the rate values in the loaded by RCP initially
 	 * @param techniqueID
 	 * @param evaluationID
 	 * @return rateValue
 	 */
 	private int getValueFromlistOfRateValues(int techniqueID, int evaluationID)
 	{
 		
 		for(int j=0; j<listOfRateValues.size();j++)
 		{
 		
 			int eID=listOfRateValues.get(j).getEvaluationId();
 			int tID =listOfRateValues.get(j).getTechniqueId();
 			
 			if(techniqueID==tID && evaluationID==eID)
 			{
 				return listOfRateValues.get(j).getValue();
 			}
 			
 		}
 		return 0;
 
 	}
 	
 	private void setValueFromlistOfRateValues(int techniqueID, int evaluationID, int value)
 	{
 		
 		for(int j=0; j<listOfRateValues.size();j++)
 		{
 		
 			int eID=listOfRateValues.get(j).getEvaluationId();
 			int tID =listOfRateValues.get(j).getTechniqueId();
 			
 			if(techniqueID==tID && evaluationID==eID)
 			{
 				listOfRateValues.get(j).setValue(value);
 			}
 			
 		}
 	}
 	/**
 	 * 
 	 * This class extend the texbox to add 
 	 * the techniqueID and evaluationID as properties
 	 *
 	 */
 	class RateValueTextbox extends TextBox
 	{
 		private int tecniqueID;
 		private int evaluationID;
 		private String oldValue= "-1";
 		public RateValueTextbox(int tecniqueID, int evaluationID, int value) {
 		
 			this.tecniqueID=tecniqueID;
 			this.evaluationID=evaluationID;
 			
 			this.setWidth("35px");
 			this.setText(String.valueOf(value));;
 			this.setOldValue(String.valueOf(value));;
 			
 			// TODO Auto-generated constructor stub
 		}
 		public void setTecniqueID(int tecniqueID) {
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID() {
 			return tecniqueID;
 		}
 		public void setEvaluationID(int evaluationID) {
 			this.evaluationID = evaluationID;
 		}
 		public int getEvaluationID() {
 			return evaluationID;
 		}
 		public void setOldValue(String oldValue) {
 			this.oldValue = oldValue;
 		}
 		public String getOldValue() {
 			return oldValue;
 		}
 	}
 
 	/**
 	 * 
 	 * the techniqueID as properties
 	 *
 	 */
 	class RateValueLabel extends Label
 	{
 		private int tecniqueID;
 		public RateValueLabel(int tecniqueID) {
 		
 			this.tecniqueID=tecniqueID;
 			
 			this.setWidth("35px");
 			
 			// TODO Auto-generated constructor stub
 		}
 		public void setTecniqueID(int tecniqueID) {
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID() {
 			return tecniqueID;
 		}
 
 	}
 
 
 	
 }
