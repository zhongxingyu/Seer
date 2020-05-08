 package com.thezomg.zomgctf;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 
 /***
  * 
  * Joining is from Apache Commons
  */
 
 public class util {
     public static void setLocationInConfig(YamlConfiguration config, String key, Location loc) {
         if (loc != null) {
             config.set(key + ".world", loc.getWorld().getName());
             config.set(key + ".x", loc.getX());
             config.set(key + ".y", loc.getY());
             config.set(key + ".z", loc.getZ());
         }
     }
     
     public static Location getLocationFromConfig(YamlConfiguration config, String key) {
         World world = Bukkit.getWorld(config.getString(key + ".world", Bukkit.getWorlds().get(0).getName()));
         double x = config.getDouble(key + ".x", 0);
         double y = config.getDouble(key + ".y", 0);
         double z = config.getDouble(key + ".z", 0);
         
         return new Location(world, x, y, z);
     }
     
     public static boolean inSameLocation(Location loc1, Location loc2) { 
         return (
                 (loc1.getBlockX() == loc2.getBlockX()) &&
                 (loc1.getBlockY() == loc2.getBlockY()) &&
                 (loc1.getBlockZ() == loc2.getBlockZ()) &&
                 (loc1.getWorld().equals(loc2.getWorld()))
                 );
     }
     
     public static ItemStack stringToItemStack(String block_name) throws IllegalArgumentException {
         if (block_name == null)
             return null;
 
         if (block_name.contains(":")) {
             String[] blockNameSplit = block_name.split(":");
             if (blockNameSplit.length > 2)
                 throw new IllegalArgumentException("No material matching: '" + block_name + "'");
 
             final int data;
             try {
                 data = Integer.parseInt(blockNameSplit[1]);
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Data type not a valid number: '" + blockNameSplit[1] + "'");
             }
 
             if (data > 255 || data < 0)
                 throw new IllegalArgumentException("Data type out of range (0-255): '" + data + "'");
 
             Material material = Material.matchMaterial(blockNameSplit[0]);
             if (material == null)
                 throw new IllegalArgumentException("No material matching: '" + block_name + "'");
 
            ItemStack item_stack = new ItemStack(material, 1, Short.parseShort(blockNameSplit[1]));
 
             return item_stack;
         } else {
             final Material material = Material.matchMaterial(block_name);
             if (material == null)
                 throw new IllegalArgumentException("No material matching: '" + block_name + "'");
 
             ItemStack item_stack = new ItemStack(material, 1);
 
             return item_stack;
         }
     }
     
     // Joining
     //-----------------------------------------------------------------------
     /**
      * <p>Joins the elements of the provided array into a single String
      * containing the provided list of elements.</p>
      *
      * <p>No separator is added to the joined String.
      * Null objects or empty strings within the array are represented by
      * empty strings.</p>
      *
      * <pre>
      * StringUtils.join(null)            = null
      * StringUtils.join([])              = ""
      * StringUtils.join([null])          = ""
      * StringUtils.join(["a", "b", "c"]) = "abc"
      * StringUtils.join([null, "", "a"]) = "a"
      * </pre>
      *
      * @param <T> the specific type of values to join together
      * @param elements  the values to join together, may be null
      * @return the joined String, {@code null} if null array input
      * @since 2.0
      * @since 3.0 Changed signature to use varargs
      */
     public static <T> String join(T... elements) {
         return join(elements, null);
     }
 
     /**
      * <p>Joins the elements of the provided array into a single String
      * containing the provided list of elements.</p>
      *
      * <p>No delimiter is added before or after the list.
      * A {@code null} separator is the same as an empty String ("").
      * Null objects or empty strings within the array are represented by
      * empty strings.</p>
      *
      * <pre>
      * StringUtils.join(null, *)                = null
      * StringUtils.join([], *)                  = ""
      * StringUtils.join([null], *)              = ""
      * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
      * StringUtils.join(["a", "b", "c"], null)  = "abc"
      * StringUtils.join(["a", "b", "c"], "")    = "abc"
      * StringUtils.join([null, "", "a"], ',')   = ",,a"
      * </pre>
      *
      * @param array  the array of values to join together, may be null
      * @param separator  the separator character to use, null treated as ""
      * @return the joined String, {@code null} if null array input
      */
     public static String join(Object[] array, String separator) {
         if (array == null) {
             return null;
         }
         return join(array, separator, 0, array.length);
     }
 
     /**
      * <p>Joins the elements of the provided array into a single String
      * containing the provided list of elements.</p>
      *
      * <p>No delimiter is added before or after the list.
      * A {@code null} separator is the same as an empty String ("").
      * Null objects or empty strings within the array are represented by
      * empty strings.</p>
      *
      * <pre>
      * StringUtils.join(null, *)                = null
      * StringUtils.join([], *)                  = ""
      * StringUtils.join([null], *)              = ""
      * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
      * StringUtils.join(["a", "b", "c"], null)  = "abc"
      * StringUtils.join(["a", "b", "c"], "")    = "abc"
      * StringUtils.join([null, "", "a"], ',')   = ",,a"
      * </pre>
      *
      * @param array  the array of values to join together, may be null
      * @param separator  the separator character to use, null treated as ""
      * @param startIndex the first index to start joining from.  It is
      * an error to pass in an end index past the end of the array
      * @param endIndex the index to stop joining from (exclusive). It is
      * an error to pass in an end index past the end of the array
      * @return the joined String, {@code null} if null array input
      */
     public static String join(Object[] array, String separator, int startIndex, int endIndex) {
         if (array == null) {
             return null;
         }
         if (separator == null) {
             separator = "";
         }
 
         // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
         //           (Assuming that all Strings are roughly equally long)
         int noOfItems = endIndex - startIndex;
         if (noOfItems <= 0) {
             return "";
         }
 
         StringBuilder buf = new StringBuilder(noOfItems * 16);
 
         for (int i = startIndex; i < endIndex; i++) {
             if (i > startIndex) {
                 buf.append(separator);
             }
             if (array[i] != null) {
                 buf.append(array[i]);
             }
         }
         return buf.toString();
     }
 
 }
