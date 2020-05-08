 package mcextreme.utilities;
 
 import java.io.File;
 
 import mcextreme.core.MCExtremeCore;
 import mcextreme.core.config.MCExtremeConfig;
 import mcextreme.core.utils.CreativeTab;
import mcextreme.utilties.blocks.BlocksUtilities;
 import mcextreme.vtweaks.CraftingVTweaks;
 import mcextreme.vtweaks.MCExtremeVTweaks;
 import mcextreme.vtweaks.blocks.BlockNonSolidLeaves;
 import mcextreme.vtweaks.item.ItemsVTweaks;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid="MCExtremeUtilities", name="MC-X Utilities", version=MCExtremeUtilities.version, dependencies = "required-after:MCExtremeCore")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class MCExtremeUtilities
 {	
 	
 	public static final String version = "0.2.4";
 	public static final String pluginName = "MCX Utilities";
 	
     @Instance("MCExtremeUtilities")
     public static MCExtremeUtilities instance;
     
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         MCExtremeCore.tabUtilities = new CreativeTab("mcx.utilities");
         
     	ModMetadata modMeta = event.getModMetadata();
  		modMeta.parent = "MCExtremeCore";
  		
  		MCExtremeConfig.loadConfig(new Configuration(new File(event.getModConfigurationDirectory() + "/mcextreme", "mcx-utilities.cfg")), "UTIL");
     }
     
     @EventHandler
     public void load(FMLInitializationEvent event)
     {
        BlocksUtilities.initBlocks();
     }
 }
