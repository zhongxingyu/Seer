 package edu.unca.jderbysh.MinedGames;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 /*
  * This is a sample event listener
  */
 public class MinedGamesListener implements Listener {
     private final MinedGames plugin;
     
     /*
      * This listener needs to know about the plugin which it came from
      */
     public MinedGamesListener(MinedGames plugin) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
     }
 
     /*
      * Send the sample message to all players that join
      */
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         event.getPlayer().sendMessage(this.plugin.getConfig().getString("sample.message"));
         
     }
     
     /*
      * Another example of a event handler. This one will give you the name of
      * the entity you interact with, if it is a Creature it will give you the
      * creature Id.
      */
     @EventHandler
     public void onPlayerInteract(PlayerInteractEntityEvent event) {
         final EntityType entityType = event.getRightClicked().getType();
         
         event.getPlayer().sendMessage(MessageFormat.format(
                 "You interacted with a {0} it has an id of {1}",
                 entityType.getName(),
                 entityType.getTypeId()));
     }
     
     @EventHandler
     public void onBlockPickup(PlayerPickupItemEvent e) {
    	if(e.getItem().getItemStack().getType().isBlock()) {
	    	int score = e.getItem().getItemStack().getTypeId();
	    	if(!plugin.scores.containsKey(e.getPlayer()) && plugin.gameRunning) {
	    		plugin.getLogger().info(e.getPlayer().getDisplayName() + " is not in the mined game");
	    		//e.getPlayer().sendMessage("You're not in the game.");
	    	}
	    	else {
	    		plugin.scores.put(e.getPlayer(), plugin.scores.get(e.getPlayer())+score);
	    		plugin.getLogger().info("picked up an item with score: " + score + " and gave that score to player " + e.getPlayer());
	    		e.getPlayer().sendMessage("You had " + score + " points added to your score!");
 	    	}
     	}
     }
 }
