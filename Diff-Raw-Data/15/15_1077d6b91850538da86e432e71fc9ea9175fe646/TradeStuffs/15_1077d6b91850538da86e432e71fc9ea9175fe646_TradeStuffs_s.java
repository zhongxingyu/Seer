 /**
  * CCM Modding, ModJam
  */
 package ccm.trade_stuffs;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraftforge.common.MinecraftForge;
 import ccm.trade_stuffs.blocks.ModBlocks;
 import ccm.trade_stuffs.configuration.Config;
 import ccm.trade_stuffs.items.ModItems;
 import ccm.trade_stuffs.proxy.CommonProxy;
 import ccm.trade_stuffs.utils.handler.EntityHandler;
 import ccm.trade_stuffs.utils.handler.ItemHandler;
<<<<<<< HEAD
 import ccm.trade_stuffs.utils.handler.PlayerStalker;
 import ccm.trade_stuffs.utils.handler.WorldHandler;
>>>>>>> 3cbc447... Made bank saving global
 import ccm.trade_stuffs.utils.lib.Archive;
 import ccm.trade_stuffs.utils.lib.Locations;
 import ccm.trade_stuffs.utils.registry.CoinAdditionRegistry;
 import ccm.trade_stuffs.utils.registry.SackAdditionRegistry;
 
 /**
  * TradeStuffs
  * 
  * @author Captain_Shadows
  */
 @Mod(modid = Archive.MOD_ID,
      name = Archive.MOD_NAME,
      version = Archive.MOD_VERSION)
 @NetworkMod(clientSideRequired = true,
             serverSideRequired = false)
 public class TradeStuffs
 {
     @Instance(Archive.MOD_ID)
     public static TradeStuffs instance;
 
     @SidedProxy(serverSide = Locations.SERVER_PROXY,
                 clientSide = Locations.CLIENT_PROXY)
     public static CommonProxy proxy;
 
     public MinecraftServer    server;
 
     @EventHandler
     public void preInit(final FMLPreInitializationEvent event)
     {
         Config.load(event);
 
         ModBlocks.init();
 
         ModItems.init();
 
         CoinAdditionRegistry.addCoins();
     }
 
     @EventHandler
     public void init(final FMLInitializationEvent event)
     {
         NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 
         proxy.registerTileEntitys();
 
         proxy.initRenderingStuffs();
 
         final PlayerStalker playerStalker = new PlayerStalker();
 
         MinecraftForge.EVENT_BUS.register(new ItemHandler());
         MinecraftForge.EVENT_BUS.register(new EntityHandler());
         MinecraftForge.EVENT_BUS.register(new WorldHandler());
         MinecraftForge.EVENT_BUS.register(playerStalker);
 
         GameRegistry.registerPlayerTracker(playerStalker);
 
         CoinAdditionRegistry.addMobDrops();
 
         SackAdditionRegistry.addMobDrops();
     }
 
     @EventHandler
     public void postInit(final FMLPostInitializationEvent event)
     {
 
     }
 
     @EventHandler
     public void serverStarting(final FMLServerStartingEvent event)
     {
         server = event.getServer();
     }
 }
