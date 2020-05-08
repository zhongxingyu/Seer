 package emris.mods.Air;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid="Air", name="Replace to Air", version="1.0")
 @NetworkMod(clientSideRequired=false, serverSideRequired=false)
 public class Air {
 	
 	@Instance("Air")
 	public static Air instance;
 	
 	@ServerStarting
 	public void serverStarting(FMLServerStartingEvent event) {
 		event.registerServerCommand(new CommandAir());
 	}
 }
