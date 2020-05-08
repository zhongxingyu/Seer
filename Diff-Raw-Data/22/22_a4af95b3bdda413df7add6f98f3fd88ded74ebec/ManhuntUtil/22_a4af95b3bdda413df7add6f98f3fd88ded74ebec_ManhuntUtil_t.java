 package com.bendude56.hunted;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import com.bendude56.hunted.games.Game.GameStage;
 import com.bendude56.hunted.teams.TeamManager.Team;
 
 public class ManhuntUtil {
 	
 	public static Location safeTeleport(Location loc) {
 		Location location1 = loc.clone();
 		location1.setY(location1.getY() - 1);
 		Location location2 = loc.clone();
 		location2.setY(location2.getY() - 1);
 
 		while (!isTransparent(location1.getBlock())
 				&& !isTransparent(location2.getBlock())) {
 			location1.setY(location1.getY() + 1);
 			location2.setY(location2.getY() + 1);
 		}
 		return location1;
 	}
 	
 	public static void stepPlayer(Player player, Double distance, Location target) {
 		double x1 = player.getLocation().getX();
 		double z1 = player.getLocation().getZ();
 		double x2 = target.getX();
 		double z2 = target.getZ();
 		Location destination = player.getLocation();
 
 		destination.setX(x1 + distance * (x2 - x1) / getDistance(x1, 0, z1, x2, 0, z2, false));
 		destination.setZ(z1 + distance * (z2 - z1) / getDistance(z1, 0, z1, z2, 0, z2, false));
 
 		player.teleport(safeTeleport(destination));
 	}
 	
 	public static Location randomLocation(Location origin, double radius)
 	{
 		Location randomLocation = origin.clone();
 		int sign = (int) Math.floor(Math.random() * 2);
 		if (sign == 0)
 			sign = -1;
 		else
 			sign = 1;
 		randomLocation.setX(randomLocation.getX() + (sign)
 				* (Math.random() * radius)
 				* (Math.cos(Math.toRadians(Math.random() * 180))));
 		sign = (int) Math.floor(Math.random() * 2);
 		if (sign == 0)
 			sign = -1;
 		else
 			sign = 1;
 		randomLocation.setZ(randomLocation.getZ() + (sign)
 				* (Math.random() * radius)
 				* (Math.cos(Math.toRadians(Math.random() * 180))));
 		return randomLocation;
 	}
 	
 	public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2, boolean ignoreY)
 	{
 		return Math.sqrt(Math.pow((x2 - x1), 2) + (ignoreY ? 0 : Math.pow((y2 - y1), 2)) + Math.pow((z2 - z1), 2));
 	}
 
 	public static double getDistance(Location loc1, Location loc2, boolean ignoreY)
 	{
 		return getDistance(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getZ(), loc2.getZ(), ignoreY);
 	}
 
 	public static double getDistance(Player p1, Player p2, boolean ignoreY)
 	{
 		return getDistance(p1.getLocation(), p2.getLocation(), ignoreY);
 	}
 
 	public static double getDirection(Location loc1, Location loc2)
 	{
 		double direction;
 		direction = loc2.getZ() - loc1.getZ();
 		direction = direction / ManhuntUtil.getDistance(loc1, loc2, true);
 		direction = Math.acos(direction);
 		direction = Math.toDegrees(direction);
 
 		if (loc2.getX() < loc1.getX())
 		{
 			direction = 180 - direction + 180;
 		}
 
 		return direction;
 	}
 
 	public static double getDirectionFacing(Player p)
 	{
 		double direction = p.getLocation().getYaw();
 		
 		if (direction < 0)
 		{
 			direction += 360;
 		}
 		
 		direction = 360 - direction;
 		
 		return direction;
 	}
 
 	public static double getDirectionDifference(double dir1, double dir2)
 	{
 		return dir2 - dir1;
 	}
 
 	public static boolean areEqualLocations(Location loc1, Location loc2, double tolerance, boolean ignoreY) {
 		if (loc1.getWorld() == loc2.getWorld()
 				&& loc2.getX() >= loc1.getX() - tolerance
 				&& loc2.getX() <= loc1.getX() + tolerance
 				&& (ignoreY || loc2.getY() >= loc1.getY() - tolerance)
 				&& (ignoreY || loc2.getY() <= loc1.getY() + tolerance)
 				&& loc2.getZ() >= loc1.getZ() - tolerance
 				&& loc2.getZ() <= loc1.getZ() + tolerance) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean checkPlayerInBounds(Player p)
 	{
 		if (!isInBounds(p))
 		{
 			stepInBounds(p);
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	private static boolean isInBounds(Player p)
 	{
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		
 		if (!plugin.gameIsRunning())
 		{
 			return true;
 		}
 		else if (plugin.getGame().getStage() == GameStage.PREGAME)
 		{
 			return true;
 		}
 		
 		Team team = plugin.getTeams().getTeamOf(p);
 		
 		if (team != Team.HUNTERS && team != Team.PREY)
 		{
 			return true;
 		}
 		
 		if (plugin.getSettings().BOUNDARY_BOXED.value)
 		{
 			if (plugin.gameIsRunning() && plugin.getGame().getStage() == GameStage.SETUP && team == Team.HUNTERS)
 			{
 				if (plugin.getSettings().BOUNDARY_SETUP.value > 0)
 				{
 					if (p.getLocation().getX() < plugin.getSettings().SPAWN_SETUP.value.getX() - plugin.getSettings().BOUNDARY_SETUP.value)
 						return false;
 					if (p.getLocation().getX() > plugin.getSettings().SPAWN_SETUP.value.getX() + plugin.getSettings().BOUNDARY_SETUP.value)
 						return false;
 					if (p.getLocation().getZ() < plugin.getSettings().SPAWN_SETUP.value.getZ() - plugin.getSettings().BOUNDARY_SETUP.value)
 						return false;
 					if (p.getLocation().getZ() > plugin.getSettings().SPAWN_SETUP.value.getZ() + plugin.getSettings().BOUNDARY_SETUP.value)
 						return false;
 				}
 			}
 			else
 			{
 				if (plugin.getSettings().BOUNDARY_WORLD.value > 0)
 				{
 					if (p.getLocation().getX() < plugin.getSettings().SPAWN_HUNTER.value.getX() - plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getX() < plugin.getSettings().SPAWN_PREY.value.getX() - plugin.getSettings().BOUNDARY_WORLD.value)
 						return false;
 					if (p.getLocation().getX() > plugin.getSettings().SPAWN_HUNTER.value.getX() + plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getX() > plugin.getSettings().SPAWN_PREY.value.getX() + plugin.getSettings().BOUNDARY_WORLD.value)
 						return false;
 					if (p.getLocation().getZ() < plugin.getSettings().SPAWN_HUNTER.value.getZ() - plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getZ() < plugin.getSettings().SPAWN_PREY.value.getZ() - plugin.getSettings().BOUNDARY_WORLD.value)
 						return false;
 					if (p.getLocation().getZ() > plugin.getSettings().SPAWN_HUNTER.value.getZ() + plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getZ() > plugin.getSettings().SPAWN_PREY.value.getZ() + plugin.getSettings().BOUNDARY_WORLD.value)
 						return false;
 				}
 			}
 		}
 		else
 		{
 			if (plugin.gameIsRunning() && plugin.getGame().getStage() == GameStage.SETUP && team == Team.HUNTERS && plugin.getSettings().BOUNDARY_SETUP.value >= 0)
 			{
 				if (plugin.getSettings().BOUNDARY_SETUP.value > 0)
 				{
 					if (getDistance(plugin.getSettings().SPAWN_SETUP.value, p.getLocation(), true) > plugin.getSettings().BOUNDARY_SETUP.value)
 						return false;
 				}
 			}
 			else
 			{
 				if (plugin.getSettings().BOUNDARY_WORLD.value > 0)
 				{
 					Location nearestLocation = getNearestCenterPoint(p.getLocation());
 					
 					if (getDistance(p.getLocation(), nearestLocation, true) > plugin.getSettings().BOUNDARY_WORLD.value)
 						return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private static void stepInBounds(Player p)
 	{
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		
 		if (plugin.getSettings().BOUNDARY_BOXED.value)
 		{
 			Location newLoc = p.getLocation();
 			
 			if (plugin.gameIsRunning() && plugin.getTeams().getTeamOf(p) == Team.HUNTERS)
 			{
 				if (p.getLocation().getX() < plugin.getSettings().SPAWN_SETUP.value.getX() - plugin.getSettings().BOUNDARY_SETUP.value)
 					newLoc.setX(newLoc.getX() + 1);
 				if (p.getLocation().getX() > plugin.getSettings().SPAWN_SETUP.value.getX() + plugin.getSettings().BOUNDARY_SETUP.value)
 					newLoc.setX(newLoc.getX() - 1);
 				if (p.getLocation().getZ() < plugin.getSettings().SPAWN_SETUP.value.getZ() - plugin.getSettings().BOUNDARY_SETUP.value)
 					newLoc.setZ(newLoc.getZ() + 1);
 				if (p.getLocation().getZ() > plugin.getSettings().SPAWN_SETUP.value.getZ() + plugin.getSettings().BOUNDARY_SETUP.value)
 					newLoc.setZ(newLoc.getZ() - 1);
 			}
 			else
 			{
 				if (p.getLocation().getX() < plugin.getSettings().SPAWN_HUNTER.value.getX() - plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getX() < plugin.getSettings().SPAWN_PREY.value.getX() - plugin.getSettings().BOUNDARY_WORLD.value)
 					newLoc.setX(p.getLocation().getX() + 1);
 				if (p.getLocation().getX() > plugin.getSettings().SPAWN_HUNTER.value.getX() + plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getX() > plugin.getSettings().SPAWN_PREY.value.getX() + plugin.getSettings().BOUNDARY_WORLD.value)
 					newLoc.setX(p.getLocation().getX() - 1);
 				if (p.getLocation().getZ() < plugin.getSettings().SPAWN_HUNTER.value.getZ() - plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getZ() < plugin.getSettings().SPAWN_PREY.value.getZ() - plugin.getSettings().BOUNDARY_WORLD.value)
 					newLoc.setZ(p.getLocation().getZ() + 1);
 				if (p.getLocation().getZ() > plugin.getSettings().SPAWN_HUNTER.value.getZ() + plugin.getSettings().BOUNDARY_WORLD.value && p.getLocation().getZ() > plugin.getSettings().SPAWN_PREY.value.getZ() + plugin.getSettings().BOUNDARY_WORLD.value)
 					newLoc.setZ(p.getLocation().getZ() - 1);
 			}
 			
 			p.teleport(safeTeleport(newLoc));
 		}
 		else
 		{
 			if (plugin.gameIsRunning() && plugin.getGame().getStage() == GameStage.PREGAME && plugin.getTeams().getTeamOf(p) == Team.HUNTERS)
 			{
 				stepPlayer(p, (double) 1, safeTeleport(plugin.getSettings().SPAWN_SETUP.value));
 			}
 			else
 			{
 				stepPlayer(p, (double) 1, getNearestCenterPoint(safeTeleport(p.getLocation())));
 			}
 		}
 	}
 
 	private static Location getNearestCenterPoint(Location loc)
 	{
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		
 		Location nearestLocation;
 		Location hunterSpawn = plugin.getSettings().SPAWN_HUNTER.value;
 		Location preySpawn = plugin.getSettings().SPAWN_PREY.value;
 		
 		double scalar = ((((hunterSpawn.getX() - preySpawn.getX()) * (loc.getX() - preySpawn.getX())) + ((hunterSpawn.getY() - preySpawn.getY()) * (loc.getY() - preySpawn.getY())) + ((hunterSpawn.getZ() - preySpawn.getZ()) * (loc.getZ() - preySpawn.getZ()))) / (((hunterSpawn.getX() - preySpawn.getX()) * (hunterSpawn.getX() - preySpawn.getX())) + ((hunterSpawn.getY() - preySpawn.getY()) * (hunterSpawn.getY() - preySpawn.getY())) + ((hunterSpawn.getZ() - preySpawn.getZ()) * (hunterSpawn.getZ() - preySpawn.getZ()))));
 		
 		if (scalar > 1 || scalar < 0)
 		{
 			if (ManhuntUtil.getDistance(preySpawn, loc, true) < ManhuntUtil.getDistance(hunterSpawn, loc, true)) {
 				nearestLocation = preySpawn;
 			} else {
 				nearestLocation = hunterSpawn;
 			}
 		}
 		else
 		{
 			nearestLocation = new Location(plugin.getWorld(), (preySpawn.getX() + scalar * (hunterSpawn.getX() - preySpawn.getX())), (preySpawn.getY() + scalar * (hunterSpawn.getY() - preySpawn.getY())), (preySpawn.getZ() + scalar * (hunterSpawn.getZ() - preySpawn.getZ())));
 		}
 		
 		return nearestLocation;
 	}
 
 	public static boolean isTransparent(Block block) {
 		List<Material> types = new ArrayList<Material>();
 				types.add(Material.AIR);
 				types.add(Material.BREWING_STAND);
 				types.add(Material.BROWN_MUSHROOM);
 				types.add(Material.CAKE);
 				types.add(Material.CROPS);
 				types.add(Material.DETECTOR_RAIL);
 				types.add(Material.DIODE_BLOCK_ON);
 				types.add(Material.DIODE_BLOCK_OFF);
 				types.add(Material.DRAGON_EGG);
 				types.add(Material.LEVER);
 				types.add(Material.LONG_GRASS);
 				types.add(Material.MELON_STEM);
 				types.add(Material.NETHER_STALK);
 				types.add(Material.PAINTING);
 				types.add(Material.PORTAL);
 				types.add(Material.POWERED_RAIL);
 				types.add(Material.PUMPKIN_STEM);
 				types.add(Material.RAILS);
 				types.add(Material.RED_MUSHROOM);
 				types.add(Material.RED_ROSE);
 				types.add(Material.REDSTONE_TORCH_ON);
 				types.add(Material.REDSTONE_TORCH_OFF);
 				types.add(Material.REDSTONE_WIRE);
 				types.add(Material.SAPLING);
 				types.add(Material.SIGN_POST);
 				types.add(Material.SNOW);
 				types.add(Material.SUGAR_CANE_BLOCK);
 				types.add(Material.TORCH);
 				types.add(Material.VINE);
 				types.add(Material.WALL_SIGN);
 				types.add(Material.YELLOW_FLOWER);
 		return (types.contains(block.getType()));
 	}
 
 	public static boolean isHostile(Entity entity)
 	{
 		EntityType type = entity.getType();
 		
 		return (type == EntityType.ZOMBIE
 				|| type == EntityType.SKELETON
 				|| type == EntityType.SPIDER
 				|| type == EntityType.CREEPER
 				|| type == EntityType.SLIME
 				|| type == EntityType.ENDERMAN
 				|| type == EntityType.CAVE_SPIDER
 				|| type == EntityType.SILVERFISH
 				|| type == EntityType.PIG_ZOMBIE
 				|| type == EntityType.GHAST
 				|| type == EntityType.MAGMA_CUBE
 				|| type == EntityType.BLAZE);
 	}
 
 	public static boolean isPassive(Entity entity)
 	{
 		EntityType type = entity.getType();
 		
 		return (type == EntityType.PIG
 				|| type == EntityType.COW
 				|| type == EntityType.SHEEP
 				|| type == EntityType.CHICKEN
 				|| type == EntityType.SQUID
 				|| type == EntityType.WOLF
 				|| type == EntityType.OCELOT
 				|| type == EntityType.MUSHROOM_COW);
 	}
 
 	public static void sendTeamToLocation(Team team, Location loc)
 	{
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		
 		List<Player> players = plugin.getTeams().getTeamPlayers(team);
 		
		int width = (int) Math.ceil(Math.sqrt(players.size()))-1;
 		int playerIndex = 0;
 		
		for (double xOffset = -(double)width / (double)2 ; xOffset <= (double)width / (double)2 ; xOffset+=1)
 		{
			for (double zOffset = -(double)width / (double)2 ; zOffset <= (double)width / (double)2 ; zOffset+=1)
 			{
 				if (playerIndex >= players.size())
 				{
 					break;
 				}
 
 				Location l = loc.clone();
 				l.setX(l.getX() + xOffset);
 				l.setZ(l.getZ() + zOffset);
 				l = safeTeleport(l);
 
 				Player player = players.get(playerIndex);
 				
 				player.teleport(l);
 				
 				playerIndex++;
 			}
 		}
 	}
 
 	public static void sendToSpawn(Player p)
 	{
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		Location loc = plugin.getWorld().getSpawnLocation();
 		
 		switch (plugin.getTeams().getTeamOf(p))
 		{
 			case HUNTERS:	loc = randomLocation(plugin.getSettings().SPAWN_HUNTER.value, 2);
 			case PREY:		loc = randomLocation(plugin.getSettings().SPAWN_PREY.value, 2);
 			default:		loc = p.getWorld().getSpawnLocation();
 		}
 		
 		p.teleport(safeTeleport(loc));
 	}
 	
 }
