 package com.mcdr.ecore.listener;
 
 import java.util.ArrayList;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.mcdr.ecore.eCore;
 
 public class eCoreRedstoneListener implements Listener {
 
 	@EventHandler
 	public void onRedstoneUpdate(BlockRedstoneEvent e){	
 		if(e.getBlock().getWorld().getName().equalsIgnoreCase("Area51")){
			Location rsLoc = new Location(e.getBlock().getWorld(), -37D, 35D, 36D);
 			Location uLoc = e.getBlock().getLocation();
 			if(uLoc.getBlockX() == rsLoc.getBlockX() && uLoc.getBlockY() == rsLoc.getBlockY() && uLoc.getBlockZ() == rsLoc.getBlockZ()){
 				ArrayList<Location> locations = new ArrayList<Location>();
 				locations.add(new Location(e.getBlock().getWorld(), -36D, 40D, 34D));
 				locations.add(new Location(e.getBlock().getWorld(), -36D, 40D, 36D));
 				locations.add(new Location(e.getBlock().getWorld(), -38D, 40D, 36D));
 				
 				if (e.getOldCurrent()==0 && e.getNewCurrent()>0){
 					for(Location loc: locations){
 						Block b = loc.getBlock();
 						try{
 							Sign s = (Sign) b.getState();
 							s.setLine(3, ChatColor.DARK_RED+" Denied!");
 							s.update();
 						} catch (ClassCastException err){
 							eCore.logger.info("[eCore] Targeted block ("+b.getType()+") is not a sign.");
 						}
 					}
 				} else if (e.getOldCurrent()>0 && e.getNewCurrent()==0){
 					for(Location loc: locations){
 						Block b = loc.getBlock();
 						try{
 							Sign s = (Sign) b.getState();
 							s.setLine(3, ChatColor.DARK_GREEN+" Granted!");
 							s.update();
 						} catch (ClassCastException err){
 							eCore.logger.info("[eCore] Targeted block ("+b.getType()+") is not a sign.");
 						}
 					}
 				}
 			}
 		}
 	}
 }
