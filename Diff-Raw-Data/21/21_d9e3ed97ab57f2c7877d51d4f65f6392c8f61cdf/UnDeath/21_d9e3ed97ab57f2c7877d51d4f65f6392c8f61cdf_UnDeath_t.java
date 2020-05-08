 package com.nekokittygames.modjam.UnDeath;
 
 import java.util.logging.Logger;
 
import net.minecraftforge.common.Configuration;

 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid=UnDeath.ID,name=UnDeath.NAME,version=UnDeath.VERSION,modLanguage="java")
 @NetworkMod(clientSideRequired=true,serverSideRequired=false)
 public class UnDeath {
 	public static final String ID = "UnDeath";
 	public static final String VERSION = "1.0";
 	public static final String NAME = "Un Death";
 	
 	
 	@Instance(ID)
 	public static UnDeath Instance;
 	
 	@SidedProxy(clientSide="com.nekokittygames.modjam.UnDeath.client.ClientProxy",serverSide="com.nekokittygames.modjam.UnDeath.ServerProxy")
 	public ServerProxy proxy;
 	
 	public static Logger logging;
 	
 	@EventHandler
 	public void PreInit(FMLPreInitializationEvent event)
 	{
 		logging=Logger.getLogger(ID);
 		logging.setParent(FMLLog.getLogger());
		
		Configuration config=new Configuration(event.getSuggestedConfigurationFile());
		Configs.load(config);
 	}
 	
 	@EventHandler
 	public void Init(FMLInitializationEvent event)
 	{
 	}
 	
 	@EventHandler
 	public void PostInit(FMLPostInitializationEvent event)
 	{
 	}
 }
