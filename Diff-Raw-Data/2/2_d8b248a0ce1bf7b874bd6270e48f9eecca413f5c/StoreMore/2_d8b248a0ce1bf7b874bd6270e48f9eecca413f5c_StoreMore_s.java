 package mods.storemore;
 
 import mods.storemore.blockitems.packedblocksIIIItem;
 import mods.storemore.blockitems.packedblocksIIItem;
 import mods.storemore.blockitems.packedblocksIItem;
 import mods.storemore.blockitems.packedblocksIVItem;
 import mods.storemore.blockitems.packedblocksIXItem;
 import mods.storemore.blockitems.packedblocksVIIIItem;
 import mods.storemore.blockitems.packedblocksVIIItem;
 import mods.storemore.blockitems.packedblocksVIItem;
 import mods.storemore.blockitems.packedblocksVItem;
 import mods.storemore.blockitems.packedfoodIII_Item;
 import mods.storemore.blockitems.packedfoodII_Item;
 import mods.storemore.blockitems.packedfoodIV_Item;
 import mods.storemore.blockitems.packedfoodI_Item;
 import mods.storemore.blockitems.packedfoodV_Item;
 import mods.storemore.blockitems.packedglassItem;
 import mods.storemore.blockitems.fuels.packedcharcoalIIIItem;
 import mods.storemore.blockitems.fuels.packedcharcoalIIItem;
 import mods.storemore.blockitems.fuels.packedcharcoalIItem;
 import mods.storemore.blockitems.fuels.packedcharcoalIVItem;
 import mods.storemore.blockitems.fuels.packedcoalIIIItem;
 import mods.storemore.blockitems.fuels.packedcoalIIItem;
 import mods.storemore.blockitems.fuels.packedcoalIItem;
 import mods.storemore.blockitems.fuels.packedcoalIVItem;
 import mods.storemore.gui.GuiHandler;
 import mods.storemore.proxys.SProxy;
import moony.compactcrafting.items.ItemC1WoodenPickaxe;
import moony.compactcrafting.lib.ItemIDs;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod(modid = "StoreMore", name = "StoreMore", version = "1.3.5 Alpha", useMetadata = false, dependencies = "before:StoreMore|IC2")
 @NetworkMod(channels = "StoreMore", packetHandler = sm_packetHandler.class, clientSideRequired = true, serverSideRequired = false)
 public class StoreMore 
 {
 	@Instance("StoreMore")
 	public static StoreMore instance;
 
 	public static final String MOD = "StoreMore";
 
 	public static GuiHandler guiHandler = new GuiHandler();
 
 	@SidedProxy(clientSide = "mods.storemore.proxys.CProxy", serverSide = "mods.storemore.proxys.SProxy")
 	public static SProxy proxy;
 
 
 	public static final int sideBottom = 0;
 	public static final int sideTop = 1;
 	public static final int sideNorth = 2;
 	public static final int sideSouth = 3;
 	public static final int sideWest = 4;
 	public static final int sideEast = 5;
 
 
 	//Block Registering
 	public static Block packedblocksI;
 	public static Block packedblocksII;
 	public static Block packedblocksIII;
 	public static Block packedblocksIV;
 	public static Block packedblocksV;
 	public static Block packedglass;
 	public static Block packedcoalI;
 	public static Block packedcoalII;
 	public static Block packedcoalIII;
 	public static Block packedcoalIV;
 	public static Block packedcharcoalI;
 	public static Block packedcharcoalII;
 	public static Block packedcharcoalIII;
 	public static Block packedcharcoalIV;
 	public static Block packedblocksVI;
 	public static Block packedblocksVII;
 	public static Block packedblocksVIII;
 	public static Block packedblocksIX;
 	public static Block packedfoodI;
 	public static Block packedfoodII;
 	public static Block packedfoodIII;
 	public static Block packedfoodIV;
 	public static Block packedfoodV;		
 
 	public static Block BlockSuperCompressor;
 	public static Block superCompressorIdle;
 	public static Block superCompressorActive;
 	
 	public static Item hardenedWoodPickaxeI;
 	public static Item hardenedWoodPickaxeII;
 	public static Item hardenedWoodPickaxeIII;
 	public static Item hardenedWoodPickaxeIV;
 	public static Item hardenedStonePickaxeI;
 	public static Item hardenedStonePickaxeII;
 	public static Item hardenedStonePickaxeIII;
 	public static Item hardenedStonePickaxeIV;
 	public static Item hardenedIronPickaxeI;
 	public static Item hardenedIronPickaxeII;
 	public static Item hardenedIronPickaxeIII;
 	public static Item hardenedIronPickaxeIV;
 	public static Item hardenedGoldPickaxeI;
 	public static Item hardenedGoldPickaxeII;
 	public static Item hardenedGoldPickaxeIII;
 	public static Item hardenedGoldPickaxeIV;
 	public static Item hardenedDiamondPickaxeI;
 	public static Item hardenedDiamondPickaxeII;
 	public static Item hardenedDiamondPickaxeIII;
 	public static Item hardenedDiamondPickaxeIV;
 	
 
 	
 	@PreInit()
 	public void preInit(FMLPreInitializationEvent event) {
 
 		sm_config.init();
 
 		sm_config.initialize(event.getSuggestedConfigurationFile());
 
 		sm_config.save(); 
 
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event){
 		proxy.registerRenderers();
 		sm_naming.init();
 		sm_recipes.initRecipes();
 		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 	}
 
 
 
 
 	public static CreativeTabs StoreMoreTab = new StoreMoreTab(CreativeTabs.getNextID(), "Store More");
 
 	{
 
 
 		//Multiblocks	
 
 		packedblocksI = new mods.storemore.blocks.packedblocksI(sm_config.packedblocksIID).setHardness(3.5f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksI");
 		packedblocksII = new mods.storemore.blocks.packedblocksII(sm_config.packedblocksIIID).setHardness(2.0f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksII");
 		packedblocksIII = new mods.storemore.blocks.packedblocksIII(sm_config.packedblocksIIIID).setHardness(2.0f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksIII");
 		packedblocksIV = new mods.storemore.blocks.packedblocksIV(sm_config.packedblocksIVID).setHardness(2.0f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksIV");
 		packedblocksV = new mods.storemore.blocks.packedblocksV(sm_config.packedblocksVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksV");
 		packedglass = new mods.storemore.blocks.packedglass(sm_config.packedglassID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedglass");
 		packedcoalI = new mods.storemore.blocks.fuels.packedcoalI(sm_config.packedcoalIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalI");
 		packedcoalII = new mods.storemore.blocks.fuels.packedcoalII(sm_config.packedcoalIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalII");
 		packedcoalIII = new mods.storemore.blocks.fuels.packedcoalIII(sm_config.packedcoalIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalIII");
 		packedcoalIV = new mods.storemore.blocks.fuels.packedcoalIV(sm_config.packedcoalIVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalIV");
 		packedcharcoalI = new mods.storemore.blocks.fuels.packedcharcoalI(sm_config.packedcharcoalIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalI");
 		packedcharcoalII = new mods.storemore.blocks.fuels.packedcharcoalII(sm_config.packedcharcoalIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalII");
 		packedcharcoalIII = new mods.storemore.blocks.fuels.packedcharcoalIII(sm_config.packedcharcoalIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalIII");
 		packedcharcoalIV = new mods.storemore.blocks.fuels.packedcharcoalIV(sm_config.packedcharcoalIVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalIV");
 		packedblocksVI = new mods.storemore.blocks.packedblocksVI(sm_config.packedblocksVIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksVI");
 		packedblocksVII = new mods.storemore.blocks.packedblocksVII(sm_config.packedblocksVIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksVII");
 		packedblocksVIII = new mods.storemore.blocks.packedblocksVIII(sm_config.packedblocksVIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksVIII");
 		packedblocksIX = new mods.storemore.blocks.packedblocksIX(sm_config.packedblocksIXID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksIX");
 		packedfoodI = new mods.storemore.blocks.packedfoodI(sm_config.packedfoodIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedfoodI");
 		packedfoodII = new mods.storemore.blocks.packedfoodII(sm_config.packedfoodIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedfoodII");
 		packedfoodIII = new mods.storemore.blocks.packedfoodIII(sm_config.packedfoodIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedfoodIII");
 		packedfoodIV = new mods.storemore.blocks.packedfoodIV(sm_config.packedfoodIVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedfoodIV");
 		packedfoodV = new mods.storemore.blocks.packedfoodV(sm_config.packedfoodVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedfoodV");
 
 
 		//Multiblock ItemBlocks	
 
 		Item.itemsList[sm_config.packedblocksIID]	= new packedblocksIItem(sm_config.packedblocksIID-256).setUnlocalizedName("packedblocksI");
 		Item.itemsList[sm_config.packedblocksIIID]	= new packedblocksIIItem(sm_config.packedblocksIIID-256).setUnlocalizedName("packedblocksII");
 		Item.itemsList[sm_config.packedblocksIIIID] = new packedblocksIIIItem(sm_config.packedblocksIIIID-256).setUnlocalizedName("packedblocksIII");
 		Item.itemsList[sm_config.packedblocksIVID] = new packedblocksIVItem(sm_config.packedblocksIVID-256).setUnlocalizedName("packedblocksIV");
 		Item.itemsList[sm_config.packedblocksVID] = new packedblocksVItem(sm_config.packedblocksVID-256).setUnlocalizedName("packedblocksV");
 		Item.itemsList[sm_config.packedblocksVIID] = new packedblocksVIItem(sm_config.packedblocksVIID-256).setUnlocalizedName("packedblocksVI");
 		Item.itemsList[sm_config.packedblocksVIIID] = new packedblocksVIIItem(sm_config.packedblocksVIIID-256).setUnlocalizedName("packedblocksVII");
 		Item.itemsList[sm_config.packedblocksVIIIID] = new packedblocksVIIIItem(sm_config.packedblocksVIIIID-256).setUnlocalizedName("packedblocksVIII");
 		Item.itemsList[sm_config.packedblocksIXID] = new packedblocksIXItem(sm_config.packedblocksIXID-256).setUnlocalizedName("packedblocksIX");
 		Item.itemsList[sm_config.packedglassID] = new packedglassItem(sm_config.packedglassID-256).setUnlocalizedName("packedglass");
 		Item.itemsList[sm_config.packedcoalIID] = new packedcoalIItem(sm_config.packedcoalIID-256).setUnlocalizedName("packedcoalI");
 		Item.itemsList[sm_config.packedcoalIIID] = new packedcoalIIItem(sm_config.packedcoalIIID-256).setUnlocalizedName("packedcoalII");
 		Item.itemsList[sm_config.packedcoalIIIID] = new packedcoalIIIItem(sm_config.packedcoalIIIID-256).setUnlocalizedName("packedcoalIII");
 		Item.itemsList[sm_config.packedcoalIVID] = new packedcoalIVItem(sm_config.packedcoalIVID-256).setUnlocalizedName("packedcoalIV");
 		Item.itemsList[sm_config.packedcharcoalIID] = new packedcharcoalIItem(sm_config.packedcharcoalIID-256).setUnlocalizedName("packedcharcoalI");
 		Item.itemsList[sm_config.packedcharcoalIIID] = new packedcharcoalIIItem(sm_config.packedcharcoalIIID-256).setUnlocalizedName("packedcharcoalII");
 		Item.itemsList[sm_config.packedcharcoalIIIID] = new packedcharcoalIIIItem(sm_config.packedcharcoalIIIID-256).setUnlocalizedName("packedcharcoalIII");
 		Item.itemsList[sm_config.packedcharcoalIVID] = new packedcharcoalIVItem(sm_config.packedcharcoalIVID-256).setUnlocalizedName("packedcharcoalIV");
 		Item.itemsList[sm_config.packedfoodIID] = new packedfoodI_Item(sm_config.packedfoodIID-256).setUnlocalizedName("packedfoodI");
 		Item.itemsList[sm_config.packedfoodIIID] = new packedfoodII_Item(sm_config.packedfoodIIID-256).setUnlocalizedName("packedfoodII");
 		Item.itemsList[sm_config.packedfoodIIIID] = new packedfoodIII_Item(sm_config.packedfoodIIIID-256).setUnlocalizedName("packedfoodIII");
 		Item.itemsList[sm_config.packedfoodIVID] = new packedfoodIV_Item(sm_config.packedfoodIVID-256).setUnlocalizedName("packedfoodIV");
 		Item.itemsList[sm_config.packedfoodVID] = new packedfoodV_Item(sm_config.packedfoodVID-256).setUnlocalizedName("packedfoodV");
 		
 		//Tools
 		EnumToolMaterial hardenedWoodI = EnumHelper.addToolMaterial("hardenedWoodI", 0, 531, 2.0F, 0, 15);
 		EnumToolMaterial hardenedWoodII = EnumHelper.addToolMaterial("hardenedWoodII", 0, 4779, 2.0F, 0, 15);
 		EnumToolMaterial hardenedWoodIII = EnumHelper.addToolMaterial("hardenedWoodIII", 0, 43011, 2.0F, 0, 15);
 		EnumToolMaterial hardenedWoodIV = EnumHelper.addToolMaterial("hardenedWoodIV", 0, 387099, 2.0F, 0, 15);
 		EnumToolMaterial hardenedStoneI = EnumHelper.addToolMaterial("hardenedStoneI", 1, 1179, 4.0F, 0, 18);
 		EnumToolMaterial hardenedStoneII = EnumHelper.addToolMaterial("hardenedStoneII", 1, 10611, 4.0F, 0, 18);
 		EnumToolMaterial hardenedStoneIII = EnumHelper.addToolMaterial("hardenedStoneIII", 1, 95499, 4.0F, 0, 18);
 		EnumToolMaterial hardenedStoneIV = EnumHelper.addToolMaterial("hardenedStoneIV", 1, 859491, 4.0F, 0, 18);
 		EnumToolMaterial hardenedIronI = EnumHelper.addToolMaterial("hardenedIronI", 2, 2250, 8.0F, 0, 20);
 		EnumToolMaterial hardenedIronII = EnumHelper.addToolMaterial("hardenedIronII", 2, 20250, 8.0F, 0, 20);
 		EnumToolMaterial hardenedIronIII = EnumHelper.addToolMaterial("hardenedIronIII", 2, 182250, 8.0F, 0, 20);
 		EnumToolMaterial hardenedIronIV = EnumHelper.addToolMaterial("hardenedIronIV", 2, 1640250, 8.0F, 0, 20);
 		EnumToolMaterial hardenedGoldI = EnumHelper.addToolMaterial("hardenedGoldI", 2, 288, 10.0F, 0, 35);
 		EnumToolMaterial hardenedGoldII = EnumHelper.addToolMaterial("hardenedGoldII", 2, 2592, 10.0F, 0, 35);
 		EnumToolMaterial hardenedGoldIII = EnumHelper.addToolMaterial("hardenedGoldIII", 2, 23328, 10.0F, 0, 35);
 		EnumToolMaterial hardenedGoldIV = EnumHelper.addToolMaterial("hardenedGoldIV", 2, 209952, 10.0F, 0, 35);
 		EnumToolMaterial hardenedDiamondI = EnumHelper.addToolMaterial("hardenedDiamondI", 3, 14049, 16.0F, 0, 25);
 		EnumToolMaterial hardenedDiamondII = EnumHelper.addToolMaterial("hardenedDiamondII", 3, 126441, 16.0F, 0, 25);
 		EnumToolMaterial hardenedDiamondIII = EnumHelper.addToolMaterial("hardenedDiamondII", 3, 1137969, 16.0F, 0, 25);
 		EnumToolMaterial hardenedDiamondIV = EnumHelper.addToolMaterial("hardenedDiamondIV", 3, 10241721, 16.0F, 0, 25);
 		hardenedWoodPickaxeI = new mods.storemore.items.tools.hardenedWoodPickaxeI(sm_config.hardenedWoodPickaxeIID, hardenedWoodI).setUnlocalizedName("hardenedWoodPickaxeI");
 		hardenedWoodPickaxeII = new mods.storemore.items.tools.hardenedWoodPickaxeII(sm_config.hardenedWoodPickaxeIIID, hardenedWoodII).setUnlocalizedName("hardenedWoodPickaxeII");
 		hardenedWoodPickaxeIII = new mods.storemore.items.tools.hardenedWoodPickaxeIII(sm_config.hardenedWoodPickaxeIIIID, hardenedWoodIII).setUnlocalizedName("hardenedWoodPickaxeIII");
 		hardenedWoodPickaxeIV = new mods.storemore.items.tools.hardenedWoodPickaxeIV(sm_config.hardenedWoodPickaxeIVID, hardenedWoodIV).setUnlocalizedName("hardenedWoodPickaxeIV");
 		hardenedStonePickaxeI = new mods.storemore.items.tools.hardenedStonePickaxeI(sm_config.hardenedStonePickaxeIID, hardenedStoneI).setUnlocalizedName("hardenedStonePickaxeI");
 		hardenedStonePickaxeII = new mods.storemore.items.tools.hardenedStonePickaxeII(sm_config.hardenedStonePickaxeIIID, hardenedStoneII).setUnlocalizedName("hardenedStonePickaxeII");
 		hardenedStonePickaxeIII = new mods.storemore.items.tools.hardenedStonePickaxeIII(sm_config.hardenedStonePickaxeIIIID, hardenedStoneIII).setUnlocalizedName("hardenedStonePickaxeIII");
 		hardenedStonePickaxeIV = new mods.storemore.items.tools.hardenedStonePickaxeIV(sm_config.hardenedStonePickaxeIVID, hardenedStoneIV).setUnlocalizedName("hardenedStonePickaxeIV");
 		hardenedIronPickaxeI = new mods.storemore.items.tools.hardenedIronPickaxeI(sm_config.hardenedIronPickaxeIID, hardenedIronI).setUnlocalizedName("hardenedIronPickaxeI");
 		hardenedIronPickaxeII = new mods.storemore.items.tools.hardenedIronPickaxeII(sm_config.hardenedIronPickaxeIIID, hardenedIronII).setUnlocalizedName("hardenedIronPickaxeII");
 		hardenedIronPickaxeIII = new mods.storemore.items.tools.hardenedIronPickaxeIII(sm_config.hardenedIronPickaxeIIIID, hardenedIronIII).setUnlocalizedName("hardenedIronPickaxeIII");
 		hardenedIronPickaxeIV = new mods.storemore.items.tools.hardenedIronPickaxeIV(sm_config.hardenedIronPickaxeIVID, hardenedIronIV).setUnlocalizedName("hardenedIronPickaxeIV");
 		hardenedGoldPickaxeI = new mods.storemore.items.tools.hardenedGoldPickaxeI(sm_config.hardenedGoldPickaxeIID, hardenedGoldI).setUnlocalizedName("hardenedGoldPickaxeI");
 		hardenedGoldPickaxeII = new mods.storemore.items.tools.hardenedGoldPickaxeII(sm_config.hardenedGoldPickaxeIIID, hardenedGoldII).setUnlocalizedName("hardenedGoldPickaxeII");
 		hardenedGoldPickaxeIII = new mods.storemore.items.tools.hardenedGoldPickaxeIII(sm_config.hardenedGoldPickaxeIIIID, hardenedGoldIII).setUnlocalizedName("hardenedGoldPickaxeIII");
 		hardenedGoldPickaxeIV = new mods.storemore.items.tools.hardenedGoldPickaxeIV(sm_config.hardenedGoldPickaxeIVID, hardenedGoldIV).setUnlocalizedName("hardenedGoldPickaxeIV");
 		hardenedDiamondPickaxeI = new mods.storemore.items.tools.hardenedDiamondPickaxeI(sm_config.hardenedDiamondPickaxeIID, hardenedDiamondI).setUnlocalizedName("hardenedDiamondPickaxeI");
 		hardenedDiamondPickaxeII = new mods.storemore.items.tools.hardenedDiamondPickaxeII(sm_config.hardenedDiamondPickaxeIIID, hardenedDiamondII).setUnlocalizedName("hardenedDiamondPickaxeII");
 		hardenedDiamondPickaxeIII = new mods.storemore.items.tools.hardenedDiamondPickaxeIII(sm_config.hardenedDiamondPickaxeIIIID, hardenedDiamondIII).setUnlocalizedName("hardenedDiamondPickaxeIII");
 		hardenedDiamondPickaxeIV = new mods.storemore.items.tools.hardenedDiamondPickaxeIV(sm_config.hardenedDiamondPickaxeIVID, hardenedDiamondIV).setUnlocalizedName("hardenedDiamondPickaxeIV");
 		
 	}
 
 	{
 
 		GameRegistry.registerFuelHandler(new storemoreFuels());
 		
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 
 
 
 	}
 }
