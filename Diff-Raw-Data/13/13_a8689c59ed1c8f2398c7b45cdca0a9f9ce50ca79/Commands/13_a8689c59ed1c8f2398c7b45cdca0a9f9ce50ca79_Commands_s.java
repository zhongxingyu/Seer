 package com.etriacraft.probending;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import tools.Tools;
 
 public class Commands {
 
 	public static Player clockSender;
 	// Integers
 	public static int startingNumber;
 	public static int currentNumber;
 	public static int clockTask;
 	// Booleans
 	public static Boolean arenainuse;
 	public static Boolean clockRunning = false;
 	public static boolean clockPaused = false;
 	// Strings
 	public static String Prefix;
 	public static String teamAlreadyExists;
 	public static String noBendingType;
 	public static String PlayerAlreadyInTeam;
 	public static String ElementNotAllowed;
 	public static String TeamCreated;
 	public static String noPermission;
 
 	public static String PlayerNotInTeam;
 	public static String MaxSizeReached;
 	public static String TeamAlreadyHasElement;
 	public static String PlayerInviteSent;
 	public static String PlayerInviteReceived;
 	public static String InviteInstructions;
 	public static String NotOwnerOfTeam;
 	public static String PlayerNotOnline;
 
 	public static String TeamDoesNotExist;
 
 	public static String NoInviteFromTeam;
 	public static String PlayerJoinedTeam;
 
 	public static String CantBootFromOwnTeam;
 	public static String PlayerNotOnThisTeam;
 	public static String YouHaveBeenBooted;
 	public static String PlayerHasBeenBooted;
 
 	public static String YouHaveQuit;
 	public static String PlayerHasQuit;
 
 	public static String TeamDisbanded;
 
 	public static String ArenaAlreadyExists;
 	public static String ArenaCreated;
 	public static String ArenaDoesNotExist;
 	public static String ArenaDeleted;
 
 	public static String SpectatorSpawnSet;
 	public static String fieldSpawnSet;
 
 	public static String SentToArena;
 
 	public static String configReloaded;
 
 	public static String NotEnoughMoney;
 	public static String MoneyWithdrawn;
 
 	public static String NameTooLong;
 	public static String TeamRenamed;
 	public static String TeamAlreadyNamedThat;
 
 	public static String ChatEnabled;
 	public static String ChatDisabled;
 
 	public static String RoundComplete;
 	public static String OneMinuteRemaining;
 	public static String ClockAlreadyRunning;
 	public static String RoundStarted;
 	public static String ClockPaused;
 	public static String ClockNotRunning;
 	public static String ClockNotPaused;
 	public static String ClockResumed;
 	public static String ClockStopped;
 
 	public static String NoClockPermissions;
 	public static String NoArenaPermissions;
 	public static String NoTeamPermissions;
 
 	public static String WinAddedToTeam;
 	public static String LossAddedToTeam;
 
 	Probending plugin;
 
 	public Commands(Probending plugin) {
 		this.plugin = plugin;
 		init();
 	}
 	//HashMaps
 	public static Set<Player> pbChat = new HashSet<Player>();
 	public static HashMap<String, LinkedList<String>> teamInvites = new HashMap<String, LinkedList<String>>();
 	public static HashMap<String, LinkedList<String>> teamChallenges = new HashMap<String, LinkedList<String>>();
 
 	private void init() {
 		PluginCommand probending = plugin.getCommand("probending");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length == 0) {
 					s.sendMessage("-----§6Probending Commands§f-----");
 					s.sendMessage("§3/probending team§f - View team commands.");
 					s.sendMessage("§3/probending arena§f - View arena commands.");
 					if (s.hasPermission("probending.chat")) {
 						s.sendMessage("§3/probending chat§f - Turn on Probending Chat.");
 					}
 					s.sendMessage("§3/probending clock§f - View all clock commands.");
 					if (s.hasPermission("probending.reload")) {
 						s.sendMessage("§3/probending reload§f - Reload Configuration.");
 					}
 					if (s.hasPermission("probending.import")) {
 						s.sendMessage("§4/probending import§f - Import data into MySQL Database.");
 					}
 					return true;
 				}
 
 				if (args[0].equalsIgnoreCase("import")) {
 					if (!s.hasPermission("probending.import")) {
 						s.sendMessage(Prefix + noPermission);
 						return true;
 					}
 					if (args.length != 1) {
 						s.sendMessage(Prefix + "§cProper Usage: §3/pb import");
 						return true;
 					}
 					if (!Methods.storage.equalsIgnoreCase("mysql")) {
 						s.sendMessage(Prefix + "§cYou don't have MySQL enabled.");
 						return true;
 					}
 					try {
 						if (DBConnection.sql.getConnection().isClosed()) {
 							s.sendMessage(Prefix + "§cThe MySQL Connection is closed.");
 							return true;
 						}
 					} catch (SQLException ex) {
 						ex.printStackTrace();
 					}
 					Methods.importTeams();
 					s.sendMessage(Prefix + "§aData imported to MySQL database.");
 				}
 
 				if (args[0].equalsIgnoreCase("clock")) {
 					if (args.length == 1) {
 						s.sendMessage("-----§6Probending Clock Commands§f-----");
 						if (s.hasPermission("probending.clock.start")) {
 							s.sendMessage("§3/pb clock start [Seconds]");
 						}
 						if (s.hasPermission("probending.clock.pause")) {
 							s.sendMessage("§3/pb clock pause");
 						}
 						if (s.hasPermission("probending.clock.resume")) {
 							s.sendMessage("§3/pb clock resume");
 						}
 						if (s.hasPermission("probending.clock.stop")) {
 							s.sendMessage("§3/pb clock stop");
 						} else {
 							s.sendMessage(Prefix + NoClockPermissions);
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("stop")) {
 						if (!s.hasPermission("probending.clock.stop")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage("§cProper Usage: §3/pb clock stop");
 							return true;
 						}
 						if (!clockRunning) {
 							s.sendMessage(Prefix + ClockNotRunning);
 							return true;
 						}
 
 						clockPaused = false;
 						clockRunning = false;
 						Bukkit.getServer().getScheduler().cancelTask(clockTask);
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (pbChat.contains(player)) {
 								player.sendMessage(Prefix + ClockStopped);
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("resume")) {
 						if (!s.hasPermission("probending.clock.resume")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 
 						if (args.length != 2) {
 							s.sendMessage("§cProper Usage: §3/pb clock resume");
 							return true;
 						}
 						if (!clockRunning) {
 							s.sendMessage(Prefix + ClockNotRunning);
 							return true;
 						}
 						if (!clockPaused) {
 							s.sendMessage(Prefix + ClockNotPaused);
 							return true;
 						}
 						clockPaused = false;
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (pbChat.contains(player)) {
 								player.sendMessage(Prefix + ClockResumed.replace("%seconds", String.valueOf(currentNumber / 20)));
 							}
 						}
 						clockTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 							public void run() {
 								clockRunning = true;
 								currentNumber--;
 								for (Player player: Bukkit.getOnlinePlayers()) {
 									if (pbChat.contains(player)) {
 										if (currentNumber == 1200) {
 											player.sendMessage(Prefix + OneMinuteRemaining);
 										}
 										if (currentNumber == 0) {
 											player.sendMessage(Prefix + RoundComplete);
 											clockRunning = false;
 											Bukkit.getServer().getScheduler().cancelTask(clockTask);
 										}
 									}
 								}
 
 							}
 						}, 0L, 1L);
 					}
 					if (args[1].equalsIgnoreCase("pause")) {
 						if (!s.hasPermission("probending.clock.pause")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage("§cProper Usage: §3/pb clock pause");
 							return true;
 						}
 						if (!clockRunning) {
 							s.sendMessage(Prefix + ClockNotRunning);
 							return true;
 
 						}
 						Bukkit.getServer().getScheduler().cancelTask(clockTask);
 						clockPaused = true;
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (pbChat.contains(player)) {
 								player.sendMessage(Prefix + ClockPaused.replace("%seconds", String.valueOf(currentNumber / 20)));
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("start")) {
 						if (!s.hasPermission("probending.clock.start")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage("§cProper Usage: §3/pb clock start [Seconds]");
 							return true;
 						}
 						if (Bukkit.getServer().getScheduler().isCurrentlyRunning(clockTask)) {
 							s.sendMessage(Prefix + ClockAlreadyRunning);
 							return true;
 						}
 						if (clockRunning) {
 							s.sendMessage(Prefix + ClockAlreadyRunning);
 							return true;
 						}
 						int desiredTime = Integer.parseInt(args[2]);
 						currentNumber = desiredTime * 20;
 						startingNumber = desiredTime * 20;
 						clockSender = (Player) s;
 
 						clockTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 							public void run() {
 								clockRunning = true;
 								currentNumber--;
 								for (Player player: Bukkit.getOnlinePlayers()) {
 									if (pbChat.contains(player)) {
 										if (currentNumber == startingNumber - 1) {
 											player.sendMessage(Prefix + RoundStarted.replace("%seconds", String.valueOf(startingNumber / 20)));
 										}
 										if (currentNumber == 1200) {
 											player.sendMessage(Prefix + OneMinuteRemaining);
 										}
 										if (currentNumber == 0) {
 											player.sendMessage(Prefix + RoundComplete);
 											clockRunning = false;
 											Bukkit.getServer().getScheduler().cancelTask(clockTask);
 										}
 									}
 								}
 
 							}
 						}, 0L, 1L);
 
 					}
 				}
 				if (args[0].equalsIgnoreCase("chat")) {
 					if (!s.hasPermission("probending.chat")) {
 						s.sendMessage(Prefix + noPermission);
 						return true;
 					}
 					if (args.length > 1) {
 						s.sendMessage(Prefix + "§cProper Usage: §3/pb chat");
 						return true;
 					}
 					Player p = (Player) s;
 					if (!pbChat.contains(p)) {
 						pbChat.add(p);
 						s.sendMessage(Prefix + ChatEnabled);
 						return true;
 					}
 					if (pbChat.contains(p)) {
 						pbChat.remove(p);
 						s.sendMessage(Prefix + ChatDisabled);
 						return true;
 					}
 				}
 				if (args[0].equalsIgnoreCase("reload")) {
 					if (!s.hasPermission("probending.reload")) {
 						s.sendMessage(Prefix + noPermission);
 						return true;
 					}
 					plugin.reloadConfig();
 					s.sendMessage(Prefix + configReloaded);
 				}
 				if (args[0].equalsIgnoreCase("arena")) {
 					if (args.length == 1) {
 						s.sendMessage("-----§6Probending Arena Commands§f-----");
 						if (s.hasPermission("probending.arena.create")) {
 							s.sendMessage("§3/pb arena create [Name]§f - Create an arena."); // Done
 						}
 						if (s.hasPermission("probending.arena.delete")) {
 							s.sendMessage("§3/pb arena delete [Name]§f - Delete an arena."); // Done
 						}
 						if (s.hasPermission("probending.arena.setspawn.spectator")) {
 							s.sendMessage("§3/pb arena setspawn [Name] spectator§f - Set spectator spawn."); // Done
 						}
 						if (s.hasPermission("probending.arena.setspawn.field")) {
 							s.sendMessage("§3/pb arena setspawn [Name] field §f- Set field spawn point."); // Done
 						}
 						if (s.hasPermission("probending.arena.spawn")) {
 							s.sendMessage("§3/pb arena spawn [Name] spectator|field§f - Warp to an arena."); // Done
 						} else {
 							s.sendMessage(Prefix + NoArenaPermissions);
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("spawn")) {
 						if (!s.hasPermission("probending.arena.spawn")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						String spawn = null;
 						String arenaName = "Main";
 						if (args.length == 4) {
 							arenaName = args[2];
 							spawn = args[3];
 						}
 						if (!Methods.arenaExists(arenaName)) {
 							s.sendMessage(Prefix + ArenaDoesNotExist);
 							return true;
 						}
 
 						Set<String> arenas = Methods.getArenas();
 						for (String arena: arenas) {
 							if (arena.equalsIgnoreCase(arenaName)) {
 								arenaName = arena;
 							}
 						}
 
 						if (args.length > 4) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb arena spawn [Name] spectator|field");
 							return true;
 						}
 						if (args.length < 4) {
 							spawn = args[2];
 						}
 						if (!spawn.equalsIgnoreCase("spectator") && !spawn.equalsIgnoreCase("field")) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb arena spawn [Name] spectator|field");
 							return true;
 						}
 						Player p = (Player) s;
 						Location loc = null;
 						if (spawn.equalsIgnoreCase("spectator")) {
 							loc = Methods.getSpectatorSpawn(arenaName);
 						}
 						if (spawn.equalsIgnoreCase("field")) {
 							loc = Methods.getFieldSpawn(arenaName);
 						}
 						p.teleport(loc);
 						s.sendMessage(Prefix + SentToArena.replace("%arena", arenaName));
 					}
 					if (args[1].equalsIgnoreCase("setspawn")) {
 						if (args.length < 4) {
 							s.sendMessage("§3/pb arena setspawn [Name] spectator§f - Set spectator spawn.");
 							s.sendMessage("§3/pb arena setspawn [Name] field§f - Set field spawn.");
 							return true;
 						}
 						String arenaName = args[2];
 						if (!Methods.arenaExists(arenaName)) {
 							s.sendMessage(Prefix + ArenaDoesNotExist);
 							return true;
 						}
 						Set<String> arenas = Methods.getArenas();
 						for (String arena: arenas) {
 							if (arena.equalsIgnoreCase(arenaName)) {
 								arenaName = arena;
 							}
 						}
 						if (args[3].equalsIgnoreCase("spectator")) {
 							if (!s.hasPermission("probending.arena.setspawn.spectator")) {
 								s.sendMessage(Prefix + noPermission);
 								return true;
 							}
 							Location specSpawn = ((Player) s).getLocation();
 							Double x = specSpawn.getX();
 							Double y = specSpawn.getY();
 							Double z = specSpawn.getZ();
 							String world = specSpawn.getWorld().getName();
 							Methods.setSpectatorSpawn(arenaName, x,y,z,world);
 							s.sendMessage(Prefix + SpectatorSpawnSet.replace("%arena", arenaName));
 							return true;
 						}
 						if (args[3].equalsIgnoreCase("field")) {
 							if (!s.hasPermission("probending.arena.setspawn.field")) {
 								s.sendMessage(Prefix + noPermission);
 								return true;
 							}
 							Location fieldSpawn = ((Player) s).getLocation();
 							Double x = fieldSpawn.getX();
 							Double y = fieldSpawn.getY();
 							Double z = fieldSpawn.getZ();
 							String world = fieldSpawn.getWorld().getName();
 							Methods.setFieldSpawn(arenaName, x,y,z,world);
 							s.sendMessage(Prefix + fieldSpawnSet.replace("%arena", arenaName));
 							return true;
 						}
 					}
 					if (args[1].equalsIgnoreCase("create")) {
 						if (!s.hasPermission("probending.arena.create")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						String arenaName = "Main";
 						if (args.length == 3) {
 							arenaName = args[2];
 						}
 
 						if (Methods.arenaExists(arenaName)) {
 							s.sendMessage(Prefix + ArenaAlreadyExists.replace("%arena", arenaName));
 							return true;
 						}
 						Set<String> arenas = Methods.getArenas();
 						for (String arena: arenas) {
 							if (arena.equalsIgnoreCase(arenaName)) {
 								arenaName = arena;
 								s.sendMessage(Prefix + ArenaAlreadyExists.replace("%arena", arenaName));
 								return false;
 							}
 						}
 						Methods.createArena(arenaName);
 						s.sendMessage(Prefix + ArenaCreated.replace("%arena", arenaName));
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("delete")) {
 						if (!s.hasPermission("probending.arena.delete")) {
 							s.sendMessage(Prefix + noPermission);
 						}
 						if (args.length != 3) {
 							s.sendMessage("§cProper Usage: §3/pb arena delete [Name]");
 							return true;
 						}
 
 						String arenaName = args[2];
 						if (!Methods.arenaExists(arenaName)) {
 							s.sendMessage(Prefix + ArenaDoesNotExist.replace("%arena", arenaName));
 							return true;
 						}
 						Set<String> arenas = Methods.getArenas();
 						for (String arena: arenas) {
 							if (arena.equalsIgnoreCase(arenaName)) {
 								arenaName = arena;
 							}
 						}
 
 						Methods.deleteArena(arenaName);
 						s.sendMessage(Prefix + ArenaDeleted.replace("%arena", arenaName));
 						return true;
 					}
 				}
 				if (args[0].equalsIgnoreCase("team")) {
 					if (args.length == 1) {
 						s.sendMessage("-----§6Probending Team Commands§f-----");
 						if (s.hasPermission("probending.team.create")) {
 							s.sendMessage("§3/pb team create [Name]§f - Create a team."); // Done
 						}
 						if (s.hasPermission("probending.team.rename")) {
 							s.sendMessage("§3/pb team rename [Name]§f - Rename a team.");
 						}
 						if (s.hasPermission("probending.team.invite")) {
 							s.sendMessage("§3/pb team invite [Player]§f - Invite a player to a team."); // Done
 						}
 						if (s.hasPermission("probending.team.info")) {
 							s.sendMessage("§3/pb team info <Name>§f - View info on a team."); // Done
 						}
 						if (s.hasPermission("probending.team.join")) {
 							s.sendMessage("§3/pb team join <Name>§f - Join a team."); // Done
 						}
 						if (s.hasPermission("probending.team.kick")) {
 							s.sendMessage("§3/pb team kick <Name>§f - Kick a player from your team."); // Done
 						}
 						if (s.hasPermission("probending.team.quit")) {
 							s.sendMessage("§3/pb team quit §f- Quit your current team."); // Done
 						}
 						if (s.hasPermission("probending.team.disband")) {
 							s.sendMessage("§3/pb team disband §f- Disband your team."); // Done
 						}
 						if (s.hasPermission("probending.team.list")) {
 							s.sendMessage("§3/pb team list§f - List all teams.");
 						} if (s.hasPermission("probending.team.addwin")) {
 							s.sendMessage("§3/pb team addwin [Team]§f - Adds a win to a team.");
 						} if (s.hasPermission("probending.team.addloss")) {
 							s.sendMessage("§3/pb team addloss [Team]§f - Adds a loss to a team.");
 						} else {
 							s.sendMessage(Prefix + NoTeamPermissions);
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("addwin")) {
 						if (!s.hasPermission("probending.team.addwin")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb team addwin [Team]");
 							return true;
 						}
 						String teamName = args[2];
 						Set<String> teams = Methods.getTeams();
 						if (!Methods.teamExists(teamName)) {
 							s.sendMessage(Prefix + TeamDoesNotExist);
 							return true;
 						}
 						if (teams != null) {
 							for (String team: teams) {
 								if (team.equalsIgnoreCase(teamName)) {
 									Methods.addWin(team);
 									s.sendMessage(Prefix + WinAddedToTeam.replace("%team", team));
 								}
 							}
 						}
 						return true;	
 					}
 					if (args[1].equalsIgnoreCase("addloss")) {
 						if (!s.hasPermission("probending.team.addloss")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage("§cProper Usage: §3/pb team addloss [Team]");
 							return true;
 						}
 						String teamName = args[2];
 						Set<String> teams = Methods.getTeams();
 						if (!Methods.teamExists(teamName)) {
 							s.sendMessage(Prefix + TeamDoesNotExist);
 							return true;
 						}
 						if (teams != null) {
 							for (String team: teams) {
 								if (team.equalsIgnoreCase(teamName)) {
 									Methods.addLoss(team);
 									s.sendMessage(Prefix + LossAddedToTeam.replace("%team", team));
 								}
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("rename")) {
 						if (!s.hasPermission("probending.team.rename")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (!Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Prefix + PlayerNotInTeam);
 							return true;
 						}
 						if (!Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Prefix + NotOwnerOfTeam);
 							return true;
 						}
 						if (args.length < 3) {
 							s.sendMessage(Prefix + "§cProper Usage §3/pb team rename [Name]");
 							return true;
 						}
 						boolean econEnabled = plugin.getConfig().getBoolean("Economy.Enabled");
 
 						String newName = args[2];
 						if (newName.length() > 15) {
 							s.sendMessage(Prefix + NameTooLong);
 							return true;
 						}
 						if (newName.equalsIgnoreCase(teamName)) {
 							s.sendMessage(Prefix + TeamAlreadyNamedThat.replace("%newname", teamName));
 							return true;
 						}
 						if (econEnabled) {
 							Double playerBalance = Probending.econ.getBalance(s.getName());
 							Double renameFee = plugin.getConfig().getDouble("Economy.TeamRenameFee");
 							String serverAccount = plugin.getConfig().getString("Economy.ServerAccount");
 							String currency = Probending.econ.currencyNamePlural();
 							if (playerBalance < renameFee) {
 								s.sendMessage(Prefix + NotEnoughMoney.replace("%amount", renameFee.toString()).replace("%currency", currency));
 								return true;
 							}
 							Probending.econ.withdrawPlayer(s.getName(), renameFee);
 							Probending.econ.depositPlayer(serverAccount, renameFee);
 							s.sendMessage(Prefix + MoneyWithdrawn.replace("%amount", renameFee.toString()).replace("%currency", currency));
 						}
 						Methods.createTeam(newName, s.getName());
 						String airbender = Methods.getTeamAirbender(teamName);
 						String waterbender = Methods.getTeamWaterbender(teamName);
 						String earthbender = Methods.getTeamEarthbender(teamName);
 						String firebender = Methods.getTeamFirebender(teamName);
 						String chiblocker = Methods.getTeamChiblocker(teamName);
 
 						if (airbender != null) {
 							Methods.removePlayerFromTeam(teamName, airbender, "Air");
 							Methods.addPlayerToTeam(newName, airbender, "Air");
 						}
 						if (waterbender != null) {
 							Methods.removePlayerFromTeam(teamName, waterbender, "Water");
 							Methods.addPlayerToTeam(newName, waterbender, "Water");
 						}
 						if (earthbender != null) {
 							Methods.removePlayerFromTeam(teamName, earthbender, "Earth");
 							Methods.addPlayerToTeam(newName, earthbender, "Earth");
 						}
 						if (firebender != null) {
 							Methods.removePlayerFromTeam(teamName, firebender, "Fire");
 							Methods.addPlayerToTeam(newName, firebender, "Fire");
 						}
 						if (chiblocker != null) {
 							Methods.removePlayerFromTeam(teamName, chiblocker, "Chi");
 							Methods.addPlayerToTeam(newName, chiblocker, "Chi");
 						}
 
 						s.sendMessage(Prefix + TeamRenamed.replace("%newname", newName));
 						Methods.setOwner(s.getName(), newName);
 						Methods.deleteTeam(teamName);
 						plugin.saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("list")) {
 						if (!s.hasPermission("probending.team.list")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						Set<String> teams = Methods.getTeams();
 						s.sendMessage("§cTeams: §a" + teams.toString());
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("disband")) {
 						if (!s.hasPermission("probending.team.disband")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb team disband");
 							return true;
 						}
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (!Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Prefix + NotOwnerOfTeam);
 							return true;
 						}
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (Methods.getPlayerTeam(player.getName()) == null) continue;
 							if (Methods.getPlayerTeam(player.getName()).equals(teamName)) {
 								s.sendMessage(Prefix + TeamDisbanded.replace("%team", teamName));
 							}
 						}
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						Methods.removePlayerFromTeam(teamName, s.getName(), playerElement);
 						Set<String> teamelements = Methods.getTeamElements(teamName);
 						if (teamelements != null) {
 							if (teamelements.contains("Air")) {
 								Methods.removePlayerFromTeam(teamName, Methods.getTeamAirbender(teamName), "Air");
 							}
 							if (teamelements.contains("Water")) {
 								Methods.removePlayerFromTeam(teamName, Methods.getTeamWaterbender(teamName), "Water");
 							}
 							if (teamelements.contains("Earth")) {
 								Methods.removePlayerFromTeam(teamName, Methods.getTeamEarthbender(teamName), "Earth");
 							}
 							if (teamelements.contains("Fire")) {
 								Methods.removePlayerFromTeam(teamName, Methods.getTeamFirebender(teamName), "Fire");
 							}
 							if (teamelements.contains("Chi")) {
 								Methods.removePlayerFromTeam(teamName, Methods.getTeamChiblocker(teamName), "Chi");
 							}
 						}
 						Methods.deleteTeam(teamName);
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("quit")) {
 						if (!s.hasPermission("probending.team.quit")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb team quit");
 							return true;
 						}
 
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (teamName == null) {
 							s.sendMessage(Prefix + PlayerNotInTeam);
 							return true;
 						}
 						if (Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Prefix + CantBootFromOwnTeam);
 							return true;
 						}
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						Methods.removePlayerFromTeam(teamName, s.getName(), playerElement);
 						s.sendMessage(Prefix + YouHaveQuit.replace("%team", teamName));
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (Methods.getPlayerTeam(player.getName()) == null) continue;
 							if (Methods.getPlayerTeam(player.getName()).equals(teamName)) {
 								s.sendMessage(Prefix + PlayerHasQuit.replace("%team", teamName).replace("%player", s.getName()));
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("kick")) {
 						if (!s.hasPermission("probending.team.kick")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb team kick <Name>");
 							return true;
 						}
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (teamName == null) {
 							s.sendMessage(Prefix + PlayerNotInTeam);
 							return true;
 						}
 						if (!Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Prefix + NotOwnerOfTeam);
 							return true;
 						}
 						String playerName = args[2];
 						if (playerName.equals(s.getName())) {
 							s.sendMessage(Prefix + CantBootFromOwnTeam);
 							return true;
 						}
 						Player p3 = Bukkit.getPlayer(args[2]);
 						String playerTeam = null;
 
 						String playerElement = null;
 						if (p3 != null) {
 							if (p3.isOnline()) {
 								playerElement = Methods.getPlayerElementInTeam(p3.getName(), teamName);
 								playerTeam = Methods.getPlayerTeam(p3.getName());
 							}
 						} else {
 							playerElement = Methods.getPlayerElementInTeam(playerName, teamName);
 							playerTeam = Methods.getPlayerTeam(playerName);
 						}
 
 						if (playerTeam == null) {
 							s.sendMessage(Prefix + PlayerNotOnThisTeam);
 							return true;
 						}
 						if (!playerTeam.equals(teamName)) {
 							s.sendMessage(Prefix + PlayerNotOnThisTeam);
 							return true;
 						}
 						Methods.removePlayerFromTeam(teamName, playerName, playerElement);
 						Player player = Bukkit.getPlayer(playerName);
 						if (player != null) {
 							player.sendMessage(Prefix + YouHaveBeenBooted.replace("%team", teamName));
 						}
 						for (Player player2: Bukkit.getOnlinePlayers()) {
 							if (Methods.getPlayerTeam(player2.getName()) == null) continue;
 							if (Methods.getPlayerTeam(player2.getName()).equals(teamName)) {
 								player2.sendMessage(Prefix + PlayerHasBeenBooted.replace("%player", playerName).replace("%team", teamName));
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("join")) {
 						if (!s.hasPermission("probending.team.join")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 
 						if (args.length != 3) {
 							s.sendMessage("§cProper Usage: §3/pb team join [TeamName]");
 							return true;
 						}
 						if (Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Prefix + PlayerAlreadyInTeam);
 							return true;
 						}
 						String teamName = args[2];
 						if (teamInvites.get(s.getName()) == null) {
 							s.sendMessage(Prefix + NoInviteFromTeam);
 							return true;
 						}
 						if (!teamInvites.get(s.getName()).contains(teamName)) {
 							s.sendMessage(Prefix + NoInviteFromTeam);
 							return true;
 						}
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						if (playerElement == null) {
 							s.sendMessage(Prefix + noBendingType);
 							return true;
 						}
 						Set<String> teamelements = Methods.getTeamElements(teamName);
 						if (teamelements != null) {
 							if (teamelements.contains(playerElement)) {
 								s.sendMessage(Prefix + TeamAlreadyHasElement);
 								return true;
 							}
 							if (!plugin.getConfig().getBoolean("TeamSettings.Allow" + playerElement)) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", playerElement));
 								return true;
 							}
 							Methods.addPlayerToTeam(teamName, s.getName(), playerElement);
 							for (Player player: Bukkit.getOnlinePlayers()) {
								if (Methods.getPlayerTeam(player.getName()).equals(teamName)) {
									player.sendMessage(Prefix + PlayerJoinedTeam.replace("%player", player.getName()).replace("%team", teamName));
 								}
 							}
 						}
 						teamInvites.remove(s.getName());
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("info")) {
 						if (!s.hasPermission("probending.team.info")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						String teamName = null;
 						if (args.length == 2) {
 							teamName = Methods.getPlayerTeam(s.getName());
 						}
 						if (args.length == 3) {
 							teamName = args[2];
 						}
 
 						if (!Methods.teamExists(teamName)) {
 							s.sendMessage(Prefix + TeamDoesNotExist);
 							return true;
 						}
 
 						Set<String> teams = Methods.getTeams();
 						for (String team: teams) {
 							if (team.equalsIgnoreCase(teamName)) {
 								teamName = team;
 							}
 						}
 						String teamOwner = Methods.getOwner(teamName);
 						s.sendMessage("§3Team Name:§e " + teamName);
 						s.sendMessage("§3Team Owner:§5 " + teamOwner);
 
 						String air = Methods.getTeamAirbender(teamName);
 						String water = Methods.getTeamWaterbender(teamName);
 						String earth = Methods.getTeamEarthbender(teamName);
 						String fire = Methods.getTeamFirebender(teamName);
 						String chi = Methods.getTeamChiblocker(teamName);
						
 						int wins = Methods.getWins(teamName);
 						int losses = Methods.getLosses(teamName);
						
 						if (Methods.getAirAllowed()) {
 							if (air != null) {
 								s.sendMessage("§3Airbender: §7" + air);
 							}
 						}
 						if (Methods.getWaterAllowed()) {
 							if (water != null) {
 								s.sendMessage("§3Waterbender: §b" + water);
 							}
 						}
 						if (Methods.getEarthAllowed()) {
 							if (earth != null) {
 								s.sendMessage("§3Earthbender: §a" + earth);
 							}
 						}
 						if (Methods.getFireAllowed()) {
 							if (fire != null) {
 								s.sendMessage("§3Firebender: §c" + fire);
 							}
 						}
 						if (Methods.getChiAllowed()) {
 							if (chi != null) {
 								s.sendMessage("§3Chiblocker: §6" + chi);
 							}
 						}
 						s.sendMessage("§3Wins: §e" + wins);
 						s.sendMessage("§3Losses: §e" + losses);
 
 					}
 					if (args[1].equalsIgnoreCase("invite")) {
 						if (args.length != 3) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb team invite [Name]");
 							return true;
 						}
 						if (!s.hasPermission("probending.team.invite")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 
 						if (!Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Prefix + PlayerNotInTeam);
 							return true;
 						}
 
 						String playerTeam = Methods.getPlayerTeam(s.getName());
 						if (!Methods.isPlayerOwner(s.getName(), playerTeam)) {
 							s.sendMessage(Prefix + NotOwnerOfTeam);
 							return true;
 						}
 
 						Player player = Bukkit.getPlayer(args[2]);
 
 						if (player == null) {
 							s.sendMessage(Prefix + PlayerNotOnline);
 							return true;
 						}
 
 						if (Methods.playerInTeam(player.getName())) {
 							s.sendMessage(Prefix + PlayerAlreadyInTeam);
 							return true;
 						}
 
 						if (!teamInvites.containsKey(player.getName())) {
 							teamInvites.put(player.getName(), new LinkedList<String>());
 						}
 
 						int maxSize = plugin.getConfig().getInt("TeamSettings.MaxTeamSize");
 						if (Methods.getTeamSize(playerTeam) >= maxSize) {
 							s.sendMessage(Prefix + MaxSizeReached);
 							return true;
 						}
 						String playerElement = Methods.getPlayerElementAsString(player.getName());
 
 						if (playerElement == null) {
 							s.sendMessage(Prefix + Commands.noBendingType);
 							return true;
 						}
 						if (!Methods.getAirAllowed()) {
 							if (playerElement.equals("Air")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Airbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getWaterAllowed()) {
 							if (playerElement.equals("Water")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Waterbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getEarthAllowed()) {
 							if (playerElement.equals("Earth")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Earthbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getFireAllowed()) {
 							if (playerElement.equals("Fire")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Firebenders"));
 								return true;
 							}
 						}
 						if (!Methods.getChiAllowed()) {
 							if (playerElement.equals("Chi")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Chiblockers"));
 								return true;
 							}
 						}
 						Set<String> teamelements = Methods.getTeamElements(playerTeam);
 						if (teamelements != null) {
 							if (teamelements.contains(playerElement)) {
 								s.sendMessage(Prefix + TeamAlreadyHasElement);
 								return true;
 							}
 						}
 
 						teamInvites.get(player.getName()).add(playerTeam);
 						s.sendMessage(Prefix + PlayerInviteSent.replace("%team", playerTeam).replace("%player", player.getName()));
 						player.sendMessage(Prefix + PlayerInviteReceived.replace("%team", playerTeam).replace("%player", player.getName()));
 						player.sendMessage(Prefix + InviteInstructions.replace("%team", playerTeam).replace("%player", player.getName()));
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("create")) {
 						if (args.length != 3) {
 							s.sendMessage(Prefix + "§cProper Usage: §3/pb team create [Name]");
 							return true;
 						}
 						if (!s.hasPermission("probending.team.create")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						String teamName = args[2];
 						if (Methods.teamExists(teamName)) {
 							s.sendMessage(Prefix + teamAlreadyExists);
 							return true;
 						}
 
 						if (teamName.length() > 15) {
 							s.sendMessage(Prefix + NameTooLong);
 							return true;
 						}
 
 						if (!Tools.isBender(s.getName())) {
 							s.sendMessage(Prefix + noBendingType);
 							return true;
 						}
 
 						if (Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Prefix + PlayerAlreadyInTeam);
 							return true;
 						}
 						Double creationCost = plugin.getConfig().getDouble("Economy.TeamCreationFee");
 						String serverAccount = plugin.getConfig().getString("Economy.ServerAccount");
 						boolean econEnabled = plugin.getConfig().getBoolean("Economy.Enabled");
 
 
 						if (econEnabled) {
 							String currencyName = Probending.econ.currencyNamePlural();
 							Double playerBalance = Probending.econ.getBalance(s.getName());
 							if (playerBalance < creationCost) {
 								s.sendMessage(Prefix + NotEnoughMoney.replace("%currency", currencyName));
 								return true;
 							}
 							Probending.econ.withdrawPlayer(s.getName(), creationCost);
 							Probending.econ.depositPlayer(serverAccount, creationCost);
 							s.sendMessage(Prefix + MoneyWithdrawn.replace("%amount", creationCost.toString()).replace("%currency", currencyName));
 						}
 
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						if (!Methods.getAirAllowed()) {
 							if (playerElement.equals("Air")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Airbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getWaterAllowed()) {
 							if (playerElement.equals("Water")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Waterbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getEarthAllowed()) {
 							if (playerElement.equals("Earth")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Earthbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getFireAllowed()) {
 							if (playerElement.equals("Fire")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Firebenders"));
 								return true;
 							}
 						}
 						if (!Methods.getChiAllowed()) {
 							if (playerElement.equals("Chi")) {
 								s.sendMessage(Prefix + ElementNotAllowed.replace("%element", "Chiblockers"));
 								return true;
 							}
 						}
 						Methods.createTeam(teamName, s.getName());
 						Methods.addPlayerToTeam(teamName, s.getName(), playerElement);
 						s.sendMessage(Prefix + TeamCreated.replace("%team", teamName));
 						return true;
 					}
 				}
 				return true;
 			}
 		}; probending.setExecutor(exe);
 	}
 }
