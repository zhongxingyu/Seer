 package com.homie.endersgame.runnable;
 
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
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.homie.endersgame.EndersGame;
 import com.homie.endersgame.api.Game;
 import com.homie.endersgame.api.Game.GameStage;
 import com.homie.endersgame.api.Game.GameTeam;
 import com.homie.endersgame.api.GameManager;
 import com.homie.endersgame.listeners.GameListener;
 
 public class GameManageRun implements Runnable {
 
 	private EndersGame plugin;
 	private GameManager gm;
 	private int gameid;
 	private int id;
 	
 	private ArrayList<String> ingame_players = new ArrayList<String>();
 	private ArrayList<Location> gamespawns = new ArrayList<Location>();
 	private ArrayList<Block> gate_blocks = new ArrayList<Block>();
 	private ItemStack[] definv;
 	private int max;
 	private int per;
 	private int hitsToBeEjected;
 	private double perToWin;
 	
 	private int wait = 9;
 	private int lobbWait = 0;
 	private int timelimit = 0;
 	private int openDoors = 0;
 	private boolean begin = false;
 	private boolean doors = false;
 	
 	public GameManageRun(EndersGame plugin, int gameid) {
 		this.plugin = plugin;
 		this.gm = plugin.getGameManager();
 		this.gameid = gameid;
 		this.max = plugin.getConfiguration().getMaxPlayers();
 		this.per = plugin.getConfiguration().getMinPercentToStart();
 		this.hitsToBeEjected = plugin.getConfiguration().getHitsToBeEjected();
 		this.perToWin = plugin.getConfiguration().getPercentInSpawnToWin();
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public void resetDoor(Game game) {
 		for (int i = 0; i < gate_blocks.size(); i++) {
 			Block b = gate_blocks.get(i);
 			game.getLocationOne().getWorld().getBlockAt(b.getLocation()).setType(Material.GLOWSTONE);
 		}
 	}
 	
 	public int getGameId() {
 		return gameid;
 	}
 	
 	public ArrayList<String> getIngamePlayers() {
 		return ingame_players;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			Game game = gm.getGame(gameid);
 			if (game.getPlayerList().size() == 0) {
 				gm.updateGameStage(gameid, GameStage.Lobby);
 				resetDoor(game);
 				gm.removeRunner(this);
 				Bukkit.getScheduler().cancelTask(id);
 				return;
 			}
 			if (gamespawns.isEmpty()) gamespawns = gm.getGameSpawns(gameid);
 			if (game.getGameStage() == GameStage.Ingame) {
 				if (openDoors < 10) {
 					gm.sendGameMessage(gameid, ChatColor.RED + "Opening doors in " + ChatColor.GOLD + (10-openDoors));
 					openDoors++;
 					return;
 				}
 				if (openDoors == 10 && !doors) {
 					for (int i = 0; i < gate_blocks.size(); i++) {
 						Block b = gate_blocks.get(i);
 						game.getLocationOne().getWorld().getBlockAt(b.getLocation()).setType(Material.AIR);
 					}
 					doors = true;
 					gm.sendGameMessage(gameid, ChatColor.GREEN + "The gates are open!");
 				}
 				timelimit++;
 				if (timelimit == 300) {
 					gm.sendGameMessage(gameid, "Time limit reached");
 					plugin.ejectGame(gameid);
 					return;
 				}
 				ingame_players = game.getArrayListofPlayers();
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
 					if (en.getValue() >= hitsToBeEjected) {
 						gm.sendGameMessage(gameid, ChatColor.RED + "Player " + ChatColor.GOLD + en.getKey() + ChatColor.RED + " has been hit " + hitsToBeEjected + " times and has been wiped out from the game");
 						toRemoveHit.add(en.getKey());
 					}
 				}
 				for (String i : toRemoveHit) {
 					GameListener.times_players_hit.remove(i);
 					plugin.ejectPlayer(gameid, i);
 				}
 				ArrayList<String> team1spawn = gm.getPlayersInTeamSpawn(gamespawns.get(0), 4);
 				ArrayList<String> team2spawn = gm.getPlayersInTeamSpawn(gamespawns.get(1), 4);
 				ArrayList<String> team1 = gm.getPlayersOnTeam(gameid, GameTeam.Team1);
 				ArrayList<String> team1leader = gm.getPlayersOnTeam(gameid, GameTeam.Team1Leader);
 				ArrayList<String> team2 = gm.getPlayersOnTeam(gameid, GameTeam.Team2);
 				ArrayList<String> team2leader = gm.getPlayersOnTeam(gameid, GameTeam.Team2Leader);
 				if (team1.size() + team1leader.size() == 0) {
 					gm.sendGameMessage(gameid, ChatColor.GREEN + "Team 2 has won, all Team 1 players have left or have been wiped out");
 					plugin.ejectGame(gameid);
 					return;
 				}
 				if (team2.size() + team2leader.size() == 0) {
 					gm.sendGameMessage(gameid, ChatColor.GREEN + "Team 1 has won, all Team 2 players have left or have been wiped out");
 					plugin.ejectGame(gameid);
 					return;
 				}
 				if (team1leader.size() == 0) {
 					HashMap<String, GameTeam> list = game.getPlayerList();
 					ArrayList<String> t1players = gm.getPlayersOnTeam(gameid, GameTeam.Team1);
 					int t11 = (int) (Math.random() * t1players.size());
 					String t1 = t1players.get(t11);
 					list.remove(t1);
 					list.put(t1, GameTeam.Team1Leader);
 					game.setPlayerList(list);
 					gm.updateGamePlayers(gameid, list);
 					gm.sendTeamMessage(gameid, GameTeam.Team1, ChatColor.DARK_GREEN + "Your team leader has been lost, " + ChatColor.GOLD + t1 + ChatColor.DARK_GREEN + " is your new leader");
 					team1leader.add(t1);
 				}
 				if (team2leader.size() == 0) {
 					HashMap<String, GameTeam> list = game.getPlayerList();
 					ArrayList<String> t2players = gm.getPlayersOnTeam(gameid, GameTeam.Team2);
 					int t21 = (int) (Math.random() * t2players.size());
 					String t2 = t2players.get(t21);
 					list.remove(t2);
 					list.put(t2, GameTeam.Team2Leader);
 					game.setPlayerList(list);
 					gm.updateGamePlayers(gameid, list);
 					gm.sendTeamMessage(gameid, GameTeam.Team2, ChatColor.DARK_GREEN + "Your team leader has been lost, " + ChatColor.GOLD + t2 + ChatColor.DARK_GREEN + " is your new leader");
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
 					gm.sendGameMessage(gameid, ChatColor.GREEN + "Team 2 has won, at least " + (int) perToWin + "% of their team is in the enemy spawn");
 					plugin.ejectGame(gameid);
 					return;
 				}
 				if (team2spawn.size() >= t2win) {
 					gm.sendGameMessage(gameid, ChatColor.GREEN + "Team 1 has won, at least " + (int) perToWin + "% of their team is in the enemy spawn");
 					plugin.ejectGame(gameid);
 					return;
 				}
 				Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
 				Objective obj = board.registerNewObjective("spawnscore", "spawn");
 				obj.setDisplayName("In Enemy Spawn");
 				obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 				Score bluescore = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "In Spawn:"));
 				Score redscore = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "In Spawn:"));
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
 						player.getInventory().setContents(definv);
 					}
 					if (player.getInventory().getArmorContents().length != 4) {
 						ItemStack[] def = player.getInventory().getContents();
 						def[0] = new ItemStack(Material.SNOW_BALL);
 						definv = def;
 						ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
 						ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
 						ItemStack plate = new ItemStack(Material.LEATHER_CHESTPLATE);
 						LeatherArmorMeta bl = (LeatherArmorMeta) boots.getItemMeta();
 						LeatherArmorMeta ll = (LeatherArmorMeta) leg.getItemMeta();
 						LeatherArmorMeta pl = (LeatherArmorMeta) plate.getItemMeta();
 						if (team1.contains(player.getName())) {
 							bl.setColor(Color.BLUE);
 							ll.setColor(Color.BLUE);
 							pl.setColor(Color.BLUE);
 						}
 						else if (team2.contains(player.getName())) {
 							bl.setColor(Color.RED);
 							ll.setColor(Color.RED);
 							pl.setColor(Color.RED);
 						}
 						boots.setItemMeta(bl);
 						leg.setItemMeta(ll);
 						plate.setItemMeta(pl);
 						player.getInventory().setBoots(boots);
 						player.getInventory().setLeggings(leg);
 						player.getInventory().setChestplate(plate);
 						player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) 11));
 						player.getInventory().setContents(def);
 						player.sendMessage(ChatColor.RED + "Resetting armor");
 					}
 				}
 			}
 			if (game.getGameStage() == GameStage.PreGame && !begin) {
 				lobbWait++;
 				if (lobbWait == 30) {
 					gm.sendGameMessage(gameid, ChatColor.RED + "30 seconds left");
 				}
 				if (lobbWait == 60) {
 					begin = true;
 					lobbWait = 0;
 				}
 				return;
 			}
 			if (game.getGameStage() == GameStage.PreGame && begin) {
 				game.setGameStage(GameStage.Ingame);
 				gm.updateGameStage(gameid, game.getGameStage());
 				ArrayList<String> team1 = gm.getPlayersOnTeam(gameid, GameTeam.Team1);
 				team1.add(gm.getPlayersOnTeam(gameid, GameTeam.Team1Leader).get(0));
 				ArrayList<String> team2 = gm.getPlayersOnTeam(gameid, GameTeam.Team2);
 				team2.add(gm.getPlayersOnTeam(gameid, GameTeam.Team2Leader).get(0));
 				ArrayList<Location> spawns = gm.getGameSpawns(gameid);
 				for (String i : team1) {
 					Player player = plugin.getServer().getPlayer(i);
 					player.teleport(spawns.get(0));
 					player.getInventory().clear();
 					ItemStack[] def = player.getInventory().getContents();
 					def[0] = new ItemStack(Material.SNOW_BALL);
 					definv = def;
 					ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
 					ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
 					ItemStack plate = new ItemStack(Material.LEATHER_CHESTPLATE);
 					LeatherArmorMeta bl = (LeatherArmorMeta) boots.getItemMeta();
 					LeatherArmorMeta ll = (LeatherArmorMeta) leg.getItemMeta();
 					LeatherArmorMeta pl = (LeatherArmorMeta) plate.getItemMeta();
 					bl.setColor(Color.BLUE);
 					ll.setColor(Color.BLUE);
 					pl.setColor(Color.BLUE);
 					boots.setItemMeta(bl);
 					leg.setItemMeta(ll);
 					plate.setItemMeta(pl);
 					player.getInventory().setBoots(boots);
 					player.getInventory().setLeggings(leg);
 					player.getInventory().setChestplate(plate);
 					player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) 11));
 					player.getInventory().setContents(def);
 				}
 				for (String i : team2) {
 					Player player = plugin.getServer().getPlayer(i);
 					player.teleport(spawns.get(1));
 					player.getInventory().clear();
 					ItemStack[] def = player.getInventory().getContents();
 					def[0] = new ItemStack(Material.SNOW_BALL);
 					definv = def;
 					ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
 					ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
 					ItemStack plate = new ItemStack(Material.LEATHER_CHESTPLATE);
 					LeatherArmorMeta bl = (LeatherArmorMeta) boots.getItemMeta();
 					LeatherArmorMeta ll = (LeatherArmorMeta) leg.getItemMeta();
 					LeatherArmorMeta pl = (LeatherArmorMeta) plate.getItemMeta();
 					bl.setColor(Color.RED);
 					ll.setColor(Color.RED);
 					pl.setColor(Color.RED);
 					boots.setItemMeta(bl);
 					leg.setItemMeta(ll);
 					plate.setItemMeta(pl);
 					player.getInventory().setBoots(boots);
 					player.getInventory().setLeggings(leg);
 					player.getInventory().setChestplate(plate);
 					player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) 14));
 					player.getInventory().setContents(def);
 				}
 				gm.sendGameMessage(gameid, ChatColor.DARK_GREEN + "Prepare to fight!");
 				gate_blocks = gm.getGateBlocks(game.getLocationOne(), game.getLocationTwo());
 				EndersGame.debug("Detected gate blocks: " + gate_blocks.size());
 				return;
 			}
 			if (game.getGameStage() == GameStage.Lobby) {
 				wait++;
 				int u = gm.getGamePlayers(gameid).size();
 				int r = gm.getPlayersOnTeam(gameid, GameTeam.Team1).size();
 				int w = gm.getPlayersOnTeam(gameid, GameTeam.Team2).size();
 				if (u >= (max*per/100)) {
 					game.setGameStage(GameStage.PreGame);
 					gm.updateGameStage(gameid, game.getGameStage());
 					gm.sendGameMessage(gameid, ChatColor.AQUA + "There are now enough players in the game, randomly selecting team leaders");
 					int t11 = (int) (Math.random() * r);
 					int t22 = (int) (Math.random() * w);
 					String t1 = gm.getPlayersOnTeam(gameid, GameTeam.Team1).get(t11);
 					String t2 = gm.getPlayersOnTeam(gameid, GameTeam.Team2).get(t22);
 					HashMap<String, GameTeam> list = game.getPlayerList();
 					list.remove(t1);
 					list.remove(t2);
 					list.put(t1, GameTeam.Team1Leader);
 					list.put(t2, GameTeam.Team2Leader);
 					game.setPlayerList(list);
 					gm.updateGamePlayers(gameid, list);
 					Player l1 = plugin.getServer().getPlayer(t1);
 					Player l2 = plugin.getServer().getPlayer(t2);
 					l1.sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.DARK_GREEN + "You are team 1's leader!");
 					l2.sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.DARK_GREEN + "You are team 2's leader!");
 					gm.sendGameMessage(gameid, ChatColor.DARK_GREEN + l1.getDisplayName() + ChatColor.DARK_GREEN + " and " + l2.getDisplayName() + ChatColor.DARK_GREEN + " are team 1 and team 2 leaders respectively");
 					gm.sendGameMessage(gameid, ChatColor.RED + "Each team now has 1 minute with their leader to discuss battle plans," + ChatColor.BOLD + " the other team can't see your messages");
 				}
 				if (wait == 10 && game.getGameStage() == GameStage.Lobby) {
 					gm.sendGameMessage(gameid, ChatColor.DARK_GREEN + "Waiting for more players...");
 					wait = 0;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
