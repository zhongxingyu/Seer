 package com.bradsproject.BradleyJewell.bSwarm;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.entity.Player;
 
 public class bSwarmTask implements Runnable
 {
 	public int id;
 	public Player player;
 	public String type;
 	public int num;
 	public int count = 0;
 	private final bSwarm plugin;
 	
 	public String name;
 	public boolean automated = false;
 	public Location location;
 	public double radius = 12;
 	public int ticksdelay;
 	public int timeofday = -1;
 	public int minutesinterval = -1;
 	
 	public boolean enabled = false;
 	
 	public Map<String,Integer> monsters = new HashMap<String,Integer>();
 	public Map<String,Integer> curMonsters = new HashMap<String,Integer>();
 	
 	
 	public bSwarmTask(bSwarm instance, Player p)
 	{
 		plugin = instance;
 		player = p;
 	}
 	
 	public bSwarmTask(bSwarm instance)
 	{
 		plugin = instance;
 	}
 	
 	@Override
 	public void run()
 	{
 		if(automated)
 		{
 			autoSwarm();
 		}
 		else if(player != null)
 		{
 			playerSwarm();
 		}
 	}
 	
 	public void playerSwarm()
 	{
 		location = player.getLocation();
 		if((count >= 0 && num > 0) && count < num)
 		{
 			/*CraftEntity spawned = */spawnMob();
 			
 			//if(spawned instanceof Monster)
 			//{
 			//	Monster monster = (Monster) spawned;
 			//	monster.setTarget(player);
 			//}
 			count++;
 		}
 		else if(num > 0)
 		{
 			plugin.getServer().getScheduler().cancelTask(id);
 			plugin.tasks.remove(this);
 		}
 		else
 		{
 			autoSwarm();
 		}
 	}
 	
 	public void autoSwarm()
 	{
 		count = 0;
 		Map<String,Integer> mons = new HashMap<String,Integer>(curMonsters);
 		for(String mon : mons.keySet())
 		{
 			if(mons.get(mon) == 0)
 				curMonsters.remove(mon);
 			count += mons.get(mon);
 		}
 		mons = new HashMap<String,Integer>(curMonsters);
 		if(count > 0)
 		{
 			Random random = new Random(System.currentTimeMillis());
 			int randomNumber = random.nextInt(curMonsters.size());
 			int selection = -1;
 			while(selection == -1)
 			{
 				int j=0;
 			    for(String key : mons.keySet())
 			    {
 			    	if(j < randomNumber)
 			    	{
 			    		j++;
 			    		continue;
 			    	}
 			    	if(curMonsters.get(key) <= 0)
 			    		randomNumber = random.nextInt(curMonsters.size());
 			    	else
 			    		selection = 1;
 			    }
 			}
 		    
 		    int i=0;
 		    for(String key : mons.keySet())
 		    {
 		    	if(i != randomNumber)
 		    	{
 		    		i++;
 		    		continue;
 		    	}
 		    	
 		    	if(curMonsters.get(key) <= 0)
 		    		break;
 		    	
 		    	int remaining = curMonsters.get(key);
 		    	
 		    	try
 				{
 		    		type = key.substring(0,1).toUpperCase() + key.substring(1).toLowerCase();
 		    		
 		    		spawnMob();
 		    		
 		    		curMonsters.remove(key);
 		    		curMonsters.put(key, remaining - 1);
 		    		break;
 				} catch (NullPointerException e)
 				{
 					System.out.println("Autoswarm error!");
 					//e.printStackTrace();
 					id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20);
 					break;
 				}
 		    }
 		    id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, ticksdelay);
 		}
 		else
 		{
 			if(timeofday != -1)
 			{
 				curMonsters = new HashMap<String, Integer>(monsters);
 				int delay = (int)(Math.abs(timeofday - location.getWorld().getTime()));
 				id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
 			}
 			else if(minutesinterval != -1)
 			{
 				curMonsters = new HashMap<String, Integer>(monsters);
 				id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, minutesinterval * 60 * 20);
 			}
 		}
 	}
 	
 	public CraftEntity spawnMob()
 	{
 		CraftEntity spawned = null;
 		try
 		{
 			spawned = plugin.spawn(location, type);
 			WorldServer world = ((org.bukkit.craftbukkit.CraftWorld)location.getWorld()).getHandle();
 			
 			double randAngle = Math.random() * 2 * Math.PI;
 			double randX = radius * Math.cos(randAngle);
 			double randZ = radius * Math.sin(randAngle);
 			
 			double x = location.getX() + randX;
 			double z = location.getZ() + randZ;
 			
			Location loc = new Location(location.getWorld(), x, location.getY(), z);
			Block block = loc.getWorld().getBlockAt(loc);
			while (!(block.getType() == Material.AIR || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER ))
			{
				loc.setY(loc.getY() + 1);
				block = loc.getWorld().getBlockAt(loc);
			}
 			spawned.teleportTo(loc);
			world.addEntity(spawned.getHandle());
 		} catch (Exception e)
 		{
 			System.out.println("Spawnmob error!");
 			e.printStackTrace();
 		}
 		return spawned;
 	}
 }
