 package com.gmail.ne0nx3r0.coolpoints.listeners;
 
 import com.gmail.ne0nx3r0.coolpoints.CoolPoints;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class CoolPointsPlayerListener implements Listener
 {    
     private final CoolPoints plugin;
     
     public CoolPointsPlayerListener(CoolPoints plugin)
     {
         this.plugin = plugin;
     }
     
     @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerJoin(PlayerJoinEvent e)
    {                    
         if(e.getPlayer().hasPermission("coolpoints.earn")
         && CoolPoints.ppm.giveDailyWage(e.getPlayer().getName()))
         {
             e.getPlayer().sendMessage("You have earned a cool point for logging in today!");
         }

         if(plugin.UPDATE_AVAILABLE && e.getPlayer().hasPermission("coolpoints.manage"))
         {
             e.getPlayer().sendMessage(ChatColor.RED+"An update is available:"
                 +ChatColor.WHITE+plugin.UPDATE_NAME);
         }
     }
 }
