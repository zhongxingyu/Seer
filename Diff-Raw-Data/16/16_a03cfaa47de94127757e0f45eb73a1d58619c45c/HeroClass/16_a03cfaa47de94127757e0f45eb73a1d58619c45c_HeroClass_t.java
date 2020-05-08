 package com.herocraftonline.dev.heroes.classes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.bukkit.Material;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.util.RecipeGroup;
 
 /**
  * A Hero's class
  */
 public class HeroClass {
 
     private final String name;
     private String description;
     private Set<HeroClass> strongParents = new HashSet<HeroClass>();
     private Set<HeroClass> weakParents = new HashSet<HeroClass>();
     private Set<HeroClass> specializations = new LinkedHashSet<HeroClass>();
     private Set<Material> allowedArmor = EnumSet.noneOf(Material.class);
     private Set<Material> allowedWeapons = EnumSet.noneOf(Material.class);
     private Set<ExperienceType> experienceSources = null;
     private boolean primary = true;
     private boolean secondary = false;
     private int tier = 1;
     private Map<Material, Integer> itemDamage = new EnumMap<Material, Integer>(Material.class);
     private Map<ProjectileType, Integer> projectileDamage = new EnumMap<ProjectileType, Integer>(ProjectileType.class);
     private Set<String> skills = new LinkedHashSet<String>();
     private List<RecipeGroup> recipes = new ArrayList<RecipeGroup>();
 
     private int maxLevel;
     private double cost;
     private double expModifier;
     private double expLoss;
     private double baseMaxHealth;
     private double maxHealthPerLevel;
     private boolean userClass = true;
     private final Heroes plugin;
 
     /**
      * Constructs a new HeroClass with the given name
      * @param name
      * @param plugin
      */
     public HeroClass(String name, Heroes plugin) {
         this.name = name;
         this.plugin = plugin;
         description = "";
         expModifier = 1.0D;
         baseMaxHealth = 20;
         maxHealthPerLevel = 0;
         maxLevel = 1;
         cost = 0;
     }
 
     protected void addAllowedArmor(Material armor) {
         this.allowedArmor.add(armor);
     }
 
     protected void addAllowedWeapon(Material weapon) {
         this.allowedWeapons.add(weapon);
     }
 
     protected void addSkill(String name) {
         skills.add(name.toLowerCase());
     }
 
     public void addSpecialization(HeroClass heroClass) {
         specializations.add(heroClass);
     }
 
     public void addStrongParent(HeroClass parent) throws CircularParentException {
         if (parent.getAllParents().contains(this))
             throw new CircularParentException();
 
         strongParents.add(parent);
     }
 
     public void addWeakParent(HeroClass parent) throws CircularParentException {
         if (parent.getAllParents().contains(this))
             throw new CircularParentException();
 
         weakParents.add(parent);
     }
 
     /**
      * Gets a set of all Parent HeroClasses
      * This will recursively loop through all parent classes and include their parents until
      * it reaches classes with no parents
      * @return Set of HeroClasses
      */
     public Set<HeroClass> getAllParents() {
         Set<HeroClass> classes = new HashSet<HeroClass>();
         for (HeroClass hClass : this.getParents()) {
             classes.addAll(hClass.getAllParents(new HashSet<HeroClass>(classes)));
         }
         return classes;
     }
 
     private Set<HeroClass> getAllParents(Set<HeroClass> parents) {
         for (HeroClass hClass : this.getParents()) {
             parents.addAll(hClass.getAllParents(new HashSet<HeroClass>(parents)));
         }
         return parents;
     }
 
     /**
      * Gets the level that this class can craft the item at
      * The Object should be an ItemData, ItemStack, or Material
      * 
      * Returns -1 if this class can not craft the item
      * 
      * @param is
      * @return the level that this class can craft the item at.
      */
     public int getCraftLevel(Object o) {
         int level = -1;
         for (RecipeGroup rg : recipes) {
             Boolean b = rg.get(o);
             if (b == null)
                 continue;
             if (!b) {
                 return -1;
             } else {
                 if (level == -1)
                     level = rg.level;
                 else if (rg.level < level)
                     level = rg.level;
             }
         }
         return level;
     }
 
     /**
      * @return if this class is the default class
      */
     public boolean isDefault() {
         return plugin.getClassManager().getDefaultClass().equals(this);
     }
 
     /**
      * Checks if this HeroClass can be selected as a primary HeroClass
      * @return true if the HeroClass is a primary HeroClass
      */
     public boolean isPrimary() {
         return primary;
     }
 
     /**
      * Allows this class to be selected as a primary class
      * @param primary the primary to set
      */
     protected void setPrimary(boolean primary) {
         this.primary = primary;
     }
 
     /**
      * Checks if this HeroClass can be selected as a secondary HeroClass (profession)
      * @return true if the HeroClass is a secondary HeroClass
      */
     public boolean isSecondary() {
         return secondary;
     }
 
     protected void setSecondary(boolean secondary) {
         this.secondary = secondary;
     }
 
     /**
      * Gets what tier this class is
      * @return the tier
      */
     public int getTier() {
         return tier;
     }
 
     protected void setTier(int tier) {
         this.tier = tier;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         HeroClass other = (HeroClass) obj;
         if (name == null) {
             if (other.name != null)
                 return false;
         } else if (!name.equals(other.name))
             return false;
         return true;
     }
 
     /**
      * Returns true if this class is allowed to wear the specified armor
      * @param mat
      * @return true if the given material is an allowed armor type
      */
     public boolean isAllowedArmor(Material mat) {
         return this.allowedArmor.contains(mat);
     }
 
     /**
      * @return the Set of Allowed Armor materials
      */
     public Set<Material> getAllowedArmor() {
         return Collections.unmodifiableSet(allowedArmor);
     }
 
     /**
      * Returns true if this class is allowed to wear the specified weapon
      * @param mat
      * @return
      */
     public boolean isAllowedWeapon(Material mat) {
         return this.allowedWeapons.contains(mat);
     }
 
     /**
      * @return the set of Allowed Weapon materials
      */
     public Set<Material> getAllowedWeapons() {
         return Collections.unmodifiableSet(allowedWeapons);
     }
 
     /**
      * @return the base maximum health for the class.
      */
     public double getBaseMaxHealth() {
         return baseMaxHealth;
     }
 
     /**
      * @return the cost to enter or switch into this class
      */
     public double getCost() {
         return cost;
     }
 
     /**
      * @return the class description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @return the set of Experience sources allowed for this class
      */
     public Set<ExperienceType> getExperienceSources() {
         return Collections.unmodifiableSet(experienceSources);
     }
 
     /**
      * Checks if the ExperienceType is a valid experience source for the HeroClass
      * @param type - ExperienceType
      * @return true if this HeroClass can gain exp from the given ExperienceType
      */
     public boolean hasExperiencetype(ExperienceType type) {
         return experienceSources.contains(type);
     }
     /**
     * Gets the Experience Loss Multiplier specific to this HeroClass
      * @return the expLoss
      */
     public double getExpLoss() {
         return expLoss;
     }
 
     /**
      * @return the HeroClass specific experience modifier
      */
     public double getExpModifier() {
         return this.expModifier;
     }
 
     /**
      * @param material
      * @return Integer damage for the given Material
      */
     public Integer getItemDamage(Material material) {
         return itemDamage.get(material);
     }
 
     /**
      * @return double - amount of Health gained per level
      */
     public double getMaxHealthPerLevel() {
         return maxHealthPerLevel;
     }
 
     /**
      * @return int - max level this HeroClass can be leveled up till
      */
     public int getMaxLevel() {
         return maxLevel;
     }
 
     /**
      * @return String - the Name of the HeroClass
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns a Set containing only the direct Weak & Strong parents this class contains
      * @return List of all HeroClass parents
      */
     public List<HeroClass> getParents() {
         List<HeroClass> parents = new ArrayList<HeroClass>(strongParents);
         parents.addAll(weakParents);
         return Collections.unmodifiableList(parents);
     }
 
     /**
      * This returns the Integer value of the given ProjectileTypes damage.  This can return null if the class
      * doesn't have a value assigned for the Projectile
      * @param type
      * @return Integer - damage for the given projectile type
      */
     public Integer getProjectileDamage(ProjectileType type) {
         return projectileDamage.get(type);
     }
 
     /**
      * @return Set of all names of skills this class contains
      */
     public Set<String> getSkillNames() {
         return new TreeSet<String>(skills);
     }
 
     /**
      * @return Set of all child classes
      */
     public Set<HeroClass> getSpecializations() {
         return Collections.unmodifiableSet(specializations);
     }
 
     /**
      * @return the Set of all Strong parent HeroClasses
      */
     public Set<HeroClass> getStrongParents() {
         return Collections.unmodifiableSet(strongParents);
     }
 
     /**
      * @return the Set of all weak parent HeroClasses
      */
     public Set<HeroClass> getWeakParents() {
         return Collections.unmodifiableSet(weakParents);
     }
 
     @Override
     public int hashCode() {
         return name == null ? 0 : name.hashCode();
     }
 
     /**
      * @param name
      * @return true if this class contains the given skill
      */
     public boolean hasSkill(String name) {
         return skills.contains(name.toLowerCase());
     }
 
     /**
      * @return true if this HeroClass has no weak or strong parent classes
      */
     public boolean hasNoParents() {
         return strongParents.isEmpty() && weakParents.isEmpty();
     }
 
     /*
     public void removeDamageValue(Material material) {
         itemDamage.remove(material);
     }
 
     public void removeSkill(String name) {
         skills.remove(name.toLowerCase());
     }
      */
 
     protected void setBaseMaxHealth(double baseMaxHealth) {
         this.baseMaxHealth = baseMaxHealth;
     }
 
     protected void setCost(double cost) {
         this.cost = cost;
     }
 
     protected void setDescription(String description) {
         this.description = description;
     }
 
     protected void setExperienceSources(Set<ExperienceType> experienceSources) {
         this.experienceSources = experienceSources;
     }
 
     protected void setExpLoss(double expLoss) {
         this.expLoss = expLoss;
     }
 
     protected void setExpModifier(double modifier) {
         this.expModifier = modifier;
     }
 
     protected void setItemDamage(Material material, int damage) {
         itemDamage.put(material, damage);
     }
 
     protected void setMaxHealthPerLevel(double maxHealthPerLevel) {
         this.maxHealthPerLevel = maxHealthPerLevel;
     }
 
     protected void setMaxLevel(int maxLevel) {
         this.maxLevel = maxLevel;
     }
 
     protected void setProjectileDamage(ProjectileType type, int damage) {
         projectileDamage.put(type, damage);
     }
 
     protected void setSpecializations(Set<HeroClass> specializations) {
         this.specializations = specializations;
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     /**
      * @return true if this class is a user-class and was added to the heroes.classes.* permission
      */
     public boolean isUserClass() {
         return userClass;
     }
 
     protected void setUserClass(boolean userClass) {
         this.userClass = userClass;
     }
 
     /**
      * Stores Experience Source Names
      *
      */
     public static enum ExperienceType {
         SKILL,
         KILLING,
         PVP,
         MINING,
         ENCHANTING,
         FARMING,
         CRAFTING,
         LOGGING,
         DEATH,
         ADMIN,
         EXTERNAL,
         FISHING,
         QUESTING,
         BUILDING;
     }
 
 
     @SuppressWarnings("serial")
     public class CircularParentException extends Exception {
     }
     
     /**
      * Adds a recipegroup to the list of recipe groups
      * @param rg
      */
     void addRecipe(RecipeGroup rg) {
         recipes.add(rg);
     }
 }
