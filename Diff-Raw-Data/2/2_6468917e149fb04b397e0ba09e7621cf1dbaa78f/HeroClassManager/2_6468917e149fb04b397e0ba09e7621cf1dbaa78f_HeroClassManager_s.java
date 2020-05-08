 package com.herocraftonline.dev.heroes.classes;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.CircularParentException;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.RecipeGroup;
 import com.herocraftonline.dev.heroes.util.Util;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.io.File;
 import java.util.*;
 import java.util.logging.Level;
 
 /**
  * Manages all classes for the server
  */
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
 
     /**
      * Adds a HeroClass to the classmanager
      *
      * @param c
      * @return true if the class was added
      */
     public boolean addClass(HeroClass c) {
         return classes.add(c);
     }
 
     /**
      * Attempts to get the class of the given name, this will return the HeroClass if found, or null
      *
      * @param name
      * @return the class
      */
     public HeroClass getClass(String name) {
         for (HeroClass c : classes) {
             if (name.equalsIgnoreCase(c.getName())) {
                 return c;
             }
         }
         return null;
     }
 
     /**
      * @return The list of loaded Classes
      */
     public Set<HeroClass> getClasses() {
         return classes;
     }
 
     /**
      * @return The default primary class on the server - there is only ever 1
      */
     public HeroClass getDefaultClass() {
         return defaultClass;
     }
 
     /**
      * Loads all classes from the given directory
      *
      * @param file
      */
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
 
         // We also need to resave the defaults for the skill configurations in case the file was empty
         SkillConfigManager.saveSkillConfig();
         SkillConfigManager.setClassDefaults();
 
         if (defaultClass == null) {
             Heroes.log(Level.SEVERE, "You are missing a default class, this will cause A LOT of issues!");
         }
 
         //Only register the permissions once
         if (plugin.getServer().getPluginManager().getPermission("heroes.classes.*") == null) {
             registerClassPermissions();
         }
     }
 
     /**
      * Loads an individual HeroClass from the given file
      *
      * @param file
      * @return the HeroClass loaded - or null if there was an error
      */
     private HeroClass loadClass(File file) {
         Configuration config = YamlConfiguration.loadConfiguration(file);
         String className = config.getString("name");
         if (className == null) {
             return null;
         }
         HeroClass newClass = new HeroClass(className, plugin);
 
         newClass.setDescription(config.getString("description", ""));
         newClass.setExpModifier(config.getDouble("expmodifier", 1.0D));
         newClass.setPrimary(config.getBoolean("primary", true));
         newClass.setSecondary(config.getBoolean("secondary", false));
         newClass.setTier(config.getInt("tier", 1));
         if (newClass.getTier() < 0) {
             newClass.setTier(0);
         }
         // Load class allowed Armor + Weapons
 
         loadArmor(newClass, config.getStringList("permitted-armor"));
         loadWeapons(newClass, config.getStringList("permitted-weapon"));
         loadDamages(newClass, config);
         loadPermittedSkills(newClass, config.getConfigurationSection("permitted-skills"));
         loadPermissionSkills(newClass, config.getConfigurationSection("permission-skills"));
         loadExperienceTypes(newClass, config.getStringList("experience-sources"));
 
         Double baseMaxHealth = config.getDouble("base-max-health", 20);
         Double maxHealthPerLevel = config.getDouble("max-health-per-level", 0);
         boolean userClass = config.getBoolean("user-class", true);
         newClass.setBaseMaxHealth(baseMaxHealth);
         newClass.setMaxHealthPerLevel(maxHealthPerLevel);
         newClass.setUserClass(userClass);
 
 
         if (Heroes.useSpout()) {
             if (config.isSet("recipes")) {
                 for (String s : config.getStringList("recipes")) {
                     RecipeGroup rg = Heroes.properties.recipes.get(s.toLowerCase());
                     if (rg == null) {
                         Heroes.log(Level.SEVERE, "No recipe group named " + s + " defined in recipes.yml. Check " + className + " for errors or add the recipe group!");
                     } else {
                         newClass.addRecipe(rg);
                     }
                 }
             } else {
                 Heroes.log(Level.SEVERE, "Class " + className + " has no recipes set! They will not be able to craft items!");
             }
         } else // Add the default recipe if spout is not being used, this is just to prevent issues & inconsistencies in the API
         {
             newClass.addRecipe(Heroes.properties.recipes.get("default"));
         }
 
         // Get the class expLoss
         newClass.setExpLoss(config.getDouble("expLoss", -1.0));
         newClass.setPvpExpLoss(config.getDouble("pvpExpLoss", -1.0));
 
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
 
         double defaultCost = 0;
         if (newClass.isPrimary()) {
             defaultCost = Heroes.properties.swapCost;
         } else {
             defaultCost = Heroes.properties.profSwapCost;
         }
 
         double cost = config.getDouble("cost", defaultCost);
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
         } else if (config.isConfigurationSection("parents")) {
             List<String> list = config.getStringList("parents.strong");
             if (list != null) {
                 strongParents.addAll(list);
             }
 
             list = config.getStringList("parents.weak");
             Set<String> weakParents = new HashSet<String>();
             if (list != null) {
                 weakParents.addAll(list);
             }
 
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
             if (heroClass.isUserClass()) {
                 Permission p = new Permission("heroes.classes." + heroClass.getName().toLowerCase(), PermissionDefault.TRUE);
                 Bukkit.getServer().getPluginManager().addPermission(p);
                 classPermissions.put("heroes.classes." + heroClass.getName().toLowerCase(), true);
             } else {
                 Permission p = new Permission("heroes.classes." + heroClass.getName().toLowerCase(), PermissionDefault.OP);
                 Bukkit.getServer().getPluginManager().addPermission(p);
             }
         }
         Permission wildcardClassPermission = new Permission("heroes.classes.*", "Grants access to all classes.", classPermissions);
         plugin.getServer().getPluginManager().addPermission(wildcardClassPermission);
     }
 
     private void loadDamages(HeroClass newClass, Configuration config) {
         String className = newClass.getName();
 
         // Load in item/weapon damages for this class
         ConfigurationSection section = config.getConfigurationSection("item-damage");
         if (section != null) {
             Set<String> itemDamages = section.getKeys(false);
             if (itemDamages == null || itemDamages.isEmpty()) {
                 plugin.debugLog(Level.WARNING, className + " has no item damage section");
             } else {
                 for (String materialName : itemDamages) {
                     Material material = Material.matchMaterial(materialName);
                     if (material != null) {
                         int damage = section.getInt(materialName, 0);
                         newClass.setItemDamage(material, damage);
                     } else {
                         Heroes.log(Level.WARNING, "Invalid material (" + materialName + ") defined for " + className);
                     }
                 }
             }
         }
 
         // Load in Projectile Damages for the class
         section = config.getConfigurationSection("projectile-damage");
         if (section != null) {
             Set<String> projectileDamages = section.getKeys(false);
             if (projectileDamages == null || projectileDamages.isEmpty()) {
                 plugin.debugLog(Level.WARNING, className + " has no projectile damage section");
             } else {
                 for (String projectileName : projectileDamages) {
                     try {
                         ProjectileType type = ProjectileType.matchProjectile(projectileName);
 
                         int damage = section.getInt(projectileName, 0);
                         newClass.setProjectileDamage(type, damage);
                     } catch (IllegalArgumentException e) {
                         Heroes.log(Level.WARNING, "Invalid projectile type (" + projectileName + ") defined for " + className);
                     }
                 }
             }
         }
     }
 
     private void loadWeapons(HeroClass newClass, List<String> weapons) {
         StringBuilder wLimits = new StringBuilder();
         String className = newClass.getName();
         // Get the list of allowed weapons for this class
         if (weapons == null || weapons.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-weapon section");
             return;
         }
         for (String w : weapons) {
             boolean matched = false;
             for (String s : Util.weapons) {
                 if (w.equals("*") || w.equalsIgnoreCase("ALL")) {
                     newClass.addAllowedWeapon(Material.matchMaterial(s));
                     wLimits.append(" ").append(s);
                     matched = true;
                 } else if (s.contains(w.toUpperCase())) {
                     if (s.contains("PICK") && !w.contains("PICK") && w.contains("AXE")) {
                         continue;
                     } else {
                         newClass.addAllowedWeapon(Material.matchMaterial(s));
                         wLimits.append(" ").append(s);
                         matched = true;
                     }
                 }
             }
 
             if (w.equals("*") || w.equals("ALL")) {
                 break;
             }
             if (!matched) {
                 Heroes.log(Level.WARNING, "Invalid weapon type (" + w + ") defined for " + className);
             }
         }
         plugin.debugLog(Level.INFO, "Allowed Weapons - " + wLimits.toString());
     }
 
     /**
      * Remove a HeroClass from the server
      *
      * @param c
      * @return true if the class was found and remvoed
      */
     public boolean removeClass(HeroClass c) {
         return classes.remove(c);
     }
 
     /**
      * Sets the deault primary class to the given HeroClass
      *
      * @param defaultClass
      */
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
 
     private void loadArmor(HeroClass newClass, List<String> armors) {
         StringBuilder aLimits = new StringBuilder();
         String className = newClass.getName();
         // Get the list of Allowed armors for this class
         if (armors == null || armors.isEmpty()) {
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
             if (!matched) {
                 Heroes.log(Level.WARNING, "Invalid armor type (" + a + ") defined for " + className);
             }
         }
         plugin.debugLog(Level.INFO, "Allowed Armor - " + aLimits.toString());
     }
 
     private void loadExperienceTypes(HeroClass newClass, List<String> experienceNames) {
         String className = newClass.getName();
         // Get experience for each class
         Set<ExperienceType> experienceSources = EnumSet.noneOf(ExperienceType.class);
         if (experienceNames == null || experienceNames.isEmpty()) {
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
 
     private void loadPermissionSkills(HeroClass newClass, ConfigurationSection section) {
         if (section == null) {
             return;
         }
         String className = newClass.getName();
         // Load in the Permission-Skills
         Set<String> permissionSkillNames = section.getKeys(false);
         if (permissionSkillNames != null) {
             for (String skill : permissionSkillNames) {
                 // Ignore Overlapping Skill names that are already loaded as permitted-skills
                 if (newClass.hasSkill(skill)) {
                     Heroes.log(Level.WARNING, "Skill already assigned (" + skill + ") for " + className + ". Skipping this skill");
                     continue;
                 }
                 try {
                     if (!plugin.getSkillManager().isLoaded(skill)) {
                         if (!plugin.getSkillManager().loadOutsourcedSkill(skill)) {
                             continue;
                         }
                     }
 
                     newClass.addSkill(skill);
                     // Load the skill settings into the class skill config
                     ConfigurationSection skillSettings = section.getConfigurationSection(skill);
                     if (skillSettings == null) {
                         skillSettings = section.createSection(skill);
                     }
                     plugin.getSkillConfigs().addClassSkillSettings(className, plugin.getSkillManager().getSkill(skill).getName(), skillSettings);
 
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid permission skill (" + skill + ") defined for " + className + ". Skipping this skill.");
                 }
             }
         }
     }
 
     private void loadPermittedSkills(HeroClass newClass, ConfigurationSection section) {
         if (section == null) {
             return;
         }
         String className = newClass.getName();
         Set<String> skillNames = section.getKeys(false);
         // Load in Permitted Skills for the class
         if (skillNames.isEmpty()) {
             Heroes.log(Level.WARNING, className + " has no permitted-skills section");
         } else {
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
 
                 newClass.addSkill(skillName);
 
                 // Copy the settings from the class configuration to the class skill configuration
                 ConfigurationSection skillSettings = section.getConfigurationSection(skillName);
                 if (skillSettings == null) {
                     skillSettings = section.createSection(skillName);
                 }
                 plugin.getSkillConfigs().addClassSkillSettings(className, skill.getName(), skillSettings);
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
 
 
                     newClass.addSkill(skill.getName());
 
                     // Load the skill settings into the class skill config
                     ConfigurationSection skillSettings = section.getConfigurationSection(skill.getName());
                     if (skillSettings == null) {
                         skillSettings = section.createSection(skill.getName());
                     }
                     plugin.getSkillConfigs().addClassSkillSettings(newClass.getName(), skill.getName(), skillSettings);
                 }
             }
         }
     }
 }
