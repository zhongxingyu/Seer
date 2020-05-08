 package no.runsafe.warpdrive;
 
 import com.google.common.collect.Lists;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Engine
 {
 	public boolean safePlayerTeleport(RunsafeLocation originalLocation, RunsafePlayer player)
 	{
 		RunsafeLocation target = findSafeSpot(originalLocation);
 		if (target != null)
 		{
 			player.setFallDistance(0.0F);
 			player.teleport(target);
 			return true;
 		}
 		return false;
 	}
 
 	public RunsafeLocation findSafeSpot(RunsafeLocation originalLocation)
 	{
 		int maxScan = 100;
 		double x = originalLocation.getX();
 		double z = originalLocation.getZ();
 		int posX = (int) x;
 		int posZ = (int) z;
 		RunsafeLocation location = new RunsafeLocation(
 			originalLocation.getWorld(),
 			originalLocation.getX(),
 			originalLocation.getY() - 1,
 			originalLocation.getZ(),
 			originalLocation.getYaw(),
 			originalLocation.getPitch()
 		);
 
 		while (true)
 		{
 			for (RunsafeLocation option : findSafePoints(originalLocation.getWorld(), posX, posZ))
 			{
 				if (targetFloorIsSafe(option, false))
 					return option;
 			}
 			maxScan--;
 			if (maxScan < 0)
 				return null;
 
 			// Spiral out to find a safe location
 			double t = 100 - maxScan;
 			posX = (int) (5 * t * Math.cos(t) + x);
 			posZ = (int) (5 * t * Math.sin(t) + z);
 		}
 	}
 
 	private List<RunsafeLocation> findSafePoints(RunsafeWorld world, int x, int z)
 	{
 		ArrayList<RunsafeLocation> options = new ArrayList<RunsafeLocation>();
 		int safe = 0;
 		boolean safeFloor = false;
 		RunsafeLocation floor = null;
 		int maxy = world.getMaxHeight();
 		if (world.getRaw().getEnvironment() == World.Environment.NETHER)
 			maxy = 125;
 		for (int y = 0; y < maxy; ++y)
 		{
			RunsafeBlock block = world.getBlockAt(x, 0, z);
 			if (!block.canPassThrough())
 			{
 				safeFloor = !block.isHazardous();
 				floor = block.getLocation();
 			}
 			if (block.isHazardous())
 			{
 				safe = 0;
 				continue;
 			}
 			if (!safeFloor)
 				continue;
 
 			if (block.canPassThrough())
 				safe++;
 			if (safe == 2)
 				options.add(floor);
 		}
 		// Return top first
 		return Lists.reverse(options);
 	}
 
 	public RunsafeLocation findTop(RunsafeLocation location)
 	{
 		RunsafeWorld world = location.getWorld();
 		if (location.getWorld().getRaw().getEnvironment() == World.Environment.NETHER)
 		{
 			int maxHeight = 125;
 			int minHeight = 4;
 			int y = maxHeight - 1;
 			int air = 0;
 			while (y > minHeight)
 			{
 				if (world.getBlockAt(location).canPassThrough())
 					air++;
 				else if (air > 1)
 					break;
 				else
 					air = 0;
 				y--;
 			}
 			location.setY(y);
 			return location;
 		}
 		else
 			return new RunsafeLocation(world.getRaw().getHighestBlockAt(location.getRaw()).getLocation());
 	}
 
 	public boolean targetFloorIsSafe(RunsafeLocation location, boolean playerLocation)
 	{
 		Chunk chunk = location.getWorld().getRaw().getChunkAt(location.getRaw());
 		if (!chunk.isLoaded())
 			chunk.load();
 		RunsafeBlock floor;
 		if (playerLocation)
 			floor = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
 		else
 			floor = location.getWorld().getBlockAt(location);
 		if (
 			floor.isHazardous()
 				|| (
 				floor.canPassThrough()
 					&& floor.getTypeId() != Material.WATER.getId()
 					&& floor.getTypeId() != Material.STATIONARY_WATER.getId()
 			))
 			return false;
 
 		for (int y = playerLocation ? 0 : 1; y < (playerLocation ? 2 : 3); ++y)
 		{
 			RunsafeBlock block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + y, location.getBlockZ());
 			if (block.isHazardous() || !block.canPassThrough())
 				return false;
 		}
 		for (int x = -1; x < 2; ++x)
 			for (int z = -1; z < 2; ++z)
 			{
 				RunsafeBlock adjacent = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + 1, location.getBlockZ() + z);
 				if (adjacent.isHazardous())
 					return false;
 			}
 		return true;
 	}
 }
