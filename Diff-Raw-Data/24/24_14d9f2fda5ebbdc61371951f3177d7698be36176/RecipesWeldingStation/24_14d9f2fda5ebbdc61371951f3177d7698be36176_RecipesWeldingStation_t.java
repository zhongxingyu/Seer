 package ip.industrialProcessing.multiblock.core.block.weldingStation;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.machines.RecipesMachine;
 import ip.industrialProcessing.multiblock.recipes.RecipeMultiblock;
 import ip.industrialProcessing.multiblock.recipes.RecipesMultiblock;
 import ip.industrialProcessing.multiblock.tier.Tiers;
 import ip.industrialProcessing.recipes.Recipe;
 import ip.industrialProcessing.recipes.RecipeInputSlot;
 import ip.industrialProcessing.recipes.RecipeOutputSlot;
 import ip.industrialProcessing.recipes.RecipeSlotType;
 
 public class RecipesWeldingStation extends RecipesMultiblock {
 	public RecipesWeldingStation() {
 		addDefaultRecipes();
 	}
 
 	private void addDefaultRecipes() {
 		//welding
 		addRecipe(rainTank(3000,0,Tiers.Tier0));
 		addRecipe(fluidpipe(3000,0,Tiers.Tier0));
 		addRecipe(container(3000,0,Tiers.Tier0));
 		addRecipe(containerCore(3000,0,Tiers.Tier0));
 		addRecipe(heatingElement(3000,0,Tiers.Tier0));
 		addRecipe(dryer(3000,0,Tiers.Tier0));
 		addRecipe(filter(3000,0,Tiers.Tier0));
 		addRecipe(filterTray(3000,0,Tiers.Tier0));
 		addRecipe(motorStator(3000,0,Tiers.Tier0));
 		addRecipe(ironT(3000,0,Tiers.Tier0));
 		addRecipe(ironFrame(3000,0,Tiers.Tier0));
 		addRecipe(ironMachineBlock(3000, 0,Tiers.Tier0));
 		addRecipe(motorRotor(3000,0,Tiers.Tier0));
 		addRecipe(slipRing(3000,0,Tiers.Tier0));
 		addRecipe(ironBracket(3000,0,Tiers.Tier0));
 		addRecipe(manualTreeTap(3000,0,Tiers.Tier0));
 		addRecipe(drawPlate(3000,0,Tiers.Tier0));
 		addRecipe(electroMotor(3000,0,Tiers.Tier0));
 		addRecipe(multipoleRotor(3000,0,Tiers.Tier0));
 		addRecipe(generator(3000,0,Tiers.Tier0));
 		addRecipe(crankGenerator(3000,0,Tiers.Tier0));
 		addRecipe(crushingPlate(3000,0,Tiers.Tier0));
 		addRecipe(crusher(3000,0,Tiers.Tier0));
 		addRecipe(mixer(3000,0,Tiers.Tier0));
 		addRecipe(mixingHook(3000,0,Tiers.Tier0));
 		addRecipe(copperBoard(3000,0,Tiers.Tier0));
 		addRecipe(lamp(2000,0,Tiers.Tier0));
 		addRecipe(solder(4000,0,Tiers.Tier0));
 		addRecipe(smallEngine(3000,0,Tiers.Tier0));
 		addRecipe(tank(3000,0,Tiers.Tier0));
 		addRecipe(boiler(3000,0,Tiers.Tier0));
 		addRecipe(solidBurner(3000,0,Tiers.Tier0));
 		addRecipe(turbineBlades(3000,0,Tiers.Tier0));
 		addRecipe(turbine(3000,0,Tiers.Tier0));
 		
 	}
 	
 	private RecipeMultiblock turbineBlades(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronBlade.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemTurbineBlades.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock turbine(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(7, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemTurbineBlades.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemTurbineBlades.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemTurbineBlades.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.blockTransportFluids.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.blockWire.blockID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockTurbine.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock solidBurner(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(7, Block.furnaceIdle.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(0, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockSolidBurner.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock boiler(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.blockTransportFluids.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockTank.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockBoiler.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock tank(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockTransportFluids.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, Block.thinGlass.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockTank.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock rainTank(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.blockTransportFluids.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockRainTank.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock fluidpipe(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemLeadIngot.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemLeadIngot.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemLeadIngot.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemLeadIngot.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemLeadIngot.itemID, RecipeSlotType.INVENTORY, 1) ,new RecipeInputSlot(8, IndustrialProcessing.itemLeadIngot.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, Block.glass.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, Block.glass.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, Block.glass.blockID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockTransportFluids.blockID, RecipeSlotType.INVENTORY, 6, 6, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	
 	private RecipeMultiblock heatingElement(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(3, IndustrialProcessing.itemCopperWireInsulated.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemCopperWireInsulated.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemCopperWireInsulated.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 
 	private RecipeMultiblock containerCore(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, Block.chest.blockID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockContainer.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	
 	private RecipeMultiblock container(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, Item.dyePowder.itemID,1, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockContainerWall.blockID, RecipeSlotType.INVENTORY, 8, 8, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	
 	private RecipeMultiblock dryer(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemStripBoardWired.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemHeatingElement.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockDryer.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	
 	private RecipeMultiblock smallEngine(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(5, IndustrialProcessing.itemCarbonBrush.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemCopperCoil.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemCopperCoil.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(0, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemSmallEngine.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 
 	private RecipeMultiblock solder(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemSolderAlloyWire.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemSolderEmpty.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemSolder.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock lamp(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, Block.glass.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, Block.glass.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockPetrolLamp.blockID, RecipeSlotType.INVENTORY, 3, 3, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock copperBoard(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemCopperPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIsolationBoard.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemCopperPlate.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemCopperBoard.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 
 	private RecipeMultiblock filterTray(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, Item.leather.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemFilterTray.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	
 	private RecipeMultiblock filter(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemFilterTray.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemFilterTray.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemSmallEngine.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemStripBoardWired.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockFilter.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	
 	private RecipeMultiblock mixer(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(7, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemSmallEngine.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(0, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemMixingHook.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemStripBoardWired.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockMixer.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock mixingHook(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemMixingHook.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock frame(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemFrame.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock crusher(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemSmallEngine.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemStripBoardWired.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemCrusingPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemCrusingPlate.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockCrusher.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock crushingPlate(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronTips.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemCrusingPlate.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock crankGenerator(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemCrankWheel.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockGenerator.blockID, RecipeSlotType.INVENTORY, 1)};
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockManualGenerator.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock generator(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(3, IndustrialProcessing.itemMotorPoleAxis.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.blockElectroMotor.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(0, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockGenerator.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock multipoleRotor(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1), };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemMotorPoleAxis.itemID, RecipeSlotType.INVENTORY, 3, 3, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock manualTreeTap(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemIronBracket.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBowl.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockManualTreetap.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock slipRing(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemCarbonBrush.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemCarbonBrush.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, Item.ingotIron.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemCarbonBrush.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemCarbonBrush.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemSlipRing.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock electroMotor(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(3, IndustrialProcessing.itemMotorStator.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemMotorRotor.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockElectroMotor.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock motorRotor(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemElectroMagnet.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemSlipRing.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemMotorRotor.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock motorStator(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemCopperCoil.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemCopperCoil.itemID, RecipeSlotType.INVENTORY,1),new RecipeInputSlot(5, IndustrialProcessing.itemCopperCoil.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemCopperCoil.itemID, RecipeSlotType.INVENTORY, 1) };
 		
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemMotorStator.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock ironT(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1) };
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemIronT.itemID, RecipeSlotType.INVENTORY, 2, 2, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock ironFrame(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, IndustrialProcessing.itemIronT.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, IndustrialProcessing.itemIronT.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronT.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronT.itemID, RecipeSlotType.INVENTORY, 1)};
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.BLmultiblockFrame.blockID, RecipeSlotType.INVENTORY, 2, 2, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock ironBracket(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(6, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1)};
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemIronBracket.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock drawPlate(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(1, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1)};
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.itemDrawPlate.itemID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 	private RecipeMultiblock ironMachineBlock(int workTime, int power, Tiers tier) {
 		RecipeMultiblock recipe = new RecipeMultiblock();
 		recipe.inputs = new RecipeInputSlot[] { new RecipeInputSlot(0, Item.dyePowder.itemID,14, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(2, Item.dyePowder.itemID,14, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(1, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(3, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(4, IndustrialProcessing.BLmultiblockFrame.blockID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(5, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(6, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(7, IndustrialProcessing.itemIronPlate.itemID, RecipeSlotType.INVENTORY, 1),new RecipeInputSlot(8, IndustrialProcessing.itemIronBar.itemID, RecipeSlotType.INVENTORY, 1)};
 		recipe.outputs = new RecipeOutputSlot[] { new RecipeOutputSlot(9, IndustrialProcessing.blockMachineBlock.blockID, RecipeSlotType.INVENTORY, 1, 1, 0) };
 		
 		recipe.workRequired = workTime;
 		recipe.powerRequired = power;
 		recipe.tier = tier;
 		return recipe;
 	}
 }
