 package me.mentioum.plugins.lpmcnavigator;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Lpmcnavigator extends JavaPlugin {
     public void onDisable() {
         Messenger.Shutdown(this);
     }
 
     public void onEnable() {
        
         ConfigManager.Startup(this);
        
         if (ConfigManager.PluginEnabled() != true)
         {
             Messenger.StartupDisabled(this);
            this.getPluginLoader().disablePlugin(this);
            return;
         }
         Messenger.Startup(this);
         RegisterEvents();
     }
 
     private void RegisterEvents() 
     {
         PluginManager pm = this.getServer().getPluginManager();
        // pm.registerEvent(Event.Type.PLUGIN_ENABLE, new ServerEvents(), Event.Priority.Normal, this);
     }
 }
