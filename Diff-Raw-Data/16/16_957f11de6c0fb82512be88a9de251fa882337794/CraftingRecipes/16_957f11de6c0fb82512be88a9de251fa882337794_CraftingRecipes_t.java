 package mods.minecraft.darth.dc.recipe;
 
 import mods.minecraft.darth.dc.item.ModItems;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class CraftingRecipes
 {
 
     public static void init()
     {
         
         //Science Notebook
         GameRegistry.addRecipe(new ItemStack(ModItems.sciNotebook), "dld", "dbd", "dnd", 'd', new ItemStack(Item.paper), 'b', new ItemStack(Item.writableBook), 'l', new ItemStack(Item.dyePowder, 1, 4), 'n', new ItemStack(ModItems.notebookLock));
         
         //Monocle Lens
         GameRegistry.addRecipe(new ItemStack(ModItems.monocleLens), "ggg", "gpg", "ggg", 'g', new ItemStack(Item.goldNugget), 'p', new ItemStack(Block.thinGlass));
     
         //Knife
         GameRegistry.addRecipe(new ItemStack(ModItems.knife)," f ", " s ", 'f', new ItemStack(Item.flint), 's', new ItemStack(Item.stick));
     
         //Flint Shovel
         GameRegistry.addRecipe(new ItemStack(ModItems.shovelFlint)," f ", " s ", " s ", 'f', new ItemStack(Item.flint), 's', new ItemStack(Item.stick));
         
         //Flint Pickaxe
         GameRegistry.addRecipe(new ItemStack(ModItems.pickaxeFlint),"fff", " s ", " s ", 'f', new ItemStack(Item.flint), 's', new ItemStack(Item.stick));
    
         //Flint Axe
        GameRegistry.addRecipe(new ItemStack(ModItems.axeFlint),"ff ", "fs ", " s ", 'f', new ItemStack(Item.flint), 's', new ItemStack(Item.stick));
     }
     
 }
