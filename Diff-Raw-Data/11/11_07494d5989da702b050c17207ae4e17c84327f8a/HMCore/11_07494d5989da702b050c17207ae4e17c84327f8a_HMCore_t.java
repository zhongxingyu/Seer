 
 package hawksmachinery.core.common;
 
 import hawksmachinery.core.common.block.*;
 import hawksmachinery.core.common.item.*;
 import hawksmachinery.core.common.tileentity.*;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.ForgeChunkManager;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import universalelectricity.prefab.ore.OreGenerator;
 import updatemanager.client.ModReleaseType;
 import updatemanager.client.ModType;
 import updatemanager.common.ModConverter;
 import updatemanager.common.UpdateManager;
 import updatemanager.common.UpdateManagerMod;
 import updatemanager.common.checking.CheckingMethod;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 
 /**
  * 
  * The mod file for Hawk's Machinery.
  * 
  * @author Elusivehawk
  */
 @Mod(modid = "HawksMachinery", name = "Hawk's Core", version = HMCore.VERSION, useMetadata = true)
 @NetworkMod(channels = {"HawksMachinery"}, clientSideRequired = true, serverSideRequired = false)
 public class HMCore
 {
 	public static final String VERSION = "Beta v1.0.0";
 	
 	@Instance("HawksMachinery")
 	private static HMCore INSTANCE;
 	
 	public HMManager MANAGER;
 	
 	public CreativeTabs tab = new HMCreativeTab("HMTab");
 	
 	public static Block ore;
 	public static Block metalBlock;
 	public static Block endiumChunkloader;
 	public static Block technicalBlock;
 	
 	public static Item plating;
 	public static Item ingots;
 	
 	public static final String GUI_PATH = "/hawksmachinery/core/client/resources/gui";
 	public static final String BLOCK_TEXTURE_FILE = "/hawksmachinery/core/client/resources/textures/blocks.png";
 	public static final String ITEM_TEXTURE_FILE = "/hawksmachinery/core/client/resources/textures/items.png";
 	public static final String TEXTURE_PATH = "/hawksmachinery/core/client/resources/textures";
 	public static final String SOUND_PATH = "/hawksmachinery/core/client/resources/sounds";
 	public static final String LANG_PATH = "/hawksmachinery/core/client/resources/lang";
	public static final String[] SUPPORTED_LANGS = new String[]{"en_US"};
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		INSTANCE = this;
 		this.MANAGER = new HMManager();
 		
 		this.MANAGER.loadConfig();
 		
 		ore = new HMBlockOre(this.MANAGER.getBlockID("Ore", 2551));
 		endiumChunkloader = new HMBlockEndiumChunkloader(this.MANAGER.getBlockID("Endium Chunkloader", 2553));
 		metalBlock = new HMBlockMetalStorage(this.MANAGER.getBlockID("Metal Block", 2556));
 		technicalBlock = new HMBlockMulti(this.MANAGER.getBlockID("Star Forge Technical", 2558));
 		
 		plating = new HMItemPlating(this.MANAGER.getItemID("Plating", 24154)).setMaxDmg(1);
 		ingots = new HMItemIngots(this.MANAGER.getItemID("Ingots", 24157)).setMaxDmg(1);
 		
 		GameRegistry.registerCraftingHandler(this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(0, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(2, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(3, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(4, this.MANAGER);
 		ForgeChunkManager.setForcedChunkLoadingCallback(this, this.MANAGER);
 		MinecraftForge.EVENT_BUS.register(this.MANAGER);
 		OreGenerator.addOre(new HMEndiumOreGen());
 		OreGenerator.addOre(new HMCobaltOreGen());
 		
 		GameRegistry.registerTileEntity(HMTileEntityMulti.class, "HMMulti");
 		GameRegistry.registerTileEntity(HMTileEntityEndiumChunkloader.class, "HMChunkloader");
 		
 		GameRegistry.registerBlock(ore, HMItemBlockOre.class);
 		GameRegistry.registerBlock(metalBlock, HMItemBlockMetalStorage.class);
 		GameRegistry.registerBlock(endiumChunkloader, HMItemBlockEndium.class);
 		
 		MinecraftForge.setBlockHarvestLevel(endiumChunkloader, "pickaxe", 3);
 		
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		new HMUpdateHandler(ModConverter.getMod(this.getClass()));
		
		for (String lang : SUPPORTED_LANGS)
		{
			LanguageRegistry.instance().loadLocalization(LANG_PATH + "/" + lang + ".txt", lang, false);
			
		}
 		
 	}
 	
 	@PostInit
 	public void modsLoaded(FMLPostInitializationEvent event)
 	{
 		
 	}
 	
 	public void loadRecipes()
 	{
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMCore.metalBlock, 1, 0), new Object[]{"EEE", "EEE", "EEE", 'E', "ingotEndium"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMCore.metalBlock, 1, 1), new Object[]{"CCC", "CCC", "CCC", 'C', "ingotCobalt"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMCore.plating, 1, 0), new Object[]{"MM", "MM", 'M', "ingotEndium"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMCore.plating, 1, 1), new Object[]{"MM", "MM", 'M', "ingotCobalt"}));
 		GameRegistry.addShapelessRecipe(new ItemStack(HMCore.ingots, 9, 0), new ItemStack(HMCore.metalBlock, 1, 0));
 		GameRegistry.addShapelessRecipe(new ItemStack(HMCore.ingots, 9, 1), new ItemStack(HMCore.metalBlock, 1, 1));
 		
 		FurnaceRecipes.smelting().addSmelting(HMCore.plating.itemID, 0, new ItemStack(HMCore.ingots, 4, 0), 0.0F);
 		FurnaceRecipes.smelting().addSmelting(HMCore.plating.itemID, 1, new ItemStack(HMCore.ingots, 4, 1), 0.0F);
 		
 	}
 	
 	public static HMCore instance()
 	{
 		return INSTANCE;
 	}
 	
 	public class HMUpdateHandler extends UpdateManagerMod
 	{
 		public HMUpdateHandler(Mod m)
 		{
 			super(m);
 			
 		}
 		
 		@Override
 		public String getModURL()
 		{
 			return "http://bit.ly/TFrC6z";
 		}
 		
 		@Override
 		public String getUpdateURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachineryVersion.txt";
 		}
 		
 		@Override
 		public ModType getModType()
 		{
 			return ModType.ADDON;
 		}
 		
 		@Override
 		public String getModName()
 		{
 			return "Hawk's Machinery";
 		}
 		
 		@Override
 		public String getChangelogURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachinery" + VERSION.replace(".", "").replace(" ", "") + "Changelog.txt";
 		}
 		
 		@Override
 		public String getDirectDownloadURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachneryDLLink.txt";
 		}
 		
 		@Override
 		public String getDisclaimerURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachineryDisclaimer.txt";
 		}
 		
 		@Override
 		public boolean enableAutoDownload()
 		{
 			return MANAGER.enableAutoDL;
 		}
 		
 		@Override
 		public CheckingMethod getCheckingMethod()
 		{
 			return CheckingMethod.LEXICOGRAPHICAL;
 		}
 		
 		@Override
 		public boolean disableChecks()
 		{
 			return UpdateManager.isDebug ? true : !MANAGER.enableUpdateChecking;
 		}
 		
 		@Override
 		public boolean srcOnly()
 		{
 			return false;
 		}
 		
 		@Override
 		public ModReleaseType getReleaseType()
 		{
 			return ModReleaseType.BETA;
 		}
 		
 		@Override
 		public ItemStack getIconStack()
 		{
 			return new ItemStack(ingots);
 		}
 		
 	}
 	
 }
