 package com.nemock.CFGVillager;
 
 import java.io.File;
 
 import com.nemock.CFGVillager.core.handler.CFGVillagerHandler;
 import com.nemock.CFGVillager.core.handler.ConfigurationHandler;
 import com.nemock.CFGVillager.lib.Reference;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 import mca.api.VillagerRegistryMCA;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.common.Configuration;
 
 
 
 @Mod(
 		modid = Reference.MOD_ID,
 		name = Reference.MOD_NAME,
 		version = Reference.VERSION,
 		dependencies = Reference.DEPENDENCIES)
 
 @NetworkMod(
 		channels = {Reference.CHANNEL_NAME},
 		clientSideRequired = true,
 		serverSideRequired = true)
 
 public class CFGVillager
 {
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
		ConfigurationHandler.preInit(new File(event.getModConfigurationDirectory().getAbsolutePath()+ File.separator + Reference.MOD_ID + File.separator + Reference.MOD_ID + ".cfg"));		
 	}
 	
 	
 	@EventHandler
 	public void init(FMLInitializationEvent event)
 	{
 		for (int i = 0; i < ConfigurationHandler.countVillagers; i++)
 		{
 			//TODO: figure out how to get the textures from an outside location
 			ResourceLocation tPath = new ResourceLocation("cfgvillager" , "villager_" + Integer.toString(i+1) + ".png");
 			int id = ConfigurationHandler.idBase + i;
 			
 			//registers new villager, first id (int) then texture path (ResourceLocation)
 			VillagerRegistry.instance().registerVillagerId(id);
 			VillagerRegistry.instance().registerVillagerSkin( id, tPath);
 			
 			VillagerRegistryMCA.registerVillagerType(id);
 			
 			String villagertrades[][] = ConfigurationHandler.trades[i];
 			
 			CFGVillagerHandler newTradeHandler = new CFGVillagerHandler(villagertrades);
 			
 			VillagerRegistry.instance().registerVillageTradeHandler(id, newTradeHandler);
 		}
 		
 		VillagerRegistry.instance();
 		
 		//required to get Villagers spawning naturaly
 		VillagerRegistry.getRegisteredVillagers();
 	}
 
 	@EventHandler
 	public static void postInit(FMLPostInitializationEvent event)
 	{
 		
 	}
 
 }
