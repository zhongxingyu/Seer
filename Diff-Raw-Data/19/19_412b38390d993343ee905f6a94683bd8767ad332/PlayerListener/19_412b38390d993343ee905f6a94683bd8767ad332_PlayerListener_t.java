 package de.blablubbabc.dreamworld.listeners;
 
 import java.util.List;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import de.blablubbabc.dreamworld.DreamworldPlugin;
 import de.blablubbabc.dreamworld.managers.ConfigManager;
 
 public class PlayerListener extends AbstractListener {
 
 	public PlayerListener(DreamworldPlugin plugin) {
 		super(plugin);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBedLeave(PlayerBedLeaveEvent event) {
 		plugin.getDreamManager().onBedLeave(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		plugin.getDreamManager().continueDreaming(event.getPlayer(), true);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onDisconnect(PlayerQuitEvent event) {
 		plugin.getDreamManager().onDisconnect(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		// hopefully this will never be called:
 		plugin.getDreamManager().onPlayerDeath(event.getEntity());
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		// this should only be necessary, if the player was dead during plugin enable and coudln't continue his dream there:
 		final Player player = event.getPlayer();
 		if (plugin.getDreamDataStore().getStoredDreamData(player.getName()) != null) {
 			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 				
 				@Override
 				public void run() {
 					if (player.isOnline() && !player.isDead()) plugin.getDreamManager().continueDreaming(player, false);
 				}
 			}, 1L);
 		}
 	}
 
 	// disabled stuff during dreaming:
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onCommand(PlayerCommandPreprocessEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.getDreamManager().isDreaming(player.getName())) {
 			if (isAllowedCommand(event.getMessage())) {
 				if (!player.hasPermission(plugin.ADMIN_PERMISSION)) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	private boolean isAllowedCommand(String cmd) {
 		List<String> allowedCommands = plugin.getConfigManager().allowedCommands;
 		if (allowedCommands.contains(cmd)) return true;
 		String[] split = cmd.split(" ");
 		String cmds = "";
 		for (int i = 0; i < split.length; i++) {
 			cmds += split[i];
 			if (allowedCommands.contains(cmds) || allowedCommands.contains(cmds + " *")) return true;
 			cmds += " ";
 		}
 		return false;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onHunger(FoodLevelChangeEvent event) {
 		if (event.getEntityType() == EntityType.PLAYER) {
 			Player player = (Player) event.getEntity();
 			if (plugin.getDreamManager().isDreaming(player.getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onDamage(EntityDamageEvent event) {
 		if (event.getEntityType() == EntityType.PLAYER) {
 			DamageCause cause = event.getCause();
 			ConfigManager config = plugin.getConfigManager();
 			Player player = (Player) event.getEntity();
			if (plugin.getDreamManager().isDreaming(player.getName())) {
				if (config.allDamageDisabled || (cause == DamageCause.FALL && config.fallDamageDisabled) || ((cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.PROJECTILE) && config.entityDamageDisabled)) {
 					event.setCancelled(true);
 					if (cause == DamageCause.VOID) {
 						// respawn player:
 						plugin.getDreamManager().spawnPlayer(player);
 					}
				} else {
					// prevent death:
					if (event.getDamage() >= player.getHealth()) {
						event.setCancelled(true);
						// end dream:
						plugin.getDreamManager().onPlayerDeath(player);
					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onItemDrop(PlayerDropItemEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.getConfigManager().itemDroppingDisabled && plugin.getDreamManager().isDreaming(player.getName())) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onItemPickup(PlayerPickupItemEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.getConfigManager().itemPickupDisabled && plugin.getDreamManager().isDreaming(player.getName())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockPlacing(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.getConfigManager().blockPlacingDisabled && plugin.getDreamManager().isDreaming(player.getName())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockBreak(PlayerDropItemEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.getConfigManager().blockBreakingDisabled && plugin.getDreamManager().isDreaming(player.getName())) {
 			event.setCancelled(true);
 		}
 	}
 
 }
