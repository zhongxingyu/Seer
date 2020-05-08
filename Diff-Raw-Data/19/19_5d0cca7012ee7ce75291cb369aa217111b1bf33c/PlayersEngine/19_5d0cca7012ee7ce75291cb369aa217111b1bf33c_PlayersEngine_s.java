 package cz.krajcovic.playersengine.client;
 
import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import cz.krajcovic.playersengine.base.Player;
 import cz.krajcovic.playersengine.client.langs.PlayersEngineConstants;
 import cz.krajcovic.playersengine.client.langs.PlayersEngineMessages;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class PlayersEngine implements EntryPoint {
 	private static final int REFRESH_INTERVAL = 5000; // ms
 
 	/**
 	 * Create a remote service proxy to talk to the server-side Players service.
 	 */
 	private final PlayersServiceAsync playersService = GWT
 			.create(PlayersService.class);
 
 	private VerticalPanel mainPanel = new VerticalPanel();
 	private FlexTable playersTable = new FlexTable();
 
 	private HorizontalPanel addPanel = new HorizontalPanel();
 	private TextBox newSecondName = new TextBox();
 	private TextBox newFirstName = new TextBox();
 	private TextBox newDescription = new TextBox();
 
 	private Label lastUpdatedLabel = new Label();
 	private Button addNewPlayerButton = new Button();
 
 	// private ArrayList<Player> playersList = new ArrayList<Player>();
 
 	private PlayersEngineConstants constants = GWT
 			.create(PlayersEngineConstants.class);
 	private PlayersEngineMessages messages = GWT
 			.create(PlayersEngineMessages.class);
 
 	private AsyncCallback<List<Player>> getAllCallback = new AsyncCallback<List<Player>>() {
 
 		@Override
 		public void onFailure(Throwable caught) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onSuccess(List<Player> result) {
 			playersTable.removeAllRows();
 
 			createPlayersTableHeader();
 
 			for (Player player : result) {
 				updatePlayers(player);
 			}
 
 		}
 	};
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		createPlayersTableHeader();
 
 		// Assemble Add new player.
 		addPanel.add(newSecondName);
 		addPanel.add(newFirstName);
 		addPanel.add(newDescription);
 		addNewPlayerButton.setText(constants.add());
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
 				// refreshPlayerList();
 
 			}
 
 		};
 		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
 
 		// refreshPlayerList();
 	}
 
 	private void createPlayersTableHeader() {
 		// Create table for players.
 		playersTable.setText(0, 0, constants.id());
 		playersTable.setText(0, 1, constants.secondName());
 		playersTable.setText(0, 2, constants.firstName());
 		playersTable.setText(0, 3, constants.description());
 		playersTable.setText(0, 4, constants.edit());
 		playersTable.setText(0, 5, constants.remove());
 
 		// Add style to elements it the stock list table.
 		playersTable.setCellPadding(6);
 		playersTable.getRowFormatter().addStyleName(0, "playersListHeader");
 
 		playersTable.addStyleName("playersList");
 		playersTable.getCellFormatter().addStyleName(0, 0,
 				"playersListNumericColumn");
 		playersTable.getCellFormatter().addStyleName(0, 1,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(0, 2,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(0, 3,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(0, 4,
 				"playersListButtonColumn");
 		playersTable.getCellFormatter().addStyleName(0, 5,
 				"playersListButtonColumn");
 	}
 
 	protected void refreshPlayerList() {
 		// for (Player player : playersService.getAll(getAllCallback ) ) {
 		// updateTable(player);
 		// }
 		playersService.getAll(getAllCallback);
 	}
 
 	private void updatePlayers(Player player) {
 		// int row = playersList.indexOf(player) + 1;
 		// int row = playersTable.getRowCount();
 		// playersTable.setText(row, 0, String.valueOf(player.getId()));
 		// playersTable.setText(row, 1, player.getSecondName());
 		// playersTable.setText(row, 2, player.getFirstName());
 		// playersTable.setText(row, 3, player.getDescription());
 
 		add2Table(player);
 	}
 
 	/**
 	 * Add stockk to FlexTable. Executed when the user clicks the
 	 * addNewPlayerButton or press enter in the newTextBox.
 	 */
 	protected void addPlayer() {
 		final Player player = new Player();
 		player.setId(playersTable.getRowCount()); // TODO: predelat!!!!
 		player.setSecondName(newSecondName.getText().trim());
 		player.setFirstName(newFirstName.getText().trim());
 		player.setDescription(newDescription.getText().trim());
 
 		if (!player.validate()) {
 			// Window.alert("'" + player.toString() +
 			// "' are not a valid symbols.");
 			Window.alert(messages.invalidSymbol(player.toString()));
 			return;
 		}
 
 		// TODO Add the stock to the table.
 		// playersList.add(player);
 		playersService.add(player, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(messages.addNewPlayerFailed());
 
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				newSecondName.setText("");
 				newFirstName.setText("");
 				newDescription.setText("");
 				add2Table(player);
 				Window.alert(messages.addNewPlayerSuccess());
 
 			}
 		});
 	}
 
 	private void add2Table(final Player player) {
 		int lastRow = playersTable.getRowCount();
 
 		playersTable.setText(lastRow, 0, String.valueOf(player.getId()));
 		playersTable.setText(lastRow, 1, player.getSecondName());
 		playersTable.setText(lastRow, 2, player.getFirstName());
 		playersTable.setText(lastRow, 3, player.getDescription());
 		playersTable.getCellFormatter().addStyleName(lastRow, 0,
 				"playersListNumericColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 1,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 2,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 3,
 				"playersListStringColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 4,
 				"playersListButtonColumn");
 		playersTable.getCellFormatter().addStyleName(lastRow, 5,
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
 				Button editSaveButton = new Button(constants.save());
 				Button editCloseButton = new Button(constants.close());
 
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
 							Window.alert(messages.invalidSymbol(player
 									.toString()));
 
 						} else {
 
 							playersService.update(player.getId(), player,
 									new AsyncCallback<Void>() {
 
 										@Override
 										public void onFailure(Throwable caught) {
 											Window.alert(messages.playerUpdateFailed());
 
 										}
 
 										@Override
 										public void onSuccess(Void result) {
 											editDialogBox.hide();
 											// updateTable(player);
 											refreshPlayerList();
 
 										}
 									});
 						}
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
 		playersTable.setWidget(lastRow, 4, changePlayerButton);
 
 		Button removePlayerButton = new Button("x");
 		removePlayerButton.addStyleDependentName("remove");
 		removePlayerButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// int removedIndex = playersList.indexOf(player);
 				// playersList.remove(removedIndex);
 				playersService.remove(player.getId(), new AsyncCallback<Boolean>() {
 
 					@Override
 					public void onSuccess(Boolean result) {
 						// TODO Auto-generated method stub
 						if(result)
 						{
 							refreshPlayerList();
 						}
 						else
 						{
 							Window.alert(messages.playerRemoveFailed());
 						}
 
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						// TODO Auto-generated method stub
 						Window.alert(messages.playerRemoveFailed());
 
 					}
 				});
 				// playersTable.removeRow(player.getId());
 
 			}
 		});
 		playersTable.setWidget(lastRow, 5, removePlayerButton);
 	}
 }
