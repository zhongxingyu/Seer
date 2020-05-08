 package mal.carbonization;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.Random;
 import java.util.logging.Level;
 
 import mal.carbonization.blocks.BlockAutocraftingBench;
 import mal.carbonization.blocks.BlockFuel;
 import mal.carbonization.blocks.BlockFurnaceControl;
 import mal.carbonization.blocks.BlockFurnaces;
 import mal.carbonization.blocks.BlockMultiblockFurnaceControl;
 import mal.carbonization.blocks.BlockStructureBlock;
 import mal.carbonization.items.ItemBlockAutocraftingBench;
 import mal.carbonization.items.ItemBlockFuels;
 import mal.carbonization.items.ItemBlockFurnaces;
 import mal.carbonization.items.ItemBlockMultiblockFurnaceControl;
 import mal.carbonization.items.ItemDust;
 import mal.carbonization.items.ItemFuel;
 import mal.carbonization.items.ItemFuelCell;
 import mal.carbonization.items.ItemHHCompressor;
 import mal.carbonization.items.ItemHHPulverizer;
 import mal.carbonization.items.ItemHHPurifyer;
 import mal.carbonization.items.ItemIngots;
 import mal.carbonization.items.ItemMisc;
 import mal.carbonization.items.ItemRecipeCharm;
 import mal.carbonization.items.ItemStructureBlock;
 import mal.carbonization.network.PacketHandler;
 import mal.carbonization.tileentity.*;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.FMLLog;
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 //import thermalexpansion.api.crafting.CraftingHelpers;
 //import thermalexpansion.api.crafting.CraftingManagers;
 import ic2.api.item.*;
 import ic2.api.recipe.RecipeInputItemStack;
 
@Mod(modid="carbonization", name="Carbonization", version="0.9.0.0", dependencies = "required-after:Forge@[9.11,);required-after:FML@[6.4.30,)")
 @NetworkMod(clientSideRequired=true, channels={"CarbonizationChn"}, packetHandler = PacketHandler.class)
 public class carbonization {
 
 	public static int ORESLAGRATIO = 300;//number of millibuckets needed for an item
 	public static int MAXAUTOCRAFTTIME = 600;//maximum amount of time to cooldown
 	public static int MINAUTOCRAFTTIME = 5;//minimum  amount of time to cooldown
 	public static int BASEAUTOCRAFTFUEL = 800;//maximum amount of fuel per craft
 	public static ItemFuel fuel;
 	public static ItemDust dust;
 	public static ItemMisc misc;
 	public static ItemIngots ingots;
 	public static ItemHHPulverizer hhpulv;
 	public static ItemHHPurifyer hhpure;
 	public static ItemHHCompressor hhcomp;
 	public static Block fuelBlock;
 	public static BlockFurnaces furnaceBlock;
 	public static BlockFurnaces furnaceBlockActive;
 	public static BlockFurnaceControl FurnaceControl;
 	public static BlockMultiblockFurnaceControl multiblockFurnaceControl;
 	public static BlockStructureBlock structure;
 	public static ItemStructureBlock itemStructureBlock;
 	public static BlockAutocraftingBench autocraftingBench;
 	public static ItemRecipeCharm recipeCharm;
 	public static ItemFuelCell fuelCell;
 	//public static TestBlock testBlock;
 	
 	int fuelID=9540;
 	int dustID = 9541;
 	int itemID = 9542;
 	int itemID2 = 9543;
 	int itemID3 = 9544;
 	int miscID = 9545;
 	int ingotID = 9546;
 	int blockID=1560;
 	int furnaceID = 1561;
 	int furnaceID2 = 1562;
 	int multiblockfurnaceID = 1565;
 	int multiblockfurnacecontrolID = 1568;
 	int structureMachineID = 1569;
 	int structureItemID = 9547;
 	int autocraftingBenchID = 1563;
 	int recipeCharmID = 9548;
 	int fuelCellID = 9549;
 	
 	//Difficulty modifier, the higher the number, the more time the metals take to bake
 	private int difficultyMod = 10;
 	
 	private static ItemStack HHPulv;
 	private static ItemStack HHComp;
 	private static ItemStack HHPure;
 	private static ItemStack RecipeCharm;
 
 	public static CreativeTabs tabStructure = new CreativeTabs("tabStructure"){
 		public ItemStack getIconItemStack() { return new ItemStack(itemStructureBlock,1,1007);}
 	};
 	public static CreativeTabs tabMachine = new CreativeTabs("tabMachine"){
 		public ItemStack getIconItemStack() { return new ItemStack(autocraftingBench,1,0);}
 	};
 	public static CreativeTabs tabItems = new CreativeTabs("tabItems"){
 		public ItemStack getIconItemStack() { return new ItemStack(fuel,1,5);}
 	};
 	
 	@Instance
 	public static carbonization instance;
 	
 	@SidedProxy(clientSide = "mal.carbonization.ClientProxy", serverSide = "mal.carbonization.CommonProxy")
 	public static CommonProxy prox;
 	
 	@Mod.EventHandler
     public void preInit(FMLPreInitializationEvent event) {
             Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 
             config.load();
             
     		fuelID = config.getItem("Fuel ID", 9540).getInt();
     		dustID = config.getItem("Dust ID", 9541).getInt();
     		itemID = config.getItem("Handheld Pulverizer ID", 9542).getInt();
     		itemID2 = config.getItem("Handheld Compressor ID", 9543).getInt();
     		itemID3 = config.getItem("Handheld Purifyer", 9544).getInt();
     		miscID = config.getItem("Misc ID", 9545).getInt();
     		ingotID = config.getItem("Ingot ID", 9546).getInt();
     		structureItemID = config.getItem("Structure Block ID", 9547).getInt();
     		recipeCharmID = config.getItem("Recipe Charm ID", 9548).getInt();
     		fuelCellID = config.getItem("Fuel Cell ID", 9549).getInt();
     		blockID = config.getBlock("Block ID", 1560).getInt();
     		furnaceID = config.getBlock("Furnace ID", 1561).getInt();
     		furnaceID2 = config.getBlock("Active Furnace ID", 1562).getInt();
     		structureMachineID = config.getBlock("Structure Machine ID", 1563).getInt();
     		autocraftingBenchID = config.getBlock("Autocrafting Bench ID", 1564).getInt();
     		multiblockfurnaceID = config.getBlock("Multiblock Furnace ID", 1565).getInt();
     		multiblockfurnacecontrolID = config.getBlock("Multiblock Furnace Control ID", 1566).getInt();
 
     		
     		difficultyMod = config.get("Modifiers", "Metal Cook Time", 10).getInt();
     		if (difficultyMod<=0)
     		{
     			FMLLog.log(Level.WARNING, "Metal Cook time modifier is a invalid number, using the default value instead.");
     			difficultyMod = 10;
     		}
     		MAXAUTOCRAFTTIME = config.get("Modifiers", "Max Autocrafting Cooldown Time", 300).getInt();
     		if (MAXAUTOCRAFTTIME<=0)
     		{
     			FMLLog.log(Level.WARNING, "Max Autocrafting cooldown time modifier is a invalid number, using the default value instead.");
     			MAXAUTOCRAFTTIME = 300;
     		}
     		MINAUTOCRAFTTIME = config.get("Modifiers", "Min Autocrafting Cooldown Time", 5).getInt();
     		if(MINAUTOCRAFTTIME<=0)
     		{
     			FMLLog.log(Level.WARNING, "Min Autocrafting cooldown time modifier is a invalid number, using the default value instead.");
     			MINAUTOCRAFTTIME = 5;
     		}
     		if(MINAUTOCRAFTTIME > MAXAUTOCRAFTTIME)
     		{
     			FMLLog.log(Level.WARNING, "Min Autocrafting cooldown less then Max, setting minimum value to maximum");
     			MINAUTOCRAFTTIME = MAXAUTOCRAFTTIME;
     		}
     		BASEAUTOCRAFTFUEL = config.get("Modifiers", "Max Autocrafting Fuel Usage", 800).getInt();
     		if (BASEAUTOCRAFTFUEL<=0)
     		{
     			FMLLog.log(Level.WARNING, "Autocrafting fuel usage modifier is a invalid number, using the default value instead.");
     			difficultyMod = 800;
     		}
     		
     		config.save();
     		
     		//blarg
     		fuel = new ItemFuel(fuelID);
     		dust = new ItemDust(dustID);
     		misc = new ItemMisc(miscID);
     		ingots = new ItemIngots(ingotID);
     		hhpulv = new ItemHHPulverizer(itemID);
     		HHPulv  = new ItemStack(hhpulv, 1, OreDictionary.WILDCARD_VALUE);
     		hhpure = new ItemHHPurifyer(itemID3);
     		HHPure = new ItemStack(hhpure, 1, OreDictionary.WILDCARD_VALUE);
     		hhcomp = new ItemHHCompressor(itemID2);
     		HHComp = new ItemStack(hhcomp,1,OreDictionary.WILDCARD_VALUE);
     		recipeCharm = new ItemRecipeCharm(recipeCharmID);
     		RecipeCharm = new ItemStack(recipeCharm,1);
     		itemStructureBlock = new ItemStructureBlock(structureItemID);
     		fuelCell = new ItemFuelCell(fuelCellID);
     		fuelBlock = new BlockFuel(blockID,0,Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(3F).setResistance(1.0F);
     		furnaceBlock = new BlockFurnaces(furnaceID,false);
     		furnaceBlockActive = new BlockFurnaces(furnaceID2, true);
     		FurnaceControl = new BlockFurnaceControl(multiblockfurnaceID, Material.iron);
     		multiblockFurnaceControl = new BlockMultiblockFurnaceControl(multiblockfurnacecontrolID, Material.iron);
     		structure = new BlockStructureBlock(structureMachineID, Material.iron);
     		autocraftingBench = new BlockAutocraftingBench(autocraftingBenchID,Material.iron);
     		Item.itemsList[furnaceID] = new ItemBlockFurnaces(furnaceID-256,furnaceBlock);
     		
     		hhpulv.setContainerItem(hhpulv);
     		hhcomp.setContainerItem(hhcomp);
     		hhpure.setContainerItem(hhpure);
     		recipeCharm.setContainerItem(recipeCharm);
     		
     		//Basic stuff
     		GameRegistry.registerFuelHandler(new FuelHandler());
     		GameRegistry.registerWorldGenerator(new WorldgeneratorCarbonization());
     		NetworkRegistry.instance().registerGuiHandler(instance, prox);
 
     		
     		
     		GameRegistry.registerTileEntity(TileEntityFurnaces.class, "TileEntityFurnaces");
     		GameRegistry.registerTileEntity(TileEntityMultiblockInit.class, "TileEntityMultiblockInit");
     	
     		GameRegistry.registerTileEntity(TileEntityMultiblockFurnace.class, "TileEntityMultiblockFurnace");
     		GameRegistry.registerTileEntity(TileEntityStructureBlock.class, "TileEntityStructureBlock");
     		GameRegistry.registerTileEntity(TileEntityAutocraftingBench.class, "TileEntityAutocraftingBench");
     		GameRegistry.registerTileEntity(TileEntityFuelConverter.class,"TileEntityFuelConverter");
     		GameRegistry.registerTileEntity(TileEntityFuelCellFiller.class,"TileEntityFuelCellFiller");
     		
     		
     		GameRegistry.registerBlock(fuelBlock, ItemBlockFuels.class, "fuelBlock");
     		GameRegistry.registerBlock(FurnaceControl, ItemBlockMultiblockFurnaceControl.class, "furnacecontrol");
     		GameRegistry.registerBlock(autocraftingBench, ItemBlockAutocraftingBench.class, "autocraftingbench");
     		GameRegistry.registerItem(itemStructureBlock, "itemStructureBlock");
     		GameRegistry.registerItem(fuelCell, "fuelCell");
     		
     		//Names
     		//Fuels
     		LanguageRegistry.addName(new ItemStack(fuel,1,0), "Peat");
     		LanguageRegistry.addName(new ItemStack(fuel,1,1), "Lignite");
     		LanguageRegistry.addName(new ItemStack(fuel,1,2), "Sub-Bituminous Coal");
     		LanguageRegistry.addName(new ItemStack(fuel,1,3), "Bituminous Coal");
     		LanguageRegistry.addName(new ItemStack(fuel,1,4), "Anthracite");
     		LanguageRegistry.addName(new ItemStack(fuel,1,5), "Graphite");
     		
     		//Dusts
     		LanguageRegistry.addName(new ItemStack(dust,1,0), "Charcoal Dust");
     		LanguageRegistry.addName(new ItemStack(dust,1,1), "Peat Clumps");
     		LanguageRegistry.addName(new ItemStack(dust,1,2), "Lignite Clumps");
     		LanguageRegistry.addName(new ItemStack(dust,1,3), "Sub-Bituminous Coal Dust");
     		LanguageRegistry.addName(new ItemStack(dust,1,4), "Bituminous Coal Dust");
     		LanguageRegistry.addName(new ItemStack(dust,1,5), "Coal Dust");
     		LanguageRegistry.addName(new ItemStack(dust,1,6), "Anthracite Dust");
     		LanguageRegistry.addName(new ItemStack(dust,1,7), "Graphite Dust");
     		LanguageRegistry.addName(new ItemStack(dust,1,8), "Activated Charcoal");
     				
     		//Deposits
     		LanguageRegistry.addName(new ItemStack(fuelBlock,1,0), "Peat Deposit");
     		LanguageRegistry.addName(new ItemStack(fuelBlock,1,1), "Lignite Deposit");
     		LanguageRegistry.addName(new ItemStack(fuelBlock,1,2), "Sub-Bituminous Coal Deposit");
     		LanguageRegistry.addName(new ItemStack(fuelBlock,1,3), "Bituminous Coal Deposit");
     		LanguageRegistry.addName(new ItemStack(fuelBlock,1,4), "Anthracite Deposit");
     		LanguageRegistry.addName(new ItemStack(fuelBlock,1,5), "Graphite Cluster");
     		
     		//Ingots
     		LanguageRegistry.addName(new ItemStack(ingots,1,0), "Refined Iron");
     		LanguageRegistry.addName(new ItemStack(ingots,1,1), "Pig Iron");
     		LanguageRegistry.addName(new ItemStack(ingots,1,2), "Mild Steel");
     		LanguageRegistry.addName(new ItemStack(ingots,1,3), "Steel");
     		
     		//Tools
     		LanguageRegistry.addName(HHPulv, "Handheld Pulverizer");
     		LanguageRegistry.addName(HHComp, "Handheld Compressor");
     		LanguageRegistry.addName(HHPure, "Handheld Purifyer");
     		
     		LanguageRegistry.addName(new ItemStack(misc, 1, 0), "Pencil");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 1), "Cleansing Potion");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 2), "\"Cleansing\" Potion");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 3), "Carbon Chunk");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 4), "Glass Fibre Insulation");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 5), "High Density Insulation");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 6), "Ash");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 7), "Iron Gears");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 8), "Refined Iron Gears");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 9), "Pig Iron Gears");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 10), "Mild Steel Gears");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 11), "Steel Gears");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 12), "Small Pile of Graphite Dust");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 13), "Carbon Flake");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 14), "Carbon Thread");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 15), "Carbon Fibre");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 16), "Carbon Nanoflake");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 17), "Carbon Nanotube");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 18), "Coarse Threading");
     		LanguageRegistry.addName(new ItemStack(misc, 1, 19), "Fine Threading");
     		LanguageRegistry.addName(RecipeCharm, "Recipe Charm");
     		LanguageRegistry.addName(fuelCell, "Fuel Cell");
     		
     		//Machines
     		LanguageRegistry.addName(new ItemStack(furnaceBlock,1,0), "Iron Furnace");
     		LanguageRegistry.addName(new ItemStack(furnaceBlock,1,1), "Insulated Iron Furnace");
     		LanguageRegistry.addName(new ItemStack(furnaceBlock,1,2), "Insulated Steel Furnace");
     		LanguageRegistry.addName(new ItemStack(autocraftingBench,1,0), "Autocrafting Bench");
     		LanguageRegistry.addName(new ItemStack(autocraftingBench,1,1), "Fuel Conversion Bench");
     		LanguageRegistry.addName(new ItemStack(autocraftingBench,1,2), "Fuel Cell Filler");
     		//Multiblock controls
     		LanguageRegistry.addName(new ItemStack(FurnaceControl), "Furnace Control System");
     		
     		LanguageRegistry.instance().addStringLocalization("itemGroup.tabStructure", "en_US", "Carbonization Structure Blocks");
     		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMachine", "en_US", "Carbonization Blocks");
     		LanguageRegistry.instance().addStringLocalization("itemGroup.tabItems", "en_US", "Carbonization Items");
     		
     		//Structure
     		addStructureLanguage();
     		
     		//Localizations
     		LanguageRegistry.instance().addStringLocalization("tile.fuelBlock.peat.name", "Peat Deposit");
     		LanguageRegistry.instance().addStringLocalization("tile.fuelBlock.lignite.name", "Lignite Deposit");
     		LanguageRegistry.instance().addStringLocalization("tile.fuelBlock.sBituminous.name", "Sub-Bituminous Coal Deposit");
     		LanguageRegistry.instance().addStringLocalization("tile.fuelBlock.bituminous.name", "Bituminous Coal Deposit");
     		LanguageRegistry.instance().addStringLocalization("tile.fuelBlock.anthracite.name", "Anthracite Deposit");
     		LanguageRegistry.instance().addStringLocalization("tile.fuelBlock.graphite.name", "Graphite Cluster");
     		
     		LanguageRegistry.instance().addStringLocalization("tile.furnaceBlock.ironfurnace.name", "Iron Furnace");
     		LanguageRegistry.instance().addStringLocalization("tile.furnaceBlock.refinedironfurnace.name", "Insulated Iron Furnace");
     		LanguageRegistry.instance().addStringLocalization("tile.furnaceBlock.steelfurnace.name", "Insulated Steel Furnace");
     		
     		LanguageRegistry.instance().addStringLocalization("tile.autocraftingBench.autocraftingbench.name", "Autocrafting Bench");
     		LanguageRegistry.instance().addStringLocalization("tile.autocraftingBench.fuelconverter.name", "Fuel Conversion Bench");
     		
     		prox.registerRenderThings();
 	}
 	
 	@Mod.EventHandler
 	public void load(FMLInitializationEvent event)
 	{	
 		RenderingRegistry.registerBlockHandler(new StructureBlockRenderer());
 		prox.setCustomRenderers();
 		
 		//Ore dictionary
 		OreDictionary.registerOre("fuelPeat", new ItemStack(fuel, 1, 0));
 		OreDictionary.registerOre("fuelLignite", new ItemStack(fuel, 1, 1));
 		OreDictionary.registerOre("fuelSubBituminous", new ItemStack(fuel, 1, 2));
 		OreDictionary.registerOre("fuelBituminous", new ItemStack(fuel, 1, 3));
 		OreDictionary.registerOre("fuelAnthracite", new ItemStack(fuel, 1, 4));
 		OreDictionary.registerOre("fuelGraphite", new ItemStack(fuel, 1, 5));
 		OreDictionary.registerOre("dustCharcoal", new ItemStack(dust, 1, 0));
 		OreDictionary.registerOre("dustPeat", new ItemStack(dust, 1, 1));
 		OreDictionary.registerOre("dustLignite", new ItemStack(dust, 1, 2));
 		OreDictionary.registerOre("dustSubBituminous", new ItemStack(dust, 1, 3));
 		OreDictionary.registerOre("dustBituminous", new ItemStack(dust, 1, 4));
 		OreDictionary.registerOre("dustCoal", new ItemStack(dust, 1, 5));
 		OreDictionary.registerOre("dustAnthracite", new ItemStack(dust, 1, 6));
 		OreDictionary.registerOre("dustGraphite", new ItemStack(dust, 1, 7));
 		OreDictionary.registerOre("dustACharcoal", new ItemStack(dust,1,8));
 		OreDictionary.registerOre("ingotRefinedIron", new ItemStack(ingots,1,0));
 		OreDictionary.registerOre("ingotPigIron", new ItemStack(ingots,1,1));
 		OreDictionary.registerOre("ingotHCSteel", new ItemStack(ingots,1,2));
 		OreDictionary.registerOre("ingotSteel", new ItemStack(ingots,1,3));
 		
 		//make the fuel work with the correct tools
 		MinecraftForge.setBlockHarvestLevel(fuelBlock, 0, "shovel", 0);
 		MinecraftForge.setBlockHarvestLevel(fuelBlock, 1, "shovel", 0);
 		MinecraftForge.setBlockHarvestLevel(fuelBlock, 2, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(fuelBlock, 3, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(fuelBlock, 4, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(fuelBlock, 5, "pickaxe", 1);
 		
 		
 	}
 	
 	@Mod.EventHandler
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		//alternate recipe flags
 		boolean te=false;
 		boolean ic=false;
 		boolean gt = false;
 		boolean ft = false;
 
 		//some mod checking 
 		if(Loader.isModLoaded("gregtech_addon"))
 		{
 			gt = true;
 			FMLLog.log(Level.WARNING, "Gregtech detected!  Carbonization functionality may be adversely affected!");
 		}
 		
 		if(Loader.isModLoaded("factorization"))
 			ft=true;
 		
 		if(Loader.isModLoaded("ThermalExpansion"))
 		{
 			te=true;
 		}
 
 		if(Loader.isModLoaded("IC2"))
 		{
 				ic=true;
 		}
 		
 		generateMash();
 		generateTools(ic, gt, ft);
 		generateSmithing(ic);
 		generateConversions(ic, te);
 		generateMachines(ic);
 		generateStructure(ft);
 		generateMultiblockFurnaceRecipes();
 		generateCarbonizationInfo();
 	}
 	
 	private void generateMash()
 	{
 		//handheld fuel to dust
 		GameRegistry.addShapelessRecipe(new ItemStack(dust,1,0), new Object[]{HHPulv, new ItemStack(Item.coal, 1, 1)});
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,1), new Object[]{HHPulv,"brickPeat"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,2), new Object[]{HHPulv,"brickLignite"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,3), new Object[]{HHPulv,"brickSubBituminous"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,4), new Object[]{HHPulv,"brickBituminous"}));
 		GameRegistry.addShapelessRecipe(new ItemStack(dust,1,5), new Object[]{HHPulv, new ItemStack(Item.coal, 1, 0)});
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,6), new Object[]{HHPulv,"brickAnthracite"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,7), new Object[]{HHPulv,"brickGraphite"}));
 		
 		//handheld dust to fuel
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Item.coal,1,1),new Object[]{HHComp, "dustCharcoal"}));
 		GameRegistry.addShapelessRecipe(new ItemStack(fuel,1,0), new Object[]{HHComp, new ItemStack(dust,1,1)});
 		GameRegistry.addShapelessRecipe(new ItemStack(fuel,1,1), new Object[]{HHComp, new ItemStack(dust,1,2)});
 		GameRegistry.addShapelessRecipe(new ItemStack(fuel,1,2), new Object[]{HHComp, new ItemStack(dust,1,3)});
 		GameRegistry.addShapelessRecipe(new ItemStack(fuel,1,3), new Object[]{HHComp, new ItemStack(dust,1,4)});
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Item.coal,1,0),new Object[]{HHComp, "dustCoal"}));
 		GameRegistry.addShapelessRecipe(new ItemStack(fuel,1,4), new Object[]{HHComp, new ItemStack(dust,1,6)});
 		GameRegistry.addShapelessRecipe(new ItemStack(fuel,1,5), new Object[]{HHComp, new ItemStack(dust,1,7)});
 	}
 	
 	private void generateSmithing(boolean ic)
 	{
		//we have ic2, so use their iron instead
		if(!ic)
		{
			//iron into refined iron
			FurnaceRecipes.smelting().addSmelting(new ItemStack(Item.ingotIron).itemID, new ItemStack(ingots,1,0), 5);
		}
 
 		//refined iron into pig iron
 		CarbonizationRecipes.smelting().addSmelting("ingotRefinedIron", new ItemStack(ingots,1,1), 5, (50*difficultyMod), 1);
 		//pig iron into mild steel
 		CarbonizationRecipes.smelting().addSmelting("ingotPigIron", new ItemStack(ingots,1,2), 5, (75*difficultyMod), 2);
 		//mild steel into steel
 		CarbonizationRecipes.smelting().addSmelting("ingotHCSteel", new ItemStack(ingots,1,3), 5, (100*difficultyMod), 2);
 		
 		//Activated Charcoal
 		FurnaceRecipes.smelting().addSmelting((new ItemStack(dust,1,0)).itemID, 0, new ItemStack(dust,1,8), 0);
 		
 		//Silktouched fuels
 		FurnaceRecipes.smelting().addSmelting(new ItemStack(fuelBlock).itemID, 0, new ItemStack(fuel,1,0), 5);
 		FurnaceRecipes.smelting().addSmelting(new ItemStack(fuelBlock).itemID, 1, new ItemStack(fuel,1,1), 5);
 		FurnaceRecipes.smelting().addSmelting(new ItemStack(fuelBlock).itemID, 2, new ItemStack(fuel,1,2), 5);
 		FurnaceRecipes.smelting().addSmelting(new ItemStack(fuelBlock).itemID, 3, new ItemStack(fuel,1,3), 5);
 		FurnaceRecipes.smelting().addSmelting(new ItemStack(fuelBlock).itemID, 4, new ItemStack(fuel,1,4), 5);
 		FurnaceRecipes.smelting().addSmelting(new ItemStack(fuelBlock).itemID, 5, new ItemStack(fuel,1,5), 5);
 	}
 	
 	private void generateConversions(boolean ic, boolean te)
 	{
 		//1 charcoal -> 1 coal
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,5), new Object[]{HHPure, "dustCharcoal"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,0), new Object[]{HHPure, "dustCoal"}));
 		
 		//1 peat -> 1 lignite
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,2), new Object[]{HHPure, "dustPeat"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,1), new Object[]{HHPure, "dustLignite"}));
 		
 		//5 lignite -> 4 s-bit
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,4,3), new Object[]{HHPure, "dustLignite","dustLignite","dustLignite","dustLignite","dustLignite"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,5,2), new Object[]{HHPure, "dustSubBituminous","dustSubBituminous","dustSubBituminous","dustSubBituminous"}));
 		
 		//3 s-bit -> 2 bit
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,2,4), new Object[]{HHPure, "dustSubBituminous","dustSubBituminous","dustSubBituminous"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,3,3), new Object[]{HHPure, "dustBituminous","dustBituminous"}));
 		
 		//3 bit -> 2 coal
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,2,5), new Object[]{HHPure, "dustBituminous","dustBituminous","dustBituminous"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,3,4), new Object[]{HHPure, "dustCoal","dustCoal"}));
 		
 		//6 coal -> 5 anthracite
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,5,6), new Object[]{HHPure, "dustCoal","dustCoal","dustCoal","dustCoal","dustCoal","dustCoal"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,6,5), new Object[]{HHPure, "dustAnthracite","dustAnthracite","dustAnthracite","dustAnthracite","dustAnthracite"}));
 		
 		//1 anthracite -> 6 graphite
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,6), new Object[]{HHPure, "dustGraphite","dustGraphite","dustGraphite","dustGraphite","dustGraphite","dustGraphite"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,6,7), new Object[]{HHPure, "dustAnthracite"}));
 		
 		if(ic)//ic2 so make the recipes for that too
 		{
 			try {
 				ic2.api.recipe.Recipes.macerator.addRecipe(new RecipeInputItemStack(new ItemStack(fuelBlock,1,0), 1), null, new ItemStack(dust,4,1));
 				ic2.api.recipe.Recipes.macerator.addRecipe(new RecipeInputItemStack(new ItemStack(fuelBlock,1,1), 1), null, new ItemStack(dust,4,2));
 				ic2.api.recipe.Recipes.macerator.addRecipe(new RecipeInputItemStack(new ItemStack(fuelBlock,1,2), 1), null, new ItemStack(dust,4,3));
 				ic2.api.recipe.Recipes.macerator.addRecipe(new RecipeInputItemStack(new ItemStack(fuelBlock,1,3), 1), null, new ItemStack(dust,4,4));
 				ic2.api.recipe.Recipes.macerator.addRecipe(new RecipeInputItemStack(new ItemStack(fuelBlock,1,4), 1), null, new ItemStack(dust,4,6));
 				ic2.api.recipe.Recipes.macerator.addRecipe(new RecipeInputItemStack(new ItemStack(fuelBlock,1,5), 1), null, new ItemStack(dust,4,7));
 				
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,0),1), null, new ItemStack(Item.coal,1,0));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,1),1), null, new ItemStack(fuel,1,0));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,2),1), null, new ItemStack(fuel,1,1));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,3),1), null, new ItemStack(fuel,1,2));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,4),1), null, new ItemStack(fuel,1,3));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,5),1), null, new ItemStack(Item.coal,1,1));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,6),1), null, new ItemStack(fuel,1,4));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust,1,7),1), null, new ItemStack(fuel,1,5));
 			}
 			catch(Exception e)
 			{
 				FMLLog.log(Level.INFO, "Oh dear, something broke with IC2.  Prod Mal so he can fix it.");
 				e.printStackTrace();
 			}
 		}
 		
 		if(te)//thermal expansion recipes
 		{
 			//TODO: Fix when TE is out
 /*			try {
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(fuel,1,0), new ItemStack(dust,1,1), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(fuel,1,1), new ItemStack(dust,1,2), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(fuel,1,2), new ItemStack(dust,1,3), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(fuel,1,3), new ItemStack(dust,1,4), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(fuel,1,4), new ItemStack(dust,1,6), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(fuel,1,5), new ItemStack(dust,1,7), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(Item.coal,1,1), new ItemStack(dust,1,0), false);
 				CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(Item.coal,1,0), new ItemStack(dust,1,5), false);
 			}
 			catch(Exception e)
 			{
 				FMLLog.log(Level.INFO, "Oh dear, something broke with Thermal Expansion.  Prod Mal so he can fix it.");
 			}*/
 		}
 	}
 	
 	private void generateTools(boolean ic, boolean gt, boolean ft)
 	{
 		//Handheld tools
 		if(!gt)
 			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(carbonization.hhpulv), new Object[] {new ItemStack(Item.flint), new ItemStack(Item.bowlEmpty)}));
 		else
 			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(carbonization.hhpulv), new Object[] {new ItemStack(Item.flint), new ItemStack(Item.bowlEmpty), RecipeCharm}));
 		GameRegistry.addRecipe(new ItemStack(carbonization.hhcomp), new Object[] {" O ", "S O", " S ", 'O', Block.obsidian, 'S', Item.stick});
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(carbonization.hhpure), new Object[]{" I ", "ICI", "SI ", 'I', Item.ingotIron, 'C', "dustACharcoal", 'S', Item.stick}));
 		
 		if(gt || ft || ic)
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(RecipeCharm, new Object[]{"  A", " I ", "A  ", 'A', Item.coal, 'I', "ingotPigIron"}));
 		
 		//Simple Recipes
 		GameRegistry.addRecipe(new ItemStack(misc,1,0), new Object[]{"S","G", 'S', Item.stick, 'G', new ItemStack(fuel,1,5)});
 		GameRegistry.addRecipe(new ItemStack(misc,1,0), new Object[]{"S","C", 'S', Item.stick, 'C', new ItemStack(Item.coal,1,1)});
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.writableBook,1), new Object[]{Item.book, new ItemStack(misc,1,0)});
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,1), new Object[]{new ItemStack(Item.potion,1,0), "dustACharcoal"}));
 		if(!ic)
 			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,2), new Object[]{new ItemStack(Item.potion,1,0), "dustCoal"}));
 		else
 			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,2), new Object[]{new ItemStack(Item.potion,1,0), "dustCoal", RecipeCharm}));
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 2), new Object[]{"C","S",'C', new ItemStack(fuel,1,2), 'S', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 4), new Object[]{"C","S",'C', new ItemStack(fuel,1,3), 'S', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 8), new Object[]{"C","S",'C', new ItemStack(fuel,1,4), 'S', Item.stick});
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc, 1, 13), new Object[]{HHComp, "dustGraphite", "dustGraphite"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc, 1, 3), new Object[]{HHComp, new ItemStack(misc,1,13), new ItemStack(misc,1,13), new ItemStack(misc,1,13), new ItemStack(misc,1,13)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,2,14), new Object[]{"GGG", 'G', new ItemStack(misc,1,13)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,3,15), new Object[]{"GGG", 'G', new ItemStack(misc,1,14)}));
 		GameRegistry.addShapelessRecipe(new ItemStack(misc,1,16), new Object[]{HHComp, new ItemStack(misc, 1, 12), new ItemStack(misc, 1, 12), 
 			new ItemStack(misc, 1, 12), new ItemStack(misc, 1, 12), new ItemStack(misc, 1, 12), new ItemStack(misc, 1, 12), new ItemStack(misc, 1, 12), new ItemStack(misc, 1, 12)});
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,2,17), new Object[]{"GGG", 'G', new ItemStack(misc,1,16)}));
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(fuelCell,1,0), new Object[]{" S ", "SCS", " S ", 'S', "ingotSteel", 'C', new ItemStack(Item.coal,1,0)}));
 		
 		
 		
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.diamond), new Object[]{HHComp, new ItemStack(misc, 1, 3), new ItemStack(misc, 1, 3), 
 			new ItemStack(misc, 1, 3), new ItemStack(misc, 1, 3), new ItemStack(misc, 1, 3), new ItemStack(misc, 1, 3), new ItemStack(misc, 1, 3), new ItemStack(misc, 1, 3)});
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,4,4), new Object[]{"xyx", "yzy", "xyx", 'x', Block.gravel, 'y', Block.thinGlass, 'z', Item.clay}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,4,5), new Object[]{"xyx", "yxy", "xyx", 'x', Block.sand, 'y', new ItemStack(misc, 1, 3)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,0), new Object[]{new ItemStack(misc, 1, 6), new ItemStack(misc, 1, 6), new ItemStack(misc, 1, 6), new ItemStack(misc, 1, 6)}));
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc, 1, 7), new Object[]{" x ", "xyx", " x ", 'x', Item.ingotIron, 'y', Item.redstone}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc, 1, 8), new Object[]{" x ", "xyx", " x ", 'x', "ingotRefinedIron", 'y', Item.redstone}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc, 1, 9), new Object[]{" x ", "xyx", " x ", 'x', "ingotPigIron", 'y', Item.redstone}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc, 1, 10), new Object[]{" x ", "xyx", " x ", 'x', "ingotHCSteel", 'y', Item.redstone}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc, 1, 11), new Object[]{" x ", "xyx", " x ", 'x', "ingotSteel", 'y', Item.redstone}));
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,1,18), new Object[]{"xxx", 'x', new ItemStack(ingots,1,0)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(misc,1,19), new Object[]{"xxx", 'x', "ingotSteel"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,4,12), new Object[]{new ItemStack(dust,1,7)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dust,1,7), new Object[]{new ItemStack(misc,1,12), new ItemStack(misc,1,12), new ItemStack(misc,1,12), new ItemStack(misc,1,12)}));
 		
 		
 		if(ic)//IC2, so add in compressor recipes for the carbon chunk and alternate "cleansing" potion
 		{
 			try
 			{
				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(dust, 1, 7), 2), null, new ItemStack(misc, 1, 13));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(misc, 1, 13), 4), null, new ItemStack(misc, 1, 3));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(misc, 1, 3), 8), null, new ItemStack(Item.diamond));
 			
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(misc, 1, 12), 8), null, new ItemStack(misc, 1, 16));
 				ic2.api.recipe.Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(misc, 1, 16), 3), null, new ItemStack(misc, 2, 17));
 			}
 			catch(Exception e)
 			{
 				FMLLog.log(Level.INFO, "Oh dear, something broke with IC2.  Prod Mal so he can fix it.");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void generateMachines(boolean ic)
 	{
 		if(!ic)
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(furnaceBlock,1,0), 
 					new Object[]{" I ", "I I", "IFI", 'I' ,Item.ingotIron, 'F', Block.furnaceIdle}));
 		else
 		{
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(furnaceBlock,1,0), 
 					new Object[]{"   ", "III", "IFI", 'I' ,Item.ingotIron, 'F', Block.furnaceIdle}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(furnaceBlock,1,1), 
 					new Object[]{"III", "IFI", "BBB", 'I' ,"ingotRefinedIron", 'F', Items.getItem("ironFurnace"), 'B', Block.brick}));
 
 		}
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(furnaceBlock,1,1), 
 				new Object[]{"III", "IFI", "BBB", 'I' ,"ingotRefinedIron", 'F', new ItemStack(furnaceBlock,1,0), 'B', Block.brick}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(furnaceBlock,1,2), 
 				new Object[]{"SSS", "SFS", "SSS", 'S' ,"ingotSteel", 'F', new ItemStack(furnaceBlock,1,1)}));
 	}
 
 	private void generateControlBlocks()
 	{
 		//Furnace Control
 		for(int i = 0; i<19; i++)
 		{
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(FurnaceControl, 1), new Object[]
 					{"SSS", "SFS", "SSS", 'S', new ItemStack(itemStructureBlock,1,1000+i), 'F', new ItemStack(furnaceBlock,1,1)}));
 		}
 		
 		//Autocrafting Bench
 		for(int i = 0; i<19; i++)
 		{
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(autocraftingBench,1,0), new Object[]
 					{"SDS", "SCS", "SFS", 'S', new ItemStack(itemStructureBlock,1,2000+i), 'D', Item.diamond, 'C', Block.workbench, 'F', new ItemStack(furnaceBlock,1,2)}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(autocraftingBench,1,1), new Object[]
 					{"SSS", "PUC", "SFS", 'S', new ItemStack(itemStructureBlock,1,2000+i), 'P', HHPulv, 'U', HHPure, 'C', HHComp, 'F', new ItemStack(furnaceBlock,1,2)}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(autocraftingBench,1,2), new Object[]
 					{"SSS", "PUP", "SDS", 'S', new ItemStack(itemStructureBlock,1,i), 'P', new ItemStack(itemStructureBlock,1,2000+i), 'U', Item.diamond, 'D', new ItemStack(fuelCell)}));
 		}
 	}
 	
 	private void generateStructure(boolean ft)
 	{
 		generateControlBlocks();
 		
 		//basic structure blocks, basic recipes
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,0), new Object[]{"x x", " y ", "x x", 'x', Block.ice, 'y', Block.ice}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,1,0), new Object[]{"x x", " y ", "x x", 'x', Item.snowball, 'y', Block.ice}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Block.ice), new Object[]{new ItemStack(itemStructureBlock,1,0)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,1), new Object[]{"x x", " y ", "x x", 'x', Block.planks, 'y', Block.planks}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Block.planks), new Object[]{new ItemStack(itemStructureBlock,1,1)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,2), new Object[]{"x x", " y ", "x x", 'x', Block.stone, 'y', Block.stone}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Block.stone), new Object[]{new ItemStack(itemStructureBlock,1,2)}));
 		if(!ft)
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,3), new Object[]{"x x", " y ", "x x", 'x', Item.ingotIron, 'y', Item.ingotIron}));
 		else
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,3), new Object[]{"x x", "zy ", "x x", 'x', Item.ingotIron, 'y', Item.ingotIron, 'z', RecipeCharm}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Item.ingotIron), new Object[]{new ItemStack(itemStructureBlock,1,3)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,4), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(misc,1,13), 'y', new ItemStack(misc,1,13)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,13), new Object[]{new ItemStack(itemStructureBlock,1,4)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,5), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(Item.ingotIron), 'y', new ItemStack(misc,1,13)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Item.ingotIron), new Object[]{new ItemStack(itemStructureBlock,1,5)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,6), new Object[]{"x x", " y ", "x x", 'x', "ingotRefinedIron", 'y', "ingotRefinedIron"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,0), new Object[]{new ItemStack(itemStructureBlock,1,6)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,7), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(misc,1,14), 'y', new ItemStack(misc,1,14)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,14), new Object[]{new ItemStack(itemStructureBlock,1,7)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,8), new Object[]{"x x", " y ", "x x", 'x', "ingotRefinedIron", 'y', new ItemStack(misc,1,14)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,0), new Object[]{new ItemStack(itemStructureBlock,1,8)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,9), new Object[]{"x x", " y ", "x x", 'x', "ingotPigIron", 'y', "ingotPigIron"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,1), new Object[]{new ItemStack(itemStructureBlock,1,9)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,10), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(misc,1,15), 'y', new ItemStack(misc,1,15)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,15), new Object[]{new ItemStack(itemStructureBlock,1,10)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,11), new Object[]{"x x", " y ", "x x", 'x', "ingotPigIron", 'y', new ItemStack(misc,1,15)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,1), new Object[]{new ItemStack(itemStructureBlock,1,11)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,12), new Object[]{"x x", " y ", "x x", 'x', "ingotHCSteel", 'y', "ingotHCSteel"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,2), new Object[]{new ItemStack(itemStructureBlock,1,12)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,13), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(misc,1,16), 'y', new ItemStack(misc,1,16)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,16), new Object[]{new ItemStack(itemStructureBlock,1,13)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,14), new Object[]{"x x", " y ", "x x", 'x', "ingotHCSteel", 'y', new ItemStack(misc,1,16)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,2), new Object[]{new ItemStack(itemStructureBlock,1,14)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,15), new Object[]{"x x", " y ", "x x", 'x', "ingotSteel", 'y', "ingotSteel"}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,3), new Object[]{new ItemStack(itemStructureBlock,1,15)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,16), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(misc,1,17), 'y', new ItemStack(misc,1,17)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(misc,1,17), new Object[]{new ItemStack(itemStructureBlock,1,16)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,17), new Object[]{"x x", " y ", "x x", 'x', "ingotSteel", 'y', new ItemStack(misc,1,17)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ingots,1,3), new Object[]{new ItemStack(itemStructureBlock,1,17)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,2,18), new Object[]{"x x", " y ", "x x", 'x', new ItemStack(Item.netherStar), 'y', new ItemStack(Block.dragonEgg)}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Block.dragonEgg), new Object[]{new ItemStack(itemStructureBlock,1,18)}));
 		
 		//Purpose
 		for(int i = 1; i<3; i++)
 		{
 			for(int j = 0; j<19; j++)
 			{
 				if(i==1)
 				{
 					CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,4,i*1000+j), new Object[]{" x ", "xyx", " x ", 'x', new ItemStack(itemStructureBlock,1,j), 'y', (j<3)?(Block.furnaceIdle):((j<9)?(new ItemStack(furnaceBlock,1,0)):((j<12)?(new ItemStack(furnaceBlock,1,1)):(new ItemStack(furnaceBlock,1,2))))}));
 					CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(itemStructureBlock,1,j), new Object[]{new ItemStack(itemStructureBlock,1,1000*i+j)}));
 				}
 				if(i==2)
 				{
 					CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,4,i*1000+j), new Object[]{" x ", "xyx", " x ", 'x', new ItemStack(itemStructureBlock,1,j), 'y', (j<3)?(new ItemStack(misc,1,7)):((j<7)?(new ItemStack(misc,1,8)):((j<11)?(new ItemStack(misc,1,9)):((j<15)?(new ItemStack(misc,1,10)):(new ItemStack(misc,1,11)))))}));
 					CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(itemStructureBlock,1,j), new Object[]{new ItemStack(itemStructureBlock,1,1000*i+j)}));
 				}
 			}
 		}
 		
 		//Secondary Material
 		for(int i = 1; i<5; i++)
 			for(int j = 0; j<3; j++)
 				for(int k = 0; k<19; k++)
 				{
 					CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(itemStructureBlock,5,i*100+1000*j+k), new Object[]{"xyx", "yxy", "xyx", 'x', new ItemStack(itemStructureBlock,1,1000*j+k), 'y', (i==1)?(new ItemStack(misc,1,4)):((i==2)?(new ItemStack(misc,1,5)):((i==3)?(new ItemStack(misc,1,18)):(new ItemStack(misc,1,19))))}));
 					CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(itemStructureBlock,1,i*100+1000*j+k), new Object[]{new ItemStack(itemStructureBlock,1,1000*j+k), (i==1)?(new ItemStack(misc,1,4)):((i==2)?(new ItemStack(misc,1,5)):((i==3)?(new ItemStack(misc,1,18)):(new ItemStack(misc,1,19))))}));
 					CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(itemStructureBlock,1,j*1000+k), new Object[]{new ItemStack(itemStructureBlock,1,100*i+1000*j+k)}));
 				}
 	}
 	
 	private void addStructureLanguage()
 	{
 		for(int base = 0; base < 19; base++)
 		{
 			for(int sec = 0; sec < 5; sec++)
 			{
 				for(int purp = 0; purp < 3; purp++)
 				{
 					String name = "";
 					
 					switch(base)
 					{
 					case 0:
 						name += "Ice";
 						break;
 					case 1:
 						name += "Wood";
 						break;
 					case 2:
 						name += "Stone";
 						break;
 					case 3:
 						name += "Iron";
 						break;
 					case 4:
 						name += "Carbon Flake";
 						break;
 					case 5:
 						name += "Carbon-Plated Iron";
 						break;
 					case 6:
 						name += "Refined Iron";
 						break;
 					case 7:
 						name += "Carbon Thread";
 						break;
 					case 8:
 						name += "Carbon-Infused Rebar";
 						break;
 					case 9:
 						name += "Pig Iron";
 						break;
 					case 10:
 						name += "Carbon Fibre";
 						break;
 					case 11:
 						name += "Reinforced Carbon Fibre";
 						break;
 					case 12:
 						name += "High Carbon Steel";
 						break;
 					case 13:
 						name += "Carbon Nanoflake";
 						break;
 					case 14:
 						name += "Carbon-Plated Steel";
 						break;
 					case 15:
 						name += "Steel";
 						break;
 					case 16:
 						name += "Carbon Nanotube";
 						break;
 					case 17:
 						name += "Reinforced Carbon Nanotube";
 						break;
 					case 18:
 						name += "Withered End";
 						break;
 					}
 					
 					name += " ";
 					
 					switch(purp)
 					{
 					case 0:
 						name += "Structure Block";
 						break;
 					case 1:
 						name += "Furnace Block";
 						break;
 					case 2:
 						name += "Machine Block";
 						break;
 					}
 					
 /*					if(sec > 0)
 						name += " with ";
 					switch(sec)
 					{
 					case 1:
 						name += "Basic Insulation";
 						break;
 					case 2:
 						name += "High Density Insulation";
 						break;
 					case 3:
 						name += "Coarse Threading";
 						break;
 					case 4:
 						name += "Fine Threading";
 						break;
 					}*/
 					
 					LanguageRegistry.addName(new ItemStack(itemStructureBlock,1,base+sec*100+purp*1000), name);
 				}
 			}
 		}
 	}
 	
 	//TODO: Remember to update this when other things come out
 	private void generateCarbonizationInfo()
 	{
 		for(int i = 0; i < 4; i++)
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(FurnaceControl), 3+i);
 		
 		for(int i = 0; i<3; i++)
 			for(int j = 0; j<5; j++)
 				for(int k = 0; k<19; k++)
 					CarbonizationRecipes.smelting().addInfoItem(new ItemStack(itemStructureBlock, 1, i*1000+j*100+k), 2);
 		
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(autocraftingBench,1,0), 7);
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(autocraftingBench,1,0), 8);
 		
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(autocraftingBench,1,1), 10);
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(autocraftingBench,1,1), 11);
 		
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(autocraftingBench,1,2), 17);
 		
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(fuelCell,1), 16);
 		
 		for(int i = 0; i < 3; i++)
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(furnaceBlock, 1, i), 9);
 		
 		for(int i = 0; i<4; i++)
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(ingots, 1, i), 0);
 		
 		for(int i = 0; i<20; i++)
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(misc, 1, i), 0);
 		
 		
 		for(int i = 0; i<6; i++)
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(fuelBlock, 1, i), 0);
 		
 		for(int j = 0; j < 3; j++)
 		{
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(carbonization.hhpure), 12+j);
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(carbonization.hhcomp), 12+j);
 			CarbonizationRecipes.smelting().addInfoItem(new ItemStack(carbonization.hhpulv), 12+j);
 			
 			for(int i = 0; i<9; i++)
 			{
 				if(i<6)
 					CarbonizationRecipes.smelting().addInfoItem(new ItemStack(fuel, 1, i), 12+j);
 				CarbonizationRecipes.smelting().addInfoItem(new ItemStack(dust, 1, i), 12+j);
 			}
 		}
 		
 		CarbonizationRecipes.smelting().addInfoItem(new ItemStack(recipeCharm), 15);
 	}
 	
 	private void generateMultiblockFurnaceRecipes()
 	{
 		//Add in the ore slag relationships
 		CarbonizationRecipes.smelting().addOreSlag("ashSlag", new ItemStack(misc, 1, 6));
 		CarbonizationRecipes.smelting().addOreSlag("stoneSlag", new ItemStack(Block.stone));
 		CarbonizationRecipes.smelting().addOreSlag("glassSlag", new ItemStack(Block.glass));
 		CarbonizationRecipes.smelting().addOreSlag("ironSlag", new ItemStack(Item.ingotIron));
 		CarbonizationRecipes.smelting().addOreSlag("goldSlag", new ItemStack(Item.ingotGold));
 		CarbonizationRecipes.smelting().addOreSlag("brickSlag", new ItemStack(Item.brick));
 		CarbonizationRecipes.smelting().addOreSlag("brickBlockSlag", new ItemStack(Block.brick));
 		CarbonizationRecipes.smelting().addOreSlag("netherBrickBlockSlag", new ItemStack(Item.netherrackBrick));
 		CarbonizationRecipes.smelting().addOreSlag("copperSlag", "ingotCopper");
 		CarbonizationRecipes.smelting().addOreSlag("tinSlag", "ingotTin");
 		CarbonizationRecipes.smelting().addOreSlag("silverSlag", "ingotSilver");
 		CarbonizationRecipes.smelting().addOreSlag("leadSlag", "ingotLead");
 		CarbonizationRecipes.smelting().addOreSlag("nickelSlag", "ingotNickel");
 		boolean b = CarbonizationRecipes.smelting().addOreSlag("aluminumSlag", "ingotNaturalAluminum");
 		if(!b)
 			CarbonizationRecipes.smelting().addOreSlag("aluminumSlag", "ingotAluminum");
 		
 		//Add in the recipes, normal metals and blocks
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Block.cobblestone), 200, 1, "stoneSlag", true);
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Block.sand), 200, 1, "glassSlag", true);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreIron", 400, 1, "ironSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreGold", 400, 1, "goldSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.clay), 200, 1, "brickSlag", true);
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Block.blockClay), 400, 1, "brickBlockSlag", true);
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Block.netherrack), 400, 1, "netherBrickBlockSlag", true);
 		
 		//Add in common mod ores through OreDictionary
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreCopper", 400, 1, "copperSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreTin", 400, 1, "tinSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreSilver", 400, 1, "silverSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreLead", 400, 1, "leadSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreNickel", 400, 1, "nickelSlag", false);
 		CarbonizationRecipes.smelting().addMultiblockSmelting("oreNaturalAluminum", 400, 1, "aluminumSlag", false);
 		
 		//food and wood items
 		//TODO: Find a way to do this easier
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.appleRed), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.arrow), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.axeWood), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.bakedPotato), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.beefCooked), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.beefRaw), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.boat), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.bow), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.bowlEmpty), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.book), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.bowlSoup), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.bread), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.cake), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.carrot), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.carrotOnAStick), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.chickenCooked), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.chickenRaw), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.cookie), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.doorWood), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.egg), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.enchantedBook), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.feather), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.fishCooked), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.fishRaw), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.fishingRod), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.hoeWood), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.helmetLeather), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.itemFrame), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.leather), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.legsLeather), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.melon), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.melonSeeds), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.painting), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.paper), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.pickaxeWood), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.poisonousPotato), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.porkCooked), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.porkRaw), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.potato), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.pumpkinPie), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.pumpkinSeeds), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.plateLeather), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.reed), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.rottenFlesh), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.saddle), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.seeds), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.shovelWood), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.sign), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.silk), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.stick), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.swordWood), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.wheat), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.writableBook), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.writtenBook), 50, 1, "ashSlag");
 		CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Item.bow), 50, 1, "ashSlag");
 		
 		for(int i = 0; i<Block.blocksList.length; i++)
 		{
 			if(Block.blocksList[i] != null)
 			{
 				Material m = Block.blocksList[i].blockMaterial; 
 				if(m == Material.cloth || m==Material.pumpkin || m==Material.wood || m==Material.cactus)
 				{
 					if(Block.blocksList[i].damageDropped(1) == 1)//simple check for damage values not 0
 						for(int j = 0; j<16; j++)
 						{
 							if(new ItemStack(Block.blocksList[i], 1, j) != null)
 							{
 								CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Block.blocksList[i], 1, j), 100, 1, "ashSlag");
 							}
 						}
 					CarbonizationRecipes.smelting().addMultiblockSmelting(new ItemStack(Block.blocksList[i]), 100, 1, "ashSlag");
 				}
 			}
 		}
 
 	}
 }
 
 /*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the included license, which is also
 * available at http://carbonization.wikispaces.com/License
 * 
 *********************************************************************************/
