 package me.cmastudios.plugins.WarhubModChat;
 
 import java.util.ArrayList;
 import java.util.List;
 import me.cmastudios.plugins.WarhubModChat.util.Config;
 import me.cmastudios.plugins.WarhubModChat.util.Message;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
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
 		event.setMessage(Capslock(player, event.getMessage()));
 		ArrayList<Player> plrs = new ArrayList<Player>();
 		for (Player plr : plugin.getServer().getOnlinePlayers()) {
 			if (plugin.ignores.containsKey(plr))
 				plrs.add(plr);
 		}
 		for (Player plr : plrs) {
 			event.getRecipients().remove(plr);
 		}
 		if (event.getMessage().contains(ChatColor.COLOR_CHAR + "")
 				&& !player.hasPermission("warhub.moderator")) {
 			event.setMessage(event.getMessage().replaceAll(
 					ChatColor.COLOR_CHAR + "[0-9a-zA-Z]", ""));
 		}
 		if (plugin.channels.containsKey(event.getPlayer())) {
 			if (DynmapManager.getDynmapCommonAPI() != null)
 				DynmapManager.getDynmapCommonAPI()
 						.setDisableChatToWebProcessing(true);
 			if (plugin.channels.get(event.getPlayer()).equalsIgnoreCase("mod")) {
 				event.setCancelled(true);
 				sendToMods(Message
 						.colorizeText(Config.config.getString("modchat-format"))
 						.replace("%player", event.getPlayer().getDisplayName())
 						.replace("%message", event.getMessage()));
 				plugin.log.info("[MODCHAT] "
 						+ event.getPlayer().getDisplayName() + ": "
 						+ event.getMessage());
 			}
 			if (plugin.channels.get(event.getPlayer())
 					.equalsIgnoreCase("alert")) {
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
 			if (DynmapManager.getDynmapCommonAPI() != null)
 				DynmapManager.getDynmapCommonAPI()
 						.setDisableChatToWebProcessing(false);
 		}
 
 	}
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerJoin(final PlayerJoinEvent event) {
 		plugin.channels.remove(event.getPlayer());
 		plugin.ignores.remove(event.getPlayer());
 		/*
 		 * Removed IP-logging due to multi-alt griefers not being much of a
 		 * problem anymore
 		 */
 
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(final BlockBreakEvent event) {
 		if (nukerEnabled) {
 			if (WarhubModChat.blockbreaks.get(event.getPlayer()) == null)
 				WarhubModChat.blockbreaks.put(event.getPlayer(), 0);
 			int breaks = WarhubModChat.blockbreaks.get(event.getPlayer());
 			breaks++;
 			WarhubModChat.blockbreaks.put(event.getPlayer(), breaks);
 			if (breaks > Config.config.getInt("blockspersecond")) {
 				event.setCancelled(true);
 				PlayerWrapper banner = new PlayerWrapper("[WHChat]");
 				plugin.getServer().dispatchCommand(
 						banner,
 						"kick " + event.getPlayer().getName()
 								+ " Do not use nuker hacks!");
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
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
 				PlayerWrapper banner = new PlayerWrapper("[WHChat]");
 				plugin.getServer().dispatchCommand(
 						banner,
 						"kick " + event.getPlayer().getName()
 								+ " Do not use fastplace hacks!");
 			}
 		}
 	}
 
 	private String Capslock(Player player, String message) {
 		int countChars = 0;
 		int countCharsCaps = 0;
 
 		if (!player.hasPermission("warhub.moderator")) {
 			countChars = message.length();
 			if ((countChars > 0) && (countChars > 8)) {
 				for (int i = 0; i < countChars; i++) {
 					char c = message.charAt(i);
 					String ch = Character.toString(c);
 					if (ch.matches("[A-Z]")) {
 						countCharsCaps++;
 					}
 				}
 				if (100 / countChars * countCharsCaps >= 40) {
 					// Message has too many capital letters
 					message = message.toLowerCase();
 					plugin.warnings.put(player.getName(),
 							getWarnings(player) + 1);
 					PlayerWrapper banner = new PlayerWrapper("[WHChat]");
 					if (getWarnings(player) % 10 == 0) {
 						plugin.getServer().dispatchCommand(
 								banner,
 								"kick " + player.getName()
 										+ " Do not type in caps!");
 					} else if (getWarnings(player) % 50 == 0) {
 						plugin.getServer().dispatchCommand(
 								banner,
 								"kick " + player.getName()
 										+ " Do not type in caps!");
 						plugin.getServer().dispatchCommand(banner,
 								"tempban " + player.getName() + " 20m");
 					} else {
 						player.sendMessage(ChatColor.YELLOW
 								+ "Do not type in all caps ["
 								+ getWarnings(player) + " Violations]");
 						sendToMods(ChatColor.DARK_RED + "[WHChat] "
 								+ ChatColor.WHITE + player.getDisplayName()
 								+ ChatColor.YELLOW + " all caps'd ["
 								+ getWarnings(player) + " Violations]");
 					}
 				}
 			}
 
 		}
 		return message;
 	}
 
 	private int getWarnings(Player key) {
 		if (plugin.warnings.get(key.getName()) != null) {
 			return plugin.warnings.get(key.getName());
 		} else {
 			plugin.warnings.put(key.getName(), 0);
 		}
 		return 0;
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
