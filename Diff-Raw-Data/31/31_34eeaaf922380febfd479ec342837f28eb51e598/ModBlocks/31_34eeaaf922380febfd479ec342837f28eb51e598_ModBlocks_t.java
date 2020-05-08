 package obt.block;
 
 import obt.lib.Reference;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ModBlocks
 {
     public static Block testBlock;
     
     public static void init()
     {
             testBlock = (new BlockTest(obt.lib.Reference.BLOCK_ID, Material.rock))
                     .setUnlocalizedName("BlockTest");
 
             GameRegistry.registerBlock(testBlock, IB.class,
                     Reference.MOD_ID+(testBlock.getUnlocalizedName().substring(5)));
        
            LanguageRegistry.addName(new ItemStack(testBlock, 1, Reference.MAGIC_META), "Magic Block(You shouldn't have this)");
            LanguageRegistry.addName(new ItemStack(testBlock, 1, Reference.DULL_META), "Dull Block(You shouldn't have this)");
            LanguageRegistry.addName(new ItemStack(testBlock, 1, Reference.INFIN_META), "Infinity Block(You shouldn't have this)");
     }
 }
     
