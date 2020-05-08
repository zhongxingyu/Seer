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
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 
 import lombok.Getter;
 import net.dmulloy2.teamsparkle.commands.CmdBuy;
 import net.dmulloy2.teamsparkle.commands.CmdGiveTokens;
 import net.dmulloy2.teamsparkle.commands.CmdHelp;
 import net.dmulloy2.teamsparkle.commands.CmdInvite;
 import net.dmulloy2.teamsparkle.commands.CmdLeaderboard;
 import net.dmulloy2.teamsparkle.commands.CmdReload;
 import net.dmulloy2.teamsparkle.commands.CmdShop;
 import net.dmulloy2.teamsparkle.commands.CmdStats;
 import net.dmulloy2.teamsparkle.handlers.CommandHandler;
 import net.dmulloy2.teamsparkle.handlers.LogHandler;
 import net.dmulloy2.teamsparkle.handlers.PermissionHandler;
 import net.dmulloy2.teamsparkle.handlers.ResourceHandler;
 import net.dmulloy2.teamsparkle.handlers.ShopHandler;
 import net.dmulloy2.teamsparkle.io.PlayerDataCache;
 import net.dmulloy2.teamsparkle.listeners.PlayerListener;
 import net.dmulloy2.teamsparkle.types.PlayerData;
 import net.dmulloy2.teamsparkle.util.FormatUtil;
 import net.dmulloy2.teamsparkle.util.TimeUtil;
 import net.dmulloy2.teamsparkle.util.Util;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * @author dmulloy2
  */
 
 public class TeamSparkle extends JavaPlugin
 {
 	/** Handlers **/
 	private @Getter PermissionHandler permissionHandler;
 	private @Getter ResourceHandler resourceHandler;
 	private @Getter CommandHandler commandHandler;
 	private @Getter ShopHandler shopHandler;
 	private @Getter LogHandler logHandler;
 
 	/** Data Cache **/
 	private @Getter PlayerDataCache playerDataCache;
 
 	/** Global Prefix **/
 	private @Getter String prefix = FormatUtil.format("&6[&4&lTS&6] ");
 
 	@Override
 	public void onEnable()
 	{
 		long start = System.currentTimeMillis();
 
 		logHandler = new LogHandler(this);
 
 		/** Configuration **/
 		saveDefaultConfig();
 		reloadConfig();
 
 		/** Register Handlers **/
 		saveResource("messages.properties", true);
 		resourceHandler = new ResourceHandler(this, getClassLoader());
 
 		permissionHandler = new PermissionHandler(this);
 		commandHandler = new CommandHandler(this);
 		shopHandler = new ShopHandler(this);
 
 		playerDataCache = new PlayerDataCache(this);
 
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
 
 		/** Register Listener **/
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PlayerListener(this), this);
 
 		/** Deploy Auto Save task **/
 		if (getConfig().getBoolean("autoSave.enabled"))
 		{
 			long interval = TimeUtil.toTicks(60 * getConfig().getInt("autoSave.interval"));
 
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					playerDataCache.save();
 				}
 			}.runTaskTimerAsynchronously(this, interval, interval);
 		}
 
 		/** Hourly Rewards **/
 		long interval = TimeUtil.toTicks(60 * 60); // 1 hour
 		if (! getConfig().getStringList("hourlyRewards").isEmpty())
 			new HourlyRewardTask().runTaskTimerAsynchronously(this, interval, interval);
 
 		long finish = System.currentTimeMillis();
 
 		outConsole(getMessage("log_enabled"), getDescription().getFullName(), finish - start);
 	}
 
 	@Override
 	public void onDisable()
 	{
 		long start = System.currentTimeMillis();
 
 		/** Save Data **/
 		playerDataCache.save();
 
 		shopHandler.onDisable();
 
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
 		logHandler.debug(string, objects);
 	}
 
 	/**
 	 * Gets a message with a given key from the messages.properties
 	 * 
 	 * @param string
 	 *        - Message key
 	 * @return Associated message, or null if nonexistant
 	 */
 	public final String getMessage(String string)
 	{
 		try
 		{
 			return resourceHandler.getMessages().getString(string);
 		}
 		catch (MissingResourceException ex)
 		{
 			outConsole(Level.WARNING, getMessage("log_message_missing"), string);
 			return null;
 		}
 	}
 
 	public final boolean isSparkled(String name)
 	{
 		return getSparkler(name) != null;
 	}
 
 	public final boolean isSparkled(Player player)
 	{
 		return getSparkler(player) != null;
 	}
 
 	public final String getSparkler(String name)
 	{
 		for (Entry<String, PlayerData> entry : playerDataCache.getAllLoadedPlayerData().entrySet())
 		{
 			List<String> invited = entry.getValue().getInvited();
 			if (! invited.isEmpty())
 			{
 				if (invited.contains(name))
 					return entry.getKey();
 			}
 		}
 
 		return null;
 	}
 
 	public final String getSparkler(Player player)
 	{
 		return getSparkler(player.getName());
 	}
 
 	/**
 	 * Processes a sparkled player
 	 * 
 	 * @param sparkled
 	 *        - Sparkled {@link Player}
 	 */
 	public final void handleSparkle(Player sparkled)
 	{
 		String sparkler = getSparkler(sparkled);
 		if (sparkler != null)
 		{
 			outConsole("Handling sparkle of: {0}. Sparkler: {1}", sparkled.getName(), sparkler);
 
 			// Commands for newly joined sparkleds
 			List<String> commands = getConfig().getStringList("sparkledRewards");
 			if (! commands.isEmpty())
 			{
 				for (String command : commands)
 				{
					command = replacePlayerVars(command, sparkled);
 					if (! getServer().dispatchCommand(getServer().getConsoleSender(), command))
 					{
 						// Oh no, something went wrong >:(
 						outConsole(Level.WARNING, "Could not execute command \"{0}\"", command);
 					}
 					else
 					{
 						debug("Executed command \"{0}\"", command);
 					}
 				}
 			}
 			else
 			{
 				debug("\"sparkledRewards\" list is empty! Not rewarding {0}", sparkled.getName());
 			}
 
 			// Welcome them
 			sparkled.sendMessage(FormatUtil.format(getMessage("sparkled_welcome"), sparkled.getName()));
 
 			PlayerData data = playerDataCache.getData(sparkler);
 			if (data != null)
 			{
 				// Reward the sparkler
 				data.setTokens(data.getTokens() + 1);
 				data.setTotalSparkles(data.getTotalSparkles() + 1);
 				data.getInvited().remove(sparkled.getName());
 
 				debug("Setting total sparkles for {0} to {1}", sparkler, data.getTotalSparkles());
 			}
 
 			// Thank them, if online
 			OfflinePlayer sparklerp = Util.matchOfflinePlayer(sparkler);
 			if (sparklerp != null && sparklerp.isOnline())
 			{
 				((Player) sparklerp).sendMessage(FormatUtil.format(getMessage("sparkler_thanks"), sparkled.getName()));
 			}
 		}
 		else
 		{
 			// Should never happen...
 			debug("Could not get sparkler for {0}", sparkled.getName());
 		}
 	}
 
 	/**
 	 * Gives a player an item, then refreshes their inventory
 	 * 
 	 * @param player
 	 *        - {@link Player} to give item to
 	 * @param stack
 	 *        - {@link ItemStack} to give the player
 	 */
 	@SuppressWarnings("deprecation")
 	public final void giveItem(Player player, ItemStack stack)
 	{
 		player.getInventory().addItem(stack);
 		player.updateInventory();
 	}
 
 	/**
 	 * Replaces variables for player names
 	 * 
 	 * @param string
 	 *        - Base string to format
 	 * @param player
 	 *        - {@link Player} to replace vars for
 	 */
 	public final String replacePlayerVars(String string, Player player)
 	{
 		string = string.replaceAll("%p", player.getName());
 		string = string.replaceAll("%player", player.getName());
 		string = string.replaceAll("@p", player.getName());
 		string = string.replaceAll("@player", player.getName());
 		return string;
 	}
 
 	/**
 	 * Hourly Reward Task
 	 */
 	public final class HourlyRewardTask extends BukkitRunnable
 	{
 		@Override
 		public void run()
 		{
 			String message = getMessage("hourly_reward");
 			String serverName = getConfig().getString("serverName");
 			for (Player player : getServer().getOnlinePlayers())
 			{
 				List<String> rewards = getConfig().getStringList("hourlyRewards");
 				if (! rewards.isEmpty())
 				{
 					int rand = Util.random(rewards.size());
 					String entry = rewards.get(rand);
 					if (entry != null)
 					{
 						String[] split = entry.split(";");
 						String command = replacePlayerVars(split[0], player);
 						getServer().dispatchCommand(getServer().getConsoleSender(), command);
 
 						player.sendMessage(FormatUtil.format(message, serverName, split[1]));
 					}
 				}
 			}
 		}
 	}
 }
