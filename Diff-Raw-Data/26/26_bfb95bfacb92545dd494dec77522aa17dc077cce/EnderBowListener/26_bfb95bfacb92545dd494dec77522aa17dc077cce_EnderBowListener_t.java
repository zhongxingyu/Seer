 package me.zachbruggeman.enderbow;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class EnderBowListener implements Listener {
 
     private final EnderBowPlugin plugin;
 
     public EnderBowListener(EnderBowPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onShootBow(EntityShootBowEvent event) {
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
 
             if (!player.hasPermission("enderbow.fire")) {
                 player.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                 event.setCancelled(true);
                 return;
             }
 
            if (event.getBow().getEnchantmentLevel(Enchantment.ARROW_INFINITE) == 2 || event.getBow().getEnchantmentLevel(Enchantment.ARROW_INFINITE) == 3) {
                if (player.getInventory().contains(Material.ENDER_PEARL)) {
                    if (event.getBow().getEnchantmentLevel(Enchantment.ARROW_INFINITE) == 2) {
                        event.setCancelled(true);
                        player.getInventory().removeItem(new ItemStack(Material.ENDER_PEARL, 1));
                        player.launchProjectile(EnderPearl.class).setVelocity(event.getProjectile().getVelocity().multiply(1.1));
                        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    } else if (event.getBow().getEnchantmentLevel(Enchantment.ARROW_INFINITE) == 3 ) {
                        event.setCancelled(true);
                        player.launchProjectile(EnderPearl.class).setVelocity(event.getProjectile().getVelocity().multiply(1.1));
                        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    }
                } else {
                     event.setCancelled(true);
                 }
             } else {
                return;
             }
         }
     }
 
 }
