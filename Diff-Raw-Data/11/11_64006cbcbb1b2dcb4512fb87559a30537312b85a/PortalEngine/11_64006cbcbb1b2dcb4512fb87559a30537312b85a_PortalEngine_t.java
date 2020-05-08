 package no.runsafe.warpdrive.portals;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.api.event.player.IPlayerPortal;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.api.vector.IRegion3D;
 import no.runsafe.framework.internal.vector.Point3D;
 import no.runsafe.framework.internal.vector.Region3D;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.warpdrive.SmartWarpDrive;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class PortalEngine implements IPlayerPortal, IConfigurationChanged, IPlayerInteractEvent
 {
 	public PortalEngine(PortalRepository repository, SmartWarpDrive smartWarpDrive, IDebug debugger, IConsole console)
 	{
 		this.repository = repository;
 		this.smartWarpDrive = smartWarpDrive;
 		this.debugger = debugger;
 		this.console = console;
 	}
 
 	public void reloadPortals()
 	{
 		this.portals.clear();
 		int portalCount = 0;
 		for (PortalWarp portal : this.repository.getPortalWarps())
 		{
 			String portalWorldName = portal.getPortalWorld().getName();
 			if (!portals.containsKey(portalWorldName))
 				portals.put(portalWorldName, new ArrayList<PortalWarp>());
 
 			portalCount += 1;
 			portals.get(portalWorldName).add(portal);
 		}
 		this.console.logInformation("%d portals loaded in %d worlds.", portalCount, portals.size());
 	}
 
 	public void teleportPlayer(PortalWarp portal, IPlayer player)
 	{
 		this.debugger.debugFine("Teleporting player in portal: " + player.getName());
 		this.debugger.debugFine("Portal lock state: " + (portal.isLocked() ? "locked" : "unlocked"));
 
 		if (portal.getType() == PortalType.NORMAL)
 			player.teleport(portal.getLocation());
 
 		if (portal.getType() == PortalType.RANDOM_SURFACE)
 			this.smartWarpDrive.Engage(player, portal.getWorld(), false, portal.isLocked());
 
 		if (portal.getType() == PortalType.RANDOM_CAVE)
 			this.smartWarpDrive.Engage(player, portal.getWorld(), true, portal.isLocked());
 
 		if (portal.getType() == PortalType.RANDOM_RADIUS)
 			this.randomRadiusTeleport(player, portal.getLocation(), portal.getRadius());
 
 		portal.setLocked(false);
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		IBlock block = event.getBlock();
 		if (block != null && block.is(Item.Redstone.Button.Stone))
 		{
 			IPlayer player = event.getPlayer();
 			IWorld world = player.getWorld();
 
 			if (world != null && portals.containsKey(world.getName()))
 			{
 				List<PortalWarp> portalList = portals.get(world.getName());
 				for (PortalWarp warp : portalList)
 					if (warp.getPortalLocation().distance(block.getLocation()) < 5)
 						warp.setLocked(true);
 			}
 		}
 	}
 
 	private void randomRadiusTeleport(IPlayer player, ILocation theLocation, int radius)
 	{
 		ILocation location = theLocation.clone();
 		int highX = location.getBlockX() + radius;
 		int highZ = location.getBlockZ() + radius;
 		int lowX = location.getBlockX() - radius;
 		int lowZ = location.getBlockZ() - radius;
 
 		location.setX(this.getRandom(lowX, highX));
 		location.setZ(this.getRandom(lowZ, highZ));
 
 		while (!this.safeToTeleport(location))
 		{
 			location.setX(this.getRandom(lowX, highX));
 			location.setZ(this.getRandom(lowZ, highZ));
 		}
 		player.teleport(location);
 	}
 
 	private int getRandom(int low, int high)
 	{
 		return low + (int) (Math.random() * ((high - low) + 1));
 	}
 
 	private boolean safeToTeleport(ILocation location)
 	{
 		if (location.getBlock().is(Item.Unavailable.Air))
 		{
 			location.incrementY(1);
 			if (location.getBlock().is(Item.Unavailable.Air))
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean OnPlayerPortal(IPlayer player, ILocation from, ILocation to)
 	{
 		this.debugger.debugFiner("Portal event detected: " + player.getName());
 		IWorld playerWorld = player.getWorld();
 		if (playerWorld == null)
 			return false;
 
 		String worldName = playerWorld.getName();
 		if (portals.containsKey(worldName))
 		{
 			for (PortalWarp portal : this.portals.get(worldName))
 			{
 				if (portal.isInPortal(player))
 				{
 					debugger.debugFine("Player %s using portal %s in world %s.", player.getName(), portal.getID(), portal.getWorldName());
 					if (portal.canTeleport(player))
 						this.teleportPlayer(portal, player);
 					else
 						player.sendColouredMessage("&cYou do not have permission to use this portal.");
 					return false;
 				}
 			}
 		}
 		if (pending.containsKey(player.getName()))
 		{
 			finalizeWarp(player);
 			return OnPlayerPortal(player, from, to);
 		}
 		return true;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration iConfiguration)
 	{
 		this.reloadPortals();
 	}
 
 	public PortalWarp getWarp(IWorld world, String portalName)
 	{
 		String worldName = world.getName();
 		if (portals.containsKey(worldName))
 		{
 			List<PortalWarp> worldPortals = portals.get(worldName);
 			for (PortalWarp warp : worldPortals)
 				if (warp.getID().equalsIgnoreCase(portalName))
 					return warp;
 		}
 		return null;
 	}
 
 	public void createWarp(IPlayer creator, String portalName, ILocation destination, PortalType type) throws NullPointerException
 	{
 		PortalWarp warp = new PortalWarp(portalName, null, destination, type, -1, null, null); // Create new warp.
 		pending.put(creator.getName(), warp);
 	}
 
 	public void finalizeWarp(IPlayer player)
 	{
 		IRegion3D portalArea = scanArea(player.getLocation());
 		PortalWarp warp = pending.get(player.getName());
 		warp.setRegion(portalArea);
 		warp.setLocation(player.getLocation());
 		pending.remove(player.getName());
 		String worldName = player.getWorldName();
 		if (!portals.containsKey(worldName)) // Check if we're missing a container for this world.
 			portals.put(worldName, new ArrayList<PortalWarp>()); // Create a new warp container.
 
 		repository.storeWarp(warp); // Store the warp in the database.
 		portals.get(worldName).add(warp); // Add to the in-memory warp storage.
 	}
 
 	private IRegion3D scanArea(ILocation location)
 	{
 		Map<Integer, Map<Integer, Map<Integer, Boolean>>> portalMap = new HashMap<Integer, Map<Integer, Map<Integer, Boolean>>>();
 		getNeighbouringPortalBlocks(location, portalMap);
 		int xMin = Collections.min(portalMap.keySet());
 		int xMax = Collections.max(portalMap.keySet());
 		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;
 		int zMin = Integer.MAX_VALUE;
		int zMax = Integer.MIN_VALUE;
 		for (Integer x : portalMap.keySet())
 		{
 			yMin = Math.min(yMin, Collections.min(portalMap.get(x).keySet()));
			yMax = Math.max(yMax, Collections.max(portalMap.get(x).keySet()));
 			for (Integer y : portalMap.get(x).keySet())
 			{
 				zMin = Math.min(zMin, Collections.min(portalMap.get(x).get(y).keySet()));
				zMax = Math.max(zMax, Collections.max(portalMap.get(x).get(y).keySet()));
 			}
 		}
 		return new Region3D(new Point3D(xMin, yMin, zMin), new Point3D(xMax + 1, yMax + 1, zMax + 1));
 	}
 
 	private void getNeighbouringPortalBlocks(ILocation location, Map<Integer, Map<Integer, Map<Integer, Boolean>>> portalMap)
 	{
 		int x = location.getBlockX();
 		int y = location.getBlockY();
 		int z = location.getBlockZ();
 		if (!portalMap.containsKey(x))
 			portalMap.put(x, new HashMap<Integer, Map<Integer, Boolean>>());
 		if (!portalMap.get(x).containsKey(y))
 			portalMap.get(x).put(y, new HashMap<Integer, Boolean>());
 		if (!portalMap.get(x).get(y).containsKey(z))
 			portalMap.get(x).get(y).put(z, true);
 		else
 			return;
 
 		ILocation neighbour = location.clone();
 		neighbour.offset(-1, 0, 0);
 		if (neighbour.getBlock().is(Item.Unavailable.Portal))
 			getNeighbouringPortalBlocks(neighbour, portalMap);
 		neighbour.offset(2, 0, 0);
 		if (neighbour.getBlock().is(Item.Unavailable.Portal))
 			getNeighbouringPortalBlocks(neighbour, portalMap);
 		neighbour.offset(-1, -1, 0);
 		if (neighbour.getBlock().is(Item.Unavailable.Portal))
 			getNeighbouringPortalBlocks(neighbour, portalMap);
 		neighbour.offset(0, 2, 0);
 		if (neighbour.getBlock().is(Item.Unavailable.Portal))
 			getNeighbouringPortalBlocks(neighbour, portalMap);
 		neighbour.offset(0, -1, -1);
 		if (neighbour.getBlock().is(Item.Unavailable.Portal))
 			getNeighbouringPortalBlocks(neighbour, portalMap);
 		neighbour.offset(0, 0, 2);
 		if (neighbour.getBlock().is(Item.Unavailable.Portal))
 			getNeighbouringPortalBlocks(neighbour, portalMap);
 	}
 
 	public void updateWarp(PortalWarp warp)
 	{
 		String worldName = warp.getWorldName();
 		if (!portals.containsKey(worldName))
 			portals.put(worldName, new ArrayList<PortalWarp>());
 
 		repository.updatePortalWarp(warp); // Store changes in the database.
 
 		int index = 0;
 		for (PortalWarp portalWarp : portals.get(worldName))
 		{
 			if (portalWarp.getID().equalsIgnoreCase(warp.getID()))
 			{
 				portals.get(worldName).remove(index); // Remove the old warp.
 				portals.get(worldName).add(warp); // Insert the updated warp.
 				return;
 			}
 			index++;
 		}
 	}
 
 	private final Map<String, PortalWarp> pending = new ConcurrentHashMap<String, PortalWarp>();
 	private final Map<String, List<PortalWarp>> portals = new HashMap<String, List<PortalWarp>>();
 	private final PortalRepository repository;
 	private final SmartWarpDrive smartWarpDrive;
 	private final IDebug debugger;
 	private final IConsole console;
 }
