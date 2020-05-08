 package me.derflash.plugins.sgbridge;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
//import net.minecraft.server.WorldServer;
 
//import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.plugin.RegisteredListener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.onarandombox.MultiverseCore.MultiverseCore;
 import com.onarandombox.MultiverseCore.api.MVWorldManager;
 import com.skitscape.survivalgames.Game;
 import com.skitscape.survivalgames.Game.GameMode;
 import com.skitscape.survivalgames.GameManager;
 import com.skitscape.survivalgames.SettingsManager;
 
 public class SGBridge extends JavaPlugin implements Listener {
 
 	Pattern resetPattern = Pattern.compile("Arena [0-9]? reset\\.");
 	Pattern resetDonePattern = Pattern.compile("Arena reset done: [0-9]?\\.");
 
 	/**
 	 * The log file tailer
 	 */
 	private BufferedWriter logOut = null;
 
 	/*
 	 * private World _spawn = null; private BukkitWorld _bWorld = null; private
 	 * WorldGuardPlugin _wgPlugin = null; private RegionManager _rm = null;
 	 * private WorldEditPlugin _wePlugin = null;
 	 */
 
 	private MVWorldManager _wManager = null;
 	private GameManager _gm = null;
 
 	public Logger log = null;
 	public YamlConfiguration bridge = null;
 	File bridgeFile = null;
 
 	private int checkTimer = -1;
 	
 	private HashSet<Integer> activeArenas = new HashSet<Integer>();
 	private HashSet<Integer> startingArenas = new HashSet<Integer>();
 	private MultiverseCore _mwCore = null;
 
 	// private HashSet<Game> diabledArenas = new HashSet<Game>();
 
 	public MultiverseCore mvCore() {
 		if (_mwCore == null) {
 			_mwCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
 
 		}
 		return _mwCore;
 	}
 	
 	public MVWorldManager mvWManager() {
 		if (_wManager == null) {
 			MultiverseCore mvCore = mvCore();
 			if (mvCore == null) {
 				log.warning("MultiVerse plugin not found!");
 			} else {
 				_wManager = mvCore.getMVWorldManager();
 			}
 		}
 		return _wManager;
 	}
 
 	/*
 	 * public RegionManager rm() { if (_rm == null) { _rm =
 	 * wgPlugin().getGlobalRegionManager().get(spawn()); if (_rm == null) {
 	 * log.info("[CNMyZone] RegionManager not found"); this.setEnabled(false);
 	 * return null; } } return _rm; }
 	 * 
 	 * 
 	 * public WorldGuardPlugin wgPlugin() { if (_wgPlugin == null) { _wgPlugin =
 	 * (WorldGuardPlugin)
 	 * getServer().getPluginManager().getPlugin("WorldGuard"); if (_wgPlugin ==
 	 * null) { log.info("[CNMyZone] WGPlugin not found");
 	 * this.setEnabled(false); return null; } } return _wgPlugin; }
 	 * 
 	 * public World spawn() { if (_spawn == null) { _spawn =
 	 * Bukkit.getWorld("spawn"); if (_spawn == null) {
 	 * log.info("[SGAddon] World spawn not found"); this.setEnabled(false);
 	 * return null; } } return _spawn; }
 	 * 
 	 * 
 	 * public BukkitWorld bSpawn() { if (_bWorld == null) { _bWorld = new
 	 * BukkitWorld(spawn()); if (_bWorld == null) {
 	 * log.info("[CNMyZone] BWorld nova not found"); this.setEnabled(false);
 	 * return null; } } return _bWorld; }
 	 * 
 	 * public WorldEditPlugin wePlugin() { if (_wePlugin == null) { _wePlugin =
 	 * (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
 	 * if (_wePlugin == null) { log.info("[CNMyZone] WEPlugin not found");
 	 * this.setEnabled(false); return null; } } return _wePlugin; }
 	 */
 
 	public GameManager gamemanager() {
 		if (_gm == null) {
 			_gm = GameManager.getInstance();
 		}
 		return _gm;
 	}
 
 	public void onDisable() {
 		if (checkTimer > -1) getServer().getScheduler().cancelTask(checkTimer);
 		
 		try {
 			logOut.flush();
 			logOut.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void onEnable() {
 		log = getServer().getLogger();
 
 		File dFolder = getDataFolder();
 		if (!dFolder.exists())
 			dFolder.mkdirs();
 
 		bridgeFile = new File(dFolder, "bridge.yml");
 		if (bridgeFile.exists())
 			bridge = YamlConfiguration.loadConfiguration(bridgeFile);
 		else {
 			bridge = new YamlConfiguration();
 		}
 
 		try {
 			logOut = new BufferedWriter(new FileWriter(new File(
 					getDataFolder(), "bridge.log"), true));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		getServer().getPluginManager().registerEvents(this, this);
 
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				GameManager gm = gamemanager();
 				if (gm == null)
 					return;
 
 				for (final Game game : gm.getGames()) {
 					int gameID = game.getID();
 					
 					boolean changed = false;
 					int _ap = game.getActivePlayers();
 					int _ip = game.getInactivePlayers();
 					if (bridge.getInt("arena" + gameID + ".activePlayers") != _ap) {
 						bridge.set("arena" + gameID + ".activePlayers", _ap);
 						changed = true;
 					}
 					if (bridge.getInt("arena" + gameID + ".inactivePlayers") != _ip) {
 						bridge.set("arena" + gameID + ".inactivePlayers", _ip);
 						changed = true;
 					}
 					if (changed) save();
 					
 					
 					if (!startingArenas.contains(gameID) && game.getGameMode() == GameMode.STARTING) {
 						startingArenas.add(gameID);
 
 						getServer().broadcastMessage( ChatColor.AQUA + "Arena " + gameID + " ist bereit und startet in " + game.getCountdownTime() + " Sekunden!");
 
 						bridge.set("arena" + gameID + ".status", "starting");
 						bridge.set("arena" + gameID + ".countdownSince", new Date().getTime());
 						bridge.set("arena" + gameID + ".countdownTime", game.getCountdownTime());
 						save();
 
 					} else if (startingArenas.contains(gameID) && game.getGameMode() == GameMode.INGAME) {
 						startingArenas.remove(gameID);
 
 						bridge.set("arena" + gameID + ".countdownSince", null);
 						bridge.set("arena" + gameID + ".countdownTime", null);
 						save();
 
 					}
 					
 					if (!activeArenas.contains(gameID) && game.getGameMode() == GameMode.INGAME) {
 						activeArenas.add(gameID);
 						
 						bridge.set("arena" + gameID + ".status", "ingame");
 						bridge.set("arena" + gameID + ".runningSince", new Date().getTime());
 						save();
 						
 					} else if (activeArenas.contains(gameID) && game.getGameMode() == GameMode.WAITING) {
 						activeArenas.remove(gameID);
 						foundGameToReset(game);
 						
 					}
 					
 					if (game.getGameMode() == GameMode.DISABLED && !bridge.getString("arena" + gameID + ".status").equals("resetting")) {
 						bridge.set("arena" + gameID + ".status", "disabled");
 						save();
 						
 					} else if (bridge.getString("arena" + gameID + ".status").equals("disabled") && game.getGameMode() != GameMode.DISABLED) {
 						bridge.set("arena" + gameID + ".status", "free");
 						save();
 					}
 					
 				}
 			}
 		}, 20L, 20L);
 		
 	}
 
 	public void addLogEntry(String line) {
 		try {
 			logOut.write(line);
 			logOut.newLine();
 			logOut.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean save() {
 		if (!bridgeFile.exists()) {
 			bridgeFile.getParentFile().mkdirs();
 		}
 
 		try {
 			bridge.save(bridgeFile);
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	static public boolean deleteDirectory(File path) {
 		if (path.exists()) {
 			File[] files = path.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				if (files[i].isDirectory()) {
 					deleteDirectory(files[i]);
 				} else {
 					files[i].delete();
 				}
 			}
 		}
 		return (path.delete());
 	}
 	
 	@EventHandler
 	public void onPlayerJoin (PlayerJoinEvent event) {
 		final Player player = event.getPlayer();
         World safeWorld = getServer().getWorld("spawn");
         mvCore().getSafeTTeleporter().safelyTeleport(null, player, safeWorld.getSpawnLocation(), true);
 	}
 	
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onWorldUnload (WorldUnloadEvent event) {
 		for (RegisteredListener h : event.getHandlers().getRegisteredListeners()) {
 			System.out.println("Found WorldUnloadEvent Listener " + h.getPlugin().getName());
 		}
 		
 		System.out.println("Found WorldUnloadEvent: " + (event.isCancelled() ? "cancelled" : "go"));
 	}
 
 	
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 
 		Player player = null;
 		
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if (player != null && !player.hasPermission("sgbridge.admin"))
 			return true;
 
 		if (args.length != 2) {
 			if (player != null) player.sendMessage("Arena ID angeben!");
 			else System.out.println("Arena ID angeben!");
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("reset")) {
 			int _id = -1;
 			try {
 				_id = Integer.parseInt(args[1]);
 			} catch (Exception e) {
 			}
 
 			if (_id == -1) {
 				if (player != null) player.sendMessage("Arena ID als Zahl angeben!");
 				else System.out.println("Arena ID als Zahl angeben!");
 				return true;
 			}
 
 			GameManager gm = gamemanager();
 			Game game = gm.getGame(_id);
 
 			if (game == null) {
 				if (player != null) player.sendMessage("Korrekte Arena ID angeben!");
 				else System.out.println("Korrekte Arena ID angeben!");
 				return true;
 			}
 
 			foundGameToReset(game);
 
 		} else if (args[0].equalsIgnoreCase("disable")) {
 			int _id = -1;
 			try {
 				_id = Integer.parseInt(args[1]);
 			} catch (Exception e) {
 			}
 
 			if (_id == -1) {
 				if (player != null) player.sendMessage("Arena ID als Zahl angeben!");
 				else System.out.println("Arena ID als Zahl angeben!");
 				return true;
 			}
 
 			GameManager gm = gamemanager();
 			Game game = gm.getGame(_id);
 
 			if (game == null) {
 				if (player != null) player.sendMessage("Korrekte Arena ID angeben!");
 				else System.out.println("Korrekte Arena ID angeben!");
 				return true;
 			}
 			
 			game.disable();
 
 		} else if (args[0].equalsIgnoreCase("enable")) {
 			int _id = -1;
 			try {
 				_id = Integer.parseInt(args[1]);
 			} catch (Exception e) {
 			}
 
 			if (_id == -1) {
 				if (player != null) player.sendMessage("Arena ID als Zahl angeben!");
 				else System.out.println("Arena ID als Zahl angeben!");
 				return true;
 			}
 
 			GameManager gm = gamemanager();
 			Game game = gm.getGame(_id);
 
 			if (game == null) {
 				if (player != null) player.sendMessage("Korrekte Arena ID angeben!");
 				else System.out.println("Korrekte Arena ID angeben!");
 				return true;
 			}
 			
 			game.enable();
 
 		}
 
 		return true;
 	}
 
 	protected void foundGameToReset(final Game game) {
 		addLogEntry("-- Resetting Arena " + game.getID() + " [ " + new Date() + " ] --");
 		final int arenaID = game.getID();
 		final File worldFolder = getServer().getWorld("arena" + arenaID).getWorldFolder();
 
 		bridge.set("arena" + arenaID + ".status", "resetting");
 		save();
 
 		addLogEntry("Disabling arena");
 		game.disable();
 
		getServer().broadcastMessage(ChatColor.AQUA + "Arena " + arenaID + " wird nun zurückgesetz! Einen kleinen Moment bitte...");
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			//@SuppressWarnings("unchecked")
 			public void run() {
 				
 				// wait to finish the reset
 				int maxWait = 10;
 				while (game.getMode() != GameMode.DISABLED) {
 					if (maxWait == 0) {
 						getServer()
 								.broadcastMessage(
 										ChatColor.AQUA
 												+ "Arena "
 												+ arenaID
												+ " konnte leider nicht zurückgesetzt werden. Bitte informiert einen Admin. Danke!");
 						bridge.set("arena" + arenaID + ".status", "defect");
 						save();
 						return;
 					}
 					
 					try { Thread.sleep(1000); } catch (InterruptedException e) {}
 					
 					maxWait--;
 				}
 				
 				// bugfix
				/*
 				mvWManager().removePlayersFromWorld("arena" + arenaID);
 				
 				World arenaWorld = Bukkit.getWorld("arena" + arenaID);
 		        WorldServer arenaWorldHandle = ((org.bukkit.craftbukkit.CraftWorld) arenaWorld).getHandle();
 
 				World spawnWorld = Bukkit.getWorld("spawn");
 		        WorldServer spawnWorldHandle = ((org.bukkit.craftbukkit.CraftWorld) spawnWorld).getHandle();
 
 		        spawnWorldHandle.players.addAll(arenaWorldHandle.players);
 		        arenaWorldHandle.players.clear();
		        */
 
 
 				addLogEntry("Unloading arena world");
 				if (!mvWManager().unloadWorld("arena" + arenaID)) {
 					addLogEntry("Failed!");
 					// http://forums.bukkit.org/threads/unloadworld-returning-false.92367/
 					return;
 				}
 				
 				
 				try { Thread.sleep(1000); } catch (InterruptedException e) {}
 
 				addLogEntry("Deleting arena world");
 				if (!deleteDirectory(worldFolder)) {
 					addLogEntry("Failed!");
 					return;
 				}
 
 				try { Thread.sleep(1000); } catch (InterruptedException e) {}
 
 				addLogEntry("Unzipping new arena world");
 				Unzip.unzip(new File(getDataFolder(), "arena" + arenaID
 						+ ".zip"), worldFolder.getAbsolutePath());
 
 				try { Thread.sleep(1000); } catch (InterruptedException e) {}
 
 				addLogEntry("Loading arena world");
 				if (!mvWManager().loadWorld("arena" + arenaID)) {
 					addLogEntry("Failed!");
 					return;
 				}
 
 				try { Thread.sleep(1000); } catch (InterruptedException e) {}
 
 				addLogEntry("Enabling arena");
 				game.enable();
 				
 				long rs = bridge.getLong("arena" + arenaID + ".runningSince");
 				long nowTime = new Date().getTime();
 				if (rs > 0) {
 					long duration = nowTime - rs;
 					long lastDuration = bridge.getLong("arena" + arenaID + ".avgDuration");
 					if (lastDuration > 0) {
 						duration = (duration + lastDuration) / 2;
 					}
 					
 					bridge.set("arena" + arenaID + ".avgDuration", duration);
 				}
 
 				bridge.set("arena" + arenaID + ".maxplayers", SettingsManager.getInstance().getSpawnCount(arenaID));
 				bridge.set("arena" + arenaID + ".countdownSince", null);
 				bridge.set("arena" + arenaID + ".countdownTime", null);
 				bridge.set("arena" + arenaID + ".runningSince", null);
 				bridge.set("arena" + arenaID + ".lastReset", nowTime);
 				bridge.set("arena" + arenaID + ".status", "free");
 				bridge.set("arena" + arenaID + ".name", mvWManager().getMVWorld("arena"+arenaID).getAlias());
 				save();
 
 				getServer().broadcastMessage( ChatColor.GREEN + "Arena " + arenaID + " ist nun wieder bereit! Viel Spass!");
 			}
 		}, 20L);
 
 
 	}
 }
