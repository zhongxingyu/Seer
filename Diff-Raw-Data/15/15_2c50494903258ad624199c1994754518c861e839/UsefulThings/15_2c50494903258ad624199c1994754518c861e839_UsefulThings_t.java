 package zh.usefulthings;
 
 //internal imports
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import scala.Console;
 
 import zh.usefulthings.blocks.ZHBlock;
 import zh.usefulthings.blocks.ZHCoalOre;
 import zh.usefulthings.blocks.ZHMultiOreItemBlock;
 import zh.usefulthings.blocks.ZHMultiOreBlock;
 import zh.usefulthings.client.ClientProxy;
 import zh.usefulthings.crops.ZHCactus;
 import zh.usefulthings.crops.ZHCactusFruit;
 import zh.usefulthings.crops.ZHCactusItemBlock;
 import zh.usefulthings.crops.ZHFlaxCrop;
 import zh.usefulthings.crops.ZHSeeds;
 import zh.usefulthings.crops.ZHTreeShroom;
 import zh.usefulthings.eventHandlers.ZHFuelHandler;
 import zh.usefulthings.eventHandlers.ZHUsefulDropsEvent;
 import zh.usefulthings.food.*;
 import zh.usefulthings.items.ZHDye;
 import zh.usefulthings.items.ZHItem;
 import zh.usefulthings.tools.ZHAthame;
 import zh.usefulthings.tools.ZHAxe;
 import zh.usefulthings.tools.ZHHoe;
 import zh.usefulthings.tools.ZHPickaxe;
 import zh.usefulthings.tools.ZHShovel;
 import zh.usefulthings.tools.ZHSword;
 import zh.usefulthings.worldgen.ZHWorldGen;
 //forge/minecraft imports
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSeeds;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.potion.Potion;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraft.item.crafting.CraftingManager;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="ZHUsefulThings", name="Useful Things", version="0.0.2")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class UsefulThings 
 {	
 	public static Block zhOres;
 
 	//new foods
 	//All below drop bones when eaten
 	public static Item ribsRaw = null;
 	public static Item ribsCooked = null;
 	public static Item drumstickRaw = null;
 	public static Item drumstickCooked = null;
 	
 	//Generic meats
 	public static Item muttonRaw = null;
 	public static Item muttonCooked = null;
 	
 	//Generic foods
 	//Below are not done!
 	//public static Item cookedEgg = null;
 	//public static Item zombieJerky = null;
 	//public static Item jerky = null;
 	//public static Item sandwich = null;
 	//public static Item fruitSalad = null;
 	//public static Item roastedSeeds = null;
 	//public static Item soupStock = null;
 	
 	//Special foods
 	//public static Item magicFruitSalad = null;
 	//public static Item magicCake = null;
 	//public static Item soups = null;
 	
 	//ingredients
 	//Below are not done!
 	//public static Item flour = null;
 	//public static Item milkBottle= null;
 	
 	//new plants/seeds/fruit/whatever
 	public static Item cactusFruit = null;
 	public static ItemSeeds flaxSeeds = null;
 	//Below are not done!
 	//public static Item berries = null;
 	//public static Item treeShroomSeed = null;
 
 	//new dyes
 	public static Item zhDyes = null;
 		
 	public static Item tar = null;
 	public static Item sapphireGem = null;
 	public static Item salt = null;
 	
 	public static final Block multiBlock = null;
 	public static Block flaxCrop = null;
 	public static Block cactusFruitBlock = null;
 	public static Block zhCoalOre = null;
 	//TODO: Replace cactus with my own? Fruiting cactus block?
 	public static Block zhCactus = null;
 	//TODO: crafting table made from cobblestone
 	//blocks below are not done!
 	//public static Block treeShroom = null;
 	//public static Block meatPlant = null;
 	//public static Block strawberryBush = null;
 	//public static Block blueberryBush = null;
 	//public static Block blackberryBush = null;
 	//public static Block raspberryBush = null;
 	//public static Block cranberryBush = null;
 	//public static Block sapphireBlock = null;
 	
 	//Tools
 	public static Item sapphirePick = null;
 	public static Item sapphireShovel = null;
 	public static Item sapphireHoe = null;
 	public static Item sapphireAxe = null;
 	public static Item sapphireSword = null;
 	public static Item flintPick = null;
 	public static Item flintShovel = null;
 	public static Item flintHoe = null;
 	public static Item flintAxe = null;
 	public static Item flintSword = null;
 	public static Item cactusPick = null;
 	public static Item cactusShovel = null;
 	public static Item cactusHoe = null;
 	public static Item cactusAxe = null;
 	public static Item cactusSword = null;
 	public static Item swordAthame = null;
 	//below are not done!
 	public static Item sickle = null;
 		
 	public static Configuration config = null;
 	
 	public static EnumToolMaterial sapphireMaterial = null;
 	public static EnumToolMaterial cactusMaterial = null;
 	public static EnumToolMaterial flintMaterial = null;
 	
 	public static ZHWorldGen zhWorldGen;
 	
 	private static Property curID;
 	private static Property curItemID;
 	private static int blockID = 410;
 	private static int itemID = 5521;
 	
 	// The instance of your mod that Forge uses.
     @Instance("ZHUsefulThings")
     public static UsefulThings instance;
     
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide="zh.usefulthings.client.ClientProxy", serverSide="zh.usefulthings.CommonProxy")
     public static CommonProxy proxy;
 
     //default info about the ores
   	public static final int numOres = 3;
   	public static final String[] subNames = {"bitumenOre", "sapphireOre", "saltOre"};
   	public static final String[] multiBlockName = {"Tar Ore", "Sapphire Ore", "Salt Ore"};
   	public static int[] minY = {0,0,50};
   	public static int[] maxY = {55,50,128};
   	public static int[] numVeins = {3,5,10};
   	public static int[] veinSize = {16,9,20};
   	private static int[] harvestLevel = {1,2,0};
     
     //Properties!
     private static Property[] generateOre = new Property[numOres];
     private static Property[] oreMinY = new Property[numOres];
 	private static Property[] oreMaxY = new Property[numOres];
 	private static Property[] oreNumVeins = new Property[numOres];
 	private static Property[] oreVeinSize = new Property[numOres];
 	
 	//Vanilla changes
 	private static Property replaceCoal;
 	private static Property potionsStack;
 	private static Property maxPotionStack;
 	private static Property durableShovels;
 	private static Property durableAxes;
 	private static Property moreSeeds;
 	private static Property morePassiveDrops;
 	private static Property moreEnemyDrops;
 	private static Property flintTools;
 	private static Property cactusTools;
 	
 	private static Property generateCactusFruit;
 	private static Property generateClay;
 	private static Property generateMelon;
 	private static Property generateFlax;
 	private static Property generateMud;
 	private static Property generateSilverfish;
     
 	private static Property dimensionBlacklist;
     		
 	//Other Generation
 	public static boolean[] gen = new boolean[numOres];
 	public static boolean genCactusFruit;
 	public static boolean genClay;
 	public static boolean genMelon;
 	public static boolean genFlax;
 	public static boolean genSilverfish;
 	public static boolean betterPassiveMobDrops;
 	public static boolean betterEnemyMobDrops;
 
 	public static List<Integer> dimBlacklist = new ArrayList<Integer>();
     public static List<String> worldTypeBlacklist = new ArrayList<String>();
     
     public static Logger logger;
     public static File configFolder;
 		
     @PreInit
     public void preInit(FMLPreInitializationEvent event) 
     {
     	
     	configFolder = new File(event.getModConfigurationDirectory().getAbsolutePath() + "/zh/UsefulThings/");
     	
     	logger = event.getModLog();
     	
     	try
     	{
 			config = new Configuration(new File(configFolder.getAbsolutePath() + "/config.cfg"));
 			//Console.println("Loading config from: " + new File(configFolder.getAbsolutePath() + "/config.cfg").getAbsolutePath());
 			config.load();
 			
 			curID = config.getBlock("blockID", blockID);
 			curID.comment = "The block ID to use as a starting point for assignment";
 			
 			curItemID = config.getItem("itemID", itemID);
 			curItemID.comment = "The item ID to use as a starting point for assignment";
 			
 			dimensionBlacklist = config.get("World Generation", "Dimension Blacklist", "-1,1");
 	        dimensionBlacklist.comment = "A comma-separated list of dimension IDs to disable worldgen in.";
 			
 			for (int i = 0; i < numOres; i++)
 			{
 				generateOre[i] = config.get("World Generation","Generate " + multiBlockName[i],true);
 				oreMinY[i] = config.get("World Generation",multiBlockName[i] + "Min Y",minY[i]);
 				oreMaxY[i] = config.get("World Generation",multiBlockName[i] + "Max Y",maxY[i]);
 				oreVeinSize[i] = config.get("World Generation",multiBlockName[i] + "Vein Size",veinSize[i]);
 				oreNumVeins[i] = config.get("World Generation",multiBlockName[i] + "Num Veins",numVeins[i]);
 			        	
 				gen[i] = generateOre[i].getBoolean(true);
 				minY[i] = oreMinY[i].getInt();
 				maxY[i] = oreMaxY[i].getInt();
 				numVeins[i] = oreNumVeins[i].getInt();
 				veinSize[i] = oreVeinSize[i].getInt();
 			}
 			
 			generateCactusFruit = config.get("World Generation","Generate Cactus Berry pods",true);
 			generateCactusFruit.comment = "Will a single fruit-bearing cactus in about 1 in 15 Desert chunks";
 			generateMelon = config.get("World Generation", "Generate wild Melons", true);
 			generateMelon.comment = "Will rarely spawn in Ocean, Jungle, Plains, and Forest biomes";
 			generateFlax = config.get("World Generation", "Generate wild Flax plants", true);
 			generateFlax.comment = "Will rarely spawn in Plains, Forest, and Extreme Hills biomes. Note: Flax will spawn from bonemeal and drops as seeds from grass even if this is false.";
 			
 			generateClay = config.get("World Generation", "Generate Clay deposits underground", true);
 			generateSilverfish = config.get("World Generation", "Generate nice surprises", true);
 			
 			replaceCoal = config.get("Vanilla Enhanced", "Coal Ore drops more Coal", true);
 			potionsStack = config.get("Vanilla Enhanced", "Potions stack", true);
 			maxPotionStack = config.get("Vanilla Enhanced", "Potion Stack Size", 3);
 			maxPotionStack.comment = "Recommended to be a multiple of 3 due to how potion brewing works!";
 			durableShovels = config.get("Vanilla Enhanced", "Shovels more durable", true);
 			durableAxes = config.get("Vanilla Enhanced", "Axes more durable", true);
 			moreSeeds = config.get("Vanilla Enhanced", "Grass drops all seeds", true);
 			morePassiveDrops = config.get("Vanilla Enhanced", "Passive Mobs drop +1 loot", true);
 			moreEnemyDrops = config.get("Vanilla Enhanced", "Enemy Mobs drop +1 loot", true);
 			flintTools = config.get("Vanilla Enhanced", "Enable Flint Tools", true);
 			cactusTools = config.get("Vanilla Enhanced", "Enable Cactus Tools", true);
 			cactusTools.comment = "Also enables 2x Cactus -> 4x stick recipe";
 			
 			genCactusFruit = generateCactusFruit.getBoolean(true);
 			genMelon = generateMelon.getBoolean(true);
 			genFlax = generateFlax.getBoolean(true);
 			genClay = generateClay.getBoolean(true);
 			genSilverfish = generateSilverfish.getBoolean(true);
 			betterPassiveMobDrops = morePassiveDrops.getBoolean(true);
 			betterEnemyMobDrops = moreEnemyDrops.getBoolean(true);
     	}
     	catch (Exception e)
     	{
     		logger.log(Level.SEVERE, "UsefulThings couldn't load the config file");
             e.printStackTrace();   		
     	}
     	finally
     	{
     		config.save();
     	}
         
         setDimBlackList();
         extractLang(new String[] {"en_US"});
         loadLang();
         
         //ClientProxy.init();
         //CommonProxy.init();
     }
     
     public static void initClient(FMLPreInitializationEvent evt)
     {
         
     }
     
     @ServerStarting
     public void serverStarting(FMLServerStartingEvent evt)
     {
         
     }
     
     public static void extractLang(String[] languages)
     {
         String langResourceBase =  "/zh/usefulthings/lang/";
         for (String lang : languages)
         {
         	//Console.println("Load language file: " + langResourceBase + lang + ".lang");
             InputStream is = UsefulThings.instance.getClass().getResourceAsStream(langResourceBase + lang + ".lang");
            // if (is == null)
             	//Console.println("Load language file failed!");
             try
             {
                 File f = new File(configFolder.getAbsolutePath() + "/lang/" + lang + ".lang");
                 if (!f.exists())
                     f.getParentFile().mkdirs();
                 OutputStream os = new FileOutputStream(f);
                 byte[] buffer = new byte[1024];
                 int read = 0;
                 while ((read = is.read(buffer)) != -1)
                 {
                     os.write(buffer, 0, read);
                 }
                 is.close();
                 os.flush();
                 os.close();
             }
             catch (IOException e)
             {
             	logger.log(Level.SEVERE, "Couldn't load language file: " + langResourceBase + lang + ".lang");
                 e.printStackTrace();
             }
         }
     }
     
     public static void loadLang()
     {
         File f = new File(configFolder.getAbsolutePath() + "/lang/");
         //Console.println("Load language file: " + f.getAbsolutePath());
         
         for (File langFile : f.listFiles(new FilenameFilter()
         {
             @Override
             public boolean accept(File dir, String name)
             {
                 return name.endsWith(".lang");
             }
         }))
         {
             try
             {
                 Properties langPack = new Properties();
                 langPack.load(new FileInputStream(langFile));
                 String lang = langFile.getName().replace(".lang", "");
                 LanguageRegistry.instance().addStringLocalization(langPack, lang);
             }
             catch (FileNotFoundException x)
             {
                 x.printStackTrace();
             }
             catch (IOException x)
             {
                 x.printStackTrace();
             }
         }
     }
     
     private static void setDimBlackList()
     {
         String blacklist = dimensionBlacklist.getString().trim();
         
         for (String dim : blacklist.split(","))
         {
             try
             {
                 Integer dimID = Integer.parseInt(dim);
                 if (!dimBlacklist.contains(dimID))
                     dimBlacklist.add(dimID);
             }
             catch (Exception e)
             {
             }
         }
     }
     
     @Init
     public void load(FMLInitializationEvent event) 
     {	
         //Registers new Drop Event - handles new mob drops
         MinecraftForge.EVENT_BUS.register(new ZHUsefulDropsEvent());
         //Registers new Fuel handler - handles improved fuel values
         GameRegistry.registerFuelHandler(new ZHFuelHandler());  
     	
 		//New Tool Material
 		sapphireMaterial = EnumHelper.addToolMaterial("Sapphire", 2, 500, 12.0f, 3, 18);
 		cactusMaterial = EnumHelper.addToolMaterial("Cactus", 0, 131, 2.0f, 1, 15);
 		flintMaterial = EnumHelper.addToolMaterial("Flint", 1, 131, 6.0f, 2, 5);
 		
 		//Create actual block/item instances, register the blocks
 		createItems();
 		createBlocks();
 		registerBlocks();
 		
 		zhWorldGen = new ZHWorldGen();
 		
 		if(replaceCoal.getBoolean(true))
 		{
 			Block.blocksList[16] = null;
 			
 			Block.blocksList[16] = new ZHCoalOre(16).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreCoal");
 		}
 		
 		//Lets potions stack up to 3
     	if(potionsStack.getBoolean(true))
    		Item.potion.setMaxStackSize(maxPotionStack.getInt(3));
     	
     	if(durableShovels.getBoolean(true))
     	{
     		Item.shovelDiamond.setMaxDamage(Item.shovelDiamond.getMaxDamage() * 2);
     		Item.shovelGold.setMaxDamage(Item.shovelGold.getMaxDamage() * 2);
     		Item.shovelIron.setMaxDamage(Item.shovelIron.getMaxDamage() * 2);
     		Item.shovelStone.setMaxDamage(Item.shovelStone.getMaxDamage() * 2);
     		Item.shovelWood.setMaxDamage(Item.shovelWood.getMaxDamage() * 2);
     		sapphireShovel.setMaxDamage(sapphireShovel.getMaxDamage() * 2);
     		if(cactusTools.getBoolean(true))
     			cactusShovel.setMaxDamage(cactusShovel.getMaxDamage() * 2);
     		if(flintTools.getBoolean(true))
     			flintShovel.setMaxDamage(flintShovel.getMaxDamage() * 2);
     	}
     	
     	if(durableAxes.getBoolean(true))
     	{
     		Item.axeDiamond.setMaxDamage(Item.axeDiamond.getMaxDamage() * 2);
     		Item.axeGold.setMaxDamage(Item.shovelGold.getMaxDamage() * 2);
     		Item.axeIron.setMaxDamage(Item.axeIron.getMaxDamage() * 2);
     		Item.axeStone.setMaxDamage(Item.axeStone.getMaxDamage() * 2);
     		Item.axeWood.setMaxDamage(Item.axeWood.getMaxDamage() * 2);
     		sapphireAxe.setMaxDamage(sapphireAxe.getMaxDamage() * 2);
     		if(cactusTools.getBoolean(true))
     			cactusAxe.setMaxDamage(cactusAxe.getMaxDamage() * 2);
     		if(flintTools.getBoolean(true))
     			flintAxe.setMaxDamage(flintAxe.getMaxDamage() * 2);
     	}
 		
 		//Add some additional seeds to drop from grass
 		addGrassSeeds();
 		//Add some additional plants to grow when bonemeal is used on grass
 		addGrassPlants();
 		
 		//Register my items to the ore dictionary as needed
 		addOreDictionary();
 		
 		//Add World Generation
 		GameRegistry.registerWorldGenerator(zhWorldGen);
 		
 		//Add new recipes
 		addRecipes();
 		
 		//Something that's required, I imagine?
 		proxy.registerRenderers();
     }
     
     private void addGrassPlants() 
     {
 		MinecraftForge.addGrassPlant(flaxCrop, 7, 8);
 	}
 
 	private void createItems() 
     {
 		//New foods
 		ribsRaw = new ZHRibsItem(config.getItem("rawRibs", itemID).getInt(),4,0.3f,true).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabFood).setUnlocalizedName("ribsRaw");
 		ribsCooked = new ZHRibsItem(config.getItem("cookedRibs", itemID + 1).getInt(),10,0.8f,true).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabFood).setUnlocalizedName("ribsCooked");
 		drumstickRaw = new ZHRibsItem(config.getItem("rawDrumstick",itemID + 2).getInt(),1,0.2f,true).setPotionEffect(Potion.hunger.id, 30, 0, 0.3F).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabFood).setUnlocalizedName("drumstickRaw");
 		drumstickCooked = new ZHRibsItem(config.getItem("cookedDrumstick",itemID + 3).getInt(),4,0.6f,true).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabFood).setUnlocalizedName("drumstickCooked");
 		muttonRaw = new ZHFood(config.getItem("rawMutton", itemID + 4).getInt(),3,0.3f,true).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabFood).setUnlocalizedName("muttonRaw");
 		muttonCooked = new ZHFood(config.getItem("cookedMutton", itemID + 5).getInt(),8,0.8f,true).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabFood).setUnlocalizedName("muttonCooked");
 		
 		//New Dyes
 		zhDyes = new ZHDye(config.getItem("dyes", itemID + 6).getInt(),0).setMaxStackSize(64).setCreativeTab(CreativeTabs.tabMaterials).setUnlocalizedName("zhDye");
 		
 		//New items
 		tar = new ZHItem(config.getItem("tar", itemID + 7).getInt()).setUnlocalizedName("tar").setCreativeTab(CreativeTabs.tabMaterials);
 		sapphireGem = (ZHItem) new ZHItem(config.getItem("sapphire", itemID + 8).getInt()).setUnlocalizedName("gemSapphire").setCreativeTab(CreativeTabs.tabMaterials);
 		salt = new ZHItem(config.getItem("salt", itemID + 9).getInt()).setUnlocalizedName("salt").setCreativeTab(CreativeTabs.tabMaterials);
 		
 		//New tools
 		sapphirePick = new ZHPickaxe(config.getItem("sapphirePick", itemID + 10).getInt(), sapphireMaterial).setUnlocalizedName("pickaxeSapphire");
 		sapphireAxe = new ZHAxe(config.getItem("sapphireHatchet", itemID + 11).getInt(), sapphireMaterial).setUnlocalizedName("hatchetSapphire");
 		sapphireHoe = new ZHHoe(config.getItem("sapphireHoe", itemID + 12).getInt(), sapphireMaterial).setUnlocalizedName("hoeSapphire");
 		sapphireShovel = new ZHShovel(config.getItem("sapphireShovel", itemID + 13).getInt(), sapphireMaterial).setUnlocalizedName("shovelSapphire");
 		sapphireSword = new ZHSword(config.getItem("sapphireSword", itemID + 14).getInt(), sapphireMaterial).setUnlocalizedName("swordSapphire");
 		
 		//flaxSeeds = itemID + 15
 		//cactusFruit = itemID + 16
 		
 		if(flintTools.getBoolean(true))
 		{
 			flintPick = new ZHPickaxe(config.getItem("flintPick", itemID + 17).getInt(), flintMaterial).setUnlocalizedName("pickaxeFlint");
 			flintAxe = new ZHAxe(config.getItem("flintHatchet", itemID + 18).getInt(), flintMaterial).setUnlocalizedName("hatchetFlint");
 			flintHoe = new ZHHoe(config.getItem("flintHoe", itemID + 19).getInt(), flintMaterial).setUnlocalizedName("hoeSapphire");
 			flintShovel = new ZHShovel(config.getItem("flintShovel", itemID + 20).getInt(), flintMaterial).setUnlocalizedName("shovelFlint");
 			flintSword = new ZHSword(config.getItem("flintSword", itemID + 21).getInt(), flintMaterial).setUnlocalizedName("swordFlint");	
 		}
 		
 		if(cactusTools.getBoolean(true))
 		{
 			cactusPick = new ZHPickaxe(config.getItem("cactusPick", itemID + 22).getInt(), cactusMaterial).setUnlocalizedName("pickaxeCactus");
 			cactusAxe = new ZHAxe(config.getItem("cactusHatchet", itemID + 23).getInt(), cactusMaterial).setUnlocalizedName("hatchetCactus");
 			cactusHoe = new ZHHoe(config.getItem("cactusHoe", itemID + 24).getInt(), cactusMaterial).setUnlocalizedName("hoeCactus");
 			cactusShovel = new ZHShovel(config.getItem("cactusShovel", itemID + 25).getInt(), cactusMaterial).setUnlocalizedName("shovelCactus");
 			cactusSword = new ZHSword(config.getItem("cactusSword", itemID + 26).getInt(), cactusMaterial).setUnlocalizedName("swordCactus");
 		}
 		
 		swordAthame = new ZHAthame(config.getItem("athame", itemID + 27).getInt()).setUnlocalizedName("swordAthame");
 		//next id = itemID + 28
 	}
 	
 	private void createBlocks()
 	{
 		//New ores
 		zhOres = new ZHMultiOreBlock(blockID,Material.rock);
 		
 		//Flax plant
 		flaxCrop = new ZHFlaxCrop(config.getBlock("flaxCrop", blockID+1).getInt()).setUnlocalizedName("flaxCrop");
 		flaxSeeds = (ZHSeeds) new ZHSeeds(config.getItem("flaxSeeds",itemID + 15).getInt(),flaxCrop.blockID,Block.tilledField.blockID).setUnlocalizedName("flaxSeeds");
 		
 		//Cactus Fruit
 		cactusFruit = new ZHFood(config.getItem("cactusFruit", itemID + 16).getInt(), 3, 0.3f, false).setUnlocalizedName("cactusFruit");
 		cactusFruitBlock = new ZHCactusFruit(config.getBlock("cactusFruitPod",blockID+2).getInt()).setUnlocalizedName("cactusFruitBlock");
 		zhCactus = new ZHCactus(config.getBlock("fruitingCactus",blockID+3).getInt()).setHardness(0.4F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("zhCactusFlower");
 		
		//next block id = blockID + 4
		
 		//Treeshroom
 		//treeShroom = new ZHTreeShroom(config.getBlock("treeShroom",blockID+3).getInt()).setUnlocalizedName("treeShroom");
 	}
 
 	private void registerBlocks() 
     {
 		//Register blocks
 		GameRegistry.registerBlock(zhOres, ZHMultiOreItemBlock.class, "zhMultiOres");
 		for(int i = 0; i < numOres; i++)
 			MinecraftForge.setBlockHarvestLevel(zhOres, i, "pickaxe", harvestLevel[i]);
 		GameRegistry.registerBlock(flaxCrop, "zhFlaxCrop");
 		GameRegistry.registerBlock(cactusFruitBlock,"zhCactusFruit");
 		GameRegistry.registerBlock(zhCactus,"zhCactus");
 		//GameRegistry.registerBlock(treeShroom,"zhTreeShroom");
 	}
 
 	private void addGrassSeeds() 
     {
 		//Adds more vanilla seeds to Grass drops
 		if(moreSeeds.getBoolean(true))
 		{
 			MinecraftForge.addGrassSeed(new ItemStack(Item.melonSeeds),8);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.pumpkinSeeds),8);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.carrot),6);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.potato),6);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.poisonousPotato),3);
 		}
 		//Adds my crops to Grass drops
 		MinecraftForge.addGrassSeed(new ItemStack(flaxSeeds),8);
 	}
 	private void addOreDictionary() 
     {
 		//Register items/blocks to OreDictionary as needed
 		OreDictionary.registerOre("dyeBlack", new ItemStack(zhDyes,1,0));
 		OreDictionary.registerOre("dyeBlue", new ItemStack(zhDyes,1,1));
 		OreDictionary.registerOre("dyeBrown", new ItemStack(zhDyes,1,2));
 		OreDictionary.registerOre("dyeGreen", new ItemStack(zhDyes,1,3));
 		OreDictionary.registerOre("dyeWhite", new ItemStack(zhDyes,1,4));
 		                        
 		OreDictionary.registerOre("oreBitumen", new ItemStack(zhOres,1,0));
 		OreDictionary.registerOre("oreSapphire", new ItemStack(zhOres,1,1));
 		OreDictionary.registerOre("oreSalt", new ItemStack(zhOres,1,2));
 		
 		OreDictionary.registerOre("gemSapphire", sapphireGem);
 	}
 
 	@PostInit
     public void postInit(FMLPostInitializationEvent event) 
     {
     	config.save();
     }
     
     public void addRecipes()
     {
 		//Create some references to some common Items used in below recipes
 		ItemStack cactus = new ItemStack(Block.cactus);
 		ItemStack leather = new ItemStack(Item.leather);
 		ItemStack ironIngot = new ItemStack(Item.ingotIron);
 		ItemStack bone = new ItemStack(Item.bone);
 		ItemStack stick = new ItemStack(Item.stick);
 		                    
 		//Get two sticks from a sapling
 		//Using addRecipe since that seemed to be the only one that accepted any (not just 0) metadata!
 		GameRegistry.addRecipe(new ItemStack(Item.stick,3), new Object [] {
 			"x",
 			'x', Block.sapling
 		});
 		
 		//Cactus tools
 		if(cactusTools.getBoolean(true))
 		{
 			//Get 4 sticks from two cactuses on top of each other (ie: like sticks from planks)
 			GameRegistry.addShapedRecipe(new ItemStack(Item.stick,4),
 				"x",
 				"x",
 				'x',cactus
 			);
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusSword), new Object[]{
 				"x",
 				"x",
 				"y",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusShovel), new Object[]{
 				"x",
 				"y",
 				"y",
 				'x',cactus,
 				'y',stick
 			}));
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusAxe), new Object[]{
 				"xx",
 				"xy",
 				" y",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusHoe), new Object[]{
 				"xx",
 				"y ",
 				"y ",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusAxe), new Object[]{
 				"xx",
 				"yx",
 				"y ",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusHoe), new Object[]{
 				"xx",
 				" y",
 				" y",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusPick), new Object[]{
 				"xxx",
 				" y ",
 				" y ",
 				'x',cactus,
 				'y',stick
 			}));
 		}
 		
 		//flint tools
 		if(flintTools.getBoolean(true))
 		{
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintSword), new Object[]{
 				"x",
 				"x",
 				"y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintShovel), new Object[]{
 				"x",
 				"y",
 				"y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintAxe), new Object[]{
 				"xx",
 				"xy",
 				" y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintHoe), new Object[]{
 				"xx",
 				"y ",
 				"y ",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintAxe), new Object[]{
 				"xx",
 				"yx",
 				"y ",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintHoe), new Object[]{
 				"xx",
 				" y",
 				" y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintPick), new Object[]{
 				"xxx",
 				" y ",
 				" y ",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 		}
 		
 		//Saddle from 5 leather/3 iron (a la the horse saddle recipe that is/was in 1.6)
 		GameRegistry.addShapedRecipe(new ItemStack(Item.saddle),
 			"xxx",
 			"xyx",
 			"y y",
 			'x',leather,
 			'y',ironIngot
 		);
 		    
 		//Sapphire tools
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireSword), new Object[]{
 			"x",
 			"x",
 			"y",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireShovel), new Object[]{
 			"x",
 			"y",
 			"y",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireAxe), new Object[]{
 			"xx",
 			"xy",
 			" y",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireHoe), new Object[]{
 			"xx",
 			"y ",
 			"y ",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireAxe), new Object[]{
 			"xx",
 			"yx",
 			"y ",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireHoe), new Object[]{
 			"xx",
 			" y",
 			" y",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphirePick), new Object[]{
 			"xxx",
 			" y ",
 			" y ",
 			'x',"gemSapphire",
 			'y',stick
 		}));
 		
 		
 		//New meats -> bone
 		GameRegistry.addShapelessRecipe(bone,new ItemStack(ribsRaw,1));
 		GameRegistry.addShapelessRecipe(bone,new ItemStack(drumstickRaw,1));
 		
 		//Wheat -> Seeds
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.seeds,3),new ItemStack(Item.wheat));
 		//2x Wheat -> Flour
 		//Flour + Salt + Water -> Dough
 		//Smelt Dough -> Bread
 		//Cake using Flour
 		//Disable Normal Bread/Cake recipes here
 		
 		//Uses for Tar
 		GameRegistry.addShapelessRecipe(new ItemStack(Block.pistonStickyBase),new ItemStack(tar),new ItemStack(Block.pistonBase));
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.magmaCream), new ItemStack(tar), new ItemStack(Item.blazePowder));
 		
 		//3x Gravel -> 1x Flint
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.flint),new ItemStack(Block.gravel),new ItemStack(Block.gravel),new ItemStack(Block.gravel));
 		
 		//Get one leather by smelting rotten flesh, gives about as much experience as smelting stone
 		GameRegistry.addSmelting(Item.rottenFlesh.itemID, leather, 0.1f);
 		//Same as above, using a different method that allows metadata for the input - kept for reference later
 		
 		//Cooking recipe for new meats
 		GameRegistry.addSmelting(ribsRaw.itemID,new ItemStack(ribsCooked),0.35f);
 		GameRegistry.addSmelting(muttonRaw.itemID,new ItemStack(muttonCooked),0.35f);
 		GameRegistry.addSmelting(drumstickRaw.itemID,new ItemStack(drumstickCooked),0.35f);
 		
 		//Tar Ore -> Tar
 		FurnaceRecipes.smelting().addSmelting(zhOres.blockID, 0, new ItemStack(tar,4,0), 0.7f);
 		
 		//Salt
 		GameRegistry.addSmelting(Item.bucketWater.itemID,new ItemStack(salt,3),0.3f);
 		FurnaceRecipes.smelting().addSmelting(Item.potion.itemID, 0, new ItemStack(salt), 0.1f);
     }
         
 }
