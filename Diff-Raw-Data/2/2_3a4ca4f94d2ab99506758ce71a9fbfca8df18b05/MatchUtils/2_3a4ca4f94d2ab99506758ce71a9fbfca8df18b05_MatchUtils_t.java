 package com.martinbrook.tesseractuhc.util;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.plugin.Plugin;
 
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.wimbli.WorldBorder.BorderData;
 import com.wimbli.WorldBorder.WorldBorder;
 
 public class MatchUtils {
 
 	private MatchUtils() { }
 	
 	public static String formatDuration(Calendar t1, Calendar t2, boolean precise) {
 		// Get duration in seconds
 		int d = (int) (t2.getTimeInMillis() - t1.getTimeInMillis()) / 1000;
 		
 		if (precise) {
 			int seconds = d % 60;
 			d = d / 60;
 			int minutes = d % 60;
 			int hours = d / 60;
 			
 			// The string
 			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
 		} else {
 			int minutes = d / 60;
 			return minutes + " minute" + (minutes != 1 ? "s" : "");
 			
 		}
 		
 	}
 
 	/**
      * Get a BorderData from the WorldBorder plugin
      * 
      * @param w The world to get borders for
      * @return The WorldBorder BorderData object
      */
     private static BorderData getWorldBorder(World w) {
         Plugin plugin = TesseractUHC.getInstance().getServer().getPluginManager().getPlugin("WorldBorder");
 
         // Check if the plugin is loaded
         if (plugin == null || !(plugin instanceof WorldBorder))
         	return null;
 
         WorldBorder wb = (WorldBorder) plugin;
         return wb.GetWorldBorder(w.getName());
     }
     
     /**
      * Change the WorldBorder radius for a world
      * 
      * @param w The world to be changed
      * @param radius The new radius
      * @return Whether the operation succeeded
      */
     public static boolean setWorldRadius(World w, int radius) {
     	BorderData border = getWorldBorder(w);
     	if (border != null) {
     		border.setRadius(radius);
     		return true;
     	}
     	return false;
     }
 
 	/**
 	 * Attempt to parse a calcstarts command and return a list of start points
 	 * 
 	 * @param args The arguments which were passed to the command
 	 * @return List of start locations, or null if failed
 	 */
 	public static ArrayList<Location> calculateStarts(String[] args) {
 		if (args.length < 1) return null;
 		String method = args[0];
 		if ("radial".equalsIgnoreCase(method)) {
 			if (args.length != 3) return null;
 			int count;
 			int radius;
 			try {
 				count = Integer.parseInt(args[1]);
 				radius = Integer.parseInt(args[2]);
 			} catch (NumberFormatException e) {
 				return null;
 			}
 			return calculateRadialStarts(count, radius);
 			
 		}
 		return null;
 		
 
 	}
 
 	/**
 	 * Generate a list of radial start points
 	 * 
 	 * @param count Number of starts to generate
 	 * @param radius Radius of circle
 	 * @return List of starts
 	 */
 	private static ArrayList<Location> calculateRadialStarts(int count, int radius) {
 		ArrayList<Location> locations = new ArrayList<Location>();
 		
 		double arc = (2*Math.PI) / count;
 		World w = TesseractUHC.getInstance().getMatch().getStartingWorld();
 		
 		for(int i = 0; i < count; i++) {
 			int x = (int) (radius * Math.cos(i*arc));
 			int z = (int) (radius * Math.sin(i*arc));
 			
 			int y = w.getHighestBlockYAt(x, z);
 			locations.add(new Location(w,x,y,z));
 		}
 		return locations;
 	
 	}
 	
 	/**
 	 * Convert a string to a boolean.
 	 * 
 	 * true, on, yes, y, 1 => True
 	 * false, off, no, n, 0 => False
 	 * 
 	 * @param s The string to check
 	 * @return Boolean value, or null if not parsable
 	 */
 	public static Boolean stringToBoolean(String s) {
 		if ("true".equalsIgnoreCase(s) || "on".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s) || "1".equals(s))
 			return true;
 		if ("false".equalsIgnoreCase(s) || "off".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s) || "0".equals(s))
 			return false;
 		return null;
 		
 	}
 	
 	/**
 	 * Format a string for display on a sign
 	 * 
 	 * @param s The string to be formatted
 	 * @return An array with 4 elements, containing the 4 lines to go on the sign
 	 */
 	public static String[] signWrite(String s) {
 		// Create array to hold the lines, and initialise with empty strings
 		String[] lines = new String[4];
 		int currentLine = 0;
 		for (int i = 0; i < 4; i++) lines[i] = "";
 		
 		// Split the message into strings on whitespace
 		String[] words = s.split("\\s");
 		
 		// Loop through words, adding them to lines as they fit
 		int currentWord = 0;
		while(currentLine < 4 && currentWord < words.length) {
 			if (lines[currentLine].length() + words[currentWord].length() <= 14)
 				lines[currentLine] += " " + words[currentWord++];
 			else
 				currentLine++;
 		}
 		
 		// If we have only used one or two lines, move everything down by one.
 		if (currentLine < 2) {
 			lines[2]=lines[1];
 			lines[1]=lines[0];
 			lines[0]="";
 		}
 		
 		return lines;
 		
 	}
 	
 	
 }
