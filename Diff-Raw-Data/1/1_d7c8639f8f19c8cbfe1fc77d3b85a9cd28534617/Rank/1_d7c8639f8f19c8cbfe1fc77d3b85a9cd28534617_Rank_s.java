 package com.celehner.AutoRanker;
 
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.List;
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import de.diddiz.LogBlock.LogBlock;
 import de.diddiz.LogBlock.QueryParams;
 import de.diddiz.LogBlock.QueryParams.BlockChangeType;
 import de.diddiz.LogBlock.BlockChange;
 
 class Rank {
 	String name;
 	String onlineTime;
 	int daysOnline;
 	int created; // block counts
 	int destroyed;
 	String hasPermission;
 	String hasNotPermission;
 	List<String> commands;
 	AutoRanker plugin;
 
 	Rank(String name, int daysOnline, String onlineTime,
 		String hasPermission, String hasNotPermission,
 		int created, int destroyed,
 		List<String> commands, AutoRanker plugin) {
 
 		this.name = name;
 		this.daysOnline = daysOnline;
 		this.onlineTime = onlineTime;
 		this.hasPermission = hasPermission;
 		this.hasNotPermission = hasNotPermission;
 		this.created = created;
 		this.destroyed = destroyed;
 		this.commands = commands;
 		this.plugin = plugin;
 	}
 
 	public boolean playerMeetsRequirements(Player player) {
 		LogBlock logblock = plugin.getLogBlock();
 		QueryParams p = new QueryParams(logblock);
 		p.setPlayer(player.getName());
 		p.world = player.getServer().getWorlds().get(0);
 		p.since = 0;
 		try {
 			if (created > 0) {
 				p.bct = BlockChangeType.CREATED;
 				if (logblock.getCount(p) < created) return false;
 			}
 			if (destroyed > 0) {
 				p.bct = BlockChangeType.DESTROYED;
 				if (logblock.getCount(p) < destroyed) return false;
 			}
 		} catch (final SQLException ex) {
 			plugin.log.log(Level.WARNING, "Unable to lookup.", ex);
 		}
 		if (hasPermission != "") {
 			if (!player.hasPermission(hasPermission)) return false;
 		}
 		if (hasNotPermission != "") {
 			if (player.hasPermission(hasNotPermission)) return false;
 		}
 		return true;
 	}
 
 	// give the promotion
 	public void executeCommands(Player player) {
 		Server server = player.getServer();
 		plugin.log.info("[AutoRanker] Promoting player " + player.getName() +
 			" to " + name);
 		for (String commandLine : commands) {
 			server.dispatchCommand(server.getConsoleSender(),
 				commandLine.replace("PLAYER", player.getName()));
 		}
 	}
 
 	public void applyCheckToPlayer(Player player) {
 		//plugin.log.info("[AutoRanker] testing " + player.getName());
 		if (playerMeetsRequirements(player)) {
 			executeCommands(player);
 		}
 	}
 }
