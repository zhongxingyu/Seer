 package zh.tradecenter.handlers;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.village.MerchantRecipe;
 import net.minecraft.village.MerchantRecipeList;
 import zh.tradecenter.TradeCenter;
 import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;
 
 public class ZHTradeCenterVillagerHandler implements IVillageTradeHandler
 {
     @Override
     public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
     {
         if (villager.getProfession() == 0) // Farmer
         {
             // Buying Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.wheat, 18), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Block.cloth, 14), null, new ItemStack(Item.emerald, 1)));
                 //bonemeal
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.dyePowder, 12, 15), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.dyePowder, 13, 15), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.dyePowder, 14, 15), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.chickenRaw, 14), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.fishCooked, 9), null, new ItemStack(Item.emerald, 1)));
             }
             
             // Selling Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.bread, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.melon, 8)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.appleRed, 8)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.cookie, 10)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.chickenCooked, 8)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.arrow, 12)));
 
                 //cocoa beans
                recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.dyePowder, 3, 3)));
                recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.dyePowder, 5, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.dyePowder, 4, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.pumpkinSeeds, 5)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.pumpkinSeeds, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.pumpkinSeeds, 7)));
             }
         }
         else if (villager.getProfession() == 1) // Librarian
         {
             // Buying Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.paper, 24), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.book, 11), null, new ItemStack(Item.emerald, 1)));
             }
             
             // Selling Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 3), null, new ItemStack(Block.bookShelf, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Block.glass, 5)));
             }
         }
         else if (villager.getProfession() == 2) // Priest
         {
             // Selling Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 7), null, new ItemStack(Item.eyeOfEnder, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.expBottle, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.redstone, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Block.glowStone, 3)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), null, new ItemStack(Item.enderPearl, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 3), null, new ItemStack(Item.enderPearl, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 7), null, new ItemStack(Item.ghastTear, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 8), null, new ItemStack(Item.ghastTear, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 9), null, new ItemStack(Item.ghastTear, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.blazeRod, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), null, new ItemStack(Item.blazeRod, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 3), null, new ItemStack(Item.blazeRod, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.netherStalkSeeds, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 3), null, new ItemStack(Item.netherStalkSeeds, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), null, new ItemStack(Item.netherStalkSeeds, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Block.slowSand, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Block.slowSand, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Block.slowSand, 2)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.netherQuartz, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 3), null, new ItemStack(Item.netherQuartz, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), null, new ItemStack(Item.netherQuartz, 3)));
             }
         }
         else if (villager.getProfession() == 3) // Blacksmith
         {
             // Buying Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.coal, 16), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.ingotIron, 8), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.ingotGold, 6), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.diamond,4), null, new ItemStack(Item.emerald,1)));
             }
             
             // Selling Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 7), null, new ItemStack(Item.swordIron, 1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 6), null, new ItemStack(Item.axeIron, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 9), null, new ItemStack(Item.axeDiamond,1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 7), null, new ItemStack(Item.pickaxeIron, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 10), null, new ItemStack(Item.pickaxeDiamond, 1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 4), null, new ItemStack(Item.hoeIron, 1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 4), null, new ItemStack(Item.shovelIron, 1)));
                 
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 4), null, new ItemStack(Item.helmetIron, 1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 10), null, new ItemStack(Item.plateIron, 1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 8), null, new ItemStack(Item.legsIron, 1)));
                 //recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 4), null, new ItemStack(Item.bootsIron, 1)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 16), null, new ItemStack(Item.plateDiamond, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 11), null, new ItemStack(Item.legsDiamond, 1)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 4), null, new ItemStack(Item.helmetChain, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 10), null, new ItemStack(Item.plateChain, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 8), null, new ItemStack(Item.legsChain, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 4), null, new ItemStack(Item.bootsChain, 1)));
             }
             
         }
         if (villager.getProfession() == 4) // Butcher
         {
             // Buying Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.coal, 16), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.porkRaw, 14), null, new ItemStack(Item.emerald, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.beefRaw, 14), null, new ItemStack(Item.emerald, 1)));
             }
             
             // Selling Recipes
             if (TradeCenter.enableBetterVillagerTrades.getBoolean(true))
             {
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 6), null, new ItemStack(Item.saddle, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), null, new ItemStack(Item.helmetLeather, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), null, new ItemStack(Item.legsLeather, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.bootsLeather, 1)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.porkCooked, 7)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.beefCooked, 7)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.bone, 2)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.bone, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.bone, 4)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.rottenFlesh, 7)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.rottenFlesh, 8)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.rottenFlesh, 9)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.spiderEye, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.spiderEye, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.spiderEye, 5)));
                 
                recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.silk, 10)));
                recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.silk, 11)));
                recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.silk, 12)));
                
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.gunpowder, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.gunpowder, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.gunpowder, 5)));
                 
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.slimeBall, 3)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.slimeBall, 4)));
                 recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 1), null, new ItemStack(Item.slimeBall, 5)));
             }
             
         }
     }
     
 }
