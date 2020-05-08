 /**
  * The explorer controller is used to interface between the model and the view.
  * The controller is in charge of checking that all input taken from the view
  * makes sense, and converting information from the model into a type preferred
  * by the view.
  * 
  * @author Andrew Hollenbach <ahollenbach>
  */
 
 package steam.dbexplorer.controller;
 
 import java.io.StringWriter;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import steam.dbexplorer.SystemCode;
 import steam.dbexplorer.dbobject.DBReference;
 import steam.dbexplorer.model.ExplorerModel;
 
 public class ExplorerController {
 	private static HashMap<String, String[]> tableLabels;
 	
 	public static final String[] tableNames = {"Achievements", 
 										  	   "Applications", 
 										  	   "Friends", 
 										  	   "Owned Achievements", 
 										  	   "Owned Applications", 
 										  	   "Players" };
 	
 	public static final String[] supportedClauses = {"where",
 													 "sort by"};
 	
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
 		// populate tableLabels
 		tableLabels = new HashMap<String, String[]>();
 		String[][] labels = {{"Application ID", "Application Name", "Achievement Name"}, //Achievements
 							 {"Application ID", "Application Name"}, //Applications
 							 {"Steam ID #1", "Steam ID #2"}, //Friends
 							 {"Application ID", "Application Name", "Steam ID", "Persona Name", "Achievement Name"}, //Owned achievements
 							 {"Application ID", "Application Name", "Steam ID", "Persona Name"}, //Owned applications
 							 {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"}}; //player
 		for(int i=0;i<tableNames.length;i++) {
 			tableLabels.put(tableNames[i], labels[i]);
 		}
 		
 		
 	}
 	
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
 			data = ExplorerModel.retrieveOwnedAchievements(76561198049281288L, options);
 		} else if(tableName.equals("Owned Applications")) {
 			data = ExplorerModel.retrieveOwnedApplications(76561198049281288L, options);
 		} else if(tableName.equals("Players")) {
 			data = ExplorerModel.retrievePlayers(options);
 		}
 		return data;
 	}
 
 	public String[] getLabels(String tableName) {
 		String[] labels = tableLabels.get(tableName);
 		return labels;
 	}
 	
 	/**
 	 * Creates an entry using the given string values. Verifies if the 
 	 * values are correct. If any of the values are not parsable, returns
 	 * false. 
 	 * @param values The values to insert
 	 * @param entityName The name of the entity to create.
 	 * @return
 	 */
 	public SystemCode createEntry(String entityName, String[] values) {
 		int numAttr = tableLabels.get(entityName).length;
 		if(numAttr > values.length) {
 			String[] valsWithNullStrings = new String[numAttr];
 			for(int i=0;i<values.length;i++) {
 				valsWithNullStrings[i] = values[i]; 
 			}
 		}
 		return ExplorerModel.createEntity(entityName,values);
 	}
 	
 	public SystemCode deleteEntity(String entityName, JSONObject json) {
 		/*
 		try {
 			String[] values = new String[json.length()];
 			String[] names = JSONObject.getNames(json);
 			
 			for(int i=0; i<json.length();i++) {
 				String val = json.getString(names[i]);
 				values[i] = convertToDbAttr(names[i]) + "=" + val;
 			}
 			return ExplorerModel.deleteEntity(entityName, values);
 		} catch (JSONException e) {
 		}
 		return SystemCode.FAILURE;*/
 		//entityName = entityName.substring(0, entityName.length()-1); //remove s
 		//entityName = entityName.replace(" ", ""); //remove space
 		if(entityName == null) {
 		} else if(entityName.equals("Achievements")) {
 			return deleteEntity(entityName,DBReference.AchievementDisp,json,DBReference.AchievementTables);
 		} else if(entityName.equals("Applications")) {
 			return deleteEntity(entityName,DBReference.ApplicationDisp,json,DBReference.ApplicationTables);
 		} else if(entityName.equals("Friends")) {
 			return deleteEntity(entityName,DBReference.FriendDisp,json,DBReference.FriendTables);
 		} else if(entityName.equals("Owned Achievements")) {
 			return deleteEntity(entityName,DBReference.OwnedAchievementDisp,json,DBReference.OwnedAchievementTables);
 		} else if(entityName.equals("Owned Applications")) {
 			return deleteEntity(entityName,DBReference.OwnedApplicationDisp,json,DBReference.OwnedApplicationTables);
 		} else if(entityName.equals("Players")) {
 			return deleteEntity(entityName,DBReference.PlayerDisp,json,DBReference.PlayerTables);
 		}
 		return SystemCode.FAILURE;
 	}
 
 	/**
 	 * Sends a command to the explorer model to delete the entity. Ensures
 	 * the values are in the proper order.
 	 * 
 	 * @param json A JSONObject containing all the achievement values
 	 * @return Whether the operation was successful or not
 	 */
 	public SystemCode deleteEntity(String entityName, String[] attr, JSONObject json, String usingTables) {
 		try {
 			int numAttr = attr.length;
 			String[] values = new String[attr.length];
 			for(int i=0;i<numAttr;i++) {
 				values[i] = convertToDbAttr(attr[i]) + "=" + json.getString(attr[i]);
 			}
 			return ExplorerModel.deleteEntity(entityName, values,usingTables);
 		} catch(JSONException ex) {
 			return SystemCode.FAILURE;
 		}
 	}
 	
 	public SystemCode updateEntity(String entityName, JSONObject json) {
 		if(entityName == null) {
 		} else if(entityName.equals("Achievements")) {
 			return updateEntity(entityName,DBReference.AchievementDisp,json);
 		} else if(entityName.equals("Applications")) {
 			return updateEntity(entityName,DBReference.ApplicationDisp,json);
 		} else if(entityName.equals("Friends")) {
 			return updateEntity(entityName,DBReference.FriendDisp,json);
 		} else if(entityName.equals("Owned Achievements")) {
 			return updateEntity(entityName,DBReference.OwnedAchievementDisp,json);
 		} else if(entityName.equals("Owned Applications")) {
 			return updateEntity(entityName,DBReference.OwnedApplicationDisp,json);
 		} else if(entityName.equals("Players")) {
 			return updateEntity(entityName,DBReference.PlayerDisp,json);
 		}
 		return SystemCode.FAILURE;
 	}
 	
 	/**
 	 * Sends a command to the explorer model to delete the entity. Ensures
 	 * the values are in the proper order.
 	 * 
 	 * @param json A JSONObject containing all the achievement values
 	 * @return Whether the operation was successful or not
 	 */
 	public SystemCode updateEntity(String entityName, String[] attr, JSONObject json) {
 		try {
 			int numAttr = attr.length;
 			String[] values = new String[attr.length];
 			for(int i=0;i<numAttr;i++) {
 				values[i] = json.getString(attr[i]);
 			}
 			return ExplorerModel.updateEntity(entityName, values);
 		} catch(JSONException ex) {
 			return SystemCode.FAILURE;
 		}
 	}
 	
 	public static String convertToDbAttr(String orig) {
 		//SO YUCKY GET RID OF THIS
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
 	
 	public static String getAttrType(String orig) {
 		//SO YUCKY GET RID OF THIS TOO
 		HashMap<String, String> values = new HashMap<String, String>();
 		values.put("Steam ID", "long");
 		values.put("Persona Name", "string");
 		values.put("Profile URL", "string");
 		values.put("Real Name", "string");
 		values.put("Application ID", "long");
 		values.put("Date Joined", "time");
 		values.put("Steam ID #1", "long");
 		values.put("Steam ID #2", "long");
 		values.put("Application Name", "string");
 		values.put("Achievement Name", "string");
 		
 		return values.get(orig);
 	}
 }
