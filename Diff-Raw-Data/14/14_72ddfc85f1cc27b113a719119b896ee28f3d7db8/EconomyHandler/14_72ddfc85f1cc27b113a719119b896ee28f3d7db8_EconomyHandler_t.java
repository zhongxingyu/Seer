 package net.mayateck.OldEdenCore;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class EconomyHandler implements CommandExecutor {
 	private OldEdenCore plugin;
 	 
 	public EconomyHandler(OldEdenCore plugin) {
 		this.plugin = plugin;
 	}
  
 	public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
 		String name = s.getName();
 		if (cmd.getName().equalsIgnoreCase("money")){
 			if (!(s instanceof Player)){
 				s.sendMessage("The economy commands are player-executible only.");
 			} else {
 				if (args.length==0){
					if (s.hasPermission("eden.eco.check")){
						int funds = plugin.getConfig().getInt("players."+name+".funds");
						s.sendMessage(OldEdenCore.serverHead+"You have "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+".");
						return true;
					} else {
						s.sendMessage(OldEdenCore.head+ChatColor.RED+"You don't have permission to do that!");
						return true;
					}
 				} else {
 					if (args[0].equalsIgnoreCase("admin")){
 						if (args.length==1){
 							s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid parameter number. Usage: /money admin [args]");
 							return true;
 						} if (args[1].equalsIgnoreCase("set")){
 							if (args.length==4){
 								if (plugin.getConfig().contains("players."+args[2]) && s.hasPermission("eden.eco.set")){
 									int funds = Integer.parseInt(args[3]);
 									plugin.getConfig().set("players."+args[2]+".funds", funds);
 									plugin.saveConfig();
 									s.sendMessage(OldEdenCore.head+"Success! "+ChatColor.BLUE+args[2]+ChatColor.RESET+" now has "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+".");
 									return true;
 								} else {
 									s.sendMessage(OldEdenCore.head+ChatColor.RED+"Player not found or invalid permissions.");
 									return true;
 								}
 							} else {
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid parameter number.");
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Usage: /money admin set [player] [amount]");
 								return true;
 							}
 						} else if (args[1].equalsIgnoreCase("add")){
 							if (args.length==4){
 								if (plugin.getConfig().contains("players."+args[2]) && s.hasPermission("eden.eco.add")){
 									int funds = plugin.getConfig().getInt("players."+args[2]+".funds") + Integer.parseInt(args[3]);
 									plugin.getConfig().set("players."+args[2]+".funds", funds);
 									plugin.saveConfig();
 									s.sendMessage(OldEdenCore.head+"Success! "+ChatColor.BLUE+args[2]+ChatColor.RESET+" now has "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+".");
 									return true;
 								} else {
 									s.sendMessage(OldEdenCore.head+ChatColor.RED+"Player not found or invalid permissions.");
 									return true;
 								}
 							} else {
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid parameter number.");
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Usage: /money admin add [player] [amount]");
 								return true;
 							}
 						} else if (args[1].equalsIgnoreCase("subtract")){
 							if (args.length==4){
 								if (plugin.getConfig().contains("players."+args[2]) && s.hasPermission("eden.eco.add")){
 									int funds = plugin.getConfig().getInt("players."+args[2]+".funds") - Integer.parseInt(args[3]);
 									// WARNING! THIS CAN CREATE NEGATIVE VALUES! CAREFUL WHEN SUBTRACTING!
 									plugin.getConfig().set("players."+args[2]+".funds", funds);
 									plugin.saveConfig();
 									s.sendMessage(OldEdenCore.head+"Success! "+ChatColor.BLUE+args[2]+ChatColor.RESET+" now has "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+".");
 									return true;
 								} else {
 									s.sendMessage(OldEdenCore.head+ChatColor.RED+"Player not found or invalid permissions.");
 									return true;
 								}
 							} else {
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid parameter number.");
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Usage: /money admin add [player] [amount]");
 								return true;
 							}
 						} else if (args[1].equalsIgnoreCase("check")){
 							if (args.length==2){
 								if (plugin.getConfig().contains("players."+args[1]) && s.hasPermission("eden.eco.check")){
 									int funds = plugin.getConfig().getInt("players."+args[1]+".funds");
 									s.sendMessage(OldEdenCore.head+ChatColor.BLUE+args[2]+ChatColor.RESET+" has "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+".");
 									return true;
 								} else {
 									s.sendMessage(OldEdenCore.head+ChatColor.RED+"Player not found or invalid permissions.");
 									return true;
 								}
 							} else {
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid parameter number.");
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"Usage: /money check [player]");
 								return true;
 							}
 						} else {
 							s.sendMessage(OldEdenCore.head+ChatColor.RED+"Unknown sub-command.");
 							return true;
 						}
 					} else if(args[0].equalsIgnoreCase("pay")) {
 						if (args.length==3){
 							if (s.hasPermission("eden.eco.pay")){
 								int senderFunds = plugin.getConfig().getInt("players."+name+".funds");
 								int funds = Integer.parseInt(args[2]);
 								if (funds<=senderFunds){
 									if (plugin.getConfig().contains("players."+args[1])){
 										plugin.getConfig().set("players."+name+".funds", senderFunds-funds);
 										int pFunds = plugin.getConfig().getInt("players."+args[1]+".funds");
 										plugin.getConfig().set("players."+args[1]+".funds", pFunds+funds);
 										plugin.saveConfig();
 										s.sendMessage(OldEdenCore.head+"Success! Sent "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+" to "+ChatColor.BLUE+args[1]+ChatColor.RESET+".");
 										Player target = (Bukkit.getServer().getPlayer(args[1]));
 										if (!(target==null)){
 											target.sendMessage(OldEdenCore.head+"You recieved "+ChatColor.BLUE+funds+" Bits"+ChatColor.RESET+" from "+ChatColor.BLUE+name+ChatColor.RESET+".");
 										}
 									} else {
 										s.sendMessage(OldEdenCore.head+"That player doesn't exist.");
 									}
 								} else {
									s.sendMessage(OldEdenCore.head+"You can't give what you don't have!");
 								}
 							} else {
 								s.sendMessage(OldEdenCore.head+ChatColor.RED+"You do not have permission to pay other people.");
 							}
 						} else {
 							s.sendMessage(OldEdenCore.head+ChatColor.RED+"Invalid arguments.");
 							s.sendMessage(OldEdenCore.head+ChatColor.RED+"Usage: /money pay [player] [amount].");
 						}
 						return true;
 					} else {
 						s.sendMessage(OldEdenCore.head+ChatColor.RED+"Unknown sub-command.");
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
