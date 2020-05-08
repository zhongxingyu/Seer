 package net.craftrepo.WeatherControl;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;
 
 /**
  * CraftRepo Bans for Bukkit
  * @author AllGamer
  * 
  * Copyright 2011 AllGamer, LLC.
  * See LICENSE for licensing information.
  */
 
 public class weathercontrolPlayerListener extends PlayerListener 
 {
	@SuppressWarnings("unused")
 	private final weathercontrol plugin;
 	private final Logger log = Logger.getLogger("Minecraft");
 
 	public weathercontrolPlayerListener(weathercontrol instance) 
 	{
 		plugin = instance;
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		Action action = event.getAction();
 		if(action == Action.RIGHT_CLICK_AIR||action == Action.RIGHT_CLICK_BLOCK)
 		{
 			int pickid = 278;
 			if(event.getItem()!=null)
 			if(event.getItem().getTypeId()==pickid)
 			{
 				if (plugin.lightningpick.containsKey(event.getPlayer()))
 				{
 					Block targetBlock = event.getPlayer().getTargetBlock(null, 35);
 					if (targetBlock!=null)
 					{
 						Location strikeloc = targetBlock.getLocation();
 						event.getPlayer().getWorld().strikeLightning(strikeloc);
 					}
 					else
 					{
 						event.getPlayer().sendMessage("No block in sight");
 					}
 					//Location strikeloc = event.getClickedBlock().getLocation();
 					//user.getWorld().strikeLightning(strikeloc);
 				}
 			}
 		}
 	}
 }
 
