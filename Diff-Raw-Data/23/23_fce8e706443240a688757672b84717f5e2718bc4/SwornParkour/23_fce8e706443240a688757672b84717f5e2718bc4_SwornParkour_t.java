 /**
 * SwornParkour - a bukkit plugin
 * Copyright (C) 2013 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package net.dmulloy2.swornparkour;
  
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import lombok.Getter;
 import net.dmulloy2.swornparkour.commands.*;
 import net.dmulloy2.swornparkour.handlers.*;
 import net.dmulloy2.swornparkour.listeners.*;
 import net.dmulloy2.swornparkour.parkour.objects.*;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 
 /**
  * @author dmulloy2
  */
 
 public class SwornParkour extends JavaPlugin
 {	
 	/**Getters**/
 	private @Getter PermissionHandler permissionHandler;
 	private @Getter LogHandler logHandler;
 	private @Getter CommandHandler commandHandler;
 	private @Getter ResourceHandler resourceHandler;
 	
 	private @Getter ParkourManager parkourManager;
 	private @Getter FileHelper fileHelper;
 	
 	private @Getter WorldEditPlugin worldEdit;
 	private @Getter Economy economy;
 	
 	private @Getter String prefix = ChatColor.GOLD + "[Parkour] ";
 	
 	public List<ParkourZone> loadedArenas = new ArrayList<ParkourZone>();
 	public List<SavedParkourPlayer> savedPlayers = new ArrayList<SavedParkourPlayer>();
 	public List<ParkourJoinTask> waiting = new ArrayList<ParkourJoinTask>();
 	
 	public HashMap<Integer, ParkourReward> parkourRewards = new HashMap<Integer, ParkourReward>();
 	
 	public int teleportTimer, cashRewardMultiplier;
 	public boolean cumulativeRewards, itemRewardsEnabled, cashRewardsEnabled, updateChecker, debug;
 	private double newVersion, currentVersion;
 	
 	@Override
 	public void onEnable()
 	{	
 		long start = System.currentTimeMillis();
 		
 		logHandler = new LogHandler(this);
 		commandHandler = new CommandHandler(this);
 		permissionHandler = new PermissionHandler();
 		
 		saveResource("messages.properties", true);
 		resourceHandler = new ResourceHandler(this, this.getClassLoader());
 		
 		parkourManager = new ParkourManager(this);
 		
		currentVersion = Double.valueOf(getDescription().getVersion().replaceFirst("\\.", ""));
		
		saveDefaultConfig();
		loadConfig();
		
		updateRewards();
		
		createDirectories();
		
		loadGames();
		
 		fileHelper = new FileHelper(this);
 		savedPlayers = fileHelper.loadSavedPlayers();
 		for (Player player : getServer().getOnlinePlayers())
 		{
 			for (SavedParkourPlayer savedParkourPlayer : savedPlayers)
 			{
 				if (savedParkourPlayer.getName().equals(player.getName()))
 				{
 					parkourManager.normalizeSavedPlayer(savedParkourPlayer);
 				}
 			}
 		}
 		
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PlayerListener(this), this);
 		pm.registerEvents(new BlockListener(this), this);
 		
 		setupWorldEdit(pm);
 		setupVault(pm);
 		
 		/**Register Commands**/
 		getCommand("parkour").setExecutor(commandHandler);
 		commandHandler.setCommandPrefix("parkour");
 		commandHandler.registerCommand(new CmdAbandon(this));
 		commandHandler.registerCommand(new CmdClaim(this));
 		commandHandler.registerCommand(new CmdCreate(this));
 		commandHandler.registerCommand(new CmdDelete(this));
 		commandHandler.registerCommand(new CmdHelp(this));
 		commandHandler.registerCommand(new CmdJoin(this));
 		commandHandler.registerCommand(new CmdKick(this));
 		commandHandler.registerCommand(new CmdLeave(this));
 		commandHandler.registerCommand(new CmdReload(this));
 		commandHandler.registerCommand(new CmdSetPoint(this));
 		commandHandler.registerCommand(new CmdSpawn(this));
 		
 		if (updateChecker)
 			new UpdateCheckThread().runTaskTimer(this, 0, 432000);
 
 		long finish = System.currentTimeMillis();
 		
 		outConsole("{0} has been enabled ({1}ms)", getDescription().getFullName(), finish - start);
 	}
 	
 	private void createDirectories()
 	{
 		File games = new File(getDataFolder(), "games");
 		if (!games.exists()) games.mkdir();
 			
 		File players = new File(getDataFolder(), "players");
 		if (!players.exists()) players.mkdir();
 	}
 
 	@Override
 	public void onDisable()
 	{
 		long start = System.currentTimeMillis();
 		
 		getServer().getServicesManager().unregisterAll(this);
 		getServer().getScheduler().cancelTasks(this);
 
 		for (ParkourZone zone : loadedArenas)
 		{
 			fileHelper.save(zone);
 		}
 		
 		clearMemory();
 		
 		long finish = System.currentTimeMillis();
 		
 		outConsole("{0} has been disabled ({1}ms)", getDescription().getFullName(), finish - start);
 	}
 	
 	public void loadGames()
 	{
 		File folder = new File(getDataFolder(), "games");
 		File[] children = folder.listFiles();
 		for (File file : children)
 		{
 			fileHelper.load(file);
 		}
 	}
 	
 	public void loadConfig()
 	{
 		teleportTimer = getConfig().getInt("teleport-timer");
 		cumulativeRewards = getConfig().getBoolean("item-rewards-cumulative");
 		itemRewardsEnabled = getConfig().getBoolean("item-rewards-enabled");
 		cashRewardsEnabled = getConfig().getBoolean("cash-reward.enabled");
 		cashRewardMultiplier = getConfig().getInt("cash-reward.multiplier");
 		updateChecker = getConfig().getBoolean("update-checker");
 		debug = getConfig().getBoolean("debug");
 		
 	}
 	
 	public void reload()
 	{
 		reloadConfig();
 		updateRewards();
 		loadConfig();
 	}
 	
 	/**Console Logging**/
 	public void outConsole(String string, Object... objects)
 	{
 		logHandler.log(string, objects);
 	}
 	 
 	public void outConsole(Level level, String string, Object... objects)
 	{
 		logHandler.log(level, string, objects);
 	}
 
 	/**Messages**/
 	public String getMessage(String string)
 	{
 		try 
 		{
 			// TODO: Move all the messages to messages.properties
 			return resourceHandler.getMessages().getString(string);
 		}
 		catch (MissingResourceException ex) 
 		{
 			logHandler.log(Level.WARNING, "Messages locale is missing key for: {0}", string);
 			return null;
 		}
 	}
 	
 	private void setupWorldEdit(PluginManager pm)
 	{
 		if (pm.isPluginEnabled("WorldEdit"))
 		{
 			Plugin worldEditPlugin = pm.getPlugin("WorldEdit");
 			worldEdit = (WorldEditPlugin)worldEditPlugin;
 		}
 		else
 		{
 			outConsole(Level.SEVERE, "Could not find WorldEdit! Disabling!");
 			pm.disablePlugin(this);
 		}
 	}
 	
 	/**Update Rewards Table**/
 	public void updateRewards()
 	{
 		parkourRewards.clear();
 		
 		for (int i=0; i<25; i++)
 		{
 			String reward = getConfig().getString("item-rewards." + i);
 			
 			fileHelper.readReward(i, reward);
 		}
 		
 		outConsole("Loaded all rewards!");
 	}
 	
 	public ParkourZone getParkourZone(int gameId)
 	{
 		for (ParkourZone zone : loadedArenas)
 		{
 			if (zone.getId() == gameId)
 			{
 				return zone;
 			}
 		}
 		return null;
 	}
 	
 	private void setupVault(PluginManager pm)
 	{
 		if (pm.isPluginEnabled("Vault"))
 		{
 			setupEconomy();
 			outConsole(getMessage("log_vault_found"));
 		} 
 		else 
 		{
 			outConsole(getMessage("log_vault_notfound"));
 		}
 	}
 	
     private boolean setupEconomy() 
 	{
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 		if (economyProvider != null) 
 		{
 			economy = ((Economy)economyProvider.getProvider());
 		}
  
 		return economy != null;
 	}
 
     public double updateCheck(double currentVersion)
     {
         String pluginUrlString = "http://dev.bukkit.org/bukkit-mods/swornparkour/files.rss";
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
                 return Double.valueOf(firstNodes.item(0).getNodeValue().replaceAll("[a-zA-Z ]", "").replaceFirst("\\.", ""));
             }
         }
         catch (Exception e) 
         {
         	if (debug) outConsole(Level.SEVERE, getMessage("log_update_error"), e.getMessage());
         }
         
         return currentVersion;
     }
     
     public boolean updateNeeded()
     {
     	return (updateCheck(currentVersion) > currentVersion);
     }
 	
 	private void clearMemory()
 	{
 		parkourManager.onShutdown();
 		
 		parkourRewards.clear();
 		loadedArenas.clear();
 		savedPlayers.clear();
 		waiting.clear();
 	}
 	
 	public class UpdateCheckThread extends BukkitRunnable
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
 				if (debug) outConsole(Level.SEVERE, getMessage("log_update_error"), e.getMessage());
 			}
 		}
 	}
 }
