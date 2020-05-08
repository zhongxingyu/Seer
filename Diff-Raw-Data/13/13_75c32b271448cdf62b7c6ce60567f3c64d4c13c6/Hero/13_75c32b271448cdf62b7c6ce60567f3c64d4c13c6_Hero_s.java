 package com.herocraftonline.dev.heroes.hero;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ExperienceChangeEvent;
 import com.herocraftonline.dev.heroes.api.HeroChangeLevelEvent;
 import com.herocraftonline.dev.heroes.api.HeroDamageCause;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.DelayedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class Hero {
 
     private static final DecimalFormat decFormat = new DecimalFormat("#0.##");
 
     private final Heroes plugin;
     private Player player;
     private final String name;
     private HeroClass heroClass;
     private HeroClass secondClass;
     private AtomicInteger mana = new AtomicInteger(0);
     private HeroParty party = null;
     private AtomicBoolean verbose = new AtomicBoolean(true);
     private HeroDamageCause lastDamageCause = null;
     private Map<String, Effect> effects = new HashMap<String, Effect>();
     private Map<String, Double> experience = new ConcurrentHashMap<String, Double>();
     private Map<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();
     private Set<LivingEntity> summons = new HashSet<LivingEntity>();
     private Map<Material, String[]> binds = new ConcurrentHashMap<Material, String[]>();
     private Map<String, Boolean> suppressedSkills = new ConcurrentHashMap<String, Boolean>();
     private Map<String, ConfigurationSection> persistedSkillSettings = new ConcurrentHashMap<String, ConfigurationSection>();
     private Map<String, ConfigurationSection> skills = new HashMap<String, ConfigurationSection>();
     private boolean syncPrimary = true;
     private Integer tieredLevel;
     private double health;
     private PermissionAttachment transientPerms;
     private DelayedSkill delayedSkill = null;
     private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 
     public Hero(Heroes plugin, Player player, HeroClass heroClass, HeroClass secondClass) {
         this.plugin = plugin;
         this.player = player;
         this.name = player.getName();
         this.heroClass = heroClass;
         this.secondClass = secondClass;
         transientPerms = player.addAttachment(plugin);
     }
 
     /**
      * Adds the Effect onto the hero, and calls it's apply method initiating it's first tic.
      *
      * @param effect
      */
     public void addEffect(Effect effect) {
         if (hasEffect(effect.getName())) {
             removeEffect(getEffect(effect.getName()));
         }
 
         if (effect instanceof Periodic || effect instanceof Expirable) {
             plugin.getEffectManager().manageEffect(this, effect);
         }
 
         effects.put(effect.getName().toLowerCase(), effect);
         effect.apply(this);
     }
 
     /**
      * Adds the given permission to the hero
      *
      * @param permission
      */
     public void addPermission(String permission) {
         transientPerms.setPermission(permission, true);
     }
 
     /**
      * Adds the given permission to the hero
      *
      * @param permission
      */
     public void addPermission(Permission permission) {
         transientPerms.setPermission(permission, true);
     }
 
     public void addSkill(String skill, ConfigurationSection section) {
         skills.put(skill.toLowerCase(), section);
     }
 
     /**
      * Returns if the hero has a class with the given experience type
      * 
      * Not Thread-Safe
      */
     public boolean hasExperienceType(ExperienceType type) {
         boolean val = false;
         try {
             rwl.readLock().lock();
             val = heroClass.hasExperiencetype(type) || (secondClass != null && secondClass.hasExperiencetype(type));
         } finally {
             rwl.readLock().unlock();
         }
         return val;
     }
 
     /**
      * Returns if the hero can gain the experience type specified
      * Thread-safe
      * 
      * @param type
      * @return
      */
     public boolean canGain(ExperienceType type) {
         if (type == ExperienceType.ADMIN) {
             return true;
         }
 
         boolean prim = false;
         rwl.readLock().lock();
         if (heroClass.hasExperiencetype(type)) {
             prim = !isMaster(heroClass);
         }
 
         boolean prof = false;
         if (secondClass != null && secondClass.hasExperiencetype(type)) {
             prof = !isMaster(secondClass);
         }
         rwl.readLock().unlock();
         return prim || prof;
     }
 
     /**
      * Adds a skill binding to the given Material.
      * Ignores Air/Null values
      * 
      * Thread-Safe
      * 
      * @param material
      * @param skillName
      */
     public void bind(Material material, String[] skillName) {
         if (material == Material.AIR || material == null) {
             return;
         }
 
         binds.put(material, skillName);
     }
 
     /**
      * Changes the hero's current class to the given class then clears all binds, effects and summons.
      * 
      * @param heroClass
      */
     public void changeHeroClass(HeroClass heroClass, boolean secondary) {
         clearEffects();
         clearSummons();
         clearBinds();
 
         setHeroClass(heroClass, secondary);
 
         if (Heroes.properties.prefixClassName) {
             player.setDisplayName("[" + getHeroClass().getName() + "]" + player.getName());
         }
         plugin.getHeroManager().performSkillChecks(this);
         getTieredLevel(true);
     }
 
     /**
      * Clears all of a player's bindings
      * Thread-Safe
      */
     public void clearBinds() {
         binds.clear();
     }
 
     /**
      * Clears all of a player's cooldowns
      * Thread-safe
      */
     public void clearCooldowns() {
         cooldowns.clear();
     }
 
     /**
      * Iterates over the effects this Hero has and removes them
      */
     public void clearEffects() {
         for (Effect effect : this.getEffects()) {
             this.removeEffect(effect);
         }
     }
 
     /**
      * Clears all experience for all classes on the hero
      * 
      * Thread-Safe
      */
     public void clearExperience() {
         for (Entry<String, Double> entry : experience.entrySet()) {
             entry.setValue(0.0);
         }
     }
 
     /**
      * Removes the summons from the game world - then removes them from the set
      */
     public void clearSummons() {
         for (LivingEntity summon : summons) {
             summon.remove();
         }
         summons.clear();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Hero other = (Hero) obj;
         if (player == null) {
             if (other.player != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     /**
      * Alters the experience for the given class on the Hero
      * This is used for admin commands or direct alterations to the Hero's classes
      *
      * @param expChange - amount of xp to change (positive or negative)
      * @param hc        - HeroClass to change the experience of
      */
     public void addExp(double expChange, HeroClass hc) {
         double exp = getExperience(hc) + expChange;
         if (exp < 0) {
             exp = 0;
         }
         int currentLevel = getLevel(hc);
         setExperience(hc, exp);
 
         //This is called but ignores cancellation.
         ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, hc, expChange, ExperienceType.ADMIN);
         plugin.getServer().getPluginManager().callEvent(expEvent);
 
         syncExperience();
         int newLevel = Properties.getLevel(exp);
         if (currentLevel != newLevel) {
             HeroChangeLevelEvent hLEvent = new HeroChangeLevelEvent(this, hc, currentLevel, newLevel);
             plugin.getServer().getPluginManager().callEvent(hLEvent);
             if (newLevel >= hc.getMaxLevel()) {
                 setExperience(hc, Properties.getTotalExp(hc.getMaxLevel()));
                 Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), hc.getName());
             }
             if (newLevel > currentLevel) {
                 //SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Up!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                 Messaging.send(player, "You gained a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
                 setHealth(getMaxHealth());
                 //Reset food stuff on level up
                 if (player.getFoodLevel() < 20) {
                     player.setFoodLevel(20);
                 }
 
                 player.setSaturation(20);
                 player.setExhaustion(0);
                 syncHealth();
                 getTieredLevel(true);
             } else {
                 if (getHealth() > getMaxHealth()) {
                     setHealth(getMaxHealth());
                     syncHealth();
                 }
                 //SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Lost!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                 Messaging.send(player, "You lost a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
             }
         }
     }
 
     @Deprecated
     public void gainExp(double expChange, ExperienceType source, boolean party) {
         if (party && this.getParty() != null) {
             this.getParty().gainExp(expChange, source, player.getLocation());
         } else {
             gainExp(expChange, source);
         }
     }
 
     /**
      * Adds the specified experience to the hero before modifiers from the given source.
      * expChange value supports negatives for experience loss.
      *
      * @param expChange - amount of base exp to add
      * @param source
      * @param boolean   - distributeToParty
      */
     public void gainExp(double expChange, ExperienceType source) {
         if (player.getGameMode() == GameMode.CREATIVE || Heroes.properties.disabledWorlds.contains(player.getWorld().getName())) {
             return;
         }
         Properties prop = Heroes.properties;
 
         HeroClass[] classes = new HeroClass[]{getHeroClass(), getSecondClass()};
 
         for (HeroClass hc : classes) {
             if (hc == null) {
                 continue;
             }
 
             if (source != ExperienceType.ADMIN && !hc.hasExperiencetype(source)) {
                 continue;
             }
 
             double gainedExp = expChange;
             double exp = getExperience(hc);
 
             // adjust exp using the class modifier if it's positive
             if (gainedExp > 0 && source != ExperienceType.ADMIN) {
                 gainedExp *= hc.getExpModifier();
             } else if (source != ExperienceType.ADMIN && source != ExperienceType.ENCHANTING && isMaster(hc) && (!prop.masteryLoss || !prop.levelsViaExpLoss)) {
                 return;
             }
 
             //This is called once for each class
             ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, hc, gainedExp, source);
             plugin.getServer().getPluginManager().callEvent(expEvent);
             if (expEvent.isCancelled()) {
                 return;
             }
 
             // Lets get our modified xp change value
             gainedExp = expEvent.getExpChange();
 
             int currentLevel = Properties.getLevel(exp);
             int newLevel = Properties.getLevel(exp + gainedExp);
 
             if (isMaster(hc) && source != ExperienceType.ADMIN && source != ExperienceType.ENCHANTING && !prop.masteryLoss) {
                 gainedExp = 0;
                 continue;
             } else if (currentLevel > newLevel && !prop.levelsViaExpLoss && source != ExperienceType.ADMIN && source != ExperienceType.ENCHANTING) {
                 gainedExp = Properties.getTotalExp(currentLevel) - (exp - 1);
             }
 
             // add the experience
             exp += gainedExp;
 
             // If we went negative lets reset our values so that we would hit 0
             if (exp < 0) {
                 gainedExp = -(gainedExp + exp);
                 exp = 0;
             } else if (exp > Properties.maxExp) {
                 exp = Properties.maxExp;
             }
 
             newLevel = Properties.getLevel(exp);
 
             // Reset our new level - in case xp adjustement settings actually don't cause us to change
             setExperience(hc, exp);
 
             // notify the user
             if (gainedExp != 0) {
                 if (isVerbose() && gainedExp > 0) {
                     Messaging.send(player, "$1: Gained $2 Exp", hc.getName(), decFormat.format(gainedExp));
                 } else if (isVerbose() && gainedExp < 0) {
                     Messaging.send(player, "$1: Lost $2 Exp", hc.getName(), decFormat.format(-gainedExp));
                 }
                 if (newLevel != currentLevel) {
                     HeroChangeLevelEvent hLEvent = new HeroChangeLevelEvent(this, hc, currentLevel, newLevel);
                     plugin.getServer().getPluginManager().callEvent(hLEvent);
                     if (newLevel >= hc.getMaxLevel()) {
                         setExperience(hc, Properties.getTotalExp(hc.getMaxLevel()));
                         Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), hc.getName());
                     }
                     if (newLevel > currentLevel) {
                         //SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Up!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                         Messaging.send(player, "You gained a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
                         setHealth(getMaxHealth());
                         setMana(100);
                         //Reset food stuff on level up
                         if (player.getFoodLevel() < 20) {
                             player.setFoodLevel(20);
                         }
 
                         player.setSaturation(20);
                         player.setExhaustion(0);
                         syncHealth();
                         getTieredLevel(true);
                     } else {
                         //SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Lost!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                         Messaging.send(player, "You lost a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
                     }
                 }
             }
 
             // Save the hero file when the Hero changes levels to prevent rollback issues
             if (newLevel != currentLevel) {
                 plugin.getHeroManager().saveHero(this, false);
             }
         }
         syncExperience();
     }
 
 
     public double currentXPToNextLevel(HeroClass hc) {
         return getExperience(hc) - Properties.getTotalExp(getLevel(hc));
     }
 
     protected double calculateXPLoss(double multiplier, HeroClass hc) {
 
         double expForNext = Properties.getExp(getLevel(hc) + 1);
         double currentPercent = currentXPToNextLevel(hc) / expForNext;
 
         if (currentPercent >= multiplier) {
             return expForNext * multiplier;
         } else {
             double amt = expForNext * currentPercent;
             multiplier -= currentPercent;
 
             for (int i = 0; getLevel(hc) - i > 1; i++) {
                 if (1 >= multiplier) {
                     return amt += Properties.getExp(getLevel(hc) - i) * multiplier;
                 }
                 amt += Properties.getExp(getLevel(hc) - i);
                 multiplier -= 1;
             }
             return amt;
         }
     }
 
     public void loseExpFromDeath(double multiplier, boolean pvp) {
         if (player.getGameMode() == GameMode.CREATIVE || Heroes.properties.disabledWorlds.contains(player.getWorld().getName()) || multiplier <= 0) {
             return;
         }
         Properties prop = Heroes.properties;
 
         HeroClass[] classes = new HeroClass[]{getHeroClass(), getSecondClass()};
 
         for (HeroClass hc : classes) {
             if (hc == null) {
                 continue;
             }
 
             double mult = multiplier;
             if (pvp && hc.getPvpExpLoss() != -1) {
                 mult = hc.getPvpExpLoss();
             } else if (!pvp && hc.getExpLoss() != -1) {
                 mult = hc.getExpLoss();
             }
 
             int currentLvl = getLevel(hc);
             double currentExp = getExperience(hc);
             double currentLvlExp = Properties.getTotalExp(currentLvl);
             double gainedExp = -calculateXPLoss(mult, hc);
 
             if (prop.resetOnDeath) {
                 gainedExp = -currentExp;
             } else if (gainedExp + currentExp < currentLvlExp && !prop.levelsViaExpLoss) {
                 gainedExp = -(currentExp - currentLvlExp);
             }
 
             //This is called once for each class
             ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, hc, gainedExp, ExperienceType.DEATH);
             plugin.getServer().getPluginManager().callEvent(expEvent);
             if (expEvent.isCancelled()) {
                 return;
             }
             gainedExp = expEvent.getExpChange();
 
             int newLevel = Properties.getLevel(currentExp + gainedExp);
             if (isMaster(hc) && !prop.masteryLoss) {
                 continue;
             } else if (currentLvl > newLevel && !prop.levelsViaExpLoss) {
                 gainedExp = currentLvlExp - (currentExp - 1);
             }
 
             double newExp = currentExp + gainedExp;
             // If we went negative lets reset our values so that we would hit 0
             if (newExp < 0) {
                 gainedExp = -currentExp;
                 newExp = 0;
             }
 
             // Reset our new level - in case xp adjustement settings actually don't cause us to change
             newLevel = Properties.getLevel(newExp);
             setExperience(hc, newExp);
             // notify the user
 
             if (gainedExp != 0) {
                 if (isVerbose() && gainedExp < 0) {
                     Messaging.send(player, "$1: Lost $2 Exp", hc.getName(), decFormat.format(-gainedExp));
                 }
                 if (newLevel != currentLvl) {
                     HeroChangeLevelEvent hLEvent = new HeroChangeLevelEvent(this, hc, currentLvl, newLevel);
                     plugin.getServer().getPluginManager().callEvent(hLEvent);
                     if (newLevel >= hc.getMaxLevel()) {
                         setExperience(hc, Properties.getTotalExp(hc.getMaxLevel()));
                         Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), hc.getName());
                     }
                     //SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Lost!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                     Messaging.send(player, "You lost a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
                 }
             }
 
 
         }
         plugin.getHeroManager().saveHero(this, false);
         syncExperience();
     }
 
     /**
      * Rerturns the player's name associated with the class
      * Thread-safe
      * @return
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns the binding for the given material
      * Thread-safe
      * 
      * @param mat
      * @return
      */
     public String[] getBind(Material mat) {
         return binds.get(mat);
     }
 
     /**
      * Gets an unmodifiable Map of all Bindings for the hero
      * Thread-Safe
      * @return
      */
     public Map<Material, String[]> getBinds() {
         return Collections.unmodifiableMap(binds);
     }
 
     /**
      * Get a specific cooldown timing
      * Thread-safe
      * 
      * @param name
      * @return
      */
     public Long getCooldown(String name) {
         return cooldowns.get(name.toLowerCase());
     }
 
     /**
      * Gets an unmodifiable Map of all cooldowns for the hero
      * Thread-Safe
      * @return
      */
     public Map<String, Long> getCooldowns() {
         return Collections.unmodifiableMap(cooldowns);
     }
 
     /**
      * Attempts to find the effect from the given name
      *
      * @param name
      * @return the Effect with the name - or null if not found
      */
     public Effect getEffect(String name) {
         return effects.get(name.toLowerCase());
     }
 
     /**
      * get a Clone of all effects active on the hero
      *
      * @return
      */
     public Set<Effect> getEffects() {
         return new HashSet<Effect>(effects.values());
     }
 
     /**
      * Get the hero's experience in it's current class.
      * 
      * Thread-safe
      * @return double experience
      */
     @Deprecated
     public double getExperience() {
         return getExperience(getHeroClass());
     }
 
     /**
      * Get the hero's experience in the given class
      * 
      * Thread-safe
      * @param heroClass
      * @return double experience
      */
     public double getExperience(HeroClass heroClass) {
         if (heroClass == null) {
             return 0;
         }
         Double exp = experience.get(heroClass.getName());
         return exp == null ? 0 : exp;
     }
 
     /**
      * Returns an unmodifiable snapshot of the hero's experience
      * 
      * Thread-Safe
      * @return map of all class-experience values
      */
     public Map<String, Double> getExperienceMap() {
         return Collections.unmodifiableMap(experience);
     }
 
     /**
      * @return the hero's current health - double
      * 
      * Thread-Safe
      */
     public double getHealth() {
         rwl.readLock().lock();
         double val = health;
         rwl.readLock().unlock();
         return val;
     }
 
     /**
      * Returns the hero's currently selected heroclass
      *
      * @return heroclass
      */
     public HeroClass getHeroClass() {
         rwl.readLock().lock();
         HeroClass hc = heroClass;
         rwl.readLock().unlock();
         return hc;
     }
 
     public HeroDamageCause getLastDamageCause() {
         return lastDamageCause;
     }
 
     /**
      * Returns the hero's current highest level
      * Thread-safe
      * @return the level of the character - returns the highest value of the secondclass or primary class
      */
     public int getLevel() {
         rwl.readLock().lock();
         int primary = getLevel(heroClass);
         int second = 0;
         if (secondClass != null) {
             second = getLevel(secondClass);
         }
         rwl.readLock().unlock();
 
         return primary > second ? primary : second;
     }
 
     /**
      * Returns a hero's level based on the skill they are attempting to use
      *
      * @param skill
      * @return
      */
     public int getSkillLevel(Skill skill) {
         int level = -1;
         int secondLevel = -1;
         HeroClass heroClass = getHeroClass();
         if (heroClass.hasSkill(skill.getName())) {
             int requiredLevel = SkillConfigManager.getSetting(heroClass, skill, Setting.LEVEL.node(), 1);
             level = getLevel(heroClass);
             // If this class doesn't meet the level requirement reset it to -1
             if (level < requiredLevel) {
                 level = -1;
             }
         }
         HeroClass secondClass = getSecondClass();
         if (secondClass != null && secondClass.hasSkill(skill.getName())) {
             int requiredLevel = SkillConfigManager.getSetting(secondClass, skill, Setting.LEVEL.node(), 1);
             secondLevel = getLevel(secondClass);
             if (secondLevel < requiredLevel) {
                 secondLevel = -1;
             }
         }
         return secondLevel > level ? secondLevel : level;
     }
 
     /**
      * Thread-safe
      * 
      * @param heroClass
      * @return
      */
     public int getLevel(HeroClass heroClass) {
         return Properties.getLevel(getExperience(heroClass));
     }
 
     public int getTieredLevel(boolean recache) {
         if (tieredLevel != null && !recache) {
             return tieredLevel;
         }
 
         HeroClass heroClass = getHeroClass();
         HeroClass secondClass = getSecondClass();
         if (secondClass == null) {
             tieredLevel = getTieredLevel(heroClass);
         } else {
             int hc = getTieredLevel(heroClass);
             int sc = getTieredLevel(secondClass);
             tieredLevel = hc > sc ? hc : sc;
         }
         return tieredLevel;
     }
 
     /**
      * Gets the tier adjusted level for this character - takes into account already gained levels on parent classes
      *
      * @return
      */
     public int getTieredLevel(HeroClass heroClass) {
         if (heroClass.hasNoParents()) {
             return getLevel(heroClass);
         }
 
         Set<HeroClass> classes = new HashSet<HeroClass>();
         for (HeroClass hClass : heroClass.getParents()) {
             if (this.isMaster(hClass)) {
                 classes.addAll(getTieredLevel(hClass, new HashSet<HeroClass>(classes)));
                 classes.add(hClass);
             }
         }
         int level = getLevel(heroClass);
         for (HeroClass hClass : classes) {
             if (hClass.getTier() == 0) {
                 continue;
             }
             level += getLevel(hClass);
         }
         return level;
     }
 
     /**
      * recursive method to lookup all classes that are upstream of the parent class and mastered
      *
      * @param heroClass
      * @param classes
      */
     private Set<HeroClass> getTieredLevel(HeroClass heroClass, Set<HeroClass> classes) {
         for (HeroClass hClass : heroClass.getParents()) {
             if (this.isMaster(hClass)) {
                 classes.addAll(getTieredLevel(hClass, new HashSet<HeroClass>(classes)));
                 classes.add(hClass);
             }
         }
         return classes;
     }
 
     /**
      * @return the secondClass
      */
     public HeroClass getSecondClass() {
         rwl.readLock().lock();
         HeroClass sc = secondClass;
         rwl.readLock().unlock();
         return sc;
     }
 
     /**
      * All mana is in percentages.
      *
      * @return Hero's current amount of mana
      */
     public int getMana() {
         return mana.get();
     }
 
     /**
      * Maximum health is derived from the hero's class. It is the classes base max hp + hp per level.
      *
      * @return the hero's maximum health
      */
     public double getMaxHealth() {
         HeroClass heroClass = getHeroClass();
         int level = Properties.getLevel(getExperience(heroClass));
         double primaryHp = heroClass.getBaseMaxHealth() + (level - 1) * heroClass.getMaxHealthPerLevel();
         double secondHp = 0;
 
         HeroClass secondClass = getSecondClass();
         if (secondClass != null) {
             level = Properties.getLevel(getExperience(secondClass));
             secondHp = secondClass.getBaseMaxHealth() + (level - 1) * secondClass.getMaxHealthPerLevel();
         }
         return primaryHp > secondHp ? primaryHp : secondHp;
     }
 
     /**
      * Gets the hero's current party - returns null if the hero has no party
      * Thread-Unsafe
      * @return HeroParty
      */
     public HeroParty getParty() {
         return party;
     }
 
     /**
      * Thread-Unsafe
      * @return player associated with this hero
      */
     public Player getPlayer() {
         return player;
     }
 
     public Map<String, ConfigurationSection> getSkills() {
         return new HashMap<String, ConfigurationSection>(skills);
     }
 
     public Map<String, ConfigurationSection> getSkillSettings() {
         return Collections.unmodifiableMap(persistedSkillSettings);
     }
 
     /**
      * gets Mapping of the persistence SkillSettings for the given skill
      * 
      * The method is Thread-Safe, but the object returned is most likely Thread-Unsafe
      * 
      * @param skill
      * @return
      */
     public ConfigurationSection getSkillSettings(Skill skill) {
         return skill == null ? null : getSkillSettings(skill.getName());
     }
 
     /**
      * gets Mapping of the persistence SkillSettings for the given skillName
      *
      * @param skill
      * @return
      */
     public ConfigurationSection getSkillSettings(String skillName) {
         HeroClass secondClass = getSecondClass();
         if (!getHeroClass().hasSkill(skillName) && (secondClass == null || !secondClass.hasSkill(skillName))) {
             return null;
         }
 
         return persistedSkillSettings.get(skillName.toLowerCase());
     }
 
     /**
      * @return set of all summons the hero currently has
      */
     public Set<LivingEntity> getSummons() {
         return summons;
     }
 
     /**
      * Returns the currently suppressed skills
      * For use with verbosity
      *
      * @return
      */
     public Set<String> getSuppressedSkills() {
         return suppressedSkills.keySet();
     }
 
     public boolean hasBind(Material mat) {
         return binds.containsKey(mat);
     }
 
     /**
      * Checks if the hero currently has the Effect with the given name.
      *
      * @param name
      * @return boolean
      */
     public boolean hasEffect(String name) {
         return effects.containsKey(name.toLowerCase());
     }
 
     public boolean hasEffectType(EffectType type) {
         for (Effect effect : effects.values()) {
             if (effect.isType(type)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return player == null ? 0 : name.hashCode();
     }
 
     /**
      * Thread-Unsafe
      * @return if the player has a party
      */
     public boolean hasParty() {
         return party != null;
     }
 
     /**
      * Checks if the hero can use the given skill
      * Thread-Unsafe
      * @param skill
      * @return
      */
     public boolean canUseSkill(Skill skill) {
         if (canPrimaryUseSkill(skill)) {
             return true;
         } else if (canSecondUseSkill(skill)) {
             return true;
         }
         HeroClass secondClass = getSecondClass();
         ConfigurationSection section = skills.get(skill.getName().toLowerCase());
         if (section != null) {
             int level = section.getInt(Setting.LEVEL.node(), 1);
             if (getLevel(getHeroClass()) >= level || (secondClass != null && getLevel(secondClass) >= level)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Checks if the hero's primary class has access to the given skill
      * Thread-Unsafe
      * @param skill
      * @return
      */
     public boolean canPrimaryUseSkill(Skill skill) {
         HeroClass heroClass = getHeroClass();
         if (heroClass.hasSkill(skill.getName())) {
             int level = SkillConfigManager.getSetting(heroClass, skill, Setting.LEVEL.node(), 1);
             if (getLevel(heroClass) >= level) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if the hero's secondary class has access to the given skill
      * Thread-Unsafe
      * @param skill
      * @return
      */
     public boolean canSecondUseSkill(Skill skill) {
         HeroClass secondClass = getSecondClass();
         if (secondClass != null && secondClass.hasSkill(skill.getName())) {
             int level = SkillConfigManager.getSetting(secondClass, skill, Setting.LEVEL.node(), 1);
             if (getLevel(secondClass) >= level) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if the hero can use the given skill
      * This does a level check to make sure the hero has a class with a high enough level to use the skill
      *
      * @param name
      * @return
      */
     public boolean canUseSkill(String name) {
         return canUseSkill(plugin.getSkillManager().getSkill(name));
     }
 
     /**
      * Checks if the hero has access to the given Skill
      *
      * @param skill
      * @return
      */
     public boolean hasAccessToSkill(Skill skill) {
         return hasAccessToSkill(skill.getName());
     }
 
     /**
      * Checks if the hero has access to the given Skill
      *
      * @param name
      * @return
      */
     public boolean hasAccessToSkill(String name) {
         HeroClass secondClass = getSecondClass();
         return getHeroClass().hasSkill(name) || (secondClass != null && secondClass.hasSkill(name)) || skills.containsKey(name.toLowerCase());
     }
 
     /**
      * Checks if the hero is a master of the given class
      *
      * Thread-safe
      * 
      * @param heroClass
      * @return boolean
      */
     public boolean isMaster(HeroClass heroClass) {
         return getLevel(heroClass) >= heroClass.getMaxLevel();
     }
 
     /**
      * Checks if verbosity is currently disabled for the current skill
      *
      * @param skill
      * @return boolean
      */
     public boolean isSuppressing(Skill skill) {
         return suppressedSkills.containsKey(skill.getName());
     }
 
     /**
      * Checks if verbosity is fully enabled/disabled for the hero
      *
      * @return boolean
      */
     public boolean isVerbose() {
         return verbose.get();
     }
 
     /**
      * @return the delayedSkillTaskId
      */
     public DelayedSkill getDelayedSkill() {
         return delayedSkill;
     }
 
     /**
      * @param delayedSkillTaskId the delayedSkillTaskId to set
      */
     public void setDelayedSkill(DelayedSkill wSkill) {
         this.delayedSkill = wSkill;
     }
 
     /**
      * Cancels the delayed skill task
      */
     public void cancelDelayedSkill() {
         if (delayedSkill == null) {
             return;
         }
         Skill skill = delayedSkill.getSkill();
         delayedSkill = null;
         skill.broadcast(player.getLocation(), "$1 has stopped using $2!", player.getDisplayName(), skill.getName());
     }
 
     public void removeCooldown(String name) {
         cooldowns.remove(name.toLowerCase());
     }
 
     public void manualRemoveEffect(Effect effect) {
         if (effect != null) {
             if (effect instanceof Expirable || effect instanceof Periodic) {
                 plugin.getEffectManager().queueForRemoval(this, effect);
             }
             effects.remove(effect.getName().toLowerCase());
         }
     }
 
     /**
      * This method can NOT be called from an iteration over the effect set
      *
      * @param effect
      */
     public void removeEffect(Effect effect) {
         if (effect != null) {
             if (effect instanceof Expirable || effect instanceof Periodic) {
                 plugin.getEffectManager().queueForRemoval(this, effect);
             }
             effect.remove(this);
             effects.remove(effect.getName().toLowerCase());
         }
     }
 
     /**
      * Removes the given permission from the hero
      *
      * @param permission
      */
     public void removePermission(String permission) {
         transientPerms.unsetPermission(permission);
         player.recalculatePermissions();
     }
 
     /**
      * Removes the given permission from the hero
      *
      * @param permission
      */
     public void removePermission(Permission permission) {
         transientPerms.unsetPermission(permission);
         player.recalculatePermissions();
     }
 
     /**
      * Remove a skill from the hero's skill
      *
      * @param skill
      */
     public void removeSkill(String skill) {
         skills.remove(skill.toLowerCase());
     }
 
     /**
      * Sets the cooldown for a specific skill
      *
      * @param name
      * @param cooldown
      */
     public void setCooldown(String name, long cooldown) {
         cooldowns.put(name.toLowerCase(), cooldown);
     }
 
     /**
      * Sets the hero's experience for the given class to the given value,
      * this method will circumvent the ExpChangeEvent
      *
      * @param heroClass
      * @param experience
      */
     public void setExperience(HeroClass heroClass, double experience) {
         this.experience.put(heroClass.getName(), experience);
     }
 
     /**
      * Sets the heros health, This method circumvents the HeroRegainHealth event
      * if you use it to regain health on a hero please make sure to call the regain health event prior to setHealth.
      *
      * @param health
      */
     public void setHealth(Double health) {
         double maxHealth = getMaxHealth();
         rwl.writeLock().lock();
         if (health > maxHealth) {
             this.health = maxHealth;
         } else if (health < 0) {
             this.health = 0;
         } else {
             this.health = health;
         }
         rwl.writeLock().unlock();
     }
 
     /**
      * Changes the hero to the given class
      *
      * @param heroClass
      */
     public void setHeroClass(HeroClass heroClass, boolean secondary) {
         double currentMaxHP = getMaxHealth();
 
         rwl.writeLock().lock();
         if (secondary) {
             this.secondClass = heroClass;
         } else {
             this.heroClass = heroClass;
         }
         rwl.writeLock().unlock();
 
         double newMaxHP = getMaxHealth();
         double health = getHealth();
         health *= newMaxHP / currentMaxHP;
         if (health > newMaxHP) {
             health = newMaxHP;
         }
         setHealth(health);
         getTieredLevel(true);
         // Check the Players inventory now that they have changed class.
         this.checkInventory();
     }
 
     /**
      * Sets the hero's last damage cause the the given value
      * Generally this should never be called through API as it is updated internally through the heroesdamagelistener
      *
      * @param lastDamageCause
      */
     public void setLastDamageCause(HeroDamageCause lastDamageCause) {
         this.lastDamageCause = lastDamageCause;
     }
 
     /**
      * Sets the heros mana to the given value
      * This circumvents the HeroRegainMana event.
      *
      * @param mana
      */
     public void setMana(int mana) {
         if (mana > 100) {
             mana = 100;
         } else if (mana < 0) {
             mana = 0;
         }
         this.mana.getAndSet(mana);
     }
 
     /**
      * Sets the players current party to the given value
      *
      * @param party
      */
     public void setParty(HeroParty party) {
         this.party = party;
     }
 
     /**
      * sets a single setting in the persistence skill-settings map
      *
      * @param skill
      * @param node
      * @param val
      */
     public void setSkillSetting(Skill skill, String node, Object val) {
         setSkillSetting(skill.getName(), node, val);
     }
 
     /**
      * sets a single setting in the persistence skill-settings map
      *
      * @param skill
      * @param node
      * @param val
      */
     public void setSkillSetting(String skillName, String node, Object val) {
         ConfigurationSection section = persistedSkillSettings.get(skillName.toLowerCase());
         if (section == null) {
             section = new MemoryConfiguration();
             persistedSkillSettings.put(skillName.toLowerCase(), section);
         }
         section.set(node, val);
     }
 
     /**
      * Adds or removes the given Skill from the set of suppressed skills
      *
      * @param skill
      * @param suppressed
      */
     public void setSuppressed(Skill skill, boolean suppressed) {
         if (suppressed) {
             suppressedSkills.put(skill.getName(), true);
         } else {
             suppressedSkills.remove(skill.getName());
         }
     }
 
     public void setSuppressedSkills(Collection<String> suppressedSkills) {
         for (String s : suppressedSkills) {
             this.suppressedSkills.put(s, true);
         }
     }
 
     /**
      * Sets the heros verbosity
      *
      * @param verbose
      */
     public void setVerbose(boolean verbose) {
         this.verbose.getAndSet(verbose);
     }
 
     public HeroClass getEnchantingClass() {
         HeroClass heroClass = getHeroClass();
         HeroClass secondClass = getSecondClass();
         int level = 0;
         if (heroClass.hasExperiencetype(ExperienceType.ENCHANTING)) {
             level = getLevel(heroClass);
         }
 
         if (secondClass != null && secondClass.hasExperiencetype(ExperienceType.ENCHANTING) && getLevel(secondClass) > level) {
             return secondClass;
         }
 
         return level != 0 ? heroClass : null;
     }
 
     /**
      * Syncs the Hero's current Experience with the minecraft experience (should also sync the level bar)
      */
     public void syncExperience() {
         HeroClass secondClass = getSecondClass();
         if (secondClass != null && !syncPrimary) {
             syncExperience(secondClass);
         } else {
             syncExperience(getHeroClass());        
         }
     }
 
     /**
      * Syncs the experience bar with the client from the given class
      *
      * @param hc
      */
     public void syncExperience(HeroClass hc) {
         int level = getLevel(hc);
         int currentLevelXP = Properties.getTotalExp(level);
 
         double maxLevelXP = Properties.getTotalExp(level + 1) - currentLevelXP;
         double currentXP = getExperience(hc) - currentLevelXP;
         float syncedPercent = (float) (currentXP / maxLevelXP);
 
         player.setTotalExperience(Util.getMCExperience(level));
         player.setExp(syncedPercent);
         player.setLevel(level);
     }
 
     /**
      * Syncs the Heros current health with the Minecraft HealthBar
      */
     public void syncHealth() {
         double health = getHealth();
         if ((player.isDead() || player.getHealth() == 0) && health <= 0) {
             return;
         }
 
         int playerHealth = (int) (health / getMaxHealth() * 20);
         if (playerHealth == 0 && health > 0) {
             playerHealth = 1;
         }
         player.setHealth(playerHealth);
     }
 
     /**
      * Unbinds the material from a skill.
      *
      * @param material
      */
     public void unbind(Material material) {
         binds.remove(material);
     }
 
     public void checkInventory() {
         if (player.getGameMode() == GameMode.CREATIVE || Heroes.properties.disabledWorlds.contains(player.getWorld().getName())) {
             return;
         }
         int removedCount = checkArmorSlots();
 
         for (int i = 0; i < 9; i++) {
             if (canEquipItem(i)) {
                 continue;
             }
 
             removedCount++;
         }
         // If items were removed from the Players inventory then we need to alert them of such event and re-sync their
         // inventory
         if (removedCount > 0) {
             Messaging.send(player, "$1 have been removed from your inventory due to class restrictions.", removedCount + " Items");
             Util.syncInventory(player, plugin);
         }
     }
 
     public int checkArmorSlots() {
         PlayerInventory inv = player.getInventory();
         Material item;
         int removedCount = 0;
 
         HeroClass heroClass = getHeroClass();
         HeroClass secondClass = getSecondClass();
         if (inv.getHelmet() != null && inv.getHelmet().getTypeId() != 0) {
             item = inv.getHelmet().getType();
             if (!Util.isArmor(item) && Heroes.properties.allowHats && (Heroes.properties.hatsLevel <= getLevel(heroClass) || (secondClass != null && Heroes.properties.hatsLevel <= getLevel(secondClass)))) {
                 // Do nothing!  
             } else if (!heroClass.isAllowedArmor(item) && (secondClass == null || !secondClass.isAllowedArmor(item))) {
                 Util.moveItem(this, -1, inv.getHelmet());
                 inv.setHelmet(null);
                 removedCount++;
             }
         }
 
         if (inv.getChestplate() != null && inv.getChestplate().getTypeId() != 0) {
             item = inv.getChestplate().getType();
             if (!heroClass.isAllowedArmor(item) && (secondClass == null || !secondClass.isAllowedArmor(item))) {
                 Util.moveItem(this, -1, inv.getChestplate());
                 inv.setChestplate(null);
                 removedCount++;
             }
         }
 
         if (inv.getLeggings() != null && inv.getLeggings().getTypeId() != 0) {
             item = inv.getLeggings().getType();
             if (!heroClass.isAllowedArmor(item) && (secondClass == null || !secondClass.isAllowedArmor(item))) {
                 Util.moveItem(this, -1, inv.getLeggings());
                 inv.setLeggings(null);
                 removedCount++;
             }
         }
         if (inv.getBoots() != null && inv.getBoots().getTypeId() != 0) {
             item = inv.getBoots().getType();
             if (!heroClass.isAllowedArmor(item) && (secondClass == null || !secondClass.isAllowedArmor(item))) {
                 Util.moveItem(this, -1, inv.getBoots());
                 inv.setBoots(null);
                 removedCount++;
             }
         }
         return removedCount;
     }
 
     public boolean canEquipItem(int slot) {
         if (Heroes.properties.disabledWorlds.contains(player.getWorld().getName())) {
             return true;
         }
 
         ItemStack itemStack = player.getInventory().getItem(slot);
         if (itemStack == null) {
             return true;
         }
         HeroClass secondClass = getSecondClass();
         Material itemType = itemStack.getType();
         if (!Util.isWeapon(itemType)) {
             return true;
         } else if (getHeroClass().isAllowedWeapon(itemType) || (secondClass != null && secondClass.isAllowedWeapon(itemType))) {
             return true;
         } else {
             Util.moveItem(this, slot, itemStack);
             return false;
         }
     }
 
     /**
      * Checks if the player is able to craft the object specified
      *
      * @param o - should be an ItemStack, ItemData, or Material
      * @return true if the class can craft the item
      */
     public boolean canCraft(Object o) {
         HeroClass heroClass = getHeroClass();
         int level = heroClass.getCraftLevel(o);
         if (level != -1 && level <= getLevel(heroClass)) {
             return true;
         }
 
         HeroClass secondClass = getSecondClass();
         if (secondClass != null) {
             level = secondClass.getCraftLevel(o);
             if (level != -1 && level <= getLevel(secondClass)) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean isSyncPrimary() {
         return syncPrimary;
     }
 
     public void setSyncPrimary(boolean syncPrimary) {
         this.syncPrimary = syncPrimary || getSecondClass() == null;
         syncExperience();
     }
 }
