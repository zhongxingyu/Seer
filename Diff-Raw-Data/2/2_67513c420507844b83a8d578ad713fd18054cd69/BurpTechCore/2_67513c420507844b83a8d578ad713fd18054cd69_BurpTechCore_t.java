 package burptech;
 
 import java.util.logging.Logger;
 
 import burptech.gui.GuiHandler;
 import burptech.item.crafting.*;
 import burptech.lib.*;
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.common.Mod.*;
 import cpw.mods.fml.common.event.*;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import net.minecraftforge.common.*;
 
 /**
  * BurpTech core mod but not... a core mod !( . Y . )!
  * 										     ^
  * Acatera: OMG lol  ----------------------- |
  */
 @Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VERSION)
 @NetworkMod(clientSideRequired = true)
 public class BurpTechCore
 {
 	@Instance(Constants.MOD_ID)
     public static BurpTechCore instance;
     public static Logger log = null;
     public static BurpTechConfig configuration;
     public static GuiHandler guiHandler; 
     
     @SidedProxy(clientSide = "burptech.client.ClientProxy", serverSide = "burptech.CommonProxy")
     public static CommonProxy proxy;
 
     @EventHandler
     public void preInitialization(FMLPreInitializationEvent e)
     {
         // setup logger
         log = Logger.getLogger(Constants.MOD_ID);
         configuration = BurpTechConfig.load(e.getModConfigurationDirectory());
         
         //gui handler
         guiHandler = new GuiHandler();
         
         // load up language translations
        TranslationHelper.loadLanguages("/assets/" + Constants.MOD_ID.toLowerCase() + "/languages/", new String[] { "en_US" });
         
         // register keyboard bindings
 
         // register the bronze age with BasicComponents
         burptech.integration.BasicComponentsIntegration.registerBronzeAge();
     }
     
     @EventHandler
     public void initialization(FMLInitializationEvent e)
     {
         // gui handlers
     	NetworkRegistry.instance().registerGuiHandler(instance, guiHandler);
     	
         // event handlers
     	if (configuration.enableSlimeSpawningRestrictions.getBoolean(true))
     		MinecraftForge.EVENT_BUS.register(new burptech.entity.monster.tweaks.EntitySlimeEventHandler());
     	
     	if (configuration.enableNetherSpawningRestrictions.getBoolean(true))
     		MinecraftForge.EVENT_BUS.register(new burptech.entity.monster.tweaks.EntityNetherMonsterEventHandler());
     	
     	if (configuration.enableMobsEatingOffOfGround.getBoolean(true))
     		MinecraftForge.EVENT_BUS.register(new burptech.entity.passive.tweaks.EntityAnimalEventHandler());
     	
     	if (configuration.enableMobsWandering.getBoolean(true))
     		MinecraftForge.EVENT_BUS.register(new burptech.entity.living.tweaks.EntityLivingEventHandler());
     	
     	if (configuration.enableGreedyVillagers.getBoolean(true))
     		MinecraftForge.EVENT_BUS.register(new burptech.entity.living.tweaks.EntityVillagerEventHandler());    		
     	
     	// tile entity registrations
     	
         // recipes
     	(new RecipeManager()).addRecipes();
     }
     
     @EventHandler
     public void postInitialization(FMLPostInitializationEvent e)
     {
     	// tweaks
     	if (configuration.disableEndermanGriefing.getBoolean(true))
     		burptech.entity.monster.tweaks.EntityEndermanTweaks.enableAntiGriefing();
 
     	
         // mod integrations
     	proxy.addNeiSupport();
     }
 }
