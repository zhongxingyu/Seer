 package com.censoredsoftware.Modules;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import redis.clients.johm.CollectionMap;
 import redis.clients.johm.Id;
 import redis.clients.johm.Model;
 
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.google.common.collect.Maps;
 
 /**
  * Module to handle the latest messages from a Twitter feed.
  */
 @Model
 public class LatestTweetModule implements Listener
 {
 	@Id
 	private static Long Id;
 	@CollectionMap(key = String.class, value = String.class)
 	private static Map<String, String> messagesData;
 	private static Plugin plugin;
 	private static Logger log = Logger.getLogger("Minecraft");
 	private static URL twitterFeed;
 	private static String pluginName, command, permission, date, link, message;
 	private static boolean notify;
 
 	public Map<String, String> getData()
 	{
 		return this.messagesData;
 	}
 
 	public void save()
 	{
 		DemigodsData.jOhm.save(this);
 	}
 
 	/**
 	 * Constructor to create a new LatestTweetModule.
 	 * 
 	 * @param pl The demigods instance running the module.
 	 * @param screenName The screen-name to the Twitter page.
 	 * @param c The full command for viewing the latest message.
 	 * @param p The full permission node for viewing the latest message.
 	 * @param n True if notifying is allowed.
 	 */
 	public static LatestTweetModule recreate(Plugin pl, String screenName, String c, String p, boolean n)
 	{
 		LatestTweetModule module = null;
 		try
 		{
 			Set<LatestTweetModule> latestTweetModules = DemigodsData.jOhm.getAll(LatestTweetModule.class);
 			for(LatestTweetModule tweet : latestTweetModules)
 			{
 				module = tweet;
 				break;
 			}
 		}
 		catch(Exception ignored)
 		{}
 
 		if(module == null)
 		{
 			module = new LatestTweetModule();
 			module.messagesData = Maps.newHashMap();
 		}
 
 		try
 		{
 			plugin = pl;
 			twitterFeed = new URL("http://api.twitter.com/1/statuses/user_timeline.rss?screen_name=" + screenName);
 			pluginName = pl.getName();
 			command = c;
 			permission = p;
 			notify = n;
 
 			module.save();
 
 			initilize(module);
 		}
 		catch(Exception e)
 		{
 			log.severe("[" + pluginName + "] Could not connect to Twitter.");
 		}
 		return module;
 	}
 
 	/**
 	 * Checks for notifications and notifies if need be.
 	 */
 	public static void initilize(LatestTweetModule module)
 	{
 		// Check for updates, and then update if need be
 		if(notify)
 		{
 			// Define Notify Listener
 			plugin.getServer().getPluginManager().registerEvents(module, plugin);
 
 			for(final Player player : Bukkit.getOnlinePlayers())
 			{
 				if(module.get(player))
 				{
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							player.sendMessage(ChatColor.GREEN + "There is a new message from the " + pluginName + " developers!");
 							player.sendMessage("Please view it now by using " + ChatColor.YELLOW + command);
 						}
 					}, 40);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the latest message from the Twitter feed.
 	 * 
 	 * @return True if successful.
 	 */
 	public synchronized boolean get(OfflinePlayer player)
 	{
 		try
 		{
 			InputStream input = twitterFeed.openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 
 			NodeList messageNodes = document.getElementsByTagName("item").item(0).getChildNodes();
 
 			try
 			{
 				this.date = messageNodes.item(5).getTextContent().substring(0, messageNodes.item(5).getTextContent().lastIndexOf("+"));
 				this.link = messageNodes.item(9).getTextContent().replace("http://", "https://");
 			}
 			catch(Exception e)
 			{
 				log.warning("[" + pluginName + "] Failed to find latest tweet.");
 			}
 			input.close();
 
 			try
 			{
 				URLConnection messageCon = (new URL(link)).openConnection();
 				messageCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"); // FIXES 403 ERROR
 				input = messageCon.getInputStream();
 			}
 			catch(Exception e)
 			{
 				log.warning("[" + pluginName + "] Failed to open connection with twitter page.");
 			}
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 			String line;
 
 			while((line = reader.readLine()) != null)
 			{
 				if(line.trim().startsWith("<p class=\"js-tweet-text tweet-text \">"))
 				{
 					this.message = line.substring(line.indexOf("<p class=\"js-tweet-text tweet-text \">") + 37, line.lastIndexOf("<"));
 					break;
 				}
 			}
 
 			reader.close();
 			input.close();
 
 			String lastMessage = messagesData.get(player.getName());
 
 			save();
 
 			return !(lastMessage != null && lastMessage.equalsIgnoreCase(message));
 		}
 		catch(Exception e)
 		{
 			log.warning("[" + pluginName + "] Failed to load twitter page.");
 		}
 		return false;
 	}
 
 	/**
 	 * The Player Join Listener, listening only.
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		// Define Variables
 		final Player player = event.getPlayer();
 		final String pluginName = this.pluginName;
 		final String command = this.command;
 
 		// Official Messages
 		if(this.notify && (player.isOp() || player.hasPermission(this.permission)))
 		{
 			if(get(player))
 			{
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						player.sendMessage(ChatColor.GREEN + "There is a new message from the " + pluginName + " developers!");
 						player.sendMessage("Please view it now by using " + ChatColor.YELLOW + command);
 					}
 				}, 40);
 			}
 		}
 	}
 
 	/**
 	 * The Player Command Preprocess Listener, cancelling the event if the message command is found, and executing the command inside of the listener.
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST)
 	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
 	{
 		// Define Variables
 		Player player = event.getPlayer();
 		String command = event.getMessage();
 
 		// Check for update command
 		if(command.toLowerCase().startsWith(this.command.toLowerCase()))
 		{
 			// Check Permissions
 			if(!(player.hasPermission(this.permission) || player.isOp()))
 			{
 				player.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
 				event.setCancelled(true);
 				return;
 			}
 
 			// Send the message
 			player.sendMessage(ChatColor.DARK_AQUA + "[" + pluginName + "] " + ChatColor.RESET + "Posted on " + date);
 			player.sendMessage(ChatColor.YELLOW + " Message: " + ChatColor.WHITE + message);
 			player.sendMessage("  ");
 			player.sendMessage(ChatColor.YELLOW + " " + ChatColor.WHITE + link.replace("https://", ""));
 
 			// Set that the message was seen
 			messagesData.put(player.getName(), message);
 			save();
 			event.setCancelled(true);
 		}
 	}
 }
