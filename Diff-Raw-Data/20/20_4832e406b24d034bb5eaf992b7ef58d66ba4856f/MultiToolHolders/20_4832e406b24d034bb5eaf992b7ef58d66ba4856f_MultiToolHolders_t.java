 package ak.MultiToolHolders;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
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
 
@Mod(modid="MultiToolHolders", name="MultiToolHolders", version="1.2")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, channels={"MTH|Tool"}, packetHandler=PacketHandler.class)
 public class MultiToolHolders
 {
 	public static int ItemIDShift;
 	public static  Item ItemMultiToolHolder3;
 	public static  Item ItemMultiToolHolder5;
 	public static  Item ItemMultiToolHolder7;
 	public static  Item ItemMultiToolHolder9;
 	public static boolean Debug;
 
 	public static String GuiToolHolder3 ="/mods/ak/MultiToolHolders/textures/gui/ToolHolder3.png";
 	public static String GuiToolHolder5 ="/mods/ak/MultiToolHolders/textures/gui/ToolHolder5.png";
 	public static String GuiToolHolder7 ="/mods/ak/MultiToolHolders/textures/gui/ToolHolder7.png";
 	public static String GuiToolHolder9 ="/mods/ak/MultiToolHolders/textures/gui/ToolHolder9.png";
 	public static String TextureDomain = "ak/MultiToolHolders:";
 
 	public static String itemTexture = "/ak/MultiToolHolders/textures/items.png";
 
 
 	@Instance("MultiToolHolders")
 	public static MultiToolHolders instance;
 	@SidedProxy(clientSide = "ak.MultiToolHolders.Client.ClientProxy", serverSide = "ak.MultiToolHolders.CommonProxy")
 	public static CommonProxy proxy;
 	public static int guiIdHolder3 = 0;
 	public static int guiIdHolder5 = 1;
 	public static int guiIdHolder9 = 2;
 	public static int guiIdHolder7 = 3;
 
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 
 		ItemIDShift = config.get(Configuration.CATEGORY_ITEM, "Item ID Shift Number", 7000).getInt();
 
 
 		Property DebugProp = config.get(Configuration.CATEGORY_GENERAL, "Debug mode", false);
 		DebugProp.comment="For Debugger";
 		Debug = DebugProp.getBoolean(false);
 
 		config.save();
 	}
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		ItemMultiToolHolder3 = (new ItemMultiToolHolder(ItemIDShift - 256, 3)).setUnlocalizedName(this.TextureDomain + "Holder3").setCreativeTab(CreativeTabs.tabTools);
 		ItemMultiToolHolder5 = (new ItemMultiToolHolder(ItemIDShift - 256 + 1, 5)).setUnlocalizedName(this.TextureDomain + "Holder5").setCreativeTab(CreativeTabs.tabTools);
 		ItemMultiToolHolder9 = (new ItemMultiToolHolder(ItemIDShift - 256 + 2, 9)).setUnlocalizedName(this.TextureDomain + "Holder9").setCreativeTab(CreativeTabs.tabTools);
 		ItemMultiToolHolder7 = (new ItemMultiToolHolder(ItemIDShift - 256 + 3, 7)).setUnlocalizedName(this.TextureDomain + "Holder7").setCreativeTab(CreativeTabs.tabTools);
 
 
 
 
 		NetworkRegistry.instance().registerGuiHandler(this, proxy);
 		proxy.registerClientInformation();
 		proxy.registerTileEntitySpecialRenderer();
 
 		GameRegistry.addRecipe(new ItemStack(ItemMultiToolHolder3), new Object[]{"AAA","ABA", "CCC", Character.valueOf('A'), Item.ingotIron,Character.valueOf('B'),Block.chest, Character.valueOf('C'),Block.tripWireSource});
 		GameRegistry.addRecipe(new ItemStack(ItemMultiToolHolder7), new Object[]{"AAA","ABA", "CCC", Character.valueOf('A'), Item.ingotGold,Character.valueOf('B'),Block.chest, Character.valueOf('C'),Block.tripWireSource});
 		GameRegistry.addRecipe(new ItemStack(ItemMultiToolHolder9), new Object[]{"AAA","ABA", "CCC", Character.valueOf('A'), Item.diamond,Character.valueOf('B'),Block.chest, Character.valueOf('C'),Block.tripWireSource});
 		GameRegistry.addRecipe(new ItemStack(ItemMultiToolHolder5), new Object[]{"AAA","ABA", "CCC", Character.valueOf('A'), new ItemStack(Item.dyePowder,1,4),Character.valueOf('B'),Block.chest, Character.valueOf('C'),Block.tripWireSource});
 		if(this.Debug)
 			DebugSystem();
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		AddLocalization();
 	}
 	public void AddLocalization()
 	{
 		LanguageRegistry.addName(ItemMultiToolHolder3, "3-Way Tool Holder");
 		LanguageRegistry.addName(ItemMultiToolHolder5, "5-Way Tool Holder");
 		LanguageRegistry.addName(ItemMultiToolHolder9, "9-Way Tool Holder");
 		LanguageRegistry.addName(ItemMultiToolHolder7, "7-Way Tool Holder");
 
 		LanguageRegistry.instance().addNameForObject(ItemMultiToolHolder3, "ja_JP","3-Wayツールホルダー");
 		LanguageRegistry.instance().addNameForObject(ItemMultiToolHolder5, "ja_JP","5-Wayツールホルダー");
 		LanguageRegistry.instance().addNameForObject(ItemMultiToolHolder9, "ja_JP","9-Wayツールホルダー");
 		LanguageRegistry.instance().addNameForObject(ItemMultiToolHolder7, "ja_JP","7-Wayツールホルダー");
 
 		LanguageRegistry.instance().addStringLocalization("container.toolholder", "ToolHolder");
 		LanguageRegistry.instance().addStringLocalization("container.toolholder", "ja_JP", "ツールホルダー");
 		LanguageRegistry.instance().addStringLocalization("container.toolholder", "ToolHolder");
 		LanguageRegistry.instance().addStringLocalization("container.toolholder", "ja_JP", "ツールホルダー");
 		LanguageRegistry.instance().addStringLocalization("Key.openToolHolder", "Open ToolHolder");
 		LanguageRegistry.instance().addStringLocalization("Key.openToolHolder", "ja_JP", "ツールホルダーを開く");
 		LanguageRegistry.instance().addStringLocalization("Key.nextToolHolder", "ToolHolder Next Slot");
 		LanguageRegistry.instance().addStringLocalization("Key.nextToolHolder", "ja_JP", "次のスロット");
 		LanguageRegistry.instance().addStringLocalization("Key.prevToolHolder", "ToolHolder Previous Slot");
 		LanguageRegistry.instance().addStringLocalization("Key.prevToolHolder", "ja_JP", "前のスロット");
 	}
 	public void DungeonLootItemResist()
 	{
 		WeightedRandomChestContent Chest;
 
 		ItemStack[] items = new ItemStack[]{new ItemStack(ItemMultiToolHolder3),new ItemStack(ItemMultiToolHolder5),new ItemStack(ItemMultiToolHolder7),new ItemStack(ItemMultiToolHolder9)};
 		int[] weights = new int[]{10,5,3,1};
 		for (int i= 0;i<items.length;i++)
 		{
 			Chest = new WeightedRandomChestContent(items[i], 0, 1, weights[i]);;
 			ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_LIBRARY, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CROSSING, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, Chest);
 			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, Chest);
 		}
 	}
 	public void DebugSystem()
 	{
 
 	}
 
 }
