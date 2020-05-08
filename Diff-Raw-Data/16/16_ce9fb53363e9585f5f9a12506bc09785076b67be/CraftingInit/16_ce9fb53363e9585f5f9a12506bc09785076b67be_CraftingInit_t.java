 package assets.tacotek.Init;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import assets.tacotek.Items.*;
 import assets.tacotek.Items.Items.*;
 import assets.tacotek.common.tacotek;
 
 public class CraftingInit {
 
 	public CraftingInit()
 	{
 		super();
 	}
 	
 	public static void addCraftingRecipes()
 	{
		//Wheat -> Flour
		GameRegistry.addRecipe(new ItemStack(Items.Flour, 1), new Object[]{
			"W",
			'W', Item.wheat,
 		});
 		
		//Flour+BucketWater -> Dough
 		GameRegistry.addRecipe(new ItemStack(Items.Dough, 1), new Object[]{
			"Fw",
 			'F', Items.Flour,
			'w', Item.bucketWater,
 		});
 	}
 	
 }
