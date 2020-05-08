 package mcextreme.tech;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import mcextreme.core.config.MCExtremeConfig;
 import mcextreme.core.MCExtremeCore;
 import mcextreme.core.utils.CreativeTab;
 import mcextreme.tech.block.BlocksTech;
 import mcextreme.tech.item.ItemsTech;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid="MCExtremeTech", name="MC-X Tech", version=MCExtremeCore.version, dependencies = "required-after:MCExtremeCore")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class MCExtremeTech
 {
	
	public static final int guiIDCompressor = 0;
	
     @Instance("MCExtremeTech")
     public static MCExtremeTech instance;
     
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         MCExtremeCore.tabTech = new CreativeTab("mcx.tech");
         
     	ModMetadata modMeta = event.getModMetadata();
  		modMeta.parent = "MCExtremeCore";
  		
  		MCExtremeConfig.loadConfig(new Configuration(new File(event.getModConfigurationDirectory() + "/mcextreme", "mcx-tech.cfg")), "TECH");
     }
     
     @EventHandler
     public void load(FMLInitializationEvent event)
     {
         BlocksTech.initBlocks();
         ItemsTech.initItems();
         CraftingTech.addRecipes();
         CraftingTech.addSmeltingRecipes();
         CraftingTech.registerInOreDict();
         EnumsTech.setHarvestLevels();
         
         ((CreativeTab)MCExtremeCore.tabTech).setIconItemStack(new ItemStack(BlocksTech.blockBasicMachine, 1, 0));
     }
 }
