 package denoflionsx.minefactoryreloaded.modhelpers.forestry.extratrees;
 
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLInterModComms;
 import cpw.mods.fml.common.network.NetworkMod;
 import denoflionsx.minefactoryreloaded.modhelpers.forestry.ForestryTrees;
 import java.lang.reflect.Field;
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 
 @Mod(modid = ExtraTrees.name, name = ExtraTrees.name, version = "1.0", dependencies = "after:ExtraTrees")
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 public class ExtraTrees {
 
     public static final String name = "MFR Compat Extra Trees";
 
     @Mod.Init
     public void init(FMLInitializationEvent evt) {
 	try {
 	    if (Loader.isModLoaded("ExtraTrees")) {
		for (Field f : Class.forName("binnie.extratrees.PluginExtraTrees").getDeclaredFields()) {
 		    if (f.getType().equals(Block.class)) {
 			if (f.getName().toLowerCase().contains("log")) {
 			    FMLInterModComms.sendMessage(ForestryTrees.name, "register_log", new ItemStack((Block) f.get(null)));
 			}
 		    }
 		}
 	    }
 	} catch (Throwable ex) {
 	}
     }
 }
