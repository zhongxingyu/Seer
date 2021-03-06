 package com.fullwall.Citizens.Utils;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 
 import com.fullwall.Citizens.NPCManager;
 import com.fullwall.Citizens.PropertyHandler;
 import com.fullwall.Citizens.Citizens;
 
 public class PropertyPool {
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static final PropertyHandler settings = new PropertyHandler(
 			"plugins/Citizens/Citizens.settings");
 	public static final PropertyHandler texts = new PropertyHandler(
 			"plugins/Citizens/Basic NPCs/Citizens.texts");
 	public static final PropertyHandler locations = new PropertyHandler(
 			"plugins/Citizens/Basic NPCs/Citizens.locations");
 	public static final PropertyHandler colours = new PropertyHandler(
 			"plugins/Citizens/Basic NPCs/Citizens.colours");
 	public static final PropertyHandler items = new PropertyHandler(
 			"plugins/Citizens/Basic NPCs/Citizens.items");
 
 	public static void saveLocation(String name, Location loc, int UID) {
 		String location = loc.getWorld().getName() + "," + loc.getX() + ","
 				+ loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + ","
 				+ loc.getPitch();
 		locations.setString(""+UID, location);
 		if (!locations.getString("list").contains(""+UID+"_"+name))
 			locations.setString("list", locations.getString("list") + ""+UID+"_"+name
 					+ ",");
 	}
 
 	public static void saveItems(int UID, ArrayList<Integer> items2) {
 		String toSave = "";
 		for (Integer i : items2) {
 			toSave += "" + i + ",";
 		}
 		items.setString(""+UID, toSave);
 	}
 
 	public static void saveColour(int UID, String colour) {
 		colours.setString(""+UID, "" + colour);
 	}
 
 	public static void saveText(int UID, ArrayList<String> text) {
 		String adding = "";
 		if(text != null){
 			for (String string : text) {
 				adding += string + ";";
 			}
 		}
 		texts.setString(""+UID, adding);
 	}
 	
 	public static int getNewNpcID(){
 		if (locations.getString("currentID").isEmpty()){
 			locations.setString("currentID", ""+0);
 		}
 		int returnResult = Integer.valueOf(locations.getString("currentID"));
 		locations.setString("currentID", ""+(returnResult+1));
 		return returnResult;
 	}
 
 	public static void getSetText(int UID) {
 		String current = texts.getString(""+UID);
 		if (!current.isEmpty()) {
 			ArrayList<String> text = new ArrayList<String>();
 			for (String string : current.split(";")) {
 				text.add(string);
 			}
 			NPCManager.setBasicNPCText(UID, text);
 		}
 	}
 
 	public static ArrayList<String> getText(int UID) {
 		String current = texts.getString(""+UID);
 		if (!current.isEmpty()) {
 			ArrayList<String> text = new ArrayList<String>();
 			for (String string : current.split(";")) {
 				text.add(string);
 			}
 			return text;
 		} else
 			return null;
 	}
 
 	public static String getColour(int UID) {
 		return colours.getString(""+UID);
 	}
 
 	public void removeFromFiles(String name) {
 		PropertyPool.colours.removeKey(name);
 		PropertyPool.items.removeKey(name);
 		PropertyPool.locations.removeKey(name);
 		PropertyPool.locations.setString("list", PropertyPool.locations
 				.getString("list").replace(name + ",", ""));
 		PropertyPool.texts.removeKey(name);
 		NPCManager.BasicNPCTexts.remove(name);
 	}
 
 	public static String getDefaultText() {
 		String[] split = settings.getString("default-text").split(";");
 		String text;
 		if (split != null) {
 			text = split[new Random(System.currentTimeMillis())
 					.nextInt(split.length)];
 			if (text == null)
 				text = "";
 		} else
 			text = "";
 		return text.replace('&', '');
 	}
 
 	public static ArrayList<Integer> getItemsFromFile(int UID) {
 		ArrayList<Integer> array = new ArrayList<Integer>();
 		String current = items.getString(""+UID);
 		if (current.isEmpty()) {
 			current = "0,0,0,0,0,";
 			items.setString(""+UID, current);
 		}
 		for (String s : current.split(",")) {
 			array.add(Integer.parseInt(s));
 		}
 		return array;
 	}
 	public static Location getLocationFromName(int UID) {
 		String[] values = PropertyPool.locations.getString(""+UID).split(",");
 		if (values.length != 6) { 
 			log.info("gotLocationFromName didn't have 6 values in values variable! Length:"+values.length);
 			return null;
 		}else{
 		Location loc = new Location(Citizens.plugin.getServer().getWorld(
 				values[0]), Double.parseDouble(values[1]),
 				Double.parseDouble(values[2]), Double.parseDouble(values[3]),
 				Float.parseFloat(values[4]), Float.parseFloat(values[5]));
 		return loc;
 		}
 	}
 	
 	public static Location getLocationFromID(int UID) {
 		String[] values = PropertyPool.locations.getString(""+UID).split(",");
 		if (values.length != 6) { 
 			log.info("gotLocationFromName didn't have 6 values in values variable! Length:"+values.length);
 			return null;
 		}else{
 		Location loc = new Location(Citizens.plugin.getServer().getWorld(
 				values[0]), Double.parseDouble(values[1]),
 				Double.parseDouble(values[2]), Double.parseDouble(values[3]),
 				Float.parseFloat(values[4]), Float.parseFloat(values[5]));
 		return loc;
 		}
 	}
 
	public static void changeName(int UID, String changeTo) {
 		//ID's Remain the same, no need for this.
		
 		//ArrayList<String> texts = PropertyPool.getText(UID);
 		//String colour = PropertyPool.getColour(UID);
 		//ArrayList<Integer> items = PropertyPool.getItemsFromFile(UID);
 		//PropertyPool.saveColour(UID, colour);
 		//PropertyPool.saveText(UID, texts);
 		//PropertyPool.saveItems(UID, items);
 	}
 }
