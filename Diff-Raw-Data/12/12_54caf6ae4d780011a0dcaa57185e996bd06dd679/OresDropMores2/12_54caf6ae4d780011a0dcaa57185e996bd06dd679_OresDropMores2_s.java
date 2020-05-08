 package bstramke.OresDropMores2;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import bstramke.OresDropMores2.Blocks.OresDropMoresBlocks;
 import bstramke.OresDropMores2.Common.CommonProxy;
 import bstramke.OresDropMores2.Items.OreItem;
 import bstramke.OresDropMores2.Items.OresDropMoresItems;
 import cpw.mods.fml.common.DummyModContainer;
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
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(name = "OresDropMores2", version = "0.2", modid = "OresDropMores2")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class OresDropMores2 extends DummyModContainer {
 	@Instance
 	public static OresDropMores2 instance = new OresDropMores2();
 
 	@SidedProxy(clientSide = "bstramke.OresDropMores2.Client.ClientProxy", serverSide = "bstramke.OresDropMores2.Common.CommonProxy")
 	public static CommonProxy proxy;
 
 	public static int OreItemId;
 	public static int IronOreHarvestXPMin;
 	public static int IronOreHarvestXPMax;
 	public static int IronOreDropMin;
 	public static int IronOreDropMax;
 	protected static float IronOreSmeltXP;
 	public static int GoldOreHarvestXPMin;
 	public static int GoldOreHarvestXPMax;
 	protected static float GoldOreSmeltXP;
 	public static int GoldOreDropMin;
 	public static int GoldOreDropMax;
 
 	public static int CoalOreHarvestXPMin;
 	public static int CoalOreHarvestXPMax;
 	public static int CoalOreDropMin;
 	public static int CoalOreDropMax;
 
 	public static int DiamondOreHarvestXPMin;
 	public static int DiamondOreHarvestXPMax;
 	public static int DiamondOreDropMin;
 	public static int DiamondOreDropMax;
 
 	public static int EmeraldOreHarvestXPMin;
 	public static int EmeraldOreHarvestXPMax;
 	public static int EmeraldOreDropMin;
 	public static int EmeraldOreDropMax;
 
 	public static int LapisOreHarvestXPMin;
 	public static int LapisOreHarvestXPMax;
 	public static int LapisOreDropMin;
 	public static int LapisOreDropMax;
 	
 	public static int RedstoneOreHarvestXPMin;
 	public static int RedstoneOreHarvestXPMax;
 	public static int RedstoneOreDropMin;
 	public static int RedstoneOreDropMax;
 
 	private static boolean ReduceToolRequirements;
 
 	@PreInit
 	public void PreLoad(FMLPreInitializationEvent event) {
 		FMLLog.info("[OresDropMores2] PreLoad");
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 
 		OreItemId = config.getItem(Configuration.CATEGORY_ITEM, "OreItemId", 5400).getInt();
 
 		IronOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "IronOre Harvest XP", 1).getInt(1);
 		IronOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "IronOre Harvest XP", 1).getInt(1);
 		IronOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "IronOre Harvest DropCount Minimum", 3).getInt(3);
 		IronOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "IronOre Harvest DropCount Maximum", 5).getInt(5);
 		IronOreSmeltXP = (float) config.get(Configuration.CATEGORY_GENERAL, "IronOre Smelt XP", 0.1F).getDouble(0.1F);
 
 		GoldOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "GoldOre Harvest XP Minimum", 1).getInt(1);
 		GoldOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "GoldOre Harvest XP Maximum", 1).getInt(1);
 		GoldOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "GoldOre Harvest DropCount Minimum", 3).getInt(3);
 		GoldOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "GoldOre Harvest DropCount Maximum", 5).getInt(5);
 		GoldOreSmeltXP = (float) config.get(Configuration.CATEGORY_GENERAL, "GoldOre Smelt XP", 0.2F).getDouble(0.2F);
 
 		CoalOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "Coal Harvest XP Minimum", 0).getInt(0);
 		CoalOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "Coal Harvest XP Maximum", 2).getInt(2);
 		CoalOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "Coal Harvest DropCount Minimum", 3).getInt(3);
 		CoalOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "Coal Harvest DropCount Maximum", 5).getInt(5);
 
 		DiamondOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "Diamond Harvest XP Minimum", 3).getInt(3);
 		DiamondOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "Diamond Harvest XP Maximum", 7).getInt(7);
 		DiamondOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "Diamond Harvest DropCount Minimum", 3).getInt(3);
 		DiamondOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "Diamond Harvest DropCount Maximum", 5).getInt(5);
 
 		EmeraldOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "Emerald Harvest XP Minimum", 3).getInt(3);
 		EmeraldOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "Emerald Harvest XP Maximum", 7).getInt(7);
 		EmeraldOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "Emerald Harvest DropCount Minimum", 3).getInt(3);
 		EmeraldOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "Emerald Harvest DropCount Maximum", 5).getInt(5);
 
 		LapisOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "Lapis Harvest XP Minimum", 2).getInt(2);
 		LapisOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "Lapis Harvest XP Maximum", 5).getInt(5);
 		LapisOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "Lapis Harvest DropCount Minimum", 4).getInt(4);
 		LapisOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "Lapis Harvest DropCount Maximum", 9).getInt(9);
 
 		RedstoneOreHarvestXPMin = config.get(Configuration.CATEGORY_GENERAL, "Redstone Harvest XP Minimum", 1).getInt(1);
 		RedstoneOreHarvestXPMax = config.get(Configuration.CATEGORY_GENERAL, "Redstone Harvest XP Maximum", 5).getInt(5);
 		RedstoneOreDropMin = config.get(Configuration.CATEGORY_GENERAL, "Redstone Harvest DropCount Minimum", 4).getInt(4);
 		RedstoneOreDropMax = config.get(Configuration.CATEGORY_GENERAL, "Redstone Harvest DropCount Maximum", 5).getInt(5);
 		
 		ReduceToolRequirements = config.get(Configuration.CATEGORY_GENERAL, "Reduce Tool Requirement for Gathering", true).getBoolean(true);
 
 		config.save();
 
 		Block.blocksList[14] = null;
 		Block.blocksList[15] = null;
 		Block.blocksList[16] = null;
 		Block.blocksList[21] = null;
 		Block.blocksList[56] = null;
 		Block.blocksList[73] = null;
 		Block.blocksList[74] = null;
 		Block.blocksList[129] = null;
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event) {
 
 		initRecipes();
 		initLanguageRegistry();
 		proxy.registerRenderThings();
 
 		Block.blocksList[14] = OresDropMoresBlocks.odmOreGoldBlock;
 		Block.blocksList[15] = OresDropMoresBlocks.odmOreIronBlock;
 		Block.blocksList[16] = OresDropMoresBlocks.odmOreCoalBlock;
 		Block.blocksList[21] = OresDropMoresBlocks.odmOreLapisBlock;
 		Block.blocksList[56] = OresDropMoresBlocks.odmOreDiamondBlock;
 		Block.blocksList[73] = OresDropMoresBlocks.odmOreRedstoneBlock;
 		Block.blocksList[74] = OresDropMoresBlocks.odmOreRedstoneBlockGlowing;
 		Block.blocksList[129] = OresDropMoresBlocks.odmOreEmeraldBlock;
 
 		if (ReduceToolRequirements) {
 			MinecraftForge.setBlockHarvestLevel(OresDropMoresBlocks.odmOreGoldBlock, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(OresDropMoresBlocks.odmOreIronBlock, "pickaxe", 0);
 			MinecraftForge.setBlockHarvestLevel(OresDropMoresBlocks.odmOreDiamondBlock, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(OresDropMoresBlocks.odmOreEmeraldBlock, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(OresDropMoresBlocks.odmOreRedstoneBlock, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(OresDropMoresBlocks.odmOreRedstoneBlockGlowing, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(Block.obsidian, "pickaxe", 2);
 		}
 	}
 
 	private void initRecipes() {
 		FurnaceRecipes.smelting().addSmelting(OresDropMoresItems.OreItem.itemID, OreItem.iron, new ItemStack(Item.ingotIron), IronOreSmeltXP);
 		FurnaceRecipes.smelting().addSmelting(OresDropMoresItems.OreItem.itemID, OreItem.gold, new ItemStack(Item.ingotGold), GoldOreSmeltXP);
 	}
 
 	private void initLanguageRegistry() {
 		for (int i = 0; i < ((OreItem) OresDropMoresItems.OreItem).getMetadataSize(); i++) {
 			LanguageRegistry.instance().addStringLocalization("item.OresDropMoresOreItem." + ((OreItem) OresDropMoresItems.OreItem).itemNames[i] + ".name",
 					((OreItem) OresDropMoresItems.OreItem).itemDisplayNames[i]);
 		}
 
 	}
 
 	@PostInit
 	public static void postInit(FMLPostInitializationEvent event) {
 		FMLLog.info("[OresDropMores2] postInit");
 	}
 }
