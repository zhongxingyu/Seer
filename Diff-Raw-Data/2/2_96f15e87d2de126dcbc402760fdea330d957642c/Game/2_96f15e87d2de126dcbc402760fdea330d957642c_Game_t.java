 package com.homie.endersgame.api;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.github.one4me.ImprovedOfflinePlayer;
 import com.homie.endersgame.EndersGame;
 import com.homie.endersgame.api.events.EventHandle;
 import com.homie.endersgame.api.events.game.GameEndEvent.GameEndReason;
 import com.homie.endersgame.listeners.GameListener;
 import com.homie.endersgame.sql.SQL;
 import com.homie.endersgame.sql.options.MySQLOptions;
 
 public class Game implements Runnable {
 
 	private EndersGame plugin;
 	private int id;
 	private SQL sql;
 	
 	private int gameid;
 	private int maxPlayers;
 	private int perToStart;
 	private int perToWin;
 	private int maxHits;
 	
 	private int signTracker = 0;
 	private int wait = 9;
 	private int lobbWait = 0;
 	private int timelimit = 0;
 	private int update = 0;
 	private int openDoors = 0;
 	private boolean running = false;
 	private boolean begin = false;
 	private boolean doors = false;
 	private boolean missingSign = false;
 	
 	private Scoreboard board;
 	private Score bluescore;
 	private Score redscore;
 	
 	private Location l1;
 	private Location l2;
 	
 	private ItemStack blueHelmet = new ItemStack(Material.WOOL, 1, (byte) 11);
 	private ItemStack blueChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
 	private ItemStack bluePants = new ItemStack(Material.LEATHER_LEGGINGS);
 	private ItemStack blueBoots = new ItemStack(Material.LEATHER_BOOTS);
 	
 	private ItemStack redHelmet = new ItemStack(Material.WOOL, 1, (byte) 14);
 	private ItemStack redChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
 	private ItemStack redPants = new ItemStack(Material.LEATHER_LEGGINGS);
 	private ItemStack redBoots = new ItemStack(Material.LEATHER_BOOTS);
 	
 	private HashMap<String, GameTeam> list = new HashMap<String, GameTeam>();
 	private ArrayList<String> ingame_players = new ArrayList<String>();
 	private ArrayList<String> qued = new ArrayList<String>();
 	private ArrayList<Location> gamespawns;
 	private ArrayList<Block> gate_blocks;
 	
 	private Sign sign;
 	private Location signLocation;
 	private GameStage gamestage;
 	private Lobby lobby;
 	
 	public Game(EndersGame plugin, int gameid, Lobby lobby, Location signLocation, Location l1, Location l2, ArrayList<Location> gamespawns) {
 		this.plugin = plugin;
 		this.sql = plugin.getSQL();
 		this.gameid = gameid;
 		this.lobby = lobby;
 		this.sign = null;
 		this.signLocation = signLocation;
 		this.l1 = l1;
 		this.l2 = l2;
 		this.gamespawns = gamespawns;
 		this.gamestage = GameStage.Lobby;
 		updateGame();
 		int i = 0;
 		while (i < 2) {
 			ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
 			ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
 			ItemStack plate = new ItemStack(Material.LEATHER_CHESTPLATE);
 			LeatherArmorMeta bl = (LeatherArmorMeta) boots.getItemMeta();
 			LeatherArmorMeta ll = (LeatherArmorMeta) leg.getItemMeta();
 			LeatherArmorMeta pl = (LeatherArmorMeta) plate.getItemMeta();
 			if (i == 0) {
 				bl.setColor(Color.BLUE);
 				ll.setColor(Color.BLUE);
 				pl.setColor(Color.BLUE);
 			}
 			if (i == 1) {
 				bl.setColor(Color.RED);
 				ll.setColor(Color.RED);
 				pl.setColor(Color.RED);
 			}
 			boots.setItemMeta(bl);
 			leg.setItemMeta(ll);
 			plate.setItemMeta(pl);
 			if (i == 0) {
 				blueChestplate = plate;
 				bluePants = leg;
 				blueBoots = boots;
 			}
 			if (i == 1) {
 				redChestplate = plate;
 				redPants = leg;
 				redBoots = boots;
 			}
 			i++;
 		}
 		board = Bukkit.getScoreboardManager().getNewScoreboard();
 		Objective obj = board.registerNewObjective("spawnscore", "spawn");
 		obj.setDisplayName("In Enemy Spawn");
 		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 		bluescore = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "In Spawn: "));
 		redscore = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "In Spawn: "));
 	}
 	
 	public static void cancelCreatingCommandEventForPlayer(Player player, boolean message) {
 		if (EndersGame.creating_game_players.contains(player.getName())) {
 			EndersGame.creating_game_players.remove(player.getName());
 			EndersGame.creating_game_ids.remove(player.getName());
 			player.getInventory().remove(Material.WOOD_SPADE);
 			if (message) player.sendMessage(ChatColor.GOLD + "You have canceled creating an arena");
 			return;
 		}
 		else if (EndersGame.creating_lobby_players.contains(player.getName())) {
 			EndersGame.creating_lobby_players.remove(player.getName());
 			EndersGame.creating_lobby_ids.remove(player.getName());
 			player.getInventory().remove(Material.WOOD_SPADE);
 			if (message) player.sendMessage(ChatColor.GOLD + "You have canceled creating a lobby");
 			return;
 		}
 		else if (EndersGame.creating_spawns_players.contains(player.getName())) {
 			EndersGame.creating_spawns_players.remove(player.getName());
 			EndersGame.creating_spawns_ids.remove(player.getName());
 			if (message) player.sendMessage(ChatColor.GOLD + "You have canceled setting a games spawn points");
 			return;
 		} else {
 			if (message) player.sendMessage(ChatColor.RED + "You aren't creating anything!");
 			return;
 		}
 	}
 	
 	public static void registerSign(EndersGame plugin, int gameid, Sign sign) throws SQLException {
 		plugin.getSQL().query("INSERT INTO signs (gameid, coordX, coordY, coordZ, world) " +
 				"VALUES (" + gameid + ", " + sign.getLocation().getX() + ", " + sign.getLocation().getY() + ", " + 
 				sign.getLocation().getZ() + ", '" + sign.getLocation().getWorld().getName() + "')");
 		plugin.getRunningGames().get(gameid).setSign(sign);
 		EventHandle.callSignRegisterEvent(sign);
 	}
 	
 	public static void unregisterSign(int gameid, SQL sql) throws SQLException {
 		sql.query("DELETE FROM signs WHERE gameid=" + gameid);
 		EventHandle.callSignUnregisterEvent(gameid);
 	}
 	
 	public static boolean isRegisteredSign(Sign sign, SQL sql) {
 		try {
 			int signID = Integer.parseInt(sign.getLine(1).split("Arena ")[1]);
 			Block b = getSign(signID, sql);
 			if (b == null) return false;
 			return (sign.getX() == b.getX() && sign.getY() == b.getY() && sign.getZ() == b.getZ());
		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public static boolean isRegisteredSign(Sign sign, int gameid, SQL sql) {
 		try {
 			Block b = getSign(gameid, sql);
 			if (b == null) return false;
 			return (sign.getX() == b.getX() && sign.getY() == b.getY() && sign.getZ() == b.getZ());
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public static Block getSign(int gameid, SQL sql) throws SQLException {
 		ResultSet rs = sql.query("SELECT * FROM signs WHERE gameid=" + gameid);
 		if (rs == null) return null;
 		if (sql.getDatabaseOptions() instanceof MySQLOptions) rs.first();
 		int x = rs.getInt("coordX");
 		int y = rs.getInt("coordY");
 		int z = rs.getInt("coordZ");
 		World world = Bukkit.getServer().getWorld(rs.getString("world"));
 		return world.getBlockAt(new Location(world, x, y, z));
 	}
 	
 	public static Location getSignLocation(int gameid, SQL sql) throws SQLException {
 		ResultSet rs = sql.query("SELECT * FROM signs WHERE gameid=" + gameid);
 		if (rs == null) return null;
 		if (sql.getDatabaseOptions() instanceof MySQLOptions) rs.first();
 		int x = rs.getInt("coordX");
 		int y = rs.getInt("coordY");
 		int z = rs.getInt("coordZ");
 		World world = Bukkit.getServer().getWorld(rs.getString("world"));
 		return new Location(world, x, y, z);
 	}
 	
 	public static ArrayList<Integer> getAllGamesFromDatabase(SQL sql) throws SQLException {
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		ResultSet rs = sql.query("SELECT gameid FROM games");
 		if (rs == null) return list;
 		while (rs.next()) {
 			list.add(rs.getInt(1));
 		}
 		return list;
 	}
 	
 	public static ArrayList<Block> getGateBlocks(Location loc1, Location loc2) {
 		ArrayList<Block> blocks = new ArrayList<Block>();
 		int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
         int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
  
         int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
         int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
  
         int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
         int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
         
         for (int x = bottomBlockX; x <= topBlockX; x++) {
         	for (int z = bottomBlockZ; z <= topBlockZ; z++) {
         		for (int y = bottomBlockY; y <= topBlockY; y++) {
         			Block block = loc1.getWorld().getBlockAt(x, y, z);
         			if (block.getType() == Material.GLOWSTONE) {
         				blocks.add(block);
         			}
         		}
         	}
         }
         return blocks;
 	}
 	
 	/**
 	 * @author Adamki11s
 	 */
 	public static ArrayList<String> getPlayersInTeamSpawn(Location center, int radius) {
 		ArrayList<String> playerSet = new ArrayList<String>();
 		for(Player p : Bukkit.getServer().getOnlinePlayers()){
 			Location point1 = new Location(center.getWorld(), center.getX() - radius, center.getY() - radius, center.getZ() - radius);
 			Location point2 = new Location(center.getWorld(), center.getX() + radius, center.getY() + radius, center.getZ() + radius);
 			double x1 = point1.getX(), x2 = point2.getX(),
 			y1 = point1.getY(), y2 = point2.getY(),
 			z1 = point1.getZ(), z2 = point2.getZ(),
 			px = p.getLocation().getX(),
 			py = p.getLocation().getY(),
 			pz = p.getLocation().getZ();
 			if((((py <= y1) && 
 				(py >= y2)) || 
 				((py >= y1) && 
 				(py <= y2))) && 
 				(((pz <= z1) && 
 				(pz >= z2)) || 
 				((pz >= z1) && 
 				(pz <= z2)))  &&  
 				(((px <= x1) && 
 				(px >= x2)) || 
 				((px >= x1) && 
 				(px <= x2))) && 
 				(((px <= x1) && 
 				(px >= x2)) || 
 				((px >= x1) && 
 				(px <= x2)))){
 				playerSet.add(p.getName());
 				}	
 		}
 		return playerSet;
 	}
 	
 	public enum GameStage {
 		Lobby, PreGame, Ingame;
 		
 		public static GameStage getFrom(String args) {
 			switch(args.toLowerCase()) {
 			case "lobby": return GameStage.Lobby;
 			case "pregame": return GameStage.PreGame;
 			case "ingame": return GameStage.Ingame;
 			default: return GameStage.Lobby;
 			}
 		}
 		
 		@Override
 		public String toString() {
 			switch(this) {
 			case PreGame: return "PreGame";
 			case Ingame: return "Ingame";
 			case Lobby: return "Lobby";
 			default: return null;
 			}
 		}
 	}
 	
 	public enum GameTeam {
 		Team1, Team1Leader, Team2, Team2Leader, TeamUnknown;
 		
 		public static GameTeam getFrom(String args) {
 			switch(args.toLowerCase()) {
 			case "team1": return GameTeam.Team1;
 			case "team1leader": return GameTeam.Team1Leader;
 			case "team2": return GameTeam.Team2;
 			case "team2leader": return GameTeam.Team2Leader;
 			default: return GameTeam.TeamUnknown;
 			}
 		}
 		
 		@Override
 		public String toString() {
 			switch(this) {
 			case Team1: return "Team1";
 			case Team1Leader: return "Team1Leader";
 			case Team2: return "Team2";
 			case Team2Leader: return "Team2Leader";
 			default: return "Unknown";
 			}
 		}
 		
 		public String toNiceString() {
 			switch(this) {
 			case Team1: return "Team 1";
 			case Team1Leader: return "Team 1 Leader";
 			case Team2: return "Team 2";
 			case Team2Leader: return "Team 2 Leader";
 			default: return "Unknown";
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		signTracker++;
 		update++;
 		ingame_players.clear();
 		for (Map.Entry<String, GameTeam> en : list.entrySet()) {
 			ingame_players.add(en.getKey());
 		}
 		
 		if (signTracker == 2) {
 			signTracker = 0;
 			updateGameSign();
 		}
 		
 		if (running && ingame_players.size() == 0) {
 			resetDoors();
 			running = false;
 			EventHandle.callGameEndEvent(this, GameEndReason.NoPlayersLeft);
 		}
 		
 		if (running && timelimit == 300) {
 			ejectAllPlayers(false);
 			running = false;
 			timelimit = 0;
 			EventHandle.callGameEndEvent(this, GameEndReason.TimeLimitReached);
 		}
 		
 		if (update == 7) {
 			update = 0;
 			updateGame();
 		}
 		
 		if (ingame_players.size() > 0 && !running) {
 			running = true;
 		}
 		
 		if (!running) {
 			if (lobbWait != 0) lobbWait = 0;
 			if (timelimit != 0) timelimit = 0;
 			if (openDoors != 0) openDoors = 0;
 			if (wait != 9) wait = 9;
 			if (begin) begin = false;
 			if (doors) doors = false;
 		}
 		
 		if (running) {
 			runGame();
 		}
 	}
 	
 	private void updateGame() {
 		this.maxPlayers = plugin.getConfiguration().getMaxPlayers();
 		this.perToStart = plugin.getConfiguration().getMinPercentToStart();
 		this.maxHits = plugin.getConfiguration().getMaxHits();
 		this.perToWin = plugin.getConfiguration().getPercentInSpawnToWin();
 		if (sign == null && !missingSign) missingSign = true; 
 		if (!missingSign && !(plugin.getServer().getWorld(sign.getWorld().getName()).getBlockAt(sign.getLocation()).getState() instanceof Sign)) { sign = null; missingSign = true; }
 	}
 	
 	private void updateGameSign() {
 		try {
 			if (!(plugin.getServer().getWorld(signLocation.getWorld().getName()).getBlockAt((int) signLocation.getX(), (int) signLocation.getY(), (int) signLocation.getZ()).getState() instanceof Sign)) return;
 			sign = (Sign) plugin.getServer().getWorld(signLocation.getWorld().getName()).getBlockAt((int) signLocation.getX(), (int) signLocation.getY(), (int) signLocation.getZ()).getState();
 		} catch (Exception e) {
 			return;
 		}
 		if (sign == null) return;
 		String line = sign.getLine(2);
 		int originalSize = 0;
 		int currentSize = 0;
 		int originalMax = 0;
 		int currentMax = 0;
 		try {
 			originalSize = Integer.parseInt(line.split("/")[0]);
 			currentSize = ingame_players.size();
 			originalMax = Integer.parseInt(line.split("/")[1]);
 			currentMax = maxPlayers;
 		} catch (NumberFormatException e) {
 			return;
 		}
 		if (ingame_players.size() == 0 && gamestage != GameStage.Lobby) {
 			gamestage = GameStage.Lobby;
 		}
 		if (gamestage == GameStage.Lobby || gamestage == GameStage.PreGame) {
 			if (!sign.getLine(0).equalsIgnoreCase(ChatColor.DARK_GREEN + "Ender's Game") && currentSize != currentMax) {
 				sign.setLine(0, ChatColor.DARK_GREEN + "Ender's Game");
 			}
 		}
 		if (gamestage == GameStage.Ingame || currentSize == currentMax) {
 			if (!sign.getLine(0).equalsIgnoreCase(ChatColor.DARK_RED + "Ender's Game")) {
 				sign.setLine(0, ChatColor.DARK_RED + "Ender's Game");
 			}
 		}
 		if (GameStage.getFrom(sign.getLine(3)) != gamestage) {
 			sign.setLine(3, gamestage.toString());
 		}
 		if (originalSize != currentSize || originalMax != currentMax) {
 			sign.setLine(2, currentSize + "/" + currentMax);
 		}
 		sign.update();
 	}
 	
 	private void runGame() {
 		if (gamestage == GameStage.Ingame) {
 			if (openDoors < 10) {
 				sendGameMessage(ChatColor.RED + "Opening doors in " + ChatColor.GOLD + (10-openDoors));
 				openDoors++;
 				return;
 			}
 			if (openDoors == 10 && !doors) {
 				for (int i = 0; i < gate_blocks.size(); i++) {
 					Block b = gate_blocks.get(i);
 					plugin.getServer().getWorld(b.getLocation().getWorld().getName()).getBlockAt(b.getLocation()).setType(Material.AIR);
 				}
 				doors = true;
 				sendGameMessage(ChatColor.GREEN + "The gates are open!");
 			}
 			timelimit++;
 			ArrayList<String> toRemove = new ArrayList<String>();
 			HashMap<String, Integer> toUpdate = new HashMap<String, Integer>();
 			for (Map.Entry<String, Integer> en : GameListener.players_hit.entrySet()) {
 				String i = en.getKey();
 				if (ingame_players.contains(i)) {
 					Integer b = en.getValue();
 					toRemove.add(i);
 					if (b == 3) continue;
 					toUpdate.put(i, b+1);
 				}
 			}
 			for (String i : toRemove) {
 				GameListener.players_hit.remove(i);
 			}
 			for (Map.Entry<String, Integer> en : toUpdate.entrySet()) {
 				GameListener.players_hit.remove(en.getKey());
 				GameListener.players_hit.put(en.getKey(), en.getValue());
 			}
 			ArrayList<String> toRemoveHit = new ArrayList<String>();
 			for (Map.Entry<String, Integer> en : GameListener.times_players_hit.entrySet()) {
 				if (en.getValue() >= maxHits) {
 					Player player = plugin.getServer().getPlayer(en.getKey());
 					if (player != null) {
 						GameTeam pteam = list.get(player.getName());
 						if (pteam == GameTeam.Team1 || pteam == GameTeam.Team1Leader) {
 							player.teleport(gamespawns.get(0));
 						}
 						if (pteam == GameTeam.Team2 || pteam == GameTeam.Team2Leader) {
 							player.teleport(gamespawns.get(1));
 						}
 						player.sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.RED + "You've been hit " + maxHits + " time(s) and you have respawned back at base");
 					}
 					toRemoveHit.add(en.getKey());
 				}
 			}
 			for (String i : toRemoveHit) {
 				GameListener.times_players_hit.remove(i);
 			}
 			ArrayList<String> team1spawn = getPlayersInTeamSpawn(gamespawns.get(0), 4);
 			ArrayList<String> team2spawn = getPlayersInTeamSpawn(gamespawns.get(1), 4);
 			ArrayList<String> team1 = getPlayersOnTeam(GameTeam.Team1);
 			ArrayList<String> team1leader = getPlayersOnTeam(GameTeam.Team1Leader);
 			ArrayList<String> team2 = getPlayersOnTeam(GameTeam.Team2);
 			ArrayList<String> team2leader = getPlayersOnTeam(GameTeam.Team2Leader);
 			if (team1.size() + team1leader.size() == 0) {
 				sendGameMessage(ChatColor.GREEN + "Team 2 has won, all Team 1 players have left or have been wiped out");
 				ejectAllPlayers(false);
 				running = false;
 				EventHandle.callGameEndEvent(this, GameEndReason.Team2Victory);
 				return;
 			}
 			if (team2.size() + team2leader.size() == 0) {
 				sendGameMessage(ChatColor.GREEN + "Team 1 has won, all Team 2 players have left or have been wiped out");
 				ejectAllPlayers(false);
 				running = false;
 				EventHandle.callGameEndEvent(this, GameEndReason.Team1Victory);
 				return;
 			}
 			if (team1leader.size() == 0) {
 				ArrayList<String> t1players = getPlayersOnTeam(GameTeam.Team1);
 				int t11 = (int) (Math.random() * t1players.size());
 				String t1 = t1players.get(t11);
 				list.remove(t1);
 				list.put(t1, GameTeam.Team1Leader);
 				sendTeamMessage(GameTeam.Team1, ChatColor.DARK_GREEN + "Your team leader has been lost, " + ChatColor.GOLD + t1 + ChatColor.DARK_GREEN + " is your new leader");
 				team1leader.add(t1);
 			}
 			if (team2leader.size() == 0) {
 				ArrayList<String> t2players = getPlayersOnTeam(GameTeam.Team2);
 				int t21 = (int) (Math.random() * t2players.size());
 				String t2 = t2players.get(t21);
 				list.remove(t2);
 				list.put(t2, GameTeam.Team2Leader);
 				sendTeamMessage(GameTeam.Team2, ChatColor.DARK_GREEN + "Your team leader has been lost, " + ChatColor.GOLD + t2 + ChatColor.DARK_GREEN + " is your new leader");
 				team2leader.add(t2);
 			}
 			team1.add(team1leader.get(0));
 			team2.add(team2leader.get(0));
 			ArrayList<String> remove_from_t1_spawn = new ArrayList<String>();
 			ArrayList<String> remove_from_t2_spawn = new ArrayList<String>();
 			for (String p : team1spawn) {
 				if (team1.contains(p)) {
 					remove_from_t1_spawn.add(p);
 					continue;
 				}
 			}
 			for (String p : team2spawn) {
 				if (team2.contains(p)) {
 					remove_from_t2_spawn.add(p);
 					continue;
 				}
 			}
 			for (String p : remove_from_t1_spawn) {
 				team1spawn.remove(p);
 			}
 			for (String p : remove_from_t2_spawn) {
 				team2spawn.remove(p);
 			}
 			double t1win = (perToWin/100)*team2.size();
 			double t2win = (perToWin/100)*team1.size();
 			if (t1win < 1) t1win = 1;
 			if (t2win < 1) t2win = 1;
 			if (team1spawn.size() >= t1win) {
 				sendGameMessage(ChatColor.GREEN + "Team 2 has won, at least " + (int) perToWin + "% of their team is in the enemy spawn");
 				ejectAllPlayers(false);
 				running = false;
 				EventHandle.callGameEndEvent(this, GameEndReason.Team2Victory);
 				return;
 			}
 			if (team2spawn.size() >= t2win) {
 				sendGameMessage(ChatColor.GREEN + "Team 1 has won, at least " + (int) perToWin + "% of their team is in the enemy spawn");
 				ejectAllPlayers(false);
 				running = false;
 				EventHandle.callGameEndEvent(this, GameEndReason.Team1Victory);
 				return;
 			}
 			bluescore.setScore(team2spawn.size());
 			redscore.setScore(team1spawn.size());
 			for (String i : ingame_players) {
 				Player player = plugin.getServer().getPlayer(i);
 				if (player == null) continue;
 				if (player.getGameMode() != GameMode.CREATIVE) player.setGameMode(GameMode.CREATIVE);
 				player.setScoreboard(board);
 				Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
 				if (below != Material.AIR && below != Material.OBSIDIAN && below != Material.REDSTONE_LAMP_ON && below != Material.REDSTONE_LAMP_OFF && !player.isFlying()) {
 					player.teleport(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY()+1, player.getLocation().getZ(),
 							player.getLocation().getYaw(), player.getLocation().getPitch()));
 					player.setFlying(true);
 				}
 				if (!player.getInventory().contains(Material.SNOW_BALL)) {
 					player.sendMessage(ChatColor.RED + "Resetting inventory");
 					player.getInventory().clear();
 					ItemStack[] genInv = player.getInventory().getContents();
 					genInv[0] = new ItemStack(Material.SNOW_BALL);
 					player.getInventory().setContents(genInv);
 				}
 				ItemStack[] parmor = player.getInventory().getArmorContents();
 				if (parmor[3].getType() != Material.WOOL || parmor[2].getType() != Material.LEATHER_CHESTPLATE || parmor[1].getType() != Material.LEATHER_LEGGINGS || parmor[0].getType() != Material.LEATHER_BOOTS) {
 					if (team1.contains(player.getName())) {
 						player.getInventory().setHelmet(blueHelmet);
 						player.getInventory().setChestplate(blueChestplate);
 						player.getInventory().setLeggings(bluePants);
 						player.getInventory().setBoots(blueBoots);
 					}
 					if (team2.contains(player.getName())) {
 						player.getInventory().setHelmet(redHelmet);
 						player.getInventory().setChestplate(redChestplate);
 						player.getInventory().setLeggings(redPants);
 						player.getInventory().setBoots(redBoots);
 					}
 					player.sendMessage(ChatColor.RED + "Resetting armor");
 				}
 			}
 		}
 		if (gamestage == GameStage.PreGame && !begin) {
 			lobbWait++;
 			if (lobbWait == 30) {
 				sendGameMessage(ChatColor.RED + "30 seconds left");
 			}
 			if (lobbWait == 60) {
 				begin = true;
 				lobbWait = 0;
 			}
 			return;
 		}
 		if (gamestage == GameStage.PreGame && begin) {
 			gamestage = GameStage.Ingame;
 			gate_blocks = getGateBlocks(l1, l2);
 			ArrayList<String> team1 = getPlayersOnTeam(GameTeam.Team1);
 			ArrayList<String> team2 = getPlayersOnTeam(GameTeam.Team2);
 			ArrayList<String> team1leader = getPlayersOnTeam(GameTeam.Team1Leader);
 			ArrayList<String> team2leader = getPlayersOnTeam(GameTeam.Team2Leader);
 			if (team1.size() + team1leader.size() == 0 || team2.size() + team2leader.size() == 0) {
 				sendGameMessage(ChatColor.RED + "A team is empty!");
 				ejectAllPlayers(true);
 				return;
 			}
 			if (team1leader.size() < 1) {
 				int t11 = (int) (Math.random() * team1.size());
 				String t1 = team1.get(t11);
 				list.remove(t1);
 				list.put(t1, GameTeam.Team1Leader);
 				team1.remove(t1);
 				team1leader.add(t1);
 			}
 			team1.add(team1leader.get(0));
 			if (team2leader.size() < 1) {
 				int t12 = (int) (Math.random() * team2.size());
 				String t2 = team2.get(t12);
 				list.remove(t2);
 				list.put(t2, GameTeam.Team2Leader);
 				team2.remove(t2);
 				team2leader.add(t2);
 			}
 			team2.add(team2leader.get(0));
 			
 			for (String i : team1) {
 				Player player = plugin.getServer().getPlayer(i);
 				player.teleport(gamespawns.get(0));
 				player.getInventory().clear();
 				ItemStack[] genInv = player.getInventory().getContents();
 				genInv[0] = new ItemStack(Material.SNOW_BALL);
 				player.getInventory().setContents(genInv);
 				player.getInventory().setHelmet(blueHelmet);
 				player.getInventory().setChestplate(blueChestplate);
 				player.getInventory().setLeggings(bluePants);
 				player.getInventory().setBoots(blueBoots);
 				player.setScoreboard(board);
 				player.setPlayerTime(16000, false);
 			}
 			for (String i : team2) {
 				Player player = plugin.getServer().getPlayer(i);
 				player.teleport(gamespawns.get(1));
 				player.getInventory().clear();
 				ItemStack[] genInv = player.getInventory().getContents();
 				genInv[0] = new ItemStack(Material.SNOW_BALL);
 				player.getInventory().setContents(genInv);
 				player.getInventory().setHelmet(redHelmet);
 				player.getInventory().setChestplate(redChestplate);
 				player.getInventory().setLeggings(redPants);
 				player.getInventory().setBoots(redBoots);
 				player.setScoreboard(board);
 				player.setPlayerTime(16000, false);
 			}
 			sendGameMessage(ChatColor.DARK_GREEN + "Prepare to fight!");
 			EventHandle.callGameStartEvent(this);
 			return;
 		}
 		if (gamestage == GameStage.Lobby) {
 			wait++;
 			int u = ingame_players.size();
 			int r = getPlayersOnTeam(GameTeam.Team1).size();
 			int w = getPlayersOnTeam(GameTeam.Team2).size();
 			if (u >= (maxPlayers*perToStart/100)) {
 				gamestage = GameStage.PreGame;
 				int t11 = (int) (Math.random() * r);
 				int t22 = (int) (Math.random() * w);
 				String t1;
 				String t2;
 				try {
 					t1 = getPlayersOnTeam(GameTeam.Team1).get(t11);
 					t2 = getPlayersOnTeam(GameTeam.Team2).get(t22);
 				} catch (Exception e) {
 					sendGameMessage(ChatColor.RED + "A team is empty!");
 					ejectAllPlayers(true);
 					return;
 				}
 				list.remove(t1);
 				list.remove(t2);
 				list.put(t1, GameTeam.Team1Leader);
 				list.put(t2, GameTeam.Team2Leader);
 				Player l1 = plugin.getServer().getPlayer(t1);
 				Player l2 = plugin.getServer().getPlayer(t2);
 				l1.sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.DARK_GREEN + "You are team 1's leader!");
 				l2.sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.DARK_GREEN + "You are team 2's leader!");
 				sendGameMessage(ChatColor.DARK_GREEN + l1.getDisplayName() + ChatColor.DARK_GREEN + " and " + l2.getDisplayName() + ChatColor.DARK_GREEN + " are team 1 and team 2 leaders respectively");
 				sendGameMessage(ChatColor.RED + "Each team now has 1 minute with their leader to discuss battle plans," + ChatColor.BOLD + " the other team can't see your messages");
 			}
 			if (wait == 10 && gamestage == GameStage.Lobby) {
 				sendGameMessage(ChatColor.DARK_GREEN + "Waiting for more players...");
 				wait = 0;
 			}
 		}
 	}
 	
 	public void resetDoors() {
 		for (Block b : gate_blocks) {
 			plugin.getServer().getWorld(b.getWorld().getName()).getBlockAt(b.getX(), b.getY(), b.getZ()).setType(Material.GLOWSTONE);
 		}
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public void addToDatabase() throws SQLException {
 		sql.query("INSERT INTO games (gameid, lobbyid, x1, y1, z1, x2, y2, z2, world, gamestage) " +
 				"VALUES (" + gameid + ", " + lobby.getLobbyId() + ", " + getLocationOne().getX() + ", " + getLocationOne().getY() + ", " + getLocationOne().getZ() + 
 				", " + getLocationTwo().getX() + ", " + getLocationTwo().getY() + ", " + getLocationTwo().getZ() + ", '" + getLocationTwo().getWorld().getName() + "', '" + 
 				GameStage.Lobby.toString() + "')");
 		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L, 20L);
 		plugin.addRunner(this);
 		EventHandle.callGameCreateEvent(this);
 	}
 	
 	public void removeFromDatabase() throws SQLException {
 		sql.query("DELETE FROM games WHERE gameid=" + gameid);
 		sql.query("DELETE FROM gamespawns WHERE gameid=" + gameid);
 		plugin.removeRunner(gameid);
 		plugin.getServer().getScheduler().cancelTask(id);
 		EventHandle.callGameDeleteEvent(this);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void addPlayer(Player player) {
 		if (ingame_players.contains(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You are already in this game!");
 			return;
 		}
 		if (EndersGame.playing_players_inventory.containsKey(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You are already in another game!");
 			return;
 		}
 		if (gamestage == GameStage.Ingame) {
 			player.sendMessage(ChatColor.GOLD + "Game is already in session, adding you to the que.");
 			qued.add(player.getName());
 			return;
 		}
 		if (ingame_players.size() == maxPlayers) {
 			player.sendMessage(ChatColor.RED + "This game is full");
 			return;
 		}
 		EndersGame.playing_players_inventory.put(player.getName(), player.getInventory().getContents());
 		EndersGame.player_players_armor.put(player.getName(), player.getInventory().getArmorContents());
 		EndersGame.playing_players_gamemode.put(player.getName(), player.getGameMode());
 		player.getInventory().clear();
 		player.setGameMode(GameMode.SURVIVAL);
 		player.getInventory().getContents()[0] = new ItemStack(Material.SNOW_BALL);
 		player.updateInventory();
 		player.teleport(lobby.getSpawn());
 		int team1 = getPlayersOnTeam(GameTeam.Team1).size() + getPlayersOnTeam(GameTeam.Team1Leader).size();
 		int team2 = getPlayersOnTeam(GameTeam.Team2).size() + getPlayersOnTeam(GameTeam.Team2Leader).size();
 		if (team1 > team2) {
 			list.put(player.getName(), GameTeam.Team2);
 		} else {
 			list.put(player.getName(), GameTeam.Team1);
 		}
 		ingame_players.add(player.getName());
 		sendGameMessage(ChatColor.GREEN + player.getDisplayName() + " has joined!");
 		EventHandle.callPlayerJoinEndersGameEvent(this, player);
 	}
 	
 	public ArrayList<String> getPlayersOnTeam(GameTeam team) {
 		ArrayList<String> p = new ArrayList<String>();
 		for(Map.Entry<String, GameTeam> en : list.entrySet()) {
 			if (en.getValue() == team) {
 				p.add(en.getKey());
 			}
 		}
 		return p;
 	}
 	
 	public void shutdown() {
 		ejectAllPlayers(false);
 		resetDoors();
 		running = false;
 		plugin.getServer().getScheduler().cancelTask(id);
 	}
 	
 	public void ejectAllPlayers(boolean message) {
 		for (String i : ingame_players) {
 			ejectPlayer(plugin.getServer().getPlayer(i), message);
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void ejectPlayer(Player player, boolean message) {
 		if (ingame_players.contains(player.getName())) {
 			String name = player.getName();
 			player.resetPlayerTime();
 			GameListener.players_hit.remove(name);
 			GameListener.times_players_hit.remove(name);
 			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
 			player.setFlying(false);
 			player.getInventory().clear();
 			player.setGameMode(EndersGame.playing_players_gamemode.get(name));
 			player.getInventory().setContents(EndersGame.playing_players_inventory.get(name));
 			player.getInventory().setArmorContents(EndersGame.player_players_armor.get(name));
 			player.updateInventory();
 			EndersGame.player_players_armor.remove(name);
 			EndersGame.playing_players_gamemode.remove(name);
 			EndersGame.playing_players_inventory.remove(name);
 			list.remove(name);
 			player.teleport(plugin.getServer().getWorld(player.getWorld().getName()).getSpawnLocation());
 			EventHandle.callPlayerLeaveEndersGameEvent(gameid, player);
 			if (message) player.sendMessage(ChatColor.GREEN + "You have left arena " + gameid);
 		}
 	}
 	
 	public void ejectOfflinePlayer(ImprovedOfflinePlayer player) {
 		String name = player.getName();
 		if (ingame_players.contains(name)) {
 			player.setLocation(player.getLocation().getWorld().getSpawnLocation());
 			PlayerInventory in = player.getInventory();
 			in.clear();
 			in.setContents(EndersGame.playing_players_inventory.get(name));
 			in.setArmorContents(EndersGame.player_players_armor.get(name));
 			player.setGameMode(EndersGame.playing_players_gamemode.get(name));
 			player.setInventory(in);
 			list.remove(name);
 			ingame_players.remove(name);
 			player.savePlayerData();
 			EndersGame.player_players_armor.remove(name);
 			EndersGame.playing_players_gamemode.remove(name);
 			EndersGame.playing_players_inventory.remove(name);
 			EndersGame.debug("Offline player " + player.getName() + " has been ejected from game " + gameid);
 		}
 	}
 	
 	public int getGameId() {
 		return gameid;
 	}
 	
 	public Lobby getLobby() {
 		return lobby;
 	}
 	
 	public Location getLocationOne() {
 		return l1;
 	}
 	
 	public Location getLocationTwo() {
 		return l2;
 	}
 	
 	public boolean isMissingSign() {
 		return missingSign;
 	}
 	
 	public HashMap<String, GameTeam> getPlayerList() {
 		return list;
 	}
 	
 	public ArrayList<String> getArrayListofPlayers() {
 		return ingame_players;
 	}
 	
 	public GameStage getGameStage() {
 		return gamestage;
 	}
 	
 	public void setGameStage(GameStage gamestage) {
 		this.gamestage = gamestage;
 		try {
 			sql.query("UPDATE games SET gamestage='" + gamestage.toString() + "' WHERE gameid=" + gameid);
 		} catch (SQLException e) {
 			debug("SQLException while updating gamestage for game " + gameid);
 			e.printStackTrace();
 		}
 	}
 	
 	public Block getSign() throws SQLException {
 		return getSign(gameid, sql);
 	}
 	
 	public void setSign(Sign sign) {
 		this.sign = sign;
 	}
 	
 	public void setSignLocation(Location signLocation) {
 		this.signLocation = signLocation;
 	}
 	
 	public void setGameSpawns(ArrayList<Location> gamespawns) throws SQLException {
 		this.gamespawns = gamespawns;
 		sql.query("INSERT INTO gamespawns (gameid, x1, y1, z1, x2, y2, z2, world) " +
 				"VALUES (" + gameid + ", " + gamespawns.get(0).getX() + ", " + gamespawns.get(0).getY() + ", " + gamespawns.get(0).getZ() + 
 				", " + gamespawns.get(1).getX() + ", " + gamespawns.get(1).getY() + ", " + gamespawns.get(1).getZ() + ", '" + gamespawns.get(0).getWorld().getName() + "')");
 	}
 	
 	public void sendGameMessage(String message) {
 		for (String i : ingame_players) {
 			plugin.getServer().getPlayer(i).sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.RESET + message);
 		}
 	}
 	
 	public void sendTeamMessage(GameTeam team, String message) {
 		for (Map.Entry<String, GameTeam> en : list.entrySet()) {
 			if (en.getValue() == team) {
 				plugin.getServer().getPlayer(en.getKey()).sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.RESET + message);
 			}
 		}
 	}
 	
 	private void debug(String debug) {
 		EndersGame.debug(debug);
 	}
 }
