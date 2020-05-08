 package cpsc310.client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.reveregroup.gwt.facebook4gwt.Facebook;
 import com.reveregroup.gwt.facebook4gwt.LoginButton;
 
 /**
  * Main EntryPoint class. UI is built, client-side request is handled. 
  */
 public class Team_02 implements EntryPoint {
 	private LayoutPanel mainPanel = new LayoutPanel();
 	private DockLayoutPanel submainPanel = new DockLayoutPanel(Unit.PX);
 	private SplitLayoutPanel mapContainerPanel = new SplitLayoutPanel();
 	private FlowPanel sidePanel = new FlowPanel();
 	private FlowPanel tableWrapPanel = new FlowPanel();
 	private PropertyMap theMap;	
 	private boolean isSidePanelHidden = false;	
 	private boolean isTablePanelHidden = false;		
 	private HouseTable houseTable = HouseTable.createHouseTable();
 	private HouseDataServiceAsync houseDataSvc = GWT.create(HouseDataService.class);
 	private LoginServiceAsync loginService = GWT.create(LoginService.class);
 	private LoginInfo loginInfo = null;
 	private boolean isLoginServiceAvailable = false;
 	private boolean isEditable = false;
 	private boolean isSearching = false;
 	private int databaseLength = 0;
 	private int pageLength = 0;	
 	private Set<HouseData> selectedHouses = null;	
 	private List<HouseData> searchHouseList = null;
 	private String[] searchCriteria = 
 		{"Address", "Postal Code", "Current Land Value",
 			"Current Improvement Value", "Assessment Year", "Previous Land Value", 
 			"Previous Improvement Value", "Year Built", "Big Improvement Year",
 			"Price", "Realtor", "For Sale"};
 	private List<HouseData> currentHouseList = null;
 
 	
 	/**
 	 * Entry point method. Initializes login service.
 	 * Upon completion of asynchronous request to login service,
 	 * UI is built. 
 	 */
 	public void onModuleLoad() {
 		// Check login status using login service.
 		if (loginService == null) {
 			loginService = GWT.create(LoginService.class);
 		}
 		// TODO: when deploying delete "Team_02.html?gwt.codesvr=127.0.0.1:9997" below.
 	    loginService.login(GWT.getHostPageBaseURL()+ "Team_02.html?gwt.codesvr=127.0.0.1:9997", 
 	    		new AsyncCallback<LoginInfo>() {
 		      public void onFailure(Throwable error) {
 		    	  Window.alert("Login service could not be loaded.");
 		    	  buildUI();
 		      }
 		      public void onSuccess(LoginInfo result) {
 		        loginInfo = result;
 		        isLoginServiceAvailable = true;
 		        buildUI();
 		      }
 		    });
 	}
 	
 	/**
 	 * Builds application's main UI
 	 */
 	private void buildUI() {
 				
 		// Initialize selection model for map and table
 		initSelection();		
 				
 		// Enable edit function only if login service is available AND
 		// the user is logged in.
 		if (isLoginServiceAvailable == true && loginInfo.isLoggedIn()) {
 			houseTable.enableEdit();
 		}
 		
 		// Make main panel fill the browser
 		mainPanel.setHeight(Window.getClientHeight() + "px");
 		submainPanel.setHeight(Window.getClientHeight() + "px");
 	  	
 		// Make sidePanel
 		buildSidePanel(sidePanel);
 		submainPanel.addWest(sidePanel, 230);
 		
 		// Make tablePanel
 		buildTablePanel(tableWrapPanel);
 	  	submainPanel.addSouth(tableWrapPanel, 300);
 	  	
 	  	// Make mapContainerPanel
 	  	buildMapPanel(mapContainerPanel);
 		submainPanel.add(mapContainerPanel);
 		
 		// Add content wrapper to the main panel
 		mainPanel.add(submainPanel);
 	  	mainPanel.setWidgetLeftWidth(submainPanel, 0, Unit.PCT, 100, Unit.PCT);
 	  	
 	  	// Associate Main panel with the HTML host page		
 	 	RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
 	 	rootLayoutPanel.add(mainPanel);
 
 	 	//Richard Added
 	 	Facebook.init("257432264338889");
 	 	LoginButton faceBookBtn = new LoginButton(true);
 	 	mainPanel.add(faceBookBtn);
 	}
 	
 	/**
 	 * Create MultiSelectionModel and attach to map and table.
 	 * Attachment of selection model enables display of selected houses in map, and
 	 * editing of houses in table.
 	 */
 	private void initSelection() {
 		
 		// Create selection model
 		final MultiSelectionModel<HouseData> selectionModel = 
 				new MultiSelectionModel<HouseData> (HouseData.KEY_PROVIDER);
 		
 		// Handle selection event. Upon selection selected houses get displayed on map.
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				selectedHouses = selectionModel.getSelectedSet();
 				if (selectedHouses.isEmpty()) {
 					if (isEditable == true) {
 						
 					}
 					theMap.clearMap();	
 					return;
 				}
 				if (isEditable == true) {
 					
 				}
 				// clear map before proceeding to add new point
 				theMap.clearMap();
 				// add marker onto map
 				for (HouseData house : selectedHouses)
 					theMap.findLocation(house.getAddress() + " VANCOUVER");
 			}
 		});
 		
 		// Attach selection model to table to enable synchronous selection between map and table.
 		houseTable.enableSelection(selectionModel);
 	}
 
 	/**
 	 * Helper to buildUI().
 	 * Assemble map container panel.
 	 * @param mapContainerPanel	SplitLayoutPanel to hold the map
 	 */
 	private void buildMapPanel(SplitLayoutPanel mapContainerPanel) {
 	  	// Open a map centered on Vancouver
 		LatLng vancouver = LatLng.newInstance(49.264448, -123.185844);
 	 	theMap = new PropertyMap(vancouver);
 	 		
 		// Assemble map panel
 		mapContainerPanel.addWest(theMap.getStreetViewMap(), 500);
 		mapContainerPanel.add(theMap.getMap());
 		mapContainerPanel.setStyleName("mapContainerPanel");
 	}
 	
 	/**
 	 * Helper to buildUI()
 	 * Assembles table panel.
 	 * @param tableWrapPanel - flow panel to hold table related elements
 	 */
 	private void buildTablePanel(FlowPanel tableWrapPanel) {
 		Button hideShowTablePanelButton = new Button("-");
 		SimplePager simplePager = new SimplePager();		
 		
 		buildTablePanelButton(hideShowTablePanelButton);  	
 	  	
 		// Create Cell Table & attach pager to table
 		simplePager.setDisplay(houseTable.getHouseTable());
 		simplePager.setStylePrimaryName("pager");		
 				
 		
 		// Assemble table panel
 		tableWrapPanel.add(hideShowTablePanelButton);
 	  	tableWrapPanel.add(houseTable.getHouseTable());
 	  	tableWrapPanel.add(simplePager);
 	  	tableWrapPanel.setStyleName("tableWrapPanel");
 	  	
 	  			
 	}
 
 	/**
 	 * Helper to buildTablePanel()
 	 * Create hide/show behavior to table panel button
 	 * @param hideShowTablePanelButton - button to behave lie hide/show button
 	 */
 	private void buildTablePanelButton (final Button hideShowTablePanelButton) {
 		hideShowTablePanelButton.setStyleName("hideShowButton");
 	  	hideShowTablePanelButton.addStyleDependentName("horizontal");
 	  	
 		hideShowTablePanelButton.addClickHandler(new ClickHandler () {
 			public void onClick (ClickEvent event) {
 				if (!isTablePanelHidden) {
 					isTablePanelHidden = true;
 					hideShowTablePanelButton.setText("+");
 					submainPanel.setWidgetSize(tableWrapPanel, 20);
 					submainPanel.animate(300);	  
 				}
 				else {
 					isTablePanelHidden = false;
 					hideShowTablePanelButton.setText("-");
 					submainPanel.setWidgetSize(tableWrapPanel, 300);
 					submainPanel.animate(300);
 				}
 			}
 		});		
 	}
 
 	/**
 	 * Helper to buildUI()
 	 * Assemble side panel.
 	 * @param sidePanel	flow panel to hold sidepanel contents
 	 */
 	private void buildSidePanel(FlowPanel sidePanel) {
 		Button hideShowSidePanelButton = new Button("-");
 		FlowPanel sidePanelContentWrap = new FlowPanel();	
 		
 		// Create hide/show ability into the button 
 		buildSidePanelButton(hideShowSidePanelButton);
 		
 		// Assemble GWT widgets to occupy side panel
 		buildSidePanelWidgets(sidePanelContentWrap);
 		
 		// Assemble side panel
 		sidePanel.add(new HTML ("<div id ='header'><h1>iVan</br>Homes</br>Prices</h1></div>"));
 		sidePanel.add(hideShowSidePanelButton);
 		sidePanel.add(sidePanelContentWrap);		
 		sidePanel.add(new HTML ("<div id ='footer'><span>iVanHomesPrices.<br/>Created by Team XD. 2012.</span></div>"));		
 	}
 	
 	/**
 	 * Helper to buildSidePanel().
 	 * Creates hide/show button behavior.
 	 * @param hideShowSidePanelButton - button to behave like hide/show button
 	 */
 	private void buildSidePanelButton(final Button hideShowSidePanelButton) {
 		hideShowSidePanelButton.setStyleName("hideShowButton");
 		hideShowSidePanelButton.addStyleDependentName("vertical");
 				
 		hideShowSidePanelButton.addClickHandler(new ClickHandler () {
 			public void onClick (ClickEvent event) {
 				if (!isSidePanelHidden) {
 					isSidePanelHidden = true;
 					hideShowSidePanelButton.setText("+");
 					submainPanel.setWidgetSize(sidePanel, 20);
 					submainPanel.animate(300);	  
 				}
 				else {
 					isSidePanelHidden = false;
 					hideShowSidePanelButton.setText("-");
 					submainPanel.setWidgetSize(sidePanel, 230);
 					submainPanel.animate(300);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Helper to buildSidePanel()
 	 * Assembles GWT widgets that needs to be included in the sidePanel.
 	 * @param sidePanelContentWrap - flow panel to wrap the widgets
 	 */
 	private void buildSidePanelWidgets(FlowPanel sidePanelContentWrap) {
 		FlowPanel loginPanel = new FlowPanel();
 		FlowPanel searchPanel = new FlowPanel();
 		HorizontalPanel uploadPanel = new HorizontalPanel();
 		Button uploadBtn = new Button("Upload");
 		TextBox uploadFileTextBox = new TextBox();		
 		
 		//Richard Added
 		HorizontalPanel docPanel = new HorizontalPanel();
 		Button helpBtn = new Button("Help");
 		Button termsBtn = new Button("Terms of Use");
 		docPanel.add(helpBtn);
 		docPanel.add(termsBtn);
 		
 		// Assemble login panel
 	  	buildLoginPanel(loginPanel);
 	  	
 	  	// Assemble search panel
 	 	buildSearchPanel(searchPanel);
 		
 	 	// TODO: delete this in the final version. Assemble UploadPanel
 	  	uploadPanel.add(uploadFileTextBox);
 	  	uploadPanel.add(uploadBtn);
 	 	
 	  	// Assemble widgets to go into the side panel
 		sidePanelContentWrap.add(loginPanel);
 	  	sidePanelContentWrap.add(new HTML("<br />"));
 	  	sidePanelContentWrap.add(searchPanel);
 	 	sidePanelContentWrap.add(new HTML("<br />"));
 	  	sidePanelContentWrap.add(uploadPanel);
 	  	sidePanelContentWrap.add(new HTML("<br />"));
 	  	//Richard Added
 	  	sidePanelContentWrap.add(docPanel);
 	  	sidePanelContentWrap.add(new HTML("<br />"));
 	  	
 	  	// Set style
 	  	sidePanelContentWrap.setStyleName("sidePanelContentWrap");
 	}
 
 	/**
 	 * Helper to buildSidePanelWidgets(). 
 	 * Assembles login panel which holds login/logout buttons.
 	 * TODO: add user info
 	 */
 	private void buildLoginPanel(FlowPanel loginPanel) {
 		Button loginBtn = new Button("Login");
 		Button logoutBtn = new Button("Log out");
 				
 		// Enable login/logout only if the login service is available.
 		if (isLoginServiceAvailable == true) {
 			// Set Login Panel
 			loginPanel.add(loginBtn);
 			loginPanel.add(logoutBtn);
 			
 			// Load the login/logout button depending on login/logout status
 	        if(loginInfo.isLoggedIn()){
 				loginBtn.setVisible(false);
 				loginBtn.setEnabled(false);
 				isEditable = true;
 			}
 			else{
 				logoutBtn.setVisible(false);
 				logoutBtn.setVisible(false);
 			}
 					
 			 // Listen for mouse events on Login
 			loginBtn.addClickHandler(new ClickHandler() {
 				public void onClick (ClickEvent event) {
 					Window.Location.assign(loginInfo.getLoginUrl());
 				}
 			});
 			
 			// Listen for mouse events on Logout
 			logoutBtn.addClickHandler(new ClickHandler() {
 				public void onClick (ClickEvent event) {
 					Window.Location.assign(loginInfo.getLogoutUrl());
 				}
 			});
 		}
 		
 		// Set style
 	  	loginPanel.setStyleName("loginPanel");
 	}
 
 	/**
 	 * Helper to buildSidePanelWidgets(). Build search panel.
 	 * searchSettingsPanel contains all the text boxes and labels needed for search field.
 	 * searchPanel wraps the searchSettingsPanel and searchBtn. 
 	 */
 	private void buildSearchPanel(FlowPanel searchPanel) {
 		FlowPanel searchSettingPanel = new FlowPanel();
 		Button searchBtn = new Button("Search");
 		final List<TextBox> searchValues = new ArrayList<TextBox> (searchCriteria.length * 2);
 		final List<RadioButton> forSale = new ArrayList<RadioButton>(3);
 		
 		// Append style
 		searchPanel.setStyleName("searchPanel");
 	  	searchSettingPanel.setStyleName("searchSettingPanel");
 	  		  	
 	  	// Build searchSettingPanel
 	  	searchSettingPanel.add(new HTML("<div class='border'></div>"));
 	  	
 	  	// Add polygon selection
 	  	buildPolygonSelection(searchSettingPanel);
 	  	
 	  	for (String criterion : searchCriteria) {
 	  		searchSettingPanel.add(new Label(criterion));
 	  		
 	  		if (criterion.endsWith("Value") || criterion.endsWith("Price") || 
 	  				criterion.endsWith("Year") || criterion.startsWith("Year")) {
 	  			buildRangeBoxes(searchValues, searchSettingPanel);
 	  		}
 	  		else if (criterion.endsWith("Sale")) {
 	  			buildForSale(forSale, searchSettingPanel);
 	  		}
 	  		else {
 	  			buildRegularBoxes(searchValues, searchSettingPanel);
 	  		}
 	  	}
 	  	
 	  	// Add searchSettingPanel and searchBtn to the searchPanel
 	  	searchPanel.add(searchSettingPanel);
 	  	searchPanel.add(new HTML("<br />"));
 	  	searchPanel.add(searchBtn);  	
 		
 		// Listen for mouse events on Search Button
 		searchBtn.addClickHandler(new ClickHandler() {
 			public void onClick (ClickEvent event) {
 				searchHouse(searchValues, forSale);
 			}
 		});
 	}
 	
 	/**
 	 * Helper to buildSearchPanel().
 	 * Adds polygon selection tools.
 	 * @param searchSettingPanel - panel to hold selection tool
 	 */
 	private void buildPolygonSelection(FlowPanel searchSettingPanel) {
 		final Button specifyRegionBtn = new Button();
 	    final Button clearPolygonBtn = new Button();
 	    final Button editPolygonBtn = new Button();	
 	    
 		//Polygon settings
 	    specifyRegionBtn.setText("Specify region on map");
 	    clearPolygonBtn.setText("Clear specified region");
 	    editPolygonBtn.setText("Edit specified region");
 	    clearPolygonBtn.setEnabled(false);
 	    editPolygonBtn.setEnabled(false);
  		
 	    // Listen for mouse events on specify region Button
  		specifyRegionBtn.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
  		    	  theMap.setSpecifyingRegion(true);  	  
  		        clearPolygonBtn.setEnabled(true);
  		        specifyRegionBtn.setEnabled(false);
  		        editPolygonBtn.setEnabled(true);
  		      }
  		    });
  		
  		// Listen for mouse events on clear polygon Button
  		clearPolygonBtn.addClickHandler(new ClickHandler() {
  		      public void onClick(ClickEvent event) {
  		        theMap.clearMap();
  		        specifyRegionBtn.setEnabled(true);
  		        clearPolygonBtn.setEnabled(false);
  		        editPolygonBtn.setEnabled(false);
  		      }
  		    });
  		
  		// Listen for mouse events on editPolygon Button
  		editPolygonBtn.addClickHandler(new ClickHandler(){
  		      public void onClick(ClickEvent event) {
  		    	  	theMap.editPolygon();
  			      }
  			    });
  		
 	 	// Add to setting panel
  		searchSettingPanel.add(specifyRegionBtn);
 	  	searchSettingPanel.add(clearPolygonBtn);
 	  	searchSettingPanel.add(editPolygonBtn);
 	}
 	
 
 	
 	/**
 	 * Helper to buildSearchPanel().
 	 * Builds and adds text boxes that represent a range of numbers.
 	 * boxes by default get predefined labels "min" and "max" in their field. 
 	 * @param searchValues	list of text boxes representing search field
 	 * @param searchSettingPanel	FlowPanel that holds all the search boxes.
 	 */
 	private void buildRangeBoxes(List<TextBox> searchValues, FlowPanel searchSettingPanel) {
 		TextBox[] rangeBox = {new TextBox(), new TextBox()};
 		String[] labels = {"min", "max"};
 		int i = 0;
 		
 		for (final TextBox box : rangeBox) {
 			// add default style, add to panel and text box list
 			box.addStyleDependentName("shorter");			
 			searchSettingPanel.add(box);
 			searchValues.add(box);
 			
 			// add predefined text "min" and "max" colored in gray font color
 			box.setText(labels[i]);
 			box.addStyleDependentName("before");
 			
 			// when user clicks the text goes away and gray font color is removed
 			box.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick (ClickEvent event) {
 					box.setText("");
 					box.removeStyleDependentName("before");
 				}
 			});
 			
 			// To prevent array out of bounds error
 			if (i < labels.length) i++;		
 		}	
 	}
 		
 	/**
 	 * Helper to buildSearchPanel().
 	 * Creates non-range boxes and adds the box to the list of search boxes, and the
 	 * flow panel that holds all the search fields.
 	 * @param searchValues	list of text boxes representing search field
 	 * @param searchSettingPanel	FlowPanel that holds all the search boxes.
 	 */
 	private void buildRegularBoxes(List<TextBox> searchValues, FlowPanel searchSettingPanel) {
 		TextBox tb = new TextBox();
 		tb.addStyleDependentName("longer");
 		searchValues.add(tb);
 		searchSettingPanel.add(tb);
 	}
 	
 	/**
 	 * Helper to buildSearchPanel().
 	 * Creates radio buttons that specify the search criterion "For Sale", and
 	 * adds the radio buttons to forSale list so that it will be passed to
 	 * the search method. 
 	 * "All" criterion is selected by default.
 	 * @param forSale	list of Radio Buttons that define "for sale" criterion
 	 * @param searchSettingPanel	FlowPanel that holds all the search boxes.
 	 */
 	private void buildForSale(List<RadioButton> forSale, FlowPanel searchSettingPanel) {
 		// Labels that go next to the button
 		String[] isSelling = {"Yes", "No", "All"};
 		
 		// Build the buttons
 		for (String value: isSelling){
 			RadioButton rdBtn = new RadioButton("isSelling", value);
 			searchSettingPanel.add(rdBtn);
 			searchSettingPanel.add(new InlineHTML("&nbsp;&nbsp;"));
 			forSale.add(rdBtn);
 		}
 		// All is selected by default
 		forSale.get(isSelling.length - 1).setValue(true);		
 	}
 	
 	/**
 	 * Gets user input from search tab, validates user input,
 	 * makes asynchronous call to server-side search,
 	 * stores search result into local store,
 	 * and updates table with the search result.
 	 */
 	private void searchHouse(List<TextBox> searchValues, List<RadioButton> forSale) {
 		// Get user input into search boxes
 		String[] userSearchInput = getUserSearchInput(searchValues);
 		
 		// Validate user input
 		if (!validateUserSearchInput (userSearchInput))	return;
 		
 		// Get radio button (For Sale) response
 		int isSelling = convertRadioBtnSearch(forSale);
 		
 		// Initialize the service proxy
 		if (houseDataSvc == null) {
 			houseDataSvc = GWT.create(HouseDataService.class);
 		}
 		
 		// Set up the callback object
 		AsyncCallback<List<HouseData>> callback = new AsyncCallback<List<HouseData>>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 			public void onSuccess(List<HouseData> result) {
 				if (result != null) {
 					searchHouseList = result;
 					isSearching = true;			
 					houseTable.setSearch(isSearching, result);
 					houseTable.updateTable(result, result.size(), 0, false);
 				}
 				else {
 					result = new ArrayList<HouseData> ();
 					houseTable.updateTable(result, 0, 0, false);
 					Window.alert("No result found");
 				}
 			}
 		};
 		// Make the call to the house data service to search for data in the server
 			houseDataSvc.getSearchedHouses(userSearchInput, isSelling, callback);
 	}
 	
 	/**
 	 * Helper to searchHouse().
 	 * Grabs the user's input into the search boxes.
 	 * @param searchValues	list of search boxes
 	 * @return	array of user's search input into text boxes
 	 */
 	private String[] getUserSearchInput(List<TextBox> searchValues) {
 		String[] userInput = new String[searchValues.size()];
 		int i =0;
 		
 		for (TextBox value : searchValues) {
 			String temp = value.getText().trim();
 			// if user left min/max labels, then the criterion is empty.
 			if (temp.equals("min") || temp.equals("max"))
 				temp = "";
 			userInput[i] = temp;
 			i++;
 		}
 		
 		return userInput;
 	}
 	
 	/**
 	 * Helper to searchHouse().
 	 * Validates user's input into search boxes.
 	 * If invalid, notifies the user
 	 * @param userSearchInput	list of user's input into search boxes
 	 * @return		boolean value representing if the inputs were all valid
 	 */
 	private boolean validateUserSearchInput(String[] userSearchInput) {
 		boolean isOK = false;
 		String numericAlert = "must be numbers only. No decimal is allowed.\n";
 		String postalCodeAlert = "is not a valid postal code.\n";
 		String invalidMsg = "";
 		int i = 0;
 		
 		for (String criterion : searchCriteria) {
 			if (criterion.endsWith("Value") || criterion.endsWith("Price")) {
 				if (!userSearchInput[i].matches("\\d*")  || 
 						!userSearchInput[i + 1].matches("\\d*")) {
 					invalidMsg = invalidMsg + criterion + numericAlert;
 					isOK = false;
 					i += 2;
 				}
 			}
 			
 			else if (criterion.equals("Postal Code")) {
 				if (!userSearchInput[i].matches("|[A-Z][0-9][A-Z][ ][0-9][A-Z][0-9]")) {
 					invalidMsg = invalidMsg + criterion + postalCodeAlert;
 					isOK = false;
 					i++;
 				}
 			}
 				
 			else {
 				isOK = true;
 				i++;
 			}
 		}
 		
 		if (isOK == false) {
 			Window.alert(invalidMsg);
 		}
 		
 		return isOK;
 	}
 	
 	/**
 	 * Helper to searchHouse().
 	 * Converts user's "For Sale" criterion response into integer.
 	 * 1 = yes; 0 = no; -1 = all;
 	 * Assumption is that the given list of radio button has always 3 buttons. 
 	 * 
 	 * @param forSale	list of radio buttons for "For Sale" criteria
 	 * @return	integer of response
 	 */
 	private int convertRadioBtnSearch(List<RadioButton> forSale) {
 		int isSelling = -1;
 		
 		
 		if (forSale.get(0).getValue() == true) {
 			isSelling = 1; 
 		}
 		else if (forSale.get(1).getValue() == true) {
 			isSelling = 0;
 		}
 		else 
 			isSelling = -1;
 		
 		return isSelling;
 	}
 
 
 }
