 package btwmods;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityTrackerEntry;
 import net.minecraft.src.NextTickListEntry;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import btwmods.measure.Average;
 import btwmods.network.NetworkType;
 import btwmods.stats.measurements.StatBlock;
 import btwmods.stats.measurements.StatChunk;
 import btwmods.stats.measurements.StatNetworkPlayer;
 import btwmods.stats.measurements.StatPositionedClass;
 import btwmods.stats.measurements.StatWorld;
 import btwmods.stats.measurements.StatWorldValue;
 
 public enum Stat {
 	WORLD_TICK,
 	WORLD_LOADED_CHUNKS,
 	WORLD_CACHED_CHUNKS,
 	WORLD_DROPPED_CHUNKS,
 	WORLD_LOADED_ENTITIES,
 	WORLD_LOADED_TILE_ENTITIES,
 	WORLD_TRACKED_ENTITIES,
 	
 	MOB_SPAWNING,
 	BLOCK_UPDATE,
 	BUILD_ACTIVE_CHUNKSET,
 	CHECK_PLAYER_LIGHT,
 	TIME_SYNC,
 	
 	MOOD_LIGHT_AND_WEATHER,
 	LIGHTNING_AND_RAIN,
 
 	ENTITIES_SECTION,
 		WEATHER_EFFECTS,
 		ENTITIES_REMOVE,
 		ENTITIES_REGULAR,
 			ENTITY_UPDATE,
 		ENTITIES_TILE,
 			TILE_ENTITY_UPDATE,
 		ENTITIES_TILEPENDING,
 		
 		UPDATE_TRACKED_ENTITY_PLAYER_LISTS,
 		UPDATE_PLAYER_ENTITIES,
 		UPDATE_TRACKED_ENTITY_PLAYER_LIST,
 	
 	LOAD_CHUNK,
 	LOAD_CHUNK_TIME,
 	SPAWN_LIVING;
 	
 	public boolean enabled = true;
 	public int averageResolution = Average.RESOLUTION;
 	
 	public static long bytesSent = 0;
 	public static long bytesReceived = 0;
 	
 	public void begin(World world) {
 		StatsAPI.begin(new StatWorld(this, world));
 	}
 	
 	public void end() {
 		StatsAPI.end(this);
 	}
 	
 	public static void beginBlockUpdate(World world, Block block, int x, int y, int z) {
 		StatsAPI.begin(new StatBlock(Stat.BLOCK_UPDATE, world, block, x, y, z));
 	}
 
 	public static void beginBlockUpdate(World world, NextTickListEntry blockUpdate) {
 		StatsAPI.begin(new StatBlock(Stat.BLOCK_UPDATE, world, blockUpdate));
 	}
 
 	public static void beginEntityUpdate(World world, Entity entity) {
 		StatsAPI.begin(new StatPositionedClass(world, entity));
 	}
 
 	public static void beginTileEntityUpdate(World world, TileEntity tileEntity) {
 		StatsAPI.begin(new StatPositionedClass(Stat.TILE_ENTITY_UPDATE, world, tileEntity));
 	}
 
 	public static void beginUpdateTrackedEntityPlayerList(World world, EntityTrackerEntry trackerEntry) {
 		StatsAPI.begin(new StatPositionedClass(world, trackerEntry));
 	}
 
 	public static void beginLoadChunk(World world, int chunkX, int chunkY) {
 		StatsAPI.begin(new StatChunk(Stat.LOAD_CHUNK, world, chunkX, chunkY));
 	}
 
 	public static void recordSpawning(World world, int spawned) {
 		StatsAPI.record(new StatWorldValue(Stat.SPAWN_LIVING, world, spawned));
 	}
 	
 	public static void recordNetworkIO(NetworkType type, int bytes) {
 		if (type == NetworkType.RECEIVED)
 			bytesReceived += (long)bytes;
 		else
 			bytesSent += (long)bytes;
 	}
 	
 	public static void recordNetworkIO(NetworkType type, int bytes, String username) {
 		recordNetworkIO(type, bytes);
 		StatsAPI.record(new StatNetworkPlayer(type, username, bytes));
 	}
 }
