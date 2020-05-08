 package FuzzCraft.Handlers;
 
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import FuzzCraft.Base.fuzzcraft;
 import net.minecraft.block.*;
 
 public class tempColorizorRecipes {
     
     public void recipes() {
         ItemStack stoneStack  = new ItemStack(Block.stone);
         
         for (int i = 0; i < 15; i++) {
            ItemStack colorizorStack = new ItemStack(fuzzcraft.colorCharge, 1, i);
             GameRegistry.addRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, i), "x", "c", 'x', stoneStack, 'c', colorizorStack); 
         }
     }
 
 }
