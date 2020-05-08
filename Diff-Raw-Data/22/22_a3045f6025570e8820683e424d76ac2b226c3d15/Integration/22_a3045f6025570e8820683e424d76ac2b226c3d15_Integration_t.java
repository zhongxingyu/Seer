 package ere_geologique.common;
 
import ic2.api.recipe.Recipes;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidDictionary;
 import buildcraft.BuildCraftCore;
 import cpw.mods.fml.common.registry.GameRegistry;
 import ere_geologique.common.block.EGBlockList;
 import ere_geologique.common.config.EGProperties;
 import ere_geologique.common.item.EGItemList;
 import forestry.api.fuels.EngineBronzeFuel;
 
 public class Integration
 {
 	public static void loadIndustrialCraft()
 	{
	 Recipes.addMaceratorRecipe(new ItemStack(EGItemList.PrehistoriqueCoal), new ItemStack(EGItemList.PrehistoriqueCoalDust,1));
	 Recipes.addCompressorRecipe(new ItemStack(EGItemList.PrehistoriqueCoal,9), new ItemStack(EGBlockList.PrehistoriqueBlockCoal,1));
	 Recipes.addExtractorRecipe(new ItemStack(EGBlockList.Wood), new ItemStack(EGItemList.PrehistoriqueCoal));
 	}
 	
 	public static void loadBuildCraft()
 	{
 	 //Recette  rajouter
 		GameRegistry.addRecipe(new ItemStack(EGItemList.IvoryGear,1), new Object[]
 	    {
 		" X ", "XOX", " X ", 'X', new ItemStack(EGItemList.IvoryIngot), 'O', new ItemStack(BuildCraftCore.diamondGearItem)
 	    });
 	}
 	
 	public static void loadRailCraft()
 	{
	 RailcraftCraftingManager.cokeOven.addRecipe(new ItemStack(EGItemList.PrehistoriqueCoal), false, false, new ItemStack(EGItemList.PrehistoriqueCokeCoal), LiquidDictionary.getLiquid("CoalLiquid", 500), 3000);
	 RailcraftCraftingManager.cokeOven.addRecipe(new ItemStack(EGBlockList.Wood), false, false, new ItemStack(EGItemList.PrehistoriqueCharCoal), LiquidDictionary.getLiquid("CoalLiquid", 250), 3000);
 	}
 
 	public static void loadForestry()
 	{
 	 forestry.api.recipes.RecipeManagers.squeezerManager.addRecipe(10, new ItemStack[]{new ItemStack(EGBlockList.Wood)}, LiquidDictionary.getLiquid("CoalLiquid", 200), new ItemStack(EGBlockList.Plank,4), 100);
 	 forestry.api.fuels.FuelManager.bronzeEngineFuel.put(new ItemStack(EGItemList.CoalLiquidItem), new EngineBronzeFuel(new ItemStack(EGItemList.CoalLiquidItem), EGProperties.FCoalLiquidMJ, EGProperties.FCoalLiquidDurability, 1));
 	}
 
 	public static void loadIC_BC()
 	{
 	 //Recette  rejouter
 	}
 
 }
