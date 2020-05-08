 package com.PlayPrey.PPCM;
 
 import java.io.File;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.src.ModLoader;
 import net.minecraft.stats.Achievement;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.EnumHelper;
 
 import com.PlayPrey.PPCM.biome.BiomeGenChristmas;
 import com.PlayPrey.PPCM.biome.BiomeGenFrostGlacier;
 import com.PlayPrey.PPCM.blocks.ModBlocks;
 import com.PlayPrey.PPCM.core.handler.ConfigurationHandler;
 import com.PlayPrey.PPCM.core.handler.LocalizationHandler;
 import com.PlayPrey.PPCM.core.proxy.CommonProxy;
 import com.PlayPrey.PPCM.dimension.WorldProviderAvatash;
 import com.PlayPrey.PPCM.items.ModItems;
 import com.PlayPrey.PPCM.lib.References;
 import com.PlayPrey.PPCM.lib.Strings;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 //import com.PlayPrey.PPCM.Gui.GuiHandlerIronFurnace;
 //import com.PlayPrey.PPCM.blocks.IronFurnace;
 //import com.PlayPrey.PPCM.tile.entity.TileEntityIronFurnace;
 
 @Mod( name = References.MODNAME, modid = References.MODID , version = References.VERSION)
 @NetworkMod( clientSideRequired = true, serverSideRequired = false)
 public class PPCM 
 {
	public static BiomeGenChristmas Christmas = new BiomeGenChristmas(23);
	public static BiomeGenFrostGlacier Glacier = new BiomeGenFrostGlacier(24);
 	public static int dimensionId = 8;
 	public static Achievement Get_Dirt;
 	
	
 	
 	
 	
 	public static EnumToolMaterial EnumToolMaterialWheat= EnumHelper.addToolMaterial("Wheat", 0, 2, 1.0F, -2.0F, 1);
 	public static EnumToolMaterial EnumToolMaterialDirt= EnumHelper.addToolMaterial("Dirt", 0, 4, 1.0F, -1.7F, 1);
 	public static EnumToolMaterial EnumToolMaterialSilver= EnumHelper.addToolMaterial("Silver", 2, 511, 7.0F, 2.5F, 16);
 	public static EnumToolMaterial EnumToolMaterialSpecial= EnumHelper.addToolMaterial("Special", 3, 3511, 13.0F, 6.0F, 32);
 	public static EnumToolMaterial EnumToolMaterialDiamond= EnumHelper.addToolMaterial("Diamond", 3, 1513, 10.0F, 4.0F, 23);
 //@Instance("Iron")
 	//public static Iron_Furnace instance;
 //public static IronFurnace INSTANCE;
 	//private GuiHandlerIron_Furnace guiHandlerHighSpeed = new GuiHandlerIron_Furnace();
 //private GuiHandlerIronFurnace guiHandlerIronFurnace = new GuiHandlerIronFurnace ();
 	
 	
 	
 	
 	
 	
 
 	
 
 	
 	
 @SidedProxy( clientSide = References.CLIENT_PROXY_LOCATION, serverSide = References.COMMON_PROXY_LOCATION)
 
 public static CommonProxy proxy;
 public final static CreativeTabs tabPPCM= new tabPPCM(CreativeTabs.getNextID(), References.MODID);
 public final static CreativeTabs tabFROSTPPCM= new tabFROSTPCMM(CreativeTabs.getNextID(), References.MODID);
 
 @EventHandler
 public void preInit(FMLPreInitializationEvent event)
 {
 	ConfigurationHandler.init(new File(event.getModConfigurationDirectory().getAbsolutePath()+ File.separator + References.MODID + File.separator + References.MODID + ".cfg"));
	ModItems.init();
	ModBlocks.init();
    ModLoader.addBiome(Christmas);
    ModLoader.addBiome(Glacier);
 	
	LocalizationHandler.init();
 	
	DimensionManager.registerProviderType(PPCM.dimensionId, WorldProviderAvatash.class, false);
	DimensionManager.registerDimension(PPCM.dimensionId, PPCM.dimensionId);
 
 	
 }
 @EventHandler
 public void init(FMLInitializationEvent event)
 {
 //	GameRegistry.registerTileEntity(TileEntityIronFurnace.class,"tileEntityIronFurnace");
 //	RenderingRegistry.registerBlockHandler(2105, PPCM.INSTANCE);
 //	NetworkRegistry.instance().registerGuiHandler(this, guiHandlerIronFurnace);
 }
 @EventHandler
 public void postInit(FMLPostInitializationEvent event)
 {
 
 }
 @EventHandler
 public void load(FMLInitializationEvent event)
 {
 
 }
 }
