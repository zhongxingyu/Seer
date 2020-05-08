 package com.notoriousdev.custom;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class NDCPlayerListener implements Listener
 {
 
     public NDCustom plugin;
 
     public NDCPlayerListener(NDCustom plugin)
     {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onPlayerJoin(final PlayerJoinEvent event)
     {
         event.setJoinMessage(ChatColor.GREEN + "Join: " + ChatColor.GOLD + event.getPlayer().getName());
 
     }
 
     @EventHandler
     public void onPlayerQuit(final PlayerQuitEvent event)
     {
         event.setQuitMessage(ChatColor.RED + "Quit: " + ChatColor.GOLD + event.getPlayer().getName());
 
     }
 
     @EventHandler
     public void onPlayerKick(final PlayerKickEvent event)
     {
         event.setLeaveMessage(ChatColor.RED + "Kick: " + ChatColor.GOLD + event.getPlayer().getName());
 
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPotionThrow(PlayerInteractEvent event)
     {
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
         if ((item.getType() == Material.POTION) || item.getType() == Material.EXP_BOTTLE && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getGameMode() == GameMode.CREATIVE)
         {
             player.sendMessage(ChatColor.RED + "You cannot use potions in creative!");
             event.setCancelled(true);
             player.getInventory().setItemInHand(null);
             player.updateInventory();
         }
         if ((item.getType() == Material.MONSTER_EGG || item.getType() == Material.MONSTER_EGGS) && (event.getAction() == Action.RIGHT_CLICK_BLOCK))
         {
             event.setCancelled(true);
             player.sendMessage(ChatColor.RED + "You cannot use monster eggs in creative!");
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBowFire(EntityShootBowEvent event)
     {
         if (event.getEntity() instanceof Player)
         {
             Player player = (Player) event.getEntity();
             if (player.getGameMode() == GameMode.CREATIVE)
             {
                 event.setCancelled(true);
                 player.sendMessage(ChatColor.RED + "You cannot use bows in creative!");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onDispenserInteract(InventoryClickEvent event)
     {
         Player player = (Player) event.getWhoClicked();
         ItemStack item = event.getCurrentItem();
         if (item != null && player.getGameMode() == GameMode.CREATIVE && event.getInventory().getType().equals(InventoryType.DISPENSER)
                 && (item.getType() == Material.POTION || item.getType() == Material.EXP_BOTTLE || item.getType() == Material.MONSTER_EGG || item.getType() == Material.MONSTER_EGGS)
                 && (event.isLeftClick() || event.isRightClick() || event.isShiftClick()))
         {
             event.setCancelled(true);
             event.setCursor(null);
             event.setCurrentItem(null);
             player.sendMessage(ChatColor.RED + "You cannot put " + item.getType().name().toLowerCase().replace("_", " ") + "s into dispensers!");
         }
 
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onCreativeDamage(EntityDamageByEntityEvent event)
     {
         if (event.getDamager() instanceof Player)
         {
            Player player = (Player) event.getDamager();
             if (player.getGameMode() == GameMode.CREATIVE)
             {
                 event.setCancelled(true);
                 player.sendMessage(ChatColor.RED + "You cannot PVP others while in creative!");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onSkyblockDeath(PlayerDeathEvent event)
     {
         Player player = event.getEntity();
         if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID && player.getLocation().getWorld().getName().equalsIgnoreCase("skyblock"))
         {
             event.setDeathMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.RED + " couldn't handle the skyblock.");
         } else
         {
             event.setDeathMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.RED + " died of unknown causes...");
         }
     }
 }
