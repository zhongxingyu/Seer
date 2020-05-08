 package kea.kme.pullpit.client.UI;
 
 import kea.kme.pullpit.client.UI.widgets.ShowWidget;
 import kea.kme.pullpit.client.objects.Band;
 import kea.kme.pullpit.client.objects.LogEntry;
 import kea.kme.pullpit.client.objects.Show;
 import kea.kme.pullpit.client.objects.ShowState;
 import kea.kme.pullpit.client.services.AdminService;
 import kea.kme.pullpit.client.services.AdminServiceAsync;
 import kea.kme.pullpit.client.services.LoginInfo;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Mikkel Clement
  * 
  */
 
 public class UIMain {
 	private String userName;
 	private Anchor signOutLink;
 	// TODO Constants to create menubuttons. should be get methods to fetch from
 	// language files
 	private PullPitConstants constants = GWT.create(PullPitConstants.class);
 	private PullPitMessages messages = GWT.create(PullPitMessages.class);
 	private AdminServiceAsync asa;
 
	private Button homeButton,showsButton,bandsButton,venuesButton,documentsButton,contactsButton,settingsButton,logoutButton;
 
 	private HorizontalPanel contentPanel;
 
 	private FlexTable bandTable;
 	private FlexTable showTable;
 	private FlexTable logTable;
 	MenuAction menuActions;
 
 	private static UIMain uiMain;
 
 	private UIMain() {
 		
 		// Defining area of content
 		contentPanel = new HorizontalPanel();
 		RootPanel.get("textArea").add(contentPanel);
 
 		// Initialising buttons
 		homeButton = new Button(constants.home());
 		showsButton = new Button(constants.Show());
 		bandsButton = new Button(constants.Band());
 		venuesButton = new Button(constants.venues());
 		documentsButton = new Button(constants.documents());
 		contactsButton = new Button(constants.contacts());
 		settingsButton = new Button("");
 		settingsButton.setHTML("<img border='0' src='resources/wrench.png' />");
 		
		logoutButton = new Button(constants.logout());
 
 		menuActions = new MenuAction();
 		menuActions.mainMenuHandler(homeButton, showsButton, bandsButton, venuesButton,
				documentsButton, contactsButton, settingsButton, logoutButton);

 		
 		
 		// Adding Buttons to rootPanel
 		RootPanel.get("homeButton").add(homeButton);
 		RootPanel.get("showsButton").add(showsButton);
 		RootPanel.get("bandsButton").add(bandsButton);
 		RootPanel.get("venuesButton").add(venuesButton);
 		RootPanel.get("documentsButton").add(documentsButton);
 		RootPanel.get("contactsButton").add(contactsButton);
 		RootPanel.get("settingsButton").add(settingsButton);
 		RootPanel.get("logoutButton").add(signOutLink);
 		
 
 		DOM.setElementAttribute(homeButton.getElement(), "id", "highlightButton");
 		createLogTable();
 //		changeContentTo(createLogTable());
 	}
 
 
 	/**
 	 * Creates a Flextable with the received parameter String array
 	 * 
 	 * @param bands
 	 * @return Flextable
 	 */
 
 	public void displayBandTable(Band... bandObject) {
 
 		bandTable = new FlexTable();
 		bandTable.setTitle("band");
 		bandTable.getRowFormatter().addStyleName(0, "tableHeader");
 		bandTable.setStyleName("dataTable");
 
 		bandTable.setText(0, 0, constants.bandName());
 		bandTable.setText(0, 1, constants.bandCountry());
 		bandTable.setText(0, 2, constants.promoter());
 		bandTable.setText(0, 3, constants.lastEdit());
 		int count = 1;
 		if (bandObject.length > 0) {
 			for (Band b : bandObject) {
 				bandTable.setText(count, 0, b.getBandName());
 				bandTable.setText(count, 1, b.getBandCountry());
 				bandTable.setText(count, 2, b.getPromoter().getPromoName());
 				bandTable.setText(count, 3, "" + b.getLastEdit());
 				bandTable.setText(count, 4, "" + b.getBandID());
 				bandTable.getCellFormatter().setVisible(count, 4, false);
 
 				if (count % 2 == 0) {
 					bandTable.getRowFormatter().addStyleName(count,
 							"tableRowColorBrown");
 				} else {
 					bandTable.getRowFormatter().addStyleName(count,
 							"tableRowColorWhite");
 				}
 				// if (bandObject[count] == null) {
 				// break;
 				// }
 				count++;
 			}
 
 		} else {
 			Window.alert("ops");
 		}
 		changeContentTo(bandTable);
 		TableActions ta = new TableActions();
 		ta.addTableEvents(bandTable);
 	}
 
 	public void displayShowTable(Show... showObject) {
 		showTable = new FlexTable();
 		showTable.setTitle("show");
 		showTable.getRowFormatter().addStyleName(0, "tableHeader");
 		showTable.setStyleName("dataTable");
 		showTable.getColumnFormatter().setWidth(0, "250px");
 		showTable.getColumnFormatter().setWidth(1, "250px");
 		showTable.getColumnFormatter().setWidth(2, "120px");
 		showTable.getColumnFormatter().setWidth(4, "120px");
 		showTable.setText(0, 0, constants.band());
 		showTable.setText(0, 1, constants.venue());
 		showTable.setText(0, 2, constants.date());
 		showTable.setText(0, 3, constants.state());
 		showTable.setText(0, 4, constants.lastEdit());
 
 		int count = 1;
 		if (showObject.length > 0) {
 			for (Show s : showObject) {
 				showTable.setText(count, 0, s.getBand().getBandName());
 				showTable.setText(count, 1, s.getVenuesString());
 				showTable.setText(count, 2, s.getDate());
 				showTable.setText(count, 3,
 						"" + ShowState.getShowStateByInt(s.getState()));
 				showTable.setText(count, 4, "" + s.getLastEdit());
 				showTable.setWidget(count, 5, new ShowWidget(s));
 				showTable.getCellFormatter().setVisible(count, 5, false);
 
 				if (count % 2 == 0) {
 					showTable.getRowFormatter().addStyleName(count,
 							"tableRowColorBrown");
 				} else {
 					showTable.getRowFormatter().addStyleName(count,
 							"tableRowColorWhite");
 				}
 				count++;
 			}
 		} else {
 			Window.alert(constants.noResults());
 		}
 		changeContentTo(showTable);
 		TableActions ta = new TableActions();
 		ta.addTableEvents(showTable);
 	}
 
 	/**
 	 * Clearing content on the contentPanel and adding the widget received as
 	 * parameter
 	 * 
 	 * @param w
 	 */
 
 	public void changeContentTo(Widget... widgetArgs) {
 		contentPanel.clear();
 		for (Widget w : widgetArgs) {
 			contentPanel.add(w);
 		}
 	}
 
 	public static UIMain getInstance() {
 		if (uiMain == null)
 			uiMain = new UIMain();
 		return uiMain;
 	}
 
 	
 	public HorizontalPanel getContentPanel() {
 		return contentPanel;
 	}
 
 	public void createLogTable() {
 		if (asa == null)
 			asa = GWT.create(AdminService.class);
 		AsyncCallback<LogEntry[]> callback = CallbackFactory.getLogCallback();
 		asa.getLog(0, 30, callback);
 	}
 
 
 	public void displayLogTable(LogEntry... entries) {
 		
 		logTable = new FlexTable();
 		logTable.setTitle("log");
 		logTable.getRowFormatter().addStyleName(0, "tableHeader");
 		logTable.setStyleName("dataTable");
 		logTable.setText(0, 0, constants.logChanges());
 		
 		int count = 1;
 		for (LogEntry le : entries) {
 			String actionType = "";
 			if (le.getActionType() == 1) {
 				actionType = constants.created();
 			} else if (le.getActionType() == 2) {
 				actionType = constants.edited();
 			} else if (le.getActionType() == 3) {
 				actionType = constants.deleted();
 			}
 			String objectType = "";
 			if (le.getObjectType() == 1) {
 				objectType = constants.aShow();
 			} else if (le.getObjectType() == 2) {
 				objectType = constants.aDoc();
 			} else if (le.getObjectType() == 3) {
 				objectType = constants.anExpenseLine();
 			}
 			String logText = messages.logMessage(le.getUserName(), actionType, objectType, le.getObjectID(), le.getLastEdit());
 			logTable.setText(count, 0, logText);
 			count++;
 		}
 		menuActions.highlightButton(homeButton);
 		changeContentTo(logTable);
 
 	}
 
 	public void setUser(LoginInfo loginInfo) {
 		setUserName(loginInfo.getPullPitUserName());
 		setSignOutLink(loginInfo.getLogoutUrl(), getUserName());
 	}
 	
 	private void setSignOutLink(String href, String text) {
 		signOutLink.setHref(href);
 		signOutLink.setText(messages.signOut(text));
 	}
 	
 	private void setUserName(String userName) {
 		this.userName = userName;
 	}
 	
 	public String getUserName() {
 		return userName;
 	}
 }
