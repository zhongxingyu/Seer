 package com.precipicegames.autoenchanter.listeners;
 
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 
 import com.precipicegames.autoenchanter.Autoenchanter;
 
 public class PlayerLstn extends PlayerListener {
 	private Autoenchanter plugin;
 	public PlayerLstn(Autoenchanter p)
 	{
 		plugin = p;
 	}
	
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		ConfigurationSection c = plugin.basicConfigurationHandler("PlayerInteractEvent", event.getPlayer(), event.getPlayer().getItemInHand().getType());
		if(c == null)
			return;
 		ConfigurationSection extended = c.getConfigurationSection(event.getAction().toString());
 		if(extended != null)	
 		{
 			Configuration conf = new MemoryConfiguration();
 			for(String setting : c.getKeys(true))
 			{
 				conf.set(setting, c.get(setting));
 			}
 			for(String setting : extended.getKeys(true))
 			{
 				conf.set(setting, extended.get(setting));
 			}
 			this.plugin.basicActionHandler(conf, event.getPlayer(), event.getPlayer().getItemInHand());
 		}
 		else
 		{
 			this.plugin.basicActionHandler(c, event.getPlayer(), event.getPlayer().getItemInHand());
 		}
 	}
 	public void onPlayerPortal(PlayerPortalEvent event)
 	{
 		ConfigurationSection c = plugin.basicConfigurationHandler("PlayerPortalEvent", event.getPlayer(), event.getPlayer().getItemInHand().getType());
 		plugin.basicActionHandler(c, event.getPlayer(), event.getPlayer().getItemInHand());
 	}
 	public void onPlayerPickupItem(PlayerPickupItemEvent event)
 	{
 		ConfigurationSection c = plugin.basicConfigurationHandler("PlayerPickupItemEvent", event.getPlayer(), event.getPlayer().getItemInHand().getType());
 		plugin.basicActionHandler(c, event.getPlayer(), event.getPlayer().getItemInHand());
 	}
 	public void onPlayerFish(PlayerFishEvent event)
 	{
 		ConfigurationSection c = plugin.basicConfigurationHandler("PlayerFishEvent", event.getPlayer(), event.getPlayer().getItemInHand().getType());
 		plugin.basicActionHandler(c, event.getPlayer(), event.getPlayer().getItemInHand());
	}
 	
 }
