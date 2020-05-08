 package net.blazecoding.magicpowders.recipe;
 
import net.blazecoding.magicpowders.item.ModItems;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class MagicInfuserRecipes {
 
 	public MagicInfuserRecipes() {
 	}
 
	public static ItemStack getInfusingResult(int i, int j, int k, int l) {
		return getOutput(i, j, k, l);
 	}
 
	private static ItemStack getOutput(int i, int j, int k, int l) {
 
		if (i == ModItems.magicDust.itemID && k == 0 && j == Item.ingotIron.itemID && l == 0 || i == Item.ingotIron.itemID && k == 0 && j == ModItems.magicDust.itemID && l == 0) {
			return new ItemStack(ModItems.magicIngot, 1, 0);
		}
		
		if (i == ModItems.magicDust.itemID && k == 1 && j == Item.ingotIron.itemID && l == 0 || i == Item.ingotIron.itemID && k == 0 && j == ModItems.magicDust.itemID && l == 1) {
			return new ItemStack(ModItems.magicIngot, 1, 1);
		}
		
		if (i == ModItems.magicDust.itemID && k == 2 && j == Item.ingotIron.itemID && l == 0 || i == Item.ingotIron.itemID && k == 0 && j == ModItems.magicDust.itemID && l == 2) {
			return new ItemStack(ModItems.magicIngot, 1, 2);
 		}
 
 		return null;
 
 	}
 
 }
