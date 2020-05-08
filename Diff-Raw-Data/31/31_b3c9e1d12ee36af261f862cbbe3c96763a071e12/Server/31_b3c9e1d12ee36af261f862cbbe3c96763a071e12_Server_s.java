 import java.net.MalformedURLException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import sun.util.BuddhistCalendar;
 /**
  * A server implementation of an instant messenger.
  * @author Michael, Simon
  *
  */
 
 public class Server extends UnicastRemoteObject implements ServerInterface {
 	private HashMap<String, ClientInterface> _userStore;
 	private String[] names;
 	private boolean secondary;
 	private static Server server;
 	private ServerInterface backupServer;
 	public boolean initRequested;
 	private String name;
 	private static Database db;
 	
 	
 
 //####################################################################################
 //###################### only relevant for the GUI ###################################
 //####################################################################################
 	public void notifyUserListChanged() {
 		try {
 			for(String user : _userStore.keySet()) {
 				System.out.println("user: "+user);
 				_userStore.get(user).updateUserList(getBuddies(user));
 			}
 		} catch (RemoteException e) {
 			System.out.println("Error: " + e.getMessage());
 		}
 	}
 //####################################################################################
 //####################################################################################
 //####################################################################################	
 	
 	
 	
 	/**
 	 * Method to get the reference to the backup-server
 	 * @return returns the reference to the backup-server
 	 */
 	private ServerInterface getBackupServer() {
 		return backupServer;
 	}
 	
 	/**
 	 * resets the backupServer to "null"
 	 */
 	private void resetBackupServer() {
 		backupServer = null;
 	}
 	
 	/**
 	 * connects to the backupServer or set it to "null", if none was found
 	 */
 	private void connectBackupServer() {
 		try {
 			if(secondary)
 				backupServer = (ServerInterface) Naming.lookup("rmi://127.0.0.1:9090/server1");
 			else
 				backupServer = (ServerInterface) Naming.lookup("rmi://127.0.0.2:9090/server2");
 			backupServer.ping();
 		} catch(Exception ex) {
 			resetBackupServer();
 		}
 	}
 	
 	/**
 	 * is called by the other server, when it starts. initRequested indicates that a new Server needs to
 	 * get the _userStore initialized
 	 */
 	@Override
 	public void requestInit() {
 		initRequested = true;
 	}
 	
 	/**
 	 * register each Client in _userStore at the other Server
 	 */
 	private void initBackupServer() {
		// TODO is kaputt!!!!
 		try {
 			connectBackupServer();
 			if(backupServer != null) {
 				
 				HashMap<String, ArrayList<String>> tblMessages;
 				HashMap<String, String> tblUser = db.getUsers();
 				HashMap<String,HashMap<String,String>> tblFriendships = db.getFriendships();
 				for(String user : tblUser.keySet()) {
 					backupServer.newUser(user, tblUser.get(user), ServerInterface.SERVER);
 					tblMessages = db.getMessages(user);
 					for(String sender : tblMessages.keySet()) {
 						for(String message : tblMessages.get(sender)) {
 							backupServer.sendMessage(sender, user, message, ServerInterface.SERVER);
 						}
 					}
 				}
				for(String id : tblFriendships.keySet()) {
					for(String user : tblFriendships.get(id).keySet()) {
						backupServer.addBuddy(user, tblFriendships.get(id).get(user), ServerInterface.SERVER);
					}
				}
 				
 				for(String user : getAllUser()) {
 					backupServer.login(user, tblUser.get(user), _userStore.get(user), ServerInterface.SERVER);
 				}
 			}
 		} catch(Exception ex) {
 			System.out.println("ERROR in registerBackupServer");
 		}
 		initRequested = false;
 	}
 
 	public Server() throws RemoteException {
 		_userStore = new HashMap<String, ClientInterface>();
 		secondary = false;
 		backupServer = null;
 	}
 	
 	
 	/**
 	 * registers a new User with Username and password
 	 * @param userName the Username to check and register
 	 * @param password the password to register
 	 * @return true if the username doesn't exist, false otherwise
 	 */
 	@Override
 	public boolean newUser(String userName, String password, int sentBy) {
 		boolean retVal = false;
 		System.out.println("register: " + userName + ", " + password);
 		retVal = db.register(userName, password);
 		if((sentBy == ServerInterface.CLIENT) && (backupServer != null)) {
 			try {
 				backupServer.ping();
 				backupServer.newUser(userName, password, ServerInterface.SERVER);
 			} catch(Exception ex) {
 				System.out.println("backup Server not responding");
 				resetBackupServer();
 			}
 		}
 			
 		
 		return retVal;
 	}
 	
 	
 	public boolean addBuddy(String user, String buddy, int sentBy) {
 		boolean retVal = db.addBuddies(user, buddy);
 		
 		if((sentBy == ServerInterface.CLIENT) && (backupServer != null) && (retVal == true)) {
 			try {
 				backupServer.ping();
 				backupServer.addBuddy(user, buddy, ServerInterface.SERVER);
 			} catch(Exception ex) {
 				System.out.println("backup Server not responding");
 				resetBackupServer();
 			}
 		}
 		
 		notifyUserListChanged();
 		
 		return retVal;
 	}
 	
 	
 	public String[] getBuddies(String user) {
 		return db.getBuddies(user).toArray(new String[0]);
 	}
 	
 	
 	/**
 	 * Registers user on the server and greets them
 	 * @param userName Name of the user which will be registered
 	 * @param clientObject The client interface which calls the register method
 	 * @param sentBy Identify whether the method was called by a Client or a Server
 	 * @return True if user name is free, false otherwise
 	 */
 	@Override
 	public boolean login(String userName, String password, ClientInterface clientObject, int sentBy) {
 		
 		if (!db.checkUser(userName, password)) {
 			System.out.println("falscher Username oder falsches Passwort");
 			return false;
 		}
 		
 		
 		_userStore.put(userName, clientObject);
 		this.notifyUserListChanged();
 
 		
 		if((sentBy == ServerInterface.CLIENT) && (backupServer != null)) {
 			try {
 				backupServer.ping();
 				backupServer.login(userName, password, clientObject, ServerInterface.SERVER);
 			} catch(Exception ex) {
 				System.out.println("backup Server not responding");
 				resetBackupServer();
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Unregisters a user from the server.
 	 * @param username Name of the user which will be unregistered
 	 * @param sentBy Identify whether the method was called by a Client or a Server
 	 */
 	@Override	
 	public void logout(String username, int sentBy) {
 		_userStore.remove(username);
 		
 		notifyUserListChanged();
 		
 		if((sentBy == ServerInterface.CLIENT) && (backupServer != null)) {
 			try {
 				backupServer.ping();
 				backupServer.logout(username, ServerInterface.SERVER);
 			} catch(Exception ex) {
 				System.out.println("backup Server not responding");
 				resetBackupServer();
 			}
 		}
 	}
 	
 	/**
 	 * Function to check, if the other server is online and responding
 	 */
 	@Override
 	public boolean ping(){
 		return true;
 	}
 
 	/**
 	 * Gets all users which are registered at the server.
 	 * @return Array of Strings with the user names
 	 */
 	@Override
 	public String[] getAllUser() {
 		return _userStore.keySet().toArray(new String[0]);
 	}
 	
 	/**
 	 * Gets a message from a client and sends it to another client
 	 * @param sender User name who sends the message
 	 * @param receiver User name to where the message should be delivered
 	 * @param message The message which should be send
 	 * @param sentBy Identify whether the method was called by a Client or a Server
 	 */
 	@Override
 	public void sendMessage(String sender, String receiver, String message, int sentBy) throws RemoteException {
 		System.out.println("sending ...");
 		if(_userStore.get(receiver) != null) {
 			System.out.println("user is online");
 			ClientInterface client = _userStore.get(receiver);
 			if(client != null)
 				client.notifyMessage(sender, message);
 		} else {
 			System.out.println("user not online!!");
 			db.storeMessage(sender, receiver, message);
 		
 			if((sentBy == ServerInterface.CLIENT) && (backupServer != null)) {
 				try {
 					backupServer.ping();
 					backupServer.sendMessage(sender, receiver, message, ServerInterface.SERVER);
 				} catch(Exception ex) {
 					System.out.println("backup Server not responding");
 					resetBackupServer();
 				}
 			}
 		}
 	}
 	
 	
 	
 	public HashMap<String, ArrayList<String>> getMessages(String user) throws RemoteException {
 		return db.getMessages(user);
 	}
 	
 	
 	public void deleteMessage(String sender, String receiver, String message, int sentBy) {
 		db.deleteMessage(sender, receiver, message);
 		
 		if((sentBy == ServerInterface.CLIENT) && (backupServer != null)) {
 			try {
 				backupServer.ping();
 				backupServer.deleteMessage(sender, receiver, message, ServerInterface.SERVER);
 			} catch(Exception ex) {
 				System.out.println("backup Server not responding");
 				resetBackupServer();
 			}
 		}
 	}
 	
 	
 	/**
 	 * Determinate if the server will be the primary or the secondary server
 	 * @return True when Server is secondary, false otherwise
 	 */
 	private boolean is_secondary(){
 		try{
 			ServerInterface remoteObj = (ServerInterface) Naming.lookup("rmi://127.0.0.1:9090/server1");
 			if(remoteObj.ping()){
 				secondary=true;
 			}
 			return secondary;
 		}catch(Exception ex){
 			System.out.println("No Server found, assume to be primary Server");
 			return secondary;
 		}
 	}
 
 	/**
 	 * Registers the server at the rmi registry. 
 	 * The binded address depends if the server is secondary or not.
 	 */
 	private void connect(){
 		try {
 			if(!is_secondary()){
 				name ="rmi://127.0.0.1:9090/server1";
 				Naming.rebind(name, server);
 				System.out.println("Server 1");
 				connectBackupServer();
 				if(backupServer != null) {
 					System.out.println("hier!!!!!!!!!");
 					db.refresh();
 					backupServer.requestInit();
 				}
 			}
 			else{
 				try {
 					ServerInterface remoteObj = (ServerInterface) Naming.lookup("rmi://127.0.0.2:9090/server2");
 					remoteObj.ping();
 					System.exit(0);
 				}
 				catch(Exception ex){
 					name="rmi://127.0.0.2:9090/server2";
 					Naming.rebind(name, server);
 					System.out.println("Server 2");
 					connectBackupServer();
 					System.out.println("hier!!!!!!!!!");
 					db.refresh();
 					backupServer.requestInit();
 				}
 			}
 			System.out.println("Server binded object successfull!");	
 		} catch(Exception ex) {
 			System.out.println(ex.getMessage());
 			System.exit(0);
 		}
 		
 	}
 	
 	/**
 	 * Connects to the database
 	 */
 	private void connect_db(){
 		if(!secondary)
 			db = new Database("jdbc:postgresql://localhost/vsy","vsy","vsy");
 		else
 			db = new Database("jdbc:postgresql://localhost/vsy2","vsy2","vsy2");
 	}
 	
 	
 	public static void main(String[] args) {
 		System.setSecurityManager(new SecurityManager());
 		
 		try {
 			server = new Server();
 			server.connect_db();
 			server.connect();
 			System.out.println(db.getUsers().toString());
 			System.out.println(db.getFriendships().toString());
 
 			
 			
 			while(true) {
 				if(server.getBackupServer() != null) {
 					try {
 						server.getBackupServer().ping();
 					} catch(Exception ex) {
 						System.out.println("backupServer not responding!");
 						server.resetBackupServer();
 					}
 				}
 				else {
 					try {
 						server.connectBackupServer();
 						server.getBackupServer().ping();
 						if(server.initRequested)
 							server.initBackupServer();
 					} catch(Exception ex) {
 						System.out.println("no backupServer found");
 						server.resetBackupServer();
 					}
 				}
 				
 				System.out.println("Users registered: " + Arrays.toString(server._userStore.keySet().toArray(new String[0])));
 				Thread.sleep(2000);
 			}
 			
 		} catch(Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 		
 	}
 }
