 package com.herocraftonline.dev.heroes.persistence;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ExperienceChangeEvent;
 import com.herocraftonline.dev.heroes.api.HeroChangeLevelEvent;
 import com.herocraftonline.dev.heroes.api.HeroDamageCause;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class Hero {
 
     private static final DecimalFormat decFormat = new DecimalFormat("#0.##");
 
     protected final Heroes plugin;
     protected Player player;
     protected HeroClass heroClass;
     protected int mana = 0;
     protected HeroParty party = null;
     protected boolean verbose = true;
     protected HeroDamageCause lastDamageCause = null;
     protected Set<Effect> effects = new HashSet<Effect>();
     protected Map<String, Double> experience = new HashMap<String, Double>();
     protected Map<String, Long> cooldowns = new HashMap<String, Long>();
     protected Set<Creature> summons = new HashSet<Creature>();
     protected Map<Material, String[]> binds = new HashMap<Material, String[]>();
     protected List<ItemStack> itemRecovery = new ArrayList<ItemStack>();
     protected Set<String> suppressedSkills = new HashSet<String>();
     protected Map<String, Map<String, String>> skillSettings = new HashMap<String, Map<String, String>>();
 
     private Map<String, ConfigurationNode> skills = new HashMap<String, ConfigurationNode>();
     protected double health;
 
     public Hero(Heroes plugin, Player player, HeroClass heroClass) {
         this.plugin = plugin;
         this.player = player;
         this.heroClass = heroClass;
     }
 
     /**
      * Syncs the Heros current health with the Minecraft HealthBar
      */
     public void syncHealth() {
        getPlayer().setHealth((int) (health / getMaxHealth() * 20));
     }
 
     /**
      * Adds the Effect onto the hero, and calls it's apply method initiating it's first tic.
      * 
      * @param effect
      */
     public void addEffect(Effect effect) {
         effects.add(effect);
         effect.apply(this);
     }
 
     public void addRecoveryItem(ItemStack item) {
         this.itemRecovery.add(item);
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
      * Changes the hero's current class to the given class then clears all binds
      * 
      * @param heroClass
      */
     public void changeHeroClass(HeroClass heroClass) {
         setHeroClass(heroClass);
         binds.clear();
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
         } else if (!player.getName().equals(other.player.getName())) {
             return false;
         }
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
      * @param expChange - amount of base exp to add
      * @param source
      * @param boolean - distributeToParty
      */
     public void gainExp(double expChange, ExperienceType source, boolean distributeToParty) {
         Properties prop = plugin.getConfigManager().getProperties();
 
         if (prop.disabledWorlds.contains(player.getWorld().getName()))
             return;
         
         if (distributeToParty && party != null && party.getExp() && expChange > 0) {
             Location location = player.getLocation();
 
             Set<Hero> partyMembers = party.getMembers();
             Set<Hero> inRangeMembers = new HashSet<Hero>();
             for (Hero partyMember : partyMembers) {
                 if (!location.getWorld().equals(partyMember.player.getLocation().getWorld()))
                     continue;
 
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
 
         double exp = getExperience();
         
         // adjust exp using the class modifier if it's positive
         if (expChange > 0 && source != ExperienceType.ADMIN) {
             expChange *= heroClass.getExpModifier();
         } else if (source != ExperienceType.ADMIN && getLevel() >= prop.maxLevel && (!prop.masteryLoss || !prop.levelsViaExpLoss)) {
             return;
         }
         
         // call event
         ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, expChange, source);
         plugin.getServer().getPluginManager().callEvent(expEvent);
         if (expEvent.isCancelled()) {
             return;
         }
         
         //Lets get our modified xp change value
         expChange = expEvent.getExpChange();
         
         int currentLevel = prop.getLevel(exp);
         int newLevel = prop.getLevel(exp + expChange);
         
         if (currentLevel >= prop.maxLevel && expChange > 0) {
             expChange = 0;
         } else if (currentLevel > newLevel && !prop.levelsViaExpLoss && source != ExperienceType.ADMIN) {
             expChange = prop.getExperience(currentLevel) - (exp - 1);
         }
 
         // add the experience
         exp += expChange;
                
         //If we went negative lets reset our values so that we would hit 0
         if (exp < 0) {
             expChange = -(expChange + exp);
             exp = 0;
         }
         
         //Reset our new level - in case xp adjustement settings actually don't cause us to change
         newLevel = prop.getLevel(exp);
         setExperience(exp);
         
         // notify the user
         if (expChange != 0) {
             if (verbose && expChange > 0) {
                 Messaging.send(player, "$1: Gained $2 Exp", heroClass.getName(), decFormat.format(expChange));
             } else if ( verbose && expChange < 0) {
                 Messaging.send(player, "$1: Lost $2 Exp", heroClass.getName(), decFormat.format(-expChange));
             }
             if (newLevel != currentLevel) {
                 HeroChangeLevelEvent hLEvent = new HeroChangeLevelEvent(this, currentLevel, newLevel);
                 plugin.getServer().getPluginManager().callEvent(hLEvent);
                 if (newLevel >= prop.maxLevel) {
                     setExperience(prop.getExperience(prop.maxLevel));
                     Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), heroClass.getName());
                     plugin.getHeroManager().saveHero(player);
                 }
                 if (newLevel > currentLevel) {
                     plugin.getSpoutUI().sendPlayerNotification(player, ChatColor.GOLD + "Level Up!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                     Messaging.send(player, "You gained a level! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                     setHealth(getMaxHealth());
                     syncHealth();
                 } else {
                     plugin.getSpoutUI().sendPlayerNotification(player, ChatColor.GOLD + "Level Lost!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                     Messaging.send(player, "You lost a level! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                 }
             }
         }
 
         
         // Save the hero file when the Hero changes levels to prevent rollback issues
         if (newLevel != currentLevel)
             plugin.getHeroManager().saveHero(player);
     }
 
     /**
      * Gets the Map of all Bindings
      * 
      * @return
      */
     public Map<Material, String[]> getBinds() {
         return binds;
     }
 
     /**
      * Gets the Map of all cooldowns
      * 
      * @return
      */
     public Map<String, Long> getCooldowns() {
         return cooldowns;
     }
 
     /**
      * Attempts to find the effect from the given name
      * 
      * @param name
      * @return the Effect with the name - or null if not found
      */
     public Effect getEffect(String name) {
         for (Effect effect : effects) {
             if (effect.getName().equalsIgnoreCase(name)) {
                 return effect;
             }
         }
         return null;
     }
 
     /**
      * get a Clone of all effects active on the hero
      * 
      * @return
      */
     public Set<Effect> getEffects() {
         return new HashSet<Effect>(effects);
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
 
     /**
      * Returns the hero's currently selected heroclass
      * 
      * @return heroclass
      */
     public HeroClass getHeroClass() {
         return heroClass;
     }
 
     /**
      * 
      * @return the level of the character - int
      */
     public int getLevel() {
         return plugin.getConfigManager().getProperties().getLevel(getExperience());
     }
 
     /**
      * 
      * @return the hero's current health - double
      */
     public double getHealth() {
         return health;
     }
 
     /**
      * Maximum health is derived from the hero's class. It is the classes base max hp + hp per level.
      * 
      * @return the hero's maximum health
      */
     public double getMaxHealth() {
         int level = plugin.getConfigManager().getProperties().getLevel(getExperience());
         return heroClass.getBaseMaxHealth() + (level - 1) * heroClass.getMaxHealthPerLevel();
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
 
     public List<ItemStack> getRecoveryItems() {
         return this.itemRecovery;
     }
 
     /**
      * 
      * @return set of all summons the hero currently has
      */
     public Set<Creature> getSummons() {
         return summons;
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
 
     /**
      * Returns the currently suppressed skills
      * For use with verbosity
      * 
      * @return
      */
     public Set<String> getSuppressedSkills() {
         return new HashSet<String>(suppressedSkills);
     }
 
     /**
      * Checks if the hero currently has the Effect with the given name.
      * 
      * @param name
      * @return boolean
      */
     public boolean hasEffect(String name) {
         for (Effect effect : effects) {
             if (effect.getName().equalsIgnoreCase(name)) {
                 return true;
             }
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
         int maxExp = plugin.getConfigManager().getProperties().maxExp;
         return getExperience(heroClass) >= maxExp || getExperience(heroClass) - maxExp > 0;
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
      * Iterates over the effects this Hero has and removes them
      * 
      */
     public void clearEffects() {
         Iterator<Effect> iter = effects.iterator();
         while (iter.hasNext()) {
             iter.next().remove(this);
             iter.remove();
         }
     }
 
     /**
      * This method can NOT be called from an iteration over the effect set
      * 
      * @param effect
      */
     public void removeEffect(Effect effect) {
         effects.remove(effect);
         if (effect != null) {
             effect.remove(this);
         }
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
      * Clears all experience for all classes on the hero
      * 
      */
     public void clearExperience() {
         for (Entry<String, Double> entry : experience.entrySet()) {
             entry.setValue(0.0);
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
         this.plugin.getInventoryChecker().checkInventory(player);
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
 
     public void setRecoveryItems(List<ItemStack> items) {
         this.itemRecovery = items;
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
         if (!heroClass.hasSkill(skillName)) {
             return null;
         }
 
         return skillSettings.get(skillName.toLowerCase());
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
      * Sets the heros verbosity
      * 
      * @param verbose
      */
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     /**
      * Unbinds the material from a skill.
      * 
      * @param material
      */
     public void unbind(Material material) {
         binds.remove(material);
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
      * Checks if the hero has access to the given Skill
      * 
      * @param name
      * @return
      */
     public boolean hasSkill(String name) {
         if(this.heroClass.hasSkill(name) || hasHeroSkill(name)) {
             return true;
         }else {
             return false;
         }
     }
     
     /**
      * Checks if the hero has access to the given Skill
      * 
      * @param skill
      * @return
      */
     public boolean hasSkill(Skill skill) {
         String name = skill.getName();
         if(this.heroClass.hasSkill(name) || hasHeroSkill(name)) {
             return true;
         }else {
             return false;
         }
     }
 
     public Map<String, ConfigurationNode> getSkills() {
         return skills;
     }
 
     public void addSkill(String skill) {
         skills.put(skill, Configuration.getEmptyNode());
     }
     
     public void removeSkill(String skill) {
         skills.remove(skill);
     }
     
     public boolean hasHeroSkill(String skill) {
         return skills.containsKey(skill);
     }
 
     public HeroDamageCause getLastDamageCause() {
         return lastDamageCause;
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
 }
