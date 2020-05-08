 package com.minecarts.miraclegrow;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.text.MessageFormat;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.ConfigurationSection;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 
 import com.minecarts.dbquery.DBQuery;
 import com.minecarts.dbconnector.provider.Provider;
 
 import com.minecarts.miraclegrow.BlockStateRestore.Cause;
 import com.minecarts.miraclegrow.listener.*;
 import org.bukkit.event.Listener;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.World;
 import org.bukkit.Chunk;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Event.Priority;
 import static org.bukkit.event.Event.Type.*;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Arrays;
 import org.bukkit.Material;
 
 
 public class MiracleGrow extends org.bukkit.plugin.java.JavaPlugin {
     private static final Logger logger = Logger.getLogger("com.minecarts.miraclegrow"); 
     
     protected boolean debug;
     protected FileConfiguration config;
     protected HashMap<World, ConfigurationSection> worlds = new HashMap<World, ConfigurationSection>();
     
     protected DBQuery dbq;
     protected Provider provider;
     
     protected boolean flush;
     protected int flushInterval;
     protected boolean restore;
     protected int restoreInterval;
     protected int restoreJobSize;
     protected boolean callEvents;
     
     protected HashMap<World, HashSet<BlockStateRestore>> queue = new HashMap<World, HashSet<BlockStateRestore>>();
     protected ArrayList<World> flushing = new ArrayList<World>();
     protected ArrayList<World> restoring = new ArrayList<World>();
     
     
     public void onEnable() {
         dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
         reloadConfig();
         
         // internal plugin commands
         getCommand("miraclegrow").setExecutor(new CommandExecutor() {
             public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                 if(!sender.hasPermission("miraclegrow.reload")) return true; // "hide" command output for nonpermissibles
                 
                 if(args[0].equalsIgnoreCase("reload")) {
                     MiracleGrow.this.reloadConfig();
                     sender.sendMessage("MiracleGrow config reloaded.");
                     return true;
                 }
                 
                 return false;
             }
         });
         
         // register event listeners
         PluginManager pluginManager = getServer().getPluginManager();
         HashMap<Listener, Type[]> listeners = new HashMap<Listener, Type[]>() {{
             put(new ServerListener(MiracleGrow.this), new Type[]{ PLUGIN_DISABLE });
             put(new WorldListener(MiracleGrow.this), new Type[]{ PORTAL_CREATE, STRUCTURE_GROW });
             put(new BlockListener(MiracleGrow.this), new Type[]{ BLOCK_PLACE, BLOCK_BREAK, BLOCK_FADE, BLOCK_FORM, BLOCK_SPREAD, BLOCK_FROMTO, LEAVES_DECAY, BLOCK_IGNITE, BLOCK_BURN, BLOCK_PISTON_EXTEND, BLOCK_PISTON_RETRACT });
             put(new EntityListener(MiracleGrow.this), new Type[]{ ENTITY_EXPLODE, ENDERMAN_PICKUP, ENDERMAN_PLACE });
         }};
         
         for(Entry<Listener, Type[]> entry : listeners.entrySet()) {
             for(Type type : entry.getValue()) {
                 pluginManager.registerEvent(type, entry.getKey(), Priority.Monitor, this);
             }
         }
         
         
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             public void run() {
                 if(flush) flushQueue();
                 getServer().getScheduler().scheduleSyncDelayedTask(MiracleGrow.this, this, flushInterval);
             }
         }, flushInterval);
         
         
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             public void run() {
                 if(restore) restoreBlocks();
                 getServer().getScheduler().scheduleSyncDelayedTask(MiracleGrow.this, this, restoreInterval);
             }
         }, restoreInterval);
         
         
 
         log("Version {0} enabled.", getDescription().getVersion());
     }
     
     public void onDisable() {
         flushQueue(false);
     }
     
     
     @Override
     public void reloadConfig() {
         super.reloadConfig();
         
         if(config == null) config = getConfig();
         
         debug = config.getBoolean("debug");
         provider = dbq.getProvider(config.getString("DBConnector.provider"));
         
         flush = !config.getBoolean("flush.disable");
         flushInterval = Math.max(20, 20 * config.getInt("flush.interval"));
         debug("Flushing block restore queue to database every {0} ticks", flushInterval);
         
         restore = !config.getBoolean("restore.disable");
         restoreInterval = Math.max(20, 20 * config.getInt("restore.interval"));
         restoreJobSize = Math.max(1, config.getInt("restore.jobSize"));
         debug("Restoring {1} blocks from database every {0} ticks", restoreInterval, restoreJobSize);
         
         
         callEvents = config.getBoolean("callEvents");
         if(!callEvents) log("Events will NOT be called!");
         
         
         ConfigurationSection worldsConfig = config.getConfigurationSection("worlds");
         worlds.clear();
         
         if(worlds != null) {
             for(World world : getServer().getWorlds()) {
                 ConfigurationSection worldConfig = worldsConfig.getConfigurationSection(world.getName());
                 if(worldConfig == null) continue;
                 
                 worlds.put(world, worldConfig);
             }
         }
     }
     
     
     public void log(String message) {
         log(Level.INFO, message);
     }
     public void log(Level level, String message) {
         logger.log(level, MessageFormat.format("{0}> {1}", getDescription().getName(), message));
     }
     public void log(String message, Object... args) {
         log(MessageFormat.format(message, args));
     }
     public void log(Level level, String message, Object... args) {
         log(level, MessageFormat.format(message, args));
     }
     
     public void debug(String message) {
         if(debug) log(message);
     }
     public void debug(String message, Object... args) {
         if(debug) log(message, args);
     }
     
     
     
     public String getTableName(World world, String id) {
         ConfigurationSection worldConfig = worlds.get(world);
         if(worldConfig == null) return null;
         
         String table = worldConfig.getString("tables." + id, null);
         if(table != null) return table;
         
         table = worldConfig.getString("table", null);
         if(table == null) return null;
         
         return String.format(config.getString("tables." + id), table);
     }
     
     
     
     public void scheduleRestore(Block block) {
         scheduleRestore(block.getState());
     }
     public void scheduleRestore(Block block, Cause cause) {
         scheduleRestore(block.getState(), cause);
     }
     public void scheduleRestore(Block block, int seconds) {
         scheduleRestore(block.getState(), seconds);
     }
     public void scheduleRestore(BlockState state) {
         scheduleRestore(state, BlockStateRestore.getBlockRestoreTime(state));
     }
     public void scheduleRestore(BlockState state, Cause cause) {
         scheduleRestore(state, BlockStateRestore.getBlockRestoreTime(state, cause));
     }
     public void scheduleRestore(BlockState state, int seconds) {
         BlockStateRestore restore = new BlockStateRestore(state, seconds);
         
         HashSet<BlockStateRestore> set = queue.get(state.getWorld());
         if(set == null) {
             set = new HashSet<BlockStateRestore>();
             queue.put(state.getWorld(), set);
         }
         
        //if(set.contains(restore)) set.remove(restore);
         set.add(restore);
     }
     
     
     private void flushQueue() {
         flushQueue(true);
     }
     private void flushQueue(boolean async) {
         if(worlds == null) {
             debug("No table names found in worlds section of configuration file");
             return;
         }
         
         for(Entry<World, HashSet<BlockStateRestore>> entry : queue.entrySet()) {
             
             final World world = entry.getKey();
             if(flushing.contains(world)) {
                 debug("Flush already in progress for world \"{0}\"...", world.getName());
                 continue;
             }
             
             final String blocksTable = getTableName(world, "blocks");
             final String jobsTable = getTableName(world, "jobs");
             HashSet<BlockStateRestore> set = entry.getValue();
             
             if(blocksTable == null || jobsTable == null) {
                 debug("Missing tables for world \"{0}\", clearing world's block queue", world.getName());
                 set.clear();
                 continue;
             }
             
             if(set.isEmpty()) {
                 debug("No blocks in queue for world \"{0}\", skipping", world.getName());
                 continue;
             }
             
             debug("Flushing block restore queue for world \"{0}\"", world.getName());
             flushing.add(world);
             
             
             final StringBuilder blocksSql = new StringBuilder("INSERT IGNORE INTO `").append(blocksTable).append("` (`x`, `y`, `z`, `type`, `data`) VALUES ");
             final ArrayList<Object> blocksParams = new ArrayList();
             
             final StringBuilder jobsSql = new StringBuilder("INSERT INTO `").append(jobsTable).append("` (`x`, `y`, `z`, `chunk_x`, `chunk_z`, `when`) VALUES ");
             final ArrayList<Object> jobsParams = new ArrayList();
 
             for(BlockStateRestore restore : set) {
                 int x = restore.state.getX();
                 int y = restore.state.getY();
                 int z = restore.state.getZ();
                 
                 blocksSql.append("(?, ?, ?, ?, ?), ");
                 blocksParams.add(x);
                 blocksParams.add(y);
                 blocksParams.add(z);
                 blocksParams.add(restore.state.getTypeId());
                 blocksParams.add(restore.state.getData().getData());
                 
                 jobsSql.append("(?, ?, ?, ?, ?, TIMESTAMPADD(SECOND, ?, NOW())), ");
                 jobsParams.add(x);
                 jobsParams.add(y);
                 jobsParams.add(z);
                 jobsParams.add(restore.state.getChunk().getX());
                 jobsParams.add(restore.state.getChunk().getZ());
                 jobsParams.add(restore.seconds);
             }
             set.clear();
             
             blocksSql.delete(blocksSql.length() - 2, blocksSql.length());
             jobsSql.replace(jobsSql.length() - 2, jobsSql.length(), " ON DUPLICATE KEY UPDATE `when`=VALUES(`when`), `job`=DEFAULT");
             
             
             new FlushQuery(world, blocksSql.toString(), async) {
                 @Override
                 public void onAffected(Integer affected) {
                     super.onAffected(affected);
                     new FlushQuery(world, jobsSql.toString(), async).affected(jobsParams.toArray());
                 }
             }.affected(blocksParams.toArray());
             
         }
     }
     
     
     private void restoreBlocks() {
         if(worlds == null) {
             debug("No table names found in worlds section of configuration file");
             return;
         }
         
         for(final World world : worlds.keySet()) {
             
             if(restoring.contains(world)) {
                 debug("Restore already in progress for world \"{0}\"...", world.getName());
                 continue;
             }
             
             final String blocksTable = getTableName(world, "blocks");
             final String jobsTable = getTableName(world, "jobs");
             
             if(blocksTable == null || jobsTable == null) {
                 debug("Missing tables for world \"{0}\", skipping restore", world.getName());
                 continue;
             }
             
             debug("Restoring blocks for world \"{0}\"", world.getName());
             restoring.add(world);
             
             
             final int job = (int) (System.currentTimeMillis() / 1000L);
             StringBuilder sql = new StringBuilder("UPDATE `").append(jobsTable).append("` SET `job`=? WHERE `when` <= NOW() ORDER BY `chunk_x`, `chunk_z`, `job`, `when` LIMIT ?");
             
             new RestoreQuery(world, sql.toString()) {
                 @Override
                 public void onAffected(Integer affected) {
                     if(affected > 0) {
                         debug("Updated {0} rows for \"{1}\" restore job #{2,number,#}", affected, world.getName(), job);
                     }
                     else {
                         debug("No rows updated for \"{0}\" restore job #{1,number,#}", world.getName(), job);
                         restoring.remove(world);
                         return;
                     }
                     
                     StringBuilder sql = new StringBuilder("SELECT `jobs`.`x`, `jobs`.`y`, `jobs`.`z`, `blocks`.`type`, `blocks`.`data` FROM `").append(jobsTable).append("` AS `jobs` ")
                                                   .append("JOIN `").append(blocksTable).append("` AS `blocks` ON `blocks`.`x` = `jobs`.`x` AND `blocks`.`y` = `jobs`.`y` AND `blocks`.`z` = `jobs`.`z` ")
                                                   .append("WHERE `jobs`.`job`=? ")
                                                   .append("ORDER BY `jobs`.`chunk_x`, `jobs`.`chunk_z`");
                     
                     new RestoreQuery(world, sql.toString()) {
                         @Override
                         public void onFetch(ArrayList<HashMap> rows) {
                             if(rows.size() > 0) {
                                 debug("Got {0} rows for \"{1}\" restore job #{2,number,#}", rows.size(), world.getName(), job);
                             }
                             else {
                                 debug("No rows found for \"{0}\" restore job #{1,number,#} ({1})", world.getName(), job);
                                 restoring.remove(world);
                                 return;
                             }
                             
                             
                             // check chunks for player entities
                             // TODO: skip jobs with unloaded chunks?
                             for(HashMap<String, Integer> row : rows) {
                                 int x = row.get("x").intValue() >> 4;
                                 int z = row.get("z").intValue() >> 4;
                                 
                                 if(!world.isChunkLoaded(x, z)) continue;
                                 
                                 for(Entity entity : world.getChunkAt(x, z).getEntities()) {
                                     if(entity instanceof Player) {
                                         debug("Player entity found in chunk [{0} {1}], skipping \"{2}\" restore job #{3,number,#}", x, z, world.getName(), job);
                                         restoring.remove(world);
                                         return;
                                     }
                                 }
                             }
                             
                             Stopwatch overall = new Stopwatch().start();
                             Stopwatch events = new Stopwatch();
                             
                             int successes = 0;
                             int skipped = 0;
                             int failures = 0;
                             HashSet<Chunk> chunks = new HashSet<Chunk>(Arrays.asList(world.getLoadedChunks()));
                             HashMap<Block, BlockState> blocks = new HashMap<Block, BlockState>();
                             
                             // get blocks
                             for(HashMap<String, Integer> row : rows) {
                                 int x = row.get("x").intValue();
                                 int y = row.get("y").intValue();
                                 int z = row.get("z").intValue();
                                 int type = row.get("type").intValue();
                                 byte data = row.get("data").byteValue();
                                 
                                 Block block = world.getBlockAt(x, y, z);
                                 if(type == block.getTypeId() && data == block.getData()) {
                                     // block is correct, no restore necessary
                                     skipped++;
                                     continue;
                                 }
                                 
                                 BlockState state = block.getState();
                                 state.setTypeId(type);
                                 state.getData().setData(data);
                                 
                                 blocks.put(block, state);
                             }
                             
                             // call restore event
                             if(callEvents) {
                                 RestoreBlocksEvent event = new RestoreBlocksEvent(blocks);
                                 events.start();
                                 getServer().getPluginManager().callEvent(event);
                                 events.stop();
 
                                 if(event.isCancelled()) {
                                     debug("RestoreBlocksEvent cancelled, skipping entire \"{0}\" restore job #{1,number,#}", world.getName(), job);
                                     restoring.remove(world);
                                     return;
                                 }
                             }
                             
                             // restore blocks
                             for(BlockState state : blocks.values()) {
                                 if(state.update(true)) {
                                     successes++;
                                 }
                                 else {
                                     debug("Failed to restore block at [{0} {1} {2}] to {3}", state.getX(), state.getY(), state.getZ(), state.getData());
                                     failures++;
                                 }
                             }
                             
                             
                             debug("\"{0}\" restore job #{1,number,#} results:", world.getName(), job);
                             if(skipped > 0) debug("{0}/{1} blocks didn''t need restoring", skipped, rows.size());
                             if(failures > 0) debug("{0}/{1} blocks FAILED to restore", failures, rows.size());
                             debug("{0}/{1} blocks successfully restored", successes, rows.size());
                             
                             // find difference of loaded chunks
                             List<Chunk> loaded = Arrays.asList(world.getLoadedChunks());
                             HashSet<Chunk> intersection = (HashSet<Chunk>) chunks.clone();
                             intersection.retainAll(loaded);
                             chunks.addAll(loaded);
                             chunks.removeAll(intersection);
                             debug("{0} new chunks loaded or unloaded during restore", chunks.size());
                             
                             debug("{0,number,#} ms spent restoring blocks TOTAL", overall.stop().elapsed());
                             debug("{0,number,#} ms spent in restore events", events.elapsed());
                             
                             
                             StringBuilder sql = new StringBuilder("DELETE FROM `").append(jobsTable).append("` WHERE `job`=?");
                             
                             new RestoreQuery(world, sql.toString()) {
                                 @Override
                                 public void onAffected(Integer affected) {
                                     debug("Deleted {0} rows from table {1} for block restore job #{2,number,#}", affected, jobsTable, job);
                                     restoring.remove(world);
                                 }
                             }.affected(job);
                         }
 
                     }.fetch(job);
                 }
                 
             }.affected(job, restoreJobSize);
             
         }
             
             
     }
     
     
     
     class Query extends com.minecarts.dbquery.Query {
         public Query(String sql, boolean async) {
             this(sql);
             this.async = async;
         }
         public Query(String sql) {
             super(MiracleGrow.this, MiracleGrow.this.provider, sql);
         }
         
         @Override
         public void onComplete(FinalQuery query) {
             if(query.elapsed() < 500) {
                 debug("Query took {0,number,#} ms", query.elapsed());
             }
             else {
                 log("Slow query took {0,number,#} ms", query.elapsed());
             }
         }
     }
     
     
     class ProcessingQuery extends Query {
         public final ArrayList processing;
         public final World world;
         
         private int tries = 0;
         
         public ProcessingQuery(ArrayList processing, World world, String sql, boolean async) {
             this(processing, world, sql);
             this.async = async;
         }
         public ProcessingQuery(ArrayList processing, World world, String sql) {
             super(sql);
             this.processing = processing;
             this.world = world;
         }
         
         @Override
         public void onException(Exception x, FinalQuery query) {
             try {
                 throw x;
             }
             catch(java.sql.SQLException e) {
                 if(++tries < 5) {
                     log("SQLException on Query, retrying...");
                     e.printStackTrace();
                     query.run();
                 }
                 else {
                     log("FAILED! SQLException on Query: {0}", query);
                     e.printStackTrace();
                     processing.remove(world);
                 }
             }
             catch(com.minecarts.dbquery.NoConnectionException e) {
                 if(++tries < 5) {
                     log("NoConnectionException on Query, retrying...");
                     e.printStackTrace();
                     query.run();
                 }
                 else {
                     log("FAILED! NoConnectionException on Query: {0}", query);
                     e.printStackTrace();
                     processing.remove(world);
                 }
             }
             catch(Exception e) {
                 log("FAILED! Exception on Query: {0}", query);
                 e.printStackTrace();
                 processing.remove(world);
             }
         }
 
     }
     
     
     class FlushQuery extends ProcessingQuery {
         public FlushQuery(World world, String sql, boolean async) {
             super(flushing, world, sql, async);
         }
         
         @Override
         public void onAffected(Integer affected) {
             debug("{0} rows affected", affected);
             flushing.remove(world);
         }
     }
     
     class RestoreQuery extends ProcessingQuery {
         public RestoreQuery(World world, String sql) {
             super(restoring, world, sql);
         }
     }
     
     
 }
