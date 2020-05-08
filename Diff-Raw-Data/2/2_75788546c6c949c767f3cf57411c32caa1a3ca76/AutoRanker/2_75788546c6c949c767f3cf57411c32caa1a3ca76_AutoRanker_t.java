 package com.celehner.AutoRanker;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.List;
 import java.util.ArrayList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import de.diddiz.LogBlock.LogBlock;
 import org.bukkit.event.EventHandler;
 
 public class AutoRanker extends JavaPlugin
 {
 
 	private LogBlock logblock = null;
 	List<Rank> ranks;
 	Logger log = Logger.getLogger("AutoRanker");
 
 	@Override
 	public void onEnable() {
 		final PluginManager pm = getServer().getPluginManager();
 		if (pm.getPlugin("LogBlock") == null) return;
 		logblock = (LogBlock)pm.getPlugin("LogBlock");
 
 		updateRanksFromConfig();
 
 		getServer().getPluginManager().registerEvents(new Listener() {
 			@EventHandler
 			public void onPlayerJoin(PlayerJoinEvent event) {
 				updatePlayerRanks(event.getPlayer());
 			}
 		}, this);
 
 		this.getCommand("autorank").setExecutor(new CommandExecutor() {
 			public boolean onCommand(CommandSender sender,
 				Command command, String label, String[] args) {
 				if (args.length < 1) {
 					return false;
 				}
 
 				if (args[0].equals("reload")) {
 					if (sender.isOp() || sender.hasPermission("autorank.reload")) {
 						reloadConfig();
 						updateRanksFromConfig();
 						sender.sendMessage("Reloaded AutoRanker config.");
 						return true;
 
 					} else {
 						sender.sendMessage(ChatColor.RED +
 							"No permission to do that.");
 						return true;
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED +
 						"Unknown command '" + args[0] + "'");
 					return true;
 				}
 			}
 		});
 
 		log.info("[AutoRanker] Enabled.");
 	}
 
 	@Override
 	public void onDisable() {
 	}
 
 	public LogBlock getLogBlock() {
 		return logblock;
 	}
 
 	public void updateRanksFromConfig() {
 		ranks = new ArrayList<Rank>();
 		FileConfiguration config = getConfig();
 		ConfigurationSection ranksSec = null;
 		if (config.isSet("ranks")) {
 			ranksSec = config.getConfigurationSection("ranks");
 		} else {
 			ranksSec = config.createSection("ranks");
 			saveConfig();
 		}
 
 		int numRanksLoaded = 0;
  		for (final String rankName : ranksSec.getKeys(false)) {
 			try {
 				ConfigurationSection rSec =
 					ranksSec.getConfigurationSection(rankName);
 
 				if (!rSec.isSet("requirements")) {
 					log.info("[AutoRanker] Rank is missing requirements.");
 					continue;
 				}
 
 				ConfigurationSection reqs =
 					rSec.getConfigurationSection("requirements");
 
 				int daysOnline = reqs.getInt("daysonline", 0);
 				int created = reqs.getInt("created", 0);
 				int destroyed = reqs.getInt("destroyed", 0);
				int hoursOnline = reqs.getInt("hoursonline", 0);
 				String hasPermission = reqs.getString("haspermission", "");
 				String hasNotPermission = reqs.getString("hasnotpermission", "");
 
 				List<String> commands = rSec.getStringList("commands");
 
 				ranks.add(new Rank(rankName, daysOnline, hoursOnline,
 					hasPermission, hasNotPermission,
 					created, destroyed, commands, this));
 				numRanksLoaded++;
 			} catch (final Exception ex) {
 				log.log(Level.WARNING, "[AutoRanker] Error at parsing rank '" +
 					rankName + "': ", ex);
 			}
 		}
 
 		log.info("[AutoRanker] Loaded " + numRanksLoaded + " rank" + (numRanksLoaded == 1 ? "" : "s"));
 	}
 
 	public void updatePlayerRanks(Player player) {
 		for (final Rank rank : ranks) {
 			rank.applyCheckToPlayer(player);
 		}
 	}
 }
