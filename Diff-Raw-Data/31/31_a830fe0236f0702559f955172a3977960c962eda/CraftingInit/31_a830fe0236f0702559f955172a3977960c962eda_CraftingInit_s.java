 package assets.tacotek.Init;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import assets.tacotek.Init.ItemInit.*;
 import assets.tacotek.Items.*;
 
 public class CraftingInit {
 
 	public CraftingInit()
 	{
 		super();
 	}
 	
 	public static void addCraftingRecipes()
 	{
 		GameRegistry.addRecipe(new ItemStack(net.minecraft.item.Item.diamond, 4), new Object[]{
 			"T T",
 			"TTT",
 			"T T",
			'T', assets.tacotek.Init.ItemInit.doughID,
 		});
 		
		GameRegistry.addRecipe(new ItemStack(assets.tacotek.Init.ItemInit.doughID, 1), new Object[]{
 			"W  ",
 			'W', Item.wheat,
 		});
 	}
 	
 }
