 package com.norcode.bukkit.buildinabox.datastore;
 
 import com.norcode.bukkit.buildinabox.BuildChest;
 import com.norcode.bukkit.buildinabox.BuildInABox;
 import com.norcode.bukkit.buildinabox.BuildingPlan;
 import com.norcode.bukkit.buildinabox.ChestData;
 import com.norcode.bukkit.buildinabox.util.ConfigAccessor;
 import com.norcode.bukkit.buildinabox.util.SerializationUtil;
 import com.norcode.bukkit.schematica.ClipboardBlock;
 import net.minecraft.server.v1_5_R3.NBTTagCompound;
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.util.BlockVector;
 
 import java.util.*;
 
 
 public class YamlDataStore extends DataStore {
     BuildInABox plugin;
     ConfigAccessor planCfg;
     ConfigAccessor chestCfg;
     int nextChestId = 0;
     HashMap<String, HashSet<Integer>> worldCache = new HashMap<String, HashSet<Integer>>();
     HashMap<Integer, ChestData> chests = new HashMap<Integer, ChestData>();
     HashMap<String, BuildingPlan> plans = new HashMap<String, BuildingPlan>();
     boolean dirty = false;
 
     public YamlDataStore(BuildInABox plugin) {
         this.plugin = plugin;
         this.planCfg = new ConfigAccessor(plugin, "plans.yml");
         this.chestCfg = new ConfigAccessor(plugin, "chests.yml");
     }
 
     void loadPlans() {
         this.planCfg.reloadConfig();
         this.plans.clear();
         // Load Plans
         ConfigurationSection sec;
         for (String key: this.planCfg.getConfig().getKeys(false)) {
             sec = this.planCfg.getConfig().getConfigurationSection(key);
             if (sec != null) {
                 BuildingPlan plan = new BuildingPlan(plugin, 
                         sec.getString("name"), 
                         sec.getString("filename"), 
                         sec.getString("display-name", sec.getString("name")),
                         sec.getStringList("description"));
                 this.plans.put(plan.getName().toLowerCase(), plan);
                 plan.registerPermissions();
             }
         }
     }
 
     void loadChests() {
         HashSet<Chunk> loadedChunks = new HashSet<Chunk>();
         this.chestCfg.reloadConfig();
         this.chests.clear();
         // Load Chests
         ConfigurationSection sec;
         int maxId = 0;
         int id;
         World world;
         String worldName = null;
         Integer x = null, y = null, z = null;
         for (String key: this.chestCfg.getConfig().getKeys(false)) {
 
             plugin.debug("Loading chest: " + key);
             sec = this.chestCfg.getConfig().getConfigurationSection(key);
             world = null;
             id = sec.getInt("id", 0);
             plugin.debug(" ... id " + id);
             if (id > 0) {
                 if (id > maxId) maxId = id;
                 String locStr = sec.getString("location");
                 if (locStr != null && !locStr.equals("")) {
                     String[] parts = locStr.split(";");
                     plugin.debug(" ... location: " + locStr);
                     if (parts.length == 4) {
                         worldName = parts[0];
                         world = plugin.getServer().createWorld(new WorldCreator(worldName));
                         x = Integer.parseInt(parts[1]);
                         y = Integer.parseInt(parts[2]);
                         z = Integer.parseInt(parts[3]);
                     }
                 } else {
                     plugin.debug(" ... no location data.");
                    continue;
                 }
                 // Store the id in a map keyed on world name
                 // for quicker lookups at world load time.
                 if (world != null) {
                    HashMap<BlockVector, NBTTagCompound> tileEntities = SerializationUtil.deserializeTileEntities(sec.getString("tile-entities"));
                    HashMap<BlockVector, ClipboardBlock> replacedBlocks = SerializationUtil.deserializeReplacedBlocks(sec.getString("replaced-blocks"));
                    ChestData cd = new ChestData(id, sec.getString("plan"), sec.getString("locked-by"), sec.getLong("last-activity"), world.getName(), x, y, z, tileEntities, replacedBlocks);
                    chests.put(id,cd);
                    BuildChest bc = new BuildChest(cd);
                     if (!bc.getLocation().getChunk().isLoaded()) {
                         if (!bc.getLocation().getChunk().load()) {
                             continue;
                         }
                     }
                     if (bc.getBlock().getTypeId() != plugin.cfg.getChestBlockId()) {
                         plugin.getDataStore().deleteChest(cd.getId());
                         continue;
                     }
                     bc.getBlock().setMetadata("buildInABox", new FixedMetadataValue(plugin, bc));
                     if (!plugin.cfg.isBuildingProtectionEnabled())
                         continue;
                     Set<Chunk> protectedChunks = bc.protectBlocks(null);
                     if (protectedChunks == null)
                         continue;
                     loadedChunks.addAll(protectedChunks);
                 } else {
                     plugin.debug("Chest w/ key: " + key + " has an invalid world.");
                 }
 
             } else {
                 plugin.debug("Chest w/ key: " + key + " has no id.");
             }
         }
         for (Chunk c: loadedChunks) {
             c.getWorld().unloadChunkRequest(c.getX(), c.getZ(), true);
         }
         nextChestId = maxId;
     }
 
     @Override
     public void load() {
         loadPlans();
         loadChests();
     }
 
     @Override
     public void save() {
         if (this.dirty) {
             this.dirty = false;
             this.chestCfg.saveConfig();
             this.planCfg.saveConfig();
         }
     }
 
     @Override
     public ChestData getChest(int id) {
         return this.chests.get(id);
     }
 
     @Override
     public ChestData createChest(String plan) {
         nextChestId++;
         ConfigurationSection sec = this.chestCfg.getConfig().createSection(Integer.toString(nextChestId));
         long now = System.currentTimeMillis();
         sec.set("plan", plan);
         sec.set("last-activity", now);
         sec.set("id", nextChestId);
         setDirty();
         ChestData data = new ChestData(nextChestId, plan, null, now,
                 null, null, null, null, // world, x, y, z
                 null, null);            // tileEntities, replacedBlocks
         chests.put(nextChestId, data);
         return data;
     }
 
     @Override
     public void saveChest(ChestData data) {
         ConfigurationSection sec = this.chestCfg.getConfig().getConfigurationSection(Integer.toString(data.getId()));
         sec.set("plan",  data.getPlanName());
         sec.set("last-activity", data.getLastActivity());
         sec.set("location", data.getSerializedLocation());
         sec.set("locked-by", data.getLockedBy());
         sec.set("replaced-blocks", SerializationUtil.serializeReplacedBlocks(data.getReplacedBlocks()));
         sec.set("tile-entities", SerializationUtil.serializeTileEntities(data.getTileEntities()));
         this.setDirty();
     }
 
     @Override
     public void deleteChest(int id) {
         plugin.debug("Deleting Chest ID#" + id);
         this.chests.remove(id);
         this.chestCfg.getConfig().set(Integer.toString(id), null);
         this.setDirty();
     }
 
     @Override
     public void setWorldChests(World world, Collection<ChestData> chests) {
         HashSet<Integer> ids = new HashSet<Integer>();
         for (ChestData cd: chests) {
             ids.add(cd.getId());
         }
         worldCache.put(world.getName(), ids);
     }
 
     @Override
     public void clearWorldChests(World world) {
         worldCache.put(world.getName(), new HashSet<Integer>());
     }
 
 
     @Override
     public Collection<ChestData> getWorldChests(World world) {
         if (worldCache.containsKey(world.getName())) {
             Set<Integer> ids = worldCache.get(world.getName());
             List<ChestData> results = new ArrayList<ChestData>(ids.size());
             for (int i: worldCache.get(world.getName())) {
                 results.add(chests.get(i));
             }
             return results;
         }
         return null;
     }
 
     @Override
     public void saveBuildingPlan(BuildingPlan plan) {
         ConfigurationSection sec = planCfg.getConfig().getConfigurationSection(plan.getName());
         if (sec == null) {
             sec = planCfg.getConfig().createSection(plan.getName());
         }
         sec.set("name", plan.getName());
         sec.set("display-name", plan.getDisplayName());
         sec.set("filename", plan.getFilename());
         sec.set("description", plan.getDescription());
         this.plans.put(plan.getName().toLowerCase(), plan);
         setDirty();
     }
 
     @Override
     public Collection<ChestData> getAllChests() {
         return chests.values();
     }
 
     @Override
     public Collection<BuildingPlan> getAllBuildingPlans() {
         return plans.values();
     }
 
     @Override
     public BuildingPlan getBuildingPlan(String name) {
         return plans.get(name.toLowerCase());
     }
 
     private void setDirty() {
         this.dirty = true;
     }
 
     @Override
     public void deleteBuildingPlan(BuildingPlan plan) {
         this.plans.remove(plan.getName().toLowerCase());
         plan.getSchematicFile().delete();
         this.planCfg.getConfig().set(plan.getName(), "");
     }
     
 }
