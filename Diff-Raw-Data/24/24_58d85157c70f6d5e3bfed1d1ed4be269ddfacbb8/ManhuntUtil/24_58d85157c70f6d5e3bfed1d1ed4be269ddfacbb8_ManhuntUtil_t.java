 package com.deaboy.manhunt;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 public abstract class ManhuntUtil
 {
 	private static HashMap<String, WorldTimeMachine> worldTimeMachines = new HashMap<String, WorldTimeMachine>();
 	
 	public static Location safeTeleport(Location loc)
 	{
 		Location location1 = loc.clone();
 		location1.setY(location1.getY() - 1);
 		Location location2 = loc.clone();
 		location2.setY(location2.getY() - 1);
 
 		while (location1.getBlock().getType().isSolid()
 				&& !location2.getBlock().getType().isSolid())
 		{
 			location1.setY(location1.getY() + 1);
 			location2.setY(location2.getY() + 1);
 		}
 		return location1;
 	}
 	
 	public static void stepPlayer(Player player, Double distance, Location target)
 	{
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
 		Location loc = origin.clone();
 		
 		double angle = Math.random() * 2 * Math.PI;
 		double length = Math.random() * radius;
 		
 		loc.setX(loc.getX() + Math.cos(angle) * length);
 		loc.setY(loc.getY() + Math.sin(angle) * length);
 		
 		return loc;
 	}
 	
 	public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2, boolean ignoreY)
 	{
 		return Math.sqrt(Math.pow((x2 - x1), 2) + (ignoreY ? 0 : Math.pow((y2 - y1), 2)) + Math.pow((z2 - z1), 2));
 	}
 
 	public static double getDistance(Location loc1, Location loc2, boolean ignoreY)
 	{
 		return getDistance(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getZ(), loc2.getZ(), ignoreY);
 	}
 	
 	/**
 	 * Compares two locations, returning whether or not they are an acceptable distance apart.
 	 * @param loc1 The first location to compare.
 	 * @param loc2 The second location to compare
 	 * @param tolerance The acceptable distance between the two locations.
 	 * @param ignoreY True if you want the calculation to ignore the Y-coordinate.
 	 * @return True if the two locations are within <CODE>tolerance</CODE> apart and are inside the same world.
 	 * 			False if the locations are farther than <CODE>tolerance</CODE> apart or are in different worlds. 
 	 */
 	public static boolean areEqualLocations(Location loc1, Location loc2, double tolerance, boolean ignoreY)
 	{
 		if (loc1.getWorld() != loc2.getWorld())
 			return false;
 		else
 			return (getDistance(loc1, loc2, ignoreY) <= tolerance);
 	}
 	
 	public static boolean isHostile(Entity entity)
 	{
 		switch (entity.getType())
 		{
 		case ZOMBIE:
 		case SKELETON:
 		case SPIDER:
 		case SLIME:
 		case ENDERMAN:
 		case CAVE_SPIDER:
 		case SILVERFISH:
 		case PIG_ZOMBIE:
 		case GHAST:
 		case MAGMA_CUBE:
 		case BLAZE:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public static boolean isPassive(Entity entity)
 	{
 		switch (entity.getType())
 		{
 		case PIG:
 		case COW:
 		case SHEEP:
 		case CHICKEN:
 		case SQUID:
 		case WOLF:
 		case OCELOT:
 		case MUSHROOM_COW:
 		case VILLAGER:
 		case BAT:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	/**
 	 * Smoothly transitions a world's time to the given day time.
 	 * @param world The world to manipulate
 	 * @param time The time of day you want the cycle to end on
 	 * @param runnable The action to perform at the end of the cycle
 	 */
 	public static void transitionWorldTime(World world, long time, Runnable runnable)
 	{
 		if (world == null) return;
		time = ((time % 24000) + 24000) % 24000;
		new WorldTimeMachine(world, ((world.getFullTime() / 24000) + (world.getFullTime() % 24000 > time ? 1 : 0)) * 24000 + time, runnable);
 	}
 	/**
 	 * Smoothly transitions a world's full time to the given full time.
 	 * @param world The world to manipulate
 	 * @param time The full time you want the cycle to end on
 	 * @param runnable The action to perform at the end of the cycle
 	 */
 	public static void transitionWorldFullTime(World world, long fulltime, Runnable runnable)
 	{
 		if (world == null) return;
 		if (fulltime < 0) return;
 		
 		new WorldTimeMachine(world, fulltime, runnable);
 	}
 	
 	/**
 	 * Cancels a world's time transition.
 	 * @param world
 	 */
 	public static void cancelWorldTimeTransition(World world)
 	{
 		if (worldTimeMachines.containsKey(world.getName()))
 		{
 			worldTimeMachines.get(world.getName()).cancel();
 		}
 	}
 	
 
 	static class WorldTimeMachine
 	{
 		private final World world;
 		private final long starttime;
 		private final long endtime;
 		private final List<Runnable> actions;
 		private final int schedule;
 		
 		private static final int maxspeed = 400; // ticks per tick
 		private static final int acceltime = 2400; // acceleration distance
 		private static final int decceltime = 4800;
 		private static final double accelrate = (double) maxspeed / (double) acceltime;
 		private static final double deccelrate = (double) maxspeed / (double) decceltime;
 		private static final double accelratio = (double) (acceltime + decceltime) / (double) decceltime;
 		
 		public WorldTimeMachine(World world, long fulltime, Runnable runnable)
 		{
 			
 			this.world = world;
 			this.starttime = world.getFullTime();
 			this.endtime = fulltime;
 			this.actions = new ArrayList<Runnable>();
 			
 			if (worldTimeMachines.containsKey(world.getName()))
 			{
 				actions.addAll(worldTimeMachines.get(world.getName()).actions);
 				worldTimeMachines.get(world.getName()).cancel();
 			}
 			
 			actions.add(runnable);
 			schedule = Bukkit.getScheduler().scheduleSyncRepeatingTask(ManhuntPlugin.getInstance(), new Runnable()
 			{
 				public void run()
 				{
 					step();
 				}
 			}, 0, 0);
			worldTimeMachines.put(world.getName(), this);
 		}
 		
 		private void step()
 		{
 			long time = world.getFullTime();
 			
 			if (Math.abs(endtime - time) <= 1)
 			{
 				end();
 			}
 			else if (Math.abs(time - starttime) > Math.abs(endtime - starttime) / accelratio) //later half
 			{
 				if (Math.abs(endtime - time) < decceltime)
 				{
 					world.setFullTime(time + (long) ((endtime - time) * deccelrate));
 				}
 				else
 				{
 					world.setFullTime(time + (endtime > starttime ? maxspeed : -maxspeed));
 				}
 			}
 			else //first half
 			{
 				if (Math.abs(time - starttime) < acceltime)
 				{
 					world.setFullTime(time + (long) ((time - starttime) * accelrate));
 				}
 				else
 				{
 					world.setFullTime(time + (endtime > starttime ? maxspeed : -maxspeed));
 				}
 			}
 		}
 		
 		public void end()
 		{
 			world.setFullTime(endtime);
 			for (Runnable runnable : actions)
 			{
 				if (runnable != null) runnable.run();
 			}
 			cancel();
 		}
 		public void cancel()
 		{
 			Bukkit.getScheduler().cancelTask(schedule);
 			if (worldTimeMachines.containsKey(world.getName()))
 				worldTimeMachines.remove(world.getName());
 		}
 		
 		
 	}
 	
 }
