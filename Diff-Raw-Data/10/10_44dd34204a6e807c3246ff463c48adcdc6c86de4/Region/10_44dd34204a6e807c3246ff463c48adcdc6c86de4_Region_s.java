 package com.censoredsoftware.Demigods.Engine.Object;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 public class Region
 {
 	private int x;
 	private int z;
 
 	public Region(int x, int z)
 	{
 		this.x = x;
 		this.z = z;
 	}
 
 	public int getX()
 	{
 		return x;
 	}
 
 	public int getZ()
 	{
 		return z;
 	}
 
 	public Location getCenter(World world)
 	{
 		return new Location(world, x, 128, z);
 	}
 
 	public static class Util
 	{
 		public static Region getRegion(Location location)
 		{
 			return new Region(getRegionCoordinate(location.getBlockX()), getRegionCoordinate(location.getBlockZ()));
 		}
 
 		public static Region getRegion(int X, int Z)
 		{
 			return new Region(getRegionCoordinate(X), getRegionCoordinate(Z));
 		}
 
 		private static int getRegionCoordinate(int number)
 		{
 			int round = ((number + 128) / 256);
 			if(number % 256 == 0) return number;
 			if(round == 0) return 0;
 			if(round == 1) return 256;
 			if(number % 256 > 128) return 256 * round;
 			return 256 * (round - 1);
 		}
 	}
 }
