 package edu.unca.atjones.InvManager;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /*
  * This is a sample event listener
  */
 public class InvManagerListener implements Listener {
     private final InvManager plugin;
 
     /*
      * This listener needs to know about the plugin which it came from
      */
     public InvManagerListener(InvManager plugin) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
     }
 
     /*
      * Send the sample message to all players that join
      */
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         //Load the player's inventory data from metadata
     	
     }
     
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
     	//Save the player's inventory data into metadata
     }
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
     	//increment the player's block count
     	Player p = event.getPlayer();
    	String playerName = p.getName();
    	if(plugin.blocks.containsKey(playerName)) {
    		plugin.blocks.put(playerName, plugin.blocks.get(playerName) + 1);
     	} else {
    		plugin.blocks.put(playerName, 1);
     	}
    	p.sendMessage("Blocks Broken: " + String.valueOf(plugin.blocks.get(playerName)));
     }
     
     @EventHandler
     public void onPickupItem(PlayerPickupItemEvent event) {
     	//route the item according to assigned routing rules
     }
     
     @EventHandler
     public void onOpenInventory(InventoryOpenEvent event) {
     	plugin.logger.info(event.getInventory().getTitle() + " Opened");
     }
     
 
 }
