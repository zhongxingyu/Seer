 package com.runetooncraft.plugins.EasyMobArmory.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
 
 import net.minecraft.server.v1_6_R2.NBTBase;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.NBTTagList;
 
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventoryCustom;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class InventorySerializer {
 	public static Inventory getArmorInventory(PlayerInventory inventory) {
         ItemStack[] a = inventory.getArmorContents();
         CraftInventoryCustom storage = new CraftInventoryCustom(null, a.length);
  
         for (int i = 0; i < a.length; i++)
             storage.setItem(i, a[i]);
  
         return storage;
 	}
 	public static Inventory getArmorEntityInventory(EntityEquipment inventory) {
         ItemStack[] a = inventory.getArmorContents();
         CraftInventoryCustom storage = new CraftInventoryCustom(null, a.length);
  
         for (int i = 0; i < a.length; i++)
             storage.setItem(i, a[i]);
  
         return storage;
 	}
 	
 	public static String tobase64(Inventory inventory) {
 		ByteArrayOutputStream outputStream = new  ByteArrayOutputStream();
 		DataOutputStream dataOutput = new DataOutputStream(outputStream);
 		NBTTagList itemList = new NBTTagList();
 		for (int i = 0; i < inventory.getSize(); i++) {
 			NBTTagCompound outputObject = new NBTTagCompound();
 			net.minecraft.server.v1_6_R2.ItemStack craft = getCraftVersion(inventory.getItem(i));
 			
 			if(craft != null) craft.save(outputObject);
 			
 			itemList.add(outputObject);
 		}
 		NBTBase.a(itemList, dataOutput);
 		
 		return new BigInteger(1, outputStream.toByteArray()).toString(32);
 	}
 	public static Inventory frombase64(String data) {
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
 		NBTTagList itemList = (NBTTagList) NBTBase.a(new DataInputStream(inputStream));
 		Inventory inventory = new CraftInventoryCustom(null, itemList.size());
 		for(int i = 0; i < itemList.size(); i++) {
 			NBTTagCompound inputObject = (NBTTagCompound) itemList.get(i);
 			
 			if(!inputObject.isEmpty()) {
 				inventory.setItem(i, CraftItemStack.asBukkitCopy(net.minecraft.server.v1_6_R2.ItemStack.createStack(inputObject)));
 			}
 		}
 		return inventory;
 	}
 
 	private static net.minecraft.server.v1_6_R2.ItemStack getCraftVersion(ItemStack stack) {
 		if(stack != null) return CraftItemStack.asNMSCopy(stack);
 		return null;
 	}
 }
