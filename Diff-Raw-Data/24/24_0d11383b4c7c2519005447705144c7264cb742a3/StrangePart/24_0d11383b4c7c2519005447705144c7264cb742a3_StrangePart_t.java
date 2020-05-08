 package to.joe.strangeweapons.meta;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import to.joe.strangeweapons.Part;
 
 public class StrangePart {
 
     private static ArrayList<String> lore = new ArrayList<String>();
 
     static {
         lore.add(ChatColor.WHITE + "Adding this Strange Part to a Strange-quality weapon");
         lore.add(ChatColor.WHITE + "will enable it to track an additional new statistic.");
         lore.add(ChatColor.GREEN + "This is a limited use item. Uses: 1");
     }
 
     public static boolean isPart(ItemStack item) {
         if (!item.getType().equals(Material.PAPER)) {
             return false;
         }
         ItemMeta meta = item.getItemMeta();
        if (!(meta.hasDisplayName() && meta.hasLore())) {
            return false;
        }
         if (!meta.getDisplayName().matches(ChatColor.YELLOW + "Strange Part: .*")) {
             return false;
         }
         return meta.getLore().equals(lore);
     }
 
     private ItemStack item;
     private ItemMeta meta;
 
     public StrangePart(ItemStack item) {
         this.item = item;
         meta = item.getItemMeta();
     }
 
     public ItemStack getItemStack() {
         item.setItemMeta(meta);
         return item;
     }
 
     public StrangePart(Part part) {
         item = new ItemStack(Material.PAPER);
         meta = item.getItemMeta();
         meta.setDisplayName(ChatColor.YELLOW + "Strange Part: " + part.getName());
         meta.setLore(lore);
     }
 
     public Part getPart() {
         Pattern p = Pattern.compile(ChatColor.YELLOW + "Strange Part: (.*)");
         Matcher m = p.matcher(meta.getDisplayName());
         m.matches();
         for (Part part : Part.values()) {
             if (part.getName().equals(m.group(1))) {
                 return part;
             }
         }
         return null;
     }
 
 }
