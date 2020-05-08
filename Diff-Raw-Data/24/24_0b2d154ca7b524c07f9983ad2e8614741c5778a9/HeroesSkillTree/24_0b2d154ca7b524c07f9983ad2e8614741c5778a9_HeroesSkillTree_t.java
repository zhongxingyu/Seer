 package me.Whatshiywl.heroesskilltree;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.classes.HeroClass;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import me.Whatshiywl.heroesskilltree.commands.SkillAdminCommand;
 import me.Whatshiywl.heroesskilltree.commands.SkillDownCommand;
 import me.Whatshiywl.heroesskilltree.commands.SkillInfoCommand;
 import me.Whatshiywl.heroesskilltree.commands.SkillListCommand;
 import me.Whatshiywl.heroesskilltree.commands.SkillLockedCommand;
 import me.Whatshiywl.heroesskilltree.commands.SkillUpCommand;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HeroesSkillTree extends JavaPlugin {
 
    //version = 1.5.1-b
    public final int VERSION = 1;
    public final double SUBVERSION = 5.1;
 
     public static final Logger logger = Logger.getLogger("Minecraft");
     public final EventListener HEventListener = new EventListener(this);
 
     public HeroesSkillTree plugin;
     public static Heroes heroes = (Heroes) Bukkit.getServer().getPluginManager().getPlugin("Heroes");
     public List<Skill> SkillStrongParents = new ArrayList<>();
     public List<Skill> SkillWeakParents = new ArrayList<>();
     private HashMap<String,HashMap<String,HashMap<String, Integer>>> playerSkills = new HashMap<>();
     private HashMap<String,HashMap<String, Integer>> playerClasses = new HashMap<>();
     private int pointsPerLevel = 1;
     private HashMap<String, FileConfiguration> hConfigs = new HashMap<>();
 
     @Override
     public void onDisable() {
         saveAll();
         logger.info("[HeroesSkillTree] Has Been Disabled!");
     }
 
     @Override
     public void onEnable() {
        String message = "[HeroesSkillTree] Version " + VERSION + "." + SUBVERSION + "-Beta Has Been Enabled!";
         logger.info(message);
         PluginManager pm = getServer().getPluginManager();
         getConfig().options().copyDefaults(true);
         saveConfig();
         loadConfig();
         pm.registerEvents(HEventListener, this);
 
         for(Player player : Bukkit.getServer().getOnlinePlayers()){
             Hero hero = heroes.getCharacterManager().getHero(player);
             recalcPlayerPoints(hero, hero.getHeroClass());
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 
         //SKILLUP
         if (commandLabel.equalsIgnoreCase("skillup")) {
             SkillUpCommand.skillUp(this, sender, args);
             return true;
         }
 
         //SKILLDOWN
         if(commandLabel.equalsIgnoreCase("skilldown")){
             SkillDownCommand.skillDown(this, sender, args);
             return true;
         }
 
         //SKILLINFO
         if(commandLabel.equalsIgnoreCase("skillinfo")){
             SkillInfoCommand.skillInfo(this, sender, args);
             return true;
         }
 
         //SKILLPOINTS
         if(commandLabel.equalsIgnoreCase("skillpoints")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                 return true;
             }
             Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
 
             if(sender.hasPermission("skilltree.points")) {
                 sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You currently have " + getPlayerPoints(hero) + " SkillPoints.");
             } else {
                 sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
             }
             return true;
         }
 
         //SKILLADMIN
         if(commandLabel.equalsIgnoreCase("skilladmin")){
             SkillAdminCommand.skillAdmin(this, sender, args);
             return true;
         }
 
         //SKILLLIST
         if (commandLabel.equalsIgnoreCase("slist") || commandLabel.equalsIgnoreCase("sl")) {
             SkillListCommand.skillList(this, sender, args);
             return true;
         }
 
         //SKILLUNLOCKABLE
         if (commandLabel.equalsIgnoreCase("unlocks") || commandLabel.equalsIgnoreCase("un")) {
             SkillLockedCommand.skillList(this, sender, args);
             return true;
         }
 
         sender.sendMessage(ChatColor.GOLD + "HeroesSkillTree Help Page:");
         sender.sendMessage(ChatColor.GRAY + "/skillup <skill> [amount] (level up a skill)");
         sender.sendMessage(ChatColor.GRAY + "/skilldown <skill> [amount] (de-levels a skill)");
         sender.sendMessage(ChatColor.GRAY + "/slist (lists all unlocked skills)");
         sender.sendMessage(ChatColor.GRAY + "/unlocks (lists all adjacent unlockable skills)");
         sender.sendMessage(ChatColor.GRAY + "/skillinfo <skill> (all info on a skill)");
         sender.sendMessage(ChatColor.GRAY + "/skilladmin <command> (amount) [player]");
         return true;
     }
 
     /*public FileConfiguration loadPlayer(String name) {
         //Creates new classes section
         FileConfiguration playerConfig = getPlayerConfig(name);
         if(!playerConfig.contains("classes")) {
             playerConfig.createSection("classes");
         }
         //Creates new skills section
         if(!playerConfig.contains("skills")) {
             playerConfig.createSection("skills");
         }
         try {
             playerConfig.save(new File(getDataFolder() + "/data", name + ".yml"));
         } catch (Exception e) {
             System.out.println("[HeroesSkillTree] failed to save " + name + ".yml");
         }
         return playerConfig;
     }*/
 
     /*public void recalcPlayer(Player player, HeroClass heroclass){
         Hero hero = heroes.getCharacterManager().getHero(player);
         //If /hero reset
         if(heroclass.isDefault()) {
             resetPlayer(player);
         }
         //If the player already has that class
         else if(!(hero.getLevel(heroclass) > 0)){
             //If the class hasn't been registered yet, register it!
             if(!getPlayerClassConfig(player).contains(heroclass.getName())){
                 getPlayerClassConfig(player).createSection(heroclass.getName());
                 int i = 0;
                 for(Skill skill : heroes.getSkillManager().getSkills()) {
                     if(heroclass.hasSkill(skill.getName())){
                         i += getSkillLevel(hero, skill);
                     }
                 }
                 if(hero.getLevel(heroclass) - 1 - i < 0) {
                     getPlayerClassConfig(player).set(heroclass.getName(), 0);
                 }
                 else {
                     getPlayerClassConfig(player).set(heroclass.getName(), hero.getLevel(heroclass) - 1 - i);
                 }
             }
         } else { //It's a new class
             FileConfiguration playerConfig = getPlayerConfig(player.getName());
             getPlayerClassConfig(player).createSection(heroclass.getName());
             getPlayerClassConfig(player).set(heroclass.getName(), 0);
             for(Skill skill : heroes.getSkillManager().getSkills()) {
                 if(heroclass.hasSkill(skill.getName()) &&
                         !getPlayerSkillConfig(player).contains(skill.getName())) {
                     playerConfig.set("skills." + skill.getName(), 0);
                 }
             }
             try {
                 playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
             } catch (Exception e) {
                 System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
             }
         }
     }*/
 
     /*public void savePlayer(Player player){
         Hero hero = heroes.getCharacterManager().getHero(player);
         setPlayerPoints(hero, getPlayerPoints(hero));
         for(Skill skill : heroes.getSkillManager().getSkills()){
             if(hero.hasAccessToSkill(skill) &&
                     !getPlayerConfig(player.getName()).getConfigurationSection("skills").contains(skill.getName())){
                     //Creates new skills to player's section
                 getPlayerSkillConfig(player).set(skill.getName(), 0);
             }
         }
         try {
             playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
         } catch (Exception e) {
             System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
         }
     }*/
 
     public void resetPlayer(Player player){
         String name = player.getName();
         playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
         playerClasses.put(name, new HashMap<String, Integer>());
         resetPlayerConfig(name);
     }
 
     private void resetPlayerConfig(String name) {
         File playerFolder = new File(getDataFolder(), "data");
         if (!playerFolder.exists()) {
             playerFolder.mkdir();
         }
         File playerFile = new File(playerFolder, name + ".yml");
         if (playerFolder.exists() && !playerFolder.delete()) {
             logger.log(Level.SEVERE, "[HeroesSkillTree] failed to delete " + name + ".yml");
             return;
         }
         try {
             playerFile.createNewFile();
         } catch (IOException ex) {
             logger.log(Level.SEVERE, "[HeroesSkillTree] failed to create new " + name + ".yml");
         }
         return;
     }
 
     public int getPlayerPoints(Hero hero){
         //return getPlayerClassConfig(hero.getPlayer()).getInt(hero.getHeroClass().getName());
         if (playerClasses.get(hero.getPlayer().getName()) == null ||
             playerClasses.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null) {
             return 0;
         }
         return playerClasses.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName());
     }
 
     public void recalcPlayerPoints(Hero hero, HeroClass hClass) {
         String name = hero.getPlayer().getName();
         String className = hClass.getName();
         int points = hero.getLevel(hClass)*getPointsPerLevel();
         if (playerClasses.get(name) == null) {
             playerClasses.put(name, new HashMap<String,Integer>());
         }
         if(hero.getPlayer().hasPermission("skilltree.override.usepoints")){
             playerClasses.get(name).put(className, points);
             return;
         }
         if(playerClasses.get(name).get(className) == null) {
             playerClasses.get(name).put(className, 0);
             return;
         }
         if (playerSkills.get(name) == null) {
             playerSkills.put(name, new HashMap<String, HashMap<String,Integer>>());
             return;
         }
         if (playerSkills.get(name).get(className) == null) {
             playerSkills.get(name).put(className, new HashMap<String,Integer>());
             return;
         }
         outer: for(Skill skill : heroes.getSkillManager().getSkills()) {
             String skillName = skill.getName();
             if (playerSkills.get(name).get(className).get(skillName) == null) {
                 continue;
             }
             points -= playerSkills.get(name).get(className).get(skillName);
             if(points < 0){
                 logger.warning("[HeroesSkillTree] " + name + "'s skills are at a too high level!");
                 points = 0;
                 continue outer;
             }
         }
         playerClasses.get(name).put(className, points);
         return;
     }
 
     public void setPlayerPoints(Hero hero, int i) {
         if (playerClasses.get(hero.getPlayer().getName()) == null) {
             playerClasses.put(hero.getPlayer().getName(), new HashMap<String, Integer>());
         }
         playerClasses.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), i);
     }
 
     public void setPlayerPoints(Hero hero, HeroClass hClass, int i) {
         if (playerClasses.get(hero.getPlayer().getName()) == null) {
             playerClasses.put(hero.getPlayer().getName(), new HashMap<String, Integer>());
         }
         playerClasses.get(hero.getPlayer().getName()).put(hClass.getName(), i);
     }
 
     /*public void setPlayerPoints(Hero hero, int i){
         FileConfiguration playerConfig = getPlayerConfig(hero.getPlayer().getName());
         playerConfig.set("classes." + hero.getHeroClass().getName(), i);
         if(getPlayerPoints(hero) < 0) {
             getPlayerClassConfig(hero.getPlayer()).set(hero.getHeroClass().getName(), 0);
         }
         try {
             playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
         } catch (Exception e) {
             System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
         }
     }*/
 
     public int getSkillLevel(Hero hero, Skill skill){
         //return getPlayerSkillConfig(hero.getPlayer()).getInt(skill.getName());
         if (playerSkills.get(hero.getPlayer().getName()) == null ||
                 playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null ||
                 playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()).get(skill.getName()) == null) {
             return 0;
         }
         return playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()).get(skill.getName());
     }
 
     public void setSkillLevel(Hero hero, Skill skill, int i) {
         /*FileConfiguration playerConfig = getPlayerConfig(hero.getPlayer().getName());
         playerConfig.set("skills." + skill.getName(), i);
         try {
             playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
         } catch (Exception e) {
             System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
         }*/
         if (playerSkills.get(hero.getPlayer().getName()) == null) {
             playerSkills.put(hero.getPlayer().getName(), new HashMap<String, HashMap<String, Integer>>());
         }
         if (playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null) {
             playerSkills.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), new HashMap<String, Integer>());
         }
         playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()).put(skill.getName(), i);
     }
 
     public int getSkillMaxLevel(Hero hero, Skill skill) {
         return SkillConfigManager.getSetting(hero.getHeroClass(), skill, "master-level", -1) == -1 ?
                 SkillConfigManager.getUseSetting(hero, skill, "master-level", -1, false) :
                 SkillConfigManager.getSetting(hero.getHeroClass(), skill, "master-level", -1);
     }
 
     public List<String> getStrongParentSkills(Hero hero, Skill skill){
         return getParentSkills(hero, skill, "strong");
     }
 
     public List<String> getWeakParentSkills(Hero hero, Skill skill){
         return getParentSkills(hero, skill, "weak");
     }
 
     public List<String> getParentSkills(Hero hero, Skill skill, String weakOrStrong) {
         FileConfiguration hCConfig = getHeroesClassConfig(hero.getHeroClass());
         if(hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") == null) {
             return null;
         }
         return hCConfig.getConfigurationSection("permitted-skills."
                 + skill.getName() + ".parents").getStringList(weakOrStrong);
     }
 
     public boolean isLocked(Hero hero, Skill skill) {
         if(skill != null && hero.canUseSkill(skill)) {
             boolean skillLevel = getSkillLevel(hero, skill) < 1;
             List<String> strongParents = getStrongParentSkills(hero, skill);
             boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
             List<String> weakParents = getWeakParentSkills(hero, skill);
             boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
             String message = skill.getName() + ": " + skillLevel + "," + hasStrongParents + ":" + hasWeakParents;
             return skillLevel && (hasStrongParents || hasWeakParents);
         }
         return true;
     }
 
     public boolean isMastered(Hero hero, Skill skill){
         if(hero.hasAccessToSkill(skill)) {
             return (getSkillLevel(hero, skill) >= getSkillMaxLevel(hero, skill));
         }
         return false;
     }
 
     public boolean canUnlock(Hero hero, Skill skill){
         if(!hero.hasAccessToSkill(skill) || !hero.canUseSkill(skill)){
             return false;
         }
         List<String> strongParents = getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List<String> weakParents = getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         if(!hasStrongParents && !hasWeakParents) {
             return true;
         }
         if(hasStrongParents){
             for(String name : getStrongParentSkills(hero, skill)){
                 if(!isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                     return false;
                 }
             }
         }
         if(hasWeakParents){
             for(String name : getWeakParentSkills(hero, skill)) {
                 if(isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                     return true;
                 }
             }
             return false;
         }
         return true;
     }
 
     public void loadPlayerConfig(String name) {
         File playerConfigFile;
         FileConfiguration playerConfig = new YamlConfiguration();
         File playerFolder = new File(getDataFolder(), "data");
         if (!playerFolder.exists()) {
             playerFolder.mkdir();
         }
         playerConfigFile = new File(playerFolder, name + ".yml");
         if (!playerConfigFile.exists()) {
             try {
                 playerConfigFile.createNewFile();
             } catch (IOException ex) {
                 logger.severe("[HeroesSkillTree] failed to create new " + name + ".yml");
                 return;
             }
         }
         try {
             playerConfig.load(playerConfigFile);
             if (!playerClasses.containsKey(name)) {
                 playerClasses.put(name, new HashMap<String, Integer>());
             }
             for (String s : playerConfig.getKeys(false)) {
                 playerClasses.get(name).put(s, playerConfig.getInt(s + ".points", 0));
                 if (!playerSkills.containsKey(s)) {
                     playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
                 }
                 if (!playerSkills.get(name).containsKey(s)) {
                     playerSkills.get(name).put(s, new HashMap<String, Integer>());
                 }
                 if (playerConfig.getConfigurationSection(s + ".skills") == null) {
                     continue;
                 }
                 for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
                     playerSkills.get(name).get(s).put(st, playerConfig.getInt(s + ".skills." + st, 0));
                 }
             }
             //Check if the player has points that existed before HST was added to the server
             /*Player player = Bukkit.getPlayer(name);
             if (player == null) {
                 return;
             }
             Hero hero = heroes.getCharacterManager().getHero(player);
             if (hero.getLevel(hero.getHeroClass()) > 1 &&
                     playerClasses.get(name).get(hero.getHeroClass().getName()) < 1) {
                 int points = hero.getLevel(hero.getHeroClass()) - 1;
                 for (String skillName : playerSkills.get(name).get(hero.getHeroClass().getName()).keySet()) {
                     points -= playerSkills.get(name).get(hero.getHeroClass().getName()).get(skillName);
                 }
                 if (points > 0) {
                     playerClasses.get(name).put(hero.getHeroClass().getName(), points);
                 }
             }*/
         } catch (Exception e) {
             logger.severe("[HeroesSkillTree] failed to load " + name + ".yml");
         }
     }
 
     /*public void reloadHeroesClassConfig(HeroClass hClass) {
         heroesClassConfigFile = new File(heroes.getDataFolder() + "/classes", hClass.getName() + ".yml");
 
         heroesClassConfig = YamlConfiguration.loadConfiguration(heroesClassConfigFile);
     }*/
 
     public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
         if (hConfigs.containsKey(hClass.getName())) {
             return hConfigs.get(hClass.getName());
         }
         File classFolder = new File(heroes.getDataFolder(), "classes");
         for (File f : classFolder.listFiles()) {
             FileConfiguration config = new YamlConfiguration();
             try {
                 config.load(f);
                 String currentClassName = config.getString("name");
                 if (currentClassName.equalsIgnoreCase(hClass.getName())) {
                     hConfigs.put(hClass.getName(), config);
                     return config;
                 } else if (!hConfigs.containsKey(currentClassName)) {
                     hConfigs.put(currentClassName, config);
                 }
             } catch (Exception e) {
                 continue;
             }
         }
         return null;
         /*if (heroesClassConfig == null || !heroesClassConfig.getString("name").equals(hClass.getName())) {
             this.reloadHeroesClassConfig(hClass);
         }
         return heroesClassConfig;*/
     }
 
     private void saveAll() {
         for (String s : playerClasses.keySet()) {
             savePlayerConfig(s);
         }
     }
 
     public void savePlayerConfig(String s) {
         FileConfiguration playerConfig = new YamlConfiguration();
         File playerDataFolder = new File(getDataFolder(), "data");
         if (!playerDataFolder.exists()) {
             playerDataFolder.mkdir();
         }
 
         File playerFile = new File(getDataFolder() + "/data", s + ".yml");
         if (!playerFile.exists()) {
             try {
                 playerFile.createNewFile();
             } catch (IOException ioe) {
                 String message = "[HeroesSkillTree] failed to save " + s + ".yml";
                 logger.severe(message);
                 return;
             }
         }
         try {
             playerConfig.load(playerFile);
             for (String className : playerClasses.get(s).keySet()) {
                 playerConfig.set(className + ".points", playerClasses.get(s).get(className));
                 if (!playerSkills.containsKey(s)) {
                     continue;
                 }
                 if (!playerSkills.get(s).containsKey(className)) {
                     continue;
                 }
                 for (String skillName : playerSkills.get(s).get(className).keySet()) {
                     playerConfig.set(className + ".skills." + skillName, playerSkills.get(s).get(className).get(skillName));
                 }
             }
 
             playerConfig.save(playerFile);
         } catch (Exception e) {
             String message = "[HeroesSkillTree] failed to save " + s + ".yml";
             logger.severe(message);
         }
     }
 
     private void loadConfig() {
         File configFile = new File(getDataFolder(), "config.yml");
         if (!configFile.exists()) {
             try {
                 configFile.createNewFile();
             } catch (IOException ioe) {
                 logger.severe("[HeroesSkillTree] failed to create new config.yml");
                 return;
             }
         }
         FileConfiguration config = new YamlConfiguration();
         try {
             config.load(configFile);
             pointsPerLevel = config.getInt("points-per-level", 1);
         } catch (Exception e) {
             logger.severe("[HeroesSkillTree] failed to load config.yml");
         }
     }
 
     public int getPointsPerLevel() {
         return pointsPerLevel;
     }
 }
