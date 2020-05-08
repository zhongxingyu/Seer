 package com.github.idragonfire.dragonskills;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Chunk;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Regions {
 	private Set<String> skillFreeZones;
 	private DragonSkillsPlugin plugin;
 	private File file;
 
 	public Regions(DragonSkillsPlugin plugin) {
 		this.plugin = plugin;
 		skillFreeZones = new HashSet<String>();
		file = new File(plugin.getDataFolder() + File.separator + "regions.yml");
 		if (!file.exists()) {
 			try {
 				file.createNewFile();
 				save();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		load();
 	}
 
 	public void save() {
 		FileConfiguration config = new YamlConfiguration();
 		config.set("chunks", skillFreeZones.toArray(new String[0]));
 		try {
 			config.save(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void load() {
 		FileConfiguration config = new YamlConfiguration();
 		try {
 			config.load(file);
 			List<String> chunks = (List<String>) config.getList("chunks");
 			if (chunks == null) {
 				return;
 			}
 			for (String chunk : chunks) {
 				skillFreeZones.add(chunk);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean isInSkillFreeRegion(Chunk chunk) {
 		return skillFreeZones.contains(hashChunk(chunk));
 	}
 
 	public void addChunk(Chunk chunk) {
 		skillFreeZones.add(hashChunk(chunk));
 		save();
 	}
 
 	public void removeChunk(Chunk chunk) {
 		skillFreeZones.remove(hashChunk(chunk));
 		save();
 	}
 
 	private String hashChunk(Chunk chunk) {
 		return hashChunk(chunk.getX(), chunk.getZ());
 	}
 
 	// TODO: make it better
 	private String hashChunk(int x, int z) {
 		return x + ":" + z;
 	}
 }
