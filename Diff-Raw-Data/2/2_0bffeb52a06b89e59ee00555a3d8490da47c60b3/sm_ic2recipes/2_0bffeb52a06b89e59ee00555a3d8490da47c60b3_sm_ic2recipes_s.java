 package mods.storemore.ic2;
 
 import mods.storemore.ic2.api.Items;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class sm_ic2recipes {
 	
 	public static ItemStack copperBlock;
 	public static ItemStack tinBlock;
 	public static ItemStack uraniumBlock;
 	public static ItemStack bronzeBlock;
 
 	
     public static void initIC2Recipes()
 	{
 		
 		
 		
 		if(Loader.isModLoaded("IC2") && sm_ic2plugin.IC2RecipesEnabled())
 		{
 			
 			copperBlock = Items.getItem("bronzeBlock").copy();
 			tinBlock = Items.getItem("tinBlock").copy();
 			uraniumBlock = Items.getItem("uraniumBlock").copy();
 			bronzeBlock = Items.getItem("bronzeBlock").copy();
 	
 
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,0), "XXX", "XXX", "XXX", Character.valueOf('X'), copperBlock);
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,1), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,0));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,2), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,1));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,3), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,2));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,4), "XXX", "XXX", "XXX", Character.valueOf('X'), tinBlock);
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,5), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,4));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,6), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,5));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,7), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,6));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,8), "XXX", "XXX", "XXX", Character.valueOf('X'), uraniumBlock);
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,9), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,8));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,10), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,9));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,11), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,10));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,12), "XXX", "XXX", "XXX", Character.valueOf('X'), bronzeBlock);
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,13), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,12));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,14), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,13));
 		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,15), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,14));
 		
		GameRegistry.addShapelessRecipe(copperBlock, new ItemStack(sm_ic2plugin.ic2blocksI,1,0));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,0), new ItemStack(sm_ic2plugin.ic2blocksI,1,1));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,1), new ItemStack(sm_ic2plugin.ic2blocksI,1,2));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,2), new ItemStack(sm_ic2plugin.ic2blocksI,1,3));
 		GameRegistry.addShapelessRecipe(tinBlock, new ItemStack(sm_ic2plugin.ic2blocksI,1,4));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,4), new ItemStack(sm_ic2plugin.ic2blocksI,1,5));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,5), new ItemStack(sm_ic2plugin.ic2blocksI,1,6));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,6), new ItemStack(sm_ic2plugin.ic2blocksI,1,7));
 		GameRegistry.addShapelessRecipe(uraniumBlock, new ItemStack(sm_ic2plugin.ic2blocksI,1,8));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,8), new ItemStack(sm_ic2plugin.ic2blocksI,1,9));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,9), new ItemStack(sm_ic2plugin.ic2blocksI,1,10));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,10), new ItemStack(sm_ic2plugin.ic2blocksI,1,11));
 		GameRegistry.addShapelessRecipe(bronzeBlock, new ItemStack(sm_ic2plugin.ic2blocksI,1,12));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,12), new ItemStack(sm_ic2plugin.ic2blocksI,1,13));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,13), new ItemStack(sm_ic2plugin.ic2blocksI,1,14));
 		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,14), new ItemStack(sm_ic2plugin.ic2blocksI,1,15));
 		
 		
 		}
 
 	}
     
 }
