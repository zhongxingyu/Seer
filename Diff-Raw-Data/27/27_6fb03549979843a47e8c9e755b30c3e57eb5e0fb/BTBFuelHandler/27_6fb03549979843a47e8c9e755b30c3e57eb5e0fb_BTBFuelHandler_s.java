 package deathrat.mods.btbees.items;
 
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.IFuelHandler;
 import deathrat.mods.btbees.BetterThanBees;
 
 public class BTBFuelHandler implements IFuelHandler
 {

 	@Override
     public int getBurnTime(ItemStack fuel)
     {
		if(fuel.itemID == BetterThanBees.riceHuskID)
			return 135;

	    return 0;
     }

 }
