 package mrkirby153.MscHouses.core;
 
 import java.io.File;
 
 import mrkirby153.MscHouses.block.BlockCopperOre;
 import mrkirby153.MscHouses.block.BlockHouse_Base;
 import mrkirby153.MscHouses.block.GUI.GuiHandler;
 import mrkirby153.MscHouses.block.TileEntity.TileEntityBlockBase;
 import mrkirby153.MscHouses.configuration.ConfigurationSettings;
 import mrkirby153.MscHouses.configuration.MscHousesConfiguration;
 import mrkirby153.MscHouses.core.command.MscHousesCommand;
 import mrkirby153.MscHouses.core.handlers.VersionCheckTickHandler;
 import mrkirby153.MscHouses.core.helpers.FuelHelper;
 import mrkirby153.MscHouses.core.helpers.LocalMaterialHelper;
 import mrkirby153.MscHouses.core.helpers.LogHelper;
 import mrkirby153.MscHouses.core.helpers.OreHelper;
 import mrkirby153.MscHouses.core.localization.TEMP_ITEMNAMES;
 import mrkirby153.MscHouses.core.network.CommonProxy;
 import mrkirby153.MscHouses.crafting.CraftingBench;
 import mrkirby153.MscHouses.crafting.Furnace;
 import mrkirby153.MscHouses.creativeTab.CreativeTabHouse;
 import mrkirby153.MscHouses.creativeTab.CreativeTabModuel;
 import mrkirby153.MscHouses.generation.HouseGen;
 import mrkirby153.MscHouses.generation.MscHouses_WorldGen;
 import mrkirby153.MscHouses.items.ItemCopper;
 import mrkirby153.MscHouses.items.ItemHouseTool;
 import mrkirby153.MscHouses.items.ItemInfiniteDimensons;
 import mrkirby153.MscHouses.items.ItemMaterialModifyer;
 import mrkirby153.MscHouses.items.ItemModuel;
 import mrkirby153.MscHouses.items.ItemPCB;
 import mrkirby153.MscHouses.items.Item_Debug;
 import mrkirby153.MscHouses.lib.BlockId;
 import mrkirby153.MscHouses.lib.ItemId;
 import mrkirby153.MscHouses.lib.Reference;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Property;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 /**
  * 
  * Msc Houses
  *
  * MscHouses
  *
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER, dependencies = Reference.DEPENDANCIES)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class MscHouses {
 	public static HouseGen h = new HouseGen();
 	public static final CreativeTabs tabHouse = new CreativeTabHouse(
 			CreativeTabs.getNextID(), "MscHouses-main");
 	public static final CreativeTabs tabHouse_moduel = new CreativeTabModuel(CreativeTabs.getNextID(), "MscHouses-Moduel");
 	@Instance("MscHouses")
 	public static MscHouses instance;
 	public static final char COLOR_CODE = '\u00A7';
 	@SidedProxy(clientSide =Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
 	public static CommonProxy proxy;
 	public static MscHousesConfiguration config;
 	
 	public static Block OreCopper;
 	public static Block BlockBaseBuild;
 	
 	public static Item Debug;
 	public static Item ingotCopper;
 	public static Item HouseTool;
 	public static Item PCB;
 	public static Item moduel;
 	public static Item modifyer;
 	public static Item infiniteDimensions;
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		//Inintialize the Log Helper
 		LogHelper.init();
 		//Check version
 		Version.check();
 		
 		config = new MscHousesConfiguration(new File(event.getModConfigurationDirectory(), "MscHouses/main.conf"));
 		try{
 			config.load();
			Property oreCopperId = config.get(config.CATEGORY_BLOCK, "copperOre.id", BlockId.ORE_COPPER_DEFAULT);
 			oreCopperId.comment = "The ID for copper ore. Defaults to " + BlockId.ORE_COPPER_DEFAULT;
			Property oreCopperEnabled = config.get(config.CATEGORY_GENERAL, "copperOre.genreation", ConfigurationSettings.generteCopper_Default);
 			oreCopperEnabled.comment= "Determines if Copper ore is generated in the world. Defaults to "+ ConfigurationSettings.generteCopper_Default;
 			
			Property houseGenId = config.get(config.CATEGORY_BLOCK, "houseGen.id", BlockId.HOUSE_BASE_DEFAULT);
 			houseGenId.comment = "The ID for the House generation Block. Defaults to " + BlockId.HOUSE_BASE_DEFAULT;
 			
			Property debugId = config.get(config.CATEGORY_GENERAL, "debug.id", ItemId.DEBUG_DEFAULT);
 			debugId.comment = "The Id for the debug tool. Defaults to " + ItemId.DEBUG_DEFAULT;
 			
			Property ingotCopperId = config.get(config.CATEGORY_ITEM, "ingotCopper.id", ItemId.INGOT_COPPER_DEFAULT);
 			ingotCopperId.comment = "The Id for Copper Ingots. Defaults to " + ItemId.INGOT_COPPER_DEFAULT;
 			
			Property itemHouseToolId = config.get(config.CATEGORY_ITEM, "houseTool.id", ItemId.ITEM_HOUSETOOL_DEFAULT);
 			itemHouseToolId.comment = "The Id for the House Tool. Defaults to " +ItemId.ITEM_HOUSETOOL_DEFAULT;
 			
			Property itemPcbId = config.get(config.CATEGORY_ITEM, "pcb.id", ItemId.ITEM_PCB_DEFAULT);
 			itemPcbId.comment = "The ID for the PCB's. Defaults to " + ItemId.ITEM_PCB_DEFAULT;
 			
			Property itemModuelId = config.get(config.CATEGORY_ITEM, "moduel.id", ItemId.ITEM_MODUEL_DEFAULT);
 			itemModuelId.comment = "The ID for the House Moduels. Defaults to " + ItemId.ITEM_MODUEL_DEFAULT;
 			
			Property itemModifyerId = config.get(config.CATEGORY_ITEM, "modifyer.id", ItemId.ITEM_MODIFYER_DEFAULT);
 			itemModifyerId.comment = "The ID for the material modifyers. Defaults to " + ItemId.ITEM_MODIFYER_DEFAULT;
 			
			Property infiniteDimId = config.get(config.CATEGORY_ITEM, "infinitedim.id", ItemId.ITEM_INFINITE_DIM_DEFAULT);
 			infiniteDimId.comment = "The ID for the jar of infinite dimensons. Defaults to " + ItemId.ITEM_INFINITE_DIM_DEFAULT;
 			//Defines Blocks
 			OreCopper = new BlockCopperOre(oreCopperId.getInt()).setUnlocalizedName("oreCopper");
 			BlockBaseBuild = new BlockHouse_Base(houseGenId.getInt()).setUnlocalizedName("houseBase");
 			//Defines Items
 			Debug = new Item_Debug(ItemId.DEBUG).setUnlocalizedName("debug");
 			ingotCopper = new ItemCopper(ItemId.INGOT_COPPER).setUnlocalizedName("ingotCopper");
 			HouseTool = new ItemHouseTool(ItemId.ITEM_HOUSETOOL).setUnlocalizedName("ingotCopper");
 			PCB = new ItemPCB(ItemId.ITEM_PCB).setUnlocalizedName("pcb");
 			moduel = new ItemModuel(ItemId.ITEM_MODUEL).setUnlocalizedName("Moduel");
 			modifyer = new ItemMaterialModifyer(ItemId.ITEM_MODIFYER_EXTRA).setUnlocalizedName("ModifyerModAdded");
 			infiniteDimensions = new ItemInfiniteDimensons(ItemId.ITEM_INFINITE_DIM).setUnlocalizedName("Infinite");
 			
 			
 			
 		} finally{
 			if(config.hasChanged())
 				config.save();
 		}
 
 		
 		//Register Version Handler
 		TickRegistry.registerTickHandler(new VersionCheckTickHandler(), Side.CLIENT);
 		FuelHelper.registerFuels();
 		LocalMaterialHelper.init();
 	}
 
 	@Init
 	public void init(FMLInitializationEvent event) {
 		GameRegistry.registerWorldGenerator(new MscHouses_WorldGen());
 	//	proxy.registerTileEntity();
 		GameRegistry.registerTileEntity(TileEntityBlockBase.class, "TileEntity_BlockBase");
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		//Inialize crafting/smelting recipies
 		CraftingBench.init();
 		Furnace.init();
 		TEMP_ITEMNAMES.init();
 		OreHelper.registerOres();
 	}
 	@ServerStarting
 	public void serverLoad(FMLServerStartingEvent event){
 		event.registerServerCommand(new MscHousesCommand());
 	}
 	
 	public static String getMCVersion(){
 		return Loader.instance().getMinecraftModContainer().getVersion();
 	}
 
 }
