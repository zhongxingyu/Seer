 package com.bradsproject.BradleyJewell.bZone;
 
 import org.bukkit.Location;
//import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class bZoneBlockListener extends BlockListener
 {
 	private final bZone plugin;
 	
 	public bZoneBlockListener(bZone instance)
 	{
 		plugin = instance;
 	}
 	
 	@Override
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		Player player = event.getPlayer();
 		
 		event.setCancelled(plugin.cancelAction(location, player, "build"));
 	}
 	
 	@Override
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		Player player = event.getPlayer();
 		
		/*
 		if(event.getBlock().getType() == Material.BEDROCK)
 		{
 			event.setCancelled(true);
 			event.getPlayer().getInventory().remove(Material.BEDROCK);
 			return;
 		}
 		else if(event.getBlock().getType() == Material.TNT)
 		{
 			if((plugin.cancelAction(location, player, "build") || plugin.cancelAction(location, player, "explosion")))
 			{
 				event.getPlayer().getInventory().remove(Material.TNT);
 				event.setCancelled(true);
 			}
 		}
 		else
 		{
		*/
 			event.setCancelled(plugin.cancelAction(location, player, "build"));
		//}
 	}
 	
 	@Override
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		Player player = event.getPlayer();
 		
 		event.setCancelled(plugin.cancelAction(location, player, "fire"));
 	}
 	
 	@Override
 	public void onBlockBurn(BlockBurnEvent event)
 	{
 		Location location = event.getBlock().getLocation();
 		
 		event.setCancelled(plugin.cancelAction(location, "fire"));
 	}
 }
