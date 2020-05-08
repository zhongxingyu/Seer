 package me.cmastudios.plugins.WarhubModChat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import me.cmastudios.plugins.WarhubModChat.util.*;
 
 public class WarhubModChat extends JavaPlugin {
 
 	Permission permissions = new Permission();
 	Message messageUtil = new Message();
 	String version;
 	Logger log = Logger.getLogger("Minecraft");
 	private final plrLstnr playerListener = new plrLstnr(this);
 	public HashMap<Player, String> channels = new HashMap<Player, String>();
 	public HashMap<Player, String> ignores = new HashMap<Player, String>();
 
 	@Override
 	public void onDisable() {
 		channels.clear();
 		log.info("[WarhubModChat] Disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		permissions.setupPermissions();
 		Config.setup();
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Low, this);
 		PluginDescriptionFile pdffile = this.getDescription();
 	    version = pdffile.getVersion();
 		log.info("[WarhubModChat] Version " + version + " by cmastudios enabled!");	
 	}
 	@SuppressWarnings("static-access")
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (cmd.getName().equalsIgnoreCase("modchat")) {
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED + "You don't have the permissions to do that!");
 				return true;
 			}
 			if (args.length < 1) {
 				if (player == null) {log.info("You can't use channels from the console, use '/modchat <message>' to chat.");return true;}
 				channels.put(player, "mod");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to mod.");
 			} else {
 				String message = "";
 			    for (String arg : args) {
 			      message = message + arg + " ";
 			    }
 			    if (message.equals("")) return false;
 			    if (player==null) {
 	    			List<Player> sendto = new ArrayList<Player>();
 			    	for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 	    				if (permissions.has(p, "warhub.moderator")) {
 	    					sendto.add(p);
 	    				}
 	    			}
 	    			for (Player p : sendto) {
 	    				p.sendMessage(messageUtil.colorizeText(Config.read("modchat-format").replace("%player", "tommytony").replace("%message", message)));
 	    			}	
 	    			log.info("[MODCHAT] tommytony: "+message);
 	    			sendto.clear();
 	    			return true;
 	    		}
 			    if (channels.containsKey(player)) {
 					String channel = channels.remove(player);
 						player.chat(message);
 						channels.put(player, channel);
 						channel = null;
 				} else {
 					channels.put(player, "mod");
 					player.chat(message);
 					channels.remove(player);
 				}
 				
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("alert")) {
 			if (player == null) {log.info("You can't use alert from the console, use '/say <message>' to chat.");return true;}
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED + "You don't have the permissions to do that!");
 				return true;
 			}
 			if (args.length < 1) {
 				channels.put(player, "alert");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to alert.");
 			} else {
 				String message = "";
 			    for (String arg : args) {
 			      message = message + arg + " ";
 			    }
 			    if (message.equals("")) return false;
 			    if (channels.containsKey(player)) {
 					String channel = channels.remove(player);
 						player.chat(message);
 						channels.put(player, channel);
 						channel = null;
 				} else {
 					channels.put(player, "alert");
 					player.chat(message);
 					channels.remove(player);
 				}
 				
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("global")) {
 			if (player == null) {log.info("You can't use global from the console, use '/say <message>' to chat.");return true;}
 			if (args.length < 1) {
 				channels.remove(player);
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to global.");
 			} else {
 				String message = "";
 			    for (String arg : args) {
 			    	message = message + arg + " ";
 			    }
 			    if (message.equals("")) return false;
 				if (channels.containsKey(player)) {
 					String channel = channels.remove(player);
 						player.chat(message);
 						channels.put(player, channel);
 						channel = null;
 				} else {
 					player.chat(message);
 				}
 				
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("channel")) {
 			if (player == null) {log.info("You can't use channels from the console, use '/<say/modchat> <message>' to chat.");return true;}
 			if (args.length < 1) {
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED + "You're not a mod, and cannot change channels.");
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED + "Use '/ch <mod/alert/global>' to change your channel.");
 			}
 			return true;
 			}
 			if (args[0].equalsIgnoreCase("mod") || args[0].equalsIgnoreCase("modchat") || args[0].equalsIgnoreCase("m")) {
 				if (!permissions.has(player, "warhub.moderator")) {
 					player.sendMessage(ChatColor.RED + "You don't have the permissions to do that!");
 					return true;
 				}
 				channels.put(player, "mod");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to mod.");
 				return true;
                                 }
 			if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("alert")) {
 				if (!permissions.has(player, "warhub.moderator")) {
 					player.sendMessage(ChatColor.RED + "You don't have the permissions to do that!");
 					return true;
 				} 
 				channels.put(player, "alert");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to alert.");
 				return true;
                                 }
 			if (args[0].equalsIgnoreCase("g") || args[0].equalsIgnoreCase("global")) {
 				channels.remove(player);
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to global.");
 				return true;
 				}
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED + "You're not a mod, and cannot change channels.");
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED + "Use '/ch <mod/alert/global>' to change your channel.");
 			}
 			return true;
                         }
 		if (cmd.getName().equalsIgnoreCase("say")) {
 			if (player != null) if (!player.isOp()) {player.sendMessage(ChatColor.RED + "You don't have the permissions to do that!"); return true;}
 			if (args.length == 0) return false;
 			String message = "";
 		    for (String arg : args) {
 		      message = message + arg + " ";
 		    }
 		    if (message.equals("")) return false;
 			this.getServer().broadcastMessage(messageUtil.colorizeText(Config.read("say-format")).replace("%message", message));
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("deaf")) {
 			if (ignores.containsKey(player)) {
 				ignores.remove(player);
 				player.sendMessage(ChatColor.YELLOW + "Un-deafened.");
 			} else {
 			ignores.put(player, "");
 			player.sendMessage(ChatColor.YELLOW + "Deafened.");
 			}
			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("me")) {
 			String message = "";
 		      for (String arg : args) {
 		        message = message + arg + " ";
 		      }
 		      if (message == "") return false;
 		      if (message == " ") return false;
 			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 		        if (!ignores.containsKey(p)) p.sendMessage( "* " + player.getDisplayName() + " " + message);
 		      }
			return true;
 		}
 		return false;
 	}
 
 }
