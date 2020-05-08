 package playacem.allrondism.configuration;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import static net.minecraftforge.common.Configuration.CATEGORY_GENERAL;
 import net.minecraftforge.common.Configuration;
 import playacem.allrondism.lib.BlockIDs;
 import playacem.allrondism.lib.ItemIDs;
 import playacem.allrondism.lib.Reference;
 import playacem.allrondism.lib.Strings;
 import cpw.mods.fml.common.FMLLog;
 
 /**
  * Allrondism
  * 
  * ConfigurationHandler
  * 
  * @author Playacem
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * @credit pahimar
  */
 public class ConfigurationHandler {
 
     public static Configuration config;
 
     public static final String CATEGORY_DOUBLING = "doublingRecipes";
     
     public static void init(File configFile) {
 
         config = new Configuration(configFile, true);
 
         try {
             config.load();
             /* Debug config*/
             ConfigurationSettings.DEBUG_MODE = config.get(CATEGORY_GENERAL, ConfigurationSettings.DEBUG_MODE_CONFIGNAME, ConfigurationSettings.DEBUG_MODE_DEFAULT).getBoolean(ConfigurationSettings.DEBUG_MODE_DEFAULT);
             
             /* Block configs */
             BlockIDs.ORE_ALLRONDIUM = config.getBlock(Strings.ORE_ALLRONDIUM_NAME, BlockIDs.ORE_ALLRONDIUM_DEFAULT).getInt(BlockIDs.ORE_ALLRONDIUM_DEFAULT);
             BlockIDs.STORAGE_BLOCKS = config.getBlock(Strings.STORAGE_BLOCKS_NAME, BlockIDs.STORAGE_BLOCKS_DEFAULT).getInt(BlockIDs.STORAGE_BLOCKS_DEFAULT);
             BlockIDs.GLASS_SUN_BLOCKER = config.getBlock(Strings.GLASS_SUN_BLOCKER_NAME, BlockIDs.GLASS_SUN_BLOCKER_DEFAULT).getInt(BlockIDs.GLASS_SUN_BLOCKER_DEFAULT);
             BlockIDs.PLANT_ROSE = config.getBlock(Strings.PLANT_ROSE_NAME, BlockIDs.PLANT_ROSE_DEFAULT).getInt(BlockIDs.PLANT_ROSE_DEFAULT);
             BlockIDs.MULTI_FURNACE_CORE = config.getBlock(Strings.MULTI_FURNACE_CORE_NAME, BlockIDs.MULTI_FURNACE_CORE_DEFAULT).getInt(BlockIDs.MULTI_FURNACE_CORE_DEFAULT);
             BlockIDs.MULTI_FURNACE_EXTENSION = config.getBlock(Strings.MULTI_FURNACE_EXTENSION_NAME, BlockIDs.MULTI_FURNACE_EXTENSION_DEFAULT).getInt(BlockIDs.MULTI_FURNACE_EXTENSION_DEFAULT);
             
             /* Item configs */
             ItemIDs.GEM_ALLRONDIUM = config.getItem(Strings.GEM_ALLRONDIUM_NAME, ItemIDs.GEM_ALLRONDIUM_DEFAULT).getInt(ItemIDs.GEM_ALLRONDIUM_DEFAULT);
             ItemIDs.APPLE_GOLD_ZOMBIE = config.getItem(Strings.APPLE_GOLD_ZOMBIE_NAME, ItemIDs.APPLE_GOLD_ZOMBIE_DEFAULT).getInt(ItemIDs.APPLE_GOLD_ZOMBIE_DEFAULT);
             ItemIDs.DYE_ROSE = config.getItem(Strings.DYE_ROSE_NAME, ItemIDs.DYE_ROSE_DEFAULT).getInt(ItemIDs.DYE_ROSE_DEFAULT);
             
             /* Doubling recipes*/
             config.addCustomCategoryComment(CATEGORY_DOUBLING, "Here you can disable or enable various doubling recipes.");
             ConfigurationSettings.DOUBLING_COPPER = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_COPPER_CONFIGNAME, ConfigurationSettings.DOUBLING_COPPER_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_COPPER_DEFAULT);
             ConfigurationSettings.DOUBLING_GOLD = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_GOLD_CONFIGNAME, ConfigurationSettings.DOUBLING_GOLD_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_GOLD_DEFAULT);
             ConfigurationSettings.DOUBLING_LEAD = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_LEAD_CONFIGNAME, ConfigurationSettings.DOUBLING_LEAD_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_LEAD_DEFAULT);
             ConfigurationSettings.DOUBLING_SILVER = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_SILVER_CONFIGNAME, ConfigurationSettings.DOUBLING_SILVER_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_SILVER_DEFAULT);
             ConfigurationSettings.DOUBLING_TIN = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_TIN_CONFIGNAME, ConfigurationSettings.DOUBLING_TIN_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_TIN_DEFAULT);
             ConfigurationSettings.DOUBLING_STEEL = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_STEEL_CONFIGNAME, ConfigurationSettings.DOUBLING_STEEL_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_STEEL_DEFAULT);
             ConfigurationSettings.DOUBLING_HSLA = config.get(CATEGORY_DOUBLING, ConfigurationSettings.DOUBLING_HSLA_CONFIGNAME, ConfigurationSettings.DOUBLING_HSLA_DEFAULT).getBoolean(ConfigurationSettings.DOUBLING_HSLA_DEFAULT);
             
         } catch (Exception e) {
             FMLLog.log(Level.SEVERE, e, Reference.MOD_NAME + " has had a problem loading its configuration");
         } finally {
             config.save();
         }
     }
 
     public static void set(String categoryName, String propertyName, String newValue) {
 
         config.load();
         if (config.getCategoryNames().contains(categoryName)) {
             if (config.getCategory(categoryName).containsKey(propertyName)) {
                config.getCategory(categoryName).get(propertyName).set(categoryName);
             }
         }
         config.save();
     }
 
 }
