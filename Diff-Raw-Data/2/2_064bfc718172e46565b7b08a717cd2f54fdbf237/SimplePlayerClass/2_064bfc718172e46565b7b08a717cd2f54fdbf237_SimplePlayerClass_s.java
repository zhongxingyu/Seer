 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import me.limebyte.battlenight.api.util.PlayerClass;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.tosort.Metadata;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.potion.PotionEffect;
 
 public class SimplePlayerClass implements PlayerClass {
     private String name;
     private Permission permission;
     private List<ItemStack> items, armour;
     private List<PotionEffect> effects;
     private HashMap<String, Boolean> permissions;
 
     private static final int LAST_INV_SLOT = 35;
 
     public SimplePlayerClass(String name, List<ItemStack> items, List<ItemStack> armour, List<PotionEffect> effects) {
         this.name = name;
         this.items = items;
         this.armour = armour;
         this.effects = effects;
         permissions = new HashMap<String, Boolean>();
 
         String perm = "battlenight.class." + name.toLowerCase();
         permission = new Permission(perm, "Permission for the class: " + name + ".", PermissionDefault.TRUE);
         try {
             Bukkit.getServer().getPluginManager().addPermission(permission);
         } catch (Exception e) {
         }
     }
 
     @Override
     public void equip(Player player) {
         PlayerInventory inv = player.getInventory();
 
         // Set it
         Metadata.set(player, "class", name);
         
         // Main Inventory
        inv.setContents((ItemStack[]) items.toArray());
 
         // Armour
         inv.setHelmet(armour.get(0));
         inv.setChestplate(armour.get(1));
         inv.setLeggings(armour.get(2));
         inv.setBoots(armour.get(3));
 
         // Effects
         player.addPotionEffects(effects);
         
         // Permissions
         PermissionAttachment perms = player.addAttachment(BattleNight.instance);
         for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
             perms.setPermission(entry.getKey(), entry.getValue());
         }
     }
 
     @Override
     public List<ItemStack> getItems() {
         return items;
     }
 
     @Override
     public List<ItemStack> getArmour() {
         return armour;
     }
 
     @Override
     public List<PotionEffect> getEffects() {
         return effects;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public Permission getPermission() {
         return permission;
     }
 
     @Override
     public HashMap<String, Boolean> getPermissions() {
         return permissions;
     }
 }
