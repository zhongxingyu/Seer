 package com.censoredsoftware.Demigods.Engine.Listener;
 
import com.censoredsoftware.Demigods.Engine.Demigods;
import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureInfo;
import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
import com.censoredsoftware.Demigods.Engine.Utility.StructureUtility;
import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
import java.util.List;
 
 public class StructureListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.partOfStructureWithFlag(location, StructureInfo.Flag.PROTECTED_BLOCKS))
 		{
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.YELLOW + Demigods.text.getText(TextUtility.Text.PROTECTED_BLOCK));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.partOfStructureWithFlag(location, StructureInfo.Flag.PROTECTED_BLOCKS)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockDamage(BlockDamageEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.partOfStructureWithFlag(location, StructureInfo.Flag.PROTECTED_BLOCKS)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPistonExtend(BlockPistonExtendEvent event)
 	{
 		List<Block> blocks = event.getBlocks();
 
 		for(Block block : blocks)
 		{
 			Location location = block.getLocation();
 
 			if(StructureUtility.partOfStructureWithFlag(location, StructureInfo.Flag.PROTECTED_BLOCKS))
 			{
 				event.setCancelled(true);
 				break;
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPistonRetract(BlockPistonRetractEvent event)
 	{
 		final Block block = event.getBlock().getRelative(event.getDirection(), 2);
 
 		if(StructureUtility.partOfStructureWithFlag(block.getLocation(), StructureInfo.Flag.PROTECTED_BLOCKS) && event.isSticky())
 		{
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(final EntityExplodeEvent event)
 	{
 		final Location location = event.getLocation();
 		if(ZoneUtility.zoneProtectedStructure(location) == null) return;
 
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// Remove all drops from explosion zone
 				for(Item drop : event.getLocation().getWorld().getEntitiesByClass(Item.class))
 				{
 					Location dropLocation = drop.getLocation();
 					if(ZoneUtility.zoneAltar(dropLocation) != null)
 					{
 						drop.remove();
 						continue;
 					}
 					if(ZoneUtility.zoneShrine(dropLocation) != null) drop.remove();
 				}
 			}
 		}, 1);
 
 		if(DataUtility.hasTimed("explode", "structure")) return;
 		DataUtility.saveTimed("explode", "structure", true, 3);
 
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if(ZoneUtility.zoneProtectedStructure(location) != null) ZoneUtility.zoneProtectedStructure(location).generate();
 			}
 		}, 30);
 	}
 }
