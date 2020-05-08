 package com.mistphizzle.donationpoints.plugin;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignListener implements Listener {
 
 	public static DonationPoints plugin;
 	
 	public SignListener(DonationPoints instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onSignChance(SignChangeEvent e) {
 		if (e.isCancelled()) return;
 		if (e.getPlayer() == null) return;
 		Player p = e.getPlayer();
 		String line1 = e.getLine(0);
 		
 		// Permissions
 		if (line1.equalsIgnoreCase("[Premium]") && !p.hasPermission("donationpoints.sign.create")) {
 			e.setCancelled(true);
 			p.sendMessage("cYou don't have permission to create DonationPoints signs.");
		} else if (p.hasPermission("donationpoints.sign.create") && line1.equalsIgnoreCase("[Premium]")) {
 			p.sendMessage("aYou have created a Premium sign.");
 		}
 	}
 }
