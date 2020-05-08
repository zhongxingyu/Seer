 package com.wolvencraft.prison.mines.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 
 public class Message extends com.wolvencraft.prison.util.Message {
 	private static Logger logger = Logger.getLogger("PrisonMine");
 
 	public static void send(CommandSender sender, String message) {
 		if(message == null) message = "";
 		sender.sendMessage(Util.parseVars(message, PrisonMine.getCurMine(sender)));
 	}
 	
 	public static void send(String message) {
 		if(message == null) message = "";
 		send(CommandManager.getSender(), message);
 	}
 	
 	public static void sendSuccess(CommandSender sender, String message) {
 		if(message == null) message = "";
 		send(sender, PrisonMine.getLanguage().GENERAL_SUCCESS + " " + ChatColor.WHITE + message);
 	}
 	
 	public static void sendSuccess(String message) {
 		if(message == null) message = "";
 		sendSuccess(CommandManager.getSender(), message);
 	}
 	
 	public static void sendError(CommandSender sender, String message) {
 		if(message == null) message = "";
 		send(sender, PrisonMine.getLanguage().GENERAL_ERROR + " " + ChatColor.WHITE + message);
 	}
 	
 	public static void sendError(String message) {
 		if(message == null) message = "";
 		sendError(CommandManager.getSender(), message);
 	}
 	
 	public static void sendCustom(String title, String message) {
 		if(message == null) message = "";
 		send(CommandManager.getSender(), ChatColor.GOLD + "[" + title + "] " + ChatColor.WHITE + message);
 	}
 
     /**
      * Broadcasts a message to all players on the server
      * @param message Message to be sent
      */
     public static void broadcast(String message) {
         if(message == null) message = "";
 		message = PrisonMine.getLanguage().GENERAL_SUCCESS + " " + ChatColor.WHITE + message;
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
        	if(p.hasPermission("mcprison.mine.reset.broadcast")) p.sendMessage(Util.parseColors(message));
         }
         Bukkit.getServer().getConsoleSender().sendMessage(Util.parseColors(message));
     }
     
     /**
      * Sends a message into the server log if debug is enabled
      * @param message Message to be sent
      */
     public static void debug(String message) {
         if (PrisonMine.getSettings().DEBUG) log(message);
     }
     
 	/**
 	 * Sends a message into the server log
 	 * @param message Message to be sent
 	 */
 	public static void log(String message) {
 		logger.info("[PrisonMine] " + message);
 	}
 	
 	/**
 	 * Sends a message into the server log
 	 * @param level Severity level
 	 * @param message Message to be sent
 	 */
 	public static void log(Level level, String message) {
 		logger.log(level, "[PrisonMine] " + message);
 	}
 	
 	public static void formatHelp(String command, String arguments, String description, String node) {
 		if(!arguments.equalsIgnoreCase("")) arguments = " " + arguments;
 		if(Util.hasPermission(node) || node.equals(""))
 			send(ChatColor.GOLD + "/mine " + command + ChatColor.GRAY + arguments + ChatColor.WHITE + " " + description);
 	}
 	
 	public static void formatMessage(String message) {
 		send(" " + message);
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
