 package to.joe.strangeweapons;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class MetaParser {
 
     private static String keyName = ChatColor.YELLOW + "Steve Co. Supply Crate Key";
     private static String nameTagName = ChatColor.YELLOW + "Name Tag";
     private static String descriptionTagName = ChatColor.YELLOW + "Description Tag";
     private static List<String> keyLore = new ArrayList<String>();
     private static List<String> nameTagLore = new ArrayList<String>();
     private static List<String> descriptionTagLore = new ArrayList<String>();
 
     static {
         keyLore.add(ChatColor.WHITE + "Used to open locked supply crates.");
         keyLore.add(ChatColor.GREEN + "This is a limited use item. Uses: 1");
         nameTagLore.add(ChatColor.WHITE + "Changes the name of an item in your inventory");
         nameTagLore.add(ChatColor.GREEN + "This is a limited use item. Uses: 1");
         descriptionTagLore.add(ChatColor.WHITE + "Changes the description of an item in your inventory");
         descriptionTagLore.add(ChatColor.GREEN + "This is a limited use item. Uses: 1");
     }
 
     public static boolean isKey(ItemStack item) {
         ItemMeta meta = item.getItemMeta();
        if (item.getType().equals(Material.BLAZE_ROD) && meta.getDisplayName().equals(keyName) && meta.getLore().equals(keyLore)) {
             return true;
         }
         return false;
     }
 
     public static ItemStack makeKey() {
         ItemStack item = new ItemStack(Material.BLAZE_ROD);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(keyName);
         meta.setLore(keyLore);
         item.setItemMeta(meta);
         return item;
     }
 
     public static boolean isNameTag(ItemStack item) {
         ItemMeta meta = item.getItemMeta();
        if (item.getType().equals(Material.PAPER) && meta.getDisplayName().equals(nameTagName) && meta.getLore().equals(nameTagLore)) {
             return true;
         }
         return false;
     }
 
     public static ItemStack makeNameTag() {
         ItemStack item = new ItemStack(Material.PAPER);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(nameTagName);
         meta.setLore(nameTagLore);
         item.setItemMeta(meta);
         return item;
     }
 
     public static boolean isDescriptionTag(ItemStack item) {
         ItemMeta meta = item.getItemMeta();
        if (item.getType().equals(Material.PAPER) && meta.getDisplayName().equals(descriptionTagName) && meta.getLore().equals(descriptionTagLore)) {
             return true;
         }
         return false;
     }
 
     public static ItemStack makeDescriptionTag() {
         ItemStack item = new ItemStack(Material.PAPER);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(descriptionTagName);
         meta.setLore(descriptionTagLore);
         item.setItemMeta(meta);
         return item;
     }
 
 }
