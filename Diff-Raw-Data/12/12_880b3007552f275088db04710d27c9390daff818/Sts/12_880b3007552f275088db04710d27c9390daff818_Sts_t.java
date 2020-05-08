 package net.othercraft.steelsecurity.commands;
 
 import net.othercraft.steelsecurity.Main;
 import net.othercraft.steelsecurity.listeners.SpectateManager;
 import net.othercraft.steelsecurity.utils.PlayerConfigManager;
 import net.othercraft.steelsecurity.utils.SSCmdExe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class Sts extends SSCmdExe {
 	
 	Main plugin;
 	
	SpectateManager spm;
 
 	PermissionManager pex = PermissionsEx.getPermissionManager();
 
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
 			if (args[0].equalsIgnoreCase("checkperm")){
 				if (sender.hasPermission("steelsecurity.commands.checkperm")) {
 					if (args.length<3) {
 						sender.sendMessage("Not enough arguments!");
 						sender.sendMessage("Usage: /sts checkperm <player> <permission>");
 					}
 					else if (args.length>4) {
 						sender.sendMessage("Too many arguments!");
 						sender.sendMessage("Usage: /sts checkperm <player> <permission>");
 					}
 					else {
 						String targetname = args[1];
 						OfflinePlayer target = Bukkit.getOfflinePlayer(targetname);
 						String perm = args[2];
 						String yes = (g + target.getName() + " has the permission " + perm);
 						String no = (r + target.getName() + " does not have the permission " + perm);
 						String world = "";
 						String worldname = "";
 						if (args.length==4) {
 							world = Bukkit.getWorld(args[3]).toString();
 							worldname = Bukkit.getWorld(args[3]).getName();
 						}
 						if (target.isOnline()) {
 							if (args.length==3) {
 								if (target.getPlayer().hasPermission(perm)) {
 									sender.sendMessage(yes);
 								}
 								else {
 									sender.sendMessage(no);
 								}
 							}
 							else {
 								if (pex.has(target.getName(), perm, world)){
 									sender.sendMessage(yes + " in world " + worldname);
 								}
 								else {
 									sender.sendMessage(no + " in world " + worldname);
 								}
 							}
 						}
 						else {
 							if (Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
 								if (args.length==4){
 									if (pex.has(target.getName(), perm, world)){
 										sender.sendMessage(yes + " in world " + worldname);
 									}
 									else {
 										sender.sendMessage(no + " in world " + worldname);
 									}
 								}
 								else {
 									sender.sendMessage(r + "Please define a a world when checking a permission of an offline player.");
 								}
 							}
 							else {
 								sender.sendMessage(r + "Please install PermissionsEx to use this command with references to offline players.");
 							}
 						}
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
 			if (args[0].equalsIgnoreCase("gamemode")) {
 				if (sender.hasPermission("steelsecurity.commands.gamemode")) {
 					new GameModeCmdCatch(noperm, plugin).stsgamemode(sender, args);
 				}
 				else {
 					sender.sendMessage(noperm);
 				}
 			}
 			if (args[0].equalsIgnoreCase("checkgm")) {
 				if (sender.hasPermission("steelsecurity.commands.checkgm")) {
 					if (args.length==2){
 						String target = Bukkit.getServer().getPlayer(args[2]).getName();
 						FileConfiguration config = PlayerConfigManager.getConfig(target);
 						int gm = config.getInt("GameMode");
 						sender.sendMessage(target + " is in" + GameMode.getByValue(gm).name() + "mode.");
 					}
 				}
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
 			if (args[0].equalsIgnoreCase("spectate")) {
 				if (sender.hasPermission("steelsecurity.commands.spectate")) {
					spm.specCmd(sender, args);
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
 		if (sender.hasPermission("steelsecurity.commands.listop")){
 			sender.sendMessage(g + "/sts listop:" + y + " List ops.");
 		}
 		if (sender.hasPermission("steelsecurity.commands.gamemode")) {
 			sender.sendMessage(g + "/sts gamemode:" + y + " Changes the gamemode of another player.");
 		}
 		if (sender.hasPermission("steelsecurity.commands.checkgm")){
 			sender.sendMessage(g + "/sts checkgm:" + y + " Checks what Game Mode a player is in.");
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
