 package se.chalmers.dryleafsoftware.androidrally.IO;
 
 import android.content.SharedPreferences;
 
 /**
  * Helps with saving and loading from the application's private storage.
  * 
  * @author 
  *
  */
 public class IOHandler {
 		
 	/**
 	 * Path to the directory where data needed for the server part is stored.
 	 */
 	public static final String SERVER_DATA = "server/";
 	/**
 	 * Path to the directory where data needed for the client part is stored.
 	 */
 	public static final String CLIENT_DATA = "saves/";
 	
 	private static final String PRIVATE_DATA = "data/settings";
 	private static final String ID_DATA = "data/idData";
 	
 	private static int currentID;
 	
 	private static SharedPreferences prefs;
 		
 	/**
 	 * Set what preferences to save to.
 	 * @param prefs The preferences to save to.
 	 */
 	public static void setPrefs(SharedPreferences prefs) {
 		IOHandler.prefs = prefs;
 	}
 	
 	/**
 	 * Saves the game with the specified ID. 
 	 * @param saveData The data to save.
 	 * @param gameID The game with the specified ID to save.
 	 * @param location The location to store the data. Use static values.
 	 */
 	public static void save(String saveData, int gameID, String location) {		
 		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(location, saveData);
 		System.out.println("(Saving) Location: \"" + location + gameID + "\", " + 
 				"data: \"" + saveData + "\"");
 		if(currentID == gameID) {
 			editor.putInt(PRIVATE_DATA, currentID);
 			System.out.println("(Saving) LastID: \"" + currentID + "\"");
 		}
 		if(!isSaved(gameID)) {
 			editor.putString(ID_DATA, prefs.getString(ID_DATA, "") + ":" + gameID);
 		}
 		System.out.println("Commit: " + editor.commit());
 	}
 	
 	/**
 	 * Checks if the specified game is saved.
 	 * @param gameID The ID of the game to check.
 	 * @return <code>true</code> if the game with the specified game is saved.
 	 */
 	public static boolean isSaved(int gameID) {
 		int[] gameIDs = getGameIDs();
 		for(int i = 0; i < gameIDs.length; i++) {
 			if(gameIDs[i] == gameID) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Gives the next empty ID which can be used to create a new game.
 	 * @return
 	 */
 	public static int getNewID() {
 		int id = prefs.getInt(PRIVATE_DATA, -1);
 		currentID = id -1;
 		System.out.println("New ID: " + currentID);
 		return id - 1;
 	}
 	
 	/**
 	 * Gives all the IDs of the games saved.
 	 * @return An array of all the IDs of all the games a client has saved on the phone.
 	 */
 	public static int[] getGameIDs() {		
 		String data = prefs.getString(ID_DATA, null);
 		if(data == null) {
 			return new int[0];
 		}
 		String idString[] = data.substring(1).split(":");
 		int[] ids = new int[idString.length];
 		for(int i = 0; i < idString.length; i++) {
 			ids[i] = Integer.parseInt(idString[i]);
 			System.out.println("Loaded stored ID: " + ids[i]);
 		}
 		return ids;
 	}
 	
 	/**
 	 * Removes the save for the specified game.
 	 * @param gameID The ID of the game to remove.
 	 * @param location The location to remove from. Use static values.
 	 */
 	public static void remove(int gameID, String location) {
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.remove(location + gameID);
 		StringBuilder sb = new StringBuilder();
 		int[] ids = getGameIDs();
 		for(int i = 0; i < ids.length; i++) {
 			if(ids[i] != gameID) {
 				sb.append(ids[i] + ":");
 			}
 		}
 		editor.putString(ID_DATA, sb.toString());
 		System.out.println("Commit: " + editor.commit());
 	}
 	
 	/**
 	 * Loads the data for the specified game.
 	 * @param gameID The ID of the game to load.
 	 * @param location The location to load from. Use static values.
 	 * @return The data stored at the location.
 	 */
 	public static String load(int gameID, String location) {
 		String data = prefs.getString(location + gameID, null);
 		System.out.println("(Loading) Location: \"" + location + gameID + "\", " + 
 				"data: \"" + data + "\"");
 		return data;
 	}
 }
