 package com.ementalo.tcl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.ementalo.helpers.Colors;
import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.Location;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.config.Configuration;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 
 
 public class TeleConfirmLite extends JavaPlugin
 {
 	public static int REQUEST_TIMEOUT = 30;
 	private TeleConfimLiteServerListener serverListener = null;
 	public Configuration config = null;
 	private static Yaml yaml = new Yaml(new SafeConstructor());
 	public final ArrayList<TpAction> pendingRequests = new ArrayList<TpAction>();
 	static final Logger log = Logger.getLogger("Minecraft");
 	public static HashMap<String, Boolean> tpToggle = null;
 	public static HashMap<Player, Location> tpBack = null;
 	public static String accepteeMsg;
 	public static String denyeeMsg;
 	public static String accepterMsg;
 	public static String denierMsg;
 	public static String fromMsg;
 	public static String tpToThemMsg;
 	public static String tpThemToYouMsg;
 	public static String acceptDenyPrompt;
 	public static String playerHasPendingReq;
 	public static String requestSent;
 	public static String msgPositiveColor;
 	public static String msgNegativeColor;
 	private Player other = null;
 	private TpAction req = null;
	public static PermissionHandler permissionHandler = null;
 	Plugin permPlugin = null;
 	static Boolean isDebug = false;
 
 	public void _expire()
 	{
 		final ArrayList<TpAction> removal = new ArrayList<TpAction>();
 		for (final TpAction req : pendingRequests)
 		{
 			if (System.currentTimeMillis() - req.getCreationTime() > REQUEST_TIMEOUT * 1000)
 			{
 				removal.add(req); // queue for removal, it's expired
 			}
 		}
 		for (final TpAction rem : removal)
 		{
 			pendingRequests.remove(rem);
 		}
 	}
 
 	/**
 	 * When the plugin is disabled the plugin will cease to function
 	 */
 	@Override
 	public void onDisable()
 	{
 	}
 
 	@Override
 	public void onEnable()
 	{
 		config = getConfiguration();
 		tpToggle = new HashMap<String, Boolean>();
 		tpBack = new HashMap<Player, Location>();
 		serverListener = new TeleConfimLiteServerListener(this);
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Low, this);
 		pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Low, this);
 
 		try
 		{
 			loadSettings();
 		}
 		catch (Exception ex)
 		{
 			log.log(Level.SEVERE, "[TeleConfimLite] Could not load the config file", ex);
 		}
 		AssignSettings();
 		log.log(Level.INFO, "[" + this.getDescription().getName() + "] [v" + this.getDescription().getVersion() + "]" + " loaded");
 	}
 
 	public TpAction getReceivingActionRequest(Player player)
 	{
 		for (final TpAction req : pendingRequests)
 		{
 			if (!req.getSender().equals(player.getName())
 				&& req.hasPlayer(player.getName()))
 			{
 				return req;
 			}
 		}
 
 		return null;
 	}
 
 	public boolean playerHasPendingRequest(Player player)
 	{
 		for (final TpAction action : pendingRequests)
 		{
 			if (action.getTo().equalsIgnoreCase(player.getName()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void loadSettings() throws Exception
 	{
 		if (!this.getDataFolder().exists())
 		{
 			this.getDataFolder().mkdirs();
 		}
 		config.load();
 		final List<String> keys = config.getKeys(null);
 		if (!keys.contains("msgPositiveColor"))
 			config.setProperty("msgPositiveColor", "green");
 		if (!keys.contains("msgNegativeColor"))
 			config.setProperty("msgNegativeColor", "red");
 		if (!keys.contains("accepteeMsg"))
 			config.setProperty("accepteeMsg", "Accepted request.");
 		if (!keys.contains("denyeeMsg"))
 			config.setProperty("denyeeMsg", "Denied request.");
 		if (!keys.contains("accepterMsg"))
 			config.setProperty("accepterMsg", "Request to teleport accepted.");
 		if (!keys.contains("denierMsg"))
 			config.setProperty("denierMsg", "Request to teleport denied.");
 		if (!keys.contains("fromMsg"))
 			config.setProperty("fromMsg", "%p would like to %t");
 		if (!keys.contains("fromMsg"))
 			config.setProperty("fromMsg", "%p would like to %t");
 		if (!keys.contains("tpToThemMsg"))
 			config.setProperty("tpToThemMsg", "teleport to you");
 		if (!keys.contains("tpThemToYouMsg"))
 			config.setProperty("tpThemToYouMsg", "teleport you to them");
 		if (!keys.contains("acceptDenyPrompt"))
 			config.setProperty("acceptDenyPrompt", "To accept this, type %a. To deny, type %d");
 		if (!keys.contains("playerHasPendingReq"))
 			config.setProperty("playerHasPendingReq", "That player already has a pending request!");
 		if (!keys.contains("playerHasPendingReq"))
 			config.setProperty("requestSent", "Request sent");
 		config.save();
 		config.load();
 	}
 
 	public void AssignSettings()
 	{
 		msgPositiveColor = Colors.parseColor(config.getString("msgPositiveColor", "green"));
 		msgNegativeColor = Colors.parseColor(config.getString("msgNegativeColor", "rose"));
 		accepteeMsg = config.getString("accepteeMsg", "Accepted request.");
 		denyeeMsg = config.getString("denyeeMsg", "Denied request.");
 		accepterMsg = config.getString("accepterMsg", "Request to teleport accepted.");
 		denierMsg = config.getString("denierMsg", "Request to teleport denied.");
 		fromMsg = config.getString("fromMsg", "%p would like to %t");
 		tpToThemMsg = config.getString("tpToThemMsg", "teleport to you");
 		tpThemToYouMsg = config.getString("tpThemToYouMsg", "teleport you to them");
 		acceptDenyPrompt = config.getString("acceptDenyPrompt", "To accept this, type %a. To deny, type %d");
 		playerHasPendingReq = config.getString("playerHasPendingReq", "That player already has a pending request!");
 		requestSent = config.getString("requestSent", "Request sent");
 		isDebug = config.getBoolean("isDebug", false);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command commands,
 							 String commandLabel, String[] args)
 	{
 		String[] split = args;
 		Player player = null;
 		String command = commandLabel;
 
 		if (sender instanceof Player)
 		{
 			player = (Player)sender;
 		}
 		else
 		{
 			sender.sendMessage("This command can only be used ingame");
 			return true;
 		}
 
 		this._expire();
 
 		if (command.equalsIgnoreCase("tpcclear"))
 		{
 			if (hasPermission("tcl.tpcclear", player))
 			{
 				pendingRequests.remove(req);
 				player.sendMessage(ChatColor.GREEN + "Removed requests");
 				return true;
 			}
 			else
 			{
 				player.sendMessage("You do not have permission to use this command");
 				return true;
 			}
 		}
 
 		if (command.equalsIgnoreCase("tpctoggle"))
 		{
 			if (hasPermission("tcl.tpctoggle", player))
 			{
 				toggleTp(player);
 				player.sendMessage(ChatColor.GREEN + "Teleportation" + (hasToggled(player) ? " disabled" : " allowed"));
 				return true;
 			}
 			else
 			{
 				player.sendMessage("You do not have permission to use this command");
 				return true;
 			}
 		}
 		if (command.equalsIgnoreCase("tpc")
 			|| command.equalsIgnoreCase("tpchere"))
 		{
 
 			if (split.length != 1)
 			{
 				player.sendMessage(Colors.Rose + "Usage: /" + commandLabel + " playername");
 				return true;
 			}
 			else
 			{
 				final String modPlayer = split[0];
 				List<Player> targets = this.getServer().matchPlayer(modPlayer);
 
 				if (targets.size() >= 1)
 				{
 					other = targets.get(0);
 				}
 				else
 				{
 					player.sendMessage(Colors.Rose + "Player " + modPlayer + " not found!");
 					return true;
 				}
 			}
 			if (!isDebug)
 			{
 				if (other.getName().equalsIgnoreCase(player.getName()))
 				{
 					player.sendMessage(TeleConfirmLite.msgNegativeColor + "You can not tp to yourself");
 					return true;
 				}
 			}
 			if (this.playerHasPendingRequest(other))
 			{
 				player.sendMessage(TeleConfirmLite.msgNegativeColor + TeleConfirmLite.playerHasPendingReq);
 				return true;
 			}
 
 			if (command.equalsIgnoreCase("tpc"))
 			{
 				if (hasPermission("tcl.tpc", player))
 				{
 					if (hasToggled(other))
 					{
 						player.sendMessage(other.getDisplayName() + ChatColor.GRAY + " is not accepting requests");
 						return true;
 					}
 
 					req = new TpAction(player.getName(), other.getName(),
 									   player.getName(),
 									   TpAction.Actions.TELEPORT_TO_PLAYER, this);
 					processRequest(player);
 					return true;
 
 				}
 				else
 				{
 					player.sendMessage("You do not have permission for this command");
 					return true;
 				}
 			}
 
 			if (command.equalsIgnoreCase("tpchere"))
 			{
 				if (hasPermission("tcl.tpchere", player))
 				{
 					if (hasToggled(other))
 					{
 						player.sendMessage(other.getDisplayName()
 										   + ChatColor.GRAY + " is not accepting requests");
 						return true;
 					}
 					req = new TpAction(player.getName(), player.getName(),
 									   other.getName(),
 									   TpAction.Actions.TELEPORT_PLAYER_TO, this);
 					processRequest(player);
 					return true;
 				}
 				else
 				{
 					player.sendMessage("You do not have permission for this command");
 					return true;
 				}
 			}
 		}
 		if (command.equalsIgnoreCase("tpca") || command.equalsIgnoreCase("tpcd"))
 		{
 			final TpAction req = this.getReceivingActionRequest(player);
 
 			if (req == null)
 			{
 				return true;
 			}
 
 			final Player to = this.getServer().getPlayer(req.getTo());
 			final Player from = this.getServer().getPlayer(req.getFrom());
 
 			if (to == null || from == null)
 			{
 				return true;
 			}
 
 			switch (req.getAction())
 			{
 			case TELEPORT_TO_PLAYER:
 
 				if (command.equalsIgnoreCase("tpca"))
 				{
 					if (hasPermission("tcl.tpca", player))
 					{
 						from.sendMessage(msgPositiveColor + TeleConfirmLite.accepterMsg);
 						to.sendMessage(TeleConfirmLite.msgPositiveColor + TeleConfirmLite.accepteeMsg);
 						req.teleport();
 						return true;
 					}
 					else
 					{
 						player.sendMessage("You do not have permission for that command");
 						return true;
 					}
 				}
 				else if (command.equalsIgnoreCase("tpcd"))
 				{
 					if (hasPermission("tcl.tpcd", player))
 					{
 						from.sendMessage(msgNegativeColor + denierMsg);
 						to.sendMessage(msgNegativeColor + denyeeMsg);
 						return true;
 					}
 					else
 					{
 						player.sendMessage("You do not have permission for that command");
 						return true;
 					}
 
 				}
 
 				break;
 
 			case TELEPORT_PLAYER_TO:
 				if (command.equalsIgnoreCase("tpca"))
 				{
 					if (hasPermission("tcl.tpca", player))
 					{
 						to.sendMessage(TeleConfirmLite.msgPositiveColor + TeleConfirmLite.accepterMsg);
 						from.sendMessage(TeleConfirmLite.msgPositiveColor + TeleConfirmLite.accepteeMsg);
 						req.teleport();
 						return true;
 					}
 					else
 					{
 						player.sendMessage("You do not have permission for that command");
 						return true;
 					}
 				}
 				else if (command.equalsIgnoreCase("tpcd"))
 					if (hasPermission("tcl.tpcd", player))
 					{
 						to.sendMessage(TeleConfirmLite.msgNegativeColor + TeleConfirmLite.denierMsg);
 						from.sendMessage(TeleConfirmLite.msgNegativeColor + TeleConfirmLite.denyeeMsg);
 						return true;
 					}
 					else
 					{
 						player.sendMessage("You do not have permission for that command");
 						return true;
 					}
 
 				break;
 			}
 			this.pendingRequests.remove(req);
 		}
 
 		if (commandLabel.equalsIgnoreCase("tpcback"))
 		{
 			if (hasPermission("tcl.tpcback", player))
 			{
 				player.sendMessage("Teleporting to your previous location");
 				player.teleport(getBackLocation(player));
 				return true;
 			}
 			else
 			{
 				player.sendMessage("You do not have permission for that command");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void processRequest(Player player)
 	{
 		req.sendRequest();
 		player.sendMessage(msgPositiveColor + requestSent);
 		this.pendingRequests.add(req);
 	}
 
 	@SuppressWarnings(
 	{
 		"rawtypes", "unchecked"
 	})
 	public Boolean hasPermission(String node, Player base)
 	{
 		if (permPlugin == null)
 		{
 			return true;
 		}
		if (permissionHandler == null)
		{
			permissionHandler = ((Permissions)permPlugin).getHandler();
		}
		return permissionHandler.has(base, node);
 	}
 
 	public void toggleTp(Player player)
 	{
 		String name = player.getName();
 		if (tpToggle.get(name) == null)
 		{
 			tpToggle.put(name, false);
 		}
 		else
 		{
 			if (tpToggle.get(name) == true ? tpToggle.put(name, false)
 				: tpToggle.put(name, true))
 				;
 		}
 	}
 
 	public void addBackLocation(Player player, Location location)
 	{
 		tpBack.put(player, location);
 	}
 
 	public Location getBackLocation(Player player)
 	{
 		if (player == null) return null;
 		return tpBack.get(player);
 	}
 
 	public Boolean hasToggled(Player player)
 	{
 		try
 		{
 			if (tpToggle.isEmpty())
 				return false;
 			if (tpToggle.get(player.getName()) == true)
 			{
 				return false;
 			}
 			else
 			{
 				return true;
 			}
 		}
 		catch (NullPointerException np)
 		{
 			return false;
 		}
 	}
 }
