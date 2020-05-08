 package com.madpcgaming.ds.handlers;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import com.madpcgaming.ds.lib.BlockIds;
 import com.madpcgaming.ds.lib.Reference;
 import com.madpcgaming.ds.lib.Strings;
 
 import cpw.mods.fml.common.FMLLog;
 
 import net.minecraftforge.common.Configuration;
 
 public class ConfigurationHandler
 {
 	public static Configuration	configuration;
 	public static final String	CATEGORY_BLOCK	= "Blocks";
 	public static final String	CATERGORY_ITEM	= "Items";
 
 	// Mobs Spawn
 	public static boolean		spawnReaperz	= true;
 	public static boolean		spawnAngel		= true;
 	public static boolean		spawnGolems		= true;
 	// Boss Spawns
 	public static boolean		spawnBrann		= true;
 	public static boolean		spawnLuft		= true;
 	public static boolean		spawnJorden		= true;
 	public static boolean		spawnVann		= true;
 	public static boolean		spawnGrim		= true;
 	public static boolean		spawnMico		= true;
 
 	public static void init(File configFile)
 	{
 		configuration = new Configuration(configFile);
 
 		try {
 			configuration.load();
 
 			/* Block ID Configuration */
 			BlockIds.SHADOW_STONE = configuration.getBlock(
					Strings.SHADOW_STONE_NAME, BlockIds.SHADOW_STONE_DEFAULT).getInt(
 					BlockIds.SHADOW_STONE_DEFAULT);
 			BlockIds.SHADOW_PORTAL = configuration.getBlock(
					Strings.SHADOW_PORTAL_NAME, BlockIds.SHADOW_PORTAL_DEFAULT).getInt(
 					BlockIds.SHADOW_STONE_DEFAULT);
 			BlockIds.BRIGHT_STONE = configuration.getBlock(
					Strings.BRIGHT_STONE_NAME, BlockIds.BRIGHT_STONE_DEFAULT).getInt(
 					BlockIds.BRIGHT_STONE_DEFAULT);
 			BlockIds.BRIGHT_PORTAL = configuration.getBlock(
					Strings.BRIGHT_PORTAL_NAME, BlockIds.BRIGHT_PORTAL_DEFAULT).getInt(
 					BlockIds.BRIGHT_PORTAL_DEFAULT);
 
 			/* Item ID Configuration */
 
 			/* GUI ID Configuration */
 		} catch (Exception e) {
 			FMLLog.log(Level.SEVERE, e, "Whoops!" + Reference.MOD_NAME
 					+ " goofed loading the configuraiton file");
 		} finally {
 			configuration.save();
 		}
 	}
 
 	public static void set(String catergoryName, String propertyName,
 			String newValue)
 	{
 		configuration.load();
 		if (configuration.getCategoryNames().contains(catergoryName)) {
 			if (configuration.getCategory(propertyName).containsKey(
 					propertyName)) {
 				configuration.getCategory(catergoryName).get(propertyName)
 						.set(newValue);
 			}
 		}
 		configuration.save();
 	}
 }
