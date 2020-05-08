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
 
 

    // TODO: Bookshelves with any woodtype, look at dye recipe for getting itemblock IDS. -- FAR FUTURE

    // TODO: Atlases
    // TODO: Paper mill - logs, seeds, wheat, string, makes paper.
    // TODO: Printing Press - dupes written books with paper, leather, ink
    // TODO: Flying books n shit -- HALF DONE, ADDED THE MOB!!  NOW SPAWNING AND AI AND SHIT GO
    // TODO: A placeable entity item, library ladder, a 3/4 block high entity which can be shoved side to side in a
    // line from the direction it is placed in, can be climbed.
    // (could possibly be done with modified minecarts and rails)

     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
         Utils.loadConfig(event);
     }
 
     @Init
    public void InitBooks(FMLInitializationEvent event) { // Main init method
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
