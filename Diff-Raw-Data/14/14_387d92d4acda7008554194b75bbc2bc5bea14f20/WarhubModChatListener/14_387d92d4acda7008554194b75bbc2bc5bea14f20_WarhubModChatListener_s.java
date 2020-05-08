 package me.cmastudios.plugins.WarhubModChat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.cmastudios.plugins.WarhubModChat.util.Channel;
 import me.cmastudios.plugins.WarhubModChat.util.Config;
 import me.cmastudios.plugins.WarhubModChat.util.Message;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class WarhubModChatListener implements Listener {
 	public boolean nukerEnabled = false;
 	public static WarhubModChat plugin;
 
 	public WarhubModChatListener(WarhubModChat instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(final PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.mutedplrs.containsKey(player.getName())) {
 			event.setCancelled(true);
 			player.sendMessage(ChatColor.RED + "You are muted.");
 			return;
 		}
 		/*
 		 * Anti-spam removed due to bukkit built in anti spam
 		 */
 		// Anti caps
 		event.setMessage(Capslock(player, event.getMessage()));
 		
 		// Don't send to deaf players
 		List<Player> plrs = new ArrayList<Player>();
 		for (Player plr : plugin.getServer().getOnlinePlayers()) {
 			if (plugin.ignores.contains(plr.getName()))
 				plrs.add(plr);
 		}
 		for (Player plr : plrs) {
 			event.getRecipients().remove(plr);
 		}
 		/*
 		 * Anti color removed due to vanilla built in anti color
 		 */
 		if (plugin.channels.containsKey(event.getPlayer())) {
 			// Typing in !global channel
 			if (DynmapManager.getDynmapCommonAPI() != null)
 				DynmapManager.getDynmapCommonAPI()
 						.setDisableChatToWebProcessing(true);
 			// Modchat processing
 			if (plugin.channels.get(event.getPlayer()) == Channel.MODCHAT) {
 				event.setCancelled(true);
 				sendToMods(Message
 						.colorizeText(Config.config.getString("modchat-format"))
 						.replace("%player", event.getPlayer().getDisplayName())
 						.replace("%message", event.getMessage()));
 				plugin.log.info("[MODCHAT] "
 						+ event.getPlayer().getDisplayName() + ": "
 						+ event.getMessage());
 			}
 			// Alert processing
 			if (plugin.channels.get(event.getPlayer()) == Channel.ALERT) {
 				event.setCancelled(true);
 				Bukkit.getServer().broadcastMessage(
 						Message.colorizeText(
 								Config.config.getString("alert-format"))
 								.replace("%player",
 										event.getPlayer().getDisplayName())
 								.replace("%message", event.getMessage()));
 				if (DynmapManager.getDynmapCommonAPI() != null)
 					DynmapManager.getDynmapCommonAPI().sendBroadcastToWeb(
 							"Attention", event.getMessage());
 			}
 		} else {
 			// Typing in global channel
 			if (DynmapManager.getDynmapCommonAPI() != null)
 				DynmapManager.getDynmapCommonAPI()
 						.setDisableChatToWebProcessing(false);
 		}
 
 	}
 
 	@EventHandler
 	public void onPlayerQuit(final PlayerQuitEvent event) {
 		plugin.channels.remove(event.getPlayer());
 		plugin.ignores.remove(event.getPlayer());
 		/*
 		 * Removed IP-logging due to multi-alt griefers not being much of a
 		 * problem anymore
 		 */
 
 	}
 
 	@EventHandler
 	public void onBlockBreak(final BlockBreakEvent event) {
 		if (nukerEnabled) {
 			if (WarhubModChat.blockbreaks.get(event.getPlayer()) == null)
 				WarhubModChat.blockbreaks.put(event.getPlayer(), 0);
 			int breaks = WarhubModChat.blockbreaks.get(event.getPlayer());
 			breaks++;
 			WarhubModChat.blockbreaks.put(event.getPlayer(), breaks);
 			if (breaks > Config.config.getInt("blockspersecond")) {
 				event.setCancelled(true);
 				plugin.getServer().dispatchCommand(
 						plugin.getServer().getConsoleSender(),
 						"kick " + event.getPlayer().getName()
 								+ " Do not use nuker hacks!");
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockPlace(final BlockPlaceEvent event) {
 		if (nukerEnabled) {
 			if (WarhubModChat.blockbreaks.get(event.getPlayer()) == null)
 				WarhubModChat.blockbreaks.put(event.getPlayer(), 0);
 			int breaks = WarhubModChat.blockbreaks.get(event.getPlayer());
 			breaks++;
 			WarhubModChat.blockbreaks.put(event.getPlayer(), breaks);
 			if (breaks > Config.config.getInt("blockspersecond")) {
 				event.getBlock().breakNaturally();
 				event.setCancelled(true);
 				plugin.getServer().dispatchCommand(
 						plugin.getServer().getConsoleSender(),
 						"kick " + event.getPlayer().getName()
 								+ " Do not use fastplace hacks!");
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerCommandPreprocess(
 			final PlayerCommandPreprocessEvent event) {
 		String command = event.getMessage();
 		/*
 		 * Allow use of TooManyItems features
 		 */
 		if (command.startsWith("/toggledownfall")) {
 			event.setCancelled(true);
 			if (event.getPlayer().getWorld().hasStorm()) {
 				plugin.getServer().dispatchCommand(event.getPlayer(), "weather sun");
 			} else {
 				plugin.getServer().dispatchCommand(event.getPlayer(), "weather storm");
 			}
 		}
 		if (command.startsWith("/time set")) {
 			event.setCancelled(true);
 			int ticks;
 			try {
 				ticks = Integer.parseInt(command.split(" ")[2]);
 				plugin.getServer().dispatchCommand(event.getPlayer(), "time " + ticks + "ticks");
 			} catch (Exception e) {
 				event.getPlayer().sendMessage(ChatColor.RED + "/time set <ticks>");
 			}
 		}
 		if (command.startsWith("/give")) {
 			if (event.getPlayer().hasPermission("essentials.give"))
 				return;
 			event.setCancelled(true);
 			try {
 				int item = Integer.parseInt(command.split(" ")[2]);
 				int amount = Integer.parseInt(command.split(" ")[3]);
 				short data = Short.parseShort(command.split(" ")[4]);
 				plugin.getServer().dispatchCommand(event.getPlayer(), "i " + item + ":" + data + " " + amount);
 			} catch (Exception e) {
 				event.getPlayer().sendMessage(ChatColor.RED + "/give <player> <id> <amount> <data>");
 			}
 
 		}
 		/*
 		 * Action messages
 		 */
 		if (Config.config.getString("showactions") == "false") return;
 		if (command.startsWith("/unban") && event.getPlayer().hasPermission("essentials.unban")) {
 			String[] args = command.split(" ");
 			if (args.length > 1) {
 				for (OfflinePlayer oplr : Bukkit.getServer().getOfflinePlayers()) {
 					if (oplr.getName().equals(args[1])) {
 						for (Player cannotify : Bukkit.getServer().getOnlinePlayers()) {
 							if (cannotify.hasPermission("warhub.notify")) {
 								cannotify.sendMessage(ChatColor.RED + "Player " + event.getPlayer().getName() + " has unbanned the player " + oplr.getName());
 							}	
 						}
 					}
 				}
 			}
 		}
 		if ((command.startsWith("/jail") || command.startsWith("/unjail") || command.startsWith("/tjail") || command.startsWith("/togglejail")) && event.getPlayer().hasPermission("essentials.togglejail")) {
 			String[] args = command.split(" ");
 			if (args.length > 1) {
 				for (OfflinePlayer oplr : Bukkit.getServer().getOfflinePlayers()) {
 					if (oplr.getName().equals(args[1])) {
 						for (Player cannotify : Bukkit.getServer().getOnlinePlayers()) {
 							if (cannotify.hasPermission("warhub.notify")) {
 								cannotify.sendMessage(ChatColor.RED + "Player " + event.getPlayer().getName() + " toggled jail on " + oplr.getName());
 							}	
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	public String Capslock(Player player, String message) {
 		int countChars = 0;
 		int countCharsCaps = 0;
 
 		if (!player.hasPermission("warhub.moderator")) {
 			countChars = message.length();
 			if ((countChars > 0) && (countChars > 8)) {
 				for (int i = 0; i < countChars; i++) {
 					char c = message.charAt(i);
 					String ch = Character.toString(c);
 					if (ch.matches("[A-Z!?]")) {
 						countCharsCaps++;
 					}
 				}
 				if (100 / countChars * countCharsCaps >= 40) {
 					// Message has too many capital letters
 					message = message.toLowerCase();
 					plugin.warnings.put(player.getName(),
 							getWarnings(player) + 1);
 					if (getWarnings(player) % 50 == 0) {
 						plugin.getServer().dispatchCommand(
 								plugin.getServer().getConsoleSender(),
 								"kick " + player.getName()
 										+ " Do not type in caps!");
 						plugin.getServer().dispatchCommand(
 								plugin.getServer().getConsoleSender(),
 								"tempban " + player.getName() + " 20m");
 					} else if (getWarnings(player) % 5 == 0) {
 						plugin.getServer().dispatchCommand(
 								plugin.getServer().getConsoleSender(),
 								"kick " + player.getName()
 										+ " Do not type in caps!");
 					} else {
 						player.sendMessage(ChatColor.YELLOW
 								+ "Do not type in all caps please!");
 						/*
 						 * Removed this due to it being RLLY annoying
 						sendToMods(ChatColor.DARK_RED + "[WHChat] "
 								+ ChatColor.WHITE + player.getDisplayName()
 								+ ChatColor.YELLOW + " all caps'd ["
 								+ getWarnings(player) + " Violations]");
 								*/
 					}
 				}
 			}
 
 		}
 		return message;
 	}
 
 	private int getWarnings(Player key) {
 		if (plugin.warnings.get(key.getName()) == null) {
 			plugin.warnings.put(key.getName(), 0);
 		}
 		return plugin.warnings.get(key.getName());
 	}
 
 	private void sendToMods(String message) {
 		List<Player> sendto = new ArrayList<Player>();
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (p.hasPermission("warhub.moderator")) {
 				sendto.add(p);
 			}
 		}
 		for (Player p : sendto) {
 			p.sendMessage(message);
 		}
 	}
 }
