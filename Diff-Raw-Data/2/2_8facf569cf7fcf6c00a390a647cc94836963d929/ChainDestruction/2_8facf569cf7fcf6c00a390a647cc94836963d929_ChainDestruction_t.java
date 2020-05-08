 package ak.ChainDestruction;
 
 import java.util.HashSet;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.world.WorldEvent.Save;
 import ak.akapi.ConfigSavable;
 import cpw.mods.fml.common.Loader;
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
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid="ChainDestruction", name="ChainDestruction", version="1.0b")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, channels = {"CD|RegKey"}, packetHandler=PacketHandler.class)
 public class ChainDestruction
 {
 	@Instance("ChainDestruction")
 	public static ChainDestruction instance;
 	@SidedProxy(clientSide = "ak.ChainDestruction.ClientProxy", serverSide = "ak.ChainDestruction.CommonProxy")
 	public static CommonProxy proxy;
 	public static HashSet<Integer> enableItems = new HashSet();
 	public static HashSet<Integer> enableBlocks = new HashSet();
 	public static int[] itemsConfig;
 	public static int[] blocksConfig;
 	public static boolean digUnder;
 	public int[] vanillaTools = new int[]{
 			Item.axeDiamond.itemID,Item.axeGold.itemID,Item.axeIron.itemID,Item.axeStone.itemID,Item.axeWood.itemID,
 			Item.shovelDiamond.itemID,Item.shovelGold.itemID,Item.shovelIron.itemID,Item.shovelStone.itemID,Item.shovelWood.itemID,
 			Item.pickaxeDiamond.itemID,Item.pickaxeGold.itemID,Item.pickaxeIron.itemID,Item.pickaxeStone.itemID,Item.pickaxeWood.itemID};
 	public int[] vanillaBlocks = new int[]{Block.obsidian.blockID,Block.oreCoal.blockID,Block.oreDiamond.blockID,Block.oreEmerald.blockID,
 			Block.oreGold.blockID,Block.oreIron.blockID,Block.oreLapis.blockID, Block.oreNetherQuartz.blockID,Block.oreRedstone.blockID,Block.oreRedstoneGlowing.blockID};
 	public static int maxDestroyedBlock;
 	public static boolean dropOnPlayer = true;
 	public ConfigSavable config;
 	public InteractBlockHook interactblockhook;
 	public static boolean loadMTH = false;
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		config = new ConfigSavable(event.getSuggestedConfigurationFile());
 		config.load();
 		maxDestroyedBlock = config.get(Configuration.CATEGORY_GENERAL, "Maximum Destroued Block Counts", 100).getInt();
 		itemsConfig = config.get(Configuration.CATEGORY_GENERAL, "toolItemsId", vanillaTools).getIntList();
 		blocksConfig = config.get(Configuration.CATEGORY_GENERAL, "chainDestroyedBlockIdConfig", vanillaBlocks).getIntList();
 		digUnder = config.get(Configuration.CATEGORY_GENERAL, "digUnder", true).getBoolean(true);
 		config.save();
 	}
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		proxy.registerClientInfo();
 		interactblockhook = new InteractBlockHook();
 		MinecraftForge.EVENT_BUS.register(interactblockhook);
 		MinecraftForge.EVENT_BUS.register(new SaveConfig());
 
 		LanguageRegistry.instance().addStringLocalization("Key.CDRegistItem", "RegChainDestructItem");
 		LanguageRegistry.instance().addStringLocalization("Key.CDRegistItem", "ja_JP","一括破壊アイテム登録キー");
 		LanguageRegistry.instance().addStringLocalization("Key.CDDIgUnder", "Dig Under Key");
 		LanguageRegistry.instance().addStringLocalization("Key.CDDIgUnder", "ja_JP","下方採掘キー");
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent evet)
 	{
 		addItemsAndBlocks();
 		this.loadMTH = Loader.isModLoaded("MultiToolHolders");
 	}
 	public void addItemsAndBlocks()
 	{
 		for(int i = 0;i< itemsConfig.length;i++)
 		{
 			enableItems.add(itemsConfig[i]);
 		}
 		for(int i = 0;i< blocksConfig.length;i++)
 		{
 			enableBlocks.add(blocksConfig[i]);
 		}
 //		enableItems.addAll(new HashSet(Arrays.asList(itemsConfig)));
 //		enableBlocks.addAll(new HashSet(Arrays.asList(blocksConfig)));
 	}
 	public class SaveConfig
 	{
 		@ForgeSubscribe
 		public void WorldSave(Save event)
 		{
 			config.set(Configuration.CATEGORY_GENERAL, "toolItemsId", enableItems);
 			config.set(Configuration.CATEGORY_GENERAL, "chainDestroyedBlockIdConfig", enableBlocks);
 			config.set(Configuration.CATEGORY_GENERAL, "digUnder", digUnder);
 			config.save();
 		}
 	}
 
 }
