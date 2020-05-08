 package kea.kme.pullpit.client.UI;
 
 import kea.kme.pullpit.client.objects.Band;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Mikkel Clement
  * 
  */
 
 public class UIMain {
 
 	// TODO Constants to create menubuttons. should be get methods to fetch from
 	// language files
 	private String homeButtonText = "Home";
 	private String showsButtonText = "Shows";
 	private String bandsButtonText = "Bands";
 	private String venuesButtonText = "Venues";
 	private String documentsButtonText = "Dokumenter";
 	private String contactsButtonText = "Kontakter";
 
 	private Button homeButton;
 	private Button showsButton;
 	private Button bandsButton;
 	private Button venuesButton;
 	private Button documentsButton;
 	private Button contactsButton;
 	private Button settingsButton;
 
 	private HorizontalPanel contentPanel;
 
 	private FlexTable bandTable;
 	private FlexTable showTable;
 
 	private static UIMain uiMain = new UIMain();
 
 	private UIMain() {
 
 		// Defining area of content
 		contentPanel = new HorizontalPanel();
 		RootPanel.get("textArea").add(contentPanel);
 
 		// Initialising buttons
 		homeButton = new Button(homeButtonText);
 		showsButton = new Button(showsButtonText);
 		bandsButton = new Button(bandsButtonText);
 		venuesButton = new Button(venuesButtonText);
 		documentsButton = new Button(documentsButtonText);
 		contactsButton = new Button(contactsButtonText);
 		settingsButton = new Button("");
		settingsButton.setHTML("<img border='0' src='resources/wrench.png' />");
 	
 
 		MenuAction ma = new MenuAction();
 		ma.mainMenuHandler(homeButton, showsButton, bandsButton, venuesButton,
 				documentsButton, contactsButton,settingsButton);
 
 		// Adding Buttons to rootPanel
 		RootPanel.get("homeButton").add(homeButton);
 		RootPanel.get("showsButton").add(showsButton);
 		RootPanel.get("bandsButton").add(bandsButton);
 		RootPanel.get("venuesButton").add(venuesButton);
 		RootPanel.get("documentsButton").add(documentsButton);
 		RootPanel.get("contactsButton").add(contactsButton);
 		RootPanel.get("settingsButton").add(settingsButton);
 		
 		changeContentTo(new Label("asd"));
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
 		bandTable.setText(0, 0, "Navn");
 		bandTable.setText(0, 1, "Land");
 		bandTable.setText(0, 2, "Promoter");
 		bandTable.setText(0, 3, "Sidst ændret");
 		int count = 1;
 		if (bandObject.length > 0) {
 			for (Band b : bandObject) {
 				bandTable.setText(count, 0, b.getBandName());
 				bandTable.setText(count, 1, b.getBandCountry());
 				bandTable.setText(count, 2, b.getPromoter().getPromoName());
 				bandTable.setText(count, 3, "" + b.getLastEdit());
 
 				if (count % 2 == 0) {
 					bandTable.getRowFormatter().addStyleName(count,
 							"tableRowColorBrown");
 				} else {
 					bandTable.getRowFormatter().addStyleName(count,
 							"tableRowColorWhite");
 				}
 				if (bandObject[count]==null){
 					break;
 				}
 				count++;
 			}
 
 		} else {
 			Window.alert("ops");
 		}
 		changeContentTo(bandTable);
 		TableActions ta = new TableActions();
 		ta.addTableEvents(bandTable);
 	}
 
 	// skal ændres til show columns
 	public void displayShowTable(Band... showObject) {
 		showTable = new FlexTable();
 		showTable.setTitle("show");
 		showTable.getRowFormatter().addStyleName(0, "tableHeader");
 		showTable.setStyleName("dataTable");
 		showTable.setText(0, 0, "IDsssss");
 		showTable.setText(0, 1, "Navn");
 		showTable.setText(0, 2, "Land");
 		showTable.setText(0, 3, "Promoter");
 		showTable.setText(0, 4, "Sidst ændret");
 
 		int count = 1;
 		for (Band b : showObject) {
 			showTable.setText(count, 0, b.getBandName());
 			showTable.setText(count, 1, b.getBandCountry());
 			showTable.setText(count, 2, b.getPromoter().getPromoName());
 			showTable.setText(count, 3, "" + b.getLastEdit());
 
 			if (count % 2 == 0) {
 				showTable.getRowFormatter().addStyleName(count,
 						"tableRowColorBrown");
 			} else {
 				showTable.getRowFormatter().addStyleName(count,
 						"tableRowColorWhite");
 			}
 			count++;
 		}
 		changeContentTo(showTable);
 		// TableActions ta = new TableActions();
 		// ta.addTableEvents(showTable);
 	}
 
 	/**
 	 * Clearing content on the contentPanel and adding the widget received as
 	 * parameter
 	 * 
 	 * @param w
 	 */
 
 	public void changeContentTo(Widget w) {
 		contentPanel.clear();
 		contentPanel.add(w);
 	}
 
 	public static UIMain getInstance() {
 		return uiMain;
 	}
 
 	public HorizontalPanel getContentPanel() {
 		return contentPanel;
 	}
 
 }
