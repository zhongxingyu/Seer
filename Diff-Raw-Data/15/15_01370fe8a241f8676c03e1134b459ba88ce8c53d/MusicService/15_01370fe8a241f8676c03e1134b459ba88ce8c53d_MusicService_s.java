 package blakesteel.MusicService;
 
 import com.massivecraft.factions.Faction;
 import com.palmergames.bukkit.towny.Towny;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import couk.Adamki11s.Regios.Regions.GlobalRegionManager;
 import couk.Adamki11s.Regios.Regions.Region;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.techguard.izone.managers.ZoneManager;
 import net.techguard.izone.zones.Zone;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Music Service plugin for Bukkit
  * 
  * @author Palisade
  */
 public class MusicService extends JavaPlugin {
     public static boolean DEBUG = false;
     private static final Logger logger = Logger.getLogger("Minecraft");
     private static final String LOG_PREFIX = "[MusicService] ";
     private boolean pluginEnabled = false;
     private Map<String, Integer> radioTowers = new HashMap<String, Integer>();
     private Map<String, String> radioStations = new HashMap<String, String>();
     private Map<String, String> playerStations = new HashMap<String, String>();
     private Map<String, List<StreamInfo>> playerSearches = new HashMap<String, List<StreamInfo>>();
     private Map<String, String> playerStandingInTown = new HashMap<String, String>();
     private Map<String, Boolean> musicChangeScheduled = new HashMap<String, Boolean>();
     private Map<String, FileConfiguration> configurations = new HashMap<String, FileConfiguration>();
     private WebMemoryFile templateWebFile = null;
     private String refreshUrl = "http://localhost:8888/";
     private Plugin factions;
     private Plugin regios;
     private Plugin izone;
     private Plugin worldguard;
     private Plugin towny;
     private WebServer webserver;
     private int wwwPort = 8888;
     private String refresh = "<meta HTTP-EQUIV=\"REFRESH\" content=\"2; url=#REFRESH" +
                              "#USER.html\">There is currently no music station available at this position.";
     
     //
     // Properties
     //
     
     public boolean isPluginEnabled() {
         return pluginEnabled;
     }
     
     public Map<String, Integer> getRadioTowers() {
         return radioTowers;
     }
     
     //
     // Bukkit Plugin Events
     //
 
     @Override
     public void onEnable() {
         // Register the music service listener for events.
         getServer().getPluginManager().registerEvents(new MusicServiceListener(this), this);
 
         // Try to hook the towny listener.
         try {
             towny = SupportTowny.getPlugin(getServer());
             getServer().getPluginManager().registerEvents(new MusicServiceTownyListener((Towny)towny, this), this);
             addConfig("town");
             info("Hooked Towny Support");
         } catch (PluginUnavailableException ex) {
         }
         
         // Try to hook the regios support.
         try {
             regios = SupportRegios.getPlugin(getServer());
             addConfig("regios");
             info("Hooked Regios Support");
         } catch (PluginUnavailableException ex) {
         }
         
         // Try to hook the iZone support.
         try {
             izone = SupportIZone.getPlugin(getServer());
             addConfig("izone");
             info("Hooked iZone Support");
         } catch (PluginUnavailableException ex) {
         }
 
         try {
             // Get the factions plugin, if it exists.
             factions = SupportFactions.getPlugin(getServer());
             addConfig("faction");
             info("Hooked Factions Support");
         } catch (PluginUnavailableException ex) {
         }
         
         try {
             worldguard = SupportWorldguard.getPlugin(getServer());
             addConfig("worldguard");
             info("Hooked WorldGuard Support");
         } catch (PluginUnavailableException ex) {
         }
 
         addConfig("wilderness");
 
         // Creates a config.yml if there isn't yet one.
         getConfig().options().copyDefaults(true);
         saveConfig();
         
         // Default to plugin disabled.
         pluginEnabled = false;
         
         info("Loading configuration....");
 
         // Is the refreshUrl configured?
         if (getConfig().contains("refreshUrl")) {
             // Get the refresh URL.
             refreshUrl = getConfig().getString("refreshUrl");
         }
 
         info("Refresh URL: " + refreshUrl);
 
         if (getConfig().contains("wwwPort")) {
             wwwPort = getConfig().getInt("wwwPort");
         }
         
         info("WWW Port: " + wwwPort);
 
         try {
             String resource = Utility.loadFromResourceAsString(getClass(), "music.html");
             templateWebFile = new WebMemoryFile(resource);
         } catch (IOException ex) {
             StringWriter sw = new StringWriter();
             ex.printStackTrace(new PrintWriter(sw));
             severe("Error: " + sw.toString());
         }
 
         webserver = new WebServer();
         setWebFile("favicon.ico");
         setWebFile("ffmp3.swf");
         setWebFile("green.png");
         setWebFile("index.html");
         setWebFile("yellow.png");
         webserver.start(wwwPort);
 
         // Plugin is ready at this point.
         pluginEnabled = true;
         
         info(pluginEnabled ? "Enabled" : "Disabled");
     }
     
     @Override
     public void onDisable() {
         // Plugin disabled.
         pluginEnabled = false;
         
         if (webserver != null)
             webserver.shutdown();
         
         info("Disabled.");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         // Plugin not enabled? Return.
         if (!isPluginEnabled()) return false;
 
         // Player.
         final Player player;
 
         // Is a player? Store player variable.
         if (sender instanceof Player) {
             player = (Player)sender;
         }
         // Is server console?
         else {
             sender.sendMessage("MusicService: Command can only be run by a player.");
             return false;
         }
         if (cmd.getName().equalsIgnoreCase("MusicService")) {
             if (args.length > 0) {
                 // Removing first parameter (command)
                 String[] cmdArgs = new String[args.length - 1];
                 System.arraycopy(args, 1, cmdArgs, 0, args.length - 1);
                 
                 if (args[0].equalsIgnoreCase("findshout")) { // /music findshout
                     if (player.hasPermission("musicservice.findshout"))
                         return commandFindShout(player, cmdArgs);
                 }
                 else if (args[0].equalsIgnoreCase("findice")) { // /music findice
                     if (player.hasPermission("musicservice.findice"))
                         return commandFindIce(player, cmdArgs);
                 }
                 else if (args[0].equalsIgnoreCase("pick")) { // /music pick
                     if (player.hasPermission("musicservice.pick"))
                         return commandPick(player, cmdArgs);
                 }
                 else if (args[0].equalsIgnoreCase("setshout")) { // /music setshout
                     if (player.hasPermission("musicservice.setshout"))
                         return commandSetShout(player, cmdArgs);
                 }
                 else if (args[0].equalsIgnoreCase("setice")) { // /music setice
                     if (player.hasPermission("musicservice.setice"))
                         return commandSetIce(player, cmdArgs);
                 }
                 else if (args[0].equalsIgnoreCase("reload")) { // /music reload
                     if (player.hasPermission("musicservice.reload"))
                         return commandReload(player, cmdArgs);
                 }
                 // It might be a known command but you may not have permission, so we need an else here.
                 else {
                     sendMessage(player, "Unknown command...");
                 }
                 return commandHelp(player);
             } else {
                 return commandHelp(player);
             }
         }
         return false;
     }
 
     private boolean commandFindShout(final Player player, String[] args) {
         // Has arguments?
         if (args.length > 0) {
             String search = "";
             for (int t=0; t < args.length; t++) {
                 search += args[t] + " ";
             }
             
             final String searchString = search;
             info(player.getName() + " findshout: " + searchString);
             sendMessage(player, "findshout: " + searchString);
 
             Bukkit.getScheduler().scheduleAsyncDelayedTask(
                 this,
                 new Runnable() {
                     @Override
                     public void run() {
                         List<StreamInfo> streams = new ArrayList<StreamInfo>();
                         try {
                             // Find the music and get the streams.
                             SupportShoutcast.find(0, streams, player, searchString);
                         } catch (Exception ex) {
                             StringWriter sw = new StringWriter();
                             ex.printStackTrace(new PrintWriter(sw));
                             severe("Error: " + sw.toString());
                         }
                         
                         if (streams.isEmpty()) {
                             sendMessage(player, "Sorry, there were no stations for: " + searchString);
                         }
                         else {
                             // Track the search results for this player.
                             playerSearches.put(player.getName(), streams);
                         }
                     }
                 },
                 40 // 20 ticks = 1 second
             );
             return true; // Return true whether we found a station or not. Command invoked properly.
         } else {
             sendMessage(player, "Type /music findshout <searchString>");
         }
         return false; // Missing args? Command invoked improperly.
     }
 
     private boolean commandFindIce(final Player player, String[] args) {
         // Has arguments?
         if (args.length > 0) {
             String search = "";
             for (int t=0; t < args.length; t++) {
                 search += args[t] + " ";
             }
             
             final String searchString = search;
             info(player.getName() + " findice: " + searchString);
             sendMessage(player, "findice: " + searchString);
 
             Bukkit.getScheduler().scheduleAsyncDelayedTask(
                 this,
                 new Runnable() {
                     @Override
                     public void run() {
                         List<StreamInfo> streams = new ArrayList<StreamInfo>();
                         try {
                             // Find the music and get the streams.
                             SupportIcecast.find(0, streams, player, searchString);
                         } catch (Exception ex) {
                             StringWriter sw = new StringWriter();
                             ex.printStackTrace(new PrintWriter(sw));
                             severe("Error: " + sw.toString());
                         }
 
                         if (streams.isEmpty()) {
                             sendMessage(player, "Sorry, there were no stations for: " + searchString);
                         }
                         else {
                             // Track the search results for this player.
                             playerSearches.put(player.getName(), streams);
                         }
                     }
                 },
                 40 // 20 ticks = 1 second
             );
             return true; // Return true whether we found a station or not. Command invoked properly.
         } else {
             sendMessage(player, "Type /music findice <searchString>");
         }
         return false; // Missing args? Command invoked improperly.
     }
     
     private boolean commandPick(Player player, String[] args) {
         try {
             // Get the player name.
             String playerName = player.getName();
 
             if (args.length > 0)
                 info(playerName + " picking station: " + args[0]);
 
             // Player conducted a search?
             if (playerSearches.containsKey(playerName)) {
                 debug("Has search results.");
 
                 // Get the streams they found.
                 List<StreamInfo> infos = playerSearches.get(playerName);
 
                 debug("Got stream info list.");
 
                 if (infos != null && infos.size() > 0) {
                     boolean isIcecast = infos.get(0).Type == StreamInfo.StreamType.Icecast;
 
                     // Pick the music stream # they wanted.
                     pickMusic(player, infos, Integer.parseInt(args[0]), isIcecast);
 
                     debug("picked music");
                 }
 
                 return true;
             } else {
                 sendMessage(player, "First type: /music <findshout|findice> <searchString>");
             }
         }
         catch (NumberFormatException ex) {
             sendMessage(player, "Please type a number instead, found to the left of the search result you want to playback.");
         }
         return false;
     }
     
     private boolean commandSetShout(Player player, String[] args) {
         if (args.length > 0) {
             setStation(player, new String[] { args[0] }, false);
             return true;
         }
         sendMessage(player, "Type /music setshout http://yourdomainOrIp.com:8080");
         return false;
     }
 
     private boolean commandSetIce(Player player, String[] args) {
         if (args.length > 0) {
             setStation(player, new String[] { args[0] }, true);
             return true;
         }
         sendMessage(player, "Type /music setice http://yourdomainOrIp.com:8080");
         return false;
     }
     
     private boolean commandReload(final Player player, String[] args) {
         sendMessage(player, "Reloading music...");
         
         // Unset their music file.
         noPlayerMusic(player);
         
         // Schedule player station update.
         Bukkit.getScheduler().scheduleSyncDelayedTask(
             this,
             new Runnable() {
                 @Override
                 public void run() {
                     updatePlayerStation(player, player.getLocation());
                 }
             },
             40 // 20 ticks = 1 second
         );
         
         return true;
     }
     
     private boolean commandHelp(Player player) {
         sendMessage(player, "Commands:");
         if (player.hasPermission("musicservice.findshout"))
             sendMessage(player, "/music findshout <searchString>");
         if (player.hasPermission("musicservice.findice"))
             sendMessage(player, "/music findice <searchString>");
         if (player.hasPermission("musicservice.pick"))
             sendMessage(player, "/music pick <#>");
         if (player.hasPermission("musicservice.setshout"))
             sendMessage(player, "/music setshout <URL>");
         if (player.hasPermission("musicservice.setice"))
             sendMessage(player, "/music setice <URL>");
         if (player.hasPermission("musicservice.reload"))
             sendMessage(player, "/music reload");
         return true;
     }
     
     private void sendMessage(Player player, String message) {
         if (player != null) {
             player.sendMessage("MusicService: " + message);
         }
     }
     
     //
     // Webserver Routines
     //
     
     private void setWebFile(String path) {
         try {
             byte[] bytes = Utility.loadFromResourceAsBytes(getClass(), path);
             webserver.setMemoryValue(path, new WebMemoryFile(bytes));
         } catch (IOException ex) {
             StringWriter sw = new StringWriter();
             ex.printStackTrace(new PrintWriter(sw));
             severe("Error: " + sw.toString());
         }
     }
     
     //
     // Music Stream Support
     //
     
     private boolean setMusicInWilderness(final Player player, String[] args, boolean forceIcecast) {
         if (!pluginEnabled) return false;
         
         String regionName = Utility.getWildernessID(player.getLocation());
         
         // Has region name?
         if (regionName != null) {
             // User chose to blank out this parcel.
             if (args[0].equalsIgnoreCase("blank")) {
                 info(String.format("Removing music station for faction: %s", regionName));
                 
                 this.saveStation("wilderness", regionName, "", false);
 
                 // Stop the music.
                 noPlayerMusic(player);
 
                 sendMessage(player, "Removed music station.");
 
                 return true;
             }
 
             // Unset their music file.
             noPlayerMusic(player);
 
             // Get the URL argument.
             final String url = args[0];
             final boolean isIcecast = forceIcecast;
 
             // Has URL argument?
             if (url != null) {
                 Bukkit.getScheduler().scheduleSyncDelayedTask(
                     this,
                     new Runnable(){
                         @Override
                         public void run(){
                             setWildernessTowerStation(player, url, isIcecast);
                         }
                     },
                     40 // 20 ticks = 1 second
                 );
 
                 return true;
             }
         }
         
         return false;
     }
     
     private boolean setMusicOnFactionLand(final Player player, String[] args, boolean forceIcecast) {
         if (!pluginEnabled) return false;
 
         // Has factions?
         if (factions != null && factions.isEnabled()) {
             // Get the faction at the player's location, if any.
             Faction factionLand;
             
             try {
                 factionLand = SupportFactions.getFactionAtLocation(getServer(), player);
             } catch (PluginUnavailableException ex) {
                 return false;
             }
 
             // Does not have faction here?
             if (factionLand == null) {
                 sendMessage(player, "Faction did not exist.");
                 return false;
             }
 
             // Get faction name.
             String regionName = factionLand.getTag();
 
             // Assume we're not in wilderness.
             boolean isWilderness = false;
 
             // Has name?
             if (regionName != null) {
                 // Is it wilderness?
                 isWilderness = regionName.contains("Wilderness");
             }
 
             // Not a safe zone, war zone, or wilderness?
             if (!(factionLand.isSafeZone() || factionLand.isWarZone() || isWilderness)) {
                 try {
                     // Player was not the faction admin? Bail out.
                     if (!SupportFactions.isPlayerFactionAdmin(getServer(), factionLand, player)) {
                         sendMessage(player, "This is not your faction land.");
                         return false;
                     }
                } catch (PluginUnavailableException ex) {
                     return false;
                 }
             }
 
             // Not in wilderness.
             if (!isWilderness) {
                 // User chose to blank out this parcel.
                 if (args[0].equalsIgnoreCase("blank")) {
                     setStation("faction", regionName, player, "", forceIcecast);
                     return true;
                 }
 
                 // Unset their music file.
                 noPlayerMusic(player);
 
                 // Get the URL argument.
                 final String url = args[0];
                 final Faction faction = factionLand;
                 final boolean isIcecast = forceIcecast;
 
                 // Has URL argument?
                 if (url != null) {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(
                         this,
                         new Runnable(){
                             @Override
                             public void run(){
                                 setFactionStation(faction, player, url, isIcecast);
                             }
                         },
                         40 // 20 ticks = 1 second
                     );
 
                     return true;
                 }
             }
         }
         
         return false;
     }
     
     private void pickMusic(final Player player, List<StreamInfo> streams, final int index, final boolean isIcecast) {
         debug("pickMusic()");
         
         if (streams.size() > 0) {
             debug("has streams");
             
             final StreamInfo info = streams.get(index);
             final String[] args = new String[] {info.Host};
             
             Bukkit.getScheduler().scheduleSyncDelayedTask(
                 this,
                 new Runnable(){
                     @Override
                     public void run() {
                         try {
                             //Stream mainStream = null;
                             //URI uri = new URI(info.Host);
                             //List<Stream> iceStreams;
                             //List<Stream> shoutStreams;
                             /*IceCastScraper iceScraper = new IceCastScraper();
                             ShoutCastScraper shoutScraper = new ShoutCastScraper();
                             
                             try {
                                 iceStreams = iceScraper.scrape(uri);
                                 isIcecast = iceStreams.size() > 0;
                                 
                                 for (Stream stream : iceStreams) {
                                     if (stream.getUri().toURL().toString().equals(uri.toURL().toString())) {
                                         mainStream = stream;
                                         break;
                                     }
                                 }
                             } catch (ScrapeException ex) {
                                 isIcecast = false;
                             }
 
                             if (!isIcecast) {
                                 try {
                                     shoutStreams = shoutScraper.scrape(uri);
                                     isIcecast = false;
 
                                     for (Stream stream : shoutStreams) {
                                         if (stream.getUri().toURL().toString().equals(uri.toURL().toString())) {
                                             mainStream = stream;
                                             break;
                                         }
                                     }
                                 } catch (ScrapeException ex) {
                                 }
                             }
 
                             info("Station [" + index + "]: " + mainStream.toString());
                             sendMessage(player, "Station [" + index + "]: " + mainStream.toString());*/
 
                             debug("setStation()");
                             setStation(player, args, isIcecast);
                         }
                         catch (Exception ex) {
                             debug("Error: " + ex.getMessage());
                         }
                     }
                 },
                 40 // 20 ticks = 1 second
             );
         }
     }
 
     private boolean setStation(Player player, String[] args, boolean forceIcecast) {
         // Set music on faction land.
         if (setMusicOnFactionLand(player, args, forceIcecast)) {
             debug("Factions land");
             return true;
         }
         
         // Set music on town
         if (setTownyStation(player, args, forceIcecast)) {
             debug("Towny town");
             return true;
         }
         
         // Set music on wg region.
         if (setWorldGuardStation(player, args, forceIcecast)) {
             debug("WorldGuard region");
             return true;
         }
         
         // Set music on regios region.
         if (setRegiosStation(player, args, forceIcecast)) {
             debug("Regios region");
             return true;
         }
         
         // Set music on iZone zone
         if (setIZoneStation(player, args, forceIcecast)) {
             debug("iZone zone");
             return true;
         }
         
         // Set music on wilderness.
         if (setMusicInWilderness(player, args, forceIcecast)) {
             debug("Wilderness");
             return true;
         }
         
         // Only fails without a tower.
         sendMessage(player, "No tower here.");
         return false;
     }
     
     private void setMusicStation(Player player, String url, boolean forceIcecast) throws IOException {
         if (!pluginEnabled) return;
         
         String playerName = player.getName();
         String newUrl = "http://" + url.replace("http://", "");
         String playerPath = playerName + ".html";
         String txtPath = playerName + ".txt";
         String playerWebData = templateWebFile.getString();
 
         String rootUrl;
         try {
             rootUrl = "http://" + (new URL(newUrl).toURI().getHost()) + ":"
                     + (new URL(newUrl).toURI().getPort());
         }
         catch (Exception ex) {
             return;
         }
         
         // Has url? Replace #URL.
         if (newUrl != null)
             playerWebData = playerWebData.replace("#URL", newUrl);
 
         // Has player name? Replace #USER.
         if (playerName != null)
             playerWebData = playerWebData.replace("#USER", playerName);
         
         // Replace all occurances of #ROOT with the root url.
         playerWebData = playerWebData.replace("#ROOT", rootUrl);
 
         // Has icecast? If yes, force it.
         playerWebData = playerWebData.replace("#ISICECAST", (forceIcecast) ? "1" : "0");
 
         WebMemoryFile f = new WebMemoryFile(" ");
         webserver.setMemoryValue(txtPath, f);
 
         webserver.setMemoryValue(playerPath, new WebMemoryFile(playerWebData));
         
         debug("station set: " + newUrl);
         
         playerStations.put(player.getName(), newUrl);
     }
     
     private void changePlayerMusic(final Player player, final String url, boolean forceIcecast) {
         if (!pluginEnabled) return;
         
         // Player is on a station?
         if (playerStations.containsKey(player.getName())) {
             // Station already matches the one we're changing to?
             if (playerStations.get(player.getName()).equals(url)) {
                 // Done.
                 return;
             }
         }
 
         // Does not contain a music schedule change?
         if (!musicChangeScheduled.containsKey(player.getName())) {
             // Initialize schedule, default to no schedule.
             musicChangeScheduled.put(player.getName(), false);
         }
         
         // Not yet scheduled to change?
         if (!musicChangeScheduled.get(player.getName())) {
             // Schedule a change.
             musicChangeScheduled.put(player.getName(), true);
 
             // Turn off the music to flag the .txt file for the ajax to reset.
             noPlayerMusic(player);
             
             final boolean isIcecast = forceIcecast;
 
             // Schedule the task.
             Bukkit.getScheduler().scheduleSyncDelayedTask(
                 this,
                 new Runnable(){
                     @Override
                     public void run(){
                         try {
                             //debug("setting music station...");
                             setMusicStation(player, url, isIcecast);
                         } catch (IOException ex) {
                             severe(ex.getMessage());
                         }
 
                         // Music changed, unschedule.
                         musicChangeScheduled.put(player.getName(), false);
                     }
                 },
                 40 // 20 ticks = 1 second
             );
         }
     }
     
     private void noPlayerMusic(Player player) {
         if (!pluginEnabled) return;
 
         // Already has a station?
         if (playerStations.containsKey(player.getName())) {
             // Station is already set to none?
             if (playerStations.get(player.getName()).equals("")) {
                 // Done.
                 return;
             }
         }
 
         // Nothing is something too! ;-)
         playerStations.put(player.getName(), "");
 
         // Get the player name.
         String playerName = player.getName();
         String playerPath = playerName + ".html";
         String txtPath = playerName + ".txt";
 
         String newRefreshUrl = refreshUrl;
         if (!refreshUrl.endsWith("/")) {
             newRefreshUrl = refreshUrl + "/";
         }
         
         String rootUrl;
         try {
             rootUrl = "http://" + (new URL(refreshUrl).toURI().getHost());
         }
         catch (Exception ex) {
             return;
         }
         
         // Replace all occurances of #USER with the player name in the text.
         String newRefresh = refresh.replace("#USER", playerName);
 
         // Replace all occurances of #REFRESH with the refresh url.
         newRefresh = newRefresh.replace("#REFRESH", newRefreshUrl);
         
         // Replace all occurances of #ROOT with the root url.
         newRefresh = newRefresh.replace("#ROOT", rootUrl);
 
         WebMemoryFile f = new WebMemoryFile(" ");
         f.setEnabled(false);
         
         webserver.setMemoryValue(txtPath, f);
         webserver.setMemoryValue(playerPath, new WebMemoryFile(newRefresh));
     }
     
     public void updatePlayerStation(Player player, Location location) {
         if (!pluginEnabled) return;
         
         if (player != null && location != null) {
             // Change station for faction. Fails on wilderness / no faction.
             if (!handleFactions(player, location)) {
                 //debug("not a faction");
                 if (!handleTowny(player)) {
                     //debug("not a town");
                     // Change station for wg region. Fails on no region.
                     if (!handleWorldGuard(player)) {
                         //debug("not a wg region");
                         if (!handleRegios(player)) {
                             //debug("not a regios region");
                             if (!handleIZone(player)) {
                                 //debug("not an iZone region");
                                 // Must be wilderness, check for tower.
                                 if (!handleWilderness(player)) {
                                     //debug("no radio tower");
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
     
     //
     // Logging Support
     //
     
     public static void debug(String msg) {
         if (DEBUG) {
             logger.log(Level.INFO, String.format("%s(DEBUG) %s", LOG_PREFIX, msg));
         }
     }
 
     public static void info(String msg) {
         logger.log(Level.INFO, String.format("%s %s", LOG_PREFIX, msg));
     }
     
     public static void severe(String msg) {
         logger.log(Level.SEVERE, String.format("%s %s", LOG_PREFIX, msg));
     }
     
     //
     // Factions Support
     //
     
     private boolean handleFactions(Player player, Location location) {
         if (!pluginEnabled || factions == null) return false;
         
         // Faction exists?
         if (factions != null && factions.isEnabled()) {
             Faction faction;
             try {
                 faction = SupportFactions.getFactionAtLocation(getServer(), location);
             } catch (PluginUnavailableException ex) {
                 return false;
             }
             
             if (faction != null) {
                 // Get the faction region name.
                 String regionName = faction.getTag();
 
                 // Has region and it's not Wilderness, therfore it is faction land?
                 if (regionName != null) {
                     if (!regionName.contains("Wilderness")) {
                         return listenStation("faction", regionName, player, false);
                     }
                 }
             }
         }
         
         return false;
     }
 
     private boolean setFactionStation(Faction faction, Player player, String url, boolean forceIcecast) {
         if (!pluginEnabled || factions == null) return false;
         
         // Get the faction region name.
         String regionName = faction.getTag();
 
         if (regionName != null) {
             setStation("faction", regionName, player, url, forceIcecast);
             return true;
         }
         
         return false;
     }
     
     //
     // Wilderness Support
     //
     
     private boolean handleWilderness(Player player) {
         if (!pluginEnabled) return false;
         
         for (String locationString : radioTowers.keySet()) {
             int radius = radioTowers.get(locationString);
 
             Location location = Utility.stringToLocation(getServer(), locationString);
             
             if (Utility.towerIntersects(location, radius, player.getLocation())) {
                 return listenStation("wilderness", locationString, player, false);
             }
         }
         
         // Stop the music, we did not find any towers.
         noPlayerMusic(player);
         
         return false;
     }
     
     private boolean setWildernessTowerStation(Player player, String url, boolean forceIcecast) {
         if (!pluginEnabled) return false;
         
         for (String locationString : radioTowers.keySet()) {
             int radius = radioTowers.get(locationString);
 
             Location location = Utility.stringToLocation(getServer(), locationString);
             
             //debug(String.format("setTowerStation - TowerLocation: %f, %f TowerRadius: %d", location.getX(), location.getZ(), radius));
             
             if (Utility.towerIntersects(location, radius, player.getLocation())) {
                 if (url != null) {
                     radioStations.put(locationString, url);
                     setStation("wilderness", locationString, player, url, forceIcecast);
                     return true;
                 }
             }
         }
         
         return false;
     }
     
     //
     // WorldGuard Regions Support
     //
 
     private boolean handleWorldGuard(Player player) {
         if (!pluginEnabled || worldguard == null) return false;
 
         // Has player?
         if (player != null) {
             try {
                 ProtectedRegion stationRegion = SupportWorldguard.getWorldGuardRegionAt(getServer(), player);
                 
                 if (stationRegion != null) {
                     return listenStation("worldguard", stationRegion.getId(), player, false);
                 }
             }
             catch (PluginUnavailableException ex) {
                 // Fail silently.
             }
         }
 
         return false;
     }
     
     private boolean setWorldGuardStation(Player player, String[] args, boolean forceIcecast) {
         if (!pluginEnabled || worldguard == null) return false;
         
         try {
             ProtectedRegion stationRegion =
                     SupportWorldguard.isAdminAtWorldGuardRegionAt(getServer(), player);
             
             // Has a station region?
             if (stationRegion != null) {
                 if (args[0].equalsIgnoreCase("blank"))
                     setStation("worldguard", stationRegion.getId(), player, "", forceIcecast);
                 else
                     setStation("worldguard", stationRegion.getId(), player, args[0], forceIcecast);
                 
                 return true;
             }
         }
         catch (PluginUnavailableException ex) {
             // Quietly fail if there is no WorldGuard, user may have chosen not to install it.
         }
         
         return false;
     }
     
     //
     // Towny Advanced Support
     //
     
     public boolean handleTowny(Player player) {
         if (!pluginEnabled || towny == null) return false;
 
         try {
             // Has player?
             if (player != null) {
                 String name = getTownPlayerIsStandingIn(player);
 
                 // Has town here?
                 if (name != null) {
                     return listenStation("town", name, player, false);
                 }
                 else {
                     //debug("no town found here");
                 }
             }
         }
         catch (Exception ex) {
             // Fail silently. Might mean towny not installed, or no town here.
             StringWriter sw = new StringWriter();
             ex.printStackTrace(new PrintWriter(sw));
         }
 
         return false;
     }
     
     private boolean setTownyStation(Player player, String[] args, boolean forceIcecast) {
         if (!pluginEnabled || towny == null) return false;
         
         try {
             String name = getTownPlayerIsStandingIn(player);
             
             // Has town info?
             if (name != null) {
                 if (args[0].equalsIgnoreCase("blank"))
                     setStation("town", name, player, "", forceIcecast);
                 else
                     setStation("town", name, player, args[0], forceIcecast);
 
                 return true;
             }
         }
         catch (Exception ex) {
             // Fail silently. No plugin or no town here.
         }
         
         return false;
     }
     
     public void playerNowStandingInTown(Player player, String name) {
         if (player != null) {
             playerStandingInTown.put(player.getName(), name);
         }
     }
     
     public String getTownPlayerIsStandingIn(Player player) {
         if (player != null) {
             if (playerStandingInTown.containsKey(player.getName())) {
                 return playerStandingInTown.get(player.getName());
             }
         }
         return null;
     }
     
     //
     // Regios Support
     //
     
     private boolean handleRegios(Player player) {
         if (!pluginEnabled || regios == null) return false;
 
         try {
             // Has player?
             if (player != null) {
                 Region region = GlobalRegionManager.getRegion(player);
 
                 // Has regios region here?
                 if (region != null) {
                     String name = region.getName();
                     //debug("has regios here: " + name);
                     return listenStation("regios", name, player, false);
                 }
                 else {
                     //debug("no regios found here");
                 }
             }
         }
         catch (Exception ex) {
             // Fail silently. Might mean regios not installed, or no region here.
             StringWriter sw = new StringWriter();
             ex.printStackTrace(new PrintWriter(sw));
         }
 
         return false;
     }
     
     private boolean setRegiosStation(Player player, String[] args, boolean forceIcecast) {
         if (!pluginEnabled || regios == null) return false;
         
         try {
             Region region = GlobalRegionManager.getRegion(player);
             
             // Has regios info?
             if (region != null) {
                 String name = region.getName();
 
                 //debug(String.format("setRegiosStation: %s %s %s", player.getName(), name, args[0]));
                 
                 if (args[0].equalsIgnoreCase("blank"))
                     setStation("regios", name, player, "", forceIcecast);
                 else
                     setStation("regios", name, player, args[0], forceIcecast);
 
                 return true;
             }
         }
         catch (Exception ex) {
             // Fail silently. No plugin or no region here.
         }
         
         return false;
     }
     
     //
     // iZone Support
     //
     
     private boolean handleIZone(Player player) {
         if (!pluginEnabled || izone == null) return false;
 
         try {
             // Has player?
             if (player != null) {
                 Zone zone = ZoneManager.getZone(player.getLocation());
 
                 // Has iZone zone here?
                 if (zone != null) {
                     String name = zone.getName();
                     //debug("has zone here: " + name);
                     return listenStation("izone", name, player, false);
                 }
                 else {
                     //debug("no zone found here");
                 }
             }
         }
         catch (Exception ex) {
             // Fail silently. Might mean iZone not installed, or no zone here.
             StringWriter sw = new StringWriter();
             ex.printStackTrace(new PrintWriter(sw));
         }
 
         return false;
     }
     
     private boolean setIZoneStation(Player player, String[] args, boolean forceIcecast) {
         if (!pluginEnabled || izone == null) return false;
         
         try {
             Zone zone = ZoneManager.getZone(player.getLocation());
             
             // Has zone info?
             if (zone != null) {
                 String name = zone.getName();
 
                 //debug(String.format("setIZoneStation: %s %s %s", player.getName(), name, args[0]));
                 
                 if (args[0].equalsIgnoreCase("blank"))
                     setStation("izone", name, player, "", forceIcecast);
                 else
                     setStation("izone", name, player, args[0], forceIcecast);
 
                 return true;
             }
         }
         catch (Exception ex) {
             // Fail silently. No plugin or no zone here.
             //debug("Error setIZoneStation: " + ex.getMessage());
         }
         
         return false;
     }
     
     //
     // Station Support
     //
     
     private String getConfigurationName(String prefix) {
         return prefix + ".yml";
     }
     
     private String getFieldName(String prefix, String name) {
         return prefix + "-" + name;
     }
 
     private StreamInfo loadStation(String prefix, String fieldName) {
         StreamInfo info = null;
         
         // Has the configuration file in the configuration map?
         if (configurations.containsKey(prefix)) {
             // Get the configuration file.
             FileConfiguration stationConfig = configurations.get(prefix);
         
             // Has configuration file?
             if (stationConfig != null) {
                 // Create the stations configuration section if it does not already exist.
                 ConfigurationSection stationSection = Utility.configCreateSection(stationConfig, "stations");
 
                 if (stationSection != null) {
                     debug("Has Section: stations");
                     
                     String station = (String)Utility.configGetSectionKeypair(stationSection, fieldName);
                     
                     if (station != null) {
                         String type = (String)Utility.configGetSectionKeypair(stationSection, fieldName + "-type");
 
                         boolean isIcecast = false;
 
                         if (type != null) {
                             if (type.equalsIgnoreCase("icecast")) {
                                 isIcecast = true;
                             }
                         }
 
                         info = new StreamInfo();
                         info.Host = station;
                         info.Type = (isIcecast) ? StreamInfo.StreamType.Icecast : StreamInfo.StreamType.Shoutcast;
 
                         debug("Loaded configuration.");
                     }
                 }
             }
         }
         return info;
     }
     
     private void saveStation(String prefix, String fieldName, String station, boolean isIcecast) {
         debug("saveStation()");
         // Has the configuration file in the configuration map?
         if (configurations.containsKey(prefix)) {
             // Get the configuration file name.
             String configurationName = getConfigurationName(prefix);
             debug("Configuration Name: " + configurationName);
             
             // Form the yml station file.
             File stationFile = new File(configurationName);
 
             debug("Getting station config from configurations.");
             
             // Get the configuration file.
             FileConfiguration stationConfig = configurations.get(prefix);
         
             // Has configuration file?
             if (stationConfig != null) {
                 debug("Found station config in configurations. Getting stations section...");
                 
                 // Create the stations configuration section if it does not already exist.
                 ConfigurationSection stationSection = Utility.configCreateSection(stationConfig, "stations");
 
                 if (stationSection != null) {
                     debug("Has Section: stations");
                     
                     // Set a key value pair in the station section.
                     Utility.configSetSectionKeypair(stationSection, fieldName, station);
                     
                     if (isIcecast) {
                         Utility.configSetSectionKeypair(stationSection, fieldName + "-type", "icecast");
                     }
                     else {
                         Utility.configSetSectionKeypair(stationSection, fieldName + "-type", "shoutcast");
                     }
 
                     // Save the configuration yml file for our stations.
                     Utility.configSave(this, stationConfig, stationFile);
                     
                     debug("Saved configuration.");
                 }
             }
         }
     }
     
     private void setStation(String prefix, String name, Player player, String station, boolean forceIcecast) {
         String fieldName = getFieldName(prefix, name);
         
         saveStation(prefix, fieldName, station, forceIcecast);
         
         if (station.equals("")) {
             // Stop the music.
             noPlayerMusic(player);
 
             sendMessage(player, String.format("Removed music station for: %s", fieldName));
         }
         else {
             // Change the player's music to the URL specified immediately.
             changePlayerMusic(player, station, forceIcecast);
 
             sendMessage(player, String.format("Set Music Station: %s", station));
         }
     }
     
     private boolean listenStation(String prefix, String name, Player player, boolean forceIcecast) {
         debug("listenStation()");
         if (player != null) {
             debug("has player");
             String fieldName = getFieldName(prefix, name);
             debug("prefix: " + prefix + " fieldName: " + fieldName + " Loading station...");
 
             StreamInfo info = loadStation(prefix, fieldName);
             
             // Config holds an entry for the station?
             if (info != null) {
                 debug("has station");
 
                 // Get the station url.
                 String url = info.Host;
 
                 if (url != null) {
                     debug("has url");
                     
                     //boolean isIcecast = info.Type == StreamInfo.StreamType.Icecast || forceIcecast;
                     boolean isIcecast = info.Type == StreamInfo.StreamType.Icecast;
 
                     debug(String.format("Changing station for %s in %s to %s, isIcecast = %s", player.getName(), fieldName, url, (isIcecast) ? "yes" : "no"));
 
                     // Change the player music to the url for this station.
                     changePlayerMusic(player, url, isIcecast);
 
                     return true;
                 }
             }
             // No entry at this chunk.
             else {
                 debug("no player music here");
                 // No music at this chunk.
                 noPlayerMusic(player);
             }
         }
         
         return false;
     }
     
     private void addConfig(String configName) {
         File stationFile = new File(getConfigurationName(configName));
         FileConfiguration stationConfig = Utility.configReload(this, stationFile);
         configurations.put(configName, stationConfig);
         debug("added config for: " + configName);
     }
 }
