 package com.imjake9.server.banks.utils;
 
 import com.imjake9.server.banks.JSBanksConfigurationHandler;
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class JSBankChestInventory {
     
     public static final BlockFace[] cardinals = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
     
     private Location mainChest;
     private BlockFace extension;
     
     /**
      * Gets the serializable id for a single location.
      * 
      * @param loc
      * @return 
      */
     public static String getChestID(Location loc) {
         return loc.getWorld().getName() + "."
                 + loc.getBlockX() + "+" + loc.getBlockY() + "+" + loc.getBlockZ();
     }
     
     /**
      * Gets a location from a full-length chest ID.
      * 
      * @param id
      * @return 
      */
     public static Location fromChestID(String id) {
         String[] wData = id.split(".");
        String[] lData = wData[1].split("\\+");
         Location loc = new Location(
                 Bukkit.getWorld(wData[0]),
                 Integer.parseInt(lData[0]),
                 Integer.parseInt(lData[1]),
                 Integer.parseInt(lData[2]));
         return loc;
     }
     
     /**
      * Takes a single chest and gets the direction of the connected one in a double chest.
      * 
      * Returns null if the chest is not a double chest.
      * 
      * @param chest
      * @return direction
      */
     public static BlockFace getChestExtension(Chest chest) {
         Location loc = chest.getLocation();
         for (BlockFace face : cardinals) {
             Location newLoc = new Location(loc.getWorld(),
                     loc.getBlockX() + face.getModX(),
                     loc.getBlockY() + face.getModY(),
                     loc.getBlockZ() + face.getModZ());
             if (newLoc.getBlock().getType() == Material.CHEST)
                 return face;
         }
         return null;
     }
     
     public static JSBankChestInventory fromInventoryEvent(final InventoryEvent event) {
         if (event.getInventory().getType() != InventoryType.CHEST) return null;
         Chest chest = (event.getInventory().getHolder() instanceof DoubleChest)
                 ? (Chest) ((DoubleChest)event.getInventory().getHolder()).getLeftSide()
                 : (Chest) event.getInventory().getHolder();
         return new JSBankChestInventory(chest);
     }
     
     public static boolean inventoryIsDoubleChest(Inventory inventory) {
         return inventory.getHolder() instanceof DoubleChest;
     }
     
     /**
      * Gets whether the click is in the chest or the player's inventory.
      * 
      * @param event
      * @return 
      */
     public static boolean clickIsInChest(final InventoryClickEvent event) {
         if (inventoryIsDoubleChest(event.getInventory())) {
             return event.getRawSlot() < 54;
         } else {
             return event.getRawSlot() < 27;
         }
     }
     
     public JSBankChestInventory(Location loc, BlockFace ext) {
         mainChest = loc;
         extension = ext;
     }
     
     public JSBankChestInventory(Chest chest) {
         this (chest.getLocation(), getChestExtension(chest));
     }
     
     public Location getLocation() {
         return mainChest;
     }
     
     public BlockFace getExtension() {
         return extension;
     }
     
     public Location getExtensionLocation() {
         return new Location(mainChest.getWorld(),
                 mainChest.getBlockX() + extension.getModX(),
                 mainChest.getBlockY() + extension.getModY(),
                 mainChest.getBlockZ() + extension.getModZ());
     }
     
     public Chest getMainChest() {
         return (Chest) mainChest.getBlock().getState();
     }
     
     public Chest getSecondaryChest() {
         return (extension == null)
                 ? null
                 : (Chest) getExtensionLocation().getBlock().getState();
     }
     
     public void formatInventory(double amount) {
         JSBCurrencyManager.formatChestInventory(getMainChest().getInventory(), amount);
     }
     
     public List<ItemStack> getFullInventory() {
         List<ItemStack> items = new ArrayList<ItemStack>();
         for (ItemStack stack : getMainChest().getBlockInventory()) {
             if (stack != null && stack.getType() != Material.AIR)
                 items.add(stack);
         }
         if (extension != null) {
             for (ItemStack stack : getSecondaryChest().getBlockInventory()) {
                 if (stack != null && stack.getType() != Material.AIR)
                     items.add(stack);
             }
         }
         return items;
     }
     
     public String getID() {
         if (JSBanksConfigurationHandler.getBank(getChestID(mainChest)) != null)
             return getChestID(mainChest);
         else if (extension != null && JSBanksConfigurationHandler.getBank(getChestID(getExtensionLocation())) != null)
             return getChestID(getExtensionLocation());
         else return getChestID(mainChest);
     }
     
     public JSBank getData() {
         JSBank bank = JSBanksConfigurationHandler.getBank(getChestID(mainChest));
         if (bank == null && extension != null)
             bank = JSBanksConfigurationHandler.getBank(getChestID(getExtensionLocation()));
         return bank;
     }
     
 }
