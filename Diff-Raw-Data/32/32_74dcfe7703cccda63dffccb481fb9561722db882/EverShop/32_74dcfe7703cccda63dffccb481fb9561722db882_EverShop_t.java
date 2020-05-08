 package GyD.EverShop;
 
 import com.nijiko.coelho.iConomy.*;
 import java.io.File;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.event.server.PluginEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * EverShop for Bukkit
  *
  * @author GyD
  */
 public class EverShop extends JavaPlugin {
     private final EverShopBlockListener blockListener = new EverShopBlockListener(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private final EverShopPlayerListener playerListener = new EverShopPlayerListener(this);
     
     /*
      * Added 
      * based on: http://pastie.org/1604147
      */
     public static iConomy iConomy;
     private Listener Listener = new Listener();
 
     private class Listener extends ServerListener {
 
         public Listener() { }
 
         public void onPluginEnabled(PluginEvent event) {
             if(event.getPlugin().getDescription().getName().equals("iConomy")) {
                 EverShop.iConomy = (iConomy)event.getPlugin();
                 System.out.println("[EverShop] Attached to iConomy.");
             }
         }
     }
     
    /*private void registerEvents() {
         this.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, Listener, Priority.Monitor, this);
     }*/
 
 
     public void onEnable() {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         
         // register right clics on blocs
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
         
         // register sign changes
         pm.registerEvent(Event.Type.SIGN_CHANGE, this.blockListener, Event.Priority.Normal, this);
         
         // register block placedBLOCK_PLACED
         pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Normal, this);
         
         // register block placed
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.blockListener, Event.Priority.Normal, this);
         // (thank you captain obvious ^^) 
 
         // Return the plugin infos
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
     }
     public void onDisable() {
         System.out.println("Goodbye world!");
     }
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 }
