 package multiworld.addons;
 
 import multiworld.ConfigException;
 import multiworld.NotEnabledException;
 import multiworld.data.DataHandler;
 import multiworld.data.InternalWorld;
 import multiworld.data.MyLogger;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPortalEvent;
 
 /**
  * the class that handle the use of portals between worlds
  * @author Fernando
  */
 public abstract class PortalHandler implements Listener, MultiworldAddon, SettingsListener
 {
 	@Override
 	public void onSettingsChance()
 	{
 		if (this.enabled)
 		{
 			this.load();
 		}
 	}
 	private final DataHandler data;
 	/**
 	 * The logger used for the logging messages
 	 */
 	private final MyLogger logger;
 	/**
 	 * is this plugin enabled
 	 */
 	private boolean enabled = false;
 	private final boolean handleEndPortals;
 	public static final int END_PORTAL = 1;
 	public static final int UNKNOWN_PORTAL = 0;
 	public static final int NETHER_PORTAL = -1;
 
 	/**
 	 * The default constructor
 	 * @param d 
 	 * @param server
 	 * @param logger
 	 * @param handleEndPortals  
 	 */
 	public PortalHandler(DataHandler d, Server server, MyLogger logger, boolean handleEndPortals)
 	{
 		this.data = d;
 		this.logger = logger;
 		this.handleEndPortals = handleEndPortals;
 	}
 
 	/**
 	 * Load the data
 	 * 
 	 * @throws IllegalStateException When this obj isn't enabled
 	 */
 	public void load() throws IllegalStateException
 	{
 		if (enabled == false)
 		{
 			throw new IllegalStateException();
 		}
 
 		this.logger.info("[PortalHandler] loaded!");
 	}
 
 	/**
 	 * Save the data
 	 * 
 	 * @throws ConfigException When there was a configuration error
 	 * @throws IllegalStateException When it wasn't enabled 
 	 */
 	public void save() throws ConfigException, IllegalStateException
 	{
 		if (enabled == false)
 		{
 			throw new IllegalStateException();
 		}
 		this.data.save();
 		this.logger.info("[PortalHandler] saved!");
 	}
 	/*
 	 * Is this enabled
 	 */
 
 	/**
 	 * Checks if this lugin is enabled
 	 * @return true if its been enabled, false otherwise
 	 */
 	public boolean isEnabled()
 	{
 		return this.enabled;
 	}
 
 	/**
 	 * Disable this plugin
 	 */
 	@Override
 	public void onDisable()
 	{
 		if (enabled == false)
 		{
 			throw new IllegalStateException();
 		}
 		enabled = false;
 	}
 
 	/**
 	 * Enable this plugin
 	 */
 	@Override
 	public void onEnable()
 	{
 		if (enabled == true)
 		{
 			throw new IllegalStateException("Already loaded");
 		}
 		this.enabled = true;
 		this.load();
 	}
 
 	/**
 	 * Adds a link to the database, or remove it if world2 is null
 	 * @param world1 the target world
 	 * @param world2 the destination world
 	 * @throws NotEnabledException If this plugin is not enabled
 	 */
 	public void add(String world1, String world2) throws NotEnabledException
 	{
 		if (this.handleEndPortals)
 		{
 			this.data.setEndPortal(world1, world2);
 		}
 		else
 		{
 			this.data.setPortal(world1, world2);
 		}
 	}
 
 	/**
 	 * Called when a player uses a portal
 	 * @param event The event data
 	 */
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerPortal(PlayerPortalEvent event)
 	{
 		if (event.isCancelled() || !this.enabled)
 		{
 			return;
 		}
 		int mapType = this.getPortalType(event.getFrom());
 		this.logger.fine("[PortalHandler] got portal " + mapType + " for location " + event.getFrom().toVector().toString() + ".");
 		if (this.handleEndPortals && (mapType == 1))
 		{
 			this.logger.fine("[PortalHandler] got PortalType.END.");
 			InternalWorld from = this.data.getInternalWorld(event.getFrom().getWorld().getName(), true);
 			String toWorldString = from.getEndPortalWorld();
 			if (!toWorldString.isEmpty())
 			{
 				World toWorld = this.data.getWorld(toWorldString);
 				if (toWorld != null)
 				{
 					World.Environment toDim = toWorld.getEnvironment(), fromDim = event.getFrom().getWorld().getEnvironment();
 					if (toDim == World.Environment.THE_END)
 					{
						Location loc = new Location(toWorld, 100, 54, 0);
						loc = event.getPortalTravelAgent().findOrCreate(loc);
						event.setTo(loc);
						
 					}
 					else
 					{
 						Location loc = event.getPlayer().getBedSpawnLocation();
 						if (loc == null || (!loc.getWorld().equals(toWorld)))
 						{
 							loc = toWorld.getSpawnLocation();
 						}
						loc = event.getPortalTravelAgent().findOrCreate(loc);
 						event.setTo(loc);
						
 					}
 				}
 			}
 			this.logger.fine("[PortalHandler] [PortalType.END] used for user " + event.getPlayer().getName() + " to get to world " + toWorldString);
 		}
 		else if ((!this.handleEndPortals) && (mapType == -1))
 		{
 			this.logger.fine("[PortalHandler] got PortalType.NETHER.");
 			String toWorldString = this.data.getInternalWorld(event.getFrom().getWorld().getName(), true).getPortalWorld();
 			if (!toWorldString.isEmpty())
 			{
 				World toWorld = this.data.getWorld(toWorldString);
 				if (toWorld != null)
 				{
 					World.Environment toDim = toWorld.getEnvironment(), fromDim = event.getFrom().getWorld().getEnvironment();
 					float div;
 					if (fromDim == toDim)//Env is same at both worlds
 					{
 						div = 1.0f;
 					}
 					else if (fromDim == World.Environment.NETHER) //env is nether at target world
 					{
 						div = 8.0f;
 					}
 					else if (toDim == World.Environment.NETHER) // env is nether at from world
 					{
 						div = 0.125f;
 					}
 					else
 					{
 						div = 1.0f;
 					}
 					Location to = new Location(toWorld, event.getFrom().getX() * div, event.getFrom().getY(), event.getFrom().getZ() * div, event.getFrom().getYaw(),
 								   event.getFrom().getPitch());
					to = event.getPortalTravelAgent().findOrCreate(to);
 					event.setTo(to);
 				}
 			}
 			this.logger.fine("[PortalHandler] [PortalType.NETHER] used for user " + event.getPlayer().getName() + " to get to world " + toWorldString);
 		}
 	}
 
 	private int getPortalType(Location loc)
 	{
 
 		Block mainBlock = loc.getBlock();
 		Material toCheck;
 		for (BlockFace face : new BlockFace[]
 			{
 				BlockFace.SELF, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST
 			})
 		{
 			toCheck = mainBlock.getRelative(face).getType();
 			if (toCheck == Material.ENDER_PORTAL)
 			{
 				return END_PORTAL;
 			}
 			else if (toCheck == Material.PORTAL)
 			{
 				return NETHER_PORTAL;
 			}
 		}
 
 		return PortalHandler.UNKNOWN_PORTAL;
 	}
 }
