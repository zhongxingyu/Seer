 package me.DDoS.Quarantine;
 
 import com.bekvon.bukkit.residence.Residence;
 import me.DDoS.Quarantine.command.AdminCommandExecutor;
 import me.DDoS.Quarantine.leaderboard.Leaderboard;
 import me.DDoS.Quarantine.zone.ZoneLoader;
 import me.DDoS.Quarantine.listener.*;
 import me.DDoS.Quarantine.zone.Zone;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import couk.Adamki11s.Regios.Main.Regios;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 import me.DDoS.Quarantine.command.PlayerCommandExecutor;
 import me.DDoS.Quarantine.command.SetupCommandExecutor;
 import me.DDoS.Quarantine.gui.*;
 import me.DDoS.Quarantine.permission.Permissions;
 import me.DDoS.Quarantine.permission.PermissionsHandler;
 import me.DDoS.Quarantine.util.Metrics;
 import me.DDoS.Quarantine.util.Metrics.Graph;
 import me.DDoS.Quarantine.zone.region.provider.RegionProvider;
 import me.DDoS.Quarantine.zone.region.provider.RegiosRegionProvider;
 import me.DDoS.Quarantine.zone.region.provider.ResidenceRegionProvider;
 import me.DDoS.Quarantine.zone.region.provider.WorldGuardRegionProvider;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.plugin.SpoutPlugin;
 
 /**
  *
  * @author DDoS
  */
 public class Quarantine extends JavaPlugin {
 
     public static final Logger log = Logger.getLogger("Minecraft");
     //
     private final Map<String, Zone> zones = new HashMap<String, Zone>();
     //
     private FileConfiguration config;
     //
     private Permissions permissions;
     //
     private GUIHandler guiHandler;
     //
     private RegionProvider regionProvider;
 
     public Quarantine() {
 
         checkLibs();
 
     }
 
     @Override
     public void onEnable() {
 
         CommandExecutor ace = new AdminCommandExecutor(this);
         CommandExecutor pce = new PlayerCommandExecutor(this);
         CommandExecutor sce = new SetupCommandExecutor(this);
 
         getCommand("qload").setExecutor(ace);
         getCommand("qunload").setExecutor(ace);
         getCommand("qrespawnmobs").setExecutor(ace);
         getCommand("qconvertinv").setExecutor(ace);
 
         getCommand("qjoin").setExecutor(pce);
         getCommand("qenter").setExecutor(pce);
         getCommand("qleave").setExecutor(pce);
         getCommand("qmoney").setExecutor(pce);
         getCommand("qkeys").setExecutor(pce);
         getCommand("qscore").setExecutor(pce);
         getCommand("qrank").setExecutor(pce);
         getCommand("qtop").setExecutor(pce);
         getCommand("qzones").setExecutor(pce);
         getCommand("qplayers").setExecutor(pce);
         getCommand("qkit").setExecutor(pce);
         getCommand("qkits").setExecutor(pce);
 
         getCommand("qsetlobby").setExecutor(sce);
         getCommand("qsetentrance").setExecutor(sce);
 
         config = getConfig();
 
         findRegionProvider();
 
         setupLeaderboards();
         setupGUIHandler();
 
         permissions = new PermissionsHandler(this).getPermissions();
 
         getServer().getPluginManager().registerEvents(new QListener(this), this);
 
         loadStartUpZones();
 
         startMetrics();
 
         log.info("[Quarantine] Plugin enabled. v" + getDescription().getVersion() + ", by DDoS");
 
     }
 
     @Override
     public void onDisable() {
 
         unLoadAllZones();
         zones.clear();
         log.info("[Quarantine] Plugin disabled. v" + getDescription().getVersion() + ", by DDoS");
 
     }
 
     public Collection<Zone> getZones() {
 
         return zones.values();
 
     }
 
     public boolean hasZone(String zoneName) {
 
         return zones.containsKey(zoneName.toLowerCase());
 
     }
 
     public Zone getZone(String zoneName) {
 
         return zones.get(zoneName.toLowerCase());
 
     }
 
     public Zone addZone(String zoneName, Zone zone) {
 
         return zones.put(zoneName.toLowerCase(), zone);
 
     }
 
     public void removeZone(String zoneName) {
 
         zones.remove(zoneName.toLowerCase());
 
     }
 
     public boolean hasRegionProvider() {
 
         return regionProvider != null;
 
     }
 
     public RegionProvider getRegionProvider() {
 
         return regionProvider;
 
     }
 
     public Permissions getPermissions() {
 
         return permissions;
 
     }
 
     public FileConfiguration getConfigFile() {
 
         return config;
 
     }
 
     public GUIHandler getGUIHandler() {
 
         return guiHandler;
 
     }
 
     private void findRegionProvider() {
 
         String providerName = config.getString("Region_Provider_Plugin");
 
         if (providerName.equalsIgnoreCase("worldguard")) {
 
             Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 
             if (plugin != null && plugin instanceof WorldGuardPlugin) {
 
                 regionProvider = new WorldGuardRegionProvider(((WorldGuardPlugin) plugin).getGlobalRegionManager());
                 log.info("[Quarantine] Will be using WorldGuard as a region provider.");
 
             } else {
 
                 log.info("[Quarantine] Couldn't find WorldGuard! This plugin will not work!.");
 
             }
 
         } else if (providerName.equalsIgnoreCase("residence")) {
 
             Plugin plugin = getServer().getPluginManager().getPlugin("Residence");
 
             if (plugin != null && plugin instanceof Residence) {
 
                 regionProvider = new ResidenceRegionProvider();
                 log.info("[Quarantine] Will be using Residence as a region provider.");
 
             } else {
 
                 log.info("[Quarantine] Couldn't find Residence! This plugin will not work!.");
 
             }
 
         } else if (providerName.equalsIgnoreCase("regios")) {
 
             Plugin plugin = getServer().getPluginManager().getPlugin("Regios");
 
             if (plugin != null && plugin instanceof Regios) {
 
                 regionProvider = new RegiosRegionProvider();
                 log.info("[Quarantine] Will be using Regios as a region provider.");
 
             } else {
 
                 log.info("[Quarantine] Couldn't find Regios! This plugin will not work!.");
 
             }
 
         } else {
 
             log.info("[Quarantine] No region provider defined! This plugin will not work!.");
 
         }
     }
 
     private void setupGUIHandler() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("Spout");
 
         if (plugin != null) {
 
             log.info("[Quarantine] Spout detected. Spout GUI enabled.");
             guiHandler = new SpoutEnabledGUIHandler(this);
 
         } else {
 
             log.info("[Quarantine] No Spout detected. Spout GUI disabled.");
             guiHandler = new TextGUIHandler(this);
 
         }
     }
 
     private void unLoadAllZones() {
 
         for (Zone zone : getZones()) {
 
             unloadZone(zone);
 
         }
     }
 
     public void unloadZone(Zone zone) {
 
         removePlayers(zone);
         saveZoneLocations(zone);
         disconnectLB(zone);
 
     }
 
     private void saveZoneLocations(Zone zone) {
 
         zone.saveLocations(config);
 
     }
 
     private void removePlayers(Zone zone) {
 
         zone.removeAllPlayers();
 
     }
 
     private void disconnectLB(Zone zone) {
 
         zone.disconnectLeaderboards();
 
     }
 
     private void loadStartUpZones() {
 
         if (regionProvider == null) {
 
             return;
 
         }
 
         for (String zoneToLoad : config.getStringList("Load_on_start")) {

             ZoneLoader loader = new ZoneLoader();
             Zone zone = loader.loadZone(this, zoneToLoad);
 
             if (zone == null) {
 
                 log.info("[Quarantine] Couldn't load zone " + zoneToLoad + " on start up.");
                 return;
 
             }
 
             addZone(zoneToLoad.toLowerCase(), zone);
 
             log.info("[Quarantine] Loaded zone " + zoneToLoad + ".");
 
         }
     }
 
     private void setupLeaderboards() {
 
         if (!config.getBoolean("Leaderboards.enabled")) {
 
             Leaderboard.TYPE = "None";
             return;
 
         }
 
         String type = config.getString("Leaderboards.type", "none");
 
         if (type.equalsIgnoreCase("redis")) {
 
             Leaderboard.ENABLED = true;
             Leaderboard.TYPE = "Redis";
             Leaderboard.HOST = config.getString("Leaderboards.redis_db_info.host");
             Leaderboard.PORT = config.getInt("Leaderboards.redis_db_info.port");
 
 
         } else if (type.equalsIgnoreCase("mysql")) {
 
             Leaderboard.ENABLED = true;
             Leaderboard.TYPE = "MySQL";
             Leaderboard.HOST = config.getString("Leaderboards.mysql_db_info.host");
             Leaderboard.DB_NAME = config.getString("Leaderboards.mysql_db_info.name");
             Leaderboard.PORT = config.getInt("Leaderboards.mysql_db_info.port");
             Leaderboard.USER = config.getString("Leaderboards.mysql_db_info.user");
             Leaderboard.PASSWORD = config.getString("Leaderboards.mysql_db_info.password");
 
         } else {
 
             Leaderboard.ENABLED = false;
             Leaderboard.TYPE = "None";
 
         }
     }
 
     private void checkLibs() {
 
         if (!new File("plugins/Quarantine/lib").exists()) {
 
             new File("plugins/Quarantine/lib").mkdir();
 
         }
 
         if (!new File("plugins/Quarantine/lib/jedis-2.0.0.jar").exists()) {
 
             log.info("[Quarantine] Downloading 'jedis-2.0.0.jar' library.");
 
             if (downloadJedisLib()) {
 
                 log.info("[Quarantine] Downloading done.");
 
             }
         }
     }
 
     private boolean downloadJedisLib() {
 
         try {
 
             URL url = new URL("http://dl.dropbox.com/u/43006973/jedis-2.0.0.jar");
             URLConnection connection = url.openConnection();
             DataInputStream inputStream = new DataInputStream(connection.getInputStream());
             byte[] fileData = new byte[connection.getContentLength()];
 
             for (int x = 0; x < fileData.length; x++) {
 
                 fileData[x] = inputStream.readByte();
 
             }
 
             inputStream.close();
             FileOutputStream outputStream = new FileOutputStream(new File("plugins/Quarantine/lib/jedis-2.0.0.jar"));
             outputStream.write(fileData);
             outputStream.close();
             return true;
 
         } catch (Exception ex) {
 
             log.info("[Quarantine] Couldn't download 'jedis-2.0.0.jar' library. Error: " + ex.getMessage());
             return false;
 
         }
     }
 
     private void startMetrics() {
 
         try {
 
             Metrics metrics = new Metrics(this);
 
             Graph generalInfo = metrics.createGraph("General info");
 
             generalInfo.addPlotter(new Metrics.Plotter("Number of Zones") {
 
                 @Override
                 public int getValue() {
 
                     return zones.size();
 
                 }
             });
 
             generalInfo.addPlotter(new Metrics.Plotter("Number of Players") {
 
                 @Override
                 public int getValue() {
 
                     int playerCount = 0;
 
                     for (Zone zone : getZones()) {
 
                         playerCount += zone.getNumberOfPlayers();
 
                     }
 
                     return playerCount;
 
                 }
             });
 
             Graph leaderboardInfo = metrics.createGraph("Leaderboard Types");
 
             leaderboardInfo.addPlotter(new Metrics.Plotter(Leaderboard.TYPE) {
 
                 @Override
                 public int getValue() {
 
                     return 1;
 
                 }
             });
 
             if (regionProvider != null) {
 
                 Graph regionProviderInfo = metrics.createGraph("Region Providers");
 
                 regionProviderInfo.addPlotter(new Metrics.Plotter(regionProvider.getName()) {
 
                     @Override
                     public int getValue() {
 
                         return 1;
 
                     }
                 });
             }
 
             metrics.start();
 
         } catch (IOException ex) {
 
             log.info("[Quarantine] Couldn't start Plugin Metrics. Error: " + ex.getMessage());
 
         }
     }
 }
