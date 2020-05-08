 package tk.thundaklap.enchantism;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class Constants {
     /***********************************
      *  Numbers                        *
      ***********************************/
     // Sizes
     public static final int SIZE_INVENTORY = 54;
     public static final int SIZE_HEADER = 18;
     public static final int SIZE_PAGE = SIZE_INVENTORY - SIZE_HEADER;
 
     // Header slots
     public static final int SLOT_PREV_PAGE = 0;
     public static final int SLOT_CURRENT_ITEM = 4;
     public static final int SLOT_UNENCHANT = 6;
     public static final int SLOT_NEXT_PAGE = 8;
     public static final int SLOT_ENCH_TABLE = 13;
 
     // Other numeric constants
     public static final int ENCHANTMENTS_PER_PAGE = 8;
 
     /***********************************
      *  ItemStacks                     *
      ***********************************/
     // Clone & Modify
     public static final ItemStack ITEM_BOOK;
     public static final ItemStack ITEM_ENCH_BOOK;
 
     // Just Use
     public static final ItemStack ITEM_WHITE_WOOL;
     public static final ItemStack ITEM_BLACK_WOOL;
     public static final ItemStack ITEM_ENCH_TABLE;
 
     // Have Meta
     public static final ItemStack ITEM_UNAVAILABLE_ENCHANT;
     public static final ItemStack ITEM_PREV_PAGE;
     public static final ItemStack ITEM_NEXT_PAGE;
     public static final ItemStack ITEM_UNENCHANT;
 
     // Arrays
     public static final ItemStack[] INV_EMPTY_PAGE = new ItemStack[36];
     private static final ItemStack[] INV_PAGE_TEMPLATE = new ItemStack[36]; // getter
     private static final ItemStack[] INV_TOP_ROW_TEMPLATE = new ItemStack[18]; // getter
 
     static {
         ItemMeta meta;
 
         ITEM_BOOK = new ItemStack(Material.BOOK);
         ITEM_ENCH_BOOK = new ItemStack(Material.ENCHANTED_BOOK);
 
         ITEM_WHITE_WOOL = new ItemStack(Material.WOOL, 1, DyeColor.WHITE.getWoolData());
         ITEM_BLACK_WOOL = new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getWoolData());
         ITEM_ENCH_TABLE = new ItemStack(Material.ENCHANTMENT_TABLE);
 
         ITEM_UNAVAILABLE_ENCHANT = new ItemStack(Material.BOOK);
         meta = ITEM_UNAVAILABLE_ENCHANT.getItemMeta();
         meta.setDisplayName(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Unavailable");
         ITEM_UNAVAILABLE_ENCHANT.setItemMeta(meta);
 
         ITEM_PREV_PAGE = new ItemStack(Material.WOOL, 1, DyeColor.RED.getWoolData());
         meta = ITEM_PREV_PAGE.getItemMeta();
         meta.setDisplayName(ChatColor.RED + "Previous Page");
         ITEM_PREV_PAGE.setItemMeta(meta);
 
         ITEM_NEXT_PAGE = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getWoolData());
         meta = ITEM_NEXT_PAGE.getItemMeta();
         meta.setDisplayName(ChatColor.GREEN + "Next Page");
         ITEM_NEXT_PAGE.setItemMeta(meta);
 
         ITEM_UNENCHANT = new ItemStack(Material.BLAZE_POWDER);
         meta = ITEM_UNENCHANT.getItemMeta();
         meta.setDisplayName(ChatColor.RED + "Remove Enchantments");
         ITEM_UNENCHANT.setItemMeta(meta);
 
         for (int i = 0; i < Constants.SIZE_INVENTORY - Constants.SIZE_HEADER; i++) {
             INV_EMPTY_PAGE[i] = ITEM_BLACK_WOOL;
         }
 
         INV_PAGE_TEMPLATE[4] = ITEM_BLACK_WOOL;
         INV_PAGE_TEMPLATE[4+9] = ITEM_BLACK_WOOL;
         INV_PAGE_TEMPLATE[4+9+9] = ITEM_BLACK_WOOL;
         INV_PAGE_TEMPLATE[4+9+9+9] = ITEM_BLACK_WOOL;
 
         for (int i = 0; i < SIZE_HEADER; i++) {
             INV_TOP_ROW_TEMPLATE[i] = ITEM_WHITE_WOOL;
         }
         INV_TOP_ROW_TEMPLATE[SLOT_ENCH_TABLE] = ITEM_ENCH_TABLE;
     }
 
     public static ItemStack[] getPageTemplate() {
         return Arrays.copyOf(INV_PAGE_TEMPLATE, SIZE_PAGE);
     }
 
     public static ItemStack[] getTopRowTemplate() {
         return Arrays.copyOf(INV_TOP_ROW_TEMPLATE, SIZE_HEADER);
     }
 }
