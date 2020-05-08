 package com.example.groupid;
 
 import java.text.MessageFormat;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.Material;
 import org.bukkit.ChatColor;
 import org.bukkit.enchantments.Enchantment;
 
 /*
  * This is a sample event listener
  */
 public class SampleListener implements Listener {
     private final BitLimit PVP;
 
     /*
      * This listener needs to know about the plugin which it came from
      */
     public SampleListener(Sample plugin) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
     }
 
     /*
      * Send the sample message to all players that join
      */
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
 //        event.getPlayer().sendMessage(this.plugin.getConfig().getString("sample.message"));
     }
 
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent event) {
 
         Player player = event.getPlayer(); // The player who joined
         PlayerInventory inventory = player.getInventory(); // The player's inventory
 
         ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
         ItemStack bow = new ItemStack(Material.BOW, 1);
         ItemStack arrow = new ItemStack(Material.ARROW, 1);
        ItemStack food = new ItemStack(Material.STEAK, 8);
 
         ItemStack[] armor = new ItemStack[4];
         armor[0] = new ItemStack(Material.DIAMOND_BOOTS, 1);
         armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
         armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
         armor[3] = new ItemStack(Material.DIAMOND_HELMET, 1);
         inventory.setArmorContents(armor);
 
         inventory.addItem(sword);
         inventory.addItem(bow);
         inventory.addItem(arrow);
         inventory.addItem(food);
         
 //        player.sendMessage(ChatColor.DARK_BLUE + "Have some gear, broestar.");
     }
 
     /*
      * Another example of a event handler. This one will give you the name of
      * the entity you interact with, if it is a Creature it will give you the
      * creature Id.
      */
     @EventHandler
     public void onPlayerInteract(PlayerInteractEntityEvent event) {
         final EntityType entityType = event.getRightClicked().getType();
 
         event.getPlayer().sendMessage(MessageFormat.format(
                 "You interacted with a {0} it has an id of {1}",
                 entityType.getName(),
                 entityType.getTypeId()));
     }
 }
