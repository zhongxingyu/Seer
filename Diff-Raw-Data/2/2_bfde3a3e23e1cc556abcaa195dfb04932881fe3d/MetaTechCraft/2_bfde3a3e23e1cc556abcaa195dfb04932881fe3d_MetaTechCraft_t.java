 package com.metatechcraft.mod;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.DimensionManager;
 
 import com.metatechcraft.core.proxy.CommonProxy;
 import com.metatechcraft.block.MetaBlocks;
 import com.metatechcraft.dimension.MetaDimensionWorldProvider;
 import com.metatechcraft.generators.MetaTechOreGenerators;
 import com.metatechcraft.item.MetaItems;
 import com.metatechcraft.lib.MetaTabs;
 import com.metatechcraft.lib.ModInfo;
 import com.metatechcraft.liquid.MetaLiquids;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
@Mod(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME, version = ModInfo.VERSION, dependencies = "required-after:ForgeTutorialsAPI")
 public class MetaTechCraft {
 
 	@SidedProxy(clientSide = ModInfo.CLIENT_PROXY_CLASS, serverSide = ModInfo.SERVER_PROXY_CLASS)
 	public static CommonProxy proxy;
 
 	public static final CreativeTabs tabs = new MetaTabs();
 
 	public static final int metaDimID = 17;
 
 	public static MetaTechOreGenerators metaGenerator;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		System.out.println(">> MetaTechCraft: preInit");
 		MetaBlocks.initize();
 		MetaItems.initize();
 		MetaLiquids.initize();
 
 		MetaTechCraft.metaGenerator = new MetaTechOreGenerators();
 		MetaTechCraft.metaGenerator.preInit();
 	}
 	@EventHandler
 	public void init(FMLInitializationEvent event) {
 		System.out.println(">> MetaTechCraft: init");
 		MetaTechCraft.proxy.initizeRendering();
 
 		DimensionManager.registerProviderType(MetaTechCraft.metaDimID, MetaDimensionWorldProvider.class, false);
 		DimensionManager.registerDimension(MetaTechCraft.metaDimID, MetaTechCraft.metaDimID);
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		System.out.println(">> MetaTechCraft: postInit");
 		MetaTechCraft.proxy.registerTileEntities();
 	}
 
 }
