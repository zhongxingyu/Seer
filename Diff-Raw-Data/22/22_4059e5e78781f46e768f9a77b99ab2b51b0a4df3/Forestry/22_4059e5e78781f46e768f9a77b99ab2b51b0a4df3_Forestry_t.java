 package ro.narc.liquiduu.integration;
 
 import buildcraft.api.recipes.RefineryRecipe;
 
import cpw.mods.fml.relauncher.Side;

 import forestry.api.core.ItemInterface;
 import forestry.api.recipes.RecipeManagers;
 
 import ic2.core.Ic2Items;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidContainerData;
import net.minecraftforge.liquids.LiquidContainerRegistry;

import ro.narc.liquiduu.CommonProxy;
import ro.narc.liquiduu.LiquidUU;

 public class Forestry {
     public static boolean init() {
 
         ItemStack canEmpty = ItemInterface.getItem("canEmpty");
         ItemStack ingotTin = ItemInterface.getItem("ingotTin");

        if(LiquidUU.getSide() == Side.CLIENT) {
            CommonProxy.cannedUUItemStack.getItem().setCreativeTab(canEmpty.getItem().getCreativeTab());
        }
 
         RecipeManagers.squeezerManager.addRecipe(5, new ItemStack[]{Ic2Items.matter}, CommonProxy.liquidUULiquidStack);
         RecipeManagers.squeezerManager.addRecipe(5, new ItemStack[]{CommonProxy.cannedUUItemStack},
                 CommonProxy.liquidUULiquidStack, ingotTin, 5);
 
         RecipeManagers.bottlerManager.addRecipe(5, CommonProxy.liquidUULiquidStack, Ic2Items.cell, Ic2Items.matter);
         RecipeManagers.bottlerManager.addRecipe(5, CommonProxy.liquidUULiquidStack, canEmpty, CommonProxy.cannedUUItemStack);
 
         initLiquidContainers(canEmpty);
         initRefineryRecipes();
 
         return true;
     }
 
     public static LiquidContainerData cannedUUData;
 
     @SuppressWarnings("unchecked")
     public static void initLiquidContainers(ItemStack canEmpty) {
         cannedUUData = new LiquidContainerData(CommonProxy.liquidUULiquidStack, CommonProxy.cannedUUItemStack, canEmpty);
         LiquidContainerRegistry.registerLiquid(cannedUUData);
     }
 
     public static void initRefineryRecipes() {
         ItemStack liquidBiomass = ItemInterface.getItem("liquidBiomass");
         ItemStack liquidBiofuel = ItemInterface.getItem("liquidBiofuel");
         ItemStack liquidSeedOil = ItemInterface.getItem("liquidSeedOil");
         ItemStack liquidJuice   = ItemInterface.getItem("liquidJuice");
         ItemStack liquidHoney   = ItemInterface.getItem("liquidHoney");
         ItemStack crushedIce    = ItemInterface.getItem("liquidIce");
 
         CommonProxy.addConversionRecipe(CommonProxy.baseConversionUU,        liquidBiomass.itemID, liquidBiomass.getItemDamage(),
                 CommonProxy.biomassConversion);
         CommonProxy.addConversionRecipe(CommonProxy.convenienceConversionUU, liquidBiofuel.itemID, liquidBiofuel.getItemDamage(),
                 CommonProxy.biofuelConversion);
         CommonProxy.addConversionRecipe(CommonProxy.baseConversionUU,        liquidJuice.itemID,   liquidJuice.getItemDamage(),
                 CommonProxy.appleJuiceConversion);
         CommonProxy.addConversionRecipe(CommonProxy.baseConversionUU,        liquidHoney.itemID,   liquidHoney.getItemDamage(),
                 CommonProxy.honeyConversion);
         CommonProxy.addConversionRecipe(CommonProxy.convenienceConversionUU, liquidSeedOil.itemID, liquidSeedOil.getItemDamage(),
                 CommonProxy.seedOilConversion);
         CommonProxy.addConversionRecipe(CommonProxy.nonfuelConversionUU,     crushedIce.itemID,    crushedIce.getItemDamage(),
                 CommonProxy.iceConversion);
     }
 }
