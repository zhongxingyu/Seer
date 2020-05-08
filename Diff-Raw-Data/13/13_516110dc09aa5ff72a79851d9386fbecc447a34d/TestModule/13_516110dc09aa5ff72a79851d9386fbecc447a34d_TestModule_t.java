 package com.volumetricpixels.testmodule;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 
 import org.spout.api.Spout;
 import org.spout.api.material.Material;
 import org.spout.api.plugin.CommonPlugin;
 import org.spout.vanilla.material.InitializableMaterial;
 import org.spout.vanilla.material.VanillaBlockMaterial;
 import org.spout.vanilla.material.VanillaMaterial;
 
 public class TestModule extends CommonPlugin {
	public static final WindmillBlock w = new WindmillBlock("Windmill", 150);
 	
 	public void onDisable() {
 		getLogger().info("disabled.");
 	}
 
 	public void onEnable() {
 		initialize();
 		getLogger().info("enabled.");
 	}
 	
 	public static void initialize() {
 		for (Field field : TestModule.class.getFields()) {
 			try {
 				if (field == null || ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) != (Modifier.STATIC | Modifier.PUBLIC)) || !VanillaMaterial.class.isAssignableFrom(field.getType())) {
 					continue;
 				}
 				VanillaMaterial material = (VanillaMaterial) field.get(null);
 				if (material == null) {
 					Spout.getLogger().severe("Test-Module material field '" + field.getName() + "' is not yet initialized");
 					continue;
 				}
 				if (material instanceof InitializableMaterial) {
 					InitializableMaterial initializableMaterial = (InitializableMaterial) material;
 					initializableMaterial.initialize();
 					if (material instanceof VanillaBlockMaterial) {
 						for (Material subMaterial : ((VanillaBlockMaterial) material).getSubMaterials()) {
 							((InitializableMaterial) subMaterial).initialize();
 						}
 					}
 				}
 			} catch (Throwable t) {
 				Spout.getLogger().severe("An exception occurred while reading Test-Module field '" + field.getName() + "':");
 				t.printStackTrace();
 			}
 		}
 	}
 
 }
