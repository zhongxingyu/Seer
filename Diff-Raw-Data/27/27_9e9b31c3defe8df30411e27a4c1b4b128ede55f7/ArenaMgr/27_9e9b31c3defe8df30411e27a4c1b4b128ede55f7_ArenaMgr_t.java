 package com.github.pfacheris.BukkitDuel.Handlers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import com.github.pfacheris.BukkitDuel.BukkitDuel;
 import com.github.pfacheris.BukkitDuel.Objects.Arena;
 
 public class ArenaMgr {
 
 	private BukkitDuel plugin;
 	private List<Arena> arenas = new ArrayList<Arena>();
 
 	public ArenaMgr(BukkitDuel plugin)
 	{
 		this.plugin = plugin;
 		populateArenas();
 	}
 
 	private void populateArenas()
 	{
		Set<String> arenaNames = null;
		try
		{
			arenaNames = plugin.getConfig().getConfigurationSection("Arenas").getKeys(false);
		}
		catch(Exception e)
		{
			BukkitDuel.log.info("[BukkitDuel] No arenas found, be sure to define one in game.");
		}
 
 		if (arenaNames != null && arenaNames.size() > 0)
 		{
 			for(String name : arenaNames)
 			{
 				World arenaWorld = plugin.getServer().getWorld(plugin.getConfig().getString("Arenas." + name + ".world"));
 
 				if (arenaWorld != null)
 				{
 					try{
 						int minX = plugin.getConfig().getInt("Arenas." + name + ".corner1.x");
 						int minY = plugin.getConfig().getInt("Arenas." + name + ".corner1.y");
 						int minZ = plugin.getConfig().getInt("Arenas." + name + ".corner1.z");
 
 						int maxX = plugin.getConfig().getInt("Arenas." + name + ".corner2.x");
 						int maxY = plugin.getConfig().getInt("Arenas." + name + ".corner2.y");
 						int maxZ = plugin.getConfig().getInt("Arenas." + name + ".corner2.z");
 
 						double sX = plugin.getConfig().getDouble("Arenas." + name + ".spawn1.x");
 						double sY = plugin.getConfig().getDouble("Arenas." + name + ".spawn1.y");
 						double sZ = plugin.getConfig().getDouble("Arenas." + name + ".spawn1.z");
 						Location newSpawn1 = new Location(arenaWorld, sX,sY,sZ);
 
 						sX = plugin.getConfig().getDouble("Arenas." + name + ".spawn2.x");
 						sY = plugin.getConfig().getDouble("Arenas." + name + ".spawn2.y");
 						sZ = plugin.getConfig().getDouble("Arenas." + name + ".spawn2.z");
 						Location newSpawn2 = new Location(arenaWorld, sX,sY,sZ);
 
 						Arena tempArena = new Arena(plugin, name, arenaWorld, minX, minY, minZ, maxX, maxY, maxZ);
 						tempArena.setSpawn1(newSpawn1);
 						tempArena.setSpawn2(newSpawn2);
 						this.arenas.add(tempArena);
 					}
 					catch (Exception e)
 					{
						BukkitDuel.log.info("[BukkitDuel] Configuration could not be loaded for arena: " + name);
 					}
 				}
 			}
 		}
 	}
 
 	public List<Arena> getArenas()
 	{
 		return arenas;
 	}
 	
 	public Arena getArenaByName(String arenaName)
 	{
 		for (Arena arena : arenas)
 		{
 			if (arena.getName().equalsIgnoreCase(arenaName))
 			{
 				return arena;
 			}
 		}
 
 		return null;
 	}
 
 	public List<Arena> getArenasByWorld(String worldName)
 	{
 		List<Arena> arenasByWorld = new ArrayList<Arena>();
 		for (Arena arena : arenas)
 		{
 			if (arena.getWorld().getName().equalsIgnoreCase(worldName))
 			{
 				arenasByWorld.add(arena);
 			}
 		}
 
 		return arenasByWorld;
 	}
 
 	public List<Arena> getArenasByUsed(boolean inUse)
 	{
 		List<Arena> arenasByUsed = new ArrayList<Arena>();
 		for (Arena arena : arenas)
 		{
 			if (arena.getInUse() == inUse)
 			{
 				arenasByUsed.add(arena);
 			}
 		}
 
 		return arenasByUsed;
 	}
 
 	public void save(String name, World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
 	{
 		boolean nameAvailable = true;
 
 		for (int i = 0; i < arenas.size() && nameAvailable; i++)
 		{
 			if (arenas.get(i).getName().equalsIgnoreCase(name))
 				nameAvailable = false;
 		}
 
 		if (nameAvailable)
 		{
 
 			Arena newArena = new Arena(plugin, name, world, minX, minY, minZ, maxX, maxY, maxZ);
 			arenas.add(newArena);
 
 		}
 
 	}
 
 	public void remove(Arena toDelete)
 	{
 		arenas.remove(toDelete);
 
 		plugin.getConfig().set("Arenas." + toDelete.getName(), null);
 
 		plugin.saveConfig();
 	}
 
 }
