 package network;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import config.Config;
 import config.ConfigManager;
 
 import database.DatabaseUnit;
 import database.SessionTokenManager;
 
 import logic.Debt;
 import logic.DebtStatus;
 import logic.User;
 import mail.MailSender;
 import requests.FriendRequest;
 import requests.FriendRequest.FriendRequestStatus;
 import requests.UpdateRequest;
 import requests.xml.XMLSerializable;
 import utils.CaseInsensitiveHashMap;
 import utils.PasswordHasher;
 
 public class ServerConnection {
 
 	public static final String CONFIG_FILE = "DebtList_server.conf";
 
 	private DatabaseUnit dbUnit;
 	private Map<String, User> users;
 	private Map<String, String> passwords;
 	private List<ServerConnectionHandler> handlers;
 	private long nextDebtId, nextUserId, nextFriendRequestId;
 	private Timer timer;
 	private ServerSocket serverSocket = null;
 	private boolean shouldSave = true;
 	private SessionTokenManager tokenManager;
 
 	public ServerConnection(boolean readFromDatabase) {
 		System.out.println("Starting server " + Constants.SERVER_VERSION);
 		this.handlers = new ArrayList<ServerConnectionHandler>();
 		users = new CaseInsensitiveHashMap<User>();
 		passwords = new CaseInsensitiveHashMap<String>();
 		tokenManager = new SessionTokenManager();
 		nextDebtId = 1; nextUserId = 1; nextFriendRequestId = 1;
 		
 		Config config = ConfigManager.loadConfig(CONFIG_FILE);
 		MailSender.init(config);
 		if(readFromDatabase) {
 			dbUnit = new DatabaseUnit(config);
 			try {
 				dbUnit.connect();
 				for(Map.Entry<User, String> entry :	dbUnit.loadUsers().entrySet()) {
 					users.put(entry.getKey().getUsername(), entry.getKey());
 					passwords.put(entry.getKey().getUsername(), entry.getValue());
 				}
 				dbUnit.loadFriends(users);
 				dbUnit.loadDebts(users);
 				nextDebtId = dbUnit.getNextId(DatabaseUnit.TABLE_DEBT, DatabaseUnit.FIELD_DEBT_ID);
 				nextUserId = dbUnit.getNextId(DatabaseUnit.TABLE_USER, DatabaseUnit.FIELD_USER_ID);
 				nextFriendRequestId = dbUnit.getNextId(DatabaseUnit.TABLE_FRIEND_REQUEST, DatabaseUnit.FIELD_FRIEND_REQUEST_ID);
 				// Write to database
 				(timer = new Timer()).schedule(new TimerTask() {
 
 					@Override
 					public void run() {
 						if(shouldSave) {
 							try {
 								saveAll();
 							} catch (SQLException e) {
 								// TODO Auto-generated catch block
 								System.out.println("Failed writing to database!");
 								e.printStackTrace();
 								writeToLog("Exception while writing to database:\n" + e.toString());
 							}
 						} else System.out.println("Not writing to database. Saving is disabled.");
 						// Also check if we should disconnect any inactive users
 						// FIXME: No longer needed since handlers are no longer persistent... What should be done is removing session tokens from the token manager!
 						// Lock the list
 //						synchronized (handlers) {
 //							List<ServerConnectionHandler> toBeRemoved = new ArrayList<ServerConnectionHandler>();
 //							for (ServerConnectionHandler h : handlers) {
 //								if(h.getTimeOfLastCommand() + Constants.MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT < System.currentTimeMillis()) {
 //									System.out.println("Attempting to close connection");
 //									h.close();
 //									toBeRemoved.add(h);
 //								}
 //							}
 //							handlers.removeAll(toBeRemoved);
 //						}
 					}
 				}, Constants.TIME_BETWEEN_WRITES_TO_DATABASE, Constants.TIME_BETWEEN_WRITES_TO_DATABASE);
 				Runtime.getRuntime().addShutdownHook(new Thread() {
 					@Override
 					public void run() {
 						try {
 							// Only write updates if not disabled
 							if(shouldSave) {
 								System.out.println("Writing updates to database..");
 								saveAll();
 								System.out.println("Wrote updates to database.");
 							}
 						} catch (SQLException e) {
 							System.out.println("Failed writing to database.");
 							e.printStackTrace();
 							writeToLog("Shutdown hook failed: " + e.toString());
 						}
 					}
 				});
 			} catch (Exception e) {
 				System.out.println("FAILED TO LOAD!");
 				e.printStackTrace();
 				writeToLog("Failed to load from database: " + e.toString());
 			}
 		}
 
 		// Start command listener
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 				String command = "";
 				try {
 					while(!(command = reader.readLine()).equals("exit")) {
 						if(command.equals("save")) saveAll();
 						// Close all connections
 						else if(command.equals("disconnect")) {
 //							disconnectUsers();
 							tokenManager.removeOldTokens(System.currentTimeMillis());
 						} else if(command.equals("ls connections")) {
 //							listConnections();
 							System.out.println("No longer supported since connections are not persistent. Did you mean ls tokens?");
 						} else if(command.equals("ls tokens")) {
 							System.out.println("Current active tokens and users:");
 							for (String  token : tokenManager.getTokens()) {
 								System.out.println(token + ": " + tokenManager.getUsername(token));
 							}
 						} else if(command.equals("disable saving")) {
 							shouldSave = false;
 						} else if(command.equals("enable saving")) {
 							shouldSave = true;
 						} else {
 							System.out.println("Unknown command.");
 						}
 					}
 					System.out.println("Disconnecting users..");
 					disconnectUsers();
 					System.out.println("Stopping timer..");
 					timer.cancel();
 					System.out.println("Closing server socket..");
 					serverSocket.close();
 					System.out.println("Writing updates to database..");
 					saveAll();
 					System.out.println("Bye!");
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					writeToLog(e.toString());
 				}
 			}
 		}).start();
 
 		// Timeout token sessions
 		new Timer().schedule(new TimerTask() {
 			
 			@Override
 			public void run() {
 				System.out.println("Timing out sessions..");
 				// Remove a token if it hasn't requested an update in the set interval
 				for(String token : tokenManager.removeOldTokens(System.currentTimeMillis() - Constants.MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT)) {
 					System.out.println("Removed token: " + token);
 				}	
 				System.out.println("Done.");
 			}
 		}, Constants.MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT, Constants.MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT);
 	}
 	
 	/**
 	 * Lists the current online users in System.out
 	 */
 	public synchronized void listConnections() {
 		for (ServerConnectionHandler h : handlers) {
 			if(h.getUser() == null) {
 				System.out.println("Anonymous user with IP address: " + h.getUserIp());
 			} else {
 				System.out.println(h.getUser().getUsername() + ": " + h.getUserIp());
 			}
 		}
 		if(handlers.isEmpty()) System.out.println("None");
 	}
 	
 	public synchronized void disconnectUsers() {
 		System.out.println("Attempting to close all connections");
 //		for (ServerConnectionHandler h : handlers) {
 //			h.close();
 //		}
 		// TODO: Handlers should be removed aswell, right??
 		while(!handlers.isEmpty()) {
 			handlers.remove(0).close();
 		}
 	}
 	
 	public synchronized SessionTokenManager getTokenManager() {
 		return tokenManager;
 	}
 	
 	public synchronized List<User> getUsers() {
 		List<User> users = new ArrayList<User>();
 		for (String username : this.users.keySet()) {
 			users.add(this.users.get(username));
 		}
 		return users;
 	}
 	
 	public synchronized void writeToLog(String s) {
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(new BufferedWriter(new FileWriter(Constants.SERVER_LOG_FILE, true)));
 			Calendar cal = Calendar.getInstance();
 	    	cal.getTime();
 	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	    	out.println(sdf.format(cal.getTime()) + ": " + s);
 	    	System.out.println("Wrote to error log.");
 		} catch (IOException e) {
 			System.out.println("Failed writing to log file.");
 			e.printStackTrace();
 		} finally {
 			try {
 				out.close();
 			} catch (Exception e) {}
 		}
 	}
 	
 	public synchronized void saveAll() throws SQLException {
 		dbUnit.save(users.values(), passwords);
 	}
 
 	public synchronized void saveUser(User u) throws SQLException {
 		List<User> user = new ArrayList<User>();
 		user.add(u);
 		Map<String, String> pw = new HashMap<String, String>();
 		pw.put(u.getUsername(), passwords.get(u.getUsername()));
 		dbUnit.save(user, passwords);
 	}
 
 	public synchronized void addPassword(String username, String password) {
 		passwords.put(username, password);
 	}
 
 	public synchronized void addConnectionHandler(ServerConnectionHandler handler) {
 		this.handlers.add(handler);
 	}
 
 	public synchronized void removeConnectionHandler(ServerConnectionHandler handler) {
 		this.handlers.remove(handler);
 		if(handler.getUser() != null) {
 			System.out.println("Removed connection handler for " + handler.getUser().getUsername());
 		} else {
 			System.out.println("Removed connection handler for a user that was trying to log in.");
 		}
 	}
 
 	/**
 	 * @return	The next available debt id
 	 */
 	public synchronized long getNextDebtId() {
 		return nextDebtId++;
 	}
 
 	/**
 	 * @return	The next available user id
 	 */
 	public synchronized long getNextUserId() {
 		return nextUserId++;
 	}
 
 	/**
 	 * @return	The next available friend request id
 	 */
 	public synchronized long getNextFriendRequestId() {
 		return nextFriendRequestId++;
 	}
 
 	/**
 	 * Notifies the specified user by sending the given object to the user's UpdateListener
 	 * @param username		The user to notify
 	 * @param objectToSend	The object to send
 	 */
 	public void notifyUser(String username, XMLSerializable objectToSend, String fromUserToken) {
 		System.out.println("Notifying " + username);
 //		for(ServerConnectionHandler handler : getHandlers(username)) {
 //			System.out.println("Update added to session.");
 //			handler.sendUpdate(objectToSend);
 //		}
 		for(UpdateRequest ur : tokenManager.getUpdates(username)) {
 			ur.add(objectToSend);
 		}
 		// Also send updates to this user's other handlers (e.g. for other devices)
 		System.out.println("Also notifying this user's other handlers (if it has hany)..");
 		for(UpdateRequest ur : tokenManager.getUpdates(tokenManager.getUsername(fromUserToken))) {
 			if(ur != tokenManager.getUpdate(fromUserToken)) {
 				ur.add(objectToSend);
 			}
 		}
 //		for(ServerConnectionHandler handler : getHandlers(fromHandler.getUser().getUsername())) {
 //			if(handler != fromHandler) { 
 //				System.out.println("It has!");
 //				handler.sendUpdate(objectToSend);
 //			}
 //		}
 		System.out.println("Done notifiying " + username);
 	}
 
 	/**
 	 * Returns the specified user's ServerConnectionHandlers
 	 * @param username	The user's user name
 	 * @return			The user's ServerConnectionHandlers
 	 */
 	public synchronized List<ServerConnectionHandler> getHandlers(String username) {
 		List<ServerConnectionHandler> handlers = new ArrayList<ServerConnectionHandler>();
 		System.out.println("Number of handlers: " + this.handlers.size());
 		for (ServerConnectionHandler h : this.handlers) {
 			if(h.getUser() != null && h.getUser().getUsername().equals(username)) {
 				System.out.println("Found matching handler!");
 				handlers.add(h);
 			} else {
 				System.out.println("Found handler that did not match!");
 			}
 		}
 		return handlers;
 	}
 
 	/**
 	 * Listens at the specified port for incoming connections.
 	 * Incoming connections are given to a ServerConnectionHandler that is started in a separate Thread.
 	 * This method will run "forever"
 	 * @param port						The port to listen to
 	 */
 	public void accept(int port) {
 		try {
 			serverSocket = createServerSocket(port);
 			while(true) {
 				System.out.println("Listening for incomming connections..");
 				new ServerConnectionHandler(serverSocket.accept(), this).start();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 //			e.printStackTrace();
 			writeToLog("Failed to accept incomming connection on port "+ port + ": " + e.toString());
 			System.out.println("Server socket closed.");
 		} finally {
 			try {
 				serverSocket.close();
 			} catch (Exception e) {}
 		}
 	}
 
 	public ServerSocket createServerSocket(int port) throws IOException {
 		return new ServerSocket(port);
 	}
 	
 	/**
 	 * Returns the user with the given username, or null if no user is found.
 	 * @param username	The username
 	 * @return			The user or null
 	 */
 	synchronized public User getUser(String username) {
 		return users.get(username);
 	}
 
 	synchronized public void addUser(User user, String password) {
 		users.put(user.getUsername(), user);
 		passwords.put(user.getUsername(), PasswordHasher.hashPassword(password));
 	}
 
 	synchronized public boolean checkPassword(User user, String password) {
 		return passwords.containsKey(user.getUsername()) 
 				&& passwords.get(user.getUsername()).equals(password);
 	}
 
 	public static void main(String[] args) {
//		ServerConnection server = new ServerConnection(true);
		ServerConnection server = new ServerConnection(false);
 		// Use the secure version
 //		ServerConnection server = new SecureServerConnection(true);
 
		// Add users
		User test = new User(0, "test");
		test.setIsActivated(true);
		server.addUser(test, "test");
		
		User test2 = new User(1, "test2");
		test2.setIsActivated(true);
		server.addUser(test2, "test2");
		
		test.addFriend(test2);
		test2.addFriend(test);
		
		test.addFriendRequest(new FriendRequest(test.getUsername(), test2, FriendRequestStatus.ACCEPTED, 0));
		test2.addFriendRequest(new FriendRequest(test.getUsername(), test2, FriendRequestStatus.ACCEPTED, 0));
		
 		// Print loaded users on startup
 		System.out.println("Loaded users:");
 		for (String s : server.users.keySet()) {
 			System.out.println(s);
 			User u = server.users.get(s);
 			System.out.println("Friends:");
 			for(int i = 0; i < u.getNumberOfFriends(); i++) {
 				System.out.println("\t" + u.getFriend(i).getUsername());
 			}
 		}
 
 		// Accept connections on port 13337
 		server.accept(Constants.STANDARD_SERVER_PORT);
 	}
 }
