 package com.norcode.bukkit.enhancedfishing;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
 
 import net.minecraft.server.v1_5_R2.Enchantment;
 import net.minecraft.server.v1_5_R2.EnchantmentManager;
 import net.minecraft.server.v1_5_R2.Item;
 import net.minecraft.server.v1_5_R2.ItemStack;
 
 public class AnvilCalculator {
 
     public static AnvilResult calculateCost(ItemStack itemstack, ItemStack itemstack2, Set<Integer> validEnchantmentIds) {
         int totalCost= 0;
         int i = 0;
         byte b0 = 0;
         int j = 0;
 
         if (itemstack == null) {
             return new AnvilResult(0, null);
         } else {
             ItemStack itemstack1 = itemstack.cloneItemStack();
             Map<Integer, Integer> map = EnchantmentManager.a(itemstack1);
             Map<Integer, Integer> filteredMap = new HashMap<Integer, Integer>();
             for (Entry<Integer, Integer> e: map.entrySet()) {
                 if (validEnchantmentIds.contains(e.getKey())) {
                     filteredMap.put(e.getKey(), e.getValue());
                 } else {
                 }
             }
             boolean flag = false;
             int k = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
 
             int l;
             int i1;
             int j1;
             int k1;
             int l1;
             Iterator iterator;
             Enchantment enchantment;
 
             if (itemstack2 != null) {
                 flag = itemstack2.id == Item.ENCHANTED_BOOK.id && Item.ENCHANTED_BOOK.g(itemstack2).size() > 0;
                 if (itemstack1.g() && Item.byId[itemstack1.id].a(itemstack, itemstack2)) {
                     l = Math.min(itemstack1.j(), itemstack1.l() / 4);
                     if (l <= 0) {
                         totalCost= 0;
                         return new AnvilResult(0, null);
                     }
 
                     for (i1 = 0; l > 0 && i1 < itemstack2.count; ++i1) {
                         j1 = itemstack1.j() - l;
                         itemstack1.setData(j1);
                         i += Math.max(1, l / 100) + filteredMap.size();
                         l = Math.min(itemstack1.j(), itemstack1.l() / 4);
                     }
                 } else {
                     if (!flag && (itemstack1.id != itemstack2.id || !itemstack1.g())) {
                         totalCost = 0;
                         return new AnvilResult(0, null);
                     }
 
                     if (itemstack1.g() && !flag) {
                         l = itemstack.l() - itemstack.j();
                         i1 = itemstack2.l() - itemstack2.j();
                         j1 = i1 + itemstack1.l() * 12 / 100;
                         int i2 = l + j1;
 
                         k1 = itemstack1.l() - i2;
                         if (k1 < 0) {
                             k1 = 0;
                         }
 
                         if (k1 < itemstack1.getData()) {
                             itemstack1.setData(k1);
                             i += Math.max(1, j1 / 100);
                         }
                     }
 
                     Map<Integer, Integer> map1 = EnchantmentManager.a(itemstack2);
                     Map<Integer, Integer> filteredMap1 = new HashMap<Integer, Integer>();
                     for (Entry<Integer, Integer> e: map1.entrySet()) {
                         if (validEnchantmentIds.contains(e.getKey())) {
                            filteredMap.put(e.getKey(), e.getValue());
                         }
                     }
                     iterator = filteredMap1.keySet().iterator();
 
                     while (iterator.hasNext()) {
                         j1 = ((Integer) iterator.next()).intValue();
                         enchantment = Enchantment.byId[j1];
                         k1 = filteredMap.containsKey(Integer.valueOf(j1)) ? ((Integer) filteredMap.get(Integer.valueOf(j1))).intValue() : 0;
                         l1 = ((Integer) filteredMap1.get(Integer.valueOf(j1))).intValue();
                         int j2;
 
                         if (k1 == l1) {
                             ++l1;
                             j2 = l1;
                         } else {
                             j2 = Math.max(l1, k1);
                         }
 
                         l1 = j2;
                         int k2 = l1 - k1;
                         boolean flag1 = true; //enchantment.canEnchant(itemstack);
 
 //                        if (this.n.abilities.canInstantlyBuild || itemstack.id == ItemEnchantedBook.ENCHANTED_BOOK.id) {
 //                            flag1 = true;
 //                        }
 
                         Iterator iterator1 = filteredMap.keySet().iterator();
 
                         while (iterator1.hasNext()) {
                             int l2 = ((Integer) iterator1.next()).intValue();
                             if (l2 != j1 && !enchantment.a(Enchantment.byId[l2])) {
                                 //flag1 = false;
                                 i += k2;
                             }
                         }
 
                         if (flag1) {
                             if (l1 > enchantment.getMaxLevel()) {
                                 l1 = enchantment.getMaxLevel();
                             }
 
                             filteredMap.put(Integer.valueOf(j1), Integer.valueOf(l1));
                             int i3 = 0;
 
                             switch (enchantment.getRandomWeight()) {
                             case 1:
                                 i3 = 8;
                                 break;
 
                             case 2:
                                 i3 = 4;
 
                             case 3:
                             case 4:
                             case 6:
                             case 7:
                             case 8:
                             case 9:
                             default:
                                 break;
 
                             case 5:
                                 i3 = 2;
                                 break;
 
                             case 10:
                                 i3 = 1;
                             }
 
                             if (flag) {
                                 i3 = Math.max(1, i3 / 2);
                             }
 
                             i += i3 * k2;
                         }
                     }
                 }
             }
 //
 //            if (this.m != null && !this.m.equalsIgnoreCase(itemstack.getName()) && this.m.length() > 0) {
 //                j = itemstack.g() ? 7 : itemstack.count * 5;
 //                i += j;
 //                if (itemstack.hasName()) {
 //                    k += j / 2;
 //                }
 //
 //                itemstack1.c(this.m);
 //            }
 
             l = 0;
 
             for (iterator = filteredMap.keySet().iterator(); iterator.hasNext(); k += l + k1 * l1) {
                 j1 = ((Integer) iterator.next()).intValue();
                 enchantment = Enchantment.byId[j1];
                 k1 = ((Integer) filteredMap.get(Integer.valueOf(j1))).intValue();
                 l1 = 0;
                 ++l;
                 switch (enchantment.getRandomWeight()) {
                 case 1:
                     l1 = 8;
                     break;
 
                 case 2:
                     l1 = 4;
 
                 case 3:
                 case 4:
                 case 6:
                 case 7:
                 case 8:
                 case 9:
                 default:
                     break;
 
                 case 5:
                     l1 = 2;
                     break;
 
                 case 10:
                     l1 = 1;
                 }
 
                 if (flag) {
                     l1 = Math.max(1, l1 / 2);
                 }
             }
 
             if (flag) {
                 k = Math.max(1, k / 2);
             }
 
             totalCost= k + i;
             if (i <= 0) {
                 itemstack1 = null;
             }
 
             if (j == i && j > 0 && totalCost>= 40) {
                 // this.h.getLogger().info("Naming an item only, cost too high; giving discount to cap cost to 39 levels"); // CraftBukkit - remove debug
                 totalCost= 39;
             }
 
             if (totalCost>= 40) {// && !this.n.abilities.canInstantlyBuild) {
                 itemstack1 = null;
             }
 
             if (itemstack1 != null) {
                 i1 = itemstack1.getRepairCost();
                 if (itemstack2 != null && i1 < itemstack2.getRepairCost()) {
                     i1 = itemstack2.getRepairCost();
                 }
 
                 if (itemstack1.hasName()) {
                     i1 -= 9;
                 }
 
                 if (i1 < 0) {
                     i1 = 0;
                 }
 
                 i1 += 2;
                 itemstack1.setRepairCost(i1);
                 EnchantmentManager.a(filteredMap, itemstack1);
             }
             return new AnvilResult(totalCost, itemstack1 == null ? null : CraftItemStack.asBukkitCopy(itemstack1).getEnchantments());
         }
     }
 
     public static class AnvilResult {
         int cost;
         Map<org.bukkit.enchantments.Enchantment, Integer> enchantments;
         public AnvilResult(int cost, Map<org.bukkit.enchantments.Enchantment, Integer> enchantments) {
             this.cost = cost;
             this.enchantments = enchantments;
         }
         public int getCost() {
             return cost;
         }
         public void setCost(int cost) {
             this.cost = cost;
         }
         public Map<org.bukkit.enchantments.Enchantment, Integer> getEnchantments() {
             return enchantments;
         }
         public void setStack(Map<org.bukkit.enchantments.Enchantment, Integer> map) {
             this.enchantments = map;
         }
     }
 
 }
