 package com.turt2live.antishare.debug;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.turt2live.antishare.AntiShare;
 
 public class Debugger implements Listener {
 
 	public static void displayBug(Bug bug){
 		Logger log = Logger.getLogger("Minecraft");
 		log.severe("[AntiShare Debugger] *** BUG REPORT ***");
 		log.severe("[AntiShare Debugger] Class: " + bug.getInvolvedClass().getName());
 		log.severe("[AntiShare Debugger] Sender: " + bug.getSenderInvolved());
 		log.severe("[AntiShare Debugger] Exception: " + bug.getException().getMessage());
 		log.severe("[AntiShare Debugger] Message: " + bug.getMessage());
 		log.severe("[AntiShare Debugger] World: " + bug.getWorld());
 	}
 
 	private AntiShare plugin;
 
 	public Debugger(AntiShare plugin){
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		if(event.getMessage().equalsIgnoreCase("/astest")){
 			if(AntiShare.DEBUG_MODE){
 				plugin.getRegionHandler().getWorldEditHandler().forceDisplayWorldEditInformation(event.getPlayer());
 				event.setCancelled(true);
 			}
 		}
 	}
 }
