 package com.herocraftonline.dev.heroes.persistence;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ExperienceGainEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class Hero {
 
     protected final Heroes plugin;
     protected Player player;
     protected HeroClass heroClass;
     protected int mana = 0;
     protected HeroParty party = null;
     protected boolean verbose = true;
     protected HeroEffects effects;
     protected Map<String, Integer> experience = new HashMap<String, Integer>();
     protected Map<String, Long> cooldowns = new HashMap<String, Long>();
     protected Map<Entity, CreatureType> summons = new HashMap<Entity, CreatureType>();
     protected Map<Material, String[]> binds = new HashMap<Material, String[]>();
     protected List<ItemStack> itemRecovery = new ArrayList<ItemStack>();
     protected Set<String> suppressedSkills = new HashSet<String>();
     protected double health;
 
     public Hero(Heroes plugin, Player player, HeroClass heroClass, double health) {
         this.plugin = plugin;
         this.player = player;
         this.heroClass = heroClass;
         this.effects = new HeroEffects(plugin.getCommandManager(), this);
         this.health = health;
     }
 
     public Hero(Heroes plugin, Player player, HeroClass heroClass, Map<String, Integer> experience, int mana, boolean verbose, List<ItemStack> itemRecovery, Map<Material, String[]> binds, Set<String> suppressedSkills, double health) {
         this(plugin, player, heroClass, health);
         this.experience = experience;
         this.mana = mana;
         this.itemRecovery = itemRecovery;
         this.binds = binds;
         this.verbose = verbose;
         this.suppressedSkills = suppressedSkills;
     }
 
     public void addRecoveryItem(ItemStack item) {
         this.itemRecovery.add(item);
     }
 
     public void setRecoveryItems(List<ItemStack> items) {
         this.itemRecovery = items;
     }
 
     public List<ItemStack> getRecoveryItems() {
         return this.itemRecovery;
     }
 
     public Player getPlayer() {
         Player servPlayer = plugin.getServer().getPlayer(player.getName());
         if (servPlayer != null && player != servPlayer) {
             player = servPlayer;
         }
         return player;
     }
 
     public HeroClass getHeroClass() {
         return heroClass;
     }
 
     public boolean isMaster() {
         return isMaster(heroClass);
     }
 
     public boolean isMaster(HeroClass heroClass) {
         int maxExp = plugin.getConfigManager().getProperties().maxExp;
         if (getExperience(heroClass) >= maxExp || getExperience(heroClass) - maxExp == 1) {
             return true;
         }
         return false;
     }
 
     public int getLevel() {
         return plugin.getConfigManager().getProperties().getLevel(getExperience());
     }
 
     public int getExperience() {
         return getExperience(heroClass);
     }
 
     public int getExperience(HeroClass heroClass) {
         Integer exp = experience.get(heroClass.getName());
         return exp == null ? 0 : exp;
     }
 
     public void setExperience(int experience) {
         setExperience(heroClass, experience);
     }
 
     public void setExperience(HeroClass heroClass, int experience) {
         this.experience.put(heroClass.getName(), experience);
     }
 
     public int getMana() {
         return mana;
     }
 
     public void setHeroClass(HeroClass heroClass) {
         this.heroClass = heroClass;
 
         // Check the Players inventory now that they have changed class.
         this.plugin.getInventoryChecker().checkInventory(getPlayer());
     }
 
     public void changeHeroClass(HeroClass heroClass) {
         setHeroClass(heroClass);
         binds.clear();
     }
 
    public void gainExp(double expGain, ExperienceType source, boolean distributeToParty) {
         if (distributeToParty && party != null && party.getExp()) {
             Location location = getPlayer().getLocation();
 
             Set<Player> partyMembers = new HashSet<Player>(party.getMembers());
             Set<Player> inRangeMembers = new HashSet<Player>();
             for (Player partyMember : partyMembers) {
                 if (location.distance(partyMember.getLocation()) <= 50) {
                     inRangeMembers.add(partyMember);
                 }
             }
 
             int partySize = inRangeMembers.size();
             double partyBonus = 0.10;
            double sharedExpGain = Math.ceil(expGain / partySize * (((partySize - 1) * partyBonus) + 1.0));
 
             for (Player partyMember : inRangeMembers) {
                 plugin.getHeroManager().getHero(partyMember).gainExp(sharedExpGain, source, false);
             }
 
             return;
         }
 
         int exp = getExperience();
 
         // adjust exp using the class modifier
        expGain *= heroClass.getExpModifier();
        int roundedExpGain = (int) Math.ceil(expGain);
 
         Properties prop = plugin.getConfigManager().getProperties();
         int currentLevel = prop.getLevel(exp);
        int newLevel = prop.getLevel(exp + roundedExpGain);
 
         // add the experience
         exp += expGain;
 
         // call event
         ExperienceGainEvent expEvent;
         if (newLevel == currentLevel) {
            expEvent = new ExperienceGainEvent(this, roundedExpGain, source);
         } else {
            expEvent = new LevelEvent(this, roundedExpGain, currentLevel, newLevel, source);
         }
         plugin.getServer().getPluginManager().callEvent(expEvent);
         if (expEvent.isCancelled()) {
             // undo the experience gain
             exp -= expGain;
             return;
         }
 
         // undo the previous gain to make sure we use the updated value
         exp -= expGain;
         expGain = expEvent.getExpGain();
 
         // add the updated experience
         exp += expGain;
 
         // notify the user
         if (expGain != 0) {
             if (verbose) {
                 Messaging.send(player, "$1: Gained $2 Exp", heroClass.getName(), String.valueOf(expGain));
             }
             if (newLevel != currentLevel) {
                 Messaging.send(player, "You leveled up! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                 if (newLevel >= prop.maxLevel) {
                     exp = prop.getExperience(prop.maxLevel);
                     Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), heroClass.getName());
                     plugin.getHeroManager().saveHeroFile(player);
                 }
             }
         }
 
         setExperience(exp);
     }
 
     public void gainExp(int expGain, ExperienceType source) {
         gainExp(expGain, source, true);
     }
 
     public void setMana(int mana) {
         this.mana = mana;
     }
 
     public Map<String, Long> getCooldowns() {
         return cooldowns;
     }
 
     public Map<Entity, CreatureType> getSummons() {
         return summons;
     }
 
     public HeroEffects getEffects() {
         return effects;
     }
 
     public Map<Material, String[]> getBinds() {
         return binds;
     }
 
     public void bind(Material material, String[] skillName) {
         binds.put(material, skillName);
     }
 
     public void unbind(Material material) {
         binds.remove(material);
     }
 
     public boolean isVerbose() {
         return verbose;
     }
 
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     public void setSuppressed(Skill skill, boolean suppressed) {
         if (suppressed) {
             suppressedSkills.add(skill.getName());
         } else {
             suppressedSkills.remove(skill.getName());
         }
     }
 
     public boolean isSuppressing(Skill skill) {
         return suppressedSkills.contains(skill.getName());
     }
 
     public Set<String> getSuppressedSkills() {
         return new HashSet<String>(suppressedSkills);
     }
 
     @Override
     public int hashCode() {
         return player == null ? 0 : player.getName().hashCode();
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
 
     public boolean hasParty() {
         return party != null;
     }
 
     public HeroParty getParty() {
         return party;
     }
 
     public void setParty(HeroParty party) {
         this.party = party;
     }
 
     public void dealDamage(double damage) {
         if (health - damage > 0) {
             health = health - damage;
             updatePlayerDisplay();
         } else {
             health = 0;
             updatePlayerDisplay();
         }
     }
 
     public void healHealth(double heal) {
         if (heal > 0 && health + heal <= heroClass.getMaxHealth()) {
             health = health + heal;
             updatePlayerDisplay();
         } else {
             health = health + (heroClass.getMaxHealth() - health);
         }
     }
 
     public double getHealth() {
         return health;
     }
 
     public void updatePlayerDisplay() {
         player.setHealth((int) Math.round(health / heroClass.getMaxHealth() * 20));
     }
 
 }
