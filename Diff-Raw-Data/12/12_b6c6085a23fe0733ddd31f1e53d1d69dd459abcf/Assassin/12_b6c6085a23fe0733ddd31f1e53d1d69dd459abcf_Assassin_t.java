 package com.me.tft_02.assassin;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.me.tft_02.assassin.listeners.ChatListener;
 import com.me.tft_02.assassin.listeners.EntityListener;
 import com.me.tft_02.assassin.listeners.PlayerListener;
 import com.me.tft_02.assassin.listeners.TagListener;
 import com.me.tft_02.assassin.runnables.ActiveTimer;
 import com.me.tft_02.assassin.runnables.AssassinRangeTimer;
 import com.me.tft_02.assassin.util.Data;
 import com.me.tft_02.assassin.util.DependencyDownload;
 import com.me.tft_02.assassin.util.Metrics;
 import com.me.tft_02.assassin.util.UpdateChecker;
 
 public class Assassin extends JavaPlugin {
     public static Assassin instance;
 
     private TagListener tagListener = new TagListener(this);
     private EntityListener entityListener = new EntityListener(this);
     private PlayerListener playerListener = new PlayerListener(this);
     private ChatListener chatListener = new ChatListener(this);
 
     private AssassinMode assassin = new AssassinMode(this);
     private UpdateChecker update = new UpdateChecker(this);
 
     public boolean vaultEnabled;
     public boolean debug_mode = false;
     public boolean needsUpdate;
 
     public static Economy econ = null;
 
     public static Assassin getInstance() {
         return instance;
     }
 
     /**
      * Run things on enable.
      */
     @Override
     public void onEnable() {
         instance = this;
         PluginManager pm = getServer().getPluginManager();
         if (pm.getPlugin("TagAPI") == null) {
             this.getLogger().log(Level.WARNING, "No TagAPI dependency found!");
             this.getLogger().log(Level.WARNING, "Downloading TagAPI now, hold on!");
             DependencyDownload.download();
             this.getLogger().log(Level.WARNING, "TagAPI downloaded! Restart server to enable [Assassin].");
             pm.disablePlugin(this);
             return;
         } else if (!pm.isPluginEnabled("TagAPI")) {
             this.getLogger().log(Level.WARNING, "TagAPI is probably outdated, check the console log.");
             pm.disablePlugin(this);
             return;
         }
         if (getConfig().getBoolean("General.debug_mode_enabled")) {
             this.getLogger().log(Level.WARNING, "Debug mode is enabled, this is only for advanced users!");
             debug_mode = true;
         }
         if (!setupEconomy()) {
             vaultEnabled = false;
         } else {
             vaultEnabled = true;
         }
 
         setupConfiguration();
         checkConfiguration();
 
         addCustomRecipes();
 
         pm.registerEvents(tagListener, this);
         pm.registerEvents(entityListener, this);
         pm.registerEvents(playerListener, this);
         pm.registerEvents(chatListener, this);
 //		pm.registerEvents(blockListener, this);
 
         getCommand("assassin").setExecutor(new Commands(this));
 
         Data.loadData();
 
         if (getConfig().getBoolean("General.stats_tracking_enabled")) {
             try {
                 Metrics metrics = new Metrics(this);
                 metrics.start();
             } catch (IOException e) {
                 System.out.println("Failed to submit stats.");
             }
         }
 
         BukkitScheduler scheduler = getServer().getScheduler();
         if (getConfig().getBoolean("Assassin.warn_others_when_near")) {
             scheduler.scheduleSyncRepeatingTask(this, new AssassinRangeTimer(this), 0, 10 * 20);
         }
         //Active check timer (Runs every two seconds)
         scheduler.scheduleSyncRepeatingTask(this, new ActiveTimer(this), 0, 40);
 
         try {
             if (getConfig().getBoolean("General.update_check_enabled")) {
                 needsUpdate = update.getUpdate();
             } else {
                 needsUpdate = false;
             }
         } catch (Exception e) {
             needsUpdate = false;
         }
         if (needsUpdate) {
            this.getLogger().log(Level.INFO, "***********************************************************************************");
            this.getLogger().log(Level.INFO, "*                              Assassin is outdated!                              *");
            this.getLogger().log(Level.INFO, "* New version available on BukkitDev! http://dev.bukkit.org/server-mods/Assassin/ *");
            this.getLogger().log(Level.INFO, "***********************************************************************************");
         }
     }
 
     private void addCustomRecipes() {
         MaterialData blackWool = new MaterialData(Material.WOOL, (byte) 15);
         ShapedRecipe AssassinMask = new ShapedRecipe(assassin.getMask(1));
         AssassinMask.shape(new String[] { "XXX", "X X" });
         AssassinMask.setIngredient('X', blackWool);
         getServer().addRecipe(AssassinMask);
     }
 
     private void setupConfiguration() {
         FileConfiguration config = this.getConfig();
         config.addDefault("General.debug_mode_enabled", false);
         config.addDefault("General.stats_tracking_enabled", true);
         config.addDefault("General.update_check_enabled", true);
         config.addDefault("General.config_version", "1.1.1");
         config.addDefault("Assassin.active_length", 3600);
         config.addDefault("Assassin.teleport_on_deactivate", true);
         config.addDefault("Assassin.cooldown_length", 600);
         config.addDefault("Assassin.messages_distance", 250);
         config.addDefault("Assassin.warn_others_on_activation", true);
         config.addDefault("Assassin.warn_others_when_near", true);
         config.addDefault("Assassin.return_mask", false);
         config.addDefault("Assassin.activation_cost", 0);
         config.addDefault("Assassin.prevent_neutral_pvp", true);
         config.addDefault("Assassin.particle_effects", true);
         config.addDefault("Assassin.potion_effects", true);
         config.addDefault("Assassin.warn_time_almost_up", 10);
 //		config.addDefault("Assassin.max_allowed", 5);
         String[] defaultBlockedcmds = { "/spawn", "/home", "/tp", "/tphere", "/tpa", "/tpahere", "/tpall", "/tpaall" };
         config.addDefault("Assassin.blocked_commands", Arrays.asList(defaultBlockedcmds));
 
 //		config.addDefault("Assassin.hide_neutral_names", false);
 
         config.options().copyDefaults(true);
         saveConfig();
     }
 
     private void checkConfiguration() {
         if (getConfig().getDouble("Assassin.activation_cost") > 0 && !vaultEnabled) {
             this.getLogger().log(Level.WARNING, "Vault dependency needed if you want to use currency!");
         }
     }
 
     private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
     /**
      * Run things on disable.
      */
     @Override
     public void onDisable() {
         Data.saveData();
         this.getServer().getScheduler().cancelTasks(this);
     }
 }
