 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import net.minecraft.server.Container;
 import net.minecraft.server.ContainerPlayer;
 import net.minecraft.server.ContainerWorkbench;
 import net.minecraft.server.InventoryCraftResult;
 import net.minecraft.server.InventoryCrafting;
 import net.minecraft.server.ItemStack;
 
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.inventory.InventoryChangedEvent;
 
 public class SkillRepairWeapon extends PassiveSkill {
 
     public SkillRepairWeapon(Heroes plugin) {
         super(plugin);
         name = "RepairWeapon";
         description = "Allows materials to repair weapons (passive)";
         usage = "Drop material on weapon";
         minArgs = 0;
         maxArgs = 0;
         plugin.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new SkillRepairListener(), Priority.Monitor, plugin);
     }
 
     private class SkillRepairListener extends CustomEventListener {
 
         @Override
         public void onCustomEvent(Event event) {
             if (event instanceof InventoryChangedEvent) {
                 InventoryChangedEvent subEvent = (InventoryChangedEvent) event;
                 CraftPlayer player = (CraftPlayer) subEvent.getPlayer();
                 Container container = player.getHandle().activeContainer;
                 InventoryCrafting ic;
                 InventoryCraftResult icr;
                 if (container instanceof ContainerPlayer) {
                     ContainerPlayer cp = (ContainerPlayer) container;
                     ic = cp.a;
                     icr = (InventoryCraftResult) cp.b;
                 } else if (container instanceof ContainerWorkbench) {
                     ContainerWorkbench cw = (ContainerWorkbench) container;
                     ic = cw.a;
                     icr = (InventoryCraftResult) cw.b;
                 } else {
                     return;
                 }
                 ItemStack[] items = ic.getContents();
                 int weaponPos = -1;
                 int repairPos = -1;
                 int repairLevel = -1;
                 int repairTypeId = -1;
                 for (int i = 0; i < items.length; i++) {
                     ItemStack is = items[i];
                     if (is == null) {
                         continue;
                     }
                     String[] name = Material.getMaterial(is.id).toString().split("_", 2);
                    if (name[1].equals("SWORD")) {
                         int newLevel = -1;
                         if (name[0].equals("WOOD")) {
                             newLevel = 0;
                             repairTypeId = Material.WOOD.getId();
                         } else if (name[0].equals("STONE")) {
                             newLevel = 1;
                             repairTypeId = Material.COBBLESTONE.getId();
                         } else if (name[0].equals("IRON")) {
                             newLevel = 2;
                             repairTypeId = Material.IRON_INGOT.getId();
                         } else if (name[0].equals("GOLD")) {
                             newLevel = 3;
                             repairTypeId = Material.GOLD_INGOT.getId();
                         } else if (name[0].equals("DIAMOND")) {
                             newLevel = 4;
                             repairTypeId = Material.DIAMOND.getId();
                         }
                         if (newLevel > repairLevel) {
                             repairLevel = newLevel;
                             weaponPos = i;
                         }
                     }
                 }
                 if (weaponPos == -1) {
                     return;
                 }
                 for (int i = 0; i < items.length; i++) {
                     ItemStack is = items[i];
                     if (is == null) {
                         continue;
                     }
                     if (is.id == repairTypeId) {
                         repairPos = i;
                     }
                 }
                 if (repairPos == -1) {
                     return;
                 }
 
                 ItemStack weapon = items[weaponPos];
                 ItemStack repair = items[repairPos];
                 if (repair.count == 1) {
                     items[repairPos] = null;
                 } else {
                     repair.count--;
                 }
                 weapon.damage = weapon.damage < 50 ? 0 : weapon.damage - 50;
                 items[weaponPos] = null;
                 icr.setItem(0, weapon);
             }
         }
     }
 }
