 /**
  * The explorer controller is used to interface between the model and the view.
  * The controller is in charge of checking that all input taken from the view
  * makes sense, and converting information from the model into a type preferred
  * by the view.
  * 
  * @author Andrew Hollenbach <ahollenbach>
  */
 
 package steam.dbexplorer.controller;
 
 import java.util.HashMap;
 import steam.dbexplorer.model.ExplorerModel;
 
 public class ExplorerController {
 	
 	//TODO populate tableLabels with proper values
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
 	/** 
 	 * A string value of the last entity type to be fetched. Might be 
 	 * deprecated.
 	 */
 	private String currentTable;
 	
 	public ExplorerController() {
 		tableLabels = new HashMap<String, String[]>();
		String[][] labels = {{"Application ID", "Achievement Name"},
							 {"Application ID", "Application Name"},
							 {"Steam ID #1", "Steam ID #2"},
							 {"Application ID", "Achievement Name", "Steam ID"},
							 {"Application ID", "Steam ID"},
							 {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"}};
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
 	public boolean createEntry(String entityName, String[] values) {
 		//TODO this is not actually working code.
 		//Has to verify values are parsable i.e. values[0] = "fdfjk234444444"
 		Object[] valuesConvertedToObjects = values;
 		ExplorerModel.createEntity(entityName,valuesConvertedToObjects);
 		return false;
 	}
 
 	/**
 	 * Gets the attributes for a given table
 	 * 
 	 * @param tableName the table to fetch the attributes of
 	 * @return a list of attributes associated with the given table
 	 */
 	public String[] getAttr(String tableName) {
 		String[] attributes = {"steamID",
 							   "personaName",
 							   "profileURL",
 							   "lastLogoff",
 							   "realName",
 							   "timeCreated"};
 		return attributes;
 	}
 	
 	/* TODO make these functions
 	public boolean delete*(String entityName, String[] values) {
 		return false;
 	}
 	
 	public boolean update*(String entityName, String[] values) {
 		return false;
 	} */
 }
