 /**
  * OrangeServer - Package: net.orange_server.orangeserver.utils.plugin
  * Created: 2013/01/05 2:59:10
  */
 package net.orange_server.orangeserver.utils.plugin;
 
 import net.orange_server.orangeserver.OrangeServer;
 import net.syamn.utils.LogUtil;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.dynmap.DynmapAPI;
 import org.dynmap.Log;
 import org.dynmap.markers.MarkerAPI;
 
 /**
  * DynmapHandler (DynmapHandler.java)
  * @author syam(syamn)
  */
 public class DynmapHandler {
     private static boolean listenerActivated = false;
     private static DynmapHandler instance;
     public static DynmapHandler getInstance(){
         return instance;
     }
     public static void dispose(){
         instance = null;
     }
     public static void createInstance(){
         instance = new DynmapHandler();
     }
     
     private final OrangeServer plugin;
     
     private Plugin dynmap;
     private DynmapAPI api;
     private MarkerAPI markerapi;
     
     private boolean activated = false;
     
     /**
      * コンストラクタ
      */
     private DynmapHandler(){
         this.plugin = OrangeServer.getInstance();
         init();
     }
     
     /**
      * 初期化
      */
     public void init(){
         // regist listener
         if (!listenerActivated){
             listenerActivated = true;
             plugin.getServer().getPluginManager().registerEvents(new DynmapPluginListener(), plugin);
         }
         
         activate();
     }
     
     /**
      * 有効化
      */
     public void activate(){
         if (activated) return;
         
         // get dynmap
         PluginManager pm = plugin.getServer().getPluginManager();
         dynmap = pm.getPlugin("dynmap");
         if (dynmap == null){
             LogUtil.warning("Cannot find dynmap!");
         }
         
         if (!dynmap.isEnabled()){
             LogUtil.warning("Dynmap is not enabled!");
         }
         
         // get api
         api = (DynmapAPI) dynmap;
         
         // get marker API
         markerapi = api.getMarkerAPI();
         if (markerapi == null){
             Log.warning("Cannot loading Dynmap marker API!");
             return;
         }
         
         // TODO do stuff..
         
         // Activated!
         activated = true;
         LogUtil.info("Hooked to dynmap!");
     }
     /**
      * 無効化
      */
     public void deactivate(){
         if (markerapi != null){
             // TODO Clear all added markers
         }
         dynmap = null;
         api = null;
         markerapi = null;
         
         activated = false;
     }
     
     /**
      * dynmap上でのプレイヤー表示/非表示を切り変える
      * @param player
      * @param visible
      */
     public void setPlayerVisiblity(final Player player, final boolean visible){
         if (activated && api != null){
             api.setPlayerVisiblity(player, visible);
         }
     }
     
     /**
      * 有効かどうかを返す
      * @return
      */
     public boolean isActivated(){
         return activated;
     }
     
     /**
      * Dynmapの有効を検出するリスナー
      * DynmapPluginListener (DynmapHandler.java)
      * @author syam(syamn)
      */
     private class DynmapPluginListener implements Listener {
         @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
         public void onPluginEnable(final PluginEnableEvent event) {
             final Plugin plugin = event.getPlugin();
             if (plugin.getDescription().getName().equals("dynmap")) {
                 activate();
             }
         }
         @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
         public void onPluginDisable(final PluginDisableEvent event) {
             final Plugin plugin = event.getPlugin();
             if (plugin.getDescription().getName().equals("dynmap")) {
                 deactivate();
             }
         }
     }
 }
