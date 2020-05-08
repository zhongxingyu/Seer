 package me.rainoboy97.scrimmage;
 
 import java.util.Iterator;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 
 public class CommandListener implements CommandExecutor {
 	private final Scrimmage plugin;
 
 	public CommandListener(Scrimmage plugin) {
 		this.plugin = plugin;
 	}
 
 	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		String command = cmd.getName();
 		Player player = (Player) sender;
 		if (command.equalsIgnoreCase("start") && player.isOp()) {
 			try {
 				if (args.length == 1) {
 					String arg = "";
 					for (String e : args) {
 						arg = e;
 					}
 					Scrimmage.starting = true;
 					Start start = new Start(plugin, Integer.parseInt(arg));
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 0L);
 				} else if (args.length == 2) {
 					Start start = new Start(plugin, 30);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 0L);
 				} else {
 					Scrimmage.starting = true;
 					Start start = new Start(plugin, 15);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 0L);
 				}
 			} catch (Exception e) {
 				player.sendMessage(ChatColor.RED + "An error occurred while performing the command, check your arguments!");
 			}
 		}
 		if (command.equalsIgnoreCase("cancel") && player.isOp()) {
 			if (args.length == 0) {
 				Bukkit.broadcastMessage(ChatColor.RED + "Canceled game or startup.");
 				Scrimmage.starting = false;
 				Scrimmage.gameActive = false;
 				for (Player i : Bukkit.getOnlinePlayers()) {
 					i.setGameMode(GameMode.CREATIVE);
 					i.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 					i.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 					i.getInventory().clear();
 					i.getInventory().setHelmet(null);
 					i.getInventory().setChestplate(null);
 					i.getInventory().setLeggings(null);
 					i.getInventory().setBoots(null);
 					i.updateInventory();
 					Location loc = i.getLocation();
 					loc = Var.observerSpawn;
 					RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, i, loc);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 0L);
 				}
 			}
 		}
 		if (command.equalsIgnoreCase("test") && player.isOp()) {
 			Scrimmage.gameActive = true;
 		}
		if (command.equalsIgnoreCase("g")) {
 			String sent = "";
 			for (String arg : args) {
 				sent += (arg + " ");
 			}
 			sent = sent.trim();
 			String message = "";
 			String chat = Scrimmage.team(player);
 			/*
 			 * if (player.getDisplayName().equals("Barnyard_Owl")) { if (chat ==
 			 * "spec") { message = ChatColor.RED + "�?�" + ChatColor.AQUA +
 			 * " [G] " + ChatColor.RESET + player.getDisplayName() + ": " +
 			 * sent; } if (chat == "blue") { message = ChatColor.RED + "�?�" +
 			 * ChatColor.BLUE + " [G] " + ChatColor.RESET +
 			 * player.getDisplayName() + ": " + sent; } if (chat == "red") {
 			 * message = ChatColor.RED + "�?�" + ChatColor.RED + " [G] " +
 			 * ChatColor.RESET + player.getDisplayName() + ": " + sent; } } else
 			 */
 			if (player.getDisplayName().equals("Barnyard_Owl")) {
 				if (chat == "spec") {
 					message = ChatColor.AQUA + "➜ " + ChatColor.DARK_PURPLE + "[DEV]" + ChatColor.AQUA + " [G] " + ChatColor.RESET + player.getDisplayName() + ": " + sent;
 				}
 				if (chat == Var.teamDisplayName) {
 					message = Var.teamTechnicalColor + "➜ " + ChatColor.DARK_PURPLE + "[DEV]" + Var.teamTechnicalColor + " [G] " + ChatColor.RESET + player.getDisplayName() + ": " + sent;
 				}
 				if (chat == Var.enemyTeamDisplayName) {
 					message = Var.enemyTeamTechnicalColor + "➜ " + ChatColor.DARK_PURPLE + "[DEV]" + Var.enemyTeamTechnicalColor + " [G] " + ChatColor.RESET + player.getDisplayName() + ": " + sent;
 				}
 			} else {
 				if (chat == "spec") {
 					message = ChatColor.AQUA + "➜ [G] " + ChatColor.RESET + player.getDisplayName() + ": " + sent;
 				}
 				if (chat == Var.teamDisplayName) {
 					message = ChatColor.BLUE + "➜ [G] " + ChatColor.RESET + player.getDisplayName() + ": " + sent;
 				}
 				if (chat == Var.enemyTeamDisplayName) {
 					message = ChatColor.RED + "➜ [G] " + ChatColor.RESET + player.getDisplayName() + ": " + sent;
 				}
 			}
 			Bukkit.broadcastMessage(message);
 		}
 		if (command.equalsIgnoreCase("join")) {
 			if (args.length == 1) {
 				String arg = "";
 				for (String e : args) {
 					arg = e.toUpperCase();
 				}
 				if ("OBSERVERS".startsWith(arg)) {
 					if (Scrimmage.specs.contains(player.getDisplayName())) {
 						Scrimmage.specs.remove(player.getDisplayName());
 					}
 					if (Scrimmage.team.contains(player.getDisplayName())) {
 						Scrimmage.team.remove(player.getDisplayName());
 					}
 					if (Scrimmage.enemyTeam.contains(player.getDisplayName())) {
 						Scrimmage.enemyTeam.remove(player.getDisplayName());
 					}
 					Scrimmage.specs.add(player.getDisplayName());
 					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
 					Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
 					Team spec = scoreboard.getTeam("spec");
 					spec.addPlayer((OfflinePlayer) player);
 					if (Scrimmage.gameActive) {
 						player.setGameMode(GameMode.CREATIVE);
 						player.setAllowFlight(true);
 						player.getInventory().clear();
 						player.getInventory().setHelmet(null);
 						player.getInventory().setChestplate(null);
 						player.getInventory().setLeggings(null);
 						player.getInventory().setBoots(null);
 						player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 						player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 						Location loc = player.getLocation();
 						loc = Var.observerSpawn;
 						player.teleport(loc);
 					}
 					player.sendMessage(ChatColor.AQUA + "Joined observers successfully.");
 				} else if (Var.teamDisplayName.startsWith(arg)) {
 					if (Scrimmage.team.contains(player.getDisplayName())) {
 						Scrimmage.team.remove(player.getDisplayName());
 					}
 					if (Scrimmage.enemyTeam.contains(player.getDisplayName())) {
 						Scrimmage.enemyTeam.remove(player.getDisplayName());
 					}
 					if (Scrimmage.specs.contains(player.getDisplayName())) {
 						Scrimmage.specs.remove(player.getDisplayName());
 					}
 					Scrimmage.team.add(player.getDisplayName());
 					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
 					Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
 					Team blue = scoreboard.getTeam(Var.teamDisplayName);
 					blue.addPlayer((OfflinePlayer) player);
 					if (Scrimmage.gameActive) {
 						player.setGameMode(GameMode.SURVIVAL);
 						player.removePotionEffect(PotionEffectType.INVISIBILITY);
 						player.removePotionEffect(PotionEffectType.SATURATION);
 						player.getInventory().clear();
 						ItemStack item;
 						Iterator iterItems = Var.items.iterator();
 						Iterator iterAmounts = Var.amounts.iterator();
 						for (int i = 1; i <= Var.itemAmount; i++) {
 							if (iterItems.hasNext() && iterAmounts.hasNext()) {
 								Object idEntry = iterItems.next();
 								Object amountEntry = iterAmounts.next();
 								item = new ItemStack(Integer.parseInt(idEntry.toString()), Integer.parseInt(amountEntry.toString()));
 								player.getInventory().setItem(i - 1, item);
 							}
 						}
 						if (Var.helmet != 0) {
 							ItemStack helmet = new ItemStack(Var.helmet, 1);
 							player.getInventory().setHelmet(helmet);
 						} else {
 							player.getInventory().setHelmet(null);
 						}
 						if (Var.chestplate != 0) {
 							ItemStack chestplate = new ItemStack(Var.chestplate, 1);
 							player.getInventory().setChestplate(chestplate);
 						} else {
 							player.getInventory().setChestplate(null);
 						}
 						if (Var.leggings != 0) {
 							ItemStack leggings = new ItemStack(Var.leggings, 1);
 							player.getInventory().setLeggings(leggings);
 						} else {
 							player.getInventory().setLeggings(null);
 						}
 						if (Var.boots != 0) {
 							ItemStack boots = new ItemStack(Var.boots, 1);
 							player.getInventory().setBoots(boots);
 						} else {
 							player.getInventory().setBoots(null);
 						}
 						player.updateInventory();
 						Location loc = player.getLocation();
 						loc = Var.teamSpawn;
 						RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, player, loc);
 						Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 1L);
 					}
 					player.sendMessage(Var.teamTechnicalColor + "Joined " + Var.teamDisplayName + " successfully.");
 
 				} else if (Var.enemyTeamDisplayName.startsWith(arg)) {
 					if (Scrimmage.team.contains(player.getDisplayName())) {
 						Scrimmage.team.remove(player.getDisplayName());
 					}
 					if (Scrimmage.enemyTeam.contains(player.getDisplayName())) {
 						Scrimmage.enemyTeam.remove(player.getDisplayName());
 					}
 					if (Scrimmage.specs.contains(player.getDisplayName())) {
 						Scrimmage.specs.remove(player.getDisplayName());
 					}
 					Scrimmage.enemyTeam.add(player.getDisplayName());
 					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
 					Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
 					Team red = scoreboard.getTeam(Var.enemyTeamDisplayName);
 					red.addPlayer((OfflinePlayer) player);
 					if (Scrimmage.gameActive) {
 						player.setGameMode(GameMode.SURVIVAL);
 						player.removePotionEffect(PotionEffectType.INVISIBILITY);
 						player.removePotionEffect(PotionEffectType.SATURATION);
 						player.getInventory().clear();
 						ItemStack item;
 						Iterator iterItems = Var.items.iterator();
 						Iterator iterAmounts = Var.amounts.iterator();
 						for (int i = 1; i <= Var.itemAmount; i++) {
 							if (iterItems.hasNext() && iterAmounts.hasNext()) {
 								Object idEntry = iterItems.next();
 								Object amountEntry = iterAmounts.next();
 								item = new ItemStack(Integer.parseInt(idEntry.toString()), Integer.parseInt(amountEntry.toString()));
 								player.getInventory().setItem(i - 1, item);
 							}
 						}
 						if (Var.helmet != 0) {
 							ItemStack helmet = new ItemStack(Var.helmet, 1);
 							player.getInventory().setHelmet(helmet);
 						} else {
 							player.getInventory().setHelmet(null);
 						}
 						if (Var.chestplate != 0) {
 							ItemStack chestplate = new ItemStack(Var.chestplate, 1);
 							player.getInventory().setChestplate(chestplate);
 						} else {
 							player.getInventory().setChestplate(null);
 						}
 						if (Var.leggings != 0) {
 							ItemStack leggings = new ItemStack(Var.leggings, 1);
 							player.getInventory().setLeggings(leggings);
 						} else {
 							player.getInventory().setLeggings(null);
 						}
 						if (Var.boots != 0) {
 							ItemStack boots = new ItemStack(Var.boots, 1);
 							player.getInventory().setBoots(boots);
 						} else {
 							player.getInventory().setBoots(null);
 						}
 						player.updateInventory();
 						Location loc = player.getLocation();
 						loc = Var.enemyTeamSpawn;
 						RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, player, loc);
 						Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 1L);
 					}
 					player.sendMessage(Var.enemyTeamTechnicalColor + "Joined " + Var.enemyTeamDisplayName + " successfully.");
 				}
 			}
 		}
 		if (command.equalsIgnoreCase("tp")) {
 			if (player.getGameMode() == GameMode.CREATIVE) {
 				if (args.length == 1) {
 					try {
 						Player player2 = null;
 						for (String arg : args) {
 							player2 = Bukkit.getPlayer(arg);
 						}
 						player.teleport(player2);
 						player.sendMessage(ChatColor.AQUA + "Teleported to " + player2.getDisplayName());
 					} catch (Exception e) {
 						player.sendMessage(ChatColor.RED + "An error occurred while performing the command, check your arguments!");
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 }
