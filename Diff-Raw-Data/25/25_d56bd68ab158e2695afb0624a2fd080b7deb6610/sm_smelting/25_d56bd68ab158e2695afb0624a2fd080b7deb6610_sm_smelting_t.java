 package mods.storemore;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 
 public class sm_smelting extends StoreMore
 {
 	
 	public static void initSmelting()
     
 	{	
 		
 		//Fish
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 0, new ItemStack(StoreMore.packedfoodV, 4), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 1, new ItemStack(StoreMore.packedfoodV, 5), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 2, new ItemStack(StoreMore.packedfoodV, 6), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 3, new ItemStack(StoreMore.packedfoodV, 7), 20.0F);
 		//Pork
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 8, new ItemStack(StoreMore.packedfoodV, 12), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 9, new ItemStack(StoreMore.packedfoodV, 13), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 10, new ItemStack(StoreMore.packedfoodV, 14), 20.F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodV.blockID, 11, new ItemStack(StoreMore.packedfoodV, 15), 20.0F);
 		//Chicken
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIII.blockID, 0, new ItemStack(StoreMore.packedfoodIII, 4), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIII.blockID, 1, new ItemStack(StoreMore.packedfoodIII, 5), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIII.blockID, 2, new ItemStack(StoreMore.packedfoodIII, 6), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIII.blockID, 3, new ItemStack(StoreMore.packedfoodIII, 7), 20.0F);
 		//Potato
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIV.blockID, 4, new ItemStack(StoreMore.packedfoodIV, 8), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIV.blockID, 5, new ItemStack(StoreMore.packedfoodIV, 9), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIV.blockID, 6, new ItemStack(StoreMore.packedfoodIV, 10), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodIV.blockID, 7, new ItemStack(StoreMore.packedfoodIV, 11), 20.0F);
 		//Beef
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodI.blockID, 8, new ItemStack(StoreMore.packedfoodI, 12), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodI.blockID, 9, new ItemStack(StoreMore.packedfoodI, 13), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodI.blockID, 10, new ItemStack(StoreMore.packedfoodI, 14), 20.0F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedfoodI.blockID, 11, new ItemStack(StoreMore.packedfoodI, 15), 20.0F);
 		
 		//Sand to Glass
		FurnaceRecipes.smelting().addSmelting(PackedSand1.itemID, new ItemStack(StoreMore.packedglass, 0), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedSand2.itemID, new ItemStack(StoreMore.packedglass, 1), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedSand3.itemID, new ItemStack(StoreMore.packedglass, 2), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedSand4.itemID, new ItemStack(StoreMore.packedglass, 3), 0.9F);
 		
 		//Cobblestone to Stone
		FurnaceRecipes.smelting().addSmelting(PackedCobble1.itemID, new ItemStack(PackedStone1), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble2.itemID, new ItemStack(PackedStone2), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble3.itemID, new ItemStack(PackedStone3), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble4.itemID, new ItemStack(PackedStone4), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble5.itemID, new ItemStack(PackedStone5), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble6.itemID, new ItemStack(PackedStone6), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble7.itemID, new ItemStack(PackedStone7), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedCobble8.itemID, new ItemStack(PackedStone8), 0.9F);
 		
 		//Wood to Charcoal
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 0, new ItemStack(StoreMore.packedcharcoalI), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 1, new ItemStack(StoreMore.packedcharcoalII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 2, new ItemStack(StoreMore.packedcharcoalIII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 3, new ItemStack(StoreMore.packedcharcoalIV), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 4, new ItemStack(StoreMore.packedcharcoalI), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 5, new ItemStack(StoreMore.packedcharcoalII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 6, new ItemStack(StoreMore.packedcharcoalIII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 7, new ItemStack(StoreMore.packedcharcoalIV), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 8, new ItemStack(StoreMore.packedcharcoalI), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 9, new ItemStack(StoreMore.packedcharcoalII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 10, new ItemStack(StoreMore.packedcharcoalIII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 11, new ItemStack(StoreMore.packedcharcoalIV), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 12, new ItemStack(StoreMore.packedcharcoalI), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 13, new ItemStack(StoreMore.packedcharcoalII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 14, new ItemStack(StoreMore.packedcharcoalIII), 0.9F);
 		FurnaceRecipes.smelting().addSmelting(StoreMore.packedblocksVII.blockID, 15, new ItemStack(StoreMore.packedcharcoalIV), 0.9F);
     
 	}
 
 }
