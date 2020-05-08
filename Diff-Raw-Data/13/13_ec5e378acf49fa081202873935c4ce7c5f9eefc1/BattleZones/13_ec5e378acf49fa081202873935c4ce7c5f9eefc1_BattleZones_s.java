 /*
  * Origonally written by Jacob Tyo for the BattleZones project. You may modify
  * and use the following code however you wish, as long as you give credit to its
  * original author.
  */
 package js2.battlezones;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @TODO Refactor code
  * @TODO Enable option to use PVPToggle instead of built-in PvP system.
  * 
  * BattleZones is a CraftBukkit plugin.
  * 
  * BattleZones allows for the creation of simple - open PvP arenas where players
  * may enter and leave as they wish, without having to set up lobbies and matches.
  * 
  * @author Jacob Tyo
  * @version 12/11/2011
  */
 public class BattleZones extends JavaPlugin {
     public static final Logger LOG = Logger.getLogger("Minecraft");
     
     public ArrayList<String> nestedZones;
     public BattleZonesCommandExecutor commandExecutor;
     public BattleZonesMovementListener movementListener;
     public BattleZonesEntityListener entityListener;
     public boolean isZonesSet;
     public boolean enabled;
     public PluginManager manager;
     public PrefConfig prefConfig;
     public PVPHandler pvpHandler;
     public ZoneConfig zoneConfig;
     
     @Override
     public void onDisable() {
        LOG.info((Message.getPrefix() + "Plugin Disabled!"));
     }
 
     @Override
     public void onEnable() {
        preprocess();
         init();
         run();
     }
 
     /**
      * Initialize all variables.
      */
     private void init() {
        if (!enabled) return;
         LOG.log(Level.INFO, (Message.getPrefix() + "Initializing..."));
         enabled                             = true;
         nestedZones                         = new ArrayList<String>();
         prefConfig                          = new PrefConfig(this);
         zoneConfig                          = new ZoneConfig(this);
         commandExecutor                     = new BattleZonesCommandExecutor(this);
         movementListener                    = new BattleZonesMovementListener(this);
         entityListener                      = new BattleZonesEntityListener(this);
         manager                             = this.getServer().getPluginManager();
         pvpHandler                          = new PVPHandler(this);
     }
 
     /**
      * Provide initial setup for BattleZones. Register events, register commands
      * and apply global plugin settings.
      */
     private void run() {
         getCommand("bz").setExecutor(commandExecutor);
         indexZones(true);
         zoneConfig.reloadZoneSet();
         manager.registerEvent(Event.Type.PLAYER_JOIN, pvpHandler, Event.Priority.Normal, this);
         manager.registerEvent(Event.Type.PLAYER_QUIT, pvpHandler, Event.Priority.Lowest, this);
         manager.registerEvent(Event.Type.PLAYER_MOVE, movementListener, Event.Priority.Normal, this);
         manager.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
         LOG.log(Level.INFO, (Message.getPrefix() + getDescription().getName() + " Enabled! Version: " + getDescription().getVersion()));
     }
 
     /**
      * This method iterates over all registered zones and indexes them into the 
      * global variable {@code nestedZones} according to world location and zone 
      * name. The parameter {@code isStartup} defines whether it is indexing for
      * the first time or not.
      * @param isStartup Is {@code indexZones} being called from startup?
      */
     public void indexZones(boolean isStartup) {
         Set<String> rootSet = zoneConfig.getConfig().getConfigurationSection("zones").getKeys(false);
         if (rootSet != null) {
             LOG.log(Level.INFO, (Message.getPrefix() + (isStartup ? "Indexing" : "Reindexing") + " zones..."));
             nestedZones.clear();
             for (Iterator<String> it = rootSet.iterator(); it.hasNext();) {
                 String worldName = it.next();
                 Set<String> zoneSet = zoneConfig.getConfig().getConfigurationSection("zones." + worldName).getKeys(false);
                 for (Iterator<String> zones = zoneSet.iterator(); zones.hasNext();) {
                     String zoneName = zones.next();
                     nestedZones.add(worldName + "." + zoneName);
                     if (!pvpHandler.isGlobalPVPEnabled(getServer().getWorld(worldName))) pvpHandler.setGlobalPVP(getServer().getWorld(worldName), true);
                 }
             }
         }
     }
 
     /**
      * This method runs a series of tests to determine the best settings to implement.
      */
     private void preprocess() {
         if (!prefConfig.getConfig().getBoolean(PrefConfig.PREF_ENABLED))
         {
            LOG.info((Message.getPrefix() + "Plugin not enabled. See config.yml..."));
             enabled = false;
             return;
         }
     }
     
 }
