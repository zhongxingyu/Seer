 package com.geNAZt.RegionShop.Database.Model;
 
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.ItemStorageHolder;
 import com.geNAZt.RegionShop.Database.Table.ItemMeta;
 import com.geNAZt.RegionShop.Database.Table.ItemMetaID;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.enchantments.EnchantmentWrapper;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 01.09.13
  */
 public class Item {
     public static ItemMeta getMeta(ItemStack itemStack) {
         return Database.getServer().find(ItemMeta.class).
                 where().
                     eq("item_id", itemStack.getTypeId()).
                     eq("data_value", itemStack.getData().getData()).
                 findUnique();
     }
 
     public static boolean hasMeta(ItemStack itemStack) {
         return !(getMeta(itemStack) == null);
     }
 
     public static void createMeta(ItemStack itemStack) {
         ItemMeta itemMeta = new ItemMeta();
         itemMeta.setId(new ItemMetaID(itemStack.getTypeId(), itemStack.getData().getData()));
         itemMeta.setMaxStackSize(itemStack.getType().getMaxStackSize());
         itemMeta.setMaxDurability(itemStack.getType().getMaxDurability());
 
         Database.getServer().save(itemMeta);
     }
 
     public static ItemStack fromDBItem(Items item) {
         ItemStack iStack = new ItemStack(Material.getMaterial(item.getMeta().getId().getItemID()), 1);
 
         if(item.getMeta().getId().getDataValue() > 0) {
             iStack.getData().setData(item.getMeta().getId().getDataValue());
         }
 
         if(item.getDurability() > 0) {
             iStack.setDurability(item.getDurability());
         } else {
             iStack.setDurability((short) item.getMeta().getId().getDataValue());
         }
 
         List<com.geNAZt.RegionShop.Database.Table.Enchantment> enchants = Database.getServer().find(com.geNAZt.RegionShop.Database.Table.Enchantment.class).
                 setUseQueryCache(true).
                 where().
                     eq("item", item).
                 findList();
 
         if(enchants.size() > 0) {
             for(com.geNAZt.RegionShop.Database.Table.Enchantment ench : enchants) {
                 Enchantment enchObj = new EnchantmentWrapper(ench.getEnchId()).getEnchantment();
                 iStack.addEnchantment(enchObj, ench.getEnchLvl());
             }
         }
 
         if(item.getCustomName() != null) {
             org.bukkit.inventory.meta.ItemMeta iMeta = iStack.getItemMeta();
             iMeta.setDisplayName(item.getCustomName());
             iStack.setItemMeta(iMeta);
         }
 
         return iStack;
     }
 
     public static Items toDBItem(ItemStack item, ItemStorageHolder region, String owner, Float buy, Float sell, Integer amount) {
         if(!hasMeta(item)) {
             createMeta(item);
         }
 
         ItemMeta itemMeta = getMeta(item);
 
         Items newItem = new Items();
         newItem.setMeta(itemMeta);
         newItem.setItemStorage(region.getItemStorage());
         newItem.setCurrentAmount(item.getAmount());
         newItem.setDurability(item.getDurability());
         newItem.setOwner(owner);
        newItem.setCustomName((item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) ? item.getItemMeta().getDisplayName() : null);
 
         newItem.setBuy(buy);
         newItem.setSell(sell);
         newItem.setUnitAmount(amount);
 
         Database.getServer().save(newItem);
 
         Map<Enchantment, Integer> itemEnch = item.getEnchantments();
         if(itemEnch != null) {
             for(Map.Entry<Enchantment, Integer> entry : itemEnch.entrySet()) {
                 com.geNAZt.RegionShop.Database.Table.Enchantment ench = new com.geNAZt.RegionShop.Database.Table.Enchantment();
                 ench.setEnchId(entry.getKey().getId());
                 ench.setEnchLvl(entry.getValue());
                 ench.setItem(newItem);
 
                 Database.getServer().save(ench);
             }
         }
 
         region.getItemStorage().setItemAmount(region.getItemStorage().getItemAmount() + item.getAmount());
 
         Database.getServer().update(region.getItemStorage());
 
         return newItem;
     }
 }
