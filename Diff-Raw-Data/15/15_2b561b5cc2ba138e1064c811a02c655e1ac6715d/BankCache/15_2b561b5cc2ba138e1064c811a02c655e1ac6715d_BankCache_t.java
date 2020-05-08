 package org.rsbot.script.util;
 
 import org.rsbot.script.wrappers.RSItem;
 import org.rsbot.util.GlobalConfiguration;
 import org.rsbot.util.IniParser;
 
 import java.io.*;
 import java.util.HashMap;
 
 /**
  * Bank cache class. Used for web.
  *
  * @author Timer
  */
 public class BankCache {
	private final static File cacheFile = new File(GlobalConfiguration.Paths.getBankCache());
 	private final static HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
 
 	/**
 	 * Saves a bank cache for a user.
 	 *
 	 * @param name  The name of the character.
 	 * @param items The array of items in the bank.
 	 */
 	public static void Save(final String name, final RSItem[] items)
 			throws Exception {
 		Load();// For multiple bot instances.
 		FileWriter fw = new FileWriter(cacheFile, false);
 		BufferedWriter bw = new BufferedWriter(fw);
 		IniParser.serialise(data, bw);
 		bw.close();
 	}
 
 	public static void Load() throws Exception {
 		if (cacheFile.exists()) {
 			if (!cacheFile.createNewFile()) {
 				return;
 			}
 		}
 		FileReader fr = new FileReader(cacheFile);
 		BufferedReader br = new BufferedReader(fr);
 		data.clear();
 		data.putAll(IniParser.deserialise(br));
 		br.close();
 	}
 
 	/**
 	 * Checks the bank cache for an item.
 	 *
 	 * @param name Character name.
 	 * @param o    The object to look for.
 	 * @return <tt>true</tt> if the bank cache contains it.
 	 */
 	public static boolean Contains(final String name, final Object o) {
 		try {
 			Load();// For multiple bot instances.
 			if (data.containsKey(name)) {
 				HashMap<String, String> userData = data.get(name);
 				return userData.containsKey(o) || userData.containsValue(o);
 			}
 		} catch (Exception e) {
 		}
 		return false;
 	}
 }
