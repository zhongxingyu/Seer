 package me.ase34.citylanterns;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import me.ase34.citylanterns.executor.SelectLanternExecutor;
 import me.ase34.citylanterns.listener.LanternRedstoneListener;
 import me.ase34.citylanterns.listener.LanternSelectListener;
 import me.ase34.citylanterns.runnable.LanternUpdateThread;
 import me.ase34.citylanterns.storage.LanternFileStorage;
 import me.ase34.citylanterns.storage.LanternStorage;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 public class CityLanterns extends JavaPlugin {
 
     private List<String> selectingPlayers;
     private List<Location> lanterns;
     private LanternStorage storage;
 
     @Override
     public void onDisable() {
         try {
             storage.save(lanterns);
         } catch (Exception e) {
             getLogger().log(Level.SEVERE, "An Exception occured!", e);
             return;
         }
         getLogger().info(getDescription().getFullName() + " by " + getDescription().getAuthors().get(0) + " disabled!");
     }
 
     @Override
     public void onEnable() {
         try {
             
             getDataFolder().mkdir();
            saveDefaultConfig();
             File storageFile = new File(getDataFolder(), "storage.txt");
             storageFile.createNewFile();
             storage = new LanternFileStorage(storageFile);
             lanterns = storage.load();
             selectingPlayers = new ArrayList<String>();
             getCommand("selectlanterns").setExecutor(new SelectLanternExecutor(this));
             getServer().getPluginManager().registerEvents(new LanternSelectListener(this), this);
             getServer().getPluginManager().registerEvents(new LanternRedstoneListener(this), this);
             getServer().getScheduler().scheduleSyncRepeatingTask(this, new LanternUpdateThread(this), 0, 1);
             try {
                 Metrics metrics = new Metrics(this);
                 metrics.start();
             } catch (IOException e) {
                 getServer().getLogger().log(Level.WARNING, "Submitting plugin metrics failed: ", e);
             }
         
         } catch (Exception e) {
             getLogger().log(Level.SEVERE, "An Exception occured! Aborting plugin start.", e);
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
         
         getLogger().info(getDescription().getFullName() + " by " + getDescription().getAuthors().get(0) + " enabled!");
     }
 
     public List<String> getSelectingPlayers() {
         return selectingPlayers;
     }
 
     public List<Location> getLanterns() {
         return lanterns;
     }
 
 }
