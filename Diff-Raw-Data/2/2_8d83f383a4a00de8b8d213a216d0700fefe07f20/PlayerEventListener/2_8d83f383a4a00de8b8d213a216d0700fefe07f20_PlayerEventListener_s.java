 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import com.araeosia.ArcherGames.utils.Archer;
 import com.araeosia.ArcherGames.utils.BookItem;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.ItemStack;
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
 		if (plugin.configToggles.get("lockdownMode") && event.getPlayer().hasPermission("archergames.overrides.lockdown")) {
 			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, plugin.strings.get("kickLockdown"));
 			return;
 		}
 		if(event.getPlayer().isOp()){
 			event.getPlayer().setDisplayName(ChatColor.RED + event.getPlayer().getName() + ChatColor.WHITE);
 		} else if(event.getPlayer().hasPermission("ArcherGames.color.mod")){
 			event.getPlayer().setDisplayName(ChatColor.DARK_RED + event.getPlayer().getName() + ChatColor.WHITE);
 		} else if(event.getPlayer().hasPermission("ArcherGames.color.god")){
 			event.getPlayer().setDisplayName(ChatColor.GOLD +""+ ChatColor.ITALIC + event.getPlayer().getName() + ChatColor.WHITE);
 		}
 		Archer a = new Archer(event.getPlayer().getName());
 		ArcherGames.players.add(a);
 	}
 
 	@EventHandler
 	public void onJoinEvent(final PlayerJoinEvent event) {
 		event.getPlayer().setAllowFlight(true);
 		if(ScheduledTasks.gameStatus >= 2){
 			//Vanish?
 		}
 		event.getPlayer().sendMessage(String.format(plugin.strings.get("joinedgame"), event.getPlayer().getName(), plugin.strings.get("servername")));
 		int taskID = plugin.scheduler.nagPlayerKit(event.getPlayer());
 		naggerTask.put(event.getPlayer().getName(), taskID);
 		event.getPlayer().getInventory().clear();
 		if (!event.getPlayer().getInventory().contains(Material.BOOK)) {
 			BookItem bi = new BookItem(new ItemStack(387, 1));
 			bi.setAuthor(plugin.getConfig().getString("ArcherGames.startbook.author"));
 			bi.setTitle(plugin.getConfig().getString("ArcherGames.startbook.Title"));
 			String[] newPages;
 			newPages = new String[11];
 			String[] pages = plugin.getConfig().getStringList("ArcherGames.startbook.pages").toArray(newPages);
 			bi.setPages(pages);
 			event.getPlayer().getInventory().addItem(bi.getItemStack());
 		}
 		plugin.db.recordJoin(event.getPlayer().getName());
 		event.setJoinMessage("");
 	}
 
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onPlayerChatEvent(final AsyncPlayerChatEvent event) {
 		// If the player is allowed to talk, pass their message on, Else cancel the event
 		Archer archer = Archer.getByName(event.getPlayer().getName());
 		if (!archer.canTalk() && !event.getPlayer().hasPermission("archergames.overrides.chat")) {
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
 		if (event.getEntity() instanceof Player && !event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
 			Player player = (Player) event.getEntity();
 			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5 || !(plugin.serverwide.getArcher(player).isAlive())) {
 				if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onDamageByEntity(final EntityDamageByEntityEvent event){
 		if(event.getEntity() instanceof Player){
 			Player player = (Player) event.getEntity();
			if(ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5 || !(plugin.serverwide.getArcher(player).isAlive())){
 				if(event.getDamager() instanceof Player){ // PVP
 					Player attacker = (Player) event.getDamager();
 					if(!plugin.serverwide.getArcher(attacker).isAlive()){
 						event.setCancelled(true);
 						attacker.sendMessage(plugin.strings.get("nopvp"));
 					}
 				}else{
 					if((event.getDamager() instanceof Slime || event.getDamager() instanceof Spider) && ScheduledTasks.gameStatus==1 || ScheduledTasks.gameStatus == 5){
 						event.getDamager().remove();
 					}
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
 			if (!(ScheduledTasks.gameStatus == 1) && !(ScheduledTasks.gameStatus == 2) && !(ScheduledTasks.gameStatus == 5)) {
 				/*if (plugin.serverwide.livingPlayers.size() == 3) {
 					plugin.econ.givePlayer(event.getEntity().getName(), 5000);
 				} else if (plugin.serverwide.livingPlayers.size() == 2) {
 					plugin.econ.givePlayer(event.getEntity().getName(), 10000);
 				}*/
 				plugin.serverwide.killPlayer(event.getEntity().getName());
 /*
 				if (plugin.serverwide.livingPlayers.size() == 1) {
 					for (Archer a : plugin.serverwide.livingPlayers) { // should only be one player, the winner
 						plugin.econ.givePlayer(a.getName(), 15000);
 						plugin.db.addWin(player.getName());
 						plugin.winner = a;
 					}
 				}*/
 
 				if (event.getEntity().getKiller() instanceof Player) {
 //					plugin.serverwide.getArcher(event.getEntity().getKiller()).setPoints(plugin.serverwide.getArcher(event.getEntity().getKiller()).getPoints() + 1);
 				}
 				event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation()); // Theatric effect.
 			}
 		}
 	}
 
 	@EventHandler
 	public void onQuitEvent(final PlayerQuitEvent event) {
 		if (!Archer.getByName(event.getPlayer().getName()).isAlive() && !event.getPlayer().hasPermission("archergames.quitkill.override")) {
 			plugin.serverwide.killPlayer(event.getPlayer().getName());
 			for (ItemStack is : event.getPlayer().getInventory().getContents()) {
 				event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), is);
 			}
 			event.getPlayer().getInventory().clear();
 		}
 		plugin.db.recordQuit(event.getPlayer().getName());
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
 			if (!event.getPlayer().getAllowFlight()) {
 				event.getPlayer().setAllowFlight(true);
 			}
 			event.getPlayer().sendMessage(plugin.strings.get("respawn"));
 		}
 	}
 
 	@EventHandler
 	public void onPlayerDropItem(final PlayerDropItemEvent event) {
 		if (ScheduledTasks.gameStatus == 1) {
 			event.getPlayer().sendMessage(plugin.strings.get("nodroppickup"));
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
 		if (ScheduledTasks.gameStatus == 1) {
 			event.getPlayer().sendMessage(plugin.strings.get("nodroppickup"));
 			event.setCancelled(true);
 		}
 	}
 }
