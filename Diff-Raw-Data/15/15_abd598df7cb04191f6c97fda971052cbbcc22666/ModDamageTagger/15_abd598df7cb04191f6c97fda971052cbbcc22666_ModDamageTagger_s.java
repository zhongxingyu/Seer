 package com.KoryuObihiro.bukkit.ModDamage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
 import org.yaml.snakeyaml.Yaml;
 
 import com.KoryuObihiro.bukkit.ModDamage.PluginConfiguration.LoadState;
 import com.KoryuObihiro.bukkit.ModDamage.PluginConfiguration.OutputPreset;
 
 public class ModDamageTagger
 {
 	public static final String configString_save = "interval-save";
 	public static final String configString_clean = "interval-clean";
 	public static final int defaultInterval = 120 * 20;
 	
 	private final Map<String, HashMap<UUID, Integer>> tags = Collections.synchronizedMap(new LinkedHashMap<String, HashMap<UUID, Integer>>());
 	
	private final HashSet<Integer> pendingTaskIDs = new HashSet<Integer>();
 	private long saveInterval;
 	private long cleanInterval;
 	private Integer saveTaskID;
 	private Integer cleanTaskID;
 
 	final File file;
 	private InputStream reader = null;
 	private FileWriter writer = null;
 	private Yaml yaml = new Yaml();
 
 	public ModDamageTagger(File file, long saveInterval, long cleanInterval)
 	{
 		this.file = file;
 		if(file != null)
 		{
 			try
 			{
 				if(!file.exists())
 				{
 					ModDamage.addToLogRecord(OutputPreset.INFO, "No tags file found at " + file.getAbsolutePath() + ", generating a new one...");
 					if(!file.getParentFile().mkdirs() && !file.createNewFile())
 						ModDamage.addToLogRecord(OutputPreset.FAILURE, "Couldn't make new tags file! Tags will not have persistence between reloads.");
 				}
 				reader = new FileInputStream(file);
 				Object tagFileObject = yaml.load(reader);
 				reader.close();
 				if(tagFileObject != null)
 				{
 					if(tagFileObject instanceof LinkedHashMap)
 					{
 						@SuppressWarnings("unchecked")
 						LinkedHashMap<String, Object> tagMap = (LinkedHashMap<String, Object>)tagFileObject;
 						for(Entry<String, Object> entry : tagMap.entrySet())
 						{
 							if(entry.getValue() instanceof LinkedHashMap)
 							{
 								HashMap<UUID, Integer> uuidMap = new HashMap<UUID, Integer>();
 								
 								@SuppressWarnings("unchecked")
 								LinkedHashMap<String, Object> rawUuidMap = (LinkedHashMap<String, Object>)entry.getValue();
 								for(Entry<String, Object> tagEntry : rawUuidMap.entrySet())
 								{
 									UUID uuid = UUID.fromString(tagEntry.getKey());
 									Integer integer = tagEntry.getValue() != null && tagEntry.getValue() instanceof Integer?(Integer)tagEntry.getValue():null;
 									if(uuid != null)
 									{
 										if(integer != null) uuidMap.put(uuid, integer);
 										else ModDamage.addToLogRecord(OutputPreset.FAILURE, "Could not read value for entity UUID " + tagEntry.getKey() + " under tag \"" + tagEntry + "\".");
 									}
 									else ModDamage.addToLogRecord(OutputPreset.FAILURE, "Could not read entity UUID " + tagEntry.getKey() + " under tag \"" + tagEntry + "\".");
 								}
 								if(!uuidMap.isEmpty()) tags.put(entry.getKey(), uuidMap);
 								else ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, "No entity IDs added for tag \"" + entry + "\" in tags.yml.");
 							}
 							else ModDamage.addToLogRecord(OutputPreset.FAILURE, "Could not read nested content under tag \"" + entry.getKey() + "\".");
 						}
 					}
 					else ModDamage.addToLogRecord(OutputPreset.FAILURE, "Incorrectly formatted tags.yml. Starting with an empty tag list.");
 				}
 			}
 			catch(Exception e){ ModDamage.addToLogRecord(OutputPreset.FAILURE, "Error loading tags.yml.");}
 		}
 		else
 		{
 			saveTaskID = null;
 			cleanTaskID = null;
 		}
 		this.saveInterval = saveInterval;
 		this.cleanInterval = cleanInterval;
 		reload(false);
 	}
 	
 	public void reload(){ reload(true);}
 	
 	private synchronized void reload(boolean initialized)
 	{
 		cleanUp();
 		save();
 		if(initialized)
 		{
 			if(file != null)
 			{
 				if(saveTaskID != null) Bukkit.getScheduler().cancelTask(saveTaskID);
 				if(cleanTaskID != null) Bukkit.getScheduler().cancelTask(cleanTaskID);
 			}
 		}
 		Plugin modDamage = Bukkit.getPluginManager().getPlugin("ModDamage");
 		
 		saveTaskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(modDamage, new Runnable(){
 			@Override public void run()
 			{
 				save();
 			}
 		}, saveInterval, saveInterval);
 		
 		cleanTaskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(modDamage, new Runnable(){
 			@Override public void run()
 			{
 				cleanUp();
 			}
 		}, cleanInterval, cleanInterval);	
 	}
 	
 	/**
 	 * Saves all tags to a file.
 	 */
 	public synchronized void save()
 	{
 		if(file != null)
 		{
 			LinkedHashMap<String, List<String>> tempMap = new LinkedHashMap<String, List<String>>();
 			for(Entry<String, HashMap<UUID, Integer>> entry : tags.entrySet())
 			{
 				List<String> convertedUUIDS = new ArrayList<String>();
 				for(UUID uuid : entry.getValue().keySet())
 					convertedUUIDS.add(uuid.toString());
 				tempMap.put(entry.getKey(), convertedUUIDS);
 			}
 			try
 			{
 				writer = new FileWriter(file);
 				writer.write(yaml.dump(tempMap));
 				writer.close();
 			}
 			catch (IOException e){ PluginConfiguration.log.warning("Error writing to " + file.getAbsolutePath() + "!");}
 		}
 	}
 	
 //public API
 	/**
 	 * Add the entity's UUID to a tag. A new tag is made if it doesn't already exist.
 	 */
 	public synchronized void addTag(String tag, Entity entity, int tagValue)
 	{
 		if(!tags.containsKey(tag))
 			tags.put(tag, new HashMap<UUID, Integer>());
 		tags.get(tag).put(entity.getUniqueId(), tagValue);
 	}
 	
 	public class ModDamageTagRemoveTask implements Runnable
 	{
 		final String tag;
 		final Entity entity;
 		
 		ModDamageTagRemoveTask(String tag, Entity entity)
 		{
 			this.tag = tag;
 			this.entity = entity;
 		}
 		
 		@Override
 		public void run()
 		{
 			if(entity != null)
 				removeTag(tag, entity);
 		}
 	}
 
 	/**
 	 * Checks if entity has been tagged with the specified tag.
 	 * @return Boolean indicating whether or not the entity was tagged.
 	 */
 	public synchronized boolean isTagged(Entity entity, String tag)
 	{
 		return tags.containsKey(tag)?tags.get(tag).containsKey(entity.getUniqueId()):false;
 	}
 	
 	/**
 	 * @param entity - The entity whose UUID will be checked for tags.
 	 * @return List of found tags.
 	 */
 	public synchronized List<String> getTags(Entity entity)
 	{
 		List<String> entityTags = new ArrayList<String>();
 		int id = entity.getEntityId();
 		for(Entry<String, HashMap<UUID, Integer>> entry : tags.entrySet())
 			if(entry.getValue().containsKey(id))
 				entityTags.add(entry.getKey());
 		return entityTags;
 	}
 	
 	public synchronized Integer getTagValue(Entity entity, String tag)
 	{
 		if(isTagged(entity, tag))
 			return tags.get(tag).get(entity.getUniqueId());
 		return null;
 	}
 	
 	/**
 	 * Removes the entity's UUID from a tag, if {@link void generateTag(String tag) [generateTag]} was called correctly.	 * 
 	 */
 	public synchronized void removeTag(String tag, Entity entity)
 	{
 		if(tags.containsKey(tag))
 			tags.get(tag).remove(entity.getEntityId());
 	}
 	
 	/**
 	 * This method checks whether or not the number of tagged entities exceeds the number of entities in the server,
 	 * and if so removes entities that no longer exist.
 	 */
 	public synchronized void cleanUp()
 	{
 		//clean up the entities
 		HashSet<UUID> ids = new HashSet<UUID>();
 		for(World world : Bukkit.getWorlds())
 			for(Entity entity : world.getEntities())
 				ids.add(entity.getUniqueId());
 		for(HashMap<UUID, Integer> tagList : tags.values())
			for(UUID id : Collections.unmodifiableSet(tagList.keySet()))
 				if(!ids.contains(id))
 					tagList.remove(id);
		//clean up the tasks
		HashSet<Integer> bukkitTaskIDs = new HashSet<Integer>();
		List<BukkitTask> bukkitTasks = Bukkit.getScheduler().getPendingTasks();
		for(BukkitTask task : bukkitTasks)
			bukkitTaskIDs.add(task.getTaskId());
		for(Integer taskID : pendingTaskIDs)
			if(!bukkitTaskIDs.contains(taskID))
				pendingTaskIDs.remove(taskID);
 	}
 	
 	/**
 	 * Only the ModDamage main should use this method.
 	 */
 	public synchronized void clear(){ tags.clear();}
 	
 	/**
 	 * This is used in the ModDamage main to finish any file IO.
 	 */
 	public synchronized void close()
 	{
 		cleanUp();
 		save();
 	}
 	
 	/**
 	 * @return LoadState reflecting the file's load state.
 	 */
 	public LoadState getLoadState(){ return file != null?LoadState.SUCCESS:LoadState.NOT_LOADED;}
 }
