 package btwmods;
 
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Set;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.Block;
 import net.minecraft.src.Chunk;
 import net.minecraft.src.ChunkProviderServer;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityTracker;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.LongHashMap;
 import net.minecraft.src.Packet5PlayerInventory;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldServer;
 import btwmods.events.EventDispatcher;
 import btwmods.events.EventDispatcherFactory;
 import btwmods.events.IAPIListener;
 import btwmods.io.Settings;
 import btwmods.world.BlockEvent;
 import btwmods.world.EntityEvent;
 import btwmods.world.IBlockListener;
 import btwmods.world.IEntityListener;
 import btwmods.world.IWorldTickListener;
 import btwmods.world.WorldTickEvent;
 
 public class WorldAPI {
 	private static EventDispatcher listeners = EventDispatcherFactory.create(new Class[] { IBlockListener.class, IEntityListener.class, IWorldTickListener.class });
 	
 	private static MinecraftServer server;
 	
 	private static List[] loadedChunks;
 	private static LongHashMap[] id2ChunkMap;
 	private static Set[] droppedChunksSet;
 	private static Set[] trackedEntitiesSet;
 	
 	private WorldAPI() { }
 	
 	public static void init(@SuppressWarnings("unused") Settings settings) throws NoSuchFieldException, IllegalAccessException {
 		server = MinecraftServer.getServer();
 		
 		Field loadedChunksField = ReflectionAPI.getPrivateField(ChunkProviderServer.class, "loadedChunks");
 		Field id2ChunkMapField = ReflectionAPI.getPrivateField(ChunkProviderServer.class, "id2ChunkMap");
 		Field droppedChunksSetField = ReflectionAPI.getPrivateField(ChunkProviderServer.class, "droppedChunksSet");
 		Field trackedEntitiesSetField = ReflectionAPI.getPrivateField(EntityTracker.class, "trackedEntitySet");
 		
 		if (loadedChunksField == null)
 			throw new NoSuchFieldException("loadedChunks");
 		if (id2ChunkMapField == null)
 			throw new NoSuchFieldException("id2ChunkMap");
 		if (droppedChunksSetField == null)
 			throw new NoSuchFieldException("droppedChunksSet");
 		if (trackedEntitiesSetField == null)
 			throw new NoSuchFieldException("trackedEntitySet");
 		
 		loadedChunks = new List[server.worldServers.length];
 		id2ChunkMap = new LongHashMap[server.worldServers.length];
 		droppedChunksSet = new Set[server.worldServers.length];
 		trackedEntitiesSet = new Set[server.worldServers.length];
 		
 		for (int i = 0; i < server.worldServers.length; i++) {
 			ChunkProviderServer provider = (ChunkProviderServer)server.worldServers[i].getChunkProvider();
 			loadedChunks[i] = (List)loadedChunksField.get(provider);
 			id2ChunkMap[i] = (LongHashMap)id2ChunkMapField.get(provider);
 			droppedChunksSet[i] = (Set)droppedChunksSetField.get(provider);
 			trackedEntitiesSet[i] = (Set)trackedEntitiesSetField.get(server.worldServers[i].getEntityTracker());
 		}
 	}
 	
 	public static List[] getLoadedChunks() {
 		return loadedChunks;
 	}
 	
 	public static LongHashMap[] getCachedChunks() {
 		return id2ChunkMap;
 	}
 	
 	public static Set[] getDroppedChunks() {
 		return droppedChunksSet;
 	}
 	
 	public static Set[] getTrackedEntities() {
 		return trackedEntitiesSet;
 	}
 
 	public static void addListener(IAPIListener listener) {
 		listeners.addListener(listener);
 	}
 
 	public static void removeListener(IAPIListener listener) {
		listeners.removeListener(listener);
 	}
 
 	public static void onStartTick(int worldIndex) {
 		WorldTickEvent event = WorldTickEvent.StartTick(worldIndex);
 		((IWorldTickListener)listeners).onWorldTick(event);
 	}
 
 	public static void onEndTick(int worldIndex) {
 		WorldTickEvent event = WorldTickEvent.EndTick(worldIndex);
 		((IWorldTickListener)listeners).onWorldTick(event);
 	}
 	
 	public static void onBlockBroken(World world, Chunk chunk, Block block, int x, int y, int z, @SuppressWarnings("unused") int blockID, int blockMetadata) {
 		BlockEvent event = BlockEvent.Broken(world, chunk, block, blockMetadata, x, y, z);
 		((IBlockListener)listeners).onBlockAction(event);
 	}
 
 	public static boolean onBlockExplodeAttempt(World world, int blockId, int x, int y, int z) {
 		if (!listeners.isEmpty(IBlockListener.class)) {
 			BlockEvent event = BlockEvent.ExplodeAttempt(world, blockId, x, y, z);
 			((IBlockListener)listeners).onBlockAction(event);
 			
 			if (!event.isAllowed())
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public static void sendEntityEquipmentUpdate(EntityLiving entity) {
 		if (entity.worldObj instanceof WorldServer) {
 			WorldServer world = (WorldServer)entity.worldObj;
 			for (int slot = 0; slot < 5; ++slot) {
 				ItemStack item = entity.getEquipmentInSlot(slot);
 				world.getEntityTracker().sendPacketToTrackedPlayers(entity, new Packet5PlayerInventory(entity.entityId, slot, item));
 			}
 		}
 	}
 
 	public static boolean onCheckEntityIsInvulnerable(Entity entity) {
 		if (!listeners.isEmpty(IEntityListener.class)) {
 			EntityEvent event = EntityEvent.CheckIsEntityInvulnerable(entity);
 			((IEntityListener)listeners).onEntityAction(event);
 			
 			if (event.isInvulnerable())
 				return true;
 		}
 		
 		return false;
 	}
 
 	public static boolean onDestroyBlockWithFireAttempt(World world, int blockId, int x, int y, int z) {
 		if (!listeners.isEmpty(IBlockListener.class)) {
 			BlockEvent event = BlockEvent.BurnAttempt(world, blockId, x, y, z);
 			((IBlockListener)listeners).onBlockAction(event);
 			
 			if (!event.isAllowed())
 				return false;
 		}
 		
 		return true;
 	}
 
 	public static boolean onGetChanceOfNeighborsEncouragingFireAllowed(World world, int x, int y, int z) {
 		if (!listeners.isEmpty(IBlockListener.class)) {
 			BlockEvent event = BlockEvent.FireSpreadAttempt(world, x, y, z);
 			((IBlockListener)listeners).onBlockAction(event);
 			
 			if (!event.isAllowed())
 				return false;
 		}
 		
 		return true;
 	}
 
 	public static boolean onIsFlammableBlock(World world, int x, int y, int z) {
 		if (!listeners.isEmpty(IBlockListener.class)) {
 			BlockEvent event = BlockEvent.IsFlammableBlock(world, x, y, z);
 			((IBlockListener)listeners).onBlockAction(event);
 			
 			if (!event.isFlammable())
 				return false;
 		}
 		
 		return true;
 	}
 
 	public static boolean onCanPushBlock(World world, int orientation, int pistonX, int pistonY, int pistonZ, int blockX, int blockY, int blockZ) {
 		if (!listeners.isEmpty(IBlockListener.class)) {
 			BlockEvent event = BlockEvent.CanPushBlock(world, orientation, pistonX, pistonY, pistonZ, blockX, blockY, blockZ);
 			((IBlockListener)listeners).onBlockAction(event);
 			
 			if (!event.isAllowed())
 				return false;
 		}
 		
 		return true;
 	}
 }
