 package com.djdch.bukkit.deathlog;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.djdch.bukkit.deathlog.listener.DeathListener;
 
 /**
  * Main class of the <b>DeathLog</b> plugin for Bukkit.
  * <p>
  * Show death messages in the server console/log.
  * 
  * @author DjDCH
  */
 public class DeathLog extends JavaPlugin {
     /**
      * Contains the Logger instance.
      */
     protected Logger logger;
 
     /**
      * Contains the deathListener instance.
      */
     protected final DeathListener deathListener = new DeathListener(this);
 
     /**
      * Method execute when the plugin is enable.
      */
     public void onEnable() {
         this.logger = this.getLogger();
 
        this.getServer().getPluginManager().registerEvents(this.deathListener, this);
     }
 
     /**
      * Method execute when the plugin is disable.
      */
     public void onDisable() {
     }
 }
