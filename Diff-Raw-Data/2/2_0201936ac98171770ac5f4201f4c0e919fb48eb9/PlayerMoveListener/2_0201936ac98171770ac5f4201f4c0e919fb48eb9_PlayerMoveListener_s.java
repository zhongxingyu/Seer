 package com.warrows.plugins.TreeSpirit;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class PlayerMoveListener implements Listener
 {
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerMoveEvent(PlayerMoveEvent event)
 	{
 		Player player = event.getPlayer();
 		GreatTree tree = TreeSpiritPlugin.getGreatTree(player);
 
 		if (tree.isAtProximity(event.getTo().getBlock()))
 		{
 
 		} else
 		{
 			event.setCancelled(true);
 		}
 	}
 }
