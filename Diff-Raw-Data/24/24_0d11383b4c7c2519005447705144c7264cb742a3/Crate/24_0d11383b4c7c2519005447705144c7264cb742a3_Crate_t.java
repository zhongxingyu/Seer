 package to.joe.strangeweapons.meta;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import to.joe.strangeweapons.RandomCollection;
 import to.joe.strangeweapons.StrangeWeapons;
 
 public class Crate {
 
     public static StrangeWeapons plugin;
 
     public static boolean isCrate(ItemStack item) {
         if (!item.getType().equals(Material.CHEST)) {
             return false;
         }
         ItemMeta meta = item.getItemMeta();
        if (!(meta.hasDisplayName() && meta.hasLore())) {
            return false;
        }
         if (!meta.getDisplayName().equals(ChatColor.YELLOW + "Steve Co. Supply Crate")) {
             return false;
         }
         List<String> lore = meta.getLore();
         if (lore.size() != 3) {
             return false;
         }
         if (!lore.get(1).equals(ChatColor.WHITE + "You need a Steve Co. Supply Crate Key to open this.")) {
             return false;
         }
         if (!lore.get(2).equals(ChatColor.WHITE + "You can pick one up at the Steve Co. Store.")) {
             return false;
         }
         return lore.get(0).matches(ChatColor.AQUA + "Crate series #[0-9]+");
     }
 
     private ItemStack item;
     private ItemMeta meta;
 
     public Crate(ItemStack item) {
         this.item = item;
         meta = item.getItemMeta();
     }
 
     public Crate(int series) {
         item = new ItemStack(Material.CHEST);
         meta = item.getItemMeta();
         meta.setDisplayName(ChatColor.YELLOW + "Steve Co. Supply Crate");
         List<String> lore = new ArrayList<String>();
         lore.add(ChatColor.AQUA + "Crate series #" + series);
         lore.add(ChatColor.WHITE + "You need a Steve Co. Supply Crate Key to open this.");
         lore.add(ChatColor.WHITE + "You can pick one up at the Steve Co. Store.");
         meta.setLore(lore);
     }
 
     public ItemStack getItemStack() {
         item.setItemMeta(meta);
         return item;
     }
 
     private int getSeries() {
         Pattern p = Pattern.compile(ChatColor.AQUA + "Crate series #([0-9]+)");
         Matcher m = p.matcher(meta.getLore().get(0));
         m.matches();
         return Integer.parseInt(m.group(1));
     }
 
     public List<String> generateLore() {
         List<String> lore = new ArrayList<String>();
         lore.add(ChatColor.GOLD + "Contents of series " + ChatColor.AQUA + getSeries());
         boolean secret = false;
         ConfigurationSection cs = plugin.getConfig().getConfigurationSection("crates." + getSeries() + ".contents");
         for (String item : cs.getKeys(false)) {
             ConfigurationSection i = cs.getConfigurationSection(item);
             if (i.getBoolean("hidden")) {
                 secret = true;
             } else {
                 if (i.getItemStack("item").getItemMeta().hasDisplayName()) {
                     lore.add(ChatColor.WHITE + i.getItemStack("item").getItemMeta().getDisplayName());
                 } else {
                     lore.add(ChatColor.WHITE + StrangeWeapons.toTitleCase(i.getItemStack("item").getType().toString().toLowerCase().replaceAll("_", " ")));
                 }
             }
         }
         if (secret) {
             lore.add(ChatColor.LIGHT_PURPLE + "or an Exceedingly Rare Special Item!");
         }
         return lore;
     }
 
     private Map<ItemStack, Double> getContents() {
         Map<ItemStack, Double> map = new HashMap<ItemStack, Double>();
         ConfigurationSection cs = plugin.getConfig().getConfigurationSection("crates." + getSeries() + ".contents");
         for (String item : cs.getKeys(false)) {
             ConfigurationSection i = cs.getConfigurationSection(item);
             map.put(i.getItemStack("item"), i.getDouble("weight"));
         }
         return map;
     }
 
     public ItemStack getUncratedItem() {
         RandomCollection<ItemStack> rc = new RandomCollection<ItemStack>(plugin.random);
         for (Entry<ItemStack, Double> i : getContents().entrySet()) {
             rc.add(i.getValue(), i.getKey());
         }
         return rc.next();
     }
 
 }
