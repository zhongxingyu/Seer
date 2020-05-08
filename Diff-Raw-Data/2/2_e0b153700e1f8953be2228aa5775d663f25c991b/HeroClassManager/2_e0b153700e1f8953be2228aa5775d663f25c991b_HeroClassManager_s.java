 package com.herocraftonline.dev.heroes.classes;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.permissions.Permission;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.CircularParentException;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.Util;
 
 @SuppressWarnings("deprecation")
 public class HeroClassManager {
 
     private final Heroes plugin;
     private Set<HeroClass> classes;
     private HeroClass defaultClass;
     //Temp data store for loading class heirarchies
     private HashMap<HeroClass, Set<String>> weakParents = new HashMap<HeroClass, Set<String>>();
     private HashMap<HeroClass, Set<String>> strongParents = new HashMap<HeroClass, Set<String>>();
 
     public HeroClassManager(Heroes plugin) {
         this.plugin = plugin;
         this.classes = new HashSet<HeroClass>();
     }
 
     public boolean addClass(HeroClass c) {
         return classes.add(c);
     }
 
     public HeroClass getClass(String name) {
         for (HeroClass c : classes) {
             if (name.equalsIgnoreCase(c.getName()))
                 return c;
         }
         return null;
     }
 
     public Set<HeroClass> getClasses() {
         return classes;
     }
 
     public HeroClass getDefaultClass() {
         return defaultClass;
     }
 
     public void loadClasses(File file) {
         if (file.listFiles().length == 0) {
             Heroes.log(Level.WARNING, "You have no classes defined in your setup!");
             return;
         }
         for (File f : file.listFiles()) {
             if (f.isFile() && f.getName().contains(".yml")) {
                 HeroClass newClass = loadClass(f);
                 if (newClass == null) {
                     Heroes.log(Level.WARNING, "Attempted to load " + f.getName() + " but failed. Skipping.");
                     continue;
                 }
                 // Attempt to add the class
                 if (!addClass(newClass)) {
                     Heroes.log(Level.WARNING, "Duplicate class (" + newClass.getName() + ") found. Skipping this class.");
                 } else {
                     Heroes.log(Level.INFO, "Loaded class: " + newClass.getName());
                 }
             }
         }
 
         // After all classes are loaded we need to link them all together
         checkClassHeirarchy();
 
         if (defaultClass == null) {
             Heroes.log(Level.SEVERE, "You are missing a default class, this will cause A LOT of issues!");
         }
 
         //Only register the permissions once
         if (plugin.getServer().getPluginManager().getPermission("heroes.classes.*") == null)
             registerClassPermissions();
     }
 
     private HeroClass loadClass(File file) {
         Configuration config = new Configuration(file);
         config.load();
         String className = config.getString("name");
         if (className == null) {
             return null;
         }
         HeroClass newClass = new HeroClass(className, plugin);
 
         newClass.setDescription(config.getString("description", ""));
         newClass.setExpModifier(config.getDouble("expmodifier", 1.0D));
         newClass.setPrimary(config.getBoolean("primary", true));
         newClass.setSecondary(config.getBoolean("secondary", true));
         newClass.setTier(config.getInt("tier", 0));
         if (newClass.getTier() < 0)
             newClass.setTier(0);
         // Load class allowed Armor + Weapons
 
         loadArmor(newClass, config);
         loadWeapons(newClass, config);
         loadDamages(newClass, config);
         loadPermittedSkills(newClass, config);
         loadPermissionSkills(newClass, config);
         loadExperienceTypes(newClass, config);
 
         Double baseMaxHealth = config.getDouble("base-max-health", 20);
         Double maxHealthPerLevel = config.getDouble("max-health-per-level", 0);
         boolean userClass= config.getBoolean("user-class", true);
         newClass.setBaseMaxHealth(baseMaxHealth);
         newClass.setMaxHealthPerLevel(maxHealthPerLevel);
         newClass.setUserClass(userClass);
 
         // Get the class expLoss
         newClass.setExpLoss(config.getDouble("expLoss", -1));
 
         plugin.getConfigManager().getProperties();
         // Get the maximum level or use the default if it's not specified
         int defaultMaxLevel = Properties.maxLevel;
         int maxLevel = config.getInt("max-level", defaultMaxLevel);
         if (maxLevel < 1) {
             Heroes.log(Level.WARNING, "Class (" + className + ") max level is too low. Setting max level to 1.");
             maxLevel = 1;
         } else if (maxLevel > defaultMaxLevel) {
             Heroes.log(Level.WARNING, "Class (" + className + ") max level is too high. Setting max level to " + defaultMaxLevel + ".");
             maxLevel = defaultMaxLevel;
         }
         newClass.setMaxLevel(maxLevel);
 
         int defaultCost = plugin.getConfigManager().getProperties().swapCost;
         int cost = config.getInt("cost", defaultCost);
         if (cost < 0) {
             Heroes.log(Level.WARNING, "Class (" + className + ") cost is too low. Setting cost to 0.");
             cost = 0;
         }
         newClass.setCost(cost);
 
         //Setup temporary class name storage for heirarchies
 
         String oldStyleParentName = config.getString("parent");
         Set<String> strongParents = new HashSet<String>();
         if (oldStyleParentName != null) {
             strongParents.add(oldStyleParentName);
             this.strongParents.put(newClass, strongParents);
         } else {
             strongParents.addAll(config.getStringList("parents.strong", new ArrayList<String>()));
             Set<String> weakParents = new HashSet<String>(config.getStringList("parents.weak", new ArrayList<String>()));
             this.weakParents.put(newClass, weakParents);
             this.strongParents.put(newClass, strongParents);
         }
 
         //Set the default
         if (config.getBoolean("default", false)) {
             Heroes.log(Level.INFO, "Default class found: " + className);
             defaultClass = newClass;
         }
 
         return newClass;
     }
     private void registerClassPermissions() {
         Map<String, Boolean> classPermissions = new HashMap<String, Boolean>();
         for (HeroClass heroClass : classes) {
             if (heroClass.isUserClass())
                 classPermissions.put("heroes.classes." + heroClass.getName().toLowerCase(), true);
         }
         Permission wildcardClassPermission = new Permission("heroes.classes.*", "Grants access to all classes.", classPermissions);
         plugin.getServer().getPluginManager().addPermission(wildcardClassPermission);
     }
 
     public void loadDamages(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
 
         // Load in item/weapon damages for this class
         List<String> itemDamages = config.getKeys("item-damage");
         if (itemDamages == null || itemDamages.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no item damage section");
         } else {
             for (String materialName : itemDamages) {
                 Material material = Material.matchMaterial(materialName);
                 if (material != null) {
                     int damage = config.getInt("item-damage." + materialName, 0);
                     newClass.setItemDamage(material, damage);
                 } else {
                     Heroes.log(Level.WARNING, "Invalid material (" + material + ") defined for " + className);
                 }
             }
         }
 
         // Load in Projectile Damages for the class
         List<String> projectileDamages = config.getKeys("projectile-damage");
         if (projectileDamages == null || projectileDamages.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no projectile damage section");
         } else {
             for (String projectileName : projectileDamages) {
                 try {
                     ProjectileType type = ProjectileType.matchProjectile(projectileName);
 
                     int damage = config.getInt("projectile-damage." + projectileName, 0);
                     newClass.setProjectileDamage(type, damage);
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid projectile type (" + projectileName + ") defined for " + className);
                 }
             }
         }
     }
 
     public void loadWeapons(HeroClass newClass, ConfigurationNode config) {
         StringBuilder wLimits = new StringBuilder();
         String className = newClass.getName();
         // Get the list of allowed weapons for this class
         List<String> weapon = config.getStringList("permitted-weapon", new ArrayList<String>());
         if (weapon.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-weapon section");
             return;
         }
         for (String w : weapon) {
             boolean matched = false;
             for (String s : Util.weapons) {
                 if (w.equals("*") || w.equalsIgnoreCase("ALL")) {
                     newClass.addAllowedWeapon(Material.matchMaterial(s));
                     wLimits.append(" ").append(s);
                     matched = true;
                 } else if (s.contains(w.toUpperCase())) {
                    if (s.contains("PICK") && !w.contains("PICK"))
                         continue;
                     else {
                         newClass.addAllowedWeapon(Material.matchMaterial(s));
                         wLimits.append(" ").append(s);
                         matched = true;
                     }
                 }
             }
 
             if (w.equals("*") || w.equals("ALL")) 
                 break;
             if (!matched)
                 Heroes.log(Level.WARNING, "Invalid weapon type (" + w + ") defined for " + className);
         }
         plugin.debugLog(Level.INFO, "Allowed Weapons - " + wLimits.toString());
     }
 
     public boolean removeClass(HeroClass c) {
         return classes.remove(c);
     }
 
     public void setDefaultClass(HeroClass defaultClass) {
         this.defaultClass = defaultClass;
     }
 
     /**
      * Checks the full class Heirarchy and links all classes together properly.
      * 
      * @param config
      */
     private void checkClassHeirarchy() {
         for (HeroClass unlinkedClass : classes) {
             Set<String> strong = strongParents.get(unlinkedClass);
             if (strong != null && !strong.isEmpty()) {
                 for (String sp : strong) {
                     HeroClass parent = getClass(sp);
                     if (parent != null) {
                         try {
                             unlinkedClass.addStrongParent(parent);
                             parent.addSpecialization(unlinkedClass);
                         } catch (CircularParentException e) {
                             Heroes.log(Level.SEVERE, "Cannot assign " + unlinkedClass.getName() + " as a parent class as " + sp + " is already a parent of that class.");
                         }
                     } else {
                         Heroes.log(Level.WARNING, "Cannot assign " + unlinkedClass.getName() + " a parent class as " + sp + " does not exist.");
                     }
                 }
             }
             Set<String> weak = weakParents.get(unlinkedClass);
             if (weak != null && !weak.isEmpty()) {
                 for (String wp : weak) {
                     HeroClass parent = getClass(wp);
                     if (parent != null) {
                         try {
                             unlinkedClass.addWeakParent(parent);
                             parent.addSpecialization(unlinkedClass);
                         } catch (CircularParentException e) {
                             Heroes.log(Level.SEVERE, "Cannot assign " + unlinkedClass.getName() + " as a parent class as " + wp + " is already a parent of that class.");
                         }
                     } else {
                         Heroes.log(Level.WARNING, "Cannot assign " + unlinkedClass.getName() + " a parent class as " + wp + " does not exist.");
                     }
                 }
             }
         }
         //Clean out the variables just in case
         this.strongParents.clear();
         this.strongParents = null;
         this.weakParents.clear();
         this.weakParents = null;
     }
 
     private void loadArmor(HeroClass newClass, ConfigurationNode config) {
         StringBuilder aLimits = new StringBuilder();
         String className = newClass.getName();
         // Get the list of Allowed armors for this class
         List<String> armors = config.getStringList("permitted-armor", new ArrayList<String>());
         if (armors.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-armor section");
             return;
         }
         for (String a : armors) {
             boolean matched = false;
             for (String s : Util.armors) {
                 if (s.contains(a.toUpperCase()) || a.equals("*") || a.equalsIgnoreCase("ALL")) {
                     newClass.addAllowedArmor(Material.matchMaterial(s));
                     aLimits.append(" ").append(s);
                     matched = true;
                 }
             }
             //If we had an All node we don't need to continue looping through the allowed armors we can already wear them all
             if (a.equals("*") || a.equals("ALL")) {
                 break;
             }
             if (!matched)
                 Heroes.log(Level.WARNING, "Invalid armor type (" + a + ") defined for " + className);
         }
         plugin.debugLog(Level.INFO, "Allowed Armor - " + aLimits.toString());
     }
 
     private void loadExperienceTypes(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
         // Get experience for each class
         List<String> experienceNames = config.getStringList("experience-sources", null);
         Set<ExperienceType> experienceSources = EnumSet.noneOf(ExperienceType.class);
         if (experienceNames == null) {
             plugin.debugLog(Level.WARNING, className + " has no experience-sources section");
         } else {
             for (String experience : experienceNames) {
                 try {
                     boolean added = experienceSources.add(ExperienceType.valueOf(experience));
                     if (!added) {
                         Heroes.log(Level.WARNING, "Duplicate experience source (" + experience + ") defined for " + className + ".");
                     }
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid experience source (" + experience + ") defined for " + className + ". Skipping this source.");
                 }
             }
         }
         newClass.setExperienceSources(experienceSources);
     }
 
     private void loadPermissionSkills(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
         // Load in the Permission-Skills
         List<String> permissionSkillNames = config.getKeys("permission-skills");
         if (permissionSkillNames != null) {
             for (String skill : permissionSkillNames) {
                 // Ignore Overlapping Skill names that are already loaded as permitted-skills
                 if (newClass.hasSkill(skill)) {
                     Heroes.log(Level.WARNING, "Skill already assigned (" + skill + ") for " + className + ". Skipping this skill");
                     continue;
                 }
                 try {
                     if (!plugin.getSkillManager().isLoaded(skill)) {
                         if (!plugin.getSkillManager().loadOutsourcedSkill(skill))
                             continue;
                     }
                     ConfigurationNode skillSettings = Configuration.getEmptyNode();
                     List<String> settings = config.getKeys("permission-skills." + skill);
                     if (settings != null) {
                         for (String key : settings) {
                             skillSettings.setProperty(key, config.getProperty("permission-skills." + skill + "." + key));
                         }
                     }
                     newClass.addSkill(skill, skillSettings);
 
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid permission skill (" + skill + ") defined for " + className + ". Skipping this skill.");
                 }
             }
         }
     }
 
     private void loadPermittedSkills(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
         // Load in Permitted Skills for the class
         if (config.getKeys("permitted-skills") == null) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-skills section");
         } else {
             Set<String> skillNames = new HashSet<String>();
             skillNames.addAll(config.getKeys("permitted-skills"));
             boolean allSkills = false;
             for (String skillName : skillNames) {
                 if (skillName.equals("*") || skillName.toLowerCase().equals("all")) {
                     allSkills = true;
                     continue;
                 }
                 Skill skill = plugin.getSkillManager().getSkill(skillName);
                 if (skill == null) {
                     Heroes.log(Level.WARNING, "Skill " + skillName + " defined for " + className + " not found.");
                     continue;
                 }
 
                 ConfigurationNode skillSettings = Configuration.getEmptyNode();
                 List<String> settings = config.getKeys("permitted-skills." + skillName);
                 if (settings != null) {
                     for (String key : settings) {
                         skillSettings.setProperty(key, config.getProperty("permitted-skills." + skillName + "." + key));
                     }
                 }
                 newClass.addSkill(skillName, skillSettings);
             }
 
             // Load all skills onto the Class if we found ALL
             if (allSkills) {
                 // Make sure all the skills are loaded first
                 plugin.getSkillManager().loadSkills();
                 for (Skill skill : plugin.getSkillManager().getSkills()) {
                     // Ignore this skill if it was already loaded onto the class (we don't want to overwrite defined
                     // skills as they have settings)
                     if (newClass.hasSkill(skill.getName()) || skill instanceof OutsourcedSkill) {
                         continue;
                     }
 
                     ConfigurationNode skillSettings = Configuration.getEmptyNode();
                     List<String> settings = config.getKeys("permitted-skills." + skill.getName());
                     if (settings != null) {
                         for (String key : settings) {
                             skillSettings.setProperty(key, config.getProperty("permitted-skills." + skill.getName() + "." + key));
                         }
                     }
                     newClass.addSkill(skill.getName(), skillSettings);
                 }
             }
         }
     }
 }
