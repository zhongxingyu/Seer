 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.utils.Archer;
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Bruce
  */
 public class ServerWide {
 
	public static ArrayList<Archer> livingPlayers;
 
 	public static void killPlayer(Player player) {
 		livingPlayers.remove(getArcher(player));
 		ServerWide.getArcher(player).kill();
 	}
 
 	public static ArrayList<Archer> getNotReadyPlayers() {
 		ArrayList<Archer> archers = new ArrayList<Archer>();
 		for (Archer a : ArcherGames.players) {
 			if (!a.isReady()) {
 				archers.add(a);
 			}
 		}
 		return archers;
 	}
 
 	public static void sendToLivingPlayers(String message) {
 		// Send a message to every living archer
 		for (Archer a : livingPlayers) {
 			getPlayer(a).sendMessage(message);
 		}
 	}
 
 	public static void sendMessageToAllPlayers(String message) {
 		// Send a message to every online player
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			p.sendMessage(message);
 		}
 		ArcherGames.log.info("[ArcherGames]: " + message);
 	}
 
 	public static Player getPlayer(Archer a) {
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (p.getName().equalsIgnoreCase(a.getName())) {
 				return p;
 			}
 		}
 		return null;
 	}
 	
 	public static Archer getArcher(Player p){
 		for (Archer a : ArcherGames.players) {
 			if (a.getName().equalsIgnoreCase(p.getName())) {
 				return a;
 			}
 		}
 		return null;
 	}
 }
