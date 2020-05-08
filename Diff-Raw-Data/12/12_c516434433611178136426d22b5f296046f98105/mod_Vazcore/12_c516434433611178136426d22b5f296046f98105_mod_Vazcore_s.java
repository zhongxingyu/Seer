 package vazkii.codebase.common;
 
 import vazkii.codebase.client.handlers.ClientTickHandler;
 import vazkii.codebase.common.handlers.VazcoreUpdateHandler;
 import vazkii.um.common.ModConverter;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.TickRegistry;
 
@Mod(modid = "vazcore_Vaz", name = "Vazcore", version = "by Vazkii. Version [1.0.5] for 1.3.2.")
 public class mod_Vazcore {
 
 	@PreInit
 	public void onPreInit(FMLPreInitializationEvent event) {
 		IOUtils.initFiles();
 	}
 
 	@Init
 	public void onInit(FMLInitializationEvent event) {
 		if (CommonUtils.getSide().isClient()) clientInit();
 		else serverInit();
 
 		new VazcoreUpdateHandler(ModConverter.getMod(getClass()));
 	}
 
 	public void clientInit() {
 		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
 	}
 
 	public void serverInit() {
 
 	}
 }
