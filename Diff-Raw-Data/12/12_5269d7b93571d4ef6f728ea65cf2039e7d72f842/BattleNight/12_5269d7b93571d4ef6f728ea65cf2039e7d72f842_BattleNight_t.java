 package me.limebyte.battlenight.core;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.BattleNightPlugin;
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.core.commands.CommandManager;
 import me.limebyte.battlenight.core.hooks.Metrics;
 import me.limebyte.battlenight.core.hooks.Nameplates;
 import me.limebyte.battlenight.core.listeners.APIEventListener;
 import me.limebyte.battlenight.core.listeners.CheatListener;
 import me.limebyte.battlenight.core.listeners.DeathListener;
 import me.limebyte.battlenight.core.listeners.DisconnectListener;
 import me.limebyte.battlenight.core.listeners.HealthListener;
 import me.limebyte.battlenight.core.listeners.InteractListener;
 import me.limebyte.battlenight.core.listeners.NameplateListener;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.managers.ArenaManager;
 import me.limebyte.battlenight.core.managers.ClassManager;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BattleNight extends JavaPlugin implements BattleNightPlugin {
 
     /** Variables **/
 
     public static BattleNight instance;
     private BattleNightAPI api;
 
     /** Events **/
 
     @Override
     public void onEnable() {
         instance = this;
         api = new API();
         Battle battle = new FFABattle();
         api.setBattle(battle);
 
        Messenger.init(api);

         PluginManager pm = getServer().getPluginManager();
 
         // Register Serialization Classes
         ConfigurationSerialization.registerClass(Arena.class);
         ConfigurationSerialization.registerClass(Waypoint.class);
 
         // Initialize Configuration
         ConfigManager.initConfigurations();
 
         // Setup Managers
         ClassManager.reloadClasses();
         ArenaManager.loadArenas();
 
         // Debugging
         if (ConfigManager.get(Config.MAIN).getBoolean("UsePermissions", false)) {
             Messenger.debug(Level.INFO, "Permissions Enabled.");
         } else {
             Messenger.debug(Level.INFO, "Permissions Disabled, using Op.");
         }
         String loadedClasses = ClassManager.getClassNames().keySet().toString();
         Messenger.debug(Level.INFO, "Loaded Classes: " + loadedClasses.replaceAll("\\[|\\]", "") + ".");
 
         // Commands
         getCommand("battlenight").setExecutor(new CommandManager(api));
 
         // Hooks
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
             // Failed to submit the stats :-(
         }
         Nameplates.init(this, pm);
 
         // Event Registration
         pm.registerEvents(new CheatListener(api), this);
         pm.registerEvents(new HealthListener(api), this);
         pm.registerEvents(new DeathListener(api), this);
         pm.registerEvents(new DisconnectListener(api), this);
         pm.registerEvents(new NameplateListener(api), this);
         pm.registerEvents(new InteractListener(api), this);
         pm.registerEvents(new SafeTeleporter(), this);
         pm.registerEvents(new SignListener(api), this);
         pm.registerEvents(new APIEventListener(), this);
 
         // Enable Message
         Messenger.log(Level.INFO, "Version " + getDescription().getVersion() + " enabled successfully.");
         Messenger.log(Level.INFO, "Made by LimeByte.");
     }
 
     @Override
     public void onDisable() {
         SignListener.cleanSigns();
 
         // Stop the current Battle
         getAPI().getBattle().stop();
 
         // Save arenas
         ArenaManager.saveArenas();
 
         // Disable message
         PluginDescriptionFile pdfFile = getDescription();
         Messenger.log(Level.INFO, "Version " + pdfFile.getVersion() + " has been disabled.");
     }
 
     @Override
     public BattleNightAPI getAPI() {
         return api;
     }
 }
