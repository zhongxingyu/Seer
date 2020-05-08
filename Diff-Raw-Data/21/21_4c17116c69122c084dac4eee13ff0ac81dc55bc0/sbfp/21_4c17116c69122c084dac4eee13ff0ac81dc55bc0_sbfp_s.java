 package sbfp;
 
 import java.io.File;
 
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 import sbfp.world.BlockOre;
 import sbfp.world.ItemBlockOre;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod(modid = sbfp.modid, name = sbfp.shortname, version = sbfp.version)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class sbfp{
 
 	// name constants
 	public static final String modid = "sbfp";
 	public static final String shortname = "SBFP Tech";
 	public static final String version = "Aleph Null";
 
 	// mechanics constants
 	@Instance(modid)
 	private static sbfp instance;
 	private static final Configuration config = new Configuration(new File("config/SBFP/SBFP.cfg"));
 	@SidedProxy(clientSide = "sbfp.client.SBClientProxy", serverSide = "sbfp.SBCommonProxy")
 	public static SBCommonProxy proxy;
 
 	// blocks and items
 	public static final BlockOre blockOre = new BlockOre(0,getBlockID("blockOreID",0x4c0));
 
 	@Init
 	public void init(FMLInitializationEvent event){
 		GameRegistry.registerBlock(blockOre,ItemBlockOre.class,"blockOre");
 	}
 
 	private static int getBlockID(String name, int defaultid){
 		config.load();
 		Property q = config.get(Configuration.CATEGORY_BLOCK,name,defaultid);
 		return q.getInt(defaultid);
 	}
 
 	private static int getItemID(String name, int defaultid){
 		config.load();
 		Property q = config.get(Configuration.CATEGORY_ITEM,name,defaultid);
 		return q.getInt(defaultid);
 	}
 }
