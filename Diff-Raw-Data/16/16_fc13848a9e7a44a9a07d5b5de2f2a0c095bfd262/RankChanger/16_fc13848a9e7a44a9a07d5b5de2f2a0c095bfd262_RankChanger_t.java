 package me.armar.plugins.autorank;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class RankChanger implements Listener, Runnable {
 
     private Autorank plugin;
     private Config config;
     private DataStorage data;
     private VaultHandler vault;
     private int currentPlayer;
 
     public RankChanger(Autorank plugin) {
 	this.plugin = plugin;
 	config = plugin.getConf();
 	data = plugin.getData();
 	vault = plugin.getVault();
 	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 1205);
     }
 
     @Override
     public void run() {
 	Player[] players = plugin.getServer().getOnlinePlayers();
 
 	if (players.length == 0) {
 	    return;
 	}
 
 	currentPlayer++;
 	if (currentPlayer >= players.length) {
 	    currentPlayer = 0;
 	}
 	
 	try {
 	    CheckRank(players[currentPlayer]);
 	} catch (Throwable t) {
 	    t.printStackTrace();
 	}
 
 	int nextCheck = 6000 / players.length;
 	if (nextCheck < 100) {
 	    nextCheck = 100;
 	}
 	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, nextCheck);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void OnPlayerJoin(PlayerJoinEvent e) {
 	Player[] players = plugin.getServer().getOnlinePlayers();
 	if (players.length > 0) {
 	    currentPlayer = new Random().nextInt(players.length) - 1;
 	}
     }
 
     public void CheckRank(Player player) {
 
 	if (player == null) {
 	    plugin.debugMessage("Player was NULL");
 	    return;
 	}
 
 	String playerName = player.getName().toLowerCase();
 
 	if (!data.exists(playerName)) {
 	    data.set(playerName, 0);
 	}
 
 	if (!(Boolean) config.get("Enabled")) {
 	    return;
 	}
 
 	if (player.hasPermission("autorank.exclude") && !player.hasPermission("autorank.sf5k4fg7hu")) {
	    plugin.debugMessage("Player " + playerName + " has the autorank.exclude permission and will not be ranked.");
 	    return;
 	}
 
 	String world = player.getWorld().getName();
 
 	String[] playerGroups = vault.getGroups(player, world);
 	if (playerGroups.length == 0) {
 	    playerGroups = new String[] { "default" };
 	}
 
 	for (String group : playerGroups) {
 	    plugin.debugMessage("Player " + playerName + " is being rank checked in world " + world + " and has rank " + group);
 	    int entry = 1;
 	    boolean found = false;
 	    while (!found && config.get(entry + ".from") != null) {
 
 		if (plugin.getDebug()) {
 		    plugin.debugMessage("Testing entry " + entry);
 		    plugin.debugMessage("From: " + (String) config.get(entry + ".from"));
 		    plugin.debugMessage("To: " + (String) config.get(entry + ".to"));
 		    plugin.debugMessage("World: " + (String) config.get(entry + ".world"));
 		}
 
		if (group.equalsIgnoreCase((String) config.get(entry + ".from")) && ((config.get(entry + ".world") == null)
			|| world.equalsIgnoreCase((String) config.get(entry + ".world")))) {
 		    found = true;
 		    plugin.debugMessage("Confirmed entry " + entry);
 		} else {
 		    entry++;
 
 		}
 	    }
 
 	    if (found == true) {
 
 		Integer timePlayed = (Integer) data.get(playerName);
 
 		plugin.debugMessage("Time played: " + timePlayed);
 		plugin.debugMessage("Time needed: " + (Integer) config.get(entry + ".required minutes played"));
 
 		if (!(timePlayed == null || timePlayed < (Integer) config.get(entry + ".required minutes played"))) {
 
 		    // remove groups
 		    if (config.get("Remove group command") == null) {
 			vault.playerRemoveGroup(world, playerName, group);
 
 		    } else {
 			Server server = Bukkit.getServer();
 			String cmd = (String) config.get("Remove group command");
 			if (!cmd.equals("none")) {
 			    cmd = cmd.replace("&n", playerName);
 			    cmd = cmd.replace("&g", group);
 			    cmd = cmd.replace("&w", world);
 			    server.dispatchCommand(server.getConsoleSender(), cmd);
 			}
 		    }
 
 		    // add groups
 		    if (config.get("Add group command") == null) {
 			String[] toGroups = ((String) config.get(entry + ".to")).split(";");
 			for (String toGroup : toGroups)
 			    vault.playerAddGroup(world, playerName, toGroup);
 
 		    } else {
 			Server server = Bukkit.getServer();
 
 			String[] toGroups = ((String) config.get(entry + ".to")).split(";");
 			for (String toGroup : toGroups) {
 			    String cmd = (String) config.get("Add group command");
 			    if (!cmd.equals("none")) {
 				cmd = cmd.replace("&n", playerName);
 				cmd = cmd.replace("&g", toGroup);
 				cmd = cmd.replace("&w", world);
 				server.dispatchCommand(server.getConsoleSender(), cmd);
 			    }
 			}
 		    }
 
 		    // Send messages
 		    String message = (String) config.get(entry + ".message");
 		    if (message != null)
 			player.sendMessage(message.replaceAll("(&([a-f0-9]))", "\u00A7$2"));
 
 		    // Execute commands
 		    String commandEntry = (String) config.get(entry + ".commands");
 		    String[] commands = null;
 		    if (commandEntry != null) {
 			commands = commandEntry.split(";");
 		    }
 
 		    if (commands != null) {
 			for (String cmd : commands) {
 			    Server server = Bukkit.getServer();
 			    if (cmd != null) {
 				cmd = cmd.replace("&n", playerName);
 
 				// legacy (me being inconsistent >.>):
 				cmd = cmd.replace("&p", playerName);
 				server.dispatchCommand(server.getConsoleSender(), cmd);
 			    }
 			}
 		    }
 
 		    plugin.debugMessage("Sent message: " + message);
 
 		}
 	    }
 	}
     }
 
 }
