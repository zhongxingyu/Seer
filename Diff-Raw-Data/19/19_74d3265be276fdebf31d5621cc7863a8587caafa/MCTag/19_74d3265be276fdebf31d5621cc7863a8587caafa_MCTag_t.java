 package info.gomeow.mctag;
 
 import info.gomeow.mctag.util.Updater;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class MCTag extends JavaPlugin {
 
     static MCTag instance;
     static Logger LOG;
     public static boolean UPDATE = false;
     public static String NEWVERSION = "";
     public static String LINK = "";
 
     Manager manager;
     File dataFile;
     YamlConfiguration data;
 
     public void onEnable() {
         instance = this;
         LOG = getLogger();
         manager = new Manager(this);
         checkUpdate();
     }
 
     public void onDisable() {
         for (Match match : manager.getMatches()) {
             match.broadcast(ChatColor.DARK_RED + "Match Interrupted by Server Stop/Reload");
            match.reset(true);
         }
     }
 
     public void loadData() {
         File f = new File(getDataFolder(), "data.yml");
         if (!f.exists()) {
             try {
                 f.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         dataFile = f;
         data = YamlConfiguration.loadConfiguration(f);
         saveDefaultConfig();
         checkUpdate();
     }
 
     public YamlConfiguration getData() {
         return data;
     }
 
     public void saveData() {
         try {
             data.save(dataFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public Manager getManager() {
         return manager;
     }
 
     /**
      * Checks for available updates.
      */
     public void checkUpdate() {
         new BukkitRunnable() {
 
             public void run() {
                 if (getConfig().getBoolean("check-update")) {
                     Updater updater = new Updater(MCTag.instance, 40098, getFile(), Updater.UpdateType.DEFAULT, true);
                 }
             }
         }.runTaskAsynchronously(this);
     }
 
 }
