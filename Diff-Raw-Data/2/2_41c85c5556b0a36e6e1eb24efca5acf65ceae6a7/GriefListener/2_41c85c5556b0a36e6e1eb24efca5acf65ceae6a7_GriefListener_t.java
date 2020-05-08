 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureSave;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.StructureUtility;
 
 public class GriefListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE))
 		{
 			StructureSave save = StructureUtility.getInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE);
 			if(PlayerWrapper.isImmortal(event.getPlayer()) && save.getStructureInfo().getFlags().contains(StructureInfo.Flag.HAS_OWNER) && save.getOwner() != null && PlayerWrapper.getPlayer(event.getPlayer()).getCurrent().getName().equals(save.getOwner().getName())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE))
 		{
 			StructureSave save = StructureUtility.getInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE);
 			if(PlayerWrapper.isImmortal(event.getPlayer()) && save.getStructureInfo().getFlags().contains(StructureInfo.Flag.HAS_OWNER) && save.getOwner() != null && PlayerWrapper.getPlayer(event.getPlayer()).getCurrent().getName().equals(save.getOwner().getName())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE))
 		{
 			StructureSave save = StructureUtility.getInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE);
 			if(PlayerWrapper.isImmortal(event.getPlayer()) && save.getStructureInfo().getFlags().contains(StructureInfo.Flag.HAS_OWNER) && save.getOwner() != null && PlayerWrapper.getPlayer(event.getPlayer()).getCurrent().getName().equals(save.getOwner().getName())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBurn(BlockBurnEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockFall(EntityChangeBlockEvent event)
 	{
 		if(event.getEntityType() != EntityType.FALLING_BLOCK) return;
 		FallingBlock block = (FallingBlock) event.getEntity();
 		if(StructureUtility.isInRadiusWithFlag(MiscUtility.getFloorBelowLocation(block.getLocation()), StructureInfo.Flag.NO_GRIEFING_ZONE))
 		{
 			// Break the block
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
 			block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getMaterial()));
 			block.remove();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPistonExtend(BlockPistonExtendEvent event)
 	{
 		boolean in = false;
 		boolean out = false;
 		for(Block block : event.getBlocks())
 		{
 			if(StructureUtility.isInRadiusWithFlag(block.getLocation(), StructureInfo.Flag.NO_GRIEFING_ZONE)) in = true;
 			else out = true;
 		}
 		event.setCancelled(in && out);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPistonRetract(BlockPistonRetractEvent event)
 	{
 		boolean block = StructureUtility.isInRadiusWithFlag(event.getBlock().getLocation(), StructureInfo.Flag.NO_GRIEFING_ZONE);
 		boolean retract = StructureUtility.isInRadiusWithFlag(event.getRetractLocation(), StructureInfo.Flag.NO_GRIEFING_ZONE);
 		event.setCancelled(block != retract);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockDamage(BlockDamageEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(StructureUtility.isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE))
 		{
 			StructureSave save = StructureUtility.getInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE);
 			if(PlayerWrapper.isImmortal(event.getPlayer()) && save.getStructureInfo().getFlags().contains(StructureInfo.Flag.HAS_OWNER) && save.getOwner() != null && PlayerWrapper.getPlayer(event.getPlayer()).getCurrent().getId().equals(save.getOwner().getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(final EntityExplodeEvent event)
 	{
 		event.setCancelled(StructureUtility.isInRadiusWithFlag(event.getLocation(), StructureInfo.Flag.NO_GRIEFING_ZONE));
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onAttemptInventoryOpen(PlayerInteractEvent event) // TODO Fix horse inventories.
 	{
 		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
 		Block block = event.getClickedBlock();
 		Location location = block.getLocation();
 		if(!StructureUtility.isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE)) return;
 		if(block.getType().equals(Material.CHEST) || block.getType().equals(Material.ENDER_CHEST) || block.getType().equals(Material.FURNACE) || block.getType().equals(Material.BURNING_FURNACE) || block.getType().equals(Material.DISPENSER) || block.getType().equals(Material.DROPPER) || block.getType().equals(Material.BREWING_STAND) || block.getType().equals(Material.BEACON) || block.getType().equals(Material.HOPPER) || block.getType().equals(Material.HOPPER_MINECART) || block.getType().equals(Material.STORAGE_MINECART))
 		{
 			StructureSave save = StructureUtility.getInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE);
 			if(PlayerWrapper.isImmortal(event.getPlayer()) && save.getStructureInfo().getFlags().contains(StructureInfo.Flag.HAS_OWNER) && save.getOwner() != null && PlayerWrapper.getPlayer((Player) event.getPlayer()).getCurrent().getName().equals(save.getOwner().getName())) return;
 			event.setCancelled(true);
 		}
 	}
 }
