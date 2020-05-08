package com.github.fjederhaek.moreweapons;
 
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class Events implements Listener {
 
     //found it on the internet// it should make u shoot an item (redstone) like it was an arrow
 	/*@EventHandler
      public void onInteract(PlayerInteractEvent e) {
      Player p = e.getPlayer();
      World world = p.getWorld();
      if(e.getAction() == Action.RIGHT_CLICK_AIR && p.getItemInHand().getType() == Material.STICK){
      final Item grenade = world.dropItem(p.getEyeLocation(), new ItemStack(Material.REDSTONE));
      grenade.setVelocity(p.getLocation().getDirection().normalize().multiply(2));
      }
      }
     */
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerInteractChopperGunner(PlayerInteractEvent evt) {
         if (Killstreaks.useKillstreak(evt.getPlayer(), "choppergunner")) {
             PlayerInventory inventory = evt.getPlayer().getInventory(); // The player's inventory
             ItemStack itemstack = new ItemStack(Material.SHEARS, 1); // a Thundergunner activator	
             //insert code using inventory and itemstack here
         }
     }
 
     // Egg grenades
     @EventHandler(priority = EventPriority.HIGHEST)
     public void rightclick(PlayerEggThrowEvent e) {
         Egg egg = e.getEgg();
         egg.setVelocity(egg.getVelocity().multiply(0.001));
         e.setHatchingType(EntityType.PRIMED_TNT);
         e.setHatching(true);
         e.setNumHatches(Byte.valueOf("1"));
     }
 
     // Some testing
     @EventHandler(priority = EventPriority.HIGH)
     public void rightclick(EntityShootBowEvent shooter) {
         LivingEntity en = shooter.getEntity();
         ItemStack bow = shooter.getBow();
         en.getServer().broadcastMessage(bow.serialize().toString()); //debugging
         if (bow.containsEnchantment(Enchantment.ARROW_DAMAGE)) {
             PotionEffect slownessPotion = new PotionEffect(PotionEffectType.SLOW, 10, 4);
             slownessPotion.apply(en);
         }
     }
 
     // Lightning attack (killstreak)
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteractThundergun(PlayerInteractEvent evt) {
         if (evt.getPlayer().getItemInHand().getTypeId() == Material.BLAZE_ROD.getId()) {
             if (Killstreaks.useKillstreak(evt.getPlayer(), "lightning strike")) {
                 //maximal distance between player and lightning is 150 blocks
                 evt.getPlayer().getWorld().strikeLightning(evt.getPlayer().getTargetBlock(null, 150).getLocation());
             }
         }
     }
     //scope for sniper
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerEnterScope(PlayerInteractEvent evt) {
         if (evt.getPlayer().getItemInHand().getTypeId() == Material.BOW.getId()
                 && (evt.getAction().equals(Action.LEFT_CLICK_AIR)
                 || evt.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
             boolean hasSlowness = evt.getPlayer().hasPotionEffect(PotionEffectType.SLOW);
             evt.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0));
             evt.getPlayer().getInventory().getHelmet().setTypeId(Material.PUMPKIN.getId());
         } else if (evt.getPlayer().getInventory().getHelmet().getTypeId() == Material.PUMPKIN.getId()) {
         }
     }
 
     // Handle PvP damage
     @EventHandler
     public void handlerAttackDefend(EntityDamageByEntityEvent event) {
         Entity attacker = event.getDamager();
         Entity defender = event.getEntity();
 
         // Maybe an arrow was shot
         if (attacker != null && event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
             attacker = ((Player) ((Projectile) event.getDamager()).getShooter());
         }
         if (attacker != null && defender != null && attacker instanceof Player && defender instanceof Player) {
             Player a = (Player) attacker;
             Player d = (Player) defender;
 
             if (event.getDamage() >= d.getHealth()) {
                 if (a != d) {
                     Killstreaks.addKillcount(a, 1);
                 }
                 Killstreaks.resetKillcount(a);
             }
         }
     }
}
