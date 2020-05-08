 package emris.mods.Air;
 
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
 import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.relauncher.Side;
 
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
