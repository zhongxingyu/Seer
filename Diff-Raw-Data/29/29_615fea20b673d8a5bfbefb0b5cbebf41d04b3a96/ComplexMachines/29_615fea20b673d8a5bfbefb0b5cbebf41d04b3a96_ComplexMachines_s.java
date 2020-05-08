 package pixlepix.complexmachines.common;
 
 import pixlepix.complexmachines.client.GuiHandler;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="ComplexMachines", name="Complex Machines", version="0.2.4")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class ComplexMachines {
 	
 	public static int blockStartingID=670;
 	
 	private GuiHandler guiHandler = new GuiHandler();
 	public static ComplexMachinesTab creativeTab=new ComplexMachinesTab();
 	
	public final static Block focalPoint = new FocalPoint(blockStartingID+3)
    .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 	
 
 	public final static Block focalPointControlled = new FocalPointControlled(blockStartingID+8)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 	
 
 	public final static Block grinder = new Grinder(blockStartingID+7)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 
 	public final static Block extractorMachine = new ExtractorMachine(blockStartingID+2)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 
 	public final static Block fillerMachine = new FillerMachine(blockStartingID+1)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 	
 
 	public final static Block replacerMachine = new ReplacerMachine(blockStartingID+6)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 
 
 
 	public final static Block oceanGenerator = new OceanGenerator(blockStartingID+5)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 
 	
 	public final static Block singlePoint = new SinglePointGenerator(blockStartingID+4)
     .setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setCreativeTab(creativeTab);
 
         
         // The instance of your mod that Forge uses.
         @Instance("ComplexMachines")
         public static ComplexMachines instance;
         public static ComplexMachinesWorldGen generator = new ComplexMachinesWorldGen();
         // Says where the client and server 'proxy' code is loaded.
         @SidedProxy(clientSide="pixlepix.complexmachines.client.ClientProxy", serverSide="pixlepix.complexmachines.common.CommonProxy")
         public static CommonProxy proxy;
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event) {
                 Configuration config = new Configuration(event.getSuggestedConfigurationFile());
                 config.load();
                 
                 blockStartingID = config.getBlock("BlockStartingID", 670).getInt();
                 
                 config.save();
         }
         
         @Init
         public void load(FMLInitializationEvent event) {
                 
                 proxy.registerRenderers();
                 
 
             	NetworkRegistry networkRegistry = NetworkRegistry.instance();
             	networkRegistry.registerGuiHandler(this, guiHandler);
             	
 
                 LanguageRegistry.addName(focalPointControlled, "Controlled focal point");
                 MinecraftForge.setBlockHarvestLevel(focalPointControlled, "pickaxe", 0);
                 GameRegistry.registerBlock(focalPointControlled, "Controlled focal point");
             	
                 LanguageRegistry.addName(grinder, "Grinder");
                 MinecraftForge.setBlockHarvestLevel(grinder, "pickaxe", 0);
                 GameRegistry.registerBlock(grinder, "Grinder");
 
                 LanguageRegistry.addName(replacerMachine, "Replacer Machine");
                 MinecraftForge.setBlockHarvestLevel(replacerMachine, "pickaxe", 0);
                 GameRegistry.registerBlock(replacerMachine, "Replacer Machine");
 
                 LanguageRegistry.addName(oceanGenerator, "Ocean Generator");
                 MinecraftForge.setBlockHarvestLevel(oceanGenerator, "pickaxe", 0);
                 GameRegistry.registerBlock(oceanGenerator, "Ocean Generator");
                 
                 LanguageRegistry.addName(extractorMachine, "Extractor");
                 MinecraftForge.setBlockHarvestLevel(extractorMachine, "pickaxe", 0);
                 GameRegistry.registerBlock(extractorMachine, "Extractor Machine");
                 
                 
                 LanguageRegistry.addName(focalPoint, "Focal Point");
                 MinecraftForge.setBlockHarvestLevel(focalPoint, "pickaxe", 0);
                 GameRegistry.registerBlock(focalPoint, "Focal point");
                 
 
                 LanguageRegistry.addName(fillerMachine, "Filler");
                 MinecraftForge.setBlockHarvestLevel(fillerMachine, "pickaxe", 0);
                 GameRegistry.registerBlock(fillerMachine, "Filler Machine");
                 
 
                 LanguageRegistry.addName(singlePoint, "Single Point Generator");
                 MinecraftForge.setBlockHarvestLevel(singlePoint, "pickaxe", 0);
                 GameRegistry.registerBlock(singlePoint, "Single Point");
                 
                 
                 LanguageRegistry.instance().addStringLocalization("itemGroup.tabComplexMachines", "Complex Machines");
                 
                 ItemStack steelPlate= new ItemStack(14239,1,3);
                 ItemStack stone= new ItemStack(1,1,0);
                 ItemStack basicCircuit= new ItemStack(14229,1,0);
                 ItemStack result=new ItemStack (blockStartingID+1,1,0);
                 ItemStack eliteCircuit=new ItemStack(14229,1,2);
                 ItemStack diamondPickaxe=new ItemStack(278,1,0);
                 ItemStack extractor=new ItemStack(blockStartingID+2,1,0);
                 ItemStack waterBucket=new ItemStack(326,1,0);
                 ItemStack advancedCircuit=new ItemStack(14229,1,1);
                 ItemStack singlePoint=new ItemStack(blockStartingID+4,1,0);
                 ItemStack oceanGenerator=new ItemStack(blockStartingID+5,1,0);
                 ItemStack replacerMachine=new ItemStack(blockStartingID+6,1,0);
                 ItemStack grinder=new ItemStack(blockStartingID+7,1,0);
 
                 
                 
 
                 GameRegistry.addRecipe(grinder, "xxx", "xyx", "xxx",
                         'x', steelPlate, 'y', eliteCircuit);
                 GameRegistry.addRecipe(replacerMachine, "xxx", "yzy", "xxx",
                         'x', steelPlate, 'y', result, 'z', basicCircuit);
                 GameRegistry.addRecipe(singlePoint, "xyx", "yxy", "xyx",
                         'x', basicCircuit, 'y', eliteCircuit);
                 GameRegistry.addRecipe(extractor, "xyx", "xzx", "xxx",
                         'x', steelPlate, 'y', diamondPickaxe, 'z', eliteCircuit);
 
                 GameRegistry.addRecipe(extractor, "xyx", "xzx", "xxx",
                         'x', steelPlate, 'y', diamondPickaxe, 'z', eliteCircuit);
 
                 GameRegistry.addRecipe(oceanGenerator, "xyx", "zyz", "xyx",
                         'x', waterBucket, 'y', eliteCircuit, 'z', steelPlate);
 
                 GameRegistry.addRecipe(result, "xyx", "yzy", "xyx",
                         'x', stone, 'y', basicCircuit, 'z', steelPlate);
                 
                 
                 
                 
                 GameRegistry.registerWorldGenerator(new ComplexMachinesWorldGen());
 
                 GameRegistry.registerTileEntity(GrinderTileEntity.class, "Grinder");
                 GameRegistry.registerTileEntity(OceanGeneratorTileEntity.class, "Ocean Generator");
                 GameRegistry.registerTileEntity(SinglePointTileEntity.class, "Single Point generator");
                 GameRegistry.registerTileEntity(FocalPointControledTileEntity.class, "Focal Point");
                 GameRegistry.registerTileEntity(FillerMachineTileEntity.class, "Filler Machine");
                 GameRegistry.registerTileEntity(ExtractorMachineTileEntity.class, "Extractor");
                 GameRegistry.registerTileEntity(ReplacerMachineTileEntity.class, "Replacer");
                 
         }
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event) {
                 // Stub Method
         }
 }
