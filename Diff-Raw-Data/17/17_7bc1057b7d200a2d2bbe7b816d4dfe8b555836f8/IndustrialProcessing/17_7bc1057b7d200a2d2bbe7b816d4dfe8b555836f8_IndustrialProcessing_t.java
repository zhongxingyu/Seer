 package ip.industrialProcessing;
 
 import ip.industrialProcessing.machines.crusher.BlockCrusher;
 import ip.industrialProcessing.machines.filter.BlockFilter;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 @Mod(modid="IndustrialProcessing", name="Industrial Processing", version="0.0.0")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class IndustrialProcessing {
 
         // The instance of your mod that Forge uses.
         @Instance("IndustrialProcessing")
         public static IndustrialProcessing instance;
         
        public static CreativeTabs tabMachines = new tabMachines(CreativeTabs.getNextID(),"IndustrialProcessing");
        
         public final static BlockCrusher blockCrusher = new BlockCrusher();
         public final static BlockFilter blockFilter = new BlockFilter();
 	        
         // Says where the client and server 'proxy' code is loaded.
         @SidedProxy(clientSide="ip.industrialProcessing.client.ClientProxy", serverSide="ip.industrialProcessing.CommonProxy")
         public static CommonProxy proxy;
         
         @EventHandler
         public void preInit(FMLPreInitializationEvent event) {
                 // Stub Method
         }
         
         @EventHandler
         public void load(FMLInitializationEvent event) {
        		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMachines", "en_US", "Industrial Processing");
        	
                 GameRegistry.registerBlock(blockCrusher, "BlockCrusher");
                 MinecraftForge.setBlockHarvestLevel(blockCrusher, "pickaxe", 1);
                 LanguageRegistry.addName(blockCrusher, "Crusher");
                
                proxy.registerRenderers();
         }
         @EventHandler
         public void postInit(FMLPostInitializationEvent event) {
                 // Stub Method
         }
 }
