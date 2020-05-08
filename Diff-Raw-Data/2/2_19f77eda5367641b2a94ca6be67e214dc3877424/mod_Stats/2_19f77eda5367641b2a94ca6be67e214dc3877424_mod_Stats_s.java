 package btwmod.stats;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 
 import net.minecraft.src.ChunkCoordIntPair;
 import net.minecraft.src.EntityAnimal;
 import net.minecraft.src.EntityGhast;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityMob;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntitySlime;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.Stat;
 import btwmods.StatsAPI;
 import btwmods.Util;
 import btwmods.io.AsynchronousFileWriter;
 import btwmods.io.QueuedWrite;
 import btwmods.io.QueuedWriteString;
 import btwmods.io.Settings;
 import btwmods.measure.Average;
 import btwmods.measure.Measurement;
 import btwmods.stats.IStatsListener;
 import btwmods.stats.StatsEvent;
 import btwmods.stats.data.WorldStats;
 import btwmods.stats.measurements.PlayerPosition;
 import btwmods.stats.measurements.StatPositionedClass;
 
 public class mod_Stats implements IMod, IStatsListener {
 	
     private final static long reportingDelayMin = 50L;
     private final static double nanoScale = 1.0E-6D;
 	
 	private static final Comparator<Double> comparatorDouble = new Comparator<Double>() {
 		@Override
 		public int compare(Double o1, Double o2) {
 			if (o1 < o2)
 				return 1;
 			else if (o1 > o2)
 				return -1;
 			else
 				return 0;
 		}
 	};
 
 	private File publicDirectory = null;
 	private File privateDirectory = null;
     private long reportingDelay = 1000L;
 	
 	private Gson gson;
 	private AsynchronousFileWriter fileWriter;
 	
 	private long lastStatsTime = 0;
 	private int lastTickCounter = -1;
 	private Average ticksPerSecond = new Average(10);
 	
 	private String profile = null;
 	private JsonObject profileJson = null;
 
 	@Override
 	public String getName() {
 		return "Stats";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		reportingDelay = Math.max(reportingDelayMin, settings.getLong("reportingDelay", reportingDelay));
 		
 		if (settings.hasKey("directory")) {
 			publicDirectory = privateDirectory = new File(settings.get("directory"));
 		}
 		
 		if (settings.hasKey("publicDirectory")) {
 			publicDirectory = new File(settings.get("publicDirectory"));
 		}
 		
 		if (publicDirectory == null) {
 			ModLoader.outputError(getName() + "'s publicDirectory setting is not set.", Level.SEVERE);
 			return;
 		}
 		else if (!publicDirectory.isDirectory()) {
 			ModLoader.outputError(getName() + "'s publicDirectory setting does not point to a directory.", Level.SEVERE);
 			return;
 		}
 		
 		if (settings.hasKey("privateDirectory")) {
 			privateDirectory = new File(settings.get("privateDirectory"));
 		}
 		
 		if (privateDirectory == null) {
 			ModLoader.outputError(getName() + "'s privateDirectory setting is not set.", Level.SEVERE);
 			return;
 		}
 		else if (!privateDirectory.isDirectory()) {
 			ModLoader.outputError(getName() + "'s privateDirectory setting does not point to a directory.", Level.SEVERE);
 			return;
 		}
 		
 		if (settings.getBoolean("prettyPrinting", false)) {
 			gson = new GsonBuilder()
 				.setPrettyPrinting()
 				.create();
 		}
 		else {
 			gson = new GsonBuilder()
 				.create();
 		}
 		
 		lastStatsTime = System.currentTimeMillis();
 		fileWriter = new AsynchronousFileWriter(getClass().getSimpleName());
 		
 		StatsAPI.addListener(this);
 	}
 
 	@Override
 	public void unload() throws Exception {
 		StatsAPI.removeListener(this);
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 
 	@Override
 	public void onStats(StatsEvent event) {
 		long currentTime = System.currentTimeMillis();
 		
 		if (lastStatsTime == 0) {
 			lastStatsTime = System.currentTimeMillis();
 			return;
 		}
 		
 		if (currentTime - lastStatsTime <= reportingDelay)
 			return;
 			
 		int numTicks = event.tickCounter - lastTickCounter;
 		long timeElapsed = currentTime - lastStatsTime;
 		ticksPerSecond.record((long)((double)numTicks / (double)timeElapsed * 100000D));
 		
 		if (!event.statsProfile.equals(profile)) {
 			Map<Stat, Boolean> profile = StatsAPI.getStatsProfile(event.statsProfile);
 			profileJson = new JsonObject();
 			for (Stat stat : Stat.values())
 				if (profile.containsKey(stat) ? profile.get(stat) : stat.defaultEnabled)
 					profileJson.addProperty(stat.nameAsCamelCase(), true);
 		}
 		
 		writeBasic(event);
 		writeWorlds(event);
 	
 		lastTickCounter = event.tickCounter;
 		lastStatsTime = System.currentTimeMillis();
 	}
 	
 	private void writeBasic(StatsEvent event) {
 		String execPlaceholder = "__EXECTIME__" + System.nanoTime();
 		long start = System.nanoTime();
 		
 		JsonObject basicStats = new JsonObject();
 		basicStats.addProperty("execTime", execPlaceholder);
 		basicStats.addProperty("tickNumber", event.tickCounter);
 		basicStats.add("tick", averageToJson(event.serverStats.tickTime, nanoScale, false));
 		basicStats.add("tickSec", averageToJson(ticksPerSecond, 0.01D, false));
 		basicStats.add("profile", profileJson);
 		
 		JsonArray worlds = new JsonArray();
 		for (int i = 0, l = event.worldStats.length; i < l; i++) {
 			JsonObject worldStats = new JsonObject();
 			worldStats.add(Stat.WORLD_TICK.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.WORLD_TICK), nanoScale, false));
 
 			worldStats.add(Stat.ENTITIES_REGULAR.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.ENTITIES_REGULAR), nanoScale, false));
			worldStats.add(Stat.ENTITIES_TILE.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.TILE_ENTITY_UPDATE), nanoScale, false));
 			
 			worldStats.add(Stat.ENTITY_UPDATE.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.ENTITY_UPDATE), nanoScale, false));
 			worldStats.add(Stat.TILE_ENTITY_UPDATE.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.TILE_ENTITY_UPDATE), nanoScale, false));
 			worldStats.add(Stat.BLOCK_UPDATE.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.BLOCK_UPDATE), nanoScale, false));
 			
 			worldStats.add(Stat.WORLD_LOADED_ENTITIES.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.WORLD_LOADED_ENTITIES), 1.0D, false));
 			worldStats.add(Stat.WORLD_LOADED_TILE_ENTITIES.nameAsCamelCase(), averageToJson(event.worldStats[i].averages.get(Stat.WORLD_LOADED_TILE_ENTITIES), 1.0D, false));
 			
 			worldStats.add("measurements", averageToJson(event.worldStats[i].measurementsQueued, 1.0D, false));
 			
 			worlds.add(worldStats);
 		}
 		
 		basicStats.add("worlds", worlds);
 		
 		String basicJson = gson.toJson(basicStats);
 		basicJson = basicJson.replace(execPlaceholder, Double.toString(Math.round((System.nanoTime() - start) * 1.0E-6D * 100D) / 100D));
 		
 		fileWriter.queueWrite(new QueuedWriteString(new File(publicDirectory, "basic.txt"), basicJson, QueuedWrite.TYPE.OVERWRITE_SAFE));
 	}
 	
 	private void writeWorlds(StatsEvent event) {
 		String execPlaceholder = "__EXECTIME__" + System.nanoTime();
 		for (int i = 0, l = event.worldStats.length; i < l; i++) {
 			long start = System.nanoTime();
 			JsonObject worldStats = new JsonObject();
 			
 			worldStats.addProperty("execTime", execPlaceholder);
 			worldStats.addProperty("tickNumber", event.tickCounter);
 			worldStats.add("profile", profileJson);
 			
 			JsonObject measurementsByStatJson = new JsonObject();
 			for (Entry<Stat, Average> entry : event.worldStats[i].measurementsQueuedByStat.entrySet()) {
 				measurementsByStatJson.add(entry.getKey().nameAsCamelCase(), averageToJson(entry.getValue(), 1.0D, false));
 			}
 			
 			for (Entry<Stat, Average> entry : event.worldStats[i].averages.entrySet()) {
 				worldStats.add(entry.getKey().nameAsCamelCase(), averageToJson(entry.getValue(), entry.getKey().scale, false));
 			}
 			
 			Map<Class, List<StatPositionedClass>> uniqueEntitiesByClass = uniqueByClass(Stat.ENTITY_UPDATE, event.worldStats[i]);
 			Map<Class, List<StatPositionedClass>> uniqueBlocksByClass = uniqueByClass(Stat.BLOCK_UPDATE, event.worldStats[i]);
 			Map<Class, List<StatPositionedClass>> uniqueTileEntitiesByClass = uniqueByClass(Stat.TILE_ENTITY_UPDATE, event.worldStats[i]);
 			
 			String measurementsByStat = gson.toJson(measurementsByStatJson);
 			String timeByEntity = gson.toJson(timeByClassToJson(Stat.ENTITY_UPDATE, event.worldStats[i], uniqueEntitiesByClass));
 			String timeByTileEntity = gson.toJson(timeByClassToJson(Stat.TILE_ENTITY_UPDATE, event.worldStats[i], uniqueTileEntitiesByClass));
 			String timeByBlock = gson.toJson(timeByClassToJson(Stat.BLOCK_UPDATE, event.worldStats[i], uniqueBlocksByClass));
 			String timeByRegion = gson.toJson(timeByRegionToJson(event.worldStats[i]));
 			String timeByChunk = gson.toJson(timeByChunkToJson(event.worldStats[i]));
 			String entitiesLiving = gson.toJson(uniqueByClassToJson(uniqueEntitiesByClass, true, EntityLiving.class, false));
 			String entitiesNonLiving = gson.toJson(uniqueByClassToJson(uniqueEntitiesByClass, false, EntityLiving.class, true));
 			String players = gson.toJson(playerPositionsToJson(event.worldStats[i]));
 			String worldJson = gson.toJson(worldStats);
 			
 			worldJson = worldJson.replace(execPlaceholder, Util.DECIMAL_FORMAT_3.format((System.nanoTime() - start) * 1.0E-6D));
 			
 			// Measurements by stat.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_measurements.txt"),
 					measurementsByStat,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write time by entity.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_byentity.txt"),
 					timeByEntity,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write time by block.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_byblock.txt"),
 					timeByBlock,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write time by block.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_bytileentity.txt"),
 					timeByTileEntity,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write time by chunk.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_bychunk.txt"),
 					timeByChunk,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write time by region.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_byregion.txt"),
 					timeByRegion,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write unique entities.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_entitiesliving.txt"),
 					entitiesLiving,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write unique entities.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_entities.txt"),
 					entitiesNonLiving,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write player positions.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + "_players.txt"),
 					players,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 			
 			// Write the overall world stats.
 			fileWriter.queueWrite(
 				new QueuedWriteString(
 					new File(privateDirectory, "world" + i + ".txt"),
 					worldJson,
 					QueuedWrite.TYPE.OVERWRITE_SAFE
 				)
 			);
 		}
 	}
 	
 	private static JsonObject playerPositionsToJson(WorldStats worldStats) {
 		JsonObject players = new JsonObject();
 		for (PlayerPosition playerPosition : worldStats.playerPositions) {
 			JsonObject player = new JsonObject();
 			player.addProperty("x", Math.floor(playerPosition.x * 100D) / 100D);
 			player.addProperty("y", Math.floor(playerPosition.y * 100D) / 100D);
 			player.addProperty("z", Math.floor(playerPosition.z * 100D) / 100D);
 			players.add(playerPosition.identifier, player);
 		}
 		return players;
 	}
 	
 	private static Map<Class, List<StatPositionedClass>> uniqueByClass(Stat stat, WorldStats worldStats) {
 		Map<Class, List<StatPositionedClass>> list = new LinkedHashMap<Class, List<StatPositionedClass>>();
 		Set<Integer> ids = new HashSet<Integer>();
 		
 		for (Measurement measurement : worldStats.measurements) {
 			if (measurement.identifier == stat && measurement instanceof StatPositionedClass) {
 				StatPositionedClass entityMeasurement = (StatPositionedClass)measurement;
 				if (!ids.contains(Integer.valueOf(entityMeasurement.id))) {
 					List classList = list.get(entityMeasurement.clazz);
 					if (classList == null)
 						list.put(entityMeasurement.clazz, classList = new ArrayList<StatPositionedClass>());
 					
 					classList.add(entityMeasurement);
 				}
 			}
 		}
 		return list;
 	}
 	
 	private static JsonObject uniqueByClassToJson(Map<Class, List<StatPositionedClass>> uniqueByClass, boolean useGroups, Class classFilter, boolean isExcludeFilter) {
 		JsonObject ret = new JsonObject();
 		
 		JsonObject animals = useGroups ? new JsonObject() : null;
 		JsonObject mobs = useGroups ? new JsonObject() : null;
 		JsonObject players = useGroups ? new JsonObject() : null;
 		JsonObject other = new JsonObject();
 		
 		for (Entry<Class, List<StatPositionedClass>> entry : uniqueByClass.entrySet()) {
 			Class clazz = entry.getKey();
 			
 			if (classFilter == null || (
 					(!isExcludeFilter && classFilter.isAssignableFrom(clazz))
 					|| (isExcludeFilter && !classFilter.isAssignableFrom(clazz))
 				)) {
 				
 				JsonObject classGroup = other;
 				if (useGroups) {
 					if (EntityAnimal.class.isAssignableFrom(clazz)) {
 						classGroup = animals;
 					}
 					else if (EntityMob.class.isAssignableFrom(clazz) || EntityGhast.class.isAssignableFrom(clazz) || EntitySlime.class.isAssignableFrom(clazz)) {
 						classGroup = mobs;
 					}
 					else if (EntityPlayer.class.isAssignableFrom(clazz)) {
 						classGroup = players;
 					}
 				}
 				
 				JsonObject classList = new JsonObject();
 				
 				for (StatPositionedClass entityMeasurement : entry.getValue()) {
 					JsonObject entity = new JsonObject();
 					entity.addProperty("x", Math.floor(entityMeasurement.xDouble * 100D) / 100D);
 					entity.addProperty("y", Math.floor(entityMeasurement.yDouble * 100D) / 100D);
 					entity.addProperty("z", Math.floor(entityMeasurement.zDouble * 100D) / 100D);
 					classList.add(Integer.toString(entityMeasurement.id), entity);
 				}
 				
 				classGroup.add(clazz.getSimpleName(), classList);
 			}
 		}
 		
 		if (useGroups) {
 			ret.add("isAnimal", animals);
 			ret.add("isMonster", mobs);
 			ret.add("isPlayer", players);
 		}
 		
 		ret.add("isOther", other);
 		
 		return ret;
 	}
 	
 	private static JsonArray timeByClassToJson(Stat stat, WorldStats worldStats, Map<Class, List<StatPositionedClass>> uniqueEntitiesByClass) {
 		Map<Double, JsonElement> sorted = new TreeMap<Double, JsonElement>(comparatorDouble);
 		
 		for (Entry<Class, Average> entry : worldStats.timeByClass.get(stat).entrySet()) {
 			JsonObject json = averageToJson(entry.getValue(), stat.scale, false);
 			json.addProperty("name", entry.getKey().getSimpleName());
 			
 			if (uniqueEntitiesByClass != null) {
 				List count = uniqueEntitiesByClass.get(entry.getKey());
 				json.addProperty("count", count == null ? -1 : count.size());
 			}
 			
 			sorted.put(entry.getValue().getAverage(), json);
 		}
 		
 		JsonArray list = new JsonArray();
 		for (Entry<Double, JsonElement> entry : sorted.entrySet()) {
 			list.add(entry.getValue());
 		}
 		
 		return list;
 	}
 	
 	private static JsonObject timeByChunkToJson(WorldStats worldStats) {
 		JsonObject list = new JsonObject();
 		for (Entry<ChunkCoordIntPair, Average> entry : worldStats.timeByChunk.entrySet()) {
 			list.add(entry.getKey().chunkXPos + "," + entry.getKey().chunkZPos, averageToJson(entry.getValue(), nanoScale, false));
 		}
 		return list;
 	}
 	
 	private static JsonObject timeByRegionToJson(WorldStats worldStats) {
 		Map<String, Double> regions = new LinkedHashMap<String, Double>();
 		for (Entry<ChunkCoordIntPair, Average> entry : worldStats.timeByChunk.entrySet()) {
 			String key = (entry.getKey().chunkXPos >> 5) + "," + (entry.getKey().chunkZPos >> 5);
 			Double total = regions.get(key);
 			if (total == null)
 				regions.put(key, entry.getValue().getAverage());
 			else
 				regions.put(key, total.doubleValue() + entry.getValue().getAverage());
 		}
 		
 		JsonObject list = new JsonObject();
 		for (Entry<String, Double> entry : regions.entrySet()) {
 			list.addProperty(entry.getKey(), Util.DECIMAL_FORMAT_3.format(entry.getValue() * 1.0E-6D));
 		}
 		return list;
 	}
 	
 	public static JsonObject averageToJson(Average average, double scale, boolean includeHistory) {
 		JsonObject json = new JsonObject();
 		
 		json.addProperty("average", Math.round(average.getAverage() * scale * 100D) / 100D);
 		
 		if (scale != 1.0D) {
 			json.addProperty("latest", Math.round(average.getLatest() * scale * 100D) / 100D);
 		}
 		else {
 			json.addProperty("latest", average.getLatest());
 		}
 		
 		if (includeHistory) {
 			json.addProperty("resolution", average.getResolution());
 			
 			JsonArray historyArray = new JsonArray();
 			if (average.getTotal() > 0 && average.getTick() >= 0) {
 				long[] history = average.getHistory();
 				int backIndex = average.getTick() - average.getResolution();
 				for (int i = average.getTick(); i >= 0 && i > backIndex; i--) {
 
 					if (scale != 1.0D)
 						historyArray.add(new JsonPrimitive(Math.round(history[i % average.getResolution()] * scale * 100D) / 100D));
 					else
 						historyArray.add(new JsonPrimitive(history[i % average.getResolution()]));
 				}
 			}
 			
 			json.add("history", historyArray);
 		}
 		
 		return json;
 	}
 
 }
