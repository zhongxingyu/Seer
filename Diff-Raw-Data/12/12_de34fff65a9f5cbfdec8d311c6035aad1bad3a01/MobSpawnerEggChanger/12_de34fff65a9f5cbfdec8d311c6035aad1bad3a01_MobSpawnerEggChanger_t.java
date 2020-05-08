 package me.p000ison.MobSpawnerEggChanger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 /**
  * Simply Changes the Spawnertype
  *
  * @author p000ison
  */
 public class MobSpawnerEggChanger extends JavaPlugin {
 
     private String SpawnerMesssage;
     private String NotEnoughSpawnerEggs;
     private String FailedSpawnerEggs;
     private boolean perMobPermissions;
 
     @Override
     public void onDisable()
     {
     }
 
     @Override
     public void onEnable()
     {
         PluginManager pm = this.getServer().getPluginManager();
         pm.registerEvents(new SECPlayerListener(this), this);
 
         load();
 
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             getLogger().log(Level.WARNING, "Could not send Plugin-Stats: " + e.getMessage());
         }
     }
 
     public void load()
     {
         FileConfiguration config = getConfig();
         config.options().copyDefaults(true);
         saveConfig();
 
         SpawnerMesssage = config.getString("Messages.ChangeSpawner");
         NotEnoughSpawnerEggs = config.getString("Messages.NotEnoughSpawnerEggs");
         FailedSpawnerEggs = config.getString("Messages.FailedSpawnerEggs");
         perMobPermissions = config.getBoolean("Global.perMobPermissions");
 
         saveConfig();
     }
 
     /**
      * @param typename
      * @return the SpawnerEggs
      */
     public int getSpawnerEggs(String typename)
     {
        return getConfig().getInt("Global.AmountOfSpawnerEggsYouNeed." + typename.toLowerCase(), getConfig().getInt("Global.DefaultAmountOfSpawnerEggsYouNeed"));
     }
 
     /**
      * @return the SpawnerMesssage
      */
     public String getSpawnerMesssage()
     {
         return SpawnerMesssage;
     }
 
     /**
      * @return the NotEnoughSpawnerEggs
      */
     public String getNotEnoughSpawnerEggs()
     {
         return NotEnoughSpawnerEggs;
     }
 
     /**
      * @return the FailedSpawnerEggs
      */
     public String getFailedSpawnerEggs()
     {
         return FailedSpawnerEggs;
     }
 
     /**
      * @return the perMobPermissions
      */
     public boolean isPerMobPermissions()
     {
         return perMobPermissions;
     }
 }
 
