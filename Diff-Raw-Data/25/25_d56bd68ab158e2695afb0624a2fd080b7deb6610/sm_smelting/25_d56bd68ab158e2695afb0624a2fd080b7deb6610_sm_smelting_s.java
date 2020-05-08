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
		FurnaceRecipes.smelting().addSmelting(StoreMore.packedglass.blockID, 0, new ItemStack(PackedSand1), 0.9F);
		FurnaceRecipes.smelting().addSmelting(StoreMore.packedglass.blockID, 1, new ItemStack(PackedSand2), 0.9F);
		FurnaceRecipes.smelting().addSmelting(StoreMore.packedglass.blockID, 2, new ItemStack(PackedSand3), 0.9F);
		FurnaceRecipes.smelting().addSmelting(StoreMore.packedglass.blockID, 3, new ItemStack(PackedSand4), 0.9F);
 		
 		//Cobblestone to Stone
		FurnaceRecipes.smelting().addSmelting(PackedStone1.itemID, new ItemStack(PackedCobble1), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone2.itemID, new ItemStack(PackedCobble2), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone3.itemID, new ItemStack(PackedCobble3), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone4.itemID, new ItemStack(PackedCobble4), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone5.itemID, new ItemStack(PackedCobble5), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone6.itemID, new ItemStack(PackedCobble6), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone7.itemID, new ItemStack(PackedCobble7), 0.9F);
		FurnaceRecipes.smelting().addSmelting(PackedStone8.itemID, new ItemStack(PackedCobble8), 0.9F);
 		
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
