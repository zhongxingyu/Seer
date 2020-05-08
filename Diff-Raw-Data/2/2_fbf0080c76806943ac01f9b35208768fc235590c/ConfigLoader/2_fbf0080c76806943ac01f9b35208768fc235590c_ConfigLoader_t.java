 package hunternif.mc.rings.config;
 
 import hunternif.mc.rings.RingsOfPower;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ConfigLoader {
 	/** @param idsConfig Forge configuration file which holds Block and Item IDs.
 	 * @param config a class containing static fields with Blocks and Items. */
 	public static void preLoad(Configuration idConfig, Class config) {
 		try {
 			idConfig.load();
 			Field[] fields = config.getFields();
 			for (Field field : fields) {
 				if (field.getType().equals(CfgInfo.class)) {
 					CfgInfo<?> info = (CfgInfo)field.get(null);
 					info.initialize(field);
 					int id = info.id;
 					if (info.isBlock()) {
 						id = idConfig.getBlock(field.getName(), id).getInt();
 					} else {
 						id = idConfig.getItem(field.getName(), id).getInt();
 					}
 					info.id = id;
 				}
 			}
 		} catch(Exception e) {
 			FMLLog.log(RingsOfPower.ID, Level.SEVERE, "Failed to load config: " + e.toString());
 		} finally {
 			idConfig.save();
 		}
 	}
 	
 	public static void load(Class config) {
 		try {
 			List<CfgInfo> itemsWithRecipes = new ArrayList<CfgInfo>();
 			Field[] fields = config.getFields();
 			// Parse fields to instantiate the items:
 			for (Field field : fields) {
 				if (field.getType().equals(CfgInfo.class)) {
 					CfgInfo info = (CfgInfo)field.get(null);
 					Constructor constructor = info.type.getConstructor(int.class);
 					info.instance = constructor.newInstance(info.id);
 					if (info.isBlock()) {
 						((Block)info.instance).setUnlocalizedName(field.getName());
 						GameRegistry.registerBlock((Block)info.instance, ItemBlock.class, field.getName(), RingsOfPower.ID);
 						LanguageRegistry.addName(info.instance, info.name);
 					} else {
 						((Item)info.instance).setUnlocalizedName(field.getName());
 						LanguageRegistry.addName(info.instance, info.name);
 						GameRegistry.registerItem((Item)info.instance, field.getName(), RingsOfPower.ID);
 						RingsOfPower.itemList.add((Item)info.instance);
 						
 					}
 					FMLLog.log(RingsOfPower.ID, Level.INFO, "Registered item " + info.name);
 					// Add recipe for rings of power
 					if (!info.coreItems.isEmpty()) {
 						for (Object coreItem : info.coreItems) {
 							GameRegistry.addRecipe(new ItemStack((Item)info.instance), "CDC", "DRD", "CDC",
 									'C', coreItem, 'D', Item.diamond, 'R', Config.commonRing.instance);
 							FMLLog.log(RingsOfPower.ID, Level.INFO, "Added recipe for item " + info.name);
						}
 					}
 				}
 			}
 		} catch(Exception e) {
 			FMLLog.log(RingsOfPower.ID, Level.SEVERE, "Failed to instantiate items: " + e.toString());
 		}
 	}
 }
