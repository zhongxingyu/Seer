 package com.minecarts.miraclegrow;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.text.MessageFormat;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 
 import com.minecarts.dbquery.DBQuery;
 
 import com.minecarts.miraclegrow.listener.*;
 import org.bukkit.event.Listener;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.Location;
 
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Event.Priority;
 import static org.bukkit.event.Event.Type.*;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 
 public class MiracleGrow extends org.bukkit.plugin.java.JavaPlugin {
     private static final Logger logger = Logger.getLogger("com.minecarts.miraclegrow"); 
     
     private PluginDescriptionFile pdf;
     
     protected DBQuery dbq;
     protected HashSet<BlockStateRestore> queue = new HashSet<BlockStateRestore>();
     
     public void onEnable() {
         pdf = getDescription();
        dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
         
         PluginManager pluginManager = getServer().getPluginManager();
         HashMap<Listener, Type[]> listeners = new HashMap<Listener, Type[]>() {{
             put(new BlockListener(MiracleGrow.this), new Type[]{ BLOCK_PLACE, BLOCK_BREAK });
             put(new EntityListener(MiracleGrow.this), new Type[]{ ENTITY_EXPLODE });
             put(new ServerListener(MiracleGrow.this), new Type[]{ PLUGIN_DISABLE });
         }};
         
         for(java.util.Map.Entry<Listener, Type[]> entry : listeners.entrySet()) {
             for(Type type : entry.getValue()) {
                 pluginManager.registerEvent(type, entry.getKey(), Priority.Monitor, this);
             }
         }
         
 
         logf("Enabled {0}", pdf.getVersion());
     }
     
     public void onDisable() {
         processQueue(false);
     }
     
     
     public void log(String message) {
         log(Level.INFO, message);
     }
     public void log(Level level, String message) {
         logger.log(level, MessageFormat.format("{0}> {1}", pdf.getName(), message));
     }
     
     public void logf(String message, Object... args) {
         log(MessageFormat.format(message, args));
     }
     public void logf(Level level, String message, Object... args) {
         log(level, MessageFormat.format(message, args));
     }
     
     
     
     class BlockStateRestore {
         public final BlockState state;
         public final int seconds;
         
         public BlockStateRestore(Block block, int seconds) {
             this(block.getState(), seconds);
         }
         public BlockStateRestore(BlockState state, int seconds) {
             this.state = state;
             this.seconds = seconds;
         }
         
         @Override
         public boolean equals(Object o) {
             if(o == this) return true;
             if(!(o instanceof BlockStateRestore)) return false;
             
             return state.getBlock().equals(((BlockStateRestore) o).state.getBlock());
         }
         
         @Override
         public int hashCode() {
             return state.getBlock().hashCode();
         }
     }
     
     public void scheduleRestore(Block block) {
         scheduleRestore(block.getState());
     }
     public void scheduleRestore(Block block, int seconds) {
         scheduleRestore(block.getState(), seconds);
     }
     public void scheduleRestore(BlockState state) {
         scheduleRestore(state, getBlockRestoreTime(state));
     }
     public void scheduleRestore(BlockState state, int seconds) {
         BlockStateRestore restore = new BlockStateRestore(state, seconds);
         
         if(queue.contains(restore)) queue.remove(restore);
         queue.add(restore);
     }
     
     
     public void processQueue() {
         processQueue(true);
     }
     public void processQueue(final boolean asyncQuery) {
         if(queue.isEmpty()) return;
         
         StringBuilder query = new StringBuilder("INSERT INTO `block_restore_queue` (`world`, `x`, `y`, `z`, `type`, `data`, `when`) VALUES ");
         ArrayList<Object> params = new ArrayList();
         
         for(BlockStateRestore restore : queue) {
             query.append("(?, ?, ?, ?, ?, ?, TIMESTAMPADD(SECOND, ?, NOW())), ");
             params.add(restore.state.getBlock().getWorld().getName());
             params.add(restore.state.getBlock().getX());
             params.add(restore.state.getBlock().getY());
             params.add(restore.state.getBlock().getZ());
             params.add(restore.state.getTypeId());
             params.add(restore.state.getData().getData());
             params.add(restore.seconds);
         }
         // TODO: requeue in case of query failure
         queue.clear();
         
         query.replace(query.length() - 2, query.length(), " ON DUPLICATE KEY UPDATE `when`=VALUES(`when`)");
         
         new Query(query.toString()) {
             protected boolean async = asyncQuery;
             
             @Override
             public void onAffected(Integer affected) {
                 logf("{0} rows affected", affected);
             }
         }.affected(params.toArray());
     }
     
     
     
     class Query extends com.minecarts.dbquery.Query {
         public Query(String sql) {
             // TODO: configurable provider name
             super(MiracleGrow.this, ((DBQuery) getServer().getPluginManager().getPlugin("DBQuery")).getProvider("minecarts"), sql);
         }
         
         @Override
         public void onException(Exception x, FinalQuery query) {
             try {
                 throw x;
             }
             catch(java.sql.SQLException e) {
                 log("Query SQLException:");
                 e.printStackTrace();
             }
             catch(com.minecarts.dbquery.NoConnectionException e) {
                 log("Query NoConnectionException:");
                 e.printStackTrace();
             }
             catch(Exception e) {
                 log("Query generic Exception:");
                 e.printStackTrace();
             }
         }
     }
     
     
     public int getBlockRestoreTime(Block block) {
         return getBlockRestoreTime(block.getState());
     }
     public int getBlockRestoreTime(BlockState state) {
         logf("Getting block restore time for {1} at {0}", state.getBlock().getLocation(), state.getType());
         return 60*60*24;
     }
     
 }
