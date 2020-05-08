 /**
  * SakuraCmd - Package: net.syamn.sakuracmd
  * Created: 2012/12/28 13:39:51
  */
 package net.syamn.sakuracmd;
 
 import java.io.File;
 
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.file.FileStructure;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 /**
  * ConfigurationManager (ConfigurationManager.java)
  * @author syam(syamn)
  */
 public class ConfigurationManager {
     /* Current config.yml file version */
     private final int latestVersion = 1;
     
     // logPrefix
     private static final String logPrefix = SakuraCmd.logPrefix;
     private static final String msgPrefix = SakuraCmd.msgPrefix;
     
     private FileConfiguration conf;
     private File pluginDir;
     
     private final SakuraCmd plugin;
     
     /**
      * Constructor
      * @param plugin
      */
     public ConfigurationManager(final SakuraCmd plugin){
         this.plugin = plugin;
         this.pluginDir = plugin.getDataFolder();
     }
     
     /**
      * Load configuration
      * @param initialLoad
      * @throws Exception
      */
     public void loadConfig(final boolean initialLoad) throws Exception{
         FileStructure.createDir(pluginDir);
         
         File file = new File(pluginDir, "config.yml");
         if (!file.exists()){
             FileStructure.extractResource("/config.yml", pluginDir, false, false, plugin);
             LogUtil.info(logPrefix + "config.yml is not found! Created default config.yml!");
         }
         
         plugin.reloadConfig();
         conf = plugin.getConfig();
         
         checkver(conf.getInt("ConfigVersion", 1));
     }
     
     /**
      * Check configuration file version
      * @param ver
      */
     private void checkver(final int ver){
      // compare configuration file version
         if (ver < latestVersion) {
             // first, rename old configuration
             final String destName = "oldconfig-v" + ver + ".yml";
             String srcPath = new File(pluginDir, "config.yml").getPath();
             String destPath = new File(pluginDir, destName).getPath();
             try {
                 FileStructure.copyTransfer(srcPath, destPath);
                 LogUtil.info("Copied old config.yml to " + destName + "!");
             } catch (Exception ex) {
                 LogUtil.warning("Failed to copy old config.yml!");
             }
 
             // force copy config.yml and languages
             FileStructure.extractResource("/config.yml", pluginDir, true, false, plugin);
             // Language.extractLanguageFile(true);
 
             plugin.reloadConfig();
             conf = plugin.getConfig();
 
             LogUtil.info("Deleted existing configuration file and generate a new one!");
         }
     }
     
     /* ***** Begin Configuration Getters ************************** */
     // General
     public boolean getUseNamePrefix(){
         return conf.getBoolean("UseNamePrefix", true);
     }
     public boolean getUseDisplayname(){
         return conf.getBoolean("UseDisplayName", true);
     }
     public int getAfkCheckIntervalInSec(){
         return conf.getInt("AfkCheckIntervalInSec", 30);
     }
     
     // Permissions
     public String getPermissionCtrl(){
         return conf.getString("Permission", "PerimssionsEx");
     }
     
     // Debug
     public boolean isDebug(){
         return conf.getBoolean("Debug", false);
     }
 }
