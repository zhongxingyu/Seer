 package com.etriacraft.probending;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Color;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import tools.Tools;
 
 public class Commands {
 
 	// Integers
 	public static int startingNumber;
 	public static int currentNumber;
 	public static int clockTask;
 	// Booleans
 	public static Boolean arenainuse;
 	// Strings
 
 
 	Probending plugin;
 
 	public Commands(Probending plugin) {
 		this.plugin = plugin;
 		init();
 	}
 	//HashMaps
 	public static Set<Player> pbChat = new HashSet<Player>();
 	public static HashMap<String, LinkedList<String>> teamInvites = new HashMap<String, LinkedList<String>>();
 	public static HashMap<String, LinkedList<String>> teamChallenges = new HashMap<String, LinkedList<String>>();
 	public static HashMap<Player, ItemStack[]> tmpArmor = new HashMap<Player, ItemStack[]>();
 
 	private void init() {
 		PluginCommand probending = plugin.getCommand("probending");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length == 0) {
 					s.sendMessage("-----§6Probending Commands§f-----");
 					s.sendMessage("§3/probending team§f - View team commands.");
 					s.sendMessage("§3/probending round§f - View round Commands");
 					if (s.hasPermission("probending.chat")) {
 						s.sendMessage("§3/probending chat§f - Turn on Probending Chat.");
 					}
 					if (s.hasPermission("probending.reload")) {
 						s.sendMessage("§3/probending reload§f - Reload Configuration.");
 					}
 					if (s.hasPermission("probending.setspawn")) {
 						s.sendMessage("§3/probending setspawn [TeamOne|TeamTwo]");
 					}
 					if (s.hasPermission("probending.import")) {
 						s.sendMessage("§4/probending import§f - Import data into MySQL Database.");
 					}
 					return true;
 				}
 
 				if (args[0].equalsIgnoreCase("setspawn")) {
 					if (!s.hasPermission("probending.setspawn")) {
 						s.sendMessage(Strings.Prefix + Strings.noPermission);
 						return true;
 					}
 
 					if (args.length != 2) {
 						s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb setspawn [TeamOne|TeamTwo]");
 						return true;
 					}
 
 					if (!args[1].equalsIgnoreCase("teamone") && !args[1].equalsIgnoreCase("teamtwo")) {
 						s.sendMessage(Strings.Prefix + "§cProper Usage:  §3/pb setspawn [TeamOne|TeamTwo]");
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("teamone")) {
 						Methods.setTeamOneSpawn(((Player) s).getLocation());
 						s.sendMessage(Strings.Prefix + Strings.TeamSpawnSet.replace("%team", "TeamOne"));
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("teamtwo")) {
 						Methods.setTeamTwoSpawn(((Player) s).getLocation());
 						s.sendMessage(Strings.Prefix + Strings.TeamSpawnSet.replace("%team", "TeamTwo"));
 						return true;
 					}
 
 				}
 				if (args[0].equalsIgnoreCase("round")) {
 					if (args.length == 1) {
 						s.sendMessage("-----§6Probending Round Commands§f-----");
 						if (s.hasPermission("probending.round.start")) {
 							s.sendMessage("§3/pb round start [Team1] [Team2]§f - Starts Round.");
 						}
 						if (s.hasPermission("probending.round.stop")) {
 							s.sendMessage("§3/pb round stop§f - Stops Round.");
 						}
 						if (s.hasPermission("probending.round.pause")) {
 							s.sendMessage("§3/pb round pause§f - Pauses Round.");
 						}
 						if (s.hasPermission("probending.round.resume")) {
 							s.sendMessage("§3/pb round resume§f - Round Resumed.");
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("resume")) {
 						if (args[1].equalsIgnoreCase("resume")) {
 							if (!s.hasPermission("probending.round.resume")) {
 								s.sendMessage(Strings.Prefix + Strings.noPermission);
 								return true;
 							}
 
 							if (args.length != 2) {
 								s.sendMessage("§cProper Usage: §3/pb round resume");
 								return true;
 							}
 							if (!Methods.matchStarted) {
 								s.sendMessage(Strings.Prefix + Strings.NoOngoingRound);
 								return true;
 							}
 							if (!Methods.matchPaused) {
 								s.sendMessage(Strings.Prefix + Strings.RoundNotPaused);
 								return true;
 							}
 							Methods.matchPaused = false;
 							Methods.sendPBChat(Strings.RoundResumed.replace("%seconds", String.valueOf(currentNumber / 20)));
 
 							clockTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 								public void run() {
 									currentNumber--;
 									if (currentNumber == 1200) {
 										Methods.sendPBChat(Strings.OneMinuteRemaining);
 									}
 									if (currentNumber == 0) {
 										Methods.sendPBChat(Strings.RoundComplete);
 										Methods.matchStarted = false;
 										Bukkit.getServer().getScheduler().cancelTask(clockTask);
 										Methods.restoreArmor();
 									}
 								}
 							}, 0L, 1L);
 						}
 					}
 					if (args[1].equalsIgnoreCase("pause")) {
 						if (!s.hasPermission("probending.round.pause")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage("§cProper Usage: §3/pb round pause");
 							return true;
 						}
 						if (!Methods.matchStarted) {
 							s.sendMessage(Strings.Prefix + Strings.NoOngoingRound);
 							return true;
 						}
 						Bukkit.getServer().getScheduler().cancelTask(clockTask);
 						Methods.matchPaused = true;
 						Methods.sendPBChat(Strings.RoundPaused.replace("%seconds", String.valueOf(currentNumber / 20)));
 					}
 					if (args[1].equalsIgnoreCase("stop")) {
 						// Permissions
 						if (!s.hasPermission("probending.round.stop")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 
 						if (args.length != 2) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb round stop");
 							return true;
 						}
 
 						if (!Methods.matchStarted) {
 							s.sendMessage(Strings.Prefix + Strings.NoOngoingRound);
 							return true;
 						}
 						Methods.restoreArmor();
 
 						Bukkit.getServer().getScheduler().cancelTask(clockTask);
 
 						Methods.matchPaused = false;
 						Methods.playingTeams.clear();
 						Methods.matchStarted = false;
 						Methods.sendPBChat(Strings.RoundStopped);
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("start")) {
 						// Permissions check.
 						if (!s.hasPermission("probending.round.start")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 
 						// Makes sure the command has enough arguments.
 						if (args.length != 4) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb round start [Team1] [Team2]");
 							return true;
 						}
 
 						// Just so we dont start another match if one is already going.
 						if (Methods.matchStarted) {
 							s.sendMessage(Strings.Prefix + Strings.RoundAlreadyGoing);
 							return true;
 						}
 
 						String team1 = args[2]; // Team 1
 						String team2 = args[3]; // Team 2
 
 						// Checks to make sure both teams exist.
 						if (!Methods.teamExists(team1) || !Methods.teamExists(team2)) {
 							s.sendMessage(Strings.Prefix + Strings.TeamDoesNotExist);
 							return true;
 						}
 
 						int minSize = plugin.getConfig().getInt("TeamSettings.MinTeamSize");
 
 						// Checks to make sure the team has enough players.
 						if (Methods.getOnlineTeamSize(team1) < minSize || Methods.getOnlineTeamSize(team2) < minSize) {
 							s.sendMessage(Strings.Prefix + Strings.InvalidTeamSize);
 							return true;
 						}
 						// Add players to list of playing teams and send a message confirming it.
 						Methods.playingTeams.add(team1.toLowerCase());
 						Methods.playingTeams.add(team2.toLowerCase());
 						Methods.TeamOne = team1.toLowerCase();
 						Methods.TeamTwo = team2.toLowerCase();
 
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							String playerTeam = Methods.getPlayerTeam(player.getName());
 							Color teamColor = null;
 							if (playerTeam != null) {
 								if (playerTeam.equalsIgnoreCase(team1)) teamColor = Methods.getColorFromString(plugin.getConfig().getString("TeamSettings.TeamOneColor"));
 								if (playerTeam.equalsIgnoreCase(team2)) teamColor = Methods.getColorFromString(plugin.getConfig().getString("TeamSettings.TeamTwoColor"));
 								if (playerTeam.equalsIgnoreCase(Methods.TeamOne)) {
 									Methods.teamOnePlayers.add(player.getName());
 									player.teleport(Methods.getTeamOneSpawn());
 								}
 								if (playerTeam.equalsIgnoreCase(Methods.TeamTwo)) {
 									player.teleport(Methods.getTeamTwoSpawn());
 									Methods.teamTwoPlayers.add(player.getName());
 								}
 								tmpArmor.put(player, player.getInventory().getArmorContents()); // Backs up their armor.
 								ItemStack armor1 = Methods.createColorArmor(new ItemStack(Material.LEATHER_HELMET), teamColor);
 								ItemStack armor2 = Methods.createColorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), teamColor);
 								ItemStack armor3 = Methods.createColorArmor(new ItemStack(Material.LEATHER_LEGGINGS), teamColor);
 								ItemStack armor4 = Methods.createColorArmor(new ItemStack(Material.LEATHER_BOOTS), teamColor);
 								player.getInventory().setHelmet(armor1);
 								player.getInventory().setChestplate(armor2);
 								player.getInventory().setLeggings(armor3);
 								player.getInventory().setBoots(armor4);
 
 							}
 						}
 
 						int roundTime = plugin.getConfig().getInt("RoundSettings.Time");
 						currentNumber = roundTime * 20;
 						startingNumber = roundTime * 20;
 
 						clockTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 							public void run() {
 								Methods.matchStarted = true;
 								currentNumber--;
 
 								if (currentNumber == startingNumber - 1) {
 									Methods.sendPBChat(Strings.RoundStarted.replace("%seconds", String.valueOf(startingNumber / 20)).replace("%team1", Methods.TeamOne).replace("%team2", Methods.TeamTwo));
 								}
 								if (currentNumber == 1200) {
 									Methods.sendPBChat(Strings.Prefix + Strings.OneMinuteRemaining);
 								}
 								if (currentNumber == 0) {
 									Methods.sendPBChat(Strings.RoundComplete);
 									Methods.matchStarted = false;
 									Bukkit.getServer().getScheduler().cancelTask(clockTask);
 									Methods.restoreArmor();
 								}
 
 							}
 						}, 0L, 1L);
 
 						if (Methods.WGSupportEnabled) {
 							if (Methods.getWorldGuard() != null) {
 								for (Player player: Bukkit.getOnlinePlayers()) {
 									String teamName = Methods.getPlayerTeam(player.getName());
 									if (teamName != null) {
 										if (teamName.equalsIgnoreCase(team1)) {
 											Methods.allowedZone.put(player.getName(), Methods.t1z1);
 										}
 										if (teamName.equalsIgnoreCase(team2)) {
 											Methods.allowedZone.put(player.getName(), Methods.t2z1);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 
 
 
 				if (args[0].equalsIgnoreCase("import")) {
 					if (!s.hasPermission("probending.import")) {
 						s.sendMessage(Strings.Prefix + Strings.noPermission);
 						return true;
 					}
 					if (args.length != 1) {
 						s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb import");
 						return true;
 					}
 					if (!Methods.storage.equalsIgnoreCase("mysql")) {
 						s.sendMessage(Strings.Prefix + "§cYou don't have MySQL enabled.");
 						return true;
 					}
 					try {
 						if (DBConnection.sql.getConnection().isClosed()) {
 							s.sendMessage(Strings.Prefix + "§cThe MySQL Connection is closed.");
 							return true;
 						}
 					} catch (SQLException ex) {
 						ex.printStackTrace();
 					}
 					Methods.importTeams();
 					s.sendMessage(Strings.Prefix + "§aData imported to MySQL database.");
 				}
 
 				if (args[0].equalsIgnoreCase("chat")) {
 					if (!s.hasPermission("probending.chat")) {
 						s.sendMessage(Strings.Prefix + Strings.noPermission);
 						return true;
 					}
 					if (args.length > 1) {
 						s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb chat");
 						return true;
 					}
 					Player p = (Player) s;
 					if (!pbChat.contains(p)) {
 						pbChat.add(p);
 						s.sendMessage(Strings.Prefix + Strings.ChatEnabled);
 						return true;
 					}
 					if (pbChat.contains(p)) {
 						pbChat.remove(p);
 						s.sendMessage(Strings.Prefix + Strings.ChatDisabled);
 						return true;
 					}
 				}
 				if (args[0].equalsIgnoreCase("reload")) {
 					if (!s.hasPermission("probending.reload")) {
 						s.sendMessage(Strings.Prefix + Strings.noPermission);
 						return true;
 					}
 					plugin.reloadConfig();
 					s.sendMessage(Strings.Prefix + "§cVersion: " + plugin.getDescription().getVersion());
 					s.sendMessage(Strings.Prefix + Strings.configReloaded);
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
 							s.sendMessage(Strings.Prefix + Strings.NoTeamPermissions);
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("addwin")) {
 						if (!s.hasPermission("probending.team.addwin")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb team addwin [Team]");
 							return true;
 						}
 						String teamName = args[2];
 						Set<String> teams = Methods.getTeams();
 						if (!Methods.teamExists(teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.TeamDoesNotExist);
 							return true;
 						}
 						if (teams != null) {
 							for (String team: teams) {
 								if (team.equalsIgnoreCase(teamName)) {
 									Methods.addWin(team);
 									s.sendMessage(Strings.Prefix + Strings.WinAddedToTeam.replace("%team", team));
 								}
 							}
 						}
 						return true;	
 					}
 					if (args[1].equalsIgnoreCase("addloss")) {
 						if (!s.hasPermission("probending.team.addloss")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage("§cProper Usage: §3/pb team addloss [Team]");
 							return true;
 						}
 						String teamName = args[2];
 						Set<String> teams = Methods.getTeams();
 						if (!Methods.teamExists(teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.TeamDoesNotExist);
 							return true;
 						}
 						if (teams != null) {
 							for (String team: teams) {
 								if (team.equalsIgnoreCase(teamName)) {
 									Methods.addLoss(team);
 									s.sendMessage(Strings.Prefix + Strings.LossAddedToTeam.replace("%team", team));
 								}
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("rename")) {
 						if (!s.hasPermission("probending.team.rename")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (!Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotInTeam);
 							return true;
 						}
 						if (!Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.NotOwnerOfTeam);
 							return true;
 						}
 						if (args.length < 3) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage §3/pb team rename [Name]");
 							return true;
 						}
 						boolean econEnabled = plugin.getConfig().getBoolean("Economy.Enabled");
 
 						String newName = args[2];
 						if (newName.length() > 15) {
 							s.sendMessage(Strings.Prefix + Strings.NameTooLong);
 							return true;
 						}
 						if (newName.equalsIgnoreCase(teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.TeamAlreadyNamedThat.replace("%newname", teamName));
 							return true;
 						}
 						if (econEnabled) {
 							Double playerBalance = Probending.econ.getBalance(s.getName());
 							Double renameFee = plugin.getConfig().getDouble("Economy.TeamRenameFee");
 							String serverAccount = plugin.getConfig().getString("Economy.ServerAccount");
 							String currency = Probending.econ.currencyNamePlural();
 							if (playerBalance < renameFee) {
 								s.sendMessage(Strings.Prefix + Strings.NotEnoughMoney.replace("%amount", renameFee.toString()).replace("%currency", currency));
 								return true;
 							}
 							Probending.econ.withdrawPlayer(s.getName(), renameFee);
 							Probending.econ.depositPlayer(serverAccount, renameFee);
 							s.sendMessage(Strings.Prefix + Strings.MoneyWithdrawn.replace("%amount", renameFee.toString()).replace("%currency", currency));
 						}
 						
 						int Wins = Methods.getWins(teamName);
 						int Losses = Methods.getLosses(teamName);
 						
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
 						
 						Methods.setLosses(Losses, newName);
 						Methods.setWins(Wins, newName);
 
 						s.sendMessage(Strings.Prefix + Strings.TeamRenamed.replace("%newname", newName));
 						Methods.setOwner(s.getName(), newName);
 						Methods.deleteTeam(teamName);
 						plugin.saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("list")) {
 						if (!s.hasPermission("probending.team.list")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						Set<String> teams = Methods.getTeams();
 						s.sendMessage("§cTeams: §a" + teams.toString());
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("disband")) {
 						if (!s.hasPermission("probending.team.disband")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb team disband");
 							return true;
 						}
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (!Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.NotOwnerOfTeam);
 							return true;
 						}
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (Methods.getPlayerTeam(player.getName()) == null) continue;
 							if (Methods.getPlayerTeam(player.getName()).equals(teamName)) {
 								s.sendMessage(Strings.Prefix + Strings.TeamDisbanded.replace("%team", teamName));
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
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						if (args.length != 2) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb team quit");
 							return true;
 						}
 
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (teamName == null) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotInTeam);
 							return true;
 						}
 						if (Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.CantBootFromOwnTeam);
 							return true;
 						}
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						Methods.removePlayerFromTeam(teamName, s.getName(), playerElement);
 						s.sendMessage(Strings.Prefix + Strings.YouHaveQuit.replace("%team", teamName));
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (Methods.getPlayerTeam(player.getName()) == null) continue;
 							if (Methods.getPlayerTeam(player.getName()).equals(teamName)) {
 								s.sendMessage(Strings.Prefix + Strings.PlayerHasQuit.replace("%team", teamName).replace("%player", s.getName()));
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("kick")) {
 						if (!s.hasPermission("probending.team.kick")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb team kick <Name>");
 							return true;
 						}
 						String teamName = Methods.getPlayerTeam(s.getName());
 						if (teamName == null) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotInTeam);
 							return true;
 						}
 						if (!Methods.isPlayerOwner(s.getName(), teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.NotOwnerOfTeam);
 							return true;
 						}
 						String playerName = args[2];
 						if (playerName.equals(s.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.CantBootFromOwnTeam);
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
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotOnThisTeam);
 							return true;
 						}
 						if (!playerTeam.equals(teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotOnThisTeam);
 							return true;
 						}
 						Methods.removePlayerFromTeam(teamName, playerName, playerElement);
 						Player player = Bukkit.getPlayer(playerName);
 						if (player != null) {
 							player.sendMessage(Strings.Prefix + Strings.YouHaveBeenBooted.replace("%team", teamName));
 						}
 						for (Player player2: Bukkit.getOnlinePlayers()) {
 							if (Methods.getPlayerTeam(player2.getName()) == null) continue;
 							if (Methods.getPlayerTeam(player2.getName()).equals(teamName)) {
 								player2.sendMessage(Strings.Prefix + Strings.PlayerHasBeenBooted.replace("%player", playerName).replace("%team", teamName));
 							}
 						}
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("join")) {
 						if (!s.hasPermission("probending.team.join")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 
 						if (args.length != 3) {
 							s.sendMessage("§cProper Usage: §3/pb team join [TeamName]");
 							return true;
 						}
 						if (Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerAlreadyInTeam);
 							return true;
 						}
 						String teamName = args[2];
 						if (teamInvites.get(s.getName()) == null) {
 							s.sendMessage(Strings.Prefix + Strings.NoInviteFromTeam);
 							return true;
 						}
 						if (!teamInvites.get(s.getName()).contains(teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.NoInviteFromTeam);
 							return true;
 						}
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						if (playerElement == null) {
 							s.sendMessage(Strings.Prefix + Strings.noBendingType);
 							return true;
 						}
 						Set<String> teamelements = Methods.getTeamElements(teamName);
 						if (teamelements != null) {
 							if (teamelements.contains(playerElement)) {
 								s.sendMessage(Strings.Prefix + Strings.TeamAlreadyHasElement);
 								return true;
 							}
 							if (!plugin.getConfig().getBoolean("TeamSettings.Allow" + playerElement)) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", playerElement));
 								return true;
 							}
 							Methods.addPlayerToTeam(teamName, s.getName(), playerElement);
 							for (Player player: Bukkit.getOnlinePlayers()) {
 								String teamName2 = Methods.getPlayerTeam(player.getName());
 								if (teamName2 != null) {
 									if (Methods.getPlayerTeam(player.getName()).equals(teamName)) {
 										player.sendMessage(Strings.Prefix + Strings.PlayerJoinedTeam.replace("%player", s.getName()).replace("%team", teamName));
 									}
 								}
 							}
 						}
 						teamInvites.remove(s.getName());
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("info")) {
 						if (!s.hasPermission("probending.team.info")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
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
 							s.sendMessage(Strings.Prefix + Strings.TeamDoesNotExist);
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
 						int points = Methods.getPoints(teamName);
 
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
						s.sendMessage("§3Points: §e" + points);
 
 					}
 					if (args[1].equalsIgnoreCase("invite")) {
 						if (args.length != 3) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb team invite [Name]");
 							return true;
 						}
 						if (!s.hasPermission("probending.team.invite")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 
 						if (!Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotInTeam);
 							return true;
 						}
 
 						String playerTeam = Methods.getPlayerTeam(s.getName());
 						if (!Methods.isPlayerOwner(s.getName(), playerTeam)) {
 							s.sendMessage(Strings.Prefix + Strings.NotOwnerOfTeam);
 							return true;
 						}
 
 						Player player = Bukkit.getPlayer(args[2]);
 
 						if (player == null) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerNotOnline);
 							return true;
 						}
 
 						if (Methods.playerInTeam(player.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerAlreadyInTeam);
 							return true;
 						}
 
 						if (!teamInvites.containsKey(player.getName())) {
 							teamInvites.put(player.getName(), new LinkedList<String>());
 						}
 
 						int maxSize = plugin.getConfig().getInt("TeamSettings.MaxTeamSize");
 						if (Methods.getTeamSize(playerTeam) >= maxSize) {
 							s.sendMessage(Strings.Prefix + Strings.MaxSizeReached);
 							return true;
 						}
 						String playerElement = Methods.getPlayerElementAsString(player.getName());
 
 						if (playerElement == null) {
 							s.sendMessage(Strings.Prefix + Strings.noBendingType);
 							return true;
 						}
 						if (!Methods.getAirAllowed()) {
 							if (playerElement.equals("Air")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Airbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getWaterAllowed()) {
 							if (playerElement.equals("Water")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Waterbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getEarthAllowed()) {
 							if (playerElement.equals("Earth")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Earthbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getFireAllowed()) {
 							if (playerElement.equals("Fire")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Firebenders"));
 								return true;
 							}
 						}
 						if (!Methods.getChiAllowed()) {
 							if (playerElement.equals("Chi")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Chiblockers"));
 								return true;
 							}
 						}
 						Set<String> teamelements = Methods.getTeamElements(playerTeam);
 						if (teamelements != null) {
 							if (teamelements.contains(playerElement)) {
 								s.sendMessage(Strings.Prefix + Strings.TeamAlreadyHasElement);
 								return true;
 							}
 						}
 
 						teamInvites.get(player.getName()).add(playerTeam);
 						s.sendMessage(Strings.Prefix + Strings.PlayerInviteSent.replace("%team", playerTeam).replace("%player", player.getName()));
 						player.sendMessage(Strings.Prefix + Strings.PlayerInviteReceived.replace("%team", playerTeam).replace("%player", player.getName()));
 						player.sendMessage(Strings.Prefix + Strings.InviteInstructions.replace("%team", playerTeam).replace("%player", player.getName()));
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("create")) {
 						if (args.length != 3) {
 							s.sendMessage(Strings.Prefix + "§cProper Usage: §3/pb team create [Name]");
 							return true;
 						}
 						if (!s.hasPermission("probending.team.create")) {
 							s.sendMessage(Strings.Prefix + Strings.noPermission);
 							return true;
 						}
 						String teamName = args[2];
 						if (Methods.teamExists(teamName)) {
 							s.sendMessage(Strings.Prefix + Strings.teamAlreadyExists);
 							return true;
 						}
 
 						if (teamName.length() > 15) {
 							s.sendMessage(Strings.Prefix + Strings.NameTooLong);
 							return true;
 						}
 
 						if (!Tools.isBender(s.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.noBendingType);
 							return true;
 						}
 
 						if (Methods.playerInTeam(s.getName())) {
 							s.sendMessage(Strings.Prefix + Strings.PlayerAlreadyInTeam);
 							return true;
 						}
 						Double creationCost = plugin.getConfig().getDouble("Economy.TeamCreationFee");
 						String serverAccount = plugin.getConfig().getString("Economy.ServerAccount");
 						boolean econEnabled = plugin.getConfig().getBoolean("Economy.Enabled");
 
 						String playerElement = Methods.getPlayerElementAsString(s.getName());
 
 						if (!Methods.getAirAllowed()) {
 							if (playerElement.equals("Air")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Airbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getWaterAllowed()) {
 							if (playerElement.equals("Water")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Waterbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getEarthAllowed()) {
 							if (playerElement.equals("Earth")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Earthbenders"));
 								return true;
 							}
 						}
 						if (!Methods.getFireAllowed()) {
 							if (playerElement.equals("Fire")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Firebenders"));
 								return true;
 							}
 						}
 						if (!Methods.getChiAllowed()) {
 							if (playerElement.equals("Chi")) {
 								s.sendMessage(Strings.Prefix + Strings.ElementNotAllowed.replace("%element", "Chiblockers"));
 								return true;
 							}
 						}
 
 						if (econEnabled) {
 							String currencyName = Probending.econ.currencyNamePlural();
 							Double playerBalance = Probending.econ.getBalance(s.getName());
 							if (playerBalance < creationCost) {
 								s.sendMessage(Strings.Prefix + Strings.NotEnoughMoney.replace("%currency", currencyName));
 								return true;
 							}
 							Probending.econ.withdrawPlayer(s.getName(), creationCost);
 							Probending.econ.depositPlayer(serverAccount, creationCost);
 							s.sendMessage(Strings.Prefix + Strings.MoneyWithdrawn.replace("%amount", creationCost.toString()).replace("%currency", currencyName));
 						}
 
 
 						Methods.createTeam(teamName, s.getName());
 						Methods.addPlayerToTeam(teamName, s.getName(), playerElement);
 						s.sendMessage(Strings.Prefix + Strings.TeamCreated.replace("%team", teamName));
 						return true;
 					}
 				}
 				return true;
 			}
 		}; probending.setExecutor(exe);
 	}
 }
