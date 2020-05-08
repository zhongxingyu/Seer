 
 package hawksmachinery.misc;
 
 import hawksmachinery.HawksMachinery;
 import java.util.Random;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MerchantRecipe;
 import net.minecraft.src.MerchantRecipeList;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;
 
 /**
  * 
  * Where all trades are handled.
  * 
  * @author Elusivehawk
  */
public class HawkVillagerTrades //implements IVillageTradeHandler
 {
	/*
 	public static HawksMachinery BASEMOD;
 	
 	public HawkVillagerTrades() {}
 	
 	@Override
 	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
 	{
 		int profession = villager.getProfession();
 		
 		if (profession == 1)
 		{
 			for (int emeralds = 10; emeralds <= 58; ++emeralds)
 			{
 				if (emeralds <= 17)
 				{
 					recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, emeralds), new ItemStack(Item.writableBook, 1), new ItemStack(BASEMOD.blueprints, 1, 9)));
 					
 				}
 				
 				if (emeralds >= 24 && emeralds <= 32)
 				{
 					for (int meta = 0; meta <= 8; ++meta)
 					{
 						recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, emeralds), new ItemStack(Item.writableBook, 1), new ItemStack(BASEMOD.blueprints, 1, meta)));
 					}
 					
 				}
 				
 				if (emeralds >= 52)
 				{
 					recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, emeralds), new ItemStack(Item.writableBook, 1), new ItemStack(BASEMOD.blueprints, 1, 10)));
 				}
 				
 			}
 			
 		}
 		
 		if (profession == 2)
 		{
 			//TODO: Make Priest trades.
 		}
 		
 		if (profession == 3)
 		{
 			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 6), new ItemStack(BASEMOD.ingots, 1, 0)));
 			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 7), new ItemStack(BASEMOD.ingots, 1, 0)));
 			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 8), new ItemStack(BASEMOD.ingots, 1, 0)));
 			
 			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 6), new ItemStack(BASEMOD.ingots, 1, 1)));
 			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 7), new ItemStack(BASEMOD.ingots, 1, 1)));
 			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 8), new ItemStack(BASEMOD.ingots, 1, 1)));
 			
 		}
 		
 		recipeList.add(new MerchantRecipe(new ItemStack(BASEMOD.ingots, 8, 2), new ItemStack(Item.emerald, 1)));
 		recipeList.add(new MerchantRecipe(new ItemStack(BASEMOD.ingots, 9, 2), new ItemStack(Item.emerald, 1)));
 		
 	} 
	 */
 	
 }
