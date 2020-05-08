 package com.pwn9.PwnChickenLay;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PwnChickenLay extends JavaPlugin 
 {
 	
 	public final Logger logger = Logger.getLogger("Minecraft.PwnChickenLay");   	
 	public static List<String> enabledWorlds;
 	public static List<String> replaceWith;
 	public static int layChance;
 	static Random randomNumberGenerator = new Random();
 	
 	@Override
 	public void onEnable() 
 	{
     	this.saveDefaultConfig();
     	new PwnChickenLayItemSpawnListener(this);
     	this.loadConfig();
     	// Command Executor
     	getCommand("pwnlay").setExecutor(new PwnChickenLayCommands(this));
     	
 		// Start Metrics
 		try 
 		{
 		    MetricsLite metricslite = new MetricsLite(this);
 		    metricslite.start();
 		} 
 		catch (IOException e) 
 		{
 		    // Failed to submit the stats :-(
 		}    	
 	}
 	
 	public void onDisable() 
 	{
 
 	}	
 	
	public void spawnCheck(ItemSpawnEvent event) 
 	{
 		if(event.isCancelled()) 
 		{
 			return;
 		}
 		
 		Item test = (Item) event.getEntity();
 		
 		ItemStack is = test.getItemStack();
 		
 		if(is.getType() != Material.EGG) 
 		{
 			return;
 		}
 		
 		List<Entity> nearby = test.getNearbyEntities(0.01, 0.3, 0.01);
 		
 		for(int i = 0; i < nearby.size(); i++) 
 		{
 			if (nearby.get(i) instanceof Player)
 			{
 				return;
 			}
 		}
		  	
    	if (PwnChickenLay.random(layChance)) 
 		{	
     		String randomReplacement = replaceWith.get(randomNumberGenerator.nextInt(replaceWith.size()));     	
 			//event.setCancelled(true);
 			is.setType(Material.getMaterial(randomReplacement));
 		}
 	}	
 
 	static boolean random(int percentChance) {
 		return randomNumberGenerator.nextInt(100) > percentChance;
 	}
 	
 	public static boolean isEnabledIn(String world) 
 	{
 		return enabledWorlds.contains(world);
 	}	
 	
 	public void loadConfig() {
 		PwnChickenLay.enabledWorlds = getConfig().getStringList("enabled_worlds");
 		PwnChickenLay.layChance = getConfig().getInt("layChance");
 		PwnChickenLay.replaceWith = getConfig().getStringList("replaceWith");			
 	}	
 	
 }
