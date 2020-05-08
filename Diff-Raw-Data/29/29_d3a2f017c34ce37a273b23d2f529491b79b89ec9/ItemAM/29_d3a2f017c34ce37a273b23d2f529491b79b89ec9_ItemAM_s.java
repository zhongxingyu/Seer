 package de.playacem.allrondism.item;
 
 import net.minecraft.item.Item;
 
 import de.playacem.allrondism.lib.Reference;
 
 public class ItemAM extends Item{
 
 	public ItemAM(int id) {
 		super(id - Reference.SHIFTED_ID_RANGE_CORRECTION);
 		maxStackSize = 1;
 		setNoRepair();
 	}
 
 }
