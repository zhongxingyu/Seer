 package com.wolflink289.bukkit.worldregions.listen;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import com.wolflink289.bukkit.worldregions.WorldRegionsFlags;
 import com.wolflink289.bukkit.worldregions.WorldRegionsPlugin;
 import com.wolflink289.bukkit.worldregions.misc.BlockList;
 import com.wolflink289.bukkit.worldregions.misc.WGCommon;
 import com.wolflink289.bukkit.worldregions.util.RegionUtil;
 
 public class BlockListener implements Listener {
 	
 	/**
 	 * Listener for: BLOCKED-BREAK, ALLOWED-BREAK, INSTABREAK
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockDamage(BlockDamageEvent event) {
 		// BLOCKED-BREAK, ALLOWED-BREAK
 		if (!handleBreak(event.getPlayer(), event.getBlock())) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(WorldRegionsPlugin.getInstanceConfig().MSG_NO_BREAK);
 			return;
 		}
 		
 		// INSTABREAK
 		if (WorldRegionsPlugin.getInstanceConfig().ENABLE_INSTABREAK) {
 			// Isn't disabled?
 			if (!WGCommon.areRegionsDisabled(event.getPlayer().getWorld())) {
 				// Doesn't bypass?
 				if (WGCommon.willFlagApply(event.getPlayer(), WorldRegionsFlags.INSTABREAK)) {
 					// Is set
 					if (RegionUtil.getFlag(WorldRegionsFlags.INSTABREAK, event.getBlock().getLocation())) {
 						// Instant break
 						event.setInstaBreak(true);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Listener for: BLOCKED-BREAK, ALLOWED-BREAK
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (!handleBreak(event.getPlayer(), event.getBlock())) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(WorldRegionsPlugin.getInstanceConfig().MSG_NO_BREAK);
 		}
 	}
 	
 	/**
 	 * Listener for: BLOCKED-BREAK, ALLOWED-BREAK
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockDrain(PlayerBucketFillEvent event) {
 		if (!handleBreak(event.getPlayer(), event.getBlockClicked())) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(WorldRegionsPlugin.getInstanceConfig().MSG_NO_BREAK);
			event.getPlayer().sendBlockChange(event.getBlockClicked().getLocation(), event.getBlockClicked().getType(), event.getBlockClicked().getData());
 		}
 	}
 	
 	/**
 	 * Listener for: BLOCKED-PLACE, ALLOWED-PLACE
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockFill(PlayerBucketEmptyEvent event) {
		if (!handlePlace(event.getPlayer(), event.getBlockClicked().getWorld().getBlockAt(event.getBlockClicked().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ())), Material.WATER)) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(WorldRegionsPlugin.getInstanceConfig().MSG_NO_PLACE);
 		}
 	}
 	
 	/**
 	 * Listener for: BLOCKED-PLACE, ALLOWED-PLACE
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (!handlePlace(event.getPlayer(), event.getBlock())) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(WorldRegionsPlugin.getInstanceConfig().MSG_NO_PLACE);
 		}
 	}
 	
 	private boolean handleBreakAllowed(Player player, Block block) {
 		if (WorldRegionsPlugin.getInstanceConfig().ENABLE_ALLOWED_BREAK && WGCommon.willFlagApply(player, WorldRegionsFlags.ALLOWED_BREAK)) {
 			// Disabled?
 			if (WGCommon.areRegionsDisabled(player.getWorld())) return true;
 			
 			// Bypass?
 			if (!WGCommon.willFlagApply(player, WorldRegionsFlags.ALLOWED_BREAK)) return true;
 			
 			// Get blocked
 			Object blocked = RegionUtil.getFlag(WorldRegionsFlags.ALLOWED_BREAK, block.getLocation());
 			if (blocked == null) return true;
 			
 			// Check
 			BlockList list = (BlockList) blocked;
 			Material type = block.getType();
 			if (type == Material.STATIONARY_WATER) type = Material.WATER;
 			if (type == Material.STATIONARY_LAVA) type = Material.LAVA;
 			
 			if (list.contains(type)) { return true; }
 			
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private boolean handleBreakBlocked(Player player, Block block) {
 		if (WorldRegionsPlugin.getInstanceConfig().ENABLE_BLOCKED_BREAK && WGCommon.willFlagApply(player, WorldRegionsFlags.BLOCKED_BREAK)) {
 			// Disabled?
 			if (WGCommon.areRegionsDisabled(player.getWorld())) return true;
 			
 			// Bypass?
 			if (!WGCommon.willFlagApply(player, WorldRegionsFlags.BLOCKED_BREAK)) return true;
 			
 			// Get blocked
 			Object blocked = RegionUtil.getFlag(WorldRegionsFlags.BLOCKED_BREAK, block.getLocation());
 			if (blocked == null) return true;
 			
 			// Check
 			BlockList list = (BlockList) blocked;
 			Material type = block.getType();
 			if (type == Material.STATIONARY_WATER) type = Material.WATER;
 			if (type == Material.STATIONARY_LAVA) type = Material.LAVA;
 			
 			if (list.contains(type)) { return false; }
 			
 			return true;
 		}
 		
 		return true;
 	}
 	
	private boolean handlePlaceAllowed(Player player, Block block, Material type) {
 		if (WorldRegionsPlugin.getInstanceConfig().ENABLE_ALLOWED_PLACE && WGCommon.willFlagApply(player, WorldRegionsFlags.ALLOWED_PLACE)) {
 			// Disabled?
 			if (WGCommon.areRegionsDisabled(player.getWorld())) return true;
 			
 			// Bypass?
 			if (!WGCommon.willFlagApply(player, WorldRegionsFlags.ALLOWED_PLACE)) return true;
 			
 			// Get blocked
 			Object blocked = RegionUtil.getFlag(WorldRegionsFlags.ALLOWED_PLACE, block.getLocation());
 			if (blocked == null) return true;
 			
 			// Check
 			BlockList list = (BlockList) blocked;
 			if (type == Material.STATIONARY_WATER) type = Material.WATER;
 			if (type == Material.STATIONARY_LAVA) type = Material.LAVA;
 			
 			if (list.contains(type)) { return true; }
 			
 			return false;
 		}
 		
 		return true;
 	}
 	
	private boolean handlePlaceBlocked(Player player, Block block, Material type) {
 		if (WorldRegionsPlugin.getInstanceConfig().ENABLE_BLOCKED_PLACE && WGCommon.willFlagApply(player, WorldRegionsFlags.BLOCKED_PLACE)) {
 			// Disabled?
 			if (WGCommon.areRegionsDisabled(player.getWorld())) return true;
 			
 			// Bypass?
 			if (!WGCommon.willFlagApply(player, WorldRegionsFlags.BLOCKED_PLACE)) return true;
 			
 			// Get blocked
 			Object blocked = RegionUtil.getFlag(WorldRegionsFlags.BLOCKED_PLACE, block.getLocation());
 			if (blocked == null) return true;
 			
 			// Check
 			BlockList list = (BlockList) blocked;
 			if (type == Material.STATIONARY_WATER) type = Material.WATER;
 			if (type == Material.STATIONARY_LAVA) type = Material.LAVA;
 			
 			if (list.contains(type)) { return false; }
 			
 			return true;
 		}
 		
 		return true;
 	}
 	
 	private boolean handleBreak(Player player, Block block) {
 		// Enabled?
 		if (!WorldRegionsPlugin.getInstanceConfig().ENABLE_ALLOWED_BREAK && !WorldRegionsPlugin.getInstanceConfig().ENABLE_BLOCKED_BREAK) return true;
 		
 		// ALLOWED-BREAK
 		if (!handleBreakAllowed(player, block)) return false;
 		
 		// BLOCKED-BREAK
 		if (!handleBreakBlocked(player, block)) return false;
 		
 		// ...
 		return true;
 	}
 	
 	private boolean handlePlace(Player player, Block block) {
		return handlePlace(player, block, block.getType());
	}
	
	private boolean handlePlace(Player player, Block block, Material type) {
 		// Enabled?
 		if (!WorldRegionsPlugin.getInstanceConfig().ENABLE_ALLOWED_PLACE && !WorldRegionsPlugin.getInstanceConfig().ENABLE_BLOCKED_PLACE) return true;
 		
 		// ALLOWED-PLACE
		if (!handlePlaceAllowed(player, block, type)) return false;
 		
 		// BLOCKED-PLACE
		if (!handlePlaceBlocked(player, block, type)) return false;
 		
 		// ...
 		return true;
 	}
 }
