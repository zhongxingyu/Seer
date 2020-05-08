 package com.gmail.klezst.reserves;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Enables the reservation of player slots, in case the server is at max players.
  * 
  * @author Klezst
  */
 public class Reserves extends JavaPlugin {
     
     private static Logger logger = Logger.getLogger("Minecraft");
     private static Permission permission = null;
     
     /**
      * Determines, if a player needs to be kicked.
      * 
      * @author Klezst
      */
     private Listener listener = new Listener() {
 	private final String permissionNode = "reserves";
 	
 	private HashMap<Player, Long> joinTimes = new HashMap<Player, Long>();
 	private Server server = Bukkit.getServer();
 	
 	@SuppressWarnings("unused")
 	@EventHandler
 	public void onPlayerPreLogin(PlayerLoginEvent event) {
 	    // Record join time, to determine who should be kicked.
 	    Player player = event.getPlayer();
 	    joinTimes.put(player, player.getPlayerTime());
 	    
 	    // Determine, if the server is full.
	    if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
 		if (permission.has(player, permissionNode)) {
 		    
 		    // Calculate the time each player has been connected to the server.
		    Player[] players = server.getOnlinePlayers();
 		    HashMap<Player, Long> timeConnected = new HashMap<Player, Long>();
 		    for (Player connected : players) {
 			timeConnected.put(connected, connected.getPlayerTime() - joinTimes.get(connected));
 		    }
 		    
 		    // Sort players based on timeConnected in descending order.
 		    for (int i = 1; i < players.length; i++) {
 			int j = i;
 			while (j > 0 && timeConnected.get(players[j]) > timeConnected.get(players[j - 1])) {
 			    Player temp = players[j - 1];
 			    players[j - 1] = players[j];
 			    players[j] = temp;
 			    
 			    j--;
 			}
 		    }
 		    
 		    // Kick the player who has been connected longest and does not have a slot reserved.
 		    for (Player connected : players) {
 			if (!permission.has(connected, permissionNode)) {
			    connected.kickPlayer("Sorry, the server is full; a player with a reserved slot logged in.");
			    event.allow(); // Allow the player to join the server despite that it was full.
 			    break;
 			}
 		    }
 		}
 	    }
 	}
     };
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 	return false;
     }
     
     @Override
     public void onDisable() {
 	log(Level.INFO, "Disabled");
     }
     
     @Override
     public void onEnable()
     {
 	setupPermissions();
 	this.getServer().getPluginManager().registerEvents(listener, this);
 	log(Level.INFO, "Enabled.");
     }
     
     /**
      * Logs a message.
      * @param level The importance of the message.
      * @param message The message to log.
      * 
      * @author Klezst
      */
     private void log(Level level, String message) {
 	logger.log(level, "[Reserves]" + message);
     }
     
     /**
      * Loads Vault permission management system.
      * 
      * @author Vault
      */
     private Boolean setupPermissions() {
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
 }
