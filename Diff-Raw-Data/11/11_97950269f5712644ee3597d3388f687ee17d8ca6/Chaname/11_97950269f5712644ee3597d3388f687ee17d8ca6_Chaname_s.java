 package me.Bambusstock.Chaname;
 import java.util.logging.Logger;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Chaname extends JavaPlugin{
     Logger log = Logger.getLogger("Minecraft");
     
     public void onEnable() {
	getServer().getPluginManager().registerEvents(new ChatListener(this), this);
 	log.info("Chaname enabled.");
     }
 
     public void onDisable() {
 	log.info("Chaname disabled!");
     }
 }
