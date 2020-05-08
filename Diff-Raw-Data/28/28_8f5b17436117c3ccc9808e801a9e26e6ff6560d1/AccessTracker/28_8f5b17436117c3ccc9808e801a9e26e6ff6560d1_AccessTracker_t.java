 package main;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
import javax.swing.JOptionPane;

 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class AccessTracker {
 	
 	private ArrayList<Machine> machines;
 	private ArrayList<Tool>	tools;
 	private ArrayList<Tool> availableTools;
 	private ArrayList<User> currentUsers;
 	private InputReader inputReader;
 	private Log log;
 	private DB database;
 	private final String hostName = "dharma.mongohq.com";
 	private final int port = 10096;
 	private final String dbName = "CSM_Machine_Shop";
 	private final String username = "tsallee";
 	private final String password = "machineshop";
 	
 	public AccessTracker() {
 		currentUsers = new ArrayList<User>();
 		inputReader = new InputReader();
 		machines = new ArrayList<Machine>();
 		tools = new ArrayList<Tool>();
 		availableTools = new ArrayList<Tool>();
 		// Do the initialization stuff for the log and database
 		databaseSetup();
 		Log.setup();
 	}
 	
 	private void databaseSetup() {
 		try {
 			MongoClient client = new MongoClient(hostName, port);
 			database = client.getDB(dbName);
 			database.authenticate(username, password.toCharArray());			
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void messAroundWithDatabase() {
 		DBCollection tools = database.getCollection("Tools");
 		DBCursor cursor = tools.find(new BasicDBObject("upc", 100));
 		while ( cursor.hasNext() ) {
 			DBObject result = cursor.next();
 			System.out.println((String) result.get("name"));
 		}
 	}
 	
 	public boolean userExistsInDataBase(int CWID) {
 		return false;
 	}
 	
 	// Loads an existing user from the database into RAM
 	// The user is found by querying by CWID
 	public User loadUser(int CWID) {
 		return new User("", "", 0);
 	}
 	
 	// Loads all the tools from the database into RAM
 	public void loadTools() {
 		// tools = db.get(tools);
 	}
 	
 	// Loads all the machines from the database into RAM
 	public void loadMachines() {
 		// tools = db.get(machines);
 	}
 	
 	// Creates a new user. Should be called by processLogIn()
 	public void createUser(String firstName, String lastName, int CWID) {
 
 	}
 	
 	// Takes a CWID and attempts to load that user from the
 	// database and add them to the list of current users.
 	// If the CWID doesn't exist, a new user is created, added to the list
 	// of current users, and persisted to the database.
 	// Also adds data to the log for this user
 	// Returns the name of the user with this CWID
 	public User processLogIn(int CWID) {
 		// IF the user with this CWID is locked (boolean isLocked)
 		// THEN display some error message, and make a note somewhere
 		// (log this attempt for admin to view later)
 		
 		User currentUser;
 		boolean isAdministrator;
 		boolean isSystemAdministrator;
		String firstName = "";
		String lastName = "";
 		
 		DBCollection users = database.getCollection("Users");
 		DBCursor cursor = users.find(new BasicDBObject("CWID", CWID));
		
		if ( !cursor.hasNext() ) {
			if ( !checkLegitimacy(CWID) ) {
				// DO_SOMETHING
			} else {
				
				// FOR NOW (UNTIL WE GET BLASTERCARD DATABASE ACCESS)
				firstName = JOptionPane.showInputDialog("Enter your first name.");
				lastName = JOptionPane.showInputDialog("Enter your last name.");
				createUser(firstName, lastName, CWID);
			}			
		}
				
 		DBObject result = cursor.next();
 		
 		if (result.get("isAdmin") == null) {
 			isAdministrator = false;
 		} else {
 			isAdministrator = (boolean) result.get("isAdmin");
 		}
 		
 		if ( result.get("isSystemAdmin") == null ) {
 			isSystemAdministrator = false;
 		} else {
 			isSystemAdministrator = (boolean) result.get("isSystemAdmin");
 		}
 		
		firstName = (String) result.get("firstName");
		lastName = (String) result.get("lastName");
 		
 		if ( isAdministrator ) {
 			if ( isSystemAdministrator ) {
 				currentUser = new SystemAdministrator(firstName, lastName, CWID);
 			} else {
 				currentUser = new Administrator(firstName, lastName, CWID);
 			}
 		}  else {
 			currentUser = new User(firstName, lastName, CWID);
 		}
 		
 		currentUsers.add(currentUser);
 		
 		return currentUser;
 		
 	}
 	
 	public void displayUserMachines(int CWID) {
 		
 	}
 	
 	// Removes the user with this CWID from the list
 	// of current users.
 	// Also finishes the log entry for this user.
 	public void processLogOut(int CWID) {
 		
 	}
 	
 	// super bad. myLife.close()
 	public void updateTools() {
 		availableTools.clear();
 		for (Tool t: tools) {
 			if (!t.isCheckedOut())
 				availableTools.add(t);
 		}
 	}
 	
 	// Check that the CWID exists in the blastercard database
 	public boolean checkLegitimacy(int CWID) {
 		return false;
 	}
 
 	
 	/********************************** GETTERS AND SETTERS *******************************************/
 	
 	public User getUser(int CWID) {
 		// cheating right now
 		// need to fix later
 		User returnUser = new User("", "", CWID);
 		return returnUser;
 	}
 	
 	public ArrayList<User> getCurrentUsers() {
 		return currentUsers;
 	}
 
 	public Log getLog() {
 		return log;
 	}
 
 	public ArrayList<Machine> getMachines() {
 		return machines;
 	}
 
 	public ArrayList<Tool> getTools() {
 		return tools;
 	}
 
 	public ArrayList<Tool> getAvailableTools() {
 		return availableTools;
 	}
 	
 }
 
