 package me.tehbeard.BeardAch;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import me.tehbeard.BeardAch.achievement.*;
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.*;
 import me.tehbeard.BeardAch.achievement.rewards.*;
 import me.tehbeard.BeardAch.commands.*;
 import me.tehbeard.BeardAch.dataSource.*;
 import me.tehbeard.BeardAch.dataSource.configurable.IConfigurable;
 import me.tehbeard.BeardAch.listener.BeardAchPlayerListener;
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 import me.tehbeard.utils.addons.AddonLoader;
 import me.tehbeard.utils.factory.ConfigurableFactory;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.*;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 import org.bukkit.event.Listener;
 import org.bukkit.permissions.Permissible;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.hydrox.bukkit.DroxPerms.DroxPerms;
 import de.hydrox.bukkit.DroxPerms.DroxPermsAPI;
 
 public class BeardAch extends JavaPlugin {
 
     public static BeardAch self;
     private PlayerStatManager stats = null;
     private AchievementManager achievementManager;
     private AddonLoader<IConfigurable> addonLoader;
     public PlayerStatManager getStats(){
         return stats;
 
     }
     public static DroxPermsAPI droxAPI = null;
     private static final String PERM_PREFIX = "ach";
 
     public static boolean hasPermission(Permissible player,String node){
 
         return (player.hasPermission(PERM_PREFIX + "." + node) || player.isOp());
 
 
     }
     public static void printCon(String line){
         System.out.println("[BeardAch] " + line);
     }
 
     public static void printDebugCon(String line){
         if(self.getConfig().getBoolean("general.debug")){
             System.out.println("[BeardAch][DEBUG] " + line);
         }
     }
 
     public void onDisable() {
 
         achievementManager.database.flush();
 
     }
 
     private void EnableBeardStat(){
         BeardStat bs = (BeardStat) Bukkit.getServer().getPluginManager().getPlugin("BeardStat");
         if(bs!=null && bs.isEnabled()){
             stats = bs.getStatManager();
         }
 
     }
 
     @SuppressWarnings("unchecked")
     public void onEnable() {
         self = this;
         achievementManager = new AchievementManager();
         //Load config
         printCon("Starting BeardAch");
         if(!getConfig().getKeys(false).contains("achievements")){
             getConfig().options().copyDefaults(true);
         }
         saveConfig();
         reloadConfig();
         updateConfig();
         reloadConfig();
 
         EnableBeardStat();
 
 
         //check DroxPerms
         DroxPerms droxPerms = ((DroxPerms) this.getServer().getPluginManager().getPlugin("DroxPerms"));
         if (droxPerms != null) {
             droxAPI = droxPerms.getAPI();
         }
 
         //setup events
         Listener pl = new BeardAchPlayerListener();
         getServer().getPluginManager().registerEvents(pl, this);
         
        printCon("Loading data Adapters");
         ConfigurableFactory<IDataSource,DataSourceDescriptor> dataSourceFactory = new ConfigurableFactory<IDataSource, DataSourceDescriptor>(DataSourceDescriptor.class) {
             
             @Override
             public String getTag(DataSourceDescriptor annotation) {
                 return annotation.tag();
             }
         };
         
         /*
         if(getConfig().getString("ach.database.type","").equalsIgnoreCase("mysql")){
             achievementManager.database = new SqlDataSource();
         }
         if(getConfig().getString("ach.database.type","").equalsIgnoreCase("null")){
 
             achievementManager.database = new NullDataSource();	
         }
         if(getConfig().getString("ach.database.type","").equalsIgnoreCase("file")){
 
             achievementManager.database = new YamlDataSource();	
         }*/
         achievementManager.database = dataSourceFactory.getProduct(getConfig().getString("ach.database.type",""));
 
         if(achievementManager.database == null){
             printCon("!!NO SUITABLE DATABASE SELECTED!!");
             printCon("!!DISABLING PLUGIN!!");
 
             //onDisable();
             setEnabled(false);
             return;
         }
 
         printCon("Installing default triggers");
         //Load installed triggers
         addTrigger(AchCheckTrigger.class);
         addTrigger(CuboidCheckTrigger.class);
         addTrigger(LockedTrigger.class);
         addTrigger(NoAchCheckTrigger.class);
         addTrigger(PermCheckTrigger.class);
         addTrigger(StatCheckTrigger.class);
         addTrigger(StatWithinTrigger.class);
         addTrigger(EconomyTrigger.class);
 
         printCon("Installing default rewards");
         //load installed rewards
         addReward(CommandReward.class);
         addReward(CounterReward.class);
         addReward(DroxSubGroupReward.class);
         addReward(DroxTrackReward.class);
         addReward(EconomyReward.class);
 
 
         printCon("Preparing to load addons");
         //Create addon dir if it doesn't exist
         File addonDir = (new File(getDataFolder(),"addons"));
         if(!addonDir.exists()){
             addonDir.mkdir();
         }
 
         //create the addon loader
         addonLoader = new AddonLoader<IConfigurable>(addonDir, IConfigurable.class){
             @Override
             public List<String> getClassList(ZipFile addon) {
                 List<String> classList = new ArrayList<String>();
                 try {
                     ZipEntry manifest = addon.getEntry("achaddon.yml");
                     if (manifest != null) {
                         YamlConfiguration addonConfig = new YamlConfiguration();
 
                         addonConfig.load(addon.getInputStream(manifest));
 
                         BeardAch.printCon("Loading addon " + addonConfig.getString("name","N/A"));
                         for(String className:addonConfig.getStringList("classes")){
                             classList.add(className);
                         }
                     }
                 } catch (IOException e) {
                     printCon("[ERROR] An I/O error occured while trying to access an addon. " + addon.getName());
                     if(self.getConfig().getBoolean("general.debug")){
                         e.printStackTrace();
                     }
                 } catch (InvalidConfigurationException e) {
                     printCon("[ERROR] Configuration header for "+ addon.getName() + " appears to be corrupt");
                     if(self.getConfig().getBoolean("general.debug")){
                         e.printStackTrace();
                     }
                 }
                 return classList;
             }
 
             @Override
             public void makeClass(Class<? extends IConfigurable> classType) {
                 if(classType!=null){
                     if(ITrigger.class.isAssignableFrom(classType)){
                         addTrigger((Class<? extends ITrigger>) classType);
                     }else if(IReward.class.isAssignableFrom(classType)){
                         addReward((Class<? extends IReward>) classType);
                     }
                 }
             }
 
 
         };
 
         printCon("Loading addons");
         addonLoader.loadAddons();
         printCon("Loading Achievements");
         achievementManager.loadAchievements();
 
         printCon("Starting achievement checker");
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 
             public void run() {
                 achievementManager.checkPlayers();
             }
 
         }, 600L,600L);
 
 
         printCon("Loading commands");
         //commands
 
         getCommand("ach-reload").setExecutor(new AchReloadCommand());
         getCommand("ach").setExecutor(new AchCommand());
         getCommand("ach-fancy").setExecutor(new AchFancyCommand());
         printCon("Loaded Version:" + getDescription().getVersion());
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
 
         sender.sendMessage("COMMAND NOT IMPLEMENTED");
         return true;
     }
 
     private void updateConfig(){
         File f = new File(getDataFolder(),"BeardAch.yml");
 
         if(f.exists()){
             try {
                 YamlConfiguration.loadConfiguration(f).save(new File(getDataFolder(),"config.yml"));
                 f.renameTo(new File(getDataFolder(),"BeardAch.yml.old"));
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     public void addTrigger(Class<? extends ITrigger > trigger){
         AbstractDataSource.triggerFactory.addProduct(trigger);
     }
     public void addReward(Class<? extends IReward >  reward){
         AbstractDataSource.rewardFactory.addProduct(reward);
     }
 
     /**
      * return the achievement manager
      * @return
      */
     public AchievementManager getAchievementManager(){
         return achievementManager;
 
     }
 
     /**
      * Colorises strings containing &0-f
      * @param msg
      * @return
      */
     public static String colorise(String msg){
 
         for(int i = 0;i<=9;i++){
             msg = msg.replaceAll("&" + i, ChatColor.getByChar(""+i).toString());
         }
         for(char i = 'a';i<='f';i++){
             msg = msg.replaceAll("&" + i, ChatColor.getByChar(i).toString());
         }
         return msg;
     }
 }
