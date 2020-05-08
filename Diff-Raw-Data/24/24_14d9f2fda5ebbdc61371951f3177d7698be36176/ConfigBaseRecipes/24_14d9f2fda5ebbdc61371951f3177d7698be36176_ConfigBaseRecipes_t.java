 package ip.industrialProcessing.config;
 
 import ip.industrialProcessing.IndustrialProcessing;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.item.crafting.IRecipe;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ConfigBaseRecipes {
 	public static final int WILDCARD_VALUE = Short.MAX_VALUE;
 
 	public ConfigBaseRecipes() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public void addBaseRecipes() {
 		removeRecipes();
 		
 		GameRegistry.addShapelessRecipe(new ItemStack(IndustrialProcessing.itemGuide), new ItemStack(Item.book), new ItemStack(Item.dyePowder,1,14));
 		GameRegistry.addShapelessRecipe(new ItemStack(IndustrialProcessing.blockTransportFluidsStone,1,3), new ItemStack(Block.stoneBrick), new ItemStack(IndustrialProcessing.blockTransportFluids));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemWovenPatern), new ItemStack(Item.leather), new ItemStack(ISetupItems.itemKnife, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemThickStick), new ItemStack(Item.stick), new ItemStack(Item.stick));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemScreen), new ItemStack(ISetupItems.itemWovenPatern), new ItemStack(ISetupItems.itemFrame));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemStripBoard), new ItemStack(ISetupItems.itemCopperBoard), new ItemStack(ISetupItems.itemKnife, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemPestle), new ItemStack(ISetupItems.itemThickStick), new ItemStack(ISetupItems.itemKnife, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemCoalDust), new ItemStack(Item.coal), new ItemStack(ISetupItems.itemIronBowl, 1, WILDCARD_VALUE), new ItemStack(ISetupItems.itemPestle, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemHardenedSandDust), new ItemStack(IndustrialProcessing.blockHardSand), new ItemStack(ISetupItems.itemIronBowl, 1, WILDCARD_VALUE), new ItemStack(ISetupItems.itemPestle, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemAluminumDust), new ItemStack(IndustrialProcessing.blockBauxiteOre), new ItemStack(ISetupItems.itemIronBowl, 1, WILDCARD_VALUE), new ItemStack(ISetupItems.itemPestle, 1, WILDCARD_VALUE));
		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemLeadDust), new ItemStack(IndustrialProcessing.blockGalenaOre), new ItemStack(ISetupItems.itemIronBowl, 1, WILDCARD_VALUE), new ItemStack(ISetupItems.itemPestle, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemLimestoneDust), new ItemStack(IndustrialProcessing.blockCobbleLimestone), new ItemStack(ISetupItems.itemIronBowl, 1, WILDCARD_VALUE), new ItemStack(ISetupItems.itemPestle, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemElectroMagnet), new ItemStack(ISetupItems.itemIronBar), new ItemStack(ISetupItems.itemCopperCoil));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemCopperCoil), new ItemStack(ISetupItems.itemThickStick), new ItemStack(ISetupItems.itemCopperWireInsulated));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemCopperWireInsulated), new ItemStack(ISetupFluids.bucketFluidLatex, 1, WILDCARD_VALUE), new ItemStack(ISetupItems.itemCopperWire));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemCopperWire), new ItemStack(ISetupItems.itemCopperIngot), new ItemStack(ISetupItems.itemDrawPlate, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemSolderAlloyWire), new ItemStack(ISetupItems.itemSolderAlloyIngot), new ItemStack(ISetupItems.itemDrawPlate, 1, WILDCARD_VALUE));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemActiveCoal), new ItemStack(ISetupItems.itemCoalDust), new ItemStack(ISetupItems.itemGrownCulture));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemSandCast), new ItemStack(ISetupItems.itemCastingMulsh), new ItemStack(ISetupItems.itemFlask));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemnhardenedIsolationBoard), new ItemStack(IndustrialProcessing.bucketResin), new ItemStack(ISetupItems.itemFiberGlass));
 		GameRegistry.addShapelessRecipe(new ItemStack(ISetupItems.itemRedstoneCircuitry,2), new ItemStack(Item.redstone),new ItemStack(Item.redstone), new ItemStack(Item.ingotIron), new ItemStack(Item.leather));
 		GameRegistry.addRecipe(new ItemStack(ISetupItems.itemHammer), "   ", " y ", "x  ", 'x', new ItemStack(ISetupItems.itemThickStick), 'y', new ItemStack(Item.ingotIron));
 		GameRegistry.addRecipe(new ItemStack(ISetupItems.itemCastingMulsh, 4), "xyx", "yzy", "xyx", 'x', new ItemStack(Block.sand), 'y', new ItemStack(Item.clay), 'z', new ItemStack(Item.bucketWater));
 		GameRegistry.addRecipe(new ItemStack(ISetupItems.itemFlask), " x ", "x x", " x ", 'x', new ItemStack(Block.woodSingleSlab, 1, WILDCARD_VALUE));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.blockIronBowl), "   ", "xyx","   ", 'x', new ItemStack(IndustrialProcessing.itemThickStick),'y', new ItemStack(IndustrialProcessing.itemIronBowl, 8));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.itemSilicium,8), "xxx", "xyx","xxx", 'x', new ItemStack(IndustrialProcessing.itemHardenedSandDust),'y', new ItemStack(IndustrialProcessing.itemAluminumDust));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.itemSolderEmpty,1), "xyx", " z ","xyx", 'x', new ItemStack(Item.leather),'y', new ItemStack(Block.planks,1,WILDCARD_VALUE),'z',new ItemStack(Item.stick));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.blockWire,6), "xxx", "yyy","   ", 'x', new ItemStack(Block.cloth,1,WILDCARD_VALUE),'y', new ItemStack(IndustrialProcessing.itemCopperBar));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.blockWire,6), "   ", "xxx", "yyy",'x', new ItemStack(Block.cloth,1,WILDCARD_VALUE),'y', new ItemStack(IndustrialProcessing.itemCopperBar));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.blockWire,6), "yyy", "xxx","   ", 'x', new ItemStack(Block.cloth,1,WILDCARD_VALUE),'y', new ItemStack(IndustrialProcessing.itemCopperBar));
 		GameRegistry.addRecipe(new ItemStack(IndustrialProcessing.blockWire,6), "   ", "yyy", "xxx",'x', new ItemStack(Block.cloth,1,WILDCARD_VALUE),'y', new ItemStack(IndustrialProcessing.itemCopperBar));
 		GameRegistry.addShapelessRecipe(new ItemStack(IndustrialProcessing.blockWire,2),new ItemStack(Block.cloth,1,WILDCARD_VALUE), new ItemStack(IndustrialProcessing.itemCopperBar));
 
 		GameRegistry.addRecipe(new ItemStack(ISetupItems.itemCrankWheel), "  x", "xxx", "x  ", 'x', new ItemStack(ISetupItems.itemThickStick));
		GameRegistry.addRecipe(new ItemStack(ISetupItems.itemCarbonBrush, 8), "xxx", "xyx", "xxx", 'x', new ItemStack(ISetupItems.itemCoalDust), 'y', new ItemStack(ISetupFluids.bucketResin, 1, WILDCARD_VALUE));
 
 		// welding station
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.BLmultiblockWeldingStation), "xxx", "yzy", "iji", 'x', new ItemStack(Block.stoneSingleSlab), 'y', new ItemStack(Block.planks, 1, WILDCARD_VALUE), 'z', new ItemStack(Block.workbench), 'i', new ItemStack(ISetupItems.itemThickStick), 'j', new ItemStack(ISetupItems.itemHammer, 1, WILDCARD_VALUE));
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.BLmultiblockWeldingTableExt), "xxx", "yzy", "iji", 'x', new ItemStack(Block.stoneSingleSlab), 'y', new ItemStack(Block.planks, 1, WILDCARD_VALUE), 'z', new ItemStack(Block.chest), 'i', new ItemStack(ISetupItems.itemThickStick), 'j', new ItemStack(ISetupItems.itemHammer, 1, WILDCARD_VALUE));
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.blockSolderingStation), "xxx", "yzy", "iji", 'x', new ItemStack(Block.stoneSingleSlab), 'y', new ItemStack(Block.planks, 1, WILDCARD_VALUE), 'z', new ItemStack(IndustrialProcessing.itemSolderingIron), 'i', new ItemStack(ISetupItems.itemThickStick), 'j', new ItemStack(ISetupItems.itemHammer, 1, WILDCARD_VALUE));
 
 		// Bloomery
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.blockBloomery), "xyx", "y y", "xzx", 'x', new ItemStack(Item.brick), 'y', new ItemStack(Block.dirt), 'z', new ItemStack(Block.cobblestone));
 
 		// bellows
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.blockBellows), "xxx", "yyy", "xxx", 'x', new ItemStack(Block.wood, 1, WILDCARD_VALUE), 'y', new ItemStack(Item.leather));
 
 		// grindingstone
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.blockGrindingStone), "x x", "xyx", "x x", 'x', new ItemStack(IndustrialProcessing.itemThickStick), 'y', new ItemStack(Block.cobblestone));
 
 		// anvil
 		GameRegistry.addShapedRecipe(new ItemStack(ISetupMachineBlocks.blockAnvil), "zzz", " x ", "zyz", 'x', new ItemStack(Block.blockIron), 'y', new ItemStack(ISetupItems.itemHammer, 1, WILDCARD_VALUE), 'z', new ItemStack(Item.ingotIron));
 
 		GameRegistry.addSmelting(ISetupBlocks.blockGalenaOre.blockID, new ItemStack(ISetupItems.itemSilverIngot), 0);
 		GameRegistry.addSmelting(ISetupItems.itemCopperMineral.itemID, new ItemStack(ISetupItems.itemCopperIngot), 0);
 		GameRegistry.addSmelting(ISetupItems.itemTinOxideDust.itemID, new ItemStack(ISetupItems.itemTinIngot), 0);
 		GameRegistry.addSmelting(ISetupItems.itemSilverDust.itemID, new ItemStack(ISetupItems.itemSilverIngot), 0);
 		GameRegistry.addSmelting(Block.sand.blockID, new ItemStack(IndustrialProcessing.blockHardSand), 0);
 		GameRegistry.addSmelting(IndustrialProcessing.blockHardSand.blockID, new ItemStack(Block.glass), 0);
 		GameRegistry.addSmelting(IndustrialProcessing.itemnhardenedIsolationBoard.itemID, new ItemStack(IndustrialProcessing.itemIsolationBoard), 0);
 
 	}
 
 	private void removeRecipes() {
 		Iterator recipes = CraftingManager.getInstance().getRecipeList().iterator();
 		ArrayList<IRecipe> removal = new ArrayList<IRecipe>();
 		while (recipes.hasNext()) {
 			IRecipe recipe = (IRecipe) recipes.next();
 			if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().itemID == Item.ingotIron.itemID) {
 				removal.add(recipe);
 			} else {
 
 			}
 		}
 		for (IRecipe recipe : removal) {
 			CraftingManager.getInstance().getRecipeList().remove(recipe);
 		}
 		recipes = FurnaceRecipes.smelting().getSmeltingList().entrySet().iterator();
 		ArrayList<Object> removal2 = new ArrayList<Object>();
 		while (recipes.hasNext()) {
 			Map.Entry pairs = (Map.Entry) recipes.next();
 			if (pairs.getValue() != null) {
 				ItemStack stack = (ItemStack) pairs.getValue();
 				if (stack.itemID == Item.ingotIron.itemID) {
 					removal2.add(pairs.getKey());
 				} else if (stack.itemID == Block.sand.blockID) {
 					removal2.add(pairs.getKey());
 				}
 			}
 		}
 		for (Object recipe : removal2) {
 			FurnaceRecipes.smelting().getSmeltingList().remove(recipe);
 		}
 
 	}
 
 	private static ConfigBaseRecipes instance = new ConfigBaseRecipes();
 
 	public static ConfigBaseRecipes getInstance() {
 		return instance;
 	}
 }
