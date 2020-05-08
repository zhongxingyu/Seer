 package dark.holoprojector.common;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import net.minecraftforge.common.Configuration;
 import universalelectricity.prefab.TranslationHelper;
 import universalelectricity.prefab.network.PacketManager;
 import cpw.mods.fml.common.DummyModContainer;
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
 
 @Mod(modid = HoloProjector.NAME, name = HoloProjector.NAME, version = HoloProjector.VERSION, dependencies = "after:BasicComponents")
 @NetworkMod(channels = { HoloProjector.CHANNEL }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketManager.class)
 public class HoloProjector extends DummyModContainer 
 {
 	/* MOD STUFF  ***/
 	public static final String NAME = "HoloProjectors";
 	public static final String CHANNEL = "holoProjector";	
 	public static final String VERSION = "0.0.1";
 	
 	/* FILE PATHS ***/
 	public static final String PATH = "/dark/holoprojector/";
     public static final String RESOURCE_PATH = PATH + "resource/";
     public static final String BLOCK_TEXTURE_FILE = RESOURCE_PATH + "blocks.png";
     public static final String ITEM_TEXTURE_FILE = RESOURCE_PATH + "items.png";
     public static final String LANGUAGE_PATH = RESOURCE_PATH + "lang/";
      public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir() + "/UniversalElectricity/", NAME + ".cfg"));
 
     /* SUPPORTED LANGS ***/
     private static final String[] LANGUAGES_SUPPORTED = new String[] { "en_US" };
     
     /* DEFUALT ID START ***/   
     public final static int BLOCK_ID_PREFIX = 3100;
     public final static int ITEM_ID_PREFIX = 13200;
     
     /* SIDE DEC ***/
    @SidedProxy(clientSide = "dark.holoprojector.client.ClientProxy", serverSide = "dark.holoprojector.common.CommonProxy")
     public static CommonProxy proxy;
     
     /* INSTANCE ***/
     @Instance(NAME)
     public static HoloProjector instance;
     
     /* LOGGER ***/
     public static Logger FMLog = Logger.getLogger(NAME);
     
     
     @PreInit  /* PRE-LOAD FUNCTION ***/
     public void preInit(FMLPreInitializationEvent event)
     {
         FMLog.setParent(FMLLog.getLogger());
         FMLog.info("Initializing...");
         instance = this;        
         loadSettings(); 
         
         /* REGSITRY BLOCKS ***/
         
 
         proxy.preInit();        
     }
     
     @Init /* LOAD FUNCTION ***/
     public void Init(FMLInitializationEvent event)
     {
         FMLog.info("Loading...");
         proxy.init();
         /* REGSITRY ENTITIES ***/
         
         FMLog.info("Loaded: " + TranslationHelper.loadLanguages(LANGUAGE_PATH, LANGUAGES_SUPPORTED) + " Languages."); 
     }
     
     @PostInit /* POST-LOAD FUNCTION ***/
     public void PostInit(FMLPostInitializationEvent event)
     {
         FMLog.info("Finalizing...");
         proxy.postInit();
         
         /* REGSITRY RECIPES ***/
         
     }
     
     /* CONFIG & SETTING LOADER ***/
     public void loadSettings()
     {
     	CONFIGURATION.load();
         
         CONFIGURATION.save();
     }
 
 }
