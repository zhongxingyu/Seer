 package com.herocraftonline.dev.heroes;
 
 import java.io.File;
 import java.util.logging.*;
 
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.*;
 import org.bukkit.plugin.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.herocraftonline.dev.heroes.classes.ClassManager;
 import com.herocraftonline.dev.heroes.command.CommandManager;
 import com.herocraftonline.dev.heroes.command.SkillLoader;
 import com.herocraftonline.dev.heroes.command.commands.*;
 import com.herocraftonline.dev.heroes.command.skills.*;
 import com.herocraftonline.dev.heroes.party.PartyManager;
 import com.herocraftonline.dev.heroes.persistence.*;
 import com.herocraftonline.dev.heroes.util.*;
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * Heroes Plugin for Herocraft
  * 
  * @author Herocraft's Plugin Team
  */
 public class Heroes extends JavaPlugin {
 
     // Using this instead of getDataFolder(), getDataFolder() uses the File
     // Name. We wan't a constant folder name.
     public static final File dataFolder = new File("plugins" + File.separator + "Heroes");
 
     // Simple hook to Minecraft's logger so we can output to the console.
     private static final Logger log = Logger.getLogger("Minecraft");
     private static DebugLog debugLog;
 
     // Setup the Player and Plugin listener for Heroes.
     private final HPlayerListener playerListener = new HPlayerListener(this);
     private final HPluginListener pluginListener = new HPluginListener(this);
     private final HEntityListener entityListener = new HEntityListener(this);
     private final HBlockListener blockListener = new HBlockListener(this);
 
     // Various data managers
     private ConfigManager configManager;
     private CommandManager commandManager = new CommandManager();
     private ClassManager classManager;
     private HeroManager heroManager;
     private PartyManager partyManager;
 
     // Messaging
     private Messaging messaging = new Messaging();
 
     // Variable for the Permissions plugin handler.
     public static PermissionHandler Permissions;
 
     // Variable to contain whether Heroes will utilize iConomy or not.
     private boolean useiConomy = false;
     // Variable for the iConomy plugin handler.
     private static iConomy iConomy = null;
     // Variable for mana regen
     private long regenrate = 100L;
 
     @Override
     public void onLoad() {
         dataFolder.mkdirs(); // Create the Heroes Plugin Directory.
         configManager = new ConfigManager(this);
         heroManager = new HeroManager(this);
         partyManager = new PartyManager(this);
         debugLog = new DebugLog("Heroes", dataFolder + File.separator + "debug.log");
     }
 
     @Override
     public void onEnable() {
         log(Level.INFO, "version " + getDescription().getVersion() + " is enabled!");
 
         // Attempt to load the Configuration file.
         try {
             configManager.load();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // Setup the Property for Levels * Exp
         getConfigManager().getProperties().calcExp();
 
         // Call our function to register the events Heroes needs.
         registerEvents();
         // Call our function to setup Heroes Commands.
         registerCommands();
 
         // Perform the Permissions check.
         setupPermissions();
 
         for (Player player : getServer().getOnlinePlayers()) {
             heroManager.loadHeroFile(player);
         }
 
         // Start mana regen
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             public void run() {
                 for (Hero hero : getHeroManager().getHeroes()) {
                     if (hero.getMana() < 100) {
                         if (hero.getMana() > 95) {
                             hero.setMana(100);
                         } else {
                             hero.setMana(hero.getMana() + 5);
                         }
                     }
                 }
             }
         }, 100L, regenrate);
 
         // Skills Loader
         loadSkills();
     }
 
     /**
      * Perform a Permissions check and setup Permissions if found.
      */
     public void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
         if (Heroes.Permissions == null) {
             if (test != null) {
                 Heroes.Permissions = ((Permissions) test).getHandler();
             }
         }
     }
 
     /**
      * Handle Heroes Commands, in this case we send them straight to the
      * commandManager.
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         return commandManager.dispatch(sender, command, label, args);
     }
 
     /**
      * Register the Events which Heroes requires.
      */
     private void registerEvents() {
         PluginManager pluginManager = getServer().getPluginManager();
         pluginManager.registerEvent(Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
 
         pluginManager.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
 
         pluginManager.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
 
         pluginManager.registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
     }
 
     /**
      * Register Heroes commands to DThielke's Command Manager.
      */
     private void registerCommands() {
         // Page 1
         commandManager.addCommand(new UpdateCommand(this));
         commandManager.addCommand(new SelectProfessionCommand(this));
         commandManager.addCommand(new ConfigReloadCommand(this));
         commandManager.addCommand(new SelectSpecialtyCommand(this));
         commandManager.addCommand(new PartyAcceptCommand(this));
         commandManager.addCommand(new PartyCreateCommand(this));
         commandManager.addCommand(new PartyInviteCommand(this));
         commandManager.addCommand(new SkillBlackjack(this));
         commandManager.addCommand(new SkillHarmtouch(this));
         commandManager.addCommand(new SkillJump(this));
         commandManager.addCommand(new SkillLayhands(this));
         commandManager.addCommand(new SkillTame(this));
         commandManager.addCommand(new SkillTrack(this));
         commandManager.addCommand(new SkillSummon(this));
         commandManager.addCommand(new PartyChatCommand(this));
     }
 
     /**
      * Return True/False as to whether iConomy should be used.
      * 
      * @return
      */
     public boolean checkiConomy() {
         this.useiConomy = (iConomy != null); // Tbf this needs changing... even
                                              // if iConomy is detected we only
                                              // want to use it if its setup.
         return this.useiConomy;
     }
 
     /**
      * Setup the iConomy plugin.
      * 
      * @param plugin
      * @return
      */
     public void setupiConomy() {
         Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
         if (Heroes.iConomy == null) {
             if (test != null) {
                 Heroes.iConomy = (iConomy) test;
             }
         }
     }
 
     /**
      * Return iConomy.
      * 
      * @return
      */
     public static iConomy getiConomy() {
         return iConomy;
     }
 
     /**
      * What to do during the Disabling of Heroes -- Likely save data and close
      * connections.
      */
     @Override
     public void onDisable() {
         Heroes.iConomy = null; // When it Enables again it performs the checks
                                // anyways.
         Heroes.Permissions = null; // When it Enables again it performs the
                                    // checks anyways.
 
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
         debugLog.close();
     }
 
     public ClassManager getClassManager() {
         return classManager;
     }
 
     public void setClassManager(ClassManager classManager) {
         this.classManager = classManager;
     }
 
     /**
      * Print messages to the server Log as well as to our DebugLog. 'debugLog'
      * is used to seperate Heroes information from the Servers Log Output.
      * 
      * @param level
      * @param msg
      */
     public void log(Level level, String msg) {
         log.log(level, "[Heroes] " + msg);
         debugLog.log(level, "[Heroes] " + msg);
     }
 
     /**
      * Print messages to the Debug Log, if the servers in Debug Mode then we
      * also wan't to print the messages to the standard Server Console.
      * 
      * @param level
      * @param msg
      */
     public void debugLog(Level level, String msg) {
         if (this.configManager.getProperties().debug) {
             log.log(level, "[Debug] " + msg);
         }
         debugLog.log(level, "[Debug] " + msg);
     }
 
     /**
      * Load all the external classes.
      */
     public void loadSkills() {
         File dir = new File(getDataFolder(), "externals");
         dir.mkdir();
         boolean added = false;
         for (String f : dir.list()) {
             if (f.contains(".jar")) {
                 Skill skill = SkillLoader.loadSkill(new File(dir, f), this);
                commandManager.addCommand(skill);
                if (!added) {
                    log(Level.INFO, "Yo dawg, I heard you like plugins, so I put plugins in your plugin so you could plugin while you plugin.");
                    added = true;
                }
                 if(skill != null){
                     log(Level.INFO, "Skill " + skill.getName() + " has been loaded");
                 }
             }
         }
     }
 
     public HeroManager getHeroManager() {
         return heroManager;
     }
 
     public ConfigManager getConfigManager() {
         return configManager;
     }
 
     public Messaging getMessager() {
         return messaging;
     }
 
     public CommandManager getCommandManager() {
         return commandManager;
     }
 
     public PartyManager getPartyManager() {
         return partyManager;
     }
 }
