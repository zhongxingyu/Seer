 package mods.ryanjh5521.microblocks;
 
 import mods.ryanjh5521.core.api.FMLModInfo;
 import mods.ryanjh5521.core.api.util.ErrorScreen;
 import mods.ryanjh5521.microblocks.coremod.MicroblocksCoreMod;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 
 // metadata required for AutoPublisher
 @Mod(version=ModProperties.MOD_VERSION, modid="ryanjh5521MicroblocksInstallCheck", name="ryanjh5521's Microblocks (Check for incorrect installation)")
 @FMLModInfo(
 		modid=ModProperties.MODID,
 		name=ModProperties.MOD_NAME,
		url="http://www.minecraftforum.net/topic/1001131-110-ryanjh5521s-mods-smp/",
 		description="",
 		authors="ryanjh5521"
 		)
 public class MicroblocksNonCoreMod {
 	
 	@Init
 	public void init(FMLInitializationEvent evt) {
 		if(!MicroblocksCoreMod.TEST_DISABLED && !Loader.isModLoaded("ryanjh5521Microblocks"))
 			ErrorScreen.displayFatalError(
 				"ryanjh5521's Microblocks must be installed in the coremods folder.",
 				"Please correct the problem and restart Minecraft.");
 	}
 }
