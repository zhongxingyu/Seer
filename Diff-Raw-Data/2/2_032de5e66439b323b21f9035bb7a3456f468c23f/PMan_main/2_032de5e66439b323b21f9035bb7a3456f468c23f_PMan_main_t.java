 package com.github.Mephilis7.PlayerManager;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.github.Mephilis7.PlayerManager.VAR;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PMan_main extends JavaPlugin {
 	
 	private PMan_CmdRules RulesExecutor;
 	private PMan_ReportHandler ReportHandler;
 	private PMan_IPLogger ip = new PMan_IPLogger();
 	private PMan_RulesEventHandler EventHandler = new PMan_RulesEventHandler();
 	ChatColor green = ChatColor.GREEN;
 	ChatColor darkgreen = ChatColor.DARK_GREEN;
 	ChatColor gold = ChatColor.GOLD;
 	ChatColor aqua = ChatColor.AQUA;
 	ChatColor white = ChatColor.WHITE;
 	
 	int configVersion = 5;
 	
 	
 	public void onDisable() {
 		if (VAR.f_cache.exists())
 			VAR.f_cache.delete();
 		VAR.log.info(VAR.logHeader + "Shutdown.");
 	}
 	
 	@Override
 	public void onEnable() {
 		checkVersion();
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
 		if (VAR.config.getBoolean("EnableReport")){
 			ReportHandler = new PMan_ReportHandler(this);
 			getCommand("report").setExecutor(ReportHandler);
 			getCommand("check").setExecutor(ReportHandler);
 			getCommand("checktp").setExecutor(ReportHandler);
 			getCommand("apologise").setExecutor(ReportHandler);
 		}
 		getServer().getPluginManager().registerEvents(this.ip, this);
 		VAR.log.info(VAR.logHeader + "Ready to manage your players!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		setupEconomy();
 		setupPermissions();
 		
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
 						sender.sendMessage(gold + "/pman set fire <player> <time>"+white+" - "+darkgreen+"Sets a player on fire");
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
 						if (VAR.config.getBoolean("EnableReport")){
 							sender.sendMessage(gold + "/report <player> <reason>"+white+" - "+darkgreen+"Report a player to the Admins.");
 							sender.sendMessage(gold + "/check <player> [ReportNumber]"+white+" - "+darkgreen+"Check a reported player.");
 							sender.sendMessage(gold + "/checktp <player> <ReportNumber>"+white+" - "+darkgreen+"Teleports to the reported location.");
 							sender.sendMessage(gold + "/apologise <player> <ReportNumber|all>"+white+" - "+darkgreen+"Deletes the specified report about a player.");
 						}
 						return true;
 					} else { denied(sender);}
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
 						Player p = null;
 						if (args.length == 2){
 							p = Bukkit.getServer().getPlayer(args[1]);
 							if (p == null){
 								for (Player infoPlayer: getServer().getOnlinePlayers()){
 									String[] infoIp = infoPlayer.getAddress().toString().split(":");
 									if (infoIp[0].equalsIgnoreCase(args[1]) || infoIp[0].equalsIgnoreCase("/" + args[1])){
 										p = infoPlayer;
 									}
 								}
 								if (p == null){
 									notFound(sender);
 									return true;
 								}
 							}
 							if (p != null){
 								sender.sendMessage(gold + "------------------" + green + " PlayerManager " + gold + "-----------------");
 								String[] Order = VAR.config.getString("order").split(";");
 								int i=0;
 						
 								//showing information about the specified player as specified in the config.yml
 								while (i < Order.length){
 									if (sender.hasPermission("pman.info.name") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Name"))
 											sender.sendMessage(darkgreen + "Name: " + aqua + p.getName());
 									}
 									if (sender.hasPermission("pman.info.ip") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("IP"))
 											sender.sendMessage(darkgreen + "IP Address: " + aqua + p.getAddress());
 									}
 									if (sender.hasPermission("pman.info.lastLogin") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("LastLogin"))
 											sender.sendMessage(darkgreen + "Last Login: "+aqua+VAR.pLog.getString("players."+p.getName()+".lastLogin"));
 									}
 									if (sender.hasPermission("pman.info.lastLogout") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("LastLogout"))
 											sender.sendMessage(darkgreen + "Last Logout: "+aqua+VAR.pLog.getString("players."+p.getName()+".lastLogout"));
 									}
 									if (sender.hasPermission("pman.info.world") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("World"))
 											sender.sendMessage(darkgreen + "World: " + aqua + p.getWorld().getName());
 									}
 									if (sender.hasPermission("pman.info.money") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Money") && VAR.economy != null)
 											sender.sendMessage(darkgreen + "Money: "+ aqua + VAR.economy.getBalance(p.getName()) + VAR.economy.currencyNamePlural());
 									}
 									if (sender.hasPermission("pman.info.group") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Group") && VAR.permission != null)
 											sender.sendMessage(darkgreen + "Group: "+ aqua + VAR.permission.getPrimaryGroup(p));
 									}
 									if (sender.hasPermission("pman.info.health") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Health"))
 											sender.sendMessage(darkgreen + "Health: " + aqua + p.getHealth());
 									}
 									if (sender.hasPermission("pman.info.food") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Food"))
 											sender.sendMessage(darkgreen + "Food: " + aqua + p.getFoodLevel());
 									}
 									if (sender.hasPermission("pman.info.xp") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Xp"))
 											sender.sendMessage(darkgreen + "Exp level: " + aqua + p.getLevel());
 									}
 									if (sender.hasPermission("pman.info.gamemode") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("GameMode"))
 											sender.sendMessage(darkgreen + "GameMode: " + aqua + p.getGameMode());
 									}
 									if (sender.hasPermission("pman.info.position") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Position"))
 											sender.sendMessage(darkgreen + "Position:  " + aqua +"X: "+p.getLocation().getBlockX() + "  Z: " +p.getLocation().getBlockZ()+ "  Y: " +p.getLocation().getBlockY());
 									}
 									if (sender.hasPermission("pman.info.distance") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Distance")){
 											if (sender instanceof Player){
 												int x = abs(p.getLocation().getBlockX());
 												int loc = abs(((Player) sender).getPlayer().getLocation().getBlockX());
 												x = abs(x-loc);
 												int y = abs(p.getLocation().getBlockY());
 												loc = abs(((Player) sender).getPlayer().getLocation().getBlockY());
 												y = abs(y-loc);
 												int z = abs(p.getLocation().getBlockZ());
 												loc = abs(((Player) sender).getPlayer().getLocation().getBlockZ());
 												z = abs(z-loc);
 												x = x+y+z;
 												sender.sendMessage(darkgreen + "Distance: " + aqua + x);
 											}
 										} 
 									} 
 									if (sender.hasPermission("pman.info.allowFlight") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("AllowFlight")){
 											if (p.getAllowFlight()){
 											sender.sendMessage(darkgreen + "Is allowed to fly around.");
 											} else { sender.sendMessage(darkgreen + "Is not allowed to fly.");}
 										}
 									} 
 									if (sender.hasPermission("pman.info.realName") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("RealName"))
 											sender.sendMessage(darkgreen + "Real Name: "+ aqua + p.getName());
 									} 
 									if (sender.hasPermission("pman.info.hidden") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Hidden"))
 											sender.sendMessage(darkgreen + "Hidden: "+ aqua + VAR.pLog.getBoolean("players."+p.getName()+".Hidden"));
 									}
 									if (sender.hasPermission("pman.info.mute") || sender.isOp()){
 										if (Order[i].equalsIgnoreCase("Muted"))
 											sender.sendMessage(darkgreen + "Muted: " + aqua + VAR.pLog.getBoolean("players."+p.getName()+".Muted"));
 									}
 									if (sender.hasPermission("pman.info.rules") || sender.isOp()){
 										if (VAR.config.getBoolean("enableRules")){
 											if (Order[i].equalsIgnoreCase("Rules")){
 												if (VAR.pLog.getString("players."+p.getName()+".Has accepted rules").equalsIgnoreCase("true"))
 													sender.sendMessage(darkgreen +"Has accepted the rules.");
 												else if (VAR.pLog.getString("players."+p.getName()+".Has accepted rules").equalsIgnoreCase("hasTyped"))
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
 							Player p = checkPlayer(args[1]);
 							if (p == null){
 								notFound(sender);
 								return true;
 							} else {
 								for (Player p2: getServer().getOnlinePlayers()){
 									p2.hidePlayer(p);
 								}
 								p.sendMessage(ChatColor.GOLD+"You've been hidden from everyone else.");
 								if (VAR.logit)
 									VAR.log.info(VAR.logHeader + sender.getName() + " has hidden " + p.getName());
 								try {
 									VAR.pLog.set("players."+p.getName()+".Hidden", Boolean.valueOf(true));
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
 							Player p = checkPlayer(args[1]);
 							if (p == null){
 								notFound(sender);
 								return true;
 							} else {
 								for (Player p2: getServer().getOnlinePlayers()){
 									p2.showPlayer(p);
 								}
 								p.sendMessage(ChatColor.GOLD+"Evereyone can see you now.");
 								if (VAR.logit)
 									VAR.log.info(VAR.logHeader + sender.getName() + " has un-hidden " + p.getName());
 								try {
 									VAR.pLog.set("players."+p.getName()+".Hidden", Boolean.valueOf(false));
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
 							Player p = checkPlayer(args[1]);
 							if (p == null){
 								notFound(sender);
 								return true;
 							} else {
 								try{
 									loadPlayerLog();
 									Boolean muted = VAR.pLog.getBoolean("players."+p.getName()+".Muted");
 									
 									if (muted){
 										VAR.pLog.set("players."+p.getName()+".Muted", Boolean.valueOf(false));
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has allowed " + p.getName() + " to speak");
 									} else {
 										VAR.pLog.set("players."+p.getName()+".Muted", Boolean.valueOf(true));
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has muted " + p.getName());
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
 							return true;
 						}
 						//set AllowFlight
 						if (args[1].equalsIgnoreCase("fly")){
 							found = true;
 							if (sender.hasPermission("pman.set.fly") || sender.isOp()){
 								if (args.length == 4){
 									Player p = checkPlayer(args[2]);
 									if (p == null){
 										notFound(sender);
 										return true;
 									} else if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("allow")){
 											p.setAllowFlight(true);
 											p.sendMessage(ChatColor.GOLD+"You are now allowed to fly.");
 											sender.sendMessage(VAR.Header + darkgreen + p.getName() + " is now allowed to fly.");
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has allowed " +p.getName()+ " to fly!");
 											try {
 												VAR.pLog.set("players."+p.getName()+".Allowed to fly", Boolean.valueOf(true));
 												VAR.pLog.save(VAR.f_player);
 												loadPlayerLog();
 											} catch (Exception ex){
 												ex.printStackTrace();
 											}
 											} else if (args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("deny")){
 												p.setAllowFlight(false);
 												p.sendMessage(ChatColor.GOLD+"You are not allowed to fly anymore.");
 												sender.sendMessage(VAR.Header + darkgreen + p.getName() + " is now disallowed to fly.");
 												if (VAR.logit)
 													VAR.log.info(VAR.logHeader + sender.getName() + " has disallowed " +p.getName()+ " to fly!");
 												try{
 													VAR.pLog.set("players."+p.getName()+".Allowed to fly", Boolean.valueOf(false));
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
 									Player p = checkPlayer(args[2]);
 									if (p == null){
 										notFound(sender);
 										return true;
 									} else{
 										if (args[3].equalsIgnoreCase("full")){
 											p.setHealth(20);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has filled up the health of " +p.getName());
 										} else {
 											int i = Integer.parseInt(args[3]);
 											if (i < 0)
 												i = 0;
 											if (i > 20)
 												i = 20;
 											p.setHealth(i);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has set the health of " +p.getName()+ " to " +i);
 										}
 										p.sendMessage(ChatColor.GOLD+"You have been healed.");
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
 									Player p = checkPlayer(args[2]);
 									if (p == null){
 										notFound(sender);
 										return true;
 									} else {
 										if (args[3].equalsIgnoreCase("full")){
 											p.setFoodLevel(20);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has filled up the food bar of "+p.getName());
 										} else if (args[3].equalsIgnoreCase("empty")){
 											p.setFoodLevel(0);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has emptied the food bar of "+p.getName());
 										} else {
 											int i = Integer.parseInt(args[3]);
 											if (i < 0)
 												i = 0;
 											if (i > 20)
 												i = 20;
 											p.setFoodLevel(i);
 											if (VAR.logit)
 												VAR.log.info(VAR.logHeader + sender.getName() + " has set the food level of "+p.getName()+" to "+i);
 										}
 										p.sendMessage(ChatColor.GOLD+"Your food level has been changed.");
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
 									Player p = checkPlayer(args[2]);
 									if (p == null){
 										notFound(sender);
 										return true;
 									} else { p.setLevel(Integer.parseInt(args[3]));
 									sender.sendMessage(VAR.Header + darkgreen + "The player's EXP level has been set.");
 									p.sendMessage(ChatColor.GOLD+"Your EXP level has been changed.");
 									if (VAR.logit)
 										VAR.log.info(VAR.logHeader + sender.getName() + " has set the EXP level of "+p.getName()+" to "+args[3]);
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
 									Player p = checkPlayer(args[2]);
 									if (p == null){
 										notFound(sender);
 										return true;
 									}
 									if (args[3].equalsIgnoreCase("reset")){
 										p.setDisplayName(p.getName());
 										p.setPlayerListName(p.getName());
 										sender.sendMessage(VAR.Header + darkgreen+ "The player's name has been set to default.");
 										if (VAR.logit)
 											VAR.log.info(VAR.logHeader + sender.getName() + " has reset the in-game name of " +p.getName());
 										try {
 											loadPlayerLog();
 											VAR.pLog.set("players."+p.getName()+".Displayed Name", p.getName());
 											VAR.pLog.save(VAR.f_player);
 											VAR.config.load(VAR.f_config);
 										} catch (Exception ex){
 											ex.printStackTrace();
 										}
 										return true;
 									}
 									p.setDisplayName("~"+args[3]);
 									p.setPlayerListName("~"+args[3]);
 									p.sendMessage(ChatColor.GOLD+"Your nickname has been changed to ~"+args[3]);
 									sender.sendMessage(VAR.Header + darkgreen + "The player's name has been set.");
 									if (VAR.logit)
 										VAR.log.info(VAR.logHeader + sender.getName() + " has set the in-game name of "+p.getName()+" to ~"+args[3]);
 									try {
 										loadPlayerLog();
 										VAR.pLog.set("players."+p.getName()+".Displayed Name", args[3]);
 										VAR.pLog.save(VAR.f_player);
 										VAR.config.load(VAR.f_config);
 									} catch (Exception ex){
 										ex.printStackTrace();
 									}
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 								sender.sendMessage(ChatColor.BLUE+"/pman set name <player> <name>");
 								}
 							} else denied(sender);
 						}
 						if (args[1].equalsIgnoreCase("fire")){
 							found = true;
 							if (sender.hasPermission("pman.set.fire") || sender.isOp()){
 								if (args.length == 4){
 									Player p = checkPlayer(args[2]);
 									if (p == null){
 										notFound(sender);
 										return true;
 									}
 									int fire = Integer.parseInt(args[3]);
 									fire = fire * 20;
 									p.setFireTicks(fire);
 									sender.sendMessage(VAR.Header+darkgreen+p.getName()+" is now burning...");
 									p.sendMessage(ChatColor.YELLOW+"Somebody wants to see you "+ChatColor.RED+"burning...");
 									if (VAR.logit)
 										VAR.log.info(VAR.logHeader+sender.getName()+" has set fire to "+p.getName()+" for "+(fire/20)+" seconds.");
 								} else { sender.sendMessage(VAR.Header + ChatColor.RED + "False amount of Arguments!");
 								sender.sendMessage(ChatColor.BLUE+"/pman set fire <player> <time>");
 								}
 							} else denied(sender);
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
 	void notFound(CommandSender sender){
 		sender.sendMessage(VAR.Header + ChatColor.RED + "Could not find the specified player.");
 	}
 	public void update(){
 		Boolean logConsole = VAR.config.getBoolean("logToConsole");
 		String reset = VAR.config.getString("reset", "Fly;Hidden");
 		Boolean cJQ = VAR.config.getBoolean("customJQ", true);
 		String jmsg = VAR.config.getString("joinMsg", "&aHello %NAME!");
 		String jmsgO = VAR.config.getString("joinMsgOther", "&bPlayer &e%NAME &b(&c%IP&b) has connected.");
 		String qmsg = VAR.config.getString("quitMsg", "&e%NAME has left the game.");
 		String mmsg = VAR.config.getString("mutedMsg", "&cYou have been muted.");
 		String order = VAR.config.getString("order", "Name;IP;World;Xp;Muted");
 		Boolean eRule = VAR.config.getBoolean("enableRules", true);
 		String REXCMD = VAR.config.getString("RulesExCmd", "give %NAME stone_sword 1|say WELCOME %NAME TO THE SERVER!");
 		String rRules = "";
 		int i = 1;
 		if (!VAR.config.isSet("Rules1")){
 			rRules = "&3---- &bold%SERVERNAME Rules&reset ------newLine--&1[1] &cDo not grief.--newLine--&1[2] &cBe polite.--newLine--&1[3] &aHave fun :D";
 		} else {
 			while (VAR.config.isSet("Rules"+i)){
 			rRules = rRules + VAR.config.getString("Rules"+i)+"--newLine--";
 			i++;
 			}
 		}
 		String[] outRules = rRules.split("--newLine--");
 		String PreventRNA = VAR.config.getString("PreventNotAccepted", "Move[10];DamageOthers;PickUpDrops;BlockBreak;BlockPlace");
 		String rwl = "";
 		if (VAR.config.isSet("RulesWhiteList")){
 			rwl = VAR.config.getList("RulesWhiteList").toString();
 			rwl = rwl.replace("[", "");
 			rwl = rwl.replace("]", "");
 			rwl = rwl.replace(" ", "");
 			if (rwl.equalsIgnoreCase(""))
 				rwl = "rules,acceptrules,login,register";
 		} else rwl = "rules,acceptrules,login,register";
 		String[] RWL = rwl.split(",");
 		String RNAMsg = VAR.config.getString("RulesNotAcceptedMsg", "&cYou are not allowed to do this until you accepted the server rules! Type &2/acceptrules&c!");
 		String RNADSMsg = VAR.config.getString("RulesNotAcceptedDmgSelfMsg", "&eThis player has not accepted the rules yet. Let him live until then ;)");
 		String RNAWLMsg = VAR.config.getString("RulesNotAcceptedWLMsg", "&eYou are not allowed to execute this command until you accepted the rules.");
 		Boolean RulesTp = VAR.config.getBoolean("RulesTeleport", false);
 		String RTPW = VAR.config.getString("RulesTpWorld", "world");
 		double RTPX = VAR.config.getDouble("RulesTpX", 0.0);
 		double RTPY = VAR.config.getDouble("RulesTpY", 64.0);
 		double RTPZ = VAR.config.getDouble("RulesTpZ", 0.0);
 		double RTPP = VAR.config.getDouble("RulesTpPitch", 0.0);
 		double RTPYaw = VAR.config.getDouble("RulesTpYaw", 0.0);
 		Boolean RepEn = VAR.config.getBoolean("EnableReport", true);
 		int RKick = VAR.config.getInt("ReportKick", 5);
 		String RKickMsg = VAR.config.getString("ReportKickMsg", "You have been reported too often. You should behave better.");
 		int RBan = VAR.config.getInt("ReportBan", 7);
 		String RBanMsg = VAR.config.getString("ReportBanMsg", "Too many reports. You have been warned.");
 		String RBanMeth = VAR.config.getString("ReportBanMethod", "ip");
 		int RCD = VAR.config.getInt("ReportCoolDown", 30);
 		String RCDMsg = VAR.config.getString("ReportCoolDownMsg", "&6This command is not ready yet!");
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
 			out.write("# All modifications not specified here will be re-enabled, depending on the information in the PlayerLog.yml file.\n");
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
 			out.write("# Money: The amount of money a player has. Vault is REQUIRED.\n");
 			out.write("# Group: The primary group the player is in. Vault is REQUIRED.\n");
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
 			out.write("# Chest: Prevent them from opening chests.\n");
 			out.write("# DamageSelf: Prevent them from being hurt by mobs or other players.\n");
 			out.write("# DamageOthers: Prevent them from hurting any other player or mob.\n");
 			out.write("# Move[]: Keep them in a defined radius from the spawn point.\n");
 			out.write("# PickUpDrops: Don't let them pick up any items.\n");
 			out.write("# Redstone: Prevent them from using levers, buttons or pressure plates.\n");
 			out.write("# Example: Move[10];DamageOthers;PickUpDrops;BlockBreak;BlockPlace\n");
 			out.write("PreventNotAccepted: '"+PreventRNA+"'\n");
 			out.write("# This is a command whitelist. Only the commands specified here will be allowed to players who haven't accepted the rules.\n");
 			out.write("RulesWhiteList:\n");
 			i = 0;
 			while (i < RWL.length){
 				out.write("    - "+RWL[i]+"\n");
 				i++;
 			}
 			out.write("# This is the message your players will be shown if they try to do anything you've specified above, except picking up drops.\n");
 			out.write("RulesNotAcceptedMsg: '"+RNAMsg+"'\n");
 			out.write("# This is the message a player will be shown when he tries to damage a player who has not accepted the rules yet.\n");
 			out.write("# This only has an effect if you included DamageSelf above.\n");
 			out.write("RulesNotAcceptedDmgSelfMsg: '"+RNADSMsg+"'\n");
 			out.write("# This is the message a player will be shown if he tries to execute a command that's not on the whitelist.\n");
 			out.write("RulesNotAcceptedWLMsg: '"+RNAWLMsg+"'\n\n");
 			
 			out.write("# Enable teleporting when typing /acceptrules for the first time?\n");
 			out.write("RulesTeleport: "+RulesTp+"\n");
 			out.write("# The position they will be teleported to. Change this in-game by typing /pman srtp.\n");
 			out.write("RulesTpWorld: "+RTPW+"\n");
 			out.write("RulesTpX: "+RTPX+"\n");
 			out.write("RulesTpY: "+RTPY+"\n");
 			out.write("RulesTpZ: "+RTPZ+"\n");
 			out.write("RulesTpPitch: "+RTPP+"\n");
 			out.write("RulesTpYaw: "+RTPYaw+"\n\n\n");
 			
 			
 			out.write("# Enable the /report and /check commands?\n");
 			out.write("EnableReport: "+RepEn+"\n");
 			out.write("# After how many /reports do you want the player to be kicked? -1 disables.\n");
 			out.write("ReportKick: "+RKick+"\n");
 			out.write("# The message a kicked player will be shown. Do not use any variables.\n");
 			out.write("ReportKickMsg: '"+RKickMsg+"'\n");
 			out.write("# After how many /reports do you want the player to be banned? -1 disables.\n");
 			out.write("ReportBan: "+RBan+"\n");
 			out.write("# The message a banned player will be shown. Do not use any variables.\n");
 			out.write("ReportBanMsg: '"+RBanMsg+"'\n");
 			out.write("# What banning method should be used? This should be either name, ip or both.\n");
 			out.write("ReportBanMethod: "+RBanMeth+"\n");
 			out.write("# The cooldown in seconds for the /report command. -1 disables.\n");
 			out.write("ReportCoolDown: "+RCD+"\n");
 			out.write("# The message a player will be shown when he tries to use the command but the cooldown has not finished yet.\n");
 			out.write("ReportCoolDownMsg: '"+RCDMsg+"'\n\n\n");
 			
 			
 			out.write("# Should BotBlocking be enabled?\n");
 			out.write("# Not recommended. It's old and buggy code, I will do that better soon,\n");
 			out.write("# depending on the result of the poll I created on the page where you downloaded my plugin.\n");
			out.write("#######  http://dev.bukkit.org/server-mods/playermanager  #######\n");
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
 	public Player checkPlayer(String str){
 		Player p = Bukkit.getServer().getPlayer(str);
 		return p;
 	}
 	public void loadPlayerLog() throws Exception{
 		if (!VAR.f_player.exists()){
 			VAR.f_player.createNewFile();
 			VAR.log.info(VAR.logHeader + "Creating PlayerLog file.");
 		}
 		VAR.pLog = YamlConfiguration.loadConfiguration(VAR.f_player);
 		VAR.pLog.options().header("PlayerLogs");
 		VAR.pLog.addDefault("players", null);
 		VAR.pLog.save(VAR.f_player);
 	}
 	public void checkVersion(){
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
 		public void run() {
 			try{
 				URL url = new URL("http://dev.bukkit.org/server-mods/playermanager/files");
 				URLConnection yc = url.openConnection();
 				BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
 				String inputLine = "";
 			
 				while ((inputLine = in.readLine()) != null){
 					if (inputLine.contains("col-file\"")){
 						String version = inputLine.split("PlayerManager ")[1].split("<")[0];
 						String thisVersion = 
 								Bukkit.getServer().getPluginManager().getPlugin("PlayerManager").getDescription().getVersion();
 						if (!version.equalsIgnoreCase("v"+thisVersion)){
 							VAR.log.info("");
 							VAR.log.info("------------- Found an update for PlayerManager -------------");
 							VAR.log.info("Please go to http://dev.bukkit.org/server-mods/playermanager/");
 							VAR.log.info("and download "+version+". You are running v"+thisVersion+".");
 							VAR.log.info("-------------------------------------------------------------");
 							VAR.log.info("");
 							return;
 						}
 						break;
 					}
 				}
 				VAR.log.info("PlayerManager is UpToDate (v"+Bukkit.getServer().getPluginManager().getPlugin("PlayerManager").getDescription().getVersion()+").");
 			
 			} catch (IOException ex){
 				VAR.log.info(VAR.logHeader+"Error while looking for updates.");
 			}
 			}
 		}, 15L);
 	}
 	private boolean setupPermissions()
     {
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             VAR.permission = permissionProvider.getProvider();
         }
         return (VAR.permission != null);
     }
 	private boolean setupEconomy()
     {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             VAR.economy = economyProvider.getProvider();
         }
 
         return (VAR.economy != null);
     }
 }
 
