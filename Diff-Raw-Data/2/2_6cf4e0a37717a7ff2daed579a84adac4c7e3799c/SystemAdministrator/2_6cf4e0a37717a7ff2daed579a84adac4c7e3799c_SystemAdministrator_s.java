 package main;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import GUI.Driver;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 
 public class SystemAdministrator extends Administrator {
 	private AccessTracker tracker;
 	private DB database;
 	private DBCollection users;
 	
 	public SystemAdministrator(String firstName, String lastName, String cwid, String email, String department) {
 		super(firstName, lastName, cwid, email, department);
 		isSystemAdmin = true;
 		tracker = Driver.getAccessTracker();
 		database = tracker.getDatabase();
 		users = database.getCollection("Users");
 	}
 	
 	// updates user status as user, admin, or system admin.
 	public void updatePermissions(User user, boolean isAdmin, boolean isSystemAdmin) {
 		DBCursor cursor = users.find(new BasicDBObject("CWID", user.getCWID()));
 		if (!(cursor == null)) {
 			BasicDBObject obj = (BasicDBObject) cursor.next();
 			obj.append("isAdmin", isAdmin);
 			obj.append("isSystemAdmin", isSystemAdmin);
 			users.update(new BasicDBObject("CWID", user.getCWID()), obj);
 		}
 	}
 	
 	// updates user's machine certifications.
 	public void updateCertifications(User user, ArrayList<Machine> machines) {
 		DBCursor cursor = users.find(new BasicDBObject("CWID", user.getCWID()));
 		if (cursor.hasNext()) {
 			DBObject result = cursor.next();
 			BasicDBList machinePermissions = new BasicDBList();
 			for (Machine m:machines) {
 				machinePermissions.add(new BasicDBObject("id", m.getID()));
 			}
 			result.put("certifiedMachines", machinePermissions);
 			
 			users.update(new BasicDBObject("CWID", user.getCWID()), result);
 			user.setCertifiedMachines(machines);
 		}
 	}
 	
 	// TODO used only in test. remove this and change test
 	public void removeUsers(ArrayList<User> userList) {
 		for (User u : userList) {
 			DBCursor cursor = users.find(new BasicDBObject("CWID", u.getCWID()));
 			if (!(cursor == null)) {
 				users.remove(cursor.next());
 				tracker.removeUser(u);
 			}
 		}
 	}
 	
 	public void generateReport() {
 		
 	}
 	
 	// adds the tool to database.
 	public void addTool(Tool t) {
 		DBCollection tools = database.getCollection("Tools");
 		DBCursor cursor = tools.find(new BasicDBObject("upc", t.getUPC()));
 		if (!cursor.hasNext()) {
 			BasicDBObject tool = new BasicDBObject();
 			tool.put("name", t.getName());
 			tool.put("upc", t.getUPC());
 			tool.put("isCheckedOut", false);
 			tools.insert(tool);
 			tracker.addTool(t);
 		} else {
 			JOptionPane.showMessageDialog(Driver.getMainGui(), "Tool already in system...Unable to add");
 		}
 	}
 	
 	// adds the machine to database.
 	public void addMachine(Machine m) {
 		DBCollection machines = database.getCollection("Machines");
 		DBCursor cursor = machines.find(new BasicDBObject("ID", m.getID()));
 		if (!cursor.hasNext()) {
 			BasicDBObject machine = new BasicDBObject();
 			machine.put("name", m.getName());
 			machine.put("ID", m.getID());
 			machines.insert(machine);
 			tracker.addMachine(m);
 		} else {
 			JOptionPane.showMessageDialog(Driver.getMainGui(), "Machine already in system...Unable to add");
 		}
 	}
 	
 	// adds the user to database
 	public boolean addUser(User u) {
 		DBCursor cursor = users.find(new BasicDBObject("CWID", u.getCWID()));
 		if ( !cursor.hasNext() ) {
 			BasicDBObject document = new BasicDBObject();
 			document.put("firstName", u.getFirstName());
 			document.put("lastName", u.getLastName());
 			document.put("CWID", u.getCWID());
 			document.put("email", u.getEmail());
 			document.put("department", u.getDepartment());
 			users.insert(document);
 			return true;
 		} else {
 			JOptionPane.showMessageDialog(Driver.getMainGui(), "User already in system...Unable to add");
 			return false;
 		}		
 	}
 	
 	// removes the tool from database
 	public void removeTool(String upc) {
 		DBCollection tools = database.getCollection("Tools");
 		DBCursor cursor = tools.find(new BasicDBObject("upc", upc));
 		Tool t = new Tool("", "");
 		if (cursor != null) {
 			DBObject obj = cursor.next();
 			t = Driver.getAccessTracker().getToolByUPC(upc);
 			tools.remove(obj);
 			tracker.removeTool(new Tool((String) obj.get("name"), (String) obj.get("upc")));
 		}
 		if (t.getLastUsedBy() != null) {
 			ArrayList<Tool> ts = new ArrayList<Tool>();
 			ts.add(t);
 			t.getLastUsedBy().returnTools(ts);
 		}
 		
 	}
 	
 	// removes the machine from database
 	public void removeMachine(String id) {
 		DBCollection machines = database.getCollection("Machines");
 		DBCursor cursor = machines.find(new BasicDBObject("ID", id));
 		if (!(cursor == null)) {
 			DBObject obj = cursor.next();
 			machines.remove(obj);
 			tracker.removeMachine(new Machine((String) obj.get("name"), (String) obj.get("ID")));
 		}
 	}
 	
 	// removes the user from database
 	public void removeUser(String cwid) {
 		DBCursor cursor = users.find(new BasicDBObject("CWID", cwid));
 		User u = new User("", "", "", "", "");
 		ArrayList<String> tools = new ArrayList<String>();
 		if (!(cursor == null)) {
 			DBObject obj = cursor.next();
 			u = new User((String) obj.get("firstName"), (String) obj.get("lastName"), (String) obj.get("CWID"), (String) obj.get("email"), (String) obj.get("department"));
 			tools = (ArrayList<String>) obj.get("toolsCheckedOut");
 			users.remove(obj);
 			tracker.removeUser(u);
 		}
		if (tools != null) {
 			ArrayList<Tool> ts = new ArrayList<Tool>();
 			for (String t : tools) {
 				ts.add(Driver.getAccessTracker().getToolByUPC(t));
 			}
 			u.returnTools(ts);
 		}
 	}
 	
 	// loads new user from Oracel database
 	public User loadNewUser(String cwid, OracleConnection connection) throws SQLException {
 		
 		ArrayList<String> results = connection.select(cwid);
 		
 		if ( results.size() == 0 ) {
 			return null;
 		}
 		return new User(results.get(1), results.get(2), cwid, results.get(3), results.get(4));
 	}
 	
 	// locks the user
 	public void lockUser(User user) {
 		DBCursor cursor = users.find(new BasicDBObject("CWID", user.getCWID()));
 		if (!(cursor == null)) {
 			DBObject obj = cursor.next();
 			obj.put("locked", true);
 			users.update(new BasicDBObject("CWID", user.getCWID()), obj);
 			user.setLockedStatus(true);
 		}
 	}
 	
 	// unlocks the user
 	public void unlockUser(User user) {
 		DBCursor cursor = users.find(new BasicDBObject("CWID", user.getCWID()));
 		if (!(cursor == null)) {
 			BasicDBObject obj = (BasicDBObject) cursor.next();
 			obj.append("locked", false);
 			users.update(new BasicDBObject("CWID", user.getCWID()), obj);
 			user.setLockedStatus(false);
 		}
 	}
 	
 	// logs out all the users currently logged in
 	public void logOutAllUsers() {
 		ArrayList<User> currentUsers = tracker.getCurrentUsers();
 		for (User u : currentUsers) {
 			if ( !u.equals(tracker.getCurrentUser()) ) {
 				u.getCurrentEntry().adminFinishEntry();
 			}
 		}
 		
 		currentUsers = new ArrayList<User>();
 		currentUsers.add(tracker.getCurrentUser());
 		
 		tracker.setCurrentUsers(currentUsers);
 		
 	}
 }
