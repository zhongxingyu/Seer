 package enhancedbooks.common.core;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ari
  * Date: 02/01/13
  * Time: 2:06 AM
  */
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import enhancedbooks.client.core.ClientPacketHandler;
 
 @NetworkMod(clientSideRequired = true, serverSideRequired = true, // Whether client side and server side are needed
         clientPacketHandlerSpec = @SidedPacketHandler(channels = {"EnhancedBooks"}, packetHandler = ClientPacketHandler.class), // For clientside packet handling
         serverPacketHandlerSpec = @SidedPacketHandler(channels = {"EnhancedBooks"}, packetHandler = ServerPacketHandler.class))
 // For server-side packet handling
 
 @Mod(modid = "EnhancedBooks", name = "Enhanced Books", version = "1.0") // Forge description n stuff
 
 public class BooksCore {
 
     @Instance("EnhancedBooks")
     public static BooksCore instance = new BooksCore();
 
     @SidedProxy(clientSide = "enhancedbooks.client.core.ClientProxy", serverSide = "enhancedbooks.common.core.CommonProxy")
     //Tells Forge the location of your proxies
     public static CommonProxy proxy;
 
     // Features:
     // Bookshelves (3D!)
     // XP books
     // Hollow Books
     // Bookshelves give enchantment table stats!
 
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
         Utils.loadConfig(event);
     }
 
     @Init
    public void initBooks(FMLInitializationEvent event) { // Main init method
         proxy.registerRenderInformation();
         proxy.registerEntities();
         proxy.registerItems();
         proxy.registerTileEntities(); // Myah
         proxy.registerBlocks(); //Calls the registerBlocks method
         proxy.addNames(); // Whatever
         proxy.addRecipes(); // Etc
         NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler()); // Registers the class that deals with GUI data
     }
 }
