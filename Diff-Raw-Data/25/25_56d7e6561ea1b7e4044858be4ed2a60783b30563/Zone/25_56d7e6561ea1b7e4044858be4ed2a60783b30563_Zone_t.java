 package com.bradsproject.BradleyJewell.bZone;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 public class Zone
 {
 	public final double minX, minY, minZ, maxX, maxY, maxZ;
 	public final Location spawn;
 	public final World world;
 	public final String name;
 	public boolean healing;
 	public Map<String,Boolean> protection;
 	public List<String> creatures;
 	public List<String> players;
 	
 	public Zone(String n, World w, double mx, double my, double mz, double Mx, double My, double Mz)
 	{
 		name = n;
 		
 		world = w;
 		
 		minX = mx;
 		minY = my;
 		minZ = mz;
		maxX = Mx - .001;
		maxY = My - .001;
		maxZ = Mz - .001;
 		
 		spawn = new Location(world, maxX - (maxX - minX)/2, maxY - (maxY - minY)/2, maxZ - (maxZ - minZ)/2);
 		
 		players = new ArrayList<String>();
 		creatures = new ArrayList<String>();
 		protection = new HashMap<String,Boolean>();
 	}
 	
 	public boolean contains(Location location)
 	{
 		double x = location.getX(), y = location.getY(), z = location.getZ();
		
		if(location.getWorld() != world)
			return false;
		
 		return contains(x,y,z);
 	}
 	
 	public boolean contains(double x, double y, double z)
 	{
 		if(
 				x >= minX && x <= maxX && 
 				y >= minY && y <= maxY && 
 				z >= minZ && z <= maxZ
 			)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean hasPlayer(String player)
 	{
 		player = player.toLowerCase();
 		if(name.equals(player) || players.contains(player))
 		{
 			return true;
 		}
 		return false;
 	}
 }
