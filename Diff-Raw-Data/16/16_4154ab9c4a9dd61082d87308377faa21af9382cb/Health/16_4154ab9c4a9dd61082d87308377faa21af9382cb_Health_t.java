 package net.battlenexus.paintball.game.items.impl;
 
 import net.battlenexus.paintball.entities.PBPlayer;
 import net.battlenexus.paintball.game.items.AbstractItem;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import java.util.List;
 
 public class Health extends AbstractItem {
     @Override
     public Material getMaterial() {
         return Material.IRON_INGOT;
     }
 
     @Override
     public String getName() {
         return "Increased Health";
     }
 
     @Override
     public boolean canGoInChest() {
         return false;
     }
 
     @Override
     public boolean hasTimer() {
         return false;
     }
 
     @Override
     public boolean hasAmplifier() {
         return true;
     }
 
     @Override
     public String durationMessage() {
         return "Rest of game";
     }
 
     //Options for amplifier should be more than 0
     @Override
     public void addEffect(PBPlayer p, ItemStack is) {
         ItemMeta im = is.getItemMeta();
         if(!is.getItemMeta().hasLore()) {
             return;
         }
         ItemStack other = is.clone();
        other.setAmount(is.getAmount() - 1);
         p.getBukkitPlayer().getInventory().setItem(p.getBukkitPlayer().getInventory().first(is), other);
         List<String> lore = im.getLore();
         int amount = Integer.parseInt(lore.get(1).split(" ")[1]);
         p.increasMaxHealth(amount);
     }
 }
