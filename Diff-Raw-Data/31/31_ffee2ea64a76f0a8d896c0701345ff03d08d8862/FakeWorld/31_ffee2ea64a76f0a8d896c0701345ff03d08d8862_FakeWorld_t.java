 package me.tehbeard.utils.testSuite.cuboid;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.bukkit.BlockChangeDelegate;
 import org.bukkit.Chunk;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Difficulty;
 import org.bukkit.Effect;
 import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.WorldType;
 import org.bukkit.block.Biome;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LightningStrike;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 public class FakeWorld implements World{
 
     String name;
     public FakeWorld(String name){
         this.name= name;
     }
     
     
     public Set<String> getListeningPluginChannels() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
         // TODO Auto-generated method stub
         
     }
 
     public List<MetadataValue> getMetadata(String arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public boolean hasMetadata(String arg0) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void removeMetadata(String arg0, Plugin arg1) {
         // TODO Auto-generated method stub
         
     }
 
     public void setMetadata(String arg0, MetadataValue arg1) {
         // TODO Auto-generated method stub
         
     }
 
     public boolean canGenerateStructures() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean createExplosion(Location arg0, float arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean createExplosion(Location arg0, float arg1, boolean arg2) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean createExplosion(double arg0, double arg1, double arg2,
             float arg3) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean createExplosion(double arg0, double arg1, double arg2,
             float arg3, boolean arg4) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public Item dropItem(Location arg0, ItemStack arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Item dropItemNaturally(Location arg0, ItemStack arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public boolean generateTree(Location arg0, TreeType arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean generateTree(Location arg0, TreeType arg1,
             BlockChangeDelegate arg2) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean getAllowAnimals() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean getAllowMonsters() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public int getAnimalSpawnLimit() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public Biome getBiome(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Block getBlockAt(Location arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Block getBlockAt(int arg0, int arg1, int arg2) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public int getBlockTypeIdAt(Location arg0) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public int getBlockTypeIdAt(int arg0, int arg1, int arg2) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public Chunk getChunkAt(Location arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Chunk getChunkAt(Block arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Chunk getChunkAt(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Difficulty getDifficulty() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1,
             boolean arg2, boolean arg3) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public List<Entity> getEntities() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Environment getEnvironment() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public long getFullTime() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public ChunkGenerator getGenerator() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Block getHighestBlockAt(Location arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Block getHighestBlockAt(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public int getHighestBlockYAt(Location arg0) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public int getHighestBlockYAt(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public double getHumidity(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public boolean getKeepSpawnInMemory() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public List<LivingEntity> getLivingEntities() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Chunk[] getLoadedChunks() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public int getMaxHeight() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public int getMonsterSpawnLimit() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public String getName() {
         // TODO Auto-generated method stub
         return name;
     }
 
     public boolean getPVP() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public List<Player> getPlayers() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public List<BlockPopulator> getPopulators() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public int getSeaLevel() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public long getSeed() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public Location getSpawnLocation() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public double getTemperature(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public int getThunderDuration() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public long getTicksPerAnimalSpawns() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public long getTicksPerMonsterSpawns() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public long getTime() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public UUID getUID() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public int getWaterAnimalSpawnLimit() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public int getWeatherDuration() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public File getWorldFolder() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public WorldType getWorldType() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public boolean hasStorm() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isAutoSave() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isChunkLoaded(Chunk arg0) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isChunkLoaded(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isThundering() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void loadChunk(Chunk arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void loadChunk(int arg0, int arg1) {
         // TODO Auto-generated method stub
         
     }
 
     public boolean loadChunk(int arg0, int arg1, boolean arg2) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void playEffect(Location arg0, Effect arg1, int arg2) {
         // TODO Auto-generated method stub
         
     }
 
     public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
         // TODO Auto-generated method stub
         
     }
 
     public void playEffect(Location arg0, Effect arg1, int arg2, int arg3) {
         // TODO Auto-generated method stub
         
     }
 
     public <T> void playEffect(Location arg0, Effect arg1, T arg2, int arg3) {
         // TODO Auto-generated method stub
         
     }
 
     public boolean refreshChunk(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean regenerateChunk(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void save() {
         // TODO Auto-generated method stub
         
     }
 
     public void setAnimalSpawnLimit(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setAutoSave(boolean arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setBiome(int arg0, int arg1, Biome arg2) {
         // TODO Auto-generated method stub
         
     }
 
     public void setDifficulty(Difficulty arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setFullTime(long arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setKeepSpawnInMemory(boolean arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setMonsterSpawnLimit(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setPVP(boolean arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setSpawnFlags(boolean arg0, boolean arg1) {
         // TODO Auto-generated method stub
         
     }
 
     public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void setStorm(boolean arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setThunderDuration(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setThundering(boolean arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setTicksPerAnimalSpawns(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setTicksPerMonsterSpawns(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setTime(long arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setWaterAnimalSpawnLimit(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public void setWeatherDuration(int arg0) {
         // TODO Auto-generated method stub
         
     }
 
     public <T extends Entity> T spawn(Location arg0, Class<T> arg1)
             throws IllegalArgumentException {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Arrow spawnArrow(Location arg0, Vector arg1, float arg2, float arg3) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public LivingEntity spawnCreature(Location arg0, EntityType arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public LivingEntity spawnCreature(Location arg0, CreatureType arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public LightningStrike strikeLightning(Location arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public LightningStrike strikeLightningEffect(Location arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public boolean unloadChunk(Chunk arg0) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean unloadChunk(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean unloadChunk(int arg0, int arg1, boolean arg2, boolean arg3) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean unloadChunkRequest(int arg0, int arg1) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean unloadChunkRequest(int arg0, int arg1, boolean arg2) {
         // TODO Auto-generated method stub
         return false;
     }
 
 
     public Entity spawnEntity(Location arg0, EntityType arg1) {
         // TODO Auto-generated method stub
         return null;
     }
 

    public boolean isChunkInUse(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }


    public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        
    }


    public FallingBlock spawnFallingBlock(Location arg0, Material arg1,
            byte arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }


    public FallingBlock spawnFallingBlock(Location arg0, int arg1, byte arg2)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

 }
