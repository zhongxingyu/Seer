 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.celestiusmc.plasmamachinegun;
 
 import java.util.*;
 import java.util.Map.Entry;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 /**
  * Plasma Machine Gun Listener.
  */
 public class PMGListener implements Listener {
     private Map<Player, Integer> cartridges = new HashMap<Player, Integer>();
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
 
         if (!player.hasPermission("plasmamachinegun.use")) {
             return;
         }
 
         ItemStack item = event.getItem();
 
         if (item == null) {
             return;
         }
 
         if (!item.getType().equals(Material.BLAZE_ROD)) {
             return;
         }
 
         Integer carts = cartridges.get(player);
         if (carts == null) {
             carts = 0;
             cartridges.put(player, carts);
         }
 
         PlayerInventory inventory = player.getInventory();
 
         HashMap<Integer, ? extends ItemStack> nuggets = inventory.all(Material.GOLD_NUGGET);
         if (nuggets.size() < 1) {
             return;
         }
 
         carts++;
         if (carts >= 10) {
             for (Entry<Integer, ? extends ItemStack> nuggetSlot : nuggets.entrySet()) {
                 int slot = nuggetSlot.getKey();
                 ItemStack nugget = nuggetSlot.getValue();
 
                 int amount = nugget.getAmount();
                 if (amount <= 1) {
                     inventory.remove(slot);
                 } else {
                     nugget.setAmount(amount - 1);
                 }
                 break;
             }
             carts = 0;
         }
         cartridges.put(player, carts);
 
         double speed = 20.0;
         Location location = player.getEyeLocation();
         Vector direction = location.getDirection().normalize();
 
         Fireball fb = player.launchProjectile(Fireball.class);
        fb.teleport(location.add(direction));
         fb.setYield(0f);
         fb.setIsIncendiary(false);
 
         fb.setDirection(direction);
         fb.setVelocity(direction.multiply(speed));
 
         //Now get the target
         BlockIterator bitr = new BlockIterator(location, 0.0, 50);
         Block b;
         int bx, by, bz;
         Set<LivingEntity> targets = new HashSet<LivingEntity>();
         while (bitr.hasNext()) {
             b = bitr.next();
             bx = b.getX();
             by = b.getY();
             bz = b.getZ();
 
             if (!isTransparent(b.getType())) {
                 break;
             }
 
             //Get potential targets
             Set<LivingEntity> entities = new HashSet<LivingEntity>();
             Entity temp = b.getLocation().getWorld().spawn(b.getLocation(), ExperienceOrb.class);
             List<Entity> nearbyEntities = temp.getNearbyEntities(0.4, 0.4, 0.4);
             for (Entity n : nearbyEntities) {
                 if (n instanceof LivingEntity) {
                     entities.add((LivingEntity) n);
                 }
             }
             temp.remove();
 
             for (LivingEntity e : entities) {
                 Location l = e.getLocation();
                 int ex = l.getBlockX();
                 int ey = l.getBlockY();
                 int ez = l.getBlockZ();
 
                 if (Math.abs(bx - ex) < 1
                         && Math.abs(by - ey) < 1
                         && Math.abs(bz - ez) < 1) {
                     targets.add(e);
                     entities.remove(e);
                 }
             }
         }
 
         for (LivingEntity target : targets) {
             target.damage(1);
         }
     }
 
     private boolean isTransparent(Material mat) {
         return (mat.equals(Material.AIR));
     }
 
 }
