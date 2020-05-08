 package ip.industrialProcessing.machines.diskFilter;
 
 import net.minecraftforge.fluids.FluidRegistry;
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.machines.RecipesMachine;
 import ip.industrialProcessing.recipes.Recipe;
 import ip.industrialProcessing.recipes.RecipeInputSlot;
 import ip.industrialProcessing.recipes.RecipeOutputSlot;
 import ip.industrialProcessing.recipes.RecipeSlotType;
 
 public class RecipesDiskFilter extends RecipesMachine {
 
     public RecipesDiskFilter() {
 	addDefaultRecipes();
     }
 
     private void addDefaultRecipes() {
 	addRecipe(IronSedimentToDust(3000, 1));
 	addRecipe(TinSedimentToDust(3000, 1));
 	addRecipe(CopperSedimentToDust(3000, 1));
 	addRecipe(GoldSedimentToDust(3000, 1));
 	addRecipe(TaliaSedimentToDust(3000, 1));
 	addRecipe(GalenaSedimentToDust(3000, 1));
 	addRecipe(ChromiteSedimentToDust(3000, 1));
     }
 
     private Recipe IronSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickIronSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightIronSediment.getName()), RecipeSlotType.TANK, 1000) };
 	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(1, IndustrialProcessing.itemIronOxideDust.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 
     private Recipe GoldSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickGoldSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightGoldSediment.getName()), RecipeSlotType.TANK, 1000) };
	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(2, IndustrialProcessing.itemGoldDust.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 
     private Recipe TinSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickTinSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightTinSediment.getName()), RecipeSlotType.TANK, 1000) };
	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(2, IndustrialProcessing.itemTinOxideDust.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 
     private Recipe CopperSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickCopperSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightCopperSediment.getName()), RecipeSlotType.TANK, 1000) };
	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(2, IndustrialProcessing.itemCopperMineral.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 
     private Recipe ChromiteSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickChromiteSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightChromiteSediment.getName()), RecipeSlotType.TANK, 1000) };
	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(2, IndustrialProcessing.itemChromiteDust.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 
     private Recipe GalenaSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickGalenaSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightGalenaSediment.getName()), RecipeSlotType.TANK, 1000) };
	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(2, IndustrialProcessing.itemLeadDust.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 
     private Recipe TaliaSedimentToDust(int i, int powerConsumption) {
 	Recipe recipe = new Recipe();
 	recipe.workRequired = i;
 	recipe.powerRequired = powerConsumption;
 	recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidThickTaliaSediment.getName()), RecipeSlotType.TANK, 1000), new RecipeInputSlot(1, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidLightTaliaSediment.getName()), RecipeSlotType.TANK, 1000) };
	recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(2, IndustrialProcessing.itemTaliaDust.itemID, RecipeSlotType.INVENTORY, 1, 1, 0.5), new RecipeOutputSlot(2, FluidRegistry.getFluidID(IndustrialProcessing.itemFluidDirtyWater.getName()), RecipeSlotType.TANK, 1000, 1000, 0.5) };
 	return recipe;
     }
 }
