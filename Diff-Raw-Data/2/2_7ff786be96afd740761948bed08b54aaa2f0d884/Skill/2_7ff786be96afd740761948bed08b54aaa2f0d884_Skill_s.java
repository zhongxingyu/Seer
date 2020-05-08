 package de.craftlancer.wayofshadows.skills;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import de.craftlancer.wayofshadows.WayOfShadows;
 import de.craftlancer.wayofshadows.utils.ValueWrapper;
 
 /**
  * Represents a configuration of a skill
  */
 public abstract class Skill implements Listener
 {
     protected WayOfShadows plugin;
     
     private String name;
     private List<String> lore;
     private List<Material> items = new LinkedList<Material>();
     private List<String> itemNames;
     private String levelSystem;
     private String cooldownMsg;
     private ValueWrapper cooldown;
     
     public Skill(WayOfShadows instance, String key)
     {
         plugin = instance;
         name = key;
         lore = instance.getConfig().getStringList(key + ".lore");
         
         for (String s : instance.getConfig().getStringList(key + ".items"))
         {
             Material mat = Material.matchMaterial(s);
             
             if (mat == null)
                 instance.getLogger().warning("An item is not a valid Material: " + s);
             else
                 items.add(mat);
         }
         
        itemNames = instance.getConfig().getStringList(key + ".names");
         levelSystem = instance.getConfig().getString(key + ".levelSystem", null);
         cooldown = new ValueWrapper(instance.getConfig().getString(key + ".cooldown", "0"));
         cooldownMsg = instance.getConfig().getString(key + ".cooldownMsg", "This skill for %time% seconds on cooldown!");
         
         registerPermissions();
     }
     
     protected void registerPermissions()
     {
         for (String l : lore)
             plugin.getServer().getPluginManager().addPermission(new Permission("shadow." + getName() + ".lore." + l, PermissionDefault.FALSE));
         
         for (String l : itemNames)
             plugin.getServer().getPluginManager().addPermission(new Permission("shadow." + getName() + ".names." + l, PermissionDefault.FALSE));
         
         for (Material l : items)
             plugin.getServer().getPluginManager().addPermission(new Permission("shadow." + getName() + ".item." + l.name(), PermissionDefault.FALSE));
     }
     
     public void unregisterPermissions()
     {
         for (String l : lore)
             plugin.getServer().getPluginManager().removePermission("shadow." + getName() + ".lore." + l);
         
         for (String l : itemNames)
             plugin.getServer().getPluginManager().removePermission("shadow." + getName() + ".names." + l);
         
         for (Material l : items)
             plugin.getServer().getPluginManager().removePermission("shadow." + getName() + ".item." + l.name());
     }
     
     /**
      * Constructor for pre 0.5 Updater
      */
     @Deprecated
     public Skill(WayOfShadows instance, String key, String item)
     {
         plugin = instance;
         name = key;
         
         lore = new ArrayList<String>();
         itemNames = new ArrayList<String>();
         items = new ArrayList<Material>();
         
         Material mat;
         try
         {
             mat = Material.getMaterial(Integer.parseInt(item));
         }
         catch (NumberFormatException e)
         {
             mat = Material.getMaterial(item);
         }
         
         if (mat != null)
             items.add(mat);
     }
     
     /**
      * Get the name of the skill, which is used in config
      * 
      * @return a String with the name of the skill
      */
     public String getName()
     {
         return name;
     }
     
     /**
      * Get the lore, which can specify a item as usable skillitem
      * 
      * @return a list of Strings, which can define a skillitem via lore
      */
     public List<String> getLore()
     {
         return lore;
     }
     
     /**
      * Get the names, which can specify a item as usable skillitem
      * 
      * @return a list of Strings, which can define a skillitem via itemname
      */
     public List<String> getItemNames()
     {
         return itemNames;
     }
     
     /**
      * Get the item Ids, which can specify a item as usable skillitem
      * 
      * @return a list of Integers, which can define a skillitem via itemId
      */
     public List<Material> getItemIds()
     {
         return items;
     }
     
     /**
      * Get the name of the SkillLevels levelSystem, which is used by this skill
      * 
      * @return the name of levelSystem, null of non is defined
      */
     public String getLevelSys()
     {
         return levelSystem;
     }
     
     /**
      * Get the cooldown message for this skill and the specified player.
      * It uses cooldownMsg as basis.
      * 
      * @param p
      *        - the player the message is made for
      * @return a String, which replaced every %time% with the remaining cooldown
      *         time
      */
     public String getCooldownMsg(Player p)
     {
         return cooldownMsg.replace("%time%", String.valueOf(getRemainingCooldown(p)));
     }
     
     /**
      * Checks if a player has a cooldown for this skill.
      * 
      * @param p
      *        - the checked Player
      * @return the boolean value if the player is on cooldown
      */
     public boolean isOnCooldown(Player p)
     {
         return p.hasMetadata("cooldown." + getName()) && p.getMetadata("cooldown." + getName()).get(0).asLong() >= System.currentTimeMillis();
     }
     
     /**
      * Set a specific player on cooldown, based on the cooldown value of this
      * skill.
      * 
      * @param p
      *        - the Player, which is set on cooldown
      */
     public void setOnCooldown(Player p)
     {
         long time = System.currentTimeMillis();
         int level = plugin.getLevel(p, levelSystem);
         p.setMetadata("cooldown." + getName(), new FixedMetadataValue(plugin, time + (cooldown.getValue(level) * 1000)));
     }
     
     /**
      * Get the remaining cooldown time of a player in seconds
      * 
      * @param p
      *        - the Player
      * @return the remaining cooldown in seconds
      */
     public double getRemainingCooldown(Player p)
     {
         return (p.getMetadata("cooldown." + getName()).get(0).asLong() - System.currentTimeMillis()) / 1000;
     }
     
     /**
      * Check if a player has the permission to perform this skill with the given
      * Item
      * 
      * @param p
      *        - the Player which tries to perform the skill
      * @param item
      *        - the item as ItemStack
      * @return a boolean value whether he has the permission or not
      */
     public boolean hasPermission(Player p, ItemStack item)
     {
         if (p.hasPermission("shadow." + getName()))
             return true;
         
         if (p.hasPermission("shadow." + getName() + ".item." + item.getType().name()))
             return true;
         
         if (item.hasItemMeta())
         {
             if (p.hasPermission("shadow." + getName() + ".name." + item.getItemMeta().getDisplayName()))
                 return true;
             if (item.getItemMeta().hasLore())
                 for (String str : getLore())
                     for (String str2 : item.getItemMeta().getLore())
                         if (str2.contains(str) && p.hasPermission("shadow." + getName() + ".lore." + str))
                             return true;
         }
         
         return false;
     }
     
     /**
      * Check if the given item is able to perform this skill, based on the given
      * itemIds, names and lore for this skill.
      * 
      * @param item
      *        - the item as ItemStack
      * @return a boolean value whether it's a skillitem or not
      */
     public boolean isSkillItem(ItemStack item)
     {
         if (getItemIds().isEmpty() && getItemNames().isEmpty() && getLore().isEmpty())
             return true;
         
         if (item == null)
             return false;
         
         if (getItemIds().contains(item.getType()))
             return true;
         
         if (item.hasItemMeta())
             if (item.getItemMeta().hasDisplayName() && getItemNames().contains(item.getItemMeta().getDisplayName()))
                 return true;
             else if (item.getItemMeta().hasLore())
                 for (String str : getLore())
                     for (String str2 : item.getItemMeta().getLore())
                         if (str2.contains(str))
                             return true;
         
         return false;
     }
     
     /**
      * Save the skill to the given config file
      * 
      * @param config
      *        - the config where the skill is saved to
      */
     public void save(FileConfiguration config)
     {
         List<String> mat = new LinkedList<String>();
         for (Material m : items)
             mat.add(m.name());
         
         config.set(getName() + ".lore", lore);
         config.set(getName() + ".items", mat);
         config.set(getName() + ".itemNames", itemNames);
         config.set(getName() + ".levelSystem", getLevelSys());
     }
     
     /**
      * Return the type of the skill
      * 
      * @return the type of the skill as String
      */
     public abstract String getType();
 }
