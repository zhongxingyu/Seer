 package rsi.common;
 
 import net.minecraft.src.*;
import rsi.common.blocks.RedstoneIntegrationBlocks;
 
 public class RedstoneIntegrationRecipes
 {
     public static void initRecipes()
     {
        CraftingManager.getInstance().addRecipe(new ItemStack(RedstoneIntegrationBlocks.basalt, 4, 2), new Object[] { "BB", "BB", 'B', new ItemStack(RedstoneIntegrationBlocks.basalt, 1, 0) });
        CraftingManager.getInstance().addRecipe(new ItemStack(RedstoneIntegrationBlocks.marble, 4, 1), new Object[] { "MM", "MM", 'M', new ItemStack(RedstoneIntegrationBlocks.marble, 1, 0) });
         
        FurnaceRecipes.smelting().addSmelting(RedstoneIntegrationBlocks.basaltID, 1, new ItemStack(RedstoneIntegrationBlocks.basalt, 1, 0));
     }
 }
