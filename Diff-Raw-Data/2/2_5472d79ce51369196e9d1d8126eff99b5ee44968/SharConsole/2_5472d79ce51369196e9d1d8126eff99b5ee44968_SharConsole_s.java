 package sharConsole.common;
 
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
 
 
 /**
  * This mod adds a console into the minecraft user interface.
  * 
  * @author Sharingan616
  * @version 1.4.0
  */
 @Mod(modid = "mod_sharConsole", name="Minecraft Console", version = "1.4.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class SharConsole {
 	private String version = "1.4.0";
 	@Instance("Minecraft Console")
 	public static SharConsole instance;
 	
	@SidedProxy(clientSide="sharConsole.client.ClientProxySharConsole", serverSide="sharConsole.CommonProxySharConsole")
 	public static CommonProxySharConsole proxy;
 	
 	@PreInit
 	public void preLoad(FMLPreInitializationEvent event)
 	{
 		//TODO
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		//TODO
 		proxy.registerRenderers();
 	}
 	
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent event)
 	{
 		//TODO
 	}
 	
 	public String getVersion()
 	{
 		return this.version;
 	}
 
 }
