 package monnef.autoshutdown;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.TickRegistry;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ICommandManager;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.ServerCommandManager;
 import net.minecraftforge.common.Configuration;
 
 @Mod(modid = "autoshutdown", name = mod_autoshutdown.Name, version = "0.1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
 public class mod_autoshutdown {
     public static final int minimumCountedMinutes = 2;

    @Mod.Instance("autoshutdown")
     public static mod_autoshutdown instance;
 
     @SidedProxy(clientSide = "monnef.autoshutdown.ClientProxy", serverSide = "monnef.autoshutdown.CommonProxy")
     public static CommonProxy proxy;
 
     public final static String Name = "Auto-Shutdown";
 
     private static int shutdownAfterXMinutes = Integer.MAX_VALUE;
     public static int minutesServerIsDead = 0;
     public static boolean doSave = true;
     public static boolean active = true;
     private static MinecraftServer server;
 
     public static int getShutdownAfterXMinutes() {
         return shutdownAfterXMinutes;
     }
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         server = ModLoader.getMinecraftServerInstance();
 
         Configuration config = new Configuration(event.getSuggestedConfigurationFile());
         config.load();
 
         shutdownAfterXMinutes = config.get("general", "minutes", 10).getInt();
         if (shutdownAfterXMinutes < minimumCountedMinutes) {
             println("Too small \"minutes\" value in config. Correcting shutdown timer to " + minimumCountedMinutes + "m.");
             shutdownAfterXMinutes = minimumCountedMinutes;
         }
 
         doSave = config.get("general", "force save", true).getBoolean(true);
         active = config.get("general", "enabled", true).getBoolean(true);
         config.save();
     }
 
     @Mod.Init
     public void load(FMLInitializationEvent event) {
         TickRegistry.registerScheduledTickHandler(new MinuteTicker(), Side.SERVER);
 
         println("Initialized");
         println(getStatus());
     }
 
     @Mod.PostInit
     public void postInit(FMLPostInitializationEvent event) {
     }
 
     public static void println(String msg) {
         String str = "[" + Name + "] " + msg;
         if (server == null) {
             System.out.println(str);
         } else {
             server.logInfo(str);
         }
     }
 
     @Mod.ServerStarting
     public void serverStarting(FMLServerStartingEvent event) {
         if (server == null) server = ModLoader.getMinecraftServerInstance();
         ICommandManager commandManager = server.getCommandManager();
         ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
         addCommands(serverCommandManager);
     }
 
     private void addCommands(ServerCommandManager manager) {
         manager.registerCommand(new CommandAutoShutdown());
     }
 
     public static String getStatus() {
         return "status: " + stringFromBool(active) + "; " +
                 minutesServerIsDead + "/" + getShutdownAfterXMinutes() +
                 "; save: " + stringFromBool(doSave);
     }
 
     public static String stringFromBool(boolean input) {
         return input ? "ON" : "OFF";
     }
 
 }
