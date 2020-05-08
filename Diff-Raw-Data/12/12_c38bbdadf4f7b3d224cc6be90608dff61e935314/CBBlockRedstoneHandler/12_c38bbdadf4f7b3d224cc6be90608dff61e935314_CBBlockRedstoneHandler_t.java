 package com.andoutay.clockblocker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 public class CBBlockRedstoneHandler implements Listener
 {
 	private Logger log = Logger.getLogger("Minecraft");
 	private long lastTimeChecked;
 	private ClockBlocker plugin;
 	private HashMap<Location, Long> std;
 	private HashMap<Location, Integer> suspicious;
 	private ArrayList<Location> clockblocks;
 	
 	CBBlockRedstoneHandler(ClockBlocker plugin)
 	{
 		this.plugin = plugin;
 		std = new HashMap<Location, Long>();
 		suspicious = new HashMap<Location, Integer>();
 		clockblocks = new ArrayList<Location>();
 		lastTimeChecked = System.currentTimeMillis();
 	}
 	
 	@EventHandler
 	public void onBLockRedstone(BlockRedstoneEvent evt)
 	{
		if (plugin.shouldMonitor() && evt.getNewCurrent() >= 1)
 		{
 			long startTime = System.currentTimeMillis();
 			Location coord = evt.getBlock().getLocation();
 			
 			if (std.containsKey(coord))
 			{
 				// * 1.1 is to add a 10% margin of error so we suspect more things
 				if ((startTime - std.get(coord)) < (60.0 / CBConfig.maxCyclesPerMin) * 1.1 * 1000)
 				{
 					std.remove(coord);
 					suspicious.put(coord, 1);
 				}
 				else
 					std.put(coord, startTime);
 			}
 			else if (suspicious.containsKey(coord))
 				suspicious.put(coord, suspicious.get(coord) + 1);
 			else if (clockblocks.contains(coord))
 			{
 				if (CBConfig.stopSignal)
 					evt.setNewCurrent(0);
 				else
 				{
 					//log.info("current mat: " + evt.getBlock().getType());
 					evt.getBlock().setTypeId(CBConfig.replaceBlock);
 					//log.info("new mat: " + evt.getBlock().getType());
 				}
 				clockblocks.remove(coord);
 			}
 			else
 				std.put(coord, startTime);
 
 			if (startTime - lastTimeChecked > 60 * 1000)
 			{
 				Set<Location> temp = suspicious.keySet();
 				for (Location p : temp)
 				{
 					if (suspicious.get(p) > CBConfig.maxCyclesPerMin)
 					{
 						clockblocks.add(p);
 					}
 					else
 						std.put(p, startTime);
 				}
 				suspicious.clear();
 
 				lastTimeChecked = startTime;
 			}
 
 			log.info("Done! Took " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s!");
 		}
 	}
 	
 	public HashMap<Location, Integer> getSuspicious()
 	{
 		return suspicious;
 	}
 }
