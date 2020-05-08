 package artsin.chemicalelementsmod;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 
 import artsin.chemicalelementsmod.proxy.ChemicalElementsCommonProxy;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "chemicalelementsmod_artsin", name = "Chemical Elements ArtSin Mod", version = "0.2 for 1.4.6")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class ChemicalElementsMod
 {
 	@Instance("chemicalelementsmod_artsin")
 	public static ChemicalElementsMod instance;
 	
 	@SidedProxy(clientSide = "artsin.chemicalelementsmod.proxy.ChemicalElementsClientProxy", serverSide = "artsin.chemicalelementsmod.proxy.ChemicalElementsCommonProxy")
 	public static ChemicalElementsCommonProxy proxy;
 	
 	public static Block HgOre;
 	public static Block HgLamp;
 	public static Block HgLampActive;
 	public static Block NetherIronBlock;
 	public static Block NetherFurnace;
 	public static Block NetherFurnaceActive;
 	public static Block CoalBlock;
 	public static Block SilverBlock;
 	public static Block SilverOre;
 	
 	public static Item Cinnabar;
 	public static Item HgBall;
 	public static Item HgThermometer;
 	public static Item Graphite;
 	public static Item BigCoal;
 	public static Item SmallDiamond;
 	public static Item SilverIngot;
 	public static Item SilverBottle;
 	public static Item SilverBottleWithWater;
 	
 	@Init
 	public void InitMod(FMLInitializationEvent event)
 	{
 		HgOre = new HgOre(2000, Material.rock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("HgOre");
 		HgLamp = new HgLamp(2001, Material.glass, false).setHardness(1.0F).setResistance(1.0F).setStepSound(Block.soundGlassFootstep).setBlockName("HgLamp");
 		HgLampActive = new HgLamp(2002, Material.glass, true).setHardness(1.0F).setResistance(1.0F).setStepSound(Block.soundGlassFootstep).setBlockName("HgLampActive");
 		NetherIronBlock = new ChemicalElementsBlock(2003, Material.rock, 10).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setBlockName("NetherIronBlock");
 		NetherFurnace = (new NetherFurnaceBlock(2004, false)).setHardness(3.5F).setStepSound(Block.soundStoneFootstep).setBlockName("NetherFurnace").setRequiresSelfNotify().setCreativeTab(CreativeTabs.tabDecorations);
 		NetherFurnaceActive = (new NetherFurnaceBlock(2005, true)).setHardness(3.5F).setStepSound(Block.soundStoneFootstep).setLightValue(0.9F).setBlockName("NetherFurnaceActive").setRequiresSelfNotify();
 		CoalBlock = new ChemicalElementsBlock(2006, Material.rock, 14).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("CoalBlock").setCreativeTab(CreativeTabs.tabMaterials);
 		SilverBlock = new ChemicalElementsBlock(2007, Material.rock, 17).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setBlockName("NetherIronBlock");
 		SilverOre = new ChemicalElementsBlock(2008, Material.rock, 15).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("SilverOre");
 		
 		Cinnabar = new ChemicalElementsModItem(10000).setIconIndex(1).setItemName("Cinnabar").setCreativeTab(CreativeTabs.tabMaterials);
 		HgBall = new ChemicalElementsModItem(10001).setIconIndex(2).setItemName("HgBall").setCreativeTab(CreativeTabs.tabMaterials);
 		HgThermometer = new HgThermometer(10002).setIconIndex(3).setItemName("HgThermometer").setCreativeTab(CreativeTabs.tabTools);
 		Graphite = new ChemicalElementsModItem(10003).setIconIndex(11).setItemName("Graphite").setCreativeTab(CreativeTabs.tabMaterials);
 		BigCoal = new ChemicalElementsModItem(10004).setIconIndex(12).setItemName("BigCoal").setCreativeTab(CreativeTabs.tabMaterials);
 		SmallDiamond = new ChemicalElementsModItem(10005).setIconIndex(13).setItemName("SmallDiamond").setCreativeTab(CreativeTabs.tabMaterials);
 		SilverIngot = new ChemicalElementsModItem(10006).setIconIndex(16).setItemName("SilverIngot").setCreativeTab(CreativeTabs.tabMaterials);
 		SilverBottle = new ItemSilverBottle(10007).setIconIndex(18).setItemName("SilverBottle").setCreativeTab(CreativeTabs.tabMaterials);
 		SilverBottleWithWater = new ItemSilverBottleWithWater(10008).setIconIndex(18).setItemName("SilverBottleWithWater").setCreativeTab(CreativeTabs.tabMaterials).setContainerItem(SilverBottle);
 		
 		GameRegistry.registerBlock(HgOre, "HgOre");
 		GameRegistry.registerBlock(HgLamp, "HgLamp");
 		GameRegistry.registerBlock(HgLampActive, "HgLampActive");
 		GameRegistry.registerBlock(NetherIronBlock, "NetherIronBlock");
 		GameRegistry.registerBlock(NetherFurnace, "NetherFurnace");
 		GameRegistry.registerBlock(NetherFurnaceActive, "NetherFurnaceActive");
 		GameRegistry.registerBlock(CoalBlock, "CoalBlock");
 		GameRegistry.registerBlock(SilverBlock, "SilverBlock");
 		GameRegistry.registerBlock(SilverOre, "SilverOre");
 		
 		LanguageRegistry.addName(HgOre, "Киноварь");
 		LanguageRegistry.addName(HgLamp, "Ртутная лампа");
 		LanguageRegistry.addName(HgLampActive, "Ртутная лампа (включённая)");
 		LanguageRegistry.addName(NetherIronBlock, "Адский железный блок");
 		LanguageRegistry.addName(NetherFurnace, "Адская печь");
 		LanguageRegistry.addName(CoalBlock, "Угольный блок");
 		LanguageRegistry.addName(SilverBlock, "Серебряный блок");
 		LanguageRegistry.addName(SilverOre, "Серебряная руда");
 		
 		LanguageRegistry.addName(Cinnabar, "Киноварь");
 		LanguageRegistry.addName(HgBall, "Шарик ртути");
 		LanguageRegistry.addName(HgThermometer, "Ртутный термометр");
 		LanguageRegistry.addName(Graphite, "Графит");
 		LanguageRegistry.addName(BigCoal, "Большой кусок угля");
 		LanguageRegistry.addName(SmallDiamond, "Маленький алмаз");
 		LanguageRegistry.addName(SilverIngot, "Серебряный слиток");
 		LanguageRegistry.addName(SilverBottle, "Серебряная колба");
 		LanguageRegistry.addName(SilverBottleWithWater, "Серебряная колба с водой");
 		
 		GameRegistry.registerWorldGenerator(new ChemicalElementsModWorldGenerator());
 		MinecraftForge.setBlockHarvestLevel(HgOre, "pickaxe", 2); // 2 - iron
 		MinecraftForge.setBlockHarvestLevel(NetherIronBlock, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(NetherFurnace, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(NetherFurnaceActive, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(CoalBlock, "pickaxe", 1); // 1 - stone
 		MinecraftForge.setBlockHarvestLevel(SilverOre, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(SilverBlock, "pickaxe", 2);
 		MinecraftForge.EVENT_BUS.register(new ChemicalElementsModItemPickupEvent());
 		OreDictionary.registerOre("oreSilver", SilverOre);
 		OreDictionary.registerOre("ingotSilver", SilverIngot);
 		
 		GameRegistry.registerTileEntity(TileEntityNetherFurnace.class, "TileEntityNetherFurnace");
 		NetworkRegistry.instance().registerGuiHandler(this, new ChemicalElementsGuiHandler());
 		
 		GameRegistry.addRecipe(new ItemStack(NetherIronBlock), new Object[]{ "#A#", "AZA", "#A#", Character.valueOf('A'), Block.netherrack, Character.valueOf('Z'), Block.blockSteel});
 		GameRegistry.addRecipe(new ItemStack(NetherFurnace), new Object[]{ "KKK", "APA", "KKK", Character.valueOf('A'), NetherIronBlock, Character.valueOf('P'), Block.stoneOvenIdle, Character.valueOf('K'), Block.stone});
 		GameRegistry.addRecipe(new ItemStack(HgLamp), new Object[]{ "RGR", "HHH", "RGR", Character.valueOf('G'), Item.lightStoneDust, Character.valueOf('R'), Item.redstone, Character.valueOf('H'), HgBall});
 		GameRegistry.addRecipe(new ItemStack(HgThermometer), new Object[]{ "SHS", "SHS", "SHS", Character.valueOf('S'), Block.thinGlass, Character.valueOf('H'), HgBall});
 		GameRegistry.addShapelessRecipe(new ItemStack(BigCoal), new Object[]{ new ItemStack(Item.coal), new ItemStack(Item.coal)});
 		GameRegistry.addRecipe(new ItemStack(SmallDiamond), new Object[]{ "GGG", "GGG", "GGG", Character.valueOf('G'), Graphite});
 		GameRegistry.addRecipe(new ItemStack(Item.diamond), new Object[]{ "AA", "AA", Character.valueOf('A'), SmallDiamond});
 		GameRegistry.addRecipe(new ItemStack(CoalBlock), new Object[]{ "UUU", "UUU", "UUU", Character.valueOf('U'), Item.coal});
		GameRegistry.addRecipe(new ItemStack(SilverBlock), new Object[]{ "SSS", "SSS", "SSS", Character.valueOf('S'), SilverIngot});
		GameRegistry.addRecipe(new ItemStack(SilverBottle, 2), new Object[]{ "S#S", "#S#", Character.valueOf('S'), SilverIngot});
 		
 		GameRegistry.registerFuelHandler(new ChemicalElementsModFuelHandler());
 		GameRegistry.addSmelting(Cinnabar.shiftedIndex, new ItemStack(HgBall, 3), 0.5F);
 		GameRegistry.addSmelting(SilverOre.blockID, new ItemStack(SilverIngot), 0.75F);
 		
 		proxy.registerRenderThings();
 	}
 }
