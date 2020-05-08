 package com.forgenz.mobmanager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.forgenz.mobmanager.listeners.MobListener;
 import com.forgenz.mobmanager.listeners.PlayerListener;
 import com.forgenz.mobmanager.world.MMChunk;
 import com.forgenz.mobmanager.world.MMCoord;
 import com.forgenz.mobmanager.world.MMWorld;
 
 /**
  * <b>MobManager</b> </br>
  * MobManager aims to reduce the number of unnecessary mob spawns </br>
  * 
  * @author Michael McKnight (ShadowDog007)
  *
  */
 public class P extends JavaPlugin implements Listener, CommandExecutor
 {
 	public static P p = null;
 	public static MemoryConfiguration cfg = null;
 	
 	public static HashMap<String, MMWorld> worlds = null;
 
 	@Override
 	public void onLoad()
 	{
 	}
 
 	@Override
 	public void onEnable()
 	{
 		p = this;
 
 		saveDefaultConfig();
 		cfg = getConfig();
 
 		// Makes sure the setting 'SpawnChunkDistance' is valid
 		final int searchDist = P.cfg.getInt("SpawnChunkDistance", 6);
 
 		if (searchDist < 0)
 			P.cfg.set("SpawnChunkDistance", Math.abs(searchDist));
 		else if (searchDist == 0)
 			P.cfg.set("SpawnChunkDistance", 1);
 
 		worlds = new HashMap<String, MMWorld>();
 
 		// Load all the worlds required
 		for (final String worldName : cfg.getStringList("EnabledWorlds"))
 		{
 			final World world = getServer().getWorld(worldName);

 			// Check the world exists
 			if (world == null)
 			{
 				getLogger().warning(String.format("Could not find world '%s'", worldName));
 				continue;
 			}
 
 			worlds.put(worldName, new MMWorld(world));
 		}
 		
 		if (worlds.size() == 0)
 		{
 			getLogger().warning("No valid worlds found");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		// Register Mob event listener
 		getServer().getPluginManager().registerEvents(new MobListener(), this);
 		// Register Player event listener
 		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 		// Register Chunk event listener
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		// Register MobManager command
 		getCommand("mm").setExecutor(this);
 		
 		getLogger().info("v" + getDescription().getVersion() + " ennabled with " + worlds.size() + " worlds");
 		// And we are done :D
 	}
 
 	@Override
 	public void onDisable()
 	{
 		getServer().getScheduler().cancelTasks(this);
 		p = null;
 		cfg = null;
 		worlds = null;
 	}
 
 	@Override
 	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
 	{
 		if (args.length > 0)
 			if (args[0].equalsIgnoreCase("count"))
 			{
 				Collection<MMWorld> worldList;
 				
 				if (args.length > 1)
 				{
 					MMWorld world = worlds.get(args[1]);
 					
 					if (world == null)
 					{
 						sender.sendMessage("The world '" + args[1] + "' does not exist or is inactive");
 						return true;
 					}
 					
 					worldList = new ArrayList<MMWorld>();
 					
 					worldList.add(world);
 				}
 				else
 				{
 					worldList = worlds.values();
 				}
 				
 				if (worldList.size() == 0)
 				{
 					sender.sendMessage("[MobManger] No worlds were found");
 				}
 				
 				int totalMonsters = 0;
 				int totalAnimals = 0;
 				int totalWaterAnimals = 0;
 				int totalAmbient = 0;
 				int totalVillagers = 0;
 				
 				int totalMaxMonsters = 0;
 				int totalMaxAnimals = 0;
 				int totalMaxWaterAnimals = 0;
 				int totalMaxAmbient = 0;
 				int totalMaxVillagers = 0;
 				
 				int totalWorlds = 0;
 				int totalChunks = 0;
 
 				for (final MMWorld world : worldList)
 				{					
 					world.updateMobCounts();
 					
 					int numPlayers = 0;
 					for (final Entry<MMCoord, MMChunk> chunk : world.getChunks())
 						numPlayers += chunk.getValue().getNumPlayers();
 										
 					sender.sendMessage(String.format("%1$sWorld:%2$s%3$s, %1$sChunks:%2$s%4$d, %1$sPlayers:%2$s%5$d",
 							ChatColor.DARK_GREEN, ChatColor.AQUA, world.getWorld().getName(), world.getChunks().size(), numPlayers));
 					
 					sender.sendMessage(String.format("%1$sM:%2$s%4$d%3$s/%2$s%5$d, %1$sA:%2$s%6$d%3$s/%2$s%7$d, %1$sW:%2$s%8$d%3$s/%2$s%9$d, %1$sAm:%2$s%10$d%3$s/%2$s%11$d, %1$sV:%2$s%12$d%3$s/%2$s%13$d",
 							ChatColor.GREEN, ChatColor.AQUA, ChatColor.YELLOW,
 							world.getMobCount(MobType.MONSTER), world.maxMobs(MobType.MONSTER), 
 							world.getMobCount(MobType.ANIMAL), world.maxMobs(MobType.ANIMAL), 
 							world.getMobCount(MobType.WATER_ANIMAL), world.maxMobs(MobType.WATER_ANIMAL), 
 							world.getMobCount(MobType.AMBIENT), world.maxMobs(MobType.AMBIENT),
 							world.getMobCount(MobType.VILLAGER), world.maxMobs(MobType.VILLAGER)));
 					
 					if (args.length == 1)
 					{
 						totalMonsters += world.getMobCount(MobType.MONSTER);
 						totalAnimals += world.getMobCount(MobType.ANIMAL);
 						totalWaterAnimals += world.getMobCount(MobType.WATER_ANIMAL);
 						totalAmbient += world.getMobCount(MobType.AMBIENT);
 						totalVillagers += world.getMobCount(MobType.VILLAGER);
 						
 						totalMaxMonsters += world.maxMobs(MobType.MONSTER);
 						totalMaxAnimals += world.maxMobs(MobType.ANIMAL);
 						totalMaxWaterAnimals += world.maxMobs(MobType.WATER_ANIMAL);
 						totalMaxAmbient += world.maxMobs(MobType.AMBIENT);
 						totalMaxVillagers += world.getMobCount(MobType.VILLAGER);
 						
 						++totalWorlds;
 						totalChunks += world.getChunks().size();
 					}
 				}
 				
 				if (args.length == 1)
 				{
 					int totalMobs = totalMonsters + totalAnimals + totalWaterAnimals + totalAmbient + totalVillagers;
 					int totalMaxMobs = totalMaxMonsters + totalMaxAnimals + totalMaxWaterAnimals + totalMaxAmbient + totalMaxVillagers;
 					
 					sender.sendMessage(String.format("%1$sTotals - Worlds:%2$s%3$d, %1$sChunks:%2$s%4$d, %1$sPlayers:%2$s%5$d",
 							ChatColor.GREEN, ChatColor.AQUA,
 							totalWorlds,
 							totalChunks,
 							getServer().getOnlinePlayers().length));
 					
 					sender.sendMessage(String.format("%1$sM:%2$s%4$d%3$s/%2$s%5$d, %1$sA:%2$s%6$d%3$s/%2$s%7$d, %1$sW:%2$s%8$d%3$s/%2$s%9$d, %1$sAm:%2$s%10$d%3$s/%2$s%11$d, %1$sV:%2$s%12$d%3$s/%2$s%13$d %1$sT:%2$s%14$d%3$s/%2$s%15$d",
 							ChatColor.GREEN, ChatColor.AQUA, ChatColor.YELLOW,
 							totalMonsters, totalMaxMonsters, 
 							totalAnimals, totalMaxAnimals, 
 							totalWaterAnimals, totalMaxWaterAnimals,
 							totalAmbient, totalMaxAmbient,
 							totalVillagers, totalMaxVillagers,
 							totalMobs, totalMaxMobs));
 				}
 				
 				return true;
 			}
 		return false;
 	}
 
 	// Events Listener Methods
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onChunkLoad(final ChunkLoadEvent event)
 	{
 		final MMWorld world = worlds.get(event.getChunk().getWorld().getName());
 
 		// If the world is not found it must be inactive
 		if (world == null)
 			return;
 
 		// Add the chunk to the world
 		world.addChunk(event.getChunk());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onChunkUnload(final ChunkUnloadEvent event)
 	{
 		if (event.isCancelled())
 			return;
 
 		final MMWorld world = worlds.get(event.getChunk().getWorld().getName());
 
 		// If the world is not found it must be inactive
 		if (world == null)
 			return;
 
 		// Remove the chunk from the world
 		world.removeChunk(event.getChunk());
 	}
 }
