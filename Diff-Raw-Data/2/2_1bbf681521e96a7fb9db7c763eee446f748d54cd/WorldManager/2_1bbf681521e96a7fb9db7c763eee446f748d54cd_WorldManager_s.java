 package com.ftwinston.Killer;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.lang.ref.SoftReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.RegionFile;
 import net.minecraft.server.WorldServer;
 import net.minecraft.server.WorldType;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.BlockPopulator;
 
 public class WorldManager
 {
 		public static WorldManager instance;
 		
 		private Killer plugin;
 		public WorldManager(Killer killer, String mainWorldName, String holdingWorldName)
 		{
 			plugin = killer;
 			instance = this;
 			this.mainWorldName = mainWorldName;
 			
 			seedGen = new Random();
 			bindRegionFiles();
 			serverFolder = plugin.getServer().getWorldContainer();
 			holdingWorld = createHoldingWorld(holdingWorldName);
 
 			// try to find the main world, based on the config-provided name
 			mainWorld = getWorld(mainWorldName, Environment.NORMAL, true);
 			netherWorld = getWorld(mainWorldName + "_nether", Environment.NETHER, true);
 			endWorld = getWorld(mainWorldName + "_the_end", Environment.THE_END, false);
 		}
 		
 		public void onDisable()
 		{
 			regionfiles = null;
 			rafField = null;
 			serverFolder = null;
 		}
 		
 		private String mainWorldName;
 		static File serverFolder;
 		Random seedGen;
 		
 		@SuppressWarnings("rawtypes")
 		private static HashMap regionfiles;
 		private static Field rafField;
 		
 		public World mainWorld;
 		public World holdingWorld;
 		public World netherWorld;
 		public World endWorld;
 		
 		private World getWorld(String name, Environment type, boolean getAnyOnFailure)
 		{
 			List<World> worlds = plugin.getServer().getWorlds();
 			for ( World world : worlds )
 				if ( world.getName().equals(name) )
 					return world;
 			
 			if ( !getAnyOnFailure )
 				return null;
 
 			// couldn't find world name specified, so get the first world of the right type
 			for ( World world : worlds )
 				if ( world.getEnvironment() == type )
 				{
 					plugin.log.warning("Couldn't find \"" + name + "\" world, using " + world.getName() + " instead");
 					return world;
 				}
 		
 			// still couldn't find something suitable...
 			plugin.log.warning("Couldn't find \"" + name + "\" world, using " + worlds.get(0).getName() + " instead");
 			return worlds.get(0);
 		}
 		
 		private MinecraftServer getMinecraftServer()
 		{
 			try
 			{
 				CraftServer server = (CraftServer)plugin.getServer();
 				Field f = server.getClass().getDeclaredField("console");
 				f.setAccessible(true);
 				MinecraftServer console = (MinecraftServer)f.get(server);
 				f.setAccessible(false);
 				return console;}
 			catch ( IllegalAccessException ex )
 			{
 			}
 			catch  ( NoSuchFieldException ex )
 			{
 			}
 			
 			return null;
 		}
 		
 		static final int plinthPeakHeight = 76, spaceBetweenPlinthAndGlowstone = 4;
 		static final int plinthSpawnOffsetX = 20, plinthSpawnOffsetZ = 0;
 		public Location createPlinth(World world)
 		{
 			Location spawnPoint = world.getSpawnLocation();
 			int x = spawnPoint.getBlockX() + plinthSpawnOffsetX;
 			int z = spawnPoint.getBlockZ() + plinthSpawnOffsetZ;
 			
 			// a 3x3 column from bedrock to the plinth height
 			for ( int y = 0; y < plinthPeakHeight; y++ )
 				for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(Material.BEDROCK);
 					}
 			
 			// with one block sticking up from it
 			int y = plinthPeakHeight;
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(ix == x && iz == z ? Material.BEDROCK : Material.AIR);
 					}
 			
 			// that has a pressure plate on it
 			y = plinthPeakHeight + 1;
 			Location pressurePlateLocation = new Location(world, x, y, z);
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(ix == x && iz == z ? Material.STONE_PLATE : Material.AIR);
 					}
 					
 			// then a space
 			for ( y = plinthPeakHeight + 2; y <= plinthPeakHeight + spaceBetweenPlinthAndGlowstone; y++ )
 				for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(Material.AIR);
 					}
 			
 			// and then a 1x1 pillar of glowstone, up to max height
 			for ( y = plinthPeakHeight + spaceBetweenPlinthAndGlowstone + 1; y < world.getMaxHeight(); y++ )
 				for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(ix == x && iz == z ? Material.GLOWSTONE : Material.AIR);
 					}
 			
 			return pressurePlateLocation;
 		}
 
 		// for both the CraftServer and the MinecraftServer, remove the holding world from the front of their world lists, and add it back onto the end 
 		private void sortWorldOrder()
 		{
 			try
 			{
 				Field f = plugin.getServer().getClass().getDeclaredField("worlds");
 				f.setAccessible(true);
 				@SuppressWarnings("unchecked")
 				Map<String, World> worlds = (Map<String, World>)f.get(plugin.getServer());
 				f.setAccessible(false);
 				
 				worlds.remove(holdingWorld.getName());
 				worlds.put(holdingWorld.getName(), holdingWorld);
 
 				/*plugin.log.info("CraftServer worlds:");
 				for ( Map.Entry<String, World> map : worlds.entrySet() )
 					plugin.log.info(" " + map.getKey() + " : " + map.getValue().getName());
 				
 						plugin.log.info("");
 						plugin.log.info("accessible format:");
 				for ( World world : plugin.getServer().getWorlds() )
 					plugin.log.info(" " + world.getName());*/
 			}
 			catch ( IllegalAccessException ex )
 			{
 				plugin.log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 			}
 			catch  ( NoSuchFieldException ex )
 			{
 				plugin.log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 			}
 			
 			WorldServer holdingWorldServer = ((CraftWorld)holdingWorld).getHandle();
 			
 			MinecraftServer ms = getMinecraftServer();
 			ms.worlds.remove(ms.worlds.indexOf(holdingWorldServer));
 			ms.worlds.add(holdingWorldServer);
 			
 			/*plugin.log.info("");
 			plugin.log.info("MinecraftServer worlds:");
 			for ( WorldServer world : ms.worlds )
 				plugin.log.info(" " + world.dimension);*/
 		}
 		
 		private World createHoldingWorld(String name) 
 		{
 	        World world = plugin.getServer().getWorld(name);
 	        if ( world != null )
 	        {// holding world already existed; delete it, because we might want to change it to correspond to config changes
 	        	
 	        	plugin.log.info("Deleting holding world, cos it already exists...");
 	        	
 	        	
 	        	forceUnloadWorld(world, null);
 	        	world = null;
 	        	try
 	        	{
 	        		Thread.sleep(200);
 	        	}
 	        	catch ( InterruptedException ex )
 	        	{
 	        	}
 	        	
 	        	clearWorldReference(name);
 				
 	    		try
 				{
 	    			delete(new File(serverFolder + File.separator + name));
 				}
 				catch ( Exception e )
 				{
 				}
 	        }
 	        
 	        int maxGameModeOptions = 0;
 	        for ( GameMode mode : GameMode.gameModes.values() )
 	        	if ( mode.getOptions().size() > maxGameModeOptions )
 	        		maxGameModeOptions = mode.getOptions().size();
 	        
 	        WorldCreator wc = new WorldCreator(name);
     		HoldingWorldGenerator gen = new HoldingWorldGenerator(GameMode.gameModes.size(), maxGameModeOptions, new String[] { "Hello", "Goodbye" }, true);
 	        wc.generator(gen);
 			wc.environment(Environment.THE_END);
 			world = CreateWorld(wc, true);
 			wc.generateStructures(true);
 			
 			//HoldingWorldPopulator pop = new HoldingWorldPopulator();
 			/*for ( int x=0; x<=pop.endChunkX; x++ )
 				for ( int z=0; z<=pop.endChunkZ; z++ )
 					pop.populate(world, null, world.getChunkAt(x, z));*/  // fails cos its called for ungenerated chunks ... ?
 			//world.getPopulators().clear();
 			//world.getPopulators().add(pop);
 			
 			world.setSpawnLocation(8, 2, gen.forceStartButtonZ);
 			world.setPVP(false);
 	        world.setAutoSave(false); // don't save changes to the holding world
 	        return world;
 		}
 		
 		public World CreateWorld(WorldCreator wc, boolean loadChunks)
 		{
 			World world = plugin.getServer().createWorld(wc);
 			
 			if (world != null)
 			{
 				if ( loadChunks )
 				{
 					final int keepdimension = 7;
 					int spawnx = world.getSpawnLocation().getBlockX() >> 4;
 				    int spawnz = world.getSpawnLocation().getBlockZ() >> 4;
 					for (int x = -keepdimension; x < keepdimension; x++)
 						for (int z = -keepdimension; z < keepdimension; z++)
 							world.loadChunk(spawnx + x, spawnz + z);
 				}
 				world.setKeepSpawnInMemory(loadChunks);
 				plugin.log.info("World '" + world.getName() + "' created successfully!");
 			}
 			else
 				plugin.log.info("World creation failed!");
 			
 			return world;
 		}
 		
 		public void removeAllItems(World world)
 		{
 			List<Entity> list = world.getEntities();
 			Iterator<Entity> entities = list.iterator();
 			while (entities.hasNext())
 			{
 				Entity entity = entities.next();
 				if (entity instanceof Item)
 					entity.remove();
 			}
 		}
 		
 		@SuppressWarnings("rawtypes")
 		private void bindRegionFiles()
 		{
 	        try
 	        {
 	        	Field a = net.minecraft.server.RegionFileCache.class.getDeclaredField("a");
 	        	a.setAccessible(true);
 				regionfiles = (HashMap) a.get(null);
 				rafField = net.minecraft.server.RegionFile.class.getDeclaredField("c");
 				rafField.setAccessible(true);
 	        	plugin.log.info("Successfully bound variable to region file cache.");
 	        	plugin.log.info("File references to unloaded worlds will be cleared!");
 			}
 	        catch (Throwable t)
 	        {
 	        	plugin.log.warning("Failed to bind to region file cache.");
 	        	plugin.log.warning("Files will stay referenced after being unloaded!");
 				t.printStackTrace();
 			}
 		}
 		
 		@SuppressWarnings("rawtypes")
 		private boolean clearWorldReference(String worldName)
 		{
 			if (regionfiles == null) return false;
 			if (rafField == null) return false;
 			
 			ArrayList<Object> removedKeys = new ArrayList<Object>();
 			try
 			{
 				for (Object o : regionfiles.entrySet())
 				{
 					Map.Entry e = (Map.Entry) o;
 					File f = (File) e.getKey();
 					
 					if (f.toString().startsWith("." + File.separator + worldName))
 					{
 						SoftReference ref = (SoftReference) e.getValue();
 						try
 						{
 							RegionFile file = (RegionFile) ref.get();
 							if (file != null)
 							{
 								RandomAccessFile raf = (RandomAccessFile) rafField.get(file);
 								raf.close();
 								removedKeys.add(f);
 							}
 						}
 						catch (Exception ex)
 						{
 							ex.printStackTrace();
 						}
 					}
 				}
 			}
 			catch (Exception ex)
 			{
 				plugin.log.warning("Exception while removing world reference for '" + worldName + "'!");
 				ex.printStackTrace();
 			}
 			for (Object key : removedKeys)
 				regionfiles.remove(key);
 			
 			return true;
 		}
 		
 		private void forceUnloadWorld(World world, World movePlayersTo)
 		{
 			world.setAutoSave(false);
 			if ( movePlayersTo == null )
 				for ( Player player : world.getPlayers() )
 					player.kickPlayer("World is being regenerated... and you were in it!");
 			else
 			{
 				for ( Player player : world.getPlayers() )
 					plugin.playerManager.putPlayerInWorld(player,  movePlayersTo);
 			}
 			
 			String worldName = world.getName();
 			if ( !plugin.getServer().unloadWorld(world, false) )
				plugin.log.warning("Error unloading world: " + worldName;
 		}
 
 		public void deleteWorlds(final Runnable runWhenDone)
 		{
 			plugin.log.info("Clearing out old worlds...");
 			
 			int i = 0;
 			if ( mainWorld != null )
 				i++;
 			if ( netherWorld != null )
 				i++;
 			if ( endWorld != null )
 				i++;
 			
 			String[] worldNames = new String[i];
 			i=0;
 			
 			if ( mainWorld != null )
 			{
 				forceUnloadWorld(mainWorld, holdingWorld);
 				worldNames[i++] = mainWorld.getName();
 			}
 				
 			if ( netherWorld != null )
 			{
 				forceUnloadWorld(netherWorld, holdingWorld);
 				worldNames[i++] = netherWorld.getName();
 			}
 				
 			if ( endWorld != null )
 			{
 				forceUnloadWorld(endWorld, holdingWorld);
 				worldNames[i++] = endWorld.getName();
 			}
 			
 			// now we want to try to delete the world folders
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new WorldDeleter(worldNames, new Runnable() {
 				public void run()
 				{
 					plugin.log.info("Generating new worlds...");
 					MinecraftServer ms = getMinecraftServer();
 
 					String s = ms.getPropertyManager().getString("level-name", "world");
 					String s2 = ms.getPropertyManager().getString("level-type", "DEFAULT");
 					WorldType worldtype = WorldType.getType(s2);
 
 					if (worldtype == null)
 						worldtype = WorldType.NORMAL;
 					
 					Method a;
 					try
 					{
 						a = MinecraftServer.class.getDeclaredMethod("a", String.class, String.class, long.class, WorldType.class);
 					}
 					catch ( NoSuchMethodException ex )
 					{
 						plugin.log.warning("Unable to trigger default world generation, shutting down");
 						plugin.getServer().shutdown();
 						return;
 					}
 					
 					try
 					{
 						a.setAccessible(true);
 						a.invoke(ms, s, s, seedGen.nextLong(), worldtype);
 						a.setAccessible(false);
 					}
 					catch ( IllegalAccessException ex )
 					{
 						plugin.log.warning("Illegal access: " + ex.getMessage());
 					}
 					catch ( InvocationTargetException ex )
 					{
 						plugin.log.warning("Invocation target exception: " + ex.getMessage());
 					}
 
 					mainWorld = getWorld(mainWorldName, Environment.NORMAL, true);
 					netherWorld = getWorld(mainWorldName + "_nether", Environment.NETHER, true);
 					endWorld = getWorld(mainWorldName + "_the_end", Environment.THE_END, false);
 					
 					// now we want to ensure that the holding world gets put on the end of the worlds list, instead of staying at the beginning
 					// also ensure that the other worlds on the list are in the right order
 					sortWorldOrder();
 
 					// run whatever task was passed in, before putting players back in the main world
 					if ( runWhenDone != null )
 						runWhenDone.run();
 					
 					// move ALL players back into the main world
 					for ( Player player : plugin.getOnlinePlayers() )
 					{
 						plugin.playerManager.resetPlayer(player, true);
 						plugin.playerManager.putPlayerInWorld(player, mainWorld);
 					}
 				}
 
 			}), 80);
 		}
 
 		private class WorldDeleter implements Runnable
 	    {
 	    	String[] worlds;
 	    	Runnable runWhenDone;
 	    	
 	    	static final long retryDelay = 30;
 	    	static final int maxRetries = 5;
 	    	int attempt;
 	    	
 	    	public WorldDeleter(String[] worlds, Runnable runWhenDone)
 	    	{
 	    		attempt = 0;
 	    		this.worlds = worlds;
 	    		this.runWhenDone = runWhenDone;
     		}
 	    	
 	    	public void run()
 	    	{
 	    		boolean allGood = true;
     			for ( String worldName : worlds )
     			{
     				clearWorldReference(worldName);
     				
 		    		try
 					{
 		    			if ( !delete(new File(serverFolder + File.separator + worldName)) )
 		    				allGood = false;
 					}
 					catch ( Exception e )
 					{
 						plugin.log.info("An error occurred when deleting the " + worldName + " world: " + e.getMessage());
 					}
     			}
 	    			
 	    		if ( !allGood )
 		    		if ( attempt < maxRetries )
 	    			{
 	    				plugin.log.info("Retrying world data deletion...");
 	    				attempt++;
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, retryDelay);
 						return;
 	    			}
 		    		else
 		    			plugin.log.warning("Failed to delete some world information. Continuing...");
 	    		
 	    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runWhenDone, 20);
 	    	}
 	    }
 		
 		private boolean delete(File folder)
 		{
 			if ( !folder.exists() )
 				return true;
 			boolean retVal = true;
 			if (folder.isDirectory())
 				for (File f : folder.listFiles())
 					if (!delete(f))
 					{
 						retVal = false;
 						//plugin.log.warning("Failed to delete file: " + f.getName());
 					}
 			return folder.delete() && retVal;
 		}
 		
 		public class HoldingWorldPopulator extends org.bukkit.generator.BlockPopulator
 		{
 			HoldingWorldGenerator gen;
 			
 			public HoldingWorldPopulator(HoldingWorldGenerator gen)
 			{
 				this.gen = gen;
 			}
 			
 			public Block getBlockAbs(Chunk chunk, int absX, int y, int absZ)
 			{
 				int chunkX = absX - chunk.getX() * 16, chunkZ = absZ - chunk.getZ() * 16;
 				
 				if ( chunkX >= 0 && chunkX < 16 && chunkZ >= 0 && chunkZ < 16 )
 					return chunk.getBlock(chunkX, y, chunkZ);
 				return null;
 			}
 			
 			public void populate(World world, Random random, Chunk chunk)
 			{
 				if ( chunk.getX() < 0 || chunk.getZ() < 0 || chunk.getX() > gen.endX / 16 || chunk.getZ() > gen.endZ / 16 )
 					return;
 				
 				Material mainFloor = Material.SMOOTH_BRICK;
 				Material ceiling = Material.GLOWSTONE;
 				Material walls = Material.SMOOTH_BRICK;
 				Material closedOffWall = Material.IRON_FENCE;
 				
 				Material slab = Material.STEP;
 				Material loweredFloor = Material.NETHERRACK;
 				
 				Material lamp = Material.REDSTONE_LAMP_OFF;
 				Material button = Material.STONE_BUTTON;
 				Material sign = Material.WALL_SIGN;
 				Block b;
 			
 				//
 				// the world selection area
 				//
 				
 				// floor
 				for ( int x = 4; x < gen.worldEndX; x++ )
 					for ( int z = 0; z < 5; z++ )
 					{
 						b = getBlockAbs(chunk, x, 1, z);
 						if ( b != null )
 							b.setType(mainFloor);
 					}
 
 				// ceiling
 				for ( int x = 4; x < gen.worldEndX; x++ )
 					for ( int z = 0; z < 5; z++ )
 					{
 						b = getBlockAbs(chunk, x, 7, z);
 						if ( b != null )
 							b.setType(ceiling);
 					}
 				
 				// west wall
 				for ( int z = 1; z < 5; z++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, 4, y, z);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// east wall
 				for ( int z = 1; z < 5; z++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, gen.worldEndX, y, z);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// south wall, if should be closed off
 				if ( gen.worldSelectionClosedOff )
 					for ( int x = 5; x < gen.worldEndX; x++ )
 						for ( int y = 2; y < 7; y++ )
 						{
 							b = getBlockAbs(chunk, x, y, 5);
 							if ( b != null )
 								b.setType(closedOffWall);
 						}
 				
 				// north wall
 				for ( int x = 4; x < gen.worldEndX; x++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, x, y, 0);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// now all the buttons/signs etc IN the north wall
 				int num = 0;
 				int worldX = 7;
 				while ( worldX < gen.worldEndX - 2 && num < gen.numWorldOptions )
 				{
 					b = getBlockAbs(chunk, worldX, 2, 1);
 					if ( b != null )
 					{
 						b.setType(button);
 						b.setData((byte)0x3);
 					}
 					
 					b = getBlockAbs(chunk, worldX, 3, 1);
 					if ( b != null )
 					{
 						b.setType(sign);
 						b.setData((byte)0x3);
 						Sign s = (Sign)b.getState();
 						s.setLine(1, "fWorld:");
 						
 						if ( gen.allowRandomWorlds && num == 0 )
 							s.setLine(2, "fDefault Random");
 						else
 						{
 							String name = gen.allowRandomWorlds ? gen.customWorldNames[num - 1] : gen.customWorldNames[num - 1];
 							if ( name.length() > 12 )
 							{
 								String[] words = name.split(" ");
 								s.setLine(2, "f" + words[0]);
 								if ( words.length > 1)
 								{
 									s.setLine(3, "f" + words[1]);
 									if ( words.length > 2)
 										s.setLine(4, "f" + words[2]);
 								}
 							}
 							else
 								s.setLine(2, "f" + name);
 						}
 						s.update();
 					}
 					
 					b = getBlockAbs(chunk, worldX, 3, 0);
 					if ( b != null )
 						b.setType(lamp);
 					
 					worldX += 2;
 					num ++;
 				}
 				
 				
 				//
 				// the game mode selection area
 				//
 				
 				// floor
 				for ( int x = 0; x < 5; x++ )
 					for ( int z = 4; z < gen.gameModeEndZ; z++ )
 					{
 						b = getBlockAbs(chunk, x, 1, z);
 						if ( b != null )
 							b.setType(mainFloor);
 					}
 				
 				// ceiling
 				for ( int x = 0; x < 5; x++ )
 					for ( int z = 4; z < gen.gameModeEndZ; z++ )
 					{
 						b = getBlockAbs(chunk, x, 7, z);
 						if ( b != null )
 							b.setType(ceiling);
 					}
 				
 				// north wall
 				for ( int x = 1; x < 5; x++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, x, y, 4);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// south wall, if needed
 				for ( int x = 1; x < 5; x++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, x, y, gen.gameModeEndZ);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// east wall, if should be closed off
 				if ( gen.gameModeSelectionClosedOff )
 					for ( int z = 4; z < gen.gameModeEndZ; z++ )
 						for ( int y = 2; y < 7; y++ )
 						{
 							b = getBlockAbs(chunk, 5, y, z);
 							if ( b != null )
 								b.setType(closedOffWall);
 						}
 				
 				// west wall
 				for ( int z = 4; z < gen.gameModeEndZ; z++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, 0, y, z);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// now all the buttons/signs etc IN the west wall
 				num = 0;
 				int modeZ = 7;
 				Object[] modes = GameMode.gameModes.values().toArray();
 				
 				while ( modeZ < gen.gameModeEndZ - 2 && num < gen.numGameModes )
 				{
 					b = getBlockAbs(chunk, 1, 2, modeZ);
 					if ( b != null )
 					{
 						b.setType(button);
 						b.setData((byte)0x1);
 					}
 					
 					b = getBlockAbs(chunk, 1, 3, modeZ);
 					if ( b != null )
 					{
 						b.setType(sign);
 						b.setData((byte)0x5);
 						Sign s = (Sign)b.getState();
 						s.setLine(1, "fGame mode:");
 						
 						String name = ((GameMode)modes[num]).getName();
 						if ( name.length() > 12 )
 						{
 							String[] words = name.split(" ");
 							s.setLine(2, "f" + words[0]);
 							if ( words.length > 1)
 							{
 								s.setLine(3, "f" + words[1]);
 								if ( words.length > 2)
 									s.setLine(4, "f" + words[2]);
 							}
 						}
 						else
 							s.setLine(2, "f" + name);
 						s.update();
 					}
 					
 					b = getBlockAbs(chunk, 0, 3, modeZ);
 					if ( b != null )
 						b.setType(lamp);
 					
 					modeZ += 2;
 					num ++;
 				}
 				
 				
 				//
 				// the main room
 				//
 				
 				// ceiling
 				for ( int x=5; x<=gen.endX; x++ )
 					if ( x > 5 && x < 10  )
 						continue; // leave a big hole above here, so that players won't spawn on the roof. Ah, minecraft. Joy.
 					else
 						for ( int z=5; z<gen.endZ - 4; z++ )
 						{
 							b = getBlockAbs(chunk, x, 7, z);
 							if ( b != null )
 								b.setType(ceiling);
 						}
 				
 				// high-level floor on north
 				for ( int x=5; x<gen.endX; x++ )
 					for ( int z=5; z<7; z++ )
 					{
 						b = getBlockAbs(chunk, x, 1, z);
 						if ( b != null )
 							b.setType(mainFloor);
 					}
 				
 				// high-level floor on west
 				for ( int x=5; x<10; x++ )
 					for ( int z=7; z<gen.endZ - 6; z++ )
 					{
 						b = getBlockAbs(chunk, x, 1, z);
 						if ( b != null )
 							b.setType(mainFloor);
 					}
 				
 				// high-level floor on south
 				for ( int x=5; x<gen.endX; x++ )
 					for ( int z=gen.endZ - 6; z<gen.endZ - 4; z++ )
 					{
 						b = getBlockAbs(chunk, x, 1, z);
 						if ( b != null )
 							b.setType(mainFloor);
 					}
 				
 				// half steps on north
 				for ( int x=10; x<gen.endX; x++ )
 				{
 					b = getBlockAbs(chunk, x, 1, 7);
 					if ( b != null )
 					{
 						b.setType(slab);
 						b.setData((byte)0x5);
 					}
 				}
 				
 				// half steps on west
 				for ( int z=8; z<gen.endZ - 7; z++ )
 				{
 					b = getBlockAbs(chunk, 10, 1, z);
 					if ( b != null )
 					{
 						b.setType(slab);
 						b.setData((byte)0x5);
 					}
 				}
 				
 				// half-steps on south
 				for ( int x=10; x<gen.endX; x++ )
 				{
 					b = getBlockAbs(chunk, x, 1, gen.endZ - 7);
 					if ( b != null )
 					{
 						b.setType(slab);
 						b.setData((byte)0x5);
 					}
 				}
 				
 				// lowered floor
 				for ( int x = 11; x <= gen.endX; x ++ )
 					for ( int z = 8; z < gen.endZ - 6; z++ )
 					{
 						b = getBlockAbs(chunk, x, 0, z);
 						if ( b != null )
 							b.setType(loweredFloor);
 					}
 				
 				// any extra wall required on the north
 				for ( int x = gen.worldEndX + 1; x < gen.endX; x ++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, x, y, 4);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// any extra wall required on the south
 				for ( int x = gen.optionsEndX + 1; x < gen.endX; x ++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, x, y, gen.endZ - 4);
 						if ( b != null )
 							b.setType(walls);
 					}
 			
 				// east wall
 				for ( int z = 4; z < gen.endZ - 4; z++ )
 					for ( int y = 1; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, gen.endX, y, z);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				//
 				// the game mode option selection area
 				//
 				
 				// floor
 				for ( int x = 4; x < gen.optionsEndX; x++ )
 					for ( int z = gen.endZ - 4; z <= gen.endZ; z++ )
 					{
 						b = getBlockAbs(chunk, x, 1, z);
 						if ( b != null )
 							b.setType(mainFloor);
 					}
 				
 				// ceiling
 				for ( int x = 4; x < gen.optionsEndX; x++ )
 					for ( int z = gen.endZ - 4; z <= gen.endZ; z++ )
 					{
 						b = getBlockAbs(chunk, x, 7, z);
 						if ( b != null )
 							b.setType(ceiling);
 					}
 				
 				// west wall
 				for ( int z = gen.endZ - 4; z <= gen.endZ; z++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, 4, y, z);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// east wall
 				for ( int z = gen.endZ - 4; z <= gen.endZ; z++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, gen.optionsEndX, y, z);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// north wall, if should be closed off
 				if ( gen.gameModeOptionsClosedOff )
 					for ( int x = 5; x < gen.optionsEndX; x++ )
 						for ( int y = 2; y < 7; y++ )
 						{
 							b = getBlockAbs(chunk, x, y, gen.endZ - 4);
 							if ( b != null )
 								b.setType(closedOffWall);
 						}
 				
 				// south wall
 				for ( int x = 4; x < gen.optionsEndX; x++ )
 					for ( int y = 2; y < 7; y++ )
 					{
 						b = getBlockAbs(chunk, x, y, gen.endZ);
 						if ( b != null )
 							b.setType(walls);
 					}
 				
 				// now all the buttons/signs etc IN the south wall
 				num = 0;
 				int optionX = 7;
 				while ( optionX < gen.optionsEndX - 2 && num < gen.maxGameModeOptions )
 				{
 					b = getBlockAbs(chunk, optionX, 2, gen.endZ - 1);
 					if ( b != null )
 					{
 						b.setType(button);
 						b.setData((byte)0x4);
 					}
 					
 					b = getBlockAbs(chunk, optionX, 3, gen.endZ - 1);
 					if ( b != null )
 					{
 						b.setType(sign);
 						b.setData((byte)0x2);
 						Sign s = (Sign)b.getState();
 						s.setLine(1, "fOption:");
 						s.setLine(2, "f???");
 						s.update();
 					}
 					
 					b = getBlockAbs(chunk, optionX, 3, gen.endZ);
 					if ( b != null )
 						b.setType(lamp);
 					
 					worldX += 2;
 					num ++;
 				}
 			}
 		}
 		
 		public class HoldingWorldGenerator extends org.bukkit.generator.ChunkGenerator
 		{
 			private final int numRandomWorldOptions = 1;
 			
 			private int numGameModes, maxGameModeOptions, numWorldOptions;
 			private boolean allowRandomWorlds;
 			private String[] customWorldNames;
 			
 			// these are the max extend of the whole world. coords are WITHIN the end chunk
 			int endX, endZ;
 			
 			// these are the max X of the end wall of each. coords are WITHIN the end chunk
 			int worldEndX, optionsEndX, gameModeEndZ;
 			int forceStartButtonX, forceStartButtonZ;
 			
 			boolean gameModeSelectionClosedOff, worldSelectionClosedOff, gameModeOptionsClosedOff;
 			
 			public HoldingWorldGenerator(int numGameModes, int maxGameModeOptions, String[] customWorldNames, boolean allowRandomWorldGeneration)
 			{
 				this.numGameModes = numGameModes;
 				this.maxGameModeOptions = maxGameModeOptions;
 				this.customWorldNames = customWorldNames;
 				this.allowRandomWorlds = allowRandomWorldGeneration;
 				
 				if ( allowRandomWorldGeneration )
 					numWorldOptions = customWorldNames.length + numRandomWorldOptions;
 				else
 					numWorldOptions = customWorldNames.length;
 				// if random world generation is disabled, and no custom world names are provided ... what then?
 				
 				
 				// now set up helper values for where the various "extensible" things end
 				endX = Math.max(Math.max(numWorldOptions, maxGameModeOptions) * 2 + 8, 20);
 				endZ = Math.max(numGameModes * 2 + 12, 22);
 								
 				forceStartButtonX = endX - 1;
 				forceStartButtonZ = endZ / 2;
 				
 				worldEndX = numWorldOptions * 2 + 8;
 				optionsEndX = maxGameModeOptions == 0 ? 4 : maxGameModeOptions * 2 + 8;
 				gameModeEndZ = numGameModes * 2 + 8;
 				
 				// todo: decide these, based on config or whatever
 				gameModeSelectionClosedOff = numGameModes < 2 ;
 				worldSelectionClosedOff = numWorldOptions < 2;
 				gameModeOptionsClosedOff = maxGameModeOptions < 2;
 			}
 			
 			@Override
 		    public List<BlockPopulator> getDefaultPopulators(World world) {
 		        return Arrays.asList((BlockPopulator)new HoldingWorldPopulator(this));
 		    }
 			
 		    @Override
 		    public boolean canSpawn(World world, int x, int z) {
 		        return x >= 0 && z >= 0 && x <= endX / 16 && z <= endZ / 16;
 		    }
 		    
 			public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
 			{
 				return new byte[1][];
 			}
 			
 			public Location getFixedSpawnLocation(World world, Random random)
 			{
 				Location loc = new Location(world, 8, 2, forceStartButtonZ - 2 + random.nextDouble() * 4);
 				loc.setYaw(0); // if 0 actually works, this isn't needed. But we want them to face -x, at any rate
 				return loc;
 			}
 		}
 }
