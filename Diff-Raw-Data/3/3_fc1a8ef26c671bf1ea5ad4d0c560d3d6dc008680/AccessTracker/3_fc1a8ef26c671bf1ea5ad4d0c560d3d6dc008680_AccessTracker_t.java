 package main;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import GUI.Driver;
 
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
 	private ArrayList<User> usersWithTools;
 	private static DB database;
 	private final String hostName = "dharma.mongohq.com";
 	private final int port = 10096;
 	private final String dbName = "CSM_Machine_Shop";
 	private final String username = "csm";
 	private final String password = "machineshop";
 	private User currentUser = new User("", "", "", "", "");
 
 	public AccessTracker() {
 		currentUsers = new ArrayList<User>();
 		usersWithTools = new ArrayList<User>();
 		machines = new ArrayList<Machine>();
 		tools = new ArrayList<Tool>();
 		availableTools = new ArrayList<Tool>();
 
 		// Do the initialization stuff for the log and database
 		databaseSetup();
 		Log.setup();
 		
 		setUsersWithTools();
 
 		loadMachines();
 		loadTools();
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
 	
 	public void setUsersWithTools() {
 		usersWithTools.clear();
 		DBCollection users = database.getCollection("Users");
 		DBCursor cursor = users.find();
 		while(cursor.hasNext()) {
 			DBObject current = cursor.next();
 			if (current.get("checkedOutTools") != null && !((ArrayList) current.get("checkedOutTools")).isEmpty()) {
 				usersWithTools.add(findUserByCWID(current.get("CWID").toString()));
 			}
 		}
 	}
 
 	// Takes a CWID and attempts to load that user from the
 	// database and add them to the list of current users.
 	// If the CWID doesn't exist, create new user.
 	// Returns the name of the user with this CWID
 	public User loadUser(String CWID) {
 		if (currentUsers.contains(new User("", "", CWID, "", ""))) {
 			currentUser = getUser(CWID);
 		} else {
 			currentUser = findUserByCWID(CWID);
 			if ( currentUser == null ) {
 				try {
 					currentUser = createUser(CWID);
 					if ( currentUser == null ) {
 						JOptionPane.showMessageDialog(Driver.getMainGui(),
 								"Sorry, our records show you are not a registered Mines student." +
 								"\nIf you believe this is an error, please visit the registrar." +
 								"\n\nTo use the machine shop, you must talk to the attendant on duty."
 								);
 						return null;
 					}
 				} catch (SQLException e) {
 					System.out.println(e.getMessage());
 					e.printStackTrace();
 				}
 			}
 			currentUsers.add(currentUser);
 		}
 		
 		return currentUser;
 	}
 
 	// Loads all the tools from the database into RAM
 	public void loadTools() {
 		tools.clear();
 		DBCollection allTools = database.getCollection("Tools");
 		DBCursor cursor = allTools.find();
 		while(cursor.hasNext()) {
 			DBObject tool = cursor.next();
 			Tool t = new Tool((String) tool.get("name"), (String) tool.get("upc"));
 			t.setCheckedOut((boolean) tool.get("isCheckedOut"));
 			t.setLastUsedBy((String) tool.get("lastUsedBy"));
 			tools.add(t);
 		}
 	}
 
 	public void addTool(Tool t) {
 		tools.add(t);
 	}
 
 	public void removeTool(Tool t) {
 		tools.remove(t);
 		
 	}
 
 	// Loads all the machines from the database into RAM
 	public void loadMachines() {
 		machines.clear();
 		DBCollection allMachines = database.getCollection("Machines");
 		DBCursor cursor = allMachines.find();
 		while(cursor.hasNext()) {
 			DBObject machine = cursor.next();
 			Machine m = new Machine((String) machine.get("name"), (String) machine.get("ID"));
 			machines.add(m);
 		}
 	}
 
 	public void addMachine(Machine m) {
 		machines.add(m);
 	}
 
 	public void removeMachine(Machine m) {
 		machines.remove(m);
 	}
 
 	// Creates a new user. Should be called by loadUser()
 	// Persists new user to database
 	public User createUser(String firstName, String lastName, String CWID, String email, String department) {
 		User newUser = new User(firstName, lastName, CWID, email, department);
 
 		BasicDBObject document = new BasicDBObject();
 		document.put("firstName", firstName);
 		document.put("lastName", lastName);
 		document.put("CWID", CWID);
 		document.put("email", email);
 		document.put("department", department);
 
 		DBCollection users = database.getCollection("Users");
 
 		users.insert(document);
 
 		currentUsers.add(newUser);
 
 		return newUser;
 	}
 
 	// Create user by finding that user from Oracle database.
 	public User createUser(String CWID) throws SQLException {
 		OracleConnection oracleConnection = new OracleConnection();
 		oracleConnection.getConnection();
 		ArrayList<String> results = oracleConnection.select(CWID);
 		oracleConnection.close();
 
 		if ( results.size() != 0 ) {
 			return createUser(results.get(1), results.get(2), CWID, results.get(3), results.get(4));
 		} else {
 			return null;
 		}
 	}
 
 	public void removeUser(User u) {
 		currentUsers.remove(u);
 		if (u.getToolsCheckedOut().isEmpty()) {
 			usersWithTools.remove(u);
 		}
 	}
 
 	public void clearUsers(ArrayList<User> users) {
 		for (User u:users) {
 			currentUsers.remove(u);
 		}
 	}
 
 	// Loads the user with this CWID to list of current users
 	// Adds entry to log
 	public User processLogIn(String CWID) {
 		if (currentUsers.contains(new User("", "", CWID, "", ""))) {
 			currentUser = getUser(CWID);
 		} else {
 			currentUser = loadUser(CWID);
 		
 			if ( currentUser != null ) {
 
 				// IF the user with this CWID is locked (boolean locked)
 				// THEN display some error message, and make a note somewhere
 				// (log this attempt for admin to view later)
 
 				if ( currentUser.isLocked() ) {
 					String message = "You have been locked out of the system.\n" +
 							"You must talk to a shop supervisor to get unlocked";
 					JOptionPane.showMessageDialog(Driver.getMainGui(), message);
 					currentUser = new User(currentUser.getFirstName(), currentUser.getLastName(), currentUser.getCWID() + " [LOCKED]", currentUser.getEmail(), currentUser.getDepartment());
 					Log.startEntry(currentUser);
					Log.finishEntry(currentUser.getCurrentEntry());
					removeUser(currentUser);
 					return null;
 				}
 
 				Log.startEntry(currentUser);
 			}
 		}
 		Driver.isLogInScreen = false;
 		return currentUser;
 	}
 
 	// Removes the user with this CWID from the list
 	// of current users.
 	// Also finishes the log entry for this user.
 	public void processLogOut(String CWID) {
 		System.out.println(getUser(CWID));
 		System.out.println(getUser(CWID).getCurrentEntry());
 		Log.finishEntry(getUser(CWID).getCurrentEntry());
 		removeUser(getUser(CWID));
 	}
 
 	public void updateTools() {
 		availableTools.clear();
 		for (Tool t: tools) {
 			if (!t.isCheckedOut())
 				availableTools.add(t);
 		}
 	}
 
 	public User findUserByCWID(String cwid){
 		DBCollection users = database.getCollection("Users");
 		DBObject result = users.findOne(new BasicDBObject("CWID", cwid));
 		boolean isAdministrator;
 		boolean isSystemAdministrator;
 		boolean isLocked;
 		String firstName = "";
 		String lastName = "";
 		String email = "";
 		String department = "";
 		User user;
 
 		if ( result == null ) {
 			return null;
 		}
 
 		if (result.get("locked") == null) {
 			isLocked = false;
 		} else {
 			isLocked = (boolean) result.get("locked");
 		}
 
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
 		
 		if (result.get("department") == null) {
 			department = "undeclared";
 		} else {
 			department = (String) result.get("department");
 		}
 
 		// First name, last name, and email should not be null.
 		firstName = (String) result.get("firstName");
 		lastName = (String) result.get("lastName");
 		email = (String) result.get("email");
 		
 		if ( isAdministrator ) {
 			if ( isSystemAdministrator ) {
 				user = new SystemAdministrator(firstName, lastName, cwid, email, department);
 			} else {
 				user = new Administrator(firstName, lastName, cwid, email, department);
 			}
 		}  else {
 			user = new User(firstName, lastName, cwid, email, department);
 		}
 
 		user.setLockedStatus(isLocked);
 
 		//Retrieve user's certified machines
 		ArrayList<Machine> machinesList = new ArrayList<Machine>();
 		DBCollection machinesColl = database.getCollection("Machines");
 
 		ArrayList<BasicDBObject> certMachines = (ArrayList<BasicDBObject>)result.get("certifiedMachines");
 		if (certMachines == null) {
 			user.setCertifiedMachines(new ArrayList<Machine>());
 		} else {
 			for(BasicDBObject embedded : certMachines){ 
 				String id = (String)embedded.get("id"); 
 				DBCursor machine = machinesColl.find(new BasicDBObject("ID", id));
 				if (machine.hasNext()) {
 					machinesList.add(new Machine((String) machine.next().get("name"), id));
 				}
 			}
 			user.setCertifiedMachines(machinesList);
 		}
 
 		//Retrieve user's checkedOutTools
 		ArrayList<Tool> checkedOutToolsList = new ArrayList<Tool>();
 		DBCollection toolsColl = database.getCollection("Tools");
 
 		ArrayList<BasicDBObject> COTools = (ArrayList<BasicDBObject>)result.get("checkedOutTools");
 		if(COTools == null) {
 			user.loadCheckedOutTools(new ArrayList<Tool>());
 		} else {
 			for(BasicDBObject embedded : COTools){ 
 				String upc = (String) embedded.get("upc"); 
 				DBCursor tool = toolsColl.find(new BasicDBObject("upc", upc));
 				if (tool.hasNext()) {
 					checkedOutToolsList.add(new Tool((String) tool.next().get("name"), upc));
 				}
 			} 
 		}
 		user.loadCheckedOutTools(checkedOutToolsList);
 
 		return user;
 	}
 
 	public ArrayList<DBObject> searchDatabase(String collectionName, String searchFieldName, String searchFieldValue) {
 
 		DBCollection collection = database.getCollection(collectionName);
 		Pattern p = Pattern.compile(searchFieldValue, Pattern.CASE_INSENSITIVE);
 		DBCursor cursor = collection.find(new BasicDBObject(searchFieldName, p));
 
 		ArrayList<DBObject> returnList = new ArrayList<DBObject>();
 		while (cursor.hasNext()) {
 			returnList.add(cursor.next());
 		}
 
 		return returnList;
 	}
 	
 	public ArrayList<DBObject> searchDatabaseForUser(String userName) {
 		
 		String firstName = "";
 		String lastName = "";
 		
 		if ( userName.contains(" ")) {
 			String[] names = userName.split(" ");
 			firstName = names[0];
 			lastName = names[1];
 		} else {
 			firstName = userName;
 			lastName = userName;
 		}
 		
 		DBCollection collection = database.getCollection("Users");
 		Pattern firstNamePattern = Pattern.compile(firstName, Pattern.CASE_INSENSITIVE);
 		Pattern lastNamePattern = Pattern.compile(lastName, Pattern.CASE_INSENSITIVE);
 		
 		DBCursor cursor1 = collection.find(new BasicDBObject("firstName", firstNamePattern));
 		DBCursor cursor2 = collection.find(new BasicDBObject("lastName", lastNamePattern));
 
 		ArrayList<DBObject> returnList1 = new ArrayList<DBObject>();
 		ArrayList<DBObject> returnList2 = new ArrayList<DBObject>();
 		
 		while (cursor1.hasNext()) {
 			returnList1.add(cursor1.next());
 		}
 		
 		while (cursor2.hasNext()) {
 			returnList2.add(cursor2.next());
 		}
 		
 		if ( userName.contains(" ")) {
 			returnList1.retainAll(returnList2);
 			return returnList1;
 		} else {
 			returnList1.addAll(returnList2);
 			HashSet<DBObject> returnSet = new HashSet<DBObject>(returnList1);
 			return new ArrayList<DBObject>(returnSet);
 		}
 	}
 	
 	// check if email follows the pattern.
 	public boolean checkValidEmail(String email){
 		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 		try {
 			Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 			Matcher matcher = pattern.matcher(email);
 			return matcher.matches();
 		}catch(Exception e) {
 			return false;
 		}
 	}
 
 	/********************************** GETTERS AND SETTERS *******************************************/
 
 	public User getUser(String cwid) {
 		for (User u : currentUsers) {
 			if (u.getCWID().equals(cwid)) {
 				return u;
 			}
 		}
 		return null;
 	}
 
 	public ArrayList<User> getCurrentUsers() {
 		return currentUsers;
 	}
 	
 	public void setCurrentUsers(ArrayList<User> currentUsers) {
 		this.currentUsers = currentUsers;
 	}
 
 	public ArrayList<Machine> getMachines() {
 		return machines;
 	}
 
 	public ArrayList<User> getUsersWithTools() {
 		return usersWithTools;
 	}
 
 	public Machine getMachineByName(String name) {
 		for (Machine m:machines) {
 			if (m.getName().equals(name)) {
 				return m;
 			}
 		}
 		return null;
 	}
 	
 	public Machine getMachineByID(String id) {
 		for (Machine m:machines) {
 			if (m.getID().equals(id)) {
 				return m;
 			}
 		}
 		return null;
 	}
 
 	public Tool getToolByUPC(String upc){
 		for (Tool t : tools){
 			if (t.getUPC().equals(upc)){
 				return t;
 			}
 		}
 		return null;
 	}
 	
 	public Tool getToolByName(String name){
 		for (Tool t : tools){
 			if (t.getName().equals(name)){
 				return t;
 			}
 		}
 		return null;
 	}
 	
 	public ArrayList<Tool> getTools() {
 		return tools;
 	}
 
 	public ArrayList<Tool> getAvailableTools() {
 		return availableTools;
 	}
 
 	public static DB getDatabase() {
 		return database;
 	}
 
 	public User getCurrentUser() {
 		return currentUser;
 	}
 
 	public void setCurrentUser(User currentUser) {
 		this.currentUser = currentUser;
 	}
 }
