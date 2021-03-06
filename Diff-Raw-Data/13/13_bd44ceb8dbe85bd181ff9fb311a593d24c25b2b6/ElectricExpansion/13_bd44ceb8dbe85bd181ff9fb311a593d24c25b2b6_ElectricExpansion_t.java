 package electricexpansion;
 
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
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import electricexpansion.alex_hawks.blocks.*;
 import electricexpansion.alex_hawks.items.ItemBlockInsualtedWire;
 import electricexpansion.alex_hawks.items.ItemBlockRawWire;
 import electricexpansion.alex_hawks.items.ItemBlockSwitchWire;
 import electricexpansion.alex_hawks.items.ItemBlockSwitchWireBlock;
 import electricexpansion.alex_hawks.items.ItemBlockSwitchWireBlockOff;
 import electricexpansion.alex_hawks.items.ItemBlockSwitchWireOff;
 import electricexpansion.alex_hawks.items.ItemBlockWireBlock;
 import electricexpansion.mattredsox.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.Configuration;
 
 import universalelectricity.BasicComponents;
 import universalelectricity.UEConfig;
 import universalelectricity.network.ConnectionHandler;
 import universalelectricity.network.PacketManager;
 import universalelectricity.recipe.RecipeManager;
 
 @Mod(modid="ElectricExpansion", name="Electric Expansion", version="0.2.2", dependencies = "after:UniversalElectricity", useMetadata = true)
 @NetworkMod(channels = { "ElecEx" }, clientSideRequired = true, serverSideRequired = false, connectionHandler = ConnectionHandler.class, packetHandler = PacketManager.class)
 public class ElectricExpansion {
 
 	public static int[] versionArray = {0, 2, 2}; //Change EVERY release!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	public static String version;
 	public static final int BLOCK_ID_PREFIX = 3980;
 	public static final int ITEM_ID_PREFIX = 15970;
 	
 	//private, these are the default options.
 	//Blocks
 	private static final int rawWireID = BLOCK_ID_PREFIX;
 	private static final int insulatedWireID = BLOCK_ID_PREFIX + 1;
 	private static final int wireBlocksID = BLOCK_ID_PREFIX + 2;
 	private static final int switchWireID = BLOCK_ID_PREFIX + 3;
 	private static final int switchWireBlockID = BLOCK_ID_PREFIX + 4; 
 	private static final int offSwitchWireID = BLOCK_ID_PREFIX + 5;
 	private static final int offSwitchWireBlockID = BLOCK_ID_PREFIX + 6;
 	//private static final int redstoneWireID = BLOCK_ID_PREFIX + 7;
 	//private static final int redstoneWireBlockID = BLOCK_ID_PREFIX + 8;
 	private static final int blockBigBatteryBoxID = BLOCK_ID_PREFIX + 9;
 	private static final int blockVoltDetID = BLOCK_ID_PREFIX + 10;
 	private static final int blockUPTransformerID = BLOCK_ID_PREFIX + 11;
 	private static final int blockDOWNTransformerID = BLOCK_ID_PREFIX + 12;
 	private static final int blockWireMillID = BLOCK_ID_PREFIX + 13;
 	private static final int blockFuseID = BLOCK_ID_PREFIX + 14;
 	//Items
 	private static final int itemUpgradeID = ITEM_ID_PREFIX;
 	//Other
 	private static final int superConductorUpkeepDefault = 500;
 
 	//Runtime Values
 	//Blocks
 	public static int rawWire;
 	public static int insulatedWire;
 	public static int wireBlocks;
 	public static int onSwitchWire;
 	public static int onSwitchWireBlock;
 	public static int offSwitchWire;
 	public static int offSwitchWireBlock;
 	//public static int redstoneWire;
 	//public static int redstoneWireBlock;
 	public static int BigBatteryBox;
 	public static int VoltDet;
 	public static int UPTransformer;
 	public static int DOWNTransformer;
 	public static int wireMill;
 	public static int Fuse;
 	//Items
 	public static int Upgrade;
 	//Other
 	public static double superConductorUpkeep;
 	
 	public static final Configuration CONFIG = new Configuration(new File("config/UniversalElectricity/ElectricExpansion.cfg"));
 	public static boolean configLoaded = configLoad(CONFIG);
 	
 	//Blocks
 	public static final Block blockRawWire = new BlockRawWire(rawWire, 0);
 	public static final Block blockInsulatedWire = new BlockInsulatedWire(insulatedWire, 0);
 	public static final Block blockWireBlock = new BlockWireBlock(wireBlocks, 0);
 	public static final Block blockSwitchWire = new BlockSwitchWire(onSwitchWire, 0);
 	public static final Block blockSwitchWireBlock = new BlockSwitchWireBlock(onSwitchWireBlock, 0);
 	public static final Block blockSwitchWireOff = new BlockSwitchWireOff(offSwitchWire, 0);
 	public static final Block blockSwitchWireBlockOff = new BlockSwitchWireBlockOff(offSwitchWireBlock, 0);
 	//public static final Block blockRedstoneWire = new BlockRedstoneWire(redstoneWire, 0);
 	//public static final Block blockRedstoneWireBlock = new BlockRedstoneWireBlock(redstoneWireBlock, 0);
 	public static final Block blockBigBatteryBox = new BlockAdvBatteryBox(BigBatteryBox, 0).setCreativeTab(CreativeTabs.tabDecorations);
     public static final Block blockVoltDet = new BlockVoltDetector(VoltDet, 0).setCreativeTab(CreativeTabs.tabDecorations);
     public static final Block blockUPTransformer = new BlockUPTransformer(UPTransformer, 0).setCreativeTab(CreativeTabs.tabDecorations);
     public static final Block blockDOWNTransformer = new BlockDOWNTransformer(DOWNTransformer, 0).setCreativeTab(CreativeTabs.tabDecorations);
     public static final Block blockWireMill = new BlockWireMill(wireMill).setCreativeTab(CreativeTabs.tabDecorations).setBlockName("blockEtcher");
     public static final Block blockFuse = new BlockFuse(Fuse, 0).setCreativeTab(CreativeTabs.tabDecorations).setBlockName("blockFuse");
     
 	//Items
     public static final Item itemUpgrade = new ItemUpgrade(Upgrade, 0).setCreativeTab(CreativeTabs.tabMisc).setItemName("Upgrade");
     
 	public static Logger ACLogger = Logger.getLogger("ElectricExpansion");
 	public static boolean[] startLogLogged = {false, false, false, false};
 	
 	@Instance("ElectricExpansion")
 	public static ElectricExpansion instance;
 	
 	@SidedProxy(clientSide="electricexpansion.client.EEClientProxy", serverSide="electricexpansion.EECommonProxy")
 	public static EECommonProxy proxy;
 	
 	public static boolean configLoad(Configuration i)
 	{
 		rawWire = UEConfig.getBlockConfigID(i, "Uninsulated_Wire", rawWireID);
 		insulatedWire = UEConfig.getBlockConfigID(i, "Insualted_Wire", insulatedWireID);
 		wireBlocks = UEConfig.getBlockConfigID(i, "Wire_Block", wireBlocksID);
 		onSwitchWire = UEConfig.getBlockConfigID(i, "Switch_Wire", switchWireID);
 		onSwitchWireBlock = UEConfig.getBlockConfigID(i, "Switch_Wire_Block", switchWireBlockID);
 		offSwitchWire = UEConfig.getBlockConfigID(i, "Switch_Wire_Off", offSwitchWireID);
 		offSwitchWireBlock = UEConfig.getBlockConfigID(i, "Switch_Wire_Block_Off", offSwitchWireBlockID);
 		//Redstone'd Insulated Cable
 		//Redstone'd Cable Blocks
 		
 		BigBatteryBox = UEConfig.getBlockConfigID(i, "Advanced_Bat_Box", blockBigBatteryBoxID);
 		VoltDet = UEConfig.getBlockConfigID(i, "Voltage_Detector", blockVoltDetID);
 		UPTransformer = UEConfig.getBlockConfigID(i, "Up_Transformer", blockUPTransformerID);
 		DOWNTransformer = UEConfig.getBlockConfigID(i, "Down_Transformer", blockDOWNTransformerID);
 		wireMill = UEConfig.getBlockConfigID(i, "Etcher", blockWireMillID);
 		Fuse = UEConfig.getBlockConfigID(i, "Relay", blockFuseID);
 		Upgrade = UEConfig.getItemConfigID(i, "Advanced_Bat_Box_Upgrade", itemUpgradeID);
 		
 		superConductorUpkeep = (double)((UEConfig.getItemConfigID(i, "Super_Conductor_Upkeep", superConductorUpkeepDefault))/10);
 		i.getOrCreateIntProperty("Super_Conductor_Upkeep", Configuration.CATEGORY_GENERAL, superConductorUpkeepDefault).comment = "Divide by 10 to get the Watt upkeep cost for EACH Super-Conductor Cable's super-conducting function.";
 
 		configLoaded = true;
 		return true; //returns true to configLoaded VAR
 	}
 	
 	private static void StartLog(String string1)
 	{
 		int i;
 		version = "v";
 		for (i=0; i<versionArray.length; i++)
 			version = version + "." + versionArray[i];
 		String string2 = null;
 		int j = 0;
 		
 		if(string1 != null && string1 == "preInit")
 			{
 			string2 = "PreInitializing";
 			j = 1;
 			}
 		if(string1 != null && string1 == "Init")
 			{
 			string2 = "Initializing";
 			j = 2;
 			}
 		if(string1 != null && string1 == "postInit")
 			{
 			string2 = "PostInitializing";
 			j = 3;
 			}
 		
 		ACLogger.setParent(FMLLog.getLogger());
 		ACLogger.info(string2 + " ElectricExpansion " + version);
 		startLogLogged[j] = true;
 		if(startLogLogged[1] && startLogLogged[2] && startLogLogged[3])
 			startLogLogged[0] = true;
 	}
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) 
 	{
 		if(!configLoaded){configLoad(CONFIG);}
 		if(startLogLogged[1] != true){StartLog("preInit");}
 		Item.itemsList[rawWire] = new ItemBlockRawWire(rawWire-256, blockRawWire);
 		Item.itemsList[insulatedWire] = new ItemBlockInsualtedWire(insulatedWire-256, blockInsulatedWire);
 		Item.itemsList[wireBlocks] = new ItemBlockWireBlock(wireBlocks-256, blockWireBlock);
 		Item.itemsList[onSwitchWire] = new ItemBlockSwitchWire(onSwitchWire-256, blockSwitchWire);
 		Item.itemsList[onSwitchWireBlock] = new ItemBlockSwitchWireBlock(onSwitchWireBlock-256, blockSwitchWireBlock);
 		Item.itemsList[offSwitchWire] = new ItemBlockSwitchWireOff(offSwitchWire-256, blockSwitchWireOff);
 		Item.itemsList[offSwitchWireBlock] = new ItemBlockSwitchWireBlockOff(offSwitchWireBlock-256, blockSwitchWireBlockOff);
 		//Redstone'd Insulated Cable
 		//Redstone'd Cable Blocks
 		GameRegistry.registerBlock(blockBigBatteryBox);
 		GameRegistry.registerBlock(blockDOWNTransformer);
 		GameRegistry.registerBlock(blockUPTransformer);
 		GameRegistry.registerBlock(blockWireMill);
 		GameRegistry.registerBlock(blockVoltDet);
 		instance = this;
 
 		MinecraftForgeClient.preloadTexture("/electricexpansion/textures/mattredsox/blocks1.png");
 		MinecraftForgeClient.preloadTexture("/electricexpansion/textures/mattredsox/blocks.png");
 
 		NetworkRegistry.instance().registerGuiHandler(this, this.proxy);
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) 
 	{
 		if(startLogLogged[2] != true){StartLog("Init");}
 		proxy.init();
 		
 		//Uninsulated Wire Recipes
 		RecipeManager.addRecipe(new ItemStack(blockRawWire, 7, 0), new Object [] {" @ ", " @ ", " @ ", '@', "ingotCopper"});
 		RecipeManager.addRecipe(new ItemStack(blockRawWire, 7, 1), new Object [] {" @ ", " @ ", " @ ", '@', "ingotTin"});
 		RecipeManager.addRecipe(new ItemStack(blockRawWire, 7, 2), new Object [] {" @ ", " @ ", " @ ", '@', "ingotSilver"});
 		RecipeManager.addRecipe(new ItemStack(blockRawWire, 7, 3), new Object [] {" @ ", " @ ", " @ ", '@', "ingotAluminium"});
 		
 		//Recipes for supporting other UE add-ons, the slack way...
 		RecipeManager.addShapelessRecipe(new ItemStack(BasicComponents.blockCopperWire, 1), new Object[]{new ItemStack(blockInsulatedWire, 1, 0)});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockInsulatedWire, 1, 0), new Object[]{new ItemStack(BasicComponents.blockCopperWire, 1)});
 
 		//Shapeless Insulated Wire Recipes (From insulation, and the corresponding Uninsulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockInsulatedWire, 1, 0), new Object[]{new ItemStack(blockRawWire, 1, 0), Block.cloth});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockInsulatedWire, 1, 1), new Object[]{new ItemStack(blockRawWire, 1, 1), Block.cloth});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockInsulatedWire, 1, 2), new Object[]{new ItemStack(blockRawWire, 1, 2), Block.cloth});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockInsulatedWire, 1, 3), new Object[]{new ItemStack(blockRawWire, 1, 3), Block.cloth});
 		
 		//Shaped Insulated Wire Recipes (From insulation, and the corresponding OreDictionary Ingots)
 		RecipeManager.addRecipe(new ItemStack(blockInsulatedWire, 7, 0), new Object [] {"#@#", "#@#", "#@#", '#', Block.cloth, '@', "ingotCopper"});
 		RecipeManager.addRecipe(new ItemStack(blockInsulatedWire, 7, 1), new Object [] {"#@#", "#@#", "#@#", '#', Block.cloth, '@', "ingotTin"});
 		RecipeManager.addRecipe(new ItemStack(blockInsulatedWire, 7, 2), new Object [] {"#@#", "#@#", "#@#", '#', Block.cloth, '@', "ingotSilver"});
 		RecipeManager.addRecipe(new ItemStack(blockInsulatedWire, 7, 3), new Object [] {"#@#", "#@#", "#@#", '#', Block.cloth, '@', "ingotAluminium"});
 		
 		//Wire Block Recipes (From insulation, Block.stone, and the corresponding Uninsulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 0), new Object[]{new ItemStack(blockRawWire, 1, 0), Block.cloth, Block.stone});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 1), new Object[]{new ItemStack(blockRawWire, 1, 1), Block.cloth, Block.stone});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 2), new Object[]{new ItemStack(blockRawWire, 1, 2), Block.cloth, Block.stone});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 3), new Object[]{new ItemStack(blockRawWire, 1, 3), Block.cloth, Block.stone});
 		
 		//Wire Block Recipes (From Block.stone, and the corresponding Insulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 0), new Object[]{new ItemStack(blockInsulatedWire, 1, 0), Block.stone});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 1), new Object[]{new ItemStack(blockInsulatedWire, 1, 1), Block.stone});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 2), new Object[]{new ItemStack(blockInsulatedWire, 1, 2), Block.stone});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockWireBlock, 1, 3), new Object[]{new ItemStack(blockInsulatedWire, 1, 3), Block.stone});
 		
 		//Shapeless Switch Wire Recipes (From insulation, a lever, and the corresponding Uninsulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 0), new Object[]{new ItemStack(blockRawWire, 1, 0), Block.cloth, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 1), new Object[]{new ItemStack(blockRawWire, 1, 1), Block.cloth, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 2), new Object[]{new ItemStack(blockRawWire, 1, 2), Block.cloth, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 3), new Object[]{new ItemStack(blockRawWire, 1, 3), Block.cloth, Block.lever});
 		
 		//Shapeless Switch Wire Recipes (From insulation, a lever, and the corresponding Uninsulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 0), new Object[]{new ItemStack(blockInsulatedWire, 1, 0), Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 1), new Object[]{new ItemStack(blockInsulatedWire, 1, 1), Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 2), new Object[]{new ItemStack(blockInsulatedWire, 1, 2), Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireOff, 1, 3), new Object[]{new ItemStack(blockInsulatedWire, 1, 3), Block.lever});
 		
 		//Switch Wire Block Recipes (From insulation, Block.stone, Block.lever and the corresponding Uninsulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 0), new Object[]{new ItemStack(blockRawWire, 1, 0), Block.cloth, Block.stone, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 1), new Object[]{new ItemStack(blockRawWire, 1, 1), Block.cloth, Block.stone, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 2), new Object[]{new ItemStack(blockRawWire, 1, 2), Block.cloth, Block.stone, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 3), new Object[]{new ItemStack(blockRawWire, 1, 3), Block.cloth, Block.stone, Block.lever});
 		
 		//Switch Wire Block Recipes (From Block.stone, Block,lever and the corresponding Insulated Wire)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 0), new Object[]{new ItemStack(blockInsulatedWire, 1, 0), Block.stone, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 1), new Object[]{new ItemStack(blockInsulatedWire, 1, 1), Block.stone, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 2), new Object[]{new ItemStack(blockInsulatedWire, 1, 2), Block.stone, Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 3), new Object[]{new ItemStack(blockInsulatedWire, 1, 3), Block.stone, Block.lever});
 		
 		//Switch Wire Block Recipes (From Block.lever, and the corresponding Wire Block)
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 0), new Object[]{new ItemStack(blockWireBlock, 1, 0), Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 1), new Object[]{new ItemStack(blockWireBlock, 1, 1), Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 2), new Object[]{new ItemStack(blockWireBlock, 1, 2), Block.lever});
 		RecipeManager.addShapelessRecipe(new ItemStack(blockSwitchWireBlockOff, 1, 3), new Object[]{new ItemStack(blockWireBlock, 1, 3), Block.lever});
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) 
 	{
 		if(startLogLogged[3] != true){StartLog("postInit");}
 		
 		//Set the Uninsulated Cable Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.RawWire.Copper.name", "Uninsulated Copper Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.RawWire.Tin.name", "Uninsulated Tin Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.RawWire.Silver.name", "Uninsulated Silver Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.RawWire.HV.name", "Uninsulated HV Wire");
 		//Set the Insulated Cable Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.InsulatedWire.Copper.name", "Insulated Copper Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.InsulatedWire.Tin.name", "Insulated Tin Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.InsulatedWire.Silver.name", "Insulated Silver Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.InsulatedWire.HV.name", "Insulated HV Wire");
 		//Set the Hidden Cable Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.HiddenWire.Copper.name", "Hidden Copper Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.HiddenWire.Tin.name", "Hidden Tin Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.HiddenWire.Silver.name", "Hidden Silver Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.HiddenWire.HV.name", "Hidden HV Wire");
 		//Set the Switch Cable (On/Crafted) Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWire.Copper.name", "Copper Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWire.Tin.name", "Tin Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWire.Silver.name", "Silver Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWire.HV.name", "HV Switch Wire");
 		//Set the Switch Cable Block (On/Crafted) Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlock.Copper.name", "Hidden Copper Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlock.Tin.name", "Hidden Tin Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlock.Silver.name", "Hidden Silver Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlock.HV.name", "Hidden HV Switch Wire");
 		//Set the Switch Cable (Off) Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireOff.Copper.name", "Copper Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireOff.Tin.name", "Tin Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireOff.Silver.name", "Silver Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireOff.HV.name", "HV Switch Wire");
 		//Set the Switch Cable Block (Off) Name(s)
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlockOff.Copper.name", "Hidden Copper Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlockOff.Tin.name", "Hidden Tin Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlockOff.Silver.name", "Hidden Silver Switch Wire");
 		LanguageRegistry.instance().addStringLocalization("tile.SwitchWireBlockOff.HV.name", "Hidden HV Switch Wire");
 
 		LanguageRegistry.addName(blockUPTransformer, "Up Transformer");
         LanguageRegistry.addName(blockBigBatteryBox, "Advanced Battery Box");
         LanguageRegistry.addName(blockDOWNTransformer, "Down Transformer");
         LanguageRegistry.addName(blockVoltDet, "Voltage Detector");
         LanguageRegistry.addName(blockWireMill, "Wire Mill");
         LanguageRegistry.addName(blockFuse, "120 Volt Relay");
         LanguageRegistry.addName(itemUpgrade, "Superconducting Upgrade");
         
 	//	RecipeManager.addRecipes();
 	}
 }
