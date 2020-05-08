 package me.arno.blocklog.listeners;
 
 import me.arno.blocklog.logs.BlockEntry;
 import me.arno.blocklog.logs.LogType;
 
 import org.bukkit.TreeType;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.world.StructureGrowEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 
 public class WorldListener extends BlockLogListener {
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onStructureGrow(StructureGrowEvent event) {
 		LogType type = LogType.TREE_GROW;
 		if(event.getSpecies() == TreeType.RED_MUSHROOM || event.getSpecies() == TreeType.BROWN_MUSHROOM)
 			type = LogType.MUSHROOM_GROW;
 		
 		if(!getSettingsManager().isLoggingEnabled(event.getWorld(), type))
 			return;
 		
 		for(BlockState block : event.getBlocks()) {
			getQueueManager().queueBlock(new BlockEntry(event.getPlayer().getName(), EntityType.PLAYER, type, block));
 		}
 	}
 	
 	@EventHandler
 	public void onWorldSave(WorldSaveEvent event) {
 		if(getSettingsManager().saveOnWorldSave()) {
 			plugin.saveLogs(0);
 		}
 	}
 }
