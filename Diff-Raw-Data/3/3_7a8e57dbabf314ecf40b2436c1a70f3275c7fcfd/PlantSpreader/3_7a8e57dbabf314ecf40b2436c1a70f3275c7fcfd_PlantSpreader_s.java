 package com.bukkit.MasterGuy;
 
 import java.io.File;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * PlantSpreader for Bukkit
  *
  * @author Master-Guy
  */
 
 public class PlantSpreader extends JavaPlugin {
     private final PlantSpreaderPlayerListener playerListener = new PlantSpreaderPlayerListener(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	public HashMap<String, Integer> stickMap = new HashMap<String, Integer>();
 	
 	public String pluginName = "PlantSpreader";
 	public String pluginVersion = "0.5";
 	public String pluginNotes = "The configuration file can be found in /settings/";
 
     public PlantSpreader(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
     }
 
     public void onEnable() {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
         log("Plugin enabled: "+pluginName+" version "+pluginVersion);
         if(pluginNotes.length() > 0) { log(pluginNotes); }
     }
     public void onDisable() {
         System.out.println("Plugin disabled: "+pluginName+" version "+pluginVersion);
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
     
     public void log(String logText) {
     	System.out.println(logText);
     }
 }
