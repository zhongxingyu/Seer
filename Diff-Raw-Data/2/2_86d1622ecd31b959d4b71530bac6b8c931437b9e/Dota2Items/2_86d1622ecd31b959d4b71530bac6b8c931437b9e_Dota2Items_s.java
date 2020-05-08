 package hunternif.mc.dota2items;
 
 import hunternif.mc.dota2items.client.gui.GuiHandler;
 import hunternif.mc.dota2items.config.Config;
 import hunternif.mc.dota2items.config.ConfigLoader;
 import hunternif.mc.dota2items.core.BowHandler;
 import hunternif.mc.dota2items.core.Dota2PlayerTracker;
 import hunternif.mc.dota2items.core.Mechanics;
 import hunternif.mc.dota2items.core.PlayerAttackedHandler;
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
 	public static final String ID = "Dota2Items";
 	public static final String NAME = "Dota 2 Items";
	public static final String VERSION = "0.6.3";
 	public static final String CHANNEL = ID;
 	
 	public static CreativeTabs dota2CreativeTab;
 	public static List<Item> itemList = new ArrayList<Item>();
 	
 	public static final Dota2PlayerTracker playerTracker = new Dota2PlayerTracker();
 	public static final Mechanics mechanics = new Mechanics();
 	public static final ShopSpawner shopSpawner = new ShopSpawner();
 	
 	@Instance(ID)
 	public static Dota2Items instance;
 	
 	public static Logger logger;
 	
 	@SidedProxy(clientSide="hunternif.mc.dota2items.ClientProxy", serverSide="hunternif.mc.dota2items.CommonProxy")
 	public static CommonProxy proxy;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		logger = event.getModLog();
 		proxy.registerSounds();
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		ConfigLoader.preLoad(config, Config.class);
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		dota2CreativeTab = new Dota2CreativeTab("dota2ItemTab");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.dota2ItemTab", "en_US", "Dota 2 Items");
 		
 		ConfigLoader.load(Config.class);
 		
 		InventoryShop.populate();
 		
 		GameRegistry.registerTileEntity(TileEntityCyclone.class, "Cyclone");
 		
 		int shopkeeperId = EntityRegistry.findGlobalUniqueEntityId();
 		EntityRegistry.registerGlobalEntityID(EntityShopkeeper.class, "Dota2Shopkeeper", shopkeeperId, 0x52724e, 0xc8aa64);
 		EntityRegistry.registerModEntity(EntityShopkeeper.class, "Dota2Shopkeeper", shopkeeperId, instance, 80, 3, true);
 		LanguageRegistry.instance().addStringLocalization("entity.Dota2Shopkeeper.name", "en_US", "Dota 2 Shopkeeper");
 		
 		shopSpawner.init();
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		
 		proxy.registerRenderers();
 		proxy.registerTickHandlers();
 		MinecraftForge.EVENT_BUS.register(mechanics);
 		MinecraftForge.EVENT_BUS.register(shopSpawner);
 		MinecraftForge.EVENT_BUS.register(new PlayerAttackedHandler());
 		MinecraftForge.EVENT_BUS.register(new BowHandler());
 		GameRegistry.registerPlayerTracker(playerTracker);
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 	}
 }
