 package org.zone.commandit.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.zone.commandit.CommandIt;
 
 public class FileAdapter implements DataAdapter {
     
     protected CommandIt plugin;
     protected Map<Location, LuaCode> cache;
     protected String filename;
     
     public FileAdapter(CommandIt plugin, String filename) {
         this.plugin = plugin;
         this.filename = filename;
     }
 
     @Override
     public void clear() {
         if (cache == null) load();
         cache.clear();
     }
     
     @Override
     public boolean containsKey(Object key) {
         if (cache == null) load();
         return cache.containsKey(key);
     }
     
     @Override
     public boolean containsValue(Object value) {
         if (cache == null) load();
         return cache.containsValue(value);
     }
     
     @Override
     public Set<java.util.Map.Entry<Location, LuaCode>> entrySet() {
         if (cache == null) load();
         return cache.entrySet();
     }
     
     @Override
     public LuaCode get(Object key) {
         if (cache == null) load();
         return cache.get(key);
     }
     
     @Override
     public boolean isEmpty() {
         if (cache == null) load();
         return cache.isEmpty();
     }
     
     @Override
     public Set<Location> keySet() {
         if (cache == null) load();
         return cache.keySet();
     }
     
     @Override
     public LuaCode put(Location key, LuaCode value) {
         if (cache == null) load();
         return cache.put(key, value);
     }
     
     @Override
     public void putAll(Map<? extends Location, ? extends LuaCode> m) {
         if (cache == null) load();
         cache.putAll(m);
     }
     
     @Override
     public LuaCode remove(Object key) {
         if (cache == null) load();
         return cache.remove(key);
     }
     
     @Override
     public int size() {
         if (cache == null) load();
         return cache.size();
     }
     
     @Override
     public Collection<LuaCode> values() {
         if (cache == null) load();
         return cache.values();
     }
 
     @Override
     public void load() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + filename));
         Map<Location, LuaCode> loaded = new HashMap<Location, LuaCode>();
         
         ConfigurationSection data = config.getConfigurationSection("blocks");
         if (data == null) {
             plugin.getLogger().info("No command blocks found.");
             cache = new HashMap<Location, LuaCode>();
         } else {
             String[] locText;
             World world;
             int x, y, z, block;
             Location loc;
             int attempts = 0;
             
             for (String key : data.getKeys(false)) {
                 try {
                     // Attempts to count the number of entries in the file
                     attempts++;
                     
                     // Decode location
                     locText = key.split(",");
                     world = Bukkit.getWorld(locText[0]);
                     if (world == null)
                         throw new IllegalArgumentException("World does not exist: " + locText[0] + ".");
                     x = Integer.parseInt(locText[1]);
                     y = Integer.parseInt(locText[2]);
                     z = Integer.parseInt(locText[3]);
                     loc = new Location(world, x, y, z);
                     
                     // Throws exception for an invalid location AND if the
                     // location is air
                     block = loc.getBlock().getTypeId();
                     if (block == 0)
                         throw new IllegalArgumentException("Location not valid: " + loc.toString() + ".");
                     
                     // Get attributes
                     String owner = data.getString(key + ".owner", null);
                     
                     LuaCode code = new LuaCode(owner);
                     for (Object o : data.getList(key + ".code", new ArrayList<String>())) {
                         code.addLine(o.toString());
                     }
                     
                     code.setEnabled(data.getBoolean(key + ".active", true));
                     
                     // Cooldowns as Player => Expiry (UNIX timestamp)
                     Map<String, Long> timeouts = code.getTimeouts();
                     ConfigurationSection cooldowns = data.getConfigurationSection(key + ".cooldowns");
                     if (cooldowns == null) {
                         cooldowns = data.createSection(key + "cooldowns");
                     }
                     for (String player : cooldowns.getKeys(false)) {
                         timeouts.put(player, cooldowns.getLong(player));
                     }
                     
                     loaded.put(loc, code);
                 } catch (Exception ex) {
                    plugin.getLogger().warning("Unable to load command block " + attempts + ". " + ex.getMessage());
                    ex.printStackTrace();
                 }
             }
            plugin.getLogger().info("Successfully loaded " + plugin.getCommandBlocks().size() + " command blocks");
    
             cache = loaded;
         }
     }
 
     @Override
     public void save() {
         FileConfiguration config = new YamlConfiguration();
         ConfigurationSection data = config.createSection("blocks");
         
         for (Map.Entry<Location, LuaCode> entry : cache.entrySet()) {
             Location loc = entry.getKey();
             LuaCode code = entry.getValue();
             code.trim();
             
             String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
             
             ConfigurationSection block = data.createSection(key);
             block.set("owner", code.getOwner());
             block.set("code", code.getLines());
             block.set("active", code.isEnabled());
             block.createSection("cooldowns", code.getTimeouts());
             
            try {
                config.save(new File(plugin.getDataFolder(), filename));
                plugin.getLogger().info(plugin.getCommandBlocks().size() + " command blocks saved");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save CommandIt");
                e.printStackTrace();
            }
         }
     }
 }
