 package com.BASeCamp.SurvivalChests;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.concurrent.Callable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.server.v1_4_6.NBTTagList;
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.*;
 
 import BASeCamp.Configuration.INIFile;
 
 public class BCRandomizer extends JavaPlugin {
 	// new method:
 
 	// preparegame sets the game to accept players.
 	// joingame willl be issued by players that wish to play.
 	// startgame begins that game.
 
 	// implementation: startgame will use a pre-existing list of players that
 	// used /joingame.
 	// the list will be cleared by preparegame.
 
 	// PLUGIN todolist: Sometimes games get "stuck.". stopallgames doesn't
 	// appear to fix it. investigate this and also make it actually show a
 	// message
 	// to the issuer (or the entire server) indicating the game has been
 	// cancelled.
 	// possibly add tpall command to teleport all players or something.
 	public static Configurator Configuration = null;
 	public static VictoryTracker Victories = null;
 	public static final String Prefix = ChatColor.WHITE + "[" + ChatColor.GOLD
 			+ "BCSurv" + ChatColor.WHITE + "]";
 	public final static String PluginName = "BCRandomizer";
 	public static String pluginMainDir = "./plugins/BCRandomizer";
 	// TODO: use plugin.yml to retrieve configuration data- more precisely, to
 	// retrieve the file to use for the randomization data.
 	// Another idea is to allow that to be changed via other commands, possibly
 	// referring to files in the plugin folder.
 	public static String pluginConfigLocation = pluginMainDir
 			+ "/survivalchests.cfg";
 	public static String SpawnerConfigLocation = pluginMainDir
 			+ "/spawners.cfg";
 	public RandomizerCommand Randomcommand = null;
 	public LinkedList<GameTracker> ActiveGames = new LinkedList<GameTracker>();
 
 	public GameTracker getGame(Player p) {
 		// returns true of the player is a participant or spectator of a game.
 		// null otherwise.
 		GameTracker participant = isParticipant(p);
 		if (participant != null)
 			return participant;
 		GameTracker spectator = isSpectator(p);
 		return spectator;
 
 	}
 
 	public GameTracker isParticipant(Player testplayer) {
 		for (GameTracker gt : ActiveGames) {
 
 			if (gt.getStillAlive().contains(testplayer))
 				return gt;
 
 		}
 		return null;
 
 	}
 
 	public GameTracker isSpectator(Player testplayer) {
 		for (GameTracker gt : ActiveGames) {
 			if (gt.getSpectating().contains(testplayer))
 				return gt;
 		}
 		return null;
 	}
 
 	// public GameTracker _Tracker = null;
 	public static void clearPlayerInventory(Player p) {
 
 		((CraftPlayer) p).getHandle().inventory.b(new NBTTagList());
 
 	}
 
 	public void ExtinguishFlames(final World exWorld) {
 
 		Bukkit.getScheduler().callSyncMethod(this, new Callable<Integer>() {
 
 			public Integer call() {
 
 				for (Chunk iteratechunk : exWorld.getLoadedChunks()) {
 
 					for (int x = 0; x < 15; x++) {
 						for (int y = 0; y < 255; y++)
 							for (int z = 0; z < 15; z++) {
 								// get block at this position.
 								Block getblock = iteratechunk.getBlock(x, y, z);
 								if (getblock.getType().equals(Material.FIRE)) {
 
 									// extinguish
 
 									Block blockbelow = iteratechunk.getBlock(x,
 											y - 1, z);
 									if (!blockbelow.getType().equals(
 											Material.NETHERRACK)) {
 										getblock.setType(Material.AIR);
 
 									}
 
 								}
 
 							}
 					}
 
 				}
 				return 0;
 			}
 		});
 
 	}
 
 	public Scanner acquireStream(String SourceName) {
 
 		try {
 			if (SourceName != "") {
 
 				try {
 					return new Scanner(new URL(SourceName).openStream());
 
 				} catch (Exception urlexception) {
 
 					// String pathgrab =
 					// SourceName.substring(0,SourceName.lastIndexOf(File.separatorChar));
 					// System.out.println("pathgrab=" + pathgrab);
 					try {
 						return new Scanner(new File(SourceName));
 
 					} catch (Exception directexception) {
 						//didn't even work directly. Let's look in the PluginFolder.
 						String uselocation = BCRandomizer.pluginMainDir;
 						if(!uselocation.endsWith("\\")){
 							uselocation=uselocation+"\\";
 							
 						}
 						return new Scanner(new File(uselocation + SourceName));
 						
 					}
 
 				}
 
 			}
 		} catch (Exception exx) {
 			// attempt to read it from our jar.
 			// first we only want the filename.
 
 			Bukkit.getLogger().log(Level.INFO,
 					"BCSURV:" + SourceName + " Not found. Reading from JAR...");
 			// Bukkit.getLogger().log(Level.WARNING,
 			// "Warning: Config/List file not found at " +
 			// BCRandomizer.pluginConfigLocation + " using built in.");
 			// sc = new
 			// Scanner(getClass().getClassLoader().getResourceAsStream("survivalchests.cfg"));
 			String onlyfname = new File(SourceName).getName();
 			InputStream makeresult = null;
 
 			makeresult = getClass().getClassLoader().getResourceAsStream(
 					onlyfname);
 
 			if (makeresult == null)
 				makeresult = getClass().getClassLoader().getResourceAsStream(
 						SourceName);
 
 			return makeresult == null ? null : new Scanner(makeresult);
 
 		}
 
 		String onlyfile = SourceName.substring(SourceName
 				.lastIndexOf(File.separatorChar) + 1);
 		// System.out.println("Onlyfile=" + onlyfile);
 
 		return new Scanner(this.getClass().getClassLoader()
 				.getResourceAsStream(onlyfile));
 	}
 
 	public static String getItemMaterial(ItemStack item) {
 
 		Material mat = item.getType();
 		if (mat.equals(Material.LEATHER_HELMET)
 				|| mat.equals(Material.LEATHER_CHESTPLATE)
 				|| mat.equals(Material.LEATHER_LEGGINGS)
 				|| mat.equals(Material.LEATHER_BOOTS))
 			return "Leather";
 		else if (mat.equals(Material.WOOD_SPADE)
 				|| mat.equals(Material.WOOD_AXE)
 				|| mat.equals(Material.WOOD_SWORD)
 				|| mat.equals(Material.WOOD_PICKAXE))
 			return "Wood";
 		else if (mat.equals(Material.CHAINMAIL_HELMET)
 				|| mat.equals(Material.CHAINMAIL_CHESTPLATE)
 				|| mat.equals(Material.CHAINMAIL_LEGGINGS)
 				|| mat.equals(Material.CHAINMAIL_BOOTS))
 			return "Chainmail";
 
 		else if (mat.equals(Material.IRON_HELMET)
 				|| mat.equals(Material.IRON_CHESTPLATE)
 				|| mat.equals(Material.IRON_LEGGINGS)
 				|| mat.equals(Material.IRON_BOOTS)
 				|| mat.equals(Material.IRON_SPADE)
 				|| mat.equals(Material.IRON_AXE)
 				|| mat.equals(Material.IRON_SWORD)
 				|| mat.equals(Material.IRON_PICKAXE))
 			return "Iron";
 		else if (mat.equals(Material.GOLD_HELMET)
 				|| mat.equals(Material.GOLD_CHESTPLATE)
 				|| mat.equals(Material.GOLD_LEGGINGS)
 				|| mat.equals(Material.GOLD_BOOTS)
 				|| mat.equals(Material.GOLD_SPADE)
 				|| mat.equals(Material.GOLD_AXE)
 				|| mat.equals(Material.GOLD_SWORD)
 				|| mat.equals(Material.GOLD_PICKAXE))
 			return "Gold";
 		else if (mat.equals(Material.DIAMOND_HELMET)
 				|| mat.equals(Material.DIAMOND_CHESTPLATE)
 				|| mat.equals(Material.DIAMOND_LEGGINGS)
 				|| mat.equals(Material.DIAMOND_BOOTS)
 				|| mat.equals(Material.DIAMOND_SPADE)
 				|| mat.equals(Material.DIAMOND_AXE)
 				|| mat.equals(Material.DIAMOND_SWORD)
 				|| mat.equals(Material.DIAMOND_PICKAXE))
 			return "Diamond";
 		else
 			return "";
 
 	}
 
 	public static void VanishPlayer(Player invisify) {
 
 		for (Player p : Bukkit.getOnlinePlayers()) {
 
 			p.hidePlayer(invisify);
 
 		}
 
 	}
 
 	public static void unvanishPlayer(Player visify) {
 
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			p.showPlayer(visify);
 		}
 
 	}
 
 	public BCRandomizer() {
 
 		Victories = new VictoryTracker(this);
 
 	}
 
 	public static Player getNearestPlayer(Location testlocation) {
 
 		World applyworld = testlocation.getWorld();
 
 		double MinimumDistance = Double.MAX_VALUE;
 		Player minimumPlayer = null;
 		// iterate through all the players in this world.
 		for (Player p : applyworld.getPlayers()) {
 			double gotdistance = p.getLocation().distance(testlocation);
 			if (gotdistance < MinimumDistance) {
 				MinimumDistance = gotdistance;
 				minimumPlayer = p;
 			}
 		}
 
 		return minimumPlayer;
 
 	}
 
 	@Override
 	public void onEnable() {
 		try {
 			Configuration = new Configurator(this);
			System.out.println(Configuration.getContainerStatic().toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Randomcommand = new RandomizerCommand(this);
 		PluginCommand batchcommand = this.getCommand("repopchests");
 
 		batchcommand.setExecutor(Randomcommand);
 
 		PluginCommand delayPvP = this.getCommand("startgame");
 		delayPvP.setExecutor(Randomcommand);
 
 		// PluginCommand teamsplit = this.getCommand("teamsplit");
 		// teamsplit.setExecutor(Randomcommand);
 
 		PluginCommand friendly = this.getCommand("friendly");
 		friendly.setExecutor(Randomcommand);
 
 		this.getCommand("preparegame").setExecutor(Randomcommand);
 		this.getCommand("joingame").setExecutor(Randomcommand);
 		this.getCommand("spectategame").setExecutor(Randomcommand);
 		this.getCommand("gamestate").setExecutor(Randomcommand);
 		this.getCommand("arenaborder1").setExecutor(Randomcommand);
 		this.getCommand("arenaborder2").setExecutor(Randomcommand);
 		this.getCommand("clearborders").setExecutor(Randomcommand);
 		this.getCommand("randomizespawners").setExecutor(Randomcommand);
 		this.getCommand("mobmode").setExecutor(Randomcommand);
 		this.getCommand("mobtimeout").setExecutor(Randomcommand);
 		this.getCommand("mobsweeper").setExecutor(Randomcommand);
 		// this.getCommand("setfly").setExecutor(Randomcommand);
 		// this.getCommand("prepareinfo").setExecutor(Randomcommand);
 		// this.getCommand("fixup").setExecutor(Randomcommand);
 		// this.getCommand("strike").setExecutor(Randomcommand);
 		ChestRandomizer.reload(this, null);
 	}
 
 	@Override
 	public void onDisable() {
 		try {
 		Configuration.save();
 		}
 		catch(IOException iox){
 			
 			iox.printStackTrace();
 			
 		}
 		for (GameTracker t : ActiveGames) {
 			t.gamecomplete = true;
 			t.deathwatcher._Trackers = null;
 
 		}
 
 	}
 
 	private static final boolean _debug = false;
 
 	public static void emitmessage(String msg) {
 
 		if (_debug)
 			System.out.println(msg);
 
 	}
 
 	public static void main(String[] args) {
 
 		SpawnRandomizerConfig src = new SpawnRandomizerConfig(new File(
 				"D:\\testspawndata.xml"));
 		System.out.println(src.SpawnData.size());
 
 		// ChestRandomizer.reloadXML(null);
 		System.out.println(ChestRandomizer.getRandomData().size()
 				+ " RandomData items...");
 		System.out.println(ChestRandomizer.getStaticData().size()
 				+ " Static Data items...");
 
 	}
 
 	public GameTracker getWorldGame(World testworld) {
 
 		for (GameTracker gt : ActiveGames) {
 			if (gt.getStillAlive().size() > 0) {
 				if (gt.getStillAlive().getFirst().getWorld().equals(testworld)) {
 
 					return gt;
 
 				}
 			}
 		}
 		return null;
 
 	}
 
 	public GameTracker getPlayerGame(Player useplayer) {
 		// TODO Auto-generated method stub
 
 		for (GameTracker gt : ActiveGames) {
 
 			if (gt.getStillAlive().contains(useplayer)
 					|| gt.getSpectating().contains(useplayer)
 					|| gt.getDead().contains(useplayer)) {
 
 				return gt;
 
 			}
 
 		}
 		return null;
 
 	}
 
 	public static String getHoliday(Date date) {
 		// returns null if it isn't a Holiday
 		Calendar caluse = Calendar.getInstance();
 		caluse.setTime(date);
 		return getHoliday(caluse);
 	}
 
 	private static HashMap<String, String> HolidayLookup = null; // we use
 																	// locale-inspecific
 																	// method,
 																	// MM/DD
 
 	public static String getHoliday(Calendar cal) {
 
 		if (HolidayLookup == null) {
 			// initialize holiday hashmap.
 
 			HolidayLookup = new HashMap<String, String>();
 			HolidayLookup.put("0/1", "New Years");
 			HolidayLookup.put("5/1", "Canada Day(Canada)");
 			HolidayLookup.put("5/4", "Independence Day(US)");
 
 		}
 		// create String representation of calendar...
 		String uselookup = cal.get(Calendar.MONTH) + "/"
 				+ cal.get(Calendar.DAY_OF_MONTH);
 
 		return HolidayLookup.get(uselookup);
 
 	}
 
 }
