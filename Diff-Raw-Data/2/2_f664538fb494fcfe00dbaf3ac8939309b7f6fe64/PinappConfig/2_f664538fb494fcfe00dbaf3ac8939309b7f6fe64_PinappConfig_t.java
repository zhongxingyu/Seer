 package com.ubempire.not.a.portal;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.config.Configuration;
 
 public class PinappConfig {
 	Configuration c;
 	Pinapp jp;
 	HashMap<Integer, String> ibw = new HashMap<Integer, String>();
 	HashMap<String, Integer> wbi = new HashMap<String, Integer>();
 
 	PinappConfig(Pinapp jp) {
 		this.jp = jp;
 		this.c = jp.getConfiguration();
 	}
 
 	public void setDefaults() {
 		List<String> keys = c.getKeys("worlds");
 		if (keys == null || keys.size() == 0) {
 			jp.log("No config found. Generating config file.");
 			World defaultWorld = jp.getServer().getWorlds().get(0);
 			String worldName = defaultWorld.getName();
 			c.setProperty("worlds." + worldName + ".generator", "Default");
 			c.setProperty("worlds." + worldName + ".id", 14);
 			c.setProperty("worlds." + worldName + ".env", "Normal");
 			c.setProperty("worlds." + worldName + ".seed", (int) defaultWorld.getSeed());
 			c.save();
 		}
 	}
 
 	public void setup() {
 		jp.log("Loading config.");
 		List<String> keys = c.getKeys("worlds");
 		for (String key : keys) {
 			// What is the gen?
 			String generator = c.getString("worlds." + key + ".generator",
 					"Default");
 			// What is the portal block?
 			int portalBlock = c.getInt("worlds." + key + ".id", 14);
 			// What is the seed?
 			long seed = (long) c.getInt("worlds." + key + ".seed", 0);
 			// What is the env?
 			String environment = c
 					.getString("worlds." + key + ".env", "Normal");
 			// Setup the env variables
 			Environment env = Environment.NORMAL;
 			if (environment.equalsIgnoreCase("nether"))
 				env = Environment.NETHER;
 			if (environment.equalsIgnoreCase("skylands"))
 				env = Environment.SKYLANDS;
 			// Worldname?
 			String worldName = key;
 			// Is the gen other than default?
 			if (!generator.equalsIgnoreCase("default")) {
 				String[] genSplit = generator.split(":");
 				String plugin = genSplit[0];
 				String args = "";
 				if (genSplit.length > 1)
					args = generator.substring(generator.indexOf(":") + 1);
 				ChunkGenerator gen = jp.getServer().getPluginManager()
 						.getPlugin(plugin)
 						.getDefaultWorldGenerator(worldName, args);
 				jp.log("Creating world: "+worldName);
 				if(seed != 0)
 				jp.getServer().createWorld(worldName, env, seed, gen);
 				else
 				jp.getServer().createWorld(worldName, env, gen);
 			} else {
 				jp.log("Creating world: "+worldName);
 				if(seed != 0)
 				jp.getServer().createWorld(worldName, env, seed);
 				else
 				jp.getServer().createWorld(worldName, env);
 			}
 			// Then do the stuff to let us manage it :D
 			ibw.put(portalBlock, worldName);
 			wbi.put(worldName, portalBlock);
 			Pinapp.portalTypes.add(portalBlock);
 		}
 	}
 	public int getId(String world) {
 		return wbi.get(world);
 	}
 	public String getWorld(int id) {
 		return ibw.get(id);
 	}
 
 	public String getWorld(Material mat) {
 		return getWorld(mat.getId());
 	}
 }
