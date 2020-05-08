 package cpsc310.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.maps.client.geocode.Geocoder;
 import com.google.gwt.maps.client.geocode.LatLngCallback;
 import com.google.gwt.maps.client.geocode.LocationCallback;
 import com.google.gwt.maps.client.geocode.Placemark;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextBox;
 
 /**
  * Dialog box that holds edit panel. The assumption is that type checking for
  * selecting only one house is done in the caller's class.
  */
 public class EditHouseDialog extends DialogBox {
 	private HouseDataServiceAsync houseDataSvc;
 	private HouseData selectedHouse;
 	private LoginInfo loginInfo;
 	private PropertyMap map;
 	private HouseTable table;
 	private final TextBox priceBox = new TextBox();
 	private final RadioButton yesSell = new RadioButton("editSell", "Yes");
 	private final RadioButton noSell = new RadioButton("editSell", "No");
 	private Label errorMsg = new Label("");
 	private FlowPanel loadingPanel = new FlowPanel();
 	private boolean isOkToEdit = false;
 	private boolean editDone = false;
 	private Geocoder geocoder;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param selectedHouse
 	 *            - currently selected house in the caller
 	 * @param loginInfo
 	 *            - information about the user making edit request
 	 * @param map
 	 *            - map in the caller class
 	 * @param table
 	 *            - table in the caller class
 	 */
 	public EditHouseDialog(HouseData selectedHouse, LoginInfo loginInfo,
 			PropertyMap map, HouseTable table, HouseDataServiceAsync houseDataSvc) {
 		FlowPanel editPanel = new FlowPanel();
 
 		editPanel.addStyleDependentName("editPanel");
 		
 		// Initialize class variables
 		this.selectedHouse = selectedHouse;
 		this.loginInfo = loginInfo;
 		this.map = map;
 		this.table = table;
 		this.houseDataSvc = houseDataSvc;
 
 		// Build dialog contents
 		buildEditPanel(editPanel);
 
 		// Assemble dialog
 		this.setText("Edit a house");
 		this.setGlassEnabled(true);
 		this.setAnimationEnabled(true);
 		this.setWidget(editPanel);
 	}
 
 	/**
 	 * Builds edit panel that has the form that user needs to fill in to provide
 	 * edit information.
 	 * 
 	 * @param editPanel
 	 *            - panel to add all the edit form components
 	 */
 	private void buildEditPanel(FlowPanel editPanel) {
 		Label houseName = new Label("");
 		Button okBtn = new Button("OK");
 		Button cancelBtn = new Button("Cancel");
 		noSell.setValue(true);
 
 		// Set styles of components
 		priceBox.addStyleDependentName("shorter");
 		errorMsg.addStyleDependentName("error");
 
 		// Display the address of the house currently trying to edit
 		houseName.setText("House to edit:  " + selectedHouse.getAddress());
 
 		// Build button behaviors
 		buildOKBtn(okBtn);
 		buildCancelBtn(cancelBtn);
 
 		
 		// Assemble edit panel
 		editPanel.add(houseName);
 		editPanel.add(new InlineHTML("<br />Price: "));
 		editPanel.add(priceBox);
 		editPanel.add(new InlineHTML("<br /><br />For Sale: "));
 		editPanel.add(yesSell);
 		editPanel.add(new InlineHTML("&nbsp;&nbsp;"));
 		editPanel.add(noSell);
 		editPanel.add(new HTML("<br />"));
 		editPanel.add(loadingPanel);
 		editPanel.add(errorMsg);
 		editPanel.add(new HTML("<br /><br /><br />"));
 		editPanel.add(cancelBtn);
 		editPanel.add(new InlineHTML("&nbsp;&nbsp;"));
 		editPanel.add(okBtn);
 	}
 
 	/**
 	 * Attach OK Button behavior. When OK button is clicked, button calls
 	 * editHouse(), which invokes async call to the server. While server-side
 	 * request is being processed, load image is displayed.
 	 * 
 	 * @param okBtn
 	 *            - button to attach ok button behavior
 	 */
 	private void buildOKBtn(Button okBtn) {
 		okBtn.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				editHouse();
 			}
 		});
 	}
 
 	/**
 	 * Attaches Cancel button behavior. A click on cancel button clears the
 	 * dialog and closes.
 	 * 
 	 * @param cancelBtn
 	 *            - button to attach cancel button behavior
 	 */
 	private void buildCancelBtn(Button cancelBtn) {
 		cancelBtn.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				clear();
 				hide();
 			}
 		});
 	}
 
 	/**
 	 * Actual editing function. Sends editing data to the server by an
 	 * asynchronous call. After edit was successful, refreshes table, clears and
 	 * closes the dialog. If async call was unsuccessful, it displays a message
 	 * on the dialog.
 	 */
 	private void editHouse() {
 
 		// Check user specified price
 		if (!checkHousePrice())
 			return;
 
 		// Assemble edit field
 		final int housePrice;
 		final String owner = loginInfo.getEmailAddress();
 		final String houseID = selectedHouse.getHouseID();
 		final String postalCode = selectedHouse.getPostalCode();
 		final boolean yesSelling = yesSell.getValue();
 
 		// If user did not specify the price, price is 0.
 		String price = priceBox.getValue();
 		if (!price.isEmpty())
 			housePrice = Integer.parseInt(price);
 		else housePrice = 0;
 		
 		// get Longitude and latitude
 		LatLngCallback callback = new LatLngCallback() {
 			public void onFailure() {
 				//Window.alert("Location not found");
 			}
 			public void onSuccess(LatLng point) {
 				final double latitude = point.getLatitude();
 				final double longitude = point.getLongitude();
 					
 				// get the postal code if it's not there
 				if (postalCode.equals("")) {
 					//Window.alert("postalcode is null, searching for postal code");
 					LocationCallback callback = new LocationCallback() {
 						public void onFailure(int statusCode) {
 							//Window.alert("Failed to geocode position ");
 						}
 						public void onSuccess(JsArray<Placemark> locations) {
 							if (locations.length() > 1) {
 								// Window.alert("more than one postal code found");
 							}
 							String pc = "";
 							for (int i = 0; i < locations.length(); ++i) {
 								Placemark location = locations.get(i);
 								pc = location.getPostalCode();
 							}
 							if(pc == null) pc = "";
 							else Window.alert("found postal code: " + pc);
 							storeHouse(pc, longitude, latitude, owner, housePrice, yesSelling, houseID);
 						}
 						};
 						geocoder = new Geocoder();
 						geocoder.getLocations(selectedHouse.getAddress() + " VANCOUVER, BC", callback);
 					
 				}
 				else storeHouse(postalCode, longitude, latitude, owner, housePrice, yesSelling, houseID);
 			}
 			};
 		geocoder = new Geocoder();
 		geocoder.getLatLng(selectedHouse.getAddress() + " VANCOUVER, BC", callback);
 	}
 
 	/**
 	 * 
 	 * Helper method to edit the house
 	 * Makes an async call to the server and saves the house in the db 
 	 * 
 	 * @param postalCode
 	 * @param longitude
 	 * @param latitude
 	 * @param owner
 	 * @param housePrice
 	 * @param yesSelling
 	 * @param houseID
 	 */
 	
 	
 	private void storeHouse(String postalCode, double longitude, 
 			double latitude, String owner, int housePrice, 
 			boolean yesSelling, String houseID){
 		
 		// Draw loading panel
 		drawLoading();
 		
 		// Initialize the service proxy
 		if (houseDataSvc == null) {
 			houseDataSvc = GWT.create(HouseDataService.class);
 		}
 
 		// Set up the callback object
 		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
 			public void onFailure(Throwable caught) {
 				loadingPanel.clear();
 				errorMsg.setText(caught.getMessage());
 			}
 
 			public void onSuccess(Void result) {
 				table.refreshTableCurrentView();
 				clear();
 				hide();
 			}
 		};
 		houseDataSvc.updateHouse(owner, housePrice, yesSelling, houseID,
 				latitude, longitude, postalCode, callback);
 		
 	}
 	
 	/**
 	 * Check if user specified price is in non-decimal numbers. If not, prompt
 	 * the user to change the value.
 	 * 
 	 * @return true if price is in non-decimal numbers; false otherwise.
 	 */
 	private boolean checkHousePrice() {
 		String price = priceBox.getValue();
 
 		if (!price.matches("\\d*")) {
 			errorMsg.setText("Only non-decimal numbers are allowed for price.");
 			priceBox.selectAll();
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Draw loading panel to indicate async edit call to the server is in
 	 * process
 	 */
 	private void drawLoading() {
 		Image loading = new Image("images/loading.png");
 		Label loadingMsg = new Label("Editing...");
 		loadingPanel.add(loading);
 		loadingPanel.add(loadingMsg);
 	}
}
