 package net.minecraftforge.common;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.logging.Level;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSetMultimap;
 import com.google.common.collect.LinkedHashMultimap;
 import com.google.common.collect.ListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.SetMultimap;
 import com.google.common.collect.Sets;
 import com.google.common.collect.TreeMultiset;
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.ModContainer;
 
 import net.minecraft.src.Chunk;
 import net.minecraft.src.ChunkCoordIntPair;
 import net.minecraft.src.CompressedStreamTools;
 import net.minecraft.src.Entity;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.NBTBase;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldServer;
 import net.minecraftforge.common.ForgeChunkManager.Ticket;
 
 /**
  * Manages chunkloading for mods.
  *
  * The basic principle is a ticket based system.
  * 1. Mods register a callback {@link #setForcedChunkLoadingCallback(Object, LoadingCallback)}
  * 2. Mods ask for a ticket {@link #requestTicket(Object, World, Type)} and then hold on to that ticket.
  * 3. Mods request chunks to stay loaded {@link #forceChunk(Ticket, ChunkCoordIntPair)} or remove chunks from force loading {@link #unforceChunk(Ticket, ChunkCoordIntPair)}.
  * 4. When a world unloads, the tickets associated with that world are saved by the chunk manager.
  * 5. When a world loads, saved tickets are offered to the mods associated with the tickets. The {@link Ticket#getModData()} that is set by the mod should be used to re-register
  * chunks to stay loaded (and maybe take other actions).
  *
  * The chunkloading is configurable at runtime. The file "config/forgeChunkLoading.cfg" contains both default configuration for chunkloading, and a sample individual mod
  * specific override section.
  *
  * @author cpw
  *
  */
 public class ForgeChunkManager
 {
     private static int defaultMaxCount;
     private static int defaultMaxChunks;
     private static boolean overridesEnabled;
 
     private static Map<World, Multimap<String, Ticket>> tickets = Maps.newHashMap();
     private static Map<String, Integer> ticketConstraints = Maps.newHashMap();
     private static Map<String, Integer> chunkConstraints = Maps.newHashMap();
 
     private static Map<String, LoadingCallback> callbacks = Maps.newHashMap();
 
     private static Map<World, SetMultimap<ChunkCoordIntPair,Ticket>> forcedChunks = Maps.newHashMap();
     private static BiMap<UUID,Ticket> pendingEntities = HashBiMap.create();
 
     private static Cache<Long, Chunk> dormantChunkCache;
     /**
      * All mods requiring chunkloading need to implement this to handle the
      * re-registration of chunk tickets at world loading time
      *
      * @author cpw
      *
      */
     public interface LoadingCallback
     {
         /**
          * Called back when tickets are loaded from the world to allow the
          * mod to re-register the chunks associated with those tickets
          *
          * @param tickets
          * @param world
          */
         public void ticketsLoaded(List<Ticket> tickets, World world);
     }
     public enum Type
     {
 
         /**
          * For non-entity registrations
          */
         NORMAL,
         /**
          * For entity registrations
          */
         ENTITY
     }
     public static class Ticket
     {
         private String modId;
         private Type ticketType;
         private LinkedHashSet<ChunkCoordIntPair> requestedChunks;
         private NBTTagCompound modData;
         private World world;
         private int maxDepth;
         private String entityClazz;
         private int entityChunkX;
         private int entityChunkZ;
         private Entity entity;
 
         Ticket(String modId, Type type, World world)
         {
             this.modId = modId;
             this.ticketType = type;
             this.world = world;
             this.maxDepth = getMaxChunkDepthFor(modId);
             this.requestedChunks = Sets.newLinkedHashSet();
         }
 
         /**
          * The chunk list depth can be manipulated up to the maximal grant allowed for the mod. This value is configurable. Once the maximum is reached,
          * the least recently forced chunk, by original registration time, is removed from the forced chunk list.
          *
          * @param depth The new depth to set
          */
         public void setChunkListDepth(int depth)
         {
             if (depth > getMaxChunkDepthFor(modId))
             {
                 FMLLog.warning("The mod %s tried to modify the chunk ticket depth to: %d, greater than it's maximum: %d", modId, depth, getMaxChunkDepthFor(modId));
             }
             else
             {
                 this.maxDepth = depth;
             }
         }
         /**
          * Get the maximum chunk depth size
          *
          * @return The maximum chunk depth size
          */
         public int getMaxChunkListDepth()
         {
             return getMaxChunkDepthFor(modId);
         }
 
         /**
          * Bind the entity to the ticket for {@link Type#ENTITY} type tickets. Other types will throw a runtime exception.
          *
          * @param entity The entity to bind
          */
         public void bindEntity(Entity entity)
         {
             if (ticketType!=Type.ENTITY)
             {
                 throw new RuntimeException("Cannot bind an entity to a non-entity ticket");
             }
             this.entity = entity;
         }
 
         /**
          * Retrieve the {@link NBTTagCompound} that stores mod specific data for the chunk ticket.
          * Example data to store would be a TileEntity or Block location. This is persisted with the ticket and
          * provided to the {@link LoadingCallback} for the mod. It is recommended to use this to recover
          * useful state information for the forced chunks.
          *
          * @return The custom compound tag for mods to store additional chunkloading data
          */
         public NBTTagCompound getModData()
         {
             if (this.modData == null)
             {
                 this.modData = new NBTTagCompound();
             }
             return modData;
         }
 
         /**
          * Get the entity associated with this {@link Type#ENTITY} type ticket
          * @return
          */
         public Entity getEntity()
         {
             return entity;
         }
     }
 
     static void loadWorld(World world)
     {
         ArrayListMultimap<String, Ticket> loadedTickets = ArrayListMultimap.<String, Ticket>create();
         tickets.put(world, loadedTickets);
 
         SetMultimap<ChunkCoordIntPair,Ticket> forcedChunkMap = LinkedHashMultimap.create();
         forcedChunks.put(world, forcedChunkMap);
 
         if (!(world instanceof WorldServer))
         {
             return;
         }
 
         WorldServer worldServer = (WorldServer) world;
         File chunkDir = worldServer.getChunkSaveLocation();
         File chunkLoaderData = new File(chunkDir, "forcedchunks.dat");
 
         if (chunkLoaderData.exists() && chunkLoaderData.isFile())
         {
             NBTTagCompound forcedChunkData;
             try
             {
                 forcedChunkData = CompressedStreamTools.read(chunkLoaderData);
             }
             catch (IOException e)
             {
                 FMLLog.log(Level.WARNING, e, "Unable to read forced chunk data at %s - it will be ignored", chunkLoaderData.getAbsolutePath());
                 return;
             }
             NBTTagList ticketList = forcedChunkData.getTagList("TicketList");
             for (int i = 0; i < ticketList.tagCount(); i++)
             {
                 NBTTagCompound ticketHolder = (NBTTagCompound) ticketList.tagAt(i);
                 String modId = ticketHolder.getString("Owner");
 
                 if (!Loader.isModLoaded(modId))
                 {
                     FMLLog.warning("Found chunkloading data for mod %s which is currently not available or active - it will be removed from the world save", modId);
                     continue;
                 }
 
                 if (!callbacks.containsKey(modId))
                 {
                     FMLLog.warning("The mod %s has registered persistent chunkloading data but doesn't seem to want to be called back with it - it will be removed from the world save", modId);
                     continue;
                 }
 
                 int maxTicketLength = getMaxTicketLengthFor(modId);
 
                 NBTTagList tickets = ticketHolder.getTagList("Tickets");
                 if (tickets.tagCount() > maxTicketLength)
                 {
                     FMLLog.warning("The mod %s has more tickets in to load than it is allowed. Only the first %d will be loaded - the rest will be removed", modId, maxTicketLength);
                 }
                 for (int j = 0; j < Math.min(tickets.tagCount(), maxTicketLength); j++)
                 {
                     NBTTagCompound ticket = (NBTTagCompound) tickets.tagAt(j);
                     Type type = Type.values()[ticket.getByte("Type")];
                     byte ticketChunkDepth = ticket.getByte("ChunkListDepth");
                     NBTTagCompound modData = ticket.getCompoundTag("ModData");
                     Ticket tick = new Ticket(modId, type, world);
                     tick.modData = modData;
                     if (type == Type.ENTITY)
                     {
                         tick.entityChunkX = ticket.getInteger("chunkX");
                         tick.entityChunkZ = ticket.getInteger("chunkZ");
                         UUID uuid = new UUID(ticket.getLong("PersistentIDMSB"), ticket.getLong("PersistentIDLSB"));
                         // add the ticket to the "pending entity" list
                         pendingEntities.put(uuid, tick);
                     }
                     loadedTickets.put(modId, tick);
                 }
             }
 
             for (Ticket tick : ImmutableSet.copyOf(pendingEntities.values()))
             {
                 if (tick.ticketType == Type.ENTITY && tick.entity == null)
                 {
                     // force the world to load the entity's chunk
                     // the load will come back through the loadEntity method and attach the entity
                     // to the ticket
                     world.getChunkFromChunkCoords(tick.entityChunkX, tick.entityChunkZ);
                 }
             }
             for (Ticket tick : ImmutableSet.copyOf(pendingEntities.values()))
             {
                 if (tick.ticketType == Type.ENTITY && tick.entity == null)
                 {
                     FMLLog.warning("Failed to load persistent chunkloading entity %s from store.", pendingEntities.inverse().get(tick));
                     loadedTickets.remove(tick.modId, tick);
                 }
             }
             pendingEntities.clear();
             // send callbacks
             for (String modId : loadedTickets.keySet())
             {
                 callbacks.get(modId).ticketsLoaded(loadedTickets.get(modId), world);
             }
         }
     }
 
     /**
      * Set a chunkloading callback for the supplied mod object
      *
      * @param mod  The mod instance registering the callback
      * @param callback The code to call back when forced chunks are loaded
      */
     public static void setForcedChunkLoadingCallback(Object mod, LoadingCallback callback)
     {
         ModContainer container = getContainer(mod);
         if (container == null)
         {
             FMLLog.warning("Unable to register a callback for an unknown mod %s (%s : %x)", mod, mod.getClass().getName(), System.identityHashCode(mod));
             return;
         }
 
         callbacks.put(container.getModId(), callback);
     }
 
     /**
      * Discover the available tickets for the mod in the world
      *
      * @param mod The mod that will own the tickets
      * @param world The world
      * @return The count of tickets left for the mod in the supplied world
      */
     public static int ticketCountAvailableFor(Object mod, World world)
     {
         ModContainer container = getContainer(mod);
         if (container!=null)
         {
             String modId = container.getModId();
             int allowedCount = getMaxTicketLengthFor(modId);
             return allowedCount - tickets.get(world).get(modId).size();
         }
         else
         {
             return 0;
         }
     }
 
     private static ModContainer getContainer(Object mod)
     {
         ModContainer container = Loader.instance().getModObjectList().inverse().get(mod);
         return container;
     }
 
     private static int getMaxTicketLengthFor(String modId)
     {
         int allowedCount = ticketConstraints.containsKey(modId) && overridesEnabled ? ticketConstraints.get(modId) : defaultMaxCount;
         return allowedCount;
     }
 
     private static int getMaxChunkDepthFor(String modId)
     {
         int allowedCount = chunkConstraints.containsKey(modId) && overridesEnabled ? chunkConstraints.get(modId) : defaultMaxChunks;
         return allowedCount;
     }
     /**
      * Request a chunkloading ticket of the appropriate type for the supplied mod
      *
      * @param mod The mod requesting a ticket
      * @param world The world in which it is requesting the ticket
      * @param type The type of ticket
      * @return A ticket with which to register chunks for loading, or null if no further tickets are available
      */
     public static Ticket requestTicket(Object mod, World world, Type type)
     {
         ModContainer container = getContainer(mod);
         if (container == null)
         {
             FMLLog.log(Level.SEVERE, "Failed to locate the container for mod instance %s (%s : %x)", mod, mod.getClass().getName(), System.identityHashCode(mod));
             return null;
         }
         String modId = container.getModId();
         if (!callbacks.containsKey(modId))
         {
             FMLLog.severe("The mod %s has attempted to request a ticket without a listener in place", modId);
             throw new RuntimeException("Invalid ticket request");
         }
 
         int allowedCount = ticketConstraints.containsKey(modId) ? ticketConstraints.get(modId) : defaultMaxCount;
 
         if (tickets.get(world).get(modId).size() >= allowedCount)
         {
             FMLLog.info("The mod %s has attempted to allocate a chunkloading ticket beyond it's currently allocated maximum : %d", modId, allowedCount);
             return null;
         }
         Ticket ticket = new Ticket(modId, type, world);
         tickets.get(world).put(modId, ticket);
 
         return ticket;
     }
 
     /**
      * Release the ticket back to the system. This will also unforce any chunks held by the ticket so that they can be unloaded and/or stop ticking.
      *
      * @param ticket The ticket to release
      */
     public static void releaseTicket(Ticket ticket)
     {
         if (ticket == null)
         {
             return;
         }
         if (ticket.requestedChunks!=null)
         {
             for (ChunkCoordIntPair chunk : ImmutableSet.copyOf(ticket.requestedChunks))
             {
                 unforceChunk(ticket, chunk);
             }
         }
         tickets.get(ticket.world).remove(ticket.modId, ticket);
     }
 
     /**
      * Force the supplied chunk coordinate to be loaded by the supplied ticket. If the ticket's {@link Ticket#maxDepth} is exceeded, the least
      * recently registered chunk is unforced and may be unloaded.
      * It is safe to force the chunk several times for a ticket, it will not generate duplication or change the ordering.
      *
      * @param ticket The ticket registering the chunk
      * @param chunk The chunk to force
      */
     public static void forceChunk(Ticket ticket, ChunkCoordIntPair chunk)
     {
         if (ticket == null || chunk == null)
         {
             return;
         }
         if (ticket.ticketType == Type.ENTITY && ticket.entity == null)
         {
             throw new RuntimeException("Attempted to use an entity ticket to force a chunk, without an entity");
         }
         ticket.requestedChunks.add(chunk);
         forcedChunks.get(ticket.world).put(chunk, ticket);
         if (ticket.maxDepth > 0 && ticket.requestedChunks.size() > ticket.maxDepth)
         {
             ChunkCoordIntPair removed = ticket.requestedChunks.iterator().next();
             unforceChunk(ticket,removed);
         }
     }
 
     /**
      * Unforce the supplied chunk, allowing it to be unloaded and stop ticking.
      *
      * @param ticket The ticket holding the chunk
      * @param chunk The chunk to unforce
      */
     public static void unforceChunk(Ticket ticket, ChunkCoordIntPair chunk)
     {
         if (ticket == null || chunk == null)
         {
             return;
         }
         ticket.requestedChunks.remove(chunk);
         forcedChunks.get(ticket.world).remove(chunk, ticket);
     }
 
     static void loadConfiguration(File configDir)
     {
         Configuration config = new Configuration(new File(configDir,"forgeChunkLoading.cfg"), true);
         try
         {
             config.categories.clear();
             config.load();
             config.addCustomCategoryComment("defaults", "Default configuration for forge chunk loading control");
             Property maxTicketCount = config.getOrCreateIntProperty("maximumTicketCount", "defaults", 200);
             maxTicketCount.comment = "The default maximum ticket count for a mod which does not have an override\n" +
             		"in this file. This is the number of chunk loading requests a mod is allowed to make.";
             defaultMaxCount = maxTicketCount.getInt(200);
 
             Property maxChunks = config.getOrCreateIntProperty("maximumChunksPerTicket", "defaults", 25);
             maxChunks.comment = "The default maximum number of chunks a mod can force, per ticket, \n" +
             		"for a mod without an override. This is the maximum number of chunks a single ticket can force.";
             defaultMaxChunks = maxChunks.getInt(25);
 
             Property dormantChunkCacheSize = config.getOrCreateIntProperty("dormantChunkCacheSize", "defaults", 0);
             dormantChunkCacheSize.comment = "Unloaded chunks can first be kept in a dormant cache for quicker\n" +
             		"loading times. Specify the size of that cache here";
             dormantChunkCache = CacheBuilder.newBuilder().maximumSize(dormantChunkCacheSize.getInt(0)).build();
 
             Property modOverridesEnabled = config.getOrCreateBooleanProperty("enabled", "defaults", true);
             modOverridesEnabled.comment = "Are mod overrides enabled?";
             overridesEnabled = modOverridesEnabled.getBoolean(true);
 
             config.addCustomCategoryComment("Forge", "Sample mod specific control section.\n" +
                     "Copy this section and rename the with the modid for the mod you wish to override.\n" +
                     "A value of zero in either entry effectively disables any chunkloading capabilities\n" +
                     "for that mod");
 
             Property sampleTC = config.getOrCreateIntProperty("maximumTicketCount", "Forge", 200);
             sampleTC.comment = "Maximum ticket count for the mod. Zero disables chunkloading capabilities.";
             sampleTC = config.getOrCreateIntProperty("maximumChunksPerTicket", "Forge", 25);
             sampleTC.comment = "Maximum chunks per ticket for the mod.";
             for (String mod : config.categories.keySet())
             {
                 if (mod.equals("Forge") || mod.equals("defaults"))
                 {
                     continue;
                 }
                 Property modTC = config.getOrCreateIntProperty("maximumTicketCount", mod, 200);
                 Property modCPT = config.getOrCreateIntProperty("maximumChunksPerTicket", mod, 25);
                 ticketConstraints.put(mod, modTC.getInt(200));
                 chunkConstraints.put(mod, modCPT.getInt(25));
             }
         }
         finally
         {
             config.save();
         }
     }
 
     /**
      * The list of persistent chunks in the world. This set is immutable.
      * @param world
      * @return
      */
     public static SetMultimap<ChunkCoordIntPair, Ticket> getPersistentChunksFor(World world)
     {
        return ImmutableSetMultimap.copyOf(forcedChunks.get(world));
     }
 
     static void saveWorld(World world)
     {
         // only persist persistent worlds
         if (!(world instanceof WorldServer)) { return; }
         WorldServer worldServer = (WorldServer) world;
         File chunkDir = worldServer.getChunkSaveLocation();
         File chunkLoaderData = new File(chunkDir, "forcedchunks.dat");
 
         NBTTagCompound forcedChunkData = new NBTTagCompound();
         NBTTagList ticketList = new NBTTagList();
         forcedChunkData.setTag("TicketList", ticketList);
 
         Multimap<String, Ticket> ticketSet = tickets.get(worldServer);
         for (String modId : ticketSet.keySet())
         {
             NBTTagCompound ticketHolder = new NBTTagCompound();
             ticketList.appendTag(ticketHolder);
 
             ticketHolder.setString("Owner", modId);
             NBTTagList tickets = new NBTTagList();
             ticketHolder.setTag("Tickets", tickets);
 
             for (Ticket tick : ticketSet.get(modId))
             {
                 NBTTagCompound ticket = new NBTTagCompound();
                 tickets.appendTag(ticket);
                 ticket.setByte("Type", (byte) tick.ticketType.ordinal());
                 ticket.setByte("ChunkListDepth", (byte) tick.maxDepth);
                 ticket.setCompoundTag("ModData", tick.modData);
                 if (tick.ticketType == Type.ENTITY)
                 {
                     ticket.setInteger("chunkX", MathHelper.floor_double(tick.entity.chunkCoordX));
                     ticket.setInteger("chunkZ", MathHelper.floor_double(tick.entity.chunkCoordZ));
                     ticket.setLong("persistentIDMSB", tick.entity.getPersistentID().getMostSignificantBits());
                     ticket.setLong("persistentIDLSB", tick.entity.getPersistentID().getLeastSignificantBits());
                 }
             }
         }
         try
         {
             CompressedStreamTools.write(forcedChunkData, chunkLoaderData);
         }
         catch (IOException e)
         {
             FMLLog.log(Level.WARNING, e, "Unable to write forced chunk data to %s - chunkloading won't work", chunkLoaderData.getAbsolutePath());
             return;
         }
     }
 
     static void loadEntity(Entity entity)
     {
         UUID id = entity.getPersistentID();
         Ticket tick = pendingEntities.get(id);
         if (tick != null)
         {
             tick.bindEntity(entity);
             pendingEntities.remove(id);
         }
     }
 
     public static void putDormantChunk(long coords, Chunk chunk)
     {
         dormantChunkCache.put(coords, chunk);
     }
 
     public static Chunk fetchDormantChunk(long coords)
     {
         return dormantChunkCache.getIfPresent(coords);
     }
 }
