 package com.herocraftonline.fearthereaper.spawnpoint;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
 
 public class Spawn extends YamlConfiguration {
 
     private Location location;
     private File spawnFile;
 
     public Spawn() {
         super();
     }
 
    public Spawn(String name, World world, double x, double y, double z, String group, String message, Plugin plugin) {
         super();
         this.set("name", name);
         this.set("group", group);
         this.set("message", message);
         this.location = new Location(world, x, y, z);
         this.set("x", location.getX());
         this.set("y", location.getY());
         this.set("z", location.getZ());
         this.set("world", location.getWorld().getName());
        this.spawnFile = new File(plugin.getDataFolder(), name + ".yml");
         save();
     }
 
    public Spawn(String name, Player player, Plugin plugin) {
         super();
         this.set("name", name);
         this.location = player.getLocation();
         this.set("x", location.getX());
         this.set("y", location.getY());
         this.set("z", location.getZ());
         this.set("world", location.getWorld().getName());
         this.set("group", "all");
         this.set("message", "none");
        this.spawnFile = new File(plugin.getDataFolder(), name + ".yml");
         save();
     }
 
     public String getSpawnMessage() {
         return this.getString("message");
     }
 
     public String getGroup() {
         return this.getString("group");
     }
 
     public String getWorldName() {
         return this.getString("world");
     }
 
     public boolean setSpawnMessage(String message) {
         this.set("message", message);
         return save();
     }
 
     public Location getLocation() {
         return this.location;
     }
 
     public String getName() {
         return this.getString("name");
     }
 
     public boolean setName(String name) {
         this.set("name", name);
         return save();
     }
 
     public boolean setGroup(String spawnGroup) {
         this.set("group", spawnGroup);
         return save();
     }
 
     public boolean setLocation(Location loc) {
         this.location = loc;
         this.set("x", loc.getX());
         this.set("y", loc.getY());
         this.set("z", loc.getZ());
         this.set("world", loc.getWorld().getName());
         return save();
     }
 
     public double getX() {
         return this.getDouble("x");
     }
 
     public double getY() {
         return this.getDouble("y");
     }
 
     public double getZ() {
         return this.getDouble("z");
     }
 
     public World getWorld() {
         return this.location.getWorld();
     }
 
     public boolean save() {
         try {
             this.save(spawnFile);
         } catch (IOException e) {
             System.out.println("There was an error saving " + getString("name"));
             return false;
         }
         return true;
     }
 
     public static Spawn loadConfig(File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
         Spawn spawn = new Spawn();
         spawn.load(file);
         return spawn;
     }
 
     @Override
     public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
         super.load(file);
         World world = Bukkit.getWorld(this.getString("world"));
         if (world == null) {
             throw new InvalidConfigurationException("World could not be detected properly.");
         }
         this.location = new Location(world, getX(), getY(), getZ());
     }
 }
