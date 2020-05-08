 package no.runsafe.worldguardbridge;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.Server;
 import org.bukkit.plugin.Plugin;
 
 import java.util.*;
 
 public class WorldGuardInterface
 {
 	private Server server;
 	private WorldGuardPlugin worldGuard;
 
 	public WorldGuardInterface(Server server)
 	{
 		this.server = server;
 	}
 
 	private WorldGuardPlugin getWorldGuard(Server server)
 	{
 		Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");
 
 		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
 			return null;
 
 		return (WorldGuardPlugin) plugin;
 	}
 
 	public boolean serverHasWorldGuard()
 	{
 		if (this.worldGuard == null)
 			this.worldGuard = this.getWorldGuard(this.server);
 
 		if (this.worldGuard != null)
 			return true;
 
 		return false;
 	}
 
 	public boolean isInPvPZone(RunsafePlayer player)
 	{
        if (!serverHasWorldGuard())
            return false;
        
 		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
 		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
 
         if (set.size() == 0)
             return false;
 
 		return set.allows(DefaultFlag.PVP);
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
 		for(String region : regions.keySet())
 			result.put(region, regions.get(region).getOwners().getPlayers());
 		return result;
 	}
 
 	public RunsafeLocation getRegionLocation(RunsafeWorld world, String name)
 	{
 		if(!serverHasWorldGuard())
 			return null;
 
 		ProtectedRegion region = worldGuard.getRegionManager(world.getRaw()).getRegion(name);
 		if(region == null)
 			return null;
 		BlockVector point = region.getMaximumPoint();
 		return new RunsafeLocation(world, point.getX(), point.getY(), point.getZ());
 	}
 
 	public Set<String> getOwners(RunsafeWorld world, String name)
 	{
 		if(!serverHasWorldGuard())
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
 		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
 		ArrayList<String> regions = new ArrayList<String>();
 		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
 		for(String region : regionSet.keySet())
 			if(regionSet.get(region).getOwners().contains(player.getName()))
 				regions.add(region);
 		return regions;
 	}
 }
