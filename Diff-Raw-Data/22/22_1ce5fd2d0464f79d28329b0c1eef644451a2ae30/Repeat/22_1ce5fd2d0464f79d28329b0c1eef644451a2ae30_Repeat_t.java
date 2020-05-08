 package com.codisimus.plugins.repeat;
 
import com.codisimus.plugins.repeat.listeners.CommandListener;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Loads Plugin and registers events
  *
  * @author Codisimus
  */
 public class Repeat extends JavaPlugin {
     public static PluginManager pm;
     public static Server server;
 
     /**
      * Informs the Server Admin that memory is lost when disabling
      * 
      */
     @Override
     public void onDisable () {
         System.out.println("[Repeat] Memory of commands cleared");
     }
 
     /**
     * Registers the CommandListener
      * 
      */
     @Override
     public void onEnable () {
         server = getServer();
         pm = server.getPluginManager();
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new CommandListener(), Priority.Highest, this);
         System.out.println("Repeat "+this.getDescription().getVersion()+" is enabled!");
     }
 }
