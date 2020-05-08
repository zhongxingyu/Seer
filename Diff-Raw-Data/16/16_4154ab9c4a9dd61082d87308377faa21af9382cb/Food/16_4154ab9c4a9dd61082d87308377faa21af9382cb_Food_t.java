 package net.battlenexus.paintball.game.items.impl;
 
 import net.battlenexus.paintball.entities.PBPlayer;
 import net.battlenexus.paintball.game.items.AbstractItem;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import java.util.List;
 
 public class Food extends AbstractItem {
     @Override
     public Material getMaterial() {
         return Material.GOLD_NUGGET;
     }
 
     @Override
     public String getName() {
         return "Food Refill";
     }
 
     @Override
     public boolean canGoInChest() {
         return true;
     }
 
     @Override
     public boolean hasTimer() {
         return false;
     }
 
     @Override
     public boolean hasAmplifier() {
         return false;
     }
 
     @Override
     public String durationMessage() {
         return "Instant";
     }
 
     //Options for amplifier should be more than 0
     @Override
     public void addEffect(PBPlayer p, ItemStack is) {
         if(!is.getItemMeta().hasLore()) {
             return;
         }
         ItemStack other = is.clone();
        other.setAmount(is.getAmount() - 1);
         p.getBukkitPlayer().getInventory().setItem(p.getBukkitPlayer().getInventory().first(is), other);
         p.getBukkitPlayer().setFoodLevel(20);
     }
 }
