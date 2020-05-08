 package net.battlenexus.paintball.game.items.impl;
 
 import net.battlenexus.paintball.game.items.AbstractItem;
 import net.battlenexus.paintball.entities.PBPlayer;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Material;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import java.util.List;
 
 public class Speed extends AbstractItem {
     @Override
     public Material getMaterial() {
         return Material.FEATHER;
     }
 
     @Override
     public String getName() {
         return "Speed Boost";
     }
 
     @Override
     public boolean canGoInChest() {
         return true;
     }
 
     @Override
     public String durationMessage() {
         return ""; //Does not get called because there is a timer
     }
 
     @Override
     public boolean hasAmplifier() {
         return true;
     }
 
     @Override
     public boolean hasTimer() {
         return true;
     }
 
     //Options for amplifier should be 0, or 1, max ever 3
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
         String duration = lore.get(0).split(" ")[1];
         int amplifier = Integer.parseInt(lore.get(1).split(" ")[1]);
         int dur = Integer.parseInt(duration.split(":")[0]) * 60 + Integer.parseInt(duration.split(":")[1]);
         dur *= 20; //Turns seconds into ticks
         PotionEffect potion = new PotionEffect(PotionEffectType.SPEED, dur, amplifier);
         p.getBukkitPlayer().addPotionEffect(potion);
     }
 }
