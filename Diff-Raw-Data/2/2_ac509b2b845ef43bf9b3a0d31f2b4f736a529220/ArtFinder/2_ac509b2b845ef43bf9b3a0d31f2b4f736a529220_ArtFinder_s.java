 package com.google.gwt.artvan.client;
 
 import com.google.code.gwt.geolocation.client.Coordinates;
 import com.google.code.gwt.geolocation.client.Geolocation;
 import com.google.code.gwt.geolocation.client.Position;
 import com.google.code.gwt.geolocation.client.PositionCallback;
 import com.google.code.gwt.geolocation.client.PositionError;
 import com.google.code.gwt.geolocation.client.PositionOptions;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Vector;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.Maps;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
 
 public class ArtFinder implements EntryPoint {
 	// stockwatcher items
 	private final int REFRESH_INTERVAL = 1200000; // ms
 	private VerticalPanel mainPanel = new VerticalPanel();
 	private VerticalPanel tablePanel = new VerticalPanel();
 	private FlexTable artFlexTable = new FlexTable();
 	private HorizontalPanel pagePanel = new HorizontalPanel();
 	private HorizontalPanel addPanel = new HorizontalPanel();
 	private HorizontalPanel artPanel = new HorizontalPanel();
 	private HorizontalPanel sortPanel = new HorizontalPanel();
 	private TextBox newSymbolTextBox = new TextBox();
 	private TextBox artSearchTextBox = new TextBox();
 	private Button findArtButton = new Button("findArt");
 	private Button deleteAllArtButton = new Button("DELETE_ALL_Art");
 	private Button sortButton = new Button("Go");
 	private Button addStockButton = new Button("Add");
 	private Label lastUpdatedLabel = new Label();
 	private ListBox sortByLB = new ListBox();
 	private ArrayList<String> stocks = new ArrayList<String>();
 	private StockPriceServiceAsync stockPriceSvc = GWT
 			.create(StockPriceService.class);
 	// private ArtInformationServiceAsync artInfoService =
 	// GWT.create(ArtInformationService.class);
 	private ArtList artlist = new ArtList( this);
 	// login items
 	private LoginInfo loginInfo = null;
 	private VerticalPanel loginPanel = new VerticalPanel();
 	private Label loginLabel = new Label(
 			"Sign in to your GOOG account to access ArtFinder Application");
 	private Anchor signInLink = new Anchor("Sign In");
 	private Anchor signOutLink = new Anchor("Sign Out");
 	private Label userName = new Label("unknown");
 
 	private VerticalPanel geolocationPanel = new VerticalPanel();
 
 	private final StockServiceAsync stockService = GWT
 			.create(StockService.class);
 
 	/**
 	 * Entry point method
 	 */
 
 	public void onModuleLoad() {
 		LoginServiceAsync loginService = GWT.create(LoginService.class);
 		loginService.login(GWT.getHostPageBaseURL(),
 				new AsyncCallback<LoginInfo>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						handleError(caught);
 					}
 
 					public void onSuccess(LoginInfo result) {
 
 						loginInfo = result;
 						if (loginInfo.isLoggedIn()) {
 							System.out.println("user: \""
 									+ loginInfo.getNickname()
 									+ "\"  is logged in, loading ArtWatcher");
 							userName.setText(loginInfo.getNickname());
 							loadArtWatcher();
 							Maps.loadMapsApi("AIzaSyBCzkdwkEfrlBoWpzcqUaoR7PaM7d3kSQ0", "2", false, new Runnable() {
 								public void run() {
 									buildUI();
 								}
 							});
 						} else {
 							System.out
 									.println("user not logged in, loading login page");
 							loadLogin();
 						}
 					}
 				});
 	}
 	
 	private void buildUI() {
 		 LatLng vancouver = LatLng.newInstance(49.25, -123.11);
 		final MapWidget map = new MapWidget(vancouver, 12);
 		map.setSize("60%", "60%");
 		map.addControl(new LargeMapControl());
 		final DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
 		dock.addNorth(map, 1000);
 		RootPanel.get("mapRoot").add(dock);
 	}
 
 	protected void loadLogin() {
 		signInLink.setHref(loginInfo.getLoginUrl());
 		loginPanel.add(loginLabel);
 		loginPanel.add(signInLink);
 		RootPanel.get("artList").add(loginPanel);
 
 	}
 
 	private void loadArtWatcher() {
 		// set up signout link
 		signOutLink.setHref(loginInfo.getLogoutUrl());
 		signOutLink.setVisible(true);
 		// create table for  data
 
 		artFlexTable.setText(0, 0, "Name");
 		artFlexTable.setText(0, 1, "Lat/Long");
 		artFlexTable.setText(0, 2, "Description");
 		artFlexTable.setText(0, 3, "Visits");
 		artFlexTable.setText(0, 4, "Visited");
 		artFlexTable.setText(0, 5, "Rating (5 = highest)");
 
 		// Add styles to elements in the table.
 		artFlexTable.setCellPadding(6);
 
 		// Add styles to elements in the stock list table.
 		artFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
 		artFlexTable.addStyleName("watchList");
 		artFlexTable.getCellFormatter().addStyleName(0, 1,"watchListNumericColumn");
 		artFlexTable.getCellFormatter().addStyleName(0, 2,"watchListNumericColumn");
 		artFlexTable.getCellFormatter().addStyleName(0, 3,"watchListRemoveColumn");
 		artFlexTable.getCellFormatter().addStyleName(0, 4,"watchListAddVisitsColumn");
 		artFlexTable.getCellFormatter().addStyleName(0, 5,"watchListRatingsColumn");
 
 
 		//loadStocks();
 
 		// Assemble Add Stock panel
 		//addPanel.add(addStockButton);
 		//addPanel.add(newSymbolTextBox);
 		//addPanel.addStyleName("addPanel");
 
 		//Assemble Find Art panel
 		artSearchTextBox.setStyleName("artSearchTextbox");
 		findArtButton.setStyleName("findArtButton");
 		artPanel.add(artSearchTextBox);
 		artPanel.add(findArtButton);
 		artPanel.add(deleteAllArtButton);
 		
 		//Assemble Sort Art panel
 		sortButton.addClickHandler(new ClickHandler()
 	    {
 			@Override
 			public void onClick(ClickEvent event) {
 				Window.alert("Will sort by " + sortByLB.getValue(sortByLB.getSelectedIndex()));				
 			}
 	    });
 		sortPanel.addStyleName("sortPanel");
 		sortByLB.addItem("Rating");
 		sortByLB.addItem("Most visited");
 		sortByLB.setVisibleItemCount(1);
 		sortPanel.add(new HTML("Sort by:"));
 		sortPanel.add(sortByLB);
 		sortPanel.add(sortButton);
 		
 		//Assemble Table Panel
 		tablePanel.add(new HTML("<h3>Search Results</h3>"));
 		tablePanel.add(sortPanel);
 		tablePanel.add(artFlexTable);
 		//tablePanel.add(addPanel);
 		tablePanel.add(lastUpdatedLabel	);
 		tablePanel.add(signOutLink);
 		
 		// Assemble Main Panel
 		mainPanel.add(new HTML("<p><h2>Welcome/Intro Message</h2></p>"));
 		mainPanel.add(new HTML("<b>Current login:</b>"));
 		mainPanel.add(userName);
 		mainPanel.add(new HTML("<hr />"));
 		mainPanel.add(new HTML("<h3>Enter query:<h3>"));
 		mainPanel.add(artPanel);	
 
 		// Associate Main panel with the HTML host page
 		pagePanel.add(mainPanel);
 		pagePanel.setSpacing(20);
 		pagePanel.add(tablePanel);
 		RootPanel.get("artList").add(pagePanel);
 		RootPanel.get("artList").add(createUploadForm());
 
 		// Move cursor focus to input box
 		newSymbolTextBox.setFocus(true);
 
 		// test geolocation handler
 		mainPanel.add(geolocationPanel);
 		geolocationPanel.add(new Label("Geolocation provider: "
 				+ Geolocation.getProviderName()));
 		geolocationPanel.add(new Label("GWT strongname: "
 				+ GWT.getPermutationStrongName()));
 
 		Label l1 = new Label("Obtaining Geolocation...");
 		geolocationPanel.add(l1);
 		if (!Geolocation.isSupported()) {
 			l1.setText("Obtaining Geolocation FAILED! Geolocation API is not supported.");
 			return;
 		}
 		final Geolocation geo = Geolocation.getGeolocation();
 		if (geo == null) {
 			l1.setText("Obtaining Geolocation FAILED! Object is null.");
 			return;
 		}
 		l1.setText("Obtaining Geolocation DONE!");
 		obtainPosition(geolocationPanel, geo);
 
 		// Listen for mouse events on add button
 		addStockButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				addStock();
 			}
 		});
 
 		findArtButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				refreshArtList();
 			}
 		});
 		
 		deleteAllArtButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event) {
 				artlist.deleteAllArt();
 				// just assume that delete completes asynchronously and wipe out table here
 				int rows = artFlexTable.getRowCount();
				for (int i = 2; i<rows; i++){
 					artFlexTable.removeRow(1);
 				}
 				//refreshArtList();
 			}
 		});
 
 		// Listen for keyboard
 		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {
 
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				// fix for return on linux, get real keycode, not getChar
 				int keycode = event.getNativeEvent().getKeyCode();
 				System.out.println("keypress " + event.getCharCode() + ": "
 						+ keycode);
 				if (keycode == KeyCodes.KEY_ENTER) {
 					addStock();
 				}
 
 			}
 
 		});
 
 		// setup timer for refreshing prices
 //		Timer refreshTimer = new Timer() {
 //
 //			@Override
 //			public void run() {
 //				refreshWatchList();
 //
 //			}
 //
 //		};
 //		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
 	}
 
 	private void obtainPosition(final VerticalPanel geolocationPanel2,
 			Geolocation geo) {
 		final Label l2 = new Label("Obtaining position (timeout: 15 sec)...");
 		geolocationPanel2.add(l2);
 
 		geo.getCurrentPosition(new PositionCallback() {
 			public void onFailure(PositionError error) {
 				String message = "";
 				switch (error.getCode()) {
 				case PositionError.UNKNOWN_ERROR:
 					message = "Unknown Error";
 					break;
 				case PositionError.PERMISSION_DENIED:
 					message = "Permission Denied";
 					break;
 				case PositionError.POSITION_UNAVAILABLE:
 					message = "Position Unavailable";
 					break;
 				case PositionError.TIMEOUT:
 					message = "Time-out";
 					break;
 				default:
 					message = "Unknown error code.";
 				}
 				l2.setText("Obtaining position FAILED! Message: '"
 						+ error.getMessage() + "', code: " + error.getCode()
 						+ " (" + message + ")");
 			}
 
 			public void onSuccess(Position position) {
 				l2.setText("Obtaining position DONE:");
 				Coordinates c = position.getCoords();
 				geolocationPanel2.add(new Label("lat, lon: " + c.getLatitude()
 						+ ", " + c.getLongitude()));
 				geolocationPanel2.add(new Label("Accuracy (in meters): "
 						+ c.getAccuracy()));
 				geolocationPanel2.add(new Label("Altitude: "
 						+ (c.hasAltitude() ? c.getAltitude() : "[no value]")));
 				geolocationPanel2
 						.add(new Label("Altitude accuracy (in meters): "
 								+ (c.hasAltitudeAccuracy() ? c
 										.getAltitudeAccuracy() : "[no value]")));
 				geolocationPanel2.add(new Label("Heading: "
 						+ (c.hasHeading() ? c.getHeading() : "[no value]")));
 				geolocationPanel2.add(new Label("Speed: "
 						+ (c.hasSpeed() ? c.getSpeed() : "[no value]")));
 			}
 		}, PositionOptions.getPositionOptions(false, 15000, 30000));
 	}
 
 	private void loadStocks() {
 		stockService.getStocks(new AsyncCallback<String[]>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			public void onSuccess(String[] symbols) {
 				displayStocks(symbols);
 			}
 		});
 	}
 
 	protected void displayStocks(String[] symbols) {
 		for (String symbol : symbols) {
 			displayStock(symbol);
 		}
 	}
 
 	protected void addStock() {
 		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
 		newSymbolTextBox.setFocus(true);
 
 		if (!symbol.matches("^[0-9A-Za-z\\.]{1,10}$")) {
 			Window.alert("'" + symbol + "' is not a valid symbol");
 			newSymbolTextBox.selectAll();
 			return;
 		}
 
 		newSymbolTextBox.setText("");
 
 		// dont add stock if its already in table
 		if (stocks.contains(symbol))
 			return;
 
 		// displayStock(symbol);
 		addStock(symbol);
 	}
 
 	private void addStock(final String symbol) {
 		stockService.addStock(symbol, new AsyncCallback<Void>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			public void onSuccess(Void ignore) {
 				displayStock(symbol);
 			}
 		});
 	}
 
 	private void displayStock(final String symbol) {
 		// add stock
 		int row = artFlexTable.getRowCount();
 		stocks.add(symbol);
 		artFlexTable.setText(row, 0, symbol);
 		artFlexTable.setWidget(row, 2, new Label());
 		artFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
 		artFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
 		artFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");
 	    artFlexTable.getCellFormatter().addStyleName(row, 4, "watchListRateColumn");
 	    artFlexTable.getCellFormatter().addStyleName(row, 5, "watchListNumVisitsColumn");	   
 
 		// add button to remove stock
 		Button removeStockButton = new Button("x");
 		removeStockButton.addStyleDependentName("remove");
 
 		removeStockButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 
 				removeStock(symbol);
 			}
 
 		});
 
 		artFlexTable.setWidget(row, 3, removeStockButton);
 		// get stock price
 		refreshWatchList();
 	}
 
 	protected void removeStock(final String symbol) {
 		stockService.removeStock(symbol, new AsyncCallback<Void>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			public void onSuccess(Void ignore) {
 				undisplayStock(symbol);
 			}
 		});
 	}
 
 	private void undisplayStock(final String symbol) {
 		int removedIndex = stocks.indexOf(symbol);
 		stocks.remove(removedIndex);
 		artFlexTable.removeRow(removedIndex + 1);
 	}
 
 	private void refreshWatchList() {
 
 		if (stockPriceSvc == null) {
 			stockPriceSvc = GWT.create(StockPriceService.class);
 		}
 
 		// Set up the callback object.
 		AsyncCallback<StockPrice[]> callback = new AsyncCallback<StockPrice[]>() {
 			public void onFailure(Throwable caught) {
 				// TODO: Do something with errors.
 			}
 
 			public void onSuccess(StockPrice[] result) {
 				updateTable(result);
 			}
 		};
 
 		// Make the call to the stock price service.
 		stockPriceSvc.getPrices(stocks.toArray(new String[0]), callback);
 
 	}
 
 	public void refreshArtList() {
 
 		Vector<ArtInformation> resultVector = artlist.searchByLocation(15, 15);
 		// moved table updates to inside async callback onSuccess 
 //		ArtInformation[] resultarray = new ArtInformation[resultVector.size()];
 //		resultarray = resultVector.toArray(resultarray);
 //		updateTable(resultarray);
 
 	}
 
 	@SuppressWarnings("deprecation")
 	private void updateTable(StockPrice[] prices) {
 		for (int i = 0; i < prices.length; i++) {
 			updateTable(prices[i]);
 		}
 
 		lastUpdatedLabel.setText("Last update : "
 				+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
 
 	}
 
 	public void updateTable(ArtInformation[] artInfo) {
 		int existing_rowcount = artFlexTable.getRowCount();
 		
 		for (int i = 0; i < artInfo.length; i++) {
 			updateTable(artInfo[i], i + 1);
 		}
 		// we have fewer rows than before, clear the excess
 		for (int i = artInfo.length; i< existing_rowcount; i++){
 			if(i!=0) //just so we don't remove the header if length = 0
 				artFlexTable.removeRow(i);
 		}
 		lastUpdatedLabel.setText("Last update : "
 				+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
 
 	}
 
 	private void updateTable(ArtInformation artInfo, int row) {
 
 		String latlong = " " + artInfo.getLat() + " " + artInfo.getLng();
 		String desc = artInfo.getDescription();
 		//fill row with art info
 		artFlexTable.setText(row, 0, artInfo.getName());
 		artFlexTable.setText(row, 1, latlong);
 		artFlexTable.setText(row, 2, desc);
 		artFlexTable.setText(row, 3, "" + artInfo.getVisits());
 		
 		//add button to add visits
 		final int currentRow = row;
 		Button visitedButton = new Button("+");
 		visitedButton.setSize("20px", "20px");
 		visitedButton.addClickHandler(new ClickHandler(){		
 			@Override
 			public void onClick(ClickEvent event) {
 				//increase visitor count of related art object
 				int numVisits = Integer.parseInt(artFlexTable.getText(currentRow, 3)); //get current number of visits shown in table
 				artFlexTable.setText(currentRow, 3, ""+(++numVisits));
 				//TODO Persist new visit count to server
 			}	
 		});
 		artFlexTable.setWidget(row, 4, visitedButton);
 		
 		//add ratings selector
 		RadioButton rb1 = new RadioButton("Rating", "1");
 	    RadioButton rb2 = new RadioButton("Rating", "2");
 	    RadioButton rb3 = new RadioButton("Rating", "3");
 	    RadioButton rb4 = new RadioButton("Rating", "4");
 	    RadioButton rb5 = new RadioButton("Rating", "5");
 	    FlowPanel ratingsPanel = new FlowPanel();
 	    ratingsPanel.add(rb1);
 	    ratingsPanel.add(rb2);
 	    ratingsPanel.add(rb3);
 	    ratingsPanel.add(rb4);
 	    ratingsPanel.add(rb5);
 	    artFlexTable.setWidget(row, 5, ratingsPanel);
 		
 	}
 
 	private void updateTable(StockPrice stockPrice) {
 		if (!stocks.contains(stockPrice.getSymbol())) {
 			return;
 		}
 
 		int row = stocks.indexOf(stockPrice.getSymbol()) + 1;
 
 		String priceText = NumberFormat.getFormat("#,##0.00").format(
 				stockPrice.getPrice());
 		NumberFormat changeFormat = NumberFormat
 				.getFormat("+#,##0.00;-#,##0.00");
 		String changeText = changeFormat.format(stockPrice.getChange());
 		String changePercentText = changeFormat.format(stockPrice
 				.getChangePercent() / stockPrice.getPrice());
 
 		artFlexTable.setText(row, 1, priceText);
 		// insert label into element and update label instead of setText of
 		// flextable element
 		// artFlexTable.setText(row, 2, changeText + " (" + changePercentText +
 		// "%)");
 		Label changeWidget = (Label) artFlexTable.getWidget(row, 2);
 		changeWidget.setText(changeText + " (" + changePercentText + "%)");
 
 		// Change the color of text in the Change field based on its value.
 		String changeStyleName = "noChange";
 		if (stockPrice.getChangePercent() < -0.1f) {
 			changeStyleName = "negativeChange";
 		} else if (stockPrice.getChangePercent() > 0.1f) {
 			changeStyleName = "positiveChange";
 		}
 
 		changeWidget.setStyleName(changeStyleName);
 	}
 
 	private FormPanel createUploadForm(){
 		//file upload form and button
 		final FormPanel uploadForm = new FormPanel();
 		VerticalPanel uploadPanel = new VerticalPanel();
 		HorizontalPanel buttonPanel = new HorizontalPanel();
 		final FileUpload fileSelector = new FileUpload();
 		Button submitButton = new Button("Upload File");
 		Button updateButton = new Button("Update from Data Vancouver");
 		//Button downloadButton = new Button("Download current data");
 		
 		// Add upload form, courtesy of http://examples.roughian.com/index.htm#Widgets~FileUpload
 	    uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
 	    uploadForm.setMethod(FormPanel.METHOD_POST);
 	    uploadForm.addStyleName("uploadForm");
 	    fileSelector.setName("uploadButton");
 	    uploadPanel.add(new HTML("<b>Upload xml file:</b><br />"));
 	    uploadPanel.add(new HTML("<b><font color ='red'>(ADMIN USE ONLY)</b><br />"));
 	    uploadPanel.add(fileSelector);	    
 	    uploadPanel.setHorizontalAlignment(HasAlignment.ALIGN_LEFT);
 	    submitButton.addClickHandler(new ClickHandler()
 	    {
 			@Override
 			public void onClick(ClickEvent event) {
 				uploadForm.submit();				
 			}
 	    });
 	    //TODO clickhandlers for download and update buttons
 	    uploadPanel.add(new HTML("<hr />"));
 	    buttonPanel.add(submitButton);
 	    buttonPanel.add(updateButton);
 	    //buttonPanel.add(downloadButton);
 	    uploadPanel.add(buttonPanel);
 	    uploadForm.add(uploadPanel);
 	    uploadForm.setAction("/UploadArt");
 	    uploadForm.addSubmitHandler(new SubmitHandler()
 	    {
 			@Override
 			public void onSubmit(SubmitEvent event) {
 				if(fileSelector.getFilename().endsWith("xml"))
 					uploadForm.submit();
 				else if(fileSelector.getFilename().equals(""))
 					Window.alert("Please select a xml file first");
 				else
 					Window.alert("Not a XML file");				
 			}
 	    });	    
 	    return uploadForm;
 	}
 	
 	private void handleError(Throwable error) {
 		Window.alert(error.getMessage());
 		if (error instanceof NotLoggedInException) {
 			Window.Location.replace(loginInfo.getLogoutUrl());
 		}
 	}
 
 	public FlexTable getFlexTable() {
 		// TODO Auto-generated method stub
 		return artFlexTable;
 	}
 
 	public Label getLastUpdatedLabel() {
 		// TODO Auto-generated method stub
 		return lastUpdatedLabel;
 	}
 
 }
