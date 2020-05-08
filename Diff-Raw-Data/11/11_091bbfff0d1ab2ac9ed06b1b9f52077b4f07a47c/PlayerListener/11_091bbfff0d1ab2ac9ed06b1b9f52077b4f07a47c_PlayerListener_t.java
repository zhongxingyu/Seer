 package com.etriacraft.etriabending.listeners;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 import me.ryanhamshire.GriefPrevention.PlayerData;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.Inventory;
 import com.etriacraft.etriabending.EtriaBending;
 import com.etriacraft.etriabending.suites.MessagingSuite;
 import com.etriacraft.etriabending.suites.PlayerSuite;
 import com.etriacraft.etriabending.util.Utils;
 
 public class PlayerListener implements Listener {
 
 	EtriaBending plugin;
 	public PlayerListener(EtriaBending instance) {
 		this.plugin = instance;
 	}
 	
 	public static List<String> ignoring = new ArrayList();
 	public static Set<Player> players = new HashSet();
 
 	public static String joinmessage;
 	public static String welcomemessage;
 	public static String quitmessage;
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e) {
 		for (String o : PlayerSuite.vanishDb) {
 			// Hides invisible players from the player that is signing in.
 			if (e.getPlayer().hasPermission("ec.vanish.seehidden")) continue;
 			if (Bukkit.getPlayer(o) == null) continue;
 			e.getPlayer().hidePlayer(Bukkit.getPlayer(o));
 		}
 		// If the player forgot to unvanish before signing out, unvanish them now.
 		if (PlayerSuite.isVanished(e.getPlayer())) {
 			PlayerSuite.setVanished(e.getPlayer(), false);
 			e.getPlayer().sendMessage("cYou are no longer vanished.");
 		}
 
 		MessagingSuite.showMotd(e.getPlayer());
 
 	}
 
 	@EventHandler
 	public static void ServerListPingEvent(ServerListPingEvent e) {
 		if (PlayerSuite.maintenanceon) {
 			// If Maintenance Mode is on, change the Motd.
 			e.setMotd("Server In Maintenance Mode.");
 		} else {
 			e.setMotd(e.getMotd());
 		}
 
 	}
 
 	@EventHandler
 	public void playerquitmessage(PlayerQuitEvent e) {
 		// Send the player disconnect message.
 		e.setQuitMessage(Utils.colorize(quitmessage).replaceAll("<name>", e.getPlayer().getDisplayName()));
 	}
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onLogin(PlayerLoginEvent event) {
 		if (event.getResult() == Result.KICK_FULL) {
 			// Allow the player to bypass the server login limit.
 			if (event.getPlayer().hasPermission("eb.loginlimit.bypass")) {
 				event.allow();
 			} else {
 				event.setKickMessage("cThe Server is Full!");
 			}
 			// If Maintenance Mode is on, kick the player if they do not have permission.
 		} if (PlayerSuite.maintenanceon) {
 			if (event.getPlayer().isOp() || (event.getPlayer().hasPermission("eb.maintenance.safe"))) {
 				event.getPlayer().sendMessage("aThis server is undergoing maintenance mode.");
 			} else {
				event.disallow(Result.KICK_OTHER, "aYou can't join while the server is in maintenance mode.");
 			}
 		}
 	}
 
 	@EventHandler
 	public void playerjoinmessages(PlayerJoinEvent e) {
 		Player p = e.getPlayer();
 		String displayName;
 		// If a Display Name is set, change the player's name.
 		if (!(plugin.getConfig().get("displaynames." + p.getName()) == null)) {
 			displayName = plugin.getConfig().getString("displaynames." + p.getName());
 		} else {
 			// Display Name is the player's normal name if display name is not set.
 			displayName = p.getName();
 		}
 		p.setDisplayName(displayName);
 		if (!p.hasPlayedBefore()) {
 			// Sends the player the welcome message if they have not played before.
 			e.setJoinMessage(Utils.colorize(welcomemessage).replaceAll("<name>", p.getDisplayName()));
 		} else {
 			// If they have played before, send them the normal join message..
 			e.setJoinMessage(Utils.colorize(joinmessage).replaceAll("<name>", p.getDisplayName()));
 		}
 	}
 
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent e) {
 
 		// Saves EXP on death.
 		if (e.getEntity().hasPermission("eb.savexp")) {
 			e.setKeepLevel(true);
 			e.setDroppedExp(0);
 		}
 	}
 
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		// Hides a vanished player's actions.
 		if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
 		if (e.getClickedBlock().getType().equals(Material.REDSTONE_ORE) && PlayerSuite.isVanished(e.getPlayer())) e.setCancelled(true);
 
 		if (PlayerSuite.isVanished(e.getPlayer()) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.CHEST)) {
 			e.setCancelled(true);
 			final Chest c = (Chest) e.getClickedBlock().getState();
 			final Inventory i = Bukkit.getServer().createInventory(e.getPlayer(), c.getInventory().getSize());
 			i.setContents(c.getInventory().getContents());
 			e.getPlayer().openInventory(i);
 			PlayerSuite.silentChestOpen(e.getPlayer());
 		}
 	}
 
 	@EventHandler
 	public void onTeleport(PlayerTeleportEvent event) {
 
 		// Checks GriefPrevention to see if the player is in PvP Combat.
 		PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(event.getPlayer().getName());
 		if (playerData.inPvpCombat()) {
 			TeleportCause cause = event.getCause();
 			event.getPlayer().sendMessage(ChatColor.RED + "You can't teleport while in PvP Combat.");
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void playerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		if (player.hasPermission("eb.ignore.bypass")) {
 			return;
 		}
 		String sPlayer = player.getName();
 		players = event.getRecipients();
 		Player[] playersOnline = Bukkit.getOnlinePlayers();
 		Player tempPlayer = null;
 		String sTempPlayer = "";
 		for (int i = 0; i < playersOnline.length; i++) {
 			tempPlayer = playersOnline[i];
 			if (tempPlayer != player) {
 				sTempPlayer = tempPlayer.getName().toLowerCase();
 				if (plugin.getConfig().getStringList("players." + sTempPlayer + ".ignoring") != null) {
 					ignoring = this.plugin.getConfig().getStringList("players." + sTempPlayer + ".ignoring");
 					for (int p = 0; p < ignoring.size(); p++) {
 						String ignored = (String)ignoring.get(p);
 						if (ignored.equalsIgnoreCase(sPlayer)) {
 							event.getRecipients().remove(tempPlayer);
 							players = event.getRecipients();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
 		// If the player is Vanished, don't allow them to pick up the item.
 		if (PlayerSuite.isVanished(e.getPlayer())) e.setCancelled(true);
 	}
 }
