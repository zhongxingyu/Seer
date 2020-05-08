 package com.codisimus.plugins.classsets;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 /**
  * @author Codisimus
  */
 public class ClassSets extends JavaPlugin implements Listener {
     private static Logger logger;
     private static BukkitScheduler scheduler;
     private static PluginManager pm;
     private Properties p;
     private boolean canHold = true;
     private String onlyIn;
     private boolean allowOnQuickbar = true;
     private boolean destroy = true;
     private String permissionMsg;
     private String destroyMsg;
     private Properties sets = new Properties();
 
     @Override
     public void onEnable () {
         pm = this.getServer().getPluginManager();
         pm.registerEvents(this, this);
         logger = this.getLogger();
         scheduler = this.getServer().getScheduler();
 
         /* Disable this plugin if PhatLoots is not present */
         if (!pm.isPluginEnabled("PhatLoots")) {
             logger.severe("Please install PhatLoots in order to use this plugin!");
             pm.disablePlugin(this);
             return;
         }
 
         loadSettings();
 
         Properties version = new Properties();
         try {
             version.load(this.getResource("version.properties"));
         } catch (Exception ex) {
         }
         logger.info("ClassSets " + this.getDescription().getVersion()
                 + " (Build " + version.getProperty("Build") + ") is enabled!");
 
         for (Player player: Bukkit.getOnlinePlayers()) {
             updateArmorSet(player);
         }
     }
 
     /**
      * Loads settings from the config.properties file
      */
     public void loadSettings() {
         FileInputStream fis = null;
         try {
             //Copy the file from the jar if it is missing
             File file = this.getDataFolder();
             if (!file.isDirectory()) {
                 file.mkdir();
             }
             file = new File(file.getPath() + "/config.properties");
             if (!file.exists()) {
                 this.saveResource("config.properties", true);
             }
 
             //Load config file
             p = new Properties();
             fis = new FileInputStream(file);
             p.load(fis);
 
             try {
                 canHold = Boolean.parseBoolean(loadValue("CanHoldUnusableItems"));
             } catch (Exception e) {
                 onlyIn = loadValue("CanHoldUnusableItems");
             }
             allowOnQuickbar = Boolean.parseBoolean(loadValue("AllowUnusableItemsOntheQuickbar"));
             destroy = Boolean.parseBoolean(loadValue("DestroyItemsYouCannotUse"));
             permissionMsg = format(loadValue("PermissionMessage"));
             destroyMsg = format(loadValue("DestroyMessage"));
         } catch (Exception missingProp) {
             logger.severe("Failed to load ClassSets " + this.getDescription().getVersion());
             missingProp.printStackTrace();
         } finally {
             try {
                 fis.close();
             } catch (Exception e) {
             }
         }
     }
 
     /**
      * Loads the given key and prints an error if the key is missing
      *
      * @param key The key to be loaded
      * @return The String value of the loaded key
      */
     private String loadValue(String key) {
         if (!p.containsKey(key)) {
             logger.severe("Missing value for " + key + " in config file");
             logger.severe("Please regenerate config file");
         }
         return p.getProperty(key);
     }
 
     /**
      * Adds various Unicode characters and colors to a string
      *
      * @param string The string being formated
      * @return The formatted String
      */
     private static String format(String string) {
         return string.replace("&", "§")
                 .replace("<ae>", "æ").replace("<AE>", "Æ")
                 .replace("<o/>", "ø").replace("<O/>", "Ø")
                 .replace("<a>", "å").replace("<A>", "Å");
     }
 
     /**
      * Updates a Player's armor set when they login
      *
      * @param event The PlayerLoginEvent that occurred
      */
     @EventHandler (priority = EventPriority.MONITOR)
     public void onPlayerLogin(PlayerLoginEvent event) {
         updateArmorSet(event.getPlayer());
     }
 
     /**
      * Prevents Player's from moving unusable items to where they shouldn't be
      *
      * @param event The InventoryClickEvent that occurred
      */
     @EventHandler (priority = EventPriority.LOWEST)
     public void onPlayerClick(InventoryClickEvent event) {
         HumanEntity human = event.getWhoClicked();
         if (!(human instanceof Player)) {
             return;
         }
         Player player = (Player) human;
 
         canHold = canHold(player.getWorld());
         if (canHold && allowOnQuickbar) { //Allow unsusable items on the QuickBar
             //Return if the Player's armor Inventory is not visible
            player.sendMessage(event.getInventory().getType().toString());
             if (event.getInventory().getType() != InventoryType.CRAFTING) {
                 return;
             }
         }
 
         //Retrieve the Item that the Player is attempting to move
         boolean shiftClick = event.isShiftClick();
         ItemStack item = !canHold || shiftClick
                          ? event.getCurrentItem()
                          : event.getCursor();
 
         //Return if the Player is allowed to use the Item
         if (canUseItem(player, item)) {
             //Check if the Player was modifying their armor
             if (event.getSlotType() == SlotType.ARMOR || (shiftClick && isWearable(item))) {
                 updateArmorSet(player);
             }
             return;
         }
 
         //Discover where the Item is being moved to
         if (canHold && !shiftClick) {
             switch (event.getSlotType()) {
             case QUICKBAR:
                 if (allowOnQuickbar) {
                     //The Item may be moved to the QuickBar
                     return;
                 }
                 break;
 
             case ARMOR:
                 //The Item may not be worn as armor
                 break;
 
             default:
                 //The Item may be moved to anywhere else.
                 return;
             }
         }
 
         player.sendMessage(permissionMsg.replace("<set>", getItemSet(item)));
         event.setResult(Event.Result.DENY);
         if (canHold || !shiftClick) {
             player.updateInventory();
         }
     }
 
     /**
      * Prevents Player's from picking up items that they may not hold
      * If a Player can hold the Item but not on their QuickBar then it will be moved to their inner Inventory
      *
      * @param event The PlayerPickupItemEvent that occurred
      */
     @EventHandler (priority = EventPriority.LOWEST)
     public void onPlayerPickup(PlayerPickupItemEvent event) {
         HumanEntity entity = event.getPlayer();
         if (!(entity instanceof Player)) {
             return;
         }
         Player player = (Player) entity;
 
         canHold = canHold(player.getWorld());
         if (canHold && allowOnQuickbar) { //Allow unsusable items on the QuickBar
             return;
         }
 
         //Return if the Player is allowed to use the Item
         Item item = event.getItem();
         ItemStack stack = item.getItemStack();
         if (canUseItem(player, stack)) {
             return;
         }
 
         if (canHold) { //Unusable Items are not allowed on the QuickBar
             PlayerInventory inv = player.getInventory();
             if (inv.firstEmpty() < 9) { //Item is trying to be added to the QuickBar
                 //Prevent the Item from being picked up
                 event.setCancelled(true);
 
                 int firstEmpty = firstEmptyOffQuickbar(inv);
                 /* firstEmpty == -1 if the Inventory is full
                    If the Inventory was full the a PlayerPickupItemEvent would not have been called
                    Therefore we know that if firstEmpty == -1 it is because no items may be sent to the QuickBar */
 
                 if (firstEmpty == -1) {
                     /* Possible Exploit to lag the server
                        The Player will spam the pickup event */
                     player.sendMessage(permissionMsg.replace("<set>", getItemSet(stack)));
                 } else {
                     //Remove the Item from the ground
                     item.remove();
                     //Place the Item in the Player's Inventory
                     inv.setItem(firstEmpty, stack);
                     //Send a mock Item Pickup sound to the Player
                     player.playSound(item.getLocation(), Sound.ITEM_PICKUP, 1, 4);
                 }
             }
         } else {
             //The Player may not pickup the Item
             event.setCancelled(true);
             player.sendMessage(permissionMsg.replace("<set>", getItemSet(stack)));
 
             /* If the Item is not destroyed then the Player will be spammed
                with the permission messages until they step away from the Item */
             if (destroy) {
                 player.sendMessage(destroyMsg);
                 item.remove();
             }
         }
     }
 
     /**
      * Prevents Player's from using items that they may not
      * This Event will not be called as a result of PvP
      *
      * @param event The PlayerInteractEvent that occurred
      */
     @EventHandler (priority = EventPriority.LOWEST)
     public void onPlayerUseItem(PlayerInteractEvent event) {
         //Return if unusable Items are not even allowed on the QuickBar
         if (!allowOnQuickbar) {
             return;
         }
 
         //Return if the Player is not holding an item
         if (!event.hasItem()) {
             return;
         }
 
         //Return if the Player is allowed to use the Item
         Player player = event.getPlayer();
         ItemStack item = event.getItem();
         if (canUseItem(player, item)) {
             return;
         }
 
         player.sendMessage(permissionMsg.replace("<set>", getItemSet(item)));
         event.setCancelled(true);
         moveItemOffQuickbar(player, item);
     }
 
     /**
      * Prevents Player's from using weapons that they may not
      *
      * @param event The EntityDamageByEntityEvent that occurred
      */
     @EventHandler (priority = EventPriority.LOWEST)
     public void onPlayerAttack(EntityDamageByEntityEvent event) {
         //Return if unusable Items are not even allowed on the QuickBar
         if (!allowOnQuickbar) {
             return;
         }
 
         Entity entity = event.getDamager();
         if (!(entity instanceof Player)) {
             return;
         }
         Player player = (Player) entity;
 
         //Return if the Player is allowed to use the Item
         ItemStack item = player.getItemInHand();
         if (canUseItem(player, item)) {
             return;
         }
 
         player.sendMessage(permissionMsg.replace("<set>", getItemSet(item)));
         event.setCancelled(true);
         moveItemOffQuickbar(player, item);
     }
 
     /**
      * Moves an Item from a Player's QuickBar to their inner Inventory
      *
      * @param player The specified Player
      * @param item The Item to be moved
      */
     private static void moveItemOffQuickbar(Player player, ItemStack item) {
         PlayerInventory inv = player.getInventory();
         int firstEmpty = firstEmptyOffQuickbar(inv);
 
         //May be exploited if the Player's Inventory only contains unusable Items
         if (firstEmpty == -1) {
             if (inv.firstEmpty() == -1) { //Inventory is full
                 //Find the first ItemStack that may be moved to the QuickBar
                 ItemStack[] slots = inv.getContents();
                 for (int i = 9; i < 36; i++) {
                     if (canUseItem(player, slots[i])) {
                         //Consider the ItemStack in slot i the first empty slot
                         firstEmpty = i;
                         break;
                     }
                 }
             }
         }
 
         //Swap the two Items
         inv.setItem(inv.getHeldItemSlot(), inv.getItem(firstEmpty));
         inv.setItem(firstEmpty, item);
         player.updateInventory();
     }
 
     /**
      * Returns the first empty slot within a Player's inventory
      * This does not include armor slots or the QuickBar
      * If there are no empty slots then the ItemStack in the first slot is moved to the QuickBar
      *
      * @param inv The Inventory which must may be full
      * @return The slot number which will be between 9 and 35 (inclusive) or -1 if the Inventory is full
      */
     private static int firstEmptyOffQuickbar(PlayerInventory inv) {
         int firstEmpty = inv.firstEmpty();
         if (firstEmpty == -1) { //Inventory is full
             return firstEmpty;
         }
 
         if (firstEmpty >= 9) { //First empty slot is off of the QuickBar
             return firstEmpty;
         }
 
         //See if there is an empty slot off of the QuickBar
         ItemStack[] slots = inv.getContents();
         for (int i = 9; i < 36; i++) {
             if (slots[i] == null || slots[i].getType() == Material.AIR) {
                 //Slot i is empty
                 return i;
             }
         }
         //There was not an empty slot off of the QuickBar
 
         //Return -1 if the Inventory belongs to an NPC
         HumanEntity human = inv.getHolder();
         if (!(human instanceof Player)) {
             return -1;
         }
         Player player = (Player) human;
 
         //Find the first ItemStack that may be moved to the QuickBar
         for (int i = 9; i < 36; i++) {
             if (canUseItem(player, slots[i])) {
                 //Move the ItemStack in slot i to the QuickBar
                 inv.setItem(firstEmpty, slots[i]);
                 //Slot i is now empty
                 return i;
             }
         }
 
         //The Player is not allowed to move any items to the QuickBar
         return -1;
     }
 
     /**
      * Returns whether the given Player has permission to use the specified Item
      *
      * @param player The given Player
      * @param item The ItemStack which may be part of a Set
      * @return True if the Player may use the Item
      */
     public static boolean canUseItem(Player player, ItemStack item) {
         String set = getItemSet(item);
         return set == null || set.isEmpty()
                ? true
                : canUseSet(player, set);
     }
 
     /**
      * Retrieves the Set from the given ItemStack
      *
      * @param item The ItemStack which may be part of a set
      * @return The name of the Set or null if the Item has no Set
      */
     public static String getItemSet(ItemStack item) {
         //Check if the Item is nothing
         if (item == null || item.getType() == Material.AIR) {
             return null;
         }
 
         if (!item.hasItemMeta()) {
             return null;
         }
 
         ItemMeta meta = item.getItemMeta();
         if (!meta.hasLore()) {
             return null;
         }
 
         //Read the last line of the Lore
         //This will eventually be replaced with reading a String object from the ItemMeta
         List<String> lore = meta.getLore();
         String last = lore.get(lore.size() - 1);
         return last.matches("§[0-9a-flno][0-9a-zA-Z]+ Set") //Color SetNameOfLettersAndNumbersOfAnyLength Set
                ? last.substring(2, last.length() - 4)
                : null;
     }
 
     /**
      * Returns whether the given Player has permission to use the specified Set
      *
      * @param player The given Player
      * @param set The specified Set
      * @return True if the Player may use the Set
      */
     public static boolean canUseSet(Player player, String set) {
         return player.hasPermission("set.*") || player.hasPermission("set." + set);
     }
 
 //    /**
 //     * Returns whether the given Player is allowed to hold the specified Item
 //     *
 //     * @param player The given Player
 //     * @param item The specified ItemStack which may be part of a Set
 //     * @return true if the Player may hold the Item
 //     */
 //    public boolean canHoldItem(Player player, ItemStack item) {
 //
 //    }
 
     /**
      * Returns whether a holding unusable items is allowed in the specified World
      *
      * @param world The specified World
      * @return true if holding unusable items is allowed in the specified World
      */
     public boolean canHold(World world) {
         return onlyIn == null
                ? canHold
                : onlyIn.equals(world.getName());
     }
 
     /**
      * Returns whether an item is wearable in an armor slot
      *
      * @param item The specified ItemStack
      * @return true if the item can be worn
      */
     public boolean isWearable(ItemStack item) {
         int id = item.getTypeId();
         if (id < 298) {
             return id == 86;
         } else if (id > 317) {
             return id == 397;
         } else {
             return true;
         }
     }
 
     /**
      * Updates the Set that the Player is wearing
      * This Set information is stored inside the sets HashMap
      * If the Player is not wearing a full set then they will be removed from the Map
      *
      * @param player The Player who may be wearing an armor set
      */
     public void updateArmorSet(final Player player) {
         scheduler.runTaskLater(this, new Runnable() {
                 @Override
                 public void run() {
                     String playerName = player.getName();
                     ItemStack[] armor = player.getInventory().getArmorContents();
                     ItemStack boots = armor[0];
                     String set = getItemSet(boots);
                    if (set == null) {
                        if (sets.containsKey(playerName)) {
                             sets.remove(playerName);
                             ChangeArmorSetEvent event = new ChangeArmorSetEvent(player, null);
                             pm.callEvent(event);
                         }
                         return;
                     }
 
                     for (int i = 1; i < 4; i++) {
                        if (!getItemSet(armor[i]).equals(set)) {
                            if (sets.containsKey(playerName)) {
                                 sets.remove(playerName);
                                 ChangeArmorSetEvent event = new ChangeArmorSetEvent(player, null);
                                 pm.callEvent(event);
                             }
                         }
                     }
 
                    if (!sets.contains(playerName) || !sets.getProperty(playerName).equals(set)) {
                         sets.setProperty(playerName, set);
                         ChangeArmorSetEvent event = new ChangeArmorSetEvent(player, set);
                         pm.callEvent(event);
                     }
                 }
             }, 5L);
     }
 }
