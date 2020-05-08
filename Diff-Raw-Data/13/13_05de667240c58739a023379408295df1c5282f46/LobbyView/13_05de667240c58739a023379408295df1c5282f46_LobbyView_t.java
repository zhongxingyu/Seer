 package com.storytime.client.view;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.storytime.client.StoryTimeEntryMVP;
 import com.storytime.client.StoryTimeServiceAsync;
 import com.storytime.client.changeviewevents.CustomizeSpellsLocalEvent;
 import com.storytime.client.changeviewevents.HostRoomWindowLocalEvent;
 import com.storytime.client.changeviewevents.JoinRoomWindowLocalEvent;
 import com.storytime.client.lobby.LobbyInformation;
 import com.storytime.client.lobby.LobbyUserLeftEvent;
 import com.storytime.client.lobby.UpdateLobbyMessagesEvent;
 import com.storytime.client.lobby.UpdateLobbyUsersEvent;
 
 import de.novanic.eventservice.client.event.Event;
 import de.novanic.eventservice.client.event.RemoteEventService;
 import de.novanic.eventservice.client.event.domain.Domain;
 import de.novanic.eventservice.client.event.domain.DomainFactory;
 import de.novanic.eventservice.client.event.listener.RemoteEventListener;
 
 public class LobbyView extends Composite implements com.storytime.client.presenters.LobbyPresenter.Display {
 
 	boolean DEBUG = true;
 	ArrayList<String> usersInLobby = new ArrayList<String>();
 	ArrayList<String> availableRooms = new ArrayList<String>();
 	ArrayList<String> chatWindowMessages = new ArrayList<String>();
 	String roomSelection = "";
 	StoryTimeServiceAsync rpcService = StoryTimeEntryMVP.rpcService;
 	RemoteEventService theRemoteEventService = StoryTimeEntryMVP.theRemoteEventService;
 	String totalChat = "";
 	HandlerManager eventBus = StoryTimeEntryMVP.eventBus;
 
 	FlowPanel mainFlowPanel = new FlowPanel();
 	HorizontalPanel horizontalInMainFlow = new HorizontalPanel();
 
 	VerticalPanel firstVerticalUsersInLobby = new VerticalPanel();
 	Label lblUsersInLobby = new Label("Users in Lobby");
 	ListBox usersListBox = new ListBox();
 
 	Label lblHellousername = new Label("Lobby Chat");
 
 	VerticalPanel secondVerticalLobbyChat = new VerticalPanel();
 	TextArea chatTextArea = new TextArea();
 	HorizontalPanel textToSendBoxAndButtonHolder = new HorizontalPanel();
 	TextBox textToSendToChat = new TextBox();
 	Button btnSend = new Button("Send");
 
 	VerticalPanel thirdVerticalRoomOptions = new VerticalPanel();
 	Button btnJoinRoom = new Button("Join Room");
 
 	Label lblStartYourOwn = new Label("Host/Join");
 
 	VerticalPanel underThirdVertHostOptions = new VerticalPanel();
 	Button btnHost = new Button("Host");
 	private final HorizontalPanel spellCustomizationPanel = new HorizontalPanel();
 	private final Button btnSpellCustomization = new Button("Spell Customization");
 
 	public LobbyView() {
 		deactivateExtraneousListeners();
		rpcService.enterLobby(new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("Failed to join the lobby room");
			}

			@Override
			public void onSuccess(Void result) {
				System.out.println("Successfully joined the lobby");
			}
			
		});
 		initWidget(mainFlowPanel);
 		System.out.println("Client: Trying to initialize the lobby view");
 		initialize();
 	}
 
 	public void getInitialLobbyInformation() {
 		rpcService.getInitialLobbyInformation(new AsyncCallback<LobbyInformation>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (DEBUG)
 					System.out.println("Failed to get the initial lobby information");
 			}
 
 			@Override
 			public void onSuccess(LobbyInformation result) {
 				if (DEBUG)
 					System.out.println("Got the initial lobby information");
 				for (String user : result.users) {
 					if (DEBUG)
 						System.out.println("Client: Users: " + user);
 					usersInLobby.add(user);
 				}
 				for (String r : result.rooms) {
 					if (DEBUG)
 						System.out.println("Client: Room: " + r);
 					availableRooms.add(r);
 				}
 				for (String message : result.chatMessages) {
 					chatWindowMessages.add(message);
 					if (DEBUG)
 						System.out.println("Client: Messages: " + message);
 				}
 				populateLobbyViewWithInformation();
 			}
 		});
 	}
 
 	/**
 	 * This method will initialize the view after the lobby information has been
 	 * retrieved
 	 * 
 	 */
 	public void initialize() {
 		setPanelOrder();
 		setPanelCharacteristics();
 		setRemoteEventListenersAndHandleEvents();
 		populateLobbyViewWithInformation();
 		setHandlers();
 		getInitialLobbyInformation();
 	}
 
 	/**
 	 * Sets up the panel framework for the lobby
 	 * 
 	 */
 	public void setPanelOrder() {
 		mainFlowPanel.add(horizontalInMainFlow);
 		horizontalInMainFlow.add(firstVerticalUsersInLobby);
 		lblUsersInLobby.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		firstVerticalUsersInLobby.add(lblUsersInLobby);
 		lblUsersInLobby.setSize("100%", "56px");
 		firstVerticalUsersInLobby.add(usersListBox);
 		spellCustomizationPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		spellCustomizationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		firstVerticalUsersInLobby.add(spellCustomizationPanel);
 		spellCustomizationPanel.setSize("100%", "52px");
 
 		btnSpellCustomization.setStyleName("gwt-LoginExistingButton");
 
 		spellCustomizationPanel.add(btnSpellCustomization);
 		btnSpellCustomization.setSize("182px", "30px");
 		horizontalInMainFlow.add(secondVerticalLobbyChat);
 		secondVerticalLobbyChat.add(lblHellousername);
 		secondVerticalLobbyChat.add(chatTextArea);
 		secondVerticalLobbyChat.add(textToSendBoxAndButtonHolder);
 		textToSendBoxAndButtonHolder.add(textToSendToChat);
 		textToSendBoxAndButtonHolder.add(btnSend);
 		thirdVerticalRoomOptions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		horizontalInMainFlow.add(thirdVerticalRoomOptions);
 		thirdVerticalRoomOptions.add(lblStartYourOwn);
 		thirdVerticalRoomOptions.add(btnJoinRoom);
 		underThirdVertHostOptions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		thirdVerticalRoomOptions.add(underThirdVertHostOptions);
 		underThirdVertHostOptions.add(btnHost);
 	}
 
 	/**
 	 * Sets up how the panels will look
 	 * 
 	 */
 	public void setPanelCharacteristics() {
 		mainFlowPanel.setSize("955px", "427px");
 		mainFlowPanel.setStyleName("LobbyPage");
 
 		horizontalInMainFlow.setSize("100%", "100%");
 
 		firstVerticalUsersInLobby.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		firstVerticalUsersInLobby.setSize("100%", "100%");
 
 		usersListBox.setName("User List");
 		usersListBox.setSize("95%", "279px");
 		usersListBox.setVisibleItemCount(5);
 
 		secondVerticalLobbyChat.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		secondVerticalLobbyChat.setSize("100%", "100%");
 
 		lblHellousername.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		lblHellousername.setSize("100%", "52px");
 
 		chatTextArea.setReadOnly(true);
 		chatTextArea.setDirectionEstimator(true);
 		chatTextArea.setSize("95%", "264px");
 		chatTextArea.setText(totalChat);
 		textToSendBoxAndButtonHolder.setSize("100%", "31px");
 
 		textToSendToChat.setName("Send To Chat Field");
 		textToSendToChat.setSize("95%", "22px");
 
 		btnSend.setSize("100%", "30px");
 		btnSend.setStyleName("gwt-LoginExistingButton");
 		thirdVerticalRoomOptions.setSize("100%", "100%");
 
 		btnJoinRoom.setSize("182px", "28px");
 		btnJoinRoom.setStyleName("gwt-LoginExistingButton");
 
 		lblStartYourOwn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		lblStartYourOwn.setSize("100%", "49px");
 		underThirdVertHostOptions.setSize("100%", "31px");
 
 		btnHost.setStyleName("gwt-LoginExistingButton");
 		btnHost.setText("Host Room\r\n");
 		btnHost.setSize("182px", "28px");
 	}
 
 	/**
 	 * Adds local event handlers
 	 * 
 	 */
 	public void setHandlers() {
 		textToSendToChat.addKeyDownHandler(new KeyDownHandler() {
 			public void onKeyDown(KeyDownEvent event) {
 				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 					if (!textToSendToChat.getText().equalsIgnoreCase("")) {
 						rpcService.sendLobbyChatMessage(textToSendToChat.getText(), new AsyncCallback<Void>() {
 
 							@Override
 							public void onFailure(Throwable caught) {
 							}
 
 							@Override
 							public void onSuccess(Void result) {
 								System.out.println("Client: Sent Message: " + textToSendToChat.getText());
 								textToSendToChat.setText("");
 							}
 						});
 					}
 				}
 			}
 		});
 
 		btnSend.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				// send message and update the chat window
 				if (!textToSendToChat.getText().equals("")) {
 					rpcService.sendLobbyChatMessage(textToSendToChat.getText(), new AsyncCallback<Void>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 						}
 
 						@Override
 						public void onSuccess(Void result) {
 							System.out.println("Client: Sent Message: " + textToSendToChat.getText());
 						}
 					});
 					textToSendToChat.setText(""); // blank out the message bar
 				}
 			}
 		});
 
 		btnJoinRoom.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				// Go to the join room page
 				rpcService.leaveLobby(new AsyncCallback<Boolean>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.out.println("Client: Failed to leave the lobby");
 					}
 
 					@Override
 					public void onSuccess(Boolean result) {
 						if (result) {
 							System.out.println("Fired JoinRoomWindowLocalEvent");
 							eventBus.fireEvent(new JoinRoomWindowLocalEvent());
 						}
 					}
 
 				});
 			}
 		});
 
 		btnHost.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				theRemoteEventService.removeListeners(DomainFactory.getDomain("Lobby"));
 				if (DEBUG)
 					System.out.println("Client: Fired HostRoomWindowLocalEvent");
 				eventBus.fireEvent(new HostRoomWindowLocalEvent());
 			}
 		});
 
 		btnSpellCustomization.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				theRemoteEventService.removeListeners(DomainFactory.getDomain("Lobby"));
 				if (DEBUG)
 					System.out.println("Client: Lobby listeners deactivated");
 				CustomizeSpellsLocalEvent customizeSpellsEvent = new CustomizeSpellsLocalEvent();
 				eventBus.fireEvent(customizeSpellsEvent);
 			}
 		});
 	}
 
 	/**
 	 * Activates remote event listeners and handles displaying any new
 	 * information that is passed in, via a remote event
 	 * 
 	 */
 	public void setRemoteEventListenersAndHandleEvents() {
 		// There is more logic in here than just setting listeners
 		theRemoteEventService.addListener(DomainFactory.getDomain("Lobby"), new RemoteEventListener() {
 
 			@Override
 			public void apply(Event anEvent) {
 				if (anEvent instanceof UpdateLobbyMessagesEvent) {
 					UpdateLobbyMessagesEvent messageEvent = (UpdateLobbyMessagesEvent) anEvent;
 					chatWindowMessages.add(messageEvent.message);
 					System.out.println("Client: Recieved Message " + messageEvent.message);
 					String totalChat = "";
 					for (String s : chatWindowMessages) {
 						totalChat += s + "\n";
 					}
 					chatTextArea.setText(totalChat);
 					chatTextArea.setCursorPos(chatTextArea.getText().length());
 				} else if (anEvent instanceof UpdateLobbyUsersEvent) {
 					UpdateLobbyUsersEvent usersEvent = (UpdateLobbyUsersEvent) anEvent;
 					String truncatedUserName;
 					if (usersEvent.getUsername().length() > 25) {
 						truncatedUserName = usersEvent.getUsername().substring(0, 25);
 					} else {
 						truncatedUserName = usersEvent.getUsername();
 					}
 					usersListBox.addItem(truncatedUserName);
 				} else if (anEvent instanceof LobbyUserLeftEvent) {
 					LobbyUserLeftEvent userLeftEvent = new LobbyUserLeftEvent();
 					onUserLeftEvent(userLeftEvent);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Populates the lobby with the information that has been acquired from the
 	 * server
 	 * 
 	 */
 	public void populateLobbyViewWithInformation() {
 		System.out.println("Updating the initial lobby information");
 		usersListBox.clear();
 		for (String user : usersInLobby) { // populate user list for the first
 			// time
 			if (user.length() > 25) {
 				usersListBox.addItem(user.substring(0, 25));
 			} else {
 				usersListBox.addItem(user);
 			}
 			System.out.println("Added user to display user list: " + user);
 		}
 		totalChat = "";
 		for (String s : chatWindowMessages) {
 			totalChat += s + "\n";
 		}
 		chatTextArea.setText(totalChat);
 		chatTextArea.setCursorPos(chatTextArea.getText().length());
 		for (String r : availableRooms) {
 			// roomBox.addItem(r);
 		}
 		if (DEBUG)
 			System.out.println("Lobby listeners activated");
 	}
 
 	public void onUserLeftEvent(LobbyUserLeftEvent userLeftEvent) {
 		for (int x = 0; x < usersInLobby.size(); x++) {
 			if (usersInLobby.get(x).equalsIgnoreCase(userLeftEvent.getUsernameOfUserWhoLeft())) {
 				usersInLobby.remove(x);
 				usersListBox.clear();
 				for (String user : usersInLobby) {
 					if (user.length() > 25) {
 						usersListBox.addItem(user.substring(0, 25));
 					} else {
 						usersListBox.addItem(user);
 					}
 				}
 			}
 		}
 	}
 
 	public Widget asWidget() {
 		return this;
 	}
 
 	public void deactivateExtraneousListeners() {
 		Set<Domain> domains = theRemoteEventService.getActiveDomains();
 		for (Domain domain : domains) {
 			theRemoteEventService.removeListeners(domain);
 		}
 	}
 }
