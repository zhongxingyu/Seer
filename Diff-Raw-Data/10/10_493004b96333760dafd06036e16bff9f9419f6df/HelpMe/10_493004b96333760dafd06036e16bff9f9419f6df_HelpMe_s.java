 package com.gmail.ebiggz.plugins.mythsentials;
 
 import java.util.logging.Logger;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class HelpMe implements CommandExecutor {
 	
 	private static final Logger log = Logger.getLogger("Minecraft");
 	
 	public static Mythsentials plugin;
 	 
 	public HelpMe(Mythsentials plugin) {
 		HelpMe.plugin = plugin;
 	}
  
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		String userName = sender.getName();
 		if(commandLabel.equalsIgnoreCase("helpme") || commandLabel.equalsIgnoreCase("modhelp") || commandLabel.equalsIgnoreCase("adminhelp") || commandLabel.equalsIgnoreCase("mod") || commandLabel.equalsIgnoreCase("admin")) {
 			//declare different command aliases 
 		    if(sender.hasPermission("mythica.helpsend")) {
 				//check if command sender has correct permission
 		    	Boolean mods = modsOnline();
 		    	if(!(mods == false)) { 
 			    	//makes sure mods are online
 		    	    if(args.length == 0) {
 		    	    	//when nothing is typed after command
 		    	    	for(Player mod: plugin.getServer().getOnlinePlayers()) {    		 
 		    	    	    if(mod.hasPermission("mythica.helpreceive")) {
 		    	    	    	mod.sendMessage(ChatColor.RED + "[HelpMe] " + ChatColor.YELLOW + userName + ChatColor.GOLD + " needs help!");
 				    	        log.severe(userName + " needs help! (Mods online)");	
 				    	        //send message to all mods with correct permission that command sender needs help, without a reason.
 		    	    	    }  	 
 		    	    	}
 		    	    	sender.sendMessage(ChatColor.GOLD + "Mods have been notified and will help you soon!");
 		    	        return true;
 		    	        //send message to command sender that the mods have been notified.
 		                } 
 		            else if(args.length == 1) {
 		            	//when only one argument is typed after command
 		        	    if(args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
 		        	    	//check if the argument is "?" or "help"
 		        		    sender.sendMessage(ChatColor.RED + "-------------[HelpMe]-------------" + ChatColor.GOLD + "\n/helpme [reason]" + ChatColor.WHITE + " - Alerts mods/admins\nthat you need help." + ChatColor.ITALIC + "\nNote: You can leave [reason] blank.");
 		        		    return true;
 		        		    //send help menu
 		        	    } else {
 			    	    	for(Player mod: plugin.getServer().getOnlinePlayers()) {    		 
 			    	    	    if(mod.hasPermission("mythica.helpreceive")) {	    	    	 
 			    	    	    	mod.sendMessage(ChatColor.RED + "[HelpMe] " + ChatColor.YELLOW + userName + ChatColor.GOLD + " needs help! Reason: " + args[0]);
 			    	    	    	log.severe("[HelpMe] " + userName + ": " + args[0] + "(Mods online)");
 			    	    	        //if not, notify mods that command sender needs help with first argument as the reason.
 			    	    	    }
 			    	    	}
 		        	    }
 		        	    sender.sendMessage(ChatColor.GOLD + "Mods have been notified and will help you soon!");
 		        	    return true;
 		        	    //tell command sender that mods have been notified
 		            }
 		            else if(args.length > 1) {
 					    String reason = "";
 					    for(int i = 0; i < args.length; i++){
 					    	reason += " " + args[i];
 					    }
 					    reason = reason.substring(1);
 					    //if arguments are greater than one, combine them into one string
 		    	    	for(Player mod: plugin.getServer().getOnlinePlayers()) {    		 
 		    	    	    if(mod.hasPermission("mythica.helpreceive")) {	
 		    	    	    	mod.sendMessage(ChatColor.RED + "[HelpMe] " + ChatColor.YELLOW + userName + ChatColor.GOLD + " needs help! Reason: " + reason);
 		    	    	    	log.severe("[HelpMe] " + userName + ": " + reason + "(Mods online)");
 		    	    	    	//notify mods that command sender needs help, with arguments string as the reason.
 		    	    	    }
 		    	    	}
 		    	        sender.sendMessage(ChatColor.GOLD + "Mods have been notified and will help you soon!");
 		    	    	return true;
 		            }
 		    } else {
 		    	log.severe(userName + " needs help! (No Mods online!)");
 		    	sender.sendMessage(ChatColor.GOLD + "We're sorry, unfortunately there isn't any mods on at the moment. You can make an issue on our website and our staff will review it as soon as they get on.");
 		    	//Message for command sender if no mods are currently online.
 		    	return true;		    	
 		    }
 
         }
 	    	sender.sendMessage(ChatColor.RED + "Sorry, you don't have permission to send help alerts.");
 			//message if command sender doesn't have permission to send help alerts.
 			return true;
 	  }
 	    else if(commandLabel.equalsIgnoreCase("mods") || commandLabel.equalsIgnoreCase("admins")) {
 	    	if(modsOnline() == true) {
 			    sender.sendMessage(ChatColor.GOLD + "Mods are " + ChatColor.GREEN + "Online" + ChatColor.GOLD + "! Type /helpme if you need us.");
 			    return true;
 		    } else {
 			    sender.sendMessage(ChatColor.GOLD + "Mods are " + ChatColor.RED + "Offline" + ChatColor.GOLD + ". If you need help, make an Issue on the website for mods to review as soon as they get on.");
 			    return true;
 		    }
 	   }
 		return false;
     }
     public static Boolean modsOnline() {
     	for(Player mod: plugin.getServer().getOnlinePlayers()) {    		 
     	    if(mod.hasPermission("mythica.helpreceive")) {
     	        return true;
     	    }  	 
     	}
 		return false;  	
 	}
 }
 
 
