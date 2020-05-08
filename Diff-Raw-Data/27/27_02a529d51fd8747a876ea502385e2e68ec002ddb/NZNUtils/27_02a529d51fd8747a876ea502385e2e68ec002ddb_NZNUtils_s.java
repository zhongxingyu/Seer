 package nz.co.nznetwork.minecraftplugin.utils;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import nz.co.nznetwork.minecraftplugin.NZNPlugin;
 
 public class NZNUtils {
 	
 	public final ChatColor ERROR_COLOR;
 	
 	private NZNPlugin plugin;
 	public NZNUtils(NZNPlugin masterPlugin) {
 		plugin = masterPlugin;
 		
 		//read error color from config
 		ERROR_COLOR = ChatColor.DARK_RED;
 	}
 
 	public String formatMessage(String message) {
 
 		message = message.replaceAll("&0", "0");
 		message = message.replaceAll("&1", "1");
 		message = message.replaceAll("&2", "2");
 		message = message.replaceAll("&3", "3");
 		message = message.replaceAll("&4", "4");
 		message = message.replaceAll("&5", "5");
 		message = message.replaceAll("&6", "6");
 		message = message.replaceAll("&7", "7");
 		message = message.replaceAll("&8", "8");
 		message = message.replaceAll("&9", "9");
 		message = message.replaceAll("&a", "a");
 		message = message.replaceAll("&b", "b");
 		message = message.replaceAll("&c", "c");
 		message = message.replaceAll("&d", "d");
 		message = message.replaceAll("&e", "e");
 		message = message.replaceAll("&f", "f");
 		message = message.replaceAll("&l", "l");
 		message = message.replaceAll("&m", "m");
 		message = message.replaceAll("&n", "n");
 		message = message.replaceAll("&o", "o");
 		message = message.replaceAll("&k", "k");
 		
 		/*
 		message = message.replaceAll("&black", "0");
 		message = message.replaceAll("&darkblue", "1");
 		message = message.replaceAll("&darkgreen", "2");
 		message = message.replaceAll("&darkaqua", "3");
 		message = message.replaceAll("&darkred", "4");
 		message = message.replaceAll("&darkpurple", "5");
 		message = message.replaceAll("&gold", "6");
 		message = message.replaceAll("&gray", "7");
 		message = message.replaceAll("&darkgray", "8");
 		message = message.replaceAll("&indigo", "9");
 		message = message.replaceAll("&green", "a");
 		message = message.replaceAll("&aqua", "b");
 		message = message.replaceAll("&red", "c");
 		message = message.replaceAll("&pink", "d");
 		message = message.replaceAll("&yellow", "e");
 		message = message.replaceAll("&white", "f");
 		message = message.replaceAll("&bold", "l");
 		message = message.replaceAll("&strike", "m");
 		message = message.replaceAll("&underline", "n");
 		message = message.replaceAll("&italic", "o");
 		message = message.replaceAll("&random", "k");
 		*/
 		return message;
 	}
 
 	public String secondsToString(long s) {
 		boolean invert = false;
 		// 1st sort out the edge case:
 		if (s == 0) {
 			return "0 seconds";
 		}
 		if (s < 0) {
 			invert = true;
 			s = s * -1;
 		}
 
 		// calculate how many of each unit is required
 		int days = (int) (s / (60 * 60 * 24));
 		s = s - (days * 24 * 60 * 60);
 		int hours = (int) (s / (60 * 60));
 		s = s - (hours * 60 * 60);
 		int mins = (int) (s / 60);
 		s = s - (mins * 60);
 		int seconds = (int) s;
 		// work out the number of units used
 		boolean and = false;
 		int numberOfUnits = 0;
 		if (days != 0)
 			numberOfUnits++;
 		if (hours != 0)
 			numberOfUnits++;
 		if (mins != 0)
 			numberOfUnits++;
 		if (seconds != 0)
 			numberOfUnits++;
 		if (numberOfUnits > 1)
 			and = true;
 
 		// format the string
 		String result = "";
 		if (seconds != 0) {
 			if (seconds == 1)
 				result = String.valueOf(seconds) + " second " + result;
 			else
 				result = String.valueOf(seconds) + " seconds " + result;
 			if (and) {
 				result = "and " + result;
 				and = false;
 			}
 		}
 		if (mins != 0) {
 			if (mins == 1)
 				result = String.valueOf(mins) + " mintute " + result;
 			else
 				result = String.valueOf(mins) + " mintutes " + result;
 			if (and) {
 				result = "and " + result;
 				and = false;
 			}
 		}
 		if (hours != 0) {
 			if (hours == 1)
 				result = String.valueOf(hours) + " hour " + result;
 			else
 				result = String.valueOf(hours) + " hours " + result;
 			if (and) {
 				result = "and " + result;
 				and = false;
 			}
 		}
 		if (days != 0) {
 			if (days == 1)
 				result = String.valueOf(days) + " day " + result;
 			else
 				result = String.valueOf(days) + " days " + result;
 		}
 		if (invert)
 			result = result + "ago";
 		return result;
 	}
 	
 	public long stringToSeconds(String timeinput) {
 		try {
 			//first check its not already in seconds
 			long l = Long.parseLong(timeinput);
 			return l;
 		} catch (NumberFormatException e) {}
 		
 		char timespan = timeinput.charAt(timeinput.length() - 1); //get the last character
 		String timestring = timeinput.substring(0, timeinput.length() - 1); //get the rest of the string
 		try {
 			long time = Long.parseLong(timestring);
 			if ((timespan == 's') || (timespan == 'S'))
 				time = time * 1L;
 			else if ((timespan == 'm') || (timespan == 'M'))
 				time = time * 60L;
 			else if ((timespan == 'h') || (timespan == 'H'))
 				time = time * 3600L;
 			else if ((timespan == 'd') || (timespan == 'D'))
 				time = time * 86400L;
 			else
 				return 0L;
 			return time;
 		} catch (NumberFormatException ex) {
 			return 0L;
 		}
 	}
 	
	public boolean getPermision(Player p, String permission) {
    	if(p.isOp()) return true;
    	if(p.hasPermission(permission)) return true;
     	String[] hirechy = permission.split(".");
     	for(int i = 0; i < hirechy.length; i++) {
     		String permBuilder = "";
     		for(int j = 0; j <= i; j++) {
     			permBuilder = permBuilder+hirechy[j]+".";
     		}
     		permBuilder = permBuilder+"*";
    		if(p.hasPermission(permBuilder)) return true;
     	}
     	return false;
     }
 }
