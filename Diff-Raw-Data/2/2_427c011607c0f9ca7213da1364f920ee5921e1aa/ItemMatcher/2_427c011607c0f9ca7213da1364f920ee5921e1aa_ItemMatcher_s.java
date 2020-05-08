 package co.d3s.ylt.chestplate.util;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.material.MaterialData;
 
 import co.d3s.ylt.chestplate.ChestPlate;
 class ItemMatch {
     int id;
     boolean hasMeta = false;
     byte meta = 0;
     public ItemMatch(LinkedHashMap<String, Integer> a) {
         id = a.get("id");
         Integer tmeta = a.get("meta");
         if (tmeta != null) {
             meta = tmeta.byteValue();
             hasMeta = true;
         }
     }
     public boolean equals(int id, byte meta) {
         //System.out.println("Checking "+id+", "+meta+" against "+this.id+", "+hasMeta+
         //" "+this.meta+" Result: "+(this.id == id && (!hasMeta || this.meta == meta)));
         return (this.id == id && (!hasMeta || this.meta == meta));
     }
 }
 public class ItemMatcher {
     ChestPlate cp;
     public ItemMatcher(ChestPlate cp) {
         this.cp = cp;
         load();
     }
     
     HashMap<String, ItemMatch[]> materials = new HashMap<String, ItemMatch[]>();
     
     YamlConfiguration config;
     public void load() {
         materials.clear();
         File folder = cp.getDataFolder();
         File configfile = new File(folder, "matcher.yml");
         config = YamlConfiguration.loadConfiguration(configfile);
         Map<String, Object> section = config.getConfigurationSection("multiblock").getValues(false);
         
         
         
         for(Entry<String, Object> entry : section.entrySet()) {
             String title = entry.getKey();
             List<ItemMatch> out = new LinkedList<ItemMatch>();
             Object value = entry.getValue();
             if (!(value instanceof List))
                 continue;
             List<?> blocksection = (List<?>) value;
             //System.out.println(title+" = "+blocksection);
             for(Object block : blocksection) {
                 if (block instanceof LinkedHashMap) {
                     out.add(new ItemMatch((LinkedHashMap<String, Integer>) block));
                 }
                 //System.out.println("title" + " - "+ block + " - "+block.getClass());
             }
             materials.put(title, out.toArray(new ItemMatch[]{}));
         }
     }
     
     public boolean Type_Match(String name, int pickupID, byte pickupData) {
         String dataName = "";
         String enchantmentName;
         
         if (name.equals("*")) {
             return true;
         }
         
         ItemMatch[] ids = materials.get(name);
         if (ids != null) {
             if (ids != null) {
                 for (ItemMatch matdata : ids) {
                     if (matdata.equals(pickupID, pickupData))
                         return true;
                 }
             }
         }
         //our lookup failed.
         
         if (name.contains(":")) { //metadata
             String[] parts = name.split(":");
             dataName = parts[1];
             name = parts[0];
         }
         
         try {
             int id = Integer.parseInt(name);
             int dataid = 0;
             //Material mat = Material.getMaterial(id);
             
             if (dataName != "") {
                 dataid = Integer.parseInt(dataName);
                if (id == pickupID && pickupID == dataid)
                     return true;
                 else
                     return false;
             }
             else {
                 if (id == pickupID)
                     return true;
                 else
                     return false;
             }
             
         }
         catch (NumberFormatException e) {
                 //Item ID reading failed, time for bukkit lookup.
                 
                 Material mat = Material.getMaterial(name.toUpperCase());
                 //System.out.println(mat);
                 if (mat != null && mat.getId() == pickupID)
                     return true;
                 else
                     return false;
         }
     }
 }
