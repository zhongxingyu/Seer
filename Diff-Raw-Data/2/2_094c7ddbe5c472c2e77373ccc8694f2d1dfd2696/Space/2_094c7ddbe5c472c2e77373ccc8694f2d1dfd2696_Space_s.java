 // Package Declaration
 package me.iffa.bspace;
 
 // Java Imports
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 // Pail Imports
 import me.escapeNT.pail.Pail;
 
 // PluginStats Imports
 import com.randomappdev.pluginstats.Ping;
 
 // bSpace Imports
 import me.iffa.bspace.api.SpaceConfigHandler;
 import me.iffa.bspace.api.SpaceLangHandler;
 import me.iffa.bspace.api.SpaceMessageHandler;
 import me.iffa.bspace.api.schematic.SpaceSchematicHandler;
 import me.iffa.bspace.api.SpaceWorldHandler;
 import me.iffa.bspace.commands.SpaceCommandHandler;
 import me.iffa.bspace.config.SpaceConfig;
 import me.iffa.bspace.config.SpaceConfigUpdater;
 import me.iffa.bspace.economy.Economy;
 import me.iffa.bspace.gui.PailInterface;
 import me.iffa.bspace.listeners.SpaceEntityListener;
 import me.iffa.bspace.listeners.SpacePlayerListener;
 import me.iffa.bspace.listeners.SpaceSuffocationListener;
 import me.iffa.bspace.listeners.misc.BlackHoleScannerListener;
 import me.iffa.bspace.listeners.misc.SpaceWeatherListener;
 import me.iffa.bspace.listeners.misc.SpaceWorldListener;
 import me.iffa.bspace.listeners.spout.SpaceSpoutAreaListener;
 import me.iffa.bspace.listeners.spout.SpaceSpoutCraftListener;
 import me.iffa.bspace.listeners.spout.SpaceSpoutEntityListener;
 import me.iffa.bspace.listeners.spout.SpaceSpoutKeyListener;
 import me.iffa.bspace.listeners.spout.SpaceSpoutPlayerListener;
 import me.iffa.bspace.runnables.SpoutBlackHoleAreaRunnable;
 import me.iffa.bspace.wgen.planets.PlanetsChunkGenerator;
 
 // Bukkit Imports
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Main class of bSpace.
  * 
  * @author iffa
  * @author kitskub
  * @author HACKhalo2
  */
 public class Space extends JavaPlugin {
     // Variables
     // And here goes all public statics!
     private static String prefix;
     private static String version;
     private static SpaceWorldHandler worldHandler;
     private static PailInterface pailInterface;
     private static Map<Player, Location> locCache = null;
     private static boolean jumpPressed = false;
     private PluginManager pm;
     private SpaceCommandHandler sce = null;
     private Economy economy;
     private final SpaceWeatherListener weatherListener = new SpaceWeatherListener();
     private final SpaceEntityListener entityListener = new SpaceEntityListener();
     private final SpaceWorldListener worldListener = new SpaceWorldListener();
     private final SpacePlayerListener playerListener = new SpacePlayerListener();
 
     /**
      * Called when the plugin is disabled.
      */
     @Override
     public void onDisable() {
         Bukkit.getScheduler().cancelTasks(this);
         // Finishing up disablation.
         SpaceMessageHandler.print(Level.INFO, SpaceLangHandler.getDisabledMessage());
     }
 
     /**
      * Called when the plugin is enabled.
      */
     @Override
     public void onEnable() {
         // Initializing variables.
         initVariables();
         SpaceMessageHandler.debugPrint(Level.INFO, "Initialized startup variables.");
 
         // Loading configuration files.
         SpaceConfig.loadConfigs();
         SpaceMessageHandler.debugPrint(Level.INFO, "Loaded configuration files, now checking if they need to be updated...");
         // Updating configuration files (if needed).
         SpaceConfigUpdater.updateConfigs();
 
         // Registering events.
         registerEvents();
 
         // Loading schematic files.
         SpaceSchematicHandler.loadSchematics();
 
         // Loading space worlds (startup).
         worldHandler.loadSpaceWorlds();
 
         // Initializing the CommandExecutor for /space.
         sce = new SpaceCommandHandler(this);
         getCommand("space").setExecutor(sce);
         SpaceMessageHandler.debugPrint(Level.INFO, "Initialized CommandExecutors.");
 
         // Checking if it should always be night in space worlds.
         for (World world : worldHandler.getSpaceWorlds()) {
             if (SpaceConfigHandler.forceNight(world)) {
                 worldHandler.startForceNightTask(world);
                 SpaceMessageHandler.debugPrint(Level.INFO, "Started night forcing task for world '" + world.getName() + "'.");
             }
         }
 
         // Checking if Economy is ok.
         if (economy == null && getServer().getPluginManager().getPlugin("Register") != null) {
             if (Economy.checkEconomy()) {
                 economy = new Economy();
             }
         }
 
         // Initializing the Pail tab.
         if (pm.getPlugin("Pail") != null) {
             SpaceMessageHandler.debugPrint(Level.INFO, "Starting up the Pail tab.");
             pailInterface = new PailInterface(this);
             ((Pail) pm.getPlugin("Pail")).loadInterfaceComponent("bSpace", pailInterface);
         }
         
         // Initializing black hole stuff.
         if (pm.getPlugin("Spout") != null) {
             Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SpoutBlackHoleAreaRunnable(), 1, 5);
         }
 
         // Finishing up enablation.
         SpaceMessageHandler.print(Level.INFO, SpaceLangHandler.getUsageStatsMessage());
         Ping.init(this);
         SpaceMessageHandler.print(Level.INFO, SpaceLangHandler.getEnabledMessage());
     }
 
     /**
      * Initializes variables (used on startup).
      */
     private void initVariables() {
         pm = getServer().getPluginManager();
         version = getDescription().getVersion();
         prefix = "[" + getDescription().getName() + "]";
         worldHandler = new SpaceWorldHandler(this);
         if (pm.getPlugin("Spout") != null && SpaceConfigHandler.isUsingSpout()) {
             locCache = new HashMap<Player, Location>();
         }
     }
 
     /**
      * Registers events for bSpace.
      */
     private void registerEvents() {
         // Registering other events.
         pm.registerEvent(Event.Type.WEATHER_CHANGE, weatherListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Event.Priority.Normal, this);
         SpaceMessageHandler.debugPrint(Level.INFO, "Registered events (other).");
 
         // Registering entity & player events.
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.High, this);
         pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.CUSTOM_EVENT, new SpaceSuffocationListener(), Event.Priority.Monitor, this); //Suffocation Listener
         SpaceMessageHandler.debugPrint(Level.INFO, "Registered events (entity & player).");
 
         // Registering events for Spout.
         if (pm.getPlugin("Spout") != null && SpaceConfigHandler.isUsingSpout()) {
             pm.registerEvent(Event.Type.PLAYER_TELEPORT, new SpaceSpoutPlayerListener(this), Event.Priority.Normal, this); //Player listener
             pm.registerEvent(Event.Type.PLAYER_RESPAWN, new SpaceSpoutPlayerListener(this), Event.Priority.Normal, this); // Player listener
             //pm.registerEvent(Event.Type.PLAYER_JOIN, spListener, Event.Priority.Normal, this); //moved this into a Custom Listener
             pm.registerEvent(Event.Type.ENTITY_DAMAGE, new SpaceSpoutEntityListener(), Event.Priority.Normal, this); //Entity Listener
             //pm.registerEvent(Event.Type.CREATURE_SPAWN, speListener, Event.Priority.Normal, this); //Disabled until Limitations in Spout is fixed
             pm.registerEvent(Event.Type.CUSTOM_EVENT, new SpaceSpoutCraftListener(), Event.Priority.Normal, this); //SpoutCraft Listener
             pm.registerEvent(Event.Type.CUSTOM_EVENT, new SpaceSpoutAreaListener(), Event.Priority.Normal, this); //Area Listener
             pm.registerEvent(Event.Type.CUSTOM_EVENT, new SpaceSpoutKeyListener(), Event.Priority.Normal, this); //Key Listener
             pm.registerEvent(Event.Type.CHUNK_LOAD, new BlackHoleScannerListener(), Event.Priority.Normal, this); // Black hole scanner
             SpaceMessageHandler.debugPrint(Level.INFO, "Registered events (Spout).");
         }
     }
 
     /**
      * Gets the default world generator of the plugin.
      * 
      * @param worldName World name
      * @param id ID (cow, fish etc)
      * 
      * @return ChunkGenerator to use
      */
     @Override
     public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
         boolean realID=true;
        if(id.isEmpty() || id.length() == 0 || id == null){
             realID=false;
         }
         if(realID){
             SpaceMessageHandler.debugPrint(Level.INFO, "Getting generator for '" + worldName + "' using id: '" + id + "'");
         }else{
             SpaceMessageHandler.debugPrint(Level.INFO, "Getting generator for '" + worldName + "' using default id,planets.");
         }
         SpaceWorldHandler.checkWorld(worldName);
         if (!realID) {
             return new PlanetsChunkGenerator("planets", false);
         }
         return new PlanetsChunkGenerator(id);
     }
 
     /* Some API methods */
     
     /**
      * Gets the SpaceWorldHandler.
      * 
      * @return SpaceWorldHandler
      */
     public static SpaceWorldHandler getWorldHandler() {
         return worldHandler;
     }
     
     /**
      * Gets the jump pressed value. (ie = wtf is this)
      * 
      * @return Jump pressed
      */
     public static boolean getJumpPressed() {
         return jumpPressed;
     }
     
     public static void setJumpPressed(boolean newJumpPressed) {
         jumpPressed = newJumpPressed;
     }
     
     /**
      * Gets the location cache.
      * 
      * @return Location cach
      */
     public static Map<Player, Location> getLocCache() {
         return locCache;
     }
     
     /**
      * Gets the plugin's prefix.
      * 
      * @return Prefix
      */
     public static String getPrefix() {
         return prefix;
     }
     
     /**
      * Gets the plugin's version.
      * 
      * @return Version
      */
     public static String getVersion() {
         return version;
     }
 
     /**
      * Gets the Economy-class.
      * 
      * @return Economy
      */
     public Economy getEconomy() {
         return economy;
     }
 }
