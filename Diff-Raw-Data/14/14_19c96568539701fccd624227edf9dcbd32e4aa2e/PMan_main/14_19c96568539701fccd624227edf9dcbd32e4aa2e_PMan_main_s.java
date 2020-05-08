 package com.github.Mephilis7.PlayerManager;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import com.github.Mephilis7.PlayerManager.VAR;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PMan_main extends JavaPlugin {
 	
 	private static Boolean online = false;
 	private PMan_CmdRules RulesExecutor;
 	private PMan_IPLogger ip = new PMan_IPLogger();
 	private PMan_RulesEventHandler EventHandler = new PMan_RulesEventHandler();
 	ChatColor green = ChatColor.GREEN;
 	ChatColor darkgreen = ChatColor.DARK_GREEN;
 	ChatColor gold = ChatColor.GOLD;
 	ChatColor aqua = ChatColor.AQUA;
 	ChatColor white = ChatColor.WHITE;
 	
 	int configVersion = 3;
 	
 	
 	public void onDisable() {
 		VAR.log.info(VAR.logHeader + "Shutdown.");
 	}
 	
 	@Override
 	public void onEnable() {
 		checkConfig();
 		try {
 			VAR.config.load(VAR.f_config);
 			loadPlayerLog();
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (InvalidConfigurationException e1) {
 			e1.printStackTrace();
 		} catch (Exception e1){
 			e1.printStackTrace();
 		}
 		
 		if (!VAR.config.getBoolean("enable")){
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
 		VAR.logit = VAR.config.getBoolean("logToConsole");
 		if (VAR.config.getBoolean("enableRules")){
 			RulesExecutor = new PMan_CmdRules(this);
 			getCommand("rules").setExecutor(RulesExecutor);
 			getCommand("acceptrules").setExecutor(RulesExecutor);
 			getServer().getPluginManager().registerEvents(this.EventHandler, this);
 		}
 		getServer().getPluginManager().registerEvents(this.ip, this);
 		VAR.log.info(VAR.logHeader + "Ready to manage your players!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		Player playerShowInfo = null;
 		online = false;
 		
 		if ((cmd.getName().equalsIgnoreCase("pman"))){
 			//help page
 				if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))){
 					if (sender.hasPermission("pman.help") || sender.isOp()){
 						sender.sendMessage(gold + "---------------" + green + " PlayerManager [1/2]" + gold + "---------------");
 						sender.sendMessage(gold + "/pman"+white+" - "+darkgreen+"Shows this message.");
 						sender.sendMessage(gold + "/pman hide <player>"+white+" - "+darkgreen+"Hides a player. And I mean hide.");
 						sender.sendMessage(gold + "/pman info <player|ip>"+white+" - "+darkgreen+"Show information about a player.");
 						sender.sendMessage(gold + "/pman list"+white+" - "+darkgreen+"Show all players and their gamemode.");
 						sender.sendMessage(gold + "/pman mute <player>"+white+" - "+darkgreen+"Toggle mute on/off");
 						sender.sendMessage(gold + "/pman set fly <player> <allow|deny>"+white+" - "+darkgreen+"Sets AllowFlight");
 						sender.sendMessage(gold + "/pman set food <player> <amount|full|empty>"+white+" - "+darkgreen+"Sets food level");
 						sender.sendMessage(gold + "/pman set health <player> <amount|full>"+white+" - "+darkgreen+"Sets Health");
 					} else { denied(sender);}
 					return true;
 				}
 				if ((args.length == 1 && args[0].equalsIgnoreCase("2")) || (args.length == 2 && args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("2"))){
 					if (sender.hasPermission("pman.help") || sender.isOp()){
 						sender.sendMessage(gold + "---------------" + green + " PlayerManager [2/2]" + gold + "---------------");
 						sender.sendMessage(gold + "/pman set name <player> <name|reset>"+white+" - "+darkgreen+"Sets Name");
 						sender.sendMessage(gold + "/pman set xp <player> <level>"+white+" - "+darkgreen+"Sets Xp level");
 						sender.sendMessage(gold + "/pman show <player>"+white+" - "+darkgreen+"Shows a hidden player again.");
 						sender.sendMessage(gold + "/pman srtp"+white+" - "+darkgreen+"Sets the point players will be teleported to when they type /acceptrules.");
 						sender.sendMessage(gold + "/pman reload"+white+" - "+darkgreen+"Reloads the config.yml and the PlayerLog.yml");
 						if (VAR.config.getBoolean("enableRules")){
 							sender.sendMessage(gold + "/rules"+white+" - "+darkgreen+"View the server rules.");
 							sender.sendMessage(gold + "/acceptrules"+white+" - "+darkgreen+"Accept the server rules.");
 						}
 						return true;
 					}
 				}
 				if (args.length == 1){
 					//reloading the config.yml and the PlayerLog.yml
 					if (args[0].equalsIgnoreCase("reload")){
 						if (sender.hasPermission("pman.reload") || sender.isOp()){
 							checkConfig();
 							try {
 								loadPlayerLog();
 								VAR.config.load(VAR.f_config);
 							} catch (FileNotFoundException e1) {
 								e1.printStackTrace();
 							} catch (IOException e1) {
 								e1.printStackTrace();
 							} catch (InvalidConfigurationException e1) {
 								e1.printStackTrace();
 							} catch (Exception ex){
 								ex.printStackTrace();
 							}
 							
 							VAR.logit = VAR.config.getBoolean("logToConsole");
 							sender.sendMessage(VAR.Header +ChatColor.GREEN+ "config.yml and PlayerLog.yml reloaded!");
 							if (VAR.logit)
 								if (sender instanceof Player)
 									VAR.log.info(VAR.logHeader + sender.getName() + " reloaded the config.yml");
 						} else { denied(sender);}
 						return true;
 					}
 					//listing all online players with their gamemode
 					if (args[0].equalsIgnoreCase("list")){
 						if (sender.hasPermission("pman.list") || sender.isOp()){
 							if (getServer().getOnlinePlayers().length >= 1){
 								sender.sendMessage(gold + "------------------" + green + " PlayerManager " + gold + "-----------------");
 								for (Player on: getServer().getOnlinePlayers()){
 									sender.sendMessage(ChatColor.DARK_GRAY+on.getDisplayName()+ " (" +ChatColor.GRAY+on.getName()+ ChatColor.DARK_GRAY+")"+white+" - "+ChatColor.DARK_AQUA+ChatColor.BOLD+on.getGameMode());
 								}
 							} else {sender.sendMessage(VAR.Header + ChatColor.AQUA + "Nobody's online :,(");}
 						} else { denied(sender);}
 						return true;
 					}
 				}
 				//show information about a player; check whether the player is online.
 				if (args[0].equalsIgnoreCase("info")){
 					if (sender.hasPermission("pman.info") || sender.isOp()){
 						if (args.length == 2){
 							for (Player infoPlayer: getServer().getOnlinePlayers()){
 								if (infoPlayer.getName().equalsIgnoreCase(args[1])){
 									playerShowInfo = infoPlayer;
 								}
 							}
 							if (playerShowInfo == null){
 								for (Player infoPlayer: getServer().getOnlinePlayers()){
 									String[] infoIp = infoPlayer.getAddress().toString().split(":");
 									if (infoIp[0].equalsIgnoreCase(args[1]) || infoIp[0].equalsIgnoreCase("/" + args[1])){
 										playerShowInfo = infoPlayer;
 									}
 								}
 								if (playerShowInfo == null){
 									sender.sendMessage(VAR.Header + "Could not find the specified player, is he offline?");
 									return true;
 								}
 							}
 							if (playerShowInfo != null){
 								sender.sendMessage(gold + "------------------" + green + " PlayerManager " + gold + "-----------------");
 								String[] Order = VAR.config.getString("order").split(";");
 								int i=0;
 						
 								//showing information about the specified player as specified in the config.yml
 								while (i < Order.length){
 									if (sender.hasPermission("pman.info.name") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Name"))
 											sender.sendMessage(darkgreen + "Name: " + aqua + playerShowInfo.getName());
 									}
 									if (sender.hasPermission("pman.info.ip") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("IP"))
 											sender.sendMessage(darkgreen + "IP Address: " + aqua + playerShowInfo.getAddress());
 									}
 									if (sender.hasPermission("pman.info.lastLogin") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("LastLogin"))
 											sender.sendMessage(darkgreen + "Last Login: "+aqua+VAR.pLog.getString("players."+playerShowInfo.getName()+".lastLogin"));
 									}
 									if (sender.hasPermission("pman.info.lastLogout") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("LastLogout"))
 											sender.sendMessage(darkgreen + "Last Logout: "+aqua+VAR.pLog.getString("players."+playerShowInfo.getName()+".lastLogout"));
 									}
 									if (sender.hasPermission("pman.info.world") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("World"))
 											sender.sendMessage(darkgreen + "World: " + aqua + playerShowInfo.getWorld().getName());
 									}
 									if (sender.hasPermission("pman.info.health") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Health"))
 											sender.sendMessage(darkgreen + "Health: " + aqua + playerShowInfo.getHealth());
 									}
 									if (sender.hasPermission("pman.info.food") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Food"))
 											sender.sendMessage(darkgreen + "Food: " + aqua + playerShowInfo.getFoodLevel());
 									}
 									if (sender.hasPermission("pman.info.xp") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Xp"))
 											sender.sendMessage(darkgreen + "Exp level: " + aqua + playerShowInfo.getLevel());
 									}
 									if (sender.hasPermission("pman.info.gamemode") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("GameMode"))
 											sender.sendMessage(darkgreen + "GameMode: " + aqua + playerShowInfo.getGameMode());
 									}
 									if (sender.hasPermission("pman.info.position") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Position"))
 											sender.sendMessage(darkgreen + "Position:  " + aqua +"X: "+playerShowInfo.getLocation().getBlockX() + "  Z: " +playerShowInfo.getLocation().getBlockZ()+ "  Y: " +playerShowInfo.getLocation().getBlockY());
 									}
 									if (sender.hasPermission("pman.info.distance") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Distance")){
 											if (sender instanceof Player){
 												int x = abs(playerShowInfo.getLocation().getBlockX());
 												int loc = abs(((Player) sender).getPlayer().getLocation().getBlockX());
 												x = abs(x-loc);
 												int y = abs(playerShowInfo.getLocation().getBlockY());
 												loc = abs(((Player) sender).getPlayer().getLocation().getBlockY());
 												y = abs(y-loc);
 												int z = abs(playerShowInfo.getLocation().getBlockZ());
 												loc = abs(((Player) sender).getPlayer().getLocation().getBlockZ());
 												z = abs(z-loc);
 												x = x+y+z;
 												sender.sendMessage(darkgreen + "Distance: " + aqua + x);
 											}
 										} 
 									} 
 									if (sender.hasPermission("pman.info.allowFlight") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("AllowFlight")){
 											if (playerShowInfo.getAllowFlight()){
 											sender.sendMessage(darkgreen + "Is allowed to fly around.");
 											} else { sender.sendMessage(darkgreen + "Is not allowed to fly.");}
 										}
 									} 
 									if (sender.hasPermission("pman.info.realName") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("RealName"))
 											sender.sendMessage(darkgreen + "Real Name: "+ aqua + playerShowInfo.getName());
 									} 
 									if (sender.hasPermission("pman.info.hidden") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Hidden"))
 											sender.sendMessage(darkgreen + "Hidden: "+ aqua + VAR.pLog.getBoolean("players."+playerShowInfo.getName()+".Hidden"));
 									}
 									if (sender.hasPermission("pman.info.mute") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Muted"))
 											sender.sendMessage(darkgreen + "Muted: " + aqua + VAR.pLog.getBoolean("players."+playerShowInfo.getName()+".Muted"));
 									}
 									if (sender.hasPermission("pman.info.rules") || sender.isOp()){
 										if (VAR.config.getBoolean("enableRules")){
 											if (Order[i].equalsIgnoreCase("Rules")){
 												if (VAR.pLog.getString("players."+playerShowInfo.getName()+".Has accepted rules").equalsIgnoreCase("true"))
 													sender.sendMessage(darkgreen +"Has accepted the rules.");
 												else if (VAR.pLog.getString("players."+playerShowInfo.getName()+".Has accepted rules").equalsIgnoreCase("hasTyped"))
 													sender.sendMessage(darkgreen +"Has read the rules, but not accepted them.");
 												else
 													sender.sendMessage(darkgreen +"Has "+ChatColor.RED+"not"+darkgreen+" read the rules yet!");
 											}
 										}
 									}
 									i++;
 								}
 								sender.sendMessage(gold + "--------------------------------------------------");
 								return true;
 							}
 						} else {sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 						sender.sendMessage(ChatColor.BLUE+ "/pman info <player|ip>");
 						}
 					} else { denied(sender);}
 					return true;
 				}
 				//hide a player
 				if (args[0].equalsIgnoreCase("hide")){
 					if (sender.hasPermission("pman.hide") || sender.isOp()){
 						if (args.length == 2){
 							for (Player on: Bukkit.getServer().getOnlinePlayers()){
 								if (on.getName().equals(args[1]));
 									online = true;
 							}
 							if (!online){
 								sender.sendMessage(VAR.Header + ChatColor.RED + "Could not find the specified player.");
 								return true;
 							} else {
 								Player p = getServer().getPlayer(args[2]);
 								for (Player p2: getServer().getOnlinePlayers()){
 									p2.hidePlayer(p);
 								}
 								if (VAR.logit)
 									VAR.log.info(VAR.logHeader + sender.getName() + " has hidden " + p.getName());
 								try {
 									VAR.pLog.set("players."+args[2]+".Hidden", Boolean.valueOf(true));
 									VAR.pLog.save(VAR.f_player);
 									loadPlayerLog();
 								} catch (Exception ex){
 									ex.printStackTrace();
 								}
 								return true;
 							}
 						} else { 
 							sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 							sender.sendMessage(ChatColor.BLUE + "/pman hide <player>");
 						return true;
 						}
 					} else { denied(sender);}
 				}
 				//show a player
 				if (args[0].equalsIgnoreCase("show")){
 					if (sender.hasPermission("pman.hide") || sender.isOp()){
 						if (args.length == 2){
 							for (Player on: Bukkit.getServer().getOnlinePlayers()){
 								if (on.getName().equals(args[1]));
 									online = true;
 							}
 							if (!online){
 								sender.sendMessage(VAR.Header + ChatColor.RED + "Could not find the specified player.");
 								return true;
 							} else {
 								Player p = getServer().getPlayer(args[2]);
 								for (Player p2: getServer().getOnlinePlayers()){
 									p2.showPlayer(p);
 								}
 								if (VAR.logit)
 									VAR.log.info(VAR.logHeader + sender.getName() + " has un-hidden " + p.getName());
 								try {
 									VAR.pLog.set("players."+args[2]+".Hidden", Boolean.valueOf(false));
 									VAR.pLog.save(VAR.f_player);
 									loadPlayerLog();
 								} catch (Exception ex){
 									ex.printStackTrace();
 								}
 							}
 						} else { 
 							sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 							sender.sendMessage(ChatColor.BLUE + "/pman show <player>");
 							return true;
 						}
 					} else { denied(sender);}
 				}
 				//mute a player
 				if (args[0].equalsIgnoreCase("mute")){
 					if (sender.hasPermission("pman.mute")){
 						if (args.length == 2){
 							for (Player on: Bukkit.getServer().getOnlinePlayers()){
 								if (on.getName().equals(args[1]));
 									online = true;
 							}
 							if (!online){
 								sender.sendMessage(VAR.Header+ChatColor.RED+"Could not find the specified player.");
 								return true;
 							} else {
 								try{
 									loadPlayerLog();
 									Boolean muted = VAR.pLog.getBoolean("players."+args[1]+".Muted");
 									
 									if (muted){
 										VAR.pLog.set("players."+args[1]+".Muted", Boolean.valueOf(false));
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has allowed " + args[1] + " to speak");
 									} else {
 										VAR.pLog.set("players."+args[1]+".Muted", Boolean.valueOf(true));
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has muted " + args[1]);
 									}
 									
 									VAR.pLog.save(VAR.f_player);
 									loadPlayerLog();
 								} catch (Exception ex){
 									ex.printStackTrace();
 								}
 								return true;
 							}
 						} else { 
 							sender.sendMessage(VAR.Header+ChatColor.RED+"False amount of arguments!");
 							sender.sendMessage(ChatColor.BLUE+"/pman mute <player>");
 							return true;
 						}
 					} else { denied(sender);}
 				}
 				// Defining /pman set command
 				if (args[0].equalsIgnoreCase("set")){
 					boolean found = false;
 					if (sender.hasPermission("pman.set") || sender.isOp()){
 						if (args.length == 1){
 							sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 							sender.sendMessage(ChatColor.BLUE + "Type /pman for help.");
 						}
 						//set AllowFlight
 						if (args[1].equalsIgnoreCase("fly")){
 							found = true;
 							if (sender.hasPermission("pman.set.fly") || sender.isOp()){
 								if (args.length == 4){
 									checkPlayer(sender, args);
 									if (!online){
 										sender.sendMessage(VAR.Header+ChatColor.RED+"Could not find specified player.");
 									} else if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("allow")){
 											getServer().getPlayer(args[2]).setAllowFlight(true);
 											sender.sendMessage(VAR.Header + darkgreen + args[2] + " is now allowed to fly.");
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has allowed " +args[2]+ " to fly!");
 											try {
 												VAR.pLog.set("players."+args[2]+".Allowed to fly", Boolean.valueOf(true));
 												VAR.pLog.save(VAR.f_player);
 												loadPlayerLog();
 											} catch (Exception ex){
 												ex.printStackTrace();
 											}
 											} else if (args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("deny")){
 												getServer().getPlayer(args[2]).setAllowFlight(false);
 												sender.sendMessage(VAR.Header + darkgreen + args[2] + " is now disallowed to fly.");
 												if (VAR.logit)
 													VAR.log.info(VAR.logHeader + sender.getName() + " has disallowed " +args[2]+ " to fly!");
 												try{
 													VAR.pLog.set("players."+args[2]+".Allowed to fly", Boolean.valueOf(false));
 													VAR.pLog.save(VAR.f_player);
 													loadPlayerLog();
 												} catch (Exception ex){
 													ex.printStackTrace();
 												}
 											} else { sender.sendMessage(VAR.Header + ChatColor.RED + "Usage: /pman set fly <player> allow|deny");}
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 										 sender.sendMessage(ChatColor.BLUE + "/pman set fly <player> <allow|deny>");
 								}
 							} else { denied(sender);}
 						}
 						//Set health
 						if (args[1].equalsIgnoreCase("health")){
 							found = true;
 							if (sender.hasPermission("pman.set.health") || sender.isOp()){
 								if (args.length == 4){
 									checkPlayer(sender, args);
 									if (!online){
 										sender.sendMessage(VAR.Header+ChatColor.RED + "Could not find specified player.");
 									} else{
 										if (args[3].equalsIgnoreCase("full")){
 											getServer().getPlayer(args[2]).setHealth(20);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has filled up the health of " +args[2]);
 										} else { getServer().getPlayer(args[2]).setHealth(Integer.parseInt(args[3]));
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has set the health of " +args[2]+ " to " +args[3]);
 										}
 									}
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 								 sender.sendMessage(ChatColor.BLUE + "/pman set health <player> <amount>");
 								}
 							} else { denied(sender);}
 						}
 						//Set food level
 						if (args[1].equalsIgnoreCase("food")){
 							found = true;
 							if (sender.hasPermission("pman.set.food") || sender.isOp()){
 								if (args.length == 4){
 									checkPlayer(sender, args);
 									if (!online){
 										sender.sendMessage(VAR.Header+ChatColor.RED+"Could not find specified player.");
 									} else {
 										if (args[3].equalsIgnoreCase("full")){
 											getServer().getPlayer(args[2]).setFoodLevel(20);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has filled up the food bar of "+args[2]);
 										} else if (args[3].equalsIgnoreCase("empty")){
 											getServer().getPlayer(args[2]).setFoodLevel(0);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has emptied the food bar of "+args[2]);
 										} else {
 											getServer().getPlayer(args[2]).setFoodLevel(Integer.parseInt(args[3]));
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has set the food level of "+args[2]+" to "+args[3]);
 										}
 										sender.sendMessage(VAR.Header + darkgreen + "The player's food level has been set.");
 									}
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 								 sender.sendMessage(ChatColor.BLUE + "/pman set food <player> <amount|full>");
 								}
 							} else { denied(sender);}
 						}
 						//Set EXP level
 						if (args[1].equalsIgnoreCase("xp")){
 							found = true;
 							if (sender.hasPermission("pman.set.xp") || sender.isOp()){
 								if (args.length == 4){
 									checkPlayer(sender, args);
 									if (!online){
 										sender.sendMessage(VAR.Header+ChatColor.RED+"Could not find specified player.");
 									} else { getServer().getPlayer(args[2]).setLevel(Integer.valueOf(args[3]));
 									sender.sendMessage(VAR.Header + darkgreen + "The player's EXP level has been set.");
 									if (VAR.logit)
 										VAR.log.info(VAR.logHeader + sender.getName() + " has set the EXP level of "+args[2]+" to "+args[3]);
 									}
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 								 sender.sendMessage(ChatColor.BLUE + "/pman set xp <player> <level>");
 								}
 							} else { denied(sender);}
 						}
 						//Set Display- and ListName
 						if (args[1].equalsIgnoreCase("name")){
 							found = true;
 							if (sender.hasPermission("pman.set.name") || sender.isOp()){
 								if (args.length == 4){
 									if (!getServer().getPlayer(args[2]).hasPlayedBefore()){
 										sender.sendMessage(VAR.Header+ChatColor.RED+"Could not find specified player.");
 										return true;
 									}
 									if (args[3].equalsIgnoreCase("reset")){
 										getServer().getPlayer(args[2]).setDisplayName(args[2]);
 										getServer().getPlayer(args[2]).setPlayerListName(args[2]);
 										sender.sendMessage(VAR.Header + darkgreen+ "The player's name has been set to default.");
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has reset the in-game name of " +args[2]);
 										try {
 											loadPlayerLog();
 											VAR.pLog.set("players."+args[2]+".Displayed Name", getServer().getPlayer(args[2]).getName());
 											VAR.pLog.save(VAR.f_player);
 											VAR.config.load(VAR.f_config);
 										} catch (Exception ex){
 											ex.printStackTrace();
 										}
 										return true;
 									}
 									getServer().getPlayer(args[2]).setDisplayName(args[3]);
 									getServer().getPlayer(args[2]).setPlayerListName(args[3]);
 									sender.sendMessage(VAR.Header + darkgreen + "The player's name has been set.");
 									if (VAR.logit)
 										VAR.log.info(VAR.logHeader + sender.getName() + " has set the in-game name of "+args[2]+" to "+args[3]);
 									try {
 										loadPlayerLog();
 										VAR.pLog.set("players."+args[2]+".Displayed Name", args[3]);
 										VAR.pLog.save(VAR.f_player);
 										VAR.config.load(VAR.f_config);
 									} catch (Exception ex){
 										ex.printStackTrace();
 									}
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 								sender.sendMessage(ChatColor.BLUE+"/pman set name <player> <name>");
 								}
 							}
 						}
 						if (found)
 							return true;
 						sender.sendMessage(VAR.Header + ChatColor.RED + "Your arguments have not been recognized.");
 						sender.sendMessage(VAR.Header + ChatColor.RED + "Type /pman for more information.");
 					} else { denied(sender);}
 				}
 				//Set /acceptrules SpawnPoint
 				if (args[0].equalsIgnoreCase("srtp")){
 					if (sender.hasPermission("pman.rulestp") || sender.isOp()){
 						if (sender instanceof Player){
 							try{
 								checkConfig();
 								VAR.config.set("RulesTpWorld", ((Player) sender).getWorld().getName());
 								VAR.config.set("RulesTpX", ((Player) sender).getLocation().getX());
 								VAR.config.set("RulesTpY", ((Player) sender).getLocation().getY());
 								VAR.config.set("RulesTpZ", ((Player) sender).getLocation().getZ());
 								VAR.config.set("RulesTpPitch", ((Player) sender).getLocation().getPitch());
 								VAR.config.set("RulesTpYaw", ((Player) sender).getLocation().getYaw());
 								VAR.config.set("RulesTeleport", Boolean.valueOf(true));
 								VAR.config.save(VAR.f_config);
 								VAR.config.load(VAR.f_config);
 								update();
 							} catch (Exception ex){
 								ex.printStackTrace();
 							}
 							sender.sendMessage(VAR.Header +green+"Teleportation point set.");
 							return true;
 						} else sender.sendMessage(ChatColor.YELLOW+"Sorry, but you have to be a player to set the TP point.");
 					} else denied(sender);
 					return true;
 				}
 				sender.sendMessage(VAR.Header + ChatColor.RED +"False amount of arguments! Type /pman for help.");
 		}return true;
 		
 	}
 	public void checkConfig(){
 		new File(VAR.directory).mkdir();
 		VAR.config = new YamlConfiguration();
 		if (!VAR.f_config.exists()){
 			update();
 		} else {
 			try {
 				VAR.config.load(VAR.f_config);
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			} catch (InvalidConfigurationException e1) {
 				e1.printStackTrace();
 			}
 			if (VAR.config.getInt("version") != configVersion){
 				update();
 				VAR.log.info(VAR.logHeader + "Config.yml updatet to version " + configVersion);
 			}
 		}
 	}
 	void denied(CommandSender sender){
 		sender.sendMessage(VAR.Header + ChatColor.RED + "You don't have permission to use that command.");
 	}
 	public void update(){
 		Boolean logConsole = VAR.config.getBoolean("logToConsole");
 		String reset = VAR.config.getString("reset", "Fly;Hidden");
 		Boolean cJQ = VAR.config.getBoolean("customJQ", true);
		String jmsg = VAR.config.getString("joinMsg", "'&aHello %NAME!'");
		String jmsgO = VAR.config.getString("joinMsgOther", "'&bPlayer &e%NAME &b(&c%IP&b) has connected.'");
		String qmsg = VAR.config.getString("quitMsg", "'&e%NAME has left the game.'");
		String mmsg = VAR.config.getString("mutedMsg", "'&cYou have been muted.'");
 		String order = VAR.config.getString("order", "Name;IP;World;Xp;Muted");
 		Boolean eRule = VAR.config.getBoolean("enableRules", true);
		String REXCMD = VAR.config.getString("RulesExCmd", "'give %NAME stone_sword 1|say WELCOME %NAME TO THE SERVER!'");
 		String rRules = "";
 		int i = 1;
 		if (!VAR.config.isSet("Rules1")){
			rRules = "'&3---- &bold%SERVERNAME Rules&reset ----'--newLine--'&1[1] &cDo not grief.'--newLine--'&1[2] &cBe polite.'--newLine--'&1[3] &aHave fun :D'";
 		} else {
 			while (VAR.config.isSet("Rules"+i)){
 			rRules = rRules + VAR.config.getString("Rules"+i)+"--newLine--";
 			i++;
 			}
 		}
 		String[] outRules = rRules.split("--newLine--");
 		String PreventRNA = VAR.config.getString("PreventNotAccepted", "Move[10];DamageOthers;PickUpDrops;BlockBreak");
 		String RNAMsg = VAR.config.getString("RulesNotAcceptedMsg", "&cYou are not allowed to do this until you accepted the server rules! Type &2/acceptrules&c!");
 		String RNADSMsg = VAR.config.getString("RulesNotAcceptedDmgSelfMsg", "&eThis player has not accepted the rules yet. Let him live until then ;)");
 		Boolean RulesTp = VAR.config.getBoolean("RulesTeleport", false);
 		String RTPW = VAR.config.getString("RulesTpWorld", "world");
 		double RTPX = VAR.config.getDouble("RulesTpX", 0.0);
 		double RTPY = VAR.config.getDouble("RulesTpY", 64.0);
 		double RTPZ = VAR.config.getDouble("RulesTpZ", 0.0);
 		double RTPP = VAR.config.getDouble("RulesTpPitch", 0.0);
 		double RTPYaw = VAR.config.getDouble("RulesTpYaw", 0.0);
 		Boolean bBlock = VAR.config.getBoolean("enableBotBlock", false);
 		Boolean logDouble = VAR.config.getBoolean("logDuplicatedIps", false);
 		String punish = VAR.config.getString("punishment", "kick");
 		String map = VAR.config.getString("supportReiMinimap", "false");
 		
 		try {
 			VAR.f_config.createNewFile();
 			FileWriter fstream = new FileWriter(VAR.f_config);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("# Here are all variables you may need for the messages:\n");
 			out.write("# &0 - Black          &6 - Gold          &c - Red\n");
 			out.write("# &1 - Dark Blue      &7 - Gray          &d - Light Purple\n");
 			out.write("# &2 - Dark Green     &8 - Dark Gray     &e - Yellow\n");
 			out.write("# &3 - Dark Aqua      &9 - Blue          &f - White\n");
 			out.write("# &4 - Dark Red       &a - Green         &bold - Bold\n");
 			out.write("# &5 - Dark Purple    &b - Aqua          &italic - Italic\n");
 			out.write("# &strike - Striked   &under - Underline &magic - Magic       &reset - Reset\n");
 			out.write("# %NAME  - %IP  - %WORLD  - %GAMEMODE  - %ONLINEPLAYERS  - %MAXPLAYERS\n");
 			out.write("# %ONLINELIST  - %SERVERNAME\n\n\n\n");
 			
 			
 			out.write("# Enable the plugin?\n");
 			out.write("enable: true\n");
 			out.write("# Log usage of commands to console?\n");
 			out.write("logToConsole: "+logConsole+"\n");
 			out.write("# Do you want player modifications (name,allowFly,...) to be reset\n");
 			out.write("# when the player logs out and back in? Separate with ';'\n");
 			out.write("# All modifications not specified here will be re-enabled.\n");
 			out.write("# Fly: Reset allowing/denying to fly.\n");
 			out.write("# Name: Reset the player's name.\n");
 			out.write("# Hidden: Show the player again.\n");
 			out.write("# Muted: Allow the player to speak if he/she has been muted.\n");
 			out.write("# Example: Fly;Hidden\n");
 			out.write("reset: "+reset+"\n\n\n");
 			
 			
 			out.write("# Do you want custom join/quit messages?\n");
 			out.write("customJQ: "+cJQ+"\n");
 			out.write("# Set your join message here. MUST BE SURROUNDED BY '\n");
 			out.write("joinMsg: '"+jmsg.trim()+"'\n");
 			out.write("# This is the message other players will see. MUST BE SURROUNDED BY '\n");
 			out.write("joinMsgOther: '"+jmsgO.trim()+"'\n");
 			out.write("# The quit message when somebody leaves your server. MUST BE SURROUNDED BY '\n");
 			out.write("quitMsg: '"+qmsg.trim()+"'\n");
 			out.write("# The message a muted player is shown when he tries to chat. MUST BE SURROUNDED BY '\n");
 			out.write("mutedMsg: '"+mmsg.trim()+"'\n\n\n");
 			
 			
 			out.write("# Define the order of the information shown on /pinfo here. Separate the words with ';'\n");
 			out.write("# Name: The player's name\n");
 			out.write("# IP: The player's IP address\n");
 			out.write("# LastLogin: The date and time the player has joined the last time.\n");
 			out.write("# LastLogout: The date and time the player has left the last time.\n");
 			out.write("# World: The world the player is in\n");
 			out.write("# Health: The player's health\n");
 			out.write("# Food: The player's food level\n");
 			out.write("# Xp: The player's Xp level\n");
 			out.write("# GameMode: The player's gamemode\n");
 			out.write("# Position: The player's position\n");
 			out.write("# Distance: The distance from the command executor to the player\n");
 			out.write("# AllowFlight: Whether the player is allowed to fly or not.\n");
 			out.write("# Hidden: Whether the player is hidden or not.\n");
 			out.write("# Muted: Whether the player is muted or not.\n");
 			out.write("# Rules: Whether the player has read and accepted the rules or not.\n");
 			out.write("# Example: Name;IP;World;Xp;Muted\n");
 			out.write("order: "+order+"\n\n\n");
 			
 			
 			out.write("# Should the /rules and /acceptrules commands be enabled?\n");
 			out.write("enableRules: "+eRule+"\n");
 			out.write("# This option determines the commands which are executed when someone types /acceptrules\n");
 			out.write("# Separate the commands with '|' and separate the arguments with spaces.\n");
 			out.write("# The % Variables can be used here, i.e. %NAME, &IP, %WORLD...\n");
 			out.write("# Example: give %NAME stone_sword 1|say WELCOME %NAME TO THE SERVER!\n");
 			out.write("RulesExCmd: '"+REXCMD.trim()+"'\n");
 			out.write("# Write your rules here. It does not matter how long they are, just make sure to\n");
 			out.write("# always increase the Rules[number] by one. You can have Rules1 - Rules5832, but not Rules2 - Rules4!\n");
 			out.write("# Each of those Rules[number] will be written on a new line.\n");
 			out.write("# It's higly recommended to surround them with '\n");
 			i = 0;
 			while (i < outRules.length){
 				out.write("Rules"+(i+1)+": '" + outRules[i].trim() +"'\n");
 				i++;
 			}
 			out.write("\n");
 			
 			out.write("# Prevent players who have not accepted the rules yet from doing the following actions.\n");
 			out.write("# As always, separate those actions with ';'\n");
 			out.write("# BlockBreak: Prevent them from breaking blocks.\n");
 			out.write("# BlockPlace: Prevent them from placing blocks.\n");
 			out.write("# Chat: Prevent them from chatting.\n");
 			out.write("# DamageSelf: Prevent them from being hurt by mobs or other players.\n");
 			out.write("# DamageOthers: Prevent them from hurting any other player or mob.\n");
 			out.write("# Move[]: Keep them in a defined radius from the spawn point.\n");
 			out.write("# PickUpDrops: Don't let them pick up any items.\n");
 			out.write("# Example: Move[10];DamageOthers;PickUpDrops;BlockBreak\n");
 			out.write("PreventNotAccepted: '"+PreventRNA+"'\n");
 			out.write("# This is the message your players will be shown if they try to do anything you've specified above, except picking up drops.\n");
 			out.write("RulesNotAcceptedMsg: '"+RNAMsg+"'\n");
 			out.write("# This is the message a player will be shown when he tries to damage a player who has not accepted the rules yet.\n");
 			out.write("# This only has an effect if you included DamageSelf above.\n");
 			out.write("RulesNotAcceptedDmgSelfMsg: '"+RNADSMsg+"'\n\n");
 			
 			out.write("# Enable teleporting when typing /acceptrules for the first time?\n");
 			out.write("RulesTeleport: "+RulesTp+"\n");
 			out.write("# The position they will be teleported to. Better do this in-game by typing /pman srtp.\n");
 			out.write("RulesTpWorld: "+RTPW+"\n");
 			out.write("RulesTpX: "+RTPX+"\n");
 			out.write("RulesTpY: "+RTPY+"\n");
 			out.write("RulesTpZ: "+RTPZ+"\n");
 			out.write("RulesTpPitch: "+RTPP+"\n");
 			out.write("RulesTpYaw: "+RTPYaw+"\n\n\n");
 			
 			
 			out.write("# Should BotBlocking be enabled?\n");
 			out.write("enableBotBlock: "+bBlock+"\n");
 			out.write("# Should two players with the same IP be logged in a separated file?\n");
 			out.write("logDuplicatedIps: "+logDouble+"\n");
 			out.write("# What should I do if I find two players with\n");
 			out.write("# the same IP? (Normally one of them is a bot then)\n");
 			out.write("# Accepted are kick/ban/none.\n");
 			out.write("punishment: "+punish+"\n\n\n");
 			
 			
 			out.write("# Should Rei's Minimap be supported? Separate tags with ';'\n");
 			out.write("# false: Disables. If used in combination with other tags, the minimap still won't be supported.\n");
 			out.write("# Cave: Allows cave mapping.\n");
 			out.write("# Player: Allows view of position of a player.\n");
 			out.write("# Animal: Allows view of animals.\n");
 			out.write("# Mob: Allows view of hostile mobs.\n");
 			out.write("# Slime: Allows view of slimes.\n");
 			out.write("# Squid: Allows view of squids.\n");
 			out.write("# Other: Allows view of other living, i.e. golems.\n");
 			out.write("# Example: Player;Mob;Other\n");
 			out.write("supportReiMinimap: "+map+"\n\n\n");
 			
 			
 			out.write("# DO NOT CHANGE THIS!\n");
 			out.write("version: "+ configVersion +"\n\n");
 			
 			out.close();
 			
 			VAR.config.load(VAR.f_config);
 		} catch (Exception ex){
 			ex.printStackTrace();
 		}
 	}
 	public static int abs(int a){
 		if (a < 0)
 			a = a*(-1);
 		return a;
 	}
 	public boolean checkPlayer(CommandSender sender, String[] args){
 		for (Player on: Bukkit.getServer().getOnlinePlayers()){
 			if (on.getName().equals(args[2]))
 				online = true;
 		}
 		return online;
 	}
 	private void loadPlayerLog() throws Exception{
 		if (!VAR.f_player.exists()){
 			VAR.f_player.createNewFile();
 			VAR.log.info(VAR.logHeader + "Creating PlayerLog file.");
 		}
 		VAR.pLog = YamlConfiguration.loadConfiguration(VAR.f_player);
 		VAR.pLog.options().header("PlayerLogs");
 		VAR.pLog.addDefault("players", null);
 		VAR.pLog.save(VAR.f_player);
 	}
 }
 
