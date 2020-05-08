 package com.comphenix.xp.listeners;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.lang.NullArgumentException;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.comphenix.xp.Debugger;
 import com.comphenix.xp.lookup.ItemQuery;
 
 /**
  * Keeps track of a player's last block interaction.
  * 
  * @author Kristian
  */
 public class PlayerInteractionListener implements PlayerCleanupListener, Listener {
 
 	// Last clicked block
 	private Map<String, ClickEvent> lastRightClicked = new ConcurrentHashMap<String, ClickEvent>();
 	
 	// For debugging purposes
 	private ErrorReporting report = ErrorReporting.DEFAULT;
 	private Debugger debugger;
 	
 	public PlayerInteractionListener(Debugger debugger) {
 		this.debugger = debugger;
 	}
 
 	// Last clicked event
 	private class ClickEvent {
 		// public org.bukkit.event.block.Action ...
 		public long time;
 		public ItemQuery block;
 	}
 	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerInteractEvent(PlayerInteractEvent event) {
 		
 		try {
 			// Reset the potion markers
 			Player player = event.getPlayer();
 			
 			// Make sure this is a valid block right-click event
 			if (player != null && event.hasBlock() && 
 					event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
 			
 				// Store relevant information
 				ClickEvent click = new ClickEvent();
 				click.block = ItemQuery.fromExact(event.getClickedBlock());
 				click.time = System.currentTimeMillis();
 				
 				// Store this block (by copy, so we don't keep chunks in memory)
 				lastRightClicked.put(player.getName(), click);
 			}
 			
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onInventoryCloseEvent(InventoryCloseEvent event) {
 	
 		try {
 		
 			HumanEntity player = event.getPlayer();
 			
 			// Make sure this is a valid inventory open event
 			if (player != null && player instanceof Player) {
 				// This information is now outdated
 				lastRightClicked.remove(player.getName());
 			}
 		
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	/**
 	 * Retrieves the given player's most recent right click event.
 	 * @param player - the player whose interaction we're looking for.
 	 * @param maxAge - the maximum age (in milliseconds) of the action. NULL indicates infinity.
 	 * @return The most recent action within the given limit, or NULL if no such action can be found.
 	 */
 	public ItemQuery getLastRightClick(Player player, Integer maxAge) {
 		if (player == null)
 			throw new NullArgumentException("player");
 		
 		return getLastRightClick(player, maxAge, System.currentTimeMillis());
 	}
 	
 	/**
 	 * Retrieves the given player's most recent right click event.
 	 * @param player - the player whose interaction we're looking for.
 	 * @param maxAge - the maximum age (in milliseconds) of the action. NULL indicates infinity.
 	 * @param currentTime - the current time in milliseconds since midnight, January 1, 1970.
 	 * @return The most recent action within the given limit, or NULL if no such action can be found.
 	 */
 	public ItemQuery getLastRightClick(Player player, Integer maxAge, long currentTime) {
 		if (player == null)
 			throw new NullArgumentException("player");
 		
 		ClickEvent last = lastRightClicked.get(player.getName());
 		
 		// Make sure we're not outside the age limit
 		if (last != null && (
 				 maxAge == null ||
 				 last.time + maxAge > currentTime
 		   )) {
 			
 			return last.block;
 		}
 	
 		// No action found
 		return null;
 	}
 
 	/**
 	 * Determines if a last right click event has been recorded for a given player.
 	 * @param player - the given player.
 	 * @return TRUE if a right click event has been recorded, FALSE otherwise.
 	 */
 	public boolean hasLastRightClick(Player player) {
 		return lastRightClicked.containsKey(player.getName());
 	}
 	
 	@Override
 	public void removePlayerCache(Player player) {
 		String name = player.getName();
 		
 		// Cleanup
 		lastRightClicked.remove(name);
 	}
 }
