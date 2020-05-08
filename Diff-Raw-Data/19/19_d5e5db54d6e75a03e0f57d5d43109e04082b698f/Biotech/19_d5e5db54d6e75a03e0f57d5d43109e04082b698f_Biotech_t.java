 package gigaherz.biotech;
 
 import gigaherz.biotech.block.BasicWorkerBlockMachine;
 import gigaherz.biotech.block.BiotechBlockMachine;
 import gigaherz.biotech.block.MilkFlowingBlock;
 import gigaherz.biotech.block.MilkStillBlock;
 import gigaherz.biotech.common.CommonProxy;
 import gigaherz.biotech.item.BioCircuit;
 import gigaherz.biotech.item.BiotechItemBlock;
 import gigaherz.biotech.item.CommandCircuit;
 import gigaherz.biotech.item.RangeUpgrade;
 import gigaherz.biotech.tileentity.BasicMachineTileEntity;
 import gigaherz.biotech.tileentity.BasicWorkerTileEntity;
 import gigaherz.biotech.tileentity.CowMilkerTileEntity;
 import gigaherz.biotech.tileentity.PlantingMachineTileEntity;
 import gigaherz.biotech.tileentity.TillingMachineTileEntity;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.liquids.LiquidContainerData;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import universalelectricity.prefab.network.ConnectionHandler;
 import universalelectricity.prefab.network.PacketManager;
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
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 
 @Mod(modid = "Biotech", name = "Biotech", version = "0.1.7")
 @NetworkMod(channels = Biotech.CHANNEL, clientSideRequired = true, serverSideRequired = false, connectionHandler = ConnectionHandler.class, packetHandler = PacketManager.class)
 
 public class Biotech
 {
     // The instance of your mod that Forge uses.
     @Instance("Biotech")
     public static Biotech instance;
 	
     // Texture file paths
 	public static final String FILE_PATH = "/resources/biotech/textures/";
 	public static final String BLOCK_TEXTURE_FILE = FILE_PATH + "block.png";
 	public static final String ITEM_TEXTURE_FILE = FILE_PATH + "items.png";
 
 	// Public channel used in all communication
 	public static final String CHANNEL = "Biotech";
 	
 	// First Item ID
     private final static int firstItemId = 24400;
     
     // First Block ID
     private final static int firstBlockId = 2450;
 
     // All Item ID's
     private final static int defaultBioItemId = firstItemId + 1;
     
     // All Block ID's
     private final static int defaultBiotechBlockId = firstBlockId + 1;
     
     // Default config loader
     public static final Configuration Config = new Configuration(new File(Loader.instance().getConfigDir(), "Biotech/Biotech.cfg"));
 
     // Item templates
     public static BioCircuit bioCircuit;
     // Metadata for BioCircuit
     // 0 == unprogrammed
     // 1 == wheatseeds 
     // 2 == melonseeds 
     // 3 == pumpkinseeds 
     // 4 == carrots 
     // 5 == potatoes
     
     // Tilling machine Circuits
     public static ItemStack bioCircuitEmpty;
     public static ItemStack bioCircuitWheatSeeds;
     public static ItemStack bioCircuitMelonSeeds;
     public static ItemStack bioCircuitPumpkinSeeds;
     public static ItemStack bioCircuitCarrots;
     public static ItemStack bioCircuitPotatoes;
     
     // Cow Milker Upgrades [Not yet implemented but needs to be defined for CowMilkerTileEntity to work]
     public static Item rangeUpgrade;
     
     // Block templates
     public static Block biotechBlockMachine;
     public static Block milkMoving;
     public static Block milkStill;
     
     // Metadata for biotechBlockMachine
 	//0 == Tiller
 	//1 == Foresting
 	//2 == Woodcutter
 	//3 == Crop Harvester
 	//4 == Fertilizer
 	//5 == Miner
 	//6 == Filler
     //7 == Cow Milker
     
     // Item Stack
     public static ItemStack cobbleStack = new ItemStack(Block.cobblestone);
 	public static ItemStack plankStack = new ItemStack(Block.planks);
     
     // Liquid Stack Milk
     public static LiquidStack milkLiquid;
 
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide = "gigaherz.biotech.client.ClientProxy", serverSide = "gigaherz.biotech.common.CommonProxy")
     public static CommonProxy proxy;
 
     // Chat commands
     public static Property enableChatCommand;
 
     
     // Gui Handler
     private GuiHandler guiHandler = new GuiHandler();
 
     // Biotech's own CreativeTab
     public static BiotechCreativeTab tabBiotech = new BiotechCreativeTab();
     
     // The logger for Biotech
     public static Logger biotechLogger = Logger.getLogger("Biotech");
     
     
     @PreInit
     public void preInit(FMLPreInitializationEvent event)
     {
     	biotechLogger.setParent(FMLLog.getLogger());
     	biotechLogger.info("Starting Biotech");
     	biotechLogger.info("Loading config");
     	Config.load();
         Property prop;
         
 		/**
 		 * Define the items and blocks.
 		 */
         this.bioCircuit = new BioCircuit(Config.getItem("gigaherz.biotech.BioCircuit", defaultBioItemId).getInt());
         
         this.biotechBlockMachine = new BiotechBlockMachine(Config.getBlock("gigaherz.biotech.BiotechBlock", defaultBiotechBlockId).getInt(), 1).setHardness(0.5F).setStepSound(Block.soundMetalFootstep);
        this.milkMoving = new MilkFlowingBlock(Config.getBlock("gigaherz.biotech.MilkFlowing", defaultBiotechBlockId + 2).getInt(), 4);
        this.milkStill = new MilkStillBlock(Config.getBlock("gigaherz.biotech.MilkStill", defaultBiotechBlockId + 3).getInt(), 4);
         this.rangeUpgrade = new RangeUpgrade(Config.getItem("gigaherz.biotech.RangeUpgrade", defaultBioItemId + 1).getInt());
         
 		/**
 		 * Define the subitems
 		 */
         this.bioCircuitEmpty = bioCircuit.getStack(1, 0);
         this.bioCircuitWheatSeeds = bioCircuit.getStack(1, 1);
         this.bioCircuitMelonSeeds = bioCircuit.getStack(1, 2);
         this.bioCircuitPumpkinSeeds = bioCircuit.getStack(1, 3);
         this.bioCircuitCarrots = bioCircuit.getStack(1, 4);
         this.bioCircuitPotatoes = bioCircuit.getStack(1, 5);
 
 		/**
 		 * Enable the chat commands
 		 */
         Property enableChatCommand = Config.get("general", "enableChatCommand", true);
         
         //guiHandler.preInit();
         
         Config.save();
         
         biotechLogger.info("Config loaded");
     }
 
     
     public static void initCommands(FMLServerStartingEvent event) 
     {
     if (enableChatCommand.value == "true") 
 		{
 			event.registerServerCommand(new CmdWorker());
 			biotechLogger.info("Biotech Command Enabled");
 		}
     }
     
     @Init
     public void load(FMLInitializationEvent event)
     {
     	proxy.registerRenderers();
 
 		/**
 		 * Handle the items that will be used in recipes.
 		 */
     	ItemStack itemBasicCircuit =  new ItemStack(OreDictionary.getOreID("basicCircuit"), 1, 0);
     	ItemStack itemMotor =  new ItemStack(OreDictionary.getOreID("motor"), 1 , 0);
     	ItemStack itemBronzePlate =  new ItemStack(OreDictionary.getOreID("plateBronze"), 1 , 0);
     	ItemStack itemChest = new ItemStack(Block.chest, 1);
 
 		/**
 		 * Register the TileEntity's
 		 */
         GameRegistry.registerTileEntity(BasicMachineTileEntity.class, "BasicMachineTileEntity");
         
         GameRegistry.registerTileEntity(PlantingMachineTileEntity.class, "PlantingMachineTileEntity");
         
         GameRegistry.registerTileEntity(TillingMachineTileEntity.class, "TillingMachineTileEntity");
         
         GameRegistry.registerTileEntity(CowMilkerTileEntity.class, "CowMilkerTileEntity");
         
         /**
          * Register Milk as a Liquid
          */
         milkLiquid = LiquidDictionary.getOrCreateLiquid("Milk", new LiquidStack(milkStill, 1));
         LiquidContainerRegistry.registerLiquid(new LiquidContainerData(LiquidDictionary.getLiquid("Milk", LiquidContainerRegistry.BUCKET_VOLUME),
 				new ItemStack(Item.bucketMilk), new ItemStack(Item.bucketEmpty)));
         
 		/**
 		 * Handle the blocks
 		 */
         GameRegistry.registerBlock(Biotech.biotechBlockMachine, BiotechItemBlock.class, "Basic Biotech Block");
         GameRegistry.registerBlock(Biotech.milkMoving, "Milk(Flowing)");
         GameRegistry.registerBlock(Biotech.milkStill, "Milk(Still)");
         
         //GameRegistry.registerBlock(biotechBlockMachine, "BiotechMachine");
         // Registration
         
 		/**
 		 * Handle localization and add names for all items
 		 */
         //LanguageRegistry.addName(biotechBlockMachine, "Biotech Machine");
         
         //Blocks
         
 		LanguageRegistry.addName(milkMoving, "Milk(Flowing)");
 		LanguageRegistry.addName(milkStill, "Milk(Still)");
 		
 		// Items
 		LanguageRegistry.addName(bioCircuit, "Bio Circuit");
 		LanguageRegistry.addName(rangeUpgrade, "Range Upgrade");
 		// Subitems
         LanguageRegistry.addName(bioCircuitEmpty, "Bio Circuit - Empty");
         LanguageRegistry.addName(bioCircuitWheatSeeds, "Bio Circuit - Wheat Seeds");
         LanguageRegistry.addName(bioCircuitMelonSeeds, "Bio Circuit - Melon Seeds");
         LanguageRegistry.addName(bioCircuitPumpkinSeeds, "Bio Circuit - Pumpkin Seeds");
         LanguageRegistry.addName(bioCircuitCarrots, "Bio Circuit - Carrots");
         LanguageRegistry.addName(bioCircuitPotatoes, "Bio Circuit - Potaties");
 
         // Subblocks
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.0.name", "Tilling Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.1.name", "Planting Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.2.name", "Woodcutter Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.3.name", "Harvesting Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.4.name", "Fertilizing Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.5.name", "Mining Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.6.name", "Filling Machine");
         LanguageRegistry.instance().addStringLocalization("tile.BiotechBlockMachine.7.name", "Cow Milker");
         
         //CreativeTab
         LanguageRegistry.instance().addStringLocalization("itemGroup.tabBiotech", "Biotech");
         
         /*
          * Recipes will be added after the rest works a bit better need to think of some more recipes as well!
          
         // Recipes
         ItemStack[] bcCircuits =
         {
         		itemBasicCircuit,
         		itemBasicCircuit,
         		itemBasicCircuit,
         };
         
         bcCircuits[0].setItemDamage(0); //Basic Circuit
         bcCircuits[1].setItemDamage(1); //Advanced Circuit
         bcCircuits[2].setItemDamage(2); //Elite Circuit
         biotechLogger.info("Loading recipes");
         GameRegistry.addShapelessRecipe(planter, bcCircuits[0], Item.seeds);
         GameRegistry.addShapelessRecipe(planter, bcCircuits[0], Item.melonSeeds);
         GameRegistry.addShapelessRecipe(planter, bcCircuits[0], Item.pumpkinSeeds);
         GameRegistry.addShapelessRecipe(planter, bcCircuits[0], Item.netherStalkSeeds);
         GameRegistry.addShapelessRecipe(planter, bcCircuits[0], Block.cactus);
         GameRegistry.addShapelessRecipe(planter, bcCircuits[0], Item.reed);
         GameRegistry.addShapelessRecipe(harvester, bcCircuits[0], Item.wheat);
         GameRegistry.addShapelessRecipe(woodcutter, bcCircuits[0], new ItemStack(Block.sapling, 1, -1));
         GameRegistry.addShapelessRecipe(fertilizer, bcCircuits[1], new ItemStack(Item.dyePowder, 1, 15));
         GameRegistry.addShapelessRecipe(tiller, bcCircuits[1], Block.dirt);
         GameRegistry.addShapelessRecipe(miner, bcCircuits[2], Block.stone);
         GameRegistry.addShapelessRecipe(filler, bcCircuits[2], Block.dirt);
         //1x Chest, 1x Circuit, 1x Motor, 1x Tier material, Plating?
         
 
         GameRegistry.addRecipe(new ItemStack(basicWorker, 1),
                 "ddd",
                 "bcb",
                 "dad",
                 'a', itemChest,
                 'b', itemBasicCircuit,
                 'c', itemMotor,
                 'd', itemBronzePlate
         );
         */
         
         NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
     }
 
     @PostInit
     public void postInit(FMLPostInitializationEvent event)
     {
     	biotechLogger.info("Biotech fully loaded");
     }
 }
