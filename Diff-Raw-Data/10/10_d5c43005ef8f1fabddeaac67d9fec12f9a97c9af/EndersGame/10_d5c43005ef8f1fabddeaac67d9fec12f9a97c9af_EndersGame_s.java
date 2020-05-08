 package com.homie.endersgame;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.homie.endersgame.api.Game;
 import com.homie.endersgame.api.Game.GameTeam;
 import com.homie.endersgame.api.Lobby;
 import com.homie.endersgame.listeners.DebugListener;
 import com.homie.endersgame.listeners.GameListener;
 import com.homie.endersgame.sql.SQL;
 import com.homie.endersgame.sql.options.DatabaseOptions;
 import com.homie.endersgame.sql.options.MySQLOptions;
 import com.homie.endersgame.sql.options.SQLiteOptions;
 
 public class EndersGame extends JavaPlugin {
 
 	private SQL sql;
 	private DatabaseOptions dop;
 	private Config config;
 	private String v;
 	private static boolean debug;
 	
 	private static HashMap<Integer, Game> runningGames = new HashMap<Integer, Game>();
 	private static HashMap<Integer, Lobby> lobbyList = new HashMap<Integer, Lobby>();
 	
 	public static HashMap<String, ItemStack[]> playing_players_inventory = new HashMap<String, ItemStack[]>();
 	public static HashMap<String, ItemStack[]> player_players_armor = new HashMap<String, ItemStack[]>();
 	public static HashMap<String, GameMode> playing_players_gamemode = new HashMap<String, GameMode>();
 	
 	public static ArrayList<String> creating_game_players = new ArrayList<String>();
 	public static HashMap<String, ArrayList<Integer>> creating_game_ids = new HashMap<String, ArrayList<Integer>>();
 	
 	public static ArrayList<String> creating_lobby_players = new ArrayList<String>();
 	public static HashMap<String, Integer> creating_lobby_ids = new HashMap<String, Integer>();
 	
 	public static ArrayList<String> creating_spawns_players = new ArrayList<String>();
 	public static HashMap<String, Location> creating_spawns_ids = new HashMap<String, Location>();
 	
 	public void onEnable() {
 		config = new Config(this);
 		debug = config.getDebug();
 		
 		if (!setupSQL()) {
 			sendErr("SQL could not be setup, EndersGame will disable");
 			getPluginLoader().disablePlugin(this);
 			return;
 		}
 		int a = 0;
 		int l = 0;
 		send("Setting up arenas and lobbies");
 		try {
 			ResultSet ls = sql.query("SELECT lobbyid FROM lobbies");
 			while (ls.next()) {
 				int lobbyid = ls.getInt(1);
 				ResultSet lo = sql.query("SELECT * FROM lobbies WHERE lobbyid=" + ls.getInt(1));
 				ResultSet lospawn = sql.query("SELECT * FROM lobbyspawns WHERE lobbyid=" + ls.getInt(1));
 				lobbyList.put(lobbyid, new Lobby(this, lobbyid,
 						new Location(getServer().getWorld(lo.getString("world")), lo.getInt("x1"), lo.getInt("y1"), lo.getInt("z1")),
 						new Location(getServer().getWorld(lo.getString("world")), lo.getInt("x2"), lo.getInt("y2"), lo.getInt("z2")),
 						new Location(getServer().getWorld(lospawn.getString("world")), lospawn.getInt("coordX"), lospawn.getInt("coordY"), lospawn.getInt("coordZ"))));
 				l++;
 			}
 			ResultSet rs = sql.query("SELECT gameid FROM games");
 			while (rs.next()) {
 				ResultSet ga = sql.query("SELECT * FROM games WHERE gameid=" + rs.getInt(1));
 				int gameid = ga.getInt("gameid");
 				ResultSet si = sql.query("SELECT * FROM signs WHERE gameid=" + gameid);
 				ResultSet gaspawn = sql.query("SELECT * FROM gamespawns WHERE gameid=" + gameid);
 				ArrayList<Location> gamespawns = new ArrayList<Location>();
 				gamespawns.add(new Location(getServer().getWorld(gaspawn.getString("world")), gaspawn.getInt("x1"), gaspawn.getInt("y1"), gaspawn.getInt("z1")));
 				gamespawns.add(new Location(getServer().getWorld(gaspawn.getString("world")), gaspawn.getInt("x2"), gaspawn.getInt("y2"), gaspawn.getInt("z2")));
 				Game game = new Game(this, gameid, lobbyList.get(ga.getInt("lobbyid")),
 						getServer().getWorld(si.getString("world")).getBlockAt(si.getInt("coordX"), si.getInt("coordY"), si.getInt("coordZ")).getLocation(),
 						new Location(getServer().getWorld(ga.getString("world")), ga.getInt("x1"), ga.getInt("y1"), ga.getInt("z1")),
 						new Location(getServer().getWorld(ga.getString("world")), ga.getInt("x2"), ga.getInt("y2"), ga.getInt("z2")), gamespawns);
 				runningGames.put(gameid, game);
 				getServer().getScheduler().scheduleSyncRepeatingTask(this, game, 20L, 20L);
 				a++;
 			}
 		} catch (SQLException e) {
 			sendErr("Games could not be setup, plugin is disabling");
 			getServer().getPluginManager().disablePlugin(this);
 			e.printStackTrace();
 			return;
 		}
 		send("Sucessfully setup " + a + " arenas and " + l + " lobbies");
 		
 		getServer().getPluginManager().registerEvents(new GameListener(this), this);
 		if (debug) getServer().getPluginManager().registerEvents(new DebugListener(), this);
 		send("Checking for new version(s)");
 		update();
 	}
 	
 	public void onDisable() {
 		send("Shutting arenas down");
 		for (Map.Entry<Integer, Game> en : runningGames.entrySet()) {
 			en.getValue().shutdown();
 		}
 		send("Attempting to close SQL connection");
 		try {
 			sql.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean update() {
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(new URL("https://docs.google.com/uc?export=download&id=0BynZyjWQxP7XQlRJY19RQnM1RnM").openStream()));
 			v = br.readLine();
 			br.close();
 			if (!getDescription().getVersion().equalsIgnoreCase(v)) {
 				send("!!!!!~~~~~NEW VERSION FOR ENDERS GAME~~~~~!!!!!");
 				send("Current version: " + getDescription().getVersion() + ", New version: " + v);
 				send("Attempting to download..");
 				
 				BufferedInputStream in = new BufferedInputStream(new URL("https://docs.google.com/uc?export=download&id=0BynZyjWQxP7XdjdDcGE5U1BHc0k").openStream());
 				FileOutputStream fos = new FileOutputStream(new File("plugins/EndersGame.jar"));
 				byte d[] = new byte[1024];
 				int count;
 				while ((count = in.read(d, 0, 1024)) != -1) {
 					fos.write(d, 0, count);
 				}
 				in.close();
 				fos.close();
 				send("Successfully updated EndersGame. Reload or restart to see changes");
 				return true;
 			}
 			return false;
 		} catch (IOException e) {
 			sendErr("Failed to download update or check for update");
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public HashMap<Integer, Game> getRunningGames() {
 		return runningGames;
 	}
 	
 	public void addRunner(Game game) {
 		runningGames.put(game.getGameId(), game);
 	}
 	
 	public void removeRunner(int gameid) {
 		runningGames.remove(gameid);
 	}
 	
 	public HashMap<Integer, Lobby> getLobbyList() {
 		return lobbyList;
 	}
 	
 	public void addLobby(Lobby lobby) {
 		lobbyList.put(lobby.getLobbyId(), lobby);
 	}
 	
 	public void removeLobby(int lobbyid) {
 		lobbyList.remove(lobbyid);
 	}
 	
 	private boolean setupSQL() {
 		if (config.getSQLValue().equalsIgnoreCase("MySQL")) {
 			dop = new MySQLOptions(config.getHostname(), config.getPort(), config.getDatabase(), config.getUsername(), config.getPassword());
 		}
 		
 		else if (config.getSQLValue().equalsIgnoreCase("SQLite")) {
 			dop = new SQLiteOptions(new File(getDataFolder() + "/game_data.db"));
 		} else {
 			sendErr("Enders Game cannot enable because the SQL is set to " + config.getSQLValue());
 			return false;
 		}
 		
 		sql = new SQL(this, dop);
 		try {
 			if (!sql.open()) return false;
 			sql.createTable("CREATE TABLE IF NOT EXISTS signs (gameid INT(10), coordX INT(15), coordY INT(15), coordZ INT(15), world VARCHAR(255))");
 			sql.createTable("CREATE TABLE IF NOT EXISTS games (gameid INT(10), lobbyid INT(10), x1 INT(15), y1 INT(15), z1 INT(15), x2 INT(15), y2 INT(15), z2 INT(15), world VARCHAR(255), gamestage VARCHAR(50))");
 			sql.createTable("CREATE TABLE IF NOT EXISTS lobbies (lobbyid INT(10), x1 INT(15), y1 INT(15), z1 INT(15), x2 INT(15), y2 INT(15), z2 INT(15), world VARCHAR(255))");
 			sql.createTable("CREATE TABLE IF NOT EXISTS gamespawns (gameid INT(10), x1 INT(15), y1 INT(15), z1 INT(15), x2 INT(15), y2 INT(15), z2 INT(15), world VARCHAR(255))");
 			sql.createTable("CREATE TABLE IF NOT EXISTS lobbyspawns (lobbyid INT(10), coordX INT(15), coordY INT(15), coordZ INT(15), world VARCHAR(255))");
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public SQL getSQL() {
 		return sql;
 	}
 	
 	public Config getEnderConfig() {
 		return config;
 	}
 	
 	public boolean debug() {
 		return debug;
 	}
 	
 	public Config getConfiguration() {
 		return config;
 	}
 	
 	public static void send(String message) {
 		System.out.println("[EndersGame] " + message);
 	}
 	
 	public static void sendErr(String error) {
 		System.out.println("[EndersGame] [Error] " + error);
 	}
 	
 	public static void debug(String message) {
 		if (debug) System.out.println("[EndersGame] [Debug] " + message);
 	}
 	
 	private void help(CommandSender s) {
 		s.sendMessage(ChatColor.RED + "Ender's Game Commands You Can Use");
 		if (s.hasPermission("EndersGame.join")) s.sendMessage(ChatColor.GOLD + "/eg join [arenaID]");
 		if (s.hasPermission("EndersGame.join")) s.sendMessage(ChatColor.GOLD + "/eg leave");
 		if (s.hasPermission("EndersGame.list")) s.sendMessage(ChatColor.GOLD + "/eg list");
 		if (s.hasPermission("EndersGame.create")) s.sendMessage(ChatColor.GOLD + "/eg create arena [id] [lobbyID]");
 		if (s.hasPermission("EndersGame.create")) s.sendMessage(ChatColor.GOLD + "/eg create lobby [id]");
 		if (s.hasPermission("EndersGame.create")) s.sendMessage(ChatColor.GOLD + "/eg setspawn [arena|lobby] [id]");
 		if (s.hasPermission("EndersGame.create")) s.sendMessage(ChatColor.GOLD + "/eg cancel");
 		if (s.hasPermission("EndersGame.delete")) s.sendMessage(ChatColor.GOLD + "/eg delete [arena|lobby] [id]");
 		if (s.hasPermission("EndersGame.override")) s.sendMessage(ChatColor.GOLD + "/eg ejectall - Ejects all players from all arenas");
 	}
 	
 	public HashMap<String, GameTeam> convertPlayerListToHash(String args) {
 		HashMap<String, GameTeam> pl = new HashMap<String, GameTeam>();
 		if (args == null || args.length() < 1) return pl;
 		String[] sp = args.split(",");
 		for (int i = 0; i < sp.length; i++) {
 			String a = String.valueOf(sp[i].split("#")[0]);
 			GameTeam b = GameTeam.getFrom(sp[i].split("#")[1]);
 			pl.put(a, b);
 		}
 		return pl;
 	}
 	
 	public String convertPlayerListToString(HashMap<String, GameTeam> hash) {
 		if (hash == null || hash.isEmpty() || hash.size() < 1) return "";
 		StringBuilder sb = new StringBuilder();
 		for (Map.Entry<String, GameTeam> en : hash.entrySet()) {
 			sb.append(en.getKey()+"#"+en.getValue().toString()+",");
 		}
 		sb.deleteCharAt(sb.length()-1);
 		return sb.toString();
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (args.length == 0) {
 			help(sender);
 			return true;
 		}
 		
 		if (args[0].equalsIgnoreCase("create")) {
 			if (sender.hasPermission("EndersGame.create")) {
 				if (sender instanceof Player) {
 					Player player = (Player) sender;
 					if (creating_game_players.contains(player.getName()) || creating_lobby_players.contains(player.getName())) {
 						player.sendMessage(ChatColor.RED + "You are already creating, to cancel, use /eg cancel");
 						return true;
 					}
 					if (args.length >= 3) {
 						int a = -1;
 						int b = -1;
 						try {
 							a = Integer.parseInt(args[2]);
 							if (args[1].equalsIgnoreCase("arena")) {
 								if (args.length == 4) {
 									b = Integer.parseInt(args[3]);
 								} else {
 									player.sendMessage(ChatColor.GOLD + "/eg create arena [id] [lobbyID]");
 									return true;
 								}
 							}
 						} catch (NumberFormatException e) {
 							player.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " and/or " + ChatColor.GOLD + args[3] + ChatColor.RED + " are not numbers");
 							return true;
 						}
 						if (args[1].equalsIgnoreCase("arena")) {
 							ArrayList<Integer> j = new ArrayList<Integer>();
 							j.add(a);
 							j.add(b);
 							creating_game_players.add(player.getName());
 							creating_game_ids.put(player.getName(), j);
 							Inventory i = player.getInventory();
 							if (i.firstEmpty() == -1) {
 								player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.WOOD_SPADE));
 								player.sendMessage(ChatColor.GREEN + "A wooden spade has been dropped at your location because your inventory is full");
 								player.sendMessage(ChatColor.GOLD + "Right-click at position 1 of the arena with the wooden spade");
 								return true;
 							} else {
 								i.addItem(new ItemStack(Material.WOOD_SPADE));
 								player.sendMessage(ChatColor.GREEN + "A wooden spade has been placed in your inventory");
 								player.sendMessage(ChatColor.GOLD + "Right-click at position 1 of the arena with the wooden spade");
 								return true;
 							}
 						}
 						else if (args[1].equalsIgnoreCase("lobby")) {
 							if (args.length != 3) {
 								player.sendMessage(ChatColor.GOLD + "/eg create lobby [id]");
 								return true;
 							}
 							creating_lobby_players.add(player.getName());
 							creating_lobby_ids.put(player.getName(), a);
 							Inventory i = player.getInventory();
 							if (i.firstEmpty() == -1) {
 								player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.WOOD_SPADE));
 								player.sendMessage(ChatColor.GREEN + "A wooden spade has been dropped at your location because your inventory is full");
 								player.sendMessage(ChatColor.GOLD + "Right-click at position 1 of the lobby with the wooden spade");
 								return true;
 							} else {
 								i.addItem(new ItemStack(Material.WOOD_SPADE));
 								player.sendMessage(ChatColor.GREEN + "A wooden spade has been placed in your inventory");
 								player.sendMessage(ChatColor.GOLD + "Right-click at position 1 of the lobby with the wooden spade");
 								return true;
 							}
 						}
 					} else {
 						player.sendMessage(ChatColor.GOLD + "/eg create arena [id] [lobbyID]");
 						player.sendMessage(ChatColor.GOLD + "/eg create lobby [id]");
 						return true;
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You must be a player to create a game");
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.create)");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("join")) {
 			if (sender instanceof Player) {
 				Player player = (Player) sender;
 				if (player.hasPermission("EndersGame.join")) {
 					if (args.length == 2) {
 						int x;
 						try {
 							x = Integer.parseInt(args[1]);
 							Game game = runningGames.get(x);
 							if (game != null) {
 								game.addPlayer(player);
 								return true;
 							} else {
 								player.sendMessage(ChatColor.RED + "That arena doesn't exist");
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							player.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not a number");
 							return true;
 						}
 					} else {
 						player.sendMessage(ChatColor.GOLD + "/eg join [arenaID]");
 						return true;
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.join)");
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You must be a player to join an arena");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("leave")) {
 			if (sender instanceof Player) {
 				Player player = (Player) sender;
 				for (Map.Entry<Integer, Game> en : runningGames.entrySet()) {
 					if (en.getValue().getArrayListofPlayers().contains(player.getName())) {
 						en.getValue().ejectPlayer(player, true);
 						return true;
 					}
 				}
 				player.sendMessage(ChatColor.RED + "You aren't in any games");
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "You have to be a player to leave a game");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("list")) {
 			if (sender.hasPermission("EndersGame.list")) {
 				try {
 					ArrayList<Integer> gamelist = Game.getAllGamesFromDatabase(sql);
 					ArrayList<Integer> lobbylist = Lobby.getAllLobbiesFromDatabase(sql);
 					sender.sendMessage(ChatColor.GOLD + "Game IDs: " + ChatColor.GREEN + gamelist.toString());
 					sender.sendMessage(ChatColor.GOLD + "Lobby IDs: " + ChatColor.GREEN + lobbylist.toString());
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.list)");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("ejectall")) {
 			if (sender.hasPermission("EndersGame.override")) {
 				for (Map.Entry<Integer, Game> en : runningGames.entrySet()) {
 					en.getValue().ejectAllPlayers(false);
 				}
 				sender.sendMessage(ChatColor.RED + "All players ejected");
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.override)");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("setspawn")) {
 			if (sender.hasPermission("EndersGame.create")) {
 				if (sender instanceof Player) {
 					Player player = (Player) sender;
 					String name = player.getName();
 					if (args.length == 3) {
 						int x;
 						try {
 							x = Integer.parseInt(args[2]);
 						} catch (NumberFormatException e) {
 							player.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not a number");
 							return true;
 						}
 						
 						if (args[1].equalsIgnoreCase("lobby")) {
 							try {
 								Lobby lobby = lobbyList.get(x);
 								if (lobby != null) {
 									lobby.setSpawn(player.getLocation());
 									player.sendMessage(ChatColor.GREEN + "Lobby spawn point set at your location");
 									return true;
 								} else {
 									player.sendMessage(ChatColor.RED + "That lobby doesn't exist");
 									return true;
 								}
 							} catch (SQLException e) {
 								player.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 								EndersGame.sendErr("SQLException while trying to set the spawn point for a lobby, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 								e.printStackTrace();
 								return true;
 							}
 						}
 						
 						else if (args[1].equalsIgnoreCase("arena")) {
 							if (creating_spawns_players.contains(name)) {
 								Location y = creating_spawns_ids.get(name);
 								Location a = player.getLocation();
 								creating_spawns_players.remove(name);
 								creating_spawns_ids.remove(name);
 								ArrayList<Location> spawns = new ArrayList<Location>();
 								spawns.add(y);
 								spawns.add(a);
 								try {
 									Game game = runningGames.get(x);
 									if (game != null) {
 										game.setGameSpawns(spawns);
 										player.sendMessage(ChatColor.GREEN + "Team two spawn location set. You can now create a sign to join this arena");
 										return true;
 									} else {
 										player.sendMessage(ChatColor.RED + "That arena doesn't exist");
 										return true;
 									}
 								} catch (SQLException e) {
 									player.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 									EndersGame.sendErr("SQLException while trying to set a games spawn locations, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 									e.printStackTrace();
 									return true;
 								}
 							} else {
 								creating_spawns_players.add(name);
 								creating_spawns_ids.put(name, player.getLocation());
 								player.sendMessage(ChatColor.GREEN + "Team one spawn location set. Use the same command to set team 2's spawn. To cancel, use /eg cancel");
 								return true;
 							}
 						} else {
 							player.sendMessage(ChatColor.GOLD + "/eg setspawn [arena|lobby] [id]");
 							return true;
 						}
 					} else {
 						player.sendMessage(ChatColor.GOLD + "/eg setspawn [arena|lobby] [id]");
 						return true;
 					}
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.create)");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("query")) {
 			if (sender.hasPermission("EndersGame.override")) {
 				if (args.length == 1) {
 					sender.sendMessage(ChatColor.RED + "You must enter something to query");
 					return true;
 				}
 				StringBuilder b = new StringBuilder();
 				for (int i = 1; i < args.length; i++) {
 					b.append(args[i]);
 					b.append(" ");
 				}
 				try {
 					sql.query(b.toString());
 					sender.sendMessage(ChatColor.GREEN + "Sent");
 					return true;
 				} catch (SQLException e) {
 					sender.sendMessage(ChatColor.RED + "Failed to send query, error: " + e.getMessage());
 					e.printStackTrace();
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.override)");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("setvalue")) {
 			if (sender.hasPermission("EndersGame.override")) {
 				if (args.length == 3) {
 					if (args[1].equalsIgnoreCase("minpercenttostart")) {
 						try {
 							config.setMinPercentToStart(Integer.parseInt(args[2]));
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.RED + args[2] + " is not a number");
 							return true;
 						}
 						sender.sendMessage(ChatColor.GREEN + "Value set, allow up to 7 seconds for arenas to update");
 						return true;
 					}
 					else if (args[1].equalsIgnoreCase("percentinspawntowin")) {
 						try {
 							config.setPercentInSpawnToWin(Integer.parseInt(args[2]));
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.RED + args[2] + " is not a number");
 							return true;
 						}
 						sender.sendMessage(ChatColor.GREEN + "Value set, allow up to 7 seconds for arenas to update");
 						return true;
 					}
 					else if (args[1].equalsIgnoreCase("maxplayers")) {
 						try {
 							config.setMaxPlayers(Integer.parseInt(args[2]));
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.RED + args[2] + " is not a number");
 							return true;
 						}
 						sender.sendMessage(ChatColor.GREEN + "Value set, allow up to 7 seconds for arenas to update");
 						return true;
 					}
 					else if (args[1].equalsIgnoreCase("max-hits")) {
 						try {
 							config.setMaxHits(Integer.parseInt(args[2]));
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.RED + args[2] + " is not a number");
 							return true;
 						}
 						sender.sendMessage(ChatColor.GREEN + "Value set, allow up to 7 seconds for arenas to update");
 						return true;
 					}
 				}
 				sender.sendMessage(ChatColor.GOLD + "/eg setvalue [minpercenttostart|percentinspawntowin|maxplayers]");
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.override)");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("version")) {
 			sender.sendMessage(ChatColor.GOLD + "EndersGame Version: " + ChatColor.BLUE + getDescription().getVersion());
 			sender.sendMessage(ChatColor.GOLD + "Server Version: " + ChatColor.BLUE + getServer().getVersion());
 			return true;
 		}
 		
		else if (args[0].equalsIgnoreCase("update") && sender.hasPermission("update")) {
 			send("Checking for new version(s)");
 			if (update()) {
 				sender.sendMessage(ChatColor.GREEN + "Update successful, reload to see changes");
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "No new update");
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("cancel")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(ChatColor.RED + "You must be a player to cancel");
 				return true;
 			} else {
 				Game.cancelCreatingCommandEventForPlayer((Player) sender, true);
 				return true;
 			}
 		}
 		
 		else if (args[0].equalsIgnoreCase("delete")) {
 			if (sender.hasPermission("EndersGame.delete")) {
 				if (args.length == 3) {
 					if (args[1].equalsIgnoreCase("arena")) {
 						try {
 							Game game = runningGames.get(Integer.parseInt(args[2]));
 							if (game != null) {
 								game.removeFromDatabase();
 								sender.sendMessage(ChatColor.GREEN + "Unregistered arena with the database, ID: " + args[2]);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.RED + "That arena doesn't exist");
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not a number");
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 							sendErr("SQLException while trying to delete a game, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 							e.printStackTrace();
 							return true;
 						}
 					}
 					
 					else if (args[1].equalsIgnoreCase("lobby")) {
 						try {
 							Lobby lobby = lobbyList.get(Integer.parseInt(args[2]));
 							if (lobby != null) {
 								lobby.removeFromDatabase();
 								sender.sendMessage(ChatColor.GREEN + "Unregistered lobby with the database, ID: " + args[2]);
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not a number");
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 							sendErr("SQLException while trying to delete a lobby, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						sender.sendMessage(ChatColor.GOLD + "/eg delete [arena|lobby] [id]");
 						return true;
 					}
 				} else {
 					sender.sendMessage(ChatColor.GOLD + "/eg delete [arena|lobby] [id]");
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.delete)");
 				return true;
 			}
 		}
 		help(sender);
 		return true;
 	}
 }
