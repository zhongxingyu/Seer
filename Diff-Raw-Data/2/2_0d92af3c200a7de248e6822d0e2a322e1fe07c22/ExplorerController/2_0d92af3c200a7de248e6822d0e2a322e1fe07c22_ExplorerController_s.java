 /**
  * The explorer controller is used to interface between the model and the view.
  * The controller is in charge of checking that all input taken from the view
  * makes sense, and converting information from the model into a type preferred
  * by the view.
  * 
  *  @author Andrew Hollenbach <anh7216@rit.edu>
  *  @author Andrew DeVoe <ard5852@rit.edu>
  */
 
 package steam.dbexplorer.controller;
 
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import steam.dbexplorer.SystemCode;
 import steam.dbexplorer.Utils;
 import steam.dbexplorer.dbobject.DBReference;
 import steam.dbexplorer.model.ExplorerModel;
 
 public class ExplorerController {	
 	public static final String[] tableNames = {"Achievements", 
 										  	   "Applications", 
 										  	   "Friends", 
 										  	   "Owned Achievements", 
 										  	   "Owned Applications", 
 										  	   "Players" };
 	
 	public static final String[] supportedClauses = {"Where",
 													 "Sort by"};
 	
 	public static final String[] supportedOrders = {"Ascending",
 	 												"Descending"};
 	
 	public static final String[] operators = {"less than",
 											  "less than or equal to",
 											  "equal to",
 											  "not equal to",
 											  "greater than or equal to",
 											  "greater than"};
 
 	public static final String[] opEquivs = {"<",
 											 "<=",
 											 "=",
 											 "<>",
 											 ">=",
 											 ">"};
 	
 	public static final String[] stringOps = {"contains","equals"};
 	
 	public static final String[] dateOps = {"before","on","after"};
 	
 	/** 
 	 * A string value of the last entity type to be fetched. Might be 
 	 * deprecated.
 	 */
 	private String currentTable;
 	
 	public ExplorerController() {
 	}
 	
 	/**
 	 * Retrieves data from the database for the given tablename with the 
 	 * supplied options.
 	 * 
 	 * @param tableName The name of the table to retrive from
 	 * @param options A list of options regarding where conditionals and sort bys
 	 * @return A 2d array of data of the requested information
 	 */
 	public Object[][] getData(String tableName, String[] options) {
 		this.currentTable = tableName;
 		ExplorerModel.setUp();
 		Object[][] data = {};
 		if(tableName == null) {
 		} else if(tableName.equals("Achievements")) {
 			data = ExplorerModel.retrieveAchievements(options);
 		} else if(tableName.equals("Applications")) {
 			data = ExplorerModel.retrieveApplications(options);
 		} else if(tableName.equals("Friends")) {
 			data = ExplorerModel.retrieveFriends(options);
 		} else if(tableName.equals("Owned Achievements")) {
 			data = ExplorerModel.retrieveOwnedAchievements(options);
 		} else if(tableName.equals("Owned Applications")) {
 			data = ExplorerModel.retrieveOwnedApplications(options);
 		} else if(tableName.equals("Players")) {
 			data = ExplorerModel.retrievePlayers(options);
 		}
 		return data;
 	}
 
 	/**
 	 * Gets a list of attributes for the given table in human-readable
 	 * form. 
 	 * 
 	 * @param tableName The name of the table to get the attributes from
 	 * @return a list of attributes for the given table in human-readable
 	 * form. 
 	 */
 	public String[] getLabels(String tableName) {
 		tableName = DBReference.convertToDBFormat(tableName);
 		String[] labels = DBReference.tableLabels.get(tableName);
 		return labels;
 	}
 	
 	/**
 	 * Creates an entry using the given string values. Verifies if the 
 	 * values are correct. If any of the values are not parsable, returns
 	 * false. 
 	 * 
 	 * @param values The values to insert
 	 * @param entityName The name of the entity to create.
 	 * @return a systemcode regarding the success/failure of the operation
 	 */
 	public static SystemCode createEntry(String entityName, String[] values) {
 		entityName = DBReference.convertToDBFormat(entityName);
 		
 		int numAttr = DBReference.tableLabels.get(entityName).length;
 		if(numAttr > values.length) { //should never happen
 			String[] valsWithNullStrings = new String[numAttr];
 			for(int i=0;i<values.length;i++) {
 				valsWithNullStrings[i] = values[i]; 
 			}
 		}		
 		return ExplorerModel.createEntity(entityName,values);
 	}
 	
 	/**
 	 * Deletes an entry with the given key values. The json must contain
 	 * the full primary key.
 	 * 
 	 * @param entityName The table to delete from
 	 * @param json The values of the element to delete
 	 * @return a systemcode regarding the success/failure of the operation
 	 */
 	public SystemCode deleteEntity(String entityName, JSONObject json) {
 		entityName = DBReference.convertToDBFormat(entityName);
 		String[] attr = DBReference.primaryKeys.get(entityName);
 		String usingTables = DBReference.usingTables.get(entityName);
 		
 		try {
 			int numAttr = attr.length;
 			String[] values = new String[attr.length];
 			for(int i=0;i<numAttr;i++) {
 				String val = json.getString(attr[i]);
 				if("string".equals(getAttrType(attr[i]))){
             		val = Utils.surroundWithQuotes(val);
             	}
 				values[i] = convertToDbAttr(attr[i]) + "=" + val;
 			}
 			return ExplorerModel.deleteEntity(entityName, values,usingTables);
 		} catch(JSONException ex) {
 			return SystemCode.FAILURE;
 		}
 	}
 
 	/**
 	 * Sends a command to the explorer model to delete the entity. Ensures
 	 * the values are in the proper order.
 	 * 
 	 * @param json A JSONObject containing all the achievement values
 	 * @return Whether the operation was successful or not
 	 */
 	@Deprecated
 	public SystemCode deleteEntity(String entityName, String[] attr, JSONObject json, String usingTables) {
 		try {
 			int numAttr = attr.length;
 			String[] values = new String[attr.length];
 			for(int i=0;i<numAttr;i++) {
 				String val = json.getString(attr[i]);
 				if("string".equals(getAttrType(attr[i]))){
             		val = Utils.surroundAndSanitize(val);
             	}
 				values[i] = convertToDbAttr(attr[i]) + "=" + val;
 			}
 			return ExplorerModel.deleteEntity(entityName, values,usingTables);
 		} catch(JSONException ex) {
 			return SystemCode.FAILURE;
 		}
 	}
 	
 	/**
 	 * Updates an entry with the given values. The json must contain
 	 * the full primary key.
 	 * 
 	 * @param entityName The table to update
 	 * @param json The values of the element
 	 * @return a systemcode regarding the success/failure of the operation
 	 */
 	public SystemCode updateEntity(String entityName, JSONObject json) {
 		entityName = DBReference.convertToDBFormat(entityName);
 		String[] attr = DBReference.editableValues.get(entityName);
 		String[] pKeys = DBReference.primaryKeys.get(entityName);
 		
 		try {
 			int numAttr = attr.length;
 			String[] values = new String[attr.length];
 			for(int i=0;i<numAttr;i++) {
 				try {
 					String val = json.getString(attr[i]);
 					if("string".equals(getAttrType(attr[i]))){
 						val = Utils.surroundAndSanitize(val);
 	            	}
 					values[i] = dbAttrNoPrefix(attr[i]) + "=" + val;
 				} catch (JSONException e) {
 					values[i] = dbAttrNoPrefix(attr[i]) + " = NULL ";
 				}
 			}
 			String[] keys = new String[attr.length];
 			for(int i=0;i<pKeys.length;i++) {
 				String val = json.getString(pKeys[i]);
 				if("string".equals(getAttrType(pKeys[i]))){
 					val = Utils.surroundAndSanitize(val);
             	}
 				keys[i] = dbAttrNoPrefix(pKeys[i]) + "=" + val;
 			}
 			return ExplorerModel.updateEntity(entityName, values, keys);
 		} catch(JSONException ex) {
 			return SystemCode.FAILURE;
 		}
 	}
 	
 	/**
 	 * Converts a Human readable attribute to the database format
 	 * and appends the best guess for the belonging table.
 	 * 
 	 * @param orig the original attribute name
 	 * @return The attribute database-isized
 	 * 
 	 * i.e.
 	 * <pre>
 	 * Steam ID ->player.steamId
 	 * </pre>
 	 */
 	public static String convertToDbAttr(String orig) {
 		//TODO:Possible clean up
 		HashMap<String, String> values = new HashMap<String, String>();
 		values.put("Steam ID", "player.steamId");
 		values.put("Persona Name", "player.personaName");
 		values.put("Profile URL", "player.profileUrl");
 		values.put("Real Name", "player.realName");
 		values.put("Application ID", "application.appId");
 		values.put("Date Joined", "player.timeCreated");
 		values.put("Steam ID #1", "friend.steamId1");
 		values.put("Steam ID #2", "friend.steamId2");
 		values.put("Application Name", "application.appName");
 		values.put("Achievement Name", "achievement.achievementName");
 		
 		return values.get(orig);
 	}
 	
 	/**
 	 * Converts a Human readable attribute to the database format
 	 * and does NOT append the best guess for the belonging table.
 	 * 
 	 * @param orig the original attribute name
 	 * @return The attribute database-isized
 	 * 
 	 * i.e.
 	 * <pre>
 	 * Steam ID -> steamId
 	 * </pre>
 	 */
 	public static String dbAttrNoPrefix(String orig) {
 		//Todo: Possibly Clean up
 		HashMap<String, String> values = new HashMap<String, String>();
 		values.put("Steam ID", "steamId");
 		values.put("Persona Name", "personaName");
 		values.put("Profile URL", "profileUrl");
 		values.put("Real Name", "realName");
 		values.put("Application ID", "appId");
 		values.put("Date Joined", "timeCreated");
 		values.put("Steam ID #1", "steamId1");
 		values.put("Steam ID #2", "steamId2");
 		values.put("Application Name", "appName");
 		values.put("Achievement Name", "achievementName");
 		
 		return values.get(orig);
 	}
 	
 	/**
 	 * A lovely, hardcoded list of the various attributes
 	 * 
 	 * @param orig the original attribute name
 	 * @return The type of that attribute as a string [string,long]
 	 */
 	public static String getAttrType(String orig) {
 		//Todo: Possibly Clean up
 		HashMap<String, String> values = new HashMap<String, String>();
 		values.put("Steam ID", "long");
 		values.put("Persona Name", "string");
 		values.put("Profile URL", "string");
 		values.put("Real Name", "string");
 		values.put("Application ID", "long");
 		values.put("Date Joined", "string"); //treat it like a string, stored in db as date
 		values.put("Steam ID #1", "long");
 		values.put("Steam ID #2", "long");
 		values.put("Application Name", "string");
 		values.put("Achievement Name", "string");
 		
 		return values.get(orig);
 	}
 	
 	/**
 	 * Gets the current table
 	 * @return the current table
 	 */
 	public String getCurrentTable() {
 		return currentTable;
 	}
 }
