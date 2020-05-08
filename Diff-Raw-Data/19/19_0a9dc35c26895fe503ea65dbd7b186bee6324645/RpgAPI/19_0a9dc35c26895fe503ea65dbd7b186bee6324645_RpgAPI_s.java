 package com.vartala.soulofw0lf.rpgapi;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import com.vartala.soulofw0lf.rpgapi.achievementapi.TitleAchievement;
 import com.vartala.soulofw0lf.rpgapi.borderapi.BorderCheck;
 import com.vartala.soulofw0lf.rpgapi.chatapi.ChatClass;
 import com.vartala.soulofw0lf.rpgapi.chatapi.ChatListener;
 import com.vartala.soulofw0lf.rpgapi.classapi.RpgClasses;
 import com.vartala.soulofw0lf.rpgapi.commandapi.UniqueCommands;
 import com.vartala.soulofw0lf.rpgapi.enumapi.ClassName;
 import com.vartala.soulofw0lf.rpgapi.enumapi.Spell;
 import com.vartala.soulofw0lf.rpgapi.factionapi.FactionLevel;
 import com.vartala.soulofw0lf.rpgapi.foodapi.CustomFood;
 import com.vartala.soulofw0lf.rpgapi.foodapi.FoodListener;
 import com.vartala.soulofw0lf.rpgapi.guiapi.ClickInvListener;
 import com.vartala.soulofw0lf.rpgapi.guiapi.RpgClickInv;
 import com.vartala.soulofw0lf.rpgapi.guildapi.GuildObject;
 import com.vartala.soulofw0lf.rpgapi.guildapi.GuildRank;
 import com.vartala.soulofw0lf.rpgapi.listenersapi.PoisonListener;
 import com.vartala.soulofw0lf.rpgapi.minionapi.MinionEntity;
 import com.vartala.soulofw0lf.rpgapi.mobcommandapi.MobCommand;
 import com.vartala.soulofw0lf.rpgapi.mobcommandapi.SetBuilder;
 import com.vartala.soulofw0lf.rpgapi.partyapi.LFGPlayer;
 import com.vartala.soulofw0lf.rpgapi.partyapi.PartyGroup;
 import com.vartala.soulofw0lf.rpgapi.playerapi.RpgPlayer;
 import com.vartala.soulofw0lf.rpgapi.poisonapi.PoisonBuilder;
 import com.vartala.soulofw0lf.rpgapi.poisonapi.PoisonTimeChecker;
 import com.vartala.soulofw0lf.rpgapi.poisonapi.RpgPoison;
 import com.vartala.soulofw0lf.rpgapi.spellapi.MagicSpell;
 import com.vartala.soulofw0lf.rpgapi.sqlapi.SQLHandler;
 import de.kumpelblase2.remoteentities.EntityManager;
 import de.kumpelblase2.remoteentities.RemoteEntities;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 import com.vartala.soulofw0lf.rpgapi.eventsapi.MapRightClickEvent;
 import com.vartala.soulofw0lf.rpgapi.listenersapi.MapListener;
 import com.vartala.soulofw0lf.rpgapi.listenersapi.playerLogIn;
 import com.vartala.soulofw0lf.rpgapi.mapsapi.ScrollMap;
 import com.vartala.soulofw0lf.rpgapi.util.PlayerUtil;
 
 //@author soulofwolf linksbro..
 public class RpgAPI extends JavaPlugin implements Listener {
     //Plugins
     public static RpgAPI plugin;
     //
     //files
     public static YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/RpgPlayers.yml"));
     public static YamlConfiguration localeConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Locale.yml"));
     public static YamlConfiguration guildConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Guilds.yml"));
     public static YamlConfiguration chatConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/ChatChannels.yml"));
     public static YamlConfiguration foodConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Food.yml"));
     public static YamlConfiguration clickConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Clicks.yml"));
     public static YamlConfiguration settingsConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Settings.yml"));
     public static YamlConfiguration achievementConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Achievements.yml"));
     public static YamlConfiguration minionConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Minions.yml"));
     public static YamlConfiguration classConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Classes.yml"));
     public static YamlConfiguration commandConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Commands.yml"));
     public static YamlConfiguration mobCommand = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/MobCommands.yml"));
     public static YamlConfiguration poisonCommand = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Poisons.yml"));
     public static YamlConfiguration testPlayer = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/RpgPlayer/TestPlayer.yml"));
     public static YamlConfiguration worldBorder = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/RpgBorders.yml"));
 
 
     //Listeners
     public MapListener MapListen;
     public playerLogIn PlayerListener;
     public ClickInvListener clickListener;
     public ChatListener chatListener;
     public FoodListener foodListener;
     public PoisonListener poisonlistener;
 
     //plugin booleans
     public static boolean useMySql = false;
     public static boolean guildsOn = true;
     public static boolean chatOn = true;
     public static boolean poisonedEarthOn = true;
     public static boolean classesOn = true;
     public static boolean racesOn = true;
     public static boolean achievementsOn = true;
     public static boolean foodOn = true;
     public static boolean partyOn = true;
     public static boolean minionsOn = true;
     public static boolean clickOn = true;
     public static boolean friendsOn = true;
     public static boolean lobbyOn = true;
     public static boolean questOn = true;
     public static boolean spawnsOn = true;
     public static boolean warpsOn = true;
     public static boolean tradeOn = true;
     public static boolean borderOn = true;
     public static boolean reputationOn = true;
 
 
     //Utilities
     PlayerUtil PlayerUtility = new PlayerUtil(this);
 
     //Variables
     public static ArrayList<RpgClickInv> rpgClicks = new ArrayList<RpgClickInv>();
     public Map<String, GuildObject> guilds = new HashMap<>();
     public Map<String, PartyGroup> partys = new HashMap<>();
     public Map<String, LFGPlayer> lookingForGroup = new HashMap<>();
     public Map<String, GuildRank> guildRanks = new HashMap<>();
     public Integer partyQue = 0;
     public Map<Integer, MinionEntity> activeMinions = new HashMap<>();
     public Map<String, FactionLevel> allFactions = new HashMap<>();
     public Map<String, TitleAchievement> titleAchievs = new HashMap<>();
     public Map<Spell, MagicSpell> allSpells = new HashMap<>();
     public static Map<String, RpgPlayer> rpgPlayers = new HashMap<>();
     public Map<ClassName, RpgClasses> rpgClasses = new HashMap<>();
     public static List<ChatClass> chatClasses = new ArrayList<ChatClass>();
     public static Map<String, String> activeNicks = new HashMap<>();
     public Map<String, CustomFood> foodItems = new HashMap<>();
     public static boolean rpgStyleFood = true;
     public static String dBUserName = "";
     public static String dBPassword = "";
     public static String dBEncoding = "";
     public static boolean uniCode = true;
     public static String dBURL = "";
     public static Logger logger = Logger.getLogger(RpgAPI.class.getName());
     public static SQLHandler sqlHandler = null;
     public static List<String> commands = new ArrayList<String>();
     public static Map<String, MobCommand> minionCommands = new HashMap<>();
     public static EntityManager entityManager;
     public static Map<String, RpgPoison> rpgPoisons = new HashMap<>();
     public static Map<String, File> playerFiles = new HashMap<>();
    public static Map<String, Location> worldBorders = new HashMap<>();
 
     @Override
     public void onEnable() {
         plugin = this;
         saveDefaultConfig();
         useMySql = getConfig().getBoolean("Use Mysql");
         guildsOn = getConfig().getBoolean("Plugins.Guilds");
         chatOn = getConfig().getBoolean("Plugins.Chat");
         poisonedEarthOn = getConfig().getBoolean("Plugins.Poisoned Earth");
         classesOn = getConfig().getBoolean("Plugins.Classes");
         racesOn = getConfig().getBoolean("Plugins.Races");
         achievementsOn = getConfig().getBoolean("Plugins.Achievements");
         foodOn = getConfig().getBoolean("Plugins.Food");
         partyOn = getConfig().getBoolean("Plugins.Party");
         minionsOn = getConfig().getBoolean("Plugins.Minions");
         clickOn = getConfig().getBoolean("Plugins.Click");
         friendsOn = getConfig().getBoolean("Plugins.Friends");
         lobbyOn = getConfig().getBoolean("Plugins.Lobby");
         questOn = getConfig().getBoolean("Plugins.Quest");
         spawnsOn = getConfig().getBoolean("Plugins.Spawns");
         warpsOn = getConfig().getBoolean("Plugins.Warps");
         tradeOn = getConfig().getBoolean("Plugins.Trade");
         borderOn = getConfig().getBoolean("Plugins.Border");
         reputationOn = getConfig().getBoolean("Plugins.Reputation");
         Bukkit.getPluginManager().registerEvents(this, this);
         this.MapListen = new MapListener(this);
         this.PlayerListener = new playerLogIn(this);
         File f = new File("plugins/RpgAPI/RpgPLayers");
         File[] files = f.listFiles();
         if (!(files == null)){
         for (File playerFile : files)
         {
             YamlConfiguration.loadConfiguration(playerFile);
             String fileName = playerFile.getName();
             playerFiles.put(fileName, playerFile);
         }
         }
         if (clickOn){this.clickListener = new ClickInvListener(this);}
         if (chatOn){this.chatListener = new ChatListener(this);}
         if (foodOn){this.foodListener = new FoodListener(this);}
         if (minionsOn){this.entityManager = RemoteEntities.createManager(this);}
         if (poisonedEarthOn){this.poisonlistener = new PoisonListener(this);}
         if (borderOn){
             worldBorder = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/RpgBorders.yml"));
             if (worldBorder.get("Worlds") == null){
                worldBorder.set("Worlds.Kardegah.X", 0);
                worldBorder.set("Worlds.Kardegah.Y", 0);
                worldBorder.set("Worlds.Kardegah.Z", 0);
                worldBorder.set("Worlds.Kardegah.Radius", 5100);
             }
             try {
                 worldBorder.save(new File("plugins/RpgAPI/RpgBorders.yml"));
 
             } catch (IOException e) {
             }
             Double X = 0.0;
             Double Y = 0.0;
             Double Z = 0.0;
             Location loc = null;
             Integer radius = 0;
             for (String worlds : worldBorder.getConfigurationSection("Worlds").getKeys(false)){
                 X = worldBorder.getDouble("Worlds." + worlds + ".X");
                 Y = worldBorder.getDouble("Worlds." + worlds + ".Y");
                 Z = worldBorder.getDouble("Worlds." + worlds + ".Z");
                 radius = worldBorder.getInt("Worlds." + worlds + ".Radius");
                loc = new Location(Bukkit.getWorld(worlds), X, Y, Z);
                 BorderCheck.cycleCheck(this, loc, radius);
             }
 
         }
         //grab database values if they should be used
         if (useMySql) {
             dBUserName = getConfig().getString("Mysql Database.User");
             dBPassword = getConfig().getString("Mysql Database.Password");
             dBEncoding = getConfig().getString("Mysql Database.Encoding");
             uniCode = getConfig().getBoolean("Mysql Database.UseUnicode");
             dBURL = getConfig().getString("Mysql Database.URL");
             sqlHandler = new SQLHandler(dBUserName, dBPassword, dBURL);
         }
         //load yml files and set a value to each of them if they don't exist.
         playerConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/RpgPlayers.yml"));
         testPlayer =  YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/RpgPlayer/TestPlayer.yml"));
 
         localeConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Locale.yml"));
         if (guildsOn){guildConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Guilds.yml"));}
         if (chatOn){chatConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/ChatChannels.yml"));}
         if (foodOn){foodConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Food.yml"));}
         if (clickOn){clickConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Clicks.yml"));}
         settingsConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Settings.yml"));
         if (achievementsOn){achievementConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Achievements.yml"));}
         if (minionsOn){minionConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Minions.yml"));}
         if (classesOn){classConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Classes.yml"));}
         commandConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Commands.yml"));
         if (minionsOn){mobCommand = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/MobCommands.yml"));}
         if (poisonedEarthOn){poisonCommand = YamlConfiguration.loadConfiguration(new File("plugins/RpgAPI/Poisons.yml"));
         if (poisonCommand.get("Poisons") == null){
             poisonCommand.set("Poisons.Sickening Ground.Potion Effects.POISON.Duration", 15);
             poisonCommand.set("Poisons.Sickening Ground.Potion Effects.POISON.Strength", 2);
             poisonCommand.set("Poisons.Sickening Ground.Potion Effects.WEAKNESS.Duration", 30);
             poisonCommand.set("Poisons.Sickening Ground.Potion Effects.WEAKNESS.Strength", 4);
             poisonCommand.set("Poisons.Sickening Ground.World", "Kardegah");
             poisonCommand.set("Poisons.Sickening Ground.Potion X", -2587);
             poisonCommand.set("Poisons.Sickening Ground.Potion Z", -3060);
             poisonCommand.set("Poisons.Sickening Ground.Potion Y", 162);
             poisonCommand.set("Poisons.Sickening Ground.Above Y", true);
             poisonCommand.set("Poisons.Sickening Ground.Reset Length", 10);
             poisonCommand.set("Poisons.Sickening Ground.Radius", 38);
         }}
         if (minionsOn){if (mobCommand.get("Mob Commands") == null){
             mobCommand.set("Mob Commands.Set 1.Item 1.Commands.1.ClickType", "right");
         }}
         if (commandConfig.get("Commands") == null){
             commandConfig.set("Commands.SetNick", "nick");
         }
         for (String command : commandConfig.getConfigurationSection("Commands").getKeys(false)){
              commands.add(command);
         }
         if (playerConfig.get("Active Nicks") == null) {
             playerConfig.set("Active Nicks.Sample Player", "Sample Nick");
         }
         for (String players : playerConfig.getConfigurationSection("Active Nicks").getKeys(false)){
             String currentNick = playerConfig.getString("Active Nicks." + players);
             activeNicks.put(players, currentNick);
         }
         if (localeConfig.get("Locale Settings") == null) {
             localeConfig.set("Locale Settings", "This file is used to set all language settings!");
         }
         if (guildsOn){if (guildConfig.get("Guilds Info") == null) {
             guildConfig.set("Guilds Info", "This File will save all guild info, Mysql is highly recommended!");
         }}
         if (chatOn){if (chatConfig.get("Channels") == null) {
             chatConfig.set("Channels", "This config will save all your chat channel data.");
         }}
         if (foodOn){if (foodConfig.get("Rpg Foods") == null) {
             foodConfig.set("Rpg Foods", "This file will save all your Rpg Food Items");
         }}
         if (clickOn){if (clickConfig.get("Click File") == null) {
             clickConfig.set("Click File", "This File is used to save all your click menu's");
         }}
         if (settingsConfig.get("Settings") == null) {
             settingsConfig.set("Settings", "this file is used for all plugin settings!");
         }
         if (achievementsOn){if (achievementConfig.get("Achievements") == null) {
             achievementConfig.set("Achievements", "This file stores all your servers saved Achievements");
         }}
         if (minionsOn){if (minionConfig.get("Minions") == null) {
             minionConfig.set("Minions", "this file is used to store all minion and monster data (Mysql is highly recommended!");
         }}
         if (classesOn){if (classConfig.get("Classes") == null) {
             classConfig.set("Classes", "This file is used to save all Rpg Classes");
         }}
         try {
             playerConfig.save(new File("plugins/RpgAPI/RpgPlayers.yml"));
             localeConfig.save(new File("plugins/RpgAPI/Locale.yml"));
             testPlayer.save(new File("plugins/RpgAPI/RpgPlayer/TestPlayer.yml"));
             if (guildsOn){guildConfig.save(new File("plugins/RpgAPI/Guilds.yml"));}
             if (chatOn){chatConfig.save(new File("plugins/RpgAPI/ChatChannels.yml"));}
             if (foodOn){foodConfig.save(new File("plugins/RpgAPI/Food.yml"));}
             if (clickOn){clickConfig.save(new File("plugins/RpgAPI/Clicks.yml"));}
             settingsConfig.save(new File("plugins/RpgAPI/Settings.yml"));
             if (achievementsOn){achievementConfig.save(new File("plugins/RpgAPI/Achievements.yml"));}
             if (minionsOn){minionConfig.save(new File("plugins/RpgAPI/Minions.yml"));}
             if (classesOn){classConfig.save(new File("plugins/RpgAPI/Classes.yml"));}
             if (minionsOn){mobCommand.save(new File("plugins/RpgAPI/MobCommands.yml"));}
             if (poisonedEarthOn){poisonCommand.save(new File("plugins/RpgAPI/Poisons.yml"));}
         } catch (IOException e) {
         }
         //SetBuilder.minionCommand();
         if (poisonedEarthOn){
         PoisonBuilder.newPoison();
         PoisonTimeChecker.PoisonRegionTimer();
         }
     }
 
     @EventHandler
     public void onRightClickMap(MapRightClickEvent event) {
         if (event.isPlayerSneaking()) {
             ScrollMap.getScrollMap(event.getMapID()).getMapRender().decrementIndex();
             ScrollMap.getScrollMap(event.getMapID()).update(event.getPlayer().getName());
         } else {
             ScrollMap.getScrollMap(event.getMapID()).getMapRender().incrementIndex();
             ScrollMap.getScrollMap(event.getMapID()).update(event.getPlayer().getName());
         }
     }
     @EventHandler(priority = EventPriority.HIGHEST)
     public void playerCommand(PlayerCommandPreprocessEvent event){
         Player p = event.getPlayer();
         String s = event.getMessage();
         String[] args = s.split(" ");
         String cmd = args[0].replaceAll("/","");
         for (String command : commands){
         if (cmd.equalsIgnoreCase(command)){
             UniqueCommands.BaseCommandHandler(p, args);
             event.setCancelled(true);
         }
         }
     }
 
 }
