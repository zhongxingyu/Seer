 package com.herocraftonline.dev.heroes;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClassManager;
 import com.herocraftonline.dev.heroes.command.CommandHandler;
 import com.herocraftonline.dev.heroes.command.commands.AdminClassCommand;
 import com.herocraftonline.dev.heroes.command.commands.AdminExpCommand;
 import com.herocraftonline.dev.heroes.command.commands.AdminHealthCommand;
 import com.herocraftonline.dev.heroes.command.commands.AdminLevelCommand;
 import com.herocraftonline.dev.heroes.command.commands.ArmorCommand;
 import com.herocraftonline.dev.heroes.command.commands.BindSkillCommand;
 import com.herocraftonline.dev.heroes.command.commands.ChooseCommand;
 import com.herocraftonline.dev.heroes.command.commands.ConfigReloadCommand;
 import com.herocraftonline.dev.heroes.command.commands.CooldownCommand;
 import com.herocraftonline.dev.heroes.command.commands.DebugDumpCommand;
 import com.herocraftonline.dev.heroes.command.commands.HealthCommand;
 import com.herocraftonline.dev.heroes.command.commands.HelpCommand;
 import com.herocraftonline.dev.heroes.command.commands.HeroSaveCommand;
 import com.herocraftonline.dev.heroes.command.commands.LeaderboardCommand;
 import com.herocraftonline.dev.heroes.command.commands.LevelInformationCommand;
 import com.herocraftonline.dev.heroes.command.commands.ManaCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyAcceptCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyChatCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyInviteCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyLeaveCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyModeCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyUICommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyWhoCommand;
 import com.herocraftonline.dev.heroes.command.commands.PathsCommand;
 import com.herocraftonline.dev.heroes.command.commands.ProfessionCommand;
 import com.herocraftonline.dev.heroes.command.commands.ResetCommand;
 import com.herocraftonline.dev.heroes.command.commands.SkillListCommand;
 import com.herocraftonline.dev.heroes.command.commands.SpecsCommand;
 import com.herocraftonline.dev.heroes.command.commands.SuppressCommand;
 import com.herocraftonline.dev.heroes.command.commands.ToolsCommand;
 import com.herocraftonline.dev.heroes.command.commands.VerboseCommand;
 import com.herocraftonline.dev.heroes.command.commands.WhoCommand;
 import com.herocraftonline.dev.heroes.damage.DamageManager;
 import com.herocraftonline.dev.heroes.effects.EffectManager;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.hero.HeroManager;
 import com.herocraftonline.dev.heroes.party.PartyManager;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillManager;
 import com.herocraftonline.dev.heroes.spout.SpoutInventoryListener;
 import com.herocraftonline.dev.heroes.ui.ColorMap;
 import com.herocraftonline.dev.heroes.ui.MapAPI;
 import com.herocraftonline.dev.heroes.ui.MapInfo;
 import com.herocraftonline.dev.heroes.util.ConfigManager;
 import com.herocraftonline.dev.heroes.util.DebugLog;
 import com.herocraftonline.economy.economies.BOSE;
 import com.herocraftonline.economy.economies.EssE;
 import com.herocraftonline.economy.economies.iCo4;
 import com.herocraftonline.economy.economies.iCo5;
 import com.herocraftonline.economy.economies.iCo6;
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
     public static final DebugTimer debug = new DebugTimer();
     
     // Simple hook to Minecraft's logger so we can output to the console.
     private static final Logger log = Logger.getLogger("Minecraft");
     private static DebugLog debugLog;
 
     // Setup the Player and Plugin listener for Heroes.
     private final HPlayerListener playerListener = new HPlayerListener(this);
     private final HPluginListener pluginListener = new HPluginListener(this);
     private final HEntityListener entityListener = new HEntityListener(this);
     private final HBlockListener blockListener = new HBlockListener(this);
     private final HPartyListener partyListener = new HPartyListener(this);
     private final HEventListener hEventListener = new HEventListener(this);
     private SpoutInventoryListener siListener = null;
 
     // Various data managers
     private ConfigManager configManager;
     private CommandHandler commandHandler = new CommandHandler(this);
     private HeroClassManager heroClassManager;
     private EffectManager effectManager;
     private HeroManager heroManager;
     private PartyManager partyManager;
     private DamageManager damageManager;
     private SkillManager skillManager;
     // Variable for the Permissions plugin handler.
     public static PermissionHandler Permissions;
     public static com.herocraftonline.economy.Economy econ;
 
     // Variable for Spout.
     public static boolean useSpout = false;
 
     /**
      * Print messages to the Debug Log, if the servers in Debug Mode then we also wan't to print the messages to the
      * standard Server Console.
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
 
     public HeroClassManager getClassManager() {
         return heroClassManager;
     }
 
     public CommandHandler getCommandHandler() {
         return commandHandler;
     }
 
     public ConfigManager getConfigManager() {
         return configManager;
     }
 
     public DamageManager getDamageManager() {
         return damageManager;
     }
 
     public HeroManager getHeroManager() {
         return heroManager;
     }
 
     public PartyManager getPartyManager() {
         return partyManager;
     }
 
     public SkillManager getSkillManager() {
         return skillManager;
     }
 
     public EffectManager getEffectManager() {
         return effectManager;
     }
 
     /**
      * Handle Heroes Commands, in this case we send them straight to the commandManager.
      */
     @Override
     public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
         return commandHandler.dispatch(sender, label, args);
     }
 
     /**
      * What to do during the Disabling of Heroes -- Likely save data and close connections.
      */
     @Override
     public void onDisable() {
         heroManager.stopTimers();
         final Player[] players = getServer().getOnlinePlayers();
         for (Player player : players) {
             heroManager.saveHero(player);
             Hero hero = heroManager.getHero(player);
             hero.clearSummons();
             hero.clearPermissions();
         }
         Heroes.econ = null; // When it Enables again it performs the checks anyways.
         Heroes.Permissions = null; // When it Enables again it performs the checks anyways.
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
         debugLog.close();
     }
 
     @Override
     public void onEnable() {
         debug.reset();
         configManager = new ConfigManager(this);
 
         // Attempt to load the Configuration file.
         try {
             configManager.load();
         } catch (Exception e) {
             e.printStackTrace();
             log(Level.SEVERE, "Critical error encountered while loading. Disabling...");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
         effectManager = new EffectManager(this);
         partyManager = new PartyManager(this);
         heroManager = new HeroManager(this);
         damageManager = new DamageManager(this);
         skillManager = new SkillManager(this);
         // Check for BukkitContrib
         setupSpout();
 
         // Load in the rest of the values into their managers
         configManager.loadManagers();
 
         blockListener.init();
 
         // Call our function to register the events Heroes needs.
         registerEvents();
         // Call our function to setup Heroes Commands.
         registerCommands();
         // Perform the Permissions check.
         setupPermissions();
         // Perform Economy Tests
         setupEconomy();
         log(Level.INFO, "version " + getDescription().getVersion() + " is enabled!");
 
         final Player[] players = getServer().getOnlinePlayers();
         for (Player player : players) {
             if (heroManager.containsPlayer(player)) {
                 continue;
             }
 
             // Make sure all the hero's are loaded and that we check inventories
             heroManager.getHero(player).checkInventory();
         }
 
         // Set the Party UI map to a nice splash screen.
         if (getConfigManager().getProperties().mapUI) {
             MapAPI mapAPI = new MapAPI();
             short mapId = getConfigManager().getProperties().mapID;
 
             BufferedImage image = null;
             try {
                 image = ImageIO.read(new File(this.getDataFolder(), "heroes.png"));
             } catch (IOException e) {
                 e.printStackTrace();
             }
             byte[] pixels = ColorMap.imageToBytes(image);
 
             World world = this.getServer().getWorlds().get(0);
             MapInfo info = mapAPI.loadMap(world, mapId);
             info.setDimension((byte) 9);
             info.setData(pixels);
             mapAPI.saveMap(world, mapId, info);
         }
     }
 
     @Override
     public void onLoad() {
         dataFolder.mkdirs(); // Create the Heroes Plugin Directory.
         debugLog = new DebugLog("Heroes", dataFolder + File.separator + "debug.log");
     }
 
     public void setClassManager(HeroClassManager heroClassManager) {
         this.heroClassManager = heroClassManager;
     }
 
     public void setupEconomy() {
         PluginManager pm = this.getServer().getPluginManager();
         Plugin test = pm.getPlugin("iConomy");
         if (test != null) {
             if (test.getClass().getName().equals("com.iCo6.iConomy")) {
                 Heroes.econ = new iCo6();
             } else if (test.getClass().getName().equals("com.iConomy.iConomy")) {
                 Heroes.econ = new iCo5();
             } else if (test.getClass().getName().equals("com.nijiko.coelho.iConomy.iConomy")) {
                 Heroes.econ = new iCo4();
             }
         } else {
             test = pm.getPlugin("Essentials");
             if (test != null) {
                 Heroes.econ = new EssE();
             } else {
                 test = pm.getPlugin("BOSEconomy");
                 if (test != null) {
                     Heroes.econ = new BOSE();
                 }
             }
         }
 
         if (econ != null) {
             econ.setPlugin(test);
         }
     }
 
     /**
      * Perform a Permissions check and setup Permissions if found.
      */
     public void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
         if (Heroes.Permissions == null) {
             if (test != null) {
                 // Ignore fake permissions or Bukkit Permissions
                if (!test.getDescription().getVersion().startsWith("3") || this.getServer().getPluginManager().getPlugin("GroupManager") != null || this.getServer().getPluginManager().getPlugin("PermissionsBukkit") != null || this.getServer().getPluginManager().getPlugin("bPermissions") != null || this.getServer().getPluginManager().getPlugin("PermissionsEx") != null)
                     return;
 
                 Heroes.Permissions = ((Permissions) test).getHandler();
                 log(Level.INFO, "Permissions found.");
             }
         }
         final Player[] players = getServer().getOnlinePlayers();
         for (Player player : players) {
             Hero hero = heroManager.getHero(player);
             HeroClass heroClass = hero.getHeroClass();
 
             for (Skill skill : skillManager.getSkills()) {
                 if (skill instanceof OutsourcedSkill) {
                     ((OutsourcedSkill) skill).tryLearningSkill(hero);
                 }
             }
 
             if (Heroes.Permissions != null && heroClass != heroClassManager.getDefaultClass()) {
                 if (!Heroes.Permissions.has(player, "heroes.classes." + heroClass.getName().toLowerCase())) {
                     hero.setHeroClass(heroClassManager.getDefaultClass(), false);
                 }
             }
         }
         if (this.getServer().getPluginManager().getPermission("heroes.admin.*") == null) {
             Map<String, Boolean> adminPermissions = new HashMap<String, Boolean>();
             adminPermissions.put("heroes.admin.class", true);
             adminPermissions.put("heroes.admin.level", true);
             adminPermissions.put("heroes.admin.health", true);
             adminPermissions.put("heroes.admin.exp", true);
             adminPermissions.put("heroes.admin.reload", true);
             adminPermissions.put("heroes.admin.saveall", true);
             Permission wildcardAdminPermission = new Permission("heroes.admin.*", "Grants access to all admin commands.", adminPermissions);
             this.getServer().getPluginManager().addPermission(wildcardAdminPermission);
         }
     }
 
     /**
      * Check to see if Spout is enabled on the server, if so inform Heroes to use it for Craftin XP.
      */
     public void setupSpout() {
         Heroes.useSpout = this.getServer().getPluginManager().getPlugin("Spout") != null;
         // If it was found, then lets register our custom event for spout
         if (useSpout) {
             siListener = new SpoutInventoryListener(this);
             getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, siListener, Priority.Monitor, this);
         }
     }
 
     /**
      * Register Heroes commands to DThielke's Command Manager.
      */
     private void registerCommands() {
         // Page 1
         commandHandler.addCommand(new PathsCommand(this));
         commandHandler.addCommand(new SpecsCommand(this));
         commandHandler.addCommand(new ChooseCommand(this));
         commandHandler.addCommand(new ProfessionCommand(this));
         commandHandler.addCommand(new LevelInformationCommand(this));
         commandHandler.addCommand(new SkillListCommand(this));
         commandHandler.addCommand(new BindSkillCommand(this));
         commandHandler.addCommand(new ArmorCommand(this));
         
 
         // Page 2
         commandHandler.addCommand(new ToolsCommand(this));
         commandHandler.addCommand(new ManaCommand(this));
         commandHandler.addCommand(new CooldownCommand(this));
         commandHandler.addCommand(new VerboseCommand(this));
         commandHandler.addCommand(new SuppressCommand(this));
         commandHandler.addCommand(new WhoCommand(this));
         commandHandler.addCommand(new PartyAcceptCommand(this));
         commandHandler.addCommand(new PartyInviteCommand(this));
         
 
         // Page 3
         commandHandler.addCommand(new PartyWhoCommand(this));
         commandHandler.addCommand(new PartyLeaveCommand(this));
         commandHandler.addCommand(new PartyModeCommand(this));
         commandHandler.addCommand(new PartyUICommand(this));
         commandHandler.addCommand(new PartyChatCommand(this));
         commandHandler.addCommand(new ConfigReloadCommand(this));
         commandHandler.addCommand(new HelpCommand(this));
         commandHandler.addCommand(new AdminExpCommand(this));
         
 
         // Page 4
         commandHandler.addCommand(new AdminLevelCommand(this));
         commandHandler.addCommand(new AdminClassCommand(this));
         commandHandler.addCommand(new AdminHealthCommand(this));
         commandHandler.addCommand(new HealthCommand(this));
         commandHandler.addCommand(new LeaderboardCommand(this));
         commandHandler.addCommand(new HeroSaveCommand(this));
         commandHandler.addCommand(new ResetCommand(this));
         commandHandler.addCommand(new DebugDumpCommand());
     }
 
     /**
      * Register the Events which Heroes requires.
      */
     private void registerEvents() {
         PluginManager pluginManager = getServer().getPluginManager();
         pluginManager.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Lowest, this);
         pluginManager.registerEvent(Type.PLAYER_ITEM_HELD, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_BED_ENTER, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_BED_LEAVE, playerListener, Priority.Monitor, this);
 
         pluginManager.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Highest, this);
         pluginManager.registerEvent(Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
 
         pluginManager.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Monitor, this);
 
         pluginManager.registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
 
         pluginManager.registerEvent(Type.CUSTOM_EVENT, hEventListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.CUSTOM_EVENT, new HSkillListener(this), Priority.Highest, this);
         pluginManager.registerEvent(Type.CUSTOM_EVENT, new HPermissionsListener(this), Priority.Monitor, this);
 
         // Map Party UI
         pluginManager.registerEvent(Type.ENTITY_REGAIN_HEALTH, partyListener, Priority.Monitor, this);
 
         damageManager.registerEvents();
     }
 
     /**
      * Print messages to the server Log as well as to our DebugLog. 'debugLog' is used to seperate Heroes information
      * from the Servers Log Output.
      * 
      * @param level
      * @param msg
      */
     public static void log(Level level, String msg) {
         log.log(level, "[Heroes] " + msg);
         debugLog.log(level, "[Heroes] " + msg);
     }
 }
