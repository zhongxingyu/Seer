 package com.herocraftonline.dev.heroes.persistence;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.Configuration;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 /**
  * Player management
  * 
  * @author Herocraft's Plugin Team
  */
 public class HeroManager {
 
     private Heroes plugin;
     private Set<Hero> heroes;
     private File playerFolder;
     private Timer effectTimer;
     private Timer manaTimer;
     private final static int effectInterval = 100;
     private final static int manaInterval = 5000; // Possible to be configurable?
 
     public HeroManager(Heroes plugin) {
         this.plugin = plugin;
         this.heroes = new HashSet<Hero>();
         playerFolder = new File(plugin.getDataFolder(), "players"); // Setup our Player Data Folder
         playerFolder.mkdirs(); // Create the folder if it doesn't exist.
 
         effectTimer = new Timer(false); // Maintenance thread only
         effectTimer.scheduleAtFixedRate(new EffectChecker(effectInterval, this), 0, effectInterval);
 
         manaTimer = new Timer(false); // Maintenance thread only
         manaTimer.scheduleAtFixedRate(new ManaUpdater(this), 0, manaInterval);
     }
 
     /**
      * Load the given Players Data file.
      * 
      * @param player
      */
     public void loadHeroFile(Player player) {
         File playerFile = new File(playerFolder, player.getName() + ".yml"); // Setup our Players Data File.
         // Check if it already exists, if so we load the data.
         if (playerFile.exists()) {
             Configuration playerConfig = new Configuration(playerFile); // Setup the Configuration
             playerConfig.load(); // Load the Config File
 
             HeroClass playerClass = loadClass(player, playerConfig);
             Hero playerHero = new Hero(plugin, player, playerClass, playerClass.getMaxHealth());
 
             loadExperience(playerHero, playerConfig);
             loadRecoveryItems(playerHero, playerConfig);
             loadBinds(playerHero, playerConfig);
             playerHero.mana = playerConfig.getInt("mana", 0);
 
             playerHero.setVerbose(playerConfig.getBoolean("verbose", true));
            try {
                playerHero.suppressedSkills = new HashSet<String>(playerConfig.getStringList("suppressed", null));
            } catch (Exception e) {
                playerHero.suppressedSkills = new HashSet<String>();
            }
 
             addHero(playerHero);
 
             performSkillChecks(playerHero);
 
             plugin.log(Level.INFO, "Loaded hero: " + player.getName());
         } else {
             // Create a New Hero with the Default Setup.
             createNewHero(player);
             plugin.log(Level.INFO, "Created hero: " + player.getName());
         }
     }
 
     private HeroClass loadClass(Player player, Configuration config) {
         HeroClass playerClass = null;
 
         if (config.getString("class") != null) {
            playerClass = plugin.getClassManager().getClass(config.getString("class")); // Grab the Players Class from
                                                                                        // the File.
             if (Heroes.Permissions != null && playerClass != plugin.getClassManager().getDefaultClass()) {
                 if (!Heroes.Permissions.has(player, "heroes.classes." + playerClass.getName().toLowerCase())) {
                     playerClass = plugin.getClassManager().getDefaultClass();
                 }
             }
         } else {
            playerClass = plugin.getClassManager().getDefaultClass(); // If no Class saved then revert to the Default
                                                                      // Class.
         }
         return playerClass;
     }
 
     private void loadExperience(Hero hero, Configuration config) {
         if (hero == null || hero.getClass() == null || config == null) {
             return;
         }
 
         String root = "experience";
         List<String> expList = config.getKeys(root);
         if (expList != null) {
             for (String className : expList) {
                 int exp = config.getInt(root + "." + className, 0);
                 HeroClass heroClass = plugin.getClassManager().getClass(className);
                 if (heroClass != null) {
                     hero.setExperience(heroClass, exp);
                 }
             }
         }
 
         if (hero.experience.get(hero.getHeroClass().getName()) == null) {
             hero.setExperience(0);
         }
     }
 
     private void loadRecoveryItems(Hero hero, Configuration config) {
         List<ItemStack> itemRecovery = new ArrayList<ItemStack>();
         List<String> itemKeys = config.getKeys("itemrecovery");
         if (itemKeys != null && itemKeys.size() > 0) {
             for (String item : itemKeys) {
                 try {
                     Short durability = Short.valueOf(config.getString("itemrecovery." + item, "0"));
                     Material type = Material.valueOf(item);
                     itemRecovery.add(new ItemStack(type, 1, durability));
                 } catch (IllegalArgumentException e) {
                     this.plugin.debugLog(Level.WARNING, "Either '" + item + "' doesn't exist or the durability is of an incorrect value!");
                     continue;
                 }
             }
         }
         hero.setRecoveryItems(itemRecovery);
     }
 
     private void loadBinds(Hero hero, Configuration config) {
         Map<Material, String[]> binds = new HashMap<Material, String[]>();
         List<String> bindKeys = config.getKeys("binds");
         if (bindKeys != null && bindKeys.size() > 0) {
             for (String material : bindKeys) {
                 try {
                     Material item = Material.valueOf(material);
                     String bind = config.getString("binds." + material, "");
                     if (bind.length() > 0) {
                         binds.put(item, bind.split(" "));
                     }
                 } catch (IllegalArgumentException e) {
                     this.plugin.debugLog(Level.WARNING, material + " isn't a valid Item to bind a Skill to.");
                     continue;
                 }
             }
         }
         hero.binds = binds;
     }
 
     private void performSkillChecks(Hero hero) {
         HeroClass playerClass = hero.getHeroClass();
 
         List<BaseCommand> commands = plugin.getCommandManager().getCommands();
         if (Heroes.Permissions != null) {
             for (BaseCommand cmd : commands) {
                 if (cmd instanceof OutsourcedSkill) {
                     OutsourcedSkill skill = (OutsourcedSkill) cmd;
                     if (playerClass.hasSkill(skill.getName())) {
                         skill.tryLearningSkill(hero);
                     }
                 }
             }
         }
 
         for (BaseCommand cmd : commands) {
             if (cmd instanceof PassiveSkill) {
                 PassiveSkill skill = (PassiveSkill) cmd;
                 if (playerClass.hasSkill(skill.getName())) {
                     skill.tryApplying(hero);
                 }
             }
         }
     }
 
     /**
      * Save the given Players Data to a file.
      * 
      * @param player
      */
     public void saveHeroFile(Player player) {
         File playerFile = new File(playerFolder, player.getName() + ".yml");
         Configuration playerConfig = new Configuration(playerFile);
         // Save the players stuff
         Hero hero = getHero(player);
         playerConfig.setProperty("class", hero.getHeroClass().toString());
         playerConfig.setProperty("verbose", hero.isVerbose());
        playerConfig.setProperty("suppressed", new ArrayList<String>(hero.getSuppressedSkills()));
         playerConfig.setProperty("mana", hero.getMana());
        playerConfig.removeProperty("itemrecovery"); // Just a precaution, we'll remove any values before resaving the
                                                     // list.
 
         saveExperience(hero, playerConfig);
         saveRecoveryItems(hero, playerConfig);
         saveBinds(hero, playerConfig);
 
         playerConfig.save();
         plugin.log(Level.INFO, "Saved hero: " + player.getName());
     }
 
     private void saveExperience(Hero hero, Configuration config) {
         if (hero == null || hero.getClass() == null || config == null) {
             return;
         }
 
         String root = "experience";
         for (Map.Entry<String, Integer> entry : hero.experience.entrySet()) {
             config.setProperty(root + "." + entry.getKey(), entry.getValue());
         }
     }
 
     private void saveRecoveryItems(Hero hero, Configuration config) {
         for (ItemStack item : hero.getRecoveryItems()) {
             String durability = Short.toString(item.getDurability());
             config.setProperty("itemrecovery." + item.getType().toString(), durability);
         }
     }
 
     private void saveBinds(Hero hero, Configuration config) {
         config.removeProperty("binds");
         Map<Material, String[]> binds = hero.getBinds();
         for (Material material : binds.keySet()) {
             String[] bindArgs = binds.get(material);
             StringBuilder bind = new StringBuilder();
             for (String arg : bindArgs) {
                 bind.append(arg).append(" ");
             }
             config.setProperty("binds." + material.toString(), bind.toString().substring(0, bind.toString().length() - 1));
         }
     }
 
     public boolean createNewHero(Player player) {
         Hero hero = new Hero(plugin, player, plugin.getClassManager().getDefaultClass(), plugin.getClassManager().getDefaultClass().getMaxHealth());
         return addHero(hero);
     }
 
     public boolean addHero(Hero hero) {
         return heroes.add(hero);
     }
 
     public boolean removeHero(Hero hero) {
         if (hero.hasParty()) {
             hero.getParty().removeMember(hero.getPlayer());
         }
         return heroes.remove(hero);
     }
 
     public boolean containsPlayer(Player player) {
         return getHero(player) != null;
     }
 
     public Hero getHero(Player player) {
         final Hero[] heroes = getHeroes();
         for (Hero hero : heroes) {
             if (hero == null || hero.getPlayer() == null) {
                 this.heroes.remove(hero); // Seeing as it's null we might as well remove it.
                 continue;
             }
             if (player.getName().equalsIgnoreCase(hero.getPlayer().getName())) {
                 return hero;
             }
         }
         // If it gets to this stage then clearly the HeroManager doesn't have it so we create it...
         loadHeroFile(player);
         final Hero[] heroez = getHeroes();
         for (Hero hero : heroez) {
             if (hero == null) {
                 continue;
             }
             if (player.getName().equalsIgnoreCase(hero.getPlayer().getName())) {
                 return hero;
             }
         }
         return null;
     }
 
     public final Hero[] getHeroes() {
         return heroes.toArray(new Hero[0]);
     }
 
     public Set<Hero> getHeroSet() {
         return heroes;
     }
 
     public void stopTimers() {
         effectTimer.cancel();
         manaTimer.cancel();
     }
 }
 
 class EffectChecker extends TimerTask {
     private final int interval;
     private final HeroManager manager;
 
     EffectChecker(int interval, HeroManager manager) {
         this.interval = interval;
         this.manager = manager;
     }
 
     @Override
     public void run() {
         Hero[] heroes = manager.getHeroes();
         for (Hero hero : heroes) {
             if (hero == null) {
                 continue;
             }
             hero.getEffects().update(interval);
         }
     }
 }
 
 class ManaUpdater extends TimerTask {
     private final HeroManager manager;
 
     ManaUpdater(HeroManager manager) {
         this.manager = manager;
     }
 
     @Override
     public void run() {
         Hero[] heroes = manager.getHeroes();
         for (Hero hero : heroes) {
             if (hero == null) {
                 continue;
             }
 
             int mana = hero.getMana();
            if (hero.getEffects().hasEffect("ManaFreeze")) {
                 mana = 100;
             }
             hero.setMana(mana > 100 ? mana : mana > 95 ? 100 : mana + 5); // Hooray for the ternary operator!
             if (mana != 100 && hero.isVerbose()) {
                 Messaging.send(hero.getPlayer(), "Mana: " + Messaging.createManaBar(hero.getMana()));
             }
         }
     }
 }
