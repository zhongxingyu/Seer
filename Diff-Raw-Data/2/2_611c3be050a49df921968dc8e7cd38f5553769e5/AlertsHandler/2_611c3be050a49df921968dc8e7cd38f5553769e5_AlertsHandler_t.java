 package net.mayateck.OldEdenCore;
 
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 @SuppressWarnings("unused")
 
 public class AlertsHandler implements CommandExecutor {
 	private OldEdenCore plugin;
 	 
 	public AlertsHandler(OldEdenCore plugin) {
 		this.plugin = plugin;
 	}
 	
 	
 	static String alertSys = ChatColor.DARK_GRAY+" || "+ChatColor.YELLOW+"SYSTEM"+ChatColor.DARK_GRAY+" || ";
 	static String alertNote = ChatColor.DARK_GRAY+" || "+ChatColor.WHITE+"NOTICE"+ChatColor.DARK_GRAY+" || ";
 	static String alertError = ChatColor.DARK_GRAY+" || "+ChatColor.RED+"ERROR"+ChatColor.DARK_GRAY+" || ";
	static String alertBCast = ChatColor.DARK_GRAY+" || "+ChatColor.DARK_AQUA+"BROADCAST"+ChatColor.DARK_GRAY+" || ";
 	
 	public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("alert")){
 			if (args.length==0 || args.length==1){
 				s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid parameter number. Usage: /alert [type] [message]");
 				return true;
 			} else {
 				String message = "";
 				boolean doAlert = false;
 				for (int i=1; i<=args.length-1; i++){
 					message = message+" "+args[i];
 				}
 				char amp = '&';
 				message = ChatColor.translateAlternateColorCodes(amp, message);
 				message = ChatColor.RESET+message;
 				if(args[0].equalsIgnoreCase("system")) {
 					message = alertSys+message;
 					doAlert = true;
 				} else if (args[0].equalsIgnoreCase("notice")){
 					message = alertNote+message;
 					doAlert = true;
 				} else if (args[0].equalsIgnoreCase("error")){
 					message = alertError+message;
 					doAlert = true;
 				} else if (args[0].equalsIgnoreCase("broadcast")){
 					message = alertBCast+message;
 					doAlert = true;
 				} else {
 					s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid alert type.");
 					s.sendMessage("alert."+args[0]+" "+message);
 					doAlert = true;
 					return true;
 				}
 				if (doAlert==true){
 					Bukkit.getServer().broadcastMessage(message);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
 
