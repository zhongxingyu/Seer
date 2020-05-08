 package crussell52.poi;
 
 import crussell52.poi.api.IPoiListener;
 import crussell52.poi.api.IPointsOfInterest;
 import crussell52.poi.api.PoiEvent;
 import crussell52.poi.commands.PoiCommand;
 import crussell52.poi.listeners.PlayerListener;
 import crussell52.poi.listeners.SignListener;
 import crussell52.poi.markers.MarkerManager;
 import org.apache.commons.lang3.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * PointsOfInterest for Bukkit
  *
  * @author crussell52
  */
 public class PointsOfInterest extends JavaPlugin implements IPointsOfInterest
 {
     /**
      * Does the heavy lifting for POI interactions.
      */
     private final PoiManager _poiManager = new PoiManager();
 
     /**
      * Used to keep track of who is listening
      */
     private static final Map<PoiEvent.Type, ArrayList<IPoiListener>> _listeners = new HashMap<PoiEvent.Type, ArrayList<IPoiListener>>();
 
     public static boolean resemblesPoiSign(Block block)
     {
         return block != null && (block.getState() instanceof Sign) &&
                 ((Sign) block.getState()).getLine(2).replaceAll("(?i)\u00A7[0-F]", "").matches("^POI\\[[0-9]+] by:$");
     }
 
     public static void setSignText(String[] text, String title1, String title2, String ownerName, int poiID)
     {
         text[0] = title1;
         text[1] = title2;
         text[2] = ChatColor.DARK_GRAY + "POI[" + poiID + "] by:";
         text[3] = ChatColor.DARK_GRAY + StringUtils.abbreviateMiddle(ownerName, "..", 15);
     }
 
     public static void setSignText(String[] text, String name, String ownerName, int poiID) {
         Pattern pattern = Pattern.compile("^(.{0,15})((?: .*$|$))");
         Matcher matcher = pattern.matcher(name);
         if (matcher.matches()) {
             // We were able to identify sign-friendly lines. Set them to th sign.
             setSignText(text, matcher.group(1), matcher.group(2), ownerName, poiID);
         }
         else {
             // Can't split into sign-friendly lines. This is a pre-sign POI.
             setSignText(text, StringUtils.abbreviateMiddle(name, "..", 15), "", ownerName, poiID);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void registerPoiListener(PoiEvent.Type type, IPoiListener poiListener) {
         // see if we have a container for this listener type
         if (!_listeners.containsKey(type)) {
             // we do not; create one.
             _listeners.put(type, new ArrayList<IPoiListener>());
         }
 
         // get the container for this type of listener
         ArrayList<IPoiListener> listenerList = _listeners.get(type);
 
         // don't register the listener if it is already registered for this event.
         if (!listenerList.contains(poiListener)) {
             this.getLogger().warning("Same IPoiListener registered more than once for the same event!");
             listenerList.add(poiListener);
         }
     }
 
     /**
      * Used to notify all listeners when a PoiEvent occurs.
      *
      * @param event
      */
     public static void notifyListeners(PoiEvent event) {
         // nothing to do if we don't have listeners for this type of event.
         if (!_listeners.containsKey(event.getType())) {
             return;
         }
 
         // call the onEvent method of every listener for this type.
         for (IPoiListener listener : _listeners.get(event.getType())) {
             listener.onEvent(event);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void onEnable() {
 
         // get plugin description
         PluginDescriptionFile pdfFile = this.getDescription();
 
         // create files necessary for operation
         _createSupportingFiles();
 
         // attempt to load configuration
         if(!Config.load(this.getDataFolder(), this.getLogger())) {
             // something went wrong reading in the config -- unsafe to run
             this.getLogger().severe(pdfFile.getName() + ": encountered problem loading config - Unsure if it is safe to run. Disabled.");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
         // Build dynamic permissions based on values from the config. Give them all a default value
         // of false so that ops don't automatically end up with them and get stuck with the lowest
         // maximum possible.
         for (String key : Config.getMaxPoiMap().keySet()) {
             getServer().getPluginManager().addPermission(new Permission("crussell52.poi.max." + key, PermissionDefault.FALSE));
         }
 
         // attempt to initialize the the poi manager.
         if (!this._poiManager.initialize(this.getDataFolder(), this.getLogger())) {
             this.getLogger().severe(pdfFile.getName() + ": encountered problem preparing poi manager - Unsure if it is safe to run. Disabled.");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
         // Update POI signs for all pre-loaded chunks.
         for (World world : getServer().getWorlds()) {
             if (Config.isWorldSupported(world.getName())) {
                 for (Chunk chunk : world.getLoadedChunks()) {
                     try {
                         updateChunkSigns(chunk);
                     } catch (Exception ignored) {
                         getLogger().warning("Startup: unable to update signs in chunk." + chunk.getX() + "," + chunk.getZ());
                     }
                 }
             }
         }
 
         // handle the poi command
         getCommand("poi").setExecutor(new PoiCommand(this._poiManager));
 
         final PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new PlayerListener(this._poiManager, this), this);
         pm.registerEvents(new SignListener(this._poiManager, this), this);
 
 //        try {
 //            MarkerManager markerManager = new MarkerManager(this);
 //            _poiManager.setMarkerManager(markerManager);
 //            getLogger().info("Dynmap marker support enabled. Creating markers...");
 //            markerManager.setMarkers(_poiManager.getAll());
 //        }
 //        catch (PoiException poiEx) {
 //            _poiManager.setMarkerManager(null);
 //            getLogger().severe("Unable to create markers. Disabling Marker support.");
 //            poiEx.getCause().printStackTrace();
 //        }
 //        catch (Exception e) {
 //            getLogger().info("Dynmap marker support NOT enabled.");
 //        }
     }
 
     /**
      * Responsible for creating files necessary for operation
      */
     protected void _createSupportingFiles() {
         try {
             if (!this.getDataFolder().exists() && !this.getDataFolder().mkdir()) {
                 throw new Exception("Failed to create data directory.");
             }
 
         } catch (Exception ex) {
             this.getLogger().severe("PointsOfInterest failed to create supporting files with error:" + ex);
         }
     }
 
     public static String getDirections(Vector source, Vector target, ChatColor colorCode)
     {
         int distance = (int)source.distance(target);
         String directions = colorCode + "    " + distance + " meters (";
 
         int deltaX = (int)(source.getX() - target.getX());
         int deltaY = (int)(source.getY() - target.getY());
         int deltaZ = (int)(source.getZ() - target.getZ());
 
         directions += (deltaX > 0 ? "West: " : "East: ") + Math.abs(deltaX) + ", ";
         directions += (deltaZ > 0 ? "North: " : "South: ") + Math.abs(deltaZ) + ", ";
         directions += (deltaY > 0 ? "Down: " : "Up: ") + Math.abs(deltaY) + ")";
 
         return directions;
     }
 
     public void updateChunkSigns(Chunk chunk) {
         try {
             List<Poi> results = _poiManager.getChunkPoi(chunk);
 
             for (Poi poi : results) {
                getLogger().info(poi.toString());
                 Block block = chunk.getWorld().getBlockAt(poi.getX(), poi.getY(), poi.getZ());
                getLogger().info(block.getLocation().toString());
                 if (!PointsOfInterest.resemblesPoiSign(block)) {
                     block.setType(Material.SIGN_POST);
                 }
 
                 Sign sign = (Sign) block.getState();
                 String[] lines = new String[] {"", "", "", ""};
                 PointsOfInterest.setSignText(lines, poi.getName(), poi.getOwner(), poi.getId());
                 for (int i = 0; i < lines.length; i++) {
                     sign.setLine(i, lines[i]);
                 }
                 sign.update();
             }
         } catch (Exception e) {
             getLogger().warning("Unable to update POI signs in chunk. Exception to follow");
             e.printStackTrace();
         }
     }
 
     public void updateChunkSigns(String worldName, int chunkX, int chunkZ) {
         updateChunkSigns(getServer().getWorld(worldName).getChunkAt(chunkX, chunkZ));
     }
 }
 
