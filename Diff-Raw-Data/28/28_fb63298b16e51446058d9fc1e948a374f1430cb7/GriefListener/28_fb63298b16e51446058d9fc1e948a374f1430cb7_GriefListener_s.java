 package com.censoredsoftware.demigods.listener;
 
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.censoredsoftware.demigods.util.Messages;
 import com.censoredsoftware.demigods.util.Zones;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityInteractEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 public class GriefListener implements Listener
 {
 	private final static Set<Material> blockInventories = new HashSet<Material>()
 	{
 		{
 			add(Material.CHEST);
 			add(Material.ENDER_CHEST);
 			add(Material.FURNACE);
 			add(Material.BURNING_FURNACE);
 			add(Material.DISPENSER);
 			add(Material.DROPPER);
 			add(Material.BREWING_STAND);
 			add(Material.BEACON);
 			add(Material.HOPPER);
 			add(Material.HOPPER_MINECART);
 			add(Material.STORAGE_MINECART);
 		}
 	};
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		Structure save = Structure.Util.getInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save != null && save.hasMembers())
 		{
 			DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		Structure save = Structure.Util.getInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save != null && save.hasMembers())
 		{
 			DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		if(event.getPlayer() == null)
 		{
 			event.setCancelled(true);
 			return;
 		}
 		Structure save = Structure.Util.getInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save != null && save.hasMembers())
 		{
 			DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBurn(BlockBurnEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		if(Structure.Util.isInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockFall(EntityChangeBlockEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		if(event.getEntityType() != EntityType.FALLING_BLOCK || event.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) return;
 		FallingBlock block = (FallingBlock) event.getEntity();
 		Location blockLocation = block.getLocation();
 		if(Structure.Util.isInRadiusWithFlag(DLocation.Util.getFloorBelowLocation(block.getLocation()), Structure.Flag.NO_GRIEFING))
 		{
 			// Break the block
 			event.setCancelled(true);
 			event.getBlock().setType(Material.AIR);
 			blockLocation.getWorld().dropItemNaturally(blockLocation, new ItemStack(block.getMaterial()));
 			block.remove();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPistonExtend(BlockPistonExtendEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		boolean piston = Structure.Util.isInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		boolean blocks = Iterables.any(event.getBlocks(), piston ? new Predicate<Block>()
 		{
 			@Override
 			public boolean apply(Block block)
 			{
 				return !Structure.Util.isInRadiusWithFlag(block.getLocation(), Structure.Flag.NO_GRIEFING);
 			}
 		} : new Predicate<Block>()
 		{
 			@Override
 			public boolean apply(Block block)
 			{
 				return Structure.Util.isInRadiusWithFlag(block.getLocation(), Structure.Flag.NO_GRIEFING);
 			}
 		});
 		if(piston == (piston ? blocks : !blocks)) event.setCancelled(true); // Am I overthinking this?
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPistonRetract(BlockPistonRetractEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		boolean block = Structure.Util.isInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		boolean retract = Structure.Util.isInRadiusWithFlag(event.getRetractLocation(), Structure.Flag.NO_GRIEFING);
 		if(block != retract) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockDamage(BlockDamageEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getBlock().getLocation())) return;
 		Structure save = Structure.Util.getInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save != null && save.hasMembers())
 		{
 			DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityInteract(final EntityInteractEvent event)
 	{
		if(Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
		if(!event.getEntityType().equals(EntityType.PLAYER))
		{
			event.setCancelled(true);
			return;
		}
		Structure save = Structure.Util.getInRadiusWithFlag(event.getBlock().getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save != null && save.hasMembers())
 		{
			DCharacter character = DPlayer.Util.getPlayer((Player) event.getEntity()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(EntityExplodeEvent event)
 	{
 		try
 		{
 			if(Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
 			if(Iterables.any(event.blockList(), new Predicate<Block>()
 			{
 				@Override
 				public boolean apply(Block block)
 				{
 					return Structure.Util.isInRadiusWithFlag(block.getLocation(), Structure.Flag.NO_GRIEFING);
 				}
 			})) event.setCancelled(true);
 		}
 		catch(Exception e)
 		{
 			Messages.warning("Error on entity explode, grief listener.");
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onAttemptInteractEntity(PlayerInteractEntityEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;
 		Entity entity = event.getRightClicked();
 		Structure save = Structure.Util.getInRadiusWithFlag(entity.getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save != null && save.hasMembers())
 		{
 			DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onAttemptInventoryOpen(PlayerInteractEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;
 		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
 		Block block = event.getClickedBlock();
 		Structure save = Structure.Util.getInRadiusWithFlag(block.getLocation(), Structure.Flag.NO_GRIEFING);
 		if(save == null || !save.hasMembers()) return;
 		if(blockInventories.contains(block.getType()))
 		{
 			DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 			Collection<UUID> members = save.getMembers();
 			if(character != null && members.contains(character.getId())) return;
 			event.setCancelled(true);
 		}
 	}
 }
