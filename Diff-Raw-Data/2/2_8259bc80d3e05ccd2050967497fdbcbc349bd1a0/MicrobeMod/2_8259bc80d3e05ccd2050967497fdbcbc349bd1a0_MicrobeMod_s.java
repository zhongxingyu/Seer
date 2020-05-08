 package meew0.microbes;
 
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import meew0.microbes.items.ItemCottonSwab;
 import meew0.microbes.items.MicrobeGenericItem;
 import meew0.microbes.proxy.CommonProxy;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.*;
 import cpw.mods.fml.common.event.*;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = MicrobeMod.id, name = MicrobeMod.modName, version = MicrobeMod.modVersion)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class MicrobeMod {
     // Random variables
     public static final String id = "Microbes";
     public static final String modName = "Microbes";
     public static final String modVersion = "0.1a";
     
     // Items
     
     public static Item itemAgar;
     public static Item itemEmptyAgarPlate;    
     public static Item itemCottonSwab;
     // Blocks
     
     @SidedProxy(serverSide = "meew0.microbes.proxy.CommonProxy", clientSide = "meew0.microbes.proxy.ClientProxy")
     public static CommonProxy serverProxy = new CommonProxy(); // Create proxy
     
     @Instance(id)
     public static MicrobeMod mod = new MicrobeMod(); // Create mod instance
     
     private Logger lg = Logger.getLogger(id); // Create logger
     
     @PreInit
     public void preInit(FMLPreInitializationEvent evt) {
         lg.setParent(FMLLog.getLogger()); // Initialize logger
         lg.info(getFullModName() + " loaded, initializing now");
     }
     
     @Init
     public void init(FMLInitializationEvent evt) {
         // Initialize items
         
         itemAgar = new MicrobeGenericItem(25900, CreativeTabs.tabMaterials, "agarSlime");
         itemEmptyAgarPlate = new MicrobeGenericItem(25901, CreativeTabs.tabMaterials, "agarPlate").setMaxStackSize(1);
         itemCottonSwab = new ItemCottonSwab(25902, CreativeTabs.tabTools, "cottonSwab");
         
         // Initialize blocks
         
         
         // LanguageRegistry calls
         
         LanguageRegistry.addName(itemAgar, "Agar");
         LanguageRegistry.addName(itemEmptyAgarPlate, "Empty Agar Plate");
         LanguageRegistry.addName(itemCottonSwab, "Cotton Swab");
         
         // Recipes
         
         GameRegistry.addShapelessRecipe(new ItemStack(itemAgar), Item.slimeBall, new ItemStack(Item.dyePowder, 1, 1)); // Agar
        GameRegistry.addShapedRecipe(new ItemStack(itemCottonSwab), "  W", " S ", "W  ", "W", Block.cloth, "S", Item.stick); // Cotton swab
     }
     
     @PostInit
     public void postInit(FMLPostInitializationEvent evt) {
         lg.info(getFullModName() + " successfully initialized");
     }
     
     public static String getFullModName() {
         return modName + " " + modVersion;
     }
     
     public Logger logger() {
         return lg;
     }
 }
