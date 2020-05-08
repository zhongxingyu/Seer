 package NetherStuffs;
 
 import net.minecraft.src.BiomeGenBase;
 import net.minecraft.src.BiomeGenHell;
 import net.minecraft.src.Block;
 import net.minecraft.src.EntitySkeleton;
 import net.minecraft.src.FurnaceRecipes;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.SpawnListEntry;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import NetherStuffs.Blocks.NetherBlocks;
 import NetherStuffs.Blocks.NetherLeavesItemBlock;
 import NetherStuffs.Blocks.NetherOreItemBlock;
 import NetherStuffs.Blocks.NetherPlankItemBlock;
 import NetherStuffs.Blocks.NetherPuddleItemBlock;
 import NetherStuffs.Blocks.NetherSaplingItemBlock;
 import NetherStuffs.Blocks.NetherWoodItemBlock;
 import NetherStuffs.Blocks.SoulBlocker;
 import NetherStuffs.Blocks.SoulBlockerItemBlock;
 import NetherStuffs.Blocks.SoulBombItemBlock;
 import NetherStuffs.Blocks.SoulDetectorItemBlock;
 import NetherStuffs.Client.ClientPacketHandler;
 import NetherStuffs.Common.CommonProxy;
 import NetherStuffs.Common.GuiHandler;
 import NetherStuffs.Common.NetherStuffsFuel;
 import NetherStuffs.Common.ServerPacketHandler;
 import NetherStuffs.DemonicFurnace.DemonicFurnaceRecipes;
 import NetherStuffs.DemonicFurnace.TileDemonicFurnace;
 import NetherStuffs.Items.NetherItems;
 import NetherStuffs.Items.NetherOreIngot;
 import NetherStuffs.Items.NetherPotionBottle;
 import NetherStuffs.Items.NetherSoulGlassBottle;
 import NetherStuffs.Items.NetherStoneBowl;
 import NetherStuffs.Items.NetherStonePotionBowl;
 import NetherStuffs.Items.SoulEnergyBottle;
 import NetherStuffs.SoulBlocker.TileSoulBlocker;
 import NetherStuffs.SoulDetector.TileSoulDetector;
 import NetherStuffs.SoulFurnace.TileSoulFurnace;
 import NetherStuffs.SoulWorkBench.TileSoulWorkBench;
 import NetherStuffs.WorldGen.WorldGenNetherStuffsMinable;
 import NetherStuffs.WorldGen.WorldGenNetherStuffsTrees;
 import cpw.mods.fml.common.DummyModContainer;
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
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(name = "NetherStuffs", version = "0.6.1", modid = "NetherStuffs")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "NetherStuffs" }, packetHandler = ClientPacketHandler.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "NetherStuffs" }, packetHandler = ServerPacketHandler.class))
 public class NetherStuffs extends DummyModContainer {
 	@Instance
 	public static NetherStuffs instance = new NetherStuffs();
 
 	private GuiHandler guiHandler = new GuiHandler();
 
 	@SidedProxy(clientSide = "NetherStuffs.Client.ClientProxy", serverSide = "NetherStuffs.Common.CommonProxy")
 	public static CommonProxy proxy;
 
 	public static int NetherOreBlockId;
 	public static int NetherWoodBlockId;
 	public static int NetherPlankBlockId;
 	public static int NetherDemonicFurnaceBlockId;
 	public static int NetherLeavesBlockId;
 	public static int NetherSaplingBlockId;
 	public static int NetherSoulGlassBlockid;
 	public static int NetherSoulGlassPaneBlockid;
 	public static int NetherPuddleBlockId;
 
 	public static int SoulWorkBenchBlockId;
 	public static int NetherSoulBombBlockId;
 
 	public static int NetherObsidianSwordAcidItemId;
 	public static int NetherObsidianSwordDeathItemId;
 
 	public static int NetherOreIngotItemId;
 	public static int NetherWoodCharcoalItemId;
 	public static int NetherDemonicBarHandleItemId;
 	public static int NetherObsidianSwordItemId;
 	public static int NetherWoodStickItemId;
 	public static int NetherStoneBowlItemId;
 	public static int NetherStonePotionBowlItemId;
 	public static int NetherSoulGlassBottleItemId;
 	public static int NetherPotionBottleItemId;
 
 	public static int SoulEnergyBottleItemId;
 
 	public static int NetherSoulglassSwordItemId;
 	public static int NetherSoulglassSwordAcidItemId;
 	public static int NetherSoulglassSwordDeathItemId;
 	public static int NetherSoulglassSwordHellfireItemId;
 
 	public static int NetherSoulDetectorBlockId;
 	public static int NetherSoulBlockerBlockId;
 	public static int NetherSoulFurnaceBlockId;
 
 	private static boolean SpawnSkeletonsAwayFromNetherFortresses;
 
 	@PreInit
 	public void PreLoad(FMLPreInitializationEvent event) {
 		System.out.println("[NetherStuffs] PreLoad");
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		NetherOreBlockId = config.get(Configuration.CATEGORY_BLOCK, "NetherOre", 1230).getInt();
 		NetherWoodBlockId = config.get(Configuration.CATEGORY_BLOCK, "NetherWood", 1231).getInt();
 		NetherPlankBlockId = config.get(Configuration.CATEGORY_BLOCK, "NetherPlank", 1232).getInt();
 		NetherDemonicFurnaceBlockId = config.get(Configuration.CATEGORY_BLOCK, "DemonicFurnace", 1233).getInt();
 		NetherLeavesBlockId = config.get(Configuration.CATEGORY_BLOCK, "Leaves", 1234).getInt();
 		NetherSaplingBlockId = config.get(Configuration.CATEGORY_BLOCK, "Saplings", 1235).getInt();
 		NetherSoulGlassBlockid = config.get(Configuration.CATEGORY_BLOCK, "Glass", 1236).getInt();
 		NetherSoulGlassPaneBlockid = config.get(Configuration.CATEGORY_BLOCK, "GlassPane", 1237).getInt();
 		NetherPuddleBlockId = config.get(Configuration.CATEGORY_BLOCK, "Puddle", 1238).getInt();
 		SoulWorkBenchBlockId = config.get(Configuration.CATEGORY_BLOCK, "SoulWorkbench", 1239).getInt();
 		NetherSoulBombBlockId = config.get(Configuration.CATEGORY_BLOCK, "SoulBomb", 1240).getInt();
 		NetherSoulDetectorBlockId = config.get(Configuration.CATEGORY_BLOCK, "SoulDetector", 1241).getInt();
 		NetherSoulBlockerBlockId = config.get(Configuration.CATEGORY_BLOCK, "SoulBlocker", 1242).getInt();
 		NetherSoulFurnaceBlockId = config.get(Configuration.CATEGORY_BLOCK, "SoulFurnace", 1243).getInt();
		
		
 		NetherOreIngotItemId = config.get(Configuration.CATEGORY_ITEM, "NetherIngots", 5000).getInt();
 		NetherDemonicBarHandleItemId = config.get(Configuration.CATEGORY_ITEM, "DemonicSwordHandle", 5001).getInt();
 		NetherObsidianSwordItemId = config.get(Configuration.CATEGORY_ITEM, "ObsidianSword", 5002).getInt();
 		NetherWoodStickItemId = config.get(Configuration.CATEGORY_ITEM, "NetherWoodStick", 5003).getInt();
 		NetherStoneBowlItemId = config.get(Configuration.CATEGORY_ITEM, "NetherStoneBowl", 5004).getInt();
 		NetherSoulGlassBottleItemId = config.get(Configuration.CATEGORY_ITEM, "SoulGlassBottle", 5005).getInt();
 
 		NetherObsidianSwordAcidItemId = config.get(Configuration.CATEGORY_ITEM, "ObsidianSwordAcid", 5006).getInt();
 		NetherObsidianSwordDeathItemId = config.get(Configuration.CATEGORY_ITEM, "ObsidianSwordDeath", 5007).getInt();
 
 		NetherPotionBottleItemId = config.get(Configuration.CATEGORY_ITEM, "PotionBottle", 5008).getInt();
 		NetherStonePotionBowlItemId = config.get(Configuration.CATEGORY_ITEM, "PotionBowl", 5009).getInt();
 
 		NetherSoulglassSwordItemId = config.get(Configuration.CATEGORY_ITEM, "SoulglassSword", 5010).getInt();
 		NetherSoulglassSwordAcidItemId = config.get(Configuration.CATEGORY_ITEM, "SoulglassSwordAcid", 5011).getInt();
 		NetherSoulglassSwordDeathItemId = config.get(Configuration.CATEGORY_ITEM, "SoulglassSwordDeath", 5012).getInt();
 		NetherSoulglassSwordHellfireItemId = config.get(Configuration.CATEGORY_ITEM, "SoulglassSwordHellfire", 5013).getInt();
 
 		NetherWoodCharcoalItemId = config.get(Configuration.CATEGORY_ITEM, "NetherWoodCharcoal", 5014).getInt();
 		SoulEnergyBottleItemId = config.get(Configuration.CATEGORY_ITEM, "SoulEnergyPotion", 5015).getInt();
 
 		SpawnSkeletonsAwayFromNetherFortresses = config.get(Configuration.CATEGORY_GENERAL, "SpawnSkeletonsAwayFromNetherFortresses", true).getBoolean(true);
 
 		NetherStuffsEventHook.nDetectRadius = config.get(Configuration.CATEGORY_GENERAL, "SoulBlockerRadius", 8).getInt();
 
 		config.save();
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event) {
 
 		GameRegistry.registerBlock(NetherBlocks.NetherSoulGlass);
 		GameRegistry.registerBlock(NetherBlocks.NetherSoulGlassPane);
 		GameRegistry.registerBlock(NetherBlocks.NetherDemonicFurnace);
 		GameRegistry.registerBlock(NetherBlocks.NetherSoulFurnace);
 		GameRegistry.registerBlock(NetherBlocks.netherSoulWorkBench);
 
 		Item.itemsList[NetherOreBlockId] = new NetherOreItemBlock(NetherOreBlockId - 256, NetherBlocks.netherOre).setItemName("NetherOreItemBlock");
 		Item.itemsList[NetherWoodBlockId] = new NetherWoodItemBlock(NetherWoodBlockId - 256, NetherBlocks.netherWood).setItemName("NetherWoodItemBlock");
 		Item.itemsList[NetherPlankBlockId] = new NetherPlankItemBlock(NetherPlankBlockId - 256, NetherBlocks.netherPlank).setItemName("NetherPlankItemBlock");
 		Item.itemsList[NetherLeavesBlockId] = new NetherLeavesItemBlock(NetherLeavesBlockId - 256, NetherBlocks.netherLeaves).setItemName("NetherLeavesItemBlock");
 		Item.itemsList[NetherSaplingBlockId] = new NetherSaplingItemBlock(NetherSaplingBlockId - 256).setItemName("NetherSaplingItemBlock");
 		Item.itemsList[NetherPuddleBlockId] = new NetherPuddleItemBlock(NetherPuddleBlockId - 256).setItemName("NetherPuddleItemBlock");
 		Item.itemsList[NetherSoulBombBlockId] = new SoulBombItemBlock(NetherSoulBombBlockId - 256).setItemName("NetherSoulBombItemBlock");
 		Item.itemsList[NetherSoulDetectorBlockId] = new SoulDetectorItemBlock(NetherSoulDetectorBlockId - 256).setItemName("NetherSoulDetectorItemBlock");
 		Item.itemsList[NetherSoulBlockerBlockId] = new SoulBlockerItemBlock(NetherSoulBlockerBlockId - 256).setItemName("NetherSoulBlockerItemBlock");
 
 		// set required Stuff to Gather
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherOre, NetherBlocks.demonicOre, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherOre, NetherBlocks.netherStone, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.NetherDemonicFurnace, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.NetherSoulBlocker, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.NetherSoulDetector, "pickaxe", 1);
 
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherSoulWorkBench, "axe", 0);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherWood, "axe", 2);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherWood, NetherBlocks.netherWoodAcid, "axe", 2);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherWood, NetherBlocks.netherWoodHellfire, "axe", 2);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherWood, NetherBlocks.netherWoodDeath, "axe", 2);
 
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherPlank, "axe", 1);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherPlank, NetherBlocks.netherPlankAcid, "axe", 1);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherPlank, NetherBlocks.netherPlankHellfire, "axe", 1);
 		MinecraftForge.setBlockHarvestLevel(NetherBlocks.netherPlank, NetherBlocks.netherPlankDeath, "axe", 1);
 
 		GameRegistry.registerTileEntity(TileDemonicFurnace.class, "tileEntityNetherStuffsDemonicFurnace");
 		GameRegistry.registerTileEntity(TileSoulFurnace.class, "tileEntityNetherStuffsSoulFurnace");
 		GameRegistry.registerTileEntity(TileSoulWorkBench.class, "tileEntityNetherStuffsSoulWorkBench");
 		GameRegistry.registerTileEntity(TileSoulDetector.class, "tileEntityNetherStuffsSoulDetector");
 		GameRegistry.registerTileEntity(TileSoulBlocker.class, "tileEntityNetherStuffsSoulBlocker");
 
 		GameRegistry.registerFuelHandler(new NetherStuffsFuel());
 
 		registerWorldGenerators();
 		initRecipes();
 		initLanguageRegistry();
 		proxy.registerRenderThings();
 		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 
 		MinecraftForge.EVENT_BUS.register(new NetherStuffsEventHook());
 
 		/*
 		 * This lets Skeletons Spawn away from NetherFortresses. Actual Idea by ErasmoGnome, made on the MinecraftForums:
 		 * http://www.minecraftforum.net/topic/1493398-move-wither-skeletons-out-of-fortresses-over-125-supporters/
 		 */
 		if (SpawnSkeletonsAwayFromNetherFortresses)
 			((BiomeGenHell) BiomeGenBase.hell).spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 50, 4, 4));
 	}
 
 	private void registerWorldGenerators() {
 		GameRegistry.registerWorldGenerator(new WorldGenNetherStuffsMinable(NetherBlocks.netherOre.blockID, 0, 5));
 		GameRegistry.registerWorldGenerator(new WorldGenNetherStuffsTrees(true, 4, false));
 	}
 
 	private void initRecipes() {
 		initDefaultMinecraftRecipes();
 
 		addSmeltingMeta(Block.netherrack, 0, NetherBlocks.netherOre, NetherBlocks.netherStone); // this actually is the basic recipe to get started
 
 		DemonicFurnaceRecipes.smelting().addSmelting(NetherBlocks.netherOre.blockID, 0, new ItemStack(NetherItems.NetherOreIngot, 1, 0), 1.0F);
 		DemonicFurnaceRecipes.smelting().addSmelting(Block.slowSand.blockID, 0, new ItemStack(NetherBlocks.NetherSoulGlass, 1, 0), 0.5F);
 		DemonicFurnaceRecipes.smelting().addSmelting(NetherBlocks.netherWood.blockID, 0, new ItemStack(NetherItems.NetherWoodCharcoal, 1, 0), 0.25F);
 
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.netherSoulWorkBench, 1), new Object[] { "I#I", "#W#", "I#I", '#', new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone), 'W',
 				new ItemStack(Block.workbench), 'I', new ItemStack(NetherItems.NetherOreIngot, 1, 0) });
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.netherSoulWorkBench, 1), new Object[] { "#I#", "IWI", "#I#", '#', new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone), 'W',
 				new ItemStack(Block.workbench), 'I', new ItemStack(NetherItems.NetherOreIngot, 1, 0) });
 
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.NetherDemonicFurnace, 1), new Object[] { "NNN", "N N", "NNN", 'N', new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone) });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherWoodStick, 4), new Object[] { "#", "#", '#', NetherBlocks.netherPlank });
 
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.NetherSoulBlocker, 1, SoulBlocker.NetherSoulBlocker), new Object[] { "IPI", "SBS", "XXX", 'I',
 				new ItemStack(NetherItems.NetherOreIngot, 1, 0), 'P', NetherBlocks.netherPlank, 'S', NetherItems.NetherWoodStick, 'B', NetherItems.NetherPotionBottle, 'X',
 				new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone) });
 
		GameRegistry.addShapelessRecipe(new ItemStack(NetherItems.SoulEnergyBottle, 1), new Object[] { new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.hellfire),
 				new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.acid), new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.death),
 				NetherItems.NetherSoulGlassBottleItem, new ItemStack(NetherItems.NetherOreIngot, 1, 0) });
 
		GameRegistry.addShapelessRecipe(new ItemStack(NetherItems.SoulEnergyBottle, 1), new Object[] { new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.hellfire),
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.acid), new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.death),
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.hellfire), new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.acid),
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.death), NetherItems.NetherSoulGlassBottleItem, new ItemStack(NetherItems.NetherOreIngot, 1, 0) });
 
 		// Netherwood --> Netherlogs
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.netherPlank, 4, NetherBlocks.netherPlankHellfire), new Object[] { "#", '#',
 				new ItemStack(NetherBlocks.netherWood, 1, NetherBlocks.netherWoodHellfire) });
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.netherPlank, 4, NetherBlocks.netherPlankAcid), new Object[] { "#", '#',
 				new ItemStack(NetherBlocks.netherWood, 1, NetherBlocks.netherWoodAcid) });
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.netherPlank, 4, NetherBlocks.netherPlankDeath), new Object[] { "#", '#',
 				new ItemStack(NetherBlocks.netherWood, 1, NetherBlocks.netherWoodDeath) });
 
 		// Torches out of Netherwood Sticks
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 6), new Object[] { "X", "#", 'X', Item.coal, '#', NetherItems.NetherWoodStick });
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 6), new Object[] { "X", "#", 'X', new ItemStack(Item.coal, 1, 1), '#', NetherItems.NetherWoodStick });
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 10), new Object[] { "X", "#", 'X', new ItemStack(NetherItems.NetherWoodCharcoal, 1, 0), '#', NetherItems.NetherWoodStick });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherDemonicBarHandle, 1), new Object[] { "NIN", " S ", 'N', new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone), 'I',
 				new ItemStack(NetherItems.NetherOreIngot, 1, 0), 'S', NetherItems.NetherWoodStick });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherStoneBowl, 3), new Object[] { "N N", " N ", 'N', new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone) });
 
 		GameRegistry.addRecipe(new ItemStack(NetherBlocks.NetherSoulGlassPane, 16), new Object[] { "###", "###", '#', NetherBlocks.NetherSoulGlass });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulGlassBottleItem, 3), new Object[] { "# #", " # ", '#', NetherBlocks.NetherSoulGlass });
 
 		/**
 		 * Swords
 		 */
 		initSwordRecipes();
 
 	}
 
 	private void initSwordRecipes() {
 		/**
 		 * Obsidian Sword & Obsidian Acid/Death Sword
 		 */
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherObsidianSword, 1), new Object[] { " O ", " O ", " H ", 'O', Block.obsidian, 'H', NetherItems.NetherDemonicBarHandle });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherObsidianSwordAcid, 1), new Object[] { "#S#", '#', new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.acid), 'S',
 				NetherItems.NetherObsidianSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherObsidianSwordDeath, 1), new Object[] { "#S#", '#', new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.death), 'S',
 				NetherItems.NetherObsidianSword });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherObsidianSwordAcid, 1), new Object[] { " # ", "#S#", " # ", '#',
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.acid), 'S', NetherItems.NetherObsidianSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherObsidianSwordDeath, 1), new Object[] { " # ", "#S#", " # ", '#',
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.death), 'S', NetherItems.NetherObsidianSword });
 
 		/**
 		 * Soulglass Swords
 		 */
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSword, 1), new Object[] { " # ", " # ", " H ", '#', NetherBlocks.NetherSoulGlass, 'H', NetherItems.NetherDemonicBarHandle });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordHellfire, 1), new Object[] { "#S#", '#', new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.hellfire),
 				'S', NetherItems.NetherSoulglassSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordDeath, 1), new Object[] { "#S#", '#', new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.death), 'S',
 				NetherItems.NetherSoulglassSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordAcid, 1), new Object[] { "#S#", '#', new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.acid), 'S',
 				NetherItems.NetherSoulglassSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordDeath, 1), new Object[] { "#S#", '#', new ItemStack(NetherItems.NetherPotionBottle, 1, NetherPotionBottle.death), 'S',
 				NetherItems.NetherSoulglassSword });
 
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordHellfire, 1), new Object[] { " # ", "#S#", " # ", '#',
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.hellfire), 'S', NetherItems.NetherSoulglassSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordAcid, 1), new Object[] { " # ", "#S#", " # ", '#',
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.acid), 'S', NetherItems.NetherSoulglassSword });
 		GameRegistry.addRecipe(new ItemStack(NetherItems.NetherSoulglassSwordDeath, 1), new Object[] { " # ", "#S#", " # ", '#',
 				new ItemStack(NetherItems.NetherStonePotionBowl, 1, NetherStonePotionBowl.death), 'S', NetherItems.NetherSoulglassSword });
 	}
 
 	/**
 	 * Returns Default Minecraft Items from Mod Items
 	 */
 	private void initDefaultMinecraftRecipes() {
 		GameRegistry.addRecipe(new ItemStack(Item.bed, 1), new Object[] { "###", "XXX", '#', Block.cloth, 'X', NetherBlocks.netherPlank });
 		GameRegistry.addRecipe(new ItemStack(Block.chest), new Object[] { "###", "# #", "###", '#', NetherBlocks.netherPlank });
 		GameRegistry.addRecipe(new ItemStack(Block.workbench), new Object[] { "##", "##", '#', NetherBlocks.netherPlank });
 		GameRegistry.addRecipe(new ItemStack(Block.netherBrick, 2), new Object[] { "NN", "NN", 'N', new ItemStack(NetherBlocks.netherOre, 1, NetherBlocks.netherStone) }); // this actually is not
 																																											// existant in MC yet
 	}
 
 	private void addSmeltingMeta(Block block, int blockMetadata, Item result, int itemMetadata) {
 		addSmeltingMeta(block, blockMetadata, result, itemMetadata, 1);
 	}
 
 	private void addSmeltingMeta(Block block, int blockMetadata, Item result, int itemMetadata, int itemCount) {
 		FurnaceRecipes.smelting().addSmelting(block.blockID, blockMetadata, new ItemStack(result, itemCount, itemMetadata), 0);
 	}
 
 	private void addSmeltingMeta(Block block, int blockMetadata, Block result, int itemMetadata) {
 		addSmeltingMeta(block, blockMetadata, result, itemMetadata, 1);
 	}
 
 	private void addSmeltingMeta(Block block, int blockMetadata, Block result, int itemMetadata, int itemCount) {
 		FurnaceRecipes.smelting().addSmelting(block.blockID, blockMetadata, new ItemStack(result, itemCount, itemMetadata), 0);
 	}
 
 	private void initLanguageRegistry() {
 
 		for (int i = 0; i < NetherOreItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherOre." + NetherOreItemBlock.blockNames[i] + ".name", NetherOreItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < NetherWoodItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherWood." + NetherWoodItemBlock.blockNames[i] + ".name", NetherWoodItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < NetherPlankItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherPlank." + NetherPlankItemBlock.blockNames[i] + ".name", NetherPlankItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < NetherLeavesItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherLeaves." + NetherLeavesItemBlock.blockNames[i] + ".name", NetherLeavesItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < NetherSaplingItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherSapling." + NetherSaplingItemBlock.blockNames[i] + ".name", NetherSaplingItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < NetherPuddleItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherPuddle." + NetherPuddleItemBlock.blockNames[i] + ".name", NetherPuddleItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < SoulDetectorItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherSoulDetector." + SoulDetectorItemBlock.blockNames[i] + ".name", SoulDetectorItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < SoulBlockerItemBlock.getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("tile.NetherSoulBlocker." + SoulBlockerItemBlock.blockNames[i] + ".name", SoulBlockerItemBlock.blockDisplayNames[i]);
 		}
 
 		for (int i = 0; i < ((NetherOreIngot) NetherItems.NetherOreIngot).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.NetherOreIngot." + ((NetherOreIngot) NetherItems.NetherOreIngot).itemNames[i] + ".name",
 					((NetherOreIngot) NetherItems.NetherOreIngot).itemDisplayNames[i]);
 		}
 
 		for (int i = 0; i < ((NetherStoneBowl) NetherItems.NetherStoneBowl).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.NetherStoneBowl." + ((NetherStoneBowl) NetherItems.NetherStoneBowl).itemNames[i] + ".name",
 					((NetherStoneBowl) NetherItems.NetherStoneBowl).itemDisplayNames[i]);
 		}
 
 		for (int i = 0; i < ((NetherSoulGlassBottle) NetherItems.NetherSoulGlassBottleItem).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.NetherSoulGlassBottleItem." + ((NetherSoulGlassBottle) NetherItems.NetherSoulGlassBottleItem).itemNames[i] + ".name",
 					((NetherSoulGlassBottle) NetherItems.NetherSoulGlassBottleItem).itemDisplayNames[i]);
 		}
 
 		for (int i = 0; i < ((NetherPotionBottle) NetherItems.NetherPotionBottle).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.NetherPotionBottle." + ((NetherPotionBottle) NetherItems.NetherPotionBottle).itemNames[i] + ".name",
 					((NetherPotionBottle) NetherItems.NetherPotionBottle).itemDisplayNames[i]);
 		}
 
 		for (int i = 0; i < ((NetherStonePotionBowl) NetherItems.NetherStonePotionBowl).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.NetherStonePotionBowl." + ((NetherStonePotionBowl) NetherItems.NetherStonePotionBowl).itemNames[i] + ".name",
 					((NetherStonePotionBowl) NetherItems.NetherStonePotionBowl).itemDisplayNames[i]);
 		}
 
 		for (int i = 0; i < ((SoulEnergyBottle) NetherItems.SoulEnergyBottle).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.SoulEnergyBottle." + ((SoulEnergyBottle) NetherItems.SoulEnergyBottle).itemNames[i] + ".name",
 					((SoulEnergyBottle) NetherItems.SoulEnergyBottle).itemDisplayNames[i]);
 		}
 
 		LanguageRegistry.instance().addStringLocalization("item.NetherObsidianSword.name", "Obsidian Sword");
 		LanguageRegistry.instance().addStringLocalization("item.NetherSoulglassSword.name", "Soulglass Sword");
 
 		LanguageRegistry.instance().addStringLocalization("item.NetherDemonicBarHandle.name", "Demonic Haft");
 		LanguageRegistry.instance().addStringLocalization("item.NetherWoodStick.name", "Nether Stick");
 
 		LanguageRegistry.instance().addStringLocalization("tile.NetherDemonicFurnace.name", "Demonic Furnace");
 		LanguageRegistry.instance().addStringLocalization("tile.NetherSoulFurnace.name", "Soul Furnace");
 		LanguageRegistry.instance().addStringLocalization("tile.NetherSoulWorkBench.name", "Soul Workbench");
 		LanguageRegistry.instance().addStringLocalization("tile.NetherSoulGlass.name", "Soul Glass");
 		LanguageRegistry.instance().addStringLocalization("tile.NetherSoulGlassPane.name", "Soul Glass Pane");
 
 		LanguageRegistry.instance().addStringLocalization("tile.NetherSoulBomb.NetherSoulBomb.name", "Soul Bomb");
 
 		LanguageRegistry.instance().addStringLocalization("item.NetherWoodCharcoal.name", "Nether Charcoal");
 
 	}
 
 	@PostInit
 	public static void postInit(FMLPostInitializationEvent event) {
 		System.out.println("[NetherStuffs] postInit");
 	}
 }
