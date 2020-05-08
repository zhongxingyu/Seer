 package assets.tacotek.Init;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import assets.tacotek.Items.*;
 import assets.tacotek.Items.Items.*;
 import assets.tacotek.blocks.Blocks;
 import assets.tacotek.common.tacotek;
 
 public class CraftingInit {
 
 	public CraftingInit()
 	{
 		super();
 	}
 	
 	public static void addCraftingRecipes()
 	{
 		//Wheat -> Flour
 		GameRegistry.addShapelessRecipe(new ItemStack(Items.Flour, 1), 
 			Item.wheat
 		);
 		
 		//Flour+BucketWater -> Dough
 		GameRegistry.addShapelessRecipe(new ItemStack(Items.Dough, 1), 
 			Items.Flour, Item.bucketWater
 		);
 		
 		//BucketWater->Salt
 		GameRegistry.addShapelessRecipe(new ItemStack(Items.Salt, 1), 
 			Item.bucketWater, Item.bucketWater, Item.bucketWater, Item.bucketWater
 		);
 		
 		//Dough -> Uncooked Tortilla
 		GameRegistry.addShapelessRecipe(new ItemStack(Items.UncookedTortilla, 1), 
 			Items.Dough
 		);
 		
 		//Milk, Salt -> Cheese
 		GameRegistry.addShapelessRecipe(new ItemStack(Items.Cheese, 1), 
 				Item.bucketMilk, Item.bucketMilk, Items.Salt
 		);
 		
 		//Taco
 		GameRegistry.addRecipe(new ItemStack(Items.Taco, 1), new Object[]{
 			"C",
 			"B",
 			"T",
 			'C', Items.Cheese,
 			'B', Item.beefCooked,
 			'T', Items.Tortilla,
 		});
 		
 		//Taco -> TacoBox
 		GameRegistry.addRecipe(new ItemStack(Blocks.blockTaco, 1), new Object[]{
 			"TTT",
 			"TTT",
 			"TTT",
 			'T', Items.Taco,
 		});
 		
 		//TacoBox -> Tacos
		GameRegistry.addShapelessRecipe(new ItemStack(Items.Taco, 9), 
 			Blocks.blockTaco
 		);
 		
 	}
 	
 }
