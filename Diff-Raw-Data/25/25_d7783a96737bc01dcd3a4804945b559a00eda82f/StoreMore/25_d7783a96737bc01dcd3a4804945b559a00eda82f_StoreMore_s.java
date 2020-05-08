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
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.oredict.OreDictionary;
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
 
 @Mod(modid = "StoreMore", name = "StoreMore", version = "1.3.5_MC1.5.1", useMetadata = false, dependencies = "before:StoreMore|IC2")
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
 		
 	}
 
     public static void oreRegistration()
     {
             OreDictionary.registerOre("packedCobbleI", new ItemStack(packedblocksI,1,0));
             OreDictionary.registerOre("packedCobbleII", new ItemStack(packedblocksI,1,1));
             OreDictionary.registerOre("packedCobbleIII", new ItemStack(packedblocksI,1,2));
             OreDictionary.registerOre("packedCobbleIV", new ItemStack(packedblocksI,1,3));
             OreDictionary.registerOre("packedCobbleV", new ItemStack(packedblocksI,1,4));
             OreDictionary.registerOre("packedCobbleVI", new ItemStack(packedblocksI,1,5));
             OreDictionary.registerOre("packedCobbleVII", new ItemStack(packedblocksI,1,6));
             OreDictionary.registerOre("packedStoneVIII", new ItemStack(packedblocksI,1,7));
             OreDictionary.registerOre("packedStoneI", new ItemStack(packedblocksI,1,8));
             OreDictionary.registerOre("packedStoneII", new ItemStack(packedblocksI,1,9));
             OreDictionary.registerOre("packedStoneIII", new ItemStack(packedblocksI,1,10));
             OreDictionary.registerOre("packedStoneIV", new ItemStack(packedblocksI,1,11));
             OreDictionary.registerOre("packedStoneV", new ItemStack(packedblocksI,1,12));
             OreDictionary.registerOre("packedStoneVI", new ItemStack(packedblocksI,1,3));
             OreDictionary.registerOre("packedStoneVII", new ItemStack(packedblocksI,1,14));
             OreDictionary.registerOre("packedStoneVIII", new ItemStack(packedblocksI,1,15));
             OreDictionary.registerOre("packedDirtI", new ItemStack(packedblocksII,1,0));
             OreDictionary.registerOre("packedDirtII", new ItemStack(packedblocksII,1,1));
             OreDictionary.registerOre("packedDirtIII", new ItemStack(packedblocksII,1,2));
             OreDictionary.registerOre("packedDirtIV", new ItemStack(packedblocksII,1,3));
             OreDictionary.registerOre("packedDirtV", new ItemStack(packedblocksII,1,4));
             OreDictionary.registerOre("packedDirtVI", new ItemStack(packedblocksII,1,5));
             OreDictionary.registerOre("packedDirtVII", new ItemStack(packedblocksII,1,6));
             OreDictionary.registerOre("packedDirtVIII", new ItemStack(packedblocksII,1,7));
             OreDictionary.registerOre("packedGravelI", new ItemStack(packedblocksII,1,8));
             OreDictionary.registerOre("packedGravelII", new ItemStack(packedblocksII,1,9));
             OreDictionary.registerOre("packedGravelIII", new ItemStack(packedblocksII,1,10));
             OreDictionary.registerOre("packedGravelIV", new ItemStack(packedblocksII,1,11));
             OreDictionary.registerOre("packedGravelV", new ItemStack(packedblocksII,1,12));
             OreDictionary.registerOre("packedGravelVI", new ItemStack(packedblocksII,1,3));
             OreDictionary.registerOre("packedGravelVII", new ItemStack(packedblocksII,1,14));
             OreDictionary.registerOre("packedGravelVIII", new ItemStack(packedblocksII,1,15));
             OreDictionary.registerOre("packedSandI", new ItemStack(packedblocksIII,1,0));
             OreDictionary.registerOre("packedSandII", new ItemStack(packedblocksIII,1,1));
             OreDictionary.registerOre("packedSandIII", new ItemStack(packedblocksIII,1,2));
             OreDictionary.registerOre("packedSandIV", new ItemStack(packedblocksIII,1,3));
             OreDictionary.registerOre("packedSandV", new ItemStack(packedblocksIII,1,4));
             OreDictionary.registerOre("packedSandVI", new ItemStack(packedblocksIII,1,5));
             OreDictionary.registerOre("packedSandVII", new ItemStack(packedblocksIII,1,6));
             OreDictionary.registerOre("packedSandVIII", new ItemStack(packedblocksIII,1,7));
             OreDictionary.registerOre("packedSandstonelI", new ItemStack(packedblocksIII,1,8));
             OreDictionary.registerOre("packedSandstonelII", new ItemStack(packedblocksIII,1,9));
             OreDictionary.registerOre("packedSandstonelIII", new ItemStack(packedblocksIII,1,10));
             OreDictionary.registerOre("packedSandstonelIV", new ItemStack(packedblocksIII,1,11));
             OreDictionary.registerOre("packedSandstonelV", new ItemStack(packedblocksIII,1,12));
             OreDictionary.registerOre("packedSandstonelVI", new ItemStack(packedblocksIII,1,3));
             OreDictionary.registerOre("packedSandstonelVII", new ItemStack(packedblocksIII,1,14));
             OreDictionary.registerOre("packedSandstonelVIII", new ItemStack(packedblocksIII,1,15));
             OreDictionary.registerOre("packedObsidianI", new ItemStack(packedblocksIV,1,0));
             OreDictionary.registerOre("packedObsidianII", new ItemStack(packedblocksIV,1,1));
             OreDictionary.registerOre("packedObsidianIII", new ItemStack(packedblocksIV,1,2));
             OreDictionary.registerOre("packedObsidianIV", new ItemStack(packedblocksIV,1,3));
             OreDictionary.registerOre("packedGlowstoneV", new ItemStack(packedblocksIV,1,4));
             OreDictionary.registerOre("packedGlowstoneVI", new ItemStack(packedblocksIV,1,5));
             OreDictionary.registerOre("packedGlowstoneVII", new ItemStack(packedblocksIV,1,6));
             OreDictionary.registerOre("packedGlowstoneVIII", new ItemStack(packedblocksIV,1,7));
             OreDictionary.registerOre("packedNetherracklI", new ItemStack(packedblocksIV,1,8));
             OreDictionary.registerOre("packedNetherracklII", new ItemStack(packedblocksIV,1,9));
             OreDictionary.registerOre("packedNetherracklIII", new ItemStack(packedblocksIV,1,10));
             OreDictionary.registerOre("packedNetherracklIV", new ItemStack(packedblocksIV,1,11));
             OreDictionary.registerOre("packedIronI", new ItemStack(packedblocksV,1,0));
             OreDictionary.registerOre("packedIronII", new ItemStack(packedblocksV,1,1));
             OreDictionary.registerOre("packedIronIII", new ItemStack(packedblocksV,1,2));
             OreDictionary.registerOre("packedIronIV", new ItemStack(packedblocksV,1,3));
             OreDictionary.registerOre("packedGoldV", new ItemStack(packedblocksV,1,4));
             OreDictionary.registerOre("packedGoldVI", new ItemStack(packedblocksV,1,5));
             OreDictionary.registerOre("packedGoldVII", new ItemStack(packedblocksV,1,6));
             OreDictionary.registerOre("packedGoldVIII", new ItemStack(packedblocksV,1,7));
             OreDictionary.registerOre("packedDiamondlI", new ItemStack(packedblocksV,1,8));
             OreDictionary.registerOre("packedDiamondlII", new ItemStack(packedblocksV,1,9));
             OreDictionary.registerOre("packedDiamondlIII", new ItemStack(packedblocksV,1,10));
             OreDictionary.registerOre("packedDiamondlIV", new ItemStack(packedblocksV,1,11));
             OreDictionary.registerOre("packedEmeraldlI", new ItemStack(packedblocksV,1,12));
             OreDictionary.registerOre("packedEmeraldlII", new ItemStack(packedblocksV,1,13));
             OreDictionary.registerOre("packedEmeraldlIII", new ItemStack(packedblocksV,1,14));
             OreDictionary.registerOre("packedEmeraldlIV", new ItemStack(packedblocksV,1,15));
             OreDictionary.registerOre("packedSticksI", new ItemStack(packedblocksVI,1,0));
             OreDictionary.registerOre("packedSticksII", new ItemStack(packedblocksVI,1,1));
             OreDictionary.registerOre("packedSticksIII", new ItemStack(packedblocksVI,1,2));
             OreDictionary.registerOre("packedSticksIV", new ItemStack(packedblocksVI,1,3));
             OreDictionary.registerOre("packedTorchesV", new ItemStack(packedblocksVI,1,4));
             OreDictionary.registerOre("packedTorchesVI", new ItemStack(packedblocksVI,1,5));
             OreDictionary.registerOre("packedTorchesVII", new ItemStack(packedblocksVI,1,6));
             OreDictionary.registerOre("packedTorchesVIII", new ItemStack(packedblocksVI,1,7));
             OreDictionary.registerOre("packedGunpowderlI", new ItemStack(packedblocksVI,1,8));
             OreDictionary.registerOre("packedGunpowderlII", new ItemStack(packedblocksVI,1,9));
             OreDictionary.registerOre("packedGunpowderlIII", new ItemStack(packedblocksVI,1,10));
             OreDictionary.registerOre("packedGunpowderlIV", new ItemStack(packedblocksVI,1,11));
             OreDictionary.registerOre("packedFlintlI", new ItemStack(packedblocksVI,1,12));
             OreDictionary.registerOre("packedFlintlII", new ItemStack(packedblocksVI,1,13));
             OreDictionary.registerOre("packedFlintlIII", new ItemStack(packedblocksVI,1,14));
             OreDictionary.registerOre("packedFlintlIV", new ItemStack(packedblocksVI,1,15));
             OreDictionary.registerOre("packedWoodI", new ItemStack(packedblocksVII,1,0));
             OreDictionary.registerOre("packedWoodII", new ItemStack(packedblocksVII,1,1));
             OreDictionary.registerOre("packedWoodIII", new ItemStack(packedblocksVII,1,2));
             OreDictionary.registerOre("packedWoodIV", new ItemStack(packedblocksVII,1,3));
             OreDictionary.registerOre("packedWoodV", new ItemStack(packedblocksVII,1,4));
             OreDictionary.registerOre("packedWoodVI", new ItemStack(packedblocksVII,1,5));
             OreDictionary.registerOre("packedWoodVII", new ItemStack(packedblocksVII,1,6));
             OreDictionary.registerOre("packedWoodVIII", new ItemStack(packedblocksVII,1,7));
             OreDictionary.registerOre("packedWoodI", new ItemStack(packedblocksVII,1,8));
             OreDictionary.registerOre("packedWoodII", new ItemStack(packedblocksVII,1,9));
             OreDictionary.registerOre("packedWoodIII", new ItemStack(packedblocksVII,1,10));
             OreDictionary.registerOre("packedWoodIV", new ItemStack(packedblocksVII,1,11));
             OreDictionary.registerOre("packedWoodI", new ItemStack(packedblocksVII,1,12));
             OreDictionary.registerOre("packedWoodII", new ItemStack(packedblocksVII,1,13));
             OreDictionary.registerOre("packedWoodIII", new ItemStack(packedblocksVII,1,14));
             OreDictionary.registerOre("packedWoodIV", new ItemStack(packedblocksVII,1,15));
             OreDictionary.registerOre("packedPlanksI", new ItemStack(packedblocksVIII,1,0));
             OreDictionary.registerOre("packedPlanksII", new ItemStack(packedblocksVIII,1,1));
             OreDictionary.registerOre("packedPlanksIII", new ItemStack(packedblocksVIII,1,2));
             OreDictionary.registerOre("packedPlanksIV", new ItemStack(packedblocksVIII,1,3));
             OreDictionary.registerOre("packedPlanksV", new ItemStack(packedblocksVIII,1,4));
             OreDictionary.registerOre("packedPlanksVI", new ItemStack(packedblocksVIII,1,5));
             OreDictionary.registerOre("packedPlanksVII", new ItemStack(packedblocksVIII,1,6));
             OreDictionary.registerOre("packedPlanksVIII", new ItemStack(packedblocksVIII,1,7));
             OreDictionary.registerOre("packedPlanksI", new ItemStack(packedblocksVIII,1,8));
             OreDictionary.registerOre("packedPlanksII", new ItemStack(packedblocksVIII,1,9));
             OreDictionary.registerOre("packedPlanksIII", new ItemStack(packedblocksVIII,1,10));
             OreDictionary.registerOre("packedPlanksIV", new ItemStack(packedblocksVIII,1,11));
             OreDictionary.registerOre("packedPlanksI", new ItemStack(packedblocksVIII,1,12));
             OreDictionary.registerOre("packedPlanksII", new ItemStack(packedblocksVIII,1,13));
             OreDictionary.registerOre("packedPlanksIII", new ItemStack(packedblocksVIII,1,14));
             OreDictionary.registerOre("packedPlanksIV", new ItemStack(packedblocksVIII,1,15));
             OreDictionary.registerOre("packedRedstoneI", new ItemStack(packedblocksIX,1,0));
             OreDictionary.registerOre("packedRedstoneII", new ItemStack(packedblocksIX,1,1));
             OreDictionary.registerOre("packedRedstoneIII", new ItemStack(packedblocksIX,1,2));
             OreDictionary.registerOre("packedRedstoneIV", new ItemStack(packedblocksIX,1,3));
             OreDictionary.registerOre("packedQuartzV", new ItemStack(packedblocksIX,1,4));
             OreDictionary.registerOre("packedQuartzVI", new ItemStack(packedblocksIX,1,5));
             OreDictionary.registerOre("packedQuartzVII", new ItemStack(packedblocksIX,1,6));
             OreDictionary.registerOre("packedQuartzVIII", new ItemStack(packedblocksIX,1,7));
             OreDictionary.registerOre("packedSoulsandlI", new ItemStack(packedblocksIX,1,8));
             OreDictionary.registerOre("packedSoulsandlII", new ItemStack(packedblocksIX,1,9));
             OreDictionary.registerOre("packedSoulsandlIII", new ItemStack(packedblocksIX,1,10));
             OreDictionary.registerOre("packedSoulsandlIV", new ItemStack(packedblocksIX,1,11));
             OreDictionary.registerOre("packedClaylI", new ItemStack(packedblocksIX,1,12));
             OreDictionary.registerOre("packedClaylII", new ItemStack(packedblocksIX,1,13));
             OreDictionary.registerOre("packedClaylIII", new ItemStack(packedblocksIX,1,14));
             OreDictionary.registerOre("packedClaylIV", new ItemStack(packedblocksIX,1,15));
     
     
     }
     
     
 	{
 
 		GameRegistry.registerFuelHandler(new storemoreFuels());
 		
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 
 
 
 	}
 }
