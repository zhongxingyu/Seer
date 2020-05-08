 package com.etriacraft.etriabending.suites;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 
 import com.etriacraft.etriabending.EtriaBending;
 import com.etriacraft.etriabending.Strings;
 import com.etriacraft.etriabending.util.Utils;
 
 public class MessagingSuite {
 
 	public static HashMap<CommandSender, CommandSender> chatterDb = new HashMap<CommandSender, CommandSender>();
 	public static List<String> ignoring = new ArrayList();	
 	
 	static EtriaBending plugin;
 
 	public MessagingSuite(EtriaBending instance) {
 		this.plugin = instance;
 		init();
 	}
 
 	public static void showMotd(CommandSender s) {
 		for (String mess : plugin.getConfig().getStringList("messaging.motd")) {
 			mess = mess.replace("<name>", s.getName());
 			mess = mess.replaceAll("(?i)&([a-fk-or0-9])", "\u00A7$1").replaceAll("<name>", s.getName());
 			s.sendMessage(mess);
 		}
 	}
 
 	private void init() {
 		PluginCommand motd = plugin.getCommand("motd");
 		PluginCommand message = plugin.getCommand("message");
 		PluginCommand reply = plugin.getCommand("reply");
 		PluginCommand modchat = plugin.getCommand("modchat");
 		PluginCommand ignore = plugin.getCommand("ignore");
 		PluginCommand ignorelist = plugin.getCommand("ignorelist");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.motd")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					for (String mess : plugin.getConfig().getStringList("messaging.motd")) {
 						mess = mess.replace("<name>", s.getName());
 						mess = mess.replaceAll("(?i)&([a-fk-or0-9])", "\u00A7$1").replaceAll("<name>", s.getName());
 						s.sendMessage(mess);
 					}
 					return true;
 				}
 				return true;
 			}
 		}; motd.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.msg")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
				} if (args.length < 2) {
					s.sendMessage("3Proper Usage: 6/msg [Player] [Message]");
					return true;
 				} else {
 					final Player r = Bukkit.getPlayer(args[0]);
 					if (r == null) {
 						s.sendMessage("cThat player is not online!");
 						return true;
 					}
 					if (plugin.getConfig().getStringList("players." + s.getName().toLowerCase() + ".ignoring").contains(r.getName().toLowerCase())) {
 						s.sendMessage("cYou can't send a Private Message because you have ignored this player.");
 						return true;
 					}
 					ignoring = plugin.getConfig().getStringList("players." + r.getName().toLowerCase() + ".ignoring");
 					if (ignoring.contains(s.getName().toLowerCase())) {
 						s.sendMessage("cYou can't send a Private Message because this player has ignored you.");
 						return true;
 					}
 					final String message = Strings.buildString(args, 1, " ");
 					s.sendMessage("a[7Youa -> 7" + r.getName() + "a] e" + message);
 					r.sendMessage("a[7" + s.getName() + "a -> 7Youa] e" + message);
 					EtriaBending.log.info(String.format("[PM][%1$s -> %2$s] %3$s", s.getName(), r.getName(), message));
 
 					chatterDb.put(s, r);
 					chatterDb.put(r, s);
 
 					for(Player player: Bukkit.getOnlinePlayers()) {
 						if ((player.hasPermission("eb.msg.spy"))) {
 							player.sendMessage("d[Spy][4" + s.getName() + "d -> 4" + r.getName() + "d] d" + message);
 						}
 					}
 					return true;
 				}
 			}
 		}; message.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.reply")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
 				} else {
 					if (!chatterDb.containsKey(s)) {
 						s.sendMessage("cYou have no one to reply to!");
 					} else {
 						final CommandSender r = chatterDb.get(s);
 						if (!Bukkit.getOfflinePlayer(r.getName()).isOnline()) {
 							s.sendMessage("7" + r.getName() + " cis no longer online!");
 							return true;
 						}
 						if (plugin.getConfig().getStringList("players." + s.getName().toLowerCase() + ".ignoring").contains(r.getName().toLowerCase())) {
 							s.sendMessage("cYou can't send a Private Message because you have ignored this player.");
 							return true;
 						}
 						ignoring = plugin.getConfig().getStringList("players." + r.getName().toLowerCase() + ".ignoring");
 						if (ignoring.contains(s.getName().toLowerCase())) {
 							s.sendMessage("cYou can't send a Private Message because this player has ignored you.");
 							return true;
 						}
 						final String message = Strings.buildString(args, 0, " ");
 
 						s.sendMessage("a[7Youa -> 7" + r.getName() + "a] e" + message);
 						r.sendMessage("a[7" + s.getName() + "a -> 7Youa] e" + message);
 						EtriaBending.log.info(String.format("[PM][%1$s -> %2$s] %3$s", s.getName(), r.getName(), message));
 
 						chatterDb.put(r, s);
 
 						for(Player player: Bukkit.getOnlinePlayers()) {
 							if ((player.hasPermission("eb.msg.spy"))) {
 								player.sendMessage("d[Spy][4" + s.getName() + "d -> 4" + r.getName() + "d] d" + message);
 							}
 						}
 						return true;
 
 					}
 					return false;
 				}
 			}
 		}; reply.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length < 1) return false;
 				if (!s.hasPermission("eb.modchat")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
 				} else {
 					String format = plugin.getConfig().getString("messaging.modchat");
 					format = format.replace("<message>", Utils.buildString(args, 0)).replace("<name>", s.getName());
 					format = Utils.colorize(format);
 
 					for (Player player: Bukkit.getOnlinePlayers()) {
 						if ((player.hasPermission("eb.modchat"))) {
 							player.sendMessage(format);
 						}
 					}
 					return true;
 				}
 			}
 		}; modchat.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 
 				if (args.length == 0) {
 					s.sendMessage("3Proper Usage:6 /ignore [Player]");
 					return true;
 				}
 				if (!s.hasPermission("eb.ignore.use")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
 				}
 				if (s.hasPermission("eb.ignore.bypass")) {
 					s.sendMessage("cYou are not allowed to ignore other players.");
 					s.sendMessage("cThis is probably because you're a staff member.");
 					return true;
 				}
 				Player p = plugin.getServer().getPlayer(args[0]);
 				if (p == null) {
 					s.sendMessage("cThis player is not online.");
 					return true;
 				}
 				if (s == p) {
 					s.sendMessage("cYou can't ignore yourself.");
 					return true;
 				}
 				if (p.hasPermission("eb.ignore.bypass")) {
 					s.sendMessage("cYou are not allowed to ignore 3" + p.getName());
 					return true;
 				}
 				String lowerPlayer = s.getName().toLowerCase();
 				if (plugin.getConfig().get("players." + lowerPlayer + ".ignoring") == null) {
 					ignoring.clear();
 					ignoring.add(args[0].toLowerCase());
 					plugin.getConfig().set("players." + lowerPlayer + ".ignoring", ignoring);
 					plugin.saveConfig();
 					s.sendMessage("bYou are now ignoring 3" + args[0]);
 					return true;
 				}
 				ignoring = plugin.getConfig().getStringList("players." + lowerPlayer + ".ignoring");
 				if (!ignoring.contains(args[0].toLowerCase())) {
 					ignoring.add(args[0].toLowerCase());
 					plugin.getConfig().set("players." + lowerPlayer + ".ignoring", ignoring);
 					plugin.saveConfig();
 					s.sendMessage("bYou are now ignoring 3" + args[0]);
 					return true;
 				}
 				if (ignoring.contains(args[0].toLowerCase())) {
 					ignoring.remove(args[0].toLowerCase());
 					plugin.getConfig().set("players." + lowerPlayer + ".ignoring", ignoring);
 					plugin.saveConfig();
 					s.sendMessage("cYou are no longer ignoring 3" + args[0]);
 					return true;
 				}
 				return true;
 			}
 		}; ignore.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.ignore.use")) {
 					s.sendMessage("cYou don't have permission to do that.");
 					return true;
 				}
 				String lowerPlayer = s.getName().toLowerCase();
 				if (plugin.getConfig().get("players." + lowerPlayer + ".ignoring") == null) {
 					s.sendMessage("bYou are not ignoring anyone.");
 					return true;
 				}
 				List<String> ignored = plugin.getConfig().getStringList("players." + lowerPlayer + ".ignoring");
 				String message = "You are ignoring:";
 				for (String p : ignored) {
 					message = message + " " + p;
 				}
 				s.sendMessage("b" + message);
 				return true;
 			}
 		}; ignorelist.setExecutor(exe);
 	}
 
 }
