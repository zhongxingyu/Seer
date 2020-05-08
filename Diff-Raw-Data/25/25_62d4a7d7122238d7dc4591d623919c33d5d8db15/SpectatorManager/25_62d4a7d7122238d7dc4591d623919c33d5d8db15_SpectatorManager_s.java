 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SpectatorManager {
 	private static SpectatorManager instance;
 	public static SpectatorManager get() {
 		return instance;
 	}
 	private JavaPlugin currentPlugin;
 	public SpectatorManager(JavaPlugin plugin) {
 		currentPlugin = plugin;
 		instance = this;
 	}
 	private List<Player> Spectators = new ArrayList<Player>();
 	public void PlayerJoined(Player player) {
 		for(Player p:Spectators) {
 			if(p == player) continue;
 			player.hidePlayer(p);
 		}
 	}
 	public boolean isSpectator(Player player) {
 		return Spectators.contains(player);
 	}
 	public void addSpectator(Player player) {
 		player.setAllowFlight(true);
 		player.getInventory().clear();
 		makePlayerInvisibleToAll(player);
 		if(Spectators.contains(player)) return;
 		Spectators.add(player);
 	}
 	public void removeSpectator(Player player) {
 		player.setFlying(false);
 		player.setAllowFlight(false);
 		player.getInventory().clear();
 		makePlayerVisibleToAll(player);
 		if(!Spectators.contains(player)) return;
 		Spectators.remove(player);
 	}
 	private void makePlayerInvisibleToAll(Player player) {
 		Player[] players = currentPlugin.getServer().getOnlinePlayers();
 		for(Player p : players) {
 			player.hidePlayer(p);
 		}
 	}
 	private void makePlayerVisibleToAll(Player player) {
 		Player[] players = currentPlugin.getServer().getOnlinePlayers();
 		for(Player p : players) {
 			player.showPlayer(p);
 		}
 	}
	public String handleSpectatorCommand(String command, String param) {
		// TODO Auto-generated method stub
		if ( command == "add" )
 		{
 				Player addPlayer = currentPlugin.getServer().getPlayer(param);
 				if(addPlayer == null)
 					return String.format("Player '%s' not found",param);
 				
 				addSpectator(addPlayer);
 				return "Player Added to Spectators";
 		}
		else if ( command == "remove" )
 		{
 				Player removePlayer = currentPlugin.getServer().getPlayer(param);
 				if(removePlayer == null)
 					return String.format("Player '%s' not found",param);
 				
 				removeSpectator(removePlayer);
 				return "Player removed from Spectators";
 		}
		else if ( command == "list" )
 		{
 				StringBuilder list = new StringBuilder();
 				list.append(this.Spectators.size() +" spectator(s): ");
 				if(this.Spectators.size() > 0)
 					for(Player p: this.Spectators)
 						list.append(p.getName()+", ");
 							
 				return list.toString().substring(0,list.length()-2);
 		}
 		return "No command " + command + " found. Valid commands are add {player} and remove {player}";
 	}
 }
