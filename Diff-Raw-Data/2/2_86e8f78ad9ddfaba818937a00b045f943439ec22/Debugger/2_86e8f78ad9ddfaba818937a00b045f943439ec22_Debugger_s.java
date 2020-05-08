 package com.titankingdoms.nodinchan.titanchat.debug;
 
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 
 /**
  * debugger class
  * 
  * -
  * 
  * provides methods for logging when in debug mode
  * 
  * @author slipcor
  * 
  */
 
 public class Debugger {
 	public static boolean override = false;
 
 	private static String prefix = "[TC-debug] ";
 	private static HashSet<Integer> check = new HashSet<Integer>();
 	private static byte level = 3;
 
 	private int id = 0;
 
 	/**
 	 * Debug constructor
 	 * 
 	 * @param i
 	 *            the debug id to check
 	 */
 	public Debugger(int i) {
 		id = i;
 	}
 
 	/**
 	 * does this class debug?
 	 * 
 	 * @return true if debugs, false otherwise
 	 */
 	private boolean debugs() {
 		return override || check.contains(id) || check.contains(666);
 	}
 
 	/**
 	 * log a message as prefixed INFO
 	 * 
 	 * @param s
 	 *            the message
 	 */
 	public void i(String s) {
 		if (!debugs() || level < 1)
 			return;
 		Bukkit.getLogger().info(prefix + s);
 	}
 
 	/**
 	 * log a message as prefixed WARNING
 	 * 
 	 * @param s
 	 *            the message
 	 */
 	public void w(String s) {
 		if (!debugs() || level < 2)
 			return;
 		Bukkit.getLogger().warning(prefix + s);
 	}
 
 	/**
 	 * log a message as prefixed SEVERE
 	 * 
 	 * @param s
 	 *            the message
 	 */
 	public void s(String s) {
 		if (!debugs() || level < 3)
 			return;
 		Bukkit.getLogger().severe(prefix + s);
 	}
 
 	/**
 	 * read a string array and return a readable string
 	 * 
 	 * @param s
 	 *            the string array
 	 * @return a string, the array elements joined with comma
 	 */
 	public String formatStringArray(String[] s) {
 		if (s == null)
 			return "NULL";
 		String result = "";
 		for (int i = 0; i < s.length; i++) {
 			result = result + (result.equals("") ? "" : ",") + s[i];
 		}
 		return result;
 	}
 
 	public static void load(TitanChat instance) {
		String debugs = instance.getConfig().getString("debug");
 		if (!debugs.equals("none")) {
 			if (debugs.equals("all") || debugs.equals("full")) {
 				Debugger.check.add(666);
 				System.out.print("debugging EVERYTHING");
 			} else {
 				String[] sIds = debugs.split(",");
 				for (String s : sIds) {
 					try {
 						Debugger.check.add(Integer.valueOf(s));
 						System.out.print("debugging: " + s);
 					} catch (Exception e) {
 						System.out.print("debug load error: " + s);
 					}
 					if (s.equals("i")) {
 						level = (byte) 1;
 					} else if (s.equals("w")) {
 						level = (byte) 2;
 					} else if (s.equals("s")) {
 						level = (byte) 3;
 					}
 				}
 			}
 		}
 	}
 }
 
 // debug ids:
 // 1 - titanchat
 // 2 - .addon
 // 3 - .channel
 // 4 - .command
 // 5 - .permissions
 
 
