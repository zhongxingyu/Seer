 package com.oblicom.plugins.oblicomcore.listeners;
 
 import com.oblicom.plugins.oblicomcore.OblicomCore;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import org.bukkit.ChatColor;
 
import com.oblicom.plugins.oblicomcore.entity.Citizen;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenArrestEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenReleaseEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenStolenEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenWantedEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenUnwantedEvent;
 
 /**
  *
  * @author nagib.kanaan
  */
 public class CitizenListener implements Listener {
     private OblicomCore plugin;
     
     public CitizenListener(OblicomCore plugin) {
         this.plugin = plugin;
     }
     
     public void onCitizenArrested(final CitizenArrestEvent event) {
        Citizen citizen = event.getCitizen();
        
        citizen.getPlayer().teleport(OblicomCore.world.getJail().getLocation());
        citizen.sendChatMessage(ChatColor.RED + "You were arrested.");
     }
 
     @EventHandler
     public void onCitizenReleased(CitizenReleaseEvent event) {
        Citizen citizen = event.getCitizen();
        
        citizen.getPlayer().teleport(citizen.getPlayer().getWorld().getSpawnLocation());
        citizen.sendChatMessage(ChatColor.GREEN + "You was released.");
     }
 
     @EventHandler
     public void onCitizenStolen(CitizenStolenEvent event) {
         if (event.isAlerted()) {
             
             String message = OblicomCore.configuration.getString("criminal.pickpocket.message");
             
             message = message.replace("%thief%", event.getCriminal().getPlayer().getName());
             message = message.replace("%thief%", event.getCitizen().getPlayer().getName());
             
             plugin.getServer().broadcastMessage(ChatColor.DARK_PURPLE + message);
         }
     }
 
     @EventHandler
     public void onCitizenWanted(final CitizenWantedEvent event) {
         event.getCitizen().sendChatMessage(ChatColor.RED + "You was wanted now.");
     }
 
     @EventHandler
     public void onCitizenUnwanted(CitizenUnwantedEvent event) {
         event.getCitizen().sendChatMessage(ChatColor.GREEN + "You are no longer wanted.");
     }
 }
