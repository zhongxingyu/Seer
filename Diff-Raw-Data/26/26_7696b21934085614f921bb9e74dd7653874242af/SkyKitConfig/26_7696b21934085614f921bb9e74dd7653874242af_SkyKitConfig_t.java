 /*
  * Copyright (C) 2013-2014 Dabo Ross <http://www.daboross.net/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.skywars.api.kits.impl;
 
 import java.util.Collections;
 import java.util.List;
 import lombok.Getter;
 import lombok.ToString;
 import net.daboross.bukkitdev.skywars.api.kits.SkyKit;
 import net.daboross.bukkitdev.skywars.api.kits.SkyKitItem;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 @ToString
 public class SkyKitConfig implements SkyKit {
 
     private final List<SkyKitItem> inventoryContents;
     private final List<SkyKitItem> armorContents;
     @Getter
     private final int cost;
     @Getter
     private final String permission;
     @Getter
     private final String name;
 
     public SkyKitConfig(List<SkyKitItem> inventoryContents, List<SkyKitItem> armorContents, String name, int cost, String permissionNode) {
         this.inventoryContents = inventoryContents;
         if (armorContents.size() != 4) {
             throw new IllegalArgumentException("Armor contents size not 4");
         }
         this.armorContents = armorContents;
         this.name = name;
         this.cost = cost;
         this.permission = permissionNode;
     }
 
     @Override
     public void applyTo(Player p) {
        PlayerInventory inv = p.getInventory();
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            SkyKitItem skyKitItem = armorContents.get(i);
            if (skyKitItem != null) {
                armor[i] = skyKitItem.toItem();
            }
        }
        inv.setArmorContents(armor);
        ItemStack[] contents = new ItemStack[inventoryContents.size()];
        for (int i = 0; i < contents.length; i++) {
            SkyKitItem skyKitItem = inventoryContents.get(i);
            if (skyKitItem != null) {
                contents[i] = skyKitItem.toItem();
            }
        }
        inv.setContents(contents);
     }
 
     @Override
     public List<SkyKitItem> getArmorContents() {
         return Collections.unmodifiableList(armorContents);
     }
 
     @Override
     public List<SkyKitItem> getInventoryContents() {
         return Collections.unmodifiableList(inventoryContents);
     }
 }
