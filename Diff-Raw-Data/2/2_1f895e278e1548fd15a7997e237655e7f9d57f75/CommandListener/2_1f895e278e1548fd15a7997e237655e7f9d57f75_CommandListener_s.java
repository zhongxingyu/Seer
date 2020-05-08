 package com.isitgeo.randomgift;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class CommandListener implements CommandExecutor {
 
 	private RandomGiftGen rGG;
 	private RandomGift plugin;
 
 	public CommandListener(RandomGift plugin, RandomGiftGen rGG) {
 		this.plugin = plugin;
 		this.rGG = rGG;
 	}
 
 	public boolean onCommand(CommandSender sentby, Command command,	String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("randomgift")) {
 
 			if (args.length == 0) {
 				sentby.sendMessage("RandomGift " + this.plugin.getDescription().getVersion());
 				sentby.sendMessage("Usage: /randomgift <command>");
 				return true;
 			}
 			
 			if (args[0].equalsIgnoreCase("reload")){
 				if (sentby.hasPermission("randomgift.reload")){
 					plugin.reloadConfig();
					//plugin.load();
 					sentby.sendMessage("RandomGift configuration reloaded.");
 					return true;
 				} else {
 					sentby.sendMessage(plugin.permError);
 				}
 			}
 
 			if (args[0].equalsIgnoreCase("cooldown")) {
 
 				if (sentby.hasPermission("randomgift.cooldown")) {
 					int difference = (int) (System.currentTimeMillis() - plugin.cooldown);
 					int val = plugin.cooldownTime - difference;
 
 					if (!(val <= 60000)) {
 						sentby.sendMessage(plugin.broadcastTag + " About " + val / 60 / 1000 + " minutes remaining.");
 					} else if (val <= 0) {
 						sentby.sendMessage("Ready and waiting to be triggered!");
 					} else {
 						sentby.sendMessage(plugin.broadcastTag + val / 1000 + " seconds remaining.");
 					}
 				} else {
 					sentby.sendMessage(plugin.permError);
 				}
 
 				if (args.length == 2) {
 					if (args[1].equalsIgnoreCase("reset")) {
 
 						if (sentby.hasPermission("randomgift.cooldown.reset")) {
 
 							plugin.cooldown = System.currentTimeMillis() - plugin.cooldownTime;
 							sentby.sendMessage("Cooldown timer has been reset!");
 						} else {
 							sentby.sendMessage(plugin.permError);
 						}
 					}
 				}
 
 				return true;
 			} else if (args[0].equalsIgnoreCase("gift")) {
 
 				if (args.length == 2) {
 					
 					if (sentby.hasPermission("randomgift.gift")) {
 						
 						if (plugin.getServer().getPlayer(args[1]) != null) {
 							rGG.getPlayers(plugin.getServer().getPlayer(args[1]));
 							
 						} else {
 							sentby.sendMessage("Player not online!");
 						}
 					} else {
 						sentby.sendMessage(plugin.permError);
 					}
 				} else {
 					sentby.sendMessage("You didn't specify a player!");
 				}
 			} else {
 				sentby.sendMessage(plugin.commandError);
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 }
