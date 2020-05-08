 package de.syd.equipcontrol;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EquipControl extends JavaPlugin implements Listener
 {
     FileConfiguration config;
     List<Integer> armor = new ArrayList<Integer>();
     List<Integer> weapon = new ArrayList<Integer>();
     HashMap<Integer, HashMap<String, String>> weaponnew = new HashMap<Integer, HashMap<String, String>>();
     HashMap<Integer, HashMap<String, String>> armornew = new HashMap<Integer, HashMap<String, String>>();
     String nopermweap;
     String nopermhelmet;
     String nopermchestplate;
     String nopermleggings;
     String nopermboots;
     Logger log = Logger.getLogger("Minecraft");
     
     public void onEnable()
     {
         getServer().getPluginManager().registerEvents(this, this);
         config = getConfig();
         if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
             saveDefaultConfig();
         
         loadConfig();
         
        weapon = config.getIntegerList("checked_weapons");
        armor = config.getIntegerList("checked_armor");
         nopermweap = setColored(config.getString("string.weapon", "You don't have the needed permissions to use this weapon"));
        nopermhelmet = setColored(config.getString("string.helmet", "You don't have the needed permissions to wear this helemet"));
         nopermchestplate = setColored(config.getString("string.chest", "You don't have the needed permissions to wear this chestplate"));
         nopermleggings = setColored(config.getString("string.leggings", "You don't have the needed permissions to wear this leggings"));
         nopermboots = setColored(config.getString("string.boots", "You don't have the needed permissions to wear this boots"));
     }
     
     private void loadConfig()
     {
         String w = "checked_weapons";
         String a = "checked_armor";
         
         armor = config.getIntegerList(a + ".list");
         weapon = config.getIntegerList(w + ".list");
         
         for (String key : config.getConfigurationSection(w).getKeys(false))
             try
             {
                 int id = Integer.parseInt(key);
                 
                 HashMap<String, String> helpmap = new HashMap<String, String>();
                 
                 for (String k : config.getConfigurationSection(w + "." + key).getKeys(false))
                     helpmap.put(config.getString(w + "." + key + "." + k), k);
                 
                 weaponnew.put(id, helpmap);
             }
             catch (NumberFormatException e)
             {
             }
         
         for (String key : config.getConfigurationSection(a).getKeys(false))
             try
             {
                 int id = Integer.parseInt(key);
                 
                 HashMap<String, String> helpmap = new HashMap<String, String>();
                 
                 for (String k : config.getConfigurationSection(a + "." + key).getKeys(false))
                     helpmap.put(config.getString(a + "." + key + "." + k), k);
                 
                 armornew.put(id, helpmap);
             }
             catch (NumberFormatException e)
             {
             }
     }
     
     public void onDisable()
     {
         config = null;
         armor = null;
         weapon = null;
     }
     
     /**
      * Checks ArmorSlots for forbidden equipment on closing Inventory
      * 
      * @param event
      */
     @EventHandler
     public void onInventory(InventoryCloseEvent event)
     {
         if (event.getView().getType() == InventoryType.CRAFTING)
             checkArmor((Player) event.getPlayer());
     }
     
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event)
     {
         checkArmor(event.getPlayer());
     }
     
     /**
      * check if a player uses a unallowed armor
      * 
      * @param player
      *            - the checked player
      */
     private void checkArmor(Player player)
     {
         PlayerInventory pinv = player.getInventory();
         ItemStack helmet = pinv.getHelmet();
         ItemStack chestplate = pinv.getChestplate();
         ItemStack leggings = pinv.getLeggings();
         ItemStack boots = pinv.getBoots();
                 
         if (helmet != null && (armornew.containsKey(helmet.getTypeId()) || (armor != null && armor.contains(helmet.getTypeId()))))
         {
             String s = getArmorExtraPerm(helmet);
             if (!player.hasPermission("equipcontrol.armor." + helmet.getTypeId() + s) && !player.hasPermission("equipcontrol.armor." + helmet.getType().name() + s))
             {
                 if (pinv.firstEmpty() >= 0)
                     pinv.addItem(helmet);
                 else
                     player.getWorld().dropItem(player.getLocation(), helmet);
                 pinv.setHelmet(new ItemStack(0));
                 player.sendMessage(nopermhelmet);
             }
         }
         
         if (chestplate != null && (armornew.containsKey(chestplate.getTypeId()) || (armor != null && armor.contains(chestplate.getTypeId()))))
         {
             String s = getArmorExtraPerm(chestplate);
             if (!player.hasPermission("equipcontrol.armor." + chestplate.getTypeId() + s) && !player.hasPermission("equipcontrol.armor." + chestplate.getType().name() + s))
             {
                 if (pinv.firstEmpty() >= 0)
                     pinv.addItem(chestplate);
                 else
                     player.getWorld().dropItem(player.getLocation(), chestplate);
                 pinv.setChestplate(new ItemStack(0));
                 player.sendMessage(nopermchestplate);
             }
         }
         
         if (leggings != null && (armornew.containsKey(leggings.getTypeId()) || (armor != null && armor.contains(leggings.getTypeId()))))
         {
             String s = getArmorExtraPerm(leggings);
             if (!player.hasPermission("equipcontrol.armor." + leggings.getTypeId() + s) && !player.hasPermission("equipcontrol.armor." + leggings.getType().name() + s))
             {
                 if (pinv.firstEmpty() >= 0)
                     pinv.addItem(leggings);
                 else
                     player.getWorld().dropItem(player.getLocation(), leggings);
                 pinv.setLeggings(new ItemStack(0));
                 player.sendMessage(nopermleggings);
             }
         }
         
         if (boots != null && (armornew.containsKey(boots.getTypeId()) || (armor != null && armor.contains(boots.getTypeId()))))
         {
             String s = getArmorExtraPerm(boots);
             if (!player.hasPermission("equipcontrol.armor." + boots.getTypeId() + s) && !player.hasPermission("equipcontrol.armor." + boots.getType().name() + s))
             {
                 if (pinv.firstEmpty() >= 0)
                     pinv.addItem(boots);
                 else
                     player.getWorld().dropItem(player.getLocation(), boots);
                 pinv.setBoots(new ItemStack(0));
                 player.sendMessage(nopermboots);
             }
         }
     }
     
     private String getArmorExtraPerm(ItemStack i)
     {
        return (i.hasItemMeta() && i.getItemMeta().hasDisplayName() && armornew.get(i.getTypeId()).containsKey(i.getItemMeta().getDisplayName())) ? "." + armornew.get(i.getTypeId()).get(i.getItemMeta().getDisplayName()) : "";
     }
     
     private String getWeaponExtraPerm(ItemStack i)
     {
        return (i.hasItemMeta() && i.getItemMeta().hasDisplayName() && weaponnew.get(i.getTypeId()).containsKey(i.getItemMeta().getDisplayName())) ? "." + weaponnew.get(i.getTypeId()).get(i.getItemMeta().getDisplayName()) : "";
     }
     
     /**
      * Checks Weapon on Damage
      * 
      * @param event
      */
     @EventHandler
     public void onDamage(EntityDamageByEntityEvent event)
     {
         if (event.getDamager() instanceof Player)
         {
             Player player = (Player) event.getDamager();
             ItemStack item = player.getItemInHand();
             String s = getWeaponExtraPerm(item);
             
             if (weaponnew.containsKey(item.getTypeId()) || (weapon != null && weapon.contains(item.getTypeId())))
                 if (!player.hasPermission("equipcontrol.weapon." + item.getTypeId() + s) && !player.hasPermission("equipcontrol.weapon." + item.getType().name() + s))
                 {
                     event.setCancelled(true);
                     player.sendMessage(nopermweap);
                 }
         }
         else if (event.getDamager() instanceof Arrow && (weaponnew.containsKey(261) || (weapon != null && weapon.contains(261))))
         {
             if (((Arrow) event.getDamager()).getShooter() instanceof Player)
             {
                 Player player = (Player) ((Arrow) event.getDamager()).getShooter();
                 String s = getWeaponExtraPerm(player.getItemInHand());
                 if (!player.hasPermission("equipcontrol.weapon.261" + s) && !player.hasPermission("equipcontrol.weapon.bow" + s))
                 {
                     event.setCancelled(true);
                     player.sendMessage(nopermweap);
                 }
             }
         }
     }
     
     public static String setColored(String string)
     {
         string = string.replace("&0", ChatColor.BLACK.toString());
         string = string.replace("&1", ChatColor.DARK_BLUE.toString());
         string = string.replace("&2", ChatColor.DARK_GREEN.toString());
         string = string.replace("&3", ChatColor.DARK_AQUA.toString());
         string = string.replace("&4", ChatColor.DARK_RED.toString());
         string = string.replace("&5", ChatColor.DARK_PURPLE.toString());
         string = string.replace("&6", ChatColor.GOLD.toString());
         string = string.replace("&7", ChatColor.GRAY.toString());
         string = string.replace("&8", ChatColor.DARK_GRAY.toString());
         string = string.replace("&9", ChatColor.BLUE.toString());
         string = string.replace("&a", ChatColor.GREEN.toString());
         string = string.replace("&b", ChatColor.AQUA.toString());
         string = string.replace("&c", ChatColor.RED.toString());
         string = string.replace("&d", ChatColor.LIGHT_PURPLE.toString());
         string = string.replace("&e", ChatColor.YELLOW.toString());
         string = string.replace("&f", ChatColor.WHITE.toString());
         string = string.replace("&k", ChatColor.MAGIC.toString());
         string = string.replace("&l", ChatColor.BOLD.toString());
         string = string.replace("&m", ChatColor.STRIKETHROUGH.toString());
         string = string.replace("&n", ChatColor.UNDERLINE.toString());
         string = string.replace("&o", ChatColor.ITALIC.toString());
         string = string.replace("&r", ChatColor.RESET.toString());
         return string;
     }
 }
