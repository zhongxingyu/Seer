 package com.github.ribesg.magicegg.util;
 
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 
 public class EnchantmentRandomizer {
 
     public enum Result {
         CLEAN, LOSS, NONE, BOOST, OVERBOOST
     }
 
     private final Set<Integer> goldItems = new HashSet<Integer>();
 
     private final Random       rand      = new Random(new Random().nextLong());
 
     private final float        cleanEnchThreshold;
     private final float        enchLossThreshold;
     private final float        noChangeThreshold;
     private final float        enchBoostThreshold;
     private final float        enchOverBoostThreshold;
 
     public EnchantmentRandomizer(final float cleanEnch, final float enchLoss, final float noChange, final float enchBoost, final float enchOverBoost) {
         this.cleanEnchThreshold = cleanEnch;
         this.enchLossThreshold = this.cleanEnchThreshold + enchLoss;
         this.noChangeThreshold = this.enchLossThreshold + noChange;
         this.enchBoostThreshold = this.noChangeThreshold + enchBoost;
         this.enchOverBoostThreshold = this.enchBoostThreshold + enchOverBoost; // Should be == 1
 
         this.goldItems.add(283);
         this.goldItems.add(284);
         this.goldItems.add(285);
         this.goldItems.add(286);
         this.goldItems.add(314);
         this.goldItems.add(315);
         this.goldItems.add(316);
         this.goldItems.add(317);
     }
 
     public Result randomize(final ItemStack i) {
         final int nbEnchant = i.getEnchantments().size();
         float val = this.rand.nextFloat();
         if (this.goldItems.contains(i.getTypeId())) {
             val += this.cleanEnchThreshold;
             if (val > 1) {
                 val = 1;
             }
         }
         if (val <= this.cleanEnchThreshold) {
             for (final Enchantment e : i.getEnchantments().keySet()) {
                 i.removeEnchantment(e);
             }
             return Result.CLEAN;
         } else if (val <= this.enchLossThreshold) {
             final float luck = nbEnchant == 1 ? 0.95f : nbEnchant == 2 ? 0.90f : nbEnchant == 3 ? 0.85f : 0.75f;
             for (final Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
                 if (this.rand.nextFloat() <= luck && entry.getValue() > 1) {
                     i.addUnsafeEnchantment(entry.getKey(), entry.getValue() - 1);
                 }
             }
             return Result.LOSS;
         } else if (val <= this.noChangeThreshold) {
             // Nothing !
             return Result.NONE;
         } else if (val <= this.enchBoostThreshold) {
             final float luck = nbEnchant == 1 ? 0.95f : nbEnchant == 2 ? 0.90f : nbEnchant == 3 ? 0.85f : 0.75f;
             for (final Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
                 if (this.rand.nextFloat() <= luck && entry.getValue() < 7) {
                     i.addUnsafeEnchantment(entry.getKey(), entry.getValue() + 1);
                 }
             }
             return Result.BOOST;
         } else if (val <= this.enchOverBoostThreshold) {
             final float luck = nbEnchant == 1 ? 0.95f : nbEnchant == 2 ? 0.90f : nbEnchant == 3 ? 0.85f : 0.75f;
             for (final Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
                 final float randVal = this.rand.nextFloat();
                if (randVal <= luck) {
                     if (entry.getValue() <= 7) {
                         i.addUnsafeEnchantment(entry.getKey(), entry.getValue() + ((int) (randVal * 100) % 2 == 0 ? 3 : 2));
                     }
                 } else {
                     if (entry.getValue() <= 7) {
                         i.addUnsafeEnchantment(entry.getKey(), entry.getValue() + 1);
                     }
                 }
             }
             return Result.OVERBOOST;
         } else {
             // No chance to go there, but this could reveal us a bug.
             return null;
         }
     }
 }
