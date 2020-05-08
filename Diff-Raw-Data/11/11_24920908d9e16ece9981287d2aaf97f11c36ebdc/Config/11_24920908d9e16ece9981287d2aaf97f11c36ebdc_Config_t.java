 package com.imdeity.deitynether;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Config{
 
 	YamlConfiguration config = null;
 	File file = null;
 	
 	public Config(File file){
 		config = YamlConfiguration.loadConfiguration(file);
 		this.file = file;
 	}
 	
 	public void loadDefaults(){
 		if(!config.contains("world.nether.days-until-next-regen"))			//World options
 			config.set("world.nether.days-until-next-regen", 7);
 		if(!config.contains("world.nether.name"))
 			config.set("world.nether.name", "world_nether");
 		if(!config.contains("world.nether.pigman-gold-drop-chance"))
 			config.set("world.nether.pigman-gold-drop-chance", 10);
 		if(!config.contains("world.nether.gold-blocks-need"))
 			config.set("world.nether.gold-blocks-needed", 1);
 		if(!config.contains("world.main.name"))
 			config.set("world.main.name", "world");
 		if(!config.contains("spawn.main-world.x"))							//World spawn options
 			config.set("spawn.main.x", 100);
 		if(!config.contains("spawn.main.y"))
 			config.set("spawn.main.y", 64);
 		if(!config.contains("spawn.main.z"))
 			config.set("spawn.main.z", 100);
 		if(!config.contains("spawn.nether.x"))
 			config.set("spawn.nether.x", 100);
 		if(!config.contains("spawn.nether.y"))
 			config.set("spawn.nether.y", 64);
 		if(!config.contains("spawn.nether.z"))
 			config.set("spawn.nether.z", 100);
		if(!config.contains("mysql.database.name"))							//MySQL options
 			config.set("mysql.database.name", "kingdoms");
 		if(!config.contains("mysql.database.username"))
 			config.set("mysql.database.username", "root");
		if(!config.contains("mysql.database.password"))
 			config.set("mysql.database.password", "root");
 		if(!config.contains("mysql.server.address"))
 			config.set("mysql.server.address", "localhost");
 		if(!config.contains("mysql.server.port"))
 			config.set("mysql.server.port", 3306);
 		save();
 	}
 	
 	private void save(){
 		try {
 			config.save(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getDaysUntilNextRegen(){
 		return getInt("world.nether.days-until-next-regen");
 	}
 	
 	public int getDropChance(){
 		return getInt("world.nether.pigman-gold-drop-chance");
 	}
 	
 	public String getMainWorldName(){
 		return getString("world.main.name");
 	}
 	
 	public Location getMainWorldSpawn(){
 		int x = getInt("spawn.main.x");
 		int y = getInt("spawn.main.y");
 		int z = getInt("spawn.main.z");
 		World world = Bukkit.getServer().getWorld(getMainWorldName());
 		return new Location(world, x, y, z);
 	}
 	
 	public int getNeededGold(){
 		return getInt("world.nether.gold-blocks-needed");
 	}
 	
 	public String getMySqlDatabaseName(){
 		return getString("mysql.database.name");
 	}
 	
 	public String getMySqlDatabaseUsername(){
		return getString("mysql.database.username");
 	}
 	
 	public String getMySqlDatabasePassword(){
 		return getString("mysql.database.password");
 	}
 	
 	public String getMySqlServerAddress(){
		return getString("mysql.server.address");
 	}
 	
 	public int getMySqlServerPort(){
 		return getInt("mysql.server.port");
 	}
 	
 	public Location getNetherWorldSpawn(){
 		int x = getInt("spawn.nether.x");
 		int y = getInt("spawn.nether.y");
 		int z = getInt("spawn.nether.z");
 		World world = Bukkit.getServer().getWorld(getNetherWorldName());
 		return new Location(world, x, y, z);
 	}
 	
 	public String getNetherWorldName(){
 		return getString("world.nether.name");
 	}
 	
 	public String getString(String path){
 		return config.getString(path);
 	}
 	
 	public int getInt(String path){
 		return config.getInt(path);
 	}
 	
 }
