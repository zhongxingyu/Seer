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
 		plugin.getConfig().set("ArcherGames.technical.debug", plugin.getConfig().get("ArcherGames.technical.debug", false));
 		plugin.getConfig().set("ArcherGames.toggles.arrowDelete", plugin.getConfig().get("ArcherGames.toggles.arrowDelete", true));
 		plugin.getConfig().set("ArcherGames.toggles.lockdownMode", plugin.getConfig().get("ArcherGames.toggles.lockdownMode", false));
 		ArrayList<String> voteSites = new ArrayList<String>();
 		voteSites.add("http://ow.ly/cpQI0");
 		voteSites.add("http://ow.ly/cmwer");
 		voteSites.add("http://ow.ly/cmnPu");
 		voteSites.add("http://ow.ly/cmDIF");
 		voteSites.add("http://ow.ly/cmP18");
 		voteSites.add("http://ow.ly/cmETB");
 		voteSites.add("http://ow.ly/dVcsF");
 		voteSites.add("http://ow.ly/eggLe");
 		plugin.getConfig().set("ArcherGames.vote.info", plugin.getConfig().get("ArcherGames.vote.info", "§gVote on these sites for $3000 each!"));
 		plugin.getConfig().set("ArcherGames.vote.sites", plugin.getConfig().get("ArcherGames.vote.sites", voteSites));
 		plugin.getConfig().set("ArcherGames.vote.howMuchToGive", plugin.getConfig().get("ArcherGames.vote.howMuchToGive", 3000));
 		plugin.getConfig().set("ArcherGames.timers.preGameCountdown", plugin.getConfig().get("ArcherGames.timers.preGameCountdown", 120)); // 2 minutes for everyone to get in game
 		plugin.getConfig().set("ArcherGames.timers.gameInvincibleCountdown", plugin.getConfig().get("ArcherGames.timers.gameInvincibleCountdown", 60)); // 1 minute for everyone to get far enough away from each other
 		plugin.getConfig().set("ArcherGames.timers.gameOvertimeCountdown", plugin.getConfig().get("ArcherGames.timers.gameOvertimeCountdown", 600)); // 10 minutes to play before we force the round to end
 		plugin.getConfig().set("ArcherGames.timers.shutdownTimer", plugin.getConfig().get("ArcherGames.timers.shutdownTimer", 30)); // 30 seconds until the server reboots.
 		plugin.getConfig().set("ArcherGames.timers.nagTime", plugin.getConfig().get("ArcherGames.timers.nagTime", 30));
 		/*
 		 * plugin.getConfig().set("ArcherGames.game.startPosition.world",
 		 * plugin.getServer().getWorlds().get(0).getName()); // Fetch the
 		 * default world
 		 * plugin.getConfig().set("ArcherGames.game.startPosition.x", 0);
 		 * plugin.getConfig().set("ArcherGames.game.startPosition.y", 64);
 		 * plugin.getConfig().set("ArcherGames.game.startPosition.z", 0);
 		 */
 		plugin.getConfig().set("ArcherGames.game.minPlayersToStart", plugin.getConfig().get("ArcherGames.game.minPlayersToStart", 5));
 		plugin.getConfig().set("ArcherGames.game.overtimeWorldRadius", plugin.getConfig().get("ArcherGames.game.overtimeWorldRadius", 50));
 //		plugin.getConfig().set("ArcherGames.kits.ExampleKitName1", plugin.getConfig().get("ArcherGames.kits.ExampleKitName1", "itemid:damage:enchantid:enchantlvl"););
 		plugin.getConfig().set("ArcherGames.strings.startnotenoughplayers", plugin.getConfig().get("ArcherGames.strings.startnotenoughplayers", "[ArcherGames] Attempted to start, but there were not enough players."));
 		plugin.getConfig().set("ArcherGames.strings.starting", plugin.getConfig().get("ArcherGames.strings.starting", "[ArcherGames] The Archer Games have started! You have 1 minute of invincibility to get away from enemies."));
 		plugin.getConfig().set("ArcherGames.strings.invincibilityend", plugin.getConfig().get("ArcherGames.strings.invincibilityend", "[ArcherGames] Your minute of invincibility is up! Let the games begin..."));
 		plugin.getConfig().set("ArcherGames.strings.overtimestart", plugin.getConfig().get("ArcherGames.strings.overtimestart", "[ArcherGames] Overtime has started! Fight to the death!"));
 		plugin.getConfig().set("ArcherGames.strings.gameended", plugin.getConfig().get("ArcherGames.strings.gameended", "[ArcherGames] The game is over!"));
 		plugin.getConfig().set("ArcherGames.strings.serverclosekick", plugin.getConfig().get("ArcherGames.strings.serverclosekick", "The server is rebooting."));
 		plugin.getConfig().set("ArcherGames.strings.joinedgame", plugin.getConfig().get("ArcherGames.strings.joinedgame", "[ArcherGames] Welcome %s to %s, make sure to read your book for information!"));
 		plugin.getConfig().set("ArcherGames.strings.servername", plugin.getConfig().get("ArcherGames.strings.servername", "ArcherGamesServer"));
 		plugin.getConfig().set("ArcherGames.strings.starttimeleft", plugin.getConfig().get("ArcherGames.strings.starttimeleft", "[ArcherGames] The game will start in %s!"));
 		plugin.getConfig().set("ArcherGames.strings.kitinfo", plugin.getConfig().get("ArcherGames.strings.kitinfo", "§gHere are the avalible kits: "));
 		plugin.getConfig().set("ArcherGames.strings.kitgiven", plugin.getConfig().get("ArcherGames.strings.kitgiven", "§gYour kit has been set to %s."));
 		plugin.getConfig().set("ArcherGames.strings.nochat", plugin.getConfig().get("ArcherGames.strings.nochat", "§4You must choose a kit before you can chat."));
 		plugin.getConfig().set("ArcherGames.strings.respawn", plugin.getConfig().get("ArcherGames.strings.respawn", "§4You died and have been spawned in spectator mode (flying, invisible, no editing)."));
 		plugin.getConfig().set("ArcherGames.strings.nocommand", plugin.getConfig().get("ArcherGames.strings.nocommand", "§4You may not use this command until you choose a kit."));
 		plugin.getConfig().set("ArcherGames.strings.kitnag", plugin.getConfig().get("ArcherGames.strings.kitnag", "§4Before the game begins, you need to choose a kit with /kit [kit]."));
 		plugin.getConfig().set("ArcherGames.strings.playervoted", plugin.getConfig().get("ArcherGames.strings.playervoted", "§a-- %s voted for $3000! Type /vote for money! --"));
 		plugin.getConfig().set("ArcherGames.strings.noblockediting", plugin.getConfig().get("ArcherGames.strings.noblockediting", "§4You cannot edit blocks until the game begins!"));
 		plugin.getConfig().set("ArcherGames.strings.nochestediting", plugin.getConfig().get("ArcherGames.strings.nochestediting", "§4You cannot access this until the game begins!"));
 		plugin.getConfig().set("ArcherGames.strings.nodroppickup", plugin.getConfig().get("ArcherGames.strings.nodroppickup", "§4You cannot drop or pick up items until the game begins!"));
 		plugin.getConfig().set("ArcherGames.strings.playersleft", plugin.getConfig().get("ArcherGames.strings.playersleft", "§c%s players remaining."));
 		plugin.getConfig().set("ArcherGames.irc.botname", plugin.getConfig().get("ArcherGames.irc.botname", "AG-SERVER1"));
 		plugin.getConfig().set("ArcherGames.irc.host", plugin.getConfig().get("ArcherGames.irc.host", "irc.esper.net"));
 		plugin.getConfig().set("ArcherGames.irc.password", plugin.getConfig().get("ArcherGames.irc.password", "asdfasdf"));
 		plugin.getConfig().set("ArcherGames.irc.port", plugin.getConfig().getInt("ArcherGames.irc.port", 6667));
 		plugin.getConfig().set("ArcherGames.irc.channel", plugin.getConfig().get("ArcherGames.irc.channel", "#araeosia"));
 		plugin.getConfig().set("ArcherGames.startbook.title", plugin.getConfig().get("ArcherGames.startbook.title", "Rule Book"));
 		plugin.getConfig().set("ArcherGames.startbook.author", plugin.getConfig().get("ArcherGames.startbook.author", "Server"));
 		String[] strs = new String[10];
 		strs[0] = "Welcome to §4§nArcherGames!\n§0This book will tell you all about:\n§0How to play\n§0Commands\n§0General Info\n§0Kits\n§0Voting";
 		strs[1] = "§1§nHow to play:\n§0You will first need to pick a kit. To do this type /listkits to display all avalible kits. From there type /kit [kitname]. All kits come with a bow. Premium kits can be bought from: http://eyeofender.com/archergames.html\n§0\nYour aim is to survive with your trusty bow and plant a arrow through your enemies skull and kill them! Be the last man standing and win §4$50,000!";
 		strs[2] = "§1§nCommands:\n§0\n/chunk\n/ride\n/track <player> (§4Donors Only§0)\n/money\n/baltop\n/credtop\n/stats [players]\n/credits\n/commands\n/help\n/time";
 		strs[3] = "§1§nGeneral Info: ArcherGames is based around bows. All kits come with a bow and arrows. This gametype will help you master archery.\n§0\nOwners name-tags are: §4RED\n§0Moderator name tags are: §eYELLOW\n§0God and Ridiculous name-tags are: §gGOLD\n§0Elite name-tags are §5PURPLE\n§0VIP name-tags are: §aGREEN\n§0Donor name-tags are: §9LIGHT BLUE\n§0\nFor more info, visit http://www.eyeofender.com/";
 		strs[4] = "§1§nKits: In ArcherGames, you have free kits and premium kits. Premium kits are be bought off: http://www.eyeofender.com/ The popular premium kits are: God\nRidiculous\nElite-Diamond\nElite-Tank\nDonor-infinity\nVIP-Sharpshooter\nThe popular free kits are:\nFire\nExplode\n§0\nTo put these kits on, type /kit <kitname>. If you didn’t purchase the kit then you can’t put it on and play with it!";
 		strs[5] = "§1§nVoting: For voting for ArcherGames, you will receive money which then you will be able to spend on armour, food and enchantments! Voting links:\n§0\n§4http://ow.ly/cpQI0\n§6http://ow.ly/cmwer\n§9http://ow.ly/cmnPu\n§0http://ow.ly/cmDIF\n§3http://ow.ly/cmP18\n§5http://ow.ly/cmETB\n§8http://ow.ly/dVcsF\n§9http://ow.ly/eggLe";
 		plugin.getConfig().set("ArcherGames.startbook.pages", plugin.getConfig().get("ArcherGames.startbook.pages", strs));
 		plugin.getConfig().set("ArcherGames.mysql.username", plugin.getConfig().get("ArcherGames.mysql.username", ""));
 		plugin.getConfig().set("ArcherGames.mysql.password", plugin.getConfig().get("ArcherGames.mysql.password", ""));
 		plugin.getConfig().set("ArcherGames.mysql.port", plugin.getConfig().get("ArcherGames.mysql.port", 3306));
 		plugin.getConfig().set("ArcherGames.mysql.database", plugin.getConfig().get("ArcherGames.mysql.database", ""));
		plugin.getConfig().set("ArcherGames.mysql.hostname", plugin.getConfig().get("ArcherGames.mysql.database", ""));
 		plugin.saveConfig();
 		
 		
 		plugin.voteSites = (java.util.List<String>) plugin.getConfig().getList("ArcherGames.vote.sites");
 		plugin.scheduler.preGameCountdown = plugin.getConfig().getInt("ArcherGames.timers.preGameCountdown");
 		plugin.scheduler.gameInvincibleCountdown = plugin.getConfig().getInt("ArcherGames.timers.gameInvincibleCountdown");
 		plugin.scheduler.gameOvertimeCountdown = plugin.getConfig().getInt("ArcherGames.timers.gameOvertimeCountdown");
 		plugin.scheduler.minPlayersToStart = plugin.getConfig().getInt("ArcherGames.game.minPlayersToStart");
 		plugin.scheduler.shutdownTimer = plugin.getConfig().getInt("ArcherGames.game.shutdownTimer");
 		plugin.scheduler.nagTime = plugin.getConfig().getInt("ArcherGames.timers.nagTime");
 		plugin.scheduler.overtimeWorldRadius = plugin.getConfig().getInt("ArcherGames.game.overtimeWorldRadius");
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
 		plugin.configToggles.put("lockdownMode", plugin.getConfig().getBoolean("ArcherGames.toggles.lockdownMode"));
 		plugin.getConfig().set("ArcherGames.toggles.lockdownMode", false);
 		plugin.saveConfig();
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
 		plugin.strings.put("nocommand", plugin.getConfig().getString("ArcherGames.strings.nocommand"));
 		plugin.strings.put("noblockediting", plugin.getConfig().getString("ArcherGames.strings.noblockediting"));
 		plugin.strings.put("nochestediting", plugin.getConfig().getString("ArcherGames.strings.nochestediting"));
 		plugin.strings.put("nodroppickup", plugin.getConfig().getString("ArcherGames.strings.nodroppickup"));
 		plugin.strings.put("playersleft", plugin.getConfig().getString("ArcherGames.strings.playersleft"));
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
