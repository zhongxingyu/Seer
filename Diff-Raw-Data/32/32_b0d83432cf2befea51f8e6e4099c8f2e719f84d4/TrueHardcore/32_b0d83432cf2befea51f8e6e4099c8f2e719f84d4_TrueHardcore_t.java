 package au.com.addstar.truehardcore;
 /*
 * TrueHardcore
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.FileHandler;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import net.milkbowl.vault.permission.Permission;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.vanish.VanishManager;
 import org.kitteh.vanish.VanishPlugin;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 import org.kitteh.vanish.staticaccess.VanishNotLoadedException;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.lwc.LWCPlugin;
 import com.griefcraft.model.Protection;
 import com.wimbli.WorldBorder.BorderData;
 import com.wimbli.WorldBorder.WorldBorder;
 
 import de.diddiz.LogBlock.CommandsHandler.CommandClearLog;
 import de.diddiz.LogBlock.CommandsHandler.CommandRollback;
 import de.diddiz.LogBlock.LogBlock;
 import de.diddiz.LogBlock.QueryParams;
 
 import au.com.addstar.truehardcore.HardcorePlayers.*;
 
 public final class TrueHardcore extends JavaPlugin {
 	public static TrueHardcore instance;
 	
 	public static Economy econ = null;
 	public static Permission perms = null;
 	public static Chat chat = null;
 	public boolean VaultEnabled = false;
 	public boolean DebugEnabled = false;
 	public List<String> RollbackCmds = null;
 	public boolean GameEnabled = true;
 	
 	private static final Logger logger = Logger.getLogger("Minecraft");
 	private static final Logger debuglog = Logger.getLogger("DebugLog");
 	private FileHandler debugfh;
 	
 	public ConfigManager cfg = new ConfigManager(this);
 	public PluginDescriptionFile pdfFile = null;
 	public PluginManager pm = null;
 
 	public Database dbcon = null;
 	public String DBHost;
 	public String DBPort;
 	public String DBName;
 	public String DBUser;
 	public String DBPass;
 
 	private Boolean LWCHooked = false;
 	private Boolean LBHooked = false;
 	private Boolean WBHooked = false;
 	private Boolean VNPHooked = false;
 	private LWC lwc;
 	private LogBlock logblock;
 	private WorldBorder wb;
 	private VanishManager vnp;
 
 	// Hardcore worlds
 	public Map<String, HardcoreWorld> HardcoreWorlds = new HashMap<String, HardcoreWorld>();
 	
 	// Data for ALL hardcore players 
 	public HardcorePlayers HCPlayers = new HardcorePlayers();
 
 	// List of ALL players who are allowed to enter a hardcore world
 	public Map<String, List<String>> WhiteList = new HashMap<String, List<String>>();
 	
 	public String Header = ChatColor.DARK_RED + "[" + ChatColor.RED + "TrueHardcore" + ChatColor.DARK_RED + "] " + ChatColor.YELLOW;
 	
 	private final List<Material> SpawnBlocks = Arrays.asList(
 			Material.DIRT, 
 			Material.GRASS,
 			Material.SAND,
 			Material.STONE,
 			Material.COBBLESTONE,
 			Material.BEDROCK,
 			Material.SNOW,
 			Material.SNOW_BLOCK,
 			Material.CLAY,
 			Material.OBSIDIAN,
 			Material.SANDSTONE
 	);
 	
 	@Override
 	public void onEnable(){
 		instance = this;
 
 		// This block configure the logger with handler and formatter  
         try {
 	        debuglog.setUseParentHandlers(false);
 	        debugfh = new FileHandler("plugins/TrueHardcore/debug.log", true);
 	        Util.LogFormatter formatter = new Util.LogFormatter();
 	        debugfh.setFormatter(formatter);
 	        debuglog.addHandler(debugfh);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}  
 
         // Grab that plugin manager!
 		pdfFile = this.getDescription();
 		pm = this.getServer().getPluginManager();
 
 		// Check if vault is loaded (required for economy)
 		VaultEnabled = setupEconomy();
 		if (VaultEnabled) {
 			Log("Found Vault! Hooking for economy!");
 		} else {
 			Log("Vault was not detected! Economy rewards are not available.");
 		}
 		
 		Plugin p = pm.getPlugin("LWC");
     	if (p != null && p instanceof LWCPlugin) {
     		LWCHooked = true;
     		lwc = ((LWCPlugin)p).getLWC();
     		Log("LWC Found, hooking into LWC.");
     	} else {
     		LWCHooked = false;
     		Log("LWC not Found");
     	}
     	
     	p = pm.getPlugin("LogBlock");
     	if (p != null && p instanceof LogBlock) {
     		LBHooked = true;
     		logblock = LogBlock.getInstance();
     		Log("LogBlock found, hooking it.");
     	} else {
     		LBHooked = false;
     		Log("LogBlock not found! This won't work very well...");
     	}
 
     	p = pm.getPlugin("WorldBorder");
     	if (p != null && p instanceof WorldBorder) {
     		WBHooked = true;
     		wb = WorldBorder.plugin;
     		Log("WorldBorder found, hooking it.");
     	} else {
     		WBHooked = false;
     		Log("WorldBorder not found! Spawning will not be limited...");
     	}
 
     	p = pm.getPlugin("VanishNoPacket");
     	if (p != null && p instanceof VanishPlugin) {
     		try {
 				vnp = VanishNoPacket.getManager();
 	    		VNPHooked = true;
 			} catch (VanishNotLoadedException e) {
 				e.printStackTrace();
 			}
     		Log("VanishNoPacket found, hooking it.");
     	} else {
     		Log("VanishNoPacket not found! Will not auto-unvanish...");
     	}
 
     	// Read (or initialise) plugin config file
 		cfg.LoadConfig(getConfig());
 
 		// Save the default config (if one doesn't exist)
 		saveDefaultConfig();
 
 		// Open/initialise the database
 		dbcon = new Database(this);
 		if (dbcon.IsConnected) {
 			Log("Successfully connected to the database.");
 			Log("Loading players from database...");
 			LoadAllPlayers();
 			LoadWhiteList();
 		} else {
 			Log(pdfFile.getName() + " " + pdfFile.getVersion() + " could not be enabled!");
 			this.setEnabled(false);
 			return;
 		}
 		
 		Log("Registering commands and events...");
 		getCommand("truehardcore").setExecutor(new CommandTH(this));
 		getCommand("th").setExecutor(new CommandTH(this));
 
 		pm.registerEvents(new PlayerListener(this), this);
 		Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled");
 	}
 	
 	@Override
 	public void onDisable(){
 		// cancel all tasks we created
         getServer().getScheduler().cancelTasks(this);
 		SaveAllPlayers();
 		
 		// TODO: force leave all players
 		
 		Log(pdfFile.getName() + " has been disabled!");
 		debugfh.close();
 	}
 	
 	/*
 	 * Detect/configure Vault
 	 */
 	private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 	
 	public void Log(String data) {
 		logger.info("[" + pdfFile.getName() + "] " + data);
 		debuglog.info(data);
 	}
 
 	public void Warn(String data) {
 		logger.warning("[" + pdfFile.getName() + "] " + data);
 		debuglog.warning(data);
 	}
 	
 	public void Debug(String data) {
 		if (DebugEnabled) {
 			logger.info("[" + pdfFile.getName() + "] " + data);
 		}
 		debuglog.info(data);
 	}
 
 	// Write data to debug log
 	public void DebugLog(String data) {
 		debuglog.info(data);
 	}
 	
 	public FileConfiguration Config() {
 		return getConfig();
 	}
 	
 	public boolean GiveMoney(String player, int money) {
 		if (VaultEnabled) {
 			EconomyResponse resp = econ.depositPlayer(player, money);
 			if (resp.type == ResponseType.SUCCESS) {
 				Log(player + " has been given $" + resp.amount + " (new balance $" + resp.balance + ")");
 				return true;
 			} else {
 				Warn("Vault payment failed! Error: " + resp.errorMessage);
 			}
 		}
 		return false;
 	}
 	
 	public void DoPlayerDeath(final Player player, PlayerDeathEvent event) {
 		final TrueHardcore plugin = this;
 		final World world = player.getWorld();
 
 		HardcorePlayer hcp = HCPlayers.Get(world, player);
 		hcp.setState(PlayerState.DEAD);
 		hcp.setDeathMsg(event.getDeathMessage());
 		hcp.setDeathPos(player.getLocation());
 		hcp.setDeaths(hcp.getDeaths()+1);
 		hcp.updatePlayer(player);
 		hcp.calcGameTime();
 		
 		String DeathMsg = event.getDeathMessage();
 		DeathMsg = DeathMsg.replaceFirst(player.getName(), ChatColor.AQUA + player.getName() + ChatColor.YELLOW);
 		plugin.getServer().broadcastMessage(Header + DeathMsg + "!");
 		plugin.getServer().broadcastMessage(Header + "Final Score: " + ChatColor.GREEN + player.getTotalExperience());
 		event.setDeathMessage(null);
 		if ((hcp.getTopScore() > 0) && (hcp.getScore() > hcp.getTopScore())) {
 			hcp.setTopScore(hcp.getScore());
 			player.sendMessage(ChatColor.GREEN + "Congratulations! You just beat your personal high score!");
 		}
 		
 		SavePlayer(hcp);
 		Debug("NEW STATE: " + HCPlayers.Get(player).getState());
 
 		// Dont drop XP or items
 		event.setDroppedExp(0);
 		event.getDrops().clear();
 		
 		// Reset XP levels
 		event.setNewExp(0);
 		event.setNewLevel(0);
 		event.setNewTotalExp(0);
 		event.setKeepLevel(false);
 		player.setLevel(0);
 		player.setExp(0);
 		
 		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 			@Override
 			public void run() {
 				try {
 					if (plugin.LWCHooked) {
 						plugin.Debug("Removing LWC locks...");
 				        int count = 0;
 						if (lwc.getPhysicalDatabase() != null) {
 					        List<Protection> prots = lwc.getPhysicalDatabase().loadProtectionsByPlayer(player.getName());
 					        for(Protection prot : prots) {
 					        	if (prot.getWorld().equals(world.getName())) {
 					        		count++;
 		
 					        		// Remove LWC protection
 					        		prot.remove();
 					        		prot.removeCache();
 					        	}
 					        }
 						} else {
 							plugin.Log("WARNING: LWC.getPhysicalDatabase() failed!");
 						}
 				        plugin.Debug("Removed " + count + " LWC protections.");
 					}
 
 					if (LBHooked) {
 						try {
 							final QueryParams params = new QueryParams(logblock);
 							params.setPlayer(player.getName());
 							params.world = world;
 							params.silent = false;
 							params.before = 0;
 							params.excludeVictimsMode = true;
 							params.excludeKillersMode = true;
 
 							final CommandSender cs = plugin.getServer().getConsoleSender();
 
 							if (logblock == null) {
 								plugin.Debug("CRITICAL! logblock handle is null");
 							}
 
 							plugin.Debug("Rollback changes for " + player.getName() + "...");
 							CommandRollback cr = plugin.logblock.getCommandsHandler().new CommandRollback(cs, params, true);
 							cr.close();
 
 							plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 								@Override
 								public void run() {
 									try {
 										plugin.Debug("Clearing changes for " + player.getName() + "...");
 										CommandClearLog ccl = plugin.logblock.getCommandsHandler().new CommandClearLog(cs, params, true);
 										ccl.close();
 									} catch (Exception e) {
 										e.printStackTrace();
 									}
 								}
 							}, 5 * 20L);
 							
 						} catch (Exception e) {
 						    // Do nothing or throw an error if you want
 							e.printStackTrace();
 						}
 					}
 				} catch (Exception e) {
 				    // Do nothing or throw an error if you want
 					e.printStackTrace();
 				}
 			}
 		}, 40L);
 	}
 	
 	public boolean PlayGame(String world, Player player) {
 		if (!IsOnWhiteList(world, player.getName())) {
 			player.sendMessage(ChatColor.RED + "Sorry, you are not allowed to play this world.");
 			return false;
 		}
 
 		if (!GameEnabled) {
 			player.sendMessage(ChatColor.RED + "TrueHardcore is currently disabled.");
 			return false;
 		}
 		
 		HardcoreWorld hcw = HardcoreWorlds.get(world);
 		HardcorePlayer hcp = HCPlayers.Get(world, player.getName());
 		if (hcp != null) {
 			if ((hcp.getState() == PlayerState.DEAD) && (hcp.getGameEnd() != null)) {
 				// Check last death time
 				Date now = new Date();
 				long diff = (now.getTime() - hcp.getGameEnd().getTime()) / 1000;
 				long wait = (hcw.getBantime() - diff);
 				//DebugLog("NOW: " + now.getTime());
 				//DebugLog("DIE: " + hcp.getGameEnd().getTime());
 				//DebugLog("DIFF: " + diff);
 				//DebugLog("WAIT: " + wait);
 				
 				if (wait > 0) {
 					player.sendMessage(ChatColor.RED + "Sorry, you must wait " + Util.Long2Time(wait) + " to play " + hcw.getWorld().getName() + " again.");
 					return false;
 				}
 			}
 		}
 		
 		if ((hcp == null) || (hcp.getState() == PlayerState.DEAD)) {
 				Location spawn = null;
 				World w = getServer().getWorld(world); 
 						
 				// Never played before... create them!
 				if (hcp == null) {
 					Debug("New hardcore player: " + player.getName());
 					hcp = HCPlayers.NewPlayer(world, player.getName());
 					spawn = GetNewLocation(w, 0, 0, hcw.getSpawnDistance());
 				}
 				else if (hcp.getDeathPos() == null) {
 					Warn("No previous position found for known " + player.getName());
 					spawn = GetNewLocation(w, 0, 0, hcw.getSpawnDistance());
 				} else {
 					Debug(player.getName() + " is restarting hardcore");
 					spawn = GetNewLocation(w, hcp.getDeathPos().getBlockX(), hcp.getDeathPos().getBlockZ(), hcw.getSpawnDistance());
 				}
 				
 				if (spawn != null) {
 					//Debug("OLD STATE: " + HCPlayers.Get(world, player.getName()).getState());
 					hcp.setState(PlayerState.IN_GAME);
 					//Debug("NEW STATE: " + HCPlayers.Get(world, player.getName()).getState());
 					if (NewSpawn(player, spawn)) {
 						SetProtected(hcp, hcw.getSpawnProtection());
 						hcp.setGameTime(0);
 						hcp.setChickenKills(0);
 						hcp.setCowKills(0);
 						hcp.setPigKills(0);
 						hcp.setSheepKills(0);
 						hcp.setChickenKills(0);
 						hcp.setCreeperKills(0);
 						hcp.setZombieKills(0);
 						hcp.setSkeletonKills(0);
 						hcp.setSpiderKills(0);
 						hcp.setEnderKills(0);
 						hcp.setSlimeKills(0);
 						hcp.setMooshKills(0);
 						hcp.setOtherKills(0);
 						hcp.setPlayerKills(0);
 						hcp.updatePlayer(player);
 						SavePlayer(hcp);
 						UnvanishPlayer(player);

 						String greeting = HardcoreWorlds.get(world).getGreeting();
 						if ((greeting != null) && (!greeting.isEmpty())) {
 							player.sendMessage(ChatColor.translateAlternateColorCodes('&', greeting));
 						}
 						return true;
 					} else {
 						return false;
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "Unable to find suitable spawn location. Please try again.");
 					return false;
 				}
 		}
 		else if (hcp.getState() == PlayerState.IN_GAME) {
 			player.sendMessage(ChatColor.RED + "You are already playing hardcore!");
 			return false;
 		} else {
 			// Resume existing game
 			hcp.setState(PlayerState.IN_GAME);
 			Debug("NEW STATE: " + HCPlayers.Get(world, player.getName()).getState());
 			JoinGame(world, player);
 			SavePlayer(hcp);
 			UnvanishPlayer(player);
			String greeting = HardcoreWorlds.get(world).getGreeting();
			if ((greeting != null) && (!greeting.isEmpty())) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', greeting));
			}
 			player.sendMessage(ChatColor.GREEN + "Returning to your last hardcore location... good luck!");
 			return true;
 		}
 	}
 	
 	public boolean NewSpawn(Player player, Location spawn) {
 		HardcorePlayer hcp = HCPlayers.Get(spawn.getWorld(), player);
 		
 		if (player.teleport(spawn)) {
 			hcp.setState(PlayerState.IN_GAME);
 			player.setFallDistance(0);
 			player.setHealth(20);
 			player.setFoodLevel(20);
 			player.setAllowFlight(false);
 			player.setFlying(false);
 			player.setExp(0);
 			player.setLevel(0);
 			player.setTotalExperience(0);
 			player.setWalkSpeed(0.2F);
 			player.setFlySpeed(0.2F);
 			player.setGameMode(GameMode.SURVIVAL);
 			player.setOp(false);
 			player.getEnderChest().clear();
 			player.getEquipment().clear();
 			player.getInventory().clear();
 			player.setPassenger(null);
			player.sendMessage(ChatColor.RED + "!!!! WARNING !!!! WARNING !!!!");
			player.sendMessage(ChatColor.RED + "This plugin is highly experimental! Use at own risk!");
			player.sendMessage(ChatColor.RED + "Please report ALL problems in detail.");
 			player.sendMessage(ChatColor.GREEN + "Welcome to TrueHardcore. Good luck on your adventure!");
 			player.sendMessage(ChatColor.GREEN + "Type " + ChatColor.AQUA + "/th leave" + ChatColor.GREEN + " to exit (progress will be saved)");
 			return true;
 		} else {
 			Warn("Teleport failed!");
 			return false;
 		}
 	}
 	
 	public Location GetNewLocation(World world, int oldX, int oldZ, int dist) {
 		Location l = new Location(world, oldX, 255, oldZ);
 		Debug("Selecting spawn point " + dist + " blocks from: " + l.getBlockX() + " / " + l.getBlockY() + " / " + l.getBlockZ());
 
 		double x;
 		double z;
 		int deg;
 		Location nl = null;
 
 		// Only try to find a good place 30 times
 		for (int count = 0; count < 30; count++) {
 			boolean GoodSpawn = false;
 			Location spawn = null;
 			String reason = "";
 
 			// Lets do some trig!!
 			dist = dist + (int) (Math.random() * 100);									// Random radius padding
 			deg = (int) (Math.random() * 360);											// Random degrees
 			x = (dist * Math.cos(Math.toRadians(deg))) + l.getBlockX();  
 			z = (dist * Math.sin(Math.toRadians(deg))) + l.getBlockZ(); 
 			nl = new Location(world, x, 255, z);
 
 			// Get the highest block at the selected location
 			Block b = nl.getBlock();
 			while((b.getType() == Material.AIR) && (b.getY() > 1)) {
 				b = b.getRelative(BlockFace.DOWN);
 			}
 
 			// Get worldborder
 			BorderData bd = null;
 			if (WBHooked) {
 				bd = wb.GetWorldBorder(world.getName());
 			}
 			
 			spawn = new Location(b.getWorld(), b.getX(), b.getY()+2, b.getZ());
 			if (SpawnBlocks.contains(b.getType())) {
 				if (spawn.getBlockX() >= 0) { spawn.setX(spawn.getBlockX() + 0.5); }
 				if (spawn.getBlockX() < 0)  { spawn.setX(spawn.getBlockX() - 0.5); }
 
 				if (spawn.getBlockZ() >= 0) { spawn.setZ(spawn.getBlockZ() + 0.5); }
 				if (spawn.getBlockZ() < 0)  { spawn.setZ(spawn.getBlockZ() - 0.5); }
 
 				// Make sure it's inside the world border (if one exists)
 				if ((bd == null) || bd.insideBorder(spawn)) {
 					GoodSpawn = true;
 					reason = "Allowed block type (" + b.getType() + ")!";
 				} else {
 					reason = "Outside world border";
 				}
 			} else {
 				reason = "Wrong block type (" + b.getType() + ")";
 			}
 			
 			if (GoodSpawn) {
 				Debug("GOOD: "
 						+ Util.padLeft(String.valueOf(spawn.getX()), 9)
 						+ Util.padLeft(String.valueOf(spawn.getY()), 7)
 						+ Util.padLeft(String.valueOf(spawn.getZ()), 9)
 						+ "   (" + dist + " blocks away)" 
 						+ "  => " + reason);
 
 				// Return the good location
 				spawn.setPitch(0F);
 				spawn.setYaw(0F);
 				return spawn;
 			} else {
 				Debug("BAD : "
 						+ Util.padLeft(String.valueOf(spawn.getX()), 9)
 						+ Util.padLeft(String.valueOf(spawn.getY()), 7)
 						+ Util.padLeft(String.valueOf(spawn.getZ()), 9)
 						+ "   (" + dist + " blocks away)"
 						+ "  => " + reason);
 			}
 		}
 		
 		return null;
 	}
 	
 	public void LeaveGame(Player player) {
 		HardcorePlayer hcp = HCPlayers.Get(player);
 		if ((hcp != null) && (hcp.getState() == PlayerState.IN_GAME)) {
 			hcp.setState(PlayerState.ALIVE);
 			hcp.updatePlayer(player);
 			hcp.calcGameTime();
 			SavePlayer(hcp);
 			SendToLobby(player);
 		} else {
 			player.sendMessage(ChatColor.RED + "You are not currently in a hardcore game.");
 		}
 	}
 	
 	public void SavePlayer(HardcorePlayer hcp) {
 		Debug("Saving data for " + hcp.getPlayerName());
 
 		// CowKills, PigKills, SheepKills, ChickenKills;
 		// CreeperKills, ZombieKills, SkeletonKills, SpiderKills, EnderKills, SlimeKills;
 		// OtherKills, PlayerKills;
 
 		String query = "INSERT INTO `truehardcore`.`players` \n" +
 				"(`player`, `world`, `lastpos`, `lastjoin`, `lastquit`, `gamestart`, `gameend`, `gametime`,\n" +
 				"`level`, `exp`, `score`, `topscore`, `state`, `deathmsg`, `deathpos`, `deaths`,\n" +
 				"`cowkills`, `pigkills`, `sheepkills`, `chickenkills`, `creeperkills`, `zombiekills`, `skeletonkills`,\n" +
 				"`spiderkills`, `enderkills`, `slimekills`, `mooshkills`, `otherkills`, `playerkills`)\n\n" +
 				
 				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE \n\n" +
 				
 				"`lastpos`=?, `lastjoin`=?, `lastquit`=?, `gamestart`=?, `gameend`=?, `gametime`=?,\n" +
 				"`level`=?, `exp`=?, `score`=?, `topscore`=?, `state`=?, `deathmsg`=?, `deathpos`=?, `deaths`=?,\n" +
 				"`cowkills`=?, `pigkills`=?, `sheepkills`=?, `chickenkills`=?, `creeperkills`=?, `zombiekills`=?, `skeletonkills`=?,\n" +
 				"`spiderkills`=?, `enderkills`=?, `slimekills`=?, `mooshkills`=?, `otherkills`=?, `playerkills`=?\n";
 				
 		String[] values = { 
 				hcp.getPlayerName().toLowerCase(), 
 				hcp.getLastPos().getWorld().getName(),
 				Util.Loc2Str(hcp.getLastPos()),
 				Util.Date2Mysql(hcp.getLastJoin()),
 				Util.Date2Mysql(hcp.getLastQuit()),
 				Util.Date2Mysql(hcp.getGameStart()),
 				Util.Date2Mysql(hcp.getGameEnd()),
 				String.valueOf(hcp.getGameTime()),
 				String.valueOf(hcp.getLevel()),
 				String.valueOf(hcp.getExp()),
 				String.valueOf(hcp.getScore()),
 				String.valueOf(hcp.getTopScore()),
 				hcp.getState().toString(),
 				hcp.getDeathMsg(),
 				Util.Loc2Str(hcp.getDeathPos()),
 				String.valueOf(hcp.getDeaths()),
 
 				String.valueOf(hcp.getCowKills()),
 				String.valueOf(hcp.getPigKills()),
 				String.valueOf(hcp.getSheepKills()),
 				String.valueOf(hcp.getChickenKills()),
 				String.valueOf(hcp.getCreeperKills()),
 				String.valueOf(hcp.getZombieKills()),
 				String.valueOf(hcp.getSkeletonKills()),
 				String.valueOf(hcp.getSpiderKills()),
 				String.valueOf(hcp.getEnderKills()),
 				String.valueOf(hcp.getSlimeKills()),
 				String.valueOf(hcp.getMooshKills()),
 				String.valueOf(hcp.getOtherKills()),
 				String.valueOf(hcp.getPlayerKills()),
 
 				Util.Loc2Str(hcp.getLastPos()),			// REPEATED FOR UPDATE!
 				Util.Date2Mysql(hcp.getLastJoin()),		//   |
 				Util.Date2Mysql(hcp.getLastQuit()),		//   v
 				Util.Date2Mysql(hcp.getGameStart()),
 				Util.Date2Mysql(hcp.getGameEnd()),
 				String.valueOf(hcp.getGameTime()),
 				String.valueOf(hcp.getLevel()),
 				String.valueOf(hcp.getExp()),
 				String.valueOf(hcp.getScore()),
 				String.valueOf(hcp.getTopScore()),
 				hcp.getState().toString(),
 				hcp.getDeathMsg(),
 				Util.Loc2Str(hcp.getDeathPos()),
 				String.valueOf(hcp.getDeaths()),
 				
 				String.valueOf(hcp.getCowKills()),
 				String.valueOf(hcp.getPigKills()),
 				String.valueOf(hcp.getSheepKills()),
 				String.valueOf(hcp.getChickenKills()),
 				String.valueOf(hcp.getCreeperKills()),
 				String.valueOf(hcp.getZombieKills()),
 				String.valueOf(hcp.getSkeletonKills()),
 				String.valueOf(hcp.getSpiderKills()),
 				String.valueOf(hcp.getEnderKills()),
 				String.valueOf(hcp.getSlimeKills()),
 				String.valueOf(hcp.getMooshKills()),
 				String.valueOf(hcp.getOtherKills()),
 				String.valueOf(hcp.getPlayerKills())
 		};
 
 		try {
 			int result = dbcon.PreparedUpdate(query, values);
 			if (result < 0) {
 				Debug("Player record save failed!");
 			} else {
 				hcp.setModified(false);
 			}
 		}
 		catch (Exception e) {
 			Debug("Unable to save player record to database!");
 			e.printStackTrace();
 		}
 		
 		return;
 	}
 
 	public void JoinGame(String world, Player player) {
 		Debug("Joining game for " + player.getName());
 		HardcorePlayer hcp = HCPlayers.Get(world, player.getName());
 		if (hcp != null) {
 			if (hcp.getLastPos() != null) {
 				DebugLog("Returning player to: " + hcp.getLastPos());
 				if (player.teleport(hcp.getLastPos())) {
 					player.setWalkSpeed(0.2F);
 					player.setFlySpeed(0.2F);
 					player.setGameMode(GameMode.SURVIVAL);
 					player.setOp(false);
 					player.setAllowFlight(false);
 					player.setFlying(false);
 					player.setFallDistance(0);
 					player.setNoDamageTicks(60);
 				} else {
 					Warn("Teleport failed!");
 				}
 			}
 		} else {
 			Warn("Player record NOT found!");
 		}
 		return;
 	}
 	
 	public void LoadAllPlayers() {
 		String query = "SELECT * FROM `truehardcore`.`players`";
 		try {
 			HCPlayers.Clear();
 			ResultSet res = dbcon.PreparedQuery(query, null);
 			//Debug("Found " + res.getFetchSize() + " records in database");
 			if (res != null) {
 				while (res.next()) {
 					String player = res.getString("player").toLowerCase();
 					String world = res.getString("world");
 					World w = getServer().getWorld(world);
 					DebugLog("Loading: " + world + "/" + player);
 					
 					HardcorePlayer hcp = HCPlayers.NewPlayer(world, player);
 					hcp.setLoadDataOnly(true);
 					hcp.setLastPos(Util.Str2Loc(w, res.getString("lastpos")));
 					hcp.setLastJoin(Util.Mysql2Date(res.getString("lastjoin")));
 					hcp.setLastQuit(Util.Mysql2Date(res.getString("lastquit")));
 					hcp.setGameStart(Util.Mysql2Date(res.getString("gamestart")));
 					hcp.setGameEnd(Util.Mysql2Date(res.getString("gameend")));
 					hcp.setGameTime(res.getInt("gametime"));
 					hcp.setLevel(res.getInt("level"));
 					hcp.setExp(res.getInt("exp"));
 					hcp.setScore(res.getInt("score"));
 					hcp.setTopScore(res.getInt("topscore"));
 					hcp.setState(PlayerState.valueOf(res.getString("state")));
 					hcp.setDeathMsg(res.getString("deathmsg"));
 					hcp.setDeathPos(Util.Str2Loc(w, res.getString("deathpos")));
 					hcp.setDeaths(res.getInt("deaths"));
 					hcp.setCowKills(res.getInt("cowkills"));
 					hcp.setPigKills(res.getInt("pigkills"));
 					hcp.setSheepKills(res.getInt("sheepkills"));
 					hcp.setChickenKills(res.getInt("chickenkills"));
 					hcp.setCreeperKills(res.getInt("creeperkills"));
 					hcp.setZombieKills(res.getInt("zombiekills"));
 					hcp.setSkeletonKills(res.getInt("skeletonkills"));
 					hcp.setSpiderKills(res.getInt("spiderkills"));
 					hcp.setEnderKills(res.getInt("enderkills"));
 					hcp.setSlimeKills(res.getInt("slimekills"));
 					hcp.setMooshKills(res.getInt("mooshkills"));
 					hcp.setOtherKills(res.getInt("otherkills"));
 					hcp.setPlayerKills(res.getInt("playerkills"));
 					hcp.setModified(false);
 					hcp.setLoadDataOnly(false);
 				}
 			}
 		}
 		catch (Exception e) {
 			Debug("Unable to save player record to database!");
 			e.printStackTrace();
 		}
 	}
 	
 	public void SaveAllPlayers() {
 		for (String key : HCPlayers.AllRecords().keySet()) {
 			HardcorePlayer hcp = HCPlayers.Get(key);
 			if ((hcp != null) && (hcp.isModified())) {
 				SavePlayer(hcp);
 			}
 		}
 	}
 	
 	public boolean IsHardcoreWorld(World world) {
 		return HardcoreWorlds.containsKey(world.getName());
 	}
 	
 	public void SendToLobby(Player player) {
 		Location loc = getServer().getWorld("games").getSpawnLocation();
 		player.teleport(loc);
 	}
 	
 	public boolean IsOnWhiteList(String world, String player) {
 		if (WhiteList.containsKey(player)) {
 			List<String> worlds = WhiteList.get(player);
 			if ((worlds != null) && (worlds.size() > 0)) {
 				for (String w : worlds) {
 					if ((w.equals(world)) || (w.equals("*"))) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public void LoadWhiteList() {
 		Debug("Loading player whitelist...");
 		String query = "SELECT * FROM `whitelist`";
 		try {
 			WhiteList.clear();
 			ResultSet res = dbcon.PreparedQuery(query, null);
 			if (res != null) {
 				while (res.next()) {
 					String player = res.getString("player");
 					List<String> worlds = Arrays.asList(StringUtils.split(res.getString("worlds"), ","));
 					WhiteList.put(player, worlds);
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void UnvanishPlayer(Player player) {
 		if ((VNPHooked) && (vnp.isVanished(player))) {
 			Debug("Unvanishing " + player.getName());
 			vnp.toggleVanish(player);
 		}
 	}
 	
 	public boolean SetProtected(HardcorePlayer hcp, long seconds) {
 		if (hcp != null) {
 			if (hcp.isGodMode()) {
 				Debug(hcp.getPlayerName() + " already in god mode!");
 				return false;
 			}
 
 			final String world = hcp.getWorld();
 			final String pname = hcp.getPlayerName();
 			final Player player = getServer().getPlayer(pname);
 
 			hcp.setGodMode(true);
 			player.sendMessage(ChatColor.YELLOW + "You are now invincible for " + seconds + " seconds...");
 			
             // Delay the broadcast so the player sees it as the last message on their screen
             getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                     @Override
                     public void run() {
                     	HardcorePlayer hcp = HCPlayers.Get(world, pname);
                     	if (hcp != null) {
                 			hcp.setGodMode(false);
                     		if (hcp.getState() == PlayerState.IN_GAME) {
                     			player.sendMessage(ChatColor.RED + "Your invincibility has now worn off... Good luck!");
                     		} else {
                         		//Debug("Disable protection: Player " + pname + " is no longer in game");
                     		}
                     	} else {
                     		//Debug("Disable protection: Player " + pname + " does not exist!");
                     	}
                     }
             }, (seconds * 20)); 
 
 		}
 		return false;
 	}
 }
