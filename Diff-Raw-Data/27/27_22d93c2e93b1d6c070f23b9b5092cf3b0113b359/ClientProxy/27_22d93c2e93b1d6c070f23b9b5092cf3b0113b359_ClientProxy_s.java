 package rebelkeithy.mods.keithyutils;
 
 import java.io.File;
 
 import net.minecraft.client.Minecraft;
 import rebelkeithy.mods.keithyutils.capes.CapeTickHandler;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class ClientProxy extends CommonProxy
 {
 	
 	public void registerParticles()
 	{
 	}
 	
 	public void registerTickHandlers()
 	{
		TickRegistry.registerTickHandler(new CapeTickHandler(), Side.CLIENT);
 	}
 }
