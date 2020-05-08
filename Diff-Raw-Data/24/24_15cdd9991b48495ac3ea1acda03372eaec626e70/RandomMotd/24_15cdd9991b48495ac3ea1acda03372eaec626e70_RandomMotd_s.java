 package fr.aumgn.motd;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RandomMotd extends JavaPlugin implements Listener {
 
     private static Logger logger = Logger.getLogger("Minecraft.RandomMotd");
     private static MotdsManager motdsManager;
     
     public static void update() {
         
     }
 
     @EventHandler
     public void onServerListPing(ServerListPingEvent event) {
         String motd = motdsManager.get();
         if (motd != null) {
             event.setMotd(motd);
         }
     }
 
     @Override
     public void onEnable() {
         motdsManager = new MotdsManager();
         Bukkit.getPluginManager().registerEvents(this, this);
        logger.info("RandomMotd enabled (" + motdsManager.size() + " motd loaded).");
     }
 
     @Override
     public void onDisable() {
         logger.info("RandomMotd disabled.");
     }
 
 }
