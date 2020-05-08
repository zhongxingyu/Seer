 package com.herocraftonline.dev.heroes.skill;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 /**
  * The root class of the skill heirarchy. This class implements the basic functionality of every Heroes skill including
  * configuration handling, area-based player notifications and event registration. Because this class extends
  * {@link com.herocraftonline.dev.heroes.command.BaseCommand}, the constructor of every skill should define a name,
  * description, usage, min and max arguments and at least one identifier. Any registered events must provide an event
  * listener, usually created as an inner class.
  * </br>
  * </br>
  * <b>Skill Framework:</b>
  * <ul>
  * <li>{@link ActiveSkill}</li>
  * <ul>
  * <li>{@link TargettedSkill}</li>
  * </ul>
  * <li>{@link PassiveSkill}</li> <li>{@link OutsourcedSkill}</li> </ul>
  * <b>Note:</b> All skill identifiers <i>must</i> begin with <i>skill</i>, e.g. "skill fireball".
  */
 public abstract class Skill extends BasicCommand {
 
     public final Heroes plugin;
     private ConfigurationNode config;
 
     private final Set<SkillType> types = EnumSet.noneOf(SkillType.class);
 
     /**
      * The constructor of every skill must define:
      * <ul>
      * <li><code>name</code></li>
      * <li><code>description</code></li>
      * <li><code>usage</code></li>
      * <li><code>minArgs</code></li>
      * <li><code>maxArgs</code></li>
      * <li><code>identifiers</code></li>
      * <li><code>notes</code> (optional)</li>
      * </ul>
      * 
      * @param plugin
      *            the active Heroes instance
      */
     public Skill(Heroes plugin, String name) {
         super(name);
         this.plugin = plugin;
     }
 
     public void addSpellTarget(Entity o, Hero hero) {
         plugin.getDamageManager().addSpellTarget(o, hero, this);
     }
 
     /**
      * Helper method that broadcasts a message to all players within 30 blocks of the specified source. These messages
      * can be suppressed by players on an individual basis.
      * 
      * @param source
      *            the <code>Location</code> to measure from
      * @param message
      *            the content of the message
      * @param args
      *            any text in the message of the format $<i>n</i> where <i>n</i>
      *            is an integer will be replaced with the <i>n</i>th element of
      *            this array
      */
     public void broadcast(Location source, String message, Object... args) {
         if (message == null || message.isEmpty())
             return;
 
         final Player[] players = plugin.getServer().getOnlinePlayers();
         for (Player player : players) {
             Location playerLocation = player.getLocation();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (hero.isSuppressing(this)) {
                 continue;
             }
             if (source.getWorld().equals(playerLocation.getWorld())) {
                 if (playerLocation.distance(source) < 30) {
                     Messaging.send(player, message, args);
                 }
             }
         }
     }
 
     /**
      * Tests if the target is damagable from a source. Also adds a last-damage cause to the target which allows proper
      * xp.
      * Returns if the damage check was successful.
      * 
      * @param player
      * @param target
      * @return
      */
     public boolean damageCheck(Player player, LivingEntity target) {
         if (player.equals(target))
             return false;
         
         EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled())
             return false;
         return true;
     }
 
     /**
      * The end of the execution path of a skill, this method is called whenever a command with a registered identifier
      * is used.
      * 
      * @param sender
      *            the <code>CommandSender</code> issuing the command
      * @param args
      *            the arguments provided with the command
      */
     @Override
     public abstract boolean execute(CommandSender sender, String identifier, String[] args);
 
     public ConfigurationNode getConfig() {
         return config;
     }
 
     /**
      * Creates and returns a <code>ConfigurationNode</code> containing all the default data for the skill. By default,
      * this configuration is empty.
      * 
      * @return an empty configuration
      */
     public ConfigurationNode getDefaultConfig() {
         return Configuration.getEmptyNode();
     }
 
     public Heroes getPlugin() {
         return plugin;
     }
 
     
     /**
      * Retrieves a <code>Boolean</code> value from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public boolean getSetting(Hero hero, String setting, boolean def) {
        if (hero == null)
            return config.getBoolean(setting, def);
         if (hasSetting(hero.getHeroClass(), setting))
             return hero.getHeroClass().getSkillSettings(getName()).getBoolean(setting, def);
         else if (hero.getSecondClass() != null && hasSetting(hero.getSecondClass(), setting))
             return hero.getSecondClass().getSkillSettings(getName()).getBoolean(setting, def);
         else
             return config.getBoolean(setting, def);
     }
 
     /**
      * Retrieves a <code>double</code> value from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param hero
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public double getSetting(Hero hero, String setting, double def, boolean lower) {
        if (hero == null)
            return config.getDouble(setting, def);
        
         double val1 = -1;
         double val2 = -1;
         if (hasSetting(hero.getHeroClass(), setting))
             val1 = hero.getHeroClass().getSkillSettings(getName()).getDouble(setting, def);
         if (hero.getSecondClass() != null && hasSetting(hero.getSecondClass(), setting)) {
             val2 = hero.getHeroClass().getSkillSettings(getName()).getDouble(setting, def);
         }
         
         if (val1 != -1 && val2 != -1) {
             if (lower)
                 return val1 < val2 ? val1 : val2;
             else
                 return val1 > val2 ? val1 : val2;
         } else if (val1 != -1)
             return val1;
         else if (val2 != -1)
             return val2;
         else
             return config.getDouble(setting, def);
     }
 
     /**
      * Retrieves a <code>int</code> value from the skill's configuration. Data from the provided <code>HeroClass</code>
      * will be preferred over the skill's own data, if found. If the setting is found in neither of these sources, the
      * default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public int getSetting(Hero hero, String setting, int def, boolean lower) {
        if (hero == null)
            return getConfig().getInt(setting, def);
        
         int val1 = -1;
         int val2 = -1;
         if (hasSetting(hero.getHeroClass(), setting))
             val1 = hero.getHeroClass().getSkillSettings(getName()).getInt(setting, def);
         if (hero.getSecondClass() != null && hasSetting(hero.getSecondClass(), setting)) {
             val2 = hero.getHeroClass().getSkillSettings(getName()).getInt(setting, def);
         }
         
         if (val1 != -1 && val2 != -1) {
             if (lower)
                 return val1 < val2 ? val1 : val2;
             else
                 return val1 > val2 ? val1 : val2;
         } else if (val1 != -1)
             return val1;
         else if (val2 != -1)
             return val2;
         else
             return config.getInt(setting, def);
     }
 
     /**
      * Retrieves a <code>List of String</code> values from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public List<String> getSetting(Hero hero, String setting, List<String> def) {
        if (hero == null)
            return config.getStringList(setting, def);
        
         List<String> vals = new ArrayList<String>();
         if (hasSetting(hero.getHeroClass(), setting))
             vals.addAll(hero.getHeroClass().getSkillSettings(getName()).getStringList(setting, def));
         if (hero.getSecondClass() != null && hasSetting(hero.getSecondClass(), setting))
             vals.addAll(hero.getSecondClass().getSkillSettings(getName()).getStringList(setting, def));
         
         if (!vals.isEmpty())
             return vals;
         else
             return config.getStringList(setting, def);
     }
 
     /**
      * Retrieves a <code>String</code> value from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public String getSetting(Hero hero, String setting, String def) {
         if (hero == null)
             return config.getString(setting, def);
         else if (hasSetting(hero.getHeroClass(), setting))
             return hero.getHeroClass().getSkillSettings(getName()).getString(setting, def);
         else if (hero.getSecondClass() != null && hasSetting(hero.getSecondClass(), setting))
             return hero.getSecondClass().getSkillSettings(getName()).getString(setting, def);
         else
             return config.getString(setting, def);
     }
 
     /**
      * Retrieves all <code>String</code> keys from the skill's configuration. Data from the provided
      * <code>HeroClass</code> and
      * the main configuration will be returned
      * 
      * @param heroClass
      *            the class to search for skill data
      * @return the stored setting
      */
     public List<String> getSettingKeys(Hero hero) {
         Set<String> keys = new HashSet<String>();
         keys.addAll(config.getKeys());
 
         if (hasNode(hero.getHeroClass(), null)) {
             keys.addAll(hero.getHeroClass().getSkillSettings(getName()).getKeys());
         }
         if (hero.getSecondClass() != null && hasNode(hero.getSecondClass(), null)) {
             keys.addAll(hero.getSecondClass().getSkillSettings(getName()).getKeys());
         }
         return new ArrayList<String>(keys);
     }
 
     /**
      * Retrieves all <code>String</code> keys from the skill's configuration. Data from the provided
      * <code>HeroClass</code> and
      * the main configuration will be returned
      * 
      * @param heroClass
      *            the class to search for skill data
      * @return the stored setting
      */
     public List<String> getSettingKeys(Hero hero, String setting) {
         Set<String> keys = new HashSet<String>();
         keys.addAll(config.getKeys(setting));
         if (hasNode(hero.getHeroClass(), setting)) {
             keys.addAll(hero.getHeroClass().getSkillSettings(getName()).getKeys(setting));
         }
         if (hero.getSecondClass() != null && hasNode(hero.getSecondClass(), setting)) {
             keys.addAll(hero.getSecondClass().getSkillSettings(getName()).getKeys(setting));
         }
         return new ArrayList<String>(keys);
     }
 
     public Set<SkillType> getTypes() {
         return Collections.unmodifiableSet(this.types);
     }
 
     public boolean hasNode(HeroClass heroClass, String setting) {
         if (heroClass == null)
             return false;
         if (heroClass.getSkillSettings(getName()) == null)
             return false;
         if (setting == null)
             return !heroClass.getSkillSettings(getName()).getKeys().isEmpty();
 
         return heroClass.getSkillSettings(getName()).getNode(setting) != null;
     }
 
     /**
      * An initialization method called after all configuration data is loaded.
      */
     public abstract void init();
 
     @Override
     public boolean isShownOnHelpMenu() {
         return false;
     }
 
     public boolean isType(SkillType type) {
         return types.contains(type);
     }
 
     /**
      * Sets the configuration containing all settings related to the skill. This should only be used by the skill loader
      * in most cases.
      * 
      * @param config
      *            the new skill configuration
      */
     public void setConfig(ConfigurationNode config) {
         this.config = config;
     }
 
     private boolean hasSetting(HeroClass heroClass, String setting) {
         if (heroClass == null)
             return false;
         if (heroClass.getSkillSettings(getName()) == null)
             return false;
 
         return heroClass.getSkillSettings(getName()).getString(setting) != null;
     }
 
     /**
      * Helper method to make registering an event a little easier.
      * 
      * @param type
      *            the type of event
      * @param listener
      *            the listener used to handle the event
      * @param priority
      *            the priority given to the event handler
      */
     protected void registerEvent(Type type, Listener listener, Priority priority) {
         plugin.getServer().getPluginManager().registerEvent(type, listener, priority, plugin);
     }
 
     protected void setTypes(SkillType... types) {
         this.types.addAll(Arrays.asList(types));
     }
 }
