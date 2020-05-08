 package mil.af.rl.EPTEstimator.client;
 
 
 
 import java.util.ArrayList;
 
 
 
 import mil.af.rl.EPTEstimator.shared.FieldVerifier;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TextBoxBase;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import java.text.DecimalFormat;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class EPTEstimator implements EntryPoint {
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 */
 	private static final String SERVER_ERROR = "An error occurred while "
 			+ "attempting to contact the server. Please check your network "
 			+ "connection and try again.";
 
 	/**
 	 * Create a remote service proxy to talk to the server-side Greeting service.
 	 */
 	
 	private final CreateEPTAsync EPTService = GWT
 			.create(CreateEPT.class);
 
 	/**
 	 * This is the entry point method.
 	 */
 		
 
 	
 	public void onModuleLoad() {
 try{
 /* for use in setting focus in event naming dialog		
 		final EventBus eventBus = GWT.create(SimpleEventBus.class);
 		class ReadyForFocus extends GwtEvent<EventHandler>{
 			public Type<EventHandler> TYPE = new Type<EventHandler>();
 			
 				@Override
 			public com.google.gwt.event.shared.GwtEvent.Type<EventHandler> getAssociatedType(){
 				return (Type<EventHandler>)TYPE;
 			}
 				@Override
 			protected void dispatch(EventHandler handler){
 					
 				}
 		}
 */		
 		EPTService.MMCALCReset(// resets the server without saving anything
 				new AsyncCallback<String[]>(){
 					public void onFailure(Throwable caught){								
 					}
 					public void onSuccess(String[] result){
 					}
 				});
 		
 		final TextArea currentStep = new TextArea();
 		currentStep.setText("Ready to Add an Effect");
 		currentStep.setReadOnly(true);
 		currentStep.setStylePrimaryName("currentStep");
 		
 		final Button extendButton = new Button("Extend"); 
 		
 		final Button addEventButton = new Button("Add Effect");
 		addEventButton.setStylePrimaryName("addEvent");
 		addEventButton.setFocus(true);
 		final TextBox nameField = new TextBox();
 		nameField.setText("New Effect");
 		
 		final Button doneButton = new Button("Done");
 		doneButton.setStylePrimaryName("DoneStyle");
 		doneButton.setTitle("The EPT is complete");
 		
 		final TextBox marginalEvent = new TextBox();
 		marginalEvent.setText("0");
 		marginalEvent.setTitle("Index of powerset of base events in order added");
 		
 		final TextBox currentValue = new TextBox();
 		currentValue.setTitle("Current probability of indexed event");
 		currentValue.setText("1.0");
 		currentValue.setReadOnly(true);
 		
 		final Label lowerBound = new Label();
 		lowerBound.setText("<=");
 		lowerBound.setTitle("Minimum value of indexed event");
 		lowerBound.setStylePrimaryName("lowerBound");
 
 		final TextBox newValue = new TextBox();
 		newValue.setText("1.0");
 		newValue.setTitle("Edit probability of indexed event");
 		
 		final Label upperBound = new Label();
		upperBound.setText(">=");
 		upperBound.setTitle("Maximum value of indexed event");
 		
 		final Button setValueButton = new Button("Set Value");
 		setValueButton.setFocus(false);
 		setValueButton.setEnabled(false);
 		setValueButton.setStyleName("valueButton");
 		
 		final Button showMarginalButton = new Button("Show Marginal");
 		showMarginalButton.setStylePrimaryName("showMarginal");
 		
 		final Button correlationListOK = new Button("Correlation List OK");
 		correlationListOK.setStylePrimaryName("correlationListOK");
 		correlationListOK.setEnabled(false);
 		correlationListOK.setVisible(false);
 		correlationListOK.setFocus(false);
 		
 		final ArrayList<CheckBox> checkBoxList = new ArrayList<CheckBox>();
 		final FlexTable corTable = new FlexTable();
 		
 		final Grid marginalGrid = new Grid(64,2);
 		marginalGrid.setBorderWidth(1);
 		marginalGrid.setText(0, 0, "1.0");
 		
 		final Grid subsetGrid = new Grid(64,2);
 		subsetGrid.setBorderWidth(1);
 		
 		
 		final Grid pearsonGrid = new Grid(64,2);
 		pearsonGrid.setStylePrimaryName("jointGrid");
 		pearsonGrid.setBorderWidth(1);
 		pearsonGrid.setText(0, 00, "1.0");
 		pearsonGrid.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent e){
 				com.google.gwt.user.client.ui.HTMLTable.Cell clickedCell = pearsonGrid.getCellForEvent(e);
 				if(clickedCell != null){
 					String styleNames = pearsonGrid.getStyleName();
 					String OriginalprimaryStyle = pearsonGrid.getStylePrimaryName();
 					int cellIndex = clickedCell.getCellIndex();
 					int rowIndex = clickedCell.getRowIndex();
 					String primaryStyle = pearsonGrid.getRowFormatter().getStylePrimaryName(rowIndex);
 					pearsonGrid.getRowFormatter().setStyleName(rowIndex, "selected");
 					pearsonGrid.getCellFormatter().setStyleName(0, 0, "columnHeader");
 					primaryStyle = pearsonGrid.getRowFormatter().getStylePrimaryName(rowIndex);
 					primaryStyle = pearsonGrid.getRowFormatter().getStyleName(rowIndex);
 					int psIndex= new Integer(pearsonGrid.getHTML(rowIndex, cellIndex)).intValue();
 					int j = 0;
 					int k = 0;
 					for(CheckBox cb: checkBoxList){
 						if(((psIndex&(1<<j)) != 0)){
 							String name = cb.getText();
  							subsetGrid.setText(k++, 0, name);
 						}
 						j += 1;
 					}
 				}
 			}
 		});
 		
 		final ScrollPanel marginalScrollPanel = new ScrollPanel(marginalGrid);
 		marginalScrollPanel.setHeight("200px");
 		marginalScrollPanel.setAlwaysShowScrollBars(true);
 		final ScrollPanel jointScrollPanel = new ScrollPanel(pearsonGrid);
 		jointScrollPanel.setHeight("200px");
 		jointScrollPanel.setAlwaysShowScrollBars(true);
 		final ScrollPanel subsetScrollPanel = new ScrollPanel(subsetGrid);
 		subsetScrollPanel.setHeight("200px");
 		subsetScrollPanel.setAlwaysShowScrollBars(true);
 
 		// We can add style names to widgets
 		
 //		RootPanel.setStyleName("outerFrame");
 
 		doneButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent e){
 				EPTService.MMCALCReset(// will create a new EPT object on the server
 						new AsyncCallback<String[]>(){
 							public void onFailure(Throwable caught){								
 							}
 							public void onSuccess(String[] result){
 								marginalGrid.clear(true);
 								pearsonGrid.clear(true);
 								checkBoxList.clear();
 								corTable.clear(true); //has the buttons which determine events to correlate
 								correlationListOK.setEnabled(false);
 								correlationListOK.setVisible(false);
 								correlationListOK.setFocus(false);
 							}
 						});
 						;
 				return;
 			}
 		});
 
 		
 		RootPanel.get("currentStepContainer").add(currentStep);
 		RootPanel.get("correlationListContainer").add(corTable);
 		RootPanel.get("extendButtonContainer").add(extendButton);
 		RootPanel.get("addEventContainer").add(addEventButton);
 		RootPanel.get("doneContainer").add(doneButton);
 		RootPanel.get("marginalEventContainer").add(marginalEvent);
 		RootPanel.get("currentValueContainer").add(currentValue);
 		RootPanel.get("lowerBoundContainer").add(lowerBound);
 		RootPanel.get("newValueContainer").add(newValue);
 		RootPanel.get("upperBoundContainer").add(upperBound);
 		RootPanel.get("setValueContainer").add(setValueButton);
 		RootPanel.get("marginalGridContainer").add(marginalScrollPanel);
 		RootPanel.get("jointGridContainer").add(jointScrollPanel);
 		RootPanel.get("powerSubsetContainer").add(subsetScrollPanel);
 		RootPanel.get("showMarginalButtonContainer").add(showMarginalButton);
 		RootPanel.get("correlationListOKContainer").add(correlationListOK);
 
 		// Focus the cursor on the name field when the app loads
 		nameField.setFocus(true);
 		nameField.selectAll();
 
 		// Create the popup dialog box for adding a new event (name)
 		final DialogBox addEventDialogBox = new DialogBox();
 		addEventDialogBox.setText("Name New Event");
 		addEventDialogBox.setAnimationEnabled(true);
 		final TextBox eventField = new TextBox();
 		VerticalPanel dialogVPanel = new VerticalPanel();
 		dialogVPanel.addStyleName("dialogVPanel");
 		dialogVPanel.add(new HTML("Enter Name"));
 		dialogVPanel.add(eventField);
 		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
 		dialogVPanel.add(extendButton);
 		addEventDialogBox.setWidget(dialogVPanel);
 		
 		//Add a handler for "show marginal"
 		
 		class ShowMarginal implements ClickHandler{
 			public void onClick(ClickEvent event){
 				getMarginal();
 			}
 
 			private void getMarginal() {
 				
 				//EPTService.getPearsonCorrelations(
 				
 				EPTService.getPearsonCorrelations(
 						new AsyncCallback<String[]>(){
 							public void onFailure(Throwable caught){
 								
 							}
 							public void onSuccess(String[] pearsonCors){
 								int j = 0;
 								pearsonGrid.setText(j, 0, "Power Set Index");
 								pearsonGrid.setText(j, 1, "Pearson Correlation");
 								j += 1;
 								for(String cor: pearsonCors){
 									pearsonGrid.setText(j, 0, Integer.toString(j-1));
 									pearsonGrid.setText(j, 1, cor);
 									j++;
 								}
 							}
 						});
 				EPTService.getMarginalDistribution(
 						new AsyncCallback<String[]>(){
 							public void onFailure(Throwable caught){
 								
 							}
 							public void onSuccess(String[] marginalDistribution){
 								int j = 0;
 								marginalGrid.setText(j, 0, "Power Set Index");
 								marginalGrid.setText(j, 1, "Marginal Probability");
 								j += 1;
 								for(String prob: marginalDistribution){
 									marginalGrid.setText(j, 0, Integer.toString(j-1));
 									marginalGrid.setText(j, 1, prob);
 									j++;
 								}
 							}
 						});
 			}			
 		}
 		final ShowMarginal showMarginal = new ShowMarginal();
 		showMarginalButton.addClickHandler(showMarginal);
 		
 		
 		class Extender implements ClickHandler, KeyUpHandler{
 			Extender(){};
 /*			private void coreCorrelationListOK(){
 				correlationListOK.setEnabled(false);
 				currentStep.setText("Provide probabilities.");
 				coreExtender();	
 			}
 */			public void onClick(ClickEvent event){
 				getCorrelatesAndExtend();
 			}
 			public void onKeyUp(KeyUpEvent event) {
 				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
 					getCorrelatesAndExtend();
 				}
 			}
 			private void getCorrelatesAndExtend(){
 				
 				nameField.setText(eventField.getText());
 				eventField.setText("");
 				addEventDialogBox.hide();
 				CheckBox nextCB = new CheckBox(nameField.getText());
 				nextCB.setEnabled(false);
 				nextCB.setValue(true);
 				int oldCorCount = corTable.getRowCount();
 				corTable.setWidget(oldCorCount, 0, nextCB);
 				checkBoxList.add(nextCB);
 				if(oldCorCount > 0){
 					correlationListOK.setVisible(true);
 					correlationListOK.setFocus(true);
 					correlationListOK.setEnabled(true);
 					extendButton.setEnabled(false);
 					currentStep.setText("Make sure important correlates (only) are checked; then push 'Correlation List OK' button.");
 				}else{
 					correlationListOK.setEnabled(false);
 					currentStep.setText("Provide probabilities.");
 					coreExtender();
 				}
 			}
 			private void coreExtender() {
 				int correlators[] = new int[32];
 				int cCount = 0; // the number of events to be used as constraints
 				int cbCount = 0;
 				extendButton.setEnabled(false);
 				int cblLen = checkBoxList.size() - 1;// the new CB take out of length
 				for(int k = 0; k < cblLen; k++){// get the checked boxes (except for the new variable) and put them in the list first
 					if(checkBoxList.get(k).getValue()){
 					correlators[cCount++] = k;
 					}
 				}
 				int ncCount = cCount;
 				cbCount = 0;
 				for(CheckBox x: checkBoxList){// puts the on checked boxes in the list
 					if(! x.getValue()){// depends on CB for new value being checked
 						correlators[ncCount++] = cbCount;
 					}
 					cbCount++;
 				}
 				correlators[cbCount-1] = cbCount - 1;// this correctly puts the new event in the list last
 				EPTService.MMCALC(nameField.getText(), correlators, cCount,
 						new AsyncCallback<String[]>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						/*
 						dialogBox
 								.setText("Remote Procedure Call - Failure");
 						serverResponseLabel
 								.addStyleName("serverResponseLabelError");
 						serverResponseLabel.setHTML(SERVER_ERROR);
 						dialogBox.center();
 						closeButton.setFocus(true);
 						*/
 					}
 
 					public void onSuccess(String[]range) {
 						// range = {cv, down, up, next}
 						lowerBound.setText(range[1]);
 						currentValue.setText(range[0]);
 						upperBound.setText(range[2]);
 						for(CheckBox x: checkBoxList){
 							x.setEnabled(true);
 						}
 						marginalEvent.setText(nameField.getText());
 						nameField.setText(null);
 						extendButton.setEnabled(false);
 						nameField.setEnabled(false);
 						nameField.setText("");
 						setValueButton.setEnabled(true);
 						correlationListOK.setEnabled(false);
 						newValue.setFocus(true);
 						newValue.selectAll();
 					}
 				});// end of MMCALC
 			}
 		}
 		
 		
 
 		final Extender extender = new Extender();
 		extendButton.addClickHandler(extender);
 		eventField.addKeyUpHandler(extender);
 
 		class CorrelationsOKHandler implements ClickHandler, KeyUpHandler{
 			CorrelationsOKHandler(){
 				correlationListOK.setEnabled(false);
 				correlationListOK.setFocus(false);
 			}
 			public void onKeyUp(KeyUpEvent event){
 				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
 					coreCorrelationListOK();
 				}				
 			}
 			public void onClick(ClickEvent event){
 				coreCorrelationListOK();
 			}
 			private void coreCorrelationListOK(){
 				correlationListOK.setEnabled(false);
 				currentStep.setText("Provide probabilities.");
 				extender.coreExtender();
 			}
 		}
 		
 		CorrelationsOKHandler correlationHandler = new CorrelationsOKHandler();
 		correlationListOK.addClickHandler(correlationHandler);
 		correlationListOK.addKeyUpHandler(correlationHandler);
 		
 		class SetValueHandler implements ClickHandler, KeyUpHandler{
 			public void onClick(ClickEvent event) {
 				coreSetValue( );	
 			}
 			private void coreSetValue() {
 				//get the new probability value over to mmc which has hopefully been initialized
 				double desiredValue = new Double(newValue.getText());
 				EPTService.MMCALC(desiredValue, 
 					new AsyncCallback<String[]>(){
 						public void onFailure(Throwable caught){
 							
 						}
 						public void onSuccess(String[]range){//setValue
 							// range = {cv, down, up, next}
 							if(range[3].equals(new String("done"))){
 								setValueButton.setEnabled(false);
 								extendButton.setEnabled(true);
 								nameField.setEnabled(true);
 								marginalEvent.setText("");
 								newValue.setText("");
 								currentValue.setText("");
 								nameField.setFocus(true);
 								currentStep.setText("Ready to Add an Effect");
 								upperBound.setText("");
 								lowerBound.setText("");
 								addEventButton.setEnabled(true);
 								addEventButton.setFocus(true);
 							}else{
 								lowerBound.setText(range[1]);
 								currentValue.setText(range[0]);
 								upperBound.setText(range[2]);
 								for(CheckBox x: checkBoxList){
 									x.setEnabled(true);
 								}
 								marginalEvent.setText(range[3]);
 								setValueButton.setEnabled(true);
 								newValue.setText(range[0]);
 								newValue.selectAll();								
 							}
 							showMarginal.getMarginal();
 						}							
 					});
 			}
 			public void onKeyUp(KeyUpEvent event) {
 				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
 					coreSetValue();
 				}
 			}
 		}
 		SetValueHandler settingValue = new SetValueHandler();
 		setValueButton.addClickHandler(settingValue);
 		newValue.addKeyUpHandler(settingValue);
 
 		class AddEventClickHandler implements ClickHandler, KeyUpHandler{
 			public void onClick(ClickEvent theClick){
 				coreEventAdder();
 				theClick.stopPropagation();
 			}
 			public void onKeyUp(KeyUpEvent event) {
 				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
 					coreEventAdder();
 					event.stopPropagation();
 				}
 			}
 			private void coreEventAdder(){
 				addEventButton.setEnabled(false);
 				addEventDialogBox.center();
 				addEventDialogBox.show();
 				//eventField.setFocus(true);	// would like to do this but then keyUp in the event dialog is triggered by this very event!
 				addEventDialogBox.setModal(true);
 			}
 			
 		}
 		addEventButton.addClickHandler(new AddEventClickHandler());
 		addEventButton.setFocus(true);
 }
 catch(Exception e){
 	String message = e.getMessage();
 	message = null;
 }
 }
 }
