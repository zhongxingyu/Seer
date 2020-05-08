 package com.nexized.cross.manager;
 
 import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 
 public class recipesManager {
 
 	public static void addRecipes() {
 		// @Insert OreDirectionary
 		OreDictionary.registerOre("oreAluminum", new ItemStack(blockManager.oreAluminum));
 		OreDictionary.registerOre("oreCopper", new ItemStack(blockManager.oreCopper));
 		OreDictionary.registerOre("orePlatinum", new ItemStack(blockManager.orePlatinum));
 		OreDictionary.registerOre("oreSilver", new ItemStack(blockManager.oreSilver));
 		OreDictionary.registerOre("oreTin", new ItemStack(blockManager.oreTin));
 		
 		// @Add Ingot Block Recipes	
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockAluminum), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotAluminum});
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockCopper), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotCopper});
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockPlatinum), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotPlatinum});
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockSilver, 1), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotSilver});
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockSteel), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotSteel});
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockTin), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotTin});
 		GameRegistry.addRecipe(new ItemStack(blockManager.blockBronze), new Object[] {"XXX", "XXX", "XXX", 'X', itemManager.ingotBronze});
 		
 		// @Add Reverse Recipes
 		GameRegistry.addRecipe(new ItemStack(itemManager.ingotCopper, 9), new Object[] {"X", 'X', blockManager.blockCopper});
 		GameRegistry.addRecipe(new ItemStack(itemManager.ingotPlatinum, 9), new Object[] {"X", 'X', blockManager.blockPlatinum});
 		GameRegistry.addRecipe(new ItemStack(itemManager.ingotSilver, 9), new Object[] {"X", 'X', blockManager.blockSilver});
 		GameRegistry.addRecipe(new ItemStack(itemManager.ingotSteel, 9), new Object[] {"X", 'X', blockManager.blockSteel});
 		GameRegistry.addRecipe(new ItemStack(itemManager.ingotTin, 9), new Object[] {"X", 'X', blockManager.blockTin});
 		GameRegistry.addRecipe(new ItemStack(itemManager.ingotBronze, 9), new Object[] {"X", 'X', blockManager.blockBronze});
 		
 		GameRegistry.addRecipe(new ItemStack(toolManager.axeAluminum), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotAluminum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.axeCopper), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotCopper, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.axePlatinum), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotPlatinum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.axeSilver), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotSilver, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.axeSteel), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotSteel, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.axeTin), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotTin, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.axeBronze), new Object[] {" XX", " *X", " * ", 'X', itemManager.ingotBronze, '*', Item.stick});
 		
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoeAluminum), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotAluminum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoeCopper), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotCopper, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoePlatinum), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotPlatinum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoeSilver), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotSilver, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoeSteel), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotSteel, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoeTin), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotTin, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.hoeBronze), new Object[] {" XX", " * ", " * ", 'X', itemManager.ingotBronze, '*', Item.stick});
 		
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickAluminum), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotAluminum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickCopper), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotCopper, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickPlatinum), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotPlatinum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickSilver), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotSilver, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickSteel), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotSteel, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickTin), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotTin, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.pickBronze), new Object[] {"XXX", " * ", " * ", 'X', itemManager.ingotBronze, '*', Item.stick});
 		
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelAluminum), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotAluminum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelCopper), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotCopper, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelPlatinum), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotPlatinum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelSilver), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotSilver, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelSteel), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotSteel, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelTin), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotTin, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.shovelBronze), new Object[] {" X ", " * ", " * ", 'X', itemManager.ingotBronze, '*', Item.stick});
 				
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordAluminum), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotAluminum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordCopper), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotCopper, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordPlatinum), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotPlatinum, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordSilver), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotSilver, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordSteel), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotSteel, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordTin), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotTin, '*', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(toolManager.swordBronze), new Object[] {" X ", " X ", " * ", 'X', itemManager.ingotBronze, '*', Item.stick});
 		
 		// @Add Food Recipes
 		GameRegistry.addRecipe(new ItemStack(foodManager.foodRawbacon, 3), new Object[] {"X", 'X', Item.porkRaw});
 				
 		// @Add Smelting (oreBlocks)
 		GameRegistry.addSmelting(blockManager.oreAluminum.blockID, new ItemStack(itemManager.ingotAluminum), 1.0F);
 		GameRegistry.addSmelting(blockManager.oreCopper.blockID, new ItemStack(itemManager.ingotCopper), 1.0F);
 		GameRegistry.addSmelting(blockManager.orePlatinum.blockID, new ItemStack(itemManager.ingotPlatinum), 1.0F);
 		GameRegistry.addSmelting(blockManager.oreSilver.blockID, new ItemStack(itemManager.ingotSilver), 1.0F);
 		GameRegistry.addSmelting(blockManager.oreTin.blockID, new ItemStack(itemManager.ingotTin), 1.0F);
 	
 		// @Add Smelting (foodItems)
 		GameRegistry.addSmelting(foodManager.foodRawbacon.itemID, new ItemStack (foodManager.foodBacon), 0.1F);
 	}
 	
 	public static void addFusionRecipes() {
 		// @Dust recipes
 		fusionRecipes.smelting().addSmelting(itemManager.dustIron.itemID, itemManager.dustSilver.itemID, Item.flint.itemID, new ItemStack(itemManager.ingotSteel));
 		fusionRecipes.smelting().addSmelting(itemManager.dustIron.itemID, itemManager.dustTin.itemID, Item.flint.itemID, new ItemStack(itemManager.ingotSteel));
		fusionRecipes.smelting().addSmelting(itemManager.dustIron.itemID, itemManager.dustCopper.itemID, Item.flint.itemID, new ItemStack(itemManager.ingotBronze));
 		
 		// @Ingot recipes
 		fusionRecipes.smelting().addSmelting(Item.ingotIron.itemID, itemManager.ingotSilver.itemID, Item.flint.itemID, new ItemStack(itemManager.ingotSteel));
 		fusionRecipes.smelting().addSmelting(Item.ingotIron.itemID, itemManager.ingotTin.itemID, Item.flint.itemID, new ItemStack(itemManager.ingotSteel));
		fusionRecipes.smelting().addSmelting(Item.ingotIron.itemID, itemManager.ingotCopper.itemID, Item.flint.itemID, new ItemStack(itemManager.ingotBronze));
 	}
 	
 }
