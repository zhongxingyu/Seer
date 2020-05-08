 package com.gmail.klezst.reserves;
 
 import java.util.logging.Level;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
 
 import bukkitutil.BukkitUtilJavaPlugin;
 
 /**
  * Enables the reservation of player slots, in case the server is at max players.
  * 
  * @author Klezst
  */
 public class Reserves extends BukkitUtilJavaPlugin {
    private final PlayerListener listener = new PlayerListener();
     
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
	Player[] players = this.getServer().getOnlinePlayers();
	for(Player player : players)
	    listener.playerJoinOrder.add(player);
 	log(Level.INFO, "Enabled.");
     }
 }
