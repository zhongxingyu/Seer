 package teamm.mods.virtious; 
 
 import java.io.File;
 import java.util.logging.Level;
 
 import teamm.mods.virtious.entity.EntityBurfalaunt;
 import teamm.mods.virtious.entity.EntityLaser;
 import teamm.mods.virtious.entity.EntityNative;
 import teamm.mods.virtious.entity.EntityNativeSkeleton;
 import teamm.mods.virtious.entity.item.EntityStickyBomb;
 import teamm.mods.virtious.entity.item.EntityVirtiousFishHook;
 import teamm.mods.virtious.event.VirtiousEventHandler;
 import teamm.mods.virtious.fluids.VirtiousFluids;
 import teamm.mods.virtious.lib.VirtiousBlocks;
 import teamm.mods.virtious.lib.VirtiousCreativeTabBlocks;
 import teamm.mods.virtious.lib.VirtiousCreativeTabItems;
 import teamm.mods.virtious.lib.VirtiousItems;
 import teamm.mods.virtious.lib.VirtiousRecipes;
 import teamm.mods.virtious.lib.VirtiousSound;
 import teamm.mods.virtious.misc.VirtiousFuelHandler;
 import teamm.mods.virtious.network.PacketHandler;
 import teamm.mods.virtious.proxy.CommonProxy;
 import teamm.mods.virtious.world.TileEntityPortal;
 import teamm.mods.virtious.world.VirtiousProvider;
 import teamm.mods.virtious.world.biome.BiomeGenCanyon;
 import teamm.mods.virtious.world.biome.BiomeGenVirtious;
 import teamm.mods.virtious.world.biome.BiomeGenVirtiousOcean;
 import teamm.mods.virtious.world.biome.BiomeGenWhiskerfield;
 import teamm.mods.virtious.world.gen.VirtiousStructureGenerator;
 import teamm.mods.virtious.world.gen.VirtiousWorldGenerator;
 
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemSlab;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
 import net.minecraftforge.fluids.FluidRegistry;
 import net.minecraftforge.liquids.LiquidEvent.LiquidMotionEvent;
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 
 import cpw.mods.fml.common.SidedProxy;
 
 import cpw.mods.fml.common.SidedProxy;
 
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(name = Virtious.modName, modid = Virtious.modId, useMetadata = false, version = "B1.0")
@NetworkMod(/*channels = {Virtious.CHANNEL_NAME}, packetHandler = PacketHandler.class, */clientSideRequired = true, serverSideRequired = true)
 public class Virtious {
 	public static final String modId = "virtious";
 	public static final String modName = "Virtious Mod";
 	public static final String CHANNEL_NAME = "virtious_chan";
 	
 	public static Fluid virtiousFluid;
 	public static EntityVirtiousFishHook fishEntity;
 	
 	@Instance(Virtious.modId)
 	private static Virtious instance;
 	
 	public static Virtious getInstance()
 	{
 		return instance;
 	}
 	
 	public static int dimensionID;
 	
 	public static int virtiousBiomeID;
 	public static int canyonBiomeID;
 	public static int whiskerfieldBiomeID;
 	public static int GravelBeachBiomeID;
 	public static int virtiousOceanID;
 
 	/* Biomes */
 	public static BiomeGenBase virtiousBiome = null;
 	public static BiomeGenBase biomeCanyon = null;
 	public static BiomeGenBase biomeWhiskerfield = null;
 	public static BiomeGenBase biomeGravelBeach = null;
 	public static BiomeGenBase virtiousOceanBiome = null;
 
 	public static Configuration config;
 	
 	public static VirtiousCreativeTabBlocks tabVirtiousBlocks = new VirtiousCreativeTabBlocks(CreativeTabs.getNextID(), "Virtious Mod - Blocks");
 	public static VirtiousCreativeTabItems tabVirtiousItems = new VirtiousCreativeTabItems(CreativeTabs.getNextID(), "Virtious Mod - Items");
 
 	@SidedProxy(clientSide = "teamm.mods.virtious.proxy.ClientProxy", serverSide = "teamm.mods.virtious.proxy.CommonProxy")
 	public static CommonProxy proxy;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		GameRegistry.registerTileEntity(TileEntityPortal.class, "VirtiousPortalTile");
 
 		GameRegistry.registerWorldGenerator(new VirtiousWorldGenerator());
 		
 		virtiousFluid = new VirtiousFluids("Virtian Acid");
 		
 		config = new Configuration(new File(event.getModConfigurationDirectory(), "Virtious.cfg"));
 		
 		try
 		{
 			config.load();
 			
 			//Other config
 			Property idDim = Virtious.config.get("Special", "dimensionID", DimensionManager.getNextFreeDimId());
 			dimensionID = idDim.getInt();
 
 			Property idVirtiousBiome = Virtious.config.get("Special", "virtiousBiomeID", Config.virtiousBiomeID);
 			virtiousBiomeID = idVirtiousBiome.getInt();
 
 			Property idCanyonBiome = Virtious.config.get("Special", "Canyon Biome Id", Config.canyoneBiomeID);
 			canyonBiomeID = idCanyonBiome.getInt();
 
 			Property idGravelBeachBiome = Virtious.config.get("Special", "Gravel Beach Biome Id", Config.gravelBeachBiomeID);
 			GravelBeachBiomeID = idGravelBeachBiome.getInt();
 			
 			Property idwhiskerfieldBiome = Virtious.config.get("Special", "Whiskerfield Biome Id", Config.whiskerfieldBiomeID);
 			whiskerfieldBiomeID = idwhiskerfieldBiome.getInt();
 			
 			Property idVirtiousOcean = Virtious.config.get("Specialk", "Virtious Ocean Biome Id", Config.virtiousOceanID);
 			virtiousOceanID = idVirtiousOcean.getInt();
 			
 			//ItemStack config
 			new VirtiousBlocks();
 			new VirtiousItems();
 			
 			new VirtiousRecipes();
 		
 		} catch(Exception e)
 		{
 			System.err.println("Virtious: Could not load id's.");
 			System.out.println(e.getMessage());
 		} 
 		finally 
 		{
 			config.save();
 		}
 		
 	}
 	
 	
 	@EventHandler
 	public void init(FMLInitializationEvent evt)
 	{	
 		GameRegistry.registerWorldGenerator(new VirtiousStructureGenerator());
 //		FluidContainerRegistry.registerFluidContainer(
 //				new FluidContainerData(
 //					FluidRegistry.getFluidStack(Virtious.virtiousFluid.getName(), FluidContainerRegistry.BUCKET_VOLUME),
 //					new ItemStack(VirtiousItems.bucketAcid),
 //					new ItemStack(Item.bucketEmpty)
 //				)
 //			);
 		
 		FluidContainerRegistry.registerFluidContainer(virtiousFluid, new ItemStack(VirtiousItems.bucketAcid), new ItemStack(Item.bucketEmpty));
 		
 		MinecraftForge.EVENT_BUS.register(new VirtiousEventHandler());
		//Sound to proxy
 		
 		GameRegistry.registerFuelHandler(new VirtiousFuelHandler());						
 		
 		DimensionManager.registerProviderType(dimensionID, VirtiousProvider.class, true);
 		DimensionManager.registerDimension(dimensionID, dimensionID);
 		
 		virtiousBiome = new BiomeGenVirtious(Virtious.virtiousBiomeID);
 		biomeCanyon = new BiomeGenCanyon(Virtious.canyonBiomeID);
 		biomeWhiskerfield = new BiomeGenWhiskerfield(Virtious.whiskerfieldBiomeID);
 		biomeGravelBeach = new BiomeGenCanyon(Virtious.canyonBiomeID);
 		virtiousOceanBiome = new BiomeGenVirtiousOcean(Virtious.virtiousOceanID);
 		
 		EntityRegistry.registerModEntity(EntityStickyBomb.class, "StickyBomb", EntityRegistry.findGlobalUniqueEntityId(), this, 64, 10, true);
 		EntityRegistry.registerModEntity(EntityVirtiousFishHook.class, "VirtiousFishingHook", EntityRegistry.findGlobalUniqueEntityId(), this, 64, 10, true);
 		EntityRegistry.registerModEntity(EntityLaser.class, "entityLaser", EntityRegistry.findGlobalUniqueEntityId(), this, 64, 10, true);
 		
 		EntityRegistry.registerGlobalEntityID(EntityBurfalaunt.class, "Burfalaunt", EntityRegistry.findGlobalUniqueEntityId(), 0x110802, 0x3f1e06);
 		LanguageRegistry.instance().addStringLocalization("entity.Burfalaunt.name", "en_US", "Burfalaunt");
 
 		EntityRegistry.registerGlobalEntityID(EntityNative.class, "NativeAlien", EntityRegistry.findGlobalUniqueEntityId(), 0x59563e, 0x2d3c21);
 		LanguageRegistry.instance().addStringLocalization("entity.NativeAlien.name", "en_US", "Native Alien");
 
 		EntityRegistry.registerGlobalEntityID(EntityNativeSkeleton.class, "NativeSkeleton", EntityRegistry.findGlobalUniqueEntityId(), 0x494949, 0xd8d8d8);
 		LanguageRegistry.instance().addStringLocalization("entity.NativeSkeleton.name", "en_US", "Native Skeleton");
 		
 		proxy.registerRenderThings();
 	}
 	
 	@EventHandler
 	public void initPost(FMLPostInitializationEvent evt)
 	{
 		Item.itemsList[VirtiousBlocks.cytoidFloor.blockID] = (new ItemSlab(VirtiousBlocks.cytoidFloor.blockID - 256, (BlockHalfSlab)VirtiousBlocks.cytoidFloor, (BlockHalfSlab)VirtiousBlocks.cytoidFloorDouble, false));
 	}
 }
