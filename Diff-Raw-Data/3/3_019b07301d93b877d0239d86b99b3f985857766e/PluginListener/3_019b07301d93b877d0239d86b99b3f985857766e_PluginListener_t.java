 package net.erbros.Lottery;
 
 import org.bukkit.event.server.PluginEvent;
 import org.bukkit.event.server.ServerListener;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Checks for plugins whenever one is enabled
  */
 public class PluginListener extends ServerListener {
     public PluginListener() { }
 
    public void PluginEnableEvent(PluginEvent event) {
         if(Lottery.getiConomy() == null) {
             Plugin iConomy = Lottery.getBukkitServer().getPluginManager().getPlugin("iConomy");
 
             if (iConomy != null) {
                 if(iConomy.isEnabled()) {
                     Lottery.setiConomy((iConomy)iConomy);
                     System.out.println("[Lottery] Successfully linked with iConomy.");
                 }
             }
         }
     }
 }
