 package ip.industrialProcessing.machines.crusher;
 
 import net.minecraft.block.Block;
 import ip.industrialProcessing.machines.RecipesMachine;
 import ip.industrialProcessing.recipes.Recipe;
 import ip.industrialProcessing.recipes.RecipeInputSlot;
 import ip.industrialProcessing.recipes.RecipeOutputSlot;
 import ip.industrialProcessing.recipes.RecipeSlotType;
 
 public class RecipesCrusher extends RecipesMachine {
 	public RecipesCrusher()
 	{
 		addDefaultRecipes();
 	}
 
 	private void addDefaultRecipes() {
 		RecipeInputSlot cobbleInput = new RecipeInputSlot();
 		cobbleInput.amount = 1;
 		cobbleInput.index = 0;
 		cobbleInput.itemId = Block.cobblestone.blockID;
 		cobbleInput.type = RecipeSlotType.INVENTORY;
 		RecipeOutputSlot sandOutput = new RecipeOutputSlot();
 		sandOutput.minAmount = 1;
 		sandOutput.maxAmount = 3;
 		sandOutput.distributionCenter = 0;
 		sandOutput.index = 1;
 		sandOutput.itemId = Block.sand.blockID;
 		sandOutput.type = RecipeSlotType.INVENTORY;
 		
 		Recipe sandRecipe = new Recipe();
		sandRecipe.inputs = new RecipeInputSlot[]{ cobbleInput};
		sandRecipe.outputs = new RecipeOutputSlot[]{ sandOutput };
 		
 		addRecipe(sandRecipe);
 	}
 }
