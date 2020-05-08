 package net.tgxn.bukkit.backup;
 
 import java.io.File;
 import net.tgxn.bukkit.backup.config.Settings;
 import net.tgxn.bukkit.backup.config.Strings;
 import net.tgxn.bukkit.backup.events.CommandHandler;
 import net.tgxn.bukkit.backup.events.EventListener;
 import net.tgxn.bukkit.backup.threading.PrepareBackup;
 import net.tgxn.bukkit.backup.utils.CheckInUtil;
 import net.tgxn.bukkit.backup.utils.LogUtils;
 import net.tgxn.bukkit.backup.utils.SharedUtils;
 import net.tgxn.bukkit.backup.utils.SyncSaveAllUtil;
 import org.bukkit.Server;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BackupMain extends JavaPlugin {
 
     // Setup public variables used in this plugin.
     public int mainBackupTaskID = -2;
     public int saveAllTaskID = -2;
     public File mainDataFolder;
     // Private variables for this class.
     private static Strings strings;
     private static Settings settings;
     private PrepareBackup prepareBackup;
     private SyncSaveAllUtil syncSaveAllUtil;
 
     @Override
     public void onLoad() {
 
         // Initalize main data folder variable.
         mainDataFolder = this.getDataFolder();
 
         // Initalize logging utilities.
         LogUtils.initLogUtils(this);
 
         // check and create main datafile.
         SharedUtils.checkFolderAndCreate(mainDataFolder);
 
         // Load configuration files.
         strings = new Strings(new File(mainDataFolder, "strings.yml"));
         settings = new Settings(this, new File(mainDataFolder, "config.yml"), strings);
 
         // Run version checking on strings file.
         strings.checkStringsVersion(settings.getStringProperty("requiredstrings"));
 
         // Complee initalization of LogUtils.
         LogUtils.finishInitLogUtils(settings.getBooleanProperty("displaylog"), settings.getBooleanProperty("logtofile"), settings.getStringProperty("backuplogname"));
 
         // Check backup path and the temp folder, then notify users of this.
         if (SharedUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath")))) {
             LogUtils.sendLog(strings.getString("createbudir"));
         }
 
     }
 
     @Override
     public void onEnable() {
 
         // Get server and plugin manager instances.
         Server pluginServer = getServer();
         PluginManager pluginManager = pluginServer.getPluginManager();
 
         // Create new "PrepareBackup" instance.
         prepareBackup = new PrepareBackup(pluginServer, settings, strings);
 
         // Initalize Command Listener.
         getCommand("backup").setExecutor(new CommandHandler(prepareBackup, this, settings, strings));
 
         // Initalize Event Listener.
         EventListener eventListener = new EventListener(prepareBackup, this, settings, strings);
         pluginManager.registerEvents(eventListener, this);
 
         // Configure main backup task schedule.
         int backupInterval = settings.getIntervalInMinutes("backupinterval");
         if (backupInterval != 0) {
 
             // Convert to server ticks.
             backupInterval *= 1200;
 
             // Should the schedule repeat?
             if (settings.getBooleanProperty("norepeat")) {
                 mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncDelayedTask(this, prepareBackup, backupInterval);
             } else {
                 mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupInterval, backupInterval);
             }
 
         } else {
             LogUtils.sendLog(strings.getString("disbaledauto"));
         }
 
         // Configure save-all schedule.
         int saveAllInterval = settings.getIntervalInMinutes("saveallinterval");
         if (saveAllInterval != 0) {
 
             // Convert to server ticks.
             saveAllInterval *= 1200;
 
             // Syncronised save-all.
             syncSaveAllUtil = new SyncSaveAllUtil(pluginServer, 0);
             saveAllTaskID = pluginServer.getScheduler().scheduleSyncRepeatingTask(this, syncSaveAllUtil, saveAllInterval, saveAllInterval);
         }
 
         // Startup configuration output.
         if (settings.getBooleanProperty("showconfigonstartup")) {
             String buInterval = settings.getStringProperty("backupinterval");
             String saInterval = settings.getStringProperty("saveallinterval");
             String max = Integer.toString(settings.getIntProperty("maxbackups"));
             String empty = (settings.getBooleanProperty("backupemptyserver")) ? "Yes" : "No";
             String everything = (settings.getBooleanProperty("backupeverything")) ? "Yes" : "No";
             String split = (settings.getBooleanProperty("splitbackup")) ? "Yes" : "No";
             String zip = (settings.getBooleanProperty("zipbackup")) ? "Yes" : "No";
             String path = settings.getStringProperty("backuppath");
             LogUtils.sendLog("Configuration:");
             LogUtils.sendLog("Interval: " + buInterval + ", Max: " + max + ", On Empty: " + empty + ", Everything: " + everything + ".", false);
             LogUtils.sendLog("Save-All Int: " + saInterval + ", Split: " + split + ", ZIP: " + zip + ", Path: " + path + ".", false);
         }
 
         // Version checking task.
         if (settings.getBooleanProperty("enableversioncheck")) {
             pluginServer.getScheduler().scheduleAsyncDelayedTask(this, new CheckInUtil(this.getDescription().getVersion(), strings));
         }
 
         // Notify loading complete.
         LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
     }
 
     @Override
     public void onDisable() {
 
         // Stop and scheduled tasks.
         this.getServer().getScheduler().cancelTasks(this);
 
         // Shutdown complete.
         LogUtils.sendLog(this.getDescription().getFullName() + " has completely un-loaded!", false);
     }
 }
