 package com.ftwinston.KillerMinecraft;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Random;
 
 import org.bukkit.Difficulty;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.World.Environment;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.generator.BlockPopulator;
 
 import com.ftwinston.KillerMinecraft.Game.GameState;
 
 class WorldManager
 {
 	public static WorldManager instance;
 	private KillerMinecraft plugin;
 	public int chunkBuilderTaskID = -1;
 	
 	public WorldManager(KillerMinecraft killer)
 	{
 		plugin = killer;
 		instance = this;
 		
 		seedGen = new Random();
 		plugin.craftBukkit.bindRegionFiles();
 	}
 	
 	public void onDisable()
 	{
 		plugin.craftBukkit.unbindRegionFiles();
 	}
 	
 	Random seedGen;
 	
 	public void hijackDefaultWorld(String name)
 	{
 		// in the already-loaded server configuration, create/update an entry specifying the generator to be used for the default world
 		YamlConfiguration configuration = plugin.craftBukkit.getBukkitConfiguration();
 		
 		ConfigurationSection section = configuration.getConfigurationSection("worlds");
 		if ( section == null )
 			section = configuration.createSection("worlds");
 		
 		ConfigurationSection worldSection = section.getConfigurationSection(name);
 		if ( worldSection == null )
 			worldSection = section.createSection(name);
 		
		worldSection.set("generator", "Killer Minecraft");
 		
 		// whatever the name of the staging world, it should be the only world on the server. So change the level-name to match that, temporarily, and don't let it get a nether/end.
 		final String prevLevelName = plugin.craftBukkit.getServerProperty("level-name", "world");
 		final String prevAllowNether = plugin.craftBukkit.getServerProperty("allow-nether", "true");
 		final boolean prevAllowEnd = configuration.getBoolean("settings.allow-end", true);
 		
 		plugin.craftBukkit.setServerProperty("level-name", name);
 		plugin.craftBukkit.setServerProperty("allow-nether", "false");			
 		configuration.set("settings.allow-end", false);
 		
 		// restore server settings, once it's finished generating
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			@Override
 			public void run() {
 				plugin.craftBukkit.setServerProperty("level-name", prevLevelName);
 				plugin.craftBukkit.setServerProperty("allow-nether", prevAllowNether);
 				
 				YamlConfiguration configuration = plugin.craftBukkit.getBukkitConfiguration();
 				configuration.set("settings.allow-end", prevAllowEnd);
 				
 				plugin.craftBukkit.saveServerPropertiesFile();
 				plugin.craftBukkit.saveBukkitConfiguration(configuration);
 			}
 		});
 	}
 	
 	public void createStagingWorld(final String name) 
 	{
 		plugin.stagingWorld = plugin.getServer().getWorld(name);
 		
 		// staging world must not be null when the init event is called, so we don't call this again
 		if ( plugin.stagingWorldIsServerDefault )
 			plugin.stagingWorld = plugin.getServer().getWorlds().get(0);
 
 		StagingWorldGenerator generator = new StagingWorldGenerator();
 		
 		plugin.stagingWorld = new WorldCreator(name)
 			.generator(generator)
 			//.environment(Environment.THE_END)
 			.createWorld();
 		
 		plugin.stagingWorld.setSpawnFlags(false, false);
 		plugin.stagingWorld.setDifficulty(Difficulty.PEACEFUL);
 		plugin.stagingWorld.setPVP(false);
 		plugin.stagingWorld.setAutoSave(false); // don't save changes to the staging world
 	}
 	
 	public static boolean isLocationInRange(Location test, Location min, Location max)
 	{
 		int x = test.getBlockX(), y = test.getBlockY(), z = test.getBlockZ();
 		return x >= min.getBlockX() && x <= max.getBlockX()
 			&& y >= min.getBlockY() && y <= max.getBlockY()
 			&& z >= min.getBlockZ() && z <= max.getBlockZ();
 	}
 	
 	public boolean isProtectedLocation(Location loc, Player player)
 	{
 		return isProtectedLocation(plugin.getGameForWorld(loc.getWorld()), loc, player);
 	}
 	
 	public boolean isProtectedLocation(Game game, Location loc, Player player)
 	{
 		if ( loc.getWorld() == plugin.stagingWorld )
 			return isLocationInRange(loc, Settings.protectionMin, Settings.protectionMax);
 		
 		if ( game != null )
 			return game.getGameMode().isLocationProtected(loc, player);
 		else
 			return false;
 	}
 
 	Random random = new Random();
 	public Location getRandomLocation(Location rangeMin, Location rangeMax)
 	{
 		return new Location(rangeMin.getWorld(),
 				rangeMin.getX() + (rangeMax.getX() - rangeMin.getX()) * random.nextDouble(),
 				rangeMin.getY() + (rangeMax.getY() - rangeMin.getY()) * random.nextDouble(),
 				rangeMin.getZ() + (rangeMax.getZ() - rangeMin.getZ()) * random.nextDouble(),
 				random.nextFloat() * 360.0f, 0f);
 	}
 
 	public Location getStagingAreaSpawnPoint() {
 		return getRandomLocation(Settings.spawnCoordMin, Settings.spawnCoordMax);
 	}
 	
 	public void deleteWorldFolders(final String prefix)
 	{
 		File worldFolder = plugin.getServer().getWorldContainer();
 		
 		String[] killerFolders = worldFolder.list(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.startsWith(prefix) && dir.isDirectory();
 			}
 		});
 		
 		for ( String worldName : killerFolders )
 			deleteWorld(worldName);
 	}
 	
 	public boolean deleteWorld(String worldName)
 	{
 		plugin.craftBukkit.clearWorldReference(worldName);
 		boolean allGood = true;
 		
 		File folder = new File(plugin.getServer().getWorldContainer() + File.separator + worldName);
 		try
 		{
 			if ( folder.exists() && !delete(folder) )
 				allGood = false;
 		}
 		catch ( Exception e )
 		{
 			plugin.log.info("An error occurred when deleting the " + worldName + " world: " + e.getMessage());
 		}
 		
 		return allGood;
 	}
 		
 	public void deleteKillerWorlds(Game game, Runnable runWhenDone)
 	{
 		plugin.log.info("Clearing out old worlds...");
 		deleteWorlds(runWhenDone, game.getWorlds().toArray(new World[0]));
 		game.getWorlds().clear();
 	}
 	
 	public void deleteWorlds(Runnable runWhenDone, World... worlds)
 	{
 		String[] worldNames = new String[worlds.length];
 		for ( int i=0; i<worlds.length; i++ )
 		{
 			worldNames[i] = worlds[i].getName();
 			for ( Player player : worlds[i].getPlayers() )
 				plugin.playerManager.putPlayerInStagingWorld(player);
 			plugin.craftBukkit.forceUnloadWorld(worlds[i]);
 		}
 		
 		// now we want to try to delete the world folders
 		plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new WorldDeleter(runWhenDone, worldNames), 80);
 	}
 	
 	private class WorldDeleter implements Runnable
 	{
 		Runnable runWhenDone;
 		String[] worlds;
 		
 		static final long retryDelay = 30;
 		static final int maxRetries = 5;
 		int attempt;
 		
 		public WorldDeleter(Runnable runWhenDone, String... names)
 		{
 			attempt = 0;
 			worlds = names;
 			this.runWhenDone = runWhenDone;
 		}
 		
 		public void run()
 		{
 			boolean allGood = true;
 			for ( String world : worlds )
 				allGood = allGood && deleteWorld(world);
 			
 			if ( !allGood )
 				if ( attempt < maxRetries )
 				{
 					plugin.log.info("Retrying world data deletion...");
 					attempt++;
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, retryDelay);
 					return;
 				}
 				else
 					plugin.log.warning("Failed to delete some world information!");
 			
 			if ( runWhenDone != null )
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runWhenDone);
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
 	
 	public void generateWorlds(final Game game, final WorldGenerator generator, final Runnable runWhenDone)
 	{
 		game.getGameMode().broadcastMessage(game.getGameMode().getNumWorlds() == 1 ? "Preparing new world..." : "Preparing new worlds...");
 		
 		Runnable generationComplete = new Runnable() {
 			@Override
 			public void run()
 			{
 				for ( World world : game.getWorlds() )
 				{
 					world.setDifficulty(game.getDifficulty());
 					
 					world.setTicksPerMonsterSpawns(1);
 					world.setMonsterSpawnLimit(game.getGameMode().getMonsterSpawnLimit(game.monsterNumbers));
 					
 					if ( world.getEnvironment() == Environment.NORMAL )
 					{
 						world.setTicksPerAnimalSpawns(400);
 						world.setMonsterSpawnLimit(game.getGameMode().getAnimalSpawnLimit(game.animalNumbers));
 					}
 				}
 				
 				
 				// run whatever task was passed in
 				if ( runWhenDone != null )
 					runWhenDone.run();
 			}
 		};
 	
 		try
 		{
 			generator.createWorlds(game, generationComplete);
 		}
 		catch (Exception ex)
 		{
 			plugin.log.warning("A crash occurred during world generation. Aborting...");
 			game.getGameMode().broadcastMessage("An error occurred during world generation.\nPlease try again...");
 			
 			game.setGameState(GameState.worldDeletion);
 		}
 	}
 
 	// this is a clone of CraftServer.createWorld, amended to accept extra block populators
 	// it also spreads chunk creation across multiple ticks, instead of locking up the server while it generates 
     public World createWorld(WorldConfig config, final Runnable runWhenDone)
     {
     	World world = plugin.craftBukkit.createWorld(config.getWorldType(), config.getEnvironment(), config.getName(), config.getSeed(), config.getGenerator(), config.getGeneratorSettings(), config.getGenerateStructures());
     	
         for ( BlockPopulator populator : config.getExtraPopulators() )
         	world.getPopulators().add(populator);
 
         Server server = plugin.getServer();
         		
         server.getPluginManager().callEvent(new WorldInitEvent(world));
         System.out.print("Preparing start region for world: " + world.getName() + " (Seed: " + config.getSeed() + ")");
         
         int worldNumber = config.getGame().getWorlds().size(), numberOfWorlds = config.getGame().getGameMode().getWorldsToGenerate().length; 
         config.getGame().drawProgressBar((float)worldNumber / (float)numberOfWorlds);
         ChunkBuilder cb = new ChunkBuilder(config.getGame(), 12, server, world, worldNumber, numberOfWorlds, runWhenDone);
         chunkBuilderTaskID = server.getScheduler().scheduleSyncRepeatingTask(plugin, cb, 1L, 1L);
     	return world;
     }
     
     class ChunkBuilder implements Runnable
     {
     	public ChunkBuilder(Game game, int numChunksFromSpawn, Server server, World world, int worldNumber, int numberOfWorlds, Runnable runWhenDone)
     	{
     		this.numChunksFromSpawn = numChunksFromSpawn;
     		sideLength = numChunksFromSpawn * 2 + 1;
     		numSteps = sideLength * sideLength;
     		
     		this.game = game;
     		this.worldNumber = worldNumber;
     		this.numberOfWorlds = numberOfWorlds;
     		this.server = server;
     		this.world = world;
     		this.runWhenDone = runWhenDone;
     		
     		Location spawnPos = world.getSpawnLocation();
         	spawnX = spawnPos.getBlockX() >> 4;
         	spawnZ = spawnPos.getBlockZ() >> 4;
     	}
     	
     	Game game;
     	int numChunksFromSpawn, stepNum = 0, sideLength, numSteps, spawnX, spawnZ;
         long reportTime = System.currentTimeMillis();
         int worldNumber, numberOfWorlds;
         Server server;
         World world;
         Runnable runWhenDone;
         static final int chunksPerTick = 3; // how many chunks to generate each tick? 
     	
     	public void run()
     	{
             long time = System.currentTimeMillis();
 
             if (time < reportTime) {
                 reportTime = time;
             }
 
             if (time > reportTime + 500L)
             {
             	float fraction = (float)stepNum/numSteps;
             	if ( numberOfWorlds > 1 )
             	{
             		fraction /= (float)numberOfWorlds;
             		fraction += (float)worldNumber/(float)numberOfWorlds;
             	}
             	game.drawProgressBar(fraction);
                 reportTime = time;
             }
 
             for ( int i=0; i<chunksPerTick; i++ )
             {
             	int offsetX = stepNum / sideLength - numChunksFromSpawn;
             	int offsetZ = stepNum % sideLength - numChunksFromSpawn;
 
             	stepNum++;
             	
             	world.loadChunk(spawnX + offsetX, spawnZ + offsetZ);
 
             	if ( stepNum >= numSteps )
             	{
             		server.getPluginManager().callEvent(new WorldLoadEvent(world));
             		server.getScheduler().cancelTask(chunkBuilderTaskID);
             		chunkBuilderTaskID = -1;
             		server.getScheduler().scheduleSyncDelayedTask(plugin, runWhenDone);
             		
             		System.out.println("Finished generating world: " + world.getName());
             		return;
             	}
             }
         }
     }
 }
