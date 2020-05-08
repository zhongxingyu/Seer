 package minnymin3.zephyrus.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import minnymin3.zephyrus.Zephyrus;
 
 import org.bukkit.ChatColor;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class ItemUtil {
 
 	public Zephyrus plugin;
 
 	public ItemUtil(Zephyrus plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Set the name for the item
 	 * @param item The ItemStack to rename
 	 * @param name The name of the ItemStack
 	 */
 	public void setItemName(ItemStack item, String name) {
 		ItemMeta m = item.getItemMeta();
 		m.setDisplayName(name);
 		item.setItemMeta(m);
 	}
 
 	/**
 	 * Sets a dummy enchantment on the item making it glow
 	 * @param item The ItemStack to add the enchantment effect to
 	 */
 	public void setGlow(ItemStack item) {
 		item.addEnchantment(Zephyrus.sGlow, 1);
 	}
 
 	/**
 	 * Used for custom enchantments and displaying their names
 	 * @param item The ItemStack for the Enchantment
 	 * @param enchant The Enchantment
 	 * @param level The level of the Enchantment
 	 */
 	public void setCustomEnchantment(ItemStack item, Enchantment enchant,
 			int level) {
 		item.addEnchantment(enchant, level);
 		ItemMeta meta = item.getItemMeta();
 		List<String> lore = meta.getLore();
 		try {
 			lore.add(ChatColor.GRAY + enchant.getName() + " " + enchantLevel(level));
 		} catch (Exception e) {
 			lore = new ArrayList<String>();
 			lore.add(ChatColor.GRAY + enchant.getName() + " " + enchantLevel(level));
 		}
 		meta.setLore(lore);
 		item.setItemMeta(meta);
 	}
 
 	/**
 	 * Gets the roman numeral for Enchantments from an integer
 	 * @param level The integer to convert
 	 * @return The equivalent roman numeral
 	 */
 	public String enchantLevel(int level) {
 		switch (level) {
 		case 1:
 			return "I";
 		case 2:
 			return "II";
 		case 3:
 			return "III";
 		case 4:
 			return "IV";
 		case 5:
 			return "V";
 		case 6:
 			return "VI";
 		case 7:
 			return "VII";
 		case 8:
 			return "VIII";
 		case 9:
 			return "IX";
 		case 10:
 			return "X";
 		}
 		return "";
 	}
 
 	/**
 	 * Used for custom items, it sets the item level
 	 * @param i The target ItemStack
 	 * @param level The target level
 	 * @return An ItemStack with the custom level
 	 */
 	public ItemStack setItemLevel(ItemStack i, int level) {
 		ItemMeta m = i.getItemMeta();
 		List<String> l = m.getLore();
 		try {
 			l.set(0, ChatColor.GRAY + "Level: " + level);
 		} catch (NullPointerException e) {
 			l = new ArrayList<String>();
			l.add(0, ChatColor.GRAY + "7Level: " + level);
 		}
 
 		m.setLore(l);
 		i.setItemMeta(m);
 		return i;
 	}
 
 	/**
 	 * Check if the name of the ItemStack is the same as the string provided
 	 * @param i The ItemStack to check
 	 * @param name The name to check
 	 * @return True if the display name of the item is the same as the string
 	 */
 	public boolean checkName(ItemStack i, String name) {
 		try {
 			if (i.getItemMeta().getDisplayName().equalsIgnoreCase(name)) {
 				return true;
 			}
 		} catch (NullPointerException exception) {
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the level of the item (defined in the lore)
 	 * @param i The target ItemStack
 	 * @return The level of the item
 	 */
 	public int getItemLevel(ItemStack i) {
 		ItemMeta m = i.getItemMeta();
 		String data = m.getLore().get(0).replace(ChatColor.GRAY + "Level: ", "");
 		return Integer.parseInt(data);
 	}
 
 	/**
 	 * Gets the tick delay from a level
 	 * @param level A level from an item
 	 * @return The tick delay from the integer. Anything above 10 will return 0
 	 */
 	public static int delayFromLevel(int level) {
 		switch (level) {
 		case 1:
 			return 400;
 		case 2:
 			return 200;
 		case 3:
 			return 100;
 		case 4:
 			return 60;
 		case 5:
 			return 20;
 		case 6:
 			return 400;
 		case 7:
 			return 200;
 		case 8:
 			return 100;
 		case 9:
 			return 20;
 		case 10:
 			return 0;
 		}
 		return 0;
 	}
 }
