 package me.DDoS.Quarantine.util;
 
 /**
  *
  * @author DDoS
  */
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 public class QInventoryItem implements Serializable {
 
     private static final long serialVersionUID = -6698956186490861187L;
     //
     private final int typeId;
     private final int amount;
     private final short durability;
     private final Map<Integer, Integer> enchantments = new HashMap<Integer, Integer>();
 
     public QInventoryItem(ItemStack item) {
 
         if (item == null) {
 
             this.typeId = -1;
             this.amount = 0;
             this.durability = 0;
 
         } else {
 
             this.typeId = item.getTypeId();
             this.amount = item.getAmount();
             this.durability = item.getDurability();
 
             for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
 
                 enchantments.put(entry.getKey().getId(), entry.getValue());
 
             }
         }
     }
 
     public ItemStack getItem() {
 
         if (typeId == -1) {
 
             return null;
 
         }
 
         ItemStack item = new ItemStack(typeId, amount, durability);
 
         if (enchantments != null) {
 
             for (Entry<Integer, Integer> entry : enchantments.entrySet()) {
 
                Enchantment enchantment = Enchantment.getById(entry.getKey());
                
                if (!enchantment.canEnchantItem(item)) {
                    
                    continue;
                    
                }
                
                int level = entry.getValue();
                
                if (level < enchantment.getStartLevel()) {
                    
                    level = enchantment.getStartLevel();
                    
                } else if (level > enchantment.getMaxLevel()) {
                    
                    level = enchantment.getMaxLevel();
                    
                }

                item.addEnchantment(enchantment, level);
 
             }
         }
 
         return item;
 
     }
 }
