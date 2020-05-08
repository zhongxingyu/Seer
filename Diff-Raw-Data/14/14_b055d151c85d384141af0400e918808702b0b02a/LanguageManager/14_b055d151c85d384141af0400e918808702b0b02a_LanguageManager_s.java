 package spacecraft.core.utility;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 import spacecraft.core.block.BlockPortalSC;
 import spacecraft.core.block.BlockTeleporter;
 import spacecraft.core.block.tile.TileEntityTeleporter;
 import spacecraft.core.item.ItemDebugText;
 import spacecraft.core.item.ItemLocator;
 import spacecraft.core.world.TeleportManager;
 import spacecraft.core.world.WorldProviderSC;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.util.StringTranslate;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public final class LanguageManager {
 	public static LanguageManager INSTANCE = new LanguageManager();
 	private static final String HEADER = "Language file for Spacecraft.";
 	
 	private Properties table;
 	
 	public static String translate(String str) {
 		return StringTranslate.getInstance().translateKey(str);
 	}
 	
 	public static void init(File langFile) {
 		INSTANCE.table = new Properties();
 		try {
 			if (!langFile.exists()) {
 				langFile.createNewFile();
 			}
 			FileInputStream is = new FileInputStream(langFile);
 			INSTANCE.table.load(is);
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		INSTANCE.fillTable();
 		LanguageRegistry.instance().addStringLocalization(INSTANCE.table);
 		try {
 			FileOutputStream os = new FileOutputStream(langFile);
 			INSTANCE.table.store(os, HEADER);
 			os.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String forClass(Class c) {
		if (c.isAssignableFrom(Item.class)) {
 			return "item." + RegistryHelper.getName(c) + ".name";
		} else {// if (c.isAssignableFrom(Block.class)) {
			return "tile." + RegistryHelper.getName(c) + ".name";
 		}
 	}
 	
 	private void fillTable() {
 		//Item
 		table.setProperty(forClass(ItemDebugText.class), "测试物品");
 		table.setProperty(forClass(ItemLocator.class), "定位器");
 		//Block
 		table.setProperty(forClass(BlockPortalSC.class), "传送方块");
 		table.setProperty(forClass(BlockTeleporter.class), "传送器");
 		//inventory
 		table.setProperty(TileEntityTeleporter.INVENTORY, "传送器");
 		//CreativeTab
 		table.setProperty(RegistryHelper.CREATIVEPAGENAME_UNL, "SpaceCraft");
 		//dimension
 		table.setProperty(WorldProviderSC.DIMENSION, "异空间");
 		//printed text
 		table.setProperty(TeleportManager.MSG, "You're not allowed to use this teleporter!");
 	}
 }
