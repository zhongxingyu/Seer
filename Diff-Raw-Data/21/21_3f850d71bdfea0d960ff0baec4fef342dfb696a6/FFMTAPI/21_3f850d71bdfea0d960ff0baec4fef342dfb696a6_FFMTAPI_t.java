 package fr.mcnanotech.FFMT.FFMTAPI;
 
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Metadata;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid = "FFMTAPI", name = "FFMT API", version = "1.0.0", useMetadata = true)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 
 public class FFMTAPI 
 {
 	public static Logger FFMTlog;
	@Metadata("FFMTAPI")
	public static ModMetadata meta;
 	
 	@PreInit
 	public void preload(FMLPreInitializationEvent event)
 	{
 		FFMTlog = event.getModLog();
	}
	
	@Init
	public void load(FMLInitializationEvent event)
	{
 		meta.modId       = "FFMTAPI";
 		meta.name        = "FFMT API";
 		meta.version     = "1.0.0";
		meta.authorList  = Arrays.asList(new String[] {"kevin_68", "robin4002", "elias54"});
 		meta.description = "simplify your coder life";
 		meta.url         = "http://forge.mcnanotech.fr/";
 		meta.logoFile    = "/ffmt_logo.png";
 	}
 	
 }
