 package com.chuckanutbay.webapp.packagingtransactionmanager.client;
 
 import static com.chuckanutbay.webapp.common.client.CbListBox.newCbListBox;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.H_ALIGN_CENTER;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.H_ALIGN_LEFT;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.V_ALIGN_MIDDLE;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.largeButtonStyles;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.newImage;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.newLabel;
 import static com.chuckanutbay.webapp.common.client.GwtWidgetHelper.newSimplePanel;
 import static com.chuckanutbay.webapp.common.client.IconUtil.ADD_LARGE;
 import static com.chuckanutbay.webapp.common.client.IconUtil.LOGO;
 import static com.chuckanutbay.webapp.common.client.ServiceUtils.createPackagingTransactionService;
 import static com.chuckanutbay.webapp.common.client.ServiceUtils.createTrayLabelService;
 import static com.chuckanutbay.webapp.packagingtransactionmanager.client.RpcHelper.createGetDamageCodeDtosCallback;
 import static com.chuckanutbay.webapp.packagingtransactionmanager.client.RpcHelper.createGetEmployeeDtosCallback;
 import static com.chuckanutbay.webapp.packagingtransactionmanager.client.RpcHelper.createGetTrayLabelDtoCallback;
 import static com.chuckanutbay.webapp.packagingtransactionmanager.client.RpcHelper.createPersistPackagingTransactionDtoCallback;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newLinkedHashMap;
 import static java.util.Collections.sort;
 
 import java.util.List;
 import java.util.Map;
 
 import com.chuckanutbay.webapp.common.client.CbDateBox;
 import com.chuckanutbay.webapp.common.client.CbFlexTable;
 import com.chuckanutbay.webapp.common.client.CbHorizontalPanel;
 import com.chuckanutbay.webapp.common.client.CbIconButton;
 import com.chuckanutbay.webapp.common.client.CbListBox;
 import com.chuckanutbay.webapp.common.client.CbTextBox;
 import com.chuckanutbay.webapp.common.client.CbVerticalPanel;
 import com.chuckanutbay.webapp.common.client.GwtWidgetHelper;
 import com.chuckanutbay.webapp.common.client.ServiceUtils;
 import com.chuckanutbay.webapp.common.shared.DamageCodeDto;
 import com.chuckanutbay.webapp.common.shared.EmployeeDto;
 import com.chuckanutbay.webapp.common.shared.OrderTrayLabelDto;
 import com.chuckanutbay.webapp.common.shared.PackagingTransactionDto;
 import com.chuckanutbay.webapp.common.shared.TrayLabelDto;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A client side tool used to keep track of which digital labels are associated
  * with a given {@link TrayLabel}. This can be used to get all the available 
  * data about an individual cheesecake just form the label number. A packager, 
  * packaged date, test weight, qty of cakes damaged can also be entered as well.
  * <p>
  * The UI is laid out with a logo and title in the header, the various data 
  * fields make up body, and some informational text is displayed at the bottom.
  * <p>
  * Each data field is "tabable." Tabbing onto the Submit button will fire a
  * click event.
  * <p>
  * When the Submit button is clicked, the data in each field is verified for
  * proper data types (no letters where there should only be numbers etc) and
  * then the data is cleared from the screen and sent to the server to be added
  * to the database.
  */
 public class PackagingTransactionManager implements EntryPoint, PackagingTransactionServerCommunicator {
 	private static final int PAGE_WIDTH_PX = Window.getClientWidth();
 	private static final int PAGE_HEIGHT_PX = Window.getClientHeight();
 	private CbFlexTable flexTable = new CbFlexTable();// Where all the fields are positioned
 	private TrayLabelDto currentTrayLabel;// The tray label with the id matching the text in the trayLabelTb
 	private Label lastTrayLabel = newLabel("", "lastTrayLabel");// Displays the id of the last tray label that was submitted.
 	
 	// Fields
 	// These are mostly "Cb" versions of each widget that I have created to make
 	// setup simpler and to be able to set the tab ordering
 	private CbListBox<EmployeeDto> packedByLb = newCbListBox();
 	private CbDateBox dateBox = new CbDateBox(60);
 	private CbTextBox trayLabelTb = new CbTextBox(100);
 	private CbTextBox startLabelTb = new CbTextBox(100);
 	private CbTextBox endLabelTb = new CbTextBox(100);
 	private CbTextBox startLabel2Tb = new CbTextBox(100);
 	private CbTextBox endLabel2Tb = new CbTextBox(100);
 	private CbTextBox testWeightTb = new CbTextBox(100);
 	private CbTextBox damagedQtyTb = new CbTextBox(100);
 	private CbListBox<DamageCodeDto> damageCodeLb = newCbListBox(); 
 	private CbIconButton submitButton = new CbIconButton(ADD_LARGE, "Submit", largeButtonStyles);
 	
 	@Override
 	public void onModuleLoad() {
 		initializeGui();
 		getDamageCodeDtosFromDatabase();
 		getEmployeeDtosFromDatabase();
 	}
 
 	private void initializeGui() {// Working from the innermost widget to the RootPanel
 		
 		// Setup flexTable
 		flexTable.setStyleName("flexTable");
 		flexTable.setWidth(PAGE_WIDTH_PX + "px");
 		
 		Map<String, Widget> labelWidgetPairsRow1 = newLinkedHashMap();
 		labelWidgetPairsRow1.put("Packed By", packedByLb);
 		labelWidgetPairsRow1.put("Date", dateBox);
 		labelWidgetPairsRow1.put("Tray Label", trayLabelTb);
 		labelWidgetPairsRow1.put("Start Label", startLabelTb);
 		labelWidgetPairsRow1.put("End Label", endLabelTb);
 		labelWidgetPairsRow1.put("Test Weight", testWeightTb);
 		labelWidgetPairsRow1.put("Qty Damaged", damagedQtyTb);
 		labelWidgetPairsRow1.put("Damage Code", damageCodeLb);
 		addLabelWidgetPairs(labelWidgetPairsRow1, 0, 0);
 		
 		Map<String, Widget> labelWidgetPairsRow2 = newLinkedHashMap();
 		labelWidgetPairsRow2.put("Start Label 2", startLabel2Tb);
 		labelWidgetPairsRow2.put("End Label 2", endLabel2Tb);
 		addLabelWidgetPairs(labelWidgetPairsRow2, 2, 3);
 		
 		// Make the button row all one column
 		int buttonRow = 4; int buttonColumn = 0;
 		flexTable.getFlexCellFormatter().setColSpan(buttonRow, buttonColumn, 8);
 		flexTable.setWidget(submitButton, buttonRow, buttonColumn).setHorizontalAlignement(buttonRow, buttonColumn, H_ALIGN_CENTER);
 		
 		// Setup Tab Links
 		packedByLb.setNextWidget(
 		dateBox.setNextWidget(
 		trayLabelTb.setNextWidget(
 		startLabelTb.setNextWidget(
 		endLabelTb.setNextWidget(
 		testWeightTb.setNextWidget(
 		damagedQtyTb.setNextWidget(
 		damageCodeLb.setNextWidget(
 		submitButton.setNextWidget(
 		trayLabelTb
 		)))))))));
 		
 		startLabel2Tb.setNextWidget(
 		endLabel2Tb.setNextWidget(
 		testWeightTb
 		));
 		
 		// Set TextBox initial values
 		damagedQtyTb.setText("0");
 		
 		// Setup TrayLabelTb
 		trayLabelTb.addChangeHandler(new ChangeHandler() {
 			@Override
 			public void onChange(ChangeEvent event) {// Called when the value of the tray label text box is changed
 				try {
 					getTrayLabelDto(Integer.valueOf(trayLabelTb.getText()));
 				} catch (NumberFormatException e) {// A non number has been entered
 					// Don't do anything
 				}
 			}
 		});
 		
 		// Setup submitButton
 		// When the submitButton is "tabbed" to we want it pretend it was clicked
 		submitButton.addFocusHandler(new FocusHandler() {
 			@Override
 			public void onFocus(FocusEvent event) {
 				submitButton.click();
 			}
 		}); 
 		submitButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {// Verify the data and then send it to the server
 				// Prepare a new PackagingTransactionDto to store the data from the fields
 				PackagingTransactionDto ptDto = new PackagingTransactionDto();
 				
 				makeBorderRed(); // This is changed to green if a good return comes
 				
 				// setEmployee
 				if (packedByLb.getSelected() == null) {// No employee is selected
 					Window.alert("Please select an employee");
 					packedByLb.setFocus(true);
 					return;
 				} else {
 					ptDto.setEmployee(packedByLb.getSelected());
 				}
 				
 				// setDate
 				if (dateBox.getValue() == null) {// No date is selected
 					Window.alert("Please select a date");
 					dateBox.setFocus(true);
 					return;
 				} else {
 					ptDto.setDate(dateBox.getValue());
 				}
 				
 				// setTrayLabelId
 				if (currentTrayLabel == null) {// A trayLabel can't be found in the database with an id matching the value of trayLabelTb
 					Window.alert("Please enter a proper Tray Label");
 					trayLabelTb.setFocus(true);
 					return;
 				} else {
 					ptDto.setTrayLabelId(currentTrayLabel.getId());
 				}
 				
 				// setLabels
 				List<CbTextBox> labelTextBoxes = newArrayList(startLabelTb, endLabelTb, startLabel2Tb, endLabel2Tb);
 				List<String> labels = newArrayList();
 				for (CbTextBox tb : labelTextBoxes) {
 					String tbText = tb.getText();
 					if (tbText.matches("[0-9A-Z]{10,15}")) {// Contains 10-15 numbers and/or capital letters
 						labels.add(tbText);
 					} else if (!tbText.equals("")) {// Something strange has been entered
 						Window.alert("Please enter a proper Label Code");
 						tb.setFocus(true);
 						return;
 					}
 				}
 				
 				// Sort the labels
 				sort(labels, String.CASE_INSENSITIVE_ORDER);
 				
 				if (labels.size() == 0) {// It is probably a case where the new labels aren't being used
 					// Do nothing
 				} else if (!digitalLabelsAndTrayLabelMatch(labels) && !Window.confirm("You have scanned a digital label that doesn't match the tray label! Click OK to ignore this problem")) {// The user wants to fix the mistake
 					startLabelTb.setFocus(true);
 					return;
 				} else if (labels.size() % 2 == 1) {// An odd number of labels were scanned. Probably a mistake
 					if (Window.confirm("You have scanned an odd number of label barcodes!")) {// It doesn't matter, move on!
 						ptDto.setStartLabel1(labels.get(0));
 						if (labels.size() == 3) {// There is a pair and a half
 							ptDto.setEndLabel1(labels.get(1));
 							ptDto.setStartLabel2(labels.get(2));
 							
 						}
 					} else {// The user wants to fix the mistake
 						startLabelTb.setFocus(true);
 						return;
 					}
 				} else {// They scanned a pair or two of label barcodes. Yay!
 					ptDto.setStartLabel1(labels.get(0));
 					ptDto.setEndLabel1(labels.get(1));
 					if (labels.size() == 4) {// There are 2 pairs
 						ptDto.setStartLabel2(labels.get(2));
 						ptDto.setEndLabel2(labels.get(3));
 					}
 				}
 				
 				// setTestWeight
 				try {
 					ptDto.setTestWeight(Double.valueOf(testWeightTb.getText()));
 				} catch (NumberFormatException e) {// Something other than a number was entered
 					Window.alert("Please enter a proper Weight");
 					testWeightTb.setFocus(true);
 					return;
 				}
 				
 				// setDamagedQty
 				try {
 					ptDto.setDamagedQty(Integer.valueOf(damagedQtyTb.getText()));
 				} catch (NumberFormatException e) {// Something other than a number was entered
 					Window.alert("Please enter a proper Damaged Qty");
 					damagedQtyTb.setFocus(true);
 					return;
 				}
 				
 				// setDamageCode
 				if (ptDto.getDamagedQty() != 0 && damageCodeLb.getSelected() != null) {
 					ptDto.setDamageCode(damageCodeLb.getSelected());
 				}
 				
 				// Send the packaging transaction off to the server and then database
 				persistPackagingTransactionDtoToDatabase(ptDto);
 				
 			}
 
 			private boolean digitalLabelsAndTrayLabelMatch(List<String> labels) {
 				if (currentTrayLabel.getQbItem() == null) {// For whatever reason the currentTrayLabel doesn't have an QuickbooksItem attached to it
 					return false;
 				}
 				String productId = currentTrayLabel.getQbItem().getId();
 				String productIdBase = productId.substring(0, productId.indexOf("-"));// Get the part before the dash
 				for (String s : labels) {
 					if (!s.startsWith(productIdBase)) {// The tray label and digital label don't match
 						return false;
 					}
 				}
 				return true;
 			}
 			
 		});
 		
 		// Setup headerPanel
 				CbHorizontalPanel headerPanel = new CbHorizontalPanel()
 					.addWidget(newImage(LOGO, 400), H_ALIGN_LEFT)
 					.addWidget(newLabel("Packaging Transaction Manager", "title"), H_ALIGN_CENTER, V_ALIGN_MIDDLE)
 					.setWidth(PAGE_WIDTH_PX);
 				
 				// Setup mainPanel
 				CbVerticalPanel mainPanel = new CbVerticalPanel()
 					.addWidget(headerPanel)
 					.addWidget(flexTable, H_ALIGN_CENTER)
 					.addWidget(lastTrayLabel, H_ALIGN_LEFT)
 					.setWidth(PAGE_WIDTH_PX)
 					.setHeight(PAGE_HEIGHT_PX)
 					.setCellSpacing(10);
 		
 		// Setup simplePanel
 		// NOTE ON GWT: For some reason I (Mitchell) have only been successful at 
 				// Applying styles to SimplePanels so I always throw the main panel into a simple panel
 		SimplePanel simplePanel = newSimplePanel(mainPanel, "backgroundPanel");
 		
 		// Setup rootPanel
 		// NOTE ON GWT: The RootPanel is the space allocated to put all the GWT widgets
 				// The root allocated for this class has the same name as the class
		RootPanel.get("PackagingTransactionManager").add(simplePanel);
 	}	
 	
 	/**
 	 * Places each of the entries in the given pairs in the flex table starting
 	 * at the given row and column and moves right.
 	 * @param pairs A Map of Widgets mapped by a label for them
 	 * @param row The row in the flex table to place the label in. The widget
 	 * will be placed in the row below.
 	 * @param column The column in the flex table to place the first label and widget in
 	 */
 	private void addLabelWidgetPairs(Map<String, Widget> pairs, int row, int column) {
 		for (Map.Entry<String, Widget> pair : pairs.entrySet()) {
 			flexTable.setText(pair.getKey(), row, column).setCellStyle("flexTableLabel", row, column); // Add the label to the right place and format it
 			flexTable.setWidget(pair.getValue(), row + 1, column);// Add the widget in the next row down
 			column++; // Move one column to the right
 		}
 	}
 	
 	/**
 	 * Makes the boarder of the flexTable containing all of the data fields green.
 	 * After 5 seconds the boarder is returned to the default color.
 	 */
 	private void makeBorderGreen() {
 		flexTable.setStyleName("greenBorder");
 		
 		// After 5 seconds set the style back to normal
 		Timer timer = new Timer() {
 
 			@Override
 			public void run() {
 				flexTable.setStyleName("flexTable");
 			}
 			
 		};
 		timer.schedule(5000);
 	}
 	
 	/**
 	 * Makes the boarder of the flexTable containing all of the data fields red.
 	 */
 	private void makeBorderRed() {
 		flexTable.setStyleName("redBorder");
 	}
 	
 	/*#################################
 	 * Remote Procedure Calls (RPC) 
 	 *#################################*/ 
 	
 	// NOTE ON GWT: RPC's require you to create a Service and an Asynchronous Callback
 			// If you want to have the server do something, you tell the Service 
 			// what you want done and give it the Asynchronous Callback. The Asynchronous
 			// Callback is called when the server method is done. To make this process
 			// more comprehensible, the Services are handled by the ServiceUtils class
 			// and the Asynchronous Callback's are handled by RpcHelper classes
 	
 	@Override
 	public void getDamageCodeDtosFromDatabase() {
 		// Request the damage codes from the server
 		// Tell the server to call the onSuccessfulGetDamageCodeDtos method when it finishes
 		createPackagingTransactionService().getDamageCodeDtos(createGetDamageCodeDtosCallback(this));
 	}
 
 	@Override
 	public void getEmployeeDtosFromDatabase() {
 		// Request the employees from the server
 		// Tell the server to call the onSuccessfulGetEmployeeDtos method when it finishes
 		createPackagingTransactionService().getEmployeeDtos(createGetEmployeeDtosCallback(this));
 	}
 	
 	@Override
 	public void getTrayLabelDto(Integer id) {
 		// Request the tray label with the given id
 		// Tell the server to call the onSuccessfulGetTrayLabelDto method when it finishes
 		createTrayLabelService().getTrayLabelDto(id, createGetTrayLabelDtoCallback(this));
 	}
 	
 	@Override
 	public void persistPackagingTransactionDtoToDatabase(
 			PackagingTransactionDto packagingTransaction) {
 		// Request that server store the given packaging transaction in the database
 		// Tell the server to call the onSuccessfulPersistPackagingTransactionDto method when it finishes
 		createPackagingTransactionService().persistPackagingTransactionDto(packagingTransaction, createPersistPackagingTransactionDtoCallback(this));
 	}
 
 	@Override
 	public void onSuccessfulGetDamageCodeDtos(List<DamageCodeDto> damageCodes) {// The server found the given damageCodes in the database
 		// Store each damageCode in a map where the key is a String representation of the damageCode and the value is the DamageCodeDto object
 		Map<String, DamageCodeDto> map = newLinkedHashMap();
 		for (DamageCodeDto damageCode : damageCodes) {
 			map.put(damageCode.getId() + " - " + damageCode.getCode(), damageCode);
 		}
 		// Set the data of the damageCodeLb to be the map
 		damageCodeLb.setData(map);
 	}
 
 	@Override
 	public void onSuccessfulGetEmployeeDtos(List<EmployeeDto> employees) {// The server found the given employees in the database
 		// Store each employee in a map where the key is a String representation of the employee's name and the value is the EmployeeDto object
 		Map<String, EmployeeDto> map = newLinkedHashMap();
 		for (EmployeeDto employee : employees) {
 			map.put(employee.getFirstName() + " " + employee.getLastName(), employee);
 		}
 		// Set the data of the packedByLb to be the map
 		packedByLb.setData(map);
 	}
 	
 	@Override
 	public void onSuccessfulGetTrayLabelDto(TrayLabelDto result) {// The server looked for a tray label with the id it was given and returned what it found. If result is null it means that there was no tray label with the id passed to the server
 		currentTrayLabel = result;
 	}
 
 	@Override
 	public void onSuccessfulPersistPackagingTransactionDto() {// The packaging transaction was successfully stored in the database
 		makeBorderGreen();
 		
 		lastTrayLabel.setText("You just entered Tray Label: " + trayLabelTb.getText());
 		
 		// Reset the text boxes
 		trayLabelTb.setText("");
 		startLabelTb.setText("");
 		endLabelTb.setText("");
 		startLabel2Tb.setText("");
 		endLabel2Tb.setText("");
 		testWeightTb.setText("");
 		damagedQtyTb.setText("0");
 		
 		trayLabelTb.setFocus(true);
 	}
 
 }
