 package com.ghomerr.travelgates;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.TreeSpecies;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.ghomerr.travelgates.constants.TravelGatesConstants;
 import com.ghomerr.travelgates.enums.TravelGatesCommands;
 import com.ghomerr.travelgates.enums.TravelGatesConfigurations;
 import com.ghomerr.travelgates.enums.TravelGatesOptions;
 import com.ghomerr.travelgates.enums.TravelGatesPermissionsNodes;
 import com.ghomerr.travelgates.enums.TravelGatesWorldType;
 import com.ghomerr.travelgates.listeners.TravelGatesCommandExecutor;
 import com.ghomerr.travelgates.listeners.TravelGatesPlayerListener;
 import com.ghomerr.travelgates.listeners.TravelGatesPortalListener;
 import com.ghomerr.travelgates.listeners.TravelGatesSignListener;
 import com.ghomerr.travelgates.messages.TravelGatesMessages;
 import com.ghomerr.travelgates.messages.TravelGatesMessagesManager;
 import com.ghomerr.travelgates.objects.TravelGatesOptionsContainer;
 import com.ghomerr.travelgates.objects.TravelGatesTeleportBlock;
 import com.ghomerr.travelgates.utils.TravelGatesUtils;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class TravelGates extends JavaPlugin
 {
 	private static final Logger _LOGGER = Logger.getLogger(TravelGatesConstants.MINECRAFT);
 
 	// Misc
 	private boolean _pluginEnabled = false;
 	private PluginManager _pm = null;
 
 	// config
 	private String _language = TravelGatesConstants.DEFAULT_LANGUAGE;
 	private boolean _usePermissions = false;
 	private boolean _teleportWithSign = true;
 	private boolean _teleportWithPortal = false;
 	private boolean _clearAllInventory = false;
 	private boolean _protectAdminInventory = false;
 	private boolean _autosave = false;
 	private boolean _isDebugEnabled = false;
 	private TravelGatesTeleportBlock _tpBlock = new TravelGatesTeleportBlock();
 
 	// messages
 	private TravelGatesMessagesManager _messages = null;
 	private String _portalSignOnState = null;
 	private String _portalSignOffState = null;
 
 	// Cache
 	private HashMap<String, String> _mapShortLocationsByDest = new HashMap<String, String>();
 	private HashMap<String, Location> _mapLocationsByDest = new HashMap<String, Location>();
 	private HashMap<String, String> _mapDestinationsByShortLoc = new HashMap<String, String>();
 	private HashMap<String, TravelGatesOptionsContainer> _mapOptionsByDest = new HashMap<String, TravelGatesOptionsContainer>();
 	private HashMap<String, Integer> _mapMaterialIdByName = new HashMap<String, Integer>();
 	private HashMap<String, DyeColor> _mapDyeColorByName = new HashMap<String, DyeColor>();
 	private HashMap<String, TreeSpecies> _mapTreeSpeciesByName = new HashMap<String, TreeSpecies>();
 	private HashSet<String> _setAdditionalWorlds = new HashSet<String>();
 
 	// Files and data
 	private Properties _configData = new Properties();
 	private File _configFile = null;
 	private Properties _destinationsData = new Properties();
 	private File _destinationsFile = null;
 	private Properties _restrictionsData = new Properties();
 	private File _restrictionsFile = null;
 
 	// Permissions
 	private boolean _useNativePermissions = false;
 	private boolean _usePermissionsBukkit = false;
 	private boolean _usePermissionsEx = false;
 	private PermissionHandler _permHandler = null;
 	private PermissionManager _permManager = null;
 
 	// Listeners
 	public TravelGatesPlayerListener playerListener = null;
 	public TravelGatesPortalListener portalListener = null;
 	public TravelGatesSignListener portalSignListener = null;
 
 	// Constants
 	private final String _tag = TravelGatesConstants.PLUGIN_TAG;
 	private final String _debug = TravelGatesConstants.DEBUG_TAG;
 
 	public void onEnable()
 	{
 		super.onEnable();
 		
 		// Must be done before loadXXX() methods !
 		_pm = getServer().getPluginManager();
 		
 		// Load Configuration
 		_pluginEnabled = loadConfiguration();
 		if (_pluginEnabled)
 		{
 			// Must be loaded before loadConfiguration()
 			_pluginEnabled = _pluginEnabled && loadAdditionalWorld();
 			_pluginEnabled = _pluginEnabled && loadMessages();
 			_pluginEnabled = _pluginEnabled && loadPermissions();
 			_pluginEnabled = _pluginEnabled && loadDestinations();
 		}
 
 		if (!_pluginEnabled)
 		{
 			_LOGGER.severe(_tag + " Plugin loading failed. All commands are disabled.");
 		}
 		else
 		{
 			playerListener = new TravelGatesPlayerListener(this);
 			portalListener = new TravelGatesPortalListener(this);
 			portalSignListener = new TravelGatesSignListener(this);
 
 			final TravelGatesCommandExecutor commandeExecutor = new TravelGatesCommandExecutor(this);
 
 			// Register commands
 			for (final String cmd : TravelGatesCommands.TRAVELGATES.list())
 			{
 				final PluginCommand pluginCmd = this.getCommand(cmd);
 				if (pluginCmd != null)
 				{
 					pluginCmd.setExecutor(commandeExecutor);
 				}
 				else
 				{
 					_LOGGER.severe(_tag + " Command " + cmd + " could not be added.");
 				}
 			}
 		}
 
 		// End
 		_LOGGER.info(_tag + " Plugin loading done. There are " + _mapShortLocationsByDest.size() + " destinations loaded.");
 	}
 
 	public void onDisable()
 	{
 		super.onDisable();
 
 		saveAll();
 
 		_LOGGER.info(_tag + " Plugin unloading done.");
 	}
 
 	public boolean saveConfiguration()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start saveConfiguration()");
 		}
 
 		boolean saveSuccess = saveFile(_configFile, _configData, TravelGatesConstants.CONFIG_FILE_NAME);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End saveConfiguration : " + saveSuccess);
 		}
 		return saveSuccess;
 	}
 
 	public boolean saveDestinations()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start saveDestinations()");
 		}
 
 		boolean saveSuccess = saveFile(_destinationsFile, _destinationsData, TravelGatesConstants.DEST_FILE_NAME);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End saveDestinations : " + saveSuccess);
 		}
 		return saveSuccess;
 	}
 
 	public boolean saveRestrictions()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start saveRestrictions()");
 		}
 
 		boolean saveSuccess = saveFile(_restrictionsFile, _restrictionsData, TravelGatesConstants.RESTRICTIONS_FILE_NAME);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End saveRestrictions : " + saveSuccess);
 		}
 		return saveSuccess;
 	}
 
 	public boolean saveData()
 	{
 		return saveDestinations() && saveRestrictions();
 	}
 
 	public boolean saveAll()
 	{
 		return saveConfiguration() && saveData();
 	}
 
 	private boolean saveFile(final File file, final Properties data, final String fileName)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start saveFile : " + fileName);
 		}
 
 		boolean ret = false;
 
 		if (file != null && file.exists())
 		{
 			if (data != null && !data.isEmpty())
 			{
 				FileOutputStream out = null;
 				try
 				{
 					out = new FileOutputStream(file);
 				}
 				catch (final FileNotFoundException ex)
 				{
 					_LOGGER.severe(_tag + " File " + fileName + " not found. ");
 					ex.printStackTrace();
 				}
 
 				try
 				{
 					data.store(out, null);
 					out.close();
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " End saveFile : " + true);
 					}
 					ret = true;
 				}
 				catch (final IOException ex)
 				{
 					_LOGGER.severe(_tag + " " + fileName + " file update failed !");
 					ex.printStackTrace();
 				}
 			}
 			else
 			{
 				_LOGGER.info(_tag + " No data to save in " + fileName);
 				ret = true;
 			}
 		}
 		else
 		{
 			_LOGGER.severe(_tag + " File " + fileName + " doesn't exist !");
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End saveFile : " + ret);
 		}
 		return ret;
 	}
 
 	public boolean isPluginEnabled()
 	{
 		return _pluginEnabled;
 	}
 
 	public void addDestination(final Player player, final String destination, final Location loc, final TravelGatesOptionsContainer container)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start addDestination(destination=" + destination + ", container=" + container + ", player=" + player + ")");
 		}
 
 		final String shortLoc = TravelGatesUtils.locationToShortString(loc);
 		String fullLoc = TravelGatesUtils.locationToFullString(loc);
 
 		final String lowerCaseDest = destination.toLowerCase();
 
 		_mapShortLocationsByDest.put(lowerCaseDest, shortLoc);
 		_mapLocationsByDest.put(lowerCaseDest, TravelGatesUtils.shortStringToLocation(shortLoc, getServer().getWorlds()));
 		_mapDestinationsByShortLoc.put(shortLoc, lowerCaseDest);
 
 		fullLoc = fullLoc + TravelGatesConstants.DELIMITER + container.getOptionsForData();
 
 		_destinationsData.put(lowerCaseDest, fullLoc);
 		if (container.has(TravelGatesOptions.RESTRICTION))
 		{
 			_restrictionsData.put(destination, container.getRestrictionsListString());
 		}
 
 		if (container.has(TravelGatesOptions.SAVE) || _autosave)
 		{
 			final boolean saved = saveData();
 			if (saved)
 			{
 				player.sendMessage(ChatColor.GREEN + _messages.get(TravelGatesMessages.SAVE_DONE));
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.SAVE_FAILED));
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End addDestination");
 		}
 	}
 
 	public Location getLocationFromPosition(final Player player, final Location playerLoc, final String position)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getLocationFromPosition(player=" + player + ", playerLoc=" + playerLoc + ", position=" + position + ")");
 		}
 
 		Location destinationLocation = null;
 
 		if (TravelGatesUtils.stringIsBlank(position))
 		{
 			destinationLocation = playerLoc;
 		}
 		else
 		{
 			final String[] positionData = position.split(TravelGatesConstants.DELIMITER);
 			final int numberOfItems = positionData.length;
 
 			if (numberOfItems == 3)
 			{
 				try
 				{
 					final World world = player.getWorld();
 
 					destinationLocation = TravelGatesUtils.getDestinationLocation(world, positionData, playerLoc,
 							TravelGatesConstants.POSITION_WITHOUT_WORLD);
 				}
 				catch (final Throwable th)
 				{
 					player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.WRONG_POSITION_VALUE));
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " Exception caught : ");
 					}
 					th.printStackTrace();
 				}
 			}
 			else if (numberOfItems == 4)
 			{
 				try
 				{
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " World name : " + positionData[0]);
 					}
 					final World world = getServer().getWorld(positionData[0]);
 
 					destinationLocation = TravelGatesUtils.getDestinationLocation(world, positionData, playerLoc,
 							TravelGatesConstants.POSITION_WITH_WORLD);
 				}
 				catch (final Throwable th)
 				{
 					player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.WRONG_POSITION_VALUE));
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " Exception caught : ");
 					}
 					th.printStackTrace();
 				}
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.WRONG_POSITION_VALUE));
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getLocationFromPosition : " + destinationLocation);
 		}
 
 		return destinationLocation;
 	}
 
 	public void deleteDestination(final String destination, final boolean save, final Player player)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start deleteDestination(destination=" + destination + ")");
 		}
 
 		final String lowerCaseDest = destination.toLowerCase();
 
 		_destinationsData.remove(lowerCaseDest);
 		_restrictionsData.remove(lowerCaseDest);
 
 		_mapDestinationsByShortLoc.remove(_mapShortLocationsByDest.get(lowerCaseDest));
 		_mapLocationsByDest.remove(lowerCaseDest);
 		_mapShortLocationsByDest.remove(lowerCaseDest);
 
 		_mapOptionsByDest.get(lowerCaseDest).clear();
 		_mapOptionsByDest.remove(lowerCaseDest);
 
 		if (save || _autosave)
 		{
 			final boolean saved = saveData();
 			if (saved)
 			{
 				player.sendMessage(ChatColor.GREEN + _messages.get(TravelGatesMessages.SAVE_DONE));
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.SAVE_FAILED));
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End deleteDestination");
 		}
 	}
 
 	public boolean hasDestination(final String destination)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start hasDestination(destination=" + destination + ")");
 		}
 
 		final boolean hasDest = _mapShortLocationsByDest.containsKey(destination.toLowerCase());
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End hasDestination : " + hasDest);
 		}
 
 		return hasDest;
 	}
 
 	public boolean hasLocation(final Location loc)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start hasLocation(loc=" + loc + ")");
 		}
 
 		final String shortLoc = TravelGatesUtils.locationToShortString(loc);
 		final boolean hasLoc = hasShortLocation(shortLoc); // _shortLocations.containsValue(shortLoc);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End hasLocation : " + hasLoc);
 		}
 		return hasLoc;
 	}
 
 	public boolean hasShortLocation(final String shortLoc)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start hasShortLocation(loc=" + shortLoc + ")");
 		}
 
 		final boolean hasShortLoc = _mapDestinationsByShortLoc.containsKey(shortLoc);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End hasShortLocation : " + hasShortLoc);
 		}
 		return hasShortLoc;
 	}
 
 	public String getDestinationsList()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start destList()");
 		}
 
 		final StringBuilder strBuild = new StringBuilder();
 
 		strBuild.append(_messages.get(TravelGatesMessages.AVAILABLE_DESTINATIONS));
 
 		final int initLength = strBuild.length();
 
 		for (final String dest : _mapShortLocationsByDest.keySet())
 		{
 			strBuild.append(" ").append(ChatColor.AQUA).append(dest).append(ChatColor.YELLOW).append(TravelGatesConstants.DELIMITER);
 		}
 
 		final int endLength = strBuild.length();
 
 		if (initLength < endLength)
 		{
 			strBuild.deleteCharAt(endLength - 1);
 		}
 		else
 		{
 			strBuild.append(ChatColor.AQUA).append(_messages.get(TravelGatesMessages.NONE));
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End destList : " + strBuild.toString());
 		}
 
 		return strBuild.toString();
 	}
 
 	public String getRestrictionsList(final String destination)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start restrictionsList(destination=" + destination + ")");
 		}
 
 		final String lowerCaseDest = destination.toLowerCase();
 
 		final StringBuilder strBuild = new StringBuilder();
 
 		strBuild.append(_messages.get(TravelGatesMessages.RESTRICTED_DESTINATIONS_ARE, ChatColor.AQUA + lowerCaseDest + ChatColor.YELLOW));
 
 		final int initLength = strBuild.length();
 
 		final HashSet<String> restrictedDests = _mapOptionsByDest.get(destination).getRestrictionsList();
 
 		if (restrictedDests != null)
 		{
 			for (final String dest : restrictedDests)
 			{
 				strBuild.append(" ").append(ChatColor.AQUA).append(dest).append(ChatColor.YELLOW).append(TravelGatesConstants.DELIMITER);
 			}
 		}
 
 		final int endLength = strBuild.length();
 
 		if (initLength < endLength)
 		{
 			strBuild.deleteCharAt(endLength - 1);
 		}
 		else
 		{
 			strBuild.append(ChatColor.AQUA).append(" ").append(_messages.get(TravelGatesMessages.ALL));
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End restrictionsList : " + strBuild.toString());
 		}
 
 		return strBuild.toString();
 	}
 
 	public String getDestinationsDetailsList()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start destDetailedList()");
 		}
 
 		final StringBuilder strBuild = new StringBuilder();
 
 		strBuild.append(_messages.get(TravelGatesMessages.AVAILABLE_DESTINATIONS));
 
 		final int initLength = strBuild.length();
 
 		for (final String dest : _mapShortLocationsByDest.keySet())
 		{
 			strBuild.append(getDestinationDetails(dest));
 		}
 
 		final int endLength = strBuild.length();
 
 		if (initLength == endLength)
 		{
 			strBuild.append(ChatColor.AQUA).append(_messages.get(TravelGatesMessages.NONE));
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End destDetailedList : " + strBuild.toString());
 		}
 
 		return strBuild.toString();
 	}
 
 	public String getDestinationDetails(final String dest)
 	{
 		final StringBuilder strBuild = new StringBuilder();
 
 		if (strBuild.length() > 0)
 		{
 			strBuild.append(TravelGatesConstants.DELIMITER).append(" ");
 		}
 
 		final boolean inventoryCleared = getOptionOfDestination(dest, TravelGatesOptions.INVENTORY);
 		final String msgInventory = (inventoryCleared) ? ChatColor.RED + _messages.get(TravelGatesMessages.INVENTORY_CLEAR) : ChatColor.GREEN
 				+ _messages.get(TravelGatesMessages.INVENTORY_KEEP);
 
 		final boolean isAdminTP = getOptionOfDestination(dest, TravelGatesOptions.ADMINTP);
 		final String msgAdmin = (isAdminTP) ? ChatColor.RED + _messages.get(TravelGatesMessages.ADMIN_TP) : ChatColor.GREEN
 				+ _messages.get(TravelGatesMessages.FREE_TP);
 
 		final boolean isRestricted = getOptionOfDestination(dest, TravelGatesOptions.RESTRICTION);
 		final String msgRestrictions = (isRestricted) ? ChatColor.RED + _messages.get(TravelGatesMessages.DEST_RESTRICTED) : ChatColor.GREEN
 				+ _messages.get(TravelGatesMessages.DEST_FREE);
 
 		strBuild.append(ChatColor.AQUA).append(dest).append(ChatColor.YELLOW).append("=(").append(ChatColor.GREEN)
 				.append(_mapShortLocationsByDest.get((String) dest).toLowerCase()).append(ChatColor.YELLOW).append(")[").append(msgAdmin)
 				.append(ChatColor.YELLOW).append(TravelGatesConstants.DELIMITER).append(msgInventory).append(ChatColor.YELLOW)
 				.append(TravelGatesConstants.DELIMITER).append(msgRestrictions).append(ChatColor.YELLOW).append("]");
 
 		return strBuild.toString();
 	}
 
 	public String getCurrentConfiguration()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getCurrentConfiguration()");
 		}
 
 		final StringBuilder strBuild = new StringBuilder();
 		strBuild.append(ChatColor.YELLOW).append(_messages.get(TravelGatesMessages.CURRENT_CONFIG));
 
 		for (final Object o : _configData.keySet())
 		{
 			final String key = o.toString();
 			final String value = _configData.getProperty(key);
 			final Boolean boolValue = new Boolean(value);
 
 			if (TravelGatesConfigurations.LANGUAGE.value().equalsIgnoreCase(key))
 			{
 				strBuild.append(" ").append(ChatColor.AQUA).append(TravelGatesConfigurations.LANGUAGE.value()).append(ChatColor.YELLOW).append("=")
 						.append(ChatColor.WHITE).append(_language);
 			}
 			else if (TravelGatesConfigurations.TPBLOCK.value().equalsIgnoreCase(key))
 			{
 				strBuild.append(" ").append(ChatColor.AQUA).append(TravelGatesConfigurations.TPBLOCK.value()).append(ChatColor.YELLOW).append("=")
 						.append(ChatColor.WHITE).append(_tpBlock);
 			}
 			else if (TravelGatesConfigurations.WORLDS.value().equalsIgnoreCase(key))
 			{
 				strBuild.append(" ").append(ChatColor.AQUA).append(TravelGatesConfigurations.WORLDS.value()).append(ChatColor.YELLOW).append("=")
 						.append(ChatColor.WHITE).append(getListOfAdditionnalWorld());
 			}
 			else
 			{
 				strBuild.append(" ").append(ChatColor.AQUA).append(key).append(ChatColor.YELLOW).append("=")
 						.append((boolValue.booleanValue()) ? ChatColor.GREEN : ChatColor.RED).append(value);
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getCurrentConfiguration : " + strBuild.toString());
 		}
 
 		return strBuild.toString();
 	}
 
 	public TravelGatesOptionsContainer getOptionsOfDestination(final String destination)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getOptionsOfDestination(destination=" + destination + ")");
 		}
 
 		final TravelGatesOptionsContainer container = _mapOptionsByDest.get(destination.toLowerCase());
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getOptionsOfDestination : " + container);
 		}
 
 		return container;
 	}
 
 	public boolean getOptionOfDestination(final String destination, final TravelGatesOptions option)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getOptionOfDestination(destination=" + destination + ", option=" + option + ")");
 		}
 
 		String lowerDest = null;
 		boolean optionValue = false;
 
 		try
 		{
 			lowerDest = destination.toLowerCase();
 		}
 		catch (final Throwable th)
 		{
 			_LOGGER.severe(_tag + " Exception caught while getting lower case of destination : " + destination);
 			th.printStackTrace();
 		}
 
 		if (lowerDest != null)
 		{
 			final TravelGatesOptionsContainer container = _mapOptionsByDest.get(lowerDest);
 
 			optionValue = container.has(option);
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getOptionOfDestination : " + optionValue);
 		}
 
 		return optionValue;
 	}
 
 	public void setOptionOfDestination(final String destination, final TravelGatesOptions option, final boolean newValue)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start setOptionOfDestination(destination=" + destination + ", option=" + option + ", newValue=" + newValue + ")");
 		}
 
 		final TravelGatesOptionsContainer container = _mapOptionsByDest.get(destination.toLowerCase());
 
 		container.set(option, newValue);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End setOptionOfDestination");
 		}
 	}
 
 	public String getShortLoc(final String destination)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getShortLoc(destination=" + destination + ")");
 		}
 
 		final String shortLoc = _mapShortLocationsByDest.get(destination.toLowerCase());
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getShortLoc : " + shortLoc);
 		}
 
 		return shortLoc;
 	}
 
 	public Location getLocation(final String destination)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getLocation(destination=" + destination + ")");
 		}
 
 		final Location loc = _mapLocationsByDest.get(destination.toLowerCase());
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getLocation : " + loc);
 		}
 
 		return loc;
 	}
 
 	public String getFullLoc(final String destination)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getFullLoc(destination=" + destination + ")");
 		}
 
 		final String fullLoc = _destinationsData.getProperty(destination.toLowerCase());
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getFullLoc : " + fullLoc);
 		}
 
 		return fullLoc;
 	}
 
 	public String getDestination(final Location location)
 	{
 		String dest = null;
 
 		final String shortLoc = TravelGatesUtils.locationToShortString(location);
 
 		dest = getDestination(shortLoc);
 
 		return dest;
 	}
 
 	public String getDestination(final String shortLoc)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getDestination(shortLoc=" + shortLoc + ")");
 		}
 
 		String dest = null;
 
 		dest = _mapDestinationsByShortLoc.get(shortLoc);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getDestination : " + dest);
 		}
 
 		return dest;
 	}
 
 	public String getDestPattern()
 	{
 		return TravelGatesConstants.DESTINATION_NAME_PATTERN;
 	}
 
 	public boolean teleportPlayerToDest(final String dest, final Player player, final boolean destHasBeenChecked, 
 			final boolean ignorePlayerLocation, final String portalDestinationShortLoc)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start teleportPlayerToDest(dest=" + dest + ", player=" + player + ", destHasBeenChecked=" + destHasBeenChecked
 					+ ", ignorePlayerLocation=" + ignorePlayerLocation + ")");
 		}
 
 		final String destination = dest.toLowerCase();
 
 		if (ignorePlayerLocation || destHasBeenChecked || hasDestination(destination))
 		{
 			final String fullLoc = getFullLoc(destination);
 			final String shortLoc = getShortLoc(destination);
 
 			if (getOptionOfDestination(destination, TravelGatesOptions.ADMINTP))
 			{
 				if (!hasPermission(player, TravelGatesPermissionsNodes.ADMINTP))
 				{
 					player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.ONLY_ADMIN_TP));
 					return false;
 				}
 			}
 
 			final Location playerLocation = player.getLocation();
 			final String playerShortLoc = TravelGatesUtils.locationToShortString(playerLocation);
 
 			final boolean targetAndCurrentLocationAreDifferent = !shortLoc.equalsIgnoreCase(playerShortLoc);
 			final boolean playerIsOnExistingDestination = hasShortLocation(playerShortLoc);
 			boolean playerNotOnTeleportBlock = false;
 
 			String nearestDestinationShortLocation = null;
 
 			if (!ignorePlayerLocation)
 			{
 				if (playerIsOnExistingDestination || portalDestinationShortLoc != null)
 				{
 					if (portalDestinationShortLoc == null && _tpBlock.isEnabled() && !_tpBlock.isTPBlock(player.getWorld().getBlockAt(playerLocation).getRelative(BlockFace.DOWN)))
 					{
 						playerNotOnTeleportBlock = true;
 					}
 
 					if (playerIsOnExistingDestination)
 					{
 						nearestDestinationShortLocation = playerShortLoc;
 					}
 					else if (portalDestinationShortLoc != null)
 					{
 						nearestDestinationShortLocation = portalDestinationShortLoc;
 					}
 				}
 				else
 				{
 					if (_tpBlock.isEnabled())
 					{
 						if (_isDebugEnabled)
 						{
 							_LOGGER.info("#0 : Not on dest and tp block enabled");
 						}
 
 						final World currWorld = player.getWorld();
 						if (_isDebugEnabled)
 						{
 							_LOGGER.info("#0-bis : currWorld = " + currWorld);
 							_LOGGER.info("#0-ters : type block = " + currWorld.getBlockAt(playerLocation).getRelative(BlockFace.DOWN).getType());
 						}
 
 						playerNotOnTeleportBlock = !_tpBlock.isTPBlock(currWorld.getBlockAt(playerLocation).getRelative(BlockFace.DOWN));
 
 						if (_isDebugEnabled)
 						{
 							_LOGGER.info("#1 : playerNotOnTeleportBlock = " + playerNotOnTeleportBlock);
 						}
 
 						if (!playerNotOnTeleportBlock)
 						{
 							// Search the locations in the current player's
 							// world
 							ArrayList<Location> rightWorldsList = new ArrayList<Location>();
 							for (final Object key : _mapLocationsByDest.keySet())
 							{
 								final Location loc = _mapLocationsByDest.get(key);
 
 								if (_isDebugEnabled)
 								{
 									_LOGGER.info("#1bis : key = " + key + " ; playerLocation.getWorld()=" + playerLocation.getWorld() 
 										+ " ; loc.getWorld()= " + loc.getWorld());
 								}
 								
 								if (playerLocation.getWorld() == loc.getWorld())
 								{
 									rightWorldsList.add(loc);
 								}
 							}
 
 							if (_isDebugEnabled)
 							{
 								_LOGGER.info("#2 : rightWorldsList size = " + rightWorldsList.size());
 							}
 
 							if (!rightWorldsList.isEmpty())
 							{
 								// Search the locations at the same height
 								ArrayList<Location> rightHeightList = new ArrayList<Location>();
 								for (final Location loc : rightWorldsList)
 								{
 									if (loc.getBlockY() == playerLocation.getBlockY())
 									{
 										rightHeightList.add(loc);
 									}
 								}
 
 								if (_isDebugEnabled)
 								{
 									_LOGGER.info("#3 : rightHeightList size = " + rightHeightList.size());
 								}
 
 								if (!rightHeightList.isEmpty())
 								{
 									// Search the nearest destination from
 									// the
 									// Player's location
 									Location nearestDestinationLocation = null;
 									int lastMinX = TravelGatesConstants.MAX_COORDINATE;
 									int lastMinZ = TravelGatesConstants.MAX_COORDINATE;
 
 									if (_isDebugEnabled)
 									{
 										_LOGGER.info("#3-a : nearestDestinationLocation = " + nearestDestinationLocation);
 									}
 
 									for (final Location loc : rightHeightList)
 									{
 										if (_isDebugEnabled)
 										{
 											_LOGGER.info("#3-b : loc = " + loc);
 										}
 
 										if (nearestDestinationLocation == null)
 										{
 											lastMinX = TravelGatesUtils.getCoordinateDiff(loc.getBlockX(), playerLocation.getBlockX());
 											lastMinZ = TravelGatesUtils.getCoordinateDiff(loc.getBlockZ(), playerLocation.getBlockZ());
 											nearestDestinationLocation = loc;
 
 											if (_isDebugEnabled)
 											{
 												_LOGGER.info("#3-c : lastMinX = " + lastMinX + " ; lastMinZ = " + lastMinZ);
 											}
 										}
 										else
 										{
 											final int xDiff = TravelGatesUtils.getCoordinateDiff(loc.getBlockX(), playerLocation.getBlockX());
 											final int zDiff = TravelGatesUtils.getCoordinateDiff(loc.getBlockZ(), playerLocation.getBlockZ());
 
 											if (_isDebugEnabled)
 											{
 												_LOGGER.info("#3-d : xDiff = " + xDiff + " ; zDiff = " + zDiff);
 												_LOGGER.info("#3-e : lastMinX = " + lastMinX + " ; lastMinZ = " + lastMinZ);
 											}
 
 											if (xDiff + zDiff <= lastMinX + lastMinZ)
 											{
 												lastMinX = xDiff;
 												lastMinZ = zDiff;
 												nearestDestinationLocation = loc;
 											}
 										}
 
 										if (_isDebugEnabled)
 										{
 											_LOGGER.info("#3-f : nearestDestinationLocation = " + nearestDestinationLocation);
 										}
 									}
 
 									if (_isDebugEnabled)
 									{
 										_LOGGER.info("#4 : nearestDestinationLocation = " + nearestDestinationLocation);
 									}
 
 									if (nearestDestinationLocation != null)
 									{
 										int pX = playerLocation.getBlockX();
 										int pZ = playerLocation.getBlockZ();
 
 										if (_isDebugEnabled)
 										{
 											_LOGGER.info("#5 : pX = " + pX + " ; pZ = " + pZ);
 										}
 
 										final int dX = nearestDestinationLocation.getBlockX();
 										final int dZ = nearestDestinationLocation.getBlockZ();
 
 										if (_isDebugEnabled)
 										{
 											_LOGGER.info("#6 : dX = " + dX + " ; dZ = " + dZ);
 										}
 
 										int xDiff = TravelGatesUtils.getCoordinateDiff(dX, pX);
 										int zDiff = TravelGatesUtils.getCoordinateDiff(dZ, pZ);
 
 										if (_isDebugEnabled)
 										{
 											_LOGGER.info("#7 : xDiff = " + xDiff + " ; zDiff = " + zDiff);
 										}
 
 										// The nearest destination is at 5
 										// blocks max from the player
 										if (xDiff <= TravelGatesConstants.MAX_TARGET_RANGE && zDiff <= TravelGatesConstants.MAX_TARGET_RANGE)
 										{
 											final int offsetX = ((pX - dX) > 0) ? -1 : 1;
 											final int offsetZ = ((pZ - dZ) > 0) ? -1 : 1;
 
 											if (_isDebugEnabled)
 											{
 												_LOGGER.info("#8 : offsetX = " + offsetX + " ; offsetZ = " + offsetZ);
 											}
 
 											final int heightOfBeneathBlock = playerLocation.getBlockY() - 1;
 
 											if (_isDebugEnabled)
 											{
 												_LOGGER.info("#9 : heightOfBeneathBlock = " + heightOfBeneathBlock);
 											}
 
 											// Is blocks between player and
 											// the
 											// nearest destination are TP
 											// blocks
 											// ?
 											while (xDiff > 0 && !playerNotOnTeleportBlock)
 											{
 												pX += offsetX;
 												playerNotOnTeleportBlock = !_tpBlock.isTPBlock(currWorld.getBlockAt(pX, heightOfBeneathBlock, pZ));
 												xDiff = TravelGatesUtils.getCoordinateDiff(dX, pX);
 
 												if (_isDebugEnabled)
 												{
 													_LOGGER.info("#10 : pX = " + pX + " ; xDiff = " + xDiff + " ; playerNotOnTeleportBlock = "
 															+ playerNotOnTeleportBlock);
 												}
 											}
 
 											if (!playerNotOnTeleportBlock)
 											{
 												while (zDiff > 0 && !playerNotOnTeleportBlock)
 												{
 													pZ += offsetZ;
 													playerNotOnTeleportBlock = !_tpBlock
 															.isTPBlock(currWorld.getBlockAt(pX, heightOfBeneathBlock, pZ));
 													zDiff = TravelGatesUtils.getCoordinateDiff(dZ, pZ);
 
 													if (_isDebugEnabled)
 													{
 														_LOGGER.info("#11 : pZ = " + pZ + " ; zDiff = " + zDiff + " ; playerNotOnTeleportBlock = "
 																+ playerNotOnTeleportBlock);
 													}
 												}
 
 												// Get the short loc of the
 												// nearest destination
 												if (!playerNotOnTeleportBlock)
 												{
 													nearestDestinationShortLocation = TravelGatesUtils
 															.locationToShortString(nearestDestinationLocation);
 												}
 											}
 										}
 										else
 										{
 											playerNotOnTeleportBlock = true;
 										}
 									}
 									else
 									{
 										playerNotOnTeleportBlock = true;
 									}
 								}
 								else
 								{
 									playerNotOnTeleportBlock = true;
 								}
 							}
 							else
 							{
 								playerNotOnTeleportBlock = true;
 							}
 						}
 					}
 				}
 			}
 
 			if (_isDebugEnabled)
 			{
 				_LOGGER.info("#12 : playerNotOnTeleportBlock = " + playerNotOnTeleportBlock);
 				_LOGGER.info("#13 : nearestDestinationShortLocation = " + nearestDestinationShortLocation);
 			}
 			if (_tpBlock.isEnabled() && playerNotOnTeleportBlock)
 			{
 				player.sendMessage(ChatColor.RED
 						+ _messages.get(TravelGatesMessages.NOT_STANDING_ON_TPBLOCK, ChatColor.YELLOW + _tpBlock.toString() + ChatColor.RED));
 				return false;
 			}
 
 			if (ignorePlayerLocation || targetAndCurrentLocationAreDifferent
					&& (playerIsOnExistingDestination || !playerNotOnTeleportBlock && _tpBlock.isEnabled()))
 			{
 				final String currentDest = _mapDestinationsByShortLoc.get(nearestDestinationShortLocation);
 
 				if (_isDebugEnabled)
 				{
 					_LOGGER.info("#14 : currentDest = " + currentDest);
 				}
 
 				if (!ignorePlayerLocation)
 				{
 					if (currentDest != null && getOptionOfDestination(currentDest, TravelGatesOptions.RESTRICTION))
 					{
 						final TravelGatesOptionsContainer container = _mapOptionsByDest.get(currentDest);
 
 						if (container != null && !container.isDestinationAllowed(destination))
 						{
 							player.sendMessage(ChatColor.RED
 									+ _messages.get(TravelGatesMessages.DESTINATION_IS_RESTRICTED, ChatColor.AQUA + currentDest + ChatColor.RED,
 											ChatColor.AQUA + destination + ChatColor.RED));
 							return false;
 						}
 					}
 					else if (currentDest == null)
 					{
 						player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.NO_STANDING_ON_DESTINATION));
 						return false;
 					}
 				}
 
 				final Location targetLocation = TravelGatesUtils.fullStringToLocation(fullLoc, player.getServer().getWorlds());
 
 				if (targetLocation.getWorld() != null)
 				{
 					player.teleport(targetLocation);
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED
 							+ _messages.get(TravelGatesMessages.TELEPORT_CANCELLED_WORLD_UNLOADED, ChatColor.AQUA + destination + ChatColor.RED));
 					return false;
 				}
 
 				if (ignorePlayerLocation)
 				{
 					_LOGGER.info(_tag + " " + player.getName() + " has forced the travel from " + playerShortLoc + " to " + destination);
 				}
 				else
 				{
 					_LOGGER.info(_tag + " " + player.getName() + " has travelled from " + currentDest + " to " + destination);
 				}
 
 				final boolean inventoryCleared = getOptionOfDestination(destination, TravelGatesOptions.INVENTORY);
 //				System.out.println("inventoryCleared=" + inventoryCleared + "; _protectAdminInventory=" + _protectAdminInventory 
 //						+ "; perm=" + hasPermission(player, TravelGatesPermissionsNodes.PROTECTADMININV));
 				if (!inventoryCleared || isProtectedInventory(player))
 				{
 					player.sendMessage(ChatColor.YELLOW
 							+ _messages.get(TravelGatesMessages.YOU_ARE_ARRIVED_AT, ChatColor.AQUA + destination + ChatColor.YELLOW)
 							+ ChatColor.GREEN + _messages.get(TravelGatesMessages.INVENTORY_KEPT));
 				}
 				else
 				{
 					String inventoryMessage = "";
 					final PlayerInventory inventory = player.getInventory();
 
 					if (_clearAllInventory)
 					{
 						inventory.setArmorContents(null);
 						inventoryMessage = _messages.get(TravelGatesMessages.ALL_INVENTORY_LOST);
 					}
 					else
 					{
 						inventoryMessage = _messages.get(TravelGatesMessages.INVENTORY_LOST);
 					}
 
 					inventory.clear();
 
 					player.sendMessage(ChatColor.YELLOW
 							+ _messages.get(TravelGatesMessages.YOU_ARE_ARRIVED_AT, ChatColor.AQUA + destination + ChatColor.YELLOW) + ChatColor.RED
 							+ inventoryMessage);
 				}
 
 				// If the arrival chunk is unloaded, it will be forced to load
 				final Chunk arrivalChunk = player.getWorld().getChunkAt(targetLocation);
 				if (!arrivalChunk.isLoaded())
 				{
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " The " + destination + "'s chunk was not loaded at " + targetLocation);
 					}
 
 					arrivalChunk.load();
 				}
 
 				if (_isDebugEnabled)
 				{
 					_LOGGER.info(_debug + " End teleportPlayerToDest : true");
 				}
 
 				return true;
 			}
 			else
 			{
 				if (!targetAndCurrentLocationAreDifferent)
 				{
 					player.sendMessage(ChatColor.RED
 							+ _messages.get(TravelGatesMessages.YOURE_ALREADY_AT, ChatColor.AQUA + destination + ChatColor.RED));
 				}
 				else if (!playerIsOnExistingDestination)
 				{
 					player.sendMessage(ChatColor.RED
 							+ _messages.get(TravelGatesMessages.YOU_CANT_GO_THERE, ChatColor.AQUA + destination + ChatColor.RED));
 				}
 			}
 		}
 		else
 		{
 			player.sendMessage(ChatColor.RED
 					+ _messages.get(TravelGatesMessages.DESTINATION_DOESNT_EXIST, ChatColor.AQUA + destination + ChatColor.RED));
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End teleportPlayerToDest : false");
 		}
 
 		return false;
 	}
 
 	public String getMessage(final TravelGatesMessages message, final String... vars)
 	{
 		return _messages.get(message, vars);
 	}
 
 	public boolean usePermissions()
 	{
 		return _usePermissions;
 	}
 
 	private boolean loadDestinations()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start loadDestinations()");
 		}
 
 		boolean ret = false;
 		boolean isfileCreated = false;
 
 		_destinationsFile = new File(TravelGatesConstants.PLUGIN_FILE_PATH);
 
 		if (!_destinationsFile.exists())
 		{
 			try
 			{
 				final File rootDir = new File(TravelGatesConstants.PLUGIN_ROOT_PATH);
 				isfileCreated = rootDir.mkdir();
 
 				isfileCreated &= _destinationsFile.createNewFile();
 				_destinationsFile.setReadable(true, false);
 				_destinationsFile.setWritable(true, false);
 				_destinationsFile.setExecutable(true, false);
 			}
 			catch (final IOException ioex)
 			{
 				_LOGGER.severe(_tag + " Destinations file creation failed !");
 				ioex.printStackTrace();
 			}
 		}
 		else
 		{
 			isfileCreated = true;
 		}
 
 		if (!isfileCreated)
 		{
 			_LOGGER.severe(_tag + " Destinations file creation failed !");
 		}
 		else
 		{
 			FileInputStream in = null;
 			try
 			{
 				in = new FileInputStream(_destinationsFile);
 			}
 			catch (final FileNotFoundException ex)
 			{
 				_LOGGER.info(_tag + " Destinations file failed to be read : ");
 				ex.printStackTrace();
 			}
 
 			if (in != null)
 			{
 				try
 				{
 					_destinationsData.load(in);
 					in.close();
 				}
 				catch (final IOException ex)
 				{
 					_LOGGER.severe(_tag + " Error while reading the Destinations file.");
 					ex.printStackTrace();
 				}
 
 				if (!_destinationsData.isEmpty())
 				{
 					for (final Object key : _destinationsData.keySet())
 					{
 						final String dest = String.valueOf(key).toLowerCase();
 
 						final String fullString = _destinationsData.getProperty(dest);
 						final String shortLoc = TravelGatesUtils.fullStringToShortString(fullString);
 
 						_mapShortLocationsByDest.put(dest, shortLoc);
 						_mapLocationsByDest.put(dest, TravelGatesUtils.shortStringToLocation(shortLoc, getServer().getWorlds()));
 						_mapDestinationsByShortLoc.put(shortLoc, dest);
 						final TravelGatesOptionsContainer container = new TravelGatesOptionsContainer(this, fullString.substring(1 + fullString
 								.lastIndexOf(TravelGatesConstants.DELIMITER)));
 						_mapOptionsByDest.put(dest, container);
 					}
 
 					loadRestrictions();
 				}
 
 				ret = true;
 			}
 			else
 			{
 				_LOGGER.info(_tag + " Destinations file could not be loaded.");
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End loadDestinations : " + ret);
 		}
 
 		return ret;
 	}
 
 	private void loadRestrictions()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start loadRestrictions()");
 		}
 
 		_restrictionsFile = new File(TravelGatesConstants.PLUGIN_RESTRICTIONS_FILE_PATH);
 
 		if (_restrictionsFile.exists())
 		{
 			FileInputStream in = null;
 			try
 			{
 				in = new FileInputStream(_restrictionsFile);
 			}
 			catch (final FileNotFoundException ex)
 			{
 				_LOGGER.info(_tag + " Restrictions file not found.");
 				ex.printStackTrace();
 				return;
 			}
 
 			if (in != null)
 			{
 				try
 				{
 					_restrictionsData.load(in);
 					in.close();
 				}
 				catch (final IOException ex)
 				{
 					_LOGGER.severe(_tag + " Error while reading the Restrictions file.");
 					ex.printStackTrace();
 				}
 
 				if (!_restrictionsData.isEmpty())
 				{
 					for (final Object key : _restrictionsData.keySet())
 					{
 						final String dest = String.valueOf(key).toLowerCase();
 						final TravelGatesOptionsContainer optionsContainer = _mapOptionsByDest.get(dest);
 
 						if (optionsContainer.has(TravelGatesOptions.RESTRICTION))
 						{
 							optionsContainer.setRestrictionsList(_restrictionsData.getProperty(dest));
 						}
 					}
 				}
 			}
 		}
 		else
 		{
 			_LOGGER.info(_tag + " Restrictions file not found. New file created with the name : " + TravelGatesConstants.RESTRICTIONS_FILE_NAME);
 
 			try
 			{
 				_restrictionsFile.createNewFile();
 				_restrictionsFile.setReadable(true, false);
 				_restrictionsFile.setWritable(true, false);
 				_restrictionsFile.setExecutable(true, false);
 			}
 			catch (final IOException e)
 			{
 				_LOGGER.severe(_tag + " Unable to create Restriction file: ");
 				e.printStackTrace();
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End loadRestrictions");
 		}
 	}
 
 	private boolean loadConfiguration()
 	{
 		_configFile = new File(TravelGatesConstants.PLUGIN_CONFIG_PATH);
 
 		if (_configFile.exists())
 		{
 			FileInputStream in = null;
 			try
 			{
 				in = new FileInputStream(_configFile);
 			}
 			catch (final Throwable ex)
 			{
 				_LOGGER.severe(_tag + " Unable to create a stream to read the configuration file.");
 				ex.printStackTrace();
 				return false;
 			}
 
 			if (in != null)
 			{
 				// LOAD CONFIGURATIONS
 				try
 				{
 					_configData.load(in);
 					in.close();
 				}
 				catch (final IOException ex)
 				{
 					_LOGGER.severe(_tag + " Error while loading the Configuration file.");
 					ex.printStackTrace();
 					return false;
 				}
 
 				// DEBUG
 				try
 				{
 					final String debugEnabled = _configData.getProperty(TravelGatesConfigurations.DEBUG.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(debugEnabled))
 					{
 						_isDebugEnabled = Boolean.parseBoolean(debugEnabled.toLowerCase());
 						TravelGatesUtils.setDebugState(_isDebugEnabled);
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Debug configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Debug configuration set to : " + _isDebugEnabled);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Debug configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// LANGUAGE
 				try
 				{
 					final String language = _configData.getProperty(TravelGatesConfigurations.LANGUAGE.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(language))
 					{
 						_language = language.toLowerCase();
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Language configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Language configuration set to : " + _language);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Language configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// USE PERMISSIONS
 				try
 				{
 					final String usePermissions = _configData.getProperty(TravelGatesConfigurations.USEPERMISSIONS.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(usePermissions))
 					{
 						_usePermissions = Boolean.parseBoolean(usePermissions.toLowerCase());
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Permissions configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Permissions configuration set to : " + _usePermissions);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Permissions configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// TELEPORT MODES
 				try
 				{
 					final String teleportWithSign = _configData.getProperty(TravelGatesConfigurations.TELEPORTWITHSIGN.value());
 					final String teleportWithPortal = _configData.getProperty(TravelGatesConfigurations.TELEPORTWITHPORTAL.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(teleportWithSign))
 					{
 						_teleportWithSign = Boolean.parseBoolean(teleportWithSign.toLowerCase());
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Sign teleportation configuration not found.");
 					}
 
 					if (TravelGatesUtils.stringIsNotBlank(teleportWithPortal))
 					{
 						_teleportWithPortal = Boolean.parseBoolean(teleportWithPortal.toLowerCase());
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Portal teleportation configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Teleport modes configuration set to : sign=" + _teleportWithSign + ", portal=" + _teleportWithPortal);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Teleport modes configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// CLEAR ALL INVENTORY
 				try
 				{
 					final String clearAllInventory = _configData.getProperty(TravelGatesConfigurations.CLEARALLINVENTORY.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(clearAllInventory))
 					{
 						_clearAllInventory = Boolean.parseBoolean(clearAllInventory.toLowerCase());
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Clear all inventory configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Clear all inventory configuration set to : " + _clearAllInventory);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Clear all inventory configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 				
 				// PROTECT ADMIN INVENTORY
 				try
 				{
 					final String protectAdminInventory = 
 							_configData.getProperty(TravelGatesConfigurations.PROTECTADMININVENTORY.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(protectAdminInventory))
 					{
 						_protectAdminInventory = Boolean.parseBoolean(protectAdminInventory.toLowerCase());
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Protect admin inventory configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Protect admin inventory configuration set to : " + _protectAdminInventory);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Protect admin inventory configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// AUTO SAVE
 				try
 				{
 					final String autosave = _configData.getProperty(TravelGatesConfigurations.AUTOSAVE.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(autosave))
 					{
 						_autosave = Boolean.parseBoolean(autosave.toLowerCase());
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Autosave configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " Autosave configuration set to : " + _autosave);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Autosave configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// TP BLOCK
 				try
 				{
 					final String tpblock = _configData.getProperty(TravelGatesConfigurations.TPBLOCK.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(tpblock))
 					{
 						loadMaterialTypes();
 						configTeleportBlock(tpblock, false);
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " TP Block configuration not found.");
 					}
 
 					_LOGGER.info(_tag + " TP Block configuration set to : " + _tpBlock);
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " TP Block configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 
 				// ADDITIONAL WORLDS
 				try
 				{
 					final String worlds = _configData.getProperty(TravelGatesConfigurations.WORLDS.value());
 
 					if (TravelGatesUtils.stringIsNotBlank(worlds))
 					{
 						final String[] worldsList = worlds.split(TravelGatesConstants.DELIMITER);
 
 						for (final String world : worldsList)
 						{
 							_setAdditionalWorlds.add(world);
 						}
 					}
 					else
 					{
 						_LOGGER.warning(_tag + " Additional Worlds configuration not found.");
 					}
 
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " Additional Worlds configuration loaded with : " + _setAdditionalWorlds.size() + " worlds.");
 					}
 				}
 				catch (final Throwable th)
 				{
 					_LOGGER.severe(_tag + " Additional Worlds configuration reading failed.");
 					th.printStackTrace();
 					return false;
 				}
 			}
 		}
 		else
 		{
 			_LOGGER.info(_tag + " Configuration file not found. New file created with the name : " + TravelGatesConstants.CONFIG_FILE_NAME);
 
 			try
 			{
 				_configFile.createNewFile();
 				_configFile.setReadable(true, false);
 				_configFile.setWritable(true, false);
 				_configFile.setExecutable(true, false);
 
 				// Add default config
 				_configData.put(TravelGatesConfigurations.AUTOSAVE.value(), String.valueOf(_autosave));
 				_configData.put(TravelGatesConfigurations.CLEARALLINVENTORY.value(), String.valueOf(_clearAllInventory));
 				_configData.put(TravelGatesConfigurations.PROTECTADMININVENTORY.value(), String.valueOf(_protectAdminInventory));
 				_configData.put(TravelGatesConfigurations.DEBUG.value(), String.valueOf(_isDebugEnabled));
 				_configData.put(TravelGatesConfigurations.LANGUAGE.value(), _language);
 				_configData.put(TravelGatesConfigurations.TELEPORTWITHPORTAL.value(), String.valueOf(_teleportWithPortal));
 				_configData.put(TravelGatesConfigurations.TELEPORTWITHSIGN.value(), String.valueOf(_teleportWithSign));
 				_configData.put(TravelGatesConfigurations.TPBLOCK.value(), _tpBlock.toString());
 				_configData.put(TravelGatesConfigurations.USEPERMISSIONS.value(), String.valueOf(_usePermissions));
 			}
 			catch (IOException e)
 			{
 				_LOGGER.severe(_tag + " Unable to create Configuration file: ");
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End loadConfiguration");
 		}
 		
 		return true;
 	}
 
 	private boolean loadMessages()
 	{
 		_messages = new TravelGatesMessagesManager(this, _language);
 		if (_messages != null)
 		{
 			_portalSignOnState = _messages.get(TravelGatesMessages.ON);
 			_portalSignOffState = _messages.get(TravelGatesMessages.OFF);
 			return true;
 		}
 		return false;
 	}
 
 	private boolean loadPermissions()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start loadPermissions()");
 		}
 
 		try
 		{
 			if (_usePermissions)
 			{
 				// Search PermissionsBukkit plugin
 				Plugin permPlugin = _pm.getPlugin(TravelGatesConstants.PLUGIN_PERMISSIONS_BUKKIT);
 
 				if (permPlugin != null)
 				{
 					if (permPlugin.isEnabled())
 					{
 						_usePermissionsBukkit = true;
 						if (_isDebugEnabled)
 						{
 							_LOGGER.info(_debug + " " + permPlugin.getDescription().getFullName() + "  will be used to manage Permissions.");
 						}
 					}
 					else
 					{
 						_usePermissionsBukkit = forcePermissionsPluginLoading(permPlugin);
 						_usePermissions = _usePermissionsBukkit;
 					}
 				}
 				else
 				{
 					// Search PermissionsEx plugin
 					permPlugin = _pm.getPlugin(TravelGatesConstants.PLUGIN_PERMISSIONS_EX);
 
 					if (permPlugin != null)
 					{
 						if (!permPlugin.isEnabled())
 						{
 							_usePermissions = forcePermissionsPluginLoading(permPlugin);
 						}
 
 						if (_usePermissions)
 						{
 							_permManager = PermissionsEx.getPermissionManager();
 
 							if (_permManager != null)
 							{
 								_usePermissionsEx = true;
 								if (_isDebugEnabled)
 								{
 									_LOGGER.info(_debug + " " + permPlugin.getDescription().getFullName() + "  will be used to manage Permissions.");
 								}
 							}
 							else
 							{
 								_usePermissions = false;
 								_LOGGER.warning(_tag + " " + permPlugin.getDescription().getFullName()
 										+ "  has not been loaded correctly. Permissions disabled.");
 							}
 						}
 					}
 					else
 					{
 						// Search Permissions 2x/3x plugin
 						permPlugin = _pm.getPlugin(TravelGatesConstants.PLUGIN_PERMISSONS);
 
 						if (permPlugin != null)
 						{
 							if (!permPlugin.isEnabled())
 							{
 								_usePermissions = forcePermissionsPluginLoading(permPlugin);
 							}
 
 							if (_usePermissions)
 							{
 								_permHandler = ((Permissions) permPlugin).getHandler();
 
 								if (_permHandler != null)
 								{
 									if (_isDebugEnabled)
 									{
 										_LOGGER.info(_debug + " " + permPlugin.getDescription().getFullName()
 												+ "  will be used to manage Permissions.");
 									}
 								}
 								else
 								{
 									_usePermissions = false;
 									_LOGGER.warning(_tag + " " + permPlugin.getDescription().getFullName()
 											+ "  has not been loaded correctly. Permissions disabled.");
 								}
 							}
 						}
 						else
 						{
 							// Use Native Bukkit's Permission is used
 							_useNativePermissions = true;
 
 							_LOGGER.info(_debug
 									+ " Bukkit's Native Permissions system is used. Do not forget to fill the permissions.yml file of your server.");
 						}
 					}
 				}
 			}
 		}
 		catch (final Throwable th)
 		{
 			_usePermissions = false;
 			_LOGGER.severe(_tag + " Permissions loading has failed. Permissions disabled.");
 			th.printStackTrace();
 			return false;
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End loadPermissions");
 		}
 		
 		return true;
 	}
 
 	private void loadMaterialTypes()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start loadMaterialTypes()");
 		}
 
 		for (final Material mat : Material.values())
 		{
 			switch (mat)
 			{
 				case BEDROCK:
 				case BOOKSHELF:
 				case BRICK:
 				case CLAY:
 				case COAL_ORE:
 				case COBBLESTONE:
 				case DIAMOND_BLOCK:
 				case DIAMOND_ORE:
 				case DIRT:
 				case ENDER_STONE:
 				case FURNACE:
 				case GLASS:
 				case GLOWING_REDSTONE_ORE:
 				case GLOWSTONE:
 				case GOLD_BLOCK:
 				case GOLD_ORE:
 				case GRASS:
 				case GRAVEL:
 				case ICE:
 				case IRON_BLOCK:
 				case IRON_ORE:
 				case JACK_O_LANTERN:
 				case JUKEBOX:
 				case LAPIS_BLOCK:
 				case LAPIS_ORE:
 				case LOG:
 				case MELON_BLOCK:
 				case MOSSY_COBBLESTONE:
 				case MYCEL:
 				case NETHER_BRICK:
 				case NETHERRACK:
 				case NOTE_BLOCK:
 				case OBSIDIAN:
 				case PUMPKIN:
 				case REDSTONE:
 				case SAND:
 				case SANDSTONE:
 				case SMOOTH_BRICK:
 				case SNOW_BLOCK:
 				case SOUL_SAND:
 				case SPONGE:
 				case STONE:
 				case TNT:
 				case WOOD:
 				case WOOL:
 				case WORKBENCH:
 					_mapMaterialIdByName.put(mat.name().toLowerCase(), new Integer(mat.getId()));
 			}
 
 		}
 		for (final DyeColor color : DyeColor.values())
 		{
 			_mapDyeColorByName.put(color.name().toLowerCase(), color);
 		}
 		for (final TreeSpecies species : TreeSpecies.values())
 		{
 			_mapTreeSpeciesByName.put(species.name().toLowerCase(), species);
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " _materialTypeMap has " + _mapMaterialIdByName.size() + " elements.");
 			_LOGGER.info(_debug + " _colorMap has " + _mapDyeColorByName.size() + " elements.");
 			_LOGGER.info(_debug + " _treeSpeciesMap has " + _mapTreeSpeciesByName.size() + " elements.");
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End loadMaterialTypes");
 		}
 	}
 
 	private boolean forcePermissionsPluginLoading(final Plugin permPlugin)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start forcePermissionsPluginLoading(plugin=" + permPlugin.getDescription().getFullName() + ")");
 		}
 
 		boolean pluginLoaded = false;
 
 		try
 		{
 			_pm.enablePlugin(permPlugin);
 
 			if (permPlugin.isEnabled())
 			{
 				pluginLoaded = true;
 			}
 		}
 		catch (final Throwable th)
 		{
 			_LOGGER.warning(_tag + " " + permPlugin.getDescription().getFullName() + " could not have been forced to load. Permissions disabled.");
 			th.printStackTrace();
 		}
 
 		if (pluginLoaded)
 		{
 			if (_isDebugEnabled)
 			{
 				_LOGGER.info(_debug + " " + permPlugin.getDescription().getFullName()
 						+ "  has been forced to load and will be used to manage Permissions.");
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End forcePermissionsPluginLoading : " + pluginLoaded);
 		}
 
 		return pluginLoaded;
 	}
 
 	public boolean hasPermission(final Player player, final TravelGatesPermissionsNodes permissionNode)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start hasPermission(player=" + player + ", permissionNode=" + permissionNode + ")");
 		}
 
 		boolean hasPerm = false;
 
 		try
 		{
 			if (_usePermissions)
 			{
 				if (_usePermissionsBukkit || _useNativePermissions)
 				{
 					hasPerm = player.hasPermission(permissionNode.getNode());
 				}
 				else if (_usePermissionsEx)
 				{
 					hasPerm = _permManager.has(player, permissionNode.getNode());
 				}
 				else
 				{
 					hasPerm = _permHandler.has(player, permissionNode.getNode());
 				}
 			}
 			else
 			{
 				if (permissionNode.isAdminOnly())
 				{
 					if (player.isOp())
 					{
 						hasPerm = true;
 					}
 				}
 				else
 				{
 					hasPerm = true;
 				}
 			}
 		}
 		catch (final Throwable th)
 		{
 			_LOGGER.severe(_tag + " Permissions access has failed. Permissions disabled.");
 			th.printStackTrace();
 			_usePermissions = false;
 		}
 
 		if (!hasPerm)
 		{
 			player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.USER_NOT_ALLOWED));
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End hasPermission : " + hasPerm);
 		}
 
 		return hasPerm;
 	}
 
 	public boolean updateDestination(final String destination, final TravelGatesOptionsContainer container, final Player player)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start updateDestination(destination=" + destination + ", container=" + container + ", player=" + player + ")");
 		}
 
 		boolean optionsUpdated = false;
 
 		String fullStringLocation = null;
 
 		if (container.has(TravelGatesOptions.POSITION))
 		{
 			final Location newLocation = getLocationFromPosition(player, player.getLocation(), container.getPosition());
 
 			if (newLocation != null)
 			{
 				fullStringLocation = TravelGatesUtils.locationToFullString(newLocation) + TravelGatesConstants.DELIMITER;
 
 				final String shortStringLocation = TravelGatesUtils.locationToShortString(newLocation);
 				final String oldShortLoc = _mapShortLocationsByDest.get(destination);
 
 				_mapDestinationsByShortLoc.remove(oldShortLoc);
 				_mapDestinationsByShortLoc.put(shortStringLocation, destination);
 
 				_mapShortLocationsByDest.put(destination, shortStringLocation);
 				_mapLocationsByDest.put(destination, TravelGatesUtils.shortStringToLocation(shortStringLocation, getServer().getWorlds()));
 			}
 			else
 			{
 				if (_isDebugEnabled)
 				{
 					_LOGGER.info(_debug + " End updateDestination : " + false);
 				}
 				return false;
 			}
 		}
 		else
 		{
 			fullStringLocation = _destinationsData.getProperty(destination);
 			fullStringLocation = fullStringLocation.substring(0, fullStringLocation.lastIndexOf(TravelGatesConstants.DELIMITER) + 1);
 		}
 
 		fullStringLocation += container.getOptionsForData();
 
 		_destinationsData.put(destination, fullStringLocation);
 
 		if (!container.has(TravelGatesOptions.RESTRICTION))
 		{
 			_restrictionsData.remove(destination);
 		}
 		else
 		{
 			_restrictionsData.put(destination, container.getRestrictionsListString());
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " New details : " + _destinationsData.getProperty(destination));
 		}
 
 		if (container.has(TravelGatesOptions.SAVE) || _autosave)
 		{
 			final boolean saved = saveData();
 			optionsUpdated = saved;
 			if (saved)
 			{
 				player.sendMessage(ChatColor.GREEN + _messages.get(TravelGatesMessages.SAVE_DONE));
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + _messages.get(TravelGatesMessages.SAVE_FAILED));
 			}
 		}
 		else
 		{
 			optionsUpdated = true;
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End updateDestination : " + optionsUpdated);
 		}
 
 		return optionsUpdated;
 	}
 
 	public boolean isDebugEnabled()
 	{
 		return _isDebugEnabled;
 	}
 
 	public TravelGatesTeleportBlock getTeleportBlock()
 	{
 		return _tpBlock;
 	}
 
 	public void toggleDebugState()
 	{
 		_isDebugEnabled = !_isDebugEnabled;
 		TravelGatesUtils.setDebugState(_isDebugEnabled);
 
 		_configData.put(TravelGatesConfigurations.DEBUG.value(), String.valueOf(_isDebugEnabled));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		_LOGGER.info(_tag + " DEBUG MODE : " + ((_isDebugEnabled) ? "ENABLED" : "DISABLED"));
 	}
 
 	public boolean togglePermissionsState()
 	{
 		_usePermissions = !_usePermissions;
 
 		if (_usePermissions)
 		{
 			loadPermissions();
 		}
 
 		_configData.put(TravelGatesConfigurations.USEPERMISSIONS.value(), String.valueOf(_usePermissions));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " USE PERMISSIONS : " + ((_usePermissions) ? "ENABLED" : "DISABLED"));
 		}
 
 		return _usePermissions;
 	}
 
 	public boolean toggleSignTeleportState()
 	{
 		_teleportWithSign = !_teleportWithSign;
 
 		_configData.put(TravelGatesConfigurations.TELEPORTWITHSIGN.value(), String.valueOf(_teleportWithSign));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " TELEPORT WITH SIGN : " + ((_teleportWithSign) ? "ENABLED" : "DISABLED"));
 		}
 
 		return _teleportWithSign;
 	}
 
 	public boolean togglePortalTeleportState()
 	{
 		_teleportWithPortal = !_teleportWithPortal;
 
 		_configData.put(TravelGatesConfigurations.TELEPORTWITHPORTAL.value(), String.valueOf(_teleportWithPortal));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " TELEPORT WITH PORTAL : " + ((_teleportWithPortal) ? "ENABLED" : "DISABLED"));
 		}
 
 		return _teleportWithPortal;
 	}
 
 	public boolean toggleClearAllInventoryState()
 	{
 		_clearAllInventory = !_clearAllInventory;
 
 		_configData.put(TravelGatesConfigurations.CLEARALLINVENTORY.value(), String.valueOf(_clearAllInventory));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " CLEAR ALL INVENTORY : " + ((_clearAllInventory) ? "ENABLED" : "DISABLED"));
 		}
 
 		return _clearAllInventory;
 	}
 	
 	public boolean toggleProtectAdminInventoryState()
 	{
 		_protectAdminInventory = !_protectAdminInventory;
 
 		_configData.put(TravelGatesConfigurations.PROTECTADMININVENTORY.value(), String.valueOf(_protectAdminInventory));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " PROTECT ADMIN INVENTORY : " + ((_protectAdminInventory) ? "ENABLED" : "DISABLED"));
 		}
 
 		return _protectAdminInventory;
 	}
 
 	public boolean toggleTeleportBlockState()
 	{
 		_tpBlock.toggleState();
 		
 		_configData.put(TravelGatesConfigurations.TPBLOCK.value(), _tpBlock.toString());
 		
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " TELEPORT BLOCK : " + _tpBlock);
 		}
 
 		return _tpBlock.isEnabled();
 	}
 
 	public boolean toggleAutoSaveState()
 	{
 		_autosave = !_autosave;
 
 		_configData.put(TravelGatesConfigurations.AUTOSAVE.value(), String.valueOf(_autosave));
 
 		if (_autosave)
 		{
 			saveConfiguration();
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " AUTO SAVE : " + ((_autosave) ? "ENABLED" : "DISABLED"));
 		}
 
 		return _autosave;
 	}
 
 	public boolean isSignTeleportEnabled()
 	{
 		return _teleportWithSign;
 	}
 
 	public boolean isPortalTeleportEnabled()
 	{
 		return _teleportWithPortal;
 	}
 
 	public String getListOfWorlds()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getListOfWorlds()");
 		}
 
 		final StringBuilder strBld = new StringBuilder();
 
 		strBld.append(ChatColor.YELLOW).append(_messages.get(TravelGatesMessages.AVAILABLE_WORLDS_ARE)).append(" ");
 
 		final int initialSize = strBld.length();
 
 		for (final World world : this.getServer().getWorlds())
 		{
 			if (strBld.length() != initialSize)
 			{
 				strBld.append(ChatColor.YELLOW).append(TravelGatesConstants.DELIMITER).append(" ");
 			}
 
 			strBld.append(ChatColor.AQUA).append(world.getName());
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getListOfWorlds : " + strBld.toString());
 		}
 
 		return strBld.toString();
 	}
 
 	public final String getPortalSignOnState()
 	{
 		return _portalSignOnState;
 	}
 
 	public final String getPortalSignOffState()
 	{
 		return _portalSignOffState;
 	}
 
 	public PluginManager getPM()
 	{
 		return _pm;
 	}
 
 	public boolean configTeleportBlock(final String tpblock, final boolean save)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start configTeleportBlock(tpblock=" + tpblock + ")");
 		}
 
 		boolean result = false;
 
 		String[] split = tpblock.split(TravelGatesConstants.DELIMITER);
 
 		if (split.length >= 1)
 		{
 			final String material = split[0];
 			final Integer typeId = _mapMaterialIdByName.get(material.toLowerCase());
 
 			if (typeId != null)
 			{
 				final Material type = Material.getMaterial(typeId);
 				// Ticket #18: default values for Wool and Log materials
 				if (split.length == 1)
 				{
 					switch (type)
 					{
 						case WOOL:
 							_tpBlock.setColor(DyeColor.WHITE);
 							break;
 							
 						case LOG:
 							_tpBlock.setSpecies(TreeSpecies.GENERIC);
 							break;
 					}
 				}
 				
 				_tpBlock.setType(type);
 				_tpBlock.setEnabled(true);
 				result = true;
 			}
 			else
 			{
 				_LOGGER.severe(_tag + " Material " + material + " is not expected to be used as a TP Block.");
 			}
 		}
 
 		// Ticket #18: stop if result is false
 		if (result && split.length >= 2)
 		{
 			final String data = split[1];
 			
 			// Ticket #18: check if data is given by id
 			Byte rawData = null;
 			if (TravelGatesUtils.stringIsNotBlank(data) && data.matches(TravelGatesConstants.INTEGER_PATTERN))
 			{
 				rawData = Byte.parseByte(data);
 			}
 
 			switch (_tpBlock.type())
 			{
 				case WOOL:
 					DyeColor color = null;
 					if (rawData != null)
 					{
 						// Ticket #18: color found by data id
 						color = DyeColor.getByWoolData(rawData);
 					}
 					else
 					{
 						color = _mapDyeColorByName.get(data.toLowerCase());
 					}
 					
 					if (color != null)
 					{
 						_tpBlock.setColor(color);
 					}
 					else
 					{
 						_tpBlock.setEnabled(false);
 						result = false;
 						_LOGGER.severe(_tag + " TP Block data: " + data + " is invalid for WOOL Material.");
 					}
 					break;
 					
 				case LOG:
 					TreeSpecies species = null;
 					if (rawData != null)
 					{
 						// Ticket #18: species found by data id
 						species = TreeSpecies.getByData(rawData);
 					}
 					else
 					{
 						species = _mapTreeSpeciesByName.get(data.toLowerCase());
 					}
 
 					if (species != null)
 					{
 						_tpBlock.setSpecies(species);
 					}
 					else
 					{
 						_tpBlock.setEnabled(false);
 						result = false;
 						_LOGGER.severe(_tag + " TP Block data: " + data + " is invalid for LOG Material.");
 					}
 					break;
 					
 				default:
 					_LOGGER.severe(_tag + " " + _tpBlock.type() + " is not configured to have data.");
 					_tpBlock.setEnabled(false);
 					result = false;
 					break;
 			}
 		}
 
 		// Ticket #18: do not save when errors occurred
 		if (result && save)
 		{
 			_configData.put(TravelGatesConfigurations.TPBLOCK.value(), _tpBlock.toString());
 
 			if (_autosave)
 			{
 				saveConfiguration();
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End configTeleportBlock : " + result);
 		}
 
 		return result;
 	}
 
 	public TravelGatesOptionsContainer createDestinationOptions(final String destination, final String options)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start createDestinationOptions(destination=" + destination + ", options=" + options + ")");
 		}
 
 		TravelGatesOptionsContainer optionsContainer = null;
 
 		if (TravelGatesUtils.stringIsNotBlank(options))
 		{
 			String inputOptions = null;
 
 			if (options.startsWith(TravelGatesConstants.OPTION_PREFIX))
 			{
 				inputOptions = options.substring(1);
 			}
 			else
 			{
 				inputOptions = options;
 			}
 
 			optionsContainer = _mapOptionsByDest.get(destination.toLowerCase());
 
 			if (optionsContainer == null)
 			{
 				optionsContainer = new TravelGatesOptionsContainer(this);
 				_mapOptionsByDest.put(destination.toLowerCase(), optionsContainer);
 			}
 
 			// FIXME : Bug pour (2000,?,2000
 			while (inputOptions.length() > 0)
 			{
 				if (_isDebugEnabled)
 				{
 					_LOGGER.info(_debug + " inputOptions = " + inputOptions);
 				}
 
 				final String strOption = inputOptions.substring(0, 1);
 				if (_isDebugEnabled)
 				{
 					_LOGGER.info(_debug + " strOption = " + strOption);
 				}
 
 				final TravelGatesOptions option = TravelGatesOptions.get(strOption);
 
 				if (option != null && option.isDestinationOption())
 				{
 					switch (option)
 					{
 						case POSITION:
 							final int startPosIndex = inputOptions.indexOf(TravelGatesConstants.START_POSITION);
 							final int endPosIndex = inputOptions.indexOf(TravelGatesConstants.END_POSITION);
 
 							if (!optionsContainer.has(option))
 							{
 								optionsContainer.set(option, true);
 
 								if (startPosIndex > 0 && endPosIndex > 0)
 								{
 									final String pos = inputOptions.substring(startPosIndex + 1, endPosIndex);
 
 									if (_isDebugEnabled)
 									{
 										_LOGGER.info(_debug + " pos = " + pos);
 									}
 
 									optionsContainer.setPosition(pos);
 									inputOptions = inputOptions.substring(endPosIndex + 1);
 								}
 								else
 								{
 									optionsContainer.setPosition("");
 									inputOptions = inputOptions.substring(1);
 								}
 							}
 							break;
 
 						case RESTRICTION:
 							final int startResIndex = inputOptions.indexOf(TravelGatesConstants.START_RESTRICTIONS);
 							final int endResIndex = inputOptions.indexOf(TravelGatesConstants.END_RESTRICTIONS);
 
 							if (startResIndex > 0 && endResIndex > 0)
 							{
 								final String res = inputOptions.substring(startResIndex + 1, endResIndex);
 
 								if (_isDebugEnabled)
 								{
 									_LOGGER.info(_debug + " res = " + res);
 								}
 
 								optionsContainer.setRestrictionsList(res);
 
 								inputOptions = inputOptions.substring(endResIndex + 1);
 							}
 							else
 							{
 								optionsContainer.clearRestrictionsList();
 								inputOptions = inputOptions.substring(1);
 							}
 							break;
 
 						default:
 							optionsContainer.set(option, !optionsContainer.has(option)); // toggle existing options
 							inputOptions = inputOptions.substring(1);
 					}
 				}
 			}
 		}
 		else
 		{
 			optionsContainer = new TravelGatesOptionsContainer(this);
 		}
 
 		_mapOptionsByDest.put(destination.toLowerCase(), optionsContainer);
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End createDestinationOptions : " + optionsContainer);
 		}
 
 		return optionsContainer;
 	}
 
 	public String getWorldState(final World world, final String name)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getWorldState(world=" + world + ", name=" + name + ")");
 		}
 
 		String state = null;
 
 		// world unloaded or not found
 		if (world != null)
 		{
 			state = ChatColor.YELLOW
 					+ _messages.get(TravelGatesMessages.WORLD_STATE, ChatColor.AQUA + world.getName() + ChatColor.YELLOW,
 							ChatColor.GREEN + _messages.get(TravelGatesMessages.WORLD_LOADED) + ChatColor.YELLOW);
 		}
 		// world loaded
 		else
 		{
 			state = ChatColor.YELLOW
 					+ _messages.get(TravelGatesMessages.WORLD_STATE, ChatColor.AQUA + name + ChatColor.YELLOW,
 							ChatColor.RED + _messages.get(TravelGatesMessages.WORLD_UNLOADED) + ChatColor.YELLOW);
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getWorldState : " + state);
 		}
 
 		return state;
 	}
 
 	public String getWorldState(final String worldName)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getWorldState(worldName=" + worldName + ")");
 		}
 
 		final World world = getServer().getWorld(worldName);
 
 		return getWorldState(world, worldName);
 	}
 
 	public World loadWorld(final String worldName, final String worldType)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start loadWorld(worldName=" + worldName + ", worldType=" + worldType + ")");
 		}
 
 		World newWorld = getServer().getWorld(worldName);
 
 		if (newWorld == null)
 		{
 			final TravelGatesWorldType type = TravelGatesWorldType.getWorldType(worldType);
 
 			WorldCreator worldFactory = new WorldCreator(worldName);
 			worldFactory = worldFactory.environment(type.getEnv());
 			newWorld = getServer().createWorld(worldFactory);
 
 			if (!_setAdditionalWorlds.contains(worldName))
 			{
 				_setAdditionalWorlds.add(worldName);
 				_configData.put(TravelGatesConfigurations.WORLDS.value(), getListOfAdditionnalWorld());
 
 				if (_autosave)
 				{
 					saveConfig();
 				}
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End loadWorld : " + newWorld);
 		}
 
 		return newWorld;
 	}
 
 	public World unloadWorld(final String worldName)
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start unloadWorld(worldName=" + worldName + ")");
 		}
 
 		World unloadedWorld = null;
 
 		final boolean unloaded = getServer().unloadWorld(worldName, true);
 
 		if (!unloaded)
 		{
 			unloadedWorld = getServer().getWorld(worldName);
 		}
 		else
 		{
 			if (_setAdditionalWorlds.contains(worldName))
 			{
 				_setAdditionalWorlds.remove(worldName);
 				_configData.put(TravelGatesConfigurations.WORLDS.value(), getListOfAdditionnalWorld());
 
 				if (_autosave)
 				{
 					saveConfig();
 				}
 			}
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start unloadWorld : " + unloadedWorld);
 		}
 
 		return unloadedWorld;
 	}
 
 	public String getAllWorldsFromServerDirectory()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start getAllWorldsFromServerDirectory()");
 		}
 
 		final StringBuilder strBld = new StringBuilder();
 
 		strBld.append(ChatColor.YELLOW).append(_messages.get(TravelGatesMessages.WORLDS_IN_SERVER_DIR)).append(" ");
 
 		final int initialSize = strBld.length();
 
 		try
 		{
 			final File serverDir = new File(".");
 			final FilenameFilter filter = new FilenameFilter() {
 				public boolean accept(File file, String name)
 				{
 					return !name.startsWith(".") && !name.equals(TravelGatesConstants.PLUGINS_DIRECTORY);
 				}
 			};
 
 			for (final String el : serverDir.list(filter))
 			{
 				final File file = new File(el);
 
 				if (file.exists() && file.isDirectory())
 				{
 					if (strBld.length() != initialSize)
 					{
 						strBld.append(ChatColor.YELLOW).append(TravelGatesConstants.DELIMITER).append(" ");
 					}
 
 					final World world = getServer().getWorld(el);
 
 					if (world == null)
 					{
 						strBld.append(ChatColor.RED);
 					}
 					else
 					{
 						strBld.append(ChatColor.GREEN);
 					}
 
 					strBld.append(el);
 				}
 			}
 		}
 		catch (final Throwable th)
 		{
 			_LOGGER.severe(_tag + " Error while listing worlds in the server directory.");
 			th.printStackTrace();
 			strBld.append(ChatColor.RED).append(_messages.get(TravelGatesMessages.ERROR));
 		}
 
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " End getAllWorldsFromServerDirectory : " + strBld.toString());
 		}
 
 		return strBld.toString();
 	}
 
 	private boolean loadAdditionalWorld()
 	{
 		if (_isDebugEnabled)
 		{
 			_LOGGER.info(_debug + " Start loadAdditionalWorld()");
 		}
 
 		if (!_setAdditionalWorlds.isEmpty())
 		{			
 			final ArrayList<String> worldListToRemove = new ArrayList<String>();
 			for (final String worldName : _setAdditionalWorlds)
 			{
 				final File worldDir = new File(worldName);
 
 				if (worldDir.exists() && worldDir.isDirectory())
 				{
 					World world = getServer().getWorld(worldName);
 
 					if (world == null)
 					{
 						WorldCreator worldFactory = new WorldCreator(worldName);
 						world = getServer().createWorld(worldFactory);
 
 						if (world != null)
 						{
 							if (_isDebugEnabled)
 							{
 								_LOGGER.info(_debug + " World " + worldName + " has been loaded.");
 							}
 						}
 						else
 						{
 							if (_isDebugEnabled)
 							{
 								_LOGGER.info(_debug + " World " + worldName + " has not be loaded.");
 							}
 						}
 					}
 					else
 					{
 						worldListToRemove.add(worldName);
 						if (_isDebugEnabled)
 						{
 							_LOGGER.info(_debug + " World " + worldName + " is already loaded.");
 						}
 					}
 				}
 				else
 				{
 					worldListToRemove.add(worldName);
 					if (_isDebugEnabled)
 					{
 						_LOGGER.info(_debug + " World " + worldName + " does not exist in the server directory or is invalid.");
 					}
 				}
 			}
 			
 			// Fix: remove worlds after working on it
 			if (!worldListToRemove.isEmpty())
 			{ 
 				_setAdditionalWorlds.removeAll(worldListToRemove);
 			}
 
 			_configData.put(TravelGatesConfigurations.WORLDS.value(), getListOfAdditionnalWorld());
 		}
 		else
 		{
 			if (_isDebugEnabled)
 			{
 				_LOGGER.info(_debug + " No additional world to load.");
 			}
 		}
 
 		_LOGGER.info(_tag + " Additional Worlds configuration set to : " + _setAdditionalWorlds.size() + " additional worlds loaded.");
 		
 		return true;
 	}
 
 	public String getListOfAdditionnalWorld()
 	{
 		final StringBuilder strBld = new StringBuilder();
 
 		for (final String world : _setAdditionalWorlds)
 		{
 			if (strBld.length() > 0)
 			{
 				strBld.append(TravelGatesConstants.DELIMITER);
 			}
 
 			strBld.append(world);
 		}
 
 		return strBld.toString();
 	}
 	
 	public boolean isProtectedInventory(final Player player)
 	{
 		return _protectAdminInventory && hasPermission(player, TravelGatesPermissionsNodes.PROTECTADMININV);
 	}
 }
