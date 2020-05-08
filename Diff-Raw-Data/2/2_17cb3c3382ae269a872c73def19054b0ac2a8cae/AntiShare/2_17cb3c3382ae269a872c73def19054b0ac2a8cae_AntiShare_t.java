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
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.lang.LocaleMessage;
 import com.turt2live.antishare.lang.Localization;
 import com.turt2live.antishare.listener.BaseListener;
 import com.turt2live.antishare.manager.InventoryManager;
 import com.turt2live.antishare.manager.RegionManager;
 import com.turt2live.antishare.metrics.TrackerType;
 import com.turt2live.antishare.notification.Alert;
 import com.turt2live.antishare.notification.Messages;
 import com.turt2live.antishare.pail.PailHook;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.permissions.Permissions;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.signs.SignList;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.generic.ItemMap;
 import com.turt2live.antishare.util.generic.SelfCompatibility;
 import com.turt2live.antishare.util.generic.UpdateChecker;
 import com.turt2live.metrics.EMetrics;
 import com.turt2live.metrics.tracker.BasicTracker;
 import com.turt2live.metrics.tracker.EnabledTracker;
 
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
 	 * AntiShare tool for creating cuboids
 	 */
 	public static final Material ANTISHARE_CUBOID_TOOL = Material.SLIME_BALL;
 	/**
 	 * Used to force-set a block
 	 */
 	public static final Material ANTISHARE_SET_TOOL = Material.BLAZE_POWDER;
 
 	private static AntiShare instance;
 	private static boolean debugMode = false;
 	private Permissions permissions;
 	private ItemMap itemMap;
 	private BaseListener listener;
 	private Alert alerts;
 	private Messages messages;
 	private EMetrics metrics;
 	private SignList signs;
 	private final List<String> disabledSNPlayers = new ArrayList<String>();
 	private final List<String> disabledTools = new ArrayList<String>();
 	private String build = "Unknown build, custom?";
 	private Systems systems;
 
 	/**
 	 * Gets the active AntiShare instance
 	 * 
 	 * @return the instance
 	 */
 	public static AntiShare getInstance(){
 		return instance;
 	}
 
 	/**
 	 * Sets the AntiShare instance. This will throw an IllegalArgumentException if the instance is already defined
 	 * 
 	 * @param instance the instance to set
 	 * @deprecated Designed for testing only
 	 */
 	@Deprecated
 	public static void setInstance(AntiShare instance){
 		if(instance == null){
 			AntiShare.instance = null;
 			return;
 		}
 		if(AntiShare.instance == null){
 			AntiShare.instance = instance;
 		}else{
 			throw new IllegalArgumentException("Instance already set");
 		}
 	}
 
 	@Override
 	public void onEnable(){
 		instance = this;
 
 		// File check
 		if(!getDataFolder().exists()){
 			getDataFolder().mkdirs();
 		}
 		File data = new File(getDataFolder(), "data");
 		if(!data.exists()){
 			data.mkdirs();
 		}
 
 		// Check configuration
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_CHECK_CONFIG));
 		}
 		getConfig().loadDefaults(getResource("resources/config.yml"));
 		if(!getConfig().fileExists() || !getConfig().checkDefaults()){
 			getConfig().saveDefaults();
 		}
 		getConfig().load();
 
 		// Debug mode
 		debugMode = getConfig().getBoolean("other.debug");
 
 		// Get build number
 		try{
 			BufferedReader in = new BufferedReader(new InputStreamReader(getResource("plugin.yml")));
 			String line;
 			while ((line = in.readLine()) != null){
 				if(line.startsWith("build: ")){
 					line = line.replace("build: ", "");
 					build = line;
 					break;
 				}
 			}
 		}catch(IOException e){}
 
 		// Create version string
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_VERSION_STRING));
 		}
 		String val = getDescription().getVersion() + "|" + getServer().getVersion() + "|" + getServer().getOnlineMode() + "|" + build;
 		if(!getConfig().getString("error-reporting.error-string", "").equalsIgnoreCase(val)){
 			getConfig().set("error-reporting.error-string", val);
 			saveConfig();
 		}
 
 		// Move SimpleNotice file
 		File oldSNFile = new File(getDataFolder(), "disabled-simplenotice-users.txt");
 		if(oldSNFile.exists()){
 			oldSNFile.renameTo(new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt"));
 		}
 
 		// Get all disabled SimpleNotice users
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_SIMPLENOTICE));
 		}
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
 
 		// Check for online mode
 		if(!getServer().getOnlineMode()){
 			if(!getConfig().getBoolean("other.quiet-offline-mode-warning")){
 				getLogger().severe("**********************");
 				getLogger().info(Localization.getMessage(LocaleMessage.START_OFFLINE_1));
 				getLogger().info(Localization.getMessage(LocaleMessage.START_OFFLINE_2));
 				getLogger().info(Localization.getMessage(LocaleMessage.START_OFFLINE_3));
 				getLogger().severe("**********************");
 			}
 		}
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_SETUP, LocaleMessage.SERVICE_METRICS));
 		}
 		try{
 			metrics = new EMetrics(this);
 		}catch(IOException e1){
 			e1.printStackTrace();
 		}
 
 		// Register SimpleNotice channel to AntiShare
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_REGISTER, LocaleMessage.SERVICE_SIMPLE_NOTICE));
 		}
 		getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
 
 		// Setup folder structure
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_FOLDERS));
 		}
 		SelfCompatibility.folderSetup();
 
 		// Convert blocks
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_BLOCKS));
 		}
 		SelfCompatibility.convertBlocks();
 
 		// Migrate world configurations
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_WORLDS));
 		}
 		SelfCompatibility.migrateWorldConfigurations();
 
 		// Migrate region players (3.8.0-3.9.0)
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_PLAYERS));
 		}
 		SelfCompatibility.migratePlayerData();
 
 		// Convert inventories (3.1.3-3.2.0/Current)
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_INVENTORIES, "3.1.3"));
 		}
 		SelfCompatibility.convert313Inventories();
 
 		// Convert inventories (5.2.0/5.3.0)
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_INVENTORIES, "5.2.0"));
 		}
 		SelfCompatibility.cleanup520Inventories();
 
 		// Convert inventories (5.3.0/Current)
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_INVENTORIES, "5.3.0"));
 		}
 		SelfCompatibility.cleanup530Inventories();
 
 		// Cleanup old files
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_CLEANUP, LocaleMessage.SERVICE_INVENTORIES));
 		}
 		SelfCompatibility.cleanupOldInventories(); // Handles on/off in config internally
 
 		// Cleanup old files
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_CLEANUP, LocaleMessage.DICT_CONFIG_FILES));
 		}
 		SelfCompatibility.cleanupYAML();
 
 		// Cleanup old files
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info("[Self Compat] " + Localization.getMessage(LocaleMessage.START_COMPAT_CLEANUP, LocaleMessage.SERVICE_BLOCKS));
 		}
 		SelfCompatibility.cleanup520blocks();
 
 		// Pre-load
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_SIGNS));
 		}
 		signs = new SignList();
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_PERMISSIONS));
 		}
 		permissions = new Permissions();
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_ITEM_MAP));
 		}
 		itemMap = new ItemMap();
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_METRICS_TRACKERS));
 		}
 		for(TrackerType type : TrackerType.values()){
 			switch (type){
 			case FEATURE_FINES_REWARDS:
 			case FEATURE_SIGNS:
 			case FEATURE_REGIONS:
 			case FEATURE_INVENTORIES:
 			case FEATURE_GM_BLOCKS:
 			case FEATURE_WORLD_SPLIT:
 			case LOCALE:
 				metrics.addTracker(new EnabledTracker(type.getGraphName(), type.getName()));
 				break;
 			case SPECIAL:
 				break;
 			default:
 				metrics.addTracker(new BasicTracker(type.getGraphName(), type.getName()));
 				break;
 			}
 		}
 
 		// Startup Systems Manager
 		systems = new Systems();
 		systems.load();
 
 		// Setup everything
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_LISTENER));
 		}
 		listener = new BaseListener();
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_ALERTS));
 		}
 		alerts = new Alert();
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_MESSAGES));
 		}
 		messages = new Messages();
 
 		// Statistics
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_UPDATE));
 		}
 		UpdateChecker.start();
 		// mcstats.org
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_START, LocaleMessage.SERVICE_METRICS));
 		}
 		metrics.startMetrics(); // Handles it's own opt-out
 
 		// Start listeners
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_REGISTER, LocaleMessage.SERVICE_LISTENER));
 		}
 		getServer().getPluginManager().registerEvents(permissions, this);
 		getServer().getPluginManager().registerEvents(listener, this);
 
 		// Command handlers
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_SETUP, LocaleMessage.SERVICE_COMMANDS));
 		}
 		getCommand("antishare").setExecutor(new CommandHandler());
 		getCommand("antishare").setTabCompleter(new TabHandler());
 
 		// Load pail
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
			getLogger().info(Localization.getMessage(LocaleMessage.START_PAIL));
 		}
 		Plugin pail = getServer().getPluginManager().getPlugin("Pail");
 		if(pail != null){
 			PailHook.start(pail);
 		}
 
 		// Enabled
 		getLogger().info(Localization.getMessage(LocaleMessage.ENABLED));
 
 		if(!getConfig().getBoolean("other.more-quiet-startup") || debugMode){
 			getLogger().info(Localization.getMessage(LocaleMessage.START_SCHEDULE, LocaleMessage.SERVICE_REGION_INVENTORY_UPDATE));
 		}
 		loadPlayerInformation();
 	}
 
 	@Override
 	public void onDisable(){
 		// Save
 		if(systems != null){
 			systems.save();
 		}
 
 		// Disable
 		getServer().getScheduler().cancelTasks(this);
 		getLogger().info(Localization.getMessage(LocaleMessage.DISABLED));
 
 		// Prepare as though it's a reload
 		permissions = null;
 		itemMap = null;
 		listener = null;
 		alerts = null;
 		messages = null;
 		metrics = null;
 		signs = null;
 		systems = null;
 
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
 		systems.reload();
 		itemMap.reload();
 		signs.reload();
 		listener.reload();
 		alerts.reload();
 		messages.reload();
 		loadPlayerInformation();
 	}
 
 	private void loadPlayerInformation(){
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 
 			@Override
 			public void run(){
 				InventoryManager inventories = null;
 				RegionManager regions = null;
 				if(systems.isEnabled(Manager.INVENTORY)){
 					inventories = (InventoryManager) systems.getManager(Manager.INVENTORY);
 				}
 				if(systems.isEnabled(Manager.REGION)){
 					regions = (RegionManager) systems.getManager(Manager.REGION);
 				}
 				for(Player player : Bukkit.getOnlinePlayers()){
 					if(inventories != null){
 						inventories.loadPlayer(player);
 					}
 					if(regions != null){
 						Region playerRegion = regions.getRegion(player.getLocation());
 						if(playerRegion != null){
 							playerRegion.alertSilentEntry(player);
 						}
 					}
 					if(permissions.has(player, PermissionNodes.TOOL_USE) && !isToolEnabled(player.getName())){
 						ASUtils.sendToPlayer(player, ChatColor.RED + Localization.getMessage(LocaleMessage.TOOL_DISABLE), true);
 					}
 				}
 				if(inventories != null){
 					int loaded = inventories.getLoaded();
 					if(loaded > 0){
 						getLogger().info(Localization.getMessage(LocaleMessage.STATUS_INVENTORIES, String.valueOf(loaded)));
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * Gets the general purpose systems manager
 	 * 
 	 * @return the general purpose systems manager
 	 */
 	public Systems getSystemsManager(){
 		return systems;
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
 	 * Determines if a player decided to turn off tool support
 	 * 
 	 * @param name the player name
 	 * @return true if enabled
 	 */
 	public boolean isToolEnabled(String name){
 		return !disabledTools.contains(name);
 	}
 
 	/**
 	 * Enables tool support for a user
 	 * 
 	 * @param name the user
 	 */
 	public void enableTools(String name){
 		disabledTools.remove(name);
 	}
 
 	/**
 	 * Disables tool support for a user
 	 * 
 	 * @param name the user
 	 */
 	public void disableTools(String name){
 		disabledTools.add(name);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param world the world
 	 * @param material the material applied to the permissions (or null for none)
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, World world, Material material){
 		return isBlocked(player, allowPermission, null, world, material, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param material the material applied to the permissions (or null for none)
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, Material material){
 		return isBlocked(player, allowPermission, denyPermission, world, material, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param material the material applied to the permissions (or null for none)
 	 * @param specialOnly true to only check permission.[item] permissions
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, Material material, boolean specialOnly){
 		if(material != null){
 			if(permissions.has(player, allowPermission + "." + material.getId(), world)){
 				return false;
 			}
 			if(permissions.has(player, allowPermission + "." + material.name(), world)){
 				return false;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + material.getId(), world)){
 				return true;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + material.name(), world)){
 				return true;
 			}
 		}
 		if(specialOnly){
 			return false;
 		}
 		if(permissions.has(player, allowPermission, world)){
 			return false;
 		}
 		if(denyPermission != null && permissions.has(player, denyPermission, world)){
 			return true;
 		}
 		if(GamemodeAbstraction.isCreative(player.getGameMode())){
 			if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE, world) || permissions.has(player, PermissionNodes.AFFECT_ADVENTURE, world)){
 				return true;
 			}
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE, world) && player.getGameMode() == GameMode.CREATIVE){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_SURVIVAL, world) && player.getGameMode() == GameMode.SURVIVAL){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_ADVENTURE, world) && player.getGameMode() == GameMode.ADVENTURE){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param target the target to apply to this permission, spaces will removed
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, String target){
 		return isBlocked(player, allowPermission, denyPermission, world, target, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param target the target to apply to this permission, spaces will removed
 	 * @param specialOnly true to only check permission.[item] permissions
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, String target, boolean specialOnly){
 		if(target != null){
 			target = target.replaceAll(" ", "");
 			if(target.startsWith("/")){
 				target = target.substring(1);
 			}
 			if(permissions.has(player, allowPermission + "." + target, world)){
 				return false;
 			}
 			if(permissions.has(player, allowPermission + "." + target, world)){
 				return false;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + target, world)){
 				return true;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + target, world)){
 				return true;
 			}
 		}
 		if(specialOnly){
 			return false;
 		}
 		if(permissions.has(player, allowPermission, world)){
 			return false;
 		}
 		if(denyPermission != null && permissions.has(player, denyPermission, world)){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE, world) && player.getGameMode() == GameMode.CREATIVE){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_SURVIVAL, world) && player.getGameMode() == GameMode.SURVIVAL){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_ADVENTURE, world) && player.getGameMode() == GameMode.ADVENTURE){
 			return true;
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
 	public BaseListener getListener(){
 		return listener;
 	}
 
 	/**
 	 * Gets the metrics being used by AntiShare
 	 * 
 	 * @return the metrics
 	 */
 	public EMetrics getMetrics(){
 		return metrics;
 	}
 
 	/**
 	 * Gets the sign list being used by AntiShare
 	 * 
 	 * @return the sign list
 	 */
 	public SignList getSignList(){
 		return signs;
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
 	 * Gets the AntiShare build number
 	 * 
 	 * @return the build number
 	 */
 	public String getBuild(){
 		return build;
 	}
 
 	/**
 	 * Determines if AntiShare is in debug mode
 	 * 
 	 * @return true if in debug mode
 	 */
 	public static boolean isDebug(){
 		return debugMode;
 	}
 
 }
