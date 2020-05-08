 package hunternif.mc.rings;
 
 import hunternif.mc.rings.config.Config;
 import hunternif.mc.rings.config.ConfigLoader;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod(modid=RingsOfPower.ID, name=RingsOfPower.NAME, version=RingsOfPower.VERSION)
 @NetworkMod(clientSideRequired=true, serverSideRequired=true)
 public class RingsOfPower {
 	public static final String ID = "RingsOfPower";
 	public static final String NAME = "Rings of Power";
	public static final String VERSION = "@@MOD_VERSION";
 	public static final String CHANNEL = ID;
 	
 	@Instance(ID)
 	public static RingsOfPower instance;
 	
 	@SidedProxy(clientSide="hunternif.mc.rings.ClientProxy", serverSide="hunternif.mc.rings.CommonProxy")
 	public static CommonProxy proxy;
 	
 	public static final List<Item> itemList = new ArrayList<Item>();
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		proxy.registerSounds();
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		ConfigLoader.preLoad(config, Config.class);
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		ConfigLoader.load(Config.class);
 		
 		GameRegistry.addShapedRecipe(new ItemStack(Config.commonRing.instance),
 				"iii", "iXi", "iii", 'i', Item.ingotIron, 'X', Item.bucketLava);
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {}
 }
