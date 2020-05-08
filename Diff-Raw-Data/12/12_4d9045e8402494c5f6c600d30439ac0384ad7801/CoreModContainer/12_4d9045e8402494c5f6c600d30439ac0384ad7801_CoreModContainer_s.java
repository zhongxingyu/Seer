 package bstramke.NetherStuffsCore;
 
 import java.io.File;
 import java.util.Arrays;
 
 import net.minecraftforge.common.Configuration;
 
 import codechicken.core.ClientUtils;
 import codechicken.nei.ClientHandler;
 import codechicken.nei.ServerHandler;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 import cpw.mods.fml.common.DummyModContainer;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.LoadController;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 public class CoreModContainer extends DummyModContainer {
 	public static int NetherSkyBlockId;
 	public static boolean bOverrideBlockBreakable;
 	public static boolean bOverrideBlockPane;
 	public static boolean bOverrideChunk;
 	
 	public CoreModContainer() {
 		super(new ModMetadata());
 		/* ModMetadata is the same as mcmod.info */
 		ModMetadata myMeta = super.getMetadata();
 		myMeta.authorList = Arrays.asList(new String[] { "BStramke" });
 		myMeta.description = "Core Mod for NetherStuffs";
 		myMeta.modId = "NetherStuffsCore";
		myMeta.version = "0.14";
 		myMeta.name = "NetherStuffsCore";
 		myMeta.url = "http://netherstuffs.wikispaces.com/";
 	}
 
 	public boolean registerBus(EventBus bus, LoadController controller) {
 		bus.register(this);
 		return true;
 	}
 		
 	@Subscribe
 	public void preInit(FMLPreInitializationEvent event)
    {		
 		FMLLog.info("[NetherStuffsCore] preInit");
		Configuration config = new Configuration(new File(event.getModConfigurationDirectory() + "NetherStuffs.cfg"));
 		config.load();
 		NetherSkyBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SkyBlock", 1245).getInt(1245);
 		bOverrideBlockBreakable = config.get(Configuration.CATEGORY_GENERAL, "OverrideBlockBreakableClass", true).getBoolean(true);
 		bOverrideBlockPane =  config.get(Configuration.CATEGORY_GENERAL, "OverrideBlockPaneClass", true).getBoolean(true);
 		bOverrideChunk = config.get(Configuration.CATEGORY_GENERAL, "OverrideChunkClass", true).getBoolean(true);
 		config.save();	
    }
 }
