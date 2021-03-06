 /**
  * SwornRPG - a bukkit plugin 
  * Copyright (C) 2013 dmulloy2
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.dmulloy2.swornrpg;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import lombok.Getter;
 import net.dmulloy2.swornrpg.commands.CmdAbilities;
 import net.dmulloy2.swornrpg.commands.CmdAddxp;
 import net.dmulloy2.swornrpg.commands.CmdAdminChat;
 import net.dmulloy2.swornrpg.commands.CmdAdminSay;
 import net.dmulloy2.swornrpg.commands.CmdCoordsToggle;
 import net.dmulloy2.swornrpg.commands.CmdDeny;
 import net.dmulloy2.swornrpg.commands.CmdDivorce;
 import net.dmulloy2.swornrpg.commands.CmdEject;
 import net.dmulloy2.swornrpg.commands.CmdFrenzy;
 import net.dmulloy2.swornrpg.commands.CmdHat;
 import net.dmulloy2.swornrpg.commands.CmdHelp;
 import net.dmulloy2.swornrpg.commands.CmdHighCouncil;
 import net.dmulloy2.swornrpg.commands.CmdItemName;
 import net.dmulloy2.swornrpg.commands.CmdLeaderboard;
 import net.dmulloy2.swornrpg.commands.CmdLevel;
 import net.dmulloy2.swornrpg.commands.CmdMarry;
 import net.dmulloy2.swornrpg.commands.CmdMatch;
 import net.dmulloy2.swornrpg.commands.CmdPropose;
 import net.dmulloy2.swornrpg.commands.CmdReload;
 import net.dmulloy2.swornrpg.commands.CmdResetLevel;
 import net.dmulloy2.swornrpg.commands.CmdRide;
 import net.dmulloy2.swornrpg.commands.CmdSitdown;
 import net.dmulloy2.swornrpg.commands.CmdSpouse;
 import net.dmulloy2.swornrpg.commands.CmdStaffList;
 import net.dmulloy2.swornrpg.commands.CmdStandup;
 import net.dmulloy2.swornrpg.commands.CmdSuperPickaxe;
 import net.dmulloy2.swornrpg.commands.CmdTag;
 import net.dmulloy2.swornrpg.commands.CmdTagReset;
 import net.dmulloy2.swornrpg.commands.CmdUnlimitedAmmo;
 import net.dmulloy2.swornrpg.commands.CmdUnride;
 import net.dmulloy2.swornrpg.commands.CmdVersion;
 import net.dmulloy2.swornrpg.handlers.AbilityHandler;
 import net.dmulloy2.swornrpg.handlers.CommandHandler;
 import net.dmulloy2.swornrpg.handlers.ExperienceHandler;
 import net.dmulloy2.swornrpg.handlers.HealthBarHandler;
 import net.dmulloy2.swornrpg.handlers.LogHandler;
 import net.dmulloy2.swornrpg.handlers.PermissionHandler;
 import net.dmulloy2.swornrpg.handlers.ResourceHandler;
 import net.dmulloy2.swornrpg.handlers.TagHandler;
 import net.dmulloy2.swornrpg.io.PlayerDataCache;
 import net.dmulloy2.swornrpg.listeners.BlockListener;
 import net.dmulloy2.swornrpg.listeners.EntityListener;
 import net.dmulloy2.swornrpg.listeners.ExperienceListener;
 import net.dmulloy2.swornrpg.listeners.PlayerListener;
 import net.dmulloy2.swornrpg.types.BlockDrop;
 import net.dmulloy2.swornrpg.types.PlayerData;
 import net.dmulloy2.swornrpg.types.Reloadable;
 import net.dmulloy2.swornrpg.util.FormatUtil;
 import net.dmulloy2.swornrpg.util.MaterialUtil;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.earth2me.essentials.Essentials;
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.FLocation;
 import com.massivecraft.factions.Faction;
 
 /**
  * @author dmulloy2
  */
 
 public class SwornRPG extends JavaPlugin implements Reloadable
 {
 	/** Getters **/
 	private @Getter Economy economy;
 	private @Getter Permission permission;
 
 	private @Getter Essentials essentials;
 	private @Getter PluginManager pluginManager;
 	private @Getter PlayerDataCache playerDataCache;
 
 	/** Handlers **/
 	private @Getter PermissionHandler permissionHandler;
 	private @Getter CommandHandler commandHandler;
 	private @Getter ResourceHandler resourceHandler;
 	private @Getter LogHandler logHandler;
 
 	private @Getter AbilityHandler abilityHandler;
 	private @Getter ExperienceHandler experienceHandler;
 	private @Getter HealthBarHandler healthBarHandler;
 	private @Getter TagHandler tagHandler;
 
 	/** Listeners, Stored for Reloading **/
 	private List<Listener> listeners;
 
 	/** Maps **/
 	private @Getter HashMap<String, String> proposal = new HashMap<String, String>();
     private @Getter HashMap<String, HashMap<Material, Integer>> salvageRef = new HashMap<String, HashMap<Material, Integer>>();
     private @Getter Map<Material, List<BlockDrop>> blockDropsMap = new HashMap<Material, List<BlockDrop>>();
     private @Getter Map<Material, List<BlockDrop>> fishDropsMap = new HashMap<Material, List<BlockDrop>>();
 
     /** Update Checking **/
 	private double newVersion, currentVersion;
 
 	/** Global Prefix Variable **/
 	private @Getter String prefix = FormatUtil.format("&6[SwornRPG] ");
 
 	@Override
 	public void onEnable()
 	{
 		long start = System.currentTimeMillis();
 
 		/** Register Handlers **/
 		commandHandler = new CommandHandler(this);
 		permissionHandler = new PermissionHandler();
 		logHandler = new LogHandler(this);
 		playerDataCache = new PlayerDataCache(this);
 
 		abilityHandler = new AbilityHandler(this);
 		experienceHandler = new ExperienceHandler(this);
 		healthBarHandler = new HealthBarHandler(this);
 		tagHandler = new TagHandler(this);
 
 		pluginManager = getServer().getPluginManager();
 
 		/** Resource Handler / Messages **/
 		saveResource("messages.properties", true);
 		resourceHandler = new ResourceHandler(this, getClassLoader());
 
 		/** Update Checker **/
 		String version = getDescription().getVersion();
		version = version.substring(0, version.indexOf("("));
		version.replaceAll("\\.", "");
 
		currentVersion = Double.valueOf(version.trim());
 		debug("Current version: \"{0}\"", currentVersion);
 
 		/** Register Listeners **/
 		listeners = new ArrayList<Listener>();
 
 		registerListener(new PlayerListener(this));
 		registerListener(new EntityListener(this));
 		registerListener(new BlockListener(this));
 		registerListener(new ExperienceListener(this));
 
 		/** Check for PlayerData folder **/
 		File playersFile = new File(getDataFolder(), "players");
 		if (! playersFile.exists())
 		{
 			playersFile.mkdirs();
 		}
 
 		/** Configuration Stuff **/
 		File conf = new File(getDataFolder(), "config.yml");
 		if (! conf.exists())
 		{
 			outConsole(getMessage("log_configuration"));
 			saveDefaultConfig();
 		}
 		else
 		{
 			if (! getConfig().isSet("checkForUpdates"))
 			{
 				conf.renameTo(new File(getDataFolder(), "oldConfig.yml"));
 
 				outConsole(getMessage("log_old_config"));
 
 				saveDefaultConfig();
 			}
 		}
 
 		reloadConfig();
 
 		/** Update Block Tables **/
 		updateBlockDrops();
 		updateFishDrops();
 
 		/** Salvaging **/
 		updateSalvageRef();
 
 		/** Register Prefixed Commands **/
 		commandHandler.setCommandPrefix("srpg");
 		commandHandler.registerPrefixedCommand(new CmdHelp(this));
 		commandHandler.registerPrefixedCommand(new CmdLeaderboard(this));
 		commandHandler.registerPrefixedCommand(new CmdVersion(this));
 		commandHandler.registerPrefixedCommand(new CmdReload(this));
 
 		/** Register Non-Prefixed Commands **/
 		commandHandler.registerCommand(new CmdAbilities(this));
 		commandHandler.registerCommand(new CmdAddxp(this));
 		commandHandler.registerCommand(new CmdAdminChat(this));
 		commandHandler.registerCommand(new CmdAdminSay(this));
 		commandHandler.registerCommand(new CmdCoordsToggle(this));
 		commandHandler.registerCommand(new CmdDeny(this));
 		commandHandler.registerCommand(new CmdDivorce(this));
 		commandHandler.registerCommand(new CmdEject(this));
 		commandHandler.registerCommand(new CmdFrenzy(this));
 		commandHandler.registerCommand(new CmdHat(this));
 		commandHandler.registerCommand(new CmdHighCouncil(this));
 		commandHandler.registerCommand(new CmdItemName(this));
 		commandHandler.registerCommand(new CmdLevel(this));
 		commandHandler.registerCommand(new CmdMarry(this));
 		commandHandler.registerCommand(new CmdMatch(this));
 		commandHandler.registerCommand(new CmdPropose(this));
 		commandHandler.registerCommand(new CmdResetLevel(this));
 		commandHandler.registerCommand(new CmdRide(this));
 		commandHandler.registerCommand(new CmdSitdown(this));
 		commandHandler.registerCommand(new CmdSpouse(this));
 		commandHandler.registerCommand(new CmdStaffList(this));
 		commandHandler.registerCommand(new CmdStandup(this));
 		commandHandler.registerCommand(new CmdSuperPickaxe(this));
 		commandHandler.registerCommand(new CmdTag(this));
 		commandHandler.registerCommand(new CmdTagReset(this));
 		commandHandler.registerCommand(new CmdUnride(this));
 		commandHandler.registerCommand(new CmdUnlimitedAmmo(this));
 
 		/** Handle Health if Reload **/
 		for (Player player : getServer().getOnlinePlayers())
 		{
 			healthBarHandler.updateHealth(player);
 		}
 
 		/** Integration **/
 		tagHandler.load();
 		setupVaultIntegration();
 		setupEssentialsIntegration();
 
 		/** Deploy AutoSave Task **/
 		if (getConfig().getBoolean("autoSave.enabled"))
 		{
 			int interval = 20 * 60 * getConfig().getInt("autoSave.interval");
 
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					playerDataCache.save();
 				}
 			}.runTaskTimerAsynchronously(this, interval, interval);
 		}
 
 		/** Frenzy Mode Cooldown **/
 		if (getConfig().getBoolean("frenzy.enabled"))
 		{
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					for (Player player : getServer().getOnlinePlayers())
 					{
 						if (player != null && player.isOnline())
 						{
 							PlayerData data = playerDataCache.getData(player.getName());
 							if (data.isFrenzyCooldownEnabled())
 							{
 								data.setFrenzyCooldownTime(data.getFrenzyCooldownTime() - 1);
 								if (data.getFrenzyCooldownTime() <= 0)
 								{
 									data.setFrenzyCooldownEnabled(false);
 									player.sendMessage(prefix + FormatUtil.format(getMessage("ability_refreshed"), "Frenzy"));
 								}
 							}
 						}
 					}
 				}
 			}.runTaskTimer(this, 20L, 20L);
 		}
 
 		/** Super Pickaxe Cooldown **/
 		if (getConfig().getBoolean("superPickaxe.enabled"))
 		{
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					for (Player player : getServer().getOnlinePlayers())
 					{
 						if (player != null && player.isOnline())
 						{
 							PlayerData data = playerDataCache.getData(player.getName());
 							if (data.isSuperPickaxeCooldownEnabled())
 							{
 								data.setSuperPickaxeCooldownTime(data.getSuperPickaxeCooldownTime() - 1);
 								if (data.getSuperPickaxeCooldownTime() <= 0)
 								{
 									data.setSuperPickaxeCooldownEnabled(false);
 									player.sendMessage(prefix + FormatUtil.format(getMessage("ability_refreshed"), "Super Pickaxe"));
 								}
 							}
 						}
 					}
 				}
 			}.runTaskTimer(this, 20L, 20L);
 		}
 
 		/** SwornGuns Integration **/
 		if (pluginManager.isPluginEnabled("SwornGuns") && getConfig().getBoolean("unlimitedAmmo.enabled"))
 		{
 			// outConsole(getMessage("log_gun_found"));
 			// pluginManager.registerEvents(new SwornGunsListener(this), this);
 
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					for (Player player : getServer().getOnlinePlayers())
 					{
 						if (player != null && player.isOnline())
 						{
 							PlayerData data = playerDataCache.getData(player.getName());
 							if (data.isUnlimitedAmmoCooldownEnabled())
 							{
 								data.setUnlimitedAmmoCooldownTime(data.getUnlimitedAmmoCooldownTime() - 1);
 								if (data.getUnlimitedAmmoCooldownTime() <= 0)
 								{
 									data.setUnlimitedAmmoCooldownEnabled(false);
 									player.sendMessage(prefix + FormatUtil.format(getMessage("ability_refreshed"), "Unlimited Ammo"));
 								}
 							}
 						}
 					}
 				}
 			}.runTaskTimer(this, 20L, 20L);
 		}
 
 		/** Update Checker **/
 		if (getConfig().getBoolean("checkForUpdates"))
 		{
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					try
 					{
 						newVersion = updateCheck(currentVersion);
 						if (newVersion > currentVersion)
 						{
 							outConsole(getMessage("log_update"));
 							outConsole(getMessage("log_update_url"), getMessage("update_url"));
 						}
 					}
 					catch (Exception e)
 					{
 						debug(getMessage("log_update_error"), e.getMessage());
 					}
 				}
 			}.runTaskTimer(this, 20L, 432000L);
 		}
 
 		/** Online XP Gain **/
 		final int onlineXpGain = getConfig().getInt("levelingMethods.onlineTime.xpgain");
 
 		if (getConfig().getBoolean("levelingMethods.onlineTime.enabled"))
 		{
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					for (Player player : getServer().getOnlinePlayers())
 					{
 						PlayerData data = playerDataCache.getData(player);
 						data.setPlayerxp(data.getPlayerxp() + onlineXpGain);
 
 						/** Levelup check **/
 						int xp = data.getPlayerxp();
 
 						if (data.getXpNeeded() == 0)
 							data.setXpneeded(100);
 
 						int xpneeded = data.getXpNeeded();
 
 						int newlevel = xp / xpneeded;
 						int oldlevel = data.getLevel();
 
 						if ((xp - xpneeded) >= 0)
 						{
 							/** If so, call levelup event **/
 							experienceHandler.onLevelup(player, oldlevel, newlevel);
 						}
 					}
 				}
 			}.runTaskTimer(this, 120L, 120L);
 		}
 
 		long finish = System.currentTimeMillis();
 
 		outConsole(getMessage("log_enabled"), getDescription().getFullName(), finish - start);
 	}
 
 	@Override
 	public void onDisable()
 	{
 		long start = System.currentTimeMillis();
 		
 		/** Save Data **/
 		playerDataCache.save();
 		
 		/** Clear Memory **/
 		clearMemory();
 
 		/** Cancel tasks / services **/
 		getServer().getServicesManager().unregisterAll(this);
 		getServer().getScheduler().cancelTasks(this);
 		
 		long finish = System.currentTimeMillis();
 		
 		outConsole(getMessage("log_disabled"), getDescription().getFullName(), finish - start);
 	}
 	
 	/** Clear Memory **/
 	private final void clearMemory()
 	{
 //		healthBarHandler.clear();
 		
 		blockDropsMap.clear();
 		fishDropsMap.clear();
 		
 		salvageRef.clear();
 		proposal.clear();
 	}
 	    
 	/** Console logging **/
 	public final void outConsole(String string, Object... objects)
 	{
 		logHandler.log(string, objects);
 	}
 	
 	public final void outConsole(Level level, String string, Object... objects)
 	{
 		logHandler.log(level, string, objects);
 	}
 	
 	public final void debug(String string, Object... objects)
 	{
 		logHandler.debug(string, objects);
 	}
     
     /**
      * Setup Vault Integration
      */
 	private final void setupVaultIntegration() 
 	{
 		if (pluginManager.isPluginEnabled("Vault"))
 		{
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 			if (economyProvider != null) 
 			{
 				economy = economyProvider.getProvider();
 			}
 			
 			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
 			if (permissionProvider != null) 
 			{
 				permission = permissionProvider.getProvider();
 			}
 		}
 		
 		if (economy != null)
 		{
 			outConsole(getMessage("log_vault_success"), economy.getName());
 		}
 		else
 		{
 			outConsole(getMessage("log_vault_failure"));
 		}
 	}
 	
 	/**
 	 * Setup Essentials Integration
 	 */
 	private final void setupEssentialsIntegration()
 	{
 		PluginManager pm = getServer().getPluginManager();
 		if (pm.isPluginEnabled("Essentials"))
 		{
 			Plugin plugin = pm.getPlugin("Essentials");
 			essentials = (Essentials) plugin;
 		}
 	}
 
     /** Update Checker **/
     private final double updateCheck(double currentVersion)
     {
         String pluginUrlString = "http://dev.bukkit.org/bukkit-plugins/swornrpg/files.rss";
         try
         {
             URL url = new URL(pluginUrlString);
             Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
             doc.getDocumentElement().normalize();
             NodeList nodes = doc.getElementsByTagName("item");
             Node firstNode = nodes.item(0);
             if (firstNode.getNodeType() == 1) 
             {
                 Element firstElement = (Element)firstNode;
                 NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                 Element firstNameElement = (Element) firstElementTagName.item(0);
                 NodeList firstNodes = firstNameElement.getChildNodes();
                return Double.valueOf(firstNodes.item(0).getNodeValue().replaceAll("[a-zA-Z ]", "").replaceAll("\\.", ""));
             }
         }
         catch (Exception e) 
         {
         	debug(getMessage("log_update_error"), e.getMessage());
         }
         
         return currentVersion;
     }
     
     public final boolean updateNeeded()
     {
     	return (updateCheck(currentVersion) > currentVersion);
     }
     
     /** Get messages **/
 	public final String getMessage(String string) 
 	{
 		try
 		{
 			return resourceHandler.getMessages().getString(string);
 		} 
 		catch (MissingResourceException ex) 
 		{
 			outConsole(Level.WARNING, getMessage("log_message_null"),  string); //messageception :3
 			return null;
 		}
 	}
 	
 	/** 
 	 * Reloads the Configuration
 	 */
 	@Override
 	public final void reload()
 	{
 		reloadConfig();
 		reloadListeners();
 		updateSalvageRef();
 		updateBlockDrops();
 		updateFishDrops();
 
 		abilityHandler.reload();
 	}
 
 	/**
 	 * Registers a {@link Listener}
 	 * 
 	 * @param listener
 	 *        - Listener to register
 	 */
 	private final void registerListener(Listener listener)
 	{
 		listeners.add(listener);
 		pluginManager.registerEvents(listener, this);
 	}
 
 	/**
 	 * Reloads the configuration settings of the listeners
 	 */
 	private final void reloadListeners()
 	{
 		for (Listener listener : listeners)
 		{
 			if (listener instanceof Reloadable)
 			{
 				((Reloadable) listener).reload();
 			}
 		}
 	}
 	
 	/** 
 	 * Update salvage ref tables
 	 */
 	private final void updateSalvageRef() 
 	{
 		String salvage = getConfig().getString("salvage");
 
 		salvageRef.put("Iron", new HashMap<Material, Integer>());
 		salvageRef.put("Gold", new HashMap<Material, Integer>());
 		salvageRef.put("Diamond", new HashMap<Material, Integer>());
 		String[] salvageArray = salvage.split("; ");
 		for (String s : salvageArray) 
 		{
 			String[] subset = s.split(", ");
 			
 			Material mat = MaterialUtil.getMaterial(subset[0]);
 			
 			if (mat != null)
 			{
 				salvageRef.get(subset[1]).put(mat, Integer.parseInt(subset[2]));
 			}
 		}
 	}
 	
 	/** 
 	 * Update Block Drops
 	 */
 	public final void updateBlockDrops()
 	{
 		blockDropsMap.clear();
 
 		Map<String, Object> map = getConfig().getConfigurationSection("blockDropItems").getValues(true);
 
 		for (Entry<String, Object> entry : map.entrySet())
 		{
 			@SuppressWarnings("unchecked") // No way to check this :I
 			List<String> values = (List<String>) entry.getValue();
 
 			List<BlockDrop> blockDrops = new ArrayList<BlockDrop>();
 			for (String value : values)
 			{
 				String[] ss = value.split(":");
 
 				Material type = MaterialUtil.getMaterial(ss[0]);
 				if (type == null)
 				{
 					outConsole(Level.WARNING, "Null material {0} found while attempting to load block drops!", ss[0]);
 					continue;
 				}
 
 				short data = 0;
 				int chance = 0;
 				if (ss.length == 3)
 				{
 					data = Short.valueOf(ss[1]);
 					chance = Integer.valueOf(ss[2]);
 				}
 				else
 				{
 					chance = Integer.valueOf(ss[1]);
 				}
 
 				blockDrops.add(new BlockDrop(new ItemStack(type, 1, data), chance));
 			}
 
 			blockDropsMap.put(MaterialUtil.getMaterial(entry.getKey()), blockDrops);
 		}
 	}
 	
 	/** 
 	 * Update Fish Drops
 	 */
 	private final void updateFishDrops() 
 	{
 		fishDropsMap.clear();
 		
 		Map<String, Object> map = getConfig().getConfigurationSection("fishDropItems").getValues(true);
 		
 		for (Entry<String, Object> entry : map.entrySet()) 
 		{
 			@SuppressWarnings("unchecked") // No way to check this :I
 			List<String> values = (List<String>) entry.getValue();
 
 			List<BlockDrop> blockDrops = new ArrayList<BlockDrop>();
 			for (String value : values) 
 			{
 				String[] ss = value.split(":");
 				
 				Material type = MaterialUtil.getMaterial(ss[0]);
 				if (type == null)
 				{
 					outConsole(Level.WARNING, "Null material {0} found while attempting to load fish drops!", ss[0]);
 					continue;
 				}
 				
 				short data = 0;
 				int chance = 0;
 				if (ss.length == 3)
 				{
 					data = Short.valueOf(ss[1]);
 					chance = Integer.valueOf(ss[2]);
 				}
 				else 
 				{
 					chance = Integer.valueOf(ss[1]);
 				}
 				
 				blockDrops.add(new BlockDrop(new ItemStack(type, 1, data), chance));
 			}
 			
 			fishDropsMap.put(MaterialUtil.getMaterial(entry.getKey()), blockDrops);
 		}
 	}
 
 	/**
 	 * Camping Check
 	 */
 	public final boolean checkCamper(Player player)
 	{
 		Location loc = player.getLocation();
 		World world = loc.getWorld();
 		int RADIUS = getConfig().getInt("campingRadius");
 		for (int dx = -RADIUS; dx <= RADIUS; dx++) 
 		{
 			for (int dy = -RADIUS; dy <= RADIUS; dy++) 
 			{
 				for (int dz = -RADIUS; dz <= RADIUS; dz++) 
 				{
 					Material mat = world.getBlockAt(loc.getBlockX() + dx, loc.getBlockY() + dy, loc.getBlockZ() + dz).getType();
 					if (mat == Material.MOB_SPAWNER)
 					{
 						if (! isDisabledWorld(player)) 
 							player.sendMessage(FormatUtil.format(prefix + getMessage("spawner_camper")));
 						
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	// ---- Factions Checks ---- //
 
 	public final boolean checkFactions(Location location, boolean safeZoneCheck)
 	{
 		return safeZoneCheck ? isSafeZone(location) || isWarZone(location) : isWarZone(location);
 	}
 
 	public final boolean checkFactions(Player player, boolean safeZoneCheck)
 	{
 		return checkFactions(player.getLocation(), safeZoneCheck);
 	}
 
 	private final boolean isWarZone(Location location)
 	{
 		if (pluginManager.isPluginEnabled("Factions"))
 		{
 			Plugin pl = pluginManager.getPlugin("Factions");
 			String version = pl.getDescription().getVersion();
 			if (version.startsWith("1.6"))
 			{
 				Faction fac = Board.getFactionAt(new FLocation(location));
 				return fac.isWarZone();
 			}
 		}
 
 		if (pluginManager.isPluginEnabled("SwornNations"))
 		{
 			Faction fac = Board.getAbsoluteFactionAt(new FLocation(location));
 			return fac.isWarZone();
 		}
 
 		return false;
 	}
 	
 	private final boolean isSafeZone(Location location)
 	{
 		if (pluginManager.isPluginEnabled("Factions"))
 		{
 			Plugin pl = pluginManager.getPlugin("Factions");
 			String version = pl.getDescription().getVersion();
 			if (version.startsWith("1.6"))
 			{
 				Faction fac = Board.getFactionAt(new FLocation(location));
 				return fac.isSafeZone();
 			}
 		}
 
 		if (pluginManager.isPluginEnabled("SwornNations"))
 		{
 			Faction fac = Board.getAbsoluteFactionAt(new FLocation(location));
 			return fac.isSafeZone();
 		}
 
 		return false;
 	}
 	
 	public final Player getKiller(Player killed)
 	{
 		Entity attacker = killed.getKiller();
 		if (attacker == null)
 		{
 			EntityDamageEvent ed = killed.getLastDamageCause();
 			if (ed instanceof EntityDamageByEntityEvent)
 			{
 				EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) ed;
 				attacker = ede.getDamager();
 			}
 		}
 
 		Player killer = null;
 		if (attacker != null)
 		{
 			if (attacker instanceof Player) 
 			{
 				killer = (Player) attacker;
 			}
 			else if (attacker instanceof Projectile)
 			{
 				Entity shooter = ((Projectile) attacker).getShooter();
 				if (shooter instanceof Player) 
 				{
 					killer = (Player) shooter;
 				}
 			}
 		}
 
 		return killer;
 	}
 
 	// ---- Disabled World Checks ---- //
 	
 	public boolean isDisabledWorld(Player player)
 	{
 		return isDisabledWorld(player.getWorld());
 	}
 	
 	public boolean isDisabledWorld(Entity entity)
 	{
 		return isDisabledWorld(entity.getWorld());
 	}
 	
 	public boolean isDisabledWorld(Block block)
 	{
 		return isDisabledWorld(block.getWorld());
 	}
 	
 	public boolean isDisabledWorld(World world)
 	{
 		return getConfig().getStringList("disabledWorlds").contains(world.getName());
 	}
 }
