 package mods.storemore;
 
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
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.EnumHelper;
import cpw.mods.fml.common.FMLLog;
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
 
 @Mod(modid = "StoreMore", name = "StoreMore", version = "0.9.0 Public_PR", useMetadata = false, dependencies = "before:StoreMore|IC2")
 @NetworkMod(channels = "StoreMore", packetHandler = sm_packetHandler.class, clientSideRequired = true, serverSideRequired = false)
 public class StoreMore 
 {
 	@Instance("StoreMore")
 	public static StoreMore instance;
 
 	public static final String MOD = "StoreMore";
 	public static final String MODtextures = "storemore";
 
 	public static GuiHandler guiHandler = new GuiHandler();
 
 	@SidedProxy(clientSide = "mods.storemore.proxys.CProxy", serverSide = "mods.storemore.proxys.SProxy")
 	public static SProxy proxy;
 
 
 	//Block Registering
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
 	
 	public static Item hardenedWoodAxeI;
 	public static Item hardenedWoodAxeII;
 	public static Item hardenedWoodAxeIII;
 	public static Item hardenedWoodAxeIV;
 	public static Item hardenedStoneAxeI;
 	public static Item hardenedStoneAxeII;
 	public static Item hardenedStoneAxeIII;
 	public static Item hardenedStoneAxeIV;
 	public static Item hardenedIronAxeI;
 	public static Item hardenedIronAxeII;
 	public static Item hardenedIronAxeIII;
 	public static Item hardenedIronAxeIV;
 	public static Item hardenedGoldAxeI;
 	public static Item hardenedGoldAxeII;
 	public static Item hardenedGoldAxeIII;
 	public static Item hardenedGoldAxeIV;
 	public static Item hardenedDiamondAxeI;
 	public static Item hardenedDiamondAxeII;
 	public static Item hardenedDiamondAxeIII;
 	public static Item hardenedDiamondAxeIV;
 	
 	public static Item hardenedWoodShovelI;
 	public static Item hardenedWoodShovelII;
 	public static Item hardenedWoodShovelIII;
 	public static Item hardenedWoodShovelIV;
 	public static Item hardenedStoneShovelI;
 	public static Item hardenedStoneShovelII;
 	public static Item hardenedStoneShovelIII;
 	public static Item hardenedStoneShovelIV;
 	public static Item hardenedIronShovelI;
 	public static Item hardenedIronShovelII;
 	public static Item hardenedIronShovelIII;
 	public static Item hardenedIronShovelIV;
 	public static Item hardenedGoldShovelI;
 	public static Item hardenedGoldShovelII;
 	public static Item hardenedGoldShovelIII;
 	public static Item hardenedGoldShovelIV;
 	public static Item hardenedDiamondShovelI;
 	public static Item hardenedDiamondShovelII;
 	public static Item hardenedDiamondShovelIII;
 	public static Item hardenedDiamondShovelIV;
 	
 	public static Item hardenedWoodHoeI;
 	public static Item hardenedWoodHoeII;
 	public static Item hardenedWoodHoeIII;
 	public static Item hardenedWoodHoeIV;
 	public static Item hardenedStoneHoeI;
 	public static Item hardenedStoneHoeII;
 	public static Item hardenedStoneHoeIII;
 	public static Item hardenedStoneHoeIV;
 	public static Item hardenedIronHoeI;
 	public static Item hardenedIronHoeII;
 	public static Item hardenedIronHoeIII;
 	public static Item hardenedIronHoeIV;
 	public static Item hardenedGoldHoeI;
 	public static Item hardenedGoldHoeII;
 	public static Item hardenedGoldHoeIII;
 	public static Item hardenedGoldHoeIV;
 	public static Item hardenedDiamondHoeI;
 	public static Item hardenedDiamondHoeII;
 	public static Item hardenedDiamondHoeIII;
 	public static Item hardenedDiamondHoeIV;
 	
 	public static Item hardenedWoodSwordI;
 	public static Item hardenedWoodSwordII;
 	public static Item hardenedWoodSwordIII;
 	public static Item hardenedWoodSwordIV;
 	public static Item hardenedStoneSwordI;
 	public static Item hardenedStoneSwordII;
 	public static Item hardenedStoneSwordIII;
 	public static Item hardenedStoneSwordIV;
 	public static Item hardenedIronSwordI;
 	public static Item hardenedIronSwordII;
 	public static Item hardenedIronSwordIII;
 	public static Item hardenedIronSwordIV;
 	public static Item hardenedGoldSwordI;
 	public static Item hardenedGoldSwordII;
 	public static Item hardenedGoldSwordIII;
 	public static Item hardenedGoldSwordIV;
 	public static Item hardenedDiamondSwordI;
 	public static Item hardenedDiamondSwordII;
 	public static Item hardenedDiamondSwordIII;
 	public static Item hardenedDiamondSwordIV;
 	
 	//SuperPackedBlocks
 	
 	public static Item PackedCobble8;
 	public static Item PackedStone8;
 	public static Item PackedDirt8;
 	public static Item PackedGravel8;
 	public static Item PackedSand8;
 	public static Item PackedSandstone8;
 	
 	public static Item PackedCobble7;
 	public static Item PackedStone7;
 	public static Item PackedDirt7;
 	public static Item PackedGravel7;
 	public static Item PackedSand7;
 	public static Item PackedSandstone7;
 	
 	public static Item PackedCobble6;
 	public static Item PackedStone6;
 	public static Item PackedDirt6;
 	public static Item PackedGravel6;
 	public static Item PackedSand6;
 	public static Item PackedSandstone6;
 	
 	public static Item PackedCobble5;
 	public static Item PackedStone5;
 	public static Item PackedDirt5;
 	public static Item PackedGravel5;
 	public static Item PackedSand5;
 	public static Item PackedSandstone5;
 	
 	public static Item PackedCobble4;
 	public static Item PackedStone4;
 	public static Item PackedDirt4;
 	public static Item PackedGravel4;
 	public static Item PackedSand4;
 	public static Item PackedSandstone4;
 	
 	public static Item PackedCobble3;
 	public static Item PackedStone3;
 	public static Item PackedDirt3;
 	public static Item PackedGravel3;
 	public static Item PackedSand3;
 	public static Item PackedSandstone3;
 	
 	public static Item PackedCobble2;
 	public static Item PackedStone2;
 	public static Item PackedDirt2;
 	public static Item PackedGravel2;
 	public static Item PackedSand2;
 	public static Item PackedSandstone2;
 	
 	public static Item PackedCobble1;
 	public static Item PackedStone1;
 	public static Item PackedDirt1;
 	public static Item PackedGravel1;
 	public static Item PackedSand1;
 	public static Item PackedSandstone1;
 	
 	
 	@PreInit()
 	public void preInit(FMLPreInitializationEvent event) {
		FMLLog.info("[StoreMore] Loading Config");
		
 		sm_config.init();
 
 		sm_config.initialize(event.getSuggestedConfigurationFile());
 
 		sm_config.save(); 
		
		FMLLog.info("[StoreMore] Config Loaded");
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event){
 		proxy.registerRenderers();
 		sm_naming.init();
 		sm_recipes.initRecipes();
 		sm_smelting.initSmelting();
 		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 		sm_oredictionary.registerOres();
 	}
 
 	public static CreativeTabs StoreMoreTab = new StoreMoreTab(CreativeTabs.getNextID(), "Store More");
 
 	{
 
 		//Multiblocks	
 
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
 		EnumToolMaterial hardenedStoneI = EnumHelper.addToolMaterial("hardenedStoneI", 1, 1179, 4.0F, 1, 18);
 		EnumToolMaterial hardenedStoneII = EnumHelper.addToolMaterial("hardenedStoneII", 1, 10611, 4.0F, 1, 18);
 		EnumToolMaterial hardenedStoneIII = EnumHelper.addToolMaterial("hardenedStoneIII", 1, 95499, 4.0F, 1, 18);
 		EnumToolMaterial hardenedStoneIV = EnumHelper.addToolMaterial("hardenedStoneIV", 1, 859491, 4.0F, 1, 18);
 		EnumToolMaterial hardenedIronI = EnumHelper.addToolMaterial("hardenedIronI", 2, 2250, 8.0F, 2, 20);
 		EnumToolMaterial hardenedIronII = EnumHelper.addToolMaterial("hardenedIronII", 2, 20250, 8.0F, 2, 20);
 		EnumToolMaterial hardenedIronIII = EnumHelper.addToolMaterial("hardenedIronIII", 2, 182250, 8.0F, 2, 20);
 		EnumToolMaterial hardenedIronIV = EnumHelper.addToolMaterial("hardenedIronIV", 2, 1640250, 8.0F, 2, 20);
 		EnumToolMaterial hardenedGoldI = EnumHelper.addToolMaterial("hardenedGoldI", 1, 288, 10.0F, 2, 35);
 		EnumToolMaterial hardenedGoldII = EnumHelper.addToolMaterial("hardenedGoldII", 1, 2592, 10.0F, 2, 35);
 		EnumToolMaterial hardenedGoldIII = EnumHelper.addToolMaterial("hardenedGoldIII", 1, 23328, 10.0F, 2, 35);
 		EnumToolMaterial hardenedGoldIV = EnumHelper.addToolMaterial("hardenedGoldIV", 1, 209952, 10.0F, 2, 35);
 		EnumToolMaterial hardenedDiamondI = EnumHelper.addToolMaterial("hardenedDiamondI", 3, 14049, 16.0F, 3, 25);
 		EnumToolMaterial hardenedDiamondII = EnumHelper.addToolMaterial("hardenedDiamondII", 3, 126441, 16.0F, 3, 25);
 		EnumToolMaterial hardenedDiamondIII = EnumHelper.addToolMaterial("hardenedDiamondII", 3, 1137969, 16.0F, 3, 25);
 		EnumToolMaterial hardenedDiamondIV = EnumHelper.addToolMaterial("hardenedDiamondIV", 3, 10241721, 16.0F, 3, 25);
 		
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
 		
 		hardenedWoodAxeI = new mods.storemore.items.tools.hardenedWoodAxeI(sm_config.hardenedWoodAxeIID, hardenedWoodI).setUnlocalizedName("hardenedWoodAxeI");
 		hardenedWoodAxeII = new mods.storemore.items.tools.hardenedWoodAxeII(sm_config.hardenedWoodAxeIIID, hardenedWoodII).setUnlocalizedName("hardenedWoodAxeII");
 		hardenedWoodAxeIII = new mods.storemore.items.tools.hardenedWoodAxeIII(sm_config.hardenedWoodAxeIIIID, hardenedWoodIII).setUnlocalizedName("hardenedWoodAxeIII");
 		hardenedWoodAxeIV = new mods.storemore.items.tools.hardenedWoodAxeIV(sm_config.hardenedWoodAxeIVID, hardenedWoodIV).setUnlocalizedName("hardenedWoodAxeIV");
 		hardenedStoneAxeI = new mods.storemore.items.tools.hardenedStoneAxeI(sm_config.hardenedStoneAxeIID, hardenedStoneI).setUnlocalizedName("hardenedStoneAxeI");
 		hardenedStoneAxeII = new mods.storemore.items.tools.hardenedStoneAxeII(sm_config.hardenedStoneAxeIIID, hardenedStoneII).setUnlocalizedName("hardenedStoneAxeII");
 		hardenedStoneAxeIII = new mods.storemore.items.tools.hardenedStoneAxeIII(sm_config.hardenedStoneAxeIIIID, hardenedStoneIII).setUnlocalizedName("hardenedStoneAxeIII");
 		hardenedStoneAxeIV = new mods.storemore.items.tools.hardenedStoneAxeIV(sm_config.hardenedStoneAxeIVID, hardenedStoneIV).setUnlocalizedName("hardenedStoneAxeIV");
 		hardenedIronAxeI = new mods.storemore.items.tools.hardenedIronAxeI(sm_config.hardenedIronAxeIID, hardenedIronI).setUnlocalizedName("hardenedIronAxeI");
 		hardenedIronAxeII = new mods.storemore.items.tools.hardenedIronAxeII(sm_config.hardenedIronAxeIIID, hardenedIronII).setUnlocalizedName("hardenedIronAxeII");
 		hardenedIronAxeIII = new mods.storemore.items.tools.hardenedIronAxeIII(sm_config.hardenedIronAxeIIIID, hardenedIronIII).setUnlocalizedName("hardenedIronAxeIII");
 		hardenedIronAxeIV = new mods.storemore.items.tools.hardenedIronAxeIV(sm_config.hardenedIronAxeIVID, hardenedIronIV).setUnlocalizedName("hardenedIronAxeIV");
 		hardenedGoldAxeI = new mods.storemore.items.tools.hardenedGoldAxeI(sm_config.hardenedGoldAxeIID, hardenedGoldI).setUnlocalizedName("hardenedGoldAxeI");
 		hardenedGoldAxeII = new mods.storemore.items.tools.hardenedGoldAxeII(sm_config.hardenedGoldAxeIIID, hardenedGoldII).setUnlocalizedName("hardenedGoldAxeII");
 		hardenedGoldAxeIII = new mods.storemore.items.tools.hardenedGoldAxeIII(sm_config.hardenedGoldAxeIIIID, hardenedGoldIII).setUnlocalizedName("hardenedGoldAxeIII");
 		hardenedGoldAxeIV = new mods.storemore.items.tools.hardenedGoldAxeIV(sm_config.hardenedGoldAxeIVID, hardenedGoldIV).setUnlocalizedName("hardenedGoldAxeIV");
 		hardenedDiamondAxeI = new mods.storemore.items.tools.hardenedDiamondAxeI(sm_config.hardenedDiamondAxeIID, hardenedDiamondI).setUnlocalizedName("hardenedDiamondAxeI");
 		hardenedDiamondAxeII = new mods.storemore.items.tools.hardenedDiamondAxeII(sm_config.hardenedDiamondAxeIIID, hardenedDiamondII).setUnlocalizedName("hardenedDiamondAxeII");
 		hardenedDiamondAxeIII = new mods.storemore.items.tools.hardenedDiamondAxeIII(sm_config.hardenedDiamondAxeIIIID, hardenedDiamondIII).setUnlocalizedName("hardenedDiamondAxeIII");
 		hardenedDiamondAxeIV = new mods.storemore.items.tools.hardenedDiamondAxeIV(sm_config.hardenedDiamondAxeIVID, hardenedDiamondIV).setUnlocalizedName("hardenedDiamondAxeIV");
 		
 		hardenedWoodShovelI = new mods.storemore.items.tools.hardenedWoodShovelI(sm_config.hardenedWoodShovelIID, hardenedWoodI).setUnlocalizedName("hardenedWoodShovelI");
 		hardenedWoodShovelII = new mods.storemore.items.tools.hardenedWoodShovelII(sm_config.hardenedWoodShovelIIID, hardenedWoodII).setUnlocalizedName("hardenedWoodShovelII");
 		hardenedWoodShovelIII = new mods.storemore.items.tools.hardenedWoodShovelIII(sm_config.hardenedWoodShovelIIIID, hardenedWoodIII).setUnlocalizedName("hardenedWoodShovelIII");
 		hardenedWoodShovelIV = new mods.storemore.items.tools.hardenedWoodShovelIV(sm_config.hardenedWoodShovelIVID, hardenedWoodIV).setUnlocalizedName("hardenedWoodShovelIV");
 		hardenedStoneShovelI = new mods.storemore.items.tools.hardenedStoneShovelI(sm_config.hardenedStoneShovelIID, hardenedStoneI).setUnlocalizedName("hardenedStoneShovelI");
 		hardenedStoneShovelII = new mods.storemore.items.tools.hardenedStoneShovelII(sm_config.hardenedStoneShovelIIID, hardenedStoneII).setUnlocalizedName("hardenedStoneShovelII");
 		hardenedStoneShovelIII = new mods.storemore.items.tools.hardenedStoneShovelIII(sm_config.hardenedStoneShovelIIIID, hardenedStoneIII).setUnlocalizedName("hardenedStoneShovelIII");
 		hardenedStoneShovelIV = new mods.storemore.items.tools.hardenedStoneShovelIV(sm_config.hardenedStoneShovelIVID, hardenedStoneIV).setUnlocalizedName("hardenedStoneShovelIV");
 		hardenedIronShovelI = new mods.storemore.items.tools.hardenedIronShovelI(sm_config.hardenedIronShovelIID, hardenedIronI).setUnlocalizedName("hardenedIronShovelI");
 		hardenedIronShovelII = new mods.storemore.items.tools.hardenedIronShovelII(sm_config.hardenedIronShovelIIID, hardenedIronII).setUnlocalizedName("hardenedIronShovelII");
 		hardenedIronShovelIII = new mods.storemore.items.tools.hardenedIronShovelIII(sm_config.hardenedIronShovelIIIID, hardenedIronIII).setUnlocalizedName("hardenedIronShovelIII");
 		hardenedIronShovelIV = new mods.storemore.items.tools.hardenedIronShovelIV(sm_config.hardenedIronShovelIVID, hardenedIronIV).setUnlocalizedName("hardenedIronShovelIV");
 		hardenedGoldShovelI = new mods.storemore.items.tools.hardenedGoldShovelI(sm_config.hardenedGoldShovelIID, hardenedGoldI).setUnlocalizedName("hardenedGoldShovelI");
 		hardenedGoldShovelII = new mods.storemore.items.tools.hardenedGoldShovelII(sm_config.hardenedGoldShovelIIID, hardenedGoldII).setUnlocalizedName("hardenedGoldShovelII");
 		hardenedGoldShovelIII = new mods.storemore.items.tools.hardenedGoldShovelIII(sm_config.hardenedGoldShovelIIIID, hardenedGoldIII).setUnlocalizedName("hardenedGoldShovelIII");
 		hardenedGoldShovelIV = new mods.storemore.items.tools.hardenedGoldShovelIV(sm_config.hardenedGoldShovelIVID, hardenedGoldIV).setUnlocalizedName("hardenedGoldShovelIV");
 		hardenedDiamondShovelI = new mods.storemore.items.tools.hardenedDiamondShovelI(sm_config.hardenedDiamondShovelIID, hardenedDiamondI).setUnlocalizedName("hardenedDiamondShovelI");
 		hardenedDiamondShovelII = new mods.storemore.items.tools.hardenedDiamondShovelII(sm_config.hardenedDiamondShovelIIID, hardenedDiamondII).setUnlocalizedName("hardenedDiamondShovelII");
 		hardenedDiamondShovelIII = new mods.storemore.items.tools.hardenedDiamondShovelIII(sm_config.hardenedDiamondShovelIIIID, hardenedDiamondIII).setUnlocalizedName("hardenedDiamondShovelIII");
 		hardenedDiamondShovelIV = new mods.storemore.items.tools.hardenedDiamondShovelIV(sm_config.hardenedDiamondShovelIVID, hardenedDiamondIV).setUnlocalizedName("hardenedDiamondShovelIV");
 		
 		hardenedWoodHoeI = new mods.storemore.items.tools.hardenedWoodHoeI(sm_config.hardenedWoodHoeIID, hardenedWoodI).setUnlocalizedName("hardenedWoodHoeI");
 		hardenedWoodHoeII = new mods.storemore.items.tools.hardenedWoodHoeII(sm_config.hardenedWoodHoeIIID, hardenedWoodII).setUnlocalizedName("hardenedWoodHoeII");
 		hardenedWoodHoeIII = new mods.storemore.items.tools.hardenedWoodHoeIII(sm_config.hardenedWoodHoeIIIID, hardenedWoodIII).setUnlocalizedName("hardenedWoodHoeIII");
 		hardenedWoodHoeIV = new mods.storemore.items.tools.hardenedWoodHoeIV(sm_config.hardenedWoodHoeIVID, hardenedWoodIV).setUnlocalizedName("hardenedWoodHoeIV");
 		hardenedStoneHoeI = new mods.storemore.items.tools.hardenedStoneHoeI(sm_config.hardenedStoneHoeIID, hardenedStoneI).setUnlocalizedName("hardenedStoneHoeI");
 		hardenedStoneHoeII = new mods.storemore.items.tools.hardenedStoneHoeII(sm_config.hardenedStoneHoeIIID, hardenedStoneII).setUnlocalizedName("hardenedStoneHoeII");
 		hardenedStoneHoeIII = new mods.storemore.items.tools.hardenedStoneHoeIII(sm_config.hardenedStoneHoeIIIID, hardenedStoneIII).setUnlocalizedName("hardenedStoneHoeIII");
 		hardenedStoneHoeIV = new mods.storemore.items.tools.hardenedStoneHoeIV(sm_config.hardenedStoneHoeIVID, hardenedStoneIV).setUnlocalizedName("hardenedStoneHoeIV");
 		hardenedIronHoeI = new mods.storemore.items.tools.hardenedIronHoeI(sm_config.hardenedIronHoeIID, hardenedIronI).setUnlocalizedName("hardenedIronHoeI");
 		hardenedIronHoeII = new mods.storemore.items.tools.hardenedIronHoeII(sm_config.hardenedIronHoeIIID, hardenedIronII).setUnlocalizedName("hardenedIronHoeII");
 		hardenedIronHoeIII = new mods.storemore.items.tools.hardenedIronHoeIII(sm_config.hardenedIronHoeIIIID, hardenedIronIII).setUnlocalizedName("hardenedIronHoeIII");
 		hardenedIronHoeIV = new mods.storemore.items.tools.hardenedIronHoeIV(sm_config.hardenedIronHoeIVID, hardenedIronIV).setUnlocalizedName("hardenedIronHoeIV");
 		hardenedGoldHoeI = new mods.storemore.items.tools.hardenedGoldHoeI(sm_config.hardenedGoldHoeIID, hardenedGoldI).setUnlocalizedName("hardenedGoldHoeI");
 		hardenedGoldHoeII = new mods.storemore.items.tools.hardenedGoldHoeII(sm_config.hardenedGoldHoeIIID, hardenedGoldII).setUnlocalizedName("hardenedGoldHoeII");
 		hardenedGoldHoeIII = new mods.storemore.items.tools.hardenedGoldHoeIII(sm_config.hardenedGoldHoeIIIID, hardenedGoldIII).setUnlocalizedName("hardenedGoldHoeIII");
 		hardenedGoldHoeIV = new mods.storemore.items.tools.hardenedGoldHoeIV(sm_config.hardenedGoldHoeIVID, hardenedGoldIV).setUnlocalizedName("hardenedGoldHoeIV");
 		hardenedDiamondHoeI = new mods.storemore.items.tools.hardenedDiamondHoeI(sm_config.hardenedDiamondHoeIID, hardenedDiamondI).setUnlocalizedName("hardenedDiamondHoeI");
 		hardenedDiamondHoeII = new mods.storemore.items.tools.hardenedDiamondHoeII(sm_config.hardenedDiamondHoeIIID, hardenedDiamondII).setUnlocalizedName("hardenedDiamondHoeII");
 		hardenedDiamondHoeIII = new mods.storemore.items.tools.hardenedDiamondHoeIII(sm_config.hardenedDiamondHoeIIIID, hardenedDiamondIII).setUnlocalizedName("hardenedDiamondHoeIII");
 		hardenedDiamondHoeIV = new mods.storemore.items.tools.hardenedDiamondHoeIV(sm_config.hardenedDiamondHoeIVID, hardenedDiamondIV).setUnlocalizedName("hardenedDiamondHoeIV");
 		
 		hardenedWoodSwordI = new mods.storemore.items.tools.hardenedWoodSwordI(sm_config.hardenedWoodSwordIID, hardenedWoodI).setUnlocalizedName("hardenedWoodSwordI");
 		hardenedWoodSwordII = new mods.storemore.items.tools.hardenedWoodSwordII(sm_config.hardenedWoodSwordIIID, hardenedWoodII).setUnlocalizedName("hardenedWoodSwordII");
 		hardenedWoodSwordIII = new mods.storemore.items.tools.hardenedWoodSwordIII(sm_config.hardenedWoodSwordIIIID, hardenedWoodIII).setUnlocalizedName("hardenedWoodSwordIII");
 		hardenedWoodSwordIV = new mods.storemore.items.tools.hardenedWoodSwordIV(sm_config.hardenedWoodSwordIVID, hardenedWoodIV).setUnlocalizedName("hardenedWoodSwordIV");
 		hardenedStoneSwordI = new mods.storemore.items.tools.hardenedStoneSwordI(sm_config.hardenedStoneSwordIID, hardenedStoneI).setUnlocalizedName("hardenedStoneSwordI");
 		hardenedStoneSwordII = new mods.storemore.items.tools.hardenedStoneSwordII(sm_config.hardenedStoneSwordIIID, hardenedStoneII).setUnlocalizedName("hardenedStoneSwordII");
 		hardenedStoneSwordIII = new mods.storemore.items.tools.hardenedStoneSwordIII(sm_config.hardenedStoneSwordIIIID, hardenedStoneIII).setUnlocalizedName("hardenedStoneSwordIII");
 		hardenedStoneSwordIV = new mods.storemore.items.tools.hardenedStoneSwordIV(sm_config.hardenedStoneSwordIVID, hardenedStoneIV).setUnlocalizedName("hardenedStoneSwordIV");
 		hardenedIronSwordI = new mods.storemore.items.tools.hardenedIronSwordI(sm_config.hardenedIronSwordIID, hardenedIronI).setUnlocalizedName("hardenedIronSwordI");
 		hardenedIronSwordII = new mods.storemore.items.tools.hardenedIronSwordII(sm_config.hardenedIronSwordIIID, hardenedIronII).setUnlocalizedName("hardenedIronSwordII");
 		hardenedIronSwordIII = new mods.storemore.items.tools.hardenedIronSwordIII(sm_config.hardenedIronSwordIIIID, hardenedIronIII).setUnlocalizedName("hardenedIronSwordIII");
 		hardenedIronSwordIV = new mods.storemore.items.tools.hardenedIronSwordIV(sm_config.hardenedIronSwordIVID, hardenedIronIV).setUnlocalizedName("hardenedIronSwordIV");
 		hardenedGoldSwordI = new mods.storemore.items.tools.hardenedGoldSwordI(sm_config.hardenedGoldSwordIID, hardenedGoldI).setUnlocalizedName("hardenedGoldSwordI");
 		hardenedGoldSwordII = new mods.storemore.items.tools.hardenedGoldSwordII(sm_config.hardenedGoldSwordIIID, hardenedGoldII).setUnlocalizedName("hardenedGoldSwordII");
 		hardenedGoldSwordIII = new mods.storemore.items.tools.hardenedGoldSwordIII(sm_config.hardenedGoldSwordIIIID, hardenedGoldIII).setUnlocalizedName("hardenedGoldSwordIII");
 		hardenedGoldSwordIV = new mods.storemore.items.tools.hardenedGoldSwordIV(sm_config.hardenedGoldSwordIVID, hardenedGoldIV).setUnlocalizedName("hardenedGoldSwordIV");
 		hardenedDiamondSwordI = new mods.storemore.items.tools.hardenedDiamondSwordI(sm_config.hardenedDiamondSwordIID, hardenedDiamondI).setUnlocalizedName("hardenedDiamondSwordI");
 		hardenedDiamondSwordII = new mods.storemore.items.tools.hardenedDiamondSwordII(sm_config.hardenedDiamondSwordIIID, hardenedDiamondII).setUnlocalizedName("hardenedDiamondSwordII");
 		hardenedDiamondSwordIII = new mods.storemore.items.tools.hardenedDiamondSwordIII(sm_config.hardenedDiamondSwordIIIID, hardenedDiamondIII).setUnlocalizedName("hardenedDiamondSwordIII");
 		hardenedDiamondSwordIV = new mods.storemore.items.tools.hardenedDiamondSwordIV(sm_config.hardenedDiamondSwordIVID, hardenedDiamondIV).setUnlocalizedName("hardenedDiamondSwordIV");
 		
 		//SuperBlocks
 		
 		PackedCobble8 = new mods.storemore.items.PackedCobble8(sm_config.PackedCobble8ID).setUnlocalizedName("PackedCobble8");
 		PackedStone8 = new mods.storemore.items.PackedStone8(sm_config.PackedStone8ID).setUnlocalizedName("PackedStone8");		
 		PackedDirt8 = new mods.storemore.items.PackedDirt8(sm_config.PackedDirt8ID).setUnlocalizedName("PackedDirt8");
 		PackedGravel8 = new mods.storemore.items.PackedGravel8(sm_config.PackedGravel8ID).setUnlocalizedName("PackedGravel8");		
 		PackedSand8 = new mods.storemore.items.PackedSand8(sm_config.PackedSand8ID).setUnlocalizedName("PackedSand8");
 		PackedSandstone8 = new mods.storemore.items.PackedSandstone7(sm_config.PackedSandstone8ID).setUnlocalizedName("PackedSandstone8");
 		
 		PackedCobble7 = new mods.storemore.items.PackedCobble7(sm_config.PackedCobble7ID).setUnlocalizedName("PackedCobble7");
 		PackedStone7 = new mods.storemore.items.PackedStone7(sm_config.PackedStone7ID).setUnlocalizedName("PackedStone7");		
 		PackedDirt7 = new mods.storemore.items.PackedDirt7(sm_config.PackedDirt7ID).setUnlocalizedName("PackedDirt7");
 		PackedGravel7 = new mods.storemore.items.PackedGravel7(sm_config.PackedGravel7ID).setUnlocalizedName("PackedGravel7");		
 		PackedSand7 = new mods.storemore.items.PackedSand7(sm_config.PackedSand7ID).setUnlocalizedName("PackedSand7");
 		PackedSandstone7 = new mods.storemore.items.PackedSandstone7(sm_config.PackedSandstone7ID).setUnlocalizedName("PackedSandstone7");
 		
 		PackedCobble6 = new mods.storemore.items.PackedCobble6(sm_config.PackedCobble6ID).setUnlocalizedName("PackedCobble6");
 		PackedStone6 = new mods.storemore.items.PackedStone6(sm_config.PackedStone6ID).setUnlocalizedName("PackedStone6");		
 		PackedDirt6 = new mods.storemore.items.PackedDirt6(sm_config.PackedDirt6ID).setUnlocalizedName("PackedDirt6");
 		PackedGravel6 = new mods.storemore.items.PackedGravel6(sm_config.PackedGravel6ID).setUnlocalizedName("PackedGravel6");		
 		PackedSand6 = new mods.storemore.items.PackedSand6(sm_config.PackedSand6ID).setUnlocalizedName("PackedSand6");
 		PackedSandstone6 = new mods.storemore.items.PackedSandstone6(sm_config.PackedSandstone6ID).setUnlocalizedName("PackedSandstone6");
 		
 		PackedCobble5 = new mods.storemore.items.PackedCobble5(sm_config.PackedCobble5ID).setUnlocalizedName("PackedCobble5");
 		PackedStone5 = new mods.storemore.items.PackedStone5(sm_config.PackedStone5ID).setUnlocalizedName("PackedStone5");		
 		PackedDirt5 = new mods.storemore.items.PackedDirt5(sm_config.PackedDirt5ID).setUnlocalizedName("PackedDirt5");
 		PackedGravel5 = new mods.storemore.items.PackedGravel5(sm_config.PackedGravel5ID).setUnlocalizedName("PackedGravel5");		
 		PackedSand5 = new mods.storemore.items.PackedSand5(sm_config.PackedSand5ID).setUnlocalizedName("PackedSand5");
 		PackedSandstone5 = new mods.storemore.items.PackedSandstone5(sm_config.PackedSandstone5ID).setUnlocalizedName("PackedSandstone5");
 		
 		PackedCobble4 = new mods.storemore.items.PackedCobble4(sm_config.PackedCobble4ID).setUnlocalizedName("PackedCobble4");
 		PackedStone4 = new mods.storemore.items.PackedStone4(sm_config.PackedStone4ID).setUnlocalizedName("PackedStone4");		
 		PackedDirt4 = new mods.storemore.items.PackedDirt4(sm_config.PackedDirt4ID).setUnlocalizedName("PackedDirt4");
 		PackedGravel4 = new mods.storemore.items.PackedGravel4(sm_config.PackedGravel4ID).setUnlocalizedName("PackedGravel4");		
 		PackedSand4 = new mods.storemore.items.PackedSand4(sm_config.PackedSand4ID).setUnlocalizedName("PackedSand4");
 		PackedSandstone4 = new mods.storemore.items.PackedSandstone4(sm_config.PackedSandstone4ID).setUnlocalizedName("PackedSandstone4");
 		
 		PackedCobble3 = new mods.storemore.items.PackedCobble3(sm_config.PackedCobble3ID).setUnlocalizedName("PackedCobble3");
 		PackedStone3 = new mods.storemore.items.PackedStone3(sm_config.PackedStone3ID).setUnlocalizedName("PackedStone3");		
 		PackedDirt3 = new mods.storemore.items.PackedDirt3(sm_config.PackedDirt3ID).setUnlocalizedName("PackedDirt3");
 		PackedGravel3 = new mods.storemore.items.PackedGravel3(sm_config.PackedGravel3ID).setUnlocalizedName("PackedGravel3");		
 		PackedSand3 = new mods.storemore.items.PackedSand3(sm_config.PackedSand3ID).setUnlocalizedName("PackedSand3");
 		PackedSandstone3 = new mods.storemore.items.PackedSandstone3(sm_config.PackedSandstone3ID).setUnlocalizedName("PackedSandstone3");
 		
 		PackedCobble2 = new mods.storemore.items.PackedCobble2(sm_config.PackedCobble2ID).setUnlocalizedName("PackedCobble2");
 		PackedStone2 = new mods.storemore.items.PackedStone2(sm_config.PackedStone2ID).setUnlocalizedName("PackedStone2");		
 		PackedDirt2 = new mods.storemore.items.PackedDirt2(sm_config.PackedDirt2ID).setUnlocalizedName("PackedDirt2");
 		PackedGravel2 = new mods.storemore.items.PackedGravel2(sm_config.PackedGravel2ID).setUnlocalizedName("PackedGravel2");		
 		PackedSand2 = new mods.storemore.items.PackedSand2(sm_config.PackedSand2ID).setUnlocalizedName("PackedSand2");
 		PackedSandstone2 = new mods.storemore.items.PackedSandstone2(sm_config.PackedSandstone2ID).setUnlocalizedName("PackedSandstone2");
 		
 		PackedCobble1 = new mods.storemore.items.PackedCobble1(sm_config.PackedCobble1ID).setUnlocalizedName("PackedCobble1");
 		PackedStone1 = new mods.storemore.items.PackedStone1(sm_config.PackedStone1ID).setUnlocalizedName("PackedStone1");		
 		PackedDirt1 = new mods.storemore.items.PackedDirt1(sm_config.PackedDirt1ID).setUnlocalizedName("PackedDirt1");
 		PackedGravel1 = new mods.storemore.items.PackedGravel1(sm_config.PackedGravel1ID).setUnlocalizedName("PackedGravel1");		
 		PackedSand1 = new mods.storemore.items.PackedSand1(sm_config.PackedSand1ID).setUnlocalizedName("PackedSand1");
 		PackedSandstone1 = new mods.storemore.items.PackedSandstone1(sm_config.PackedSandstone1ID).setUnlocalizedName("PackedSandstone1");
 		
 
 	
 	}
 
 
     
     
 	{
 
 		GameRegistry.registerFuelHandler(new storemoreFuels());
 		
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		sm_oredictionary.addOreRecipes();
 
 	}
 }
