 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.monstuhs.betterleveling;
 
 import me.monstuhs.betterleveling.Commands.ShowStatsCommand;
 import me.monstuhs.betterleveling.EventHandlers.CombatListeners;
 import me.monstuhs.betterleveling.EventHandlers.MiningListeners;
 import me.monstuhs.betterleveling.Managers.CombatManager;
 import me.monstuhs.betterleveling.Managers.ConfigurationManager;
 import me.monstuhs.betterleveling.Managers.MiningManager;
 import me.monstuhs.betterleveling.Managers.PlayerLevelManager;
 import me.monstuhs.betterleveling.Runnables.RegenerationTask;
 import me.monstuhs.betterleveling.Utilities.BukkitHelpers;
 import me.monstuhs.betterleveling.Utilities.ConfigConstants;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author James
  */
 public class BetterLeveling extends JavaPlugin {
     public static ConfigurationManager ConfigManager;
     public static PlayerLevelManager PlayerLvlManager;
     public static MiningManager MiningManager;
     public static CombatManager CombatManager;    
     private static PluginManager _pluginManager = Bukkit.getPluginManager();
     private static World _thisWorld;
     
     @Override
     public void onDisable(){
         
     }
     
     @Override
     public void onEnable() {        
         ConfigManager = new ConfigurationManager(this);
         PlayerLvlManager = new PlayerLevelManager(ConfigManager);
         MiningManager = new MiningManager();
         CombatManager = new CombatManager();
         
         String worldName = ConfigManager.getConfigFile().getString(ConfigConstants.GlobalSettings.WORLD_NAME);
         _thisWorld = worldName.isEmpty() ? Bukkit.getServer().getWorlds().get(0) : Bukkit.getServer().getWorld(worldName);
         
         
         _pluginManager.registerEvents(new MiningListeners(), this);
         _pluginManager.registerEvents(new CombatListeners(), this);
         
         registerCommands();
         startRegenTicker();
     }
     
     public void saveConfigurationFile(){
         saveConfig();
     }
     
     private void registerCommands(){
         this.getCommand(ConfigConstants.Commands.COMMANDS_SHOW_STATS).setExecutor(new ShowStatsCommand());
     }
     
     private void startRegenTicker(){
         long initialDelay = BukkitHelpers.getDelay(10);
         long repeatDelay = BukkitHelpers.getDelay(ConfigManager.getConfigFile().getLong(ConfigConstants.PassiveActivities.ACTIVITY_PASSIVE_REGEN_DELAY));        
         double regenRate = PlayerLvlManager.getRegenHalfHeartsPerLevel();
         this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RegenerationTask(_thisWorld, regenRate), initialDelay, repeatDelay);
     }
 }
