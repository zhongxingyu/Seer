 /** 
  * Copyright (C) 2011-2013 Flow86
  * 
  * AdditionalIC2Objects is open-source.
  *
  * It is distributed under the terms of my Open Source License. 
  * It grants rights to read, modify, compile or run the code. 
  * It does *NOT* grant the right to redistribute this software or its 
  * modifications in any form, binary or source, except if expressively
  * granted by the copyright holder.
  */
 
 package aic2o;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Property;
import abo.proxy.ABOProxy;
 import aic2o.energy.BlockPetrochemicalGenerator;
 import aic2o.energy.ItemPetrochemicalGenerator;
 import aic2o.gui.AIC2OGuiHandler;
 import buildcraft.core.proxy.CoreProxy;
 import buildcraft.core.utils.Localization;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 /**
  * @author Flow86
  * 
  */
 @Mod(modid = "Additional-IC2-Objects", name = "Additional-IC2-Objects", version = "@AIC2O_VERSION@", dependencies = "required-after:Forge@7.7.2.682,);required-after:BuildCraft|Energy;required-after:IC2")
 public class AIC2O {
 	public static final String VERSION = "@AIC2O_VERSION@";
 
 	public static AIC2OConfiguration aic2oConfiguration;
 	public static Logger aic2oLog = Logger.getLogger("Additional-IC2-Objects");
 
 	public static int blockPetrochemicalGeneratorID = 1550;
 	public static Block blockPetrochemicalGenerator = null;
 
 	@Instance("Additional-IC2-Objects")
 	public static AIC2O instance;
 
 	int getSafeBlockID(int defaultID, Class<? extends Block> clazz) {
 		String name = Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
 		Property prop = aic2oConfiguration.getBlock(name + ".id", defaultID);
 
 		int id = prop.getInt(defaultID);
 
 		while (id < Block.blocksList.length && Block.blocksList[id] != null)
 			id++;
 
 		if (id >= Block.blocksList.length) {
 			aic2oLog.log(Level.SEVERE, "Cannot find free ID for Block + " + name + " starting from " + defaultID);
 			return 0;
 		}
 
 		prop.set(id);
 
 		return id;
 	}
 
 	@PreInit
 	public void preInitialize(FMLPreInitializationEvent evt) {
 
 		aic2oLog.setParent(FMLLog.getLogger());
 		aic2oLog.info("Starting Additional-IC2-Objects #@BUILD_NUMBER@ " + VERSION + " (Built for Minecraft @MINECRAFT_VERSION@ with Buildcraft @BUILDCRAFT_VERSION@ and Forge @FORGE_VERSION@");
 		aic2oLog.info("Copyright (c) Flow86, 2011-2013");
 
 		aic2oConfiguration = new AIC2OConfiguration(new File(evt.getModConfigurationDirectory(), "aic2o/main.conf"));
 		try {
 			aic2oConfiguration.load();
 
 			int id = getSafeBlockID(blockPetrochemicalGeneratorID, BlockPetrochemicalGenerator.class);
 
 			if (id > 0) {
 				blockPetrochemicalGenerator = new BlockPetrochemicalGenerator(id);
 				CoreProxy.proxy.registerBlock(blockPetrochemicalGenerator, ItemPetrochemicalGenerator.class);
 
 				LanguageRegistry.addName(new ItemStack(blockPetrochemicalGenerator, 1, 4), "Petrochemical Generator");
 			}
 
 		} finally {
 			if (aic2oConfiguration.hasChanged())
 				aic2oConfiguration.save();
 		}
 	}
 
 	@Init
 	public void load(FMLInitializationEvent evt) {
 
 		Localization.addLocalization("/lang/aic2o/", "en_US");
 
 		loadRecipes();
 
		ABOProxy.proxy.registerBlockRenderers();
		ABOProxy.proxy.registerTileEntities();
 
 		NetworkRegistry.instance().registerGuiHandler(instance, new AIC2OGuiHandler());
 	}
 
 	private void loadRecipes() {
 
 	}
 
 }
