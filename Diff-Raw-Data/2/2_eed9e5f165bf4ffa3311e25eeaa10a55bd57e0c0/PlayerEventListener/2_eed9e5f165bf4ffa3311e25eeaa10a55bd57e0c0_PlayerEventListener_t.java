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
 		if (plugin.configToggles.get("lockdownMode") && !event.getPlayer().hasPermission("archergames.overrides.lockdown")) {
 			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, plugin.strings.get("kickLockdown"));
 			return;
 		}
 		Archer a = new Archer(event.getPlayer().getName());
 		ArcherGames.players.add(a);
 	}
 
 	@EventHandler
 	public void onJoinEvent(final PlayerJoinEvent event) {
 		event.getPlayer().setAllowFlight(true);
 		if (ScheduledTasks.gameStatus >= 2) {
 			plugin.serverwide.leaveGame(event.getPlayer().getName());
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
 
 			event.getPlayer().setAllowFlight(true);
 		}
 		if (event.getPlayer().isOp() || event.getPlayer().hasPermission("ArcherGames.color.admin")) {
 			event.getPlayer().setDisplayName(ChatColor.RED + event.getPlayer().getName() + ChatColor.WHITE);
 		} else if (event.getPlayer().hasPermission("ArcherGames.color.mod")) {
 			event.getPlayer().setDisplayName(ChatColor.DARK_RED + event.getPlayer().getName() + ChatColor.WHITE);
 		} else if (event.getPlayer().hasPermission("ArcherGames.kit.God")) {
 			event.getPlayer().setDisplayName(ChatColor.GOLD + "" + ChatColor.ITALIC + event.getPlayer().getName() + ChatColor.WHITE);
 		} else if (event.getPlayer().hasPermission("ArcherGames.kit.Ridiculous")) {
 			event.getPlayer().setDisplayName(ChatColor.GOLD + "" + event.getPlayer().getName() + ChatColor.WHITE);
 		} else if (event.getPlayer().hasPermission("archergames.kit.Donor.Infinity") || event.getPlayer().hasPermission("archergames.kit.Donor.Lucky") || event.getPlayer().hasPermission("archergames.kit.Donor.Feeder") || event.getPlayer().hasPermission("archergames.kit.Donor.Juggernaut") || event.getPlayer().hasPermission("archergames.kit.VIP.Healer") || event.getPlayer().hasPermission("archergames.kit.VIP.Sharpshooter") || event.getPlayer().hasPermission("archergames.kit.VIP.Punch") || event.getPlayer().hasPermission("archergames.kit.VIP.Wizard") || event.getPlayer().hasPermission("archergames.kit.VIP.Chef") || event.getPlayer().hasPermission("archergames.kit.VIP.Beastmaker") || event.getPlayer().hasPermission("archergames.kit.Elite.Diamond") || event.getPlayer().hasPermission("archergames.kit.Elite.Miner") || event.getPlayer().hasPermission("archergames.kit.Elite.Baker") || event.getPlayer().hasPermission("archergames.kit.Elite.Tank") || event.getPlayer().hasPermission("archergames.kit.Elite.Prophet")) {
 			event.getPlayer().setDisplayName(ChatColor.GOLD + "" + event.getPlayer().getName() + ChatColor.WHITE);
 		}
 		event.getPlayer().sendMessage(String.format(plugin.strings.get("joinedgame"), event.getPlayer().getName(), plugin.strings.get("servername")));
 		event.getPlayer().sendMessage("§4If you're seeing this, that means that you've connected to the BETA version of ArcherGames. This means that stuff won't work and that there will be bugs.");
 		event.getPlayer().sendMessage("§gIf you don't want to deal with this, hop on another server and see if it's also a beta version. Sorry about any inconvenience.");
 		int taskID = plugin.scheduler.nagPlayerKit(event.getPlayer().getName());
 		naggerTask.put(event.getPlayer().getName(), taskID);
 		if (!event.getPlayer().getInventory().contains(Material.BOOK)) {
 			BookItem bi = new BookItem(new ItemStack(387, 1));
 			bi.setAuthor(plugin.getConfig().getString("ArcherGames.startbook.author"));
 			bi.setTitle(plugin.getConfig().getString("ArcherGames.startbook.Title"));
 			String[] pages = plugin.getConfig().getStringList("ArcherGames.startbook.pages").toArray(new String[11]);
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
		if ((!archer.isReady() && !event.getPlayer().hasPermission("archergames.overrides.chat")) && ScheduledTasks.gameStatus == 1) {
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
 	public void onDamageByEntity(final EntityDamageByEntityEvent event) {
 		if (event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
 			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5 || !(plugin.serverwide.getArcher(player).isAlive())) {
 				if (event.getDamager() instanceof Player) { // PVP
 					Player attacker = (Player) event.getDamager();
 					event.setCancelled(true);
 					attacker.sendMessage(plugin.strings.get("nopvp"));
 				} else {
 					if ((event.getDamager() instanceof Slime || event.getDamager() instanceof Spider) && ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 5) {
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
 			if (ScheduledTasks.gameStatus != 1 && ScheduledTasks.gameStatus != 5) {
 				if (Archer.getByName(player.getName()).isAlive() && plugin.serverwide.livingPlayers.contains(Archer.getByName(player.getName()))) {
 					plugin.serverwide.leaveGame(event.getEntity().getName());
 				}
 
 				if (event.getEntity().getKiller() instanceof Player) {
 //					plugin.serverwide.getArcher(event.getEntity().getKiller()).setPoints(plugin.serverwide.getArcher(event.getEntity().getKiller()).getPoints() + 1);
 				}
 //				event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation()); // Theatric effect.
 			}
 		}
 	}
 
 	@EventHandler
 	public void onQuitEvent(final PlayerQuitEvent event) {
 		if (naggerTask.containsKey(event.getPlayer().getName())) {
 			plugin.getServer().getScheduler().cancelTask(naggerTask.get(event.getPlayer().getName()));
 			naggerTask.remove(event.getPlayer().getName());
 		}
 		if (Archer.getByName(event.getPlayer().getName()).isAlive()) {
 			if (ScheduledTasks.gameStatus != 1 && ScheduledTasks.gameStatus != 2 && ScheduledTasks.gameStatus != 5) {
 				plugin.serverwide.leaveGame(event.getPlayer().getName());
 				for (ItemStack is : event.getPlayer().getInventory().getContents()) {
 					if (is != null) {
 						event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), is);
 					}
 				}
 				event.getPlayer().getInventory().clear();
 			}
 		}
 		plugin.serverwide.livingPlayers.remove(Archer.getByName(event.getPlayer().getName()));
 		plugin.db.recordQuit(event.getPlayer().getName());
 		event.setQuitMessage("");
 	}
 
 	@EventHandler
 	public void onRespawnEvent(final PlayerRespawnEvent event) {
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
 		event.getPlayer().setAllowFlight(true);
 		event.getPlayer().sendMessage(plugin.strings.get("respawn"));
 	}
 
 	@EventHandler
 	public void onPlayerDropItem(final PlayerDropItemEvent event) {
 		if (ScheduledTasks.gameStatus == 1) {
 			event.getPlayer().sendMessage(plugin.strings.get("nodroppickup"));
 			event.setCancelled(true);
 		}
 		if (!plugin.serverwide.getArcher(event.getPlayer()).isAlive()) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
 		if (ScheduledTasks.gameStatus == 1) {
 			event.getPlayer().sendMessage(plugin.strings.get("nodroppickup"));
 			event.getItem().remove();
 			event.setCancelled(true);
 		}
 		if (!plugin.serverwide.getArcher(event.getPlayer()).isAlive()) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
 		if (plugin.serverwide.ridingPlayers.contains(event.getPlayer().getName())) {
 			if (event.getRightClicked() instanceof Player) {
 				Player ridden = (Player) event.getRightClicked();
 				Player player = event.getPlayer();
 				if (player.getPassenger() == null) {
 					if (player.getVehicle() == null) {
 						ridden.setPassenger(player);
 					} else {
 						player.getVehicle().eject();
 					}
 				}
 			}
 		}
 	}
 }
