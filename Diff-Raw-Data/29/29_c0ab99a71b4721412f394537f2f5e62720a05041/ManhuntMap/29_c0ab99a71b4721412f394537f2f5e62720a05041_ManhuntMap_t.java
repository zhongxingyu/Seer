 package com.deaboy.manhunt.map;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 
 public class ManhuntMap implements Map
 {
 	//-------------------- Properties --------------------//
 	private String name;
 	private World world;
 	private Location spawn;
 	private HashMap<String, Spawn> points;
 	private HashMap<String, Zone> zones;
 	
 	
 	//-------------------- Constructors --------------------//
 	/**
 	 * Initializes a new ManhuntMap at the given World's spawn location.
 	 * @param world The World of the new Map.
 	 */
 	public ManhuntMap(String name, World world)
 	{
 		this(name, world.getSpawnLocation(), world);
 	}
 	
 	/**
 	 * Initializes a new ManhuntMap with the given spawn location.
 	 * @param loc The spawn location of the new map.
 	 */
 	public ManhuntMap(String name, Location loc, World world)
 	{
 		this.name = name;
 		this.world = world;
 		this.spawn = loc;
 		this.points = new HashMap<String, Spawn>();
 		this.zones = new HashMap<String, Zone>();
 	}
 	
 	
 	
 	//-------------------- Public Methods --------------------//
 	//---------------- Getters ----------------//
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 	@Override
 	public String getFullName()
 	{
 		return getWorld().getName() + "." + getName();
 	}
 	
 	@Override
 	public Location getSpawnLocation()
 	{
 		return spawn.clone();
 	}
 	@Override
 	public World getWorld()
 	{
 		return world;
 	}
 	
 	@Override
 	public Spawn getPoint(String name)
 	{
 		if (this.points.containsKey(name))
 		{
 			return this.points.get(name);
 		}
 		else
 		{
 			return null;
 		}
 	}
 	@Override
 	public List<Spawn> getPoints()
 	{
 		return (new ArrayList<Spawn>(this.points.values()));
 	}
 	@Override
 	public List<Spawn> getPoints(SpawnType type)
 	{
 		List<Spawn> spawns = new ArrayList<Spawn>();
 		for (Spawn spawn : this.points.values())
 			if (spawn.getType() == type)
 				spawns.add(spawn);
 		return spawns;
 	}
 	
 	@Override
 	public Zone getZone(String name)
 	{
 		if (zones.containsKey(name))
 			return zones.get(name);
 		else
 			return null;
 	}
 	@Override
 	public List<Zone> getZones()
 	{
 		return new ArrayList<Zone>(this.zones.values());
 	}
 	@Override
 	public List<Zone> getZones(ZoneFlag ... flags)
 	{
 		List<Zone> zones = new ArrayList<Zone>();
 		for (Zone zone : this.zones.values())
 		{
 			for (ZoneFlag flag : flags)
 			{
 				if (zone.checkFlag(flag))
 				{
 					zones.add(zone);
 					break;
 				}
 			}
 		}
 		return zones;
 	}
 	
 	
 	
 	//---------------- Setters ----------------//
 	@Override
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	@Override
 	public void setSpawn(Location loc)
 	{
 		if (loc != null && loc.getWorld() == getWorld())
 			this.spawn = loc.clone();
 	}
 	
 	@Override
 	public boolean addPoint(Spawn point)
 	{
		if (!this.points.containsKey(point.getName()) && !this.points.containsValue(point) && point.getWorld() == this.getWorld().getWorld())
 		{
 			this.points.put(point.getName(), point);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	@Override
 	public Spawn createPoint(String name, SpawnType type, Location location)
 	{
 		return createPoint(name, type, location, 0);
 	}
 	@Override
 	public Spawn createPoint(String name, SpawnType type, Location location, int range)
 	{
 		Spawn point;
 		
 		if (name == null || this.points.containsKey(name))
 			return null;
 		
 		if (location == null || location.getWorld() != this.world.getWorld())
 			return null;
 		
 		if (range < 0)
 			return null;
 		
 		if (type == null)
 			return null;
 		
 		point = new ManhuntSpawn(name, type, location, range);
 		
 		this.points.put(name, point);
 		return point;
 	}
 	public void removePoint(String name)
 	{
 		if (this.points.containsKey(name))
 		{
 			this.points.remove(name);
 		}
 	}
 	public void clearPoints()
 	{
 		this.points.clear();
 	}
 	
 	@Override
 	public boolean addZone(Zone zone)
 	{
		if (!this.zones.containsKey(zone.getName()) && !this.zones.containsValue(zone) && zone.getWorld() == this.getWorld().getWorld())
 		{
 			this.zones.put(zone.getName(), zone);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	@Override
 	public Zone createZone(String name, Location corner1, Location corner2, List<ZoneFlag> flags)
 	{
 		ZoneFlag[] f = new ZoneFlag[flags.size()];
 		for (int i = 0; i < flags.size(); i++)
 			f[i] = flags.get(i);
 		return createZone(name, corner1, corner2, f);
 	}
 	@Override
 	public Zone createZone(String name, Location corner1, Location corner2, ZoneFlag...flags)
 	{
 		Zone zone;
 		
 		if (corner1 == null)
 			return null;
 		if (corner2 == null)
 			return null;
 		if (name == null || name.isEmpty())
 			return null;
 		if (corner1.getWorld() != corner2.getWorld())
 			return null;
 		
 		
 		
 		if (name.isEmpty())
 		{
 			int i = 1;
 			while (getZone("zone" + i) != null)
 				i++;
 			name = "zone" + i;
 		}
 		else
 		{
 			if (zones.containsKey(name))
 				return null;
 		}
 		
 		zone = new ManhuntZone(name, corner1, corner2);
 		
 		for (ZoneFlag flag : flags)
 			zone.setFlag(flag, true);
 		
 		zones.put(name, zone);
 		
 		return zone;
 	}
 	@Override
 	public void removeZone(String name)
 	{
 		if (zones.containsKey(name))
 			zones.remove(name);
 	}
 	@Override
 	public void clearZones()
 	{
 		this.zones.clear();
 	}
 	
 	
 	
 	
 	
 }
