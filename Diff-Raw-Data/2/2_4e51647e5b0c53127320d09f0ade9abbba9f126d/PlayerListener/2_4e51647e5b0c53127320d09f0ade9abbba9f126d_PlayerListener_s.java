 package me.arno.blocklog.listeners;
 
 import java.util.Arrays;
 
 import me.arno.blocklog.logs.DataEntry;
 import me.arno.blocklog.logs.InteractionEntry;
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.util.Util;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.InventoryHolder;
 
 public class PlayerListener extends BlockLogListener {
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		
 		String message = Util.escape(event.getMessage());
 		
 		if(message.length() > 255) {
 			message.substring(message.length() - 255 - 3);
 			message += "...";
 		}
 		
 		
 		if(getSettingsManager().isLoggingEnabled(player.getWorld(), LogType.PLAYER_CHAT))
 			getQueueManager().queueData(new DataEntry(player.getName(), LogType.PLAYER_CHAT, player.getLocation(), message));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		Player player = event.getPlayer();
 		
		String message = event.getMessage();
 		
 		if(message.length() > 255) {
 			message.substring(message.length() - 255 - 3);
 			message += "...";
 		}
 		
 		if(getSettingsManager().isLoggingEnabled(player.getWorld(), LogType.PLAYER_COMMAND))
 			getQueueManager().queueData(new DataEntry(player.getName(), LogType.PLAYER_COMMAND, player.getLocation(), message));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		getQueueManager().queueData(new DataEntry(event.getPlayer().getName(), LogType.PLAYER_TELEPORT, event.getTo(), event.getCause().toString()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		getQueueManager().queueData(new DataEntry(player.getName(), LogType.PLAYER_LOGIN, player.getLocation(), null));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		getQueueManager().queueData(new DataEntry(player.getName(), LogType.PLAYER_LOGOUT, player.getLocation(), null));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR)
 			return;
 		
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		
 		if(block instanceof InventoryHolder)
 			return;
 		
 		Material[] allowedMaterials = new Material[]{Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.STONE_BUTTON, Material.FENCE_GATE, Material.ENCHANTMENT_TABLE, Material.BREWING_STAND, Material.BURNING_FURNACE, Material.FURNACE, Material.WORKBENCH, Material.CAULDRON, Material.BED_BLOCK, Material.CAKE_BLOCK};
 		
 
 		if(Arrays.asList(allowedMaterials).contains(block.getType())) {
 			getQueueManager().queueInteraction(new InteractionEntry(player.getName(), block.getLocation(), block.getType().getId()));
 		}
 	}
 }
