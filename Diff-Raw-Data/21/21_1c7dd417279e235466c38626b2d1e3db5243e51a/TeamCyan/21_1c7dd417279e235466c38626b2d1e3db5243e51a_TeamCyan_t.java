 package co.networkery.uvbeenzaned.SnowBrawl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class TeamCyan {
 
 	private static String name;
 	private static ChatColor color;
 	private static List<String> players = new ArrayList<String>();
 	private static List<String> playersinarena = new ArrayList<String>();
 	private static List<String> deadplayers = new ArrayList<String>();
 
 	public static String getName() {
 		return name;
 	}
 
 	public static void setName(String name) {
 		TeamCyan.name = name;
 	}
 
 	public static ChatColor getColor() {
 		return color;
 	}
 
 	public static void setColor(ChatColor color) {
 		TeamCyan.color = color;
 	}
 
 	public static void join(Player p) {
 		if (!hasPlayer(p) && !TeamLime.hasPlayer(p)) {
 			if (hasDeadPlayer(p)) {
 				removeDeadPlayer(p);
 			}
 			addPlayer(p);
 			Rank.checkRank(p.getName());
 			Chat.sendAllTeamsMsg(p.getName() + ChatColor.RESET
 					+ " has joined team CYAN.");
 			p.teleport(Lobby.getLobbyspawnlocation());
 			Board.addPlayer(p);
 			if (!TeamLime.isEmpty()) {
 				Round.startTimerRound();
 			} else {
 				Chat.sendPPM(
 						"Team LIME"
 								+ ChatColor.RESET
 								+ " has no players! Please wait until someone joins to play.",
 						p);
 			}
 		} else {
 			Chat.sendPPM(
 					"You're already on a team!  Please leave to join another.",
 					p);
 		}
 	}
 
 	public static void leave(Player p) {
 		if (hasPlayer(p)) {
 			p.getInventory().setChestplate(new ItemStack(Material.AIR));
 			p.getInventory().remove(Material.SNOW_BALL);
 			Board.removePlayer(p);
 			removePlayer(p);
 			Chat.sendPPM("You've left team CYAN.", p);
 			Chat.sendTeamCyanMsg(p.getName() + ChatColor.RESET
 					+ " has left team CYAN.");
 			p.teleport(Lobby.getLobbyspawnlocation());
 			if (TeamCyan.isEmpty() && !TeamLime.isEmpty()) {
 				Round.setGameActive(false);
 				Clock.stopTimer();
 				TeamLime.teleportAllPlayersToLobby();
 				for (String tp : TeamLime.getPlayers()) {
 					Chat.sendPPM("There are no players on team CYAN"
 							+ ChatColor.RESET
 							+ " anymore.  Please wait for someone to join!",
 							Bukkit.getPlayer(tp));
 				}
 			}
 		}
 	}
 
 	public static boolean isEmpty() {
 		return getPlayers().isEmpty();
 	}
 
 	public static List<String> getPlayers() {
 		return players;
 	}
 
 	public static void addPlayer(Player p) {
 		players.add(p.getName());
 	}
 
 	public static void setPlayers(List<String> players) {
 		TeamCyan.players = players;
 	}
 
 	public static void removePlayer(Player p) {
 		removeArenaPlayer(p);
 		players.remove(p.getName());
 	}
 
 	public static boolean hasPlayer(Player p) {
 		return getPlayers().contains(p.getName());
 	}
 
 	public static boolean hasPlayer(String p) {
 		return getPlayers().contains(p);
 	}
 
 	public static boolean stringContainsPlayer(String s) {
 		for (String p : getPlayers()) {
 			if (s.toLowerCase().contains(p.toLowerCase())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static List<String> getPlayersinarena() {
 		return playersinarena;
 	}
 
 	public static void addArenaPlayer(Player p) {
 		playersinarena.add(p.getName());
 	}
 
 	public static void setPlayersinarena(List<String> playersinarena) {
 		TeamCyan.playersinarena = playersinarena;
 	}
 
 	public static void removeArenaPlayer(Player p) {
 		if (hasArenaPlayer(p)) {
 			playersinarena.remove(p.getName());
 			p.teleport(Lobby.getLobbyspawnlocation());
 		}
 	}
 
 	public static boolean hasArenaPlayer(Player p) {
 		return getPlayersinarena().contains(p.getName());
 	}
 
 	public static boolean isArenaPlayersEmpty() {
 		return playersinarena.isEmpty();
 	}
 
 	public static List<String> getDeadplayers() {
 		return deadplayers;
 	}
 
 	public static void addDeadPlayer(Player p) {
 		deadplayers.add(p.getName());
 		leave(p);
 	}
 
 	public static void setDeadplayers(List<String> deadplayers) {
 		TeamCyan.deadplayers = deadplayers;
 	}
 
 	public static void removeDeadPlayer(Player p) {
 		if (hasDeadPlayer(p)) {
 			deadplayers.remove(p.getName());
 		}
 	}
 
 	public static boolean hasDeadPlayer(Player p) {
 		return getDeadplayers().contains(p.getName());
 	}
 
 	public static void teleportAllPlayersToArena(Arena a) {
 		for (String p : getPlayers()) {
 			if (!hasArenaPlayer(Bukkit.getServer().getPlayer(p))) {
 				addArenaPlayer(Bukkit.getServer().getPlayer(p));
 				Bukkit.getServer().getPlayer(p).setGameMode(GameMode.SURVIVAL);
 				Bukkit.getServer().getPlayer(p).getInventory().clear();
 				Utilities.giveSnowballs(Bukkit.getServer().getPlayer(p));
				a.getCyanSide().getChunk().load();
				while (!a.getCyanSide().getChunk().isLoaded()) {
				}
 				Bukkit.getServer().getPlayer(p).teleport(a.getCyanSide());
 			}
 		}
 	}
 
 	public static void teleportAllPlayersToLobby() {
 		for (String p : getPlayers()) {
 			if (hasArenaPlayer(Bukkit.getServer().getPlayer(p))) {
 				removeArenaPlayer(Bukkit.getServer().getPlayer(p));
 			}
 			Rank.checkRank(p);
 		}
 	}
 
 	public static void awardTeamPoints() {
 		for (String p : getPlayers()) {
 			Stats s = new Stats(Bukkit.getPlayer(p));
 			s.giveTeamPoints();
 		}
 		Chat.sendAllTeamsMsg(ChatColor.RED + "+"
 				+ String.valueOf(Settings.getTeamPoints()) + ChatColor.RESET
 				+ " points for all of team CYAN!");
 		checkAllPlayersRanks();
 	}
 
 	private static void checkAllPlayersRanks() {
 		for (String p : getPlayers()) {
 			Rank.checkRank(p);
 		}
 	}
 }
