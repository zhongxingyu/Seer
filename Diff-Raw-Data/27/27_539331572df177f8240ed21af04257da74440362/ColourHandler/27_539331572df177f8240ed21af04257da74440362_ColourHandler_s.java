 package com.mcnsa.mcnsachat2.util;
 
 import java.util.Random;
 
 import org.bukkit.entity.Player;
 
 public class ColourHandler {
	static Integer nextColour = new Integer(-1);
 	
 	// make it a static class) private constructor, static methods
 	private ColourHandler() {}
 
 	public static String translateColour(String name) {
 		String colour = new String("???");
 		
 		// map it!
 		if(name.equalsIgnoreCase("&0")) colour = "black";
 		else if(name.equalsIgnoreCase("&1")) colour = "dark blue";
 		else if(name.equalsIgnoreCase("&2")) colour = "dark green";
 		else if(name.equalsIgnoreCase("&3")) colour = "dark teal";
 		else if(name.equalsIgnoreCase("&4")) colour = "dark red";
 		else if(name.equalsIgnoreCase("&5")) colour = "purple";
 		else if(name.equalsIgnoreCase("&6")) colour = "gold";
 		else if(name.equalsIgnoreCase("&7")) colour = "grey";
 		else if(name.equalsIgnoreCase("&8")) colour = "dark grey";
 		else if(name.equalsIgnoreCase("&9")) colour = "blue";
 		else if(name.equalsIgnoreCase("&a")) colour = "green";
 		else if(name.equalsIgnoreCase("&b")) colour = "teal";
 		else if(name.equalsIgnoreCase("&c")) colour = "red";
 		else if(name.equalsIgnoreCase("&d")) colour = "pink";
 		else if(name.equalsIgnoreCase("&e")) colour = "yellow";
 		else if(name.equalsIgnoreCase("&f")) colour = "white";
 		
 		return colour;
 	}
 
 	public static String translateName(String name) {
 		// default colour will be white.
 		String colour = new String("");
 		
 		// map it!
 		if(name.equalsIgnoreCase("black")) colour = "&0";
 		else if(name.equalsIgnoreCase("dblue")) colour = "&1";
 		else if(name.equalsIgnoreCase("dark blue")) colour = "&1";
 		else if(name.equalsIgnoreCase("dgreen")) colour = "&2";
 		else if(name.equalsIgnoreCase("dark green")) colour = "&2";
 		else if(name.equalsIgnoreCase("dteal")) colour = "&3";
 		else if(name.equalsIgnoreCase("dark teal")) colour = "&3";
 		else if(name.equalsIgnoreCase("daqua")) colour = "&3";
 		else if(name.equalsIgnoreCase("dark aqua")) colour = "&3";
 		else if(name.equalsIgnoreCase("dred")) colour = "&4";
 		else if(name.equalsIgnoreCase("dark red")) colour = "&4";
 		else if(name.equalsIgnoreCase("purple")) colour = "&5";
 		else if(name.equalsIgnoreCase("dpink")) colour = "&5";
 		else if(name.equalsIgnoreCase("dark pink")) colour = "&5";
 		else if(name.equalsIgnoreCase("gold")) colour = "&6";
 		else if(name.equalsIgnoreCase("orange")) colour = "&6";
 		else if(name.equalsIgnoreCase("grey")) colour = "&7";
 		else if(name.equalsIgnoreCase("gray")) colour = "&7";
 		else if(name.equalsIgnoreCase("dgrey")) colour = "&8";
 		else if(name.equalsIgnoreCase("dark grey")) colour = "&8";
 		else if(name.equalsIgnoreCase("dgray")) colour = "&8";
 		else if(name.equalsIgnoreCase("dark gray")) colour = "&8";
 		else if(name.equalsIgnoreCase("blue")) colour = "&9";
 		else if(name.equalsIgnoreCase("green")) colour = "&a";
 		else if(name.equalsIgnoreCase("bright green")) colour = "&a";
 		else if(name.equalsIgnoreCase("teal")) colour = "&b";
 		else if(name.equalsIgnoreCase("aqua")) colour = "&b";
 		else if(name.equalsIgnoreCase("red")) colour = "&c";
 		else if(name.equalsIgnoreCase("pink")) colour = "&d";
 		else if(name.equalsIgnoreCase("yellow")) colour = "&e";
 		else if(name.equalsIgnoreCase("white")) colour = "&f";
 		
 		return colour;
 	}
 
 	// allow for colour tags to be used in strings..
 	public static String processColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 	}
 
 	// strip colour tags from strings..
 	public static String stripColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "").replaceAll("(\u00A7([a-f0-9]))", "");
 	}
 	
 	public static void sendMessage(Player player, String message) {
 		player.sendMessage(processColours(message));
 	}
 	
 	public static String randomColour() {
 		// get a random number
 		Random generator = new Random();
 		Integer roll = generator.nextInt(15) + 1; // don't use black
 		// convert to hex string
 		// and return!
 		return "&" + Integer.toHexString(roll);
 	}
 	
 	public static String rainbowColour() {
 		// get the next colour
 		nextColour += 1;
 		if(nextColour >= 16) {
			nextColour = 0;
 		}
 		return "&" + Integer.toHexString(nextColour);
 	}
 }
