 package com.censoredsoftware.Modules;
 
 import java.io.InputStream;
 import java.net.URL;
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
 
 import redis.clients.johm.Attribute;
 import redis.clients.johm.CollectionMap;
 import redis.clients.johm.Id;
 import redis.clients.johm.Model;
 
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.google.common.collect.Maps;
 
 /**
  * Module to handle the latest messages from a Twitter feed.
  */
 @Model
 public class LatestTweetModule implements Listener
 {
	private static Plugin plugin;
	private static Logger log = Logger.getLogger("Minecraft");
	private static URL twitterFeed;
	private static String pluginName, command, permission, date;
	private static boolean notify;
 	@Id
 	private Long Id;
 	@Attribute
 	private String link;
 	@CollectionMap(key = String.class, value = String.class)
 	private Map<String, String> messagesData;
 
 	public Map<String, String> getData()
 	{
 		return this.messagesData;
 	}
 
 	public void save()
 	{
 		save(this);
 	}
 
 	public static void save(LatestTweetModule tweet)
 	{
 		DataUtility.jOhm.save(tweet);
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
 			Set<LatestTweetModule> latestTweetModules = DataUtility.jOhm.getAll(LatestTweetModule.class);
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
 			module.get();
 
 			initilize(module);
 		}
 		catch(Exception e)
 		{
 			log.severe("[" + pluginName + "] Could not connect to Twitter.");
 			e.printStackTrace();
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
 	 * @return The message.
 	 */
 	public synchronized String get()
 	{
 		try
 		{
 			InputStream input = twitterFeed.openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 
 			NodeList messageNodes = document.getElementsByTagName("item").item(0).getChildNodes();
 
 			try
 			{
 				date = messageNodes.item(5).getTextContent().substring(0, messageNodes.item(5).getTextContent().lastIndexOf("+"));
 				this.link = messageNodes.item(9).getTextContent().replace("http://", "https://");
 			}
 			catch(Exception e)
 			{
 				log.warning("[" + pluginName + "] Failed to find latest tweet.");
 			}
 			input.close();
 		}
 		catch(Exception e)
 		{
 			log.warning("[" + pluginName + "] Failed to load twitter page.");
 		}
 		save();
 		return link;
 	}
 
 	/**
 	 * Gets the latest message from the Twitter feed.
 	 * 
 	 * @return True if successful.
 	 */
 	public boolean get(OfflinePlayer player)
 	{
 		get();
 		String lastMessage = messagesData.get(player.getName());
 		return !(lastMessage != null && lastMessage.equalsIgnoreCase(this.link));
 	}
 
 	/**
 	 * The Player Join Listener, listening only.
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		// Define Variables
 		Player player = event.getPlayer();
 		String pluginName = LatestTweetModule.pluginName;
 		String command = LatestTweetModule.command;
 
 		// Official Messages
 		if(notify && (player.isOp() || player.hasPermission(permission)))
 		{
 			// Seperate to prevent extra load when this option is disabled.
 			if(get(player))
 			{
 				player.sendMessage(ChatColor.GREEN + "There is a new message from the " + pluginName + " developers!");
 				player.sendMessage("Please view it now by using " + ChatColor.YELLOW + command);
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
 		if(command.toLowerCase().startsWith(LatestTweetModule.command.toLowerCase()))
 		{
 			// Check Permissions
 			if(!(player.hasPermission(permission) || player.isOp()))
 			{
 				player.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
 				event.setCancelled(true);
 				return;
 			}
 
 			// Send the message
 			player.sendMessage(ChatColor.DARK_AQUA + "[" + pluginName + "] " + ChatColor.RESET + "Posted on " + date);
 			player.sendMessage(ChatColor.YELLOW + " Link: ");
 			player.sendMessage(ChatColor.YELLOW + " " + ChatColor.WHITE + this.link.replace("https://", ""));
 
 			// Set that the message was seen
 			messagesData.put(player.getName(), this.link);
 			save();
 			event.setCancelled(true);
 		}
 	}
 }
