 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 import com.zolli.rodolffoutilsreloaded.utils.textUtils;
 import com.zolli.rodolffoutilsreloaded.utils.webUtils;
 
 public class commandExecutor implements CommandExecutor {
 
 	private String matchedPlayers = null;
 	private Player bannedPlayer;
 	private Player unbannedPlayer;
 	private rodolffoUtilsReloaded plugin;
 	private textUtils tu = new textUtils();
 	public webUtils wu = new webUtils();
 	public commandExecutor(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		
 		if(command.getName().equalsIgnoreCase("achat") || command.getName().equalsIgnoreCase("ac")) {
 			
 			if(args.length < 1) {
 				
 				sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/ac [üzenet]");
 				return false;
 				
 			}
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.adminChat")) {
 				
 				String msgFormat = plugin.config.getString("adminChatFormat").replace("(NAME)", sender.getName());
 				String param = tu.arrayToString(args, 0);
 				
 				for(Player p : Bukkit.getServer().getOnlinePlayers()) {
 					
 					if(p.isOp() || plugin.perm.has(p, "rur.adminchat")) {
 						
 						p.sendMessage(msgFormat.replace("(MSG)", param).toString());
 						
 					}
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("fakechat") || command.getName().equalsIgnoreCase("/fc")) {
 			
 			Player p = null;
 			String message = tu.arrayToString(args, 1);
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.fakeChat")) {
 			
 				if(args.length < 2) {
 					
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/fc [név] [üzenet]");
 					return false;
 					
 				}
 				
 				
 				List<Player> matchedPlayerList = Bukkit.getServer().matchPlayer(args[0]);
 				
 				if(matchedPlayerList.isEmpty() == false) {
 					
 					if(matchedPlayerList.size() == 1) {
 						
 						p = matchedPlayerList.get(0);
 						
 						if(!plugin.perm.has(p, "rur.fakeChat.exempt") && p != null) {
 							
 							for(Player pl : plugin.getServer().getOnlinePlayers()) {
 								
 								if(pl.isOp() || plugin.perm.has(pl, "rur.fakeChat.showname")) {
 									
 									pl.sendMessage(pl.getDisplayName() + " írta:");
 									
 								}
 								
 							}
 							
 							p.chat(message);
 							
 						} else {
 							
 							sender.sendMessage(plugin.messages.getString("fekechat.fakechatexempt"));
 							
 						}
 						
 					} else {
 						
 						sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 						
 						for(Player matched : matchedPlayerList) {
 							if(matchedPlayers == null) {
 								matchedPlayers = matched.getName() + ", ";
 							} else {
 								matchedPlayers = matchedPlayers + matched.getName() + ", ";
 							}
 						}
 						
 						sender.sendMessage("§2" + matchedPlayers);
 						matchedPlayers = null;
 						
 					}
 					
 				} else {
 					
 					sender.sendMessage(plugin.messages.getString("common.noPlayerFound"));
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 			
 		}
 		
 		if(command.getName().equalsIgnoreCase("definebutton")) {
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.specialButton.define")) {
 				
 				if(args.length < 2) {
 					
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/definebutton [tipus] [név]");
 					return false;
 					
 				}
 				
 				if(args[0].equalsIgnoreCase("weathersun") || args[0].equalsIgnoreCase("promote")) {
 				
 					plugin.SelectorPlayer = sender.getName();
 					plugin.selectType = args[0];
 					plugin.selectName = args[1];
 					sender.sendMessage(plugin.messages.getString("definebutton.defineproc1") + args[0]);
 					sender.sendMessage(plugin.messages.getString("definebutton.defineproc2"));
 					
 				} else {
 					
 					sender.sendMessage(plugin.messages.getString("definebutton.onlytype"));
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 			
 		}
 		
 		if(command.getName().equalsIgnoreCase("idban")) {
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.idban")) {
 				
 				if(args.length < 1) {
 					
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/idban [név]");
 					return false;
 					
 				}
 				
 				List<Player> mathcPlayerList = Bukkit.matchPlayer(args[0]);
 				
 				if(mathcPlayerList.isEmpty() == false) {
 					
 					if(mathcPlayerList.size() == 1) {
 						
 						bannedPlayer = mathcPlayerList.get(0);
 						String answer = wu.idBan(bannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							
 							sender.sendMessage(bannedPlayer.getDisplayName() + plugin.messages.getString("banning.successbanned"));
 							
							if(!bannedPlayer.isBanned() && plugin.config.getBoolean("idbanAlsoBanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("ban " + bannedPlayer.getName());
 							}
 							
 						} else {
 							
 							String result = plugin.messages.getString("banning.warningBan").replace("(NAME)", bannedPlayer.getName());
 							sender.sendMessage(result + answer);
 							
 						}
 						
 					} else {
 						
 						sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 						
 						for(Player p : mathcPlayerList) {
 							if(matchedPlayers == null) {
 								matchedPlayers = p.getName() + ", ";
 							} else {
 								matchedPlayers = matchedPlayers + p.getName() + ", ";
 							}
 						}
 						
 						sender.sendMessage("§2" + matchedPlayers);
 						matchedPlayers = null;
 						
 					}
 					
 				} else {
 					
 					OfflinePlayer offlinePl = plugin.getServer().getOfflinePlayer(args[0]);
 					
 					if(offlinePl != null) {
 						
 						bannedPlayer = offlinePl.getPlayer();
 						String answer = wu.idBan(bannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							
 							sender.sendMessage(bannedPlayer.getName() + plugin.messages.getString("banning.successbanned"));
 							
 							if(bannedPlayer.isBanned() == false && plugin.config.getBoolean("idbanAlsoBanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("ban " + args[0]);
 							}
 							
 						} else {
 							
 							String result = plugin.messages.getString("banning.warningBan").replace("(NAME)", bannedPlayer.getName());
 							sender.sendMessage(result + answer);
 							
 						}
 						
 					}
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 			
 		}
 		
 		if(command.getName().equalsIgnoreCase("idunban")) {
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.idunban")) {
 				
 				if(args.length < 1) {
 					
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/idunban [név]");
 					return false;
 					
 				}
 				
 				List<Player> mathcPlayerList = Bukkit.matchPlayer(args[0]);
 				
 				if(mathcPlayerList.isEmpty() == false) {
 					
 					if(mathcPlayerList.size() == 1) {
 						
 						unbannedPlayer = mathcPlayerList.get(0);
 						String answer = wu.idunBan(unbannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							
 							sender.sendMessage(unbannedPlayer.getDisplayName() + plugin.messages.getString("banning.successunbanned"));
 							
 							if(plugin.config.getBoolean("idUnbanAlsoUnbanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("unban " + unbannedPlayer.getName());
 							}
 							
 						} else {
 							
 							String result = plugin.messages.getString("banning.warningUnBan").replace("(NAME)", unbannedPlayer.getName());
 							sender.sendMessage(result + answer);
 							
 						}
 						
 					} else {
 						
 						sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 						
 						for(Player p : mathcPlayerList) {
 							if(matchedPlayers == null) {
 								matchedPlayers = p.getName() + ", ";
 							} else {
 								matchedPlayers = matchedPlayers + p.getName() + ", ";
 							}
 						}
 						
 						sender.sendMessage("§2" + matchedPlayers);
 						matchedPlayers = null;
 						
 					}
 					
 				} else {
 					
 					OfflinePlayer offlinePl = plugin.getServer().getOfflinePlayer(args[0]);
 					
 					if(offlinePl != null) {
 						
 						unbannedPlayer = offlinePl.getPlayer();
 						String answer = wu.idBan(unbannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							
 							sender.sendMessage(unbannedPlayer.getName() + plugin.messages.getString("banning.successunbanned"));
 							
 							if(unbannedPlayer.isBanned() == true && plugin.config.getBoolean("idUnbanAlsoUnbanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("unban " + unbannedPlayer.getName());
 							}
 							
 						} else {
 							
 							String result = plugin.messages.getString("banning.warningUnBan").replace("(NAME)", unbannedPlayer.getName());
 							sender.sendMessage(result + answer);
 							
 						}
 						
 					}
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 			
 		}
 		
 		if(command.getName().equalsIgnoreCase("nappal")) {
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.nappal")) {
 				
 				Player pl = plugin.getServer().getPlayer(sender.getName());
 				pl.getWorld().setTime(200L);
 				pl.sendMessage(plugin.messages.getString("othercommand.day"));
 				List<Player> players = pl.getWorld().getPlayers();
 				
 				for(Player msgTaker : players) {
 					
 					if(!msgTaker.getName().equalsIgnoreCase(pl.getName())) {
 						msgTaker.sendMessage(pl.getName() + plugin.messages.getString("othercommand.daybroadcast"));
 					}
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("napos")) {
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.napos")) {
 				
 				Player pl = plugin.getServer().getPlayer(sender.getName());
 				pl.getWorld().setStorm(false);
 				pl.sendMessage(plugin.messages.getString("othercommand.sunny"));
 				List<Player> players = pl.getWorld().getPlayers();
 				
 				for(Player msgTaker : players) {
 					
 					if(!msgTaker.getName().equalsIgnoreCase(pl.getName())) {
 						msgTaker.sendMessage(pl.getName() + plugin.messages.getString("othercommand.sunnybroadcast"));
 					}
 					
 				}
 				
 			} else {
 				
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 				
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 }
