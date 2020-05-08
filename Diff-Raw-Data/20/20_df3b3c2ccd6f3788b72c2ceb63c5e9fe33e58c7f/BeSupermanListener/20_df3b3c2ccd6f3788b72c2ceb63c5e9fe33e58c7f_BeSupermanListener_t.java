 package me.C0nsole.BeSuperman;
  
  
  
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Damageable;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffectType;
  
 public class BeSupermanListener implements Listener{
         private BeSuperman plugin;
         private List<String> supermen;
  
         public BeSupermanListener(BeSuperman instance) {
                 plugin = instance;
                 supermen = plugin.getList();
         }
         @EventHandler
         public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                Entity entity = event.getEntity();
                Entity damager = event.getDamager();
                if (damager instanceof Player) {
                        if (supermen.contains(((Player)damager).getName())) {
                                if (entity instanceof LivingEntity) {
                                        ((LivingEntity)entity).setHealth(0);
                                } else if (entity instanceof Player) {
                                        Player player = (Player)entity;
                                        player.setHealth(Math.max(player.getHealth() - 20, 0));
                                }
                        }
                 }
         }
        
         @EventHandler
         public void onPlayerQuitEvent(PlayerQuitEvent event){
         	Player player = event.getPlayer();
         	if(supermen.contains(player.getName())){
         		player.setAllowFlight(false);
                 player.setFlying(false);
                 player.setMaxHealth(20);
                 Inventory inv = player.getInventory();
                 if(inv instanceof PlayerInventory){
                         PlayerInventory pinv = (PlayerInventory) inv;
                         pinv.setChestplate(new ItemStack(Material.AIR));
                         pinv.setHelmet(new ItemStack(Material.AIR));
                         pinv.setLeggings(new ItemStack(Material.AIR));
                         pinv.setBoots(new ItemStack(Material.AIR));
                        
                 }
                 player.removePotionEffect(PotionEffectType.WATER_BREATHING);
                 supermen.remove(player.getName());
                 }
         	}
         
         @EventHandler
         public void onPlayerDeath(PlayerDeathEvent event) {
         	if(supermen.contains(event.getEntity())){
         		event.getEntity().removePotionEffect(PotionEffectType.WATER_BREATHING);
         		event.getEntity().setAllowFlight(false);
         		event.getEntity().setFlying(false);
         		supermen.remove(event.getEntity());
         		event.getEntity().setMaxHealth(20);
         	}
         	
         }
   }
                
