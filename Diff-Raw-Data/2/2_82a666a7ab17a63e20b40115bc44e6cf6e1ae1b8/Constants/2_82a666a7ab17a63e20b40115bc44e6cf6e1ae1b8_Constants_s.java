 package me.Kruithne.WolfHunt;
 
 import org.bukkit.Material;
 
 public class Constants {
 	
 	public static boolean enableVanishNoPacketSupport = false;
 	public static boolean trackOP = true;
 	
 	public static String messageNoPlayers = "The wolf's tail wags as it looks up at you";
 	public static String messageDetected = "The wolf barks in the %s direction";
 	public static String messageNearby = "The wolf growls loudly";
 	
 	public static String messageBaby = "The baby wolf just licks the string happily";
 	
 	public static String directionNorth = "north";
 	public static String directionEast = "east";
 	public static String directionSouth = "south";
 	public static String directionWest = "west";
 	
 	public static String commandNoParameters = "Error, Format: '/wolfhunt [command] [parameters]'";
 	public static String commandSeeHelp = "See '/wolfhunt help' for available commands";
 	public static String commandAvailable = "Your available commands for WolfHunt:";
 	public static String commandNoneAvail = "No available commands found";
 	public static String commandUnknown = "Unknown commmand '%s' entered";
 	public static String commandFormatInvalid = "Invalid command format";
 	public static String commandInfoFormat = "- '/wolfhunt %s' - %s";
 	public static String commandNoPermission = "You do not have permission to use that command";
 	
 	public static String commandInfoSpawnWolf = String.format(Constants.commandInfoFormat, "spawnwolf", "Spawns an untamed wolf");
 	public static String commandInfoGetConfig = String.format(Constants.commandInfoFormat, "getconfig [key]", "Gets a config value");
	public static String commandInfoSetConfig = String.format(Constants.commandInfoFormat, "getconfig [key] [value]", "Sets a config value");
 	
 	public static String commandInfoConfigNoExists = "That config value does not exist in WolfHunt";
 	public static String commandInfoGetConfigReturnFormat = "WolfHunt Config: %s = %s";
 	public static String commandInfoSetConfigDone = "Configration value '%s' has been set to '%s'";
 	
 	public static String commandInfoSpawnWolfDone = "Spawned an untamed wolf at your location";
 	
 	public static String outputPluginTag = "[WolfHunt]";
 	public static String outputToConsoleFormat = "%s %s";
 	public static String outputToPlayerFormat = "%s%s";
 	
 	public static String pluginNodePath = "wolfhunt.%s";
 	
 	public static String default_trackingItem = Integer.toString(Material.STRING.getId());
 	public static String default_trackingRadius = "200";
 	public static String default_allowOpOverride = "true";
 	public static String default_preventTrackingOps = "true";
 	public static String default_babyWolvesCanTrack = "false";
 	
 }
