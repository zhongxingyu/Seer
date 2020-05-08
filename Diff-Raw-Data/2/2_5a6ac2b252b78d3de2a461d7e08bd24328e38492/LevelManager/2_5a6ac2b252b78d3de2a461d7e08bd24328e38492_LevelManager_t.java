 package net.lordsofcode.zephyrus.player;
 
 import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.events.PlayerLevelUpEvent;
 import net.lordsofcode.zephyrus.utils.PlayerConfigHandler;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Sound;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class LevelManager {
 
 	public static Zephyrus plugin;
 	private int levelBalance;
 
 	public LevelManager(Zephyrus plugin) {
 		LevelManager.plugin = plugin;
 	}
 
 	/**
 	 * Levelup the player
 	 * 
 	 * @param player
 	 *            The player to levelup
 	 */
 	public void levelUp(Player player) {
 		FileConfiguration cfg = PlayerConfigHandler.getConfig(plugin, player);
 		int current = PlayerConfigHandler.getConfig(plugin, player).getInt(
 				"Level");
 		current = current + 1;
 		cfg.set("Level", current);
 		PlayerConfigHandler.saveConfig(plugin, player, cfg);
 		player.sendMessage(ChatColor.AQUA + "You leveled up to level "
 				+ getLevel(player));
 		if (Zephyrus.getInstance().getConfig().getBoolean("Levelup-Sound")) {
 			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, 1);
 			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, 8);
 			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, -1);
 		}
 		PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, current);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 	}
 
 	/**
 	 * Add levelprogress to player
 	 * 
 	 * @param player
 	 *            The player to add progress to
 	 * @param amount
 	 *            The amount to add
 	 */
 	public void levelProgress(Player player, int amount) {
 		levelBalance = plugin.getConfig().getInt("LevelBalance");
 		int current = PlayerConfigHandler.getConfig(plugin, player).getInt(
 				"progress");
 		current = current + amount;
 		int level = getLevel(player);
 		while (current > (level * levelBalance) + (level * level + 100)) {
			current = current - ((level * levelBalance) + (level * level + 100));
 			levelUp(player);
 			level++;
 		}
 		FileConfiguration cfg = PlayerConfigHandler.getConfig(plugin, player);
 		cfg.set("progress", current);
 		PlayerConfigHandler.saveConfig(plugin, player, cfg);
 	}
 
 	/**
 	 * Get the level progress of the player
 	 * 
 	 * @param player
 	 *            The player
 	 * @return The player's level progress
 	 */
 	public static int getLevelProgress(Player player) {
 		int current = PlayerConfigHandler.getConfig(plugin, player).getInt(
 				"progress");
 		return current;
 	}
 
 	/**
 	 * Gets the level of the player
 	 * 
 	 * @param player
 	 *            The player
 	 * @return The level of the player
 	 */
 	public static int getLevel(Player player) {
 		return PlayerConfigHandler.getConfig(plugin, player).getInt("Level");
 	}
 
 	/**
 	 * Gets the level of the player
 	 * 
 	 * @param player
 	 *            The player
 	 * @return The level of the player
 	 */
 	public int getLevel(HumanEntity player) {
 		Player p = (Player) player;
 		return PlayerConfigHandler.getConfig(plugin, p).getInt("Level");
 	}
 
 	/**
 	 * Reloads the mana from the player's level
 	 * 
 	 * @param player
 	 *            The player
 	 */
 	public static void resetMana(Player player) {
 		Zephyrus.mana.put(player.getName(), getLevel(player) * 100);
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	public static int getMana(Player player) {
 		try {
 			return (Integer) Zephyrus.mana.get(player.getName());
 		} catch (Exception e) {
 			resetMana(player);
 			return getLevel(player) * 100;
 		}
 	}
 
 	/**
 	 * Saves the mana level of a player to that player's config
 	 * 
 	 * @param player
 	 *            The player
 	 */
 	public static void saveMana(Player player) {
 		FileConfiguration cfg = PlayerConfigHandler.getConfig(plugin, player);
 		cfg.set("mana", Zephyrus.mana.get(player.getName()));
 		PlayerConfigHandler.saveConfig(plugin, player, cfg);
 	}
 
 	/**
 	 * Loads the mana from the config
 	 * 
 	 * @param player
 	 *            The player
 	 * @return The player's mana level (from the config)
 	 */
 	public static int loadMana(Player player) {
 		int i = PlayerConfigHandler.getConfig(plugin, player).getInt("mana");
 		return i;
 	}
 
 	/**
 	 * Drains mana from a player
 	 * 
 	 * @param player
 	 *            The player
 	 * @param amount
 	 *            The amount to drain
 	 */
 	public static void drainMana(Player player, int amount) {
 		Zephyrus.mana.put(player.getName(),
 				(Integer) Zephyrus.mana.get(player.getName()) - amount);
 	}
 
 	/**
 	 * Sends the mana level to the player (in an awesome display way)
 	 * 
 	 * @param player
 	 *            The player
 	 */
 	public void displayMana(Player player) {
 		new DisplayMana(player).run();
 	}
 
 	public void displayMana(Player player, CommandSender sender) {
 		new DisplayMana(player, sender).run();
 	}
 
 	/**
 	 * Sends the level and levelprogress to the player (in an awesome display
 	 * way)
 	 * 
 	 * @param player
 	 *            The player
 	 */
 	public void displayLevel(Player player) {
 		new DisplayLevel(player).run();
 	}
 
 	public void displayLevel(Player player, CommandSender sender) {
 		new DisplayLevel(player, sender).run();
 	}
 
 	private class DisplayMana extends BukkitRunnable {
 
 		Player player;
 		CommandSender sender;
 
 		DisplayMana(Player player) {
 			this.player = player;
 			this.sender = player;
 		}
 
 		DisplayMana(Player player, CommandSender sender) {
 			this.player = player;
 			this.sender = sender;
 		}
 
 		@Override
 		public void run() {
 			double maxMana = getLevel(player) * 100;
 			double currentMana = getMana(player);
 			double devider = maxMana / 120;
 			StringBuffer full = new StringBuffer();
 			StringBuffer empty = new StringBuffer();
 			if (currentMana != 0) {
 				double emptyMana = maxMana - currentMana;
 				for (double i = currentMana / devider; i >= 0; i = i - 1) {
 					full.append("|");
 				}
 				for (double i = emptyMana / devider; i >= 1; i = i - 1) {
 					if (emptyMana >= 1) {
 						empty.append("|");
 					} else {
 						break;
 					}
 				}
 				sender.sendMessage(ChatColor.GOLD + "        ---===["
 						+ ChatColor.RED + "Mana: " + currentMana + " / "
 						+ maxMana + ChatColor.GOLD + "]===---");
 				sender.sendMessage(ChatColor.DARK_AQUA + "{" + ChatColor.AQUA
 						+ full + ChatColor.GRAY + empty + ChatColor.DARK_AQUA
 						+ "}");
 				sender.sendMessage(ChatColor.DARK_AQUA + "{" + ChatColor.AQUA
 						+ full + ChatColor.GRAY + empty + ChatColor.DARK_AQUA
 						+ "}");
 			} else {
 				for (int i = 120; i > 0; i = i - 1) {
 					empty.append("|");
 				}
 				sender.sendMessage(ChatColor.GOLD + "              ---===["
 						+ ChatColor.RED + "Mana: " + currentMana + " / "
 						+ maxMana + ChatColor.GOLD + "]===---");
 				sender.sendMessage(ChatColor.DARK_AQUA + "{" + ChatColor.GRAY
 						+ empty + ChatColor.DARK_AQUA + "}");
 				sender.sendMessage(ChatColor.DARK_AQUA + "{" + ChatColor.GRAY
 						+ empty + ChatColor.DARK_AQUA + "}");
 			}
 		}
 	}
 
 	private class DisplayLevel extends BukkitRunnable {
 
 		Player player;
 		CommandSender sender;
 
 		DisplayLevel(Player player) {
 			this.player = player;
 			this.sender = player;
 		}
 
 		DisplayLevel(Player player, CommandSender sender) {
 			this.player = player;
 			this.sender = sender;
 		}
 
 		@Override
 		public void run() {
 			levelBalance = plugin.getConfig().getInt("LevelBalance");
 			int level = getLevel(player);
 			int currentLevelProg = getLevelProgress(player);
 			int maxLevelProg = (level * levelBalance) + (level * level + 100);
 			long devider = (maxLevelProg * 100) / 120;
 			StringBuffer full = new StringBuffer();
 			StringBuffer empty = new StringBuffer();
 			if (level != 0) {
 				if (currentLevelProg != 0) {
 					double emptyLvl = maxLevelProg - currentLevelProg;
 					for (long i = (currentLevelProg * 100) / devider; i >= 0; i = i - 1) {
 						if (i != Double.POSITIVE_INFINITY) {
 							full.append("|");
 						}
 					}
 					for (double i = (emptyLvl * 100) / devider; i >= 1; i = i - 1) {
 						if (emptyLvl >= 1) {
 							empty.append("|");
 						} else {
 							break;
 						}
 					}
 					sender.sendMessage(ChatColor.DARK_BLUE + "  ---===["
 							+ ChatColor.BLUE + "Level: " + level
 							+ ChatColor.BOLD + "" + ChatColor.DARK_BLUE
 							+ " -=- " + ChatColor.BLUE + "Progress: "
 							+ currentLevelProg + "/" + maxLevelProg
 							+ ChatColor.DARK_BLUE + "]===---");
 					sender.sendMessage(ChatColor.DARK_GRAY + "{"
 							+ ChatColor.LIGHT_PURPLE + full + ChatColor.GRAY
 							+ empty + ChatColor.DARK_GRAY + "}");
 				} else {
 					for (int i = 120; i > 0; i = i - 1) {
 						empty.append("|");
 					}
 					sender.sendMessage(ChatColor.DARK_BLUE + "  ---===["
 							+ ChatColor.BLUE + "Level: " + level
 							+ ChatColor.DARK_BLUE + " -=- " + ChatColor.BLUE
 							+ "Progress: " + currentLevelProg + "/"
 							+ maxLevelProg + ChatColor.DARK_BLUE + "]===---");
 					sender.sendMessage(ChatColor.DARK_AQUA + "{"
 							+ ChatColor.GRAY + empty + ChatColor.DARK_AQUA
 							+ "}");
 				}
 
 			} else {
 				player.sendMessage(ChatColor.DARK_BLUE + "             ---===["
 						+ ChatColor.BLUE + "Level: " + 0 + ChatColor.DARK_BLUE
 						+ "]===---");
 			}
 		}
 	}
 
 }
