 package com.wolvencraft.prison.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.CommandManager;
 import com.wolvencraft.prison.PrisonSuite;
 
 public class Message {
	private static Logger logger = Logger.getLogger("PrisonSuite");
 	
 	public static void send(String message) {
 		CommandSender sender = CommandManager.getSender();
 		if(sender instanceof Player) send((Player) sender, message);
		else Bukkit.getConsoleSender().sendMessage(message);
 	}
 	
 	public static void sendSuccess(Player player, String message) {
 		message = PrisonSuite.getLanguage().GENERAL_SUCCESS + " " + ChatColor.WHITE + message;
 		send(player, message);
 	}
 	
 	public static void sendSuccess(String message) {
 		CommandSender sender = CommandManager.getSender();
 		if(sender instanceof Player) sendSuccess((Player) sender, message);
		else Bukkit.getConsoleSender().sendMessage(message);
 	}
 	
 	public static void sendError(Player player, String message) {
 		message = PrisonSuite.getLanguage().GENERAL_ERROR + " " + ChatColor.WHITE + message;
 		send(player, message);
 	}
 	
 	public static void sendError(String message) {
 		CommandSender sender = CommandManager.getSender();
 		if(sender instanceof Player) sendError((Player) sender, message);
		else Bukkit.getConsoleSender().sendMessage(message);
 	}
 	
     /**
      * Sends a message to the player
      * @param player Recipient
      * @param message Message to be sent
      */
 	public static void send(Player player, String message) {
 		if(message == null) message = "";
 		player.sendMessage(Util.parseColors(message));
 	}
 
     /**
      * Broadcasts a message to all players on the server
      * @param message Message to be sent
      */
     public static void broadcast(String message) {
         if(message == null) message = "";
 		message = PrisonSuite.getLanguage().GENERAL_SUCCESS + " " + ChatColor.WHITE + message;
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
         	p.sendMessage(Util.parseColors(message));
         }
     }
 	
     /**
      * Sends a message into the server log if debug is enabled
      * @param message Message to be sent
      */
     public static void debug(String message) {
         if (PrisonSuite.getSettings().DEBUG) log(message);
     }
 	
 	/**
 	 * Sends a message into the server log
 	 * @param message Message to be sent
 	 */
 	public static void log(String message) {
 		logger.info("[PrisonCore] " + message);
 	}
 	
 	/**
 	 * Sends a message into the server log
 	 * @param level Severity level
 	 * @param message Message to be sent
 	 */
 	public static void log(Level level, String message) {
 		logger.log(level, "[PrisonCore] " + message);
 	}
 	
 	public static void formatHelp(String command, String arguments, String description, String node) {
 		CommandSender sender = CommandManager.getSender();
 		if(!arguments.equalsIgnoreCase("")) arguments = " " + arguments;
 		if(sender.hasPermission(node) || node.equals(""))
 			sender.sendMessage(ChatColor.GOLD + "/ps " + command + ChatColor.GRAY + arguments + ChatColor.WHITE + " " + description);
 		return;
 	}
 	
 	public static void formatHelp(String command, String arguments, String description) {
 		formatHelp(command, arguments, description, "");
 		return;
 	}
 	
 	public static void formatHeader(int padding, String name) {
 		CommandSender sender = CommandManager.getSender();
 		String spaces = "";
 		for(int i = 0; i < padding; i++) { spaces = spaces + " "; }
 		sender.sendMessage(spaces + "-=[ " + ChatColor.BLUE + name + ChatColor.WHITE + " ]=-");
 	}
 }
