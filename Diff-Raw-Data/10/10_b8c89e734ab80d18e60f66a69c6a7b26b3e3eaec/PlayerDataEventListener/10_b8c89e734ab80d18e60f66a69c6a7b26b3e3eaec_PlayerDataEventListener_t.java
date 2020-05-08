 package net.daboross.bukkitdev.playerdata;
 
 import java.util.logging.Level;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  *
  * @author daboross
  */
 public class PlayerDataEventListener implements Listener {
     
     private PlayerData pDataMain;
     
     protected PlayerDataEventListener(PlayerData main) {
         pDataMain = main;
     }
 
     /**
      *
      * @param evt
      */
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerJoin(PlayerJoinEvent evt) {
         PData pData = pDataMain.getPDataHandler().getPDataFromUsername(evt.getPlayer().getName());
         if (pData == null) {
             pDataMain.getLogger().log(Level.INFO, "Teleporting {0} to spawn", new Object[]{evt.getPlayer().getName()});
             evt.getPlayer().performCommand("spawn");
         }
        pDataMain.getLogger().log(Level.INFO, "{0} Joined", evt.getPlayer().getName());
         pDataMain.getPDataHandler().logIn(evt.getPlayer());
         
     }
 
     /**
      *
      * @param evt
      */
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent evt) {
         pDataMain.getPDataHandler().getPData(evt.getPlayer()).loggedOut();
     }
 }
