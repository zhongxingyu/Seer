 package net.othercraft.steelsecurity.commands;
 
 import net.othercraft.steelsecurity.Main;
 import net.othercraft.steelsecurity.utils.SSCmdExe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Sts extends SSCmdExe {
 	Main plugin;
 
	public Sts(String name, Main instance) {
 		super(name, false);
		plugin = instance;
 	}
 	//Defines Chat Colors
 	ChatColor r = ChatColor.RED;
 	ChatColor g = ChatColor.GREEN;
 	ChatColor y = ChatColor.YELLOW;
 	ChatColor w = ChatColor.WHITE;
 	ChatColor b = ChatColor.BLUE;
 	//Defines No Permission String
 	String noperm = (r + "You don't have permission to do this!");
 	//Receives command and takes actions.
 	public void command(CommandSender sender, String[] args) {
 		if (args.length==0) {
 			if (sender.hasPermission("steelsecurity.commands.sts")){
 				sender.sendMessage(g + "This server is running Steel Security");
 				sender.sendMessage(g + "For a list of commands please type /sts help");
 			}
 			else {
 				sender.sendMessage(noperm);
 			}
 		}
 		else if (args.length>0) {
 			if (args[0].equalsIgnoreCase("help")) {
 				if (sender.hasPermission("steelsecurity.commands.help")){
 					if (args.length==1){
 						p1(sender);
 					}
 					else if (args.length==2){
 						if (args[1].equalsIgnoreCase("1")){
 							p1(sender);
 						}
 					}
 					else {
 						sender.sendMessage(r + "Too many arguments!");
 					}
 				}
 				else {
 					sender.sendMessage(noperm);
 				}
 			}
 			if (args[0].equalsIgnoreCase("checkperm")){//FIXME stack error on check for an offline player.
 				if (sender.hasPermission("steelsecurity.commands.checkperm")) {
 					if (args.length<3) {
 						sender.sendMessage("Not enough arguments!");
 						sender.sendMessage("Usage: /sts checkperm <player> <permission>");
 					}
 					else if (args.length>3) {
 						sender.sendMessage("Too many arguments!");
 						sender.sendMessage("Usage: /sts checkperm <player> <permission>");
 					}
 					else {
 						String targetname = args[1];
 						Player target = Bukkit.getPlayer(targetname);
 						String perm = args[2];
 						if (target.hasPermission(perm)) {
 							sender.sendMessage(g + target.getName() + " has the permission " + perm);
 						}
 						else {
 							sender.sendMessage(r + target.getName() + " does not have the permission " + perm);
 						}
 					}
 				}
 				else {
 					sender.sendMessage(noperm);
 				}
 			}
 			if (args[0].equalsIgnoreCase("listgm")){//FIXME add a way to include offline players
 				if (sender.hasPermission("steelsecurity.commands.listgm")) {
 					Player[] p = Bukkit.getOnlinePlayers();
 					int count = p.length;
 					int counter = 0;
 					while (counter<count) {
 						Player rt = p[counter];
 						GameMode gm = rt.getGameMode();
 						if (args.length==2) {
 							if (args[1].equalsIgnoreCase(g.toString())){
 								sender.sendMessage(rt.getName() + ": " + gm);
 							}
 						}
 						else {
 							sender.sendMessage(rt.getName() + ": " + gm);
 						}
 						++counter;
 					}
 				}
 				else {
 					sender.sendMessage(noperm);
 				}
 			}
 			if (args[0].equalsIgnoreCase("listop")){
 				if (sender.hasPermission("steelsecurity.commands.listop")) {
 					String list = "";
 					int total = 0;
 					int online = 0;
 					for(OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()){
 						if(player.isOp()){
 							total = total + 1;
 							if(player.isOnline()){
 								online = online + 1;
 								if (list.equals("")) {
 									list = (list + g + player.getName());
 								}
 								else {
 									list = (list + w + ", " + g + player.getName());
 								}
 							}
 							else {
 								if (list.equals("")) {
 									list = (list + r + player.getName());
 								}
 								else {
 									list = (list + w + ", " + r + player.getName());
 								}
 							}
 						}
 					}
 					if (list.equals("")){
 						sender.sendMessage(r + "Sorry, there appears to be no ops on this server!");
 					}
 					else {
 						sender.sendMessage( w + "There are " + b + online + w + " out of " + b + total + w + " ops online.");
 						sender.sendMessage(list);
 					}
 				}
 				else {
 					sender.sendMessage(noperm);
 				}
 			}
 			if (args[0].equalsIgnoreCase("checkgm")) {
 			}
 			if (args[0].equalsIgnoreCase("reload")) {
 				if (sender.hasPermission("steelsecurity.commands.reload")) {
 					plugin.reloadConfig();
					sender.sendMessage(g + plugin.getConfig().getString("General.Prefix") + ": Config Reloaded.");
 				}
 				else {
 					sender.sendMessage(noperm);
 				}
 			}
 		}
 	}
 	private void p1(CommandSender sender) {
 		if (sender.hasPermission("steelsecurity.commands.sts")){
 			sender.sendMessage(g + "/sts:" + y + " Base Command");
 		}
 		if (sender.hasPermission("steelsecurity.commands.help")){
 			sender.sendMessage(g + "/sts help:" + y + " Displays this help screen.");
 		}
 		if (sender.hasPermission("steelsecurity.commands.checkperm")){
 			sender.sendMessage(g + "/sts checkperm:" + y + " Checks a permmision for another player.");
 		}
 		if (sender.hasPermission("steelsecurity.commands.listgm")){
 			sender.sendMessage(g + "/sts listgm:" + y + " List online players with their gamemode.");
 		}
 		if (sender.hasPermission("steelsecurity.commands.listop")){
 			sender.sendMessage(g + "/sts listop:" + y + " List ops.");
 		}
 		if (sender.hasPermission("steelsecurity.commands.reload")){
 			sender.sendMessage(g + "/sts reload:" + y + " Reloads config.");
 		}
 	}
 	@Override
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		command(sender, args);
 		return true;
 	}
 }
