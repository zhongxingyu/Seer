 package com.notoriousdev.custom.listeners;
 
 import com.notoriousdev.custom.NDCustom;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerListener implements Listener
 {
 
     private final NDCustom plugin;
 
     public PlayerListener(NDCustom plugin)
     {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onItemThrow(PlayerInteractEvent event)
     {
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
         if (player.getGameMode() == GameMode.CREATIVE
                 && (item.getType() == Material.POTION
                 || item.getType() == Material.EXP_BOTTLE
                 || item.getType() == Material.SNOW_BALL
                 || item.getType() == Material.EGG
                 || item.getType() == Material.MONSTER_EGG
                 || item.getType() == Material.MONSTER_EGGS)){
             event.setCancelled(true);
             player.sendMessage(ChatColor.RED + "You cannot use " + item.getType()
                     .name().toLowerCase().replace("_", " ") + "s in creative!");
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBowFire(EntityShootBowEvent event)
     {
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
             if (player.getGameMode() == GameMode.CREATIVE) {
                 event.setCancelled(true);
                 player.sendMessage(ChatColor.RED + "You cannot use bows in creative!");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onItemDrop(PlayerDropItemEvent event)
     {
         if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
         {
             event.setCancelled(true);
             event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop items in creative!");
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onDispenserInteract(InventoryClickEvent event)
     {
         Player player = (Player) event.getWhoClicked();
         ItemStack item = event.getCurrentItem();
         if (item != null && player.getGameMode() == GameMode.CREATIVE
                 && event.getInventory().getType() == InventoryType.DISPENSER
                 && (item.getType() == Material.POTION
                 || (item.getType() == Material.EXP_BOTTLE)
                 || item.getType() == Material.MONSTER_EGG
                 || item.getType() == Material.MONSTER_EGGS
                 || item.getType() == Material.ARROW
                 || item.getType() == Material.SNOW_BALL
                 || item.getType() == Material.EGG)){
             event.setCancelled(true);
             player.sendMessage(ChatColor.RED + "You cannot put " + item.getType()
                     .name().toLowerCase().replace("_", " ") + "s into dispensers!");
             event.setCursor(null);
         }
 
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onCreativeDamage(EntityDamageByEntityEvent event)
     {
         if (event.getDamager() instanceof Player) {
             Player player = (Player) event.getDamager();
             if (player.getGameMode() == GameMode.CREATIVE) {
                 event.setCancelled(true);
                 player.sendMessage(ChatColor.RED + "You cannot PVP while in creative mode, dumbass!");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onSkyblockDeath(PlayerDeathEvent event)
     {
         Player player = event.getEntity();
         if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID && player.getLocation().getWorld().getName().equalsIgnoreCase("skyblock")) {
             event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.skyblock-fall").replace("{PLAYER}", player.getDisplayName())));
         } else {
             // Random death messages? Random death messages.
             event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.generic-death").replace("{PLAYER}", player.getDisplayName())));
         }
     }
 
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         Player player = event.getPlayer();
        if (Permissions.NDCUSTOM_CHAT.isAuthorised(player)) {
             return;
         }
         event.setCancelled(true);
         player.sendMessage(ChatColor.RED + "YOU SHALL NOT SPEAK!");
     }
 }
