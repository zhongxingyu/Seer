 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 public class Config {
 
 	private ArcherGames plugin;
 
 	/**
 	 *
 	 * @param plugin
 	 */
 	public Config(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 *
 	 */
 	public void loadConfiguration() {
 		if (!(plugin.getConfig().getDouble("ArcherGames.technical.version") == 0.1)) {
 			plugin.getConfig().set("ArcherGames.technical.debug", false);
 			plugin.getConfig().set("ArcherGames.technical.version", 0.1);
 			plugin.getConfig().set("ArcherGames.toggles.arrowDelete", true);
 			ArrayList<String> voteSites = new ArrayList<String>();
 			voteSites.add("http://ow.ly/cpQI0");
 			voteSites.add("http://ow.ly/cmwer");
 			voteSites.add("http://ow.ly/cmnPu");
 			voteSites.add("http://ow.ly/cmDIF");
 			voteSites.add("http://ow.ly/cmP18");
 			voteSites.add("http://ow.ly/cmETB");
 			voteSites.add("http://ow.ly/dVcsF");
 			voteSites.add("http://ow.ly/eggLe");
 			plugin.getConfig().set("ArcherGames.vote.info", "§gVote on these sites for $3000 each!");
 			plugin.getConfig().set("ArcherGames.vote.sites", voteSites);
 			plugin.getConfig().set("ArcherGames.vote.howMuchToGive", 3000);
 			plugin.getConfig().set("ArcherGames.timers.preGameCountdown", 120); // 2 minutes for everyone to get in game
 			plugin.getConfig().set("ArcherGames.timers.gameInvincibleCountdown", 60); // 1 minute for everyone to get far enough away from each other
 			plugin.getConfig().set("ArcherGames.timers.gameOvertimeCountdown", 600); // 10 minutes to play before we force the round to end
 			plugin.getConfig().set("ArcherGames.timers.shutdownTimer", 30); // 30 seconds until the server reboots.
 			plugin.getConfig().set("ArcherGames.timers.nagTime", 30);
 			/*
 			 * plugin.getConfig().set("ArcherGames.game.startPosition.world",
 			 * plugin.getServer().getWorlds().get(0).getName()); // Fetch the
 			 * default world
 			 * plugin.getConfig().set("ArcherGames.game.startPosition.x", 0);
 			 * plugin.getConfig().set("ArcherGames.game.startPosition.y", 64);
 			 * plugin.getConfig().set("ArcherGames.game.startPosition.z", 0);
 			 */
 			plugin.getConfig().set("ArcherGames.game.minPlayersToStart", 5);
 			plugin.getConfig().set("ArcherGames.kits.ExampleKitName1", "itemid:damage:enchantid:enchantlvl");
 			plugin.getConfig().set("ArcherGames.strings.startnotenoughplayers", "[ArcherGames] Attempted to start, but there were not enough players.");
 			plugin.getConfig().set("ArcherGames.strings.starting", "[ArcherGames] The Archer Games have started! You have 1 minute of invincibility to get away from enemies.");
 			plugin.getConfig().set("ArcherGames.strings.invincibilityend", "[ArcherGames] Your minute of invincibility is up! Let the games begin...");
 			plugin.getConfig().set("ArcherGames.strings.overtimestart", "[ArcherGames] Overtime has started! Fight to the death!");
 			plugin.getConfig().set("ArcherGames.strings.gameended", "[ArcherGames] The game is over!");
 			plugin.getConfig().set("ArcherGames.strings.serverclosekick", "The server is rebooting.");
 			plugin.getConfig().set("ArcherGames.strings.joinedgame", "[ArcherGames] Welcome %s to %s, make sure to read your book for information!");
 			plugin.getConfig().set("ArcherGames.strings.servername", "ArcherGamesServer");
 			plugin.getConfig().set("ArcherGames.strings.starttimeleft", "[ArcherGames] The game will start in %s!");
 			plugin.getConfig().set("ArcherGames.strings.kitinfo", "§gHere are the avalible kits: ");
 			plugin.getConfig().set("ArcherGames.strings.kitgiven", "§gYour kit has been set to %s.");
 			plugin.getConfig().set("ArcherGames.strings.nochat", "§4You must choose a kit before you can chat.");
 			plugin.getConfig().set("ArcherGames.strings.respawn", "§4You died and have been spawned in spectator mode (flying, invisible, no editing).");
 			plugin.getConfig().set("ArcherGames.strings.nocommand", "§4You may not use this command until you choose a kit.");
 			plugin.getConfig().set("ArcherGames.strings.kitnag", "§4Before the game begins, you need to choose a kit with /kitchoose.");
 			plugin.getConfig().set("ArcherGames.strings.playervoted", "§a-- %s voted for $3000! Type /vote for money! --");
 			plugin.getConfig().set("ArcherGames.irc.botname", "AG-SERVER1");
 			plugin.getConfig().set("ArcherGames.irc.host", "irc.esper.net");
 			plugin.getConfig().set("ArcherGames.irc.password", "asdfasdf");
 			plugin.getConfig().set("ArcherGames.irc.port", 6667);
 			plugin.getConfig().set("ArcherGames.irc.channel", "#araeosia");
 			plugin.getConfig().set("ArcherGames.mysql.username", "");
 			plugin.getConfig().set("ArcherGames.mysql.password", "");
 			plugin.getConfig().set("ArcherGames.mysql.port", 3306);
 			plugin.getConfig().set("ArcherGames.mysql.database", "");
 			plugin.getConfig().set("ArcherGames.mysql.hostname", "");
 			plugin.saveConfig();
 		}
 		plugin.voteSites = (java.util.List<String>) plugin.getConfig().getList("ArcherGames.vote.sites");
 		plugin.scheduler.preGameCountdown = plugin.getConfig().getInt("ArcherGames.timers.preGameCountdown");
 		plugin.scheduler.gameInvincibleCountdown = plugin.getConfig().getInt("ArcherGames.timers.gameInvincibleCountdown");
 		plugin.scheduler.gameOvertimeCountdown = plugin.getConfig().getInt("ArcherGames.timers.gameOvertimeCountdown");
 		plugin.scheduler.minPlayersToStart = plugin.getConfig().getInt("ArcherGames.game.minPlayersToStart");
 		plugin.scheduler.shutdownTimer = plugin.getConfig().getInt("ArcherGames.game.shutdownTimer");
 		plugin.scheduler.nagTime = plugin.getConfig().getInt("ArcherGames.timers.nagTime");
 		plugin.debug = plugin.getConfig().getBoolean("ArcherGames.technical.debug");
 		plugin.IRCBot.host = plugin.getConfig().getString("ArcherGames.irc.host");
 		plugin.IRCBot.botname = plugin.getConfig().getString("ArcherGames.irc.botname");
 		plugin.IRCBot.password = plugin.getConfig().getString("ArcherGames.irc.password");
 		plugin.IRCBot.channel = plugin.getConfig().getString("ArcherGames.irc.channel");
 		plugin.IRCBot.port = plugin.getConfig().getInt("ArcherGames.irc.port");
 		/*
 		 * plugin.startPosition = new Location(
 		 * plugin.getServer().getWorld(plugin.getConfig().getString("ArcherGames.game.startPosition.world")),
 		 * plugin.getConfig().getInt("ArcherGames.game.startPosition.x"),
 		 * plugin.getConfig().getInt("ArcherGames.game.startPosition.y"),
 		 * plugin.getConfig().getInt("ArcherGames.game.startPosition.z"));
 		 */
 		plugin.configToggles.put("arrowDelete", plugin.getConfig().getBoolean("ArcherGames.toggles.arrowDelete"));
 		plugin.strings.put("startnotenoughplayers", plugin.getConfig().getString("ArcherGames.strings.startnotenoughplayers"));
 		plugin.strings.put("starting", plugin.getConfig().getString("ArcherGames.strings.starting"));
 		plugin.strings.put("invincibilityend", plugin.getConfig().getString("ArcherGames.strings.invincibilityend"));
 		plugin.strings.put("overtimestart", plugin.getConfig().getString("ArcherGames.strings.overtimestart"));
 		plugin.strings.put("gameended", plugin.getConfig().getString("ArcherGames.strings.gameended"));
 		plugin.strings.put("serverclosekick", plugin.getConfig().getString("ArcherGames.strings.serverclosekick"));
 		plugin.strings.put("joinedgame", plugin.getConfig().getString("ArcherGames.strings.joinedgame"));
 		plugin.strings.put("starttimeleft", plugin.getConfig().getString("ArcherGames.strings.starttimeleft"));
 		plugin.strings.put("voteinfo", plugin.getConfig().getString("ArcherGames.vote.info"));
 		plugin.strings.put("kitinfo", plugin.getConfig().getString("ArcherGames.strings.kitinfo"));
 		plugin.strings.put("kitgiven", plugin.getConfig().getString("ArcherGames.strings.kitgiven"));
 		plugin.strings.put("nochat", plugin.getConfig().getString("ArcherGames.strings.nochat"));
 		plugin.strings.put("respawn", plugin.getConfig().getString("ArcherGames.strings.respawn"));
 		plugin.strings.put("kitnag", plugin.getConfig().getString("ArcherGames.strings.kitnag"));
 		plugin.strings.put("playervoted", plugin.getConfig().getString("ArcherGames.strings.playervoted"));
 		loadKits();
 	}
 
 	/**
 	 * Load the Kits form the Config
 	 *
 	 */
 	public void loadKits() {
 		ArrayList<ItemStack> temp;
 		for (String key : plugin.getConfig().getConfigurationSection("ArcherGames.kits").getKeys(false)) {
 			temp = new ArrayList<ItemStack>();
 			String s = plugin.getConfig().getString("ArcherGames.kits." + key);
 			for (String str : s.split(",")) {
 				try {
 					ItemStack itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(str.split(":")[0])), Integer.parseInt(str.split(":")[1]));
 					if (!(Integer.parseInt(str.split(":")[2]) < 0)) {
 						itemStack.addEnchantment(Enchantment.getById(Integer.parseInt(str.split(":")[2])), Integer.parseInt(str.split(":")[3]));
 					}
 					temp.add(itemStack);
 				} catch (NumberFormatException e) {
 					plugin.log.log(Level.SEVERE, "Warning: ArcherGames Kit " + key + " is not configured correctly!");
 				}
 			}
 			plugin.kits.put(key, temp);
 		}
 	}
 }
