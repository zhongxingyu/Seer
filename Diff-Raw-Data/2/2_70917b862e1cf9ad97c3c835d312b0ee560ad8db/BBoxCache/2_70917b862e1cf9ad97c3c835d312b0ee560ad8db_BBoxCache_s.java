 package cc.apoc.bboutline;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import cc.apoc.bboutline.util.BBoxFactory;
 import cc.apoc.bboutline.util.BBoxInt;
 
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.village.Village;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.ChunkProviderEnd;
 import net.minecraft.world.gen.ChunkProviderGenerate;
 import net.minecraft.world.gen.ChunkProviderHell;
 import net.minecraft.world.gen.feature.MapGenScatteredFeature;
 import net.minecraft.world.gen.structure.MapGenMineshaft;
 import net.minecraft.world.gen.structure.MapGenStronghold;
 import net.minecraft.world.gen.structure.MapGenStructure;
 import net.minecraft.world.gen.structure.StructureComponent;
 import net.minecraft.world.gen.structure.StructureStart;
 
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 
 /**
  */
 public class BBoxCache {
     
     public static final int OVERWORLD = 0;
     public static final int NETHER = -1;
     public static final int END = 1;
     
     private Config config;
     private World world;
     
     public BBoxCache(Config config, World world, int dimensionId, IChunkProvider chunkProvider) {
         this.config = config;
         this.world = world;
         this.dimensionId = dimensionId;
         this.chunkProvider = chunkProvider;
         cache = new ConcurrentHashMap<BBoxInt, Vector<BBoxInt>>();
         villageCache = new HashSet<BBoxInt>();
         cacheSent = new HashMap<EntityPlayerMP, Vector<BBoxInt>>();
     }
     
     public void unload() {
         // TODO: tell the server to remove from playerDimensions?
     }
     
     private int dimensionId;
     private IChunkProvider chunkProvider;
     private ConcurrentHashMap<BBoxInt, Vector<BBoxInt>> cache;
     private Set<BBoxInt> villageCache;
     private HashMap<EntityPlayerMP, Vector<BBoxInt>> cacheSent;
     
     
     
     public ConcurrentHashMap<BBoxInt, Vector<BBoxInt>> getCache() {
         return cache;
     }
 
 
     
     
     
     
     public int getDimensionId() {
         return dimensionId;
     }
     
     /**
      * Return a list of structure start instances.
      */
     private Collection<StructureStart> getStructures() {
         Map<ChunkCoordIntPair, StructureStart> structureMap = 
                 new HashMap<ChunkCoordIntPair, StructureStart>();
         
         // overworld
         if (chunkProvider instanceof ChunkProviderGenerate) {
             
             /* pretty useless anyways:
             if (config.drawVillage) {
                 Object mapGenVillage = ReflectionHelper.getPrivateValue(
                         ChunkProviderGenerate.class, (ChunkProviderGenerate) chunkProvider, 14);
                 if (mapGenVillage instanceof MapGenVillage)
                     structureMap.putAll(getPrivateStructureMap((MapGenStructure) mapGenVillage));
                 else
                     FMLLog.severe("village generator not found in provider");
             }*/
 
             // scattered: witch hut, etc.
             if (config.drawScattered) {
                 Object mapGenScattered = ReflectionHelper.getPrivateValue(
                         ChunkProviderGenerate.class, (ChunkProviderGenerate) chunkProvider, 16);
                 if (mapGenScattered instanceof MapGenScatteredFeature)
                     structureMap.putAll(getPrivateStructureMap((MapGenStructure) mapGenScattered));
                 else
                     FMLLog.severe("scattered generator not found in provider");
             }
 
             // stronghold
             if (config.drawStronghold) {
                 Object mapGenStronghold = ReflectionHelper.getPrivateValue(
                         ChunkProviderGenerate.class, (ChunkProviderGenerate) chunkProvider, 13);
                 if (mapGenStronghold instanceof MapGenStronghold)
                     structureMap.putAll(getPrivateStructureMap((MapGenStructure) mapGenStronghold));
                 else
                     FMLLog.severe("stronghold generator not found in provider");
             }
 
             // mineshaft
             if (config.drawMineshaft) {
                 Object mapGenMineshaft = ReflectionHelper.getPrivateValue(
                         ChunkProviderGenerate.class, (ChunkProviderGenerate) chunkProvider, 15);
                 if (mapGenMineshaft instanceof MapGenMineshaft)
                     structureMap.putAll(getPrivateStructureMap((MapGenStructure) mapGenMineshaft));
                 else
                     FMLLog.severe("mineshaft generator not found in provider");
             }
         }
         // nether
         else if (chunkProvider instanceof ChunkProviderHell) {
             
             // nether fortress
             if (config.drawNether) {
                 structureMap = getPrivateStructureMap(((ChunkProviderHell)chunkProvider).genNetherBridge);
             }
         }
         else if (chunkProvider instanceof ChunkProviderEnd) {
             // end
             // ?
         }
         
         return structureMap.values();
     }
     
     private Map<ChunkCoordIntPair, StructureStart> getPrivateStructureMap(MapGenStructure mapGenStructure) {
         return (Map<ChunkCoordIntPair, StructureStart>) 
                 ReflectionHelper.getPrivateValue(
                         MapGenStructure.class, 
                        mapGenStructure, 0);
     }
 
     /**
      * Fill the structure bounding box cache, ignores already present ones.
      */
     public synchronized void update() {
         for (StructureStart structureStart : getStructures()) {
             BBoxInt structureStartBB = BBoxFactory.createBBoxInt(structureStart.getBoundingBox());
             if (!cache.containsKey(structureStartBB)) {
                 Vector<BBoxInt> componentsBB = new Vector<BBoxInt>();
                 Iterator componentsIter = structureStart.getComponents().iterator();
                 while (componentsIter.hasNext()) {
                     StructureComponent structureComponent = (StructureComponent) componentsIter.next();
                     componentsBB.add(BBoxFactory.createBBoxInt(structureComponent.getBoundingBox()));
                 }
                 cache.put(structureStartBB, componentsBB);
                 FMLLog.info("[%d] new cache entries: %d", dimensionId, componentsBB.size());
             }
         }
         
         // gather bounding boxes of dynamic villages:
         if (config.drawVillage) {  
             if (world.villageCollectionObj != null) {
                 
                 // collect all bounding boxes:
                 Set<BBoxInt> villages = new HashSet<BBoxInt>();
                 List<Village> villageInternal = world.villageCollectionObj.getVillageList();
                 for (Village village : villageInternal) {
                     ChunkCoordinates center =  ReflectionHelper.getPrivateValue(
                             Village.class, village, 3);
                     Integer radius =  ReflectionHelper.getPrivateValue(
                             Village.class, village, 4);
 
                     villages.add(new BBoxInt(
                         center.posX - radius, 
                         center.posY - 4, 
                         center.posZ - radius, 
                         center.posX + radius, 
                         center.posY + 4, 
                         center.posZ + radius
                     ));
                 }
                 
                 for (BBoxInt bb : villageCache) {
                     if (!villages.contains(bb)) {
                         // remove village bb:
                         cache.remove(bb);
                         // send remove packet:
                         PacketHandler.writeRemoveBBPacket(dimensionId, bb);
                     }
                     else {
                         if (!cache.containsKey(bb)) {
                             cache.put(bb, new Vector<BBoxInt>());
                             cache.get(bb).add(bb);
                         }
                     }
                 }
                 
                 villageCache = villages;
             }
 
         }
     }
 
     
     /**
      * Sends the cache data to the player, this is called every few
      * seconds.
      * Keeps track of data already sent and sends only data from chunks
      * the player is currently around. 
      */
     public synchronized void sendToPlayer(EntityPlayerMP player) {
         
         List<Chunk> loadedChunks = (List<Chunk>) player.loadedChunks;
         
         // collect a subset of the cache that is going to be send:
         HashMap<BBoxInt, Vector<BBoxInt>> cacheSubset = new HashMap<BBoxInt, Vector<BBoxInt>>();
 
         for (BBoxInt structureStartBB : cache.keySet()) {
             if (!cacheSent.containsKey(player) || !cacheSent.get(player).contains(structureStartBB)) {
                 for (BBoxInt componentBB : cache.get(structureStartBB)) {
                     // TODO: check here if the BB is within the loaded chunks
                     if (!cacheSubset.containsKey(structureStartBB)) cacheSubset.put(structureStartBB, new Vector<BBoxInt>());
                     cacheSubset.get(structureStartBB).add(componentBB);
                 }
             }
         }
         
         if (cacheSubset.keySet().size() > 0) {
             FMLLog.info("send %d entries to %s (%d)", cacheSubset.keySet().size(), player.username, dimensionId);
         }
 
         for (BBoxInt structureStartBB : cacheSubset.keySet()) {
             Vector<BBoxInt> componentBBList = cacheSubset.get(structureStartBB);
             PacketHandler.writeBBoxUpdatePacket(player, dimensionId, structureStartBB, componentBBList);
             //sendCacheEntryToPlayer(player, structureStartBB, componentBBList);
             
             if (!cacheSent.containsKey(player)) cacheSent.put(player,  new Vector<BBoxInt>());
             cacheSent.get(player).add(structureStartBB);
         }
     }
 
     public void merge(BBoxInt structBB, Vector<BBoxInt> bbList) {
         if (!cache.containsKey(structBB)) {
             cache.put(structBB, bbList);
         }
         else {
             for (BBoxInt bb : bbList) {
                 if (!cache.get(structBB).contains(bb)) {
                     cache.get(structBB).add(bb);
                 }
             }
         }
     }
 
     public void remove(BBoxInt bb) {
         cache.remove(bb);
     }
 }
