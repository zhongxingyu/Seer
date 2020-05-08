 package com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 
 public class SpawnerConfig {
 	private YamlConfiguration Spawners;
 	private File SpawnerFile;
 	
 	public SpawnerConfig(File Spawnerfile) {
 		this.Spawners = new YamlConfiguration();
 		this.SpawnerFile = Spawnerfile;
 	}
 	public boolean load() {
 		try {
 			if (!SpawnerFile.exists()) {
 				SpawnerFile.createNewFile();
 				loaddefaults();
 			}
 			Spawners.load(SpawnerFile);
 			return true;
 		}
 		catch (Exception e) {
 			Messenger.severe("The Eggs data file did not load correctly, returned error:\n" + e.getMessage());
 			return false;
 		}
 	}
 	private void loaddefaults() {
 		String[] eggslist = {};
 		String[] RunningSpawnersList = {};
 		Spawners.addDefault("Spawners.List", Arrays.asList(eggslist));
 		Spawners.addDefault("Spawners.Running.List", Arrays.asList(RunningSpawnersList));
 		Spawners.options().copyDefaults(true);
 		save();
 	}
 	public boolean save() {
 		try {
 			Spawners.save(SpawnerFile);
 		} catch (Exception e) { 
 			Messenger.severe("The Eggs data file failed to save, returned error:\n" + e.getMessage());
 		}
 		return true;
 	}
 	public YamlConfiguration GetConfig() {
 		return Spawners;
 	}
 	public void addtolist(String path, String item) {
 		List<String> l = Spawners.getStringList(path);
 		if(!l.contains(item)) l.add(item);
 		Spawners.set(path, l);
 		save();
 	}
 	public void RemoveFromList(String path, String item) {
 		List<String> l = Spawners.getStringList(path);
 		if(l.contains(item)) l.remove(item);
 		Spawners.set(path, l);
 		save();
 	}
 	public List<String> getList(String path) {
 		return (List<String>) Spawners.getList(path);
 	}
 	public int getInt(String path) {
 		return Spawners.getInt(path);
 	}
 	public Boolean getBoolean(String path) {
 		return Spawners.getBoolean(path);
 	}
 	public String getString(String path) {
 		return Spawners.getString(path);
 	}
 	public Location getlocation(String path) {
 		String locstring = Spawners.getString(path);
 		Location loc = ParseLocation(locstring);
 		return loc;
 	}
 	public void SetLocation(String path,Location item) {
 		String l = LocString(item);
 		Spawners.set(path, l);
 		save();
 	}
 	public static Location ParseLocation(String l) {
 		String[] s = l.split(",");
 		if (s.length !=4) {
 			throw new IllegalArgumentException("Invalid coordiantes for Spawner ParseLocation.");
 		}
 		Integer x = parseint(s[0]);
 		Integer y = parseint(s[1]);
 		Integer z = parseint(s[2]);
 		World world = Bukkit.getWorld(s[3]);
 		return new Location(world, x, y, z);
 	}
 	public String LocString(Location l) {
 		StringBuffer s = new StringBuffer();
 		s.append(l.getBlockX() + ",");
 		s.append(l.getBlockY() + ",");
 		s.append(l.getBlockZ() + ",");
 		s.append(l.getWorld().getName());
 		return s.toString();
 	}
 	public static Integer parseint(String i) {
 		try {
 			return Integer.parseInt(i.trim());
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}
 	public void SetString(String path, String item) {
 		Spawners.set(path, item);
 		save();
 	}
 	public void SetList(String path,ArrayList<String> list) {
 		  List<String> configList = Spawners.getStringList(path);
 		    configList.addAll(list);
 		    Spawners.set(path, configList);
 		    save();
 	}
 	public void SetBoolean(String path, boolean item) {
 		Spawners.set(path, item);
 		save();
 	}
 	public void setInt(String path, int item) {
 		Spawners.set(path, item);
 		save();
 	}
 }
