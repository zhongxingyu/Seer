 package net.slipcor.pvparena.modules.flyspectate;
 
 import java.util.HashSet;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.core.Debug;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class RealSpectateListener implements Listener {
 	final FlySpectate rs;
 	HashSet<Player> spectators = new HashSet<Player>();
 	Debug debug = new Debug(456);
 	public RealSpectateListener(FlySpectate realSpectate) {
 		rs = realSpectate;
 	}
 
 	void initiate(ArenaPlayer ap) {
 		for (ArenaPlayer a : rs.getArena().getEveryone()) {
 			a.get().hidePlayer(ap.get());
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		if (event.getEntityType() != EntityType.PLAYER) {
 			return;
 		}
 		
 		Player subject = (Player) event.getEntity();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player subject = event.getPlayer();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		// --> cancel
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		Player subject = event.getPlayer();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		// --> cancel
 		event.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player subject = event.getPlayer();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		// --> cancel
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onInventoryOpen(InventoryOpenEvent event) {
 		Player subject = (Player) event.getPlayer();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		// --> cancel
 		event.setCancelled(true);
 		event.getPlayer().closeInventory();
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onInventoryClick(InventoryClickEvent event) {
 		Player subject = (Player) event.getWhoClicked();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		// --> cancel
 		event.setCancelled(true);
 		event.getWhoClicked().closeInventory();
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event == null ||
				event.getEntity() == null ||
				event.getEntity().getShooter() == null ||
				!event.getEntity().getShooter().getType().equals(EntityType.PLAYER)) {
 			return;
 		}
 		Player subject = (Player) event.getEntity().getShooter();
 		
 		if (!spectators.contains(subject)) {
 			return;
 		}
 		
 		// subject is spectating
 		// --> cancel
 		event.setCancelled(true);
 	}
 
 	public void hidePlayerLater(final Player s) {
 		if (!spectators.contains(s)) {
 			spectators.add(s);
 
 			class LaterRun implements Runnable {
 				@Override
 				public void run() {
 					
 					for (ArenaPlayer ap : rs.getArena().getEveryone()) {
 						ap.get().hidePlayer(s);
 					}
 				}
 			}
 			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(), 5L);
 		}
 	}
 
 	public void hideAllSpectatorsLater() {
 		for (Player s : spectators) {
 
 			class LaterRun implements Runnable {
 				private final Player s;
 				LaterRun(final Player p) {
 					s = p;
 				}
 				@Override
 				public void run() {
 					for (ArenaPlayer ap : rs.getArena().getEveryone()) {
 						ap.get().hidePlayer(s);
 					}
 				}
 			}
 			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(s), 5L);
 		}
 	}
 
 	public void removeSpectator(Player spectator) {
 		spectators.remove(spectator);
 	}
 
 	public void stop() {
 		HashSet<Player> removals = new HashSet<Player>();
 		removals.addAll(spectators);
 		for (Player p : removals) {
 			Bukkit.getServer().dispatchCommand(p, "pa leave");
 		}
 		spectators.clear();
 	}
 }
