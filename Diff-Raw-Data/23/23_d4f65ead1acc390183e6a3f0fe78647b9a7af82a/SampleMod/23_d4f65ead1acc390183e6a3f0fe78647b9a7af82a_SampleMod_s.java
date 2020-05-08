 package stargazer.minecraft.samples.blockwithdrops;
 
 import chronosws.minecraft.ultracraft.blocks.UltraCraftingTable;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.MinecraftForge;
 import stargazer.minecraft.samples.common.CommonProxy;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 //
 //This is a minimal (but complete) mod entry point.
 //
 @Mod(modid = SampleMod.ID, name = SampleMod.NAME, version = SampleMod.VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class SampleMod
 {
   public final static String ID = "SampleBreakableBlock";
   public final static String NAME = "Sample Breakable Block";
   public final static String VERSION = "1.0";
     
   // The instance of your mod that Forge uses.
   @Instance(ID)
   public static SampleMod  instance;
 
   // Says where the client and server 'proxy' code is loaded.
   @SidedProxy(clientSide = "stargazer.minecraft.samples.common.ClientProxy", serverSide = "stargazer.minecraft.samples.common.CommonProxy")
   public static CommonProxy proxy;
   
   public static Block breakableBlock;
   public static Item sampleItem;
   
   //
   // Mod entry points
   //  
 
   @Init
   public void load(FMLInitializationEvent event)
   {
     proxy.registerRenderers();
     
    this.sampleItem = new SampleItem(SampleItem.SAMPLE_ITEM_ID);
     
     this.breakableBlock = new SampleBreakableBlock(1001, Material.wood)
     .setHardness(2.5F)
     .setStepSound(Block.soundWoodFootstep)
     .setUnlocalizedName("sampleBreakableBlock")
     .setCreativeTab(CreativeTabs.tabBlock);
   
     GameRegistry.registerBlock(breakableBlock, breakableBlock.getUnlocalizedName());
     LanguageRegistry.addName(breakableBlock, "Breakable Block");
     MinecraftForge.setBlockHarvestLevel(breakableBlock, "axe", 0);
   }
 
   @PostInit
   public void postInit(FMLPostInitializationEvent event)
   {
   }
 }
