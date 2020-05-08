 package hunternif.mc.dota2items;
 
 import hunternif.mc.dota2items.block.BlockCycloneContainer;
 import hunternif.mc.dota2items.item.BlinkDagger;
 import hunternif.mc.dota2items.item.BootsOfSpeed;
 import hunternif.mc.dota2items.item.Dota2Logo;
 import hunternif.mc.dota2items.item.EulsScepter;
 import hunternif.mc.dota2items.item.GoldCoin;
 import hunternif.mc.dota2items.item.QuellingBlade;
 import hunternif.mc.dota2items.item.RingOfProtection;
 import hunternif.mc.dota2items.item.SagesMask;
 import hunternif.mc.dota2items.item.StaffOfWizardry;
 import hunternif.mc.dota2items.item.Tango;
 import hunternif.mc.dota2items.item.VoidStone;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Config {
 	private static Map<Class<?>, CfgInfo> map = new HashMap<Class<?>, CfgInfo>();
 	
 	public static void preLoad(Configuration config) {
 		try {
 			config.load();
 			Field[] fields = Config.class.getFields();
 			for (Field field : fields) {
 				if (field.getType().equals(CfgInfo.class)) {
 					CfgInfo info = (CfgInfo)field.get(null);
 					int id = info.id;
 					if (info.isBlock) {
 						id = config.getBlock(field.getName(), id).getInt();
 					} else {
 						id = config.getItem(field.getName(), id).getInt();
 					}
 					info.id = id;
 					map.put(info.clazz, info);
 				}
 			}
 		} catch(Exception e) {
 			FMLLog.severe(Dota2Items.ID, Level.SEVERE, "Failed to load config");
 		} finally {
 			config.save();
 		}
 	}
 	
 	public static void load() {
 		try {
 			Field[] fields = Config.class.getFields();
 			for (Field field : fields) {
 				if (field.getType().equals(CfgInfo.class)) {
 					CfgInfo info = (CfgInfo)field.get(null);
 					info.instance = info.clazz.getConstructor(int.class).newInstance(info.id);
 					if (info.isBlock) {
 						((Block)info.instance).setUnlocalizedName(field.getName());
 						GameRegistry.registerBlock((Block)info.instance, field.getName());
 						LanguageRegistry.addName(info.instance, info.name);
 					} else {
 						((Item)info.instance).setUnlocalizedName(field.getName());
 						LanguageRegistry.addName(info.instance, info.name);
						GameRegistry.registerItem((Item)info.instance, field.getName());
 						Dota2Items.itemList.add((Item)info.instance);
 					}
 				}
 			}
 		} catch(Exception e) {
 			FMLLog.log(Dota2Items.ID, Level.SEVERE, "Failed to instantiate items");
 		}
 	}
 	
 	public static CfgInfo dota2Logo = new CfgInfo(Dota2Logo.class, 27000, "Dota 2 Logo");
 	public static CfgInfo blinkDagger = new CfgInfo(BlinkDagger.class, 27001, "Blink Dagger");
 	public static CfgInfo tango = new CfgInfo(Tango.class, 27002, "Tango");
 	public static CfgInfo quellingBlade = new CfgInfo(QuellingBlade.class, 27003, "Quelling Blade");
 	public static CfgInfo eulsScepter = new CfgInfo(EulsScepter.class, 27004, "Eul's Scepter of Divinity");
 	public static CfgInfo ringOfProtection = new CfgInfo(RingOfProtection.class, 27005, "Ring of Protection");
 	public static CfgInfo bootsOfSpeed = new CfgInfo(BootsOfSpeed.class, 27006, "Boots of Speed");
 	public static CfgInfo goldCoin = new CfgInfo(GoldCoin.class, 27007, "Gold Coin");
 	public static CfgInfo voidStone = new CfgInfo(VoidStone.class, 27008, "Void Stone");
 	public static CfgInfo sagesMask = new CfgInfo(SagesMask.class, 27009, "Sage's Mask");
 	public static CfgInfo staffOfWizardry = new CfgInfo(StaffOfWizardry.class, 27010, "Staff of Wizardry");
 	public static CfgInfo recipe = new CfgInfo(SagesMask.class, 27011, "Recipe");
 	
 	public static CfgInfo cycloneContainer = new CfgInfo(BlockCycloneContainer.class, 2700, "Cyclone Container", true);
 	
 	public static class CfgInfo {
		protected Class<?> clazz;
		protected int id;
 		public String name;
 		public boolean isBlock;
 		public Object instance;
 		public CfgInfo(Class<?> clazz, int defaultID, String englishName, boolean isBlock) {
 			this.clazz = clazz;
 			this.id = defaultID;
 			this.name = englishName;
 			this.isBlock = isBlock;
 		}
 		public CfgInfo(Class<?> clazz, int defaultID, String englishName) {
 			this(clazz, defaultID, englishName, false);
 		}
		public int getID() {
			return isBlock ? ((Block)instance).blockID : ((Item)instance).itemID;
		}
 	}
 
 	public static CfgInfo forClass(Class<?> clazz) {
 		return map.get(clazz);
 	}
 }
