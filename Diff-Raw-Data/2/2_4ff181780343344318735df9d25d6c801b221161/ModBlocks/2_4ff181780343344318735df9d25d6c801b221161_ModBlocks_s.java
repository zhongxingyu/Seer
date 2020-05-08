 package mods.minecraft.darth.dc.block;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import net.minecraft.block.Block;
 import net.minecraftforge.common.MinecraftForge;
 
 import mods.minecraft.darth.dc.lib.BlockIDs;
 import mods.minecraft.darth.dc.lib.Strings;
 
 public class ModBlocks
 {
 
     //Block Instances
     public static Block unknownOre;
     
     
     public static void init()
     {
         //Initialize each block
         unknownOre = new BlockUnknownOre(BlockIDs.UNKNOWN_ORE);
         
         
         //GameRegistry Initialization
         GameRegistry.registerBlock(unknownOre, Strings.UNKNOWN_ORE_NAME);
         
         
        MinecraftForge.setBlockHarvestLevel(unknownOre, "pickaxe", 4);
         
         blockRecipiesInit();
     }
     
     public static void blockRecipiesInit()
     {
         
         
         
     }
 }
