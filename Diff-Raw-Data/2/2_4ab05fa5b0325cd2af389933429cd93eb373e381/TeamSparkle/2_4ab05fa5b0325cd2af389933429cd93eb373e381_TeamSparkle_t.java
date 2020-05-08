 /**
 * TeamSparkle - a bukkit plugin
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
 package net.dmulloy2.teamsparkle;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import lombok.Getter;
 
 import net.dmulloy2.teamsparkle.commands.*;
 import net.dmulloy2.teamsparkle.data.*;
 import net.dmulloy2.teamsparkle.handlers.*;
 import net.dmulloy2.teamsparkle.listeners.*;
 import net.dmulloy2.teamsparkle.util.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author dmulloy2
  */
 
 public class TeamSparkle extends JavaPlugin
 {
 	/** Getters **/
 	private @Getter PlayerDataCache playerDataCache;
 	private @Getter PermissionHandler permissionHandler;
 	private @Getter CommandHandler commandHandler;
 	private @Getter ResourceHandler resourceHandler;
 	private @Getter LogHandler logHandler;
 	
 	private @Getter ShopManager shopManager;
 	
 	/** Sparkled Player HashMap. Format: Sparkler, Sparkled **/
 	private @Getter HashMap<String, String> sparkled = new HashMap<String, String>();
 
     /** Update Checking **/
 	private double newVersion, currentVersion;
     
 	/** Global Prefix **/
 	public String prefix;
 	
 	@Override
 	public void onEnable()
 	{
 		long start = System.currentTimeMillis();
 		
 		/** Save Config **/
 		saveDefaultConfig();
 		
 		prefix = ChatColor.GOLD + "[TeamSparkle] ";
 
 		/**Register Handlers / Managers**/
 		saveResource("messages.properties", true);
 		resourceHandler = new ResourceHandler(this, getClassLoader());
 		
 		commandHandler = new CommandHandler(this);
 		permissionHandler = new PermissionHandler(this);
 		logHandler = new LogHandler(this);
 		playerDataCache = new PlayerDataCache(this);
 		
 		shopManager = new ShopManager(this);
 		
 		/** Register Commands **/
 		commandHandler.setCommandPrefix("ts");
 		commandHandler.registerPrefixedCommand(new CmdBuy(this));
 		commandHandler.registerPrefixedCommand(new CmdGiveTokens(this));
 		commandHandler.registerPrefixedCommand(new CmdHelp(this));
 		commandHandler.registerPrefixedCommand(new CmdInvite(this));
 		commandHandler.registerPrefixedCommand(new CmdLeaderboard(this));
 		commandHandler.registerPrefixedCommand(new CmdReload(this));
 		commandHandler.registerPrefixedCommand(new CmdShop(this));
 		commandHandler.registerPrefixedCommand(new CmdStats(this));
 		
 		/** Register Listeners **/
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PlayerListener(this), this);
 
 		/**Schedule player data cache saving**/
 		if (getConfig().getBoolean("autoSave.enabled"))
 		{
 			int interval = 20 * 60 * getConfig().getInt("autoSave.interval");
 			new AutoSaveTask().runTaskTimer(this, interval, interval);
 		}
 		
 		/** Hourly Rewards **/
 		if (! getConfig().getStringList("hourlyRewards").isEmpty())
 			new HourlyRewardTask().runTaskTimer(this, 72000L, 72000L);
 		
 		long finish = System.currentTimeMillis();
 		
 		outConsole(getMessage("log_enabled"), getDescription().getFullName(), finish - start);
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		long start = System.currentTimeMillis();
 		
 		/** Save Data **/
 		playerDataCache.save();
 		
 		/** Clear HashMaps **/
 		sparkled.clear();
 
 		/** Cancel tasks / services **/
 		getServer().getServicesManager().unregisterAll(this);
 		getServer().getScheduler().cancelTasks(this);
 		
 		long finish = System.currentTimeMillis();
 		
 		outConsole(getMessage("log_disabled"), getDescription().getFullName(), finish - start);
 	}
 	
 	/** Console logging **/
 	public void outConsole(String string, Object... objects)
 	{
 		logHandler.log(string, objects);
 	}
 	
 	public void outConsole(Level level, String string, Object... objects)
 	{
 		logHandler.log(level, string, objects);
 	}
 	
 	public void debug(String string, Object... objects)
 	{
 		if (getConfig().getBoolean("debug", false))
 		{
 			logHandler.log("[Debug] " + string, objects);
 		}
 	}
 	
     /** Update Checker **/
     public double updateCheck(double currentVersion)
     {
         String pluginUrlString = "http://dev.bukkit.org/bukkit-plugins/teamsparkle/files.rss";
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
         	debug(getMessage("log_update_error"), e.getMessage());
         }
         
         return currentVersion;
     }
     
     public boolean updateNeeded()
     {
     	return (updateCheck(currentVersion) > currentVersion);
     }
 	
     /**Get messages**/
 	public String getMessage(String string) 
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
 	
 	public boolean isSparkled(Player sparkledPlayer)
 	{
 		for (Entry<String, String> entry : sparkled.entrySet())
 		{
 			if (entry.getValue().equalsIgnoreCase(sparkledPlayer.getName()))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public String getSparkler(Player sparkledPlayer)
 	{
 		for (Entry<String, String> entry : sparkled.entrySet())
 		{
 			if (entry.getValue().equalsIgnoreCase(sparkledPlayer.getName()))
 				return entry.getKey();
 		}
 		
 		return null;
 	}
 	
 	public void rewardSparkledPlayer(Player sparkledPlayer)
 	{
 		String sparkler = getSparkler(sparkledPlayer);
 		if (sparkler != null)
 		{
 			List<String> commands = getConfig().getStringList("sparkledRewards");
 			if (! commands.isEmpty())
 			{
 				for (String command : commands)
 				{
					getServer().dispatchCommand(getServer().getConsoleSender(), command.replaceAll("%p", sparkledPlayer.getName()));
 				}
 				
 				sparkledPlayer.sendMessage(FormatUtil.format(getMessage("sparkled_welcome"), sparkledPlayer.getName()));
 			}
 			else
 			{
 				outConsole(Level.WARNING, "Could not reward new player {0}: Rewards list cannot be empty!", sparkledPlayer.getName());
 			}
 			
 			OfflinePlayer sparklerp = Util.matchOfflinePlayer(sparkler);
 			
 			PlayerData data = playerDataCache.getData(sparkler);
 			data.setTokens(data.getTokens() + 1);
 			data.setTotalSparkles(data.getTotalSparkles() + 1);
 			
 			if (sparklerp.isOnline())
 			{
 				((Player)sparklerp).sendMessage(FormatUtil.format(getMessage("sparkler_thanks"), sparkledPlayer.getName()));
 			}
 			
 			sparkled.remove(sparkler);
 		}
 	}
 	
 	/** Gives a player an item **/
 	@SuppressWarnings("deprecation")
 	public void giveItem(Player player, ItemStack stack)
 	{
 		player.getInventory().addItem(stack);
 		player.updateInventory();
 	}
 	
 	/** Timers and Runnables **/
 	public class AutoSaveTask extends BukkitRunnable
 	{
 		@Override
 		public void run() 
 		{
 			playerDataCache.save();
 		}
 	}
 	
 	public class UpdateCheckTask extends BukkitRunnable
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
 	}
 	
 	public class HourlyRewardTask extends BukkitRunnable
 	{
 		@Override
 		public void run()
 		{
 			for (Player player : getServer().getOnlinePlayers())
 			{
 				List<String> rewards = getConfig().getStringList("hourlyRewards");
 				if (! rewards.isEmpty())
 				{
 					int rand = Util.random(rewards.size());
 					String entry = rewards.get(rand);
 					if (entry != null)
 					{
 						String command = entry.split(";")[0].replaceAll("%p", player.getName());
 						getServer().dispatchCommand(getServer().getConsoleSender(), command);
 						
 						player.sendMessage(FormatUtil.format(
 								getMessage("hourly_reward"), 
 								getConfig().getString("serverName"),
 								entry.split(";")[1]));
 					}
 				}
 			}
 		}
 	}
 }
