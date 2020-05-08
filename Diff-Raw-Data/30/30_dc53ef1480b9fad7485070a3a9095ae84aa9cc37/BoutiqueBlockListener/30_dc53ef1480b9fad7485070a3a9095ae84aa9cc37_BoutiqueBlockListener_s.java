 package com.danstoncube.plugin.drzoid.Boutique;
 
 import org.bukkit.Location;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class BoutiqueBlockListener extends BlockListener
 {
 	private Boutique plugin;
 	
 	public BoutiqueBlockListener(Boutique signTrader)
 	{
 		this.plugin = signTrader;
 		signTrader.getClass();
 	}
 
 	public void onSignChange(SignChangeEvent e) 
 	{
 		Location location = e.getBlock().getLocation();
 		
 		if ((!e.isCancelled()) && (plugin.signmanager.haveLocation(location)))
 			plugin.signmanager.remove(location);
		
		//e.getPlayer().sendMessage("dbg0");
		
		//String[] lines = e.getLines();
		
 		
 		this.plugin.signmanager.setSign(e.getBlock(), e.getPlayer(), e.getLines());
 	}
 
 	public void onBlockPlace(BlockPlaceEvent e) 
 	{
 		Location location = e.getBlock().getLocation();
 		
 		//on enleve de la hashmap, on l'y remettra au moment ou un panneau sera edité
 		if ((!e.isCancelled()) && (plugin.signmanager.haveLocation(location)))
 			plugin.signmanager.remove(location);
 	}
 
 	public void onBlockBreak(BlockBreakEvent e) 
 	{
 		Location location = e.getBlock().getLocation();
 		
 		//on enleve de la hashmap s'il y est
 		if ((!e.isCancelled()) && (plugin.signmanager.haveLocation(location))) 
 			plugin.signmanager.remove(location);
 	}
 }
