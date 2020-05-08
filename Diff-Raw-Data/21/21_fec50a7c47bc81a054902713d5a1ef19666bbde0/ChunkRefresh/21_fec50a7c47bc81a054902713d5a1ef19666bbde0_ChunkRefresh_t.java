 package com.nationsmc.chunkrefresh;
 
 import com.nationsmc.chunkrefresh.commands.Commands;
 import com.nationsmc.chunkrefresh.listeners.onDeathListener;
 import com.nationsmc.chunkrefresh.scheduler.SchedulerPool;
import com.nationsmc.chunkrefresh.tasks.replace;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChunkRefresh extends JavaPlugin {
     protected static Logger logger = null;
     public static boolean debugMode = false;
    
 
     protected static SchedulerPool poolInstance;
     protected Server server;
     protected PluginManager pm;
     protected CRConfiguration config;
     protected File dataFolder;
     protected File savedChunks;
     protected File savedTempChunks;
    public onDeathListener listener;
     
     public replace r;
     //XXX:Temporary copypasta, move if needed
     public static SchedulerPool getPool(){
 		if(poolInstance == null){
 			//Assume Default Dir if pool is not already initialized. 
 			poolInstance = new SchedulerPool(SchedulerPool.defaultFlatFileDir);
 		} 
 		return poolInstance;		   
     }		
     public ChunkRefresh()
     {
     }
 
     public List<Integer> pool;
     public List<String> chunkPool;
 
     public List<Chunk> chunkList;
     
     @Override
     public void onEnable(){
         logger = this.getLogger();
         
         this.server = this.getServer();
         this.pm = this.server.getPluginManager();
         this.dataFolder = this.getDataFolder();
 
         this.dataFolder.mkdirs();
         
         this.makeDir();
         
         pool = new ArrayList();
         chunkPool = new ArrayList();
         chunkList = new ArrayList();
         
        this.listener = new onDeathListener(this);
        
         Configuration configuration = this.getConfig();
         configuration.options().copyDefaults(true);
         debugMode = configuration.getBoolean("debug");
 	//XXX: Temporary copypasta, move if needed.
 	String poolPath = configuration.getString("scheduleData");
 	if(poolPath != ""){
 	ChunkRefresh.poolInstance = new SchedulerPool(poolPath);
 	} else {
 		ChunkRefresh.getPool();
 	}
         this.config = new CRConfiguration(configuration);
         
         this.saveConfig();
         
         this.fillChunkPool();
         
         getCommand("cr").setExecutor(new Commands(this));
         
         log("Version " + this.getDescription().getVersion() + " enabled");
     }
 
     @Override
     public void onDisable(){
         log("Version " + this.getDescription().getVersion() + " disabled");
     }
 
     public static void log(String msg){
         logger.log(Level.INFO, msg);
     }
 
     public static void error(String msg){
         logger.log(Level.SEVERE, msg);
     }
 
     public static void error(String msg, Throwable t){
         logger.log(Level.SEVERE, msg, t);
     }
 
     public static void debug(String msg){
         if (debugMode)
         {
             log("[debug] " + msg);
         }
     }
     
     public void makeDir(){
         savedChunks = new File("plugins/ChunkRefresh/savedChunks/temp");
         if(!savedChunks.exists()){
             savedChunks.mkdirs();
         }
     }
     
     public void fillChunkPool(){
         File[] files = savedChunks.listFiles();
         for(int x = 0; x < files.length; x++){
             chunkPool.add(files[x].getName());
         }
     }
     
     public void createNewChunkFile(Chunk chunk){
         File file = new File("plugins/ChunkRefresh/savedChunks/" + chunk.getX() + "," + chunk.getZ());
         
         if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ChunkRefresh.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
          chunkPool.add("plugins/ChunkRefresh/savedChunks/" + chunk.getX() + "," + chunk.getZ());
    }
     
     public void createNewTempChunkFile(Chunk chunk){
         File file = new File("plugins/ChunkRefresh/savedChunks/temp/" + chunk.getX() + "," + chunk.getZ());
         
         if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ChunkRefresh.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
     }
         
     public void addChunkData(Chunk chunk, String path){
         StringBuilder chunkBlockList = new StringBuilder();
         
         for(int x = 0; x < 16; x++){
             for(int z = 0; z < 16; z++){
                 for(int y = 0; y < Bukkit.getWorlds().get(0).getMaxHeight(); y++){
                     chunkBlockList.append(chunk.getBlock(x,y,z).getTypeId());
                     if(x<=15){
                         chunkBlockList.append(",");
                     }
                 }
             }
         }
         this.createNewChunkFile(chunk);
         
         try {
             FileWriter fs = new FileWriter(path + chunk.getX() + "," + chunk.getZ());
             BufferedWriter out = new BufferedWriter(fs);
             
             out.write(chunkBlockList.toString());
             
             out.close();
         } catch (IOException ex) {
             Logger.getLogger(ChunkRefresh.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void save(Chunk chunk){
         String path = "plugins/ChunkRefresh/savedChunks/";
         this.regen(chunk);
         addChunkData(chunk, path);
 
     }
 
     /**
      * Checks to see if the given chunk is saved.
      * 
      * @param   chunk to check for players
      * @return  boolean if player present
      */
     public boolean checkSave(Chunk chunk){
         File f = new File("plugins/ChunkRefresh/savedChunks/" + chunk.getX() + "," + chunk.getZ());
         
         return f.exists();
     }
     
     public void run(Chunk chunk){
         String tempPath = "plugins/ChunkRefresh/savedChunks/temp/";
         addChunkData(chunk, tempPath);
         this.chunkList.add(chunk);
         refreshChunk(chunk);
     }
     
     public void run(int chunkPoolNumber){
         String[] split = chunkPool.get(chunkPoolNumber).split(",");
         int x = Integer.parseInt(split[0]);
         int y = Integer.parseInt(split[1]);
         Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(x,y);
         refreshChunk(chunk);
     }
     
     public void removeTemp(String fileName){
         File f = new File("/plugins/ChunkRefresh/savedChunks/temp/" + fileName);
         f.delete();
     }
     
     public void saveMore(Chunk chunk, int amount, String direction){
         String path = "plugins/ChunkRefresh/savedChunks/";
         Chunk newChunk;
         World world = chunk.getWorld();
         if(this.debugMode)
             Bukkit.getLogger().info("[ChunkRefresh] Direction is: " + direction);
         if(direction.toLowerCase().contains("n")){
             if(this.debugMode)
                 Bukkit.getLogger().info("[ChunkRefresh] Saving North");
             if(direction.toLowerCase().contains("w")){
                 if(this.debugMode)
                     Bukkit.getLogger().info("[ChunkRefresh] Saving North West");
                 for(int x = 0; x < amount; x++){
                    for(int y = 0; y < amount; y++){
                         if(this.debugMode)
                             Bukkit.getLogger().info("[ChunkRefresh] Saving NW: " + chunk.getX() + "," + chunk.getZ());
                        newChunk = Bukkit.getWorlds().get(0).getChunkAt(chunk.getX() + y, chunk.getZ() - x);
                        this.regen(newChunk);
                        addChunkData(newChunk,path);
                    }
                 }
             } else if(direction.toLowerCase().contains("e")){
                 if(this.debugMode)
                     Bukkit.getLogger().info("[ChunkRefresh] Saving North East");
                 for(int x = 0; x < amount; x++){
                     for(int y = 0; y < amount; y++){
                         if(this.debugMode)
                             Bukkit.getLogger().info("[ChunkRefresh] Saving NE: " + chunk.getX() + "," + chunk.getZ());
                         newChunk = Bukkit.getWorlds().get(0).getChunkAt(chunk.getX() + y, chunk.getZ() + x);
                         this.regen(newChunk);
                         addChunkData(newChunk,path);
                     }
                 }
             }
         } else if(direction.toLowerCase().contains("s")){
             if(this.debugMode)
                 Bukkit.getLogger().info("[ChunkRefresh] Saving South");
             if(direction.toLowerCase().contains("w")){
                 if(this.debugMode)
                     Bukkit.getLogger().info("[ChunkRefresh] Saving South West");
                 for(int x = 0; x < amount; x++){
                    for(int y = 0; y < amount; y++){
                         if(this.debugMode)
                             Bukkit.getLogger().info("[ChunkRefresh] Saving SE: " + chunk.getX() + "," + chunk.getZ());
                        newChunk = Bukkit.getWorlds().get(0).getChunkAt(chunk.getX() - y, chunk.getZ() + x);
                        this.regen(newChunk);
                        addChunkData(newChunk,path);
                    }
                 }
             } else if(direction.toLowerCase().contains("e")){
                 if(this.debugMode)
                     Bukkit.getLogger().info("[ChunkRefresh] Saving South East");
                 for(int x = 0; x < amount; x++){
                    for(int y = 0; y < amount; y++){
                         if(this.debugMode)
                             Bukkit.getLogger().info("[ChunkRefresh] Saving SW: " + chunk.getX() + "," + chunk.getZ());
                        newChunk = Bukkit.getWorlds().get(0).getChunkAt(chunk.getX() - y, chunk.getZ() - x);
                        this.regen(newChunk);
                        addChunkData(newChunk,path);
                    }
                 }                
             }
         }
     }
     
     public void refreshChunk(Chunk chunk){
         r = new replace(chunk, this);
         pool.add(this.getServer().getScheduler().scheduleAsyncDelayedTask(this, r));
         
 //        int taskID = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, r, 10L, 200L);
 //        this.getServer().getScheduler().scheduleAsyncDelayedTask(this, r);
         
         
     }
     
     /**
      * Returns true of current chunk has a player present.
      * 
      * @param chunk
      * @return 
      */
     public boolean playersPresent(Chunk chunk, Player player){
         Boolean answer = false;
         Entity[] entities = chunk.getEntities();
         List<Player> players = chunk.getWorld().getPlayers();
         for(int x = 0;x < players.size(); x++){
             for(int y = 0;y < entities.length; y++){
                 Player player2 = players.get(x).getPlayer();
                 if(entities[y].getEntityId() == players.get(x).getEntityId()){
                     if(entities[y].getEntityId() == player.getEntityId()){
                         continue;
                     }
                     answer = true;
                     if(this.debugMode){
                         Bukkit.getLogger().info("[ChunkRefresh] There is a player present in chunk!");
                         this.log(player2.getName());
                     }
                     player2.sendMessage("[ChunkRefresh] Chunk is trying to refresh! Please leave the chunk as soon as possible.");
 //                    break;
                 }
             }
         }
         return answer;
     }
     
     /**
      * Returns true of current chunk has a player present.
      * 
      * @param chunk
      * @return 
      */
     public boolean playersPresent(Chunk chunk){
         Boolean answer = false;
         Entity[] entities = chunk.getEntities();
         List<Player> players = chunk.getWorld().getPlayers();
         for(int x = 0;x < players.size(); x++){
             for(int y = 0;y < entities.length; y++){
                 if(entities[y].getEntityId() == players.get(x).getEntityId()){
                     answer = true;
                     if(this.debugMode){
                         Bukkit.getLogger().info("[ChunkRefresh] There is a player!");
                     }
                     break;
                 }
             }
         }
         return answer;
     }
     
     public boolean chunkRefreshing(Chunk chunk){
         if(this.chunkList.contains(chunk)){
             return true;
         } else {
             return false;
         }
     }
     
     public void sendAway(Player player, Chunk chunk){
         int x = 1;
         int z = 1;
         Chunk newChunk = chunk.getWorld().getChunkAt(chunk.getX()+x,chunk.getZ()+z);
         while(this.chunkRefreshing(newChunk)){
             newChunk = chunk.getWorld().getChunkAt(chunk.getX()+x,chunk.getZ()+z);
             x++;
             z++;
         }
         Block block = newChunk.getBlock(0, 0, 0);
         player.teleport(newChunk.getBlock(0, chunk.getWorld().getHighestBlockYAt(block.getX(), block.getZ()), 0).getLocation());
     }
     
     public boolean isNumeric(String string){
         boolean answer = false;
         try{
             Integer.parseInt(string);
             answer = true;
         } catch(NumberFormatException nfe){
             answer = false;
         }
         return answer;
     }
     
     public void runAll(){
         String path = "plugins/ChunkRefresh/savedChunks/";
         String tempPath = "plugins/ChunkRefresh/savedChunks/temp/";
         File file = new File(path);
         File[] files = file.listFiles();
         
         for(File f : files){
             String[] split = f.getName().split(",");
             String x = split[0];
             String z = split[1];
             Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(Integer.parseInt(x), Integer.parseInt(z));
             addChunkData(chunk, tempPath);
             this.refreshChunk(chunk);
         }
     }
     
     public void regen(Chunk chunk) {
         World world = chunk.getWorld();
         int x = chunk.getX();
         int z = chunk.getZ();
 
         if (world.unloadChunk(x,z,false)) {
             if (world.regenerateChunk(x, z)) {
                 if(world.refreshChunk(x, z)){
                     world.loadChunk(x,z);
                 }
             }
         }
     }
 }
