 package fr.ethilvan.bukkit.drops.customDrops;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import fr.aumgn.bukkitutils.util.Util;
 
 public class CustomDrop {
 
     private String entityName;
     private int id;
     private short data;
     private String enchantmentName;
     private int enchantmentLevel;
     private int amountMin;
     private int amountMax;
     private double rate;
 
     public CustomDrop(String entityName, ItemStack drop, int amountMin,
             int amountMax, double rate) {
         this.entityName = entityName;
         this.id = drop.getTypeId();
         this.data = drop.getDurability();
         this.amountMin = amountMin;
         this.amountMax = amountMax;
         this.rate = rate;
     }
 
     public CustomDrop(String entityName, ItemStack drop, int amountMin,
             int amountMax, double rate, Enchantment enchantment,
             int enchantmentLevel) {
         this(entityName, drop, amountMin, amountMax, rate);
         this.enchantmentName = enchantment.getName();
         this.enchantmentLevel = enchantmentLevel;
     }
 
     public ItemStack toItemStack() {
         int randomAmount = Util.getRandom().nextInt(amountMin, amountMax);
         ItemStack stack = new ItemStack(id, randomAmount, data);
         if (enchantmentName != null || enchantmentLevel != 0) {
             Enchantment enchant = Enchantment.getByName(enchantmentName);
             if (enchant != null) {
                 stack.addUnsafeEnchantment(enchant, enchantmentLevel);
                 if (stack.getType() == Material.WRITTEN_BOOK) {
                     ItemMeta meta = stack.getItemMeta();
                     meta.setDisplayName(ChatColor.YELLOW + meta.getDisplayName());
                 }
             }
         }
         return stack;
     }
 
     public EntityType getEntityType() {
         return EntityType.fromName(entityName);
     }
 
     public String getEntityName() {
         return entityName;
     }
 
     public boolean drop(EntityType entity) {
         return getEntityType() == entity 
                 && Util.getRandom().nextDouble(100) + 1 <= rate;
     }
 }
