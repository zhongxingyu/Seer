 package org.t244.minecraft;
 
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.effect.EntityLightningBolt;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.EventPriority;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.Event.Result;
 import net.minecraftforge.event.entity.player.AttackEntityEvent;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid=LightningAspectMod.modid, name="LightningAspectMod", version="1.0")
 @NetworkMod(clientSideRequired=true, serverSideRequired=true)
 public class LightningAspectMod {
 	public static final String modid = "T244_LightningAspectMod";
 	
 	public static Configuration config;
 	public static int EnchantmentID;
 	public static LightningAspect instance;
	private static final int defaultID = 52;
 	
 	
 	@PreInit
 	public void loadConfig(FMLPreInitializationEvent e) {
 		config = new Configuration(e.getSuggestedConfigurationFile());
 		config.load();
 		EnchantmentID = config.get("enchantment", "LightningAspect", defaultID).getInt();
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent e) {
		if (EnchantmentID != -1) {
 			instance = new LightningAspect(EnchantmentID,1);
 			return;
 		}
 		
 		for (int id=defaultID; id<256; id++) {
 			try {
 				instance = new LightningAspect(id, 1);
 			} catch (IllegalArgumentException ex) {
				//TODO: figure out why this catch doesn't catch
 				instance = null;
 				continue;
 			}
 			EnchantmentID = id;
 			/*TODO: save enchantment ID (not likely, 
 				will need to move allocation to pre-init, 
 				check existence of key before attempting to load) */
 			break;
 		}
 
 		if (instance ==  null) {
 			throw new RuntimeException("Could not get a free enchantment ID");
 		}
 	}
 	
 	@PostInit
 	public void registerHandler(FMLPostInitializationEvent e) {
 		MinecraftForge.EVENT_BUS.register(instance);
 	}
 	
 	
 
 }
