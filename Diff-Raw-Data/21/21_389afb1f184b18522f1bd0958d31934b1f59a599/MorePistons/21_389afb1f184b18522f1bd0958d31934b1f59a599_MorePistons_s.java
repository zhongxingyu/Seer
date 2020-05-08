 package morePistons.morePistons;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import morePistons.morePistons.pistons.*;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.Item;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit; 
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="MorePistons", name="MorePistons", version="0.0.1")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class MorePistons {
 	
 		public static final int idPistonExtension = 700;
 		public static final int idPistonMoving = 701;
 		public static final int idDoublePiston = 702;
 		public static final int idDoublePistonS = 703;
 		
 		
 		public static final BlockMorePistonExtension pistonExtension = new BlockMorePistonExtension(idPistonExtension);
 		public static final BlockMorePistonMoving pistonMoving = new BlockMorePistonMoving(idPistonMoving);
 		public static final BlockDoublePistonBase doublePiston = new BlockDoublePistonBase(idDoublePiston, false);
 		public static final BlockDoublePistonBase doublePistonS = new BlockDoublePistonBase(idDoublePistonS, true);
 		
 		
 		
 		  public static CreativeTabs tabPistons = new CreativeTabs("tabPistons") {
               public ItemStack getIconItemStack() {
                       return new ItemStack(net.minecraft.item.Item.appleRed, 1, 0);
               }
       };
 	
         // The instance of your mod that Forge uses.
         @Instance("MorePistons")
         public static MorePistons instance;
         
         // Says where the client and server 'proxy' code is loaded.
         @SidedProxy(clientSide="morePistons.morePistons.ClientProxy", serverSide="morePistons.morePistons.CommonProxy")
         public static CommonProxy proxy;
         
         @EventHandler
         public void preInit(FMLPreInitializationEvent event) {
                 // Stub Method
         }
         
         @EventHandler
         public void load(FMLInitializationEvent event) {
             proxy.registerRenderers();
             
         	LanguageRegistry.addName(pistonExtension, "Piston Extension");
         	LanguageRegistry.addName(pistonMoving, "Piston Moving");
         	
         	
         	
         	LanguageRegistry.addName(doublePiston, "Double Piston");
         	
         	LanguageRegistry.addName(doublePistonS, "Double Sticky Piston");
         	
         	
         	LanguageRegistry.instance().addStringLocalization("itemGroup.tabPistons", "en_US", "Pistons");
         	
         	
         	addCreativeTab();
         	
         }
         
         
         
         public void addCreativeTab(){
         	
         	doublePiston.setCreativeTab(tabPistons);
         	
         }
         
         
         @EventHandler
         public void postInit(FMLPostInitializationEvent event) {
                 // Stub Method
         }
 }
