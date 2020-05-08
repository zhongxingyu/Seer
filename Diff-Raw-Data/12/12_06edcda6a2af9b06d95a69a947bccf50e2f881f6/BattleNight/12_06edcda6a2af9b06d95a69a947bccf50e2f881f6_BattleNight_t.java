 package me.limebyte.battlenight.core;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.BattleNightPlugin;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.battle.SimpleArena;
 import me.limebyte.battlenight.core.battle.SimpleWaypoint;
 import me.limebyte.battlenight.core.commands.CommandManager;
 import me.limebyte.battlenight.core.hooks.Metrics;
 import me.limebyte.battlenight.core.hooks.Nameplates;
 import me.limebyte.battlenight.core.listeners.BlockListener;
 import me.limebyte.battlenight.core.listeners.CheatListener;
 import me.limebyte.battlenight.core.listeners.DeathListener;
 import me.limebyte.battlenight.core.listeners.DisconnectListener;
 import me.limebyte.battlenight.core.listeners.HealthListener;
 import me.limebyte.battlenight.core.listeners.InteractListener;
 import me.limebyte.battlenight.core.listeners.NameplateListener;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.listeners.SpectatorListener;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.tosort.SafeTeleporter;
 import me.limebyte.battlenight.core.tosort.UpdateChecker;
 
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BattleNight extends JavaPlugin implements BattleNightPlugin {
 
     public static BattleNight instance;
     private BattleNightAPI api;
 
     @Override
     public BattleNightAPI getAPI() {
         return api;
     }
 
     @Override
     public void onDisable() {
         SignListener.cleanSigns();
         getAPI().getBattleManager().getActiveBattle().stop();
         api.getArenaManager().saveArenas();
 
         PluginDescriptionFile pdfFile = getDescription();
         api.getMessenger().log(Level.INFO, "Version " + pdfFile.getVersion() + " has been disabled.");
     }
 
     /** Events **/
 
     @Override
     public void onEnable() {
         instance = this;
 
         ConfigurationSerialization.registerClass(SimpleArena.class);
         ConfigurationSerialization.registerClass(SimpleWaypoint.class);
         ConfigManager.initConfigurations();
 
         api = new API(this);
 
         Messenger messenger = api.getMessenger();
         PluginManager pm = getServer().getPluginManager();
         PluginDescriptionFile pdf = getDescription();
 
         // Debugging
         if (ConfigManager.get(Config.MAIN).getBoolean("UsePermissions", false)) {
             messenger.debug(Level.INFO, "Permissions Enabled.");
         } else {
             messenger.debug(Level.INFO, "Permissions Disabled, using Op.");
         }
 
         // Commands
         getCommand("battlenight").setExecutor(new CommandManager(api));
 
         // Hooks
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
         }
 
         Nameplates.init(this, pm);
 
         // Event Registration
         pm.registerEvents(new BlockListener(api), this);
         pm.registerEvents(new CheatListener(api), this);
         pm.registerEvents(new HealthListener(api), this);
         pm.registerEvents(new DeathListener(api), this);
         pm.registerEvents(new DisconnectListener(api), this);
         pm.registerEvents(new NameplateListener(api), this);
         pm.registerEvents(new InteractListener(api), this);
         pm.registerEvents(new SafeTeleporter(), this);
         pm.registerEvents(new SignListener(api), this);
         pm.registerEvents(new SpectatorListener(api), this);
 
         if (ConfigManager.get(Config.MAIN).getBoolean("UpdateCheck", true)) {
             new UpdateChecker(api, pdf).check();
         }
 
         // Enable Message
         messenger.log(Level.INFO, "Version " + pdf.getVersion() + " enabled successfully.");
         messenger.log(Level.INFO, "Made by LimeByte.");
     }
 }
