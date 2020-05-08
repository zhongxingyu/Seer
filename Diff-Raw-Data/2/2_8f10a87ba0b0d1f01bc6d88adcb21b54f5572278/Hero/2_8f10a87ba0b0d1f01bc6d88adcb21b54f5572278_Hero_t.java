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
 import com.herocraftonline.dev.heroes.api.HeroLevelEvent;
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
 
     public void syncHealth() {
         getPlayer().setHealth((int) (health / getMaxHealth() * 20));
     }
 
     public void addEffect(Effect effect) {
         effects.add(effect);
         effect.apply(this);
     }
 
     public void addRecoveryItem(ItemStack item) {
         this.itemRecovery.add(item);
     }
 
     public void bind(Material material, String[] skillName) {
         binds.put(material, skillName);
     }
 
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
 
     public void gainExp(double expGain, ExperienceType source) {
         gainExp(expGain, source, true);
     }
 
     public void gainExp(double expChange, ExperienceType source, boolean distributeToParty) {
         Properties prop = plugin.getConfigManager().getProperties();
 
         if (prop.disabledWorlds.contains(player.getWorld().getName()))
             return;
         
         if (distributeToParty && party != null && party.getExp() && expChange > 0) {
             Location location = getPlayer().getLocation();
 
             Set<Hero> partyMembers = party.getMembers();
             Set<Hero> inRangeMembers = new HashSet<Hero>();
             for (Hero partyMember : partyMembers) {
                 if (!location.getWorld().equals(partyMember.getPlayer().getLocation().getWorld()))
                     continue;
 
                 if (location.distanceSquared(partyMember.getPlayer().getLocation()) <= 2500) {
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
         if (expChange > 0) {
             expChange *= heroClass.getExpModifier();
         } else if (expChange < 0 && !prop.levelsViaExpLoss){
             double currentLevelExp = prop.getExperience(getLevel());
             if (exp + expChange < currentLevelExp) {
                 exp = currentLevelExp;
             }
         }
         
         int currentLevel = prop.getLevel(exp);
         int newLevel = prop.getLevel(exp + expChange);
         if (currentLevel >= prop.maxLevel) {
             expChange = 0;
         } else if (currentLevel > newLevel && !prop.levelsViaExpLoss) {
             expChange = (exp + expChange) - prop.getExperience(currentLevel);
         }
 
         // add the experience
         exp += expChange;
         
         //If we went negative lets reset our values so that we would hit 0
         if (exp < 0) {
             expChange = expChange - exp;
             exp = 0;
         }
         
         // call event
         ExperienceChangeEvent expEvent;
         if (newLevel == currentLevel) {
             expEvent = new ExperienceChangeEvent(this, expChange, source);
         } else {
             expEvent = new HeroLevelEvent(this, expChange, currentLevel, newLevel, source);
         }
         plugin.getServer().getPluginManager().callEvent(expEvent);
         if (expEvent.isCancelled()) {
             // undo the experience gain
             exp -= expChange;
             return;
         }
 
         // undo the previous gain to make sure we use the updated value
         exp -= expChange;
         expChange = expEvent.getExpChange();
 
         // add the updated experience
         exp += expChange;
 
         // Track if the Hero leveled for persisting
         boolean changedLevel = false;
 
         // notify the user
         if (expChange != 0) {
             if (verbose && expChange > 0) {
                 Messaging.send(player, "$1: Gained $2 Exp", heroClass.getName(), decFormat.format(expChange));
             } else if ( verbose && expChange < 0) {
                Messaging.send(player, "$1: Lost $2 Exp", heroClass.getName(), decFormat.format(-expChange));
             }
             if (newLevel != currentLevel) {
                 changedLevel = true;
                 setHealth(getMaxHealth());
                 syncHealth();
                 if (newLevel >= prop.maxLevel) {
                     exp = prop.getExperience(prop.maxLevel);
                     Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), heroClass.getName());
                     plugin.getHeroManager().saveHero(player);
                 }
                 if (newLevel > currentLevel) {
                     plugin.getSpoutUI().sendPlayerNotification(player, ChatColor.GOLD + "Level Up!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                     Messaging.send(player, "You leveled up! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                 } else {
                     plugin.getSpoutUI().sendPlayerNotification(player, ChatColor.GOLD + "Level Lost!", ChatColor.DARK_RED + "Level - " + String.valueOf(newLevel), Material.DIAMOND_HELMET);
                     Messaging.send(player, "You lost a level up! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                 }
             }
         }
 
         setExperience(exp);
         // Save the hero file when the Hero levels to prevent rollback issues
         if (changedLevel)
             plugin.getHeroManager().saveHero(getPlayer());
     }
 
     public Map<Material, String[]> getBinds() {
         return binds;
     }
 
     public Map<String, Long> getCooldowns() {
         return cooldowns;
     }
 
     public Effect getEffect(String name) {
         for (Effect effect : effects) {
             if (effect.getName().equalsIgnoreCase(name)) {
                 return effect;
             }
         }
         return null;
     }
 
     public Set<Effect> getEffects() {
         return new HashSet<Effect>(effects);
     }
 
     public double getExperience() {
         return getExperience(heroClass);
     }
 
     public double getExperience(HeroClass heroClass) {
         Double exp = experience.get(heroClass.getName());
         return exp == null ? 0 : exp;
     }
 
     public HeroClass getHeroClass() {
         return heroClass;
     }
 
     public int getLevel() {
         return plugin.getConfigManager().getProperties().getLevel(getExperience());
     }
 
     public double getHealth() {
         return health;
     }
 
     public double getMaxHealth() {
         int level = plugin.getConfigManager().getProperties().getLevel(getExperience());
         return heroClass.getBaseMaxHealth() + (level - 1) * heroClass.getMaxHealthPerLevel();
     }
 
     public int getMana() {
         return mana;
     }
 
     public HeroParty getParty() {
         return party;
     }
 
     public Player getPlayer() {
         Player servPlayer = plugin.getServer().getPlayer(player.getName());
         if (servPlayer != null && player != servPlayer) {
             player = servPlayer;
         }
         return player;
     }
 
     public List<ItemStack> getRecoveryItems() {
         return this.itemRecovery;
     }
 
     public Set<Creature> getSummons() {
         return summons;
     }
 
     public void clearSummons() {
         for (Creature summon : summons) {
             summon.remove();
         }
         summons.clear();
     }
 
     public Set<String> getSuppressedSkills() {
         return new HashSet<String>(suppressedSkills);
     }
 
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
 
     public boolean hasParty() {
         return party != null;
     }
 
     public boolean isMaster() {
         return isMaster(heroClass);
     }
 
     public boolean isMaster(HeroClass heroClass) {
         int maxExp = plugin.getConfigManager().getProperties().maxExp;
         return getExperience(heroClass) >= maxExp || getExperience(heroClass) - maxExp > 0;
     }
 
     public boolean isSuppressing(Skill skill) {
         return suppressedSkills.contains(skill.getName());
     }
 
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
 
     public void setExperience(double experience) {
         setExperience(heroClass, experience);
     }
 
     public void setExperience(HeroClass heroClass, double experience) {
         this.experience.put(heroClass.getName(), experience);
     }
 
     public void clearExperience() {
         for (Entry<String, Double> entry : experience.entrySet()) {
             entry.setValue(0.0);
         }
     }
 
     public void setHeroClass(HeroClass heroClass) {
         double currentMaxHP = getMaxHealth();
         this.heroClass = heroClass;
         double newMaxHP = getMaxHealth();
         health *= newMaxHP / currentMaxHP;
         if (health > newMaxHP) {
             health = newMaxHP;
         }
 
         // Check the Players inventory now that they have changed class.
         this.plugin.getInventoryChecker().checkInventory(getPlayer());
     }
 
     public void setMana(int mana) {
         if (mana > 100) {
             mana = 100;
         } else if (mana < 0) {
             mana = 0;
         }
         this.mana = mana;
     }
 
     public void setParty(HeroParty party) {
         this.party = party;
     }
 
     public void setRecoveryItems(List<ItemStack> items) {
         this.itemRecovery = items;
     }
 
     public void setSuppressed(Skill skill, boolean suppressed) {
         if (suppressed) {
             suppressedSkills.add(skill.getName());
         } else {
             suppressedSkills.remove(skill.getName());
         }
     }
 
     public Map<String, String> getSkillSettings(Skill skill) {
         return skill == null ? null : getSkillSettings(skill.getName());
     }
 
     public Map<String, String> getSkillSettings(String skillName) {
         if (!heroClass.hasSkill(skillName)) {
             return null;
         }
 
         return skillSettings.get(skillName.toLowerCase());
     }
 
     public void setSkillSetting(Skill skill, String node, Object val) {
         setSkillSetting(skill.getName(), node, val);
     }
 
     public void setSkillSetting(String skillName, String node, Object val) {
         Map<String, String> settings = skillSettings.get(skillName.toLowerCase());
         if (settings == null) {
             settings = new HashMap<String, String>();
             skillSettings.put(skillName.toLowerCase(), settings);
         }
         settings.put(node, val.toString());
     }
 
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     public void unbind(Material material) {
         binds.remove(material);
     }
 
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
 
     public boolean hasSkill(String skill) {
         return skills.containsKey(skill);
     }
 
     public Map<String, ConfigurationNode> getSkills() {
         return skills;
     }
 
     public void addSkill(String skill) {
         skills.put(skill, Configuration.getEmptyNode());
     }
 }
