 package me.limebyte.battlenight.core.util;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class BattleClass {
     private String name;
     private List<ItemStack> items, armour;
 
     public BattleClass(String name, List<ItemStack> items, List<ItemStack> armour) {
         this.name = name;
         this.items = items;
         this.armour = armour;
     }
 
     public String getName() {
         return name;
     }
 
     public List<ItemStack> getItems() {
         return items;
     }
 
     public List<ItemStack> getArmour() {
         return armour;
     }
 
     public void equip(Player player) {
         PlayerInventory inv = player.getInventory();
 
        inv.setContents((ItemStack[]) items.toArray());
         inv.setHelmet(armour.get(0));
         inv.setChestplate(armour.get(1));
         inv.setLeggings(armour.get(2));
         inv.setBoots(armour.get(3));
     }
 }
