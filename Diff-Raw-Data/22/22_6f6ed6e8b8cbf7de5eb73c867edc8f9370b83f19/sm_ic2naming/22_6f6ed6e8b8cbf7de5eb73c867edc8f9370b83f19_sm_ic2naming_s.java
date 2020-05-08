 package mods.storemore.ic2;
 
 import mods.storemore.ic2.sm_ic2plugin;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class sm_ic2naming {
 	
 
 	public static void initIC2Naming()
     {   
        if(Loader.isModLoaded("IC2") == true)
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 0), "Packed Copper I");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 1), "Packed Copper II");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 2), "Packed Copper III");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 3), "Packed Copper IV");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 4), "Packed Tin I");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 5), "Packed Tin II");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 6), "Packed Tin III");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 7), "Packed Tin IV");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 8), "Packed Uranium I");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 9), "Packed Uranium II");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 10), "Packed Uranium III");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 11), "Packed Uranium IV");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 12), "Packed Bronze I");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 13), "Packed Bronze II");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 14), "Packed Bronze III");
         LanguageRegistry.instance().addName(new ItemStack(sm_ic2plugin.ic2blocksI, 1, 15), "Packed Bronze IV");
     }
 		
 }
