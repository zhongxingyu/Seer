 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import java.util.List;
 
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.DemigodsText;
 import com.censoredsoftware.Demigods.Engine.Event.Altar.AltarCreateEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Altar.AltarCreateEvent.AltarCreateCause;
 import com.censoredsoftware.Demigods.Engine.Event.Altar.AltarRemoveEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Shrine.ShrineCreateEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Shrine.ShrineRemoveEvent;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Structure.Altar;
 import com.censoredsoftware.Demigods.Engine.Structure.Shrine;
 import com.censoredsoftware.Demigods.Engine.Structure.StructureFactory;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBlock;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedLocation;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.AdminUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 
 public class BlockListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(TrackedBlock.isProtected(location))
 		{
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.YELLOW + Demigods.text.getText(DemigodsText.Text.PROTECTED_BLOCK));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(TrackedBlock.isProtected(location)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockDamage(BlockDamageEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		if(TrackedBlock.isProtected(location)) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPistonExtend(BlockPistonExtendEvent event)
 	{
 		List<Block> blocks = event.getBlocks();
 
 		for(Block block : blocks)
 		{
 			Location location = block.getLocation();
 
 			if(TrackedBlock.isProtected(location))
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
 
 		if(TrackedBlock.isProtected(block.getLocation()) && event.isSticky())
 		{
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(final EntityExplodeEvent event)
 	{
 		final Location location = event.getLocation();
 		if(ZoneUtility.zoneAltar(location) == null && ZoneUtility.zoneShrine(location) == null) return;
 
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
 
 		if(DemigodsData.hasTimed("explode", "structure")) return;
 		DemigodsData.saveTimed("explode", "structure", true, 3);
 
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if(ZoneUtility.zoneAltar(location) != null) Altar.generate(ZoneUtility.zoneAltar(location), ZoneUtility.zoneAltar(location).getLocation());
 				if(ZoneUtility.zoneShrine(location) != null) Shrine.generate(ZoneUtility.zoneShrine(location), ZoneUtility.zoneShrine(location).getLocation());
 			}
 		}, 30);
 	}
 
 	/*
 	 * @EventHandler(priority = EventPriority.HIGHEST)
 	 * public void stopDestroyEnderCrystal(EntityDamageEvent event)
 	 * {
 	 * try
 	 * {
 	 * if(TrackedBlock.isProtected(event.getEntity().getLocation().subtract(0.5, 1.0, 0.5)))
 	 * {
 	 * event.setDamage(0);
 	 * event.setCancelled(true);
 	 * }
 	 * }
 	 * catch(Exception ignored)
 	 * {}
 	 * }
 	 */
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void demigodsAdminWand(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null) return;
 
 		// Define variables
 		Block clickedBlock = event.getClickedBlock();
 		Location location = clickedBlock.getLocation();
 		Player player = event.getPlayer();
 
 		/**
 		 * Handle Altars
 		 */
 		if(AdminUtility.useWand(player) && clickedBlock.getType().equals(Material.EMERALD_BLOCK))
 		{
 			event.setCancelled(true);
 
 			AltarCreateEvent altarEvent = new AltarCreateEvent(location, AltarCreateCause.ADMIN_WAND);
 			Bukkit.getServer().getPluginManager().callEvent(altarEvent);
 
 			player.sendMessage(ChatColor.GRAY + Demigods.text.getText(DemigodsText.Text.ADMIN_WAND_GENERATE_ALTAR));
 			StructureFactory.createAltar(location);
 			player.sendMessage(ChatColor.GREEN + Demigods.text.getText(DemigodsText.Text.ADMIN_WAND_GENERATE_ALTAR_COMPLETE));
 		}
 
 		if(AdminUtility.useWand(player) && Altar.isAltar(location))
 		{
 			event.setCancelled(true);
 
 			Altar altar = Altar.getAltar(location);
 
 			if(DemigodsData.hasTimed(player.getName(), "destroy_altar"))
 			{
 				AltarRemoveEvent altarRemoveEvent = new AltarRemoveEvent(location, AltarRemoveEvent.AltarRemoveCause.ADMIN_WAND);
 				Bukkit.getServer().getPluginManager().callEvent(altarRemoveEvent);
 				if(altarRemoveEvent.isCancelled()) return;
 
 				// Remove the Altar
 				altar.remove();
 				DemigodsData.removeTimed(player.getName(), "destroy_altar");
 
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(DemigodsText.Text.ADMIN_WAND_REMOVE_ALTAR_COMPLETE));
 			}
 			else
 			{
 				DemigodsData.saveTimed(player.getName(), "destroy_altar", true, 5);
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(DemigodsText.Text.ADMIN_WAND_REMOVE_ALTAR));
 			}
 		}
 
 		/**
 		 * Handle Shrines
 		 */
 		if(TrackedPlayer.isImmortal(player))
 		{
 			PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && character.getDeity().getInfo().getClaimItems().contains(event.getPlayer().getItemInHand().getType()) && Shrine.validBlockConfiguration(event.getClickedBlock()))
 			{
 				try
 				{
 					// Shrine created!
 					ShrineCreateEvent shrineCreateEvent = new ShrineCreateEvent(character, location);
 					Bukkit.getServer().getPluginManager().callEvent(shrineCreateEvent);
 					if(shrineCreateEvent.isCancelled()) return;
 
 					StructureFactory.createShrine(character, location);
 					location.getWorld().strikeLightningEffect(location);
 
					player.sendMessage(ChatColor.GRAY + Demigods.text.getText(DemigodsText.Text.CREATE_SHRINE_1).replace("{alliance}", "" + ChatColor.YELLOW + character.getAlliance() + "s" + ChatColor.GRAY));
					player.sendMessage(ChatColor.GRAY + Demigods.text.getText(DemigodsText.Text.CREATE_SHRINE_2).replace("{deity}", "" + ChatColor.YELLOW + character.getDeity().getInfo().getName() + ChatColor.GRAY));
 				}
 				catch(Exception e)
 				{
 					// Creation of shrine failed...
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if(AdminUtility.useWand(player) && Shrine.isShrine(location))
 		{
 			event.setCancelled(true);
 
 			Shrine shrine = Shrine.getShrine(location);
 
 			if(DemigodsData.hasTimed(player.getName(), "destroy_shrine"))
 			{
 				ShrineRemoveEvent shrineRemoveEvent = new ShrineRemoveEvent(shrine.getCharacter(), location);
 				Bukkit.getServer().getPluginManager().callEvent(shrineRemoveEvent);
 				if(shrineRemoveEvent.isCancelled()) return;
 
 				// Remove the Shrine
 				shrine.remove();
 				DemigodsData.removeTimed(player.getName(), "destroy_shrine");
 
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(DemigodsText.Text.ADMIN_WAND_REMOVE_SHRINE_COMPLETE));
 			}
 			else
 			{
 				DemigodsData.saveTimed(player.getName(), "destroy_shrine", true, 5);
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(DemigodsText.Text.ADMIN_WAND_REMOVE_SHRINE));
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void divineBlockAlerts(PlayerMoveEvent event)
 	{
 		if(event.getFrom().distance(event.getTo()) < 0.1) return;
 
 		// Define variables
 		Player player = event.getPlayer();
 		Location to = event.getTo();
 		Location from = event.getFrom();
 
 		/**
 		 * Entering Altar
 		 */
 		if(ZoneUtility.enterZoneAltar(to, from) && !TrackedLocation.hasWarp(ZoneUtility.zoneAltar(to), TrackedPlayer.getTracked(player).getCurrent())) // TODO This is an annoying message.
 		{
 			// player.sendMessage(ChatColor.GRAY + Demigods.text.getText(DemigodsText.Text.NO_WARP_ALTAR));
 		}
 	}
 }
