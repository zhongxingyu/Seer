 package net.hexid.hexbot.bot;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import net.hexid.Utils;
 
 public class Bots {
 	private static TreeMap<String, HashMap<String, String>> bots = new TreeMap<>();
 
 	/**
 	 * Add a new bot to the list of usable bots
 	 * @param shortName
 	 * @param longName
 	 * @param cliClassPath
 	 * @param guiClassPath
 	 * @param fileName
 	 */
 	public static void addBot(String sName, String lName, String cli, String gui, String fName) {
 		HashMap<String, String> bot = new HashMap<>();
 		bot.put("longName", lName);
 		bot.put("cliClassPath", cli);
 		bot.put("guiClassPath", gui);
 		bot.put("fileName", fName);
 		bots.put(sName.toLowerCase(), bot);
 	}
 
 	/**
	 * Remove all bots whose file does not exist
 	 */
 	public static void removeInvalidBots() {
 		Iterator<Entry<String, HashMap<String, String>>> iter = bots.entrySet().iterator();
 		while(iter.hasNext()) {
 			if(!(new File(getBotFile(iter.next().getKey()))).exists())
 				iter.remove();
 		}
 	}
 
 	/**
 	 * @return TreeMap of botNames linked with HashMaps of botData
 	 */
 	public static TreeMap<String, HashMap<String, String>> getBots() {
 		return bots;
 	}
 	
 	/**
 	 * @return Array of all bot names (keys to map)
 	 */
 	public static String[] botNames() {
 		return bots.keySet().toArray(new String[0]);
 	}
 	
 	/**
 	 * @param botName
 	 * @return HashMap with bot's data
 	 */
 	public static HashMap<String, String> getBot(String botName) {
 		return bots.get(botName.toLowerCase()); // get the data associated with a bot
 	}
 
 	/**
 	 * @param botName
 	 * @return true if the bot has data
 	 */
 	public static boolean hasBot(String botName) {
 		return bots.containsKey(botName.toLowerCase());
 	}
 
 	/**
 	 * @param botName
 	 * @return Full name of the bot; null if bot doesn't exist
 	 */
 	public static String getBotLongName(String botName) {
 		return getFromBot(botName, "longName");
 	}
 	/**
 	 * @param botName
 	 * @return Class path for CLI bot; null if bot doesn't exist
 	 */
 	public static String getBotCliClassPath(String botName) {
 		return getFromBot(botName, "cliClassPath");
 	}
 	/**
 	 * @param botName
 	 * @return Class path for GUI bot; null if bot doesn't exist
 	 */
 	public static String getBotGuiClassPath(String botName) {
 		return getFromBot(botName, "guiClassPath");
 	}
 	/**
 	 * @param botName
 	 * @return Bot file name; null if bot doesn't exist
 	 */
 	public static String getBotFileName(String botName) {
 		return getFromBot(botName, "fileName");
 	}
 
 	/**
 	 * Get the specified bot's value at a given key
 	 * @param botName
 	 * @param key Key to query for in bot's HashMap
 	 * @return botData; null if nonexistant
 	 */
 	public static String getFromBot(String botName, String key) {
 		return (hasBot(botName)) ? getBot(botName).get(key) : null;
 	}
 
 	public static String getBotEnvPath(String oldEnvPath) {
 		String dir = Utils.getPWD().getPath();
 		String sysSpecific = System.getProperty("os.name").toLowerCase();
 		String casperBin = "bin";
 
 		if(sysSpecific.contains("win")) {
 			sysSpecific = "windows";
 			casperBin = "batchbin";
 		} else if(sysSpecific.contains("mac")) {
 			sysSpecific = "macosx";
 		} else {
 			sysSpecific += ("-" + System.getProperty("os.arch"));
 		}
 
 		String phantom = Utils.joinFile(dir, "libs", "phantomjs");
 		String phantomBin = Utils.joinFile(phantom, "bin");
 		String phantomOS = Utils.joinFile(phantomBin, sysSpecific);
 		String casper = Utils.joinFile(dir, "libs", "casperjs", casperBin);
 		return Utils.join(File.pathSeparator, oldEnvPath, phantomOS, phantomBin, phantom, casper);
 	}
 
 	public static String getBotFile(String botName) {
 		return Utils.joinFile(Utils.getPWD().getPath(), "bots", getBotFileName(botName));
 	}
 	public static String getBotFile(Bot bot) {
 		return getBotFile(bot.getShortName());
 	}
 
 	public static String getAvailableBots() {
 		return java.util.Arrays.toString(bots.keySet().toArray());
 	}
 }
