 /**
  *
  * @author Indivisible0
  */
 package com.github.indiv0.alchemicalcauldron;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.plugin.java.JavaPlugin;
 
import com.github.Indiv0.BukkitUtils.UtilManager;
 
 public class AlchemicalCauldron extends JavaPlugin {
     private final int CONFIG_VERSION = 1;
 
     private final HashMap<Material, Double> inputMaterials = new HashMap<Material, Double>();
     private final HashMap<Material, HashMap<Material, Double>> materialMatches = new HashMap<Material, HashMap<Material, Double>>();
 
     private final UtilManager utilManager = new UtilManager();
 
     @Override
     public void onLoad() {
         // Initialize all utilities.
         utilManager.initialize(this, CONFIG_VERSION);
     }
 
     @Override
     public void onEnable() {
         utilManager.getListenerUtil().registerListener(new ItemDropListener(this));
 
         loadInputMaterials("inputs");
         loadOutputMaterials("outputs");
     }
 
     @Override
     public void onDisable() {
         // Cancels any tasks scheduled by this plugin.
         getServer().getScheduler().cancelTasks(this);
     }
 
     private void loadInputMaterials(String section) {
         Set<String> keyList = getConfigurationSectionKeySet(section);
 
         for (String materialID : keyList) {
             // Attempts to get the material represented by the key.
             Material material = Material.matchMaterial(materialID);
 
             // Checks to make sure the material is legitimate, has not been
             // entered twice, and is accepted by
             // the plugin prior to proceeding.
             if (!isAllowedMaterial(getInputMaterials(), material))
                 continue;
 
             // Gets the probability value of the provided material.
             double val = getAndParseConfigDouble(section, materialID);
 
             // Makes sure that the probability value falls within the expected
             // range.
             if (val < 0 || val > 1) {
                 utilManager.getLogUtil().logException(Level.WARNING, "Config contains an invalid value for key: " + materialID);
                 continue;
             }
 
             // Adds the material/probability key/value set to the material
             // cache.
             getInputMaterials().put(material, val);
         }
     }
 
     private void loadOutputMaterials(String section) {
         Set<String> keyList = getConfigurationSectionKeySet(section);
 
         for (String materialID : keyList) {
             // Attempts to get the material represented by the key.
             Material material = Material.matchMaterial(materialID);
 
             // Checks to make sure the material is legitimate, has not been
             // entered twice, and is accepted by
             // the plugin prior to proceeding.
             if (!isAllowedMaterial(getOutputMaterials(), material))
                 continue;
 
             // Adds the input material and its corresponding HashMap of possible
             // outputs to the cache.
             getOutputMaterials().put(material, new HashMap<Material, Double>());
 
             // Gets the secondary material list.
             Set<String> outputList = getConfigurationSectionKeySet(section + "." + materialID);
 
             for (String outputID : outputList) {
                 // Attempts to get the material represented by the key.
                 Material outputMaterial = Material.matchMaterial(outputID);
 
                 // Checks to make sure the material is legitimate, has not been
                 // entered twice, and is accepted by
                 // the plugin prior to proceeding.
                 if (!isAllowedMaterial(getMaterialMatches(material), outputMaterial))
                     continue;
 
                 // Gets the probability value of the provided material.
                 double val = getAndParseConfigDouble(section + "." + materialID, outputID);
 
                 // Makes sure that the probability value falls within the
                 // expected range.
                 if (val < 0 || val > 1) {
                     utilManager.getLogUtil().logException(Level.WARNING, "Config contains an invalid value for key: " + materialID);
                     continue;
                 }
 
                 // Adds the material/probability key/value set to the material
                 // cache.
                 getMaterialMatches(material).put(outputMaterial, val);
             }
         }
     }
 
     private <K> boolean isAllowedMaterial(HashMap<Material, K> materialList, Material material) {
         // If the key is invalid, output as such.
         if (material == null || material == Material.AIR) {
             utilManager.getLogUtil().logException(Level.WARNING, "Config contains an invalid key.");
             return false;
         }
 
         // Makes sure an item is not being added twice, then adds the
         // material and its value to the cache.
         if (materialList.containsKey(material)) {
             utilManager.getLogUtil().logException(Level.WARNING, "Config contains the material " + material.toString() + " twice. It will not be added again.");
             return false;
         }
 
         return true;
     }
 
     private double getAndParseConfigDouble(String section, String key) {
         // Tries to lead the ratio value for that key.
         double val = -1;
         try {
             String valString = utilManager.getConfigUtil().
                     getValue(section + "." + key, String.class);
             val = Double.parseDouble(valString);
         } catch (Exception ex) {
             utilManager.getLogUtil().logException(ex, Level.WARNING, "Config contains an invalid value for key: " + key);
         }
 
         // Reduce the precision to 2 decimal places.
         DecimalFormat form = new DecimalFormat();
         form.setMaximumFractionDigits(2);
         form.setMinimumFractionDigits(0);
         val = Double.parseDouble(form.format(val));
 
         return val;
     }
 
     private Set<String> getConfigurationSectionKeySet(String section) {
         // If the configuration section does not exist, outputs a warning.
         if (utilManager.getConfigUtil().getConfigurationSection(section) == null) {
             utilManager.getLogUtil().logException(Level.WARNING, "No keys/values have been defined for the section \"" + section + "\".");
             return null;
         }
 
         // Gets all of the keys for the section.
         Set<String> keyList = utilManager.getConfigUtil().getConfigurationSection(section).getKeys(false);
 
         return keyList;
     }
 
     public HashMap<Material, Double> getInputMaterials() {
         return inputMaterials;
     }
 
     public HashMap<Material, HashMap<Material, Double>> getOutputMaterials() {
         return materialMatches;
     }
 
     public HashMap<Material, Double> getMaterialMatches(Material inputMaterial) {
         return materialMatches.get(inputMaterial);
     }
 }
