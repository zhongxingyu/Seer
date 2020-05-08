 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage.moneybags;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import me.xhawk87.Coinage.Coinage;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 
 /**
  *
  * @author XHawk87
  */
 public class MoneyBag implements InventoryHolder {
 
     private String id;
     private Coinage plugin;
     private Inventory inventory;
 
     public MoneyBag(Coinage plugin, String id) {
         this.plugin = plugin;
         this.id = id;
     }
 
     public MoneyBag(Coinage plugin, String id, int size, String title) {
         this(plugin, id);
         inventory = plugin.getServer().createInventory(this, size, title);
     }
 
     @Override
     public Inventory getInventory() {
         return inventory;
     }
 
     public void save(ConfigurationSection data) {
         data.set("size", inventory.getSize());
         data.set("title", inventory.getTitle());
         ItemStack[] contents = inventory.getContents();
         ConfigurationSection contentsData = data.createSection("contents");
         for (int i = 0; i < contents.length; i++) {
             ItemStack coin = contents[i];
             if (coin == null || coin.getTypeId() == 0) {
                 contentsData.set(Integer.toString(i), null);
             } else {
                 contentsData.set(Integer.toString(i), coin);
             }
         }
     }
 
     public void load(ConfigurationSection data) {
         int size = data.getInt("size");
         String title = data.getString("title");
         inventory = plugin.getServer().createInventory(this, size, title);
         ConfigurationSection contentsData = data.getConfigurationSection("contents");
         for (String key : contentsData.getKeys(false)) {
             int slot = Integer.parseInt(key);
            ItemStack coin = data.getItemStack(key);
             inventory.setItem(slot, coin);
         }
     }
 
     public void checkCoins(Inventory out) {
         ItemStack[] contents = inventory.getContents();
         for (int i = 0; i < contents.length; i++) {
             ItemStack coin = contents[i];
             if (coin == null || coin.getTypeId() == 0) {
                 continue;
             }
             if (plugin.getDenominationOfCoin(coin) == null) {
                 inventory.clear(i);
                 out.addItem(coin);
             }
         }
 
         // Update player inventory immediately
         if (out.getHolder() instanceof Player) {
             ((Player) out.getHolder()).updateInventory();
         }
 
         plugin.saveMoneyBag(this);
     }
 
     public String getId() {
         return id;
     }
 
     public static Recipe createMoneyBagType(String title, int size, int itemId, int itemData, String[] shape, Map<Character, ItemStack> ingredients) {
         ItemStack result = new ItemStack(itemId, 1, (short) itemData);
         ItemMeta meta = result.getItemMeta();
         meta.setDisplayName(title);
         String lore = MoneyBag.encodeLore("moneybag," + size) + ChatColor.LIGHT_PURPLE + "Right-click while holding to open";
         List<String> loreStrings = new ArrayList<>();
         loreStrings.add(lore);
         meta.setLore(loreStrings);
         meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
         result.setItemMeta(meta);
         ShapedRecipe recipe = new ShapedRecipe(result);
         recipe.shape(shape);
         for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
             ItemStack material = entry.getValue();
             recipe.setIngredient(entry.getKey(), material.getType(), material.getDurability());
         }
         return recipe;
     }
 
     public static Recipe createMoneyBagType(String title, int size, int itemId, int itemData, List<ItemStack> ingredients) {
         ItemStack result = new ItemStack(itemId, 1, (short) itemData);
         ItemMeta meta = result.getItemMeta();
         meta.setDisplayName(title);
         String lore = MoneyBag.encodeLore("moneybag," + size) + ChatColor.LIGHT_PURPLE + "Right-click while holding to open";
         List<String> loreStrings = new ArrayList<>();
         loreStrings.add(lore);
         meta.setLore(loreStrings);
         meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
         result.setItemMeta(meta);
         ShapelessRecipe recipe = new ShapelessRecipe(result);
         for (ItemStack material : ingredients) {
             recipe.addIngredient(material.getType(), material.getDurability());
         }
         return recipe;
     }
 
     public static Recipe loadMoneyBagType(ConfigurationSection data) {
         String title = data.getString("title");
         int size = data.getInt("size", -1);
         int itemId = data.getInt("item-id", -1);
         int itemData = data.getInt("item-data", -1);
         if (title == null || size == -1 || itemId == -1 || itemData == -1) {
             return null;
         }
         ItemStack result = new ItemStack(itemId, 1, (short) itemData);
         ItemMeta meta = result.getItemMeta();
         meta.setDisplayName(title);
         String lore = MoneyBag.encodeLore("moneybag," + size) + ChatColor.LIGHT_PURPLE + "Right-click while holding to open";
         List<String> loreStrings = new ArrayList<>();
         loreStrings.add(lore);
         meta.setLore(loreStrings);
         meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
         result.setItemMeta(meta);
         if (data.isConfigurationSection("shaped-recipe")) {
             ConfigurationSection shapedSection = data.getConfigurationSection("shaped-recipe");
             ShapedRecipe shapedRecipe = new ShapedRecipe(result);
             List<String> lines = shapedSection.getStringList("shape");
             shapedRecipe.shape(lines.toArray(new String[lines.size()]));
             ConfigurationSection materials = shapedSection.getConfigurationSection("materials");
             for (String key : materials.getKeys(false)) {
                 String[] materialParts = materials.getString(key).split(":");
                 int materialId = Integer.parseInt(materialParts[0]);
                 int materialData = 0;
                 if (materialParts.length == 2) {
                     materialData = Integer.parseInt(materialParts[1]);
                 }
                 Material material = Material.getMaterial(materialId);
                 shapedRecipe.setIngredient(key.charAt(0), material, materialData);
             }
             return shapedRecipe;
         } else if (data.isConfigurationSection("shapeless-recipe")) {
             ConfigurationSection shapelessSection = data.getConfigurationSection("shapeless-recipe");
             ShapelessRecipe shapelessRecipe = new ShapelessRecipe(result);
             List<String> materials = shapelessSection.getStringList("materials");
             for (String materialString : materials) {
                 String[] materialParts = materialString.split(":");
                 int materialId = Integer.parseInt(materialParts[0]);
                 int materialData = 0;
                 if (materialParts.length == 2) {
                     materialData = Integer.parseInt(materialParts[1]);
                 }
                 Material material = Material.getMaterial(materialId);
                 shapelessRecipe.addIngredient(material, materialData);
             }
             return shapelessRecipe;
         } else {
             return null;
         }
     }
 
     public static void saveMoneyBagType(ConfigurationSection data, Recipe recipe) {
         ItemStack result = recipe.getResult();
         ItemMeta meta = result.getItemMeta();
         List<String> loreStrings = meta.getLore();
         String lore = loreStrings.get(0);
         String sizeString = MoneyBag.decodeLore(lore).split(",")[1];
         int size = Integer.parseInt(sizeString);
 
         data.set("title", meta.getDisplayName());
         data.set("size", size);
         data.set("item-id", result.getTypeId());
         data.set("item-data", result.getDurability());
 
         if (recipe instanceof ShapedRecipe) {
             ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
             ConfigurationSection shapedSection = data.createSection("shaped-recipe");
             shapedSection.set("shape", Arrays.asList(shapedRecipe.getShape()));
 
             ConfigurationSection materials = shapedSection.createSection("materials");
             for (Map.Entry<Character, ItemStack> entry : shapedRecipe.getIngredientMap().entrySet()) {
                 String key = entry.getKey().toString();
                 ItemStack material = entry.getValue();
                 int materialId = material.getTypeId();
                 int materialData = material.getDurability();
                 materials.set(key, materialId + ":" + materialData);
             }
         } else if (recipe instanceof ShapelessRecipe) {
             ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
             ConfigurationSection shapelessSection = data.createSection("shapeless-recipe");
             List<String> materialList = new ArrayList<>();
             for (ItemStack material : shapelessRecipe.getIngredientList()) {
                 int materialId = material.getTypeId();
                 int materialData = material.getDurability();
                 materialList.add(materialId + ":" + materialData);
             }
             shapelessSection.set("materials", materialList);
         }
     }
 
     public static String decodeLore(String lore) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < lore.length(); i++) {
             char c = lore.charAt(i);
             if (c == ChatColor.COLOR_CHAR) {
                 i++;
                 char d = lore.charAt(i);
                 if (d == ';') {
                     return sb.toString();
                 } else {
                     sb.append(d);
                 }
             }
         }
         return "";
     }
 
     public static String encodeLore(String data) {
         StringBuilder sb = new StringBuilder();
         for (char c : data.toCharArray()) {
             sb.append(ChatColor.COLOR_CHAR);
             sb.append(c);
         }
         sb.append(ChatColor.COLOR_CHAR);
         sb.append(';');
         return sb.toString();
     }
 }
