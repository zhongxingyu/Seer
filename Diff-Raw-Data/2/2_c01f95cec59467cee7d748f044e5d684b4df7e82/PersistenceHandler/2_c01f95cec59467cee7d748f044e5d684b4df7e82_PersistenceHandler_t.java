 package com.game.fickapets;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.Vector;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Element;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 
 public class PersistenceHandler {
 	
 	private static final String PET_FILE = "petAttributesFile";
 	private static final String USER_FILE = "userAttributesFile";
 	private static final String CACHE_FILE = "cacheStateFile";
 	private static final String BATTLE_FILE = "battleStateFile";
 
 	/* key values for PET_FILE */
 	private static final String HEALTH_KEY = "health";
 	private static final String HUNGER_KEY = "hunger";
 	private static final String AWAKE_KEY = "awake";
 	private static final String STRENGTH_KEY = "strength";
 	private static final String TIREDNESS_KEY = "tirednes";
 	private static final String LASTUPDATE_KEY = "lastUpdate";
 	private static final String DEFAULTSET_KEY = "defaultsSet";
 	
 	/* key values for USER_FILE */
 	private static final String ACCESS_TOKEN_KEY = "facebookAccessToken";
 	private static final String ACCESS_EXPIRATION_KEY = "facebookAccessExpires";
 	private static final String COINS_KEY = "coins";
 	private static final String INVENTORY_KEY = "inventory";
 	
 	/* key values for CACHE_FILE */
 	private static final String FILE_QUEUE_KEY = "filesOnDisk";
 	private static final String BYTES_ON_DISK_KEY = "bytesOnDisk";
 	
 	/* key values for battle JSONObject */
 	public static final String MY_MOVE = "myMove";
 	public static final String BATTLE_ID = "battleId";
 	public static final String OPPONENT = "opponentName";
 	public static final String MY_ID = "myFacebookId";
 	public static final String OPPONENT_ID = "opponentFacebookId";
 	public static final String OPPONENT_HEALTH = "opponentBattleHealth";
 	public static final String MY_HEALTH = "myBattleHealth";
 
 	private static Attributes getAttributesFromStoredState (SharedPreferences petState) {
 		Attributes atts = new Attributes ();
 		atts.health = petState.getFloat(HEALTH_KEY, 0);
 		atts.hunger = petState.getFloat(HUNGER_KEY, 0);
 		atts.isAwake = petState.getBoolean(AWAKE_KEY, true);
 		atts.strength = petState.getFloat(STRENGTH_KEY, 0);
 		atts.tiredness = petState.getFloat(TIREDNESS_KEY, 0);
 		atts.lastUpdate = petState.getLong(LASTUPDATE_KEY, 0);
 		return atts;
 	}
 	
 
 	
 	
 	/* pull all defaults from an xml doc in res/raw */
 	private static Attributes getAttributesFromDefaults (Context context) {
 		Attributes atts = new Attributes ();
 		
 	
 		Element pet = XMLUtils.getDocumentElement(context.getResources().openRawResource(R.raw.pet_defaults));
 		atts.health = Double.valueOf(XMLUtils.getChildElementTextByTagName(pet, "health"));
 		atts.hunger = Double.valueOf(XMLUtils.getChildElementTextByTagName(pet, "hunger"));
 		atts.strength = Double.valueOf(XMLUtils.getChildElementTextByTagName(pet, "strength"));
 		if (Integer.valueOf(XMLUtils.getChildElementTextByTagName(pet, "awake")) != 0) {
 			atts.isAwake = true;
 		} else {
 			atts.isAwake = false;
 		}
 		/* sleepTime tells us what time we'd like the pet to need to sleep when it's first initialized */
 		atts.tiredness = Tiredness.getInitialTiredness(Double.valueOf(XMLUtils.getChildElementTextByTagName(pet, "sleepTime")));
 		atts.lastUpdate = Calendar.getInstance(TimeZone.getDefault ()).getTimeInMillis ();
 		return atts;
 	}
 	/* set the pet's values.  If it's the first time running, loads values from the default xml file, 
 	 * otherwise they're loaded from SharedPreferences
 	 */
 	public static Pet buildPet (Context context) {
 		Attributes atts;
 		Pet pet;
 		SharedPreferences petState = context.getSharedPreferences(PET_FILE, 0);
 
 		if (petState.getBoolean(DEFAULTSET_KEY, false)) {
 			atts = getAttributesFromStoredState (petState);
 			
 		} else {
 			atts = getAttributesFromDefaults (context);
 		}
 		pet = new Pet (atts);
 		return pet;
 	}
 	
 	public static Pet reset (Context context) {
 		SharedPreferences petState = context.getSharedPreferences (PET_FILE, 0);
 		SharedPreferences.Editor editor = petState.edit ();
 		editor.putBoolean (DEFAULTSET_KEY, false);
 		editor.commit ();
 
 		return buildPet (context);
 		
 	}
 	
 	public static void saveState(Context context, Pet pet) {
 		Attributes atts = pet.getAttributes(false);
 		SharedPreferences petState = context.getSharedPreferences(PET_FILE, 0);
 		SharedPreferences.Editor editor = petState.edit();
 		editor.putFloat(HEALTH_KEY, (float) atts.health);
 		editor.putFloat(HUNGER_KEY, (float) atts.hunger);
 		editor.putBoolean(AWAKE_KEY, atts.isAwake);
 		editor.putFloat(STRENGTH_KEY, (float) atts.strength);
 		editor.putFloat(TIREDNESS_KEY, (float) atts.tiredness);
 		editor.putLong(LASTUPDATE_KEY, atts.lastUpdate);
 		editor.putBoolean(DEFAULTSET_KEY, true);
 		editor.commit();
 	}
 	
 	public static void saveState(Context context, User user) {
 		SharedPreferences userState = context.getSharedPreferences(USER_FILE, 0);
 		SharedPreferences.Editor editor = userState.edit();
 		editor.putInt(COINS_KEY, user.getCoins());
 		editor.putString(INVENTORY_KEY, encodeInventory(user.getInventory()));
 		editor.commit();
 	}
 	
 	/* saves everything in SharedPreferences which is android's persistent key value store */
 	public static void saveState (Context context, Pet pet, User user) {
 		saveState(context, pet);
 		saveState(context, user);
 	}
 	
 	private static String encodeInventory(List<Item> inventory) {
 		System.out.println(inventory);
 		StringBuilder sb = new StringBuilder();
 		for (Item item : inventory) {
 			sb.append(item.getId());
 			sb.append(",");
 		}
 		if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
 		System.out.println(sb.toString());
 		return sb.toString();
 	}
 	
 	private static List<Item> decodeInventory(Context context, String text) {
 		String[] ids = text.split(",");
 		List<Item> items = new ArrayList<Item>();
 		for (String id : ids) {
 			/* don't want to add anything for empty strings */
 			if (id.compareTo("") != 0) {
 				items.add(ItemManager.getItem(context, id));
 			}
 		}
 		return items;
 	}
 	
 	public static User buildUser(Context context) {
 		SharedPreferences userState = context.getSharedPreferences(USER_FILE, 0);
 
 		if (userState.getInt(COINS_KEY, -1) == -1) {
 			return new User();
 		} else {
 			int coins = userState.getInt(COINS_KEY, 0);
 			List<Item> inventory = decodeInventory(context, userState.getString(INVENTORY_KEY, ""));
 			return new User(coins, inventory);
 		}
 	}
 	
 	public static String facebookAccessToken (Context context) {
 		SharedPreferences facebookPrefs = context.getSharedPreferences(USER_FILE, 0);
 		return facebookPrefs.getString(ACCESS_TOKEN_KEY, null);
 	}
 	public static long facebookTokenExpiration (Context context) {
 		SharedPreferences facebookPrefs = context.getSharedPreferences(USER_FILE, 0);
 		return facebookPrefs.getLong(ACCESS_EXPIRATION_KEY, 0);
 	}
 	
 	public static void saveFacebookAccess(Context context, String accessToken, long accessExpires) {
 		SharedPreferences facebookPrefs = context.getSharedPreferences(USER_FILE, 0);
 		SharedPreferences.Editor editor = facebookPrefs.edit();
 		editor.putString(ACCESS_TOKEN_KEY, accessToken);
 		editor.putLong(ACCESS_EXPIRATION_KEY, accessExpires);
 		editor.commit();
 	}
 	
 	private static String encodeFileArr(Vector<CacheEntry> files) {
 		StringBuilder sb = new StringBuilder();
 		for (CacheEntry entry: files) {
 			String entryStr = entry.name + ":" + entry.bytes.toString() + "|";
 			sb.append(entryStr);
 		}
 		sb = sb.deleteCharAt(sb.length()-1);
 		return sb.toString();
 	}
 	
 	private static Vector<CacheEntry> decodeFileArr(String filesStr) {
 		Vector<CacheEntry> files = new Vector<CacheEntry>();
 		if (filesStr.equals("")) return files;
 		String[] entries = filesStr.split("|");
 		for (String entryStr : entries) {
 			int delimiterIndex = entryStr.indexOf(":");
 			String filename = entryStr.substring(0, delimiterIndex);
 			int bytes = Integer.valueOf(entryStr.substring(delimiterIndex + 1, entryStr.length()));
 			CacheEntry entry = new CacheEntry(filename, bytes);
 			files.add(entry);
 		}
 		return files;
 	}
 	
 	public static void saveImageCacheState(Context context, int bytesOnDisk, Vector<CacheEntry> files) {
 		SharedPreferences cachePrefs = context.getSharedPreferences(CACHE_FILE, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = cachePrefs.edit();
 
 		String filesString = encodeFileArr(files);
 		editor.putString(FILE_QUEUE_KEY, filesString);
 		editor.putInt(BYTES_ON_DISK_KEY, bytesOnDisk);
 		editor.commit();
 	}
 	
 	
 	public static Object[] getImageViewHandlerAtts(Context context) {
 		SharedPreferences cachePrefs = context.getSharedPreferences(CACHE_FILE, Context.MODE_PRIVATE);
 		/* decodeFileArr never returns null, at the least returns empty vector */
 		Vector<CacheEntry> files = decodeFileArr(cachePrefs.getString(FILE_QUEUE_KEY, ""));
 		Integer bytesOnDisk = cachePrefs.getInt(BYTES_ON_DISK_KEY, 0);
 		Object[] attArray = new Object[2];
 		attArray[0] = files;
 		attArray[1] = bytesOnDisk;
 		return attArray;
 	}
 	
 	public static String getFileAsString(Context context, String filename) throws IOException {
 		File file = context.getFileStreamPath(filename);
 		if (!file.exists()) return null;
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 		char[] bytes = new char[1024];
 		StringBuilder sb = new StringBuilder();
 		int bytesRead = br.read(bytes, 0, bytes.length);
 		while (bytesRead != -1) {
 			sb.append(bytes, 0, bytesRead);
 			bytesRead = br.read(bytes, 0, bytes.length);
 		}
 		return sb.toString();
 	}
 	
 	public static JSONArray getBattles(Context context) {
 		try {
 			String jsonString = getFileAsString(context, BATTLE_FILE);
 			if (jsonString == null) {
 				return new JSONArray();
 			} else {
 				return new JSONArray(jsonString);
 			}
 		} catch(Exception ex) {
 			return null;
 		}
 	}
 	/* returns index with bid if it exists in array.  Otherwise returns -1 */
 	private static int getIndexWithBattle(String bid, JSONArray battles) {
 		try {
 			for (int i = 0; i < battles.length(); i++) {
 				JSONObject battle = battles.getJSONObject(i);
				if (battle.getString(BATTLE_ID).equals(bid)) {
 					return i;
 				}
 			}
 		} catch(Exception ex) {
 			System.out.println("Json battle file is invalid");
 		}
 		return -1;
 	}
 	
 	private static void writeStringToFile(Context context, String filename, String jsonArrStr) {
 		try {
 			File file = context.getFileStreamPath(filename);
 			if (file.exists()) file.delete();
 			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
 			fos.write(jsonArrStr.getBytes());
 			fos.flush();
 			fos.close();
 		} catch(IOException ex) {
 			System.out.println("Failed to write to json battle file");
 			ex.printStackTrace();
 		}
 		
 	}
 	
 	public static void saveBattle(Context context, Bundle bundle) {
 		try {
 			String bid = bundle.getString(BattleActivity.BATTLE_ID_KEY);
 			String opponentName = bundle.getString(BattleActivity.OPPONENT_NAME_KEY);
 			String myMove = bundle.getString(BattleActivity.MY_MOVE_KEY);
 			String myId = bundle.getString(BattleActivity.MY_ID_KEY);
 			String opponentId = bundle.getString(BattleActivity.OPPONENT_ID_KEY);
 			Integer opponentBattleHealth = bundle.getInt(BattleActivity.OPPONENT_HEALTH_KEY);
 			Integer myBattleHealth = bundle.getInt(BattleActivity.MY_HEALTH_KEY);
 			
 			JSONArray battles = getBattles(context);
 			int index = getIndexWithBattle(bid, battles);
 			JSONObject battle;
 			if (index >= 0) {
 				battle = battles.getJSONObject(index);
 			} else {
 				battle = new JSONObject();
 			}
 			battle.put(BATTLE_ID, bid);
 			battle.put(OPPONENT, opponentName);
 			battle.put(MY_MOVE, myMove);
 			battle.put(MY_ID, myId);
 			battle.put(OPPONENT_ID, opponentId);
 			battle.put(OPPONENT_HEALTH, opponentBattleHealth);
 			battle.put(MY_HEALTH, myBattleHealth);
 			if (index == -1) {
 				battles.put(battles.length(), battle);
 			} else {
 				battles.put(index, battle);
 			}
 			writeStringToFile(context, BATTLE_FILE, battles.toString());
 		} catch(JSONException ex) {
 			System.out.println("tried to add an invalid value to a json object");
 			ex.printStackTrace();
 		} 
 	}
 	/* can't seem to change size so I'll just copy over to new json array */
 	private static JSONArray removeBattleAtIndex(JSONArray battles, int battleIndex) throws JSONException {
 		JSONArray newArr = new JSONArray();
 		int newArrIndex = 0;
 		for (int i = 0; i < battles.length(); i++) {
 			if (i != battleIndex) {
 				newArr.put(newArrIndex, battles.getJSONObject(i));
 				newArrIndex++;
 			}
 		}
 		return newArr;
 	}
 	
 	public static void removeBattle(Context context, String bid) {
 		try {
 			JSONArray battles = getBattles(context);
 			int battleIndex = getIndexWithBattle(bid, battles);
 			if (battleIndex >= 0) {
 				battles = removeBattleAtIndex(battles, battleIndex);
 			}
 			writeStringToFile(context, BATTLE_FILE, battles.toString());
 		} catch (Exception ex) {
 			System.out.println("failed to remove battle");
 			ex.printStackTrace();
 		}
 	}
 	
 	
 }
