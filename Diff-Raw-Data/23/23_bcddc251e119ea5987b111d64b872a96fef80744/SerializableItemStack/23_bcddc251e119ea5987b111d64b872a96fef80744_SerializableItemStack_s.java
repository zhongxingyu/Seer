 /**
  * LICENSING
  * 
  * This software is copyright by sunkid <sunkid@iminurnetz.com> and is
  * distributed under a dual license:
  * 
  * Non-Commercial Use:
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Commercial Use:
  *    Please contact sunkid@iminurnetz.com
  */
 package com.iminurnetz.bukkit.util;
 
 import java.io.Serializable;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
import flexjson.JSON;

 public class SerializableItemStack implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private String material;
     private short damage;
     private int amount;
     private byte data;
     
     public SerializableItemStack() {
     }
 
     public SerializableItemStack(ItemStack stack) {
         this.material = stack.getType().name();
         this.damage = stack.getDurability();
         this.amount = stack.getAmount();
         this.data = stack.getData() != null ? stack.getData().getData()
                 : (byte) damage;
     }
 
    @JSON(include=false)
     public ItemStack getStack() {
         Material m = Material.getMaterial(material);
         ItemStack stack = new ItemStack(m, amount);
         stack.setDurability(damage);
         stack.setData(m.getNewData(data == 0 ? (byte) damage : data));
         return stack;
     }
 
     @Override
     public String toString() {
         StringBuilder s = new StringBuilder();
         s.append(amount).append(" x ").append(material).append(":")
                 .append(data).append(" (").append(damage).append(")");
         return s.toString();
     }
 
     public String getMaterial() {
         return material;
     }
 
     public short getDamage() {
         return damage;
     }
 
     public int getAmount() {
         return amount;
     }
 
     public byte getData() {
         return data;
     }
     
     public void setMaterial(String material) {
         this.material = material;
     }
 
     public void setDamage(short damage) {
         this.damage = damage;
     }
 
     public void setAmount(int amount) {
         this.amount = amount;
     }
 
     public void setData(byte data) {
         this.data = data;
     }
 }
