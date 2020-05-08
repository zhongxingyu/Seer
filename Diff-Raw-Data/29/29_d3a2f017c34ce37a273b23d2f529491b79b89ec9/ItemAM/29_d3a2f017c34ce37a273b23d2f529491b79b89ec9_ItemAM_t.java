 package de.playacem.allrondism.item;
 
 import net.minecraft.item.Item;
 
 import de.playacem.allrondism.lib.Reference;
import de.playacem.allrondism.lib.Sprites;
 
 public class ItemAM extends Item{
 
 	public ItemAM(int id) {
 		super(id - Reference.SHIFTED_ID_RANGE_CORRECTION);
 		maxStackSize = 1;
 		setNoRepair();
 	}
 
	public String getTextureFile() {
		return Sprites.ITEMS_PNG;
		
	}
 }
