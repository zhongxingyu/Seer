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
 
 import database.DatabaseUnit;
 
 import logic.User;
 import requests.xml.XMLSerializable;
 import utils.PasswordHasher;
 
 public class ServerConnection {
 
 	private DatabaseUnit dbUnit;
 	private Map<String, User> users;
 	private Map<String, String> passwords;
 	private List<ServerConnectionHandler> handlers;
 	private long nextDebtId, nextUserId, nextFriendRequestId;
 	private Timer timer;
 	private ServerSocket serverSocket = null;
 	private boolean shouldSave = true;
 
 	public ServerConnection(boolean readFromDatabase) {
 		this.handlers = new ArrayList<ServerConnectionHandler>();
 		users = new HashMap<String, User>();
 		passwords = new HashMap<String, String>();
 		nextDebtId = 1; nextUserId = 1; nextFriendRequestId = 1;
 		if(readFromDatabase) {
 			dbUnit = new DatabaseUnit();
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
 				// Write to database and disconnect inactive users
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
 						// Lock the list
						synchronized (this) {
 							List<ServerConnectionHandler> toBeRemoved = new ArrayList<ServerConnectionHandler>();
 							for (ServerConnectionHandler h : handlers) {
 								if(h.getTimeOfLastCommand() + Constants.MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT < System.currentTimeMillis()) {
 									System.out.println("Attempting to close connection");
 									h.close();
 									toBeRemoved.add(h);
 								}
 							}
 							handlers.removeAll(toBeRemoved);
 						}
 					}
 				}, Constants.TIME_BETWEEN_WRITES_TO_DATABASE, Constants.TIME_BETWEEN_WRITES_TO_DATABASE);
 				Runtime.getRuntime().addShutdownHook(new Thread() {
 					@Override
 					public void run() {
 						try {
 							System.out.println("Writing updates to database..");
 							saveAll();
 							System.out.println("Wrote updates to database.");
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
 							disconnectUsers();
 						} else if(command.equals("ls connections")) {
 							listConnections();
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
 		for (ServerConnectionHandler h : handlers) {
 			h.close();
 		}
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
 	public void notifyUser(String username, XMLSerializable objectToSend) {
 		System.out.println("Notifying " + username);
 		ServerConnectionHandler handler = getHandler(username);
 		if(handler != null) {
 			handler.sendUpdate(objectToSend);
 		}
 	}
 
 	/**
 	 * Returns the specified user's ServerConnectionHandler
 	 * @param username	The user's user name
 	 * @return			The user's ServerConnectionHandler
 	 */
 	public synchronized ServerConnectionHandler getHandler(String username) {
 		for (ServerConnectionHandler h : handlers) {
 			if(h.getUser().getUsername().equals(username)) return h;
 		}
 		return null;
 	}
 
 	/**
 	 * Listens at the specified port for incoming connections.
 	 * Incoming connections are given to a ServerConnectionHandler that is started in a separate Thread.
 	 * This method will run "forever"
 	 * @param port						The port to listen to
 	 */
 	public void accept(int port) {
 		try {
 			serverSocket = new ServerSocket(port);
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
 		ServerConnection server = new ServerConnection(true);
 
 		// START TEST DATA
 		//		User arne = new User(1, "arnegopro");
 		//		User stian = new User(2, "stian");
 		//		User test = new User(0, "test");
 		//		// All friends must also have a corresponding friend request to work with the database!
 		//		stian.addFriendRequest(new FriendRequest(stian.getUsername(), test, FriendRequestStatus.PENDING, 0));
 		//		stian.addFriendRequest(new FriendRequest(stian.getUsername(), arne, FriendRequestStatus.ACCEPTED, 1));
 		//		stian.addFriend(arne);
 		//		arne.addFriend(stian);
 		//		server.addUser(arne, "qazqaz");
 		//		server.addUser(stian, "asd");
 		//		server.addUser(test, "test");
 		//		Debt d1 = new Debt(0, 15, "kr", stian, arne, "Tralalala", stian, DebtStatus.CONFIRMED);
 		//		stian.addConfirmedDebt(d1);
 		//		arne.addConfirmedDebt(d1);
 		//		Debt d2 = new Debt(1, 7, "kr", arne, stian, "Tralalla2", arne, DebtStatus.REQUESTED);
 		//		stian.addPendingDebt(d2);
 		//		arne.addPendingDebt(d2);
 		//		server.nextDebtId = 2;
 		//		server.nextFriendRequestId =  2;
 		//		server.nextUserId = 3;
 		// END TEST DATA		
 		// Print loaded users on startup
 		System.out.println("Loaded users:");
 		for (String s : server.users.keySet()) {
 			System.out.println(s);
 		}
 
 		// Accept connections on port 13337
 		server.accept(Constants.STANDARD_SERVER_PORT);
 	}
 }
