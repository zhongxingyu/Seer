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
 			"FW",
 			'F', Items.Flour,
 			'W', Item.bucketWater,
 		});
 		
 		//BucketWater->Salt
 		GameRegistry.addRecipe(new ItemStack(Items.Salt, 1), new Object[]{
 			"WW",
 			"WW",
 			'W', Item.bucketWater,
 		});
 		
 		//Dough -> Uncooked Tortilla
 		GameRegistry.addRecipe(new ItemStack(Items.UncookedTortilla, 1), new Object[]{
 			"D",
 			'D', Items.Dough,
 		});
 		
		//Taco
 		GameRegistry.addRecipe(new ItemStack(Items.Taco, 1), new Object[]{
 			"C",
 			"B",
 			"T",
 			'C', Items.Cheese,
 			'B', Item.beefCooked,
 			'T', Items.Tortilla,
 		});
 		
 	}
 	
 }
