 package ruby.bamboo;
 
 import net.minecraft.block.BlockDispenser;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import ruby.bamboo.dispenser.DispenserBehaviorBambooSpear;
 import ruby.bamboo.dispenser.DispenserBehaviorDirtySnowball;
 import ruby.bamboo.dispenser.DispenserBehaviorFireCracker;
 
 @Mod(modid = "BambooMod", name = "BambooMod",
        version = "Minecraft1.6.2 ver2.6.3")
 @NetworkMod(channels = { "B_Entity", "bamboo", "bamboo2" },
         packetHandler = NetworkHandler.class,
         connectionHandler = NetworkHandler.class)
 public class BambooCore {
     public static final String MODID = "BambooMod";
     private final boolean DEBUGMODE = false;
 
     @SidedProxy(serverSide = "ruby.bamboo.CommonProxy",
             clientSide = "ruby.bamboo.ClientProxy")
     public static CommonProxy proxy;
 
     @Instance("BambooMod")
     public static BambooCore instance;
 
     private static Config conf = new Config();
 
     public static Config getConf() {
         return conf;
     }
 
     @Mod.EventHandler
     public void preLoad(FMLPreInitializationEvent e) {
         proxy.preInit();
     }
 
     @Mod.EventHandler
     public void load(FMLInitializationEvent e) {
         proxy.init();
         proxy.registerTESTileEntity();
         registDispencer();
 
         // debug
         if (DEBUGMODE) {
             System.out.println("DEBUG MODE Enable");
         }
     }
 
     @Mod.EventHandler
     public void serverStart(FMLServerStartedEvent event) {
         getConf().serverInit();
         ManekiHandler.instance.clearManekiList();
     }
 
     @Mod.EventHandler
     public void postInit(FMLPostInitializationEvent event) {
         NetworkRegistry.instance().registerGuiHandler(BambooCore.getInstance(), new GuiHandler());
     }
 
     private void registDispencer() {
         BlockDispenser.dispenseBehaviorRegistry.putObject(BambooInit.snowBallIID, new DispenserBehaviorDirtySnowball());
         BlockDispenser.dispenseBehaviorRegistry.putObject(BambooInit.firecrackerIID, new DispenserBehaviorFireCracker());
         BlockDispenser.dispenseBehaviorRegistry.putObject(BambooInit.bambooSpearIID, new DispenserBehaviorBambooSpear());
     }
 
     public static BambooCore getInstance() {
         return instance;
     }
 }
