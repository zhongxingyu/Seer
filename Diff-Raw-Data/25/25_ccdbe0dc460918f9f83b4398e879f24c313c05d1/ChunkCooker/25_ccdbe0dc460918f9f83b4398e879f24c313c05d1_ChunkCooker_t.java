 package com.mikeprimm.bukkit.ChunkCooker;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChunkCooker extends JavaPlugin {
     public Logger log;
     
     private static final int COOKER_PERIOD_INC = 5; // 1/4 second
     
     private int chunk_tick_interval = 1; // Every tick
     private boolean verbose = false;
     private ArrayList<World.Environment> worldenv;
         
     private static class UseCount {
         int cnt; // Number of ticking neighbors loaded
         boolean isTicking;
     }
     
     private Field chunkTickList;
     private Method chunkTickListPut;
     private Method cw_gethandle;
     private boolean isSpigotStyleChunkTickList;
     
     private class WorldHandler {
         private final World world;
         private final int cooker_period; // 30 seconds
         private final int chunks_per_period;
         private final boolean storm_on_empty;
         private final int maxLoadsPerTick;
         private final int minCycleTime;
 
         private final HashMap<TileFlags.TileCoord, UseCount> loadedChunks = new HashMap<TileFlags.TileCoord, UseCount>();
         private final TileFlags.TileCoord[][] tickingQueue;
         private int tickingQueueIndex;
         private long cycleStart;
         private int endIndex;
         
         private final TileFlags chunkmap = new TileFlags();
         private TileFlags.Iterator iter;
         private boolean stormset;
 
         WorldHandler(FileConfiguration cfg, World w) {
             world = w;
             ConfigurationSection sec = cfg.getConfigurationSection("worlds." + w.getName());
             int cpp = cfg.getInt("chunks-per-period", 100);
             int cp = cfg.getInt("seconds-per-period", 30);
             boolean soe = cfg.getBoolean("storm-on-empty-world", true);
             int mincycle = cfg.getInt("minimum-cycle-time", 3600);
             if (sec != null) {
                 cpp = sec.getInt("chunks-per-period", cpp);
                 cp = sec.getInt("seconds-per-period", cp);
                 soe = sec.getBoolean("storm-on-empty-world", soe);
                 mincycle = sec.getInt("minimum-cycle-time", mincycle);
             }
             chunks_per_period = cpp;
             cooker_period = cp;
             storm_on_empty = soe;
             minCycleTime = mincycle;
             // Initialize chunk queue : enough buckets for giving chunks enough time
             tickingQueue = new TileFlags.TileCoord[cooker_period * 20 / COOKER_PERIOD_INC][];
             tickingQueueIndex = 0;
             
             if (tickingQueue.length > 0)
                 maxLoadsPerTick = 1 + (chunks_per_period / tickingQueue.length);
             else
                 maxLoadsPerTick = 1;
             cycleStart = 0;
             log.info("World '" + w.getName() + "': chunks=" + chunks_per_period + ", period=" + cooker_period + ", storm=" + storm_on_empty + ", mintime=" + minCycleTime);
         }
         void tickCooker() {
             TileFlags.TileCoord tc = new TileFlags.TileCoord();
             // See if any chunks due to be unloaded
             if (tickingQueue[tickingQueueIndex] != null) {
                 TileFlags.TileCoord[] chunkToUnload = tickingQueue[tickingQueueIndex];
                 tickingQueue[tickingQueueIndex] = null;
                 // Unload the ticking chunks
                 for (TileFlags.TileCoord c : chunkToUnload) {
                     UseCount cnt = loadedChunks.get(c);
                     if (cnt != null) {
                         cnt.isTicking = false;
                         // Now, decrement all loaded neighbors (and self)
                         for (tc.x = c.x - 1; tc.x <= c.x + 1; tc.x++) {
                             for (tc.y = c.y - 1; tc.y <= c.y + 1; tc.y++) {
                                 UseCount ncnt = loadedChunks.get(tc);
                                 if (ncnt != null) {
                                     ncnt.cnt--; // Drop neighbor count
                                     if ((ncnt.cnt == 0) && (ncnt.isTicking == false)) { // No neighbors nor ticking
                                         loadedChunks.remove(tc); // Remove from set
                                         // And unload it, if its not in use
                                         if (world.isChunkInUse(tc.x, tc.y) == false) {
                                             world.unloadChunkRequest(tc.x, tc.y);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
            if ((iter != null) && (iter.hasNext() == false) && (tickingQueueIndex == endIndex)) { // Are we done with world?
                log.info("World '" + world.getName() + "' - chunk cooking completed");
                iter = null;
                endIndex = -1;
            }
             if (iter == null) {
                 // If first cycle, or we're due for next cycle
                 if ((cycleStart == 0) || (((System.nanoTime() - cycleStart) / 1000000000L) > minCycleTime)) {
                     // Now, get current chunk map for world
                     int ccnt = getChunkMap(world, chunkmap);
                    log.info(String.format("Starting cook pass for world '%s' - %d existing chunks (estimated time: %.2f hrs, minimum period: %.2f hrs)",
                            world.getName(), ccnt, (double)(ccnt * cooker_period * 3.0) / (double)chunks_per_period / 3600.0,
                            (double) minCycleTime / 3600.0));
                     iter = chunkmap.getIterator();  // Get iterator
                     cycleStart = System.nanoTime();
                    endIndex = -1;
                 }
                 else {
                     return;
                 }
             }
             // Now, load next N chunks (and their neighbors)
             ArrayList<TileFlags.TileCoord> newticking = null;
             int newloads = 0;
             while(iter.hasNext() && (loadedChunks.size() < chunks_per_period) && (newloads < maxLoadsPerTick)) {
                 iter.next(tc);
                 chunkmap.setFlag(tc.x,  tc.y, false);
                 int x0 = tc.x;
                 int z0 = tc.y;
                 // Try to load chunk, and its 8 neighbors
                 for (tc.x = x0 - 1; tc.x <= x0 + 1; tc.x++) {
                     for (tc.y = z0 - 1; tc.y <= z0 + 1; tc.y++) {
                         UseCount cnt = loadedChunks.get(tc);    // See if loaded
                         if (cnt == null) {  // Not yet, load it */
                             if (world.loadChunk(tc.x, tc.y, false)) {
                                 cnt = new UseCount();
                                 loadedChunks.put(new TileFlags.TileCoord(tc.x, tc.y), cnt);
                             }
                             newloads++;
                         }
                         if (cnt != null) {
                             cnt.cnt++;  // Bump count
                             if ((tc.x == x0) && (tc.y == z0)) { // If the ticking chunk, set it
                                 cnt.isTicking = true;
                                 if (newticking == null)
                                     newticking = new ArrayList<TileFlags.TileCoord>();
                                 newticking.add(new TileFlags.TileCoord(tc.x, tc.y));
                             }
                         }
                     }
                 }
             }
             // Add new ticking chunks to queue
             if (newticking != null) {
                 tickingQueue[tickingQueueIndex] = newticking.toArray(new TileFlags.TileCoord[0]);
             }
             else {
                 tickingQueue[tickingQueueIndex] = null;
             }
             // If iterator is exhausted, done with current world
            if ((iter != null) && (iter.hasNext() == false) && (endIndex == -1)) {
                log.info("World '" + world.getName() + "' done - finising pending chunks");
                 endIndex = tickingQueueIndex;
             }
             // Increment to next index
             tickingQueueIndex++;
             if (tickingQueueIndex >= tickingQueue.length) {
                 tickingQueueIndex = 0;
             }
             
             if (storm_on_empty) {
                 if (world.getPlayers().isEmpty()) { // If world is empty
                     if (world.hasStorm() == false) {
                         world.setStorm(true);
                         stormset = true;
                         if(verbose)
                             log.info("Setting storm on empty world '" + world.getName() + "'");
                     }
                 }
                 else {
                     if (stormset) {
                         world.setStorm(false);
                         stormset = false;
                     }
                 }
             }
         }
         private void putChunkTickList() {
             Exception xx = null;
             try {
                 if ((chunkTickList != null) && (world != null)) {
                     Object w = cw_gethandle.invoke(world);
                     Object lst = chunkTickList.get(w);
                     if (lst != null) {
                         for (TileFlags.TileCoord[] ticklist : this.tickingQueue) {
                             if (ticklist == null) continue;
                             for (TileFlags.TileCoord tc : ticklist) {
                                 int x = tc.x;
                                 int z = tc.y;
                                 if (isSpigotStyleChunkTickList) {
                                     Long k = ((((long)x) & 0xFFFF0000L) << 16) | ((((long)x) & 0x0000FFFFL) << 0);
                                     k |= ((((long)z) & 0xFFFF0000L) << 32) | ((((long)z) & 0x0000FFFFL) << 16);
                                     chunkTickListPut.invoke(lst, k, Short.valueOf((short)-1));
                                 }
                                 else {
                                     Long v = ((long) x << 32) + z - Integer.MIN_VALUE;
                                     chunkTickListPut.invoke(lst, v);
                                 }
                             }
                         }
                     }
                     else {
                         log.info("No chunkTickQueue");
                         chunkTickList = null;
                     }
                 }
             } catch (IllegalArgumentException e) {
                 xx = e;
             } catch (IllegalAccessException e) {
                 xx = e;
             } catch (InvocationTargetException e) {
                 xx = e;
             }
             if (xx != null) {
                 log.log(Level.INFO,  "Cannot send ticks", xx);
                 chunkTickList = null;
             }
         }
         public void cleanup() {
             iter = null;
             loadedChunks.clear();
         }
     }
     
     private ArrayList<WorldHandler> worlds = new ArrayList<WorldHandler>();
     
     
     private void tickChunks() {
         for (WorldHandler wh : worlds) {
             wh.putChunkTickList();
         }
     }
     
     private String getNMSPackage() {
         Server srv = Bukkit.getServer();
         /* Get getHandle() method */
         try {
             Method m = srv.getClass().getMethod("getHandle");
             Object scm = m.invoke(srv); /* And use it to get SCM (nms object) */
             return scm.getClass().getPackage().getName();
         } catch (Exception x) {
             log.severe("Error finding net.minecraft.server packages");
             return null;
         }
     }
 
     private void findChunkTickListFields() {
         String nms = this.getNMSPackage();
         String obc = Bukkit.getServer().getClass().getPackage().getName();
         boolean good = false;
         Exception x = null;
         try {
             Class<?> craftworld = Class.forName(obc + ".CraftWorld");
             cw_gethandle = craftworld.getMethod("getHandle", new Class[0]);
             
             Class<?> cls = Class.forName(nms + ".World");
             chunkTickList = cls.getDeclaredField("chunkTickList");
             if (chunkTickList == null) {
                 log.info("Cannot find chunkTickList: cannot tick chunks");
                 chunk_tick_interval = 0;
                 return;
             }
             chunkTickList.setAccessible(true);
             // If LongHashSet, its bukkit style
             Class<?> ctlclass = chunkTickList.getType();
             String clsname = ctlclass.getName();
             if (clsname.endsWith("LongHashSet")) {
                 chunkTickListPut = ctlclass.getMethod("add", new Class[] { long.class });
                 good = true;
             }
             else if (clsname.endsWith("TLongShortHashMap")) {
                 isSpigotStyleChunkTickList = true;
                 chunkTickListPut = ctlclass.getMethod("put", new Class[] { long.class, short.class });
                 good = true;
             }
         } catch (ClassNotFoundException e) {
             x = e;
         } catch (SecurityException e) {
             x = e;
         } catch (NoSuchFieldException e) {
             x = e;
         } catch (NoSuchMethodException e) {
             x = e;
         } finally {
             if (!good) {
                 if (x != null)
                     log.log(Level.INFO, "Cannot find chunkTickList: cannot tick chunks", x);
                 else
                     log.info("Cannot find chunkTickList: cannot tick chunks");
                 chunk_tick_interval = 0;
                 chunkTickList = null;
             }
         }
     }
     
     public WorldHandler findWorldHandler(World w) {
         for (WorldHandler wh : worlds) {
             if (wh.world == w) {
                 return wh;
             }
         }
         return null;
     }
 
     /* On disable, stop doing our function */
     public void onDisable() {
         
     }
 
     public void onEnable() {
         log = this.getLogger();
         
         log.info("ChunkCooker v" + this.getDescription().getVersion() + " loaded");
         
         FileConfiguration cfg = getConfig();
         cfg.options().copyDefaults(true);   /* Load defaults, if needed */
         this.saveConfig();  /* Save updates, if needed */
 
         worldenv = new ArrayList<World.Environment>();
         List<String> we = cfg.getStringList("world-env");
         if (we != null) {
             for (String env : we) {
                 World.Environment envval = World.Environment.valueOf(env);
                 if (envval != null) {
                     worldenv.add(envval);
                 }
             }
         }
         else {
             worldenv.add(World.Environment.NORMAL);
         }
         
         chunk_tick_interval = cfg.getInt("chunk-tick-interval", 1);
         verbose = cfg.getBoolean("verbose", false);
         // See if we can tick chunk fields
         if (chunk_tick_interval > 0) {
             findChunkTickListFields();
         }
         
         this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 for (int i = 0; i < worlds.size(); i++) {
                     worlds.get(i).tickCooker();
                 }
             }
         }, COOKER_PERIOD_INC, COOKER_PERIOD_INC);
 
         if (chunk_tick_interval > 0) {
             this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                 public void run() {
                     tickChunks();
                 }
             }, chunk_tick_interval, chunk_tick_interval);
         }
         
         // Initialize worlds
         for (World w : this.getServer().getWorlds()) {
             if (worldenv.contains(w.getEnvironment())) {
                 WorldHandler wh = new WorldHandler(ChunkCooker.this.getConfig(), w);
                 worlds.add(wh);
             }
         }
         // Load listener
         Listener pl = new Listener() {
             @EventHandler(priority=EventPriority.NORMAL)
             public void onChunkUnload(ChunkUnloadEvent evt) {
                 if(evt.isCancelled()) return;
                 Chunk c = evt.getChunk();
                 WorldHandler wh = findWorldHandler(c.getWorld()) ;
                 if(wh != null) {
                     TileFlags.TileCoord tc = new TileFlags.TileCoord(c.getX(), c.getZ());
                     if (wh.loadedChunks.containsKey(tc)) { // If loaded, cancel unload
                         evt.setCancelled(true);
                     }
                 }
             }
             @EventHandler(priority=EventPriority.MONITOR)
             public void onWorldUnload(WorldUnloadEvent evt) {
                 if (evt.isCancelled()) return;
                 WorldHandler wh = findWorldHandler(evt.getWorld());
                 if (wh != null) {
                     worlds.remove(wh);
                     log.info("World '" + wh.world.getName() + "' unloaded - chunk cooking cancelled");
                     wh.cleanup();
                 }
             }
             @EventHandler(priority=EventPriority.MONITOR)
             public void onWorldLoad(WorldLoadEvent evt) {
                 World w = evt.getWorld();
                 if (worldenv.contains(w.getEnvironment()) == false) {
                     return;
                 }
                 WorldHandler wh = findWorldHandler(w);
                 if (wh == null) {
                     wh = new WorldHandler(ChunkCooker.this.getConfig(), w);
                     worlds.add(wh);
                 }
             }
         };
         getServer().getPluginManager().registerEvents(pl, this);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         return false;
     }
 
     public int getChunkMap(World world, TileFlags map) {
         map.clear();
         if (world == null) return -1;
         int cnt = 0;
         // Mark loaded chunks
         for(Chunk c : world.getLoadedChunks()) {
             map.setFlag(c.getX(), c.getZ(), true);
             cnt++;
         }
         File f = world.getWorldFolder();
         File regiondir = new File(f, "region");
         File[] lst = regiondir.listFiles();
         if(lst != null) {
             byte[] hdr = new byte[4096];
             for(File rf : lst) {
                 if(!rf.getName().endsWith(".mca")) {
                     continue;
                 }
                 String[] parts = rf.getName().split("\\.");
                 if((!parts[0].equals("r")) && (parts.length != 4)) continue;
                 
                 RandomAccessFile rfile = null;
                 int x = 0, z = 0;
                 try {
                     x = Integer.parseInt(parts[1]);
                     z = Integer.parseInt(parts[2]);
                     rfile = new RandomAccessFile(rf, "r");
                     rfile.read(hdr, 0, hdr.length);
                 } catch (IOException iox) {
                     Arrays.fill(hdr,  (byte)0);
                 } catch (NumberFormatException nfx) {
                     Arrays.fill(hdr,  (byte)0);
                 } finally {
                     if(rfile != null) {
                         try { rfile.close(); } catch (IOException iox) {}
                     }
                 }
                 for (int i = 0; i < 1024; i++) {
                     int v = hdr[4*i] | hdr[4*i + 1] | hdr[4*i + 2] | hdr[4*i + 3];
                     if (v == 0) continue;
                     int xx = (x << 5) | (i & 0x1F);
                     int zz = (z << 5) | ((i >> 5) & 0x1F);
                     if (!map.setFlag(xx, zz, true)) {
                         cnt++;
                     }
                 }
             }
         }
         return cnt;
     }
 
 }
