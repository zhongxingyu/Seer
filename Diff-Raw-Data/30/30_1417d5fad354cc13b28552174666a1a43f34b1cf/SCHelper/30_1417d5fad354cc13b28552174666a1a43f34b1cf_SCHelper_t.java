 /**
  * SakuraCmd - Package: net.syamn.sakuracmd
  * Created: 2013/01/04 13:02:22
  */
 package net.syamn.sakuracmd;
 
 import net.syamn.sakuracmd.feature.GeoIP;
 import net.syamn.sakuracmd.listener.feature.MCBansListener;
 import net.syamn.sakuracmd.permission.PermissionManager;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.storage.ConfigurationManager;
 import net.syamn.sakuracmd.storage.Database;
 import net.syamn.sakuracmd.storage.I18n;
 import net.syamn.sakuracmd.storage.ServerData;
 import net.syamn.sakuracmd.utils.plugin.DynmapHandler;
 import net.syamn.sakuracmd.utils.plugin.SakuraCmdUtil;
 import net.syamn.sakuracmd.worker.AFKWorker;
 import net.syamn.sakuracmd.worker.FlymodeWorker;
 import net.syamn.sakuracmd.worker.InvisibleWorker;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.queue.ConfirmQueue;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  * SCHelper (SCHelper.java)
  * @author syam(syamn)
  */
 public class SCHelper {
     private static long mainThreadID;
     private static long pluginStarted;
     private static SCHelper instance = new SCHelper();
     
     public static SCHelper getInstance(){
         return instance;
     }
     public static void dispose(){
         instance = null;
     }
     
     private SakuraCmd plugin;
     private ConfigurationManager config;
     private ServerData saveData;
     
     private int afkTaskID = -1;
     private int flymodeTaskID = -1;
     private boolean isEnableEcon = false;
     
     private boolean enabledMCB = false;
     private static boolean enabledMCBlistener = false;
     
     /**
      * プラスグインの初期化時と有効化時に呼ばれる
      */
    private void init(final boolean startup){
         // loadconfig
         try {
             config.loadConfig(true);
         } catch (Exception ex) {
             LogUtil.warning("an error occured while trying to load the config file.");
             ex.printStackTrace();
         }
         
         Plugin test = plugin.getServer().getPluginManager().getPlugin("MCBans");
         if (test != null && test.isEnabled()){
             if (!enabledMCBlistener){
                 plugin.getServer().getPluginManager().registerEvents(new MCBansListener(plugin), plugin);
                 LogUtil.info("MCBans integration is enabled!");
                 enabledMCBlistener = true;
             }
             enabledMCB = true;
         }else{
             enabledMCB = false;
         }
         
         // connect database
         Database db = Database.getInstance(plugin);
         db.createStructure();
         
         // worker
         AFKWorker.getInstance(); // AFK worker
         afkTaskID = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                 this.plugin, AFKWorker.getInstance().getAfkChecker(), 0, config.getAfkCheckIntervalInSec() * 20).getTaskId();
         FlymodeWorker.getInstance(); // Flymode worker
         flymodeTaskID = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                 this.plugin, FlymodeWorker.getInstance().getTask(), 0, 20).getTaskId();
         InvisibleWorker.createInstance(); // Invisible worker
        
         PermissionManager.setupPermissions(plugin); // init permission
         
         // dynmap
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
             @Override
             public void run(){
                 DynmapHandler.createInstance();
             }
         }, 20L);
         
         // coloring tab list
         for (final Player p  : plugin.getServer().getOnlinePlayers()){
             SakuraCmdUtil.changeTabColor(p);
         }
         
         // queue, create instance
         ConfirmQueue.getInstance();
         
         // Mapping already online players
         PlayerManager.clearAll();
         for (final Player player : Bukkit.getOnlinePlayers()){
             PlayerManager.addPlayer(player);
         }
         
        // Setup language
        LogUtil.info("Loading language file: " + config.getLanguage());
        if (startup){
            I18n.init(config.getLanguage());
        }else{
            try {
                I18n.setCurrentLanguage(config.getLanguage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
         // last, restore save data
         saveData.loadRestore();
     }
     
     public void setMainPlugin(final SakuraCmd plugin){
         mainThreadID = Thread.currentThread().getId();
         this.plugin = plugin;
         this.config = new ConfigurationManager(plugin);
         this.saveData = new ServerData(plugin);
         
        init(true);
     }
     
     public void disableAll(){
         // first, save all data
         saveData.save();
         
         if (afkTaskID != -1){
             plugin.getServer().getScheduler().cancelTask(afkTaskID);
             afkTaskID = -1;
         }
         if (flymodeTaskID != -1){
             plugin.getServer().getScheduler().cancelTask(flymodeTaskID);
             flymodeTaskID = -1;
         }
         
         AFKWorker.dispose();
         InvisibleWorker.dispose();
         FlymodeWorker.dispose();
         
         if (DynmapHandler.getInstance() != null){
             DynmapHandler.getInstance().deactivate();
         }
         DynmapHandler.dispose();
         ConfirmQueue.dispose();
         GeoIP.dispose();
         Database.dispose(); // conn close
     }
     
     /**
      * プラグインをリロードする
      */
     public synchronized void reload(){
         disableAll();
         System.gc();
        init(false);
         
         try {
             I18n.setCurrentLanguage(config.getLanguage());
         } catch (Exception ex) {
             LogUtil.warning("An error occured while trying to load the language file!");
             ex.printStackTrace();
         }
     }
     
     // Economy getter/setter
     public void setEnableEcon(final boolean enable){
         this.isEnableEcon = enable;
     }
     public boolean isEnableEcon(){
         return this.isEnableEcon;
     }
     
     /**
      * 設定マネージャを返す
      *
      * @return ConfigurationManager
      */
     public ConfigurationManager getConfig() {
         return config;
     }
 }
