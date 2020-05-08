 package me.SamV522.dBUB;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 import java.util.logging.Level;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Sam
  * Date: 8/02/12
  * Time: 11:36 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PlayerListener implements Listener{
     private dBUBLogger pluginLogger = Main.pluginLogger;
 
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         String preGroup = GMHook.getGroup(event.getPlayer());
         String newGroup = GroupMapping.getGroupFromDb(event.getPlayer());
         if(newGroup!=null&&preGroup != newGroup){
             if(newGroup != null)
             {
                 GMHook.setGroup(event.getPlayer(), newGroup);
                 Main.pluginLogger.log(Level.INFO, "Successfully set user \""+ event.getPlayer().getDisplayName()+
                                "\" to group \""+newGroup+"\"");
             }
             else
             {
                 GMHook.setGroup(event.getPlayer(),  Main.Config.getString("group-mapping.default"));
                 Main.pluginLogger.log(Level.INFO, "User \"" + event.getPlayer().getDisplayName() + "\" was not found in the database. " +
                                 " setting them to the \"" + Main.Config.getString("group-mapping.default") + "\" group");
             }
         }
     }
 }
