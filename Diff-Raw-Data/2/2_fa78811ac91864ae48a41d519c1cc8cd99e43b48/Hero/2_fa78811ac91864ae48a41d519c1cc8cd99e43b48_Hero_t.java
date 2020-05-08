 package com.herocraftonline.dev.heroes.hero;
 
 import java.text.DecimalFormat;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.minecraft.server.EntityPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ExperienceChangeEvent;
 import com.herocraftonline.dev.heroes.api.HeroChangeLevelEvent;
 import com.herocraftonline.dev.heroes.api.HeroDamageCause;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponItems;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.spout.SpoutUI;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class Hero {
 
     private static final DecimalFormat decFormat = new DecimalFormat("#0.##");
 
     private final Heroes plugin;
     private Player player;
     private HeroClass heroClass;
     private HeroClass secondClass;
     private int mana = 0;
     private HeroParty party = null;
     private boolean verbose = true;
     private HeroDamageCause lastDamageCause = null;
     private Map<String, Effect> effects = new HashMap<String, Effect>();
     private Map<String, Double> experience = new HashMap<String, Double>();
     private Map<String, Long> cooldowns = new HashMap<String, Long>();
     private Set<Creature> summons = new HashSet<Creature>();
     private Map<Material, String[]> binds = new EnumMap<Material, String[]>(Material.class);
     private Set<String> suppressedSkills = new HashSet<String>();
     private Map<String, Map<String, String>> skillSettings = new HashMap<String, Map<String, String>>();
     private Map<String, ConfigurationNode> skills = new HashMap<String, ConfigurationNode>();
     private int delayedSkillTaskId = -1;
     private double health;
     private PermissionAttachment transientPerms;
 
     public Hero(Heroes plugin, Player player, HeroClass heroClass) {
         this.plugin = plugin;
         this.player = player;
         this.heroClass = heroClass;
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
 
     public void addSkill(String skill) {
         skills.put(skill, Configuration.getEmptyNode());
     }
 
     public boolean hasExperienceType(ExperienceType type) {
        return heroClass.hasExperiencetype(type) || (secondClass != null && secondClass.hasExperiencetype(type));
     }
 
     /**
      * Adds a skill binding to the given Material.
      * Ignores Air/Null values
      * 
      * @param material
      * @param skillName
      */
     public void bind(Material material, String[] skillName) {
         if (material == Material.AIR || material == null)
             return;
 
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
 
         if (!secondary) 
             setHeroClass(heroClass);
         else
             setSecondClass(heroClass);
 
         if (plugin.getConfigManager().getProperties().prefixClassName) {
             player.setDisplayName("[" + getHeroClass().getName() + "]" + player.getName());
         }
         plugin.getHeroManager().performSkillChecks(this);
     }
 
     public void clearBinds() {
         binds.clear();
     }
 
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
      */
     public void clearExperience() {
         for (Entry<String, Double> entry : experience.entrySet()) {
             entry.setValue(0.0);
         }
     }
 
     /**
      * Clears all set Permissions on the hero's permission attachment
      */
     public void clearPermissions() {
         player.removeAttachment(transientPerms);
         if (plugin.isEnabled())
             transientPerms = player.addAttachment(plugin);
     }
 
     /**
      * Removes the summons from the game world - then removes them from the set
      * 
      */
     public void clearSummons() {
         for (Creature summon : summons) {
             summon.remove();
         }
         summons.clear();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Hero other = (Hero) obj;
         if (player == null) {
             if (other.player != null)
                 return false;
         } else if (!player.getName().equals(other.player.getName()))
             return false;
         return true;
     }
 
     /**
      * Standard Experience gain Call - automatically splits the gain between party members
      * expChange supports negative values for experience loss.
      * 
      * @param expGain
      * @param source
      */
     public void gainExp(double expGain, ExperienceType source) {
         gainExp(expGain, source, true);
     }
 
     /**
      * Adds the specified experience to the hero before modifiers from the given source.
      * expChange value supports negatives for experience loss.
      * 
      * @param expChange
      *            - amount of base exp to add
      * @param source
      * @param boolean - distributeToParty
      */
     public void gainExp(double expChange, ExperienceType source, boolean distributeToParty) {
         if (player.getGameMode() == GameMode.CREATIVE || plugin.getConfigManager().getProperties().disabledWorlds.contains(player.getWorld().getName()))
             return;
         Properties prop = plugin.getConfigManager().getProperties();
 
         if (prop.disabledWorlds.contains(player.getWorld().getName()))
             return;
 
         if (distributeToParty && party != null && party.getExp() && expChange > 0) {
             Location location = player.getLocation();
 
             Set<Hero> partyMembers = party.getMembers();
             Set<Hero> inRangeMembers = new HashSet<Hero>();
             for (Hero partyMember : partyMembers) {
                 if (!location.getWorld().equals(partyMember.player.getLocation().getWorld())) {
                     continue;
                 }
 
                 if (location.distanceSquared(partyMember.player.getLocation()) <= 2500) {
                     inRangeMembers.add(partyMember);
                 }
             }
 
             int partySize = inRangeMembers.size();
             double sharedExpGain = expChange / partySize * ((partySize - 1) * prop.partyBonus + 1.0);
 
             for (Hero partyMember : inRangeMembers) {
                 partyMember.gainExp(sharedExpGain, source, false);
             }
 
             return;
         }
 
         HeroClass[] classes = new HeroClass[] {heroClass, secondClass};
 
         for (HeroClass hc : classes) {
             if (hc == null)
                 continue;
 
             double exp = getExperience(hc);
 
             // adjust exp using the class modifier if it's positive
             if (expChange > 0 && source != ExperienceType.ADMIN) {
                 expChange *= hc.getExpModifier();
             } else if (source != ExperienceType.ADMIN && isMaster() && (!prop.masteryLoss || !prop.levelsViaExpLoss))
                 return;
 
             //This is called once for each class
             ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, hc, expChange, source);
             plugin.getServer().getPluginManager().callEvent(expEvent);
             if (expEvent.isCancelled())
                 return;
 
             // Lets get our modified xp change value
             expChange = expEvent.getExpChange();
 
             int currentLevel = prop.getLevel(exp);
             int newLevel = prop.getLevel(exp + expChange);
 
             if (isMaster(hc) && source != ExperienceType.ADMIN) {
                 expChange = 0;
             } else if (currentLevel > newLevel && !prop.levelsViaExpLoss && source != ExperienceType.ADMIN) {
                 expChange = prop.getExperience(currentLevel) - (exp - 1);
             }
 
             // add the experience
             exp += expChange;
 
             // If we went negative lets reset our values so that we would hit 0
             if (exp < 0) {
                 expChange = -(expChange + exp);
                 exp = 0;
             }
 
             // Reset our new level - in case xp adjustement settings actually don't cause us to change
             newLevel = prop.getLevel(exp);
             setExperience(hc, exp);
 
             // notify the user
             if (expChange != 0) {
                 if (verbose && expChange > 0) {
                     Messaging.send(player, "$1: Gained $2 Exp", heroClass.getName(), decFormat.format(expChange));
                 } else if (verbose && expChange < 0) {
                     Messaging.send(player, "$1: Lost $2 Exp", heroClass.getName(), decFormat.format(-expChange));
                 }
                 if (newLevel != currentLevel) {
                     HeroChangeLevelEvent hLEvent = new HeroChangeLevelEvent(this, currentLevel, newLevel);
                     plugin.getServer().getPluginManager().callEvent(hLEvent);
                     if (newLevel >= hc.getMaxLevel()) {
                         setExperience(prop.getExperience(hc.getMaxLevel()));
                         Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), hc.getName());
                     }
                     if (newLevel > currentLevel) {
                         SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Up!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                         Messaging.send(player, "You gained a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
                         setHealth(getMaxHealth());
                         //Reset food stuff on level up
                         if (player.getFoodLevel() < 20)
                             player.setFoodLevel(20);
 
                         player.setSaturation(20);
                         player.setExhaustion(0);
                         syncHealth();
                     } else {
                         SpoutUI.sendPlayerNotification(player, ChatColor.GOLD + "Level Lost!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                         Messaging.send(player, "You lost a level! (Lvl $1 $2)", String.valueOf(newLevel), hc.getName());
                     }
                 }
             }
 
             // Save the hero file when the Hero changes levels to prevent rollback issues
             if (newLevel != currentLevel) {
                 plugin.getHeroManager().saveHero(this);
             }
         }
         syncExperience();
     }
 
     public String[] getBind(Material mat) {
         return binds.get(mat);
     }
 
     /**
      * Gets the Map of all Bindings
      * 
      * @return
      */
     public Map<Material, String[]> getBinds() {
         return Collections.unmodifiableMap(binds);
     }
 
     public Long getCooldown(String name) {
         return cooldowns.get(name.toLowerCase());
     }
 
     /**
      * Gets the Map of all cooldowns
      * 
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
      * @return double experience
      */
     public double getExperience() {
         return getExperience(heroClass);
     }
 
     /**
      * Get the hero's experience in the given class
      * 
      * @param heroClass
      * @return double experience
      */
     public double getExperience(HeroClass heroClass) {
         Double exp = experience.get(heroClass.getName());
         return exp == null ? 0 : exp;
     }
 
     public Map<String, Double> getExperienceMap() {
         return Collections.unmodifiableMap(experience);
     }
 
     /**
      * 
      * @return the hero's current health - double
      */
     public double getHealth() {
         return health;
     }
 
     /**
      * Returns the hero's currently selected heroclass
      * 
      * @return heroclass
      */
     public HeroClass getHeroClass() {
         return heroClass;
     }
 
     public HeroDamageCause getLastDamageCause() {
         return lastDamageCause;
     }
 
     /**
      * 
      * @return the level of the character - int
      */
     public int getLevel() {
         return plugin.getConfigManager().getProperties().getLevel(getExperience());
     }
 
     public int getLevel(HeroClass heroClass) {
         return plugin.getConfigManager().getProperties().getLevel(getExperience(heroClass));
     }
 
     /**
      * Gets the tier adjusted level for this character - takes into account already gained levels on parent classes
      * @return
      */
     public int getTieredLevel() {
         if (heroClass.hasNoParents())
             return getLevel();
 
         Set<HeroClass> classes = new HashSet<HeroClass>();
         for (HeroClass hClass : heroClass.getParents()) {
             if (this.isMaster(hClass)) {
                 classes.addAll(getTieredLevel(hClass, new HashSet<HeroClass>(classes)));
                 classes.add(hClass);
             }
         }
         int level = getLevel();
         for (HeroClass hClass : classes) {
             level += getLevel(hClass);
         }
         return level;
     }
 
     /**
      * recursive method to lookup all classes that are upstream of the parent class and mastered
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
         return secondClass;
     }
 
     /**
      * @param secondClass the secondClass to set
      */
     public void setSecondClass(HeroClass secondClass) {
         //TODO: need to adjust class change methods to resolve proper abilities/HP after setting a new class
         this.secondClass = secondClass;
     }
 
     /**
      * All mana is in percentages.
      * 
      * @return Hero's current amount of mana
      */
     public int getMana() {
         return mana;
     }
 
     /**
      * Maximum health is derived from the hero's class. It is the classes base max hp + hp per level.
      * 
      * @return the hero's maximum health
      */
     public double getMaxHealth() {
         int level = plugin.getConfigManager().getProperties().getLevel(getExperience(heroClass));
         double primaryHp = heroClass.getBaseMaxHealth() + (level - 1) * heroClass.getMaxHealthPerLevel();
         double secondHp = 0;
         if (secondClass != null) {
             level = plugin.getConfigManager().getProperties().getLevel(getExperience(secondClass));
             secondHp = secondClass.getBaseMaxHealth() + (level - 1) * secondClass.getMaxHealthPerLevel();
         }
         return primaryHp > secondHp ? primaryHp : secondHp;
     }
 
     /**
      * Gets the hero's current party - returns null if the hero has no party
      * 
      * @return HeroParty
      */
     public HeroParty getParty() {
         return party;
     }
 
     /**
      * 
      * @return player associated with this hero
      */
     public Player getPlayer() {
         return player;
     }
 
     public Map<String, ConfigurationNode> getSkills() {
         return skills;
     }
 
     public Map<String, Map<String, String>> getSkillSettings() {
         return Collections.unmodifiableMap(skillSettings);
     }
 
     /**
      * gets Mapping of the persistence SkillSettings for the given skill
      * 
      * @param skill
      * @return
      */
     public Map<String, String> getSkillSettings(Skill skill) {
         return skill == null ? null : getSkillSettings(skill.getName());
     }
 
     /**
      * gets Mapping of the persistence SkillSettings for the given skillName
      * 
      * @param skill
      * @return
      */
     public Map<String, String> getSkillSettings(String skillName) {
         if (!heroClass.hasSkill(skillName))
             return null;
 
         return skillSettings.get(skillName.toLowerCase());
     }
 
     /**
      * 
      * @return set of all summons the hero currently has
      */
     public Set<Creature> getSummons() {
         return summons;
     }
 
     /**
      * Returns the currently suppressed skills
      * For use with verbosity
      * 
      * @return
      */
     public Set<String> getSuppressedSkills() {
         return Collections.unmodifiableSet(suppressedSkills);
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
             if (effect.isType(type))
                 return true;
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return player == null ? 0 : player.getName().hashCode();
     }
 
     /**
      * 
      * @return if the player has a party
      */
     public boolean hasParty() {
         return party != null;
     }
 
     /**
      * Checks if the hero has access to the given Skill
      * 
      * @param skill
      * @return
      */
     public boolean hasSkill(Skill skill) {
         return hasSkill(skill.getName());
     }
 
     /**
      * Checks if the hero has access to the given Skill
      * 
      * @param name
      * @return
      */
     public boolean hasSkill(String name) {
         return this.heroClass.hasSkill(name) || skills.containsKey(name);
     }
 
     /**
      * 
      * @return if the hero is a master of his current class (max level)
      */
     public boolean isMaster() {
         return isMaster(heroClass);
     }
 
     /**
      * Checks if the hero is a master of the given class
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
         return suppressedSkills.contains(skill.getName());
     }
 
     /**
      * Checks if verbosity is fully enabled/disabled for the hero
      * 
      * @return boolean
      */
     public boolean isVerbose() {
         return verbose;
     }
 
     /**
      * @return the delayedSkillTaskId
      */
     public int getDelayedSkillTaskId() {
         return delayedSkillTaskId;
     }
 
     /**
      * @param delayedSkillTaskId
      *            the delayedSkillTaskId to set
      */
     public void setDelayedSkillTaskId(int delayedSkillTaskId) {
         this.delayedSkillTaskId = delayedSkillTaskId;
     }
 
     /**
      * Cancels the delayed skill task
      */
     public void cancelDelayedSkill() {
         if (delayedSkillTaskId == -1)
             return;
 
         plugin.getServer().getScheduler().cancelTask(delayedSkillTaskId);
         Skill skill = ActiveSkill.getDelayedSkill(player);
         skill.broadcast(player.getLocation(), "$1 has stopped using $2!", player.getDisplayName(), skill.getName());
         delayedSkillTaskId = -1;
         ActiveSkill.removeDelayedSkill(player);
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
 
     public void removeSkill(String skill) {
         skills.remove(skill);
     }
 
     public void setCooldown(String name, long cooldown) {
         cooldowns.put(name.toLowerCase(), cooldown);
     }
 
     /**
      * Sets the hero's experience to the given value - this circumvents the standard Exp change event
      * 
      * @param experience
      */
     public void setExperience(double experience) {
         setExperience(heroClass, experience);
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
         if (health > maxHealth) {
             this.health = maxHealth;
         } else if (health < 0) {
             this.health = 0;
         } else {
             this.health = health;
         }
     }
 
     /**
      * Changes the hero to the given class
      * 
      * @param heroClass
      */
     public void setHeroClass(HeroClass heroClass) {
         double currentMaxHP = getMaxHealth();
         this.heroClass = heroClass;
         double newMaxHP = getMaxHealth();
         health *= newMaxHP / currentMaxHP;
         if (health > newMaxHP) {
             health = newMaxHP;
         }
 
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
         this.mana = mana;
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
         Map<String, String> settings = skillSettings.get(skillName.toLowerCase());
         if (settings == null) {
             settings = new HashMap<String, String>();
             skillSettings.put(skillName.toLowerCase(), settings);
         }
         settings.put(node, val.toString());
     }
 
     /**
      * Adds or removes the given Skill from the set of suppressed skills
      * 
      * @param skill
      * @param suppressed
      */
     public void setSuppressed(Skill skill, boolean suppressed) {
         if (suppressed) {
             suppressedSkills.add(skill.getName());
         } else {
             suppressedSkills.remove(skill.getName());
         }
     }
 
     public void setSuppressedSkills(Set<String> suppressedSkills) {
         this.suppressedSkills = suppressedSkills;
     }
 
     /**
      * Sets the heros verbosity
      * 
      * @param verbose
      */
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     /**
      * Syncs the Hero's current Experience with the minecraft experience
      */
     public void syncExperience() {
         Properties props = plugin.getConfigManager().getProperties();
         int level = getLevel();
         int currentLevelXP = props.getExperience(level);
 
         double maxLevelXP = props.getExperience(level + 1) - currentLevelXP;
         double currentXP = getExperience() - currentLevelXP;
         int syncedXP = (int) (currentXP / maxLevelXP * 100);
 
         CraftPlayer craftPlayer = (CraftPlayer) player;
         EntityPlayer entityPlayer = craftPlayer.getHandle();
         entityPlayer.exp = 0;
         entityPlayer.expTotal = 0;
         entityPlayer.expLevel = 0;
         entityPlayer.d(450 + syncedXP);
     }
 
     /**
      * Syncs the Heros current health with the Minecraft HealthBar
      */
     public void syncHealth() {
         if ((player.isDead() || player.getHealth() == 0) && health <= 0)
             return;
 
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
         if (player.getGameMode() == GameMode.CREATIVE || plugin.getConfigManager().getProperties().disabledWorlds.contains(player.getWorld().getName()))
             return;
         int removedCount = checkArmorSlots();
 
         for (int i = 0; i < 9; i++) {
             if (canEquipItem(i))
                 continue;
 
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
         String item;
         int removedCount = 0;
 
         if (inv.getHelmet() != null && inv.getHelmet().getTypeId() != 0 && !plugin.getConfigManager().getProperties().allowHats) {
             item = inv.getHelmet().getType().toString();
             if (!heroClass.getAllowedArmor().contains(item) && !heroClass.getAllowedArmor().contains("*")) {
                 Util.moveItem(this, -1, inv.getHelmet());
                 inv.setHelmet(null);
                 removedCount++;
 
             }
         }
         if (inv.getChestplate() != null && inv.getChestplate().getTypeId() != 0) {
             item = inv.getChestplate().getType().toString();
             if (!heroClass.getAllowedArmor().contains(item) && !heroClass.getAllowedArmor().contains("*")) {
                 Util.moveItem(this, -1, inv.getChestplate());
                 inv.setChestplate(null);
                 removedCount++;
             }
         }
 
         if (inv.getLeggings() != null && inv.getLeggings().getTypeId() != 0) {
             item = inv.getLeggings().getType().toString();
             if (!heroClass.getAllowedArmor().contains(item) && !heroClass.getAllowedArmor().contains("*")) {
                 Util.moveItem(this, -1, inv.getLeggings());
                 inv.setLeggings(null);
                 removedCount++;
             }
         }
         if (inv.getBoots() != null && inv.getBoots().getTypeId() != 0) {
             item = inv.getBoots().getType().toString();
             if (!heroClass.getAllowedArmor().contains(item) && !heroClass.getAllowedArmor().contains("*")) {
                 Util.moveItem(this, -1, inv.getBoots());
                 inv.setBoots(null);
                 removedCount++;
             }
         }
         return removedCount;
     }
 
     public boolean canEquipItem(int slot) {
         if (plugin.getConfigManager().getProperties().disabledWorlds.contains(player.getWorld().getName()))
             return true;
 
         ItemStack itemStack = player.getInventory().getItem(slot);
         String itemType = itemStack.getType().toString();
         if (!itemType.equalsIgnoreCase("BOW")) {
             try {
                 WeaponItems.valueOf(itemType.substring(itemType.indexOf("_") + 1, itemType.length()));
             } catch (IllegalArgumentException e1) {
                 return true;
             }
         }
         if (!heroClass.getAllowedWeapons().contains(itemType) && !heroClass.getAllowedWeapons().contains("*")) {
             Util.moveItem(this, slot, itemStack);
             return false;
         }
         return true;
     }
 }
