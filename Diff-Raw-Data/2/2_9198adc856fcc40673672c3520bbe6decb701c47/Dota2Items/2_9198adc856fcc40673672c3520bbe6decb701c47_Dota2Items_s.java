 package hunternif.mc.dota2items;
 
 import hunternif.mc.dota2items.client.gui.GuiHandler;
 import hunternif.mc.dota2items.config.Config;
 import hunternif.mc.dota2items.config.ConfigLoader;
 import hunternif.mc.dota2items.core.AttackHandler;
 import hunternif.mc.dota2items.core.BaseStatsUpdater;
 import hunternif.mc.dota2items.core.BowHandler;
 import hunternif.mc.dota2items.core.GoldHandler;
 import hunternif.mc.dota2items.core.ItemTracker;
 import hunternif.mc.dota2items.core.LivingUpdateHandler;
 import hunternif.mc.dota2items.core.StatsTracker;
 import hunternif.mc.dota2items.effect.ContinuousEffect;
 import hunternif.mc.dota2items.effect.EntityDagonBolt;
 import hunternif.mc.dota2items.effect.EntityMidasEffect;
 import hunternif.mc.dota2items.entity.EntityShopkeeper;
 import hunternif.mc.dota2items.inventory.Dota2CreativeTab;
 import hunternif.mc.dota2items.inventory.InventoryShop;
 import hunternif.mc.dota2items.network.CustomPacketHandler;
 import hunternif.mc.dota2items.tileentity.TileEntityCyclone;
 import hunternif.mc.dota2items.world.ShopSpawner;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid=Dota2Items.ID, name=Dota2Items.NAME, version=Dota2Items.VERSION, dependencies="required-after:libschematic@[1.0,)")
 @NetworkMod(clientSideRequired=true, serverSideRequired=true, packetHandler=CustomPacketHandler.class, channels={Dota2Items.CHANNEL})
 public class Dota2Items {
 	public static final String ID = "dota2items";
 	public static final String NAME = "Dota 2 Items";
 	public static final String VERSION = "@@MOD_VERSION@@";
 	public static final String CHANNEL = ID;
 	
 	public static CreativeTabs dota2CreativeTab;
 	public static List<Item> itemList = new ArrayList<Item>();
 	
 	public static final StatsTracker stats = new StatsTracker();
 	
 	@Instance(ID)
 	public static Dota2Items instance;
 	
 	public static Logger logger;
 	public static boolean debug = false;
 	
 	@SidedProxy(clientSide="hunternif.mc.dota2items.ClientProxy", serverSide="hunternif.mc.dota2items.CommonProxy")
 	public static CommonProxy proxy;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		logger = event.getModLog();
 		proxy.registerSounds();
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		debug = config.get("logging", "debug", false).getBoolean(false);
 		ConfigLoader.preLoad(config, Config.class);
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		dota2CreativeTab = new Dota2CreativeTab("dota2ItemTab");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.dota2ItemTab", "en_US", "Dota 2 Items");
 		
 		ConfigLoader.load(Config.class);
 		InventoryShop.populate();
 		
 		// ============================ Entities ===============================
 		GameRegistry.registerTileEntity(TileEntityCyclone.class, "Cyclone");
 		
 		int shopkeeperID = EntityRegistry.findGlobalUniqueEntityId();
 		EntityRegistry.registerGlobalEntityID(EntityShopkeeper.class, "Dota2Shopkeeper", shopkeeperID, 0x52724e, 0xc8aa64);
 		EntityRegistry.registerModEntity(EntityShopkeeper.class, "Dota2Shopkeeper", shopkeeperID, instance, 80, 3, true);
 		LanguageRegistry.instance().addStringLocalization("entity.Dota2Shopkeeper.name", "en_US", "Dota 2 Shopkeeper");
 		
 		int dagonBoltID = EntityRegistry.findGlobalUniqueEntityId();
 		EntityRegistry.registerGlobalEntityID(EntityDagonBolt.class, "DagonBolt", dagonBoltID);
 		EntityRegistry.registerModEntity(EntityDagonBolt.class, "DagonBolt", dagonBoltID, instance, 80, 20, false);
 		
 		int midasFxID = EntityRegistry.findGlobalUniqueEntityId();
 		EntityRegistry.registerGlobalEntityID(EntityMidasEffect.class, "MidasEffect", midasFxID);
 		EntityRegistry.registerModEntity(EntityMidasEffect.class, "MidasEffect", midasFxID, instance, 80, 20, false);
 		
 		// Register effect entities:
 		for (Class<? extends ContinuousEffect> effectClass : ContinuousEffect.buffMap.values()) {
 			// Never update the EntityWrappers because something causes them to periodically flick vertically
 			int id= EntityRegistry.findGlobalUniqueEntityId();
 			EntityRegistry.registerGlobalEntityID(effectClass, effectClass.getSimpleName(), id);
 			EntityRegistry.registerModEntity(effectClass, effectClass.getSimpleName(), id, instance, 80, 10000000, false);
 		}
 		
 		// ============================ Handlers ===============================
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		
 		proxy.registerRenderers();
 		proxy.registerTickHandlers();
 		
 		MinecraftForge.EVENT_BUS.register(stats);
 		GameRegistry.registerPlayerTracker(stats);
 		
 		LivingUpdateHandler livingUpdateHandler = new LivingUpdateHandler(stats);
 		MinecraftForge.EVENT_BUS.register(livingUpdateHandler);
 		
 		BaseStatsUpdater baseStatsUpdater = new BaseStatsUpdater();
 		livingUpdateHandler.registerEntityUpdater(baseStatsUpdater);
 		
 		ShopSpawner shopSpawner = new ShopSpawner();
 		shopSpawner.init();
 		MinecraftForge.EVENT_BUS.register(shopSpawner);
 		
 		ItemTracker itemTracker = new ItemTracker();
 		livingUpdateHandler.registerEntityUpdater(itemTracker);
 		MinecraftForge.EVENT_BUS.register(itemTracker);
 		GameRegistry.registerPlayerTracker(itemTracker);
 		
 		AttackHandler attackHandler = new AttackHandler();
 		livingUpdateHandler.registerEntityUpdater(attackHandler);
 		MinecraftForge.EVENT_BUS.register(attackHandler);
 		
 		GoldHandler goldHandler = new GoldHandler();
 		livingUpdateHandler.registerEntityUpdater(goldHandler);
 		MinecraftForge.EVENT_BUS.register(goldHandler);
 		
 		MinecraftForge.EVENT_BUS.register(new BowHandler());
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 	}
 }
