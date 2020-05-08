 package com.github.ephemeralis.chunkbreedlimit;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class BreederListener implements Listener {
 	
 	//private ChunkBreedLimit basePlugin;
 	private int spawnCap;
 	private List<EntityType> allowedEntities;
	private String breedingFailMessage;
 	
	public BreederListener(ChunkBreedLimit plugin, int cap, List<EntityType> allowedEnts, String bmsg)
 	{
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		spawnCap = cap;
 		allowedEntities = allowedEnts;
		breedingFailMessage = bmsg;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onCreatureSpawnEvent(CreatureSpawnEvent event)
 	{
 		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING 
 				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)
 		{
 			Entity[] entitiesInEventChunk = event.getLocation().getChunk().getEntities();
 			
 			int entcount = 0;
 			for (Entity ent : entitiesInEventChunk)
 			{
 				//we have entity list, so iterate through it
 				//to determine just how many of the same type
 				//we have on the same chunk
 				if (allowedEntities.contains(ent.getType()))
 				{
 					//same type detected, so increase
 					entcount++;
 				}
 			}
 			
 			if (entcount >= spawnCap) //replace this with configuratio0n reader
 			{
 				event.setCancelled(true); //must be a better way to can the event
 			}
 		}
 	
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
 	{
 		Player p = event.getPlayer();
 		
 		if (p.getItemInHand().getType() == Material.WHEAT 
 				&& allowedEntities.contains(event.getRightClicked().getType()))
 		{
 			int entcount = 0;
 			//player is holding wheat and the entity is allowed, check
 			for (Entity ent : event.getRightClicked().getLocation().getChunk().getEntities())
 			{
				if (allowedEntities.contains(ent.getType()))
 					entcount++;
 			}
 			
 			if (entcount >= spawnCap)
 			{
				p.sendMessage(ChatColor.RED + breedingFailMessage);
 				event.setCancelled(true);	
 			}
 		}
 		
 	}
 	
 }
 
 
