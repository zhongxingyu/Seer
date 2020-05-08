 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.turt2live.antishare.compatibility.HookManager;
 import com.turt2live.antishare.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.inventory.InventoryManager;
 import com.turt2live.antishare.metrics.Metrics;
 import com.turt2live.antishare.metrics.TrackerList;
 import com.turt2live.antishare.money.MoneyManager;
 import com.turt2live.antishare.notification.Alert;
 import com.turt2live.antishare.notification.Messages;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.permissions.Permissions;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.regions.RegionFactory;
 import com.turt2live.antishare.regions.RegionManager;
 import com.turt2live.antishare.signs.SignManager;
 import com.turt2live.antishare.storage.BlockManager;
 import com.turt2live.antishare.tekkitcompat.ServerHas;
 import com.turt2live.antishare.tekkitcompat.TabRegister;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.SQL;
 import com.turt2live.antishare.util.generic.ConflictThread;
 import com.turt2live.antishare.util.generic.ItemMap;
 import com.turt2live.antishare.util.generic.SelfCompatibility;
 import com.turt2live.antishare.util.generic.UpdateChecker;
 
 /**
  * AntiShare
  * 
  * @author turt2live
  */
 public class AntiShare extends PluginWrapper {
 
 	/**
 	 * AntiShare tool material
 	 */
 	public static final Material ANTISHARE_TOOL = Material.BLAZE_ROD;
 	/**
 	 * Used for debug stuff
 	 */
 	public static final Material ANTISHARE_DEBUG_TOOL = Material.BONE;
 	private static AntiShare instance;
 	private boolean useSQL = false;
 	private boolean sqlRetry = false;
 	private Permissions permissions;
 	private ItemMap itemMap;
 	private ASListener listener;
 	private Alert alerts;
 	private Messages messages;
 	private RegionManager regions;
 	private RegionFactory factory;
 	private BlockManager blocks;
 	private InventoryManager inventories;
 	private SQL sql;
 	private Metrics metrics;
 	private TrackerList trackers;
 	private SignManager signs;
 	private MoneyManager tender;
 	private List<String> disabledSNPlayers = new ArrayList<String>();
 	private HookManager hooks;
 
 	/**
 	 * Gets the active AntiShare instance
 	 * 
 	 * @return the instance
 	 */
 	public static AntiShare getInstance(){
 		return instance;
 	}
 
 	@Override
 	public void onEnable(){
 		instance = this;
 
 		// File check
 		if(!getDataFolder().exists()){
 			getDataFolder().mkdirs();
 		}
 
 		// Move SimpleNotice file
 		File oldSNFile = new File(getDataFolder(), "disabled-simplenotice-users.txt");
 		if(oldSNFile.exists()){
 			oldSNFile.renameTo(new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt"));
 		}
 
 		// Get all disabled SimpleNotice users
 		try{
 			File snFile = new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt");
 			if(snFile.exists()){
 				BufferedReader in = new BufferedReader(new FileReader(snFile));
 				String line;
 				while ((line = in.readLine()) != null){
 					disabledSNPlayers.add(line);
 				}
 				in.close();
 			}else{
 				snFile.createNewFile();
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 		// Check configuration
 		getConfig().loadDefaults(getResource("resources/config.yml"));
 		if(!getConfig().fileExists() || !getConfig().checkDefaults()){
 			getConfig().saveDefaults();
 		}
 		getConfig().load();
 
 		// We need to initiate an SQL connection now
 		startSQL();
 
 		// Check for online mode
 		if(!getServer().getOnlineMode()){
 			if(!getConfig().getBoolean("other.quiet-offline-mode-warning")){
 				getLogger().severe("**********************");
 				getLogger().severe("Your server is in Offline Mode. AntiShare does not support offline mode servers.");
 				getLogger().severe("AntiShare will still run, but you will not get help from turt2live!!");
 				getLogger().severe("You can turn this message off in the configuration.");
 				getLogger().severe("**********************");
 			}
 		}
 
 		// Setup (order is important!)
 		try{
 			metrics = new Metrics(this);
 		}catch(IOException e1){
 			getLogger().severe("AntiShare encountered and error. Please report this to turt2live.");
 			e1.printStackTrace();
 		}
 
 		// Register SimpleNotice channel to AntiShare
 		getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
 
 		// Convert blocks
 		SelfCompatibility.convertBlocks();
 
 		// Setup everything
 		trackers = new TrackerList();
 		hooks = new HookManager();
 		signs = new SignManager();
 		tender = new MoneyManager();
 		permissions = new Permissions();
 		itemMap = new ItemMap();
 		listener = new ASListener();
 		alerts = new Alert();
 		messages = new Messages();
 		regions = new RegionManager();
 		factory = new RegionFactory();
 		blocks = new BlockManager();
 		inventories = new InventoryManager();
 
 		// Migrate world configurations
 		SelfCompatibility.migrateWorldConfigurations();
 
 		// Migrate region players (3.8.0-3.9.0)
 		SelfCompatibility.migratePlayerData();
 
 		// Convert inventories (3.1.3-3.2.0/Current)
 		SelfCompatibility.convert313Inventories();
 
 		// Cleanup old files
 		SelfCompatibility.cleanupOldInventories(); // Handles on/off in config internally
 
 		// Statistics
 		UpdateChecker.start();
 		// mcstats.org
 		trackers.addTo(metrics);
 		metrics.start(); // Handles it's own opt-out
 
 		// Start listeners
 		getServer().getPluginManager().registerEvents(permissions, this);
 		getServer().getPluginManager().registerEvents(listener, this);
 
 		// Command handlers
 		getCommand("antishare").setExecutor(new CommandHandler());
 		if(ServerHas.tabComplete()){
 			TabRegister.register(getCommand("antishare"));
 		}
 
 		// Check players
 		for(Player player : Bukkit.getOnlinePlayers()){
 			ASRegion playerRegion = regions.getRegion(player.getLocation());
 			if(playerRegion != null){
 				playerRegion.alertSilentEntry(player);
 			}
 		}
 
 		// Enabled
 		getLogger().info("Enabled!");
 
 		// Scan for players
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 			@Override
 			public void run(){
 				for(Player player : Bukkit.getOnlinePlayers()){
 					inventories.loadPlayer(player);
 				}
 			}
 		});
 
 		// Conflict messages
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new ConflictThread());
 	}
 
 	@Override
 	public void onDisable(){
 		// Save
 		if(regions != null){
 			regions.save();
 		}
 		if(blocks != null){
 			blocks.save();
 		}
 		if(inventories != null){
 			inventories.save();
 		}
 		if(tender != null){
 			tender.save();
 		}
 		if(metrics != null){
 			metrics.flush();
 		}
 		if(sql != null){
 			sql.disconnect();
 		}
 
 		// Disable
 		getServer().getScheduler().cancelTasks(this);
 		getLogger().info("Disabled!");
 
 		// Prepare as though it's a reload
 		permissions = null;
 		itemMap = null;
 		listener = null;
 		alerts = null;
 		messages = null;
 		factory = null;
 		blocks = null;
 		inventories = null;
 		regions = null;
 		sql = null;
 		metrics = null;
 		trackers = null;
 		signs = null;
 		tender = null;
 		hooks = null;
 
 		// Save disabled SimpleNotice users
 		try{
 			File snFile = new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt");
 			BufferedWriter out = new BufferedWriter(new FileWriter(snFile, false));
 			for(String user : disabledSNPlayers){
 				out.write(user + "\r\n");
 			}
 			out.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reloads AntiShare
 	 */
 	public void reload(){
 		reloadConfig();
 		// Permissions has no reload
 		itemMap.reload();
 		signs.reload();
 		listener.reload();
 		alerts.reload();
 		messages.reload();
 		tender.reload();
 		regions.reload();
 		// Region Factory has no reload
 		blocks.reload();
 		inventories.reload();
 		if(sql != null){
 			sql.reconnect();
 		}
 		// Metrics has no reload
 		// Tracker List has no reload
 		// Simple Notice has no reload
 		// xMail has no reload
 	}
 
 	/**
 	 * Determines if a player decided to turn off SimpleNotice support
 	 * 
 	 * @param name the player name
 	 * @return true if enabled (gets messages through SimpleNotice)
 	 */
 	public boolean isSimpleNoticeEnabled(String name){
 		return !disabledSNPlayers.contains(name);
 	}
 
 	/**
 	 * Enables SimpleNotice support for a user
 	 * 
 	 * @param name the user
 	 */
 	public void enableSimpleNotice(String name){
 		disabledSNPlayers.remove(name);
 	}
 
 	/**
 	 * Disables SimpleNotice support for a user
 	 * 
 	 * @param name the user
 	 */
 	public void disableSimpleNotice(String name){
 		disabledSNPlayers.add(name);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param world the world
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, World world){
 		if(permissions.has(player, allowPermission, world)){
 			return false;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE, world) && player.getGameMode() == GameMode.CREATIVE){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_SURVIVAL, world) && player.getGameMode() == GameMode.SURVIVAL){
 			return true;
 		}
 		if(ServerHas.adventureMode()){
 			if(permissions.has(player, PermissionNodes.AFFECT_ADVENTURE, world) && player.getGameMode() == GameMode.ADVENTURE){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gets a message
 	 * 
 	 * @param path the path to the message
 	 * @return the message
 	 */
 	public String getMessage(String path){
 		return messages.getMessage(path);
 	}
 
 	/**
 	 * Gets the messages handler in AntiShare
 	 * 
 	 * @return the messages handler
 	 */
 	public Messages getMessages(){
 		return messages;
 	}
 
 	/**
 	 * Gets the permissions handler for AntiShare
 	 * 
 	 * @return the permissions
 	 */
 	public Permissions getPermissions(){
 		return permissions;
 	}
 
 	/**
 	 * Gets the Item Map for AntiShare
 	 * 
 	 * @return the item map
 	 */
 	public ItemMap getItemMap(){
 		return itemMap;
 	}
 
 	/**
 	 * Gets the Alert instance for AntiShare
 	 * 
 	 * @return the alerts system
 	 */
 	public Alert getAlerts(){
 		return alerts;
 	}
 
 	/**
 	 * Gets the listener being used by AntiShare
 	 * 
 	 * @return the listener
 	 */
 	public ASListener getListener(){
 		return listener;
 	}
 
 	/**
 	 * Gets the region manager being used by AntiShare
 	 * 
 	 * @return the region manager
 	 */
 	public RegionManager getRegionManager(){
 		return regions;
 	}
 
 	/**
 	 * Gets the region factory being used by AntiShare
 	 * 
 	 * @return the region factory
 	 */
 	public RegionFactory getRegionFactory(){
 		return factory;
 	}
 
 	/**
 	 * Gets the block manager being used by AntiShare
 	 * 
 	 * @return the block manager
 	 */
 	public BlockManager getBlockManager(){
 		return blocks;
 	}
 
 	/**
 	 * Gets the inventory manager being used by AntiShare
 	 * 
 	 * @return the inventory manager
 	 */
 	public InventoryManager getInventoryManager(){
 		return inventories;
 	}
 
 	/**
 	 * Gets the metrics being used by AntiShare
 	 * 
 	 * @return the metrics
 	 */
 	public Metrics getMetrics(){
 		return metrics;
 	}
 
 	/**
 	 * Gets the tracker list being used by AntiShare
 	 * 
 	 * @return the trackers
 	 */
 	public TrackerList getTrackers(){
 		return trackers;
 	}
 
 	/**
 	 * Gets the sign manager being used by AntiShare
 	 * 
 	 * @return the sign manager
 	 */
 	public SignManager getSignManager(){
 		return signs;
 	}
 
 	/**
 	 * Gets the money manager (rewards/fines) being used by AntiShare
 	 * 
 	 * @return the money manager
 	 */
 	public MoneyManager getMoneyManager(){
 		return tender;
 	}
 
 	/**
 	 * Gets the SQL manager for AntiShare
 	 * 
 	 * @return the SQL manager
 	 */
 	public SQL getSQL(){
 		return sql;
 	}
 
 	/**
 	 * Determines if AntiShare should use SQL or not
 	 * 
 	 * @return true if SQL should be used
 	 */
 	public boolean useSQL(){
 		if(getConfig().getBoolean("enabled-features.sql") && !useSQL && !sqlRetry){
 			startSQL();
 			sqlRetry = true;
 		}
 		return useSQL && getConfig().getBoolean("enabled-features.sql") && sql.isConnected();
 	}
 
 	/**
 	 * Force starts the SQL connection
 	 * 
 	 * @return true if connected
 	 */
 	public boolean startSQL(){
 		if(!getConfig().getBoolean("enabled-features.sql")){
 			return false;
 		}
 		sql = new SQL();
 		if(getConfig().getBoolean("settings.sql.sqlite.use-instead")){
 			// Setup properties
 			String location = getConfig().getString("settings.sql.sqlite.location");
 			String name = getConfig().getString("settings.sql.sqlite.name");
 
 			// Try connection
 			boolean connected = sql.connect(location, name);
 			if(connected){
 				sql.setup();
 				useSQL = true;
 				return true;
 			}
 		}else{
 			// Setup properties
 			String hostname = getConfig().getString("settings.sql.host");
 			String username = getConfig().getString("settings.sql.username");
 			String password = getConfig().getString("settings.sql.password");
 			String database = getConfig().getString("settings.sql.database");
 			String port = getConfig().getString("settings.sql.port");
 
 			// Try connection
 			boolean connected = sql.connect(hostname, username, password, database, port);
 			if(connected){
 				sql.setup();
 				useSQL = true;
 				return true;
 			}
 		}
 
 		// Failed connection
 		return false;
 	}
 
 	/**
 	 * Gets the Hook Manager in use by AntiShare
 	 * 
 	 * @return the hook manager
 	 */
 	public HookManager getHookManager(){
 		return hooks;
 	}
 
 	/**
 	 * Log a message
 	 * 
 	 * @param string the message
 	 * @param level the level
 	 */
 	public void log(String string, Level level){
 		getLogger().log(level, string);
 	}
 
 	/**
 	 * Gets the message prefix
 	 * 
 	 * @return the message prefix
 	 */
 	public String getPrefix(){
 		return messages.getPrefix();
 	}
 
 	/**
 	 * Determines if the plugin should cancel an event. This method assumes that the event would be cancelled normally, but needs a debug check.
 	 * 
 	 * @param target the target to alert to (null to not send the message). Alert the user why the event was not cancelled
 	 * @param ignoreTool set to true to ignore the force use tool setting
 	 * @return true if the event should be cancelled, false if the server is in debug mode
 	 */
 	public boolean shouldCancel(CommandSender target, boolean ignoreTool){
 		if(target != null){
 			if(getConfig().getBoolean("other.debug-settings.force-use-tool") && target instanceof Player && !ignoreTool){
 				Player player = (Player) target;
 				if(player.getItemInHand() != null){
 					if(player.getItemInHand().getType() != ANTISHARE_DEBUG_TOOL){
 						ASUtils.sendToPlayer(target, ChatColor.AQUA + "Event cancelled. Debug mode tool not in hand.", true);
						return true;
 					}
 				}
 			}
 			ASUtils.sendToPlayer(target, ChatColor.AQUA + "Event not cancelled. You are in Debug Mode", true);
 			ASUtils.sendToPlayer(target, ChatColor.DARK_AQUA + "Debug Mode Settings: TOOL: " + getConfig().getBoolean("other.debug-settings.force-use-tool") + " IGNORE: " + ignoreTool, true);
 		}
 		return !getConfig().getBoolean("other.debug");
 	}
 
 }
