 package entanglecraft;
 
 import java.io.File;
 
 import cpw.mods.fml.client.registry.KeyBindingRegistry;
 
 import net.minecraft.src.SoundManager;
 import net.minecraft.src.World;
 import entanglecraft.SoundHandling.LambdaSoundHandler;
 import net.minecraftforge.common.MinecraftForge;
 
 public class ClientProxy extends CommonProxy{
 	
 	public static boolean isServer = false;
	public static void registerClientSide() {
 		MinecraftForge.EVENT_BUS.register(new LambdaSoundHandler());
 		KeyBindingRegistry.registerKeyBinding(new KeyHandling());
 		System.out.println("Tried to register clientside stuff");
 	}
 	
 	public File getWorldSaveDir(World world){
 		File workingDir = new File(".\\\\saves\\" + world.getWorldInfo().getWorldName() + "\\");
 		return workingDir;
 	}
 }
