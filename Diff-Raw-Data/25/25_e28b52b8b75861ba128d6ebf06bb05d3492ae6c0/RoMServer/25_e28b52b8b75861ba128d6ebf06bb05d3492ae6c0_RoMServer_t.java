 package com.rom.server;
 
 import com.rom.common.commands.*;
 import com.rom.common.dataObjects.User;
 import com.rom.common.logging.Logger;
 import com.rom.server.callbacks.IClientCommandReceivedCallback;
 import com.rom.server.callbacks.IClientConnectedCallback;
 import com.rom.server.callbacks.IClientDisconnectedCallback;
 import com.rom.server.configuration.ConfigurationFactory;
 import com.rom.server.configuration.IConfig;
 import com.rom.server.data.DataLayerFactory;
 import com.rom.server.data.IDataLayer;
 import com.rom.server.dataObjects.ClientList;
 import com.rom.server.dataObjects.GroupList;
 import com.rom.server.dataObjects.RoMClient;
 import com.rom.server.eventlisteners.IUserLoggedInListener;
 import com.rom.server.eventobjects.UserLoggedInEvent;
 import com.rom.server.helper.UserListHelper;
 import com.rom.server.threads.ClientAcceptThread;
 import com.rom.server.threads.ClientCommandQueueThread;
 import com.rom.server.threads.ReceiveClientCommandThread;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.util.*;
 
 /**
  * RoMServer - The Server Class which will handle all Clients
  * 
  * @author Michael Seiwert
  */
 public class RoMServer implements IClientConnectedCallback, IClientCommandReceivedCallback, IClientDisconnectedCallback {
 
 	// Private Fields
 	private InetAddress serverIpAddress;
 	private int serverPort;
 	private int backLog;
 	private ServerSocket serverSocket;
 	private IDataLayer dataLayer;
 	private ClientList clients;
 	private GroupList groupList;
 	private boolean isAlive;
 
 	// Event Listener
 	private Collection<IUserLoggedInListener> userConnectedListener;
 
 	public boolean isAlive() {
 		return this.isAlive;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param serverPort
 	 *            Port which the Server will listen
 	 */
 	private RoMServer(InetAddress serverIpAddress, int serverPort) {
 		this.serverIpAddress = serverIpAddress;
 		this.serverPort = serverPort;
 		this.clients = new ClientList();
 		this.groupList = new GroupList();
 
 		// Event listener init
 		this.userConnectedListener = new HashSet<>();
 	}
 
 	/**
 	 * Server initialization (Setup Server socket etc.)
 	 * 
 	 * @throws IOException
 	 */
 	private void initServer() throws IOException {
 		Logger.logMessage("Binding Server to " + this.serverIpAddress + ":" + this.serverPort);
 
 		this.isAlive = true;
 		this.serverSocket = new ServerSocket(this.serverPort, this.backLog, this.serverIpAddress);
 		this.dataLayer = DataLayerFactory.getDataLayer();
 
 		// Generate the grouplist
 		Collection<String> dbGroupList = this.dataLayer.getGroups();
 
 		for (String group : dbGroupList) {
 			this.groupList.addGroup(group);
 		}
 
 		Logger.logMessage("Server successfuly bound!");
 	}
 
 	/**
 	 * Starts a thread which is dedicated to accept new clients
 	 * 
 	 * @throws Exception
 	 */
 	private void beginAcceptClients() throws Exception {
 		ClientAcceptThread acceptClientsThread = new ClientAcceptThread(this.serverSocket, this);
 		acceptClientsThread.start();
 
 		Logger.logMessage("ClientAcceptThread started");
 	}
 
 	/**
 	 * Sends a Command to all clients
 	 * 
 	 * @param serverCommand
 	 */
 	public void sendCommandToClient(ServerCommand serverCommand) {
 		Collection<RoMClient> clientsToSend = this.clients.getClients();
 
 		this.sendCommandToClient(clientsToSend, serverCommand);
 	}
 
 	public void sendCommandToClient(RoMClient client, ServerCommand serverCommand) {
 		Collection<RoMClient> clientsToSend = Arrays.asList(new RoMClient[] { client });
 
 		this.sendCommandToClient(clientsToSend, serverCommand);
 	}
 
 	/**
 	 * Sends a Command to clients
 	 * 
 	 * @param clientsToSend
 	 *            Collection of RoMClients which the command will be sent to
 	 * @param serverCommand
 	 *            The command
 	 */
 	public void sendCommandToClient(Collection<RoMClient> clientsToSend, ServerCommand serverCommand) {
 		Logger.logMessage("Sending Command \"" + serverCommand + "\"");
 
 		// Add Command to clients MessageQueue
 		for (RoMClient client : clientsToSend) {
 			client.addCommandToQueue(serverCommand);
 		}
 	}
 
 	/**
 	 * Adds the new RoMClient pair to the internal List
 	 */
 	@Override
 	public void clientConnectedCallback(RoMClient newClient) {
 		// Add new client and start listening to this one
 		this.clients.addClient(newClient);
 
 		// Create the ReceiveClientCommandThread for this client
 		ReceiveClientCommandThread receiveCommandThread = new ReceiveClientCommandThread(newClient, this, this);
 		receiveCommandThread.start();
 
 		// Create the CommandQueue Thread for this client
 		ClientCommandQueueThread commandQueueThread = new ClientCommandQueueThread(newClient);
 		commandQueueThread.start();
 	}
 
 	/**
 	 * Removes a Client from list
 	 * 
 	 * @param client
 	 */
 	@Override
 	public void clientDisconnectedCallback(RoMClient client) {
 		this.disconnectClient(client);
 	}
 
 	/**
 	 * Disconnects a client
 	 * 
 	 * @param client
 	 */
 	private void disconnectClient(RoMClient client) {
 		// Remove Client from user and grouplist
 		if (this.clients.isConnected(client)) {
 			String oldGroup = this.groupList.getGroupOfClient(client);
 
 			this.groupList.removeClient(client);
 			this.clients.removeClientBySessionID(client.getSessionID());
 			Collection<RoMClient> clientsToNotify = this.groupList.getClientsInGroup(oldGroup);
 
 			Collection<User> userList = UserListHelper.clientToUserList(clientsToNotify);
 			User disconnectedUser = client.toUser();
 
 			// Notify all other clients about the disconnect
 			UserLogoffServerCommand userLogoffCommand = new UserLogoffServerCommand(disconnectedUser, userList, null);
 			this.sendCommandToClient(clientsToNotify, userLogoffCommand);
 		}
 
 		// Close Streams and socket
 		try {
 			client.getOutputStream().close();
 			client.getInputStream().close();
 			client.getSocket().close();
 		} catch (IOException ex) {
 			Logger.logException("Exception in disconnectClient", ex);
 		}
 	}
 
 	/**
 	 * Method which will be invoked after a client command was received from the
 	 * other thread
 	 * 
 	 * @param client
 	 * @param clientCommand
 	 */
 	@Override
 	public void clientCommandReceivedCallback(RoMClient client, ClientCommand clientCommand) {
 		Logger.logMessage(client.getSessionID() + " newCommand: " + clientCommand);
 
 		this.processClientCommand(client, clientCommand);
 	}
 
 	/**
 	 * This method will analyze the command and invoke the dedicated method
 	 * 
 	 * @param client
 	 *            Client which sent the command
 	 * @param clientCommand
 	 *            The received ClientCommand
 	 */
 	private void processClientCommand(RoMClient client, ClientCommand clientCommand) {
 		ServerCommand serverResponseCommand = null;
 		Collection<RoMClient> receiverClients = new ArrayList<>();
 
 		// Check which command was sent
 		// ---------------------------
 		// UserLoginCommand
 		if (clientCommand instanceof UserLoginClientCommand) {
 
 			UserLoginClientCommand loginCommand = (UserLoginClientCommand) clientCommand;
 			serverResponseCommand = this.processUserLoginCommand(client, loginCommand);
 
 			// LoginResponse to client
 			receiverClients.add(client);
 		}
 
 		// PrivateMessageCommand
 		else if (clientCommand instanceof PrivateMessageClientCommand) {
 			PrivateMessageClientCommand privateMessageCommand = (PrivateMessageClientCommand) clientCommand;
 			serverResponseCommand = this.processPrivateMesageCommand(client, privateMessageCommand);
 
 			// Sender
 			receiverClients.add(client);
 			// Receiver
 			receiverClients.add(this.clients.getClientByUsername(privateMessageCommand.getToUser()));
 		}
 
 		// MessageClientCommand
 		else if (clientCommand instanceof MessageClientCommand) {
 			MessageClientCommand messageCommand = (MessageClientCommand) clientCommand;
 			serverResponseCommand = this.processMessageCommand(client, messageCommand);
 
 			// MessageClient to everybody
 			receiverClients = this.groupList.getClientsInSameGroup(client);
 		}
 
 		// CreateUserClientCommand
 		else if (clientCommand instanceof CreateUserClientCommand) {
 			CreateUserClientCommand newUserCommand = (CreateUserClientCommand) clientCommand;
 			serverResponseCommand = this.processCreateUserCommand(client, newUserCommand);
 
 			receiverClients.add(client);
 		}
 
 		// GetGroupsClientCommand
 		else if (clientCommand instanceof GetGroupsClientCommand) {
 			GetGroupsClientCommand getGroupsCommand = (GetGroupsClientCommand) clientCommand;
 			serverResponseCommand = this.processGetGroupsCommand(client, getGroupsCommand);
 
 			receiverClients.add(client);
 		}
 
 		// JoinGroupClientCommand
 		else if (clientCommand instanceof JoinGroupClientCommand) {
 			JoinGroupClientCommand joinGroupCommand = (JoinGroupClientCommand) clientCommand;
 			serverResponseCommand = this.processJoinGroupCommand(client, joinGroupCommand);
 
 			receiverClients.add(client);
 		}
 		// NewGroupClientCommnad
 		else if (clientCommand instanceof CreateGroupClientCommand) {
 			CreateGroupClientCommand createGroupCommand = (CreateGroupClientCommand) clientCommand;
 			serverResponseCommand = this.processCreateGroupCommand(client, createGroupCommand);
 
			if (serverResponseCommand != null) {
				receiverClients = this.clients.getClients();
			}
 		}
 
 		// DeleteGroupClientCommand
 		else if (clientCommand instanceof DeleteGroupClientCommand) {
 			DeleteGroupClientCommand delteGroupCommand = (DeleteGroupClientCommand) clientCommand;
 			serverResponseCommand = this.processDeleteGroupCommand(client, delteGroupCommand);
 
 			receiverClients.add(client);
 		}
 
 		// CreateUserClientCommand
 		else if (clientCommand instanceof CreateUserClientCommand) {
 			CreateUserClientCommand createUserCommand = (CreateUserClientCommand) clientCommand;
 			serverResponseCommand = this.processCreateUserCommand(client, createUserCommand);
 
 			receiverClients.add(client);
 		}
 
 		// DeleteUserClientCommand
 		else if (clientCommand instanceof DeleteUserClientCommand) {
 			DeleteUserClientCommand deleteUserCommand = (DeleteUserClientCommand) clientCommand;
 			serverResponseCommand = this.processDeleteUserCommand(client, deleteUserCommand);
 
 			receiverClients.add(client);
 		}
 
 		// GetUserListClientCommand
 		else if (clientCommand instanceof GetUserListClientCommand) {
 			GetUserListClientCommand userListCommand = (GetUserListClientCommand) clientCommand;
 			serverResponseCommand = this.processGetUserListCommand(client, userListCommand);
 
 			receiverClients.add(client);
 		}
 
 		// UserLogoffCommand
 		else if (clientCommand instanceof UserLogoffClientCommand) {
 			// We won't send a response to a disconnting client
 			UserLogoffClientCommand logoffCommand = (UserLogoffClientCommand) clientCommand;
 			serverResponseCommand = this.processUserLogoffCommand(client, logoffCommand);
 
 			receiverClients = null;
 		}
 		// ---------------------------------
 
 		// Send server response
 		if (serverResponseCommand != null) {
 			if (receiverClients == null) {
 				Logger.logMessage("Tried to send a ServerCommand but receiverClients list is emtpy!");
 			} else {
 				this.sendCommandToClient(receiverClients, serverResponseCommand);
 			}
 		}
 	}
 
 	/**
 	 * Handles the UserLoginCommand
 	 * 
 	 * @param client
 	 *            Client which logged in
 	 * @param userLoginCommand
 	 *            the Command
 	 * @return ServerCommand which will represent the response
 	 */
 	private ServerCommand processUserLoginCommand(RoMClient client, UserLoginClientCommand userLoginCommand) {
 		ServerCommand serverResponseCommand;
 		boolean loginValid = false;
 		String errorMessage = null;
 
 		// Frist check if the user is already logged in
 		RoMClient loggedInClient = this.clients.getClientByUsername(userLoginCommand.getUsername());
 		if (loggedInClient != null) {
 			loginValid = false;
 			errorMessage = "Account already logged in";
 		} else {
 			loginValid = this.loginUser(client.getSessionID(), userLoginCommand.getUsername(), userLoginCommand.getPassword());
 		}
 
 		serverResponseCommand = new UserLoginServerCommand(client.getSessionID(), loginValid, client.getIsAdmin(), errorMessage, userLoginCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Handles the UserLogoffCommand
 	 * 
 	 * @param client
 	 * @param userLogoffCommand
 	 * @return
 	 */
 	private ServerCommand processUserLogoffCommand(RoMClient client, UserLogoffClientCommand userLogoffCommand) {
 		this.disconnectClient(client);
 
 		return null;
 	}
 
 	/**
 	 * Process the MessageCommand
 	 * 
 	 * @param client
 	 * @param messageCommand
 	 * @return
 	 */
 	private ServerCommand processMessageCommand(RoMClient client, MessageClientCommand messageCommand) {
 		ServerCommand serverResponseCommand = new MessageServerCommand(messageCommand.getMessage(), client.getUsername(), messageCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Processes the PrivateMessageCommand
 	 * 
 	 * @param client
 	 * @param privateMessageCommand
 	 * @return
 	 */
 	private ServerCommand processPrivateMesageCommand(RoMClient client, PrivateMessageClientCommand privateMessageCommand) {
 		ServerCommand serverResponseCommand = new PrivateMessageServerCommand(privateMessageCommand.getMessage(), client.getUsername(), privateMessageCommand.getToUser(), privateMessageCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Sends a message to a user
 	 * 
 	 * @param username
 	 * @param message
 	 */
 	public void serverSendMessageToUser(String username, String message) {
 		ServerCommand serverCommand = new PrivateMessageServerCommand(message, "server", username, null);
 
 		RoMClient userClient = this.clients.getClientByUsername(username);
 
 		this.sendCommandToClient(userClient, serverCommand);
 	}
 
 	/**
 	 * Sends a message to all users
 	 * 
 	 * @param message
 	 */
 	public void serverSendMessageToAllUsers(String message) {
 		ServerCommand serverCommand = new MessageServerCommand(message, "server", null);
 
 		this.sendCommandToClient(this.clients.getClients(), serverCommand);
 	}
 
 	/**
 	 * Processes the GetGroupsClientCommand
 	 * 
 	 * @param client
 	 * @param getGroupsCommand
 	 * @return
 	 */
 	private ServerCommand processGetGroupsCommand(RoMClient client, GetGroupsClientCommand getGroupsCommand) {
 		Collection<String> serverGroups = this.groupList.getGroups();
 
 		ServerCommand serverResponseCommand = new GetGroupsServerCommand(serverGroups, getGroupsCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Processes the CreateGroupCommand
 	 * 
 	 * @param client
 	 * @param createGroupCommand
 	 * @return
 	 */
 	private ServerCommand processCreateGroupCommand(RoMClient client, CreateGroupClientCommand createGroupCommand) {
 		boolean success = false;
		ServerCommand serverResponseCommand = null;
 		// Check if admin
 		if (client.getIsAdmin()) {
 			success = this.createGroup(createGroupCommand.getNewGroupName());
 		}
 
		CreateGroupServerCommand createGroupCommandResponse = new CreateGroupServerCommand(success, this.groupList.getGroups(), createGroupCommand);
		this.sendCommandToClient(client, createGroupCommandResponse);
		
		// If created, then notify all clients
		if (success) {
			serverResponseCommand = new GroupCreatedServerCommand(createGroupCommand.getNewGroupName());	
		}
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Processes the DeleteGroupCommand
 	 * 
 	 * @param client
 	 * @param deleteGroupCommand
 	 * @return
 	 */
 	private ServerCommand processDeleteGroupCommand(RoMClient client, DeleteGroupClientCommand deleteGroupCommand) {
 		Collection<RoMClient> clientsInGroup = this.groupList.getClientsInGroup(deleteGroupCommand.getGroupname());
 		boolean success = false;
 
 		// Check if admin
 		if (client.getIsAdmin()) {
 			// Only delete if group is emtpy
 			if (clientsInGroup.isEmpty()) {
 				success = this.dataLayer.deleteGroup(deleteGroupCommand.getGroupname());
 
 				if (success) {
 					this.groupList.removeGroup(deleteGroupCommand.getGroupname());
 
 					// Notify all clients about the delete
 					GroupDeletedServerCommand groupDeletedCommand = new GroupDeletedServerCommand(deleteGroupCommand.getGroupname());
 					this.sendCommandToClient(groupDeletedCommand);
 				}
 			}
 		}
 
 		ServerCommand serverResponseCommand = new DeleteGroupServerCommand(success, this.groupList.getGroups(), deleteGroupCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Process the CreateUser Command
 	 * 
 	 * @param client
 	 * @param createUserCommand
 	 * @return
 	 */
 	private ServerCommand processCreateUserCommand(RoMClient client, CreateUserClientCommand createUserCommand) {
 		boolean success = false;
 
 		// Check if user is admin
 		if (client.getIsAdmin()) {
 			success = this.dataLayer.addUser(createUserCommand.getNewUserName(), createUserCommand.getSHA1EncryptedPassword());
 		}
 
 		ServerCommand serverResponseCommand = new CreateUserServerCommand(success, createUserCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Processes the DeleteUser Command
 	 * 
 	 * @param client
 	 * @param deleteUserCommand
 	 * @return
 	 */
 	private ServerCommand processDeleteUserCommand(RoMClient client, DeleteUserClientCommand deleteUserCommand) {
 		boolean success = false;
 
 		// Check if user is admin
 		if (client.getIsAdmin()) {
 			// Check if user is online
 			RoMClient clientToDelete = this.clients.getClientByUsername(deleteUserCommand.getUserName());
 
 			if (clientToDelete == null) {
 				success = this.dataLayer.deleteUser(deleteUserCommand.getUserName());
 			}
 		}
 
 		ServerCommand serverResponseCommand = new DeleteUserServerCommand(success, deleteUserCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Processes the JoinGroupClientCommand
 	 * 
 	 * @param client
 	 * @param joinGroupCommand
 	 * @return
 	 */
 	private ServerCommand processJoinGroupCommand(RoMClient client, JoinGroupClientCommand joinGroupCommand) {
 		// Check whether the user is already in a group
 		if (this.groupList.isInGroup(client)) {
 			// If so, then remove from list and notivy clients in the old group
 			String oldgroup = this.groupList.getGroupOfClient(client);
 			this.groupList.removeClient(client);
 			Collection<RoMClient> clientsInOldGroup = this.groupList.getClientsInGroup(oldgroup);
 
 			// If other users in the group, notify them
 			if (!clientsInOldGroup.isEmpty()) {
 				LeaveGroupServerCommand serverCommand = new LeaveGroupServerCommand(client.getUsername(), oldgroup, joinGroupCommand);
 				this.sendCommandToClient(clientsInOldGroup, serverCommand);
 			}
 		}
 
 		Collection<RoMClient> oldClients = new ArrayList<>();
 		oldClients.addAll(this.groupList.getClientsInGroup(joinGroupCommand.getGroup()));
 
 		// Put user into new group
 		this.groupList.putClient(client, joinGroupCommand.getGroup());
 
 		// Response to the client which joins the new group
 		Collection<RoMClient> userlist = this.groupList.getClientsInGroup(joinGroupCommand.getGroup());
 		Collection<User> userObjectList = UserListHelper.clientToUserList(userlist);
 		User user = client.toUser();
 
 		// Notify other users
 		UserJoinedGroupServerCommand userJoinedCommand = new UserJoinedGroupServerCommand(user);
 		this.sendCommandToClient(oldClients, userJoinedCommand);
 
 		// Response to joined client
 		ServerCommand serverResponseCommand = new JoinGroupServerCommand(userObjectList, joinGroupCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Handles the GetUserListClientCommand
 	 * 
 	 * @param client
 	 * @param userListCommand
 	 * @return
 	 */
 	private ServerCommand processGetUserListCommand(RoMClient client, GetUserListClientCommand userListCommand) {
 		TreeSet<String> users = new TreeSet<>();
 		users.addAll(this.dataLayer.getUser());
 
 		ServerCommand serverResponseCommand = new GetUserListServerCommand(users, userListCommand);
 
 		return serverResponseCommand;
 	}
 
 	/**
 	 * Logs in a user
 	 * 
 	 * @param sessionID
 	 * @param username
 	 * @param password
 	 * @return
 	 */
 	public boolean loginUser(String sessionID, String username, String password) {
 		boolean loginValid = this.dataLayer.validateUser(username, password);
 
 		// Login valid -> set Username
 		if (loginValid) {
 			boolean isAdmin = this.dataLayer.isAdmin(username);
 			RoMClient loggedInClient = this.clients.getClientBySessionID(sessionID);
 			loggedInClient.setUsername(username);
 			loggedInClient.setIsAdmin(isAdmin);
 
 			// Fire event
 			this.notifyUserLoggedInListener(new UserLoggedInEvent(this, username));
 		}
 
 		return loginValid;
 	}
 
 	/**
 	 * Creates a new user
 	 * 
 	 * @param username
 	 * @param password
 	 * @return
 	 */
 	public boolean createUser(String username, String password) {
 		boolean result = this.dataLayer.addUser(username, password);
 
 		return result;
 	}
 
 	/**
 	 * Creates a new Group
 	 * 
 	 * @param groupName
 	 * @return
 	 */
 	public boolean createGroup(String groupName) {
 		boolean result = this.dataLayer.addGroup(groupName);
 
 		// if succesfuly created on dataLayer, then add into GroupList
 		if (result)
 			this.groupList.addGroup(groupName);
 
 		return result;
 	}
 
 	/**
 	 * Gets a list of all groups on the server
 	 * 
 	 * @return
 	 */
 	public Collection<String> getGroups() {
 		Collection<String> serverGroups = this.groupList.getGroups();
 
 		return serverGroups;
 	}
 
 	/**
 	 * Gets a sorted list of all users
 	 * 
 	 * @return
 	 */
 	public Collection<String> getUsers() {
 		Collection<String> users = new TreeSet<>();
 		users.addAll(this.dataLayer.getUser());
 
 		return users;
 	}
 
 	/**
 	 * Shutdown the Server. All Clients will be disconnected before socket is
 	 * closed
 	 * 
 	 * @throws IOException
 	 */
 	public void shutDownServer() throws IOException {
 		this.shutDownServer(true);
 	}
 
 	/**
 	 * Shutdown the Server. All Clients will be disconnected before socket is
 	 * closed
 	 * 
 	 * @param countdown
 	 *            if the server waits 10 seconds until shutdown
 	 * @throws IOException
 	 */
 	public void shutDownServer(boolean countdown) throws IOException {
 		this.isAlive = false;
 
 		if (countdown) {
 			try {
 				int totalSecondsToWait = 10000;
 				int decrease = 1000;
 
 				while (totalSecondsToWait > 0) {
 					this.serverSendMessageToAllUsers("Server will be shut down in " + totalSecondsToWait / 1000 + " seconds...");
 					Thread.sleep(decrease);
 					totalSecondsToWait -= decrease;
 				}
 			} catch (Exception ex) {
 
 			}
 		}
 
 		// Close streams and connections to all clients
 		for (RoMClient client : this.clients.getClients()) {
 			client.getOutputStream().close();
 			client.getInputStream().close();
 			client.getSocket().close();
 		}
 
 		// Close server socket
 		this.serverSocket.close();
 	}
 
 	// ---------------------
 	// EVENT HANDLING
 	// ---------------------
 	// NOTIFICATION
 	/**
 	 * Notifies listener that a user logged in
 	 * 
 	 * @param event
 	 */
 	private void notifyUserLoggedInListener(UserLoggedInEvent event) {
 		for (IUserLoggedInListener listener : this.userConnectedListener) {
 			listener.userLoggedIn(event);
 		}
 	}
 
 	// ----------------------
 	// EVENT REGISTRATION
 	// ----------------------
 	/**
 	 * Adds a listener to the UserLoggedInEvent
 	 * 
 	 * @param listener
 	 */
 	public void addUserLoggedInListener(IUserLoggedInListener listener) {
 		this.userConnectedListener.add(listener);
 	}
 
 	/**
 	 * Removes a listner from the UserLoggedInEvent
 	 * 
 	 * @param listener
 	 */
 	public void removeUserLoggedInListener(IUserLoggedInListener listener) {
 		this.userConnectedListener.remove(listener);
 	}
 
 	/**
 	 * Factory Method to create and initialize the Server The IPAddress/Port
 	 * will be read from the Serverconfig
 	 * 
 	 * @return the fully set up RoMServer Object
 	 * @throws IOException
 	 *             , Exception
 	 */
 	public static RoMServer createServer() throws IOException, Exception {
 		// Ip and Port out of config
 		IConfig cfg = ConfigurationFactory.getConfig();
 
 		return RoMServer.createServer(cfg.getIp(), cfg.getPort());
 	}
 
 	/**
 	 * Factory Method to create and initialize the Server
 	 * 
 	 * @param serverPort
 	 *            Port which the Server will listen
 	 * @return the fully set up RoMServer Object
 	 * @throws IOException
 	 *             , Exception
 	 */
 	public static RoMServer createServer(int serverPort) throws IOException, Exception {
 		return RoMServer.createServer(InetAddress.getLocalHost(), serverPort);
 	}
 
 	/**
 	 * Factory Method to create and initialize the Server
 	 * 
 	 * @param serverIpAddress
 	 *            IP Address of the Server
 	 * @param serverPort
 	 *            Port which the Server will listen
 	 * @return the fully set up RoMServer Object
 	 * @throws IOException
 	 *             , Exception
 	 */
 	public static RoMServer createServer(InetAddress serverIpAddress, int serverPort) throws IOException, Exception {
 		Logger.logMessage("Starting Server...");
 		RoMServer server = new RoMServer(serverIpAddress, serverPort);
 
 		server.initServer();
 		server.beginAcceptClients();
 
 		return server;
 	}
 }
