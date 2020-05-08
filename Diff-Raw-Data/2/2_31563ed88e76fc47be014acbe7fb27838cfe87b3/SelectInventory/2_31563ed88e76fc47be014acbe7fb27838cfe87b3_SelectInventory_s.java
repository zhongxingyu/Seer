 
 package com.quartercode.quarterbukkit.api.select;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import com.quartercode.quarterbukkit.api.MetaUtil;
 
 /**
  * This class is for simple creating of custom selection {@link Inventory}s.
  * You can define your own titles using {@link ChatColor}s and sort your selections in the {@link Inventory}.
  */
 public abstract class SelectInventory implements Listener {
 
     private final Plugin               plugin;
     private String                     title;
     private InventoryLayouter          layouter     = new LineInventoryLayouter();
     private final List<Selection>      selections   = new ArrayList<Selection>();
 
     private final Map<Player, ViewMap> inventoryMap = new HashMap<Player, ViewMap>();
 
     /**
      * Creates an empty select inventory without a title.
      * 
      * @param plugin The plugin to bind the internal methods on.
      */
     public SelectInventory(final Plugin plugin) {
 
         this.plugin = plugin;
     }
 
     /**
      * Creates an empty select inventory with a title.
      * You can also colorize the title with {@link ChatColor}s.
      * 
      * @param plugin The plugin to bind the internal methods on.
      * @param title The visible title, maybe colored with {@link ChatColor}s.
      */
     public SelectInventory(final Plugin plugin, final String title) {
 
         this.plugin = plugin;
         this.title = title;
     }
 
     /**
      * Creates an empty select inventory with an {@link InventoryLayouter}.
      * 
      * @param plugin The plugin to bind the internal methods on.
      * @param layouter The {@link InventoryLayouter} for layouting the {@link Inventory}.
      */
     public SelectInventory(final Plugin plugin, final InventoryLayouter layouter) {
 
         this.plugin = plugin;
         this.layouter = layouter;
     }
 
     /**
      * Creates an empty select inventory with a title and an {@link InventoryLayouter}.
      * You can also colorize the title with {@link ChatColor}s.
      * 
      * @param plugin The plugin to bind the internal methods on.
      * @param title The visible title, maybe colored with {@link ChatColor}s.
      * @param layouter The {@link InventoryLayouter} for layouting the {@link Inventory}.
      */
     public SelectInventory(final Plugin plugin, final String title, final InventoryLayouter layouter) {
 
         this.plugin = plugin;
         this.title = title;
         this.layouter = layouter;
     }
 
     /**
      * Returns the title of the {@link Inventory}.
      * 
      * @return The title of the {@link Inventory}.
      */
     public String getTitle() {
 
         return title;
     }
 
     /**
      * Sets the title of the {@link Inventory}.
      * 
      * @param title The new title of the {@link Inventory}.
      */
     public void setTitle(final String title) {
 
         this.title = title;
     }
 
     /**
      * Returns the {@link InventoryLayouter}.
      * 
      * @return The {@link InventoryLayouter}.
      */
     public InventoryLayouter getLayouter() {
 
         return layouter;
     }
 
     /**
      * Sets the new {@link InventoryLayouter}.
      * 
      * @param layouter The new {@link InventoryLayouter}.
      */
     public void setLayouter(final InventoryLayouter layouter) {
 
         this.layouter = layouter;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material) {
 
         add(value, new ItemStack(material));
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material} and the amount.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param amount The amount of items.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final int amount) {
 
         add(value, new ItemStack(material, amount));
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material} and the data/damage.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param data The data for non-damageable items and damage for damageable ones.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final short data) {
 
         add(value, new ItemStack(material, 1, data));
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the amount and the data/damage.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param amount The amount of items.
      * @param data The data for non-damageable items and damage for damageable ones.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final int amount, final short data) {
 
         add(value, new ItemStack(material, amount, data));
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link ItemStack} directly.
      * 
      * @param value The information for the option as {@link Object}.
      * @param itemStack The {@link ItemStack}.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final ItemStack itemStack) {
 
         selections.add(new Selection(value, itemStack));
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the name and the descriptions as {@link String}-array.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-array.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final String name, final String... descriptions) {
 
         add(value, new ItemStack(material), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the amount, the name and the descriptions as {@link String}-array.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param amount The amount of items.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-array.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final int amount, final String name, final String... descriptions) {
 
         add(value, new ItemStack(material, amount), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the data/damage, the name and the descriptions as {@link String}-array.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param data The data for non-damageable items and damage for damageable ones.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-array.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final short data, final String name, final String... descriptions) {
 
         add(value, new ItemStack(material, 1, data), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the amount, the data/damage, the name and the descriptions as {@link String}-array.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param amount The amount of items.
      * @param data The data for non-damageable items and damage for damageable ones.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-array.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final int amount, final short data, final String name, final String... descriptions) {
 
         add(value, new ItemStack(material, amount, data), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link ItemStack} directly. Furthemore, it sets the name and the descriptions as {@link String}-array.
      * 
      * @param value The information for the option as {@link Object}.
      * @param itemStack The {@link ItemStack}.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-array.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final ItemStack itemStack, final String name, final String... descriptions) {
 
         MetaUtil.setName(itemStack, name);
         MetaUtil.setDescriptions(itemStack, descriptions);
         add(value, itemStack);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the name and the descriptions as {@link String}-{@link List}.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-{@link List}.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final String name, final List<String> descriptions) {
 
         add(value, new ItemStack(material), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the amount, the name and the descriptions as {@link String}-{@link List}.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param amount The amount of items.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-{@link List}.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final int amount, final String name, final List<String> descriptions) {
 
         add(value, new ItemStack(material, amount), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the data/damage, the name and the descriptions as {@link String}-{@link List}.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param data The data for non-damageable items and damage for damageable ones.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-{@link List}.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final short data, final String name, final List<String> descriptions) {
 
         add(value, new ItemStack(material, 1, data), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link Material}, the amount, the data/damage, the name and the descriptions as {@link String}-{@link List}.
      * 
      * @param value The information for the option as {@link Object}.
      * @param material The {@link Material} for the item.
      * @param amount The amount of items.
      * @param data The data for non-damageable items and damage for damageable ones.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-{@link List}.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final Material material, final int amount, final short data, final String name, final List<String> descriptions) {
 
         add(value, new ItemStack(material, amount, data), name, descriptions);
         return this;
     }
 
     /**
      * Adds a new item option to the inventory and sets the {@link ItemStack} directly. Furthemore, it sets the name and the descriptions as {@link String}-{@link List}.
      * 
      * @param value The information for the option as {@link Object}.
      * @param itemStack The {@link ItemStack}.
      * @param name The name of the item as {@link String}.
      * @param descriptions The descriptions for the item as {@link String}-{@link List}.
      * @return This instance of SelectInventory.
      */
     public SelectInventory add(final Object value, final ItemStack itemStack, final String name, final List<String> descriptions) {
 
         MetaUtil.setName(itemStack, name);
         MetaUtil.setDescriptions(itemStack, descriptions);
         add(value, itemStack);
         return this;
     }
 
     /**
      * Returns if the defined {@link Player} has an open {@link Inventory} of this SelectInventory.
      * 
      * @param player The {@link Player} to check.
      * @return If the defined {@link Player} has an open {@link Inventory} of this SelectInventory.
      */
     public boolean isOpen(final Player player) {
 
         return inventoryMap.containsKey(player);
     }
 
     /**
      * Opens an {@link Inventory} for a defined {@link Player}.
      * 
      * @param player The {@link Player} which gets the {@link Inventory}.
      * @return The opened {@link Inventory}.
      */
     public InventoryView open(final Player player) {
 
         final InventoryLayout layout = layouter.getLayout(this, selections);
 
         int slots = 0;
         for (final List<Selection> column : layout.getLayout()) {
             slots += column.size();
         }
         while (slots % 9 != 0) {
             slots++;
         }
 
         if (slots > 0) {
             if (inventoryMap.isEmpty()) {
                 Bukkit.getPluginManager().registerEvents(this, plugin);
             }
 
             if (isOpen(player)) {
                 close(player);
             }
 
             final Inventory inventory = Bukkit.createInventory(player, slots, title);
             final Map<Integer, Selection> slotMap = new HashMap<Integer, Selection>();
             for (int x = 0; x < layout.getLayout().size(); x++) {
                 for (int y = 0; y < layout.getLayout().get(x).size(); y++) {
                     inventory.setItem(x * 9 + y, layout.get(x, y) == null ? new ItemStack(Material.AIR) : layout.get(x, y).getItemStack());
                     slotMap.put(x * 9 + y, layout.get(x, y));
                 }
             }
 
             final InventoryView inventoryView = player.openInventory(inventory);
             inventoryMap.put(player, new ViewMap(inventory, inventoryView, slotMap));
             return inventoryView;
         } else {
             return null;
         }
     }
 
     /**
      * Closes the {@link Inventory} for a defined {@link Player}.
      * 
      * @return This closed {@link Inventory}.
      */
     public Inventory close(final Player player) {
 
         Inventory inventory = null;
 
         if (isOpen(player)) {
             inventory = inventoryMap.get(player).getInventory();
             player.closeInventory();
             inventoryMap.remove(player);
         }
 
         if (inventoryMap.isEmpty()) {
             HandlerList.unregisterAll(this);
         }
 
         return inventory;
     }
 
     @EventHandler
     public void onInventoryClick(final InventoryClickEvent event) {
 
         if (event.getWhoClicked() instanceof Player && isOpen((Player) event.getWhoClicked()) && event.getView().equals(inventoryMap.get(event.getWhoClicked()).getView())) {
             final Map<Integer, Selection> slotMap = inventoryMap.get(event.getWhoClicked()).getSlotMap();
             if (slotMap.containsKey(event.getSlot())) {
                 onClick(slotMap.get(event.getSlot()), ClickType.getClickType(event.isLeftClick(), event.isRightClick(), event.isShiftClick()), (Player) event.getWhoClicked());
                 return;
             }
         }
     }
 
     /**
      * Gets called if the holder clicks on a registered option.
      * 
      * @param selection The selected {@link Selection} (with the informational value and the graphical {@link ItemStack}).
      * @param clickType The {@link ClickType} of the click.
      * @param player The {@link Player} who selected.
      */
     protected abstract void onClick(Selection selection, ClickType clickType, Player player);
 
     @EventHandler
     public void onInventoryClose(final InventoryCloseEvent event) {
 
        if (event.getView().equals(inventoryMap.get(event.getPlayer()).getView())) {
             close((Player) event.getPlayer());
         }
     }
 
     @EventHandler
     public void onPluginDisable(final PluginDisableEvent event) {
 
         if (event.getPlugin().equals(plugin)) {
             for (final Entry<Player, ViewMap> entry : inventoryMap.entrySet()) {
                 close(entry.getKey());
             }
         }
     }
 
     private class ViewMap {
 
         private final Inventory               inventory;
         private final InventoryView           view;
         private final Map<Integer, Selection> slotMap;
 
         private ViewMap(final Inventory inventory, final InventoryView view, final Map<Integer, Selection> slotMap) {
 
             this.inventory = inventory;
             this.view = view;
             this.slotMap = slotMap;
         }
 
         private Inventory getInventory() {
 
             return inventory;
         }
 
         private InventoryView getView() {
 
             return view;
         }
 
         private Map<Integer, Selection> getSlotMap() {
 
             return slotMap;
         }
 
     }
 
 }
