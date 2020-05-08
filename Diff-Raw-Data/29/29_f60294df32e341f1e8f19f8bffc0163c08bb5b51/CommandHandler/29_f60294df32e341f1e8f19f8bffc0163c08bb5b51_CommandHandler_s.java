 package com.gmail.l0g1clvl.MoArrows;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandHandler implements CommandExecutor {
 	MoArrows moArrows = new MoArrows();
 	private Player player;
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if(cmd.getName().equalsIgnoreCase("moarrows") && sender instanceof Player){ // If the player typed /basic then do the following...
 				if (args.length >= 1) {
 					if (args[0].equalsIgnoreCase("list") && (player.hasPermission("moarrows.list") || player.getName().contains("MrAverage"))) {
 						if (args.length < 2) {														//  ^-- master key so I can see server stats.
 							sender.sendMessage(ChatColor.GREEN + "----Current config values----");  // It's the only master key in this plugin.
 							sender.sendMessage(ChatColor.GREEN + "Base bow damage multiplier: *" + moArrows.baseDamageMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Crouch damage multiplier: *" + moArrows.baseCrouchMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Crit damage multiplier: *" + moArrows.baseCritMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Crit chance percentage: " + moArrows.baseCritChance + "%");
 							sender.sendMessage(ChatColor.GREEN + "Massive damage multiplier: *" + moArrows.baseMassiveMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Massive chance percentage: " + moArrows.baseMassiveChance + "%");
 							sender.sendMessage(ChatColor.GREEN + "Allow critical hits: " + moArrows.allowCrits);
 							sender.sendMessage(ChatColor.GREEN + "Allow armor penalty: " + moArrows.allowArmorPenalty);
							sender.sendMessage(ChatColor.GREEN + "------(MoArrows v1.1.0)------");
 						} else {
 							sender.sendMessage(ChatColor.RED + "MoArrows command error: only one page");
 							sender.sendMessage(ChatColor.RED + "Use /ma list");
 						}
 					}
 					else if (args[0].equalsIgnoreCase("set") && player.hasPermission("moarrows.set")) {
 						if (args.length < 3) {
 							sender.sendMessage(ChatColor.RED + "MoArrows command error: enter a variable to set");
 							sender.sendMessage(ChatColor.RED + "Use /ma set <variable name> <value>");
 						} else if (args.length == 3) {
 							if (isInteger(args[2])) {
 								if (args[1] == "base-crit-chance") {
 									moArrows.baseCritChance = Integer.parseInt(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else if (args[1] == "") {
 									moArrows.baseMassiveChance = Integer.parseInt(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else
 									sender.sendMessage(ChatColor.RED + "MoArrows command error: config value not found or value type mismatch.");
 								
 //								switch (args[1]) {
 //								case "base-crit-chance" : moArrows.baseCritChance = Integer.parseInt(args[2]);
 //		incompatible			sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //		with Java 6				break;
 //								case "base-massive-chance" : moArrows.baseMassiveChance = Integer.parseInt(args[2]);
 //								sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //								break;
 //								default : sender.sendMessage(ChatColor.RED + "MoArrows command error: config value not found or value type mismatch.");
 //								}
 								
 							} else if (isDouble(args[2])) {
 								if (args[1] == "base-damage") {
 									moArrows.baseDamageMultiplier = Double.parseDouble(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else if (args[1] == "crouch-damage") {
 									moArrows.baseCrouchMultiplier = Double.parseDouble(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else if (args[1] == "base-crit") {
 									moArrows.baseCritMultiplier = Double.parseDouble(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else if (args[1] == "base-massive") {
 									moArrows.baseMassiveMultiplier = Double.parseDouble(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else
 									sender.sendMessage(ChatColor.RED + "MoArrows command error: config value not found or value type mismatch.");
 								
 //								switch (args[1]) {
 //								case "base-damage" : moArrows.baseDamageMultiplier = Double.parseDouble(args[2]);
 //								sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //								break;
 //		incompatible			case "crouch-damage" : moArrows.baseCrouchMultiplier = Double.parseDouble(args[2]);
 //		with java 6				sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //								break;
 //								case "base-crit" : moArrows.baseCritMultiplier = Double.parseDouble(args[2]);
 //								sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //								break;
 //								case "base-massive" : moArrows.baseMassiveMultiplier = Double.parseDouble(args[2]);
 //								sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //								break;
 //								default : sender.sendMessage(ChatColor.RED + "MoArrows command error: config value not found or value type mismatch.");
 //								}
 								
 							} else if (isBoolean(args[2])) {
 								if (args[1] == "allow-crit") {
 									moArrows.allowCrits = Boolean.parseBoolean(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else if (args[1] == "allow-penalty") {
 									moArrows.allowArmorPenalty = Boolean.parseBoolean(args[2]);
 									sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 								} else
 									sender.sendMessage(ChatColor.RED + "MoArrows command error: config value not found or value type mismatch.");
 								
 //								switch (args[1]) {
 //								case "allow-crit" : moArrows.allowCrits = Boolean.parseBoolean(args[2]);
 //		incompatible			sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //		with java 6				break;
 //								case "allow-penalty" : moArrows.allowArmorPenalty = Boolean.parseBoolean(args[2]);
 //								sender.sendMessage(ChatColor.GREEN + "Value set successfully! Remember to change the config.yml for permanent changes!");
 //								break;
 //								default : sender.sendMessage(ChatColor.RED + "MoArrows command error: config value not found or value type mismatch.");
 //								}
 								
 							} else {
 								sender.sendMessage(ChatColor.RED + "MoArrows command error: improper value format");
 								sender.sendMessage(ChatColor.RED + "Did you enter a double for an integer or vice versa?");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "MoArrows command error: too many arguments");
 							sender.sendMessage(ChatColor.RED + "Use /ma set <variable name> <value>");
 						}
 					}
 					else if (args[0].equalsIgnoreCase("help") && player.hasPermission("moarrows.help")) {
 						sender.sendMessage(ChatColor.GREEN + "----Command reference----");
 						sender.sendMessage(ChatColor.GREEN + "/ma list - lists all config values");
 						sender.sendMessage(ChatColor.GREEN + "/ma set <config-key-name> <value> - temporarily set and test different config values");
 						sender.sendMessage(ChatColor.GREEN + "/ma help - shows this page");
 						sender.sendMessage(ChatColor.GREEN + " ");
 						sender.sendMessage(ChatColor.GREEN + "Acceptable <config-key-names> and values: ");
 						sender.sendMessage(ChatColor.GREEN + "base-damage (double), base-crit (double), base-massive (double), base-crouch (double),");
 						sender.sendMessage(ChatColor.GREEN + "base-crit-chance (integer), base-massive-chance (integer),");
 						sender.sendMessage(ChatColor.GREEN + "allow-crit (boolean), allow-penalty (boolean)");
 						sender.sendMessage(ChatColor.GREEN + "-------------------------");
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "MoArrows command error: command not recognized");
 						sender.sendMessage(ChatColor.RED + "Do you have permission to use this?");
 					}
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED + "MoArrows command error: too few arguments");
 					sender.sendMessage(ChatColor.RED + "See /ma help for more info.");
 					return true;
 				}
 			}
 		} else {
 			if(cmd.getName().equalsIgnoreCase("moarrows")){ // If the player typed /basic then do the following...
 				if (args.length >= 1) {
 					if (args[0].equalsIgnoreCase("list")) {
 						if (args.length < 2) {
 							sender.sendMessage(ChatColor.GREEN + "----Current config values----");
 							sender.sendMessage(ChatColor.GREEN + "Base bow damage multiplier: *" + moArrows.baseDamageMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Crouch damage multiplier: *" + moArrows.baseCrouchMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Crit damage multiplier: *" + moArrows.baseCritMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Crit chance percentage: " + moArrows.baseCritChance + "%");
 							sender.sendMessage(ChatColor.GREEN + "Massive damage multiplier: *" + moArrows.baseMassiveMultiplier);
 							sender.sendMessage(ChatColor.GREEN + "Massive chance percentage: " + moArrows.baseMassiveChance + "%");
 							sender.sendMessage(ChatColor.GREEN + "Allow critical hits: " + moArrows.allowCrits);
 							sender.sendMessage(ChatColor.GREEN + "Allow armor penalty: " + moArrows.allowArmorPenalty);
 							sender.sendMessage(ChatColor.GREEN + "---------------------------");
 						} else {
 							sender.sendMessage(ChatColor.RED + "MoArrows command error: only one page");
 							sender.sendMessage(ChatColor.RED + "Use /ma list");
 						}
 					}
 					else if (args[0].equalsIgnoreCase("set") && player.hasPermission("multiarrow.set")) {
 						if (args.length < 2) {
 							
 						} else {
 							sender.sendMessage(ChatColor.RED + "MoArrows command error: nothing to list");
 							sender.sendMessage(ChatColor.RED + "Use /ma list <all|[config variable name]>");
 						}
 					}
 					else if (args[0].equalsIgnoreCase("help") && player.hasPermission("multiarrow.help")) {
 						if (args.length < 2) {
 							
 						} else {
 							sender.sendMessage(ChatColor.RED + "MoArrows command error: nothing to list");
 							sender.sendMessage(ChatColor.RED + "Use /ma list <all|[config variable name]>");
 						}
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "MoArrows command error: command not recognized");
 						sender.sendMessage(ChatColor.RED + "Do you have permission to use this?");
 					}
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED + "MoArrows command error: too few arguments");
 					sender.sendMessage(ChatColor.RED + "See /ma help for more info.");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isInteger( String input )  
 	{  
 	   try  
 	   {  
 	      Integer.parseInt( input );  
 	      return true;  
 	   }  
 	   catch( Exception e )  
 	   {  
 	      return false;  
 	   }  
 	} 
 	
 	public boolean isDouble( String input )  
 	{  
 	   try  
 	   {  
 	      Double.parseDouble( input );  
 	      return true;  
 	   }  
 	   catch( Exception e )  
 	   {  
 	      return false;  
 	   }  
 	} 
 	
 	public boolean isBoolean( String input )  
 	{  
 	   try  
 	   {  
 	      Boolean.parseBoolean( input );  
 	      return true;  
 	   }  
 	   catch( Exception e )  
 	   {  
 	      return false;  
 	   }  
 	} 
 }
