 package com.timvisee.safecreeper.listener;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Villager;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 
 import com.timvisee.safecreeper.SafeCreeper;
 import com.timvisee.safecreeper.util.SCUpdateChecker;
 
 public class SCPlayerListener implements Listener {
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player p = event.getEntity();
 		Location l = p.getLocation();
 		World w = l.getWorld();
 		Random rand = new Random();
 		
 		// Handle the 'CustomDrops' feature
 		// Make sure the custom drops feature is enabled
 		if(SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CustomDrops.Enabled", false, true, l)) {
 			
 			// Should Safe Creeper overwrite the default drops
 			if(SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CustomDrops.OverwriteDefaultDrops", false, true, l))
 				event.getDrops().clear();
 			
 			// Check if XP is enabled from the Custom Drops feature
 			if(SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CustomDrops.XP.Enabled", false, true, l)) {
 
 				// Should XP be dropped
 				if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CustomDrops.XP.DropXP", true, true, l))
 					event.setDroppedExp(0);
 				else {
 					
 					// Apply the drop chance
 					double dropChance = SafeCreeper.instance.getConfigManager().getOptionDouble(w, "PlayerControl", "CustomDrops.XP.DropChance", 100, true, l);
 					if(((int) dropChance * 10) <= rand.nextInt(1000))
 						event.setDroppedExp(0);
 					
 					// Apply the drop multiplier
 					double xpMultiplier = SafeCreeper.instance.getConfigManager().getOptionDouble(w, "PlayerControl", "CustomDrops.XP.Multiplier", 1, true, l);
 					if(xpMultiplier != 1 && xpMultiplier >= 0)
 						event.setDroppedExp((int) (event.getDroppedExp() * xpMultiplier));
 				}
 				
 				// Should XP be kept
 				if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CustomDrops.XP.KeepXP", false, true, l))
 					event.setNewTotalExp(0);
 				else
 					event.setNewTotalExp(p.getTotalExperience());
 				
 				// Should XP levels be kept
 				event.setKeepLevel(SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CustomDrops.XP.KeepLevel", false, true, l));
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player p = event.getPlayer();
 		Location l = event.getRespawnLocation();
 		World w = l.getWorld();
 		Random rand = new Random();
 		
 		// Check if mobs are able to spawn
 		String controlName = SafeCreeper.instance.getConfigManager().getControlName(p);
 		if(!SafeCreeper.instance.getConfigManager().isValidControl(controlName))
 			controlName = "OtherMobControl";
 		
 		boolean customHealthEnabled = SafeCreeper.instance.getConfigManager().getOptionBoolean(w, controlName, "CustomHealth.Enabled", false, true, l);
 		if(customHealthEnabled) {
 			int customHealthMin = SafeCreeper.instance.getConfigManager().getOptionInt(w, controlName, "CustomHealth.MinHealth", p.getMaxHealth(), true, l) - 1;
 			int customHealthMax = SafeCreeper.instance.getConfigManager().getOptionInt(w, controlName, "CustomHealth.MaxHealth", p.getMaxHealth(), true, l);
 			int customHealth =
 					rand.nextInt(Math.max(customHealthMax - customHealthMin, 1)) + customHealthMin;
 			
 			// Set the max health and the health of the player
 			p.setMaxHealth(customHealthMax);
 			p.setHealth(customHealth);
 		} else {
 			// Reset the max health of the player
 			p.setMaxHealth(20);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
 		Block b = event.getBlockClicked();
 		Location l = b.getLocation();
 		World w = b.getWorld();
 		
 		switch (event.getBucket()) {
 		case WATER_BUCKET:
 			if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "WaterControl", "CanPlaceWater", true, true, l))
 				event.setCancelled(true);
 			break;
 			
 		case LAVA_BUCKET:
 			if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "LavaControl", "CanPlaceLava", true, true, l))
 				event.setCancelled(true);
 			break;
 		default:
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
 		Player p = event.getPlayer();
 		Location l = event.getBed().getLocation();
 		World w = p.getWorld();
 		
 		// Check if the player is allowed to sleep, if not, cancel the event
 		if(!hasBypassPermission(event.getPlayer(), "BedControl", "PlayerCanSleep", false))
 			if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "BedControl", "PlayerCanSleep", true, true, l))
 				event.setCancelled(true);
 		
 		// Play the control effects
 		if(!event.isCancelled())
 			SafeCreeper.instance.getConfigManager().playControlEffects("PlayerControl", "Sleeping", l);
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		Block b = event.getClickedBlock();
 		
 		// Make sure the block instance is not null
 		if(b == null)
 			return;
 		
 		Location l = b.getLocation();
 		World w = l.getWorld();
 		Environment we = w.getEnvironment();
 		Action a = event.getAction();
 		
 		// Is the player interacting with a bed
 		if(b.getType().equals(Material.BED_BLOCK)) {
 			// Is the player trying to enter the bed
 			if(a.equals(Action.RIGHT_CLICK_BLOCK)) {
 				// Is the bed going to explode in this world environment
 				if(we.equals(Environment.NETHER) || we.equals(Environment.THE_END)) {
 					// Check if the bed's may explode
 					if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "BedControl", "CanExplode", true, true, l)) {
 						// Cancel the bed interaction which causes the bed to explode
 						event.setCancelled(true);
 						
 						// Show a message to the player
 						switch(we) {
 						case NETHER:
 							p.sendMessage("You can't sleep in the Nether!");
 							break;
 						case THE_END:
 							p.sendMessage("You can't sleep in The End!");
 							break;
 						default:
 							p.sendMessage("You can't sleep right here!");
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		Entity e = event.getRightClicked();
 		Location l = e.getLocation();
 		World w = l.getWorld();
 		
 		// Control villager trading
 		if(e instanceof Villager) {
 			if(!hasBypassPermission(event.getPlayer(), "VillagerControl", "CanTrade", false))
 				if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "VillagerControl", "CanTrade", true, true, l))
 					event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		Location l = p.getLocation();
 		
 		// Make sure the player has permission to see update notifications
 		if(SafeCreeper.instance.getPermissionsManager().hasPermission(p, "safecreeper.notification.update", p.isOp()) &&
				SafeCreeper.instance.getConfig().getBoolean("updateChecker.enabled", true) &&
				SafeCreeper.instance.getConfig().getBoolean("updateChecker.notifyForUpdatesInGame", true)) {
 			
 			SCUpdateChecker uc = SafeCreeper.instance.getUpdateChecker();
 			
 			// Check if any update exists
 			if(uc.isNewVersionAvailable()) {
 				final String newVer = uc.getNewestVersion();
 				
 				// Is the update important
 				if(uc.isImportantUpdateAvailable()) {
 					if(!uc.isNewVersionCompatibleWithCurrentBukkit()) {
 						p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] New important Safe Creeper update available! (v" + newVer + ")");
 						p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] Version not compatible, please update to Bukkit " + uc.getRequiredBukkitVersion() + " or higher!");
 					} else {
 						if(uc.isUpdateDownloaded()) {
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] New important Safe Creeper update installed! (v" + newVer + ")");
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] Server reload required!");
 						} else {
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] New important Safe Creeper update available! (v" + newVer + ")");
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] Use " + ChatColor.GOLD + "/sc installupdate" + ChatColor.YELLOW + " to install the update!");
 						}
 					}
 				} else {
 					if(uc.isNewVersionCompatibleWithCurrentBukkit()) {
 						if(uc.isUpdateDownloaded()) {
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] New Safe Creeper update installed! (v" + newVer + ")");
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] Server reload required!");
 						} else {
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] New important Safe Creeper update available! (v" + newVer + ")");
 							p.sendMessage(ChatColor.YELLOW + "[SafeCreeper] Use " + ChatColor.GOLD + "/sc installupdate" + ChatColor.YELLOW + " to install the update!");
 						}
 					}
 				}
 			}
 		}
 		
 		// Play effects
 		SafeCreeper.instance.getConfigManager().playControlEffects("PlayerControl", "Join", l);
 	}
 	
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		Player p = event.getPlayer();
 		Location l = p.getLocation();
 		World w = l.getWorld();
 		
 		if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CanPickupItems", true, true, l))
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
 		Player p = event.getPlayer();
 		Location l = p.getLocation();
 		World w = l.getWorld();
 		boolean sneaking = event.isSneaking();
 		
 		// Is the player allowed to sneak
 		if(!sneaking)
 			if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CanSneak", true, true, l))
 				event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
 		Player p = event.getPlayer();
 		Location l = p.getLocation();
 		World w = l.getWorld();
 		boolean sprinting = event.isSprinting();
 		
 		// Is the player allowed to sneak
 		if(!sprinting)
 			if(!SafeCreeper.instance.getConfigManager().getOptionBoolean(w, "PlayerControl", "CanSprint", true, true, l))
 				event.setCancelled(true);
 	}
 	
 	public boolean hasBypassPermission(Player player, String controlName, String bypassName, boolean def) {
 		if(!SafeCreeper.instance.getPermissionsManager().isEnabled())
 			return def;
 		return SafeCreeper.instance.getPermissionsManager().hasPermission(player, "safecreeper.bypass." + controlName + "." + bypassName, def);
 	}
 }
