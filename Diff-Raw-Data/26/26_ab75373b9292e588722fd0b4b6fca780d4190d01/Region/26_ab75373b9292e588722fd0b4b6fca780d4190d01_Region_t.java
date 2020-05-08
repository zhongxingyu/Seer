 package com.censoredsoftware.Demigods.Engine.Object;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 
 import com.google.common.collect.DiscreteDomain;
 
 public class Region
 {
	public final static int REGION_LENGTH = 64;
	public final static int HALF_REGION_LENGTH = REGION_LENGTH / 2;

 	private int x;
 	private int z;
 	private String world;
 
 	public Region(int x, int z, String world)
 	{
 		this.x = x;
 		this.z = z;
 		this.world = world;
 	}
 
 	public int getX()
 	{
 		return x;
 	}
 
 	public int getZ()
 	{
 		return z;
 	}
 
 	public String getWorld()
 	{
 		return world;
 	}
 
 	public Location getCenter() throws Exception
 	{
 		return new Location(Bukkit.getWorld(world), x, 128, z);
 	}
 
 	public static class Util
 	{
 		public static Region getRegion(Location location)
 		{
 			return new Region(getRegionCoordinate(location.getBlockX()), getRegionCoordinate(location.getBlockZ()), location.getWorld().getName());
 		}
 
 		public static Region getRegion(int X, int Z, String WORLD)
 		{
 			return new Region(getRegionCoordinate(X), getRegionCoordinate(Z), WORLD);
 		}
 
 		private static int getRegionCoordinate(int number)
 		{
			int temp = number % REGION_LENGTH;
			if(temp >= HALF_REGION_LENGTH) return number + REGION_LENGTH - temp;
 			return number - temp;
 		}
 
 		public static Size size()
 		{
 			return new Size();
 		}
 
 		private static class Size extends DiscreteDomain<Integer>
 		{
 			@Override
 			public Integer next(Integer integer)
 			{
				return integer + REGION_LENGTH;
 			}
 
 			@Override
 			public Integer previous(Integer integer)
 			{
				return integer - REGION_LENGTH;
 			}
 
 			@Override
 			public long distance(Integer integer, Integer integer2)
 			{
				return Math.abs((getRegionCoordinate(integer) / REGION_LENGTH) - (getRegionCoordinate(integer2) / REGION_LENGTH));
 			}
 		}
 	}
 }
