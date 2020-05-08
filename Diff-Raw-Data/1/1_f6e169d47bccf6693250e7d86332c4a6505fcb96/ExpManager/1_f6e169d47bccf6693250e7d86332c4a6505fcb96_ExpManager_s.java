 package net.siriuser.expmanager;
 
 import net.siriuser.expmanager.listeners.BlockListener;
 import net.siriuser.expmanager.listeners.EntityListener;
import net.siriuser.expmanager.listeners.PlayerListener;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.Metrics;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.IOException;
 
 public class ExpManager extends JavaPlugin {
     private static ExpManager instance;
 
     private Helper worker;
 
     @Override
     public void onEnable() {
         LogUtil.init(this);
 
         worker = Helper.getInstance();
         worker.setMainPlugin(this);
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new BlockListener(this), this);
         pm.registerEvents(new EntityListener(this), this);
 
         PluginDescriptionFile pdfFile = this.getDescription();
         LogUtil.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 
         setupMetrics();
     }
 
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
 
         worker.disableAll();
         Helper.dispose();
 
         PluginDescriptionFile pdfFile = this.getDescription();
         LogUtil.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
     }
 
     private void setupMetrics() {
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException ex) {
             LogUtil.warning("cant send metrics data!");
             ex.printStackTrace();
         }
     }
 
     /**
      * @return ExpManager Instance
      */
     public static ExpManager getInstance() {
         return instance;
     }
 }
