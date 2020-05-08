 package com.pwn9.PwnChickenLay;
 
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ItemSpawnEvent;
 
 public class PwnChickenLayItemSpawnListener implements Listener 
 {
     private final PwnChickenLay plugin;
     
 	public PwnChickenLayItemSpawnListener(PwnChickenLay plugin) 
 	{
 	    plugin.getServer().getPluginManager().registerEvents(this, plugin);    
 	    this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onSpawn(ItemSpawnEvent event) 
 	{
 		World world = event.getLocation().getWorld();
 		if (PwnChickenLay.isEnabledIn(world.getName())) 
 		{
			plugin.spawnCheck(event);
 		}
 	}  
 }
