 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.listeners.PlayerEventListener;
 import com.araeosia.ArcherGames.utils.Archer;
 import com.araeosia.ArcherGames.utils.Kit;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Bruce
  */
 public class ServerWide {
 
 	public ArrayList<String> ridingPlayers = new ArrayList<String>();
 	public ArrayList<Archer> livingPlayers = new ArrayList<Archer>();
 	public ArcherGames plugin;
 	public String winner;
 	public Random r = new Random();
 	public HashMap<Integer, String> playerPlaces = new HashMap<Integer, String>();
 
 	public ServerWide(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	public void leaveGame(String playerName) {
 		
 		if(livingPlayers.contains(Archer.getByName(playerName))){
 			playerPlaces.put(livingPlayers.size(), playerName);
 			livingPlayers.remove(Archer.getByName(playerName));
 			int alivePlayers = 0;
 			for (Player p : plugin.getServer().getOnlinePlayers()) {
 				if (Archer.getByName(p.getName()).isAlive()) {
 					alivePlayers++;
 				}
 			}
 			sendMessageToAllPlayers(String.format(plugin.strings.get("playersleft"), alivePlayers));
 		}
 		Archer.getByName(playerName).kill();
 	}
 
 	public void joinGame(String playerName, Kit selectedKit) {
 		plugin.serverwide.livingPlayers.add(Archer.getByName(playerName));
 		Archer.getByName(playerName).setAlive(true);
		Archer.getByName(playerName).ready();
 		Archer.getByName(playerName).selectKit(selectedKit);
 		if (PlayerEventListener.naggerTask.containsKey(playerName)) {
 			plugin.getServer().getScheduler().cancelTask(PlayerEventListener.naggerTask.get(playerName));
 			PlayerEventListener.naggerTask.remove(playerName);
 		}
 	}
 
 	public ArrayList<Archer> getNotReadyPlayers() {
 		ArrayList<Archer> archers = new ArrayList<Archer>();
 		for (Archer a : ArcherGames.players) {
 			if (!a.isReady()) {
 				archers.add(a);
 			}
 		}
 		return archers;
 	}
 
 	public void sendToLivingPlayers(String message) {
 		// Send a message to every living archer
 		for (Archer a : livingPlayers) {
 			getPlayer(a).sendMessage(message);
 		}
 	}
 
 	public void sendMessageToAllPlayers(String message) {
 		// Send a message to every online player
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			p.sendMessage(message);
 		}
 		plugin.log.info("[ArcherGames]: " + message);
 	}
 
 	public Player getPlayer(Archer a) {
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (p.getName().equalsIgnoreCase(a.getName())) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public Archer getArcher(Player p) {
 		for (Archer a : ArcherGames.players) {
 			if (a.getName().equalsIgnoreCase(p.getName())) {
 				return a;
 			}
 		}
 		return null;
 	}
 
 	public void handleGameStart() {
 		// Handle the game start. Populate player inventories, teleport players, etc.
 	}
 
 	public void handleGameEnd() {
 		// Announce the winner, announce the runners up, give winners their money.
 		winner = playerPlaces.get(0);
 		plugin.econ.depositPlayer(playerPlaces.get(0), 5000);
 		plugin.econ.depositPlayer(playerPlaces.get(1), 2500);
 		plugin.econ.depositPlayer(playerPlaces.get(2), 1000);
 		sendMessageToAllPlayers(plugin.strings.get("gameended"));
 	}
 
 	public void tpToRandomLocation(Player player) {
 		int x = r.nextInt(32 + 1) - 16;
 		int z = r.nextInt(32 + 1) - 16;
 
 		x = player.getWorld().getSpawnLocation().getBlockX() + x;
 		z = player.getWorld().getSpawnLocation().getBlockZ() + z;
 
 		player.teleport(new Location(player.getWorld(), x, player.getWorld().getHighestBlockYAt(x, z), z));
 	}
 }
