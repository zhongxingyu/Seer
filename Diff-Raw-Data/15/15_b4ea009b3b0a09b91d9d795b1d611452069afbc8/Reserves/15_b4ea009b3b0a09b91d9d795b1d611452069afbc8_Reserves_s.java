 package com.gmail.klezst.reserves;
 
 import java.util.logging.Level;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import bukkitutil.BukkitUtilJavaPlugin;
 
 /**
  * Enables the reservation of player slots, in case the server is at max players.
  * 
  * @author Klezst
  */
 public class Reserves extends BukkitUtilJavaPlugin {
    private PlayerListener listener = new PlayerListener();
     
     public Reserves() {
 	super("[Reserves]");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd,
 	    String commandLabel, String[] args) {
 	return false;
     }
 
     @Override
     public void onDisable() {
 	log(Level.INFO, "Disabled");
     }
 
     @Override
     public void onEnable() {
 	this.getServer().getPluginManager().registerEvents(listener, this);
 	log(Level.INFO, "Enabled.");
     }
 }
