 package io.snw.magicfurnace;
 
 import io.snw.magicfurnace.factions.FactionsManager;
 import io.snw.magicfurnace.listener.JoinListener;
 import io.snw.magicfurnace.listener.SmeltListener;
 import io.snw.magicfurnace.util.MetricsLite;
 import io.snw.magicfurnace.util.Updater;
 import io.snw.magicfurnace.util.Updater.UpdateResult;
 import io.snw.magicfurnace.util.Updater.UpdateType;
 import org.bukkit.Material;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 
 /**
  * @author drtshock
  */
 public class MagicFurnace extends JavaPlugin implements Listener {
 
     private boolean useFactions = false;
     private MagicFurnace plugin;
     private boolean needsUpdate = false;
     private String newVersion = "";
     private FactionsManager factions;
 
     @Override
     public void onEnable() {
         plugin = this;
         saveDefaultConfig();
         checkConfig();
         bettyCrocker();
         getServer().getPluginManager().registerEvents(new SmeltListener(this), this);
         checkFactions();
         startMetrics();
         checkUpdate();
     }
 
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelAllTasks(); // Clean up after ourselves.
     }
 
     private void checkConfig() {
         if(getConfig().get("change-air") == null) {
             getConfig().set("change-air", true);
             saveConfig();
         }
     }
 
     private void checkFactions() {
        if (getConfig().getBoolean("use-factions")) {
             Plugin factions = getServer().getPluginManager().getPlugin("Factions");
             if (factions != null) {
                 this.factions = new FactionsManager(this, factions); // Loads the hooks internally, if nothing is found that's fine.
                 if (this.factions.getFactions() == null) {
                     getLogger().info("Factions was found, but the version you have is not supported.");
                 } else {
                     getLogger().info("Factions hook for version " + this.factions.getFactions().getVersion() + " has been loaded!");
                 }
             } else {
                 getLogger().info("Factions hook enabled, but Factions wasn't found. What were you thinking ;o");
             }
         }
     }
 
     protected void bettyCrocker() {
         Material mat = Material.getMaterial(getConfig().getString("smeltme", "DIAMOND"));
         getServer().addRecipe(new FurnaceRecipe(new ItemStack(Material.RED_MUSHROOM, 1), mat));
     }
 
     // PROTECTED
     protected void checkUpdate() {
         if (getConfig().getBoolean("check-update", true)) {
             final File file = getFile();
             final Updater.UpdateType updateType = (getConfig().getBoolean("download-update", true) ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD);
             getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                 @Override
                 public void run() {
                     Updater updater = new Updater(plugin, 67135, file, updateType, false);
                     needsUpdate = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                     newVersion = updater.getLatestName();
                     if (updater.getResult() == UpdateResult.SUCCESS) {
                         getLogger().log(Level.INFO, "Successfully updated MagicFurnace to version {0} for next restart!", updater.getLatestName());
                     } else if (updater.getResult() == UpdateResult.NO_UPDATE) {
                         getLogger().log(Level.INFO, "We didn't find an update!");
                     }
                 }
             });
             if (needsUpdate) {
                 getServer().getPluginManager().registerEvents(new JoinListener(newVersion), this);
             }
         }
     }
 
     protected void startMetrics() {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             // Failed to submit the stats :-(
         }
     }
 
     /**
      * Check if the plugin should use factions.
      *
      * @return true if config is set to true and the factions manager is not null. Otherwise false.
      */
     public boolean isUsingFactions() {
         return useFactions && factions != null && factions.getFactions() != null;
     }
 
 
     /**
      * Gets the Factions Manager.
      *
      * @return the FactionsManager.
      */
     public FactionsManager getFactionsManager() {
         return factions;
     }
 
     public int getLarge() {
         return getConfig().getInt("large", 20);
     }
 
     public int getMedium() {
         return getConfig().getInt("medium", 13);
     }
 
     public int getSmall() {
         return getConfig().getInt("small", 5);
     }
 
     public int getRange() {
         return getConfig().getInt("range", 5);
     }
 
     public Material getNormalMaterial() {
         return Material.getMaterial(getConfig().getString("material.normal", "STONE"));
     }
 
     public Material getNetherMaterial() {
         return Material.getMaterial(getConfig().getString("material.nether", "NETHER_RACK"));
     }
 
     public Material getEndMaterial() {
         return Material.getMaterial(getConfig().getString("material.end", "END_STONE"));
     }
 
     public boolean isAllowedInWilderness() {
         return getConfig().getBoolean("allow-in-wilderness", false);
     }
 
     public boolean isChangeAir() {
         return getConfig().getBoolean("change-air", true);
     }
 }
