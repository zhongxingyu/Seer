 /**
  * This is a reference class to the DDL. Every attribute is in the order that
  * it is in the database. A Disp ending means that the attributes are in the
  * format they are displayed in to the user.
  * 
  * @author Andrew Hollenbach <ahollenbach>
  */
 
 package steam.dbexplorer.dbobject;
 
 import java.util.HashMap;
 
 public class DBReference {
 	public static HashMap<String, String[]> displayNames = new HashMap<String, String[]>();
 	/** Hopefully this pointless hashmap won't be around for long */
 	public static HashMap<String, String[]> editableValues = new HashMap<String, String[]>();
 	public static HashMap<String, String[]> primaryKeys = new HashMap<String, String[]>();
 	public static HashMap<String, String> usingTables = new HashMap<String, String>();
 	
 	static {
 		displayNames.put("Player", new String[] {"Steam ID", "Persona Name", "Profile URL", "Real Name", "Date Joined"});
 		displayNames.put("Friend", new String[] {"Steam ID #1", "Steam ID #2"});
 		displayNames.put("Application", new String[] {"Application ID", "Application Name"});
 		displayNames.put("Achievement", new String[] {"Application ID", "Achievement Name"});
 		displayNames.put("OwnedAchievement", new String[] {"Application ID", "Achievement Name", "Steam ID"});
 		displayNames.put("OwnedApplication", new String[] {"Application ID", "Steam ID"});
 		
 		editableValues.put("Player", new String[] {"Persona Name", "Profile URL", "Real Name", "Date Joined"});
 		editableValues.put("Friend", new String[] {});
 		editableValues.put("Application", new String[] {"Application Name"});
 		editableValues.put("Achievement", new String[] {});
 		editableValues.put("OwnedAchievement", new String[] {});
 		editableValues.put("OwnedApplication", new String[] {});
 		
 		primaryKeys.put("Player", new String[] {"Steam ID"});
 		primaryKeys.put("Friend", new String[] {"Steam ID #1", "Steam ID #2"});
 		primaryKeys.put("Application", new String[] {"Application ID"});
 		primaryKeys.put("Achievement", new String[] {"Application ID", "Achievement Name"});
 		primaryKeys.put("OwnedAchievement", new String[] {"Application ID", "Achievement Name", "Steam ID"});
 		primaryKeys.put("OwnedApplication", new String[] {"Application ID", "Steam ID"});
 		
 		usingTables.put("Player", "");
 		usingTables.put("Friend", "Player");
 		usingTables.put("Application", "");
 		usingTables.put("Achievement", "Application");
		//usingTables.put("OwnedAchievement", "Application,Achievement,Player");
 		usingTables.put("OwnedAchievement", "Application,Player");
 		usingTables.put("OwnedApplication", "Application,Player");
 	}
 	
 	public static Boolean isPK(String tableName,String attrName) {
 		if(tableName == null) {
 			return false;
 		} else {
 			tableName = tableName.substring(0, tableName.length()-1); //remove s
 			tableName = tableName.replace(" ", ""); //remove spaces 
 			return contains(primaryKeys.get(tableName), attrName);
 		}
 	}
 	
 	private static Boolean contains(String[] values, String value) {
 		for(int i=0;i<values.length;i++) {
 			if(value.equals(values[i])) return true;
 		}
 		return false;
 	}
 }
