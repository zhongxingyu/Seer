 package client.data;
 
 
 import java.util.Vector;
 
 import net.rim.device.api.io.FileNotFoundException;
 import client.Common;
 import data.bonus.Bonus;
 import data.me.json.JSONException;
 import data.me.json.JSONObject;
 
 public class GameData extends data.GameData {
 
 	private Vector allList; // lits of
 	// all/remaining
 	// contestants
 	public static String filePath = "file:///SDCard/res/data/Settings.dat";
 	
 	public GameData(int numContestants) {
 		super(numContestants);
 		
 		allList = allContestants;
 	}
 	
 	/**
 	 * initGameData reads in a data file and builds a GameData object out
 	 * of it, returning it to the user.
 	 * 
 	 * @param inputFile   file to be read in
 	 * @return GameData object made out of file or null if season not created
 	 * 
 	 */
 	public static boolean initGameData() {
 		System.out.println("initGameData started");
 		JSONObject json;
 		
 		try {
			json = JSONUtils.readFile(filePath,true);
 			if(json==null){
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to read file.");	
 			return false;
 		}
 		
 		System.out.println("json read fine");
 		
 		try {
 			if (!json.getBoolean(KEY_SEASON_STARTED)){
 				Common.displayErrorMsg("Season has not started.");
 				return false;
 			}
 			currentGame = new GameData(json.getInt(KEY_NUM_CONTEST));
 		} catch (JSONException e) {
 			System.out.println("json exception");
 			return false;
 		}
 		
 		System.out.println("creating gamedata from json");
 		
 		try {
 			GameData.getCurrentGame().fromJSONObject(json);
 		} catch (JSONException e) {
 			Common.displayErrorMsg("Corrupted file.");
 			System.out.println("json exception");
 			return false;
 		}
 		
 		System.out.println("json read");
 		Bonus.initBonus();//initalize bonus
 		
 		System.out.println("initGameData finished");
 		return true;
 	}
 
 	public void fromJSONObject(JSONObject obj) throws JSONException {
 		super.fromJSONObject(obj);
 		allList = allContestants;
 		
 	}
 	
 	public void writeData() {
 		try {
 			JSONUtils.writeData();
 		} catch (JSONException e) {
 			System.out.println("***write data json exception");
 			e.printStackTrace();
 		}
 		
 	}
 
 }
