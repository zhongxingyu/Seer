 package com.github.soniex2.endermoney.trading.helper.item;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 public class ItemStackMapKey {
 
 	public final int itemID;
 	public final int damage;
 	private final NBTTagCompound tag;
 
 	public ItemStackMapKey(ItemStack is) {
 		this.itemID = is.itemID;
 		this.damage = is.getItemDamage();
 		this.tag = (NBTTagCompound) (is.stackTagCompound != null ? is.stackTagCompound.copy()
 				: null);
 	}
 
 	public ItemStackMapKey(int itemID, int damage, NBTTagCompound tagCompound) {
 		this.itemID = itemID;
 		this.damage = damage;
 		this.tag = (NBTTagCompound) (tagCompound != null ? tagCompound.copy() : null);
 	}
 
 	public boolean equals(Object obj) {
 		if (!(obj instanceof ItemStackMapKey)) return false;
 		return itemID == ((ItemStackMapKey) obj).itemID
 				&& damage == ((ItemStackMapKey) obj).damage
 				&& (tag == null ? ((ItemStackMapKey) obj).tag == null : tag
 						.equals(((ItemStackMapKey) obj).tag));
 	}
 
 	public int hashCode() {
 		return ((itemID & 32767) << 16 | (damage & 65536)) << 1 | (tag != null ? 1 : 0);
 	}
 
 	public NBTTagCompound getTag() {
		return (NBTTagCompound) tag.copy();
 	}
 }
