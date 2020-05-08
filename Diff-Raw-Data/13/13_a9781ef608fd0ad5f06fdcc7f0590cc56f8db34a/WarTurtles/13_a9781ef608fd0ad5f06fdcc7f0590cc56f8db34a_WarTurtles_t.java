 package leoverto.warturtles;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="WarTurtles", name="WarTurtles", version="0.0.1")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class WarTurtles {
 
         // The instance of your mod that Forge uses.
    @Instance("WarTurtles")
 	public static WarTurtles instance;
 	
 	// Says where the client and server 'proxy' code is loaded.
 	@SidedProxy(clientSide="leoverto.warturtles.ClientProxy", serverSide="leoverto.warturtles.CommonProxy")
 	public static CommonProxy proxy;

	public static int remoteControllerID;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
        remoteControllerID = config.getOrCreateIntProperty("RemoteControllerID", Configuration.CATEGORY_ITEM, 6250).getInt(6250);
 		config.save();
 	}
 	
 	private final static Item remoteController = new RemoteController(remoteControllerID);
 	
 	@Init
 	public void load(FMLInitializationEvent event) {
 		proxy.registerRenderers();
 		
 		LanguageRegistry.addName(remoteController, "Remote Controller");
 		
 		ItemStack stoneStack = new ItemStack(Block.stone);
 		ItemStack redstoneStack = new ItemStack(Item.redstone);
 		ItemStack diamondStack = new ItemStack(Item.diamond);
 
		GameRegistry.addRecipe(new ItemStack(remoteController), "x x", "yzy", "xyx",
 		        'x', stoneStack, 'y', redstoneStack, 'z', diamondStack);
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 	}
 }
