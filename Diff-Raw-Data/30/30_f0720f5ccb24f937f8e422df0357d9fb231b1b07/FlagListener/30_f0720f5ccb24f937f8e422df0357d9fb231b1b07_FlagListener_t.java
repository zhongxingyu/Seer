 package com.censoredsoftware.demigods.engine.listener;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.structure.StructureData;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.google.common.collect.Lists;
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
 
 public class FlagListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		if(event.isCancelled() || Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		if(Structure.Util.partOfStructureWithFlag(event.getBlock().getLocation(), Structure.Flag.PROTECTED_BLOCKS, Structure.Flag.DESTRUCT_ON_BREAK))
 		{
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.YELLOW + Demigods.LANGUAGE.getText(Translation.Text.PROTECTED_BLOCK));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	private void onBlockBreak(BlockBreakEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(event.isCancelled() || Zones.inNoDemigodsZone(location)) return;
 		if(Structure.Util.partOfStructureWithFlag(location, Structure.Flag.PROTECTED_BLOCKS, Structure.Flag.DESTRUCT_ON_BREAK))
 		{
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.YELLOW + Demigods.LANGUAGE.getText(Translation.Text.PROTECTED_BLOCK));
 			return;
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		if(event.isCancelled() || Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		if(Structure.Util.partOfStructureWithFlag(event.getBlock().getLocation(), Structure.Flag.PROTECTED_BLOCKS, Structure.Flag.DESTRUCT_ON_BREAK)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockDamage(BlockDamageEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(event.isCancelled() || Zones.inNoDemigodsZone(location)) return;
 		if(Structure.Util.partOfStructureWithFlag(location, Structure.Flag.PROTECTED_BLOCKS))
 		{
 			event.setCancelled(true);
 			return;
 		}
		if(DPlayer.Util.getPlayer(event.getPlayer()).getCurrent() != null && Structure.Util.partOfStructureWithFlag(location, Structure.Flag.DESTRUCT_ON_BREAK))
 		{
 			StructureData structureData = Structure.Util.getStructureRegional(location);
 			structureData.damageBy(DPlayer.Util.getPlayer(event.getPlayer()).getCurrent());
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPistonExtend(BlockPistonExtendEvent event)
 	{
 		if(event.isCancelled() || Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		for(Block block : event.getBlocks())
 		{
 			if(Structure.Util.partOfStructureWithFlag(block.getLocation(), Structure.Flag.PROTECTED_BLOCKS, Structure.Flag.DESTRUCT_ON_BREAK))
 			{
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPistonRetract(BlockPistonRetractEvent event)
 	{
 		if(event.isCancelled() || Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		if(Structure.Util.partOfStructureWithFlag(event.getBlock().getRelative(event.getDirection(), 2).getLocation(), Structure.Flag.PROTECTED_BLOCKS, Structure.Flag.DESTRUCT_ON_BREAK) && event.isSticky()) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(final EntityExplodeEvent event)
 	{
 		if(event.getEntity() == null || Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
 		final List<StructureData> saves = Lists.newArrayList(Structure.Util.getInRadiusWithFlag(event.getLocation(), Structure.Flag.PROTECTED_BLOCKS, Structure.Flag.DESTRUCT_ON_BREAK));
 
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// Remove all drops from explosion zone
 				for(final StructureData save : saves)
 					for(Item drop : event.getLocation().getWorld().getEntitiesByClass(Item.class))
 						if(drop.getLocation().distance(save.getReferenceLocation()) <= save.getType().getRadius()) drop.remove();
 			}
 		}, 1);
 
 		if(DataManager.hasTimed("explode", "structure")) return;
 		DataManager.saveTimed("explode", "structure", true, 2);
 
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				for(final StructureData save : saves)
 					save.generate();
 			}
 		}, 30);
 	}
 }
