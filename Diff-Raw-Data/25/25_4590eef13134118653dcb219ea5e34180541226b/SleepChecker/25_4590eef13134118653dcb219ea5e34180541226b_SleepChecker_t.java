 package com.github.limdingwen.RealSleep;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.command.CommandSender;
 
 public class SleepChecker {	
 	static Logger log;
 	
	public static Float getSleep(String sender) {
 		log = Logger.getLogger("RealSleepChecker");
 		
 		Map<String, Float> data = new HashMap<String, Float>();
 		
 		try {
 			data = (Map) SLAPI.load("RealSleepData");
 		} catch (Exception e) {
 			log.warning("Cannot get sleep!");
 			
 			return null;
 		}
 		
		if (data.containsKey(sender)) {
			return data.get(sender);
 		}
 		else return null;
 	}
 }
