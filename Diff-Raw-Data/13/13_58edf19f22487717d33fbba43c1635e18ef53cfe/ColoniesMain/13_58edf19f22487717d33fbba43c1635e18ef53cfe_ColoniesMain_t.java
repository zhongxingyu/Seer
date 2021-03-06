 package colonies.anglewyrm.src;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import colonies.vector67.src.BlockColoniesChest;
 import colonies.vector67.src.TileEntityColoniesChest;
 import net.minecraft.src.BiomeGenBase;
 import net.minecraft.src.Block;
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EnumCreatureType;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.SoundManager;
 import net.minecraftforge.common.MinecraftForge;
 import colonies.lohikaarme.src.ItemMeasuringTape;
 import colonies.vector67.src.BlockColoniesChest;
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
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "Colonies", name = "Colonies", version = "7 Dec 2012")
 @NetworkMod(
         channels = { "Colonies" },
         clientSideRequired = true,
         serverSideRequired = false,
         packetHandler = PacketHandler.class )
 
 public class ColoniesMain 
 {
 	public static Block test; 	
 	public static Item MeasuringTape;
 	public static Block chestBlock;
 	public static Block townHall;
 	
 	public static List<TownHall> townsList;
 	
 
 	@Instance
 	public static ColoniesMain instance;
 
 	@SidedProxy(clientSide = "colonies.anglewyrm.src.ClientProxy", serverSide = "colonies.anglewyrm.src.ServerProxy")
 	public static ServerProxy proxy;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		System.out.println("Initializing Colonies"); 
 		ConfigFile.load();
 		MinecraftForge.EVENT_BUS.register(new ColoniesSoundManager());
 	}
 
 	@Init
 	public void init(FMLInitializationEvent evt)
 	{	
 		registerColoniesStuff(); // at bottom of this file for legibility
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent evt)
 	{
 	    // TODO: Add Post-Initialization code such as mod hooks
 	}
 	
 	public String Version(){
 		return "Pre-Alpha, Revision 3";
 	}
 	
 	// Register Colonies stuff with Minecraft Forge
 	private void registerColoniesStuff()
 	{
 		// List of towns
 		// TODO: find a way to save/load this data structure
 		townsList = new ArrayList<TownHall>();
 		
 		// Chest block
 		chestBlock = new BlockColoniesChest(ConfigFile.parseInt("DefaultChestID"));
 		LanguageRegistry.addName(chestBlock, "Colonies Chest");
 		GameRegistry.registerBlock(chestBlock);
 
 		GameRegistry.registerTileEntity(TileEntityColoniesChest.class, "Colonies Chest TileEntity");
 		LanguageRegistry.instance().addStringLocalization("Colonies Chest TileEntity" + ".name", "en_US", "Colonies Chest TileEntity");
 		proxy.registerTileEntitySpecialRenderer(TileEntityColoniesChest.class);
 		
 		// Town Hall
 		townHall = new TownHall(ConfigFile.parseInt("TownHallID"),townsList);
 		LanguageRegistry.addName(townHall, "Town Hall");
 		GameRegistry.registerBlock(townHall);
 		GameRegistry.addRecipe( new ItemStack(townHall), new Object[]{
 			"BIB","ICI","BIB", 'B',Item.book, 'I',Item.ingotIron, 'C',BlockContainer.chest});
 		
 		// Measuring tape
 		MeasuringTape = new ItemMeasuringTape(ConfigFile.parseInt("MeasuringTape")).setItemName("Measuring Tape");
 		LanguageRegistry.addName(MeasuringTape,"Measuring Tape");
 		GameRegistry.addRecipe(new ItemStack(MeasuringTape),"II", 'I',Item.ingotIron);
 		
 		// Test block
 		test = (TestBlock) new TestBlock(ConfigFile.parseInt("TestBlockID"), 3, Material.ground)
 			.setBlockName("test").setHardness(0.75f).setCreativeTab(CreativeTabs.tabBlock);
 		MinecraftForge.setBlockHarvestLevel(test, "shovel", 0);
 		LanguageRegistry.addName(test, "Test Block");
 		GameRegistry.registerBlock(test);
 
 		// Citizens
		// the three params after the class are ChanceWeight, minPackSize and maxPackSize
 		EntityRegistry.registerGlobalEntityID(EntityCitizen.class, "Citizen", ModLoader.getUniqueEntityId(), 0xCCCCFF, 0xFF4444);
		EntityRegistry.addSpawn(EntityCitizen.class, 1, 1, 3, EnumCreatureType.creature, 
				BiomeGenBase.forest, BiomeGenBase.plains, BiomeGenBase.taiga);
 		LanguageRegistry.instance().addStringLocalization("entity.Citizen.name", "en_US", "Default Citizen");
 
 		// Miner
 		EntityRegistry.registerGlobalEntityID(EntityMiner.class, "Miner", ModLoader.getUniqueEntityId(), 0xCCCCFF, 0xFF8888);
		EntityRegistry.addSpawn(EntityMiner.class, 1, 1, 3, EnumCreatureType.creature, 
			BiomeGenBase.forest, BiomeGenBase.plains, BiomeGenBase.taiga);
 		LanguageRegistry.instance().addStringLocalization("entity.Miner.name", "en_US", "Miner");
 
 		// Wife
 		EntityRegistry.registerGlobalEntityID(EntityWife.class, "Wife", ModLoader.getUniqueEntityId(), 0xCCCCFF, 0xFFcccc);
		EntityRegistry.addSpawn(EntityWife.class, 1, 1, 3, EnumCreatureType.creature, 
			BiomeGenBase.forest, BiomeGenBase.plains, BiomeGenBase.taiga);
 		LanguageRegistry.instance().addStringLocalization("entity.Wife.name", "en_US", "Wife");
 		
 		proxy.registerRenderInformation();		
 	}
 }
