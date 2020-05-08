 package com.herpingdo.infinitedispenser.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class OnSignChanged implements Listener {
 	/* Called when we update a sign, or place it. */
 	@EventHandler
 	public void onSignChanged(SignChangeEvent event)
 	{
		//if (!(event.getBlock() instanceof Sign)) return;
		Sign sign = (Sign) event.getBlock().getState();
 		String[] signLines = sign.getLines();
 		if (signLines.length <= 0) return;
 		if (signLines[0].equalsIgnoreCase("[Infinite]"))
 		{
 			event.getPlayer().sendMessage(ChatColor.GREEN+"[InfiniteDispenser]"+ChatColor.YELLOW+" You just created an infinite dispenser!");
 		}
 	}
 
 }
