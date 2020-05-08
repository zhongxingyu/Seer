 package rsi.common;
 
 import net.minecraft.src.*;
import static rsi.common.blocks.RedstoneIntegrationBlock;
 
 public class RedstoneIntegrationRecipes
 {
     public static void initRecipes()
     {
        CraftingManager.addRecipe(new ItemStack(basalt, 4, 2), new Object[] { "BB", "BB", 'B', new ItemStack(basalt, 1, 0) });
        CraftingManager.addRecipe(new ItemStack(marble, 4, 1), new Object[] { "MM", "MM", 'M', new ItemStack(marble, 1, 0) });
         
        FurnaceRecipes.addSmelting(basaltID, 1, new ItemStack(basalt, 1, 0));
     }
 }
