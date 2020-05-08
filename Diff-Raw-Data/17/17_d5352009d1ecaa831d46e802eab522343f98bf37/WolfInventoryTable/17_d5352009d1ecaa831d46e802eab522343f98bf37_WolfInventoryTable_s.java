 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Wolf.
  *
  * Wolf is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Wolf is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Wolf.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.Wolf.wolf;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Represents a WolfInventoryTable
  * 
  * @author halvors
  */
 @Entity()
 @Table(name = "wolf_wolfInventory")
 public class WolfInventoryTable {
     @Id
     private int id;
    private String unigueId;
     private String chestRow1;
     private String chestRow2;
     private String chestRow3;
    
    public WolfInventoryTable() {
        chestRow1 = "0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0";
        chestRow2 = "0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0";
        chestRow3 = "0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0;0:0:0";
    }
    
     public int getId() {
         return id;
     }
     
     public void setId(final int id) {
         this.id = id;
     }
     
     public String getUnigueId() {
    	return unigueId;
     }
     
     public void setUnigueId(String uniqueId) {
    	this.unigueId = uniqueId;
     }
     
     public String getChestRow1() {
         return this.chestRow1;
     }
 
     public void setChestRow1(String chestRow1) {
         this.chestRow1 = chestRow1;
     }
 
     public String getChestRow2() {
         return this.chestRow2;
     }
 
     public void setChestRow2(String chestRow2) {
         this.chestRow2 = chestRow2;
     }
 
     public String getChestRow3() {
         return this.chestRow3;
     }
     
     public void setChestRow3(String chestRow3) {
         this.chestRow3 = chestRow3;
     }
     
     public void setItemStack(int stackId, ItemStack stack) {
         if (stackId >= 1 && stackId <= 9) {
             setItemStack(1,stackId - 1, stack);
         } else if (stackId >= 10 && stackId <= 18) {
             setItemStack(2,stackId - 10, stack);
         } else if (stackId >= 19 && stackId <= 27) {
             setItemStack(3,stackId - 19, stack);
         }
                 
     }
     
     public void setItemStack(int row, int column, ItemStack stack) {
         String chestRow = "";
         
         if (row == 1) {
             chestRow = chestRow1;
         } else if (row == 2) {
             chestRow = chestRow2;
         } else if (row == 3) {
             chestRow = chestRow3;
         }
         
         String[] columns = chestRow.split(";");
         columns[column] = getStackString(stack);
         
         for (int i = 0; i < 9; i++) {
             chestRow += columns[i];
             
             if (i != 8){
                 chestRow += ";";
             }
         }
         
         if (row == 1) {
             chestRow1 = chestRow;
         } else if (row == 2) {
             chestRow2 = chestRow;
         } else if (row == 3) {
             chestRow3 = chestRow;
         }
     }
     
     public void setContents(List<ItemStack> stacks) {
         ItemStack[] stack = stacks.toArray(new ItemStack[0]);
         
         for (int i = 0; i < 27; i++) {
             if (i < stack.length) {
                 setItemStack(i, stack[i]);
             } else {
                 setItemStack(i, null);
             }
         }
     }
     
     public String getStackString(ItemStack stack) {
         String out = null;
         
         if (stack != null) {
             out = stack.getTypeId() + ":" + stack.getDurability() + ":" + stack.getAmount();
         } else {
             out = "0:0:0";
         }
         
         return out;
     }
     
     public ItemStack getItemStack(int stackId) {
         ItemStack stack = new ItemStack(0);
         
         if (stackId >= 1 && stackId <= 9) {
             stack = getItemStack(1, stackId - 1);
         } else if (stackId >= 10 && stackId <= 18) {
             stack =  getItemStack(2, stackId - 10);
         } else if (stackId >= 19 && stackId <= 27) {
             stack =  getItemStack(3, stackId - 19);
         }
         
         return stack;
     }
     
     public ItemStack getItemStack(int row, int column) {
         String chestRow = "";
         
         if (row == 1) {
             chestRow = chestRow1;
         } else if (row == 2) {
             chestRow = chestRow2;
         } else if (row == 3) {
             chestRow = chestRow3;
         }
         
         String[] columns = chestRow.split(";");
         String[] itemStack = columns[column].split(":");
         int typeId = Integer.valueOf(itemStack[0]);
         short damage = Short.valueOf(itemStack[1]);
         int amount = Integer.valueOf(itemStack[2]);
         
         ItemStack stack = new ItemStack(typeId, amount, damage);
         
         return stack;
     }
     
     public ItemStack[] getItemStacks() {
         ItemStack[] stack = new ItemStack[27];
         
         for (int i = 0; i < 27; i++) {
             stack[i] = getItemStack(i);
         }
                
         return stack;
         
     }
     
     public List<ItemStack> getItemStackList() {
         List<ItemStack> stacks = new ArrayList<ItemStack>(27);
         
         for (ItemStack iStack : getItemStacks()) {
             stacks.add(iStack);
         }
         
         return stacks;
     }
 }
