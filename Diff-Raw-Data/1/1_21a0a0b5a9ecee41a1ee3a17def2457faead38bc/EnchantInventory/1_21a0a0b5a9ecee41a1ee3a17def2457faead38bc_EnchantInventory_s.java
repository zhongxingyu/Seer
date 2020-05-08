 package tk.thundaklap.enchantism;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.inventory.ClickType;
 import org.bukkit.event.inventory.InventoryAction;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryDragEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.meta.EnchantmentStorageMeta;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 import static tk.thundaklap.enchantism.Constants.*;
 
 public final class EnchantInventory {
 
     public Player player;
     public BukkitTask updateTask;
     private EnchantPage[] pages;
     private int pageCount = 0;
     private int currentPage = 0;
     private int levelToShow;
     private Inventory inventory;
     private boolean showUnenchant = false;
     private boolean unenchantEnabled;
     private boolean vanillaUIEnabled;
     private Location tableLocation;
 
     
     public EnchantInventory(Player player, Location tableLoc, boolean useBookshelves){
         
         if(useBookshelves){
             int numBookshelves = Utils.getApplicableBookshelves(tableLoc);
             levelToShow = numBookshelves > 15 ? 4 : (numBookshelves / 5) + 1;
         }else{
             levelToShow = 4;
         }
         
         
         tableLocation = tableLoc;
         unenchantEnabled = Enchantism.getInstance().configuration.enableUnenchantButton;
         vanillaUIEnabled = Enchantism.getInstance().configuration.vanillaUiAvailable;
         this.player = player;
         this.inventory = Bukkit.createInventory(player, SIZE_INVENTORY, "Enchant");
         inventory.setMaxStackSize(1);
         slotChange();
         this.player.openInventory(inventory);
     }
 
     public Inventory getInventory() {
         return inventory;
     }
 
     public void updatePlayerInv() {
         boolean isMultiPage = pageCount != 0;
         inventory.setContents((ItemStack[]) ArrayUtils.addAll(topRows(isMultiPage && pageCount != currentPage, isMultiPage && currentPage != 0, showUnenchant && unenchantEnabled), pages[currentPage].getInventory()));
         new DelayUpdateInventory(player).runTask(Enchantism.getInstance());
     }
 
     public void slotChange() {
         
         
         ItemStack change = inventory.getItem(SLOT_CURRENT_ITEM);
         List<Enchantment> applicableEnchantments = Utils.getEnchantments(change);
 
         currentPage = 0;
         
         if (applicableEnchantments.isEmpty()) {
             pageCount = 0;
             pages = new EnchantPage[1];
             pages[0] = new EnchantPage(0);
             pages[0].setEmpty(vanillaUIEnabled);
 
             // allow unenchanting of books
             showUnenchant = change == null ? false : change.getType() == Material.ENCHANTED_BOOK;
 
         } else {
             int numberOfEnchants = applicableEnchantments.size();
             pageCount = (numberOfEnchants - 1) / ENCHANTMENTS_PER_PAGE;
             pages = new EnchantPage[pageCount + 1];
             
             for (int i = 0; i < pages.length; i++) {
                 pages[i] = new EnchantPage(levelToShow);
             }
             
             int currentlyAddingPage = 0;
 
             for (Enchantment enchant : applicableEnchantments) {
                 // Method returns false if the page is full.
                 if (!pages[currentlyAddingPage].addEnchantment(enchant)) {
                     pages[++currentlyAddingPage].addEnchantment(enchant);
                 }
             }
 
             pages[currentlyAddingPage].fill();
             showUnenchant = true;
         }
         updatePlayerInv();
     }
 
     public void inventoryClicked(InventoryClickEvent event) {
         int rawSlot = event.getRawSlot();
         InventoryView view = event.getView();
         assert SIZE_INVENTORY == view.getTopInventory().getSize();
 
         // Default to cancel, uncancel if we want vanilla behavior
         event.setResult(Result.DENY);
         
         updateTask = Bukkit.getScheduler().runTask(Enchantism.getInstance(), new SlotChangeTask(this, inventory.getItem(SLOT_CURRENT_ITEM)));
         
         // Let people drop items
         // Raw slot check since border_left, border_right check only works sometimes
         if(event.getClick() == ClickType.CONTROL_DROP || event.getClick() == ClickType.DROP
                 || event.getClick() == ClickType.WINDOW_BORDER_LEFT || event.getClick() == ClickType.WINDOW_BORDER_RIGHT
                 || event.getRawSlot() == WINDOW_BORDER_RAW_SLOT){
             
             event.setResult(Result.DEFAULT);
             return;
         }
         
         // Let people shift-click in tools
         if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
             if (rawSlot >= SIZE_INVENTORY) {
                 // Swappy swap swap!
                 ItemStack old = view.getItem(SLOT_CURRENT_ITEM);
                 ItemStack newI = view.getItem(rawSlot);
                 ItemStack toPlace = newI;
                 ItemStack replace = null;
 
                 // Only allow in 1 item
                 if (newI.getAmount() > 1) {
                     replace = newI;
                     toPlace = newI.clone();
 
                     replace.setAmount(newI.getAmount() - 1);
                     toPlace.setAmount(1);
                 }
                 view.setItem(SLOT_CURRENT_ITEM, toPlace);
 
                 if (replace != null) {
                     view.setItem(rawSlot, replace);
                     view.getBottomInventory().addItem(old);
                 } else {
                     view.setItem(rawSlot, old);
                 }
                 return;
             }
         }
 
         // Predefined slot behavior
         if (rawSlot == SLOT_PREV_PAGE) {
             if (currentPage != 0 && pageCount > 0) {
                 currentPage--;
                 updatePlayerInv();
                 player.playSound(player.getLocation(), Sound.CLICK, 2F, 1F);
             }
             return;
 
         } else if (rawSlot == SLOT_NEXT_PAGE) {
             if (currentPage != pageCount) {
                 currentPage++;
                 updatePlayerInv();
                 player.playSound(player.getLocation(), Sound.CLICK, 2F, 1F);
             }
             return;
 
         } else if (rawSlot == SLOT_CURRENT_ITEM) {
             event.setResult(Result.DEFAULT);
             return;
 
         } else if (rawSlot == SLOT_UNENCHANT) {
             if (showUnenchant && unenchantEnabled && event.getClick() == ClickType.LEFT) {
                 ItemStack item = inventory.getItem(SLOT_CURRENT_ITEM);
 
                 if (item != null && !item.getType().equals(Material.AIR)) {
                     if (item.getType() == Material.ENCHANTED_BOOK) {
                         
                         EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
                         
                         for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
                             meta.removeStoredEnchant(entry.getKey());
                         }
                         
                         item.setItemMeta(meta);
                         item.setType(Material.BOOK);
                         
                     } else {
                         for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                             item.removeEnchantment(entry.getKey());
                         }
                     }
                     player.playSound(player.getLocation(), Sound.GLASS, 2F, 1F);
                     slotChange();
                 }
             }
             return;
 
         } else if(rawSlot == SLOT_VANILLA_UI) {
             
             if(vanillaUIEnabled && event.getClick() == ClickType.LEFT){
                 player.closeInventory();
                dropItem();
                 player.openEnchanting(tableLocation, false);
             }
             
         } else if (rawSlot >= SIZE_HEADER && rawSlot < SIZE_INVENTORY) {
             EnchantLevelCost enchant = pages[currentPage].enchantAtSlot(rawSlot - SIZE_HEADER);
 
             if (enchant == null || enchant.cost < 0) {
                 return;
             }
 
             if (player.getLevel() < enchant.cost) {
                 player.sendMessage(ChatColor.RED + "You don\'t have enough XP to enchant the item with that enchantment!");
                 player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 2F, 1F);
                 return;
             }
             
             ItemStack item = inventory.getItem(SLOT_CURRENT_ITEM);
             
             for(Map.Entry itemEnchantment : item.getEnchantments().entrySet()){
                 if(enchant.enchant.conflictsWith((Enchantment)itemEnchantment.getKey())){
                     player.sendMessage(ChatColor.RED + "That enchantment would conflict with one of the enchantments already on the tool!");
                     player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 2F, 1F);
                     return;
                 }
             }
 
             player.setLevel(player.getLevel() - enchant.cost);
             player.playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 2F, 1F);
 
 
             if (item.getType() == Material.BOOK) {
                 item.setType(Material.ENCHANTED_BOOK);
                 
             } 
             if(item.getType() == Material.ENCHANTED_BOOK){
 
                 EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                 meta.addStoredEnchant(enchant.enchant, enchant.level, true);
 
                 item.setItemMeta(meta);
                 return;
             }
 
             try {
                 item.addUnsafeEnchantment(enchant.enchant, enchant.level);
             } catch (Exception e) {
                 player.sendMessage(ChatColor.RED + "[Enchantism] Unexpected error. See console for details.");
                 Enchantism.getInstance().getLogger().severe(e.getMessage());
             }
             inventory.setItem(SLOT_CURRENT_ITEM, item);
         } else if (rawSlot >= SIZE_INVENTORY) {
             // Uncancel, unless on blacklist
             if (event.getAction() != InventoryAction.COLLECT_TO_CURSOR && event.getAction() != InventoryAction.CLONE_STACK && event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                 event.setResult(Result.DEFAULT);
             }
         }
     }
 
     public void inventoryDragged(InventoryDragEvent event) {
         if (event.getRawSlots().contains(SLOT_CURRENT_ITEM)) {
             Bukkit.getScheduler().runTask(Enchantism.getInstance(), new SlotChangeTask(this, inventory.getItem(Constants.SLOT_CURRENT_ITEM)));
         }
     }
     
     public void dropItem() {
         
         ItemStack currentItem = inventory.getItem(SLOT_CURRENT_ITEM);
         if (currentItem != null && currentItem.getType() != Material.AIR) {
             tableLocation.getWorld().dropItemNaturally(tableLocation.add(0, 0.75D, 0), currentItem);
         }
         
     }
 
     private ItemStack[] topRows(boolean showNextPage, boolean showPrevPage, boolean showUnenchantButton) {
         ItemStack[] is = Constants.getTopRowTemplate();
 
         is[SLOT_CURRENT_ITEM] = inventory.getItem(SLOT_CURRENT_ITEM);
         if (showPrevPage) {
             is[SLOT_PREV_PAGE] = ITEM_PREV_PAGE;
         }
         if (showUnenchantButton) {
             is[SLOT_UNENCHANT] = ITEM_UNENCHANT;
         }
         if (showNextPage) {
             is[SLOT_NEXT_PAGE] = ITEM_NEXT_PAGE;
         }
         return is;
     }
 }
