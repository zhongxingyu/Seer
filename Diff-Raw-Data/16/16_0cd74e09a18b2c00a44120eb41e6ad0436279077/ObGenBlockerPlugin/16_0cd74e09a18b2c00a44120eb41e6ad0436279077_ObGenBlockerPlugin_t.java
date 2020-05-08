 package com.puidokas.epuidokas;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.logging.Level;
 
 public class ObGenBlockerPlugin extends JavaPlugin{
     public void onEnable(){
         getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, new ObGenBlockerBlockListener(), Event.Priority.Low, this);
        getServer().getLogger().log(Level.INFO, "[" + getDescription().getName() + "] Version " + getDescription().getVersion() + " enabled!");
     }
     public void onDisable(){
        getServer().getLogger().log(Level.INFO, "[" + getDescription().getName() + "] Version " + getDescription().getVersion() + " disabled!");
     }
 }
