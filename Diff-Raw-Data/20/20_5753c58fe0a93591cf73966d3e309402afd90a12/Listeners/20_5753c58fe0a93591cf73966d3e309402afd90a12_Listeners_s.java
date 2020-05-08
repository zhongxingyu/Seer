 package com.hcherndon.dj;
 
 import com.hcherndon.dj.framework.DJP;
 import com.hcherndon.dj.framework.Validate;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: HcHerndon
  * Date: 7/13/13
  * Time: 5:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Listeners implements Listener{
     @EventHandler(priority = EventPriority.NORMAL)
     public void onJoin(PlayerJoinEvent e){
        com.hcherndon.dj.DoubleJumper.getInstance().addDJP(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onQuit(PlayerQuitEvent e){
        com.hcherndon.dj.DoubleJumper.getInstance().getDJP(e.getPlayer().getName()).decimate();
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onKick(PlayerKickEvent e){
        com.hcherndon.dj.DoubleJumper.getInstance().getDJP(e.getPlayer().getName()).decimate();
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onToggleFlight(PlayerToggleFlightEvent e){
         Validate.checkNullDJP(e.getPlayer().getName());
        e.setCancelled(com.hcherndon.dj.DoubleJumper.getInstance().getDJP(e.getPlayer()).invoke());
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void commandPreprocess(PlayerCommandPreprocessEvent e){
         String label = e.getMessage().replaceFirst("/", "");
         label = label.split(" ")[0];
        if(label.equalsIgnoreCase(com.hcherndon.dj.DoubleJumper.getInstance().getFlyCommand())){
             e.setCancelled(true);
             Validate.checkNullDJP(e.getPlayer().getName());
             DJP d = com.hcherndon.dj.DoubleJumper.getInstance().getDJP(e.getPlayer());
             if(d.canPlayerFly()){
                 d.toggleFlight();
                 return;
             } else
                 d.println("&cYou do not have permission to fly!");
         } else return;
     }
 }
