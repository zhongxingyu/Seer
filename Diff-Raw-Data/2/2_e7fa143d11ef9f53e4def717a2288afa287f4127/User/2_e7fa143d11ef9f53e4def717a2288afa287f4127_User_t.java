 package main;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import GUI.Driver;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class User {
 	private ArrayList<Machine> certifiedMachines;
 	private ArrayList<Tool>	toolsCheckedOut;
 	private String CWID;
 	private String firstName;
 	private String lastName;
 	private LogEntry currentEntry;
 	private boolean locked;
 	protected boolean isAdmin = false;
 	protected boolean isSystemAdmin = false;
 	private String email;
 	private String department;
 		
 	public User(String firstName, String lastName, String CWID, String email, String department) {
 		// needs to be extracted from data base
 		certifiedMachines = new ArrayList<Machine>();
 		toolsCheckedOut = new ArrayList<Tool>();
 		this.CWID = CWID;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.email = email;
 		this.department = department;
 		locked = false;
 	}
 	
 	public void checkoutTool(Tool tool) {
 		tool.checkoutTool();
 		tool.updateCheckoutStatus(this);
 		toolsCheckedOut.add(tool);
 		
 		DBCollection usersCollection = Driver.getAccessTracker().getDatabase().getCollection("Users");
 		DBCursor cursor = usersCollection.find(new BasicDBObject("CWID", CWID));
 		if (cursor.hasNext()) {
 			DBObject result = cursor.next();
 			BasicDBList checkedoutTools = new BasicDBList();
 			for (Tool t:toolsCheckedOut) {
 				checkedoutTools.add(new BasicDBObject("upc", t.getUPC()));
 			}
 			result.put("checkedOutTools", checkedoutTools);
 
 			usersCollection.update(new BasicDBObject("CWID", CWID), result);
 		}
 	}
 
 	public void returnTools(ArrayList<Tool> tools) {
 		
 		for (int i=0; i < tools.size(); ++i) {
 			Tool tool = tools.get(i);
 			
 			Tool to = Driver.getAccessTracker().getToolByUPC(tool.getUPC());
 			if (to != null) {
 				to.returnTool();
 				to.updateCheckoutStatus(this);
 			}
			toolsCheckedOut.remove(tool);
 		}
 //		
 //		DBCollection usersCollection = Driver.getAccessTracker().getDatabase().getCollection("Users");
 //		DBCursor cursor = usersCollection.find(new BasicDBObject("CWID", CWID));
 //		if (cursor.hasNext()) {
 //			DBObject result = cursor.next();
 //			BasicDBList checkedoutTools = new BasicDBList();
 //			for (Tool t:toolsCheckedOut) {
 //				checkedoutTools.add(new BasicDBObject("upc", t.getUPC()));
 //			}
 //			result.put("checkedOutTools", checkedoutTools);
 //
 //			usersCollection.update(new BasicDBObject("CWID", CWID), result);
 //		}
 	}
 	
 	public void useMachine(Machine m) {
 		m.use();
 	}
 	
 	public void stopUsingMachine(Machine m) {
 		m.stopUsing();
 	}
 
 	public String getCWID() {
 		return CWID;
 	}
 	
 	public boolean isLocked() {
 		return locked;
 	}
 	
 	public void setLockedStatus(boolean lock) {
 		locked = lock;
 	}
 
 	public ArrayList<Machine> getCertifiedMachines() {
 		return certifiedMachines;
 	}
 	
 	public void setCertifiedMachines(ArrayList<Machine> machines) {
 		certifiedMachines = machines;
 	}
 	
 	public ArrayList<Tool> getToolsCheckedOut() {
 		return toolsCheckedOut;
 	}
 	
 	public void loadCheckedOutTools(ArrayList<Tool> tools) {
 		toolsCheckedOut = tools;
 	}
 	
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	public String getLastName() {
 		return lastName;
 	}
 	
 	public boolean isAdmin() {
 		return isAdmin;
 	}
 	
 	public boolean isSystemAdmin() {
 		return isSystemAdmin;
 	}
 	
 	public void setAdmin(boolean b) {
 		isAdmin = b;
 	}
 	
 	public void setSystemAdmin(boolean b) {
 		isSystemAdmin = b;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public String getDepartment() {
 		return department;
 	}
 
 	// only the CWID are compared to check if two users are the same
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof User))
 			return false;
 		
 		User obj = (User) o;
 		return (this.CWID.equals(obj.getCWID()));
 	}
 	
 	public String toString() {
 		return firstName + " " + lastName;
 	}
 	
 	public LogEntry getCurrentEntry() {
 		return currentEntry;
 	}
 
 	public void setCurrentEntry(LogEntry currentEntry) {
 		this.currentEntry = currentEntry;
 	}
 }
