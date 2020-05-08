 package me.arno.blocklog.listeners;
 
 import me.arno.blocklog.logs.BlockEntry;
 import me.arno.blocklog.logs.LogType;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 
 public class BlockListener extends BlockLogListener {
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		
 		if(getSettingsManager().isLoggingEnabled(player.getWorld(), LogType.BLOCK_PLACE))
			getQueueManager().queueBlock(new BlockEntry(player.getName(), EntityType.PLAYER, LogType.BLOCK_PLACE, event.getBlock().getState()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		
 		if(getSettingsManager().isLoggingEnabled(player.getWorld(), LogType.BLOCK_BREAK))
 			getQueueManager().queueBlock(new BlockEntry(player.getName(), EntityType.PLAYER, LogType.BLOCK_BREAK, event.getBlock().getState()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockForm(BlockFormEvent event) {
 		if(getSettingsManager().isLoggingEnabled(event.getNewState().getWorld(), LogType.BLOCK_FORM))
 			getQueueManager().queueBlock(new BlockEntry(LogType.BLOCK_FORM, event.getNewState(), event.getBlock().getState()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockSpread(BlockSpreadEvent event) {
 		if(getSettingsManager().isLoggingEnabled(event.getNewState().getWorld(), LogType.BLOCK_SPREAD))
 			getQueueManager().queueBlock(new BlockEntry(LogType.BLOCK_SPREAD, event.getNewState(), event.getBlock().getState()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockFade(BlockFadeEvent event) {
 		if(getSettingsManager().isLoggingEnabled(event.getNewState().getWorld(), LogType.BLOCK_FADE))
 			getQueueManager().queueBlock(new BlockEntry(LogType.BLOCK_FADE, event.getNewState(), event.getBlock().getState()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockBurn(BlockBurnEvent event) {
 		if(getSettingsManager().isLoggingEnabled(event.getBlock().getWorld(), LogType.BLOCK_BURN))
 			getQueueManager().queueBlock(new BlockEntry(LogType.BLOCK_BURN, event.getBlock().getState()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onLeavesDecay(LeavesDecayEvent event) {
 		if(getSettingsManager().isLoggingEnabled(event.getBlock().getWorld(), LogType.LEAVES_DECAY))
 			getQueueManager().queueBlock(new BlockEntry(LogType.LEAVES_DECAY, event.getBlock().getState()));
 	}
 }
