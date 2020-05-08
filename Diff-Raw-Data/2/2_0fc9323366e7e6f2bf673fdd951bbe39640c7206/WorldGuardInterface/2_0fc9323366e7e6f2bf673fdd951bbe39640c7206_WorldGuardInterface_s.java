 package no.runsafe.worldguardbridge;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.GlobalRegionManager;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import no.runsafe.framework.event.IPluginEnabled;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.entity.Player;
 
 import java.awt.geom.Rectangle2D;
 import java.util.*;
 
 public class WorldGuardInterface implements IPluginEnabled
 {
 
 	public WorldGuardInterface(IOutput console)
 	{
 		this.console = console;
 	}
 
 	@Override
 	public void OnPluginEnabled()
 	{
 		if (!serverHasWorldGuard())
 			console.write("Could not find WorldGuard on this server!");
 	}
 
 	public boolean serverHasWorldGuard()
 	{
 		if (this.worldGuard == null)
 			this.worldGuard = RunsafeServer.Instance.getPlugin("WorldGuard");
 
 		return this.worldGuard != null;
 	}
 
 	public boolean isInPvPZone(RunsafePlayer player)
 	{
 		if (player == null || !serverHasWorldGuard())
 			return false;
 		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
 		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
 		return set.size() != 0 && set.allows(DefaultFlag.PVP);
 	}
 
 	public String getCurrentRegion(RunsafePlayer player)
 	{
 		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
 		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
 		if (set.size() == 0)
 			return null;
 		StringBuilder sb = new StringBuilder();
 		for (ProtectedRegion r : set)
 		{
 			if (sb.length() > 0)
 				sb.append(";");
 			sb.append(r.getId());
 		}
 		return sb.toString();
 	}
 
 	public List<String> getRegionsAtLocation(RunsafeLocation location)
 	{
 		RegionManager regionManager = worldGuard.getRegionManager(location.getWorld().getRaw());
 		ApplicableRegionSet set = regionManager.getApplicableRegions(location.getRaw());
 
 		if (set.size() == 0)
 			return null;
 
 		ArrayList<String> regions = new ArrayList<String>();
 		for (ProtectedRegion region : set)
 			regions.add(region.getId());
 
 		return regions;
 	}
 
 	public List<String> getApplicableRegions(RunsafePlayer player)
 	{
 		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
 		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
 		if (set.size() == 0)
 			return null;
 		ArrayList<String> regions = new ArrayList<String>();
 		for (ProtectedRegion r : set)
 		{
 			regions.add(r.getId());
 		}
 		return regions;
 	}
 
 	public Map<String, Set<String>> getAllRegionsWithOwnersInWorld(RunsafeWorld world)
 	{
 		HashMap<String, Set<String>> result = new HashMap<String, Set<String>>();
 		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
 		Map<String, ProtectedRegion> regions = regionManager.getRegions();
 		for (String region : regions.keySet())
 			result.put(region, regions.get(region).getOwners().getPlayers());
 		return result;
 	}
 
 	public RunsafeLocation getRegionLocation(RunsafeWorld world, String name)
 	{
 		if (!serverHasWorldGuard())
 			return null;
 
 		ProtectedRegion region = worldGuard.getRegionManager(world.getRaw()).getRegion(name);
 		if (region == null)
 			return null;
 		BlockVector point = region.getMaximumPoint();
 		return new RunsafeLocation(world, point.getX(), point.getY(), point.getZ());
 	}
 
 	public Set<String> getOwners(RunsafeWorld world, String name)
 	{
 		if (!serverHasWorldGuard())
 			return null;
 
 		return worldGuard.getRegionManager(world.getRaw()).getRegion(name).getOwners().getPlayers();
 	}
 
 	public Set<String> getMembers(RunsafeWorld world, String name)
 	{
 		if (!serverHasWorldGuard())
 			return null;
 
 		return worldGuard.getRegionManager(world.getRaw()).getRegion(name).getMembers().getPlayers();
 	}
 
 	public List<String> getOwnedRegions(RunsafePlayer player, RunsafeWorld world)
 	{
 		if (world == null || player == null)
 			return null;
 		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
 		ArrayList<String> regions = new ArrayList<String>();
 		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
 		for (String region : regionSet.keySet())
 			if (regionSet.get(region).getOwners().contains(player.getName()))
 				regions.add(region);
 		return regions;
 	}
 
 	public List<String> getRegionsInWorld(RunsafeWorld world)
 	{
 		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
 		return new ArrayList<String>(regionManager.getRegions().keySet());
 	}
 
 	public Map<String, Rectangle2D> getRegionRectanglesInWorld(RunsafeWorld world)
 	{
 		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
 		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
 		HashMap<String, Rectangle2D> result = new HashMap<String, Rectangle2D>();
 		for (String regionName : regionSet.keySet())
 		{
 			Rectangle2D.Double area = new Rectangle2D.Double();
 			ProtectedRegion region = regionSet.get(regionName);
 			BlockVector min = region.getMinimumPoint();
 			BlockVector max = region.getMaximumPoint();
 			area.setRect(min.getX(), min.getZ(), max.getX() - min.getX(), max.getZ() - min.getZ());
 			result.put(regionName, area);
 		}
 		return result;
 	}
 
 	public boolean deleteRegion(RunsafeWorld world, String name)
 	{
 		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
 		if (regionManager.getRegion(name) == null)
 			return false;
 		regionManager.removeRegion(name);
 		return true;
 	}
 
 	public boolean addMemberToRegion(RunsafeWorld world, String name, RunsafePlayer player)
 	{
 		if (!serverHasWorldGuard())
 			return false;
 
 		DefaultDomain members = worldGuard.getRegionManager(world.getRaw()).getRegion(name).getMembers();
 		if (!members.contains(player.getName()))
 		{
 			members.addPlayer(player.getName());
 			return true;
 		}
 		return false;
 	}
 
 	public boolean removeMemberFromRegion(RunsafeWorld world, String name, RunsafePlayer player)
 	{
 		if (!serverHasWorldGuard())
 			return false;
 
 		DefaultDomain members = worldGuard.getRegionManager(world.getRaw()).getRegion(name).getMembers();
 		if (members.contains(player.getName()))
 		{
 			members.removePlayer(player.getName());
 			return true;
 		}
 		return false;
 	}
 
 	public Rectangle2D getRectangle(RunsafeWorld world, String name)
 	{
 		if (!serverHasWorldGuard())
 			return null;
 		ProtectedRegion region = worldGuard.getRegionManager(world.getRaw()).getRegion(name);
 		if (region == null)
 			return null;
 		Rectangle2D.Double area = new Rectangle2D.Double();
 		BlockVector min = region.getMinimumPoint();
 		BlockVector max = region.getMaximumPoint();
 		area.setRect(min.getX(), min.getZ(), max.getX() - min.getX(), max.getZ() - min.getZ());
 		return area;
 	}
 
 	GlobalRegionManager getGlobalRegionManager()
 	{
 		return worldGuard.getGlobalRegionManager();
 	}
 
 	LocalPlayer wrapPlayer(Player rawPlayer)
 	{
 		return worldGuard.wrapPlayer(rawPlayer);
 	}
 
 	private WorldGuardPlugin worldGuard;
 	private final IOutput console;
 }
