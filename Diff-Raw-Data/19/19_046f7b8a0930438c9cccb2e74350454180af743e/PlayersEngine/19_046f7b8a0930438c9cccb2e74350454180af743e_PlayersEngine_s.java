 package cz.krajcovic.playersengine.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import cz.krajcovic.playersengine.base.Player;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class PlayersEngine implements EntryPoint {
 	private static final int REFRESH_INTERVAL = 5000; // ms
 
 	/**
 	 * Create a remote service proxy to talk to the server-side Greeting
 	 * service.
 	 */
 	// private final GreetingServiceAsync greetingService = GWT
 	// .create(GreetingService.class);
 
 	private VerticalPanel mainPanel = new VerticalPanel();
 	private FlexTable playersTable = new FlexTable();
 
 	private HorizontalPanel addPanel = new HorizontalPanel();
 	private TextBox newSecondName = new TextBox();
 	private TextBox newFirstName = new TextBox();
 	private TextBox newDescription = new TextBox();
	private Button addNewPlayerButton = new Button("Add");
 
 	private Label lastUpdatedLabel = new Label();
 
 	private ArrayList<Player> playersList = new ArrayList<Player>();
 	
	private PlayersEngineConstants constants = GWT.create(PlayersEngineConstants.class);
	private PlayersEngineMessages messages = GWT.create(PlayersEngineMessages.class);
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		// Create table for players.
 		playersTable.setText(0, 0, constants.secondName());
 		playersTable.setText(0, 1, constants.firstName());
 		playersTable.setText(0, 2, constants.description());
 		playersTable.setText(0, 3, constants.edit());
 		playersTable.setText(0, 4, constants.remove());
 
 		// Add style to elements it the stock list table.
 		playersTable.setCellPadding(6);
 		playersTable.getRowFormatter().addStyleName(0, "playersListHeader");
 		playersTable.addStyleName("playersList");
 		playersTable.getCellFormatter().addStyleName(0, 0,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(0, 1,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(0, 2,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(0, 3,
 				"playersListButtonColumn");
 		playersTable.getCellFormatter().addStyleName(0, 4,
 				"playersListButtonColumn");
 
 		// Assemble Add new player.
 		addPanel.add(newSecondName);
 		addPanel.add(newFirstName);
 		addPanel.add(newDescription);
 		addPanel.add(addNewPlayerButton);
 		addPanel.addStyleName("addPanel");
 
 		// Assemble Main panel.
 		mainPanel.add(playersTable);
 		mainPanel.add(addPanel);
 		mainPanel.add(lastUpdatedLabel);
 
 		// Associate the Main panel with the HTML host page.
 		RootPanel.get("playersList").add(mainPanel);
 
 		// Move cursor focus to the input box.
 		newSecondName.setFocus(true);
 
 		// Listen for mouse events on the add button
 		addNewPlayerButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				addPlayer();
 
 			}
 		});
 
 		// Setup timer to refresh list automatically
 		Timer refreshTimer = new Timer() {
 
 			@Override
 			public void run() {
 				refreshPlayerList();
 
 			}
 
 		};
 		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
 	}
 
 	protected void refreshPlayerList() {
 		for (Player player : playersList) {
 			updateTable(player);
 		}
 
 	}
 
 	private void updateTable(Player player) {
 		if (!playersList.contains(player)) {
 			return;
 		}
 
 		int row = playersList.indexOf(player) + 1;
 		playersTable.setText(row, 0, player.getSecondName());
 		playersTable.setText(row, 1, player.getFirstName());
 		playersTable.setText(row, 2, player.getDescription());
 
 	}
 
 	/**
 	 * Add stockk to FlexTable. Executed when the user clicks the
 	 * addNewPlayerButton or press enter in the newTextBox.
 	 */
 	protected void addPlayer() {
 		final Player player = new Player();
 		player.setSecondName(newSecondName.getText().trim());
 		player.setFirstName(newFirstName.getText().trim());
 		player.setDescription(newDescription.getText().trim());
 
 		if (!player.validate()) {
			//Window.alert("'" + player.toString() + "' are not a valid symbols.");
 			Window.alert(messages.invalidSymbol(player.toString()));
 			return;
 		}
 
 		// TODO Add the stock to the table.
 		playersList.add(player);
 		int lastRow = playersTable.getRowCount();
 
 		playersTable.setText(lastRow, 0, player.getSecondName());
 		playersTable.setText(lastRow, 1, player.getFirstName());
 		playersTable.setText(lastRow, 2, player.getDescription());
 		playersTable.getCellFormatter().addStyleName(lastRow, 0,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 1,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 2,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 3,
 				"playersListButtonColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 4,
 				"playersListButtonColumn");
 
 		// Add a button to edit and remove this player from the table.
 		Button changePlayerButton = new Button("e");
 		changePlayerButton.addStyleDependentName("edit");
 		changePlayerButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 
 				final DialogBox editDialogBox = new DialogBox();
 				VerticalPanel editPanel = new VerticalPanel();
 				final TextBox editSecondName = new TextBox();
 				final TextBox editFirstName = new TextBox();
 				final TextBox editDescription = new TextBox();
 				Button editSaveButton = new Button("Save");
 				Button editCloseButton = new Button("Close");
 
 				editDialogBox.setText("Edit player");
 				// editDialogBox.setGlassEnabled(true);
 				editDialogBox.setAnimationEnabled(true);
 				// editCloseButton.getElement().setId("closeButton");
 				editPanel.addStyleName("editPanel");
 
 				editPanel.add(editSecondName);
 				editPanel.add(editFirstName);
 				editPanel.add(editDescription);
 				editPanel.add(editSaveButton);
 				editPanel.add(editCloseButton);
 				editDialogBox.setWidget(editPanel);
 
 				editSaveButton.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						player.setSecondName(editSecondName.getText());
 						player.setFirstName(editFirstName.getText());
 						player.setDescription(editDescription.getText());
 
 						if (!player.validate()) {
 							Window.alert("'" + player.toString()
 									+ "' are not a valid symbols.");
 							return;
 						}
 
 						editDialogBox.hide();
 						updateTable(player);
 					}
 				});
 
 				editCloseButton.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						editDialogBox.hide();
 
 					}
 				});
 
 				editSecondName.setText(player.getSecondName());
 				editFirstName.setText(player.getFirstName());
 				editDescription.setText(player.getDescription());
 
 				editDialogBox.center();
 
 				editDialogBox.show();
 
 			}
 		});
 		playersTable.setWidget(lastRow, 3, changePlayerButton);
 
 		Button removePlayerButton = new Button("x");
 		removePlayerButton.addStyleDependentName("remove");
 		removePlayerButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				int removedIndex = playersList.indexOf(player);
 				playersList.remove(removedIndex);
 				playersTable.removeRow(removedIndex + 1);
 
 			}
 		});
 		playersTable.setWidget(lastRow, 4, removePlayerButton);
 
 		newSecondName.setText("");
 		newFirstName.setText("");
 		newDescription.setText("");
 
 	}
 }
