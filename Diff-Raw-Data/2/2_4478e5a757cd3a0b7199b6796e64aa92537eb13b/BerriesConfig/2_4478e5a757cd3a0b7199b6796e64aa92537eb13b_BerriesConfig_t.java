 
 import net.minecraft.src.*;
 import net.minecraft.src.forge.*;
 import net.minecraft.src.ic2.api.CropCard;
 import java.io.*;
 
 public class BerriesConfig {
     public final Configuration config;
 
     public final int cropid_vine;
     public final int cropid_blackberry;
     public final int cropid_raspberry;
     public final int cropid_strawberry;
     public final int cropid_blueberry;
     public final int cropid_huckleberry;
 
     public final ItemStack item_blackberry;
     public final ItemStack item_raspberry;
     public final ItemStack item_strawberry;
     public final ItemStack item_blueberry;
     public final ItemStack item_huckleberry;
 
     public BerriesConfig(Configuration config) {
         this.config = config;
 
         try {
             config.load();
         } catch (RuntimeException e) {
             e.printStackTrace();
         }
 
         cropid_vine = getInt("cropid.vine", 60);
         cropid_blackberry = getInt("cropid.blackberry", 61);
         cropid_raspberry = getInt("cropid.raspberry", 62);
         cropid_strawberry = getInt("cropid.strawberry", 63);
         cropid_blueberry = getInt("cropid.blueberry", 64);
         cropid_huckleberry = getInt("cropid.huckleberry", 65);
 
         item_blackberry = getItem("item.blackberry", 20000, 5);
         item_raspberry = getItem("item.raspberry", 20000, 8);
         item_strawberry = getItem("item.strawberry", 0, 0);  // not in Trees++  TODO: new custom item
         item_blueberry = getItem("item.blueberry", 20000, 7);
         item_huckleberry = getItem("item.huckleberry", 20000, 6);
 
         config.save();
     }
 
     public int getInt(String key, int defaultValue) {
         return Integer.parseInt(config.getOrCreateIntProperty(key, Configuration.CATEGORY_GENERAL, defaultValue).value);
     }
 
     public ItemStack getItem(String key, int defaultID, int defaultDamage) {
         int id = this.getInt(key, defaultID);
         if (id == 0) {
             return null;
         }
 
        int damage = this.getInt(key + ".damage", defaultDamage);
 
         return new ItemStack(id, 1, damage);
     }
 
     public void registerCrops() {
         if (cropid_vine != 0) new VineCrop(cropid_vine);
         if (cropid_blackberry != 0 && item_blackberry != null) new BlackberryCrop(cropid_blackberry, item_blackberry);
         if (cropid_raspberry != 0 && item_raspberry != null) new RaspberryCrop(cropid_raspberry, item_raspberry);
         if (cropid_strawberry != 0 && item_strawberry != null) new StrawberryCrop(cropid_strawberry, item_strawberry);
         if (cropid_blueberry != 0 && item_blueberry != null) new BlueberryCrop(cropid_blueberry, item_blueberry);
         if (cropid_huckleberry != 0 && item_huckleberry != null) new HuckleberryCrop(cropid_huckleberry, item_huckleberry);
     }
 }
 
