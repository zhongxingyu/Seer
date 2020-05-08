 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import com.araeosia.ArcherGames.utils.Archer;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 import org.kitteh.vanish.staticaccess.VanishNotLoadedException;
 
 public class PlayerEventListener implements Listener {
 
 	public ArcherGames plugin;
 	public int howLongToWait; // How long do we wait?
 	public static HashMap<String, Integer> naggerTask = new HashMap<String, Integer>();
 	public PlayerEventListener(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onLoginEvent(final PlayerLoginEvent event) {
 		Archer a = new Archer(event.getPlayer().getName());
 		ArcherGames.players.add(a);
 		event.getPlayer().sendMessage(String.format(plugin.strings.get("joinedgame"), event.getPlayer().getName(), plugin.strings.get("servername")));
 		naggerTask.put(event.getPlayer().getName(), plugin.scheduler.nagPlayerKit(event.getPlayer().getName()));
 		// TODO: Give them a book if they don't have one.
 	}
 
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onPlayerChatEvent(final AsyncPlayerChatEvent event) {
 		// If the player is allowed to talk, pass their message on, Else cancel the event
 		Archer archer = Archer.getByName(event.getPlayer().getName());
 		if (!archer.canTalk && !event.getPlayer().hasPermission("archergames.chat.override")) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("nochat"));
 		}
 	}
 
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onDamageEvent(final EntityDamageEvent event) {
 		if (event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
 			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5 || !(plugin.serverwide.getArcher(player).isAlive())) {
 				if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onDeathEvent(final PlayerDeathEvent event) {
 		if (event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
 			if (!(ScheduledTasks.gameStatus == 1) || !(ScheduledTasks.gameStatus == 2) || !(ScheduledTasks.gameStatus == 5) || (plugin.serverwide.getArcher(player).isAlive())) {
 				plugin.serverwide.killPlayer(event.getEntity().getName());
 
 				if (event.getEntity().getKiller() instanceof Player) {
 //					plugin.serverwide.getArcher(event.getEntity().getKiller()).setPoints(plugin.serverwide.getArcher(event.getEntity().getKiller()).getPoints() + 1);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onQuitEvent(final PlayerQuitEvent event) {
 		if (!Archer.getByName(event.getPlayer().getName()).isAlive() && !event.getPlayer().hasPermission("archergames.quitkill.override")) {
 			plugin.serverwide.killPlayer(event.getPlayer().getName());
 		}
 	}
 
 	@EventHandler
 	public void onRespawnEvent(final PlayerRespawnEvent event) {
 		if (!Archer.getByName(event.getPlayer().getName()).isAlive()) {
 			try {
 				if (!VanishNoPacket.isVanished(event.getPlayer().getName())) {
 					if (plugin.debug) {
 						plugin.log.info(event.getPlayer().getName() + " respawned and was made invisible.");
 					}
 					VanishNoPacket.toggleVanishSilent(event.getPlayer());
 				}
 			} catch (VanishNotLoadedException ex) {
 				Logger.getLogger(PlayerEventListener.class.getName()).log(Level.SEVERE, null, ex);
 			}
 			event.getPlayer().sendMessage(plugin.strings.get("respawn"));
 		}
 	}
 }
