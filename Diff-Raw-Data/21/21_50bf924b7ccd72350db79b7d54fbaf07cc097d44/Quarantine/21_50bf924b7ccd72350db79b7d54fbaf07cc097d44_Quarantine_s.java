 package me.DDoS.Quarantine;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 
 import me.DDoS.Quarantine.command.*;
 import me.DDoS.Quarantine.leaderboard.Leaderboard;
 import me.DDoS.Quarantine.zone.ZoneLoader;
 import me.DDoS.Quarantine.listener.QListener;
 import me.DDoS.Quarantine.zone.Zone;
 import me.DDoS.Quarantine.gui.*;
 import me.DDoS.Quarantine.permission.Permissions;
 import me.DDoS.Quarantine.permission.PermissionsHandler;
 import me.DDoS.Quarantine.util.EconomyConverter;
 import me.DDoS.Quarantine.zone.region.provider.*;
 import me.DDoS.Quarantine.util.Metrics;
 import me.DDoS.Quarantine.util.Metrics.Graph;
 import me.DDoS.Quarantine.util.Metrics.Plotter;
 import me.DDoS.Quarantine.player.QPlayer;
 
 import com.bekvon.bukkit.residence.Residence;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 import couk.Adamki11s.Regios.Main.Regios;
 
 import net.milkbowl.vault.economy.Economy;
 
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
     //
     private EconomyConverter economyConverter;
 
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
         getCommand("qconvertmoney").setExecutor(pce);
 
         getCommand("qsetlobby").setExecutor(sce);
         getCommand("qsetentrance").setExecutor(sce);
 
         checkFiles();
 
         config = getConfig();
 
         findRegionProvider();
 
         setupGUIHandler();
         setupEconomyConverter();
         setupLeaderboards();
 
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
 
     public boolean isQuarantinePlayer(String playerName) {
 
         for (Zone zone : getZones()) {
 
             if (zone.hasPlayer(playerName)) {
 
                 return true;
 
             }
         }
 
         return false;
 
     }
 
     public QPlayer getQuarantinePlayer(String playerName) {
 
         for (Zone zone : getZones()) {
 
             if (zone.hasPlayer(playerName)) {
 
                 return zone.getPlayer(playerName);
 
             }
         }
 
         return null;
 
     }
 
     public Zone getZoneByPlayer(String playerName) {
 
         for (Zone zone : getZones()) {
 
             if (zone.hasPlayer(playerName)) {
 
                 return zone;
 
             }
         }
 
         return null;
 
     }
 
     public Zone getZoneByMob(LivingEntity living) {
 
         for (Zone zone : getZones()) {
 
             if (zone.hasMob(living)) {
 
                 return zone;
 
             }
         }
 
         return null;
 
     }
 
     public Zone getZoneByLocation(Location loc) {
 
         for (Zone zone : getZones()) {
 
             if (zone.isInZone(loc)) {
 
                 return zone;
 
             }
         }
 
         return null;
 
     }
 
     public Zone getZoneByChunk(Chunk chunk) {
 
         for (Zone zone : getZones()) {
 
             if (zone.isInZone(chunk)) {
 
                 return zone;
 
             }
         }
 
         return null;
 
     }
 
     public boolean hasRegionProvider() {
 
         return regionProvider != null;
 
     }
 
     public RegionProvider getRegionProvider() {
 
         return regionProvider;
 
     }
 
     public EconomyConverter getEconomyConverter() {
 
         return economyConverter;
 
     }
     
     public boolean hasEconomyConverter() {
 
         return economyConverter != null;
 
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
 
         Plugin plugin = getServer().getPluginManager().getPlugin("Spout");
 
         if (plugin != null) {
 
             log.info("[Quarantine] Spout detected. Spout GUI enabled.");
             guiHandler = new SpoutEnabledGUIHandler(this);
 
         } else {
 
             log.info("[Quarantine] No Spout detected. Spout GUI disabled.");
             guiHandler = new TextGUIHandler(this);
 
         }
     }
 
     private void setupEconomyConverter() {
 
         ConfigurationSection configSec = config.getConfigurationSection("External_Economy_Link");
 
         if (!configSec.getBoolean("enabled")) {
 
             return;
 
         }
 
         Plugin plugin = getServer().getPluginManager().getPlugin("Vault");
 
         if (plugin == null) {
 
             log.info("[Quarantine] No Vault detected. Economy converter disabled.");
             return;
 
         }
 
        Economy economy = plugin.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
         float externalToInternalRate;
         float internalToExternalRate;
 
         if (configSec.getBoolean("external_to_internal.allow")) {
 
             externalToInternalRate = (float) configSec.getDouble("external_to_internal.rate");
 
         } else {
 
             externalToInternalRate = -1f;
 
         }
 
         if (configSec.getBoolean("internal_to_external.allow")) {
 
             internalToExternalRate = (float) configSec.getDouble("internal_to_external.rate");
 
         } else {
 
             internalToExternalRate = -1f;
 
         }
 
         economyConverter = new EconomyConverter(economy, externalToInternalRate, internalToExternalRate);
         log.info("[Quarantine] Vault detected. Economy converter enabled.");
 
     }
 
     private void unLoadAllZones() {
 
         for (Zone zone : getZones()) {
 
             unloadZone(zone);
 
         }
     }
 
     public void unloadZone(Zone zone) {
 
         zone.removeAllPlayers();
         zone.saveLocations(config);
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
 
             addZone(zoneToLoad, zone);
 
             log.info("[Quarantine] Loaded zone " + zoneToLoad + ".");
 
         }
     }
 
     private void setupLeaderboards() {
 
         ConfigurationSection configSec = config.getConfigurationSection("Leaderboards");
 
         if (!configSec.getBoolean("enabled")) {
 
             Leaderboard.TYPE = "None";
             return;
 
         }
 
         String type = configSec.getString("type", "none");
 
         if (type.equalsIgnoreCase("redis")) {
 
             Leaderboard.ENABLED = true;
             Leaderboard.TYPE = "Redis";
             Leaderboard.HOST = configSec.getString("redis_db_info.host");
             Leaderboard.PORT = configSec.getInt("redis_db_info.port");
 
 
         } else if (type.equalsIgnoreCase("mysql")) {
 
             Leaderboard.ENABLED = true;
             Leaderboard.TYPE = "MySQL";
             Leaderboard.HOST = configSec.getString("mysql_db_info.host");
             Leaderboard.DB_NAME = configSec.getString("mysql_db_info.name");
             Leaderboard.PORT = configSec.getInt("mysql_db_info.port");
             Leaderboard.USER = configSec.getString("mysql_db_info.user");
             Leaderboard.PASSWORD = configSec.getString("mysql_db_info.password");
 
         } else {
 
             Leaderboard.ENABLED = false;
             Leaderboard.TYPE = "None";
 
         }
     }
 
     private void checkFiles() {
 
         File mainDir = new File("plugins/Quarantine");
 
         if (!mainDir.exists()) {
 
             mainDir.mkdir();
 
         }
 
         File configDir = new File(mainDir.getPath() + "/config.yml");
 
         if (!configDir.exists()) {
 
             saveDefaultConfig();
 
         }
     }
 
     private void checkLibs() {
 
         File libDir = new File("plugins/Quarantine/lib");
 
         if (!libDir.exists()) {
 
             libDir.mkdir();
 
         }
 
         File jedisFile = new File(libDir.getPath() + "/jedis-2.0.0.jar");
 
         if (!jedisFile.exists()) {
 
             log.info("[Quarantine] Downloading 'jedis-2.0.0.jar' library.");
 
             if (downloadFile("http://dl.dropbox.com/u/43006973/jedis-2.0.0.jar", jedisFile)) {
 
                 log.info("[Quarantine] Downloading done.");
 
             } else {
 
                 log.info("[Quarantine] Couldn't download 'jedis-2.0.0.jar' library.");
 
             }
         }
     }
 
     private boolean downloadFile(String urlString, File outputFile) {
 
         try {
 
             URL url = new URL(urlString);
             URLConnection connection = url.openConnection();
             DataInputStream inputStream = new DataInputStream(connection.getInputStream());
             byte[] fileData = new byte[connection.getContentLength()];
 
             for (int x = 0; x < fileData.length; x++) {
 
                 fileData[x] = inputStream.readByte();
 
             }
 
             inputStream.close();
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             outputStream.write(fileData);
             outputStream.close();
             return true;
 
         } catch (Exception ex) {
 
             return false;
 
         }
     }
 
     private void startMetrics() {
 
         try {
 
             Metrics metrics = new Metrics(this);
 
             Graph generalInfo = metrics.createGraph("General info");
 
             generalInfo.addPlotter(new Plotter("Number of Zones") {
 
                 @Override
                 public int getValue() {
 
                     return zones.size();
 
                 }
             });
 
             generalInfo.addPlotter(new Plotter("Number of Players") {
 
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
 
             leaderboardInfo.addPlotter(new Plotter(Leaderboard.TYPE) {
 
                 @Override
                 public int getValue() {
 
                     return 1;
 
                 }
             });
 
             if (regionProvider != null) {
 
                 Graph regionProviderInfo = metrics.createGraph("Region Providers");
 
                 regionProviderInfo.addPlotter(new Plotter(regionProvider.getName()) {
 
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
