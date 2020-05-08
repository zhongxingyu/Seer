 package com.deaboy.manhunt;
 
 import java.io.Closeable;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.deaboy.manhunt.commands.CommandUtil;
 import com.deaboy.manhunt.finder.Finder;
 import com.deaboy.manhunt.finder.FinderManager;
 import com.deaboy.manhunt.game.Game;
 import com.deaboy.manhunt.game.GameType;
 import com.deaboy.manhunt.game.ManhuntGame;
 import com.deaboy.manhunt.loadouts.LoadoutManager;
 import com.deaboy.manhunt.lobby.GameLobby;
 import com.deaboy.manhunt.lobby.HubLobby;
 import com.deaboy.manhunt.lobby.Lobby;
 import com.deaboy.manhunt.lobby.LobbyType;
 import com.deaboy.manhunt.map.World;
 import com.deaboy.manhunt.settings.ManhuntSettings;
 import com.deaboy.manhunt.timeouts.TimeoutManager;
 
 public class Manhunt implements Closeable
 {
 	//---------------- Constants ----------------//
 	/** The extension to use for property files. */
 	public static final String extension_properties = ".properties";
 	/** The extension to use for loadout files. */
 	public static final String extension_loadouts = ".dat";
 	/** The extension to use for the world data files. */
 	public static final String extension_worldprops = ".dat";
 	
 	/** The filename to use for the basic config property file. */
 	public static final String filename_settings = "config";
 	/** The filename to use for manhunt world data files. */
 	public static final String filename_worldprops = "world";
 	
 	/** The directory name for the server's plugins. */
 	public static final String dirname_plugins = "plugins";
 	/** The directory name for this plugin, nested within <CODE>dirname_plugins</CODE>. */
 	public static final String dirname_root = "Manhunt";
 	/** The directory name for this plugin's loadouts, nested within <CODE>dirname_main</CODE>. */
 	public static final String dirname_loadouts = "loadouts";
 	/** The directory name for the world data files stored in each Minecraft world's folder. */
 	public static final String dirname_world = "Manhunt";
 	/** The directory name for the lobby data files. These files will hold each lobby's individual settings. */
 	public static final String dirname_lobbies = "lobbies";
 	
 	/** The directory path for this plugin's root directory, relative to the server's running directory. */
 	public static final String path_rootdir = dirname_plugins + "/" + dirname_root;
 	/** The path for this plugin's main config file, relative to the server's running directory. */
 	public static final String path_settings = path_rootdir + "/" + filename_settings + extension_properties;
 	/** The directory path for the loadouts folder, relative to the server's running directory. */
 	public static final String path_loadouts = path_rootdir + "/" + dirname_loadouts;
 	/** The directory path for the lobbies folder, relative to the server's running directory. */
 	public static final String path_lobbies = path_rootdir + "/" + dirname_lobbies;
 	
 	
 	
 	//---------------- Local variables -----------------//
 	private static	Manhunt				instance;
 	private 		ManhuntSettings		settings;
 	private 		TimeoutManager		timeouts;
 	private 		FinderManager		finders;
 	private 		CommandUtil		command_util;
 	private 		LoadoutManager		loadouts;
 	
 	private 		HashMap<Long, Lobby>	lobbies;
 	private			HashMap<String, Long>	players;
 	private 		HashMap<String, World>	worlds;
 	private			HashMap<Long, GameType>games;
 	
 	
 	
 	//---------------- Constructor ----------------//
 	public Manhunt()
 	{
 		Manhunt.instance =		this;
 		this.settings =			new ManhuntSettings(path_settings);
 		
 		if (Material.getMaterial(settings.FINDER_ITEM.getValue()) == null)
 			settings.FINDER_ITEM.resetToDefault();
 		
 		this.timeouts =			new TimeoutManager();
 		this.finders =			new FinderManager();
 		this.command_util =	new CommandUtil();
 		this.loadouts =			new LoadoutManager();
 		
 		this.lobbies =			new HashMap<Long, Lobby>();
 		this.worlds =			new HashMap<String, World>();
 		this.players =			new HashMap<String, Long>();
 		this.games =			new HashMap<Long, GameType>();
 		
 		
 		//////// Register game types
 		registerGameType(ManhuntGame.class, "Manhunt");
 		// registerGameType(JuggernautGame.class, "Juggerhunt");
 		// registerGameType(GhostGame.class, "Manhaunt");
 	}
 	
 	
 	//-------------------- Getters --------------------//
 	public static Lobby getDefaultLobby()
 	{
 		return getLobby(Bukkit.getWorld(getSettings().DEFAULT_WORLD.getValue()));
 	}
 	
 	public static ManhuntSettings getSettings()
 	{
 		return getInstance().settings;
 	}
 	
 	public static Lobby getLobby(long id)
 	{
 		if (getInstance().lobbies.containsKey(id))
 			return getInstance().lobbies.get(id);
 		else
 			return null;
 		
 	}
 	
 	public static Lobby getLobby(String name)
 	{
 		for (Lobby lobby : getInstance().lobbies.values())
 		{
 			if (lobby.getName().equalsIgnoreCase(name))
 			{
 				return lobby;
 			}
 		}
 		return null;
 		
 	}
 	
 	public static Lobby getLobby(org.bukkit.World world)
 	{
 		for (Lobby lobby : getInstance().lobbies.values())
 		{
 			if (lobby.getWorlds().contains(world))
 			{
 				return lobby;
 			}
 		}
 		return null;
 		
 	}
 	
 	public static List<Lobby> getLobbies()
 	{
 		return new ArrayList<Lobby>(getInstance().lobbies.values());
 	}
 	
 	public static Lobby getPlayerLobby(Player player)
 	{
 		if (player == null)
 			return null;
 		else
 			return getPlayerLobby(player.getName());
 	}
 	
 	public static Lobby getPlayerLobby(String name)
 	{
 		if (getInstance().players.containsKey(name))
 			return getLobby(getInstance().players.get(name));
 		else
 			return null;
 	}
 	
 	public static List<World> getWorlds()
 	{
 		return new ArrayList<World>(getInstance().worlds.values());
 	}
 	
 	public static World getWorld(org.bukkit.World world)
 	{
 		for (World w : getWorlds())
 		{
 			if (w.getWorld() == world)
 				return w;
 		}
 		return null;
 	}
 	
 	public static World getWorld(String name)
 	{
 		if (getInstance().worlds.containsKey(name))
 			return getInstance().worlds.get(name);
 		else
 			return null;
 	}
 	
 	public static TimeoutManager getTimeoutManager()
 	{
 		return getInstance().timeouts;
 	}
 	
 	public static FinderManager getFinders()
 	{
 		return getInstance().finders;
 	}
 	
 	public static CommandUtil getCommandUtil()
 	{
 		return getInstance().command_util;
 	}
 	
 	public static LoadoutManager getLoadouts()
 	{
 		return getInstance().loadouts;
 	}
 	
 	
 	
 	//---------------- Private Methods ----------------//
 	private static Manhunt getInstance()
 	{
 		return instance;
 	}
 	
 	private void removePlayer(String name)
 	{
 		timeouts.stopTimeout(name);
 		stopFinder(name, true);
 		getPlayerLobby(name).removePlayer(name);
 		players.remove(name);
 	}
 	
 	private long getNextLobbyId()
 	{
 		long i;
 		for (i = 0; lobbies.containsKey(i); i++);
 		return i;
 	}
 	
 	/**
 	 * Resets a player's food, hunger, inventory, game mode, etc.
 	 * @param p
 	 */
 	private static void resetPlayer(Player p)
 	{
 		p.getInventory().clear();
 		p.setHealth(p.getMaxHealth());
 		p.setFoodLevel(20);
 		p.setSaturation(20f);
 		p.setExhaustion(20f);
 		p.getEnderChest().clear();
 		p.setBedSpawnLocation(null);
 		p.setCompassTarget(p.getLocation());
 		p.setExp(0f);
 		p.setLevel(0);
 		p.setFlying(false);
 		p.setRemainingAir(10);
 		p.setTotalExperience(0);
 	}
 	
 	private void registerGameType(Class<? extends Game> gameType, String name)
 	{
 		if (gameType.isInterface() || Modifier.isAbstract(gameType.getModifiers()))
 			throw new IllegalArgumentException("Game class cannot be abstract or an interface.");
 		
 		for (GameType gc : getInstance().games.values())
 			if (gc.getGameClass().equals(gameType))
 				return;
 		
 		long i = 0;
 		while (getInstance().games.containsKey(i))
 			i++;
 		
 		getInstance().games.put(i, new GameType(gameType, i, name, ManhuntPlugin.getInstance()));
 		
 	}
 	
 	
 	
 	//---------------- Public Interface Methods ----------------//
 	public static Lobby createLobby(String name, LobbyType type, org.bukkit.World world)
 	{
 		Lobby lobby = null;
 		World manhunt_world = getWorld(world);
 		long id = getInstance().getNextLobbyId();
 		
 		lobby = getLobby(name);
 		if (lobby != null)
 			return null;
 		
 		lobby = getLobby(world);
 		if (lobby != null)
 			return null;
 		
 		switch (type)
 		{
 		case HUB:
 			lobby = new HubLobby(id, name, manhunt_world);
 			break;
 		case GAME:
 			lobby = new GameLobby(id, name, manhunt_world);
 			break;
 		default:
 			return null;
 		}
 		
 		return lobby;
 	}
 	
 	public static void changePlayerLobby(Player p, long lobby_id)
 	{
 		
 		Lobby old_lobby = getPlayerLobby(p);
 		Lobby lobby = Manhunt.getLobby(lobby_id);
 		
 		if (lobby == null) 
 			throw new IllegalArgumentException("Invalid lobby id.");
 		
 		p.teleport(lobby.getSpawn().getRandomLocation());
 		resetPlayer(p);
 		
 		getInstance().players.put(p.getName(), lobby.getId());
 		
 		if (lobby.addPlayer(p.getName()))
 		{
 			if (old_lobby != null)
 				old_lobby.broadcast(p.getName() + " has left.");
 			lobby.broadcast(p.getName() + " has joined.");
 		}
 		
 	}
 	
 	public static void playerJoin(Player p)
 	{
 		if (timeoutExists(p))
 			stopTimeout(p);
 		
 		if (!getInstance().players.containsKey(p.getName()))
 		{
 			setPlayerLobby(p, Manhunt.getDefaultLobby().getId());
 		}
 		
 		
 		
 	}
 	
 	public static void playerLeave(Player p)
 	{
 		/*
 		 * Function for protocol to remove player from Manhunt
 		 * 
 		 * When a player leaves,...
 		 * 		Starts a timeout,
 		 * 		Or just plain kicks him
 		 * 
 		 * Completely removing a player involves...
 			 * 	Stopping their timeout
 			 * 	Stopping their finder
 			 * 	Removing them from lobbies
 			 * 	Forfeiting them from games
 		 */
 		
 		if (getPlayerLobby(p) != null && getPlayerLobby(p).gameIsRunning() && getSettings().OFFLINE_TIMEOUT.getValue() > 0)
 		{
 			startTimeout(p, getPlayerLobby(p), getSettings().OFFLINE_TIMEOUT.getValue() * 1000);
 		}
 		else
 		{
 			if (getPlayerLobby(p).gameIsRunning())
 				getPlayerLobby(p).forfeitPlayer(p.getName());
 			else
 				getPlayerLobby(p).removePlayer(p);
 			stopFinder(p.getName(), false);
 			getInstance().removePlayer(p.getName());
 		}
 	}
 	
 	public static void setPlayerLobby(Player p, long lobby_id)
 	{
 		Lobby lobby_old;
 		Lobby lobby_new;
 		
 		lobby_old = getPlayerLobby(p);
 		lobby_new = getLobby(lobby_id);
 		
 		if (lobby_new == null)
 			return;
 		
 		if (lobby_old != null)
 		{
 			if (lobby_old.gameIsRunning())
 				lobby_old.forfeitPlayer(p.getName());
 			lobby_old.removePlayer(p);
 		}
 		
 		lobby_new.addPlayer(p.getName());
 		resetPlayer(p);
 		p.teleport(lobby_new.getSpawn().getRandomLocation());
 		
 		getInstance().players.put(p.getName(), lobby_new.getId());
 		
 	}
 	
 	
 	
 	//////////////// TIMEOUTS ////////
 	public static void startTimeout(Player player, Lobby lobby)
 	{
 		if (getSettings().OFFLINE_TIMEOUT.getValue() >= 0)
 			startTimeout(player, lobby, getSettings().OFFLINE_TIMEOUT.getValue() * 1000);
 	}
 	
 	public static void startTimeout(Player player, Lobby lobby, long time)
 	{
 		if (getInstance().timeouts.hasTimeout(player))
 			getInstance().timeouts.stopTimeout(player);
 		
 		getInstance().timeouts.startTimeout(player, lobby.getId(), time);
 	}
 	
 	public static boolean timeoutExists(Player p)
 	{
 		return getInstance().timeouts.hasTimeout(p);
 	}
 	
 	public static void stopTimeout(Player player)
 	{
 		getInstance().timeouts.stopTimeout(player);
 	}
 	
 	
 	
 	//////////////// LOGGING ////////
 	public static void log(String message)
 	{
 		log(Level.INFO, message);
 	}
 	
 	public static void log(Level level, String message)
 	{
 		Bukkit.getLogger().log(level, "[Manhunt]  " + message);
 	}
 	
 	public static void log(Exception e)
 	{
 		if (getSettings().DISPLAY_ERRORS.getValue())
 		{
 			log(Level.SEVERE, e.toString());
 			log(Level.SEVERE, e.getMessage());
 		}
 		else
 		{
 			log(Level.SEVERE, e.toString());
 		}
 		
 		
 		// TODO Get some sort of separate logger set up.
 	}
 	
 	
 	
 	//////////////// REGISTERING GAME TYPES ////////
 	public static boolean registerGameType(Class<? extends Game> gameType, String name, JavaPlugin plugin)
 	{
 		if (gameType.isInterface() || Modifier.isAbstract(gameType.getModifiers()))
 			throw new IllegalArgumentException("Game class cannot be abstract or an interface.");
 		
 		for (GameType gc : getInstance().games.values())
 			if (gc.getGameClass().equals(gameType))
 				return false;
 		
 		if (plugin == ManhuntPlugin.getInstance())
 			throw new IllegalArgumentException("\"plugin\" arugment cannot be the Manhunt plugin itself.");
 		
 		long i = 0;
 		while (getInstance().games.containsKey(i))
 			i++;
 		
 		getInstance().games.put(i, new GameType(gameType, i, name, plugin));
 		
 		return true;
 		
 	}
 	
 	public static List<GameType> getRegisteredGameTypes()
 	{
 		return new ArrayList<GameType>(getInstance().games.values());
 	}
 	
 	public static GameType getGameType(long id)
 	{
 		if (getInstance().games.containsKey(id))
 			return getInstance().games.get(id);
 		else
 			return null;
 	}
 	
 	/**
 	 * Used for lobby loading. Allows lobbies to get the GameType based
 	 * off the Game class's canonical name.
 	 * @param class_canonical_name
 	 * @return
 	 */
 	public static GameType getGameTypeByClassCanonicalName(String class_canonical_name)
 	{
 		for (GameType gt : getRegisteredGameTypes())
 		{
 			if (gt.getGameClass().getCanonicalName().equals(class_canonical_name))
 				return gt;
 		}
 		return null;
 	}
 	
 	
 	
 	//////////////// FINDERS ////////
 	public static void startFinder(Player p, long lobby_id)
 	{
 		getInstance().finders.startFinder(p, lobby_id);
 	}
 	
 	public static void stopAllFinders(long lobby_id)
 	{
 		getInstance().finders.stopLobbyFinders(lobby_id);
 	}
 	
 	public static void stopFinder(Player p, boolean ignoreUsed)
 	{
 		stopFinder(p.getName(), ignoreUsed);
 	}
 	
 	public static void stopFinder(String name, boolean ignoreUsed)
 	{
 		getInstance().finders.stopFinder(name, ignoreUsed);
 	}
 	
 	public static Finder getFinder(Player p)
 	{
 		return getFinders().getFinder(p);
 	}
 	
 	public static Finder getFinder(String name)
 	{
 		return getFinders().getFinder(name);
 	}
 	
 	public static boolean finderExists(Player p)
 	{
 		return getFinders().finderExists(p);
 	}
 	
 	public static boolean finderExists(String name)
 	{
 		return getFinders().finderExists(name);
 	}
 	
 	
 	
 	public void close()
 	{
 		for (GameType gc : games.values())
 			gc.close();
 	}
 	
 }
