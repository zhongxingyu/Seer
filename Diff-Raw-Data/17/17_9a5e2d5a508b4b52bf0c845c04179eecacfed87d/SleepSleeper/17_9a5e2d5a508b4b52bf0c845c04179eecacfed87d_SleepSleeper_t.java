 package com.github.limdingwen.RealSleep;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class SleepSleeper {
 	Logger log = Logger.getLogger("RealSleep");
 	Map<String, Long> timeSlept = new HashMap<String, Long>();
 	
 	public void playerBedEnterEvent(Player who, Block bed) {
 		try {
			timeSlept = (Map<String, Long>) SLAPI.load("SleepTempData");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
			log.warning("Could not load SleepTempData! Cannot log bed enter.");
 			
 			return;
 		}
 		
 		timeSlept.put(who.getName(), who.getWorld().getFullTime());
 		
 		try {
			SLAPI.save(timeSlept, "SleepTempData");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
			log.warning("Could not find SleepTempData! Unable to save bed enter.");
 		}
 	}
 	
 	public void playerBedLeaveEvent(Player who, Block bed) {
 		try {
			timeSlept = (Map<String, Long>) SLAPI.load("SleepTempData");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			log.warning("Could not load RealSleepData! Cannot apply effect to bed leave.");
 			
 			return;
 		}
 		
 		if (timeSlept.containsKey(who.getName())) {
			log.warning("SleepTempData's player cannot be found! May be because of sleeping malfunctioning.");
 			
 			return;
 		}
 		
 		Long startTick = timeSlept.get(who.getName());
 		Long sleptTicks = who.getWorld().getFullTime() - startTick;
 		Float amountToRestore = (float) (sleptTicks * 0.016);
 		
 		if (!SleepRefresher.regain(who.getName(), amountToRestore)) {
 			log.warning("Cannot regain sleep (Bed Event)!");
 		}
 	}
 }
