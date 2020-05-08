 package TFC_carpentersblocks_adapter;
 
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class BlockHandler {
 	
     public static int recipeQuantitySlope = 4;
     public static int recipeQuantityStairs = 4;
     public static int recipeQuantityBarrier = 2;
     public static int recipeQuantityGate = 1;
     public static int recipeQuantityBlock = 3;
     public static int recipeQuantityButton = 1;
     public static int recipeQuantityLever = 1;
     public static int recipeQuantityPressurePlate = 1;
     public static int recipeQuantityDaylightSensor = 1;
     public static int recipeQuantityHatch = 1;
     public static int recipeQuantityDoor = 1;
     public static int recipeQuantityBed = 1;
     public static int recipeQuantityLadder = 4;
     
     enum RecipeType{
     	ObjArray,
     	ShapedOre,
     	ShapedTFC
     }
     public static void purgeBlockRecipes(String blockname,int stacksize){
     	Block block=Util.findBlock(blockname);
     	if(block != null){
     		ItemStack itemStack=new ItemStack(block, stacksize);
     		Util.removeRecipes(itemStack);
     	}
     }
     public static void changeBlockRecipe(String blockname,Object[] recipe,int oldstacksize, int newstacksize,RecipeType type){
     	purgeBlockRecipes(blockname,oldstacksize);
     	addBlockRecipe(blockname,recipe,newstacksize,type);
     }
     public static void addBlockRecipe(String blockname,Object[] recipe,int stacksize,RecipeType type){
 		Block block=Util.findBlock(blockname);
 		if(block != null){
 			ItemStack itemStack=new ItemStack(block, stacksize);
 			ModLogger.log(Level.INFO, "attempting to remove Recipe for "+block.getUnlocalizedName());
 			
 			switch(type){
 			case ObjArray:
 			default:
 				GameRegistry.addRecipe(itemStack, recipe);
 				break;
 			case ShapedOre:
 				ShapedOreRecipe shapedOreRecipe = new ShapedOreRecipe(itemStack,recipe);
 				GameRegistry.addRecipe(shapedOreRecipe);
 				break;
 			case ShapedTFC:
 				
 				break;
 			}
 		} else {
 			ModLogger.log(Level.SEVERE,"Could not find block:"+blockname);
 		}
 	}
     
 	public static void changeBlockRecipes()
     {
 		ModLogger.log(Level.INFO, "attempting to adjust Carpenter's Blocks Block Recipes");
 		Block blockCarpentersBlock = Util.findBlock("blockCarpentersBlock");
 		Item TFCplank=Util.findItem("SinglePlank");
 		Block TFChorizSupport = Util.findBlock("WoodSupportH");
 		Block TFCvertSupport = Util.findBlock("WoodSupportV");
 		//Carpenter Block
 		Object[] recipe;
		recipe=new Object[]{"XXX", "XYX", "XXX", 'X', new ItemStack(TFCplank,1,32767), 'Y', "plankWood"};
 		changeBlockRecipe("blockCarpentersBlock",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityBlock,recipeQuantityBlock,RecipeType.ShapedOre);
 		//Slope
		recipe=new Object[]{"  X", " XY", "XYY", 'X', new ItemStack(TFCplank,1,32767), 'Y', "plankWood"};
 		changeBlockRecipe("blockCarpentersSlope",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantitySlope,recipeQuantitySlope,RecipeType.ShapedOre);
 		//Stairs
 		recipe=new Object[]{"  X", " XX", "XXX", 'X', blockCarpentersBlock};
 		changeBlockRecipe("blockCarpentersStairs",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityStairs,recipeQuantityStairs,RecipeType.ObjArray);
 		//Barrier (fence)
 		recipe=new Object[]{"XYX", "XYX", 'X', TFCvertSupport, 'Y',TFChorizSupport};
 		changeBlockRecipe("blockCarpentersBarrier",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityBarrier,recipeQuantityBarrier,RecipeType.ShapedOre);
 		//Gate
 		recipe=new Object[]{"XYX", "XYX", 'X', TFCvertSupport, 'Y', blockCarpentersBlock};
 		changeBlockRecipe("blockCarpentersGate",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityGate,recipeQuantityGate,RecipeType.ShapedOre);
 	    
 
 	    //--Don't Need Changes (at least not yet)
 
 	    //Light Sensor
 //	    recipe=new Object[] {"XXX", "YYY", "ZZZ", 'X', Block.glass, 'Y', Item.netherQuartz, 'Z', blockCarpentersBlock};
 //	    changeBlockRecipe("blockCarpentersDaylightSensor",recipe,recipeQuantityDaylightSensor,false);
 	    //Hatch
 //	    recipe=new Object[] {"XXX", "XXX", 'X', blockCarpentersBlock};
 //	    changeBlockRecipe("blockCarpentersHatch",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityHatch,false);
 	    //Lever
 //	    recipe=new Object[] {"X", "Y", 'X', "stickWood", 'Y', blockCarpentersBlock};
 //	    changeBlockRecipe("blockCarpentersLever",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityLever,true);
 		//Button
 //		recipe=new Object[] {"X", 'X', blockCarpentersBlock};
 //		changeBlockRecipe("blockCarpentersButton",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityButton,true);
 	    //Pressure Plate
 //	    recipe=new Object[] {"XX", 'X', blockCarpentersBlock};
 //	    changeBlockRecipe("blockCarpentersPressurePlate",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityPressurePlate,false);
 	    //Ladder
 //	    recipe=new Object[] {"X X", "XXX", "X X", 'X', blockCarpentersBlock};
 //	    changeBlockRecipe("blockCarpentersLadder",recipe,carpentersblocks.util.handler.BlockHandler.recipeQuantityLadder,false);
     }
 }
